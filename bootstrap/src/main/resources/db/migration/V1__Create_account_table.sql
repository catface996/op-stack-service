-- Create account table for user authentication
-- This table stores user account information including credentials and status

CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账号ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(60) NOT NULL COMMENT '加密后的密码(BCrypt)',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色: ROLE_USER, ROLE_ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态: ACTIVE, LOCKED, DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username) COMMENT '用户名唯一索引',
    UNIQUE KEY uk_email (email) COMMENT '邮箱唯一索引',
    INDEX idx_status (status) COMMENT '状态索引，用于查询活跃/锁定账号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';
