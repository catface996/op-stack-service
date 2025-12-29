-- =====================================================
-- V31: agent_bound 表改用物理删除
-- Feature: 040-agent-bound-refactor
-- Date: 2025-12-30
-- Description:
--   1. 删除所有软删除记录（deleted=1）
--   2. 重建唯一约束（不包含 deleted 字段）
--   3. 保留 deleted 字段但不再使用（向后兼容）
-- =====================================================

-- 1. 删除所有软删除记录
DELETE FROM agent_bound WHERE deleted = 1;

-- 2. 重建唯一约束（幂等）
-- 检查并删除旧约束（包含 deleted 字段的版本）
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = DATABASE()
               AND table_name = 'agent_bound'
               AND index_name = 'uk_agent_entity');
SET @sql := IF(@exist > 0, 'ALTER TABLE agent_bound DROP INDEX uk_agent_entity', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 创建新的唯一约束（不包含 deleted 字段）
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics
               WHERE table_schema = DATABASE()
               AND table_name = 'agent_bound'
               AND index_name = 'uk_agent_entity');
SET @sql := IF(@exist = 0, 'ALTER TABLE agent_bound ADD UNIQUE INDEX uk_agent_entity (agent_id, entity_id, entity_type)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
