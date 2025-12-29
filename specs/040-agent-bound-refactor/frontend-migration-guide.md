# 前端重构指导：Agent 绑定 API 迁移

## 概述

Feature 040 将分散的 Agent 绑定 API 统一为单一入口 `/api/service/v1/agent-bounds/*`，简化前端调用逻辑。

### 变更原因

1. **统一入口**：原来绑定 Global Supervisor 和 Team Agent 需要调用不同的 API，现在统一
2. **简化逻辑**：后端自动根据 Agent 的 hierarchyLevel 判断绑定类型
3. **增强查询**：新增按 Agent 查询、层级结构查询等 API

---

## API 变更对照表

### 废弃的 API（已删除）

| 旧 API | 用途 | 替代方案 |
|--------|------|----------|
| `POST /topologies/bind-supervisor` | 绑定 Global Supervisor 到拓扑图 | `/agent-bounds/bind` |
| `POST /topologies/unbind-supervisor` | 解绑 Global Supervisor | `/agent-bounds/unbind` |
| `POST /nodes/bindAgent` | 绑定 Agent 到节点 | `/agent-bounds/bind` |
| `POST /nodes/unbindAgent` | 解绑 Agent 与节点 | `/agent-bounds/unbind` |
| `POST /nodes/listAgents` | 查询节点的 Agent 列表 | `/agent-bounds/query-by-entity` |
| `POST /nodes/listNodesByAgent` | 查询 Agent 绑定的节点 | `/agent-bounds/query-by-agent` |
| `POST /nodes/listUnboundAgents` | 查询未绑定的 Agent | 使用 Agent 列表 + 过滤 |

### 新 API

| 新 API | 用途 |
|--------|------|
| `POST /api/service/v1/agent-bounds/bind` | 统一绑定接口 |
| `POST /api/service/v1/agent-bounds/unbind` | 统一解绑接口 |
| `POST /api/service/v1/agent-bounds/query-by-entity` | 按实体查询绑定 |
| `POST /api/service/v1/agent-bounds/query-by-agent` | 按 Agent 查询绑定 |
| `POST /api/service/v1/agent-bounds/query-hierarchy` | 查询拓扑图层级结构 |

---

## 详细迁移指南

### 1. 绑定 Global Supervisor 到 Topology

**旧代码：**
```typescript
// 旧 API - 已废弃
const response = await fetch('/api/service/v1/topologies/bind-supervisor', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    topologyId: 4,
    agentId: 7,
    operatorId: 1
  })
});
```

**新代码：**
```typescript
// 新 API
const response = await fetch('/api/service/v1/agent-bounds/bind', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    agentId: 7,           // Agent ID
    entityId: 4,          // Topology ID
    entityType: 'TOPOLOGY' // 实体类型
  })
});

// 响应
{
  "code": 0,
  "message": "绑定成功",
  "data": {
    "id": 1,
    "agentId": 7,
    "agentName": "Global Supervisor Agent",
    "agentRole": "GLOBAL_SUPERVISOR",
    "hierarchyLevel": "GLOBAL_SUPERVISOR",
    "entityId": 4,
    "entityType": "TOPOLOGY",
    "entityName": "用户中心",
    "createdAt": "2025-12-30T00:15:31"
  },
  "success": true
}
```

---

### 2. 绑定 Team Supervisor/Worker 到 Node

**旧代码：**
```typescript
// 旧 API - 已废弃
const response = await fetch('/api/service/v1/nodes/bindAgent', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    nodeId: 8,
    agentId: 2,
    operatorId: 1
  })
});
```

**新代码：**
```typescript
// 新 API - 绑定 Team Supervisor 或 Worker 到 Node
const response = await fetch('/api/service/v1/agent-bounds/bind', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    agentId: 2,        // Agent ID (后端自动识别层级)
    entityId: 8,       // Node ID
    entityType: 'NODE' // 实体类型
  })
});
```

> **注意**：后端会根据 Agent 的 `hierarchyLevel` 自动判断是 Supervisor 还是 Worker：
> - `TEAM_SUPERVISOR` → 替换绑定（一个 Node 只能有一个 Supervisor）
> - `TEAM_WORKER` → 追加绑定（一个 Node 可以有多个 Worker）

---

### 3. 解绑 Agent

**旧代码：**
```typescript
// 旧 API（Topology）- 已废弃
await fetch('/api/service/v1/topologies/unbind-supervisor', {
  method: 'POST',
  body: JSON.stringify({ topologyId: 4, operatorId: 1 })
});

// 旧 API（Node）- 已废弃
await fetch('/api/service/v1/nodes/unbindAgent', {
  method: 'POST',
  body: JSON.stringify({ nodeId: 8, agentId: 2, operatorId: 1 })
});
```

