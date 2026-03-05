package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.entity.ChatMessage;
import com.example.xinli.entity.UserChatMemory;
import com.example.xinli.mapper.ChatMessageMapper;
import com.example.xinli.mapper.UserChatMemoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户 AI 长期记忆服务
 *
 * 实现「混合记忆架构（Hybrid Memory Architecture）」：
 *  - 第1层 核心记忆（Core Memory）：每用户保留1条滚动摘要，始终注入 System Prompt
 *  - 第2层 细节检索（Pseudo-RAG）：用户提及"以前/记得"等词时，从历史记录模糊匹配
 */
@Service
public class UserMemoryService {

    private static final Logger log = LoggerFactory.getLogger(UserMemoryService.class);

    /** 触发滚动总结所需的"新未处理消息阈值" */
    private static final int SUMMARY_TRIGGER_THRESHOLD = 15;

    /** 核心记忆的字数上限（prompt 里的 max_tokens 描述，大约 200 汉字） */
    private static final int CORE_MEMORY_MAX_CHARS = 400;

    /** 细节检索返回的最大消息条数 */
    private static final int MAX_RECALLED_MESSAGES = 4;

    /**
     * 触发用户"试图回忆过去"行为的关键词列表
     * 后端检测到这些词时，主动追加历史细节检索
     */
    private static final List<String> RECALL_TRIGGER_WORDS = Arrays.asList(
            "还记得", "你记得", "记得吗", "之前说", "以前说", "上次说",
            "之前提", "我跟你说过", "上次提", "以前聊", "之前聊",
            "那次", "之前那", "以前那", "记不记得"
    );

    @Autowired
    private UserChatMemoryMapper userChatMemoryMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private DeepSeekApiService deepSeekApiService;

    // =====================================================================
    //  第1层：核心记忆（Core Memory）的读取
    // =====================================================================

    /**
     * 获取用户的核心记忆文本，用于注入 System Prompt
     * @param userId 用户ID
     * @return 记忆文本（可能为 null，表示尚无记忆）
     */
    public String getUserCoreMemory(Long userId) {
        UserChatMemory memory = getUserMemory(userId);
        return (memory != null) ? memory.getCoreMemory() : null;
    }

    // =====================================================================
    //  第2层：细节检索（Pseudo-RAG）—— 关键词模糊匹配
    // =====================================================================

    /**
     * 检测用户当前发送的消息是否包含"回忆触发词"
     * @param userMessage 用户输入的消息文本
     * @return true = 需要追加历史细节检索
     */
    public boolean shouldRecallDetails(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return false;
        return RECALL_TRIGGER_WORDS.stream().anyMatch(userMessage::contains);
    }

    /**
     * 从历史消息中模糊匹配与用户输入相关的内容片段（Pseudo-RAG）
     * 提取用户消息中的关键名词，在 chat_message 表里做 LIKE 搜索，返回最相关的几条原文
     *
     * @param userId      用户ID（限制仅搜该用户的数据）
     * @param userMessage 用户当前的消息
     * @return 拼接好的"辅助回忆档案"文本，可以直接追加进 Prompt
     */
    public String recallRelatedDetails(Long userId, String userMessage) {
        try {
            // 简单关键词提取：去掉触发词，剩余的短句作为搜索词
            // 例：用户说"你还记得我说的那家咖啡店叫什么" → 提取"咖啡店"
            String searchKeyword = extractSearchKeyword(userMessage);
            if (searchKeyword == null || searchKeyword.length() < 2) {
                return null;
            }

            // 在 chat_message 表里对该用户的历史记录做模糊搜索
            QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                    .eq("status", 1)
                    .eq("role", "user")           // 只搜用户说过的话（不搜AI回复）
                    .like("content", searchKeyword)
                    .orderByDesc("created_at")
                    .last("LIMIT " + MAX_RECALLED_MESSAGES);

            List<ChatMessage> recalled = chatMessageMapper.selectList(wrapper);

            if (recalled.isEmpty()) return null;

            // 拼接成辅助档案文本
            StringBuilder sb = new StringBuilder();
            sb.append("【辅助回忆档案 - 以下是用户过往提到的相关内容，请借此作出更有温度的回应】\n");
            recalled.forEach(msg -> {
                String preview = msg.getContent().length() > 80
                        ? msg.getContent().substring(0, 80) + "…"
                        : msg.getContent();
                sb.append("- ").append(preview).append("\n");
            });

            log.info("Pseudo-RAG: 为用户 {} 检索到 {} 条相关历史，关键词:「{}」",
                    userId, recalled.size(), searchKeyword);
            return sb.toString();

        } catch (Exception e) {
            log.warn("Pseudo-RAG 检索失败，跳过细节注入: {}", e.getMessage());
            return null;
        }
    }

    // =====================================================================
    //  滚动总结异步触发
    // =====================================================================

