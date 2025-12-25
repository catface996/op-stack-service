# Feature Specification: 资源分类体系设计

**Feature Branch**: `001-resource-category-design`
**Created**: 2025-12-25
**Status**: Draft
**Input**: User description: "Resource应该要能区分是资源节点，还是资源拓扑图，如果是资源节点，继续区分资源类型，我需要做这样一个设计的变更。"

## 背景

当前系统中，所有资源（Resource）都通过 `resource_type` 表来区分类型，包括：
- SERVER（服务器）
- APPLICATION（应用）
- DATABASE（数据库）
- API（API接口）
- MIDDLEWARE（中间件）
- REPORT（报表）
- SUBGRAPH（子图/拓扑图）

**问题**：SUBGRAPH 与其他资源类型在本质上有根本性差异：
- SUBGRAPH 是**容器**，用于组织和展示其他资源的拓扑关系
- 其他类型是**实际资源节点**，代表真实的IT资源

这种混合设计导致：
1. 查询资源列表时，拓扑图和资源节点混在一起
2. 无法清晰地区分"可作为拓扑图成员的资源"和"拓扑图本身"
3. API 语义不清晰，用户需要通过 resource_type 来判断资源性质

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 查询所有拓扑图 (Priority: P1)

作为系统管理员，我希望能够单独查询所有的拓扑图（不包含资源节点），以便管理和浏览业务场景视图。

**Why this priority**: 拓扑图是业务场景的核心入口，用户需要快速找到并进入特定的拓扑图。

**Independent Test**: 可通过调用拓扑图列表接口，验证返回结果只包含拓扑图类型的资源，不包含服务器、应用等资源节点。

**Acceptance Scenarios**:

1. **Given** 系统中存在10个资源节点和3个拓扑图, **When** 用户查询拓扑图列表, **Then** 只返回3个拓扑图
2. **Given** 系统中没有拓扑图, **When** 用户查询拓扑图列表, **Then** 返回空列表

---

### User Story 2 - 查询所有资源节点 (Priority: P1)

作为系统管理员，我希望能够查询所有的资源节点（不包含拓扑图），以便管理IT基础设施资源。

**Why this priority**: 资源节点是系统管理的核心对象，需要与拓扑图分开管理。

**Independent Test**: 可通过调用资源节点列表接口，验证返回结果只包含资源节点，不包含拓扑图。

**Acceptance Scenarios**:

1. **Given** 系统中存在10个资源节点和3个拓扑图, **When** 用户查询资源节点列表, **Then** 只返回10个资源节点
2. **Given** 用户按资源类型筛选服务器, **When** 查询资源节点列表, **Then** 只返回类型为 SERVER 的资源节点

---

### User Story 3 - 创建拓扑图 (Priority: P2)

作为系统管理员，我希望通过专用接口创建拓扑图，而不是通过通用的资源创建接口并指定类型为 SUBGRAPH。

**Why this priority**: 明确的接口语义有助于减少误操作，提高API可用性。

**Independent Test**: 可通过调用拓扑图创建接口，验证创建的资源自动被标记为拓扑图类型。

**Acceptance Scenarios**:

1. **Given** 用户提供拓扑图名称和描述, **When** 调用创建拓扑图接口, **Then** 系统创建一个拓扑图资源
2. **Given** 拓扑图名称已存在, **When** 调用创建拓扑图接口, **Then** 系统返回名称冲突错误

---

### User Story 4 - 创建资源节点 (Priority: P2)

作为系统管理员，我希望创建资源节点时只能选择节点类型（SERVER/APPLICATION/DATABASE等），不能选择 SUBGRAPH 类型。

**Why this priority**: 防止用户通过资源创建接口误创建拓扑图，保持接口职责清晰。

**Independent Test**: 可通过尝试用资源创建接口创建 SUBGRAPH 类型，验证系统拒绝该操作。

**Acceptance Scenarios**:

1. **Given** 用户选择 APPLICATION 类型, **When** 调用创建资源节点接口, **Then** 系统成功创建应用类型资源
2. **Given** 用户尝试选择 SUBGRAPH 类型, **When** 调用创建资源节点接口, **Then** 系统返回类型不允许错误

---

### Edge Cases

- 如果删除一个拓扑图，其成员资源节点如何处理？（假设：成员资源节点不被删除，只解除关联关系）
- 如果将一个资源节点从所有拓扑图中移除，该资源节点是否仍可查询？（假设：是的，资源节点独立存在）
- 现有数据如何迁移？（假设：现有 SUBGRAPH 类型资源自动识别为拓扑图，其他类型为资源节点）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须支持将资源分为两大类：**拓扑图**（Topology）和**资源节点**（Resource Node）
- **FR-002**: 系统必须提供独立的拓扑图列表查询接口，只返回拓扑图类型的资源
- **FR-003**: 系统必须提供独立的资源节点列表查询接口，只返回非拓扑图类型的资源
- **FR-004**: 系统必须提供独立的拓扑图创建接口
- **FR-005**: 系统必须在资源节点创建接口中禁止选择拓扑图类型
- **FR-006**: 资源节点必须能够按节点类型（SERVER/APPLICATION/DATABASE/API/MIDDLEWARE/REPORT）进一步筛选
- **FR-007**: 系统必须支持现有数据的自动识别，SUBGRAPH 类型识别为拓扑图，其他类型识别为资源节点
- **FR-008**: 拓扑图删除时，必须解除与成员资源的关联关系，但不删除成员资源本身

### Key Entities

- **Resource（资源）**: 系统的核心实体，包含名称、描述、类型、属性等信息
- **ResourceCategory（资源分类）**: 区分资源是"拓扑图"还是"资源节点"的标识
  - 值域：TOPOLOGY（拓扑图）、NODE（资源节点）
- **ResourceType（资源类型）**: 资源节点的具体类型
  - 仅适用于资源节点：SERVER、APPLICATION、DATABASE、API、MIDDLEWARE、REPORT
  - 仅适用于拓扑图：SUBGRAPH（或可不再使用，直接通过 category 识别）

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户查询拓扑图列表时，100% 的返回结果为拓扑图类型
- **SC-002**: 用户查询资源节点列表时，100% 的返回结果为非拓扑图类型
- **SC-003**: 用户通过资源节点创建接口无法创建拓扑图类型（接口拒绝率100%）
- **SC-004**: 现有数据迁移后，所有 SUBGRAPH 类型资源可通过拓扑图接口查询
- **SC-005**: API 调用错误率（因分类混淆导致）降低至 0%

## Assumptions

1. 现有 `resource_type` 表结构保持不变，通过增加 `category` 字段或在代码层面区分
2. SUBGRAPH 类型等价于拓扑图，其他类型等价于资源节点
3. 拓扑图与资源节点的关联关系（成员关系）通过现有的 `subgraph_member` 表维护
4. 不需要创建新的数据库表，只需调整接口设计和查询逻辑
5. 现有资源的 `resource_type_id` 数据不变，迁移无需修改历史数据
