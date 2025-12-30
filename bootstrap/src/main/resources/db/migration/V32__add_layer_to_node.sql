-- =====================================================
-- V32: 为 node 表添加 layer 字段
-- Feature: 资源节点层级标注
-- Date: 2025-12-30
-- Description: 添加 layer 字段标注资源所属架构层级
-- =====================================================

-- 添加 layer 字段
ALTER TABLE node
ADD COLUMN layer VARCHAR(30) NULL COMMENT '架构层级: BUSINESS_SCENARIO, BUSINESS_FLOW, BUSINESS_APPLICATION, MIDDLEWARE, INFRASTRUCTURE'
AFTER status;

-- 添加索引（方便按层级查询）
ALTER TABLE node
ADD INDEX idx_layer (layer);
