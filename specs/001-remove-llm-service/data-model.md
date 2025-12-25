# Data Model: 移除LLM服务管理功能

**Feature**: 001-remove-llm-service
**Date**: 2025-12-25

## Overview

本功能为移除操作，不创建新的数据模型。以下记录需要删除的现有数据模型。

## Entities to Remove

### LlmService (Domain Model)

**Location**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/LlmService.java`

**Status**: TO BE REMOVED

### LlmServiceEntity (Repository Entity)

**Location**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/entity/LlmServiceEntity.java`

**Status**: TO BE REMOVED

### LlmServicePO (Persistence Object)

**Location**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/llm/LlmServicePO.java`

**Status**: TO BE REMOVED

## Database Table to Drop

### llm_service_config

```sql
-- V9__Drop_llm_service_table.sql
DROP TABLE IF EXISTS llm_service_config;
```

**Reason**: LLM 服务管理功能不再需要，表及数据可安全删除。

## Migration Script

新建迁移脚本: `V9__Drop_llm_service_table.sql`

```sql
-- 移除 LLM 服务配置表
-- Feature: 001-remove-llm-service
-- Date: 2025-12-25

DROP TABLE IF EXISTS llm_service_config;
```
