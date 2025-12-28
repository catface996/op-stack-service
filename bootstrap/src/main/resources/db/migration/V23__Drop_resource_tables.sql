-- ==============================================
-- 移除 Resource 资源管理相关表
-- Feature: 001-remove-resource-api
-- Date: 2025-12-28
-- ==============================================
-- 说明：
-- V12 迁移已将 resource 表拆分为 topology 和 node，
-- 并且已删除 resource 和 resource_relationship 表，
-- 同时 resource_type 已被重命名为 node_type。
--
-- 此迁移用于清理任何可能遗留的表结构：
-- - resource_relationship（应已被 V12 删除）
-- - resource（应已被 V12 删除）
-- - resource_type（应已被 V12 重命名为 node_type）
--
-- 使用 IF EXISTS 确保幂等性。
-- ==============================================

-- 禁用外键检查（安全删除）
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 删除 resource_relationship 表（如果存在）
DROP TABLE IF EXISTS resource_relationship;

-- 2. 删除 resource 表（如果存在）
DROP TABLE IF EXISTS resource;

-- 3. 删除 resource_type 表（如果存在，应该不存在因为已重命名为 node_type）
DROP TABLE IF EXISTS resource_type;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
