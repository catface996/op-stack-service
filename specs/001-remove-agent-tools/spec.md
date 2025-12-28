# Feature Specification: 移除 Agent-Tools 绑定功能

**Feature Branch**: `001-remove-agent-tools`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "agent绑定tools相关的功能，已经在另外一个服务中实现，当前服务中不再需要这部分功能，移除。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 清理 Agent-Tools 绑定代码 (Priority: P1)

作为系统维护者，我需要移除当前服务中所有与 Agent-Tools 绑定相关的代码，因为该功能已在另一个服务中实现，保留这些代码会造成维护负担和潜在的数据不一致问题。

**Why this priority**: 这是本次需求的核心目标，移除冗余代码可以降低维护成本，避免功能重复导致的混乱。

**Independent Test**: 移除代码后，服务能够正常编译、启动，所有现有 Agent CRUD 功能（创建、查询、更新、删除）正常工作，只是不再包含 toolIds 字段。

**Acceptance Scenarios**:

1. **Given** 服务包含 Agent-Tools 绑定相关代码, **When** 执行移除操作, **Then** 服务能够正常编译
2. **Given** 移除代码后的服务, **When** 启动应用, **Then** 应用正常启动无报错
3. **Given** 移除代码后的服务, **When** 调用 Agent 创建接口（不带 toolIds）, **Then** Agent 创建成功
4. **Given** 移除代码后的服务, **When** 调用 Agent 查询接口, **Then** 返回 Agent 信息（不包含 toolIds 字段）
5. **Given** 移除代码后的服务, **When** 调用 Agent 更新接口（不带 toolIds）, **Then** Agent 更新成功

---

### User Story 2 - 清理数据库表结构 (Priority: P2)

作为数据库管理员，我需要移除 Agent-Tools 关联表，以保持数据库结构的简洁，避免存储无用数据。

**Why this priority**: 数据库清理是代码清理的延续，但优先级较低，因为即使表存在也不会影响系统运行。

**Independent Test**: 执行数据库迁移脚本后，`agent_2_tool` 表被成功删除，系统功能不受影响。

**Acceptance Scenarios**:

1. **Given** 数据库中存在 `agent_2_tool` 表, **When** 执行迁移脚本, **Then** 表被成功删除
2. **Given** 删除表后, **When** 查询 Agent 列表, **Then** 查询正常返回结果

---

### Edge Cases

- 如果 `agent_2_tool` 表中存在数据：迁移脚本会直接删除表及其数据
- 如果有其他代码依赖 toolIds 字段：编译时会报错，需要同步修复

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须移除 `AgentToolRelation` 领域模型
- **FR-002**: 系统必须移除 `AgentToolRelationPO` 持久化对象
- **FR-003**: 系统必须移除 `AgentToolRelationMapper` MyBatis 映射器
- **FR-004**: 系统必须移除 `AgentToolRelationRepository` 接口及其实现
- **FR-005**: 系统必须从 `Agent` 领域模型中移除 `toolIds` 字段
- **FR-006**: 系统必须从 `AgentDTO` 中移除 `toolIds` 字段
- **FR-007**: 系统必须从 `CreateAgentRequest` 中移除 `toolIds` 字段
- **FR-008**: 系统必须从 `UpdateAgentRequest` 中移除 `toolIds` 字段
- **FR-009**: 系统必须从 `AgentApplicationServiceImpl` 中移除所有 Tools 绑定相关逻辑
- **FR-010**: 系统必须从 `AgentController` 文档注释中移除 Tools 绑定相关说明
- **FR-011**: 系统必须创建数据库迁移脚本删除 `agent_2_tool` 表

### Key Entities

- **AgentToolRelation**: 待删除的 Agent-Tool 关联实体
- **agent_2_tool**: 待删除的数据库关联表
- **Agent**: 需要移除 toolIds 字段的现有实体

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 项目能够成功编译，无编译错误
- **SC-002**: 服务能够正常启动并通过健康检查
- **SC-003**: Agent CRUD 接口全部正常工作（创建、查询、更新、删除）
- **SC-004**: API 响应中不再包含 `toolIds` 字段
- **SC-005**: 数据库中不再存在 `agent_2_tool` 表
- **SC-006**: 移除的代码文件数量 ≥ 5 个
