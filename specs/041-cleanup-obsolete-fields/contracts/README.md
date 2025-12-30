# Contracts: 清理数据库废弃字段

**Feature**: 041-cleanup-obsolete-fields
**Date**: 2025-12-30

## 概述

本功能为代码清理任务，不涉及新增 API。现有 API 的变更是移除废弃字段，属于向后兼容的简化（已确认无外部系统依赖）。

## API 变更摘要

### Node API

| 接口 | 变更 |
|------|------|
| POST /api/service/v1/nodes/create | Request 移除 `agentTeamId` 字段 |
| POST /api/service/v1/nodes/update | Request 移除 `agentTeamId` 字段 |
| POST /api/service/v1/nodes/query | Response 移除 `agentTeamId` 字段 |
| POST /api/service/v1/nodes/get | Response 移除 `agentTeamId` 字段 |

### Topology API

| 接口 | 变更 |
|------|------|
| POST /api/service/v1/topologies/create | Request 移除 `coordinatorAgentId` 字段 |
| POST /api/service/v1/topologies/update | Request 移除 `coordinatorAgentId` 字段 |
| POST /api/service/v1/topologies/query | Response 移除 `coordinatorAgentId`, `globalSupervisorAgentId` 字段 |
| POST /api/service/v1/topologies/get | Response 移除 `coordinatorAgentId`, `globalSupervisorAgentId` 字段 |

## 无新增 Contract

由于本功能仅涉及移除废弃字段，不新增任何 API 或数据契约，因此 contracts/ 目录下不包含 OpenAPI 规范文件。

## 前端适配

前端已有适配指南：`docs/frontend-adaptation/node-layer-field.md`

对于本次清理，前端需要：

1. **Node 相关组件**：
   - 移除 `agentTeamId` 字段的显示和表单项
   - 移除创建/更新请求中的 `agentTeamId` 参数

2. **Topology 相关组件**：
   - 移除 `coordinatorAgentId` 字段的显示和表单项
   - 移除 `globalSupervisorAgentId` 字段的显示
   - 移除创建/更新请求中的 `coordinatorAgentId` 参数

## 注意事项

- 清理完成后，Swagger UI 将自动更新，移除废弃字段的文档
- 建议前端在后端部署后同步更新，避免发送已移除的字段
