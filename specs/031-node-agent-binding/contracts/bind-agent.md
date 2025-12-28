# API Contract: 绑定 Agent 到节点

## Endpoint

**POST** `/api/service/v1/nodes/bindAgent`

## Description

将指定的 Agent 绑定到指定的资源节点。绑定成功后，该 Agent 可以监控和管理该节点。

## Request

### Headers

| Header | Required | Description |
|--------|----------|-------------|
| Content-Type | Yes | application/json |
| Authorization | Yes | Bearer {token} |

### Body

```json
{
  "nodeId": 1,
  "agentId": 2,
  "operatorId": 100
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| nodeId | Long | Yes | 资源节点 ID |
| agentId | Long | Yes | Agent ID |
| operatorId | Long | No | 操作人 ID（网关注入，hidden） |

## Response

### Success (201 Created)

```json
{
  "code": 0,
  "message": "绑定成功",
  "success": true,
  "data": {
    "id": 1,
    "nodeId": 1,
    "agentId": 2,
    "createdAt": "2025-12-28T10:30:00"
  }
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

#### 404 Not Found - 节点不存在

```json
{
  "code": 404001,
  "message": "节点不存在",
  "success": false,
  "data": null
}
```

#### 404 Not Found - Agent 不存在

```json
{
  "code": 404002,
  "message": "Agent不存在",
  "success": false,
  "data": null
}
```

#### 409 Conflict - 绑定关系已存在

```json
{
  "code": 409001,
  "message": "该节点与Agent已绑定",
  "success": false,
  "data": null
}
```

## Validation Rules

1. `nodeId` 必须是有效的、未删除的节点 ID
2. `agentId` 必须是有效的、未删除的 Agent ID
3. 同一 nodeId + agentId 组合不能重复绑定

## Example

### Request

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/bindAgent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "nodeId": 1,
    "agentId": 2
  }'
```

### Response

```json
{
  "code": 0,
  "message": "绑定成功",
  "success": true,
  "data": {
    "id": 1,
    "nodeId": 1,
    "agentId": 2,
    "createdAt": "2025-12-28T10:30:00"
  }
}
```

## Traceability

- **User Story**: US1 - 绑定 Agent 到资源节点
- **Functional Requirement**: FR-002
