-- V33: Remove obsolete agent_team_id field from node table
-- Description: This field was never used in production and is being cleaned up
-- Feature: 041-cleanup-obsolete-fields (US1)

-- Drop the agent_team_id column from node table (if exists)
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'node' AND column_name = 'agent_team_id');

SET @sql = IF(@col_exists > 0, 'ALTER TABLE node DROP COLUMN agent_team_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
