-- 移除 LLM 服务配置表
-- Feature: 001-remove-llm-service
-- Date: 2025-12-25
-- Reason: LLM 服务管理功能不再需要

DROP TABLE IF EXISTS llm_service_config;