**新代码：**
```typescript
// 新 API - 统一解绑
const response = await fetch('/api/service/v1/agent-bounds/unbind', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    agentId: 7,            // Agent ID
    entityId: 4,           // Entity ID (Topology 或 Node)
    entityType: 'TOPOLOGY' // 'TOPOLOGY' 或 'NODE'
  })
});

// 响应
{
  "code": 0,
  "message": "解绑成功",
  "data": null,
  "success": true
}
```

---

### 4. 查询实体绑定的 Agent 列表

**旧代码：**
```typescript
// 旧 API - 已废弃
const response = await fetch('/api/service/v1/nodes/listAgents', {
  method: 'POST',
  body: JSON.stringify({ nodeId: 8, operatorId: 1 })
});
```

**新代码：**
```typescript
// 新 API - 查询 Node 绑定的所有 Agent
const response = await fetch('/api/service/v1/agent-bounds/query-by-entity', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    entityType: 'NODE',
    entityId: 8
  })
});

// 响应
{
  "code": 0,
  "data": [
    {
      "id": 35,
      "agentId": 2,
      "agentName": "Team Lead Alpha",
      "hierarchyLevel": "TEAM_SUPERVISOR",
      "entityId": 8,
      "entityType": "NODE"
    },
    {
      "id": 5,
      "agentId": 1,
      "agentName": "Security Expert",
      "hierarchyLevel": "TEAM_WORKER",
      "entityId": 8,
      "entityType": "NODE"
    }
  ],
  "success": true
}
```

**按层级过滤（可选）：**
```typescript
// 只查询 Supervisor
const response = await fetch('/api/service/v1/agent-bounds/query-by-entity', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    entityType: 'NODE',
    entityId: 8,
    hierarchyLevel: 'TEAM_SUPERVISOR'  // 可选过滤
  })
});
```

---

### 5. 查询 Topology 绑定的 Global Supervisor

**旧代码：**
```typescript
// 旧方式：从 Topology 详情中获取
const topology = await fetch('/api/service/v1/topologies/get', {
  method: 'POST',
  body: JSON.stringify({ id: 4 })
});
const globalSupervisorId = topology.data.globalSupervisorAgentId;
```

**新代码：**
```typescript
// 新 API - 直接查询绑定
const response = await fetch('/api/service/v1/agent-bounds/query-by-entity', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    entityType: 'TOPOLOGY',
    entityId: 4
  })
});

// 响应
{
  "code": 0,
  "data": [
    {
      "agentId": 7,
      "agentName": "Global Supervisor Agent",
      "hierarchyLevel": "GLOBAL_SUPERVISOR",
      "entityId": 4,
      "entityType": "TOPOLOGY"
    }
  ],
  "success": true
}
```

---

### 6. 查询 Agent 绑定的所有实体

**旧代码：**
```typescript
// 旧 API - 已废弃
const response = await fetch('/api/service/v1/nodes/listNodesByAgent', {
  method: 'POST',
  body: JSON.stringify({ agentId: 1 })
});
```

**新代码：**
```typescript
// 新 API
const response = await fetch('/api/service/v1/agent-bounds/query-by-agent', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    agentId: 1,
    entityType: 'NODE'  // 可选：过滤实体类型
  })
});

// 响应
{
  "code": 0,
  "data": [
    {
      "agentId": 1,
      "agentName": "Security Expert",
      "hierarchyLevel": "TEAM_WORKER",
      "entityId": 8,
      "entityType": "NODE",
      "entityName": "ecom-gateway"
    },
    {
      "agentId": 1,
      "agentName": "Security Expert",
      "hierarchyLevel": "TEAM_WORKER",
      "entityId": 42,
      "entityType": "NODE",
      "entityName": "支付路由数据库-MySQL"
    }
  ],
  "success": true
}
```

---

### 7. 查询拓扑图层级结构（新功能）

这是新增的 API，用于一次性获取拓扑图的完整 Agent 团队结构。

