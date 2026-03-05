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
     */
    public Page<UserNotification> getUserNotifications(Long userId, int page, int size) {
        QueryWrapper<UserNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        return notificationMapper.selectPage(new Page<>(page, size), wrapper);
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
}
