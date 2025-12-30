# Feature Specification: 清理数据库废弃字段

**Feature Branch**: `041-cleanup-obsolete-fields`
**Created**: 2025-12-30
**Status**: Draft
**Input**: User description: "清理数据库废弃字段和冗余表的迁移计划"

## Clarifications

### Session 2025-12-30

- Q: 废弃字段的 API 向后兼容过渡期应持续多长时间？ → A: 无需过渡期（直接移除，无外部依赖）

## 背景与分析

基于当前数据库和代码分析，发现以下废弃字段和冗余表需要清理：

### 废弃字段清单

| 字段/表 | 位置 | 数据库记录 | 状态 | 说明 |
|--------|------|-----------|------|------|
| `agent_team_id` | node 表 | 0 条 | 废弃 | 预留字段，从未使用 |
| `coordinator_agent_id` | topology 表 | 0 条 | 废弃 | 预留字段，从未使用 |
| `global_supervisor_agent_id` | topology 表 | 3 条 | 冗余 | 已迁移到 agent_bound 表 |
| `node_2_agent` | 独立表 | 11 条 | 冗余 | 已迁移到 agent_bound 表 |

### 代码影响范围

- **node.agent_team_id**: 9 个 Java 文件引用
- **topology.coordinator_agent_id**: 9 个 Java 文件引用
- **topology.global_supervisor_agent_id**: 4 个 Java 文件引用
- **node_2_agent 表**: 6 个 Java 文件 + 1 个 XML 文件引用

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 移除 Node 表废弃字段 (Priority: P1)

作为系统维护人员，我需要移除 node 表中未使用的 `agent_team_id` 字段，以保持数据模型的简洁性和准确性。

**Why this priority**: 该字段从未使用，0 条数据记录，清理风险最低，可作为第一步验证清理流程。

**Independent Test**: 可通过验证 Node 相关 API（创建、查询、更新）功能正常来独立测试。

**Acceptance Scenarios**:

1. **Given** node 表存在 agent_team_id 字段, **When** 执行数据库迁移, **Then** agent_team_id 字段被成功移除
2. **Given** Node 创建/更新 API 请求中包含 agentTeamId 字段, **When** 执行 API 调用, **Then** 系统正常处理请求（忽略该字段）
3. **Given** Node 查询 API, **When** 执行查询, **Then** 返回结果不再包含 agentTeamId 字段

---

### User Story 2 - 移除 Topology 表废弃字段 (Priority: P2)

作为系统维护人员，我需要移除 topology 表中未使用的 `coordinator_agent_id` 字段，以保持数据模型的简洁性。

**Why this priority**: 该字段从未使用，0 条数据记录，清理后可简化拓扑图相关代码。

**Independent Test**: 可通过验证 Topology 相关 API（创建、查询、更新）功能正常来独立测试。

**Acceptance Scenarios**:

1. **Given** topology 表存在 coordinator_agent_id 字段, **When** 执行数据库迁移, **Then** coordinator_agent_id 字段被成功移除
2. **Given** Topology 创建/更新 API 请求中包含 coordinatorAgentId 字段, **When** 执行 API 调用, **Then** 系统正常处理请求（忽略该字段）
3. **Given** Topology 查询 API, **When** 执行查询, **Then** 返回结果不再包含 coordinatorAgentId 字段

---

### User Story 3 - 移除 Topology 表冗余字段 (Priority: P3)

作为系统维护人员，我需要移除 topology 表中已迁移到 agent_bound 表的 `global_supervisor_agent_id` 字段，以消除数据冗余。

**Why this priority**: 该字段有 3 条数据记录，数据已迁移到 agent_bound 表，需确认迁移完整性后才能安全移除。

**Independent Test**: 可通过验证 Global Supervisor Agent 绑定功能通过 agent_bound 表正常工作来独立测试。

**Acceptance Scenarios**:

