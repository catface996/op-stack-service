-- V21__Create_node_agent_relation_table.sql
-- Node-Agent 绑定功能数据库迁移脚本
-- Feature: 031-node-agent-binding
-- Date: 2025-12-28

-- Node-Agent 关联表
CREATE TABLE node_2_agent (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联记录 ID',
    node_id     BIGINT          NOT NULL COMMENT '资源节点 ID',
    agent_id    BIGINT          NOT NULL COMMENT 'Agent ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_node_id (node_id),
    INDEX idx_agent_id (agent_id),
    UNIQUE INDEX uk_node_agent (node_id, agent_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Node-Agent 关联表';
