# Tasks: Resource 模型分离重构

**Feature**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Date**: 2025-12-26

## Overview

| Phase | Description | Tasks | Est. Complexity |
|-------|-------------|-------|-----------------|
| 1 | Setup - 数据库迁移脚本 | 3 | Medium |
| 2 | Foundational - 领域模型和仓储层 | 8 | Medium |
| 3 | US1 - 查询拓扑图列表 | 4 | Low |
| 4 | US2 - 查询资源节点列表 | 4 | Low |
| 5 | US3 - 拓扑图 CRUD 操作 | 5 | Medium |
| 6 | US4 - 资源节点 CRUD 操作 | 5 | Medium |
| 7 | US5 - 拓扑图成员管理 | 4 | Medium |
| 8 | US6/US7 - Agent 绑定能力 | 3 | Low |
| 9 | Polish - 废弃 API 兼容层 | 4 | Low |

---

## Phase 1: Setup - 数据库迁移脚本

### Task 1.1: 创建主迁移脚本

**File**: `bootstrap/src/main/resources/db/migration/V12__Split_resource_to_topology_and_node.sql`

**Requirements**: FR-001, FR-002, FR-003, FR-004, FR-005, FR-010, FR-011, FR-012

**Description**: 创建 Flyway 迁移脚本，执行以下操作：
1. 创建 `topology` 表（从 resource 表中 SUBGRAPH 类型数据迁移）
2. 创建 `node` 表（从 resource 表中非 SUBGRAPH 类型数据迁移）
3. 重命名 `resource_type` 为 `node_type`，移除 SUBGRAPH 记录
4. 创建 `topology_2_node` 关联表（替代 subgraph_member）
5. 创建 `node_2_node` 关联表（替代 resource_relationship）
6. 数据迁移（保留原 ID）
7. 删除旧表（resource, subgraph_member, resource_relationship）

**Depends on**: None

**Acceptance**:
- [ ] Flyway 迁移成功执行
- [ ] topology 表包含所有原 SUBGRAPH 类型记录
- [ ] node 表包含所有原非 SUBGRAPH 类型记录
- [ ] 所有外键约束正确建立
- [ ] 原有成员关系数据完整迁移

**Reference**: [data-model.md](./data-model.md) 中的 DDL 定义

---

### Task 1.2: 创建回滚脚本

**File**: `bootstrap/src/main/resources/db/migration/V12_1__Rollback_split_resource.sql`

**Requirements**: spec.md Edge Cases - 回滚方案

**Description**: 创建回滚脚本，支持从新表结构还原到旧表结构。
1. 重建 `resource` 表
2. 从 `topology` 和 `node` 表导回数据
3. 恢复 `resource_type` 表名
4. 恢复 `subgraph_member` 和 `resource_relationship` 表
5. 重新插入 SUBGRAPH 类型记录

**Depends on**: Task 1.1

**Acceptance**:
- [ ] 回滚脚本语法正确
- [ ] 文档化回滚执行步骤

**Note**: 此脚本为备份用途，不会被 Flyway 自动执行

---

### Task 1.3: 验证迁移数据完整性

**Description**: 编写验证 SQL，确保迁移后数据完整性。

**Requirements**: SC-002, SC-003, SC-004

**Acceptance**:
- [ ] topology 表记录数 = 原 SUBGRAPH 类型资源数
- [ ] node 表记录数 = 原非 SUBGRAPH 类型资源数
- [ ] topology_2_node 记录数 = 原 subgraph_member 中节点成员数
- [ ] node_2_node 记录数 = 原 resource_relationship 记录数

---

## Phase 2: Foundational - 领域模型和仓储层

### Task 2.1: 创建 Topology 领域模型

**File**: `domain/domain-model/src/main/java/.../domain/model/topology/Topology.java`

**Requirements**: FR-002, FR-013

