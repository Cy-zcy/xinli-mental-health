package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户 AI 长期记忆实体
 * 每个用户仅保留 1 条记录，通过滚动压缩摘要持续覆盖，避免记忆膨胀
 */
@Data
@TableName("user_chat_memories")
public class UserChatMemory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID（唯一） */
    private Long userId;

    /**
     * AI 生成的用户核心记忆摘要（始终控制在 200 字以内）
     * 内容示例："用户是大三学生，正经历考研压力，
     * 对父母干涉极度敏感，喜欢被倾听而非被建议。
     * 上次聊完后感觉放松了许多。"
     */
    private String coreMemory;

    /** 已被纳入摘要的消息总条数，用于判断是否触发新一轮滚动总结 */
    private Integer processedMsgCount;

    /** 已参与总结的会话数 */
    private Integer sessionCount;

    /** 最近一次更新时间 */
    private LocalDateTime updatedAt;
}
