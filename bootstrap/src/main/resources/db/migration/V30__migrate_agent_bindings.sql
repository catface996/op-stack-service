-- =====================================================
-- V30: 迁移现有 Agent 绑定到统一的 agent_bound 表
-- Feature: 040-agent-bound-refactor
-- Date: 2025-12-29
-- =====================================================

-- 迁移 Topology 的 Global Supervisor 绑定
-- 从 topology.global_supervisor_agent_id 迁移到 agent_bound
INSERT INTO agent_bound (agent_id, hierarchy_level, entity_id, entity_type, created_at, deleted)
SELECT
    t.global_supervisor_agent_id AS agent_id,
    'GLOBAL_SUPERVISOR' AS hierarchy_level,
    t.id AS entity_id,
    'TOPOLOGY' AS entity_type,
    COALESCE(t.created_at, NOW()) AS created_at,
    0 AS deleted
FROM topology t
WHERE t.global_supervisor_agent_id IS NOT NULL
  AND t.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.entity_type = 'TOPOLOGY'
      AND ab.entity_id = t.id
      AND ab.hierarchy_level = 'GLOBAL_SUPERVISOR'
      AND ab.deleted = 0
  );

-- 迁移 Node-Agent 绑定
-- 从 node_2_agent 迁移到 agent_bound
-- 需要根据 Agent 的 hierarchy_level 确定绑定类型
INSERT INTO agent_bound (agent_id, hierarchy_level, entity_id, entity_type, created_at, deleted)
SELECT
    na.agent_id,
    COALESCE(a.hierarchy_level, 'TEAM_WORKER') AS hierarchy_level,
    na.node_id AS entity_id,
    'NODE' AS entity_type,
    na.created_at,
    na.deleted
FROM node_2_agent na
JOIN agent a ON na.agent_id = a.id
WHERE NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.agent_id = na.agent_id
      AND ab.entity_id = na.node_id
      AND ab.entity_type = 'NODE'
      AND ab.deleted = na.deleted
  );

-- 注意：此迁移脚本是幂等的（safe to run multiple times）
-- 使用 NOT EXISTS 确保不会重复插入已存在的绑定关系