    /**
     * 在用户完成一次消息交互后，判断是否需要触发滚动记忆总结
     * 此方法由 ChatService 在 AI 回复完成后异步调用，不阻塞正常对话响应
     *
     * @param userId         用户ID
     * @param currentMsgCount 当前该用户在所有会话中的历史消息总数
     */
    @Async
    public void triggerSummaryIfNeeded(Long userId, long currentMsgCount) {
        try {
            UserChatMemory memory = getUserMemory(userId);
            int alreadyProcessed = (memory != null && memory.getProcessedMsgCount() != null)
                    ? memory.getProcessedMsgCount() : 0;

            // 未处理的新消息 = 当前总数 - 已经被纳入摘要的条数
            long unprocessed = currentMsgCount - alreadyProcessed;

            if (unprocessed < SUMMARY_TRIGGER_THRESHOLD) {
                return; // 未达到触发阈值，跳过
            }

            log.info("触发记忆滚动总结：用户 {}，新增未处理消息 {} 条", userId, unprocessed);

            // 取最近 SUMMARY_TRIGGER_THRESHOLD 条用户消息用于总结
            QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                    .eq("status", 1)
                    .orderByDesc("created_at")
                    .last("LIMIT 30");
            List<ChatMessage> recentMessages = chatMessageMapper.selectList(wrapper);

            if (recentMessages.isEmpty()) return;

            // 构建给 DeepSeek 的"总结请求"消息列表
            String oldMemory = (memory != null && memory.getCoreMemory() != null)
                    ? memory.getCoreMemory() : "（暂无之前的记忆）";

            String recentChat = recentMessages.stream()
                    .map(msg -> (msg.getRole().equals("user") ? "用户：" : "云朵：") + msg.getContent())
                    .collect(Collectors.joining("\n"));

            String summaryPrompt = String.format("""
                    你是一个专注于用户情绪画像的分析专家。
                    请基于以下信息，生成一段对用户的「核心记忆描述」，使用第二人称"你对该用户的了解是："来写。
                    要求：
                    1. 保留用户最关键的身份信息（职业/学业/生活状态）
                    2. 保留用户最核心的情绪困扰与模式
                    3. 记录哪种对话方式对TA最有效
                    4. 包含最近对话的转变或结果
                    5. 总字数严格控制在200字以内，纯文字，无标题无列表

                    【已有的旧记忆】：
                    %s

                    【本次新增的对话内容】：
                    %s

                    请直接输出新的核心记忆描述，无需任何说明。
                    """, oldMemory, recentChat);

            List<ChatRequest.Message> summaryMessages = List.of(
                    buildMsg("system", "你是用户情绪与状态的专业分析助手，专注于提炼用户的关键心理状态。"),
                    buildMsg("user", summaryPrompt)
            );

            // 调用 DeepSeek 生成新摘要（同步，该方法本身已是异步线程）
            String newMemory = deepSeekApiService.sendChatRequest(summaryMessages).block()
                    .getContent();

            // 截断，确保不超长
            if (newMemory != null && newMemory.length() > CORE_MEMORY_MAX_CHARS) {
                newMemory = newMemory.substring(0, CORE_MEMORY_MAX_CHARS);
            }

            // 覆盖更新数据库（每用户只有1条记录）
            saveOrUpdateMemory(userId, newMemory, (int) currentMsgCount, memory);
            log.info("记忆滚动总结完成：用户 {}，新记忆长度 {} 字", userId,
                    newMemory != null ? newMemory.length() : 0);

        } catch (Exception e) {
            log.error("记忆滚动总结失败，用户 {}：{}", userId, e.getMessage());
        }
    }

    // =====================================================================
    //  内部工具方法
    // =====================================================================

    private UserChatMemory getUserMemory(Long userId) {
        QueryWrapper<UserChatMemory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return userChatMemoryMapper.selectOne(wrapper);
    }

    private void saveOrUpdateMemory(Long userId, String newMemory,
                                    int processedCount, UserChatMemory existing) {
        if (existing == null) {
            UserChatMemory record = new UserChatMemory();
            record.setUserId(userId);
            record.setCoreMemory(newMemory);
            record.setProcessedMsgCount(processedCount);
            record.setSessionCount(1);
            record.setUpdatedAt(LocalDateTime.now());
            userChatMemoryMapper.insert(record);
        } else {
            existing.setCoreMemory(newMemory);
            existing.setProcessedMsgCount(processedCount);
            existing.setSessionCount(existing.getSessionCount() + 1);
            existing.setUpdatedAt(LocalDateTime.now());
            userChatMemoryMapper.updateById(existing);
        }
    }

    /**
     * 简易关键词提取：去掉触发词和停用词，保留剩余的有意义短语
     * 这是 Pseudo-RAG 检索的核心，用于 MySQL LIKE 查询
     */
    private String extractSearchKeyword(String userMessage) {
        // 先去掉触发词
        String cleaned = userMessage;
        for (String trigger : RECALL_TRIGGER_WORDS) {
            cleaned = cleaned.replace(trigger, "");
        }

        // 简单的停用词过滤
        List<String> stopWords = Arrays.asList(
                "我", "你", "他", "她", "的", "吗", "是", "有", "在",
                "了", "和", "就", "都", "也", "还", "这", "那", "不",
                "吧", "呢", "啊", "哦", "嗯", "什么", "哪里", "怎么", "为什么"
        );
        for (String stop : stopWords) {
            cleaned = cleaned.replace(stop, "");
        }

        // 移除标点
        cleaned = cleaned.replaceAll("[，。！？、\\s]", "").trim();

        // 如果剩余内容很短（不到2字）则放弃检索
        return cleaned.length() >= 2 ? cleaned : null;
    }

    private ChatRequest.Message buildMsg(String role, String content) {
        ChatRequest.Message msg = new ChatRequest.Message();
        msg.setRole(role);
        msg.setContent(content);
        return msg;
    }
}
