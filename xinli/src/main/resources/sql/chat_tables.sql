-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) DEFAULT '新的对话' COMMENT '会话标题',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('user', 'assistant', 'system') NOT NULL COMMENT '消息角色：user-用户，assistant-AI助手，system-系统',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens_used INT DEFAULT 0 COMMENT '使用的token数量',
    model VARCHAR(50) DEFAULT 'deepseek-chat' COMMENT '使用的AI模型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 插入示例数据（可选）
-- INSERT INTO chat_sessions (user_id, title) VALUES (1, '心理健康咨询');
-- INSERT INTO chat_messages (session_id, user_id, role, content) VALUES 
-- (1, 1, 'user', '你好，我最近感到很焦虑，能帮助我吗？'),
-- (1, 1, 'assistant', '你好！我很理解你的感受。焦虑是一种常见的情绪反应，我很乐意帮助你。能告诉我更多关于你焦虑的具体情况吗？比如什么时候开始的，有什么特定的触发因素吗？');
