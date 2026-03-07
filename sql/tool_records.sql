-- 心理工具使用记录表
-- 用于记录用户呼吸练习、冥想引导等工具的使用情况
CREATE TABLE IF NOT EXISTS `tool_records` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`          BIGINT       NOT NULL COMMENT '用户ID',
    `tool_type`        VARCHAR(50)  NOT NULL COMMENT '工具类型：breathing / meditation',
    `duration_seconds` INT          NOT NULL DEFAULT 0 COMMENT '本次使用时长（秒）',
    `cycles`           INT          NOT NULL DEFAULT 0 COMMENT '完成的循环/周期数',
    `completed`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否完成：1=完整完成，0=中途退出',
    `pattern`          VARCHAR(100) NULL COMMENT '使用模式，如 4-7-8 呼吸法、正念冥想',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_tool` (`user_id`, `tool_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具使用记录表';
