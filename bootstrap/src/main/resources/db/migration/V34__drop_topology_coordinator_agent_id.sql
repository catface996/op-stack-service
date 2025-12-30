-- V34: Remove obsolete coordinator_agent_id field from topology table
-- Description: This field was never used in production and is being cleaned up
-- Feature: 041-cleanup-obsolete-fields (US2)

-- Drop the coordinator_agent_id column from topology table (if exists)
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'topology' AND column_name = 'coordinator_agent_id');

SET @sql = IF(@col_exists > 0, 'ALTER TABLE topology DROP COLUMN coordinator_agent_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
