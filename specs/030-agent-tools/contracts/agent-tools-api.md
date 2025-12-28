# API Contract: Agent Tools 绑定

**Feature**: 030-agent-tools | **Date**: 2025-12-28

## 概述

本特性不新增 API 端点，而是扩展现有 Agent 管理接口以支持 Tools 绑定。

## 修改的端点

### POST /api/service/v1/agents/create

**变更**：请求体新增 `toolIds` 字段

#### 请求体（扩展）

```json
{
  "name": "string (required)",
  "role": "string (required): GLOBAL_SUPERVISOR | TEAM_SUPERVISOR | WORKER | SCOUTER",
  "specialty": "string (optional)",
  "promptTemplateId": "number (optional)",
  "model": "string (optional)",
  "temperature": "number (optional): 0.0-2.0",
  "topP": "number (optional): 0.0-1.0",
  "maxTokens": "number (optional)",
  "maxRuntime": "number (optional)",
  "toolIds": ["number (optional): Tool ID 列表"]
}
```

#### 示例请求

```json
{
  "name": "Diagnostic Agent",
  "role": "WORKER",
  "specialty": "系统诊断",
  "model": "claude-3-opus",
  "toolIds": [1, 2, 3]
}
```

#### 响应体（扩展）

```json
{
  "code": 0,
  "message": "Agent 创建成功",
  "data": {
    "id": 1,
    "name": "Diagnostic Agent",
    "role": "WORKER",
    "specialty": "系统诊断",
    "promptTemplateId": null,
    "promptTemplateName": null,
    "model": "claude-3-opus",
    "temperature": 0.3,
    "topP": 0.9,
    "maxTokens": 4096,
    "maxRuntime": 300,
    "warnings": 0,
    "critical": 0,
    "teamIds": [],
    "toolIds": [1, 2, 3],
    "createdAt": "2025-12-28T10:00:00",
    "updatedAt": "2025-12-28T10:00:00"
  }
}
```

---

### POST /api/service/v1/agents/update

**变更**：请求体新增 `toolIds` 字段（全量替换模式）

#### 请求体（扩展）

```json
{
  "id": "number (required)",
  "name": "string (optional)",
  "specialty": "string (optional)",
  "promptTemplateId": "number (optional)",
  "model": "string (optional)",
  "temperature": "number (optional): 0.0-2.0",
  "topP": "number (optional): 0.0-1.0",
  "maxTokens": "number (optional)",
  "maxRuntime": "number (optional)",
  "toolIds": ["number (optional): Tool ID 列表，全量替换"]
}
```

#### 示例请求 - 更新 Tools 绑定

```json
{
  "id": 1,
  "toolIds": [4, 5]
}
```

#### 示例请求 - 清空 Tools 绑定

```json
{
  "id": 1,
  "toolIds": []
}
```

#### 业务规则

| 规则 | 说明 |
|------|------|
| 全量替换 | 每次传入 toolIds 将完全替换原有绑定 |
| 空列表清空 | `toolIds: []` 清空所有绑定 |
| 不传不更新 | 不传 toolIds 字段则保持原有绑定 |
| 去重处理 | 自动去除重复的 Tool ID |
| 无效 ID 过滤 | 不存在的 Tool ID 被静默忽略 |
| 状态限制 | Agent 处于 WORKING/THINKING 状态时禁止更新 |

---

### POST /api/service/v1/agents/get

**变更**：响应体新增 `toolIds` 字段

#### 响应体（扩展）

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Diagnostic Agent",
    "role": "WORKER",
    "specialty": "系统诊断",
    "promptTemplateId": 10,
    "promptTemplateName": "诊断模板",
    "model": "claude-3-opus",
    "temperature": 0.3,
    "topP": 0.9,
    "maxTokens": 4096,
    "maxRuntime": 300,
    "warnings": 5,
    "critical": 1,
    "teamIds": [100, 200],
    "toolIds": [1, 2, 3],
    "createdAt": "2025-12-28T10:00:00",
    "updatedAt": "2025-12-28T12:00:00"
  }
}
```

---

### POST /api/service/v1/agents/list

**变更**：响应体中每个 Agent 对象新增 `toolIds` 字段

#### 响应体（扩展）

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "Diagnostic Agent",
        "role": "WORKER",
        "toolIds": [1, 2, 3],
        ...
      },
      {
        "id": 2,
        "name": "Monitor Agent",
        "role": "SCOUTER",
        "toolIds": [],
        ...
      }
    ],
    "page": 1,
    "size": 10,
    "total": 2
  }
}
```

## 错误码

本特性复用现有错误码，无新增。

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| AGENT_NOT_FOUND | 404 | Agent 不存在 |
| AGENT_IS_BUSY | 423 | Agent 正在工作中，无法更新 |
| AGENT_NAME_EXISTS | 409 | Agent 名称已存在 |

## 测试用例

### TC-001: 创建 Agent 时绑定 Tools

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Agent",
    "role": "WORKER",
    "toolIds": [1, 2, 3]
  }'
```

**预期**：返回 201，响应中 `toolIds` 为 `[1, 2, 3]`

### TC-002: 更新 Agent 全量替换 Tools

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [4, 5]
  }'
```

**预期**：返回 200，响应中 `toolIds` 为 `[4, 5]`

### TC-003: 清空 Agent Tools 绑定

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": []
  }'
```

**预期**：返回 200，响应中 `toolIds` 为 `[]`

### TC-004: 查询 Agent 包含 toolIds

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/get \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

**预期**：返回 200，响应中包含 `toolIds` 字段

### TC-005: 无效 Tool ID 被过滤

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [1, 999, 2]
  }'
```

**预期**：返回 200，假设 Tool ID 999 不存在，响应中 `toolIds` 为 `[1, 2]`

### TC-006: 重复 Tool ID 去重

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [1, 2, 1, 3, 2]
  }'
```

**预期**：返回 200，响应中 `toolIds` 为 `[1, 2, 3]`（去重后）
