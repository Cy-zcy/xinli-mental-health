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
            
            // 4. 获取会话历史消息
            List<ChatRequest.Message> messages = buildMessageHistory(session.getId());
            
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
                        
                        // 8. 构建响应
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
     * 按照DeepSeek多轮对话规范构建消息历史
     */
    private List<ChatRequest.Message> buildMessageHistory(Long sessionId) {
        List<ChatRequest.Message> messages = new ArrayList<>();

        // 1. 添加系统消息（必须在最前面）
        messages.add(deepSeekApiService.createSystemMessage());

        // 2. 获取历史消息（限制数量以控制token使用）
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.eq("status", 1);
        wrapper.orderByAsc("created_at"); // 按时间正序获取，保持对话顺序
        wrapper.last("LIMIT 30"); // 限制最近30条消息（15轮对话）

        List<ChatMessage> historyMessages = chatMessageMapper.selectList(wrapper);

        // 3. 转换为API消息格式，确保user和assistant消息交替出现
        for (ChatMessage msg : historyMessages) {
            ChatRequest.Message message = new ChatRequest.Message();
            message.setRole(msg.getRole());
            message.setContent(msg.getContent());
            messages.add(message);
        }

        logger.info("Built message history for session {}: {} messages (including system message)",
                sessionId, messages.size());

        // 4. 打印消息历史用于调试
        for (int i = 0; i < messages.size(); i++) {
            ChatRequest.Message msg = messages.get(i);
            logger.debug("Message {}: role={}, content={}", i, msg.getRole(),
                    msg.getContent().length() > 100 ? msg.getContent().substring(0, 100) + "..." : msg.getContent());
        }

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
}
