# Quickstart: Agent Binding Relationship Refactor

**Feature**: 040-agent-bound-refactor
**Date**: 2025-12-29

## Overview

本指南帮助快速理解和使用统一的 Agent 绑定系统。

## Key Concepts

### 1. 绑定层级 (Hierarchy Level)

| 层级 | 说明 | 可绑定实体 | 数量限制 |
|-----|------|----------|---------|
| GLOBAL_SUPERVISOR | 全局监管者 | TOPOLOGY | 每个 Topology 1个 |
| TEAM_SUPERVISOR | 团队监管者 | NODE | 每个 Node 1个 |
| TEAM_WORKER | 团队工作者 | NODE | 每个 Node 多个 |

### 2. 实体类型 (Entity Type)

| 类型 | 说明 | 对应表 |
|-----|------|-------|
| TOPOLOGY | 拓扑图 | topology |
| NODE | 资源节点 | node |

## Quick Integration Scenarios

### Scenario 1: 绑定 Global Supervisor 到 Topology

```bash
# 创建绑定
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/bind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 1,
    "hierarchyLevel": "GLOBAL_SUPERVISOR",
    "entityId": 100,
    "entityType": "TOPOLOGY"
  }'

# 响应
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "id": 1,
    "agentId": 1,
    "agentName": "Global Supervisor Agent",
    "hierarchyLevel": "GLOBAL_SUPERVISOR",
    "entityId": 100,
    "entityType": "TOPOLOGY",
    "entityName": "My Topology",
    "createdAt": "2025-12-29T10:00:00"
  }
}
```

### Scenario 2: 绑定 Team Supervisor 到 Node

```bash
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/bind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 2,
    "hierarchyLevel": "TEAM_SUPERVISOR",
    "entityId": 200,
    "entityType": "NODE"
  }'
```

### Scenario 3: 绑定 Workers 到 Node

```bash
# 绑定第一个 Worker
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/bind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 3,
    "hierarchyLevel": "TEAM_WORKER",
    "entityId": 200,
    "entityType": "NODE"
  }'

# 绑定第二个 Worker（同一个 Node 可以有多个 Worker）
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/bind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 4,
    "hierarchyLevel": "TEAM_WORKER",
    "entityId": 200,
    "entityType": "NODE"
  }'
```

### Scenario 4: 查询 Topology 的层级团队结构

```bash
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/query-hierarchy \
  -H "Content-Type: application/json" \
  -d '{
    "topologyId": 100
  }'

# 响应
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "topologyId": 100,
    "topologyName": "My Topology",
    "globalSupervisor": {
      "id": 1,
      "agentId": 1,
      "agentName": "Global Supervisor Agent",
      "hierarchyLevel": "GLOBAL_SUPERVISOR",
      "entityId": 100,
      "entityType": "TOPOLOGY"
    },
    "teams": [
      {
        "nodeId": 200,
        "nodeName": "Node A",
        "teamSupervisor": {
          "id": 2,
          "agentId": 2,
          "agentName": "Team Supervisor A",
          "hierarchyLevel": "TEAM_SUPERVISOR"
        },
        "workers": [
          {
            "id": 3,
            "agentId": 3,
            "agentName": "Worker 1",
            "hierarchyLevel": "TEAM_WORKER"
          },
          {
            "id": 4,
            "agentId": 4,
            "agentName": "Worker 2",
            "hierarchyLevel": "TEAM_WORKER"
          }
        ]
      }
    ]
  }
}
```

### Scenario 5: 解除绑定

```bash
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/unbind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 3,
    "entityId": 200,
    "entityType": "NODE"
  }'
```

### Scenario 6: 替换 Supervisor

当对已绑定 Supervisor 的实体再次绑定 Supervisor 时，系统自动替换：

```bash
# Node 200 已有 Team Supervisor (agentId=2)
# 绑定新的 Team Supervisor，旧的会被自动解绑
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/bind \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 5,
    "hierarchyLevel": "TEAM_SUPERVISOR",
    "entityId": 200,
    "entityType": "NODE"
  }'
```

## Error Handling

### Error: Agent 层级不匹配

```json
{
  "code": 400,
  "message": "Agent 层级与绑定层级不匹配：Agent 层级为 TEAM_WORKER，绑定层级为 TEAM_SUPERVISOR",
  "success": false
}
```

### Error: 实体类型不支持该层级

```json
{
  "code": 400,
  "message": "实体类型 NODE 不支持层级 GLOBAL_SUPERVISOR",
  "success": false
}
```

### Error: Agent 不存在

```json
{
  "code": 404,
  "message": "Agent 不存在: 999",
  "success": false
}
```

## Migration Notes

### 数据迁移

部署后，Flyway 会自动执行数据迁移：
1. V29: 创建 `agent_bound` 表
2. V30: 迁移现有绑定数据
   - `topology.global_supervisor_agent_id` → `agent_bound`
   - `node_2_agent` → `agent_bound`

### 验证迁移

```sql
-- 验证 Global Supervisor 迁移
SELECT COUNT(*) as gs_count
FROM agent_bound
WHERE entity_type = 'TOPOLOGY' AND hierarchy_level = 'GLOBAL_SUPERVISOR';

-- 验证 Node Agent 迁移
SELECT COUNT(*) as node_agent_count
FROM agent_bound
WHERE entity_type = 'NODE';

-- 与原表对比
SELECT COUNT(*) as old_gs_count
FROM topology
WHERE global_supervisor_agent_id IS NOT NULL AND deleted = 0;

SELECT COUNT(*) as old_node_agent_count
FROM node_2_agent
WHERE deleted = 0;
```

## Integration with Feature 039

Feature 039 (Trigger Multi-Agent Execution) 使用 `query-hierarchy` API 获取团队结构：

```java
// ExecutionApplicationService
HierarchyStructureDTO hierarchy = agentBoundService.queryHierarchy(topologyId);

// 构建 Executor 层级
GlobalSupervisor gs = createExecutor(hierarchy.getGlobalSupervisor());
for (HierarchyTeamDTO team : hierarchy.getTeams()) {
    TeamSupervisor ts = createExecutor(team.getTeamSupervisor());
    for (AgentBoundDTO worker : team.getWorkers()) {
        ts.addWorker(createExecutor(worker));
    }
    gs.addTeam(ts);
}
```

## Performance Considerations

- 层级查询使用复合索引 `idx_entity (entity_type, entity_id, deleted)`
- 单次查询返回完整团队结构，避免 N+1 问题
- 支持 100 个 Node 的 Topology 在 100ms 内返回
