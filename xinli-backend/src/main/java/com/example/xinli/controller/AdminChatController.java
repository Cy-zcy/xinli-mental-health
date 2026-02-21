package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.entity.ChatMessage;
import com.example.xinli.entity.User;
import com.example.xinli.service.ChatService;
import com.example.xinli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员聊天管理控制器
 * 处理管理员端聊天会话管理相关的接口请求
 */
@RestController
@RequestMapping("/api/admin/chat")
@CrossOrigin
public class AdminChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 1. 获取聊天会话列表（分页）
     * GET /api/admin/chat/sessions
     */
    @GetMapping("/sessions")
    public Result<Page<ChatSessionVO>> getChatSessions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userPhone,
            @RequestParam(required = false) String userNickname,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            Page<ChatSession> page = new Page<>(current, size);
            QueryWrapper<ChatSession> wrapper = new QueryWrapper<>();
            
            // 按创建时间倒序
            wrapper.orderByDesc("created_at");
            
            // 时间范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge("created_at", startDate + " 00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le("created_at", endDate + " 23:59:59");
            }
            
            Page<ChatSession> sessionPage = chatService.getChatSessionPage(page, wrapper);
            
            // 获取用户信息并构建VO
            List<Long> userIds = sessionPage.getRecords().stream()
                    .map(ChatSession::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Long, User> userMap = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            
            List<ChatSessionVO> sessionVOs = sessionPage.getRecords().stream()
                    .map(session -> {
                        User user = userMap.get(session.getUserId());
                        if (user != null) {
                            // 用户筛选
                            if (userPhone != null && !userPhone.isEmpty() && 
                                !user.getPhone().contains(userPhone)) {
                                return null;
                            }
                            if (userNickname != null && !userNickname.isEmpty() && 
                                (user.getNickname() == null || !user.getNickname().contains(userNickname))) {
                                return null;
                            }
                        }
                        
                        ChatSessionVO vo = new ChatSessionVO();
                        vo.setId(session.getId());
                        vo.setUserId(session.getUserId());
                        vo.setTitle(session.getTitle());
                        vo.setStatus(session.getStatus());
                        vo.setCreatedAt(session.getCreatedAt());
                        vo.setUpdatedAt(session.getUpdatedAt());
                        
                        if (user != null) {
                            vo.setUserPhone(user.getPhone());
                            vo.setUserNickname(user.getNickname());
                        }
                        
                        // 获取消息统计
                        int messageCount = chatService.getMessageCountBySessionId(session.getId());
                        vo.setMessageCount(messageCount);
                        
                        // 获取最后活跃时间
                        LocalDateTime lastActiveTime = chatService.getLastActiveTime(session.getId());
                        vo.setLastActiveTime(lastActiveTime);
                        
                        return vo;
                    })
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());
            
            Page<ChatSessionVO> resultPage = new Page<>(current, size);
            resultPage.setRecords(sessionVOs);
            resultPage.setTotal(sessionPage.getTotal());
            
            return Result.success(resultPage);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取聊天会话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 2. 获取聊天会话详情和消息记录
     * GET /api/admin/chat/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<ChatSessionDetailVO> getChatSessionDetail(@PathVariable Long sessionId) {
        try {
            ChatSession session = chatService.getChatSessionById(sessionId);
            if (session == null) {
                return Result.error(404, "聊天会话不存在");
            }
            
            User user = userService.getUserById(session.getUserId());
            List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId);
            
            ChatSessionDetailVO detail = new ChatSessionDetailVO();
            detail.setSession(session);
            detail.setUser(user);
            detail.setMessages(messages);
            
            return Result.success(detail);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取聊天会话详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 3. 删除聊天会话
     * DELETE /api/admin/chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteChatSession(@PathVariable Long sessionId) {
        try {
            boolean success = chatService.deleteChatSession(sessionId);
            if (success) {
                return Result.success(null, "删除成功");
            } else {
                return Result.error(500, "删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除聊天会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 聊天会话VO类
     */
    public static class ChatSessionVO {
        private Long id;
        private Long userId;
        private String userPhone;
        private String userNickname;
        private String title;
        private Integer status;
        private Integer messageCount;
        private LocalDateTime lastActiveTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        
        public String getUserNickname() { return userNickname; }
        public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public Integer getMessageCount() { return messageCount; }
        public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
        
        public LocalDateTime getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    /**
     * 聊天会话详情VO类
     */
    public static class ChatSessionDetailVO {
        private ChatSession session;
        private User user;
        private List<ChatMessage> messages;
        
        // Getters and Setters
        public ChatSession getSession() { return session; }
        public void setSession(ChatSession session) { this.session = session; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        
        public List<ChatMessage> getMessages() { return messages; }
        public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    }
}
