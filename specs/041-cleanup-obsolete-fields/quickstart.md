# Quickstart: 清理数据库废弃字段验证指南

**Feature**: 041-cleanup-obsolete-fields
**Date**: 2025-12-30

## 前置条件

- Docker 环境运行中（MySQL 容器）
- 项目可正常编译和启动

## 验证步骤

### 1. 数据完整性验证（执行 P3/P4 前）

```bash
# 连接数据库
docker exec -it aiops-mysql mysql -uroot -proot123 op_stack_service

# 验证 topology.global_supervisor_agent_id 已完全迁移
SELECT COUNT(*) AS unMigrated FROM topology t
WHERE t.global_supervisor_agent_id IS NOT NULL
  AND t.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.entity_type = 'TOPOLOGY'
      AND ab.entity_id = t.id
      AND ab.hierarchy_level = 'GLOBAL_SUPERVISOR'
      AND ab.deleted = 0
  );
-- 预期结果: 0

# 验证 node_2_agent 已完全迁移
SELECT COUNT(*) AS unMigrated FROM node_2_agent n2a
WHERE n2a.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.entity_type = 'NODE'
      AND ab.entity_id = n2a.node_id
      AND ab.agent_id = n2a.agent_id
      AND ab.deleted = 0
  );
-- 预期结果: 0
```

### 2. 编译验证

```bash
# 每个阶段代码修改后执行
mvn clean compile -DskipTests

# 预期结果: BUILD SUCCESS
```

### 3. 启动验证

```bash
# 确保 MySQL 容器运行
docker start aiops-mysql

# 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 预期结果: 应用正常启动，无报错
```

### 4. API 功能测试

#### P1 验证：Node API

```bash
# 创建节点（不含 agentTeamId）
curl -X POST http://localhost:8081/api/service/v1/nodes/create \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "name": "test-node-cleanup",
    "description": "测试清理后的节点创建",
    "nodeTypeId": 1,
    "layer": "MIDDLEWARE"
  }'

# 预期结果: 创建成功，返回 NodeDTO 不含 agentTeamId

# 查询节点
curl -X POST http://localhost:8081/api/service/v1/nodes/query \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}'

# 预期结果: 返回结果不含 agentTeamId 字段
```

#### P2 验证：Topology API

```bash
# 创建拓扑图（不含 coordinatorAgentId）
curl -X POST http://localhost:8081/api/service/v1/topologies/create \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "name": "test-topology-cleanup",
    "description": "测试清理后的拓扑图创建"
  }'

# 预期结果: 创建成功，返回 TopologyDTO 不含 coordinatorAgentId

# 查询拓扑图
curl -X POST http://localhost:8081/api/service/v1/topologies/query \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}'

# 预期结果: 返回结果不含 coordinatorAgentId, globalSupervisorAgentId 字段
```

#### P3 验证：Global Supervisor 绑定

```bash
# 查询拓扑图的 Agent 绑定（通过 agent_bound API）
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/query \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "TOPOLOGY",
    "entityId": 1,
    "page": 1,
    "size": 10
  }'

# 预期结果: 返回 GLOBAL_SUPERVISOR 绑定记录
```

#### P4 验证：Node-Agent 绑定

```bash
# 查询节点的 Agent 绑定（通过 agent_bound API）
curl -X POST http://localhost:8081/api/service/v1/agent-bounds/query \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "NODE",
    "entityId": 1,
    "page": 1,
    "size": 10
  }'

# 预期结果: 返回 Node-Agent 绑定记录
```

### 5. 数据库结构验证

```bash
# 连接数据库
docker exec -it aiops-mysql mysql -uroot -proot123 op_stack_service

# P1 完成后验证
SHOW COLUMNS FROM node LIKE 'agent_team_id';
-- 预期结果: Empty set

# P2 完成后验证
SHOW COLUMNS FROM topology LIKE 'coordinator_agent_id';
-- 预期结果: Empty set

# P3 完成后验证
SHOW COLUMNS FROM topology LIKE 'global_supervisor_agent_id';
-- 预期结果: Empty set

# P4 完成后验证
SHOW TABLES LIKE 'node_2_agent';
-- 预期结果: Empty set
```

### 6. 清理测试数据

```bash
# 连接数据库删除测试节点和拓扑图
docker exec -it aiops-mysql mysql -uroot -proot123 op_stack_service -e "
DELETE FROM node WHERE name LIKE 'test-node-cleanup%';
DELETE FROM topology WHERE name LIKE 'test-topology-cleanup%';
"
```

## 回滚指南

如需回滚某个阶段，执行对应的回滚迁移：

```bash
# 回滚到指定版本
mvn flyway:migrate -Dflyway.target=V{N-1}

# 示例：回滚 P1（V33）
mvn flyway:migrate -Dflyway.target=V32
```

**注意**: 回滚数据库后，需同步恢复对应的代码变更。