1. **Given** topology 表存在 global_supervisor_agent_id 字段且有数据, **When** 验证 agent_bound 表数据完整性, **Then** 所有绑定关系都已正确迁移到 agent_bound 表
2. **Given** agent_bound 表数据验证通过, **When** 执行数据库迁移, **Then** global_supervisor_agent_id 字段被成功移除
3. **Given** 拓扑图 Global Supervisor 绑定功能, **When** 通过 API 查询或操作, **Then** 系统通过 agent_bound 表正常处理

---

### User Story 4 - 移除冗余的 node_2_agent 表 (Priority: P4)

作为系统维护人员，我需要移除已迁移到 agent_bound 表的 `node_2_agent` 表，以消除数据冗余和维护负担。

**Why this priority**: 该表有 11 条数据记录，数据已迁移到 agent_bound 表（15 条记录），需确认所有相关功能都已切换到使用 agent_bound 表。

**Independent Test**: 可通过验证 Node-Agent 绑定功能完全通过 agent_bound 表正常工作来独立测试。

**Acceptance Scenarios**:

1. **Given** node_2_agent 表存在且有数据, **When** 验证 agent_bound 表数据完整性, **Then** 所有 Node-Agent 绑定关系都已正确迁移到 agent_bound 表
2. **Given** 相关代码已切换到使用 agent_bound 表, **When** 执行数据库迁移, **Then** node_2_agent 表被成功删除
3. **Given** Node-Agent 绑定功能, **When** 通过 API 查询或操作, **Then** 系统通过 agent_bound 表正常处理

---

### Edge Cases

- 如果迁移过程中发现数据不一致（agent_bound 缺少 node_2_agent 中的记录），系统应如何处理？
  - 迁移脚本应先验证数据完整性，发现不一致时终止迁移并报告
- 如果外部系统仍在使用废弃的 API 字段，系统应如何处理？
  - 已确认无外部系统依赖，API 直接移除废弃字段
- 如果在清理过程中需要回滚，系统应如何处理？
  - 每个清理步骤应设计为可独立回滚的迁移脚本

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须移除 node 表的 `agent_team_id` 字段
- **FR-002**: 系统必须移除 topology 表的 `coordinator_agent_id` 字段
- **FR-003**: 系统必须在验证数据完整性后移除 topology 表的 `global_supervisor_agent_id` 字段
- **FR-004**: 系统必须在验证数据完整性和代码切换后删除 `node_2_agent` 表
- **FR-005**: 系统必须移除所有 Java 代码中对废弃字段的引用（PO、DTO、Domain Model、Service、Mapper）
- **FR-006**: 系统必须更新 Mapper XML 文件，移除废弃字段的查询和映射
- **FR-007**: API 直接移除废弃字段（无需向后兼容过渡期，确认无外部系统依赖）
- **FR-008**: 系统必须提供数据迁移验证脚本，确保 agent_bound 表数据完整性

### Key Entities

- **Node**: 资源节点实体，移除 agentTeamId 属性
- **Topology**: 拓扑图实体，移除 coordinatorAgentId 和 globalSupervisorAgentId 属性
- **AgentBound**: Agent 绑定关系实体（保留，作为统一的绑定管理表）

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有废弃字段成功从数据库中移除，不影响现有功能
- **SC-002**: 所有相关 API 功能测试通过，无回归问题
- **SC-003**: 代码库中不再存在对废弃字段的引用
- **SC-004**: 数据库表结构简化，减少 4 个废弃字段/表
- **SC-005**: agent_bound 表能够完整支持所有 Agent 绑定场景
- **SC-006**: 系统响应时间保持不变或有所改善（减少不必要的字段处理）

## Assumptions

- agent_bound 表已正确包含所有从 node_2_agent 和 topology.global_supervisor_agent_id 迁移的数据
- 没有外部系统直接依赖废弃字段（如直接数据库访问）
- 清理工作可以分阶段进行，每个阶段独立可回滚
- 项目使用 Flyway 管理数据库迁移

## Out of Scope

- 性能优化（除非清理本身带来的自然改善）
- 新增 agent_bound 表的功能
- 修改现有 Agent 绑定业务逻辑
- 清理其他可能存在的废弃代码（本次仅针对识别的 4 个字段/表）
