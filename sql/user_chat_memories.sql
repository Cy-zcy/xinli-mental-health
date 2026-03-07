-- AI 用户长期记忆表
-- 每个用户仅保留 1 行记录，通过滚动压缩摘要持续覆盖更新
-- 消除记忆膨胀：无论对话积累多久，core_memory 始终维持在 200 字以内
CREATE TABLE IF NOT EXISTS `user_chat_memories` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`           BIGINT      NOT NULL COMMENT '用户ID（唯一）',
    `core_memory`       TEXT        NULL COMMENT 'AI 滚动生成的用户核心记忆摘要（200字以内）',
    `processed_msg_count` INT       NOT NULL DEFAULT 0 COMMENT '已被纳入摘要的消息条数，用于判断是否触发新一轮总结',
    `session_count`     INT         NOT NULL DEFAULT 0 COMMENT '已参与总结的会话数',
    `updated_at`        DATETIME    NULL COMMENT '最近一次更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `udx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户AI对话长期记忆表（每用户仅1行，滚动覆盖更新）';
