package com.example.xinli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息对象
     */
    @Data
    public static class Message {
        private String role; // system, user, assistant
        private String content;
    }
    
    /**
     * 发送消息请求DTO
     */
    @Data
    public static class SendMessageRequest {
        private Long sessionId; // 可选，如果为空则创建新会话
        
        @NotBlank(message = "消息内容不能为空")
        private String content;
    }
    
    /**
     * 创建会话请求DTO
     */
    @Data
    public static class CreateSessionRequest {
        private String title; // 可选，会话标题
        
        @NotBlank(message = "首条消息内容不能为空")
        private String firstMessage;
    }
}
