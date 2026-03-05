package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户系统通知实体
 * 用于发送系统消息、危机干预通知等
 */
@Data
@TableName("user_notifications")
public class UserNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知正文内容 */
    private String content;

    /**
     * 通知类型
     * SYSTEM  - 系统通知
     * CRISIS  - 危机干预通知
     * LIKE    - 点赞通知
     */
    private String type;

    /** 是否已读：0 未读 / 1 已读 */
    private Integer isRead;

    private LocalDateTime createdAt;
}
