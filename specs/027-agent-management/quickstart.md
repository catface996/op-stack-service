# Quickstart: Agent Management API

**Feature**: 027-agent-management
**Date**: 2025-12-28

## Prerequisites

- 服务运行在 `http://localhost:8081`
- 数据库迁移已执行（V15__create_agent_tables.sql）

## API Endpoints Overview

| Endpoint | Description |
|----------|-------------|
| `POST /api/service/v1/agents/list` | 查询 Agent 列表 |
| `POST /api/service/v1/agents/get` | 获取 Agent 详情 |
| `POST /api/service/v1/agents/create` | 创建 Agent |
| `POST /api/service/v1/agents/update` | 更新 Agent 信息 |
| `POST /api/service/v1/agents/config/update` | 更新 Agent 配置 |
| `POST /api/service/v1/agents/delete` | 删除 Agent |
| `POST /api/service/v1/agents/assign` | 分配 Agent 到团队 |
| `POST /api/service/v1/agents/unassign` | 取消 Agent 团队分配 |
| `POST /api/service/v1/agents/templates/list` | 查询 Agent 模板列表 |
| `POST /api/service/v1/agents/stats` | 查询 Agent 统计信息 |

## Usage Examples

### 1. 查询 Agent 列表

```bash
# 基本查询
curl -s -X POST http://localhost:8081/api/service/v1/agents/list \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}' | jq '.'

# 按角色筛选（只查 Worker）
curl -s -X POST http://localhost:8081/api/service/v1/agents/list \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10, "role": "WORKER"}' | jq '.'

# 按状态筛选
curl -s -X POST http://localhost:8081/api/service/v1/agents/list \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10, "status": "IDLE"}' | jq '.'

# 关键词搜索
curl -s -X POST http://localhost:8081/api/service/v1/agents/list \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10, "keyword": "日志分析"}' | jq '.'
```

### 2. 获取 Agent 详情

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/get \
  -H "Content-Type: application/json" \
  -d '{"id": 1}' | jq '.'
```

### 3. 创建 Agent

```bash
# 创建基本 Worker Agent
curl -s -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "日志分析 Agent",
    "role": "WORKER",
    "specialty": "Error Tracking"
  }' | jq '.'

# 创建带配置的 Agent
curl -s -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "安全审计 Agent",
    "role": "WORKER",
    "specialty": "Security Analysis",
    "config": {
      "model": "gemini-2.5-flash",
      "temperature": 0.1,
      "systemInstruction": "You are a security auditor focused on identifying vulnerabilities."
    }
  }' | jq '.'

# 创建 Team Supervisor
curl -s -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "运维团队 Supervisor",
    "role": "TEAM_SUPERVISOR",
    "specialty": "Team Coordination"
  }' | jq '.'
```

### 4. 更新 Agent 信息

```bash
# 更新名称和专业领域
curl -s -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "高级日志分析 Agent",
    "specialty": "Advanced Error Tracking"
  }' | jq '.'

# 更新状态
curl -s -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "status": "IDLE"
  }' | jq '.'
```

### 5. 更新 Agent 配置

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/config/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "config": {
      "model": "gemini-2.5-flash-thinking",
      "temperature": 0.2,
      "systemInstruction": "You are an expert log analyzer with deep knowledge of error patterns."
    }
  }' | jq '.'
```

### 6. 删除 Agent

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/delete \
  -H "Content-Type: application/json" \
  -d '{"id": 1}' | jq '.'
```

### 7. 分配 Agent 到团队

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/assign \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 1,
    "teamId": 100
  }' | jq '.'
```

### 8. 取消 Agent 团队分配

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/unassign \
  -H "Content-Type: application/json" \
  -d '{"agentId": 1}' | jq '.'
```

### 9. 查询 Agent 模板列表

```bash
curl -s -X POST http://localhost:8081/api/service/v1/agents/templates/list \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'
```

### 10. 查询 Agent 统计信息

```bash
# 全局统计
curl -s -X POST http://localhost:8081/api/service/v1/agents/stats \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'