**Description**: 创建 Topology 领域模型类，包含：
- id, name, description, status
- coordinatorAgentId（新增：协调 Agent ID）
- attributes（JSON 扩展属性）
- createdBy, version, createdAt, updatedAt

**Depends on**: None

**Acceptance**:
- [ ] 字段与 data-model.md 定义一致
- [ ] 包含必要的 getter/setter 或使用 Lombok

---

### Task 2.2: 更新 Node 领域模型

**File**: `domain/domain-model/src/main/java/.../domain/model/node/Node.java`

**Requirements**: FR-003, FR-014

**Description**: 更新现有 Node 领域模型（原 Resource），包含：
- id, name, description, nodeTypeId, status
- agentTeamId（新增：Agent Team ID）
- attributes（JSON 扩展属性）
- createdBy, version, createdAt, updatedAt

**Depends on**: None

**Acceptance**:
- [ ] 移除 resource_type 相关字段，改为 nodeTypeId
- [ ] 添加 agentTeamId 字段

---

### Task 2.3: 创建 TopologyPO 持久化对象

**File**: `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/po/topology/TopologyPO.java`

**Requirements**: FR-002

**Description**: 创建 MyBatis-Plus 实体类，映射 `topology` 表。

**Depends on**: Task 1.1

**Acceptance**:
- [ ] @TableName("topology") 注解正确
- [ ] 字段映射与数据库表一致
- [ ] 包含 @Version 注解的乐观锁字段

---

### Task 2.4: 更新 NodePO 持久化对象

**File**: `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/po/node/NodePO.java`

**Requirements**: FR-003

**Description**: 更新现有 ResourcePO（重命名为 NodePO），映射 `node` 表。

**Depends on**: Task 1.1

**Acceptance**:
- [ ] @TableName("node") 注解正确
- [ ] 字段从 resourceTypeId 改为 nodeTypeId
- [ ] 添加 agentTeamId 字段

---

### Task 2.5: 创建 Topology2NodePO 和 Node2NodePO

**Files**:
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/po/topology/Topology2NodePO.java`
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/po/node/Node2NodePO.java`

**Requirements**: FR-005, data-model.md

**Description**: 创建关联表的持久化对象。

**Depends on**: Task 1.1

**Acceptance**:
- [ ] Topology2NodePO 映射 topology_2_node 表
- [ ] Node2NodePO 映射 node_2_node 表
- [ ] 包含正确的外键字段

---

### Task 2.6: 创建 TopologyMapper

**File**: `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/mapper/topology/TopologyMapper.java`

**Description**: 创建 MyBatis-Plus Mapper 接口。

**Depends on**: Task 2.3

**Acceptance**:
- [ ] 继承 BaseMapper<TopologyPO>
- [ ] 包含自定义查询方法（按名称模糊查询、按状态查询）

---

### Task 2.7: 创建 TopologyRepository 和实现

**Files**:
- `domain/repository-api/src/main/java/.../repository/topology/TopologyRepository.java`
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/impl/topology/TopologyRepositoryImpl.java`

**Description**: 创建拓扑图仓储接口和 MySQL 实现。

**Depends on**: Task 2.6

**Acceptance**:
- [ ] 包含 CRUD 方法
- [ ] 包含分页查询方法
- [ ] 实现类使用 TopologyMapper

---

### Task 2.8: 更新 NodeRepository 和实现

**Files**:
- `domain/repository-api/src/main/java/.../repository/node/NodeRepository.java`
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/impl/node/NodeRepositoryImpl.java`

**Description**: 更新现有资源仓储（重命名为 NodeRepository）。

**Depends on**: Task 2.4

**Acceptance**:
- [ ] 接口和实现类从 Resource* 重命名为 Node*
- [ ] 查询方法不再包含 SUBGRAPH 类型过滤

---

## Phase 3: US1 - 查询拓扑图列表

**User Story**: 作为系统管理员，我希望通过拓扑图专用接口查询所有拓扑图

### Task 3.1: 创建 TopologyDTO

