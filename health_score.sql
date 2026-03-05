-- 1. 向 users 表增加 health_score 字段，默认100分
ALTER TABLE users ADD COLUMN health_score INT NOT NULL DEFAULT 100 COMMENT '用户心理健康安全分(0-100)';

-- 2. 创建用户健康分变动记录表
CREATE TABLE user_health_score_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    score_change INT NOT NULL COMMENT '分数变动(正负值)',
    current_score INT NOT NULL COMMENT '变动后的当前总分',
    reason VARCHAR(500) NOT NULL COMMENT '变动原因描述',
    change_type VARCHAR(50) NOT NULL COMMENT '变动类型(ASSESSMENT/RESOURCE/TOOL/FORUM/CHAT)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户健康分变动流水表';
