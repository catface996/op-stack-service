# Feature Specification: Topology 绑定 Global Supervisor Agent

**Feature Branch**: `035-topology-supervisor-agent`
**Created**: 2025-12-29
**Status**: Draft
**Input**: 拓扑图支持绑定 Global Supervisor Agent，增加字段和绑定/解绑接口

## 背景

Topology（拓扑图）需要关联一个 Global Supervisor Agent 来协调和监督整个拓扑图内的所有操作。每个拓扑图最多绑定一个 Global Supervisor Agent，这是一个一对一的关系。通过绑定 Global Supervisor Agent，拓扑图可以获得统一的监督和协调能力。

当前 topology 表已有 `coordinator_agent_id` 字段（预留字段），本功能新增 `global_supervisor_agent_id` 字段用于关联 Global Supervisor 角色的 Agent。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 绑定 Global Supervisor Agent (Priority: P1)

作为运维人员，我需要为拓扑图绑定一个 Global Supervisor Agent，以便该 Agent 能够监督和协调拓扑图内的所有操作。

**Why this priority**: 绑定功能是整个特性的核心基础操作。

**Independent Test**: 可以通过调用绑定接口后查询拓扑图详情来验证 Global Supervisor Agent 已绑定。

**Acceptance Scenarios**:

1. **Given** 存在拓扑图 A 和 Global Supervisor 角色的 Agent B, **When** 执行绑定操作, **Then** 绑定成功，查询拓扑图详情时能看到 globalSupervisorAgentId 为 B 的 ID
2. **Given** 拓扑图 A 已绑定 Agent B, **When** 绑定另一个 Agent C, **Then** 原绑定被替换，globalSupervisorAgentId 更新为 C 的 ID
3. **Given** 传入不存在的 Agent ID, **When** 执行绑定操作, **Then** 系统返回 "Agent 不存在" 错误
4. **Given** 传入非 GLOBAL_SUPERVISOR 角色的 Agent, **When** 执行绑定操作, **Then** 系统返回 "Agent 角色不匹配" 错误
5. **Given** 传入不存在的拓扑图 ID, **When** 执行绑定操作, **Then** 系统返回 "拓扑图不存在" 错误

---

### User Story 2 - 解绑 Global Supervisor Agent (Priority: P1)

作为运维人员，我需要解除拓扑图与 Global Supervisor Agent 的绑定关系，当不再需要该 Agent 监督时。

**Why this priority**: 解绑功能与绑定功能同等重要，是完整的操作闭环。

**Independent Test**: 可以通过解绑后查询拓扑图详情来验证 Global Supervisor Agent 已清空。

**Acceptance Scenarios**:

1. **Given** 拓扑图 A 已绑定 Agent B, **When** 执行解绑操作, **Then** 解绑成功，查询拓扑图详情时 globalSupervisorAgentId 为 null
2. **Given** 拓扑图 A 未绑定任何 Global Supervisor Agent, **When** 执行解绑操作, **Then** 系统返回成功（幂等操作）

---

### Edge Cases

- Agent 被删除时：拓扑图的 globalSupervisorAgentId 应保留，查询时需处理 Agent 不存在的情况
- 并发绑定操作：由于是简单字段更新，后执行的操作覆盖先执行的（最终一致性）
- 绑定的 Agent 角色被修改：系统不主动校验已绑定 Agent 的角色变化

## Requirements *(mandatory)*

### Functional Requirements

**绑定管理**:

- **FR-001**: 系统 MUST 在 topology 表新增 `global_supervisor_agent_id` 字段
- **FR-002**: 系统 MUST 提供将 Global Supervisor Agent 绑定到拓扑图的接口
- **FR-003**: 系统 MUST 在绑定时验证 Agent 存在且角色为 GLOBAL_SUPERVISOR
- **FR-004**: 系统 MUST 在绑定时验证拓扑图存在
- **FR-005**: 系统 MUST 支持替换绑定（重复绑定时更新为新 Agent）

**解绑管理**:

- **FR-006**: 系统 MUST 提供解除拓扑图与 Global Supervisor Agent 绑定的接口
- **FR-007**: 系统 MUST 支持解绑的幂等操作（未绑定时解绑也返回成功）

**查询功能**:

- **FR-008**: 系统 MUST 在拓扑图详情接口中返回 globalSupervisorAgentId 字段
- **FR-009**: 系统 SHOULD 在拓扑图详情中返回绑定的 Agent 基本信息（名称、角色）

### Key Entities

- **topology**: 拓扑图，新增 global_supervisor_agent_id 字段
- **agent**: Agent 表，role 为 GLOBAL_SUPERVISOR 的 Agent 可被绑定

### API Endpoints

| 操作 | Endpoint | Method | 描述 |
|------|----------|--------|------|
| 绑定 Agent | `/api/service/v1/topologies/supervisor/bind` | POST | 绑定 Global Supervisor Agent 到拓扑图 |
| 解绑 Agent | `/api/service/v1/topologies/supervisor/unbind` | POST | 解除拓扑图的 Global Supervisor Agent 绑定 |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 绑定操作成功率达到 100%（有效参数情况下）
- **SC-002**: 解绑操作成功率达到 100%
- **SC-003**: 绑定/解绑操作响应时间低于 200ms
- **SC-004**: 拓扑图详情查询正确返回 globalSupervisorAgentId 字段

## Assumptions

1. Agent 表已存在且包含 role 字段，值包括 GLOBAL_SUPERVISOR
2. Topology 表已存在
3. 所有接口遵循项目现有的 POST-Only API 规范
4. 一个拓扑图最多绑定一个 Global Supervisor Agent（一对一关系）

## Out of Scope

- Agent 的 CRUD 操作（已有）
- 拓扑图的 CRUD 操作（已有）
- Global Supervisor Agent 的实际协调逻辑（仅建立绑定关系）
- coordinator_agent_id 字段的管理（已预留，本次不处理）