**File**: `application/application-api/src/main/java/.../application/api/dto/topology/TopologyDTO.java`

**Requirements**: contracts/topology-api.yaml - TopologyDTO

**Description**: 创建拓扑图数据传输对象。

**Depends on**: Task 2.1

**Acceptance**:
- [ ] 包含所有 API 契约定义的字段
- [ ] 包含 memberCount 字段（成员数量）

---

### Task 3.2: 更新 TopologyApplicationService

**Files**:
- `application/application-api/src/main/java/.../application/api/service/topology/TopologyApplicationService.java`
- `application/application-impl/src/main/java/.../application/impl/service/topology/TopologyApplicationServiceImpl.java`

**Requirements**: FR-006

**Description**: 更新拓扑图应用服务的查询方法，改为从 topology 表查询。

**Depends on**: Task 2.7, Task 3.1

**Acceptance**:
- [ ] queryTopologies() 方法从 topology 表查询
- [ ] 返回数据包含 memberCount

---

### Task 3.3: 更新 TopologyController 查询接口

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/TopologyController.java`

**Requirements**: FR-006

**Description**: 确保 `/api/v1/topologies/query` 接口使用新的应用服务。

**Depends on**: Task 3.2

**Acceptance**:
- [ ] POST /api/v1/topologies/query 正常工作
- [ ] 返回数据格式与重构前一致

---

### Task 3.4: 验证 US1 功能

**Requirements**: SC-001, US1 Acceptance Scenarios

**Description**: 验证拓扑图列表查询功能。

**Depends on**: Task 3.3

**Acceptance**:
- [ ] 查询结果包含所有拓扑图
- [ ] 按名称筛选功能正常
- [ ] 分页功能正常

---

## Phase 4: US2 - 查询资源节点列表

**User Story**: 作为系统管理员，我希望通过资源节点专用接口查询所有节点

### Task 4.1: 创建 NodeDTO

**File**: `application/application-api/src/main/java/.../application/api/dto/node/NodeDTO.java`

**Requirements**: contracts/node-api.yaml - NodeDTO

**Description**: 创建资源节点数据传输对象。

**Depends on**: Task 2.2

**Acceptance**:
- [ ] 包含所有 API 契约定义的字段
- [ ] 包含 nodeTypeName 和 nodeTypeCode 字段

---

### Task 4.2: 创建 NodeApplicationService

**Files**:
- `application/application-api/src/main/java/.../application/api/service/node/NodeApplicationService.java`
- `application/application-impl/src/main/java/.../application/impl/service/node/NodeApplicationServiceImpl.java`

**Requirements**: FR-007

**Description**: 创建资源节点应用服务，从 node 表查询。

**Depends on**: Task 2.8, Task 4.1

**Acceptance**:
- [ ] queryNodes() 方法从 node 表查询
- [ ] 返回数据不包含拓扑图

---

### Task 4.3: 创建 NodeController

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/NodeController.java`

**Requirements**: FR-007

**Description**: 创建新的 `/api/v1/nodes/*` 控制器。

**Depends on**: Task 4.2

**Acceptance**:
- [ ] POST /api/v1/nodes/query 正常工作
- [ ] 返回数据格式符合 API 契约

---

### Task 4.4: 验证 US2 功能

**Requirements**: SC-001, US2 Acceptance Scenarios

**Description**: 验证资源节点列表查询功能。

**Depends on**: Task 4.3

**Acceptance**:
- [ ] 查询结果包含所有资源节点
- [ ] 查询结果不包含拓扑图
- [ ] 按节点类型筛选功能正常

---

## Phase 5: US3 - 拓扑图 CRUD 操作

**User Story**: 拓扑图的创建、查看、更新、删除功能保持正常

### Task 5.1: 更新 TopologyDomainService

**Files**:
- `domain/domain-api/src/main/java/.../domain/service/topology/TopologyDomainService.java`
- `domain/domain-impl/src/main/java/.../domain/impl/service/topology/TopologyDomainServiceImpl.java`

