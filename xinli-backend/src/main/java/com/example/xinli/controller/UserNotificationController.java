package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.UserNotification;
import com.example.xinli.service.UserNotificationService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端通知控制器
 * 提供获取通知列表、未读数、标记已读等接口
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class UserNotificationController {

    @Autowired
    private UserNotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的通知列表（分页）
     * GET /api/notifications
     */
    @GetMapping
    public Result<Page<UserNotification>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    /**
     * 获取未读通知数量
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.getUnreadCount(userId));
        return Result.success(data);
    }

    /**
     * 标记单条通知为已读
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 全部已读
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        notificationService.markAllRead(userId);
        return Result.success();
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return jwtUtil.getUserIdFromToken(token.substring(7));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
