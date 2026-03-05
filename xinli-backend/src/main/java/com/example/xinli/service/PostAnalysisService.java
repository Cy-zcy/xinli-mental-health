package com.example.xinli.service;

import com.example.xinli.dto.ChatRequest;
import com.example.xinli.entity.ForumPostAnalysis;
import com.example.xinli.entity.UserNotification;
import com.example.xinli.mapper.ForumPostAnalysisMapper;
import com.example.xinli.mapper.UserNotificationMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛帖子情感分析服务
 *
 * 在用户发帖后，异步调用 DeepSeek 进行情感极性分析和危机风险识别。
 * 流程：
 * 1. 构造分析 Prompt，要求 AI 返回 JSON 格式的分析结果
 * 2. 解析结果，落库至 forum_post_analysis
 * 3. 若 riskLevel >= 2（高危），向用户发送系统通知，并联动健康分扣分
 */
@Service
public class PostAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PostAnalysisService.class);

    @Autowired
    private DeepSeekApiService deepSeekApiService;

    @Autowired
    private ForumPostAnalysisMapper analysisMapper;

    @Autowired
    private UserNotificationMapper notificationMapper;

    @Autowired
    private UserScoreService userScoreService;

    @Autowired
    private ObjectMapper objectMapper;

    /** 危机干预热线引导语（发送给高危用户的通知内容） */
    private static final String CRISIS_MSG =
            "你好，系统注意到你最近发布的内容中可能包含一些痛苦的情绪。我们非常关心你的状态。\n\n" +
            "如果你现在感到难以承受，请随时联系以下心理危机援助热线：\n" +
            "• 全国：400-161-9995\n" +
            "• 北京：010-82951332\n" +
            "• 上海：021-12320-5\n\n" +
            "你并不孤单，我们在这里陪着你。💙";

    /**
     * 异步分析帖子情感
     * 由 UserForumService.createPost 在帖子创建成功后调用
     *
     * @param postId  帖子ID
     * @param userId  发帖用户ID
     * @param title   帖子标题
     * @param content 帖子正文
     */
    @Async
    public void analyzePostAsync(Long postId, Long userId, String title, String content) {
        try {
            log.info("开始对帖子 {} (userId={}) 进行情感分析", postId, userId);

            // 1. 构建分析 Prompt
            List<ChatRequest.Message> messages = buildAnalysisMessages(title, content);

            // 2. 调用 DeepSeek API（复用现有服务）
            String aiResponseText = deepSeekApiService.sendChatRequest(messages)
                    .map(resp -> resp.getContent())
                    .block();

            if (aiResponseText == null || aiResponseText.isBlank()) {
                log.warn("帖子 {} 的情感分析 AI 返回为空，跳过", postId);
                return;
            }

            // 3. 解析 AI 返回的 JSON
            ForumPostAnalysis analysis = parseAnalysisResult(aiResponseText, postId, userId);
            if (analysis == null) {
                log.warn("帖子 {} 分析结果解析失败，原始内容: {}", postId, aiResponseText);
                return;
            }
            analysisMapper.insert(analysis);
            log.info("帖子 {} 分析完成: 情绪={}, 风险等级={}", postId, analysis.getEmotionLabel(), analysis.getRiskLevel());

            // 4. 高危处理
            if (analysis.getRiskLevel() != null && analysis.getRiskLevel() >= 2) {
                handleCrisis(userId, analysis);
            }

        } catch (Exception e) {
            log.error("帖子 {} 情感分析失败: {}", postId, e.getMessage(), e);
        }
    }

    /**
     * 构建情感分析消息列表
     */
    private List<ChatRequest.Message> buildAnalysisMessages(String title, String content) {
        List<ChatRequest.Message> messages = new ArrayList<>();

        ChatRequest.Message system = new ChatRequest.Message();
        system.setRole("system");
        system.setContent(
                "你是一个专业的心理情感分析助手。你的任务是分析用户发布的论坛帖子内容，" +
                "识别情绪状态和心理健康风险程度。\n\n" +
                "请严格以 JSON 格式返回分析结果，不要有任何多余内容，格式如下：\n" +
                "{\n" +
                "  \"emotionLabel\": \"主情绪标签（只能是：焦虑/抑郁/愤怒/孤独/自责/绝望/平静/积极/其他 中的一个）\",\n" +
                "  \"emotionTags\": \"细分标签，以#开头用逗号分隔，如 #学业压力,#失眠,#人际关系（至多3个）\",\n" +
                "  \"riskLevel\": 0,\n" +
                "  \"riskReason\": \"高危原因说明（riskLevel>=2时必填，否则为null）\",\n" +
                "  \"aiSummary\": \"一句话概括这篇帖子表达的核心情绪（不超过50字）\"\n" +
                "}\n\n" +
                "风险等级说明：\n" +
                "- 0：正常，内容积极或普通倾诉，无明显风险\n" +
                "- 1：轻度关注，存在明显的负面情绪但无危险倾向\n" +
                "- 2：高危，内容中包含自杀/自伤/轻生/不想活/严重自我伤害等相关表述\n\n" +
                "**重要**：对于风险等级的判断务必严谨，不要过度放大，也不要忽视真实危险信号。"
        );
        messages.add(system);

        ChatRequest.Message user = new ChatRequest.Message();
        user.setRole("user");
        user.setContent("请分析以下帖子：\n\n【标题】" + title + "\n\n【内容】" + content);
        messages.add(user);

        return messages;
    }

    /**
     * 解析 AI 返回的 JSON 分析结果
     */
    private ForumPostAnalysis parseAnalysisResult(String aiText, Long postId, Long userId) {
        try {
            // 提取 JSON 块（防止 AI 在 JSON 外追加说明文字）
            int start = aiText.indexOf('{');
            int end = aiText.lastIndexOf('}');
            if (start < 0 || end < 0) return null;
            String json = aiText.substring(start, end + 1);

            JsonNode node = objectMapper.readTree(json);

            ForumPostAnalysis analysis = new ForumPostAnalysis();
            analysis.setPostId(postId);
            analysis.setUserId(userId);
            analysis.setEmotionLabel(node.path("emotionLabel").asText(null));
            analysis.setEmotionTags(node.path("emotionTags").asText(null));
            analysis.setRiskLevel(node.path("riskLevel").asInt(0));
            analysis.setRiskReason(node.path("riskReason").isNull() ? null : node.path("riskReason").asText(null));
            analysis.setAiSummary(node.path("aiSummary").asText(null));
            analysis.setIsAlerted(0);
            analysis.setCreatedAt(LocalDateTime.now());
            return analysis;

        } catch (Exception e) {
            log.error("解析情感分析 JSON 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 高危处理：发送危机干预通知 + 联动健康分
     */
    private void handleCrisis(Long userId, ForumPostAnalysis analysis) {
        try {
            // 1. 发送系统通知
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setTitle("💙 我们注意到你了——心理支持服务");
            notification.setContent(CRISIS_MSG);
            notification.setType("CRISIS");
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);

            // 2. 标记为已通知
            analysis.setIsAlerted(1);
            analysisMapper.updateById(analysis);

            // 3. 联动健康分（高危扣5分，原因透明记录）
            String reason = "论坛帖子检测到高危情绪内容：" +
                    (analysis.getRiskReason() != null ? analysis.getRiskReason() : "包含极端情绪表述");
            userScoreService.changeScore(userId, -5, reason, "FORUM_CRISIS");

            log.warn("高危帖子处理完成: userId={}, postId={}, 已发送通知并扣分", userId, analysis.getPostId());

        } catch (Exception e) {
            log.error("高危处理失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}