**Requirements**: FR-006, FR-013

**Description**: 更新拓扑图领域服务，操作 topology 表。

**Depends on**: Task 2.7

**Acceptance**:
- [ ] create() 方法保存到 topology 表
- [ ] update() 方法使用乐观锁
- [ ] delete() 方法级联删除成员关系

---

### Task 5.2: 更新 TopologyApplicationService CRUD 方法

**File**: `application/application-impl/src/main/java/.../application/impl/service/topology/TopologyApplicationServiceImpl.java`

**Requirements**: FR-006

**Description**: 更新应用服务的创建、更新、删除方法。

**Depends on**: Task 5.1

**Acceptance**:
- [ ] createTopology() 支持 coordinatorAgentId 参数
- [ ] updateTopology() 支持乐观锁
- [ ] deleteTopology() 清理成员关系

---

### Task 5.3: 更新 TopologyController CRUD 接口

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/TopologyController.java`

**Requirements**: FR-006

**Description**: 确保 CRUD 接口使用新的应用服务。

**Depends on**: Task 5.2

**Acceptance**:
- [ ] POST /api/v1/topologies/create 正常工作
- [ ] POST /api/v1/topologies/get 正常工作
- [ ] POST /api/v1/topologies/update 正常工作
- [ ] POST /api/v1/topologies/delete 正常工作

---

### Task 5.4: 创建 Request DTO

**Files**:
- `interface/interface-http/src/main/java/.../interface_/http/request/topology/CreateTopologyRequest.java`
- `interface/interface-http/src/main/java/.../interface_/http/request/topology/UpdateTopologyRequest.java`

**Requirements**: contracts/topology-api.yaml

**Description**: 创建/更新拓扑图请求 DTO，支持新字段。

**Depends on**: None

**Acceptance**:
- [ ] CreateTopologyRequest 包含 coordinatorAgentId 字段
- [ ] UpdateTopologyRequest 包含 version 字段

---

### Task 5.5: 验证 US3 功能

**Requirements**: SC-001, US3 Acceptance Scenarios

**Description**: 验证拓扑图 CRUD 功能。

**Depends on**: Task 5.3

**Acceptance**:
- [ ] 创建拓扑图保存到 topology 表
- [ ] 更新拓扑图使用乐观锁
- [ ] 删除拓扑图清理成员关系

---

## Phase 6: US4 - 资源节点 CRUD 操作

**User Story**: 资源节点的创建、查看、更新、删除功能保持正常

### Task 6.1: 创建 NodeDomainService

**Files**:
- `domain/domain-api/src/main/java/.../domain/service/node/NodeDomainService.java`
- `domain/domain-impl/src/main/java/.../domain/impl/service/node/NodeDomainServiceImpl.java`

**Requirements**: FR-003, FR-014

**Description**: 创建资源节点领域服务，操作 node 表。

**Depends on**: Task 2.8

**Acceptance**:
- [ ] create() 方法保存到 node 表
- [ ] create() 方法校验 nodeTypeId 不能是 SUBGRAPH
- [ ] update() 方法使用乐观锁
- [ ] delete() 方法清理相关成员关系

---

### Task 6.2: 更新 NodeApplicationService CRUD 方法

**File**: `application/application-impl/src/main/java/.../application/impl/service/node/NodeApplicationServiceImpl.java`

**Requirements**: FR-007

**Description**: 添加应用服务的创建、更新、删除方法。

**Depends on**: Task 6.1

**Acceptance**:
- [ ] createNode() 支持 agentTeamId 参数
- [ ] updateNode() 支持乐观锁
- [ ] deleteNode() 清理相关关系

---

### Task 6.3: 更新 NodeController CRUD 接口

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/NodeController.java`

**Requirements**: FR-007

**Description**: 添加 CRUD 接口。

**Depends on**: Task 6.2

