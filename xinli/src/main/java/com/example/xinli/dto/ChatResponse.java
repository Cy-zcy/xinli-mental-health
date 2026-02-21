package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天响应DTO
 */
@Data
public class ChatResponse {
    
    // DeepSeek API响应字段
    private String id;
    private String model;
    private Long created;
    private String content;
    private String role;
    private String finishReason;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    
    /**
     * 发送消息响应DTO
     */
    @Data
    public static class SendMessageResponse {
        private Long sessionId;
        private Long messageId;
        private String userMessage;
        private String aiResponse;
        private Integer tokensUsed;
        private LocalDateTime timestamp;
    }
    
    /**
     * 会话信息DTO
     */
    @Data
    public static class SessionInfo {
        private Long id;
        private String title;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer messageCount; // 消息数量
        private String lastMessage; // 最后一条消息预览
    }
    
    /**
     * 消息历史DTO
     */
    @Data
    public static class MessageHistory {
        private Long id;
        private String role;
        private String content;
        private Integer tokensUsed;
        private LocalDateTime createdAt;
    }
    
    /**
     * 聊天历史响应DTO
     */
    @Data
    public static class ChatHistoryResponse {
        private Long sessionId;
        private String sessionTitle;
        private List<MessageHistory> messages;
        private Integer totalMessages;
        private Integer totalTokens;
    }
}
