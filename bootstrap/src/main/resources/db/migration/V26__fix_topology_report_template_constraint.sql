-- =====================================================
-- V26: 修复拓扑图-报告模板绑定关系表约束
-- 问题: 软删除与唯一索引冲突，改为物理删除
-- Date: 2025-12-29
-- =====================================================

-- 1. 删除所有软删除的记录（物理删除）
DELETE FROM topology_2_report_template WHERE deleted = 1;

-- 2. 删除包含 deleted 的唯一索引
ALTER TABLE topology_2_report_template DROP INDEX uk_topology_template;

-- 3. 创建不包含 deleted 的唯一索引
ALTER TABLE topology_2_report_template ADD UNIQUE KEY uk_topology_template (topology_id, report_template_id);

-- 4. 删除 deleted 列（关联表使用物理删除）
ALTER TABLE topology_2_report_template DROP COLUMN deleted;
