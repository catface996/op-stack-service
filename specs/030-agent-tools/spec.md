# Feature Specification: Agent Tools 绑定

**Feature Branch**: `030-agent-tools`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "支持绑定tools到agent，每次更新都是全量更新绑定的tools"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 绑定 Tools 到 Agent (Priority: P1)

管理员需要为 Agent 配置可使用的工具（Tools），以便 Agent 在执行任务时能够调用这些工具完成特定操作。管理员通过更新接口为 Agent 指定一组 Tool ID 列表，系统会全量替换该 Agent 当前绑定的所有 Tools。

**Why this priority**: 这是核心功能，Agent 必须绑定 Tools 才能执行实际任务，是整个功能的基础。

**Independent Test**: 可以通过调用 Agent 更新接口，传入 toolIds 列表，然后查询 Agent 详情验证绑定的 Tools 是否正确。

**Acceptance Scenarios**:

1. **Given** 一个已存在的 Agent，**When** 管理员更新 Agent 并传入 toolIds=[1,2,3]，**Then** 该 Agent 绑定的 Tools 变为 [1,2,3]
2. **Given** 一个已绑定 Tools [1,2,3] 的 Agent，**When** 管理员更新 Agent 并传入 toolIds=[4,5]，**Then** 该 Agent 绑定的 Tools 变为 [4,5]（全量替换）
3. **Given** 一个已绑定 Tools [1,2,3] 的 Agent，**When** 管理员更新 Agent 并传入 toolIds=[]（空列表），**Then** 该 Agent 的 Tools 绑定被清空

---

### User Story 2 - 查询 Agent 绑定的 Tools (Priority: P1)

管理员或系统需要查看某个 Agent 当前绑定了哪些 Tools，以便了解该 Agent 的能力范围。

**Why this priority**: 查询是验证绑定结果的必要功能，与绑定操作同等重要。

**Independent Test**: 调用 Agent 详情接口，检查返回结果中是否包含 toolIds 列表。

**Acceptance Scenarios**:

1. **Given** 一个已绑定 Tools [1,2,3] 的 Agent，**When** 查询该 Agent 详情，**Then** 返回结果中包含 toolIds=[1,2,3]
2. **Given** 一个未绑定任何 Tools 的 Agent，**When** 查询该 Agent 详情，**Then** 返回结果中 toolIds 为空列表

---

### User Story 3 - 创建 Agent 时指定 Tools (Priority: P2)

管理员在创建新 Agent 时，可以同时指定该 Agent 绑定的 Tools，避免创建后再次调用更新接口。

**Why this priority**: 属于便利性功能，创建后也可通过更新接口补充 Tools 绑定。

**Independent Test**: 调用 Agent 创建接口并传入 toolIds，然后查询验证绑定结果。

**Acceptance Scenarios**:

1. **Given** 系统中存在 Tools [1,2,3]，**When** 创建 Agent 时传入 toolIds=[1,2]，**Then** 新创建的 Agent 已绑定 Tools [1,2]
2. **Given** 创建 Agent 时未传入 toolIds，**When** 查询该 Agent，**Then** toolIds 为空列表

---

### Edge Cases

- 传入的 toolIds 包含不存在的 Tool ID 时，如何处理？（假设：忽略不存在的 ID，仅绑定存在的）
- 传入的 toolIds 包含重复 ID 时，如何处理？（假设：自动去重）
- Agent 处于工作状态时能否更新 Tools 绑定？（假设：遵循现有规则，工作中禁止更新）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须支持在更新 Agent 时指定 toolIds 列表，实现 Tools 绑定
- **FR-002**: 系统必须支持全量替换模式——每次更新 toolIds 时，完全替换原有绑定
- **FR-003**: 系统必须支持传入空列表以清空 Agent 的 Tools 绑定
- **FR-004**: 系统必须在 Agent 详情和列表查询中返回 toolIds 字段
- **FR-005**: 系统必须支持在创建 Agent 时指定 toolIds 列表
- **FR-006**: 系统必须对传入的 toolIds 进行去重处理
- **FR-007**: 系统必须对不存在的 Tool ID 进行过滤，仅绑定有效的 Tool
- **FR-008**: 系统必须遵循现有业务规则——Agent 处于 WORKING/THINKING 状态时禁止更新

### Key Entities

- **Agent**: 已有实体，新增 toolIds 属性（Tools 绑定关系）
- **Tool**: 已有实体，表示可被 Agent 调用的工具
- **Agent-Tool 关联**: 多对多关系，一个 Agent 可绑定多个 Tools，一个 Tool 可被多个 Agent 使用

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 管理员可在 3 秒内完成 Agent 的 Tools 绑定操作
- **SC-002**: 查询 Agent 详情时能正确返回绑定的 toolIds 列表
- **SC-003**: 全量替换功能正常工作，更新后仅保留最新指定的 Tools
- **SC-004**: 系统正确处理边界情况（空列表、重复 ID、无效 ID）

## Assumptions

- Tool 实体已存在于系统中，有独立的 Tool 管理功能
- Agent 与 Tool 的关联关系存储在独立的关联表中
- 不存在的 Tool ID 会被静默忽略，不会抛出错误
- 重复的 Tool ID 会自动去重
