-- ==============================================
-- Agent-Tool 关联表
-- Feature: 030-agent-tools
-- Date: 2025-12-28
-- ==============================================
-- 说明：
-- 1. 实现 Agent 与 Tool 的多对多关系
-- 2. 支持软删除
-- 3. 唯一约束包含 deleted 字段（允许重新绑定）
-- ==============================================

CREATE TABLE agent_2_tool (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    agent_id    BIGINT       NOT NULL COMMENT 'Agent ID',
    tool_id     BIGINT       NOT NULL COMMENT 'Tool ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_agent_2_tool_agent_id (agent_id),
    INDEX idx_agent_2_tool_tool_id (tool_id),
    UNIQUE INDEX uk_agent_tool (agent_id, tool_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 与 Tool 关联表';
