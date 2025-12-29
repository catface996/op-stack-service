-- Add hierarchy_level column to agent table
-- This separates the team hierarchy position from the role (expertise domain)

ALTER TABLE agent
ADD COLUMN hierarchy_level VARCHAR(32) DEFAULT 'TEAM_WORKER' COMMENT 'Agent 层级: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER'
AFTER role;

-- Migrate existing data: map old role values to new hierarchy_level
UPDATE agent SET hierarchy_level = 'GLOBAL_SUPERVISOR' WHERE role = 'GLOBAL_SUPERVISOR';
UPDATE agent SET hierarchy_level = 'TEAM_SUPERVISOR' WHERE role = 'TEAM_SUPERVISOR';
UPDATE agent SET hierarchy_level = 'TEAM_WORKER' WHERE role IN ('WORKER', 'SCOUTER') OR role IS NULL;
