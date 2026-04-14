package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.entity.UserNotification;
import com.example.xinli.mapper.UserNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户通知服务
 * 负责通知的查询、发送、已读标记
 */
@Service
public class UserNotificationService {

    @Autowired
    private UserNotificationMapper notificationMapper;

    /**
     * 分页查询用户通知列表（按时间倒序）
     * @param type 通知类型筛选（null表示全部）
     */
    public Page<UserNotification> getUserNotifications(Long userId, int page, int size, String type) {
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            wrapper.eq("type", type.toUpperCase());
        }
        wrapper.orderByDesc("created_at");
        return notificationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 分页查询用户通知列表（按时间倒序）- 兼容旧接口
     */
    public Page<UserNotification> getUserNotifications(Long userId, int page, int size) {
        return getUserNotifications(userId, page, size, null);
    }

    /**
     * 获取用户未读通知数量
     */
    public long getUnreadCount(Long userId) {
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_read", 0);
        return notificationMapper.selectCount(wrapper);
    }

    /**
     * 标记单条通知为已读
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }
        notification.setIsRead(1);
        return notificationMapper.updateById(notification) > 0;
    }

    /**
     * 一键全部已读
     */
    public void markAllRead(Long userId) {
        UserNotification update = new UserNotification();
        update.setIsRead(1);
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_read", 0);
        notificationMapper.update(update, wrapper);
    }

    /**
     * 删除单条通知
     */
    public boolean deleteNotification(Long notificationId, Long userId) {
        UserNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }
        return notificationMapper.deleteById(notificationId) > 0;
    }

    /**
     * 清空所有通知
     */
    public int deleteAllNotifications(Long userId) {
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return notificationMapper.delete(wrapper);
    }

    /**
     * 获取各类型未读数量统计
     */
    public Map<String, Long> getUnreadCountByType(Long userId) {
        Map<String, Long> result = new HashMap<>();
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_read", 0);
        wrapper.select("type", "COUNT(*) as count");
        wrapper.groupBy("type");

        notificationMapper.selectMaps(wrapper).forEach(map -> {
            String type = (String) map.get("type");
            Long count = (Long) map.get("count");
            if (type != null) {
                result.put(type, count);
            }
        });

        // 确保所有类型都有值
        result.putIfAbsent("SYSTEM", 0L);
        result.putIfAbsent("CRISIS", 0L);
        result.putIfAbsent("LIKE", 0L);

        return result;
    }
}
