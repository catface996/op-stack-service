-- =====================================================
-- V40: 创建诊断任务相关表
-- Feature: 044-diagnosis-task
-- Date: 2026-01-05
-- Description: 创建诊断任务表和Agent诊断过程表
-- =====================================================

-- 诊断任务表
CREATE TABLE diagnosis_task (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务核心字段
    topology_id     BIGINT          NOT NULL COMMENT '关联拓扑图ID',
    user_question   TEXT            NOT NULL COMMENT '用户诊断问题',
    status          VARCHAR(20)     NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING, COMPLETED, FAILED, TIMEOUT',
    error_message   VARCHAR(500)    DEFAULT NULL COMMENT '错误信息',
    run_id          VARCHAR(100)    DEFAULT NULL COMMENT 'executor运行ID',
    completed_at    DATETIME        DEFAULT NULL COMMENT '完成时间',

    -- 审计字段
    created_by      BIGINT          COMMENT '创建人ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          COMMENT '修改人ID',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 版本控制
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_topology_id (topology_id, deleted),
    INDEX idx_status (status, deleted),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断任务表';

-- Agent诊断过程表
CREATE TABLE agent_diagnosis_process (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务核心字段
    task_id         BIGINT          NOT NULL COMMENT '关联诊断任务ID',
    agent_bound_id  BIGINT          NOT NULL COMMENT '关联AgentBound ID',
    agent_name      VARCHAR(100)    NOT NULL COMMENT 'Agent名称（冗余存储）',
    content         LONGTEXT        DEFAULT NULL COMMENT '诊断内容（整合后的完整文本）',
    started_at      DATETIME        DEFAULT NULL COMMENT 'Agent开始诊断时间',
    ended_at        DATETIME        DEFAULT NULL COMMENT 'Agent结束诊断时间',

    -- 审计字段
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 软删除
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_task_id (task_id, deleted),
    INDEX idx_agent_bound_id (agent_bound_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent诊断过程表';
