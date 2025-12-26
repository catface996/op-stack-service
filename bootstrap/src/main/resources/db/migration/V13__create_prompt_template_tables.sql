-- V13__create_prompt_template_tables.sql
-- Feature: 025-prompt-template
-- Description: Create tables for prompt template management with version control

-- 1. Template Usage Table (模板用途表)
CREATE TABLE IF NOT EXISTS `template_usage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '用途编码',
    `name` VARCHAR(100) NOT NULL COMMENT '用途名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '用途描述',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板用途表';

-- Preset usage data (预置用途数据)
INSERT INTO `template_usage` (`code`, `name`, `description`) VALUES
('FAULT_DIAGNOSIS', '故障诊断', '用于故障分析和诊断场景的提示词模板'),
('KNOWLEDGE_QA', '知识问答', '用于知识库问答场景的提示词模板'),
('CODE_GENERATION', '代码生成', '用于代码生成场景的提示词模板'),
('OPS_SUGGESTION', '运维建议', '用于运维建议和最佳实践场景的提示词模板');

-- 2. Prompt Template Table (提示词模板表)
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `usage_id` BIGINT DEFAULT NULL COMMENT '用途ID',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '模板描述',
    `current_version` INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_usage_id` (`usage_id`),
    KEY `idx_deleted` (`deleted`),
    CONSTRAINT `fk_template_usage` FOREIGN KEY (`usage_id`) REFERENCES `template_usage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

-- 3. Prompt Template Version Table (模板版本表)
CREATE TABLE IF NOT EXISTS `prompt_template_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `version_number` INT NOT NULL COMMENT '版本号',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `change_note` VARCHAR(500) DEFAULT NULL COMMENT '变更说明',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_version` (`template_id`, `version_number`),
    KEY `idx_template_created` (`template_id`, `created_at` DESC),
    CONSTRAINT `fk_version_template` FOREIGN KEY (`template_id`) REFERENCES `prompt_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板版本表';
