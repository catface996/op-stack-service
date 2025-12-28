# API Contract: 查询节点关联的 Agent 列表

## Endpoint

**POST** `/api/service/v1/nodes/listAgents`

## Description

查询指定资源节点关联的所有 Agent 列表。返回完整的 Agent 信息，而非仅 ID。

## Request

### Headers

| Header | Required | Description |
|--------|----------|-------------|
| Content-Type | Yes | application/json |
| Authorization | Yes | Bearer {token} |

### Body

```json
{
  "nodeId": 1
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| nodeId | Long | Yes | 资源节点 ID |

## Response

### Success (200 OK)

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "MonitorAgent",
      "role": "WORKER",
      "specialty": "系统监控",
      "model": "gemini-2.0-flash",
      "temperature": 0.3,
      "topP": 0.9,
      "maxTokens": 4096,
      "createdAt": "2025-12-20T08:00:00",
      "updatedAt": "2025-12-28T10:00:00"
    },
    {
      "id": 2,
      "name": "DiagnosticAgent",
      "role": "WORKER",
      "specialty": "故障诊断",
      "model": "claude-3-opus",
      "temperature": 0.5,
      "topP": 0.9,
      "maxTokens": 8192,
      "createdAt": "2025-12-21T09:00:00",
      "updatedAt": "2025-12-28T11:00:00"
    }
  ]
}
```

### Success (200 OK) - 空列表

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": []
}
```

### Error Responses

#### 400 Bad Request - 参数无效

```json
{
  "code": 400001,
  "message": "节点ID不能为空",
  "success": false,
  "data": null
}
```

## Validation Rules

1. `nodeId` 不能为空

## Example

### Request

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/listAgents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "nodeId": 1
  }'
```

### Response

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "MonitorAgent",
      "role": "WORKER",
      "specialty": "系统监控"
    }
  ]
}
```

## Notes

- 只返回未删除的 Agent
- 只返回未删除的绑定关系对应的 Agent
- 如果节点不存在或无关联 Agent，返回空列表（不报错）

## Traceability

- **User Story**: US2 - 查询节点关联的 Agent 列表
- **Functional Requirement**: FR-004
