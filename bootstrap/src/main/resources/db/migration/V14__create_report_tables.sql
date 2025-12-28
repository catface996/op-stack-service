-- V14__create_report_tables.sql
-- Report Management 模块数据库迁移脚本

-- 报告表
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '报告标题',
    type VARCHAR(20) NOT NULL COMMENT '报告类型: Diagnosis, Audit, Performance, Security',
    status VARCHAR(20) NOT NULL DEFAULT 'Final' COMMENT '报告状态: Draft, Final, Archived',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    summary VARCHAR(500) COMMENT '报告摘要',
    content TEXT COMMENT '报告内容(Markdown)',
    tags JSON COMMENT '标签数组',
    topology_id BIGINT COMMENT '关联拓扑图ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_topology_id (topology_id),
    FULLTEXT INDEX idx_search (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告表';

-- 报告模板表
CREATE TABLE report_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description VARCHAR(500) COMMENT '模板描述',
    category VARCHAR(20) NOT NULL COMMENT '模板分类: Incident, Performance, Security, Audit',
    content TEXT NOT NULL COMMENT '模板内容(含占位符的Markdown)',
    tags JSON COMMENT '标签数组',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';
