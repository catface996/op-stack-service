-- ==============================================
-- 数据库表结构合规性重构迁移脚本
-- Feature: 033-database-schema-compliance
-- Date: 2025-12-28
--
-- 变更内容:
-- 1. 表重命名: reports -> report, report_templates -> report_template
-- 2. 添加缺失的审计字段: created_by, updated_by
-- 3. 添加缺失的软删除字段: deleted
-- 4. 修复主键 COMMENT
-- ==============================================

SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 表重命名 ==========
RENAME TABLE reports TO report;
RENAME TABLE report_templates TO report_template;

-- ========== 2. report 表变更 ==========
-- 添加 created_by 字段 (在 topology_id 之后)
ALTER TABLE report ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER topology_id;
-- 添加 updated_by 字段 (在 deleted 之后)
ALTER TABLE report ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER deleted;
-- 添加 updated_at 字段 (在 created_at 之后)
ALTER TABLE report ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at;
-- 修复主键 COMMENT
ALTER TABLE report MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 3. report_template 表变更 ==========
-- 添加 created_by 字段 (在 tags 之后)
ALTER TABLE report_template ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER tags;
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE report_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
-- 修复主键 COMMENT
ALTER TABLE report_template MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 4. node 表变更 ==========
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
-- 添加 deleted 字段 (在 version 之后)
ALTER TABLE node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;
-- 修复主键 COMMENT
ALTER TABLE node MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 5. node_type 表变更 ==========
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE node_type ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
-- 添加 deleted 字段 (在 updated_at 之后)
ALTER TABLE node_type ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;

-- ========== 6. node_2_node 表变更 ==========
-- 添加 created_by 字段 (在 description 之后)
ALTER TABLE node_2_node ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE node_2_node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
-- 添加 deleted 字段 (在 updated_at 之后)
ALTER TABLE node_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;

-- ========== 7. topology 表变更 ==========
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE topology ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
-- 添加 deleted 字段 (在 version 之后)
ALTER TABLE topology ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;
-- 修复主键 COMMENT
ALTER TABLE topology MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 8. topology_2_node 表变更 ==========
-- 添加 deleted 字段 (在 added_by 之后)
ALTER TABLE topology_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER added_by;

-- ========== 9. agent 表变更 ==========
-- 添加 created_by 字段 (在 critical 之后)
ALTER TABLE agent ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER critical;
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE agent ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

-- ========== 10. template_usage 表变更 ==========
-- 添加 created_by 字段 (在 description 之后)
ALTER TABLE template_usage ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE template_usage ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

-- ========== 11. prompt_template 表变更 ==========
-- 添加 updated_by 字段 (在 created_at 之后)
ALTER TABLE prompt_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

SET FOREIGN_KEY_CHECKS = 1;
