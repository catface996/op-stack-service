# API Contract: 解除 Agent 与节点的绑定

## Endpoint

**POST** `/api/service/v1/nodes/unbindAgent`

## Description

解除指定 Agent 与指定资源节点的绑定关系。解绑后，该 Agent 将不再监控该节点。

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

### Success (200 OK)

```json
{
  "code": 0,
  "message": "解绑成功",
  "success": true,
  "data": null
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

#### 404 Not Found - 绑定关系不存在

```json
{
  "code": 404003,
  "message": "绑定关系不存在",
  "success": false,
  "data": null
}
```

## Validation Rules

1. `nodeId` 不能为空
2. `agentId` 不能为空
3. 必须存在对应的未删除绑定关系

## Example

### Request

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/unbindAgent \
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
  "message": "解绑成功",
  "success": true,
  "data": null
}
```

## Traceability

- **User Story**: US4 - 解除绑定关系
- **Functional Requirement**: FR-003
