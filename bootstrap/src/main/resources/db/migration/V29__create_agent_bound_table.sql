-- =====================================================
-- V29: 创建 agent_bound 表
-- Feature: 040-agent-bound-refactor
-- Date: 2025-12-29
-- Description: 统一管理 Agent 与各类实体（Topology、Node）的绑定关系
-- =====================================================

CREATE TABLE agent_bound (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '绑定记录 ID',

    -- 业务核心字段
    agent_id        BIGINT          NOT NULL COMMENT 'Agent ID',
    hierarchy_level VARCHAR(20)     NOT NULL COMMENT '层级: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER',
    entity_id       BIGINT          NOT NULL COMMENT '绑定实体 ID',
    entity_type     VARCHAR(20)     NOT NULL COMMENT '实体类型: TOPOLOGY, NODE',

    -- 审计字段
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 软删除
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_entity (entity_type, entity_id, deleted),
    INDEX idx_agent_id (agent_id, deleted),
    INDEX idx_hierarchy_level (hierarchy_level, deleted),
    UNIQUE INDEX uk_agent_entity (agent_id, entity_id, entity_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 绑定关系表';
