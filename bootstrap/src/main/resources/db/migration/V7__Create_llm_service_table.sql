-- F09 LLM 服务配置管理 - 数据库表结构
-- 需求追溯: FR-001~011

-- ==============================================
-- 1. LLM 服务配置表 (llm_service_config)
-- ==============================================
-- 用于存储 LLM 服务的配置信息
-- 服务名称全局唯一
CREATE TABLE llm_service_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'LLM 服务 ID（主键）',
    name VARCHAR(100) NOT NULL COMMENT '服务名称（全局唯一，1-100字符）',
    description VARCHAR(500) COMMENT '服务描述（最长500字符）',
    provider_type VARCHAR(20) NOT NULL COMMENT '供应商类型：OPENAI, CLAUDE, LOCAL, CUSTOM',
    endpoint VARCHAR(500) COMMENT 'API 端点地址（URL格式）',
    model_parameters JSON NOT NULL COMMENT '模型参数配置（JSON对象格式）',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级（1-999，数字越小优先级越高）',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为默认服务',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引定义
    UNIQUE KEY uk_name (name) COMMENT '确保服务名称全局唯一',
    INDEX idx_provider_type (provider_type) COMMENT '按供应商类型查询',
    INDEX idx_enabled (enabled) COMMENT '按启用状态过滤',
    INDEX idx_is_default (is_default) COMMENT '按默认标记查询',
    INDEX idx_priority (priority) COMMENT '按优先级排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 服务配置表';
