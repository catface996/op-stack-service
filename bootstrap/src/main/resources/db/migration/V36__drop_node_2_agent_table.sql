-- =====================================================================
-- Migration: V36 - 删除 node_2_agent 表
-- Feature: 041-cleanup-obsolete-fields (US4)
-- Description: 删除废弃的 node_2_agent 关联表
--              数据已迁移到 agent_bound 表（entity_type=NODE）
-- Author: AI Assistant
-- Date: 2025-12-30
-- =====================================================================

-- 删除 node_2_agent 表
DROP TABLE IF EXISTS node_2_agent;