**Acceptance**:
- [ ] POST /api/v1/nodes/create 正常工作
- [ ] POST /api/v1/nodes/get 正常工作
- [ ] POST /api/v1/nodes/update 正常工作
- [ ] POST /api/v1/nodes/delete 正常工作

---

### Task 6.4: 创建 Node Request DTO

**Files**:
- `interface/interface-http/src/main/java/.../interface_/http/request/node/CreateNodeRequest.java`
- `interface/interface-http/src/main/java/.../interface_/http/request/node/UpdateNodeRequest.java`

**Requirements**: contracts/node-api.yaml

**Description**: 创建/更新节点请求 DTO。

**Depends on**: None

**Acceptance**:
- [ ] CreateNodeRequest 包含 nodeTypeId 和 agentTeamId 字段
- [ ] UpdateNodeRequest 包含 version 字段

---

### Task 6.5: 验证 US4 功能

**Requirements**: SC-001, US4 Acceptance Scenarios

**Description**: 验证资源节点 CRUD 功能。

**Depends on**: Task 6.3

**Acceptance**:
- [ ] 创建节点保存到 node 表
- [ ] 不能创建 SUBGRAPH 类型节点
- [ ] 更新节点使用乐观锁
- [ ] 删除节点清理相关关系

---

## Phase 7: US5 - 拓扑图成员管理

**User Story**: 拓扑图的成员管理功能（添加/移除成员、查询成员列表）保持正常

### Task 7.1: 创建 Topology2NodeMapper 和 Repository

**Files**:
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/mapper/topology/Topology2NodeMapper.java`
- `domain/repository-api/src/main/java/.../repository/topology/Topology2NodeRepository.java`
- `infrastructure/repository/mysql-impl/src/main/java/.../repository/mysql/impl/topology/Topology2NodeRepositoryImpl.java`

**Requirements**: FR-005, data-model.md

**Description**: 创建拓扑图-节点关联的 Mapper 和仓储。

**Depends on**: Task 2.5

**Acceptance**:
- [ ] 支持添加成员关系
- [ ] 支持移除成员关系
- [ ] 支持查询拓扑图的所有成员

---

### Task 7.2: 更新成员管理领域服务

**File**: `domain/domain-impl/src/main/java/.../domain/impl/service/topology/TopologyDomainServiceImpl.java`

**Requirements**: US5 Acceptance Scenarios

**Description**: 更新成员管理方法，使用 topology_2_node 表。

**Depends on**: Task 7.1

**Acceptance**:
- [ ] addMembers() 方法添加到 topology_2_node 表
- [ ] removeMembers() 方法从 topology_2_node 表删除
- [ ] 校验成员 ID 必须存在于 node 表

---

### Task 7.3: 更新成员管理接口

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/TopologyController.java`

**Requirements**: FR-006, contracts/topology-api.yaml

**Description**: 确保成员管理接口正常工作。

**Depends on**: Task 7.2

**Acceptance**:
- [ ] POST /api/v1/topologies/members/add 正常工作
- [ ] POST /api/v1/topologies/members/remove 正常工作
- [ ] POST /api/v1/topologies/members/query 正常工作

---

### Task 7.4: 验证 US5 功能

**Requirements**: SC-001, SC-004, US5 Acceptance Scenarios

**Description**: 验证成员管理功能。

**Depends on**: Task 7.3

**Acceptance**:
- [ ] 添加成员创建关联记录
- [ ] 移除成员删除关联记录，不删除节点本身
- [ ] 查询成员返回所有关联节点信息

---

## Phase 8: US6/US7 - Agent 绑定能力

**User Story**: 拓扑图绑定协调 Agent，资源节点绑定 Agent Team

### Task 8.1: 更新 Topology 支持 coordinatorAgentId

**Requirements**: FR-013, US6 Acceptance Scenarios

**Description**: 确保 Topology 的创建和更新支持 coordinatorAgentId 字段。

**Depends on**: Task 5.2

