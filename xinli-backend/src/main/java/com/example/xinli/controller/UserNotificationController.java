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
     * @param type 通知类型筛选（ALL/SYSTEM/CRISIS/LIKE）
     */
    @GetMapping
    public Result<Page<UserNotification>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String type,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        String filterType = "ALL".equalsIgnoreCase(type) ? null : type;
        return Result.success(notificationService.getUserNotifications(userId, page, size, filterType));
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

    /**
     * 获取各类型未读数量统计
     * GET /api/notifications/unread-stats
     */
    @GetMapping("/unread-stats")
    public Result<Map<String, Long>> getUnreadStats(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        return Result.success(notificationService.getUnreadCountByType(userId));
    }

    /**
     * 删除单条通知
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        boolean success = notificationService.deleteNotification(id, userId);
        return success ? Result.success() : Result.error(404, "通知不存在或无权删除");
    }

    /**
     * 清空所有通知
     * DELETE /api/notifications/all
     */
    @DeleteMapping("/all")
    public Result<Map<String, Object>> deleteAllNotifications(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) return Result.error(401, "用户未登录");
        int count = notificationService.deleteAllNotifications(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("deletedCount", count);
        return Result.success(data);
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
