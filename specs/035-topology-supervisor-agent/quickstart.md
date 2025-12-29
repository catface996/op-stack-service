# Quickstart: Topology 绑定 Global Supervisor Agent

**Feature**: 035-topology-supervisor-agent
**Date**: 2025-12-29

## 前置条件

1. 应用已启动并运行在 `http://localhost:8081`
2. 数据库已执行 V27 迁移脚本
3. 存在至少一个拓扑图 (topology)
4. 存在至少一个角色为 GLOBAL_SUPERVISOR 的 Agent

## 测试场景

### 场景 1: 绑定 Global Supervisor Agent

**步骤**:

1. 获取一个存在的拓扑图 ID（假设为 43）
2. 获取一个角色为 GLOBAL_SUPERVISOR 的 Agent ID（假设为 1）
3. 调用绑定接口

**请求**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 43,
    "agentId": 1,
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "绑定成功",
  "success": true,
  "data": {
    "id": 43,
    "name": "拓扑图名称",
    "globalSupervisorAgentId": 1,
    "globalSupervisorAgentName": "Agent名称",
    "globalSupervisorAgentRole": "GLOBAL_SUPERVISOR"
  }
}
```

### 场景 2: 验证绑定结果

**请求**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/get \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 43,
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "success": true,
  "data": {
    "id": 43,
    "name": "拓扑图名称",
    "globalSupervisorAgentId": 1,
    "globalSupervisorAgentName": "Agent名称",
    "globalSupervisorAgentRole": "GLOBAL_SUPERVISOR"
  }
}
```

### 场景 3: 解绑 Global Supervisor Agent

**请求**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/unbind \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 43,
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 0,
  "message": "解绑成功",
  "success": true,
  "data": {
    "id": 43,
    "name": "拓扑图名称",
    "globalSupervisorAgentId": null,
    "globalSupervisorAgentName": null,
    "globalSupervisorAgentRole": null
  }
}
```

### 场景 4: 绑定非 GLOBAL_SUPERVISOR 角色的 Agent（预期失败）

**请求**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 43,
    "agentId": 2,
    "operatorId": 1
  }'
```

**预期响应** (假设 Agent 2 的角色不是 GLOBAL_SUPERVISOR):
```json
{
  "code": 400001,
  "message": "Agent 角色不匹配，必须为 GLOBAL_SUPERVISOR",
  "success": false,
  "data": null
}
```

### 场景 5: 绑定不存在的 Agent（预期失败）

**请求**:
```bash
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 43,
    "agentId": 99999,
    "operatorId": 1
  }'
```

**预期响应**:
```json
{
  "code": 404002,
  "message": "Agent 不存在",
  "success": false,
  "data": null
}
```

### 场景 6: 替换绑定

**步骤**:
1. 先绑定 Agent 1
2. 再绑定 Agent 3（假设也是 GLOBAL_SUPERVISOR 角色）
3. 验证 globalSupervisorAgentId 变为 3

**请求**:
```bash
# 第一次绑定
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": 43, "agentId": 1, "operatorId": 1}'

# 第二次绑定（替换）
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": 43, "agentId": 3, "operatorId": 1}'
```

**预期结果**: 第二次绑定后，globalSupervisorAgentId 变为 3

## 数据库验证

### 验证字段添加

```sql
-- 查看表结构
DESCRIBE topology;

-- 应该包含 global_supervisor_agent_id 字段
```

### 验证绑定数据

```sql
-- 查看拓扑图的绑定状态
SELECT id, name, global_supervisor_agent_id
FROM topology
WHERE deleted = 0;

-- 带 Agent 信息的查询
SELECT t.id, t.name, t.global_supervisor_agent_id,
       a.name AS agent_name, a.role AS agent_role
FROM topology t
LEFT JOIN agent a ON t.global_supervisor_agent_id = a.id AND a.deleted = 0
WHERE t.deleted = 0;
```

## 完整测试流程

```bash
# 1. 获取一个 GLOBAL_SUPERVISOR 角色的 Agent ID
# 查询数据库: SELECT id, name FROM agent WHERE role = 'GLOBAL_SUPERVISOR' AND deleted = 0;

# 2. 获取一个拓扑图 ID
# 查询数据库: SELECT id, name FROM topology WHERE deleted = 0 LIMIT 1;

# 3. 执行绑定
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/bind \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": <TOPOLOGY_ID>, "agentId": <AGENT_ID>, "operatorId": 1}'

# 4. 验证绑定
curl -X POST http://localhost:8081/api/service/v1/topologies/get \
  -H 'Content-Type: application/json' \
  -d '{"id": <TOPOLOGY_ID>, "operatorId": 1}'

# 5. 执行解绑
curl -X POST http://localhost:8081/api/service/v1/topologies/supervisor/unbind \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": <TOPOLOGY_ID>, "operatorId": 1}'

# 6. 验证解绑
curl -X POST http://localhost:8081/api/service/v1/topologies/get \
  -H 'Content-Type: application/json' \
  -d '{"id": <TOPOLOGY_ID>, "operatorId": 1}'
```