**Acceptance**:
- [ ] 创建拓扑图时可指定 coordinatorAgentId
- [ ] 查询拓扑图详情返回 coordinatorAgentId

---

### Task 8.2: 更新 Node 支持 agentTeamId

**Requirements**: FR-014, US7 Acceptance Scenarios

**Description**: 确保 Node 的创建和更新支持 agentTeamId 字段。

**Depends on**: Task 6.2

**Acceptance**:
- [ ] 创建节点时可指定 agentTeamId
- [ ] 查询节点详情返回 agentTeamId

---

### Task 8.3: 验证 Agent 绑定功能

**Requirements**: SC-006, US6/US7 Acceptance Scenarios

**Description**: 验证 Agent 绑定字段可正常读写。

**Depends on**: Task 8.1, Task 8.2

**Acceptance**:
- [ ] coordinatorAgentId 可正常保存和读取
- [ ] agentTeamId 可正常保存和读取

---

## Phase 9: Polish - 废弃 API 兼容层

### Task 9.1: 创建废弃的 ResourceController

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/ResourceController.java`

**Requirements**: FR-008, contracts/node-api.yaml - Deprecated section

**Description**: 创建废弃的资源控制器，保留旧路径的向后兼容。

**Depends on**: Task 4.2

**Acceptance**:
- [ ] 所有方法标记 @Deprecated
- [ ] 调用 NodeApplicationService 获取数据
- [ ] 响应添加 X-Deprecated-API header
- [ ] 响应添加 deprecationNotice 字段

---

### Task 9.2: 创建废弃的 ResourceTypeController

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/ResourceTypeController.java` (或更新现有)

**Requirements**: FR-009

**Description**: 保留 `/api/v1/resource-types/query` 路径，标记为废弃。

**Depends on**: Task 4.2

**Acceptance**:
- [ ] POST /api/v1/resource-types/query 返回节点类型列表
- [ ] 响应添加 X-Deprecated-API header

---

### Task 9.3: 创建 NodeTypeController

**File**: `interface/interface-http/src/main/java/.../interface_/http/controller/NodeTypeController.java`

**Requirements**: FR-009, contracts/node-api.yaml

**Description**: 创建新的节点类型控制器。

**Depends on**: None

**Acceptance**:
- [ ] POST /api/v1/node-types/query 返回节点类型列表
- [ ] 返回结果不包含 SUBGRAPH 类型

---

### Task 9.4: 最终验证

**Requirements**: SC-001, SC-005

**Description**: 执行完整的功能验证和性能验证。

**Depends on**: All previous tasks

**Acceptance**:
- [ ] 所有 API 功能测试通过
- [ ] 查询性能不低于重构前
- [ ] 废弃 API 正确返回提示信息
- [ ] 验证 quickstart.md 中的所有 curl 命令

---

## Dependencies Graph

```
Phase 1 (DB Migration)
    │
    ▼
Phase 2 (Domain Models & Repositories)
    │
    ├──────────────────┬──────────────────┐
    ▼                  ▼                  ▼
Phase 3 (US1)     Phase 4 (US2)     Phase 5 (US3)
    │                  │                  │
    │                  │                  │
    │                  ▼                  │
    │             Phase 6 (US4) ◄────────┘
    │                  │
    │                  ▼
    └────────────► Phase 7 (US5)
                       │
                       ▼
                  Phase 8 (Agent Binding)
                       │
                       ▼
                  Phase 9 (Deprecated APIs)
```

## Risk Mitigation

| Risk | Mitigation | Owner |
|------|------------|-------|
| 数据迁移失败 | 提前准备回滚脚本，迁移前备份数据库 | DBA |
| 外键约束错误 | 分步迁移，先创建表再建立外键 | Backend |
| API 兼容性问题 | 保留旧路径 3 个月，添加废弃提示 | Backend |
| 性能下降 | 添加必要索引，迁移后进行性能测试 | Backend |
