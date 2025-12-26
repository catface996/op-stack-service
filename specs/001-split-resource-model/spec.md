# Feature Specification: Resource 模型分离重构

**Feature Branch**: `001-split-resource-model`
**Created**: 2025-12-26
**Status**: Draft
**Input**: 将 Resource 分离成 Topology 和 Node 实体模型，在数据库中分开管理，重构过程中不能丢失现有能力的支持。

## 背景

当前系统使用单一的 `resource` 表存储所有资源（包括拓扑图和资源节点），通过 `resource_type` 字段区分。上一个迭代已在 API 层面实现了拓扑图和资源节点的分离查询，但底层仍共用同一张表。

**为什么需要分离**：

1. **语义差异**：
   - 资源节点需要绑定 Agent Team（包含 Supervisor 和多个 Worker）
   - 拓扑图需要绑定 Coordinator Agent（协调多个 Team 的单一 Agent）
   - 同一字段在不同类型下语义完全不同

2. **扩展性问题**：
   - 资源节点可能需要硬件属性（cpu、memory、disk）
   - 拓扑图可能需要布局配置（layout_config、view_settings）
   - 共用 JSON attributes 会导致字段膨胀和混乱

3. **性能考虑**：
   - 分离后可独立优化各自的查询
   - 避免每次查询都需要类型过滤

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 查询拓扑图列表 (Priority: P1)

作为系统管理员，我希望通过拓扑图专用接口查询所有拓扑图，功能与当前保持一致。

**Why this priority**: 核心功能不能因重构而丢失。

**Independent Test**: 调用 `/api/v1/topologies/query` 接口，验证返回结果与重构前一致。

**Acceptance Scenarios**:

1. **Given** 重构完成后, **When** 用户查询拓扑图列表, **Then** 返回所有拓扑图数据，字段和格式与重构前一致
2. **Given** 按名称筛选, **When** 查询拓扑图列表, **Then** 返回匹配的拓扑图

---

### User Story 2 - 查询资源节点列表 (Priority: P1)

作为系统管理员，我希望通过资源节点专用接口查询所有节点，功能与当前保持一致。

**Why this priority**: 核心功能不能因重构而丢失。

**Independent Test**: 调用 `/api/v1/resources/query` 接口（现改为 `/api/v1/nodes/query`），验证返回结果与重构前一致。

**Acceptance Scenarios**:

1. **Given** 重构完成后, **When** 用户查询资源节点列表, **Then** 返回所有节点数据，不包含拓扑图
2. **Given** 按节点类型筛选, **When** 查询资源节点列表, **Then** 返回匹配类型的节点

---

### User Story 3 - 拓扑图 CRUD 操作 (Priority: P1)

作为系统管理员，我希望拓扑图的创建、查看、更新、删除功能在重构后保持正常。

**Why this priority**: 完整的 CRUD 功能是系统的基础能力。

**Acceptance Scenarios**:

1. **Given** 用户创建拓扑图, **When** 调用创建接口, **Then** 拓扑图被保存到 topology 表
2. **Given** 用户更新拓扑图, **When** 调用更新接口, **Then** topology 表中对应记录被更新
3. **Given** 用户删除拓扑图, **When** 调用删除接口, **Then** 成员关系被解除，拓扑图被删除

---

### User Story 4 - 资源节点 CRUD 操作 (Priority: P1)

作为系统管理员，我希望资源节点的创建、查看、更新、删除功能在重构后保持正常。

**Acceptance Scenarios**:

1. **Given** 用户创建资源节点, **When** 调用创建接口, **Then** 节点被保存到 node 表
2. **Given** 用户更新资源节点, **When** 调用更新接口, **Then** node 表中对应记录被更新
3. **Given** 用户删除资源节点, **When** 调用删除接口, **Then** 节点被删除，相关成员关系被清理

---

### User Story 5 - 拓扑图成员管理 (Priority: P1)

作为系统管理员，我希望拓扑图的成员管理功能（添加/移除成员、查询成员列表）在重构后保持正常。

**Acceptance Scenarios**:

1. **Given** 向拓扑图添加节点成员, **When** 调用添加成员接口, **Then** subgraph_member 表新增关联记录
2. **Given** 从拓扑图移除节点成员, **When** 调用移除成员接口, **Then** 关联记录被删除，节点本身不受影响
3. **Given** 查询拓扑图成员, **When** 调用查询成员接口, **Then** 返回所有关联的节点信息

---

### User Story 6 - 拓扑图绑定协调 Agent (Priority: P2)

作为系统管理员，我希望能够为拓扑图绑定一个协调 Agent，用于协调该拓扑图下各 Team 的工作。

**Why this priority**: 这是新增能力，在核心功能稳定后实现。

**Acceptance Scenarios**:

1. **Given** 创建拓扑图时指定 coordinator_agent_id, **When** 保存拓扑图, **Then** 拓扑图关联到指定的协调 Agent
2. **Given** 拓扑图已绑定协调 Agent, **When** 查询拓扑图详情, **Then** 返回 coordinator_agent_id 字段

