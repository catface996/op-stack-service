-- Add missing columns to t_session table for session management features
-- These columns support remember-me functionality and session timeout management
-- Note: Using stored procedure to handle idempotent migration

DELIMITER //

DROP PROCEDURE IF EXISTS add_session_columns//

CREATE PROCEDURE add_session_columns()
BEGIN
    -- Add last_activity_at if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 't_session'
                   AND column_name = 'last_activity_at') THEN
        ALTER TABLE t_session ADD COLUMN last_activity_at DATETIME COMMENT '最后活动时间' AFTER created_at;
    END IF;

    -- Add absolute_timeout if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 't_session'
                   AND column_name = 'absolute_timeout') THEN
        ALTER TABLE t_session ADD COLUMN absolute_timeout BIGINT COMMENT '绝对超时时间(毫秒)' AFTER last_activity_at;
    END IF;

    -- Add idle_timeout if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 't_session'
                   AND column_name = 'idle_timeout') THEN
        ALTER TABLE t_session ADD COLUMN idle_timeout BIGINT COMMENT '空闲超时时间(毫秒)' AFTER absolute_timeout;
    END IF;

    -- Add remember_me if not exists
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE()
                   AND table_name = 't_session'
                   AND column_name = 'remember_me') THEN
        ALTER TABLE t_session ADD COLUMN remember_me TINYINT(1) DEFAULT 0 COMMENT '是否记住我登录' AFTER idle_timeout;
    END IF;
END//

DELIMITER ;

CALL add_session_columns();

DROP PROCEDURE IF EXISTS add_session_columns;

-- Set default values for existing records
UPDATE t_session SET last_activity_at = created_at WHERE last_activity_at IS NULL;
