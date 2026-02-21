package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.dto.ChatResponse;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.service.ChatService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * AI聊天控制器
 * 处理用户端AI聊天相关的接口请求
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 1. 发送消息给AI
     * POST /api/chat/send
     * 需要用户认证
     */
    @PostMapping("/send")
    public Mono<Result<ChatResponse.SendMessageResponse>> sendMessage(
            @Valid @RequestBody ChatRequest.SendMessageRequest request, 
            HttpServletRequest httpRequest) {
        
        return Mono.fromCallable(() -> {
            // 从JWT token获取用户ID
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            return userId;
        })
        .flatMap(userId -> {
            if (userId instanceof Result) {
                return Mono.just((Result<ChatResponse.SendMessageResponse>) userId);
            }
            
            return chatService.sendMessage((Long) userId, request)
                    .map(Result::success)
                    .onErrorResume(error -> Mono.just(Result.error("发送消息失败: " + error.getMessage())));
        });
    }
    
    /**
     * 2. 创建新的聊天会话
     * POST /api/chat/session
     * 需要用户认证
     */
    @PostMapping("/session")
    public Result<ChatSession> createSession(
            @Valid @RequestBody ChatRequest.CreateSessionRequest request, 
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            ChatSession session = chatService.createSession(userId, request);
            return Result.success(session);
        } catch (Exception e) {
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 3. 获取用户的会话列表
     * GET /api/chat/sessions
     * 需要用户认证
     */
    @GetMapping("/sessions")
    public Result<Page<ChatResponse.SessionInfo>> getUserSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            Page<ChatResponse.SessionInfo> sessions = chatService.getUserSessions(userId, page, size);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 4. 获取聊天历史记录
     * GET /api/chat/history
     * 需要用户认证
     */
    @GetMapping("/history")
    public Result<ChatResponse.ChatHistoryResponse> getChatHistory(
            @RequestParam Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            ChatResponse.ChatHistoryResponse history = chatService.getChatHistory(userId, sessionId, page, size);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取聊天历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 5. 删除会话
     * DELETE /api/chat/session/{sessionId}
     * 需要用户认证
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            boolean success = chatService.deleteSession(userId, sessionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除会话失败");
            }
        } catch (Exception e) {
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 6. 获取会话详情
     * GET /api/chat/session/{sessionId}
     * 需要用户认证
     */
    @GetMapping("/session/{sessionId}")
    public Result<ChatResponse.ChatHistoryResponse> getSessionDetail(
            @PathVariable Long sessionId, 
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            ChatResponse.ChatHistoryResponse history = chatService.getChatHistory(userId, sessionId, 1, 100);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取会话详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 7. 测试AI连接
     * GET /api/chat/test
     * 需要用户认证
     */
    @GetMapping("/test")
    public Mono<Result<Boolean>> testAiConnection(HttpServletRequest httpRequest) {
        return Mono.fromCallable(() -> {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            return userId;
        })
        .flatMap(userId -> {
            if (userId instanceof Result) {
                return Mono.just((Result<Boolean>) userId);
            }
            
            return chatService.testAiConnection()
                    .map(Result::success)
                    .onErrorResume(error -> Mono.just(Result.error("AI连接测试失败: " + error.getMessage())));
        });
    }
    
    /**
     * 从HTTP请求中提取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // Token解析失败，返回null
        }
        return null;
    }
}
