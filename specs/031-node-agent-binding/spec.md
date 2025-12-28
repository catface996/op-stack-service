# Feature Specification: Node-Agent 绑定功能

**Feature Branch**: `031-node-agent-binding`
**Created**: 2025-12-28
**Status**: Draft
**Input**: 需要支持将 Agent 和 ResourceNode 关联，并且是多对多的关联关系。创建一张 node_2_agent 表。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 绑定 Agent 到资源节点 (Priority: P1)

运维管理员希望将特定的 Agent 绑定到资源节点（ResourceNode），以便这些 Agent 能够监控和管理该节点的运行状态。一个 Agent 可以被绑定到多个节点，一个节点也可以被多个 Agent 监控。

**Why this priority**: 这是核心功能，没有绑定关系就无法实现 Agent 对节点的监控管理。

**Independent Test**: 可以通过调用绑定接口将 Agent 与 Node 关联，然后查询验证关联关系已建立。

**Acceptance Scenarios**:

1. **Given** 存在一个有效的 Agent 和一个有效的 ResourceNode，**When** 管理员执行绑定操作，**Then** 系统创建关联记录并返回成功
2. **Given** Agent 或 ResourceNode 不存在，**When** 管理员执行绑定操作，**Then** 系统返回相应的错误信息
3. **Given** 已存在相同的绑定关系，**When** 管理员再次执行相同的绑定操作，**Then** 系统提示绑定关系已存在

---

### User Story 2 - 查询节点关联的 Agent 列表 (Priority: P1)

运维管理员希望查看某个资源节点关联了哪些 Agent，以了解该节点的监控覆盖情况。

**Why this priority**: 查询是验证绑定是否成功的基础功能，与绑定同等重要。

**Independent Test**: 可以通过查询接口获取指定节点关联的所有 Agent 列表。

**Acceptance Scenarios**:

1. **Given** 一个节点已绑定多个 Agent，**When** 管理员查询该节点的 Agent 列表，**Then** 系统返回所有关联的 Agent 信息
2. **Given** 一个节点未绑定任何 Agent，**When** 管理员查询该节点的 Agent 列表，**Then** 系统返回空列表

---

### User Story 3 - 查询 Agent 关联的节点列表 (Priority: P2)

运维管理员希望查看某个 Agent 负责监控哪些资源节点，以了解该 Agent 的工作范围。

**Why this priority**: 从 Agent 角度查看关联关系是常见需求，但优先级略低于从节点角度查询。

**Independent Test**: 可以通过查询接口获取指定 Agent 关联的所有节点列表。

**Acceptance Scenarios**:

1. **Given** 一个 Agent 已绑定多个节点，**When** 管理员查询该 Agent 的节点列表，**Then** 系统返回所有关联的节点信息
2. **Given** 一个 Agent 未绑定任何节点，**When** 管理员查询该 Agent 的节点列表，**Then** 系统返回空列表

---

### User Story 4 - 解除绑定关系 (Priority: P2)

运维管理员希望解除 Agent 与资源节点的绑定关系，当某个节点不再需要特定 Agent 监控时。

**Why this priority**: 解除绑定是完整生命周期管理的一部分，优先级次于绑定和查询。

**Independent Test**: 可以通过解绑接口移除关联关系，然后查询验证关联已解除。

**Acceptance Scenarios**:

1. **Given** 存在一个有效的绑定关系，**When** 管理员执行解绑操作，**Then** 系统删除关联记录并返回成功
2. **Given** 绑定关系不存在，**When** 管理员执行解绑操作，**Then** 系统返回关联关系不存在的错误

---

### Edge Cases

- 当删除 Agent 时，系统如何处理其关联的绑定关系？（假设：级联软删除关联记录）
- 当删除 ResourceNode 时，系统如何处理其关联的绑定关系？（假设：级联软删除关联记录）
- 如何处理大批量绑定/解绑操作？（假设：支持批量操作接口）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须支持将 Agent 与 ResourceNode 建立多对多的关联关系
- **FR-002**: 系统必须提供绑定接口，允许将一个 Agent 绑定到一个 ResourceNode
- **FR-003**: 系统必须提供解绑接口，允许解除 Agent 与 ResourceNode 的绑定关系
- **FR-004**: 系统必须提供查询接口，支持根据节点 ID 查询关联的 Agent 列表
- **FR-005**: 系统必须提供查询接口，支持根据 Agent ID 查询关联的节点列表
- **FR-006**: 系统必须防止重复绑定，同一 Agent 与同一节点只能存在一条绑定记录
- **FR-007**: 系统必须对绑定记录支持软删除
- **FR-008**: 系统必须在绑定时验证 Agent 和 ResourceNode 的有效性（存在且未被删除）

### Key Entities

- **NodeAgentRelation**: 表示 ResourceNode 与 Agent 之间的绑定关系
  - 关联节点 ID
  - 关联 Agent ID
  - 创建时间
  - 软删除标记

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 管理员可以在 3 秒内完成单个绑定/解绑操作
- **SC-002**: 查询节点关联的 Agent 列表响应时间不超过 1 秒
- **SC-003**: 查询 Agent 关联的节点列表响应时间不超过 1 秒
- **SC-004**: 系统支持单个节点关联至少 100 个 Agent
- **SC-005**: 系统支持单个 Agent 关联至少 1000 个节点

## Assumptions

- ResourceNode 实体已存在于系统中，使用 `node` 表存储
- Agent 实体已存在于系统中，使用 `agent` 表存储
- 关联表命名为 `node_2_agent`，遵循项目命名规范
- 软删除机制与项目其他实体一致
- 接口遵循 POST-Only API 设计规范
- **HTTP 接口位置**: 所有绑定相关接口放在 Node 模块下（NodeController），而非 Agent 模块
  - 接口路径前缀: `/api/service/v1/nodes/...`
  - 示例: `/api/service/v1/nodes/bindAgent`, `/api/service/v1/nodes/unbindAgent`, `/api/service/v1/nodes/listAgents`