```typescript
const response = await fetch('/api/service/v1/agent-bounds/query-hierarchy', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    topologyId: 5
  })
});

// 响应
{
  "code": 0,
  "data": {
    "topologyId": 5,
    "topologyName": "交易中心",
    "globalSupervisor": {
      "id": 7,
      "name": "Global Supervisor Agent",
      "role": "GLOBAL_SUPERVISOR",
      "hierarchyLevel": "GLOBAL_SUPERVISOR"
    },
    "teams": [
      {
        "nodeId": 8,
        "nodeName": "ecom-gateway",
        "supervisor": {
          "id": 2,
          "name": "Team Lead Alpha",
          "hierarchyLevel": "TEAM_SUPERVISOR"
        },
        "workers": [
          {
            "id": 1,
            "name": "Security Expert",
            "hierarchyLevel": "TEAM_WORKER"
          },
          {
            "id": 4,
            "name": "Test Agent for Tools",
            "hierarchyLevel": "TEAM_WORKER"
          }
        ]
      },
      {
        "nodeId": 43,
        "nodeName": "巴拉巴数据库-MySQL",
        "supervisor": {
          "id": 11,
          "name": "Test Team Supervisor Agent",
          "hierarchyLevel": "TEAM_SUPERVISOR"
        },
        "workers": []
      }
    ]
  },
  "success": true
}
```

---

## 请求/响应 DTO 参考

### BindAgentRequest

```typescript
interface BindAgentRequest {
  agentId: number;      // 必填：Agent ID
  entityId: number;     // 必填：实体 ID（Topology 或 Node）
  entityType: string;   // 必填：'TOPOLOGY' 或 'NODE'
}
```

### QueryByEntityRequest

```typescript
interface QueryByEntityRequest {
  entityType: string;       // 必填：'TOPOLOGY' 或 'NODE'
  entityId: number;         // 必填：实体 ID
  hierarchyLevel?: string;  // 可选：'GLOBAL_SUPERVISOR' | 'TEAM_SUPERVISOR' | 'TEAM_WORKER'
}
```

### QueryByAgentRequest

```typescript
interface QueryByAgentRequest {
  agentId: number;       // 必填：Agent ID
  entityType?: string;   // 可选：'TOPOLOGY' 或 'NODE'
}
```

### QueryHierarchyRequest

```typescript
interface QueryHierarchyRequest {
  topologyId: number;    // 必填：Topology ID
}
```

### AgentBoundDTO（响应）

```typescript
interface AgentBoundDTO {
  id: number;
  agentId: number;
  agentName: string;
  agentRole: string;
  hierarchyLevel: string;
  entityId: number;
  entityType: string;
  entityName: string | null;
  createdAt: string;
}
```

### HierarchyStructureDTO（响应）

```typescript
interface HierarchyStructureDTO {
  topologyId: number;
  topologyName: string;
  globalSupervisor: AgentDTO | null;
  teams: HierarchyTeamDTO[];
}

interface HierarchyTeamDTO {
  nodeId: number;
  nodeName: string;
  supervisor: AgentDTO | null;
  workers: AgentDTO[];
}

interface AgentDTO {
  id: number;
  name: string;
  role: string;
  hierarchyLevel: string;
  specialty?: string;
  model?: string;
  // ... 其他字段
}
```

---

## 业务规则变更

### 绑定规则

| Agent 层级 | 可绑定实体 | 绑定模式 |
|------------|-----------|----------|
| `GLOBAL_SUPERVISOR` | `TOPOLOGY` | 替换（一个 Topology 只能有一个） |
| `TEAM_SUPERVISOR` | `NODE` | 替换（一个 Node 只能有一个） |
| `TEAM_WORKER` | `NODE` | 追加（一个 Node 可以有多个） |

### 错误处理

| 错误场景 | 错误码 | 错误信息 |
|---------|--------|----------|
| Agent 不存在 | 500000 | "Agent 不存在: {id}" |
| 层级不匹配 | 500002 | "Agent 层级 X 不能绑定到 Y" |
| Topology 不存在 | 400 | "Topology 不存在: {id}" |
| 无效实体类型 | 400 | "无效的实体类型: {type}" |

---

## 迁移检查清单

- [ ] 替换所有 `/topologies/bind-supervisor` 调用为 `/agent-bounds/bind`
- [ ] 替换所有 `/topologies/unbind-supervisor` 调用为 `/agent-bounds/unbind`
- [ ] 替换所有 `/nodes/bindAgent` 调用为 `/agent-bounds/bind`
- [ ] 替换所有 `/nodes/unbindAgent` 调用为 `/agent-bounds/unbind`
- [ ] 替换所有 `/nodes/listAgents` 调用为 `/agent-bounds/query-by-entity`
- [ ] 替换所有 `/nodes/listNodesByAgent` 调用为 `/agent-bounds/query-by-agent`
- [ ] 更新请求体结构（使用 `entityId` + `entityType` 替代 `topologyId`/`nodeId`）
- [ ] 更新响应处理逻辑以适配新的 DTO 结构
- [ ] 移除 `operatorId` 参数（新 API 不需要）
- [ ] 考虑使用新的 `/query-hierarchy` API 优化层级结构加载

---

## 联系方式

如有问题，请联系后端开发团队。
