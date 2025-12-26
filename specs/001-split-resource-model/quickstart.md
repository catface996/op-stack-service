# Quickstart: Resource 模型分离重构

**Feature**: [spec.md](./spec.md) | **Date**: 2025-12-26

## 快速开始

### 前置条件

1. Java 21 已安装
2. MySQL 8.0 数据库已运行
3. 数据库连接配置正确（参考 `application-local.yml`）

### 开发环境启动

```bash
# 1. 切换到功能分支
git checkout 001-split-resource-model

# 2. 编译项目
mvn clean package -DskipTests

# 3. 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 4. 验证服务
curl -X POST http://localhost:8080/api/v1/topologies/query \
  -H "Content-Type: application/json" \
  -d '{"operatorId": 1, "page": 1, "size": 10}'
```

### 数据库迁移

Flyway 会自动执行迁移脚本。如需手动执行：

```bash
# 查看待执行的迁移
mvn flyway:info -pl bootstrap

# 执行迁移
mvn flyway:migrate -pl bootstrap
```

### API 测试

#### 拓扑图 API

```bash
# 查询拓扑图列表
curl -X POST http://localhost:8080/api/v1/topologies/query \
  -H "Content-Type: application/json" \
  -d '{"operatorId": 1, "page": 1, "size": 10}'

# 创建拓扑图（新增 coordinatorAgentId 字段）
curl -X POST http://localhost:8080/api/v1/topologies/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试拓扑图",
    "description": "测试描述",
    "coordinatorAgentId": null,
    "operatorId": 1
  }'

# 获取拓扑图详情
curl -X POST http://localhost:8080/api/v1/topologies/get \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "operatorId": 1}'
```

#### 节点 API（新路径）

```bash
# 查询节点列表（新路径）
curl -X POST http://localhost:8080/api/v1/nodes/query \
  -H "Content-Type: application/json" \
  -d '{"operatorId": 1, "page": 1, "size": 10}'

# 创建节点（新增 agentTeamId 字段）
curl -X POST http://localhost:8080/api/v1/nodes/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试节点",
    "description": "测试描述",
    "nodeTypeId": 1,
    "agentTeamId": null,
    "operatorId": 1
  }'

# 查询节点类型（新路径）
curl -X POST http://localhost:8080/api/v1/node-types/query \
  -H "Content-Type: application/json" \
  -d '{"operatorId": 1}'
```

#### 废弃 API（仍可用，但会返回警告）

```bash
# 旧路径查询资源（返回 X-Deprecated-API header）
curl -v -X POST http://localhost:8080/api/v1/resources/query \
  -H "Content-Type: application/json" \
  -d '{"operatorId": 1, "page": 1, "size": 10}'

# 响应 header 中会包含：
# X-Deprecated-API: Use /api/v1/nodes/query instead
```

---

## 重构变更概览

### 数据库变更

| 变更类型 | 对象 | 说明 |
|---------|------|------|
| 新建表 | `topology` | 存储拓扑图数据 |
| 新建表 | `node` | 存储资源节点数据 |
| 重命名 | `resource_type` → `node_type` | 节点类型表 |
| 删除记录 | `node_type.SUBGRAPH` | 移除 SUBGRAPH 类型 |
| 删除表 | `resource` | 数据已迁移 |
| 修改外键 | `subgraph_member` | 关联 `topology.id` |

### API 路径变更

| 旧路径 | 新路径 | 状态 |
|-------|-------|------|
| `/api/v1/resources/query` | `/api/v1/nodes/query` | 旧路径 @Deprecated |
| `/api/v1/resources/create` | `/api/v1/nodes/create` | 旧路径 @Deprecated |
| `/api/v1/resources/get` | `/api/v1/nodes/get` | 旧路径 @Deprecated |
| `/api/v1/resources/update` | `/api/v1/nodes/update` | 旧路径 @Deprecated |
| `/api/v1/resources/delete` | `/api/v1/nodes/delete` | 旧路径 @Deprecated |
| `/api/v1/resource-types/query` | `/api/v1/node-types/query` | 旧路径 @Deprecated |
| `/api/v1/topologies/*` | 保持不变 | ✅ 兼容 |

### 新增字段

| 实体 | 新字段 | 类型 | 说明 |
|-----|-------|------|------|
| Topology | `coordinator_agent_id` | BIGINT | 协调 Agent ID |
| Node | `agent_team_id` | BIGINT | Agent Team ID |

---

## 验证清单

### 迁移后验证

```sql
-- 1. 验证 topology 表数据量
SELECT COUNT(*) FROM topology;  -- 预期: 6

-- 2. 验证 node 表数据量
SELECT COUNT(*) FROM node;  -- 预期: 33

-- 3. 验证 subgraph_member 数据完整性
SELECT COUNT(*) FROM subgraph_member;  -- 预期: 与迁移前一致

-- 4. 验证 node_type 中无 SUBGRAPH
SELECT COUNT(*) FROM node_type WHERE code = 'SUBGRAPH';  -- 预期: 0

-- 5. 验证外键约束
SELECT * FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_NAME = 'subgraph_member' AND CONSTRAINT_TYPE = 'FOREIGN KEY';
```

### API 功能验证

- [ ] `/api/v1/topologies/query` 返回所有拓扑图
- [ ] `/api/v1/topologies/create` 可创建拓扑图
- [ ] `/api/v1/topologies/get` 可获取拓扑图详情
- [ ] `/api/v1/topologies/update` 可更新拓扑图
- [ ] `/api/v1/topologies/delete` 可删除拓扑图
- [ ] `/api/v1/nodes/query` 返回所有节点（不含拓扑图）
- [ ] `/api/v1/nodes/create` 可创建节点（不能创建 SUBGRAPH）
- [ ] `/api/v1/node-types/query` 返回节点类型（不含 SUBGRAPH）
- [ ] `/api/v1/resources/query` 返回节点并带废弃警告
- [ ] 成员管理接口正常工作

---

## 回滚方案

如需回滚，执行以下步骤：

```bash
# 1. 停止应用
# 2. 执行回滚 SQL（需要手动创建）
mysql -u root -p aiops < V12_1__Rollback_split_resource.sql

# 3. 切换回 main 分支代码
git checkout main

# 4. 重新编译启动
mvn clean package -DskipTests
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

**回滚 SQL 要点**：
1. 重建 `resource` 表
2. 从 `topology` 和 `node` 表导回数据
3. 恢复 `resource_type` 表名
4. 恢复 `subgraph_member` 外键约束
5. 重新插入 SUBGRAPH 类型记录
