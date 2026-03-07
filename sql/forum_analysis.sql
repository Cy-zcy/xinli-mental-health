-- 帖子情感分析结果表
CREATE TABLE IF NOT EXISTS `forum_post_analysis` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
  `emotion_label` VARCHAR(50) DEFAULT NULL COMMENT '主情绪标签：焦虑/抑郁/愤怒/孤独/平静/积极',
  `emotion_tags` VARCHAR(200) DEFAULT NULL COMMENT '细分标签，以英文逗号分割，如 #学业压力,#失眠',
  `risk_level` TINYINT NOT NULL DEFAULT 0 COMMENT '风险等级：0正常 1轻度关注 2高危',
  `risk_reason` VARCHAR(500) DEFAULT NULL COMMENT '高危原因说明（仅risk_level>=2时有值）',
  `ai_summary` VARCHAR(300) DEFAULT NULL COMMENT 'AI对帖子的简短摘要',
  `is_alerted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已对用户发送危机通知：0否 1是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  PRIMARY KEY (`id`),
  INDEX `idx_post_id` (`post_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子AI情感分析结果';

-- 系统/危机通知表
CREATE TABLE IF NOT EXISTS `user_notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知正文',
  `type` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT '类型: SYSTEM-系统 CRISIS-危机干预 LIKE-点赞',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户系统通知表';