# 指定 Agent 统计
curl -s -X POST http://localhost:8081/api/service/v1/agents/stats \
  -H "Content-Type: application/json" \
  -d '{"agentId": 1}' | jq '.'

# 指定时间范围
curl -s -X POST http://localhost:8081/api/service/v1/agents/stats \
  -H "Content-Type: application/json" \
  -d '{
    "startTime": "2025-01-01T00:00:00",
    "endTime": "2025-12-31T23:59:59"
  }' | jq '.'
```

## Expected Responses

### 成功响应示例

```json
{
  "code": 0,
  "success": true,
  "message": "success",
  "data": {
    "id": 1,
    "name": "日志分析 Agent",
    "role": "WORKER",
    "specialty": "Error Tracking",
    "status": "IDLE",
    "currentTask": null,
    "findings": {
      "warnings": 0,
      "critical": 0
    },
    "config": {
      "model": "gemini-2.0-flash",
      "temperature": 0.3,
      "systemInstruction": "You are a specialized worker agent.",
      "defaultContext": ""
    },
    "teamId": null,
    "createdAt": "2025-12-28T12:00:00",
    "updatedAt": "2025-12-28T12:00:00"
  }
}
```

### 分页响应示例

```json
{
  "code": 0,
  "success": true,
  "message": "success",
  "data": {
    "content": [...],
    "page": 1,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

### 模板列表响应示例

```json
{
  "code": 0,
  "success": true,
  "message": "success",
  "data": [
    {
      "id": "1",
      "name": "Standard Coordinator",
      "description": "通用团队协调模板",
      "systemInstruction": "Coordinate workers, aggregate findings, report status",
      "defaultContext": "",
      "recommendedModel": "gemini-2.0-flash",
      "recommendedTemperature": 0.3
    },
    ...
  ]
}
```

### 统计响应示例

```json
{
  "code": 0,
  "success": true,
  "message": "success",
  "data": {
    "totalAgents": 25,
    "byRole": {
      "GLOBAL_SUPERVISOR": 1,
      "TEAM_SUPERVISOR": 3,
      "WORKER": 20,
      "SCOUTER": 1
    },
    "byStatus": {
      "IDLE": 18,
      "WORKING": 5,
      "ERROR": 2
    },
    "totalExecutions": 0,
    "successRate": 0.0,
    "avgExecutionTime": 0,
    "totalWarnings": 45,
    "totalCritical": 12
  }
}
```

## Error Responses

### 404 - Agent 不存在

```json
{
  "code": 404,
  "success": false,
  "message": "Agent not found: 999",
  "data": null
}
```

### 400 - 参数无效

```json
{
  "code": 400,
  "success": false,
  "message": "Temperature must be between 0.0 and 1.0",
  "data": null
}
```

### 409 - 冲突

```json
{
  "code": 409,
  "success": false,
  "message": "Agent name already exists in the same team",
  "data": null
}
```

### 423 - Agent 正在工作

```json
{
  "code": 423,
  "success": false,
  "message": "Agent is busy (status: WORKING), cannot update or delete",
  "data": null
}
```

## Validation Summary

| Scenario | Expected Result |
|----------|-----------------|
| 创建 Agent 成功 | 201, 返回完整 AgentDTO |
| 创建第二个 GLOBAL_SUPERVISOR | 409, 已存在 |
| 更新 WORKING 状态的 Agent | 423, Agent 正在工作 |
| 删除 GLOBAL_SUPERVISOR | 400, 不允许删除 |
| 删除有成员的 TEAM_SUPERVISOR | 400, 有成员存在 |
| TEAM_SUPERVISOR 取消分配 | 400, Supervisor 不能脱离团队 |
| temperature 超出范围 | 400, 参数无效 |
