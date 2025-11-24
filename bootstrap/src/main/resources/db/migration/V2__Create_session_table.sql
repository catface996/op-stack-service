-- Create session table as fallback when Redis is unavailable
-- This table stores user session information for session management

CREATE TABLE t_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '会话ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token TEXT NOT NULL COMMENT 'JWT Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    device_info TEXT COMMENT '设备信息 (JSON格式)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引，用于查询用户会话',
    INDEX idx_expires_at (expires_at) COMMENT '过期时间索引，用于清理过期会话',
    CONSTRAINT fk_session_user_id FOREIGN KEY (user_id) REFERENCES t_account(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表(降级方案)';
