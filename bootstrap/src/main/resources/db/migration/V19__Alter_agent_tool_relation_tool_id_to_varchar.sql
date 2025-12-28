-- ==============================================
-- 修改 agent_2_tool 表的 tool_id 字段类型
-- Feature: 030-agent-tools
-- Date: 2025-12-28
-- ==============================================
-- 说明：
-- 1. Tool ID 使用 UUID 格式，需要将 BIGINT 改为 VARCHAR(36)
-- 2. 先删除原有数据（因为 BIGINT 类型的数据无法转换为 UUID）
-- 3. 修改字段类型
-- ==============================================

-- 清空现有数据（BIGINT 类型的 tool_id 无法转为 UUID）
DELETE FROM agent_2_tool;

-- 删除旧的唯一索引
ALTER TABLE agent_2_tool DROP INDEX uk_agent_tool;

-- 删除 tool_id 的索引
ALTER TABLE agent_2_tool DROP INDEX idx_agent_2_tool_tool_id;

-- 修改 tool_id 字段类型为 VARCHAR(36) 以支持 UUID
ALTER TABLE agent_2_tool MODIFY COLUMN tool_id VARCHAR(36) NOT NULL COMMENT 'Tool ID (UUID)';

-- 重新创建索引
CREATE INDEX idx_agent_2_tool_tool_id ON agent_2_tool (tool_id);

-- 重新创建唯一约束
CREATE UNIQUE INDEX uk_agent_tool ON agent_2_tool (agent_id, tool_id, deleted);
