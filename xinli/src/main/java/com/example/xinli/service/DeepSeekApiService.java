package com.example.xinli.service;

import com.example.xinli.config.DeepSeekProperties;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.dto.ChatResponse;
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
     * 创建系统提示消息
     * @return 系统消息
     */
    public ChatRequest.Message createSystemMessage() {
        String systemPrompt = """
                你是一个专业的心理健康AI助手，名字叫"心理小助手"。你的主要职责是：
                
                1. 提供情感支持和心理健康建议
                2. 倾听用户的困扰和问题
                3. 给出专业、温暖、有帮助的回复
                4. 鼓励用户寻求专业心理咨询师的帮助（如果需要）
                5. 保持积极、理解和非评判的态度
                
                请注意：
                - 你不能替代专业的心理治疗
                - 如果用户有严重的心理健康问题，建议寻求专业帮助
                - 保持回复简洁、温暖且有帮助
                - 使用中文回复
                """;
        
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