---

### User Story 7 - 资源节点绑定 Agent Team (Priority: P2)

作为系统管理员，我希望能够为资源节点绑定一个 Agent Team，该 Team 包含 Supervisor 和多个 Worker。

**Why this priority**: 这是新增能力，在核心功能稳定后实现。

**Acceptance Scenarios**:

1. **Given** 创建资源节点时指定 agent_team_id, **When** 保存节点, **Then** 节点关联到指定的 Agent Team
2. **Given** 节点已绑定 Agent Team, **When** 查询节点详情, **Then** 返回 agent_team_id 字段

---

### Edge Cases

- 数据迁移：现有 resource 表中 SUBGRAPH 类型记录迁移到 topology 表，其他记录迁移到 node 表
- 外键约束：subgraph_member.subgraph_id 改为关联 topology.id，member_id 改为关联 node.id 或 topology.id（嵌套拓扑图）
- ID 冲突：迁移后 topology 和 node 的 ID 可能重叠，需要处理（建议保留原 ID，两表 ID 不冲突）
- 回滚方案：需要提供数据回滚脚本，以防重构失败

## Requirements *(mandatory)*

### Functional Requirements

**数据模型分离**：
- **FR-001**: 系统必须将 resource 表拆分为 topology 表和 node 表
- **FR-002**: topology 表必须包含字段：id, name, description, status, coordinator_agent_id, attributes, created_by, version, created_at, updated_at
- **FR-003**: node 表必须包含字段：id, name, description, node_type_id, status, agent_team_id, attributes, created_by, version, created_at, updated_at
- **FR-004**: 系统必须将 resource_type 表重命名为 node_type，并移除 SUBGRAPH 类型记录
- **FR-005**: subgraph_member 表的 subgraph_id 必须关联 topology.id

**API 兼容性**：
- **FR-006**: 拓扑图 API（/api/v1/topologies/*）必须保持接口契约不变
- **FR-007**: 资源节点 API 路径从 /api/v1/resources/* 变更为 /api/v1/nodes/*
- **FR-008**: 原 /api/v1/resources/* 路径必须保留并标记为 @Deprecated，返回重定向提示
- **FR-009**: 资源类型查询 API 路径从 /api/v1/resource-types/* 变更为 /api/v1/node-types/*

**数据迁移**：
- **FR-010**: 系统必须提供数据迁移脚本，将现有 resource 数据分离到 topology 和 node 表
- **FR-011**: 迁移必须保留原有 ID，确保历史数据可追溯
- **FR-012**: 迁移必须保留所有成员关系（subgraph_member 数据不变）

**Agent 绑定能力**：
- **FR-013**: topology 表必须支持 coordinator_agent_id 字段，可为空
- **FR-014**: node 表必须支持 agent_team_id 字段，可为空

### Key Entities

- **Topology（拓扑图）**: 业务场景的容器，组织和展示节点之间的关系
  - 字段：id, name, description, status, coordinator_agent_id, attributes, created_by, version, timestamps

- **Node（资源节点）**: 实际的 IT 资源，如服务器、应用、数据库等
  - 字段：id, name, description, node_type_id, status, agent_team_id, attributes, created_by, version, timestamps

- **NodeType（节点类型）**: 资源节点的分类
  - 值域：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT

- **SubgraphMember（成员关系）**: 拓扑图与成员（节点或嵌套拓扑图）的关联
  - 字段：subgraph_id → topology.id, member_id → node.id 或 topology.id

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 重构后所有现有 API 功能测试通过率达 100%
- **SC-002**: 数据迁移后，拓扑图数量与迁移前 SUBGRAPH 类型资源数量一致
- **SC-003**: 数据迁移后，资源节点数量与迁移前非 SUBGRAPH 类型资源数量一致
- **SC-004**: 所有成员关系在迁移后保持完整（subgraph_member 记录数不变）
- **SC-005**: 查询性能不低于重构前（响应时间 ≤ 重构前的 1.1 倍）
- **SC-006**: 新增 Agent 绑定字段可正常读写

## Assumptions

1. 迁移过程中系统可短暂停机（预计 < 5 分钟）
2. 现有 resource 表中的 ID 在拆分后不会冲突（topology 使用原 SUBGRAPH 的 ID，node 使用其他类型的 ID）
3. 嵌套拓扑图场景中，subgraph_member.member_id 可关联到 topology.id（即拓扑图可以包含另一个拓扑图作为成员）
4. Agent 相关表（agent, agent_team）已存在或将在后续迭代中创建，本次仅预留外键字段
5. 原 /api/v1/resources/* 接口在过渡期内保留 3 个月后移除

## Out of Scope

- Agent 和 Agent Team 的 CRUD 功能（本次仅预留字段）
- 节点与 Agent Team 的详细交互逻辑
- 拓扑图与 Coordinator Agent 的详细协调逻辑
- 前端 UI 适配（本次仅涉及后端 API 和数据库）
