-- =====================================================================
-- Migration: V35 - 移除 topology.global_supervisor_agent_id 字段
-- Feature: 041-cleanup-obsolete-fields (US3)
-- Description: 删除 topology 表中废弃的 global_supervisor_agent_id 列
--              数据已迁移到 agent_bound 表（hierarchy_level=GLOBAL_SUPERVISOR）
-- Author: AI Assistant
-- Date: 2025-12-30
-- =====================================================================

-- 删除 global_supervisor_agent_id 字段 (if exists)
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'topology' AND column_name = 'global_supervisor_agent_id');

SET @sql = IF(@col_exists > 0, 'ALTER TABLE topology DROP COLUMN global_supervisor_agent_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
