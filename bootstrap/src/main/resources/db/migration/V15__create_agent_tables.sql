-- V15__create_agent_tables.sql
-- Agent Management 模块数据库迁移脚本

-- Agent 表
CREATE TABLE agent (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'Agent ID',
    name            VARCHAR(100)    NOT NULL COMMENT 'Agent 名称',
    role            VARCHAR(32)     NOT NULL COMMENT 'Agent 角色: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER',
    specialty       VARCHAR(200)    NULL COMMENT '专业领域',
    warnings        INT             NOT NULL DEFAULT 0 COMMENT '警告数量（全局累计）',
    critical        INT             NOT NULL DEFAULT 0 COMMENT '严重问题数量（全局累计）',
    config          JSON            NULL COMMENT 'AI 配置 (JSON): model, temperature, systemInstruction, defaultContext',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_agent_role (role),
    INDEX idx_agent_deleted (deleted),
    UNIQUE INDEX uk_agent_name (name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 表';

-- Agent 与 Team 关联表
CREATE TABLE agent_2_team (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    agent_id        BIGINT          NOT NULL COMMENT 'Agent ID',
    team_id         BIGINT          NOT NULL COMMENT 'Team ID',
    status          VARCHAR(32)     NOT NULL DEFAULT 'IDLE' COMMENT 'Agent 在该团队的工作状态: IDLE, THINKING, WORKING, COMPLETED, WAITING, ERROR',
    current_task    VARCHAR(500)    NULL COMMENT '当前任务描述',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_agent_2_team_agent_id (agent_id),
    INDEX idx_agent_2_team_team_id (team_id),
    INDEX idx_agent_2_team_status (status),
    UNIQUE INDEX uk_agent_team (agent_id, team_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 与 Team 关联表';
