# API Contracts: 移除LLM服务管理功能

**Feature**: 001-remove-llm-service
**Date**: 2025-12-25

## Overview

本功能为移除操作，不创建新的 API 契约。以下记录需要移除的现有接口。

## Endpoints to Remove

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/llm-services/query` | 查询 LLM 服务列表 |
| POST | `/api/v1/llm-services/create` | 创建 LLM 服务 |
| POST | `/api/v1/llm-services/get` | 获取 LLM 服务详情 |
| POST | `/api/v1/llm-services/update` | 更新 LLM 服务 |
| POST | `/api/v1/llm-services/delete` | 删除 LLM 服务 |
| POST | `/api/v1/llm-services/update-status` | 更新服务状态 |
| POST | `/api/v1/llm-services/set-default` | 设置默认服务 |

## Expected Behavior After Removal

所有上述接口调用应返回 `404 Not Found`。

## Swagger Tag to Remove

- `LLM 服务管理` - 该标签及其下所有接口将从 Swagger 文档中消失
