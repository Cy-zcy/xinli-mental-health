package com.example.xinli.service;

import com.example.xinli.config.DeepSeekProperties;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.dto.ChatResponse;
import com.example.xinli.entity.AiCharacter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API服务类
 * 负责与DeepSeek API进行通信
 */
@Service
public class DeepSeekApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekApiService.class);
    
    @Autowired
    @Qualifier("deepSeekWebClient")
    private WebClient webClient;
    
    @Autowired
    private DeepSeekProperties deepSeekProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送聊天请求到DeepSeek API
     * @param messages 消息列表
     * @return AI回复
     */
    public Mono<ChatResponse> sendChatRequest(List<ChatRequest.Message> messages) {
        // 构建请求体 - 转换消息格式为DeepSeek API格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", convertToApiMessages(messages));
        requestBody.put("temperature", deepSeekProperties.getTemperature());
        requestBody.put("max_tokens", deepSeekProperties.getMaxTokens());
        requestBody.put("stream", false); // 暂时不使用流式响应

        logger.info("Sending chat request to DeepSeek API with {} messages", messages.size());
        logger.info("Request body: {}", requestBody);

        return webClient
                .post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(deepSeekProperties.getTimeout()))
                .retryWhen(Retry.backoff(deepSeekProperties.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .map(this::parseResponse)
                .doOnSuccess(response -> logger.info("Successfully received response from DeepSeek API"))
                .doOnError(error -> logger.error("Error calling DeepSeek API: {}", error.getMessage()));
    }

    /**
     * 转换消息格式为DeepSeek API格式
     * 确保多轮对话的上下文正确传递
     */
    private List<Map<String, String>> convertToApiMessages(List<ChatRequest.Message> messages) {
        List<Map<String, String>> apiMessages = new ArrayList<>();

        for (ChatRequest.Message message : messages) {
            Map<String, String> apiMessage = new HashMap<>();
            apiMessage.put("role", message.getRole());
            apiMessage.put("content", message.getContent());
            apiMessages.add(apiMessage);
        }

        logger.info("Converted {} messages to API format", apiMessages.size());
        return apiMessages;
    }

    /**
     * 创建基础系统提示消息（无用户信息时使用）
     */
    public ChatRequest.Message createSystemMessage() {
        return createContextualSystemMessage("朋友", 0, null, null);
    }

    /**
     * 向后兼容的2参数版本
     */
    public ChatRequest.Message createContextualSystemMessage(String userName, int messageCount) {
        return createContextualSystemMessage(userName, messageCount, null, null);
    }

    /**
     * 创建携带完整用户情境信息的系统提示消息（混合记忆架构）
     * @param userName        用户昵称
     * @param messageCount    本次会话已有消息数（判断新/继续对话）
     * @param coreMemory      长期核心记忆摘要（第1层，滚动总结，可为null）
     * @param recalledDetails Pseudo-RAG检索出的历史细节片段（第2层，按需注入，可为null）
     */
    public ChatRequest.Message createContextualSystemMessage(String userName, int messageCount,
                                                              String coreMemory, String recalledDetails) {
        return createContextualSystemMessage(userName, messageCount, coreMemory, recalledDetails, null);
    }

    /**
     * 创建携带完整用户情境信息和指定AI人设的系统提示消息（混合记忆架构 + AI角色设定）
     */
    public ChatRequest.Message createContextualSystemMessage(String userName, int messageCount,
                                                              String coreMemory, String recalledDetails, AiCharacter aiCharacter) {
        // 根据时间选择问候语
        int hour = java.time.LocalTime.now().getHour();
        String timeGreeting;
        String timeContext;
        if (hour >= 5 && hour < 9) {
            timeGreeting = "清晨好";
            timeContext = "清晨是一天的开始，有时候也是情绪最敏感的时刻。";
        } else if (hour >= 9 && hour < 12) {
            timeGreeting = "上午好";
            timeContext = "上午的时光，希望你的一天到目前为止还好。";
        } else if (hour >= 12 && hour < 14) {
            timeGreeting = "午安";
            timeContext = "午时有时候会容易感到疲倦或情绪波动。";
        } else if (hour >= 14 && hour < 18) {
            timeGreeting = "下午好";
            timeContext = "下午是一天中情绪变化较多的时段。";
        } else if (hour >= 18 && hour < 22) {
            timeGreeting = "晚上好";
            timeContext = "傍晚和夜晚，很多人喜欢在这个时候回顾一天、整理心情。";
        } else {
            timeGreeting = "你好";
            timeContext = "深夜或许是很多想法涌现的时候，也可能是感到孤独的时刻。";
        }

        // 解析 AI 人设（降级为默认云朵）
        String charName = (aiCharacter != null && aiCharacter.getName() != null) ? aiCharacter.getName() : "云朵";
        String charBackground = (aiCharacter != null && aiCharacter.getBackground() != null) ? aiCharacter.getBackground() 
            : "你是一个有温度的心理陪伴伙伴，名字叫\"云朵\"。你不是冷冰冰的AI助手，而是一个真正愿意倾听、关心对方的陪伴者。";
        String charPersonality = (aiCharacter != null && aiCharacter.getPersonality() != null) ? aiCharacter.getPersonality() 
            : "- 温暖真诚，像一个老朋友，而不是心理咨询师\n- 善于感受和回应情绪，先共情再建议\n- 偶尔用轻松的语气，让对话不那么沉重";
        String charRules = (aiCharacter != null && aiCharacter.getRules() != null) ? aiCharacter.getRules() 
            : "- 绝对不评判用户的感受或行为\n- 如涉及自杀/自伤风险，温和而坚定地建议拨打心理援助热线（北京：010-82951332，全国：400-161-9995）";
        String charGreeting = (aiCharacter != null && aiCharacter.getGreeting() != null) ? aiCharacter.getGreeting() : "";

        // 判断是否为新会话
        String sessionContext = messageCount == 0
                ? String.format("这是你和%s今天的第一次对话，用符合你人设的方式先打个招呼。%s", userName, 
                    charGreeting.isBlank() ? "询问ta今天感觉怎么样。" : "您可以参考这句默认开场白：'" + charGreeting + "'")
                : String.format("你们已经聊了一会儿了，继续自然地以你的人设倾听和陪伴%s。", userName);

        // 构建长期记忆段落（第1层：核心摘要，始终注入，约150字上下）
        String memorySection = (coreMemory != null && !coreMemory.isBlank())
                ? String.format("\n## 【用户核心档案 — 极其重要，请始终牢记并体现在对话中】\n%s\n", coreMemory.trim())
                : "";

        // 构建细节检索段落（第2层：Pseudo-RAG，仅当用户触发回忆关键词时注入）
        String ragSection = (recalledDetails != null && !recalledDetails.isBlank())
                ? String.format("\n## 【辅助回忆档案 — 用户提及以前的事，以下是相关历史片段，请自然融入回复中】\n%s\n", recalledDetails.trim())
                : "";

        String systemPrompt = String.format("""
                %s

                ## 关于用户
                - 用户的昵称是：%s
                - 当前时间：%s（%s）
                - 对话状态：%s
                %s%s
                ## 你的性格与说话方式
                %s

                ## 对话重要原则与禁忌
                %s
                - 全程使用中文
                - 不要每次都重复自我介绍

                现在，请以"%s"的身份，继续和%s聊天。
                """,
                charBackground,
                userName, timeGreeting, timeContext, sessionContext,
                memorySection, ragSection,
                charPersonality,
                charRules,
                charName, userName);

        ChatRequest.Message systemMessage = new ChatRequest.Message();
        systemMessage.setRole("system");
        systemMessage.setContent(systemPrompt);
        return systemMessage;
    }

    
    /**
     * 解析DeepSeek API响应
     */
    private ChatResponse parseResponse(Map<String, Object> responseMap) {
        try {
            ChatResponse response = new ChatResponse();
            
            // 解析基本信息
            response.setId((String) responseMap.get("id"));
            response.setModel((String) responseMap.get("model"));
            response.setCreated(((Number) responseMap.get("created")).longValue());
            
            // 解析choices
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                
                if (message != null) {
                    response.setContent((String) message.get("content"));
                    response.setRole((String) message.get("role"));
                }
                
                response.setFinishReason((String) firstChoice.get("finish_reason"));
            }
            
            // 解析usage
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            if (usage != null) {
                response.setPromptTokens(((Number) usage.get("prompt_tokens")).intValue());
                response.setCompletionTokens(((Number) usage.get("completion_tokens")).intValue());
                response.setTotalTokens(((Number) usage.get("total_tokens")).intValue());
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Error parsing DeepSeek API response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse API response", e);
        }
    }
    
    /**
     * 判断是否为可重试的异常
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int statusCode = ex.getStatusCode().value();
            // 重试5xx错误和429（限流）错误
            return statusCode >= 500 || statusCode == 429;
        }
        // 重试网络超时等异常
        return throwable instanceof java.util.concurrent.TimeoutException ||
               throwable instanceof java.net.ConnectException;
    }
    
    /**
     * 测试API连接
     */
    public Mono<Boolean> testConnection() {
        List<ChatRequest.Message> testMessages = new ArrayList<>();
        testMessages.add(createSystemMessage());
        
        ChatRequest.Message userMessage = new ChatRequest.Message();
        userMessage.setRole("user");
        userMessage.setContent("你好");
        testMessages.add(userMessage);
        
        return sendChatRequest(testMessages)
                .map(response -> response != null && response.getContent() != null)
                .onErrorReturn(false);
    }
}
