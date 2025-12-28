# API Contract: 查询 Agent 关联的节点列表

## Endpoint

**POST** `/api/service/v1/nodes/listNodesByAgent`

## Description

查询指定 Agent 关联的所有资源节点列表。返回完整的节点信息，而非仅 ID。

## Request

### Headers

| Header | Required | Description |
|--------|----------|-------------|
| Content-Type | Yes | application/json |
| Authorization | Yes | Bearer {token} |

### Body

```json
{
  "agentId": 1
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| agentId | Long | Yes | Agent ID |

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
      "name": "Web服务器-01",
      "description": "生产环境Web服务器",
      "nodeTypeId": 1,
      "nodeTypeName": "SERVER",
      "status": "RUNNING",
      "attributes": "{}",
      "createdAt": "2025-12-15T08:00:00",
      "updatedAt": "2025-12-28T10:00:00"
    },
    {
      "id": 2,
      "name": "数据库服务器-01",
      "description": "MySQL主库",
      "nodeTypeId": 2,
      "nodeTypeName": "DATABASE",
      "status": "RUNNING",
      "attributes": "{}",
      "createdAt": "2025-12-16T09:00:00",
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
  "message": "AgentID不能为空",
  "success": false,
  "data": null
}
```

## Validation Rules

1. `agentId` 不能为空

## Example

### Request

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/listNodesByAgent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "agentId": 1
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
      "name": "Web服务器-01",
      "nodeTypeName": "SERVER",
      "status": "RUNNING"
    }
  ]
}
```

## Notes

- 只返回未删除的节点
- 只返回未删除的绑定关系对应的节点
- 如果 Agent 不存在或无关联节点，返回空列表（不报错）
- 接口放在 NodeController 下，虽然从 Agent 角度查询

## Traceability

- **User Story**: US3 - 查询 Agent 关联的节点列表
- **Functional Requirement**: FR-005
