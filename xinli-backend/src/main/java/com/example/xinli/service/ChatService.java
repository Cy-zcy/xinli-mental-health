package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.dto.ChatResponse;
import com.example.xinli.entity.ChatMessage;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.ChatMessageMapper;
import com.example.xinli.mapper.ChatSessionMapper;
import com.example.xinli.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务类
 * 处理聊天会话和消息的业务逻辑
 */
@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DeepSeekApiService deepSeekApiService;

    @Autowired
    private UserMemoryService userMemoryService;
    
    /**
     * 发送消息并获取AI回复
     */
    @Transactional
    public Mono<ChatResponse.SendMessageResponse> sendMessage(Long userId, ChatRequest.SendMessageRequest request) {
        return Mono.fromCallable(() -> {
            // 1. 验证用户
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new RuntimeException("用户不存在或已被禁用");
            }
            
            // 2. 获取或创建会话
            ChatSession session;
            if (request.getSessionId() != null) {
                session = chatSessionMapper.selectById(request.getSessionId());
                if (session == null || !session.getUserId().equals(userId)) {
                    throw new RuntimeException("会话不存在或无权访问");
                }
            } else {
                // 创建新会话
                session = createNewSession(userId, "新的对话");
            }
            
            // 3. 保存用户消息
            ChatMessage userMessage = new ChatMessage();
            userMessage.setSessionId(session.getId());
            userMessage.setUserId(userId);
            userMessage.setRole("user");
            userMessage.setContent(request.getContent());
            userMessage.setStatus(1);
            userMessage.setCreatedAt(LocalDateTime.now());
            userMessage.setUpdatedAt(LocalDateTime.now());
            chatMessageMapper.insert(userMessage);
            
            return new Object[]{session, userMessage};
        })
        .flatMap(data -> {
            ChatSession session = (ChatSession) ((Object[]) data)[0];
            ChatMessage userMessage = (ChatMessage) ((Object[]) data)[1];

            // 4. 获取会话历史消息（注入用户情境 + 长期记忆 + Pseudo-RAG细节检索）
            List<ChatRequest.Message> messages = buildMessageHistory(
                    session.getId(), userId, userMessage.getContent());
            
            // 5. 调用AI API
            return deepSeekApiService.sendChatRequest(messages)
                    .map(aiResponse -> {
                        // 6. 保存AI回复
                        ChatMessage aiMessage = new ChatMessage();
                        aiMessage.setSessionId(session.getId());
                        aiMessage.setUserId(userId);
                        aiMessage.setRole("assistant");
                        aiMessage.setContent(aiResponse.getContent());
                        aiMessage.setTokensUsed(aiResponse.getTotalTokens());
                        aiMessage.setModel(aiResponse.getModel());
                        aiMessage.setStatus(1);
                        aiMessage.setCreatedAt(LocalDateTime.now());
                        aiMessage.setUpdatedAt(LocalDateTime.now());
                        chatMessageMapper.insert(aiMessage);

                        // 7. 更新会话时间
                        session.setUpdatedAt(LocalDateTime.now());
                        chatSessionMapper.updateById(session);

                        // 8. 异步触发长期记忆滚动总结（不阻塞当前对话响应）
                        QueryWrapper<ChatMessage> countWrapper = new QueryWrapper<>();
                        countWrapper.eq("user_id", userId).eq("status", 1);
                        long totalMsgCount = chatMessageMapper.selectCount(countWrapper);
                        userMemoryService.triggerSummaryIfNeeded(userId, totalMsgCount);

                        // 9. 构建响应
                        ChatResponse.SendMessageResponse response = new ChatResponse.SendMessageResponse();
                        response.setSessionId(session.getId());
                        response.setMessageId(aiMessage.getId());
                        response.setUserMessage(userMessage.getContent());
                        response.setAiResponse(aiMessage.getContent());
                        response.setTokensUsed(aiMessage.getTokensUsed());
                        response.setTimestamp(aiMessage.getCreatedAt());

                        return response;
                    });
        })
        .doOnError(error -> logger.error("Error sending message: {}", error.getMessage()));
    }
    
    /**
     * 创建新的聊天会话
     */
    @Transactional
    public ChatSession createSession(Long userId, ChatRequest.CreateSessionRequest request) {
        // 验证用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "新的对话";
        }
        
        ChatSession session = createNewSession(userId, title);
        
        // 如果提供了首条消息，则保存
        if (request.getFirstMessage() != null && !request.getFirstMessage().trim().isEmpty()) {
            ChatMessage firstMessage = new ChatMessage();
            firstMessage.setSessionId(session.getId());
            firstMessage.setUserId(userId);
            firstMessage.setRole("user");
            firstMessage.setContent(request.getFirstMessage().trim());
            firstMessage.setStatus(1);
            firstMessage.setCreatedAt(LocalDateTime.now());
            firstMessage.setUpdatedAt(LocalDateTime.now());
            chatMessageMapper.insert(firstMessage);
        }
        
        return session;
    }
    
    /**
     * 获取用户的会话列表
     */
    public Page<ChatResponse.SessionInfo> getUserSessions(Long userId, int page, int size) {
        Page<ChatSession> pageInfo = new Page<>(page, size);
        QueryWrapper<ChatSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("updated_at");
        
        Page<ChatSession> sessionPage = chatSessionMapper.selectPage(pageInfo, wrapper);
        
        // 转换为DTO
        Page<ChatResponse.SessionInfo> dtoPage = new Page<>();
        dtoPage.setCurrent(sessionPage.getCurrent());
        dtoPage.setSize(sessionPage.getSize());
        dtoPage.setTotal(sessionPage.getTotal());
        dtoPage.setPages(sessionPage.getPages());
        
        List<ChatResponse.SessionInfo> sessionInfos = sessionPage.getRecords().stream()
                .map(this::convertToSessionInfo)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(sessionInfos);
        return dtoPage;
    }
    
    /**
     * 获取聊天历史记录
     */
    public ChatResponse.ChatHistoryResponse getChatHistory(Long userId, Long sessionId, int page, int size) {
        // 验证会话权限
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId) || session.getStatus() != 1) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        
        // 查询消息
        Page<ChatMessage> pageInfo = new Page<>(page, size);
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.eq("status", 1);
        wrapper.orderByAsc("created_at");
        
        Page<ChatMessage> messagePage = chatMessageMapper.selectPage(pageInfo, wrapper);
        
        // 转换为DTO
        List<ChatResponse.MessageHistory> messageHistories = messagePage.getRecords().stream()
                .map(this::convertToMessageHistory)
                .collect(Collectors.toList());
        
        // 计算总token数
        int totalTokens = messagePage.getRecords().stream()
                .mapToInt(msg -> msg.getTokensUsed() != null ? msg.getTokensUsed() : 0)
                .sum();
        
        ChatResponse.ChatHistoryResponse response = new ChatResponse.ChatHistoryResponse();
        response.setSessionId(sessionId);
        response.setSessionTitle(session.getTitle());
        response.setMessages(messageHistories);
        response.setTotalMessages((int) messagePage.getTotal());
        response.setTotalTokens(totalTokens);
        
        return response;
    }
    
    /**
     * 删除会话
     */
    @Transactional
    public boolean deleteSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        
        // 软删除会话
        session.setStatus(0);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(session);
        
        // 软删除相关消息
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        
        for (ChatMessage message : messages) {
            message.setStatus(0);
            message.setUpdatedAt(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
        
        return true;
    }
    
    /**
     * 创建新会话的私有方法
     */
    private ChatSession createNewSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        session.setStatus(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return session;
    }
    
    /**
     * 构建消息历史（用于AI API调用）
     * 按照DeepSeek多轮对话规范构建消息历史，并注入：
     *   - 用户昵称 & 时间感知（云朵人设）
     *   - 长期核心记忆（Core Memory）
     *   - 细节检索片段（Pseudo-RAG，仅当用户触发回忆关键词时追加）
     *
     * @param sessionId   当前会话ID
     * @param userId      用户ID
     * @param userContent 用户本次发送的消息（用于Pseudo-RAG关键词检测）
     */
    private List<ChatRequest.Message> buildMessageHistory(Long sessionId, Long userId, String userContent) {
        List<ChatRequest.Message> messages = new ArrayList<>();

        // Step 1: 获取近期对话（只取最近10轮=20条，记忆系统负责长期总结）
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.eq("status", 1);
        wrapper.orderByAsc("created_at");
        wrapper.last("LIMIT 20");
        List<ChatMessage> historyMessages = chatMessageMapper.selectList(wrapper);

        // Step 2: 获取用户信息（昵称）
        String userName = "朋友";
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getNickname() != null && !user.getNickname().isBlank()) {
                userName = user.getNickname();
            }
        }

        // Step 3: 获取用户的长期核心记忆（第1层记忆）
        String coreMemory = userMemoryService.getUserCoreMemory(userId);

        // Step 4: 检测是否触发Pseudo-RAG细节检索（第2层记忆）
        String recalledDetails = null;
        if (userContent != null && userMemoryService.shouldRecallDetails(userContent)) {
            recalledDetails = userMemoryService.recallRelatedDetails(userId, userContent);
            if (recalledDetails != null) {
                logger.info("Pseudo-RAG triggered for userId {}, recalled details injected.", userId);
            }
        }

        // Step 5: 组装System Message（情境化人设 + 长期记忆 + 可选细节）
        messages.add(deepSeekApiService.createContextualSystemMessage(
                userName, historyMessages.size(), coreMemory, recalledDetails));

        // Step 6: 拼接近期对话历史
        for (ChatMessage msg : historyMessages) {
            ChatRequest.Message message = new ChatRequest.Message();
            message.setRole(msg.getRole());
            message.setContent(msg.getContent());
            messages.add(message);
        }

        logger.info("Built message history for session {}: {} messages (system+{}history), memory={}, rag={}",
                sessionId, messages.size(), historyMessages.size(),
                coreMemory != null ? "yes" : "none",
                recalledDetails != null ? "yes" : "none");

        return messages;
    }


    
    /**
     * 转换为会话信息DTO
     */
    private ChatResponse.SessionInfo convertToSessionInfo(ChatSession session) {
        ChatResponse.SessionInfo info = new ChatResponse.SessionInfo();
        info.setId(session.getId());
        info.setTitle(session.getTitle());
        info.setCreatedAt(session.getCreatedAt());
        info.setUpdatedAt(session.getUpdatedAt());
        
        // 获取消息数量
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", session.getId());
        wrapper.eq("status", 1);
        info.setMessageCount(Math.toIntExact(chatMessageMapper.selectCount(wrapper)));
        
        // 获取最后一条消息预览
        wrapper.orderByDesc("created_at");
        wrapper.last("LIMIT 1");
        List<ChatMessage> lastMessages = chatMessageMapper.selectList(wrapper);
        if (!lastMessages.isEmpty()) {
            String content = lastMessages.get(0).getContent();
            info.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        }
        
        return info;
    }
    
    /**
     * 转换为消息历史DTO
     */
    private ChatResponse.MessageHistory convertToMessageHistory(ChatMessage message) {
        ChatResponse.MessageHistory history = new ChatResponse.MessageHistory();
        history.setId(message.getId());
        history.setRole(message.getRole());
        history.setContent(message.getContent());
        history.setTokensUsed(message.getTokensUsed());
        history.setCreatedAt(message.getCreatedAt());
        return history;
    }

    /**
     * 测试AI连接
     */
    public Mono<Boolean> testAiConnection() {
        return deepSeekApiService.testConnection();
    }

    /**
     * 管理员端：获取聊天会话分页列表
     */
    public Page<ChatSession> getChatSessionPage(Page<ChatSession> page, QueryWrapper<ChatSession> wrapper) {
        return chatSessionMapper.selectPage(page, wrapper);
    }

    /**
     * 管理员端：根据ID获取聊天会话
     */
    public ChatSession getChatSessionById(Long sessionId) {
        return chatSessionMapper.selectById(sessionId);
    }

    /**
     * 管理员端：获取会话的消息数量
     */
    public int getMessageCountBySessionId(Long sessionId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        return Math.toIntExact(chatMessageMapper.selectCount(wrapper));
    }

    /**
     * 管理员端：获取会话的最后活跃时间
     */
    public LocalDateTime getLastActiveTime(Long sessionId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.orderByDesc("created_at");
        wrapper.last("LIMIT 1");

        ChatMessage lastMessage = chatMessageMapper.selectOne(wrapper);
        return lastMessage != null ? lastMessage.getCreatedAt() : null;
    }

    /**
     * 管理员端：根据会话ID获取所有消息
     */
    public List<ChatMessage> getMessagesBySessionId(Long sessionId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.orderByAsc("created_at");
        return chatMessageMapper.selectList(wrapper);
    }

    /**
     * 管理员端：删除聊天会话
     */
    @Transactional
    public boolean deleteChatSession(Long sessionId) {
        try {
            // 先删除会话中的所有消息
            QueryWrapper<ChatMessage> messageWrapper = new QueryWrapper<>();
            messageWrapper.eq("session_id", sessionId);
            chatMessageMapper.delete(messageWrapper);

            // 再删除会话
            int result = chatSessionMapper.deleteById(sessionId);
            return result > 0;
        } catch (Exception e) {
            logger.error("删除聊天会话失败: sessionId={}", sessionId, e);
            return false;
        }
    }
    /**
     * 管理员端：获取聊天统计数据
     */
    public java.util.Map<String, Object> getAdminChatStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 1. 总会话数
        QueryWrapper<ChatSession> sessionWrapper = new QueryWrapper<>();
        sessionWrapper.eq("status", 1);
        long totalSessions = chatSessionMapper.selectCount(sessionWrapper);
        stats.put("totalSessions", totalSessions);

        // 2. 总消息数
        QueryWrapper<ChatMessage> msgWrapper = new QueryWrapper<>();
        msgWrapper.eq("status", 1);
        long totalMessages = chatMessageMapper.selectCount(msgWrapper);
        stats.put("totalMessages", totalMessages);

        // 3. 总Token使用量
        QueryWrapper<ChatMessage> tokenWrapper = new QueryWrapper<>();
        tokenWrapper.eq("status", 1).isNotNull("tokens_used");
        tokenWrapper.select("IFNULL(SUM(tokens_used), 0) as totalTokens");
        java.util.Map<String, Object> tokenResult = chatMessageMapper.selectMaps(tokenWrapper).stream().findFirst().orElse(null);
        long totalTokensUsed = tokenResult != null && tokenResult.get("totalTokens") != null ? 
                Long.parseLong(tokenResult.get("totalTokens").toString()) : 0L;
        stats.put("totalTokensUsed", totalTokensUsed);

        // 4. 活跃用户数 (有过对话记录的用户数)
        QueryWrapper<ChatSession> activeUserWrapper = new QueryWrapper<>();
        activeUserWrapper.select("COUNT(DISTINCT user_id) as activeUsers").eq("status", 1);
        java.util.Map<String, Object> userResult = chatSessionMapper.selectMaps(activeUserWrapper).stream().findFirst().orElse(null);
        long activeUsers = userResult != null && userResult.get("activeUsers") != null ? 
                Long.parseLong(userResult.get("activeUsers").toString()) : 0L;
        stats.put("activeUsers", activeUsers);

        // 5. 今日消息数
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        QueryWrapper<ChatMessage> todayMsgWrapper = new QueryWrapper<>();
        todayMsgWrapper.eq("status", 1).ge("created_at", startOfDay);
        long todayMessages = chatMessageMapper.selectCount(todayMsgWrapper);
        stats.put("todayMessages", todayMessages);

        return stats;
    }
}
