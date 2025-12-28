# Quickstart: Node-Agent 绑定功能测试

**Feature**: 031-node-agent-binding
**Date**: 2025-12-28

## Prerequisites

1. 应用已启动并运行在 http://localhost:8081
2. 数据库已初始化
3. 存在至少一个 Node 和一个 Agent

## Test Data Setup

### 1. 创建测试 Agent（如果不存在）

```bash
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TestMonitorAgent",
    "role": "WORKER",
    "specialty": "系统监控",
    "operatorId": 1
  }'
```

记录返回的 `agentId`。

### 2. 创建测试 Node（如果不存在）

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TestServer-01",
    "description": "测试服务器",
    "nodeTypeId": 1,
    "operatorId": 1
  }'
```

记录返回的 `nodeId`。

## Test Scenarios

### Scenario 1: 绑定 Agent 到节点 (US1)

**预期**: 绑定成功，返回关联记录

```bash
# 绑定 Agent (id=1) 到 Node (id=1)
curl -X POST http://localhost:8081/api/service/v1/nodes/bindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1,
    "agentId": 1
  }'
```

**预期响应** (HTTP 201):
```json
{
  "code": 0,
  "message": "绑定成功",
  "success": true,
  "data": {
    "id": 1,
    "nodeId": 1,
    "agentId": 1,
    "createdAt": "2025-12-28T..."
  }
}
```

### Scenario 2: 重复绑定 (US1 - 边缘情况)

**预期**: 提示绑定关系已存在

```bash
# 再次绑定相同的 Agent 到相同的 Node
curl -X POST http://localhost:8081/api/service/v1/nodes/bindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1,
    "agentId": 1
  }'
```

**预期响应** (HTTP 409):
```json
{
  "code": 409001,
  "message": "该节点与Agent已绑定",
  "success": false,
  "data": null
}
```

### Scenario 3: 绑定不存在的 Node

**预期**: 返回节点不存在错误

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/bindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 99999,
    "agentId": 1
  }'
```

**预期响应** (HTTP 404):
```json
{
  "code": 404001,
  "message": "节点不存在",
  "success": false,
  "data": null
}
```

### Scenario 4: 绑定不存在的 Agent

**预期**: 返回 Agent 不存在错误

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/bindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1,
    "agentId": 99999
  }'
```

**预期响应** (HTTP 404):
```json
{
  "code": 404002,
  "message": "Agent不存在",
  "success": false,
  "data": null
}
```

### Scenario 5: 查询节点关联的 Agent 列表 (US2)

**预期**: 返回关联的 Agent 列表

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/listAgents \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "TestMonitorAgent",
      "role": "WORKER",
      "specialty": "系统监控"
    }
  ]
}
```

### Scenario 6: 查询无关联的节点 (US2 - 边缘情况)

**预期**: 返回空列表

```bash
# 假设 nodeId=2 无任何关联
curl -X POST http://localhost:8081/api/service/v1/nodes/listAgents \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 2
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": []
}
```

### Scenario 7: 查询 Agent 关联的节点列表 (US3)

**预期**: 返回关联的节点列表

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/listNodesByAgent \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 1
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "TestServer-01",
      "description": "测试服务器",
      "nodeTypeName": "SERVER",
      "status": "RUNNING"
    }
  ]
}
```

### Scenario 8: 解除绑定关系 (US4)

**预期**: 解绑成功

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/unbindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1,
    "agentId": 1
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 0,
  "message": "解绑成功",
  "success": true,
  "data": null
}
```

### Scenario 9: 解绑后验证

**预期**: 关联列表为空

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/listAgents \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": []
}
```

### Scenario 10: 解绑不存在的关系 (US4 - 边缘情况)

**预期**: 返回关联关系不存在错误

```bash
curl -X POST http://localhost:8081/api/service/v1/nodes/unbindAgent \
  -H "Content-Type: application/json" \
  -d '{
    "nodeId": 1,
    "agentId": 1
  }'
```

**预期响应** (HTTP 404):
```json
{
  "code": 404003,
  "message": "绑定关系不存在",
  "success": false,
  "data": null
}
```

## Quick Verification Script

```bash
#!/bin/bash
# quick-test.sh - Node-Agent 绑定功能快速测试脚本

BASE_URL="http://localhost:8081/api/service/v1"
NODE_ID=1
AGENT_ID=1

echo "=== 1. 绑定 Agent 到 Node ==="
curl -s -X POST "$BASE_URL/nodes/bindAgent" \
  -H "Content-Type: application/json" \
  -d "{\"nodeId\": $NODE_ID, \"agentId\": $AGENT_ID}" | jq

echo -e "\n=== 2. 查询节点的 Agent 列表 ==="
curl -s -X POST "$BASE_URL/nodes/listAgents" \
  -H "Content-Type: application/json" \
  -d "{\"nodeId\": $NODE_ID}" | jq

echo -e "\n=== 3. 查询 Agent 的节点列表 ==="
curl -s -X POST "$BASE_URL/nodes/listNodesByAgent" \
  -H "Content-Type: application/json" \
  -d "{\"agentId\": $AGENT_ID}" | jq

echo -e "\n=== 4. 解除绑定 ==="
curl -s -X POST "$BASE_URL/nodes/unbindAgent" \
  -H "Content-Type: application/json" \
  -d "{\"nodeId\": $NODE_ID, \"agentId\": $AGENT_ID}" | jq

echo -e "\n=== 5. 验证解绑后列表 ==="
curl -s -X POST "$BASE_URL/nodes/listAgents" \
  -H "Content-Type: application/json" \
  -d "{\"nodeId\": $NODE_ID}" | jq

echo -e "\n测试完成！"
```

## Database Verification

```sql
-- 查看所有绑定关系
SELECT * FROM node_2_agent;

-- 查看特定节点的绑定关系
SELECT na.*, n.name as node_name, a.name as agent_name
FROM node_2_agent na
JOIN node n ON na.node_id = n.id
JOIN agent a ON na.agent_id = a.id
WHERE na.node_id = 1 AND na.deleted = 0;

-- 统计每个节点的 Agent 数量
SELECT n.id, n.name, COUNT(na.id) as agent_count
FROM node n
LEFT JOIN node_2_agent na ON n.id = na.node_id AND na.deleted = 0
GROUP BY n.id, n.name;
```

## Success Criteria Checklist

| Criteria | Test Scenario | Status |
|----------|---------------|--------|
| SC-001: 绑定操作 < 3秒 | Scenario 1 | ⬜ |
| SC-002: 查询节点Agent列表 < 1秒 | Scenario 5 | ⬜ |
| SC-003: 查询Agent节点列表 < 1秒 | Scenario 7 | ⬜ |
| SC-004: 单节点支持100+ Agent | 批量测试 | ⬜ |
| SC-005: 单Agent支持1000+ 节点 | 批量测试 | ⬜ |
