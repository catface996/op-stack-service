-- =====================================================
-- V27: 为 topology 表添加 global_supervisor_agent_id 字段
-- Feature: 035-topology-supervisor-agent
-- Date: 2025-12-29
-- =====================================================

-- 添加 Global Supervisor Agent ID 字段
ALTER TABLE topology
ADD COLUMN global_supervisor_agent_id BIGINT COMMENT 'Global Supervisor Agent ID';

-- 添加索引
ALTER TABLE topology
ADD INDEX idx_global_supervisor_agent_id (global_supervisor_agent_id);
