# Feature Specification: Agent Management API

**Feature Branch**: `027-agent-management`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "实现 Agent 管理 API，包括 Agent 实体的增删改查、配置管理、模板、统计和团队分配功能。注：模型管理、工具管理已在其他系统实现，本系统不需关注。"

## Overview

Agent 管理模块用于管理执行自动化诊断、监控和分析任务的 AI Agent。Agent 采用层级结构组织：

- **Global Supervisor（全局监管者）**: 顶层协调者（单例）
- **Team Supervisor（团队监管者）**: 团队内 Agent 的协调者（每个团队一个）
- **Worker（工作者）**: 执行监管者分配的具体任务
- **Scouter（侦察者）**: 发现类 Agent

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 查询 Agent 列表 (Priority: P0)

运维人员需要查看系统中所有 Agent 的列表，了解各 Agent 的角色、状态和专业领域，以便进行管理和监控。

**Why this priority**: 查询是最基础的功能，是所有管理操作的前提，用户必须先能看到 Agent 列表才能进行后续操作。

**Independent Test**: 调用 `POST /api/service/v1/agents/list` 接口，能够返回分页的 Agent 列表，支持按角色、状态、团队筛选和关键词搜索。

**Acceptance Scenarios**:

1. **Given** 系统中存在多个 Agent，**When** 用户请求 Agent 列表（无筛选条件），**Then** 系统返回分页的 Agent 列表，包含基本信息（ID、名称、角色、状态、专业领域、团队、发现统计）
2. **Given** 系统中存在不同角色的 Agent，**When** 用户按角色筛选（如 role=Worker），**Then** 系统仅返回该角色的 Agent
3. **Given** 系统中存在不同状态的 Agent，**When** 用户按状态筛选（如 status=WORKING），**Then** 系统仅返回该状态的 Agent
4. **Given** 系统中存在多个团队的 Agent，**When** 用户按团队 ID 筛选，**Then** 系统仅返回该团队的 Agent
5. **Given** 系统中存在 Agent，**When** 用户输入关键词搜索，**Then** 系统返回名称或专业领域包含关键词的 Agent

---

### User Story 2 - 查看 Agent 详情 (Priority: P0)

运维人员需要查看单个 Agent 的详细信息，包括完整配置、当前任务、发现统计等。

**Why this priority**: 详情查看是管理操作的基础，用户需要了解 Agent 的完整信息才能做出正确的管理决策。

**Independent Test**: 调用 `POST /api/service/v1/agents/get` 接口，传入 Agent ID，能够返回该 Agent 的完整详情。

**Acceptance Scenarios**:

1. **Given** 存在指定 ID 的 Agent，**When** 用户请求 Agent 详情，**Then** 系统返回完整的 Agent 信息（包括配置、发现统计、当前任务等）
2. **Given** 不存在指定 ID 的 Agent，**When** 用户请求 Agent 详情，**Then** 系统返回 404 错误

---

### User Story 3 - 创建 Agent (Priority: P1)

运维人员需要创建新的 Worker Agent，配置其名称、专业领域和 AI 配置，并可选择分配到团队。

**Why this priority**: 创建是核心写操作，是构建 Agent 体系的基础。

**Independent Test**: 调用 `POST /api/service/v1/agents/create` 接口，提供必要参数，能够创建新的 Agent 并返回创建结果。

**Acceptance Scenarios**:

1. **Given** 用户提供有效的 Agent 信息（名称、角色），**When** 用户创建 Agent，**Then** 系统创建 Agent 并返回包含生成 ID 的完整 Agent 信息
2. **Given** 用户提供可选的配置信息（model、temperature、systemInstruction），**When** 用户创建 Agent，**Then** 系统使用提供的配置创建 Agent
3. **Given** 用户未提供配置信息，**When** 用户创建 Agent，**Then** 系统使用默认配置创建 Agent
4. **Given** 用户尝试创建第二个 GLOBAL_SUPERVISOR，**When** 用户创建 Agent，**Then** 系统返回 409 冲突错误
5. **Given** 同一团队中已存在同名 Agent，**When** 用户创建同名 Agent，**Then** 系统返回 409 冲突错误
6. **Given** 用户提供无效参数（如空名称、无效角色），**When** 用户创建 Agent，**Then** 系统返回 400 验证错误

---

### User Story 4 - 更新 Agent 信息 (Priority: P1)

运维人员需要更新 Agent 的基本信息（名称、专业领域、状态）和配置信息。

**Why this priority**: 更新操作是日常管理的核心需求，Agent 配置需要根据实际情况调整。

**Independent Test**: 调用 `POST /api/service/v1/agents/update` 接口，能够更新 Agent 信息并返回更新后的结果。

**Acceptance Scenarios**:

1. **Given** 存在指定 ID 的 Agent 且状态为 IDLE，**When** 用户更新 Agent 信息，**Then** 系统更新 Agent 并返回更新后的信息
2. **Given** Agent 状态为 WORKING 或 THINKING，**When** 用户尝试更新 Agent，**Then** 系统返回 423 锁定错误
3. **Given** 用户尝试更改 Agent 的角色，**When** 用户更新 Agent，**Then** 系统忽略角色更改（角色创建后不可变）
4. **Given** 更新后的名称与同团队其他 Agent 冲突，**When** 用户更新 Agent，**Then** 系统返回 409 冲突错误
5. **Given** 不存在指定 ID 的 Agent，**When** 用户更新 Agent，**Then** 系统返回 404 错误

---

### User Story 5 - 更新 Agent 配置 (Priority: P1)

运维人员需要单独更新 Agent 的 AI 配置（模型、温度、系统指令、默认上下文）。

**Why this priority**: 配置调整是高频操作，独立的配置更新接口提供更精细的控制。

**Independent Test**: 调用 `POST /api/service/v1/agents/config/update` 接口，能够更新 Agent 配置。

**Acceptance Scenarios**:

1. **Given** 存在指定 ID 的 Agent，**When** 用户更新配置（如 temperature），**Then** 系统更新配置并返回更新后的 Agent
2. **Given** 用户提供无效的 temperature 值（超出 0.0-1.0 范围），**When** 用户更新配置，**Then** 系统返回 400 验证错误
3. **Given** Agent 正在工作中，**When** 用户更新配置，**Then** 系统返回 423 锁定错误

---

### User Story 6 - 删除 Agent (Priority: P1)

运维人员需要删除不再需要的 Agent。

**Why this priority**: 删除是完整 CRUD 的必要组成部分，用于清理无用 Agent。

**Independent Test**: 调用 `POST /api/service/v1/agents/delete` 接口，能够删除指定 Agent。

**Acceptance Scenarios**:

1. **Given** 存在指定 ID 的 Worker Agent 且状态为 IDLE，**When** 用户删除 Agent，**Then** 系统删除 Agent 并返回成功
2. **Given** Agent 为 GLOBAL_SUPERVISOR，**When** 用户尝试删除，**Then** 系统返回 400 错误（不允许删除）
3. **Given** Agent 为 TEAM_SUPERVISOR 且团队中有成员，**When** 用户尝试删除，**Then** 系统返回 400 错误
4. **Given** Agent 状态为 WORKING 或 THINKING，**When** 用户尝试删除，**Then** 系统返回 423 锁定错误
5. **Given** 不存在指定 ID 的 Agent，**When** 用户删除 Agent，**Then** 系统返回 404 错误

---

### User Story 7 - 分配 Agent 到团队 (Priority: P2)

运维人员需要将 Worker Agent 分配到指定团队，由该团队的 Team Supervisor 管理。

**Why this priority**: 团队分配是组织 Agent 的重要功能，但不影响 Agent 基本功能使用。

**Independent Test**: 调用 `POST /api/service/v1/agents/assign` 接口，能够将 Agent 分配到指定团队。

**Acceptance Scenarios**:

1. **Given** 存在未分配团队的 Worker Agent 和目标团队，**When** 用户分配 Agent 到团队，**Then** 系统更新 Agent 的 teamId 并返回成功
2. **Given** Agent 已属于某团队，**When** 用户分配到新团队，**Then** 系统更新 teamId 到新团队
3. **Given** 目标团队不存在，**When** 用户分配 Agent，**Then** 系统返回 404 错误

---

### User Story 8 - 取消 Agent 团队分配 (Priority: P2)

运维人员需要将 Agent 从团队中移除，使其成为未分配状态。

**Why this priority**: 与团队分配配套，提供完整的团队管理能力。

**Independent Test**: 调用 `POST /api/service/v1/agents/unassign` 接口，能够取消 Agent 的团队分配。

**Acceptance Scenarios**:

1. **Given** Agent 已分配到某团队，**When** 用户取消分配，**Then** 系统将 Agent 的 teamId 设为 null 并返回成功
2. **Given** Agent 为 TEAM_SUPERVISOR，**When** 用户尝试取消分配，**Then** 系统返回 400 错误（Supervisor 不能脱离团队）

---

### User Story 9 - 查询 Agent 模板列表 (Priority: P2)

运维人员需要查看系统预定义的 Agent 配置模板，以便快速创建常用类型的 Agent。

**Why this priority**: 模板简化 Agent 创建流程，但不是核心功能。

**Independent Test**: 调用 `POST /api/service/v1/agents/templates/list` 接口，能够返回所有预定义的 Agent 模板。

**Acceptance Scenarios**:

1. **Given** 系统中存在预定义模板，**When** 用户请求模板列表，**Then** 系统返回所有模板（包含名称、描述、系统指令、推荐模型和温度）
2. **Given** 系统中存在 5 种标准模板，**When** 用户请求模板列表，**Then** 系统返回：Standard Coordinator、Strict Security Auditor、Performance Optimizer、Root Cause Analyst、Concise Reporter

---

### User Story 10 - 查询 Agent 统计信息 (Priority: P2)

运维人员需要查看 Agent 的整体统计信息，包括数量分布、执行情况、发现统计等。

**Why this priority**: 统计信息帮助运维人员了解系统整体状态，但不影响基本管理功能。

**Independent Test**: 调用 `POST /api/service/v1/agents/stats` 接口，能够返回 Agent 统计信息。

**Acceptance Scenarios**:

1. **Given** 系统中存在多个 Agent，**When** 用户请求统计信息（无筛选），**Then** 系统返回整体统计（总数、按角色分布、按状态分布、执行次数、成功率、平均执行时间、警告和严重问题总数）
2. **Given** 用户指定特定 Agent ID，**When** 用户请求统计信息，**Then** 系统返回该 Agent 的统计信息
3. **Given** 用户指定时间范围，**When** 用户请求统计信息，**Then** 系统返回该时间范围内的统计信息

---

### Edge Cases

- **单例约束**: 系统中只能存在一个 GLOBAL_SUPERVISOR，尝试创建第二个时返回 409 错误
- **角色不可变**: Agent 创建后角色不能更改
- **工作中保护**: 状态为 WORKING 或 THINKING 的 Agent 不能被更新或删除
- **级联操作**: 删除团队时，成员 Agent 应被取消分配（不删除）
- **Supervisor 约束**: TEAM_SUPERVISOR 在团队有成员时不能被删除
- **名称唯一性**: 同一团队内 Agent 名称必须唯一

## Requirements *(mandatory)*

### Functional Requirements

**Agent 基本管理**:
- **FR-001**: 系统必须支持分页查询 Agent 列表，支持按角色、状态、团队筛选和关键词搜索
- **FR-002**: 系统必须支持通过 ID 查询 Agent 详情
- **FR-003**: 系统必须支持创建新 Agent，包括名称、角色、专业领域、团队分配和配置
- **FR-004**: 系统必须支持更新 Agent 的基本信息（名称、专业领域、状态）
- **FR-005**: 系统必须支持删除 Agent（软删除）
- **FR-006**: 系统必须在 Agent 创建后禁止更改其角色

**Agent 配置管理**:
- **FR-007**: 系统必须支持独立更新 Agent 的 AI 配置（model、temperature、systemInstruction、defaultContext）
- **FR-008**: 系统必须验证 temperature 参数在 0.0-1.0 范围内
- **FR-009**: 系统必须为新创建的 Agent 提供默认配置（model: gemini-2.0-flash, temperature: 0.3）

**团队分配**:
- **FR-010**: 系统必须支持将 Agent 分配到指定团队
- **FR-011**: 系统必须支持取消 Agent 的团队分配
- **FR-012**: 系统必须禁止 TEAM_SUPERVISOR 取消团队分配

**Agent 模板**:
- **FR-013**: 系统必须提供预定义的 Agent 配置模板列表
- **FR-014**: 系统必须包含 5 种标准模板：Standard Coordinator、Strict Security Auditor、Performance Optimizer、Root Cause Analyst、Concise Reporter

**Agent 统计**:
- **FR-015**: 系统必须支持查询 Agent 统计信息（总数、角色分布、状态分布、执行统计、发现统计）
- **FR-016**: 系统必须支持按 Agent ID 和时间范围筛选统计信息

**业务约束**:
- **FR-017**: 系统必须保证 GLOBAL_SUPERVISOR 全局唯一（单例）
- **FR-018**: 系统必须禁止删除 GLOBAL_SUPERVISOR
- **FR-019**: 系统必须禁止删除有成员的 TEAM_SUPERVISOR
- **FR-020**: 系统必须禁止更新或删除状态为 WORKING 或 THINKING 的 Agent
- **FR-021**: 系统必须保证同一团队内 Agent 名称唯一

### Key Entities

- **Agent**: AI Agent 实体，包含 ID、名称、角色、专业领域、状态、当前任务、发现统计、配置、团队引用、创建/更新时间
- **AgentConfig**: Agent AI 配置，包含模型标识、温度参数、系统指令、默认上下文
- **AgentFindings**: Agent 发现统计，包含警告数量和严重问题数量
- **AgentTemplate**: 预定义配置模板，包含模板名称、描述、系统指令、推荐模型和温度
- **Team**: 团队实体（关联实体），包含团队 ID、资源/拓扑节点 ID、名称、Supervisor 引用、成员引用列表

### Enumerations

- **AgentRole**: Agent 角色枚举
  - GLOBAL_SUPERVISOR: 全局监管者（单例）
  - TEAM_SUPERVISOR: 团队监管者
  - WORKER: 工作者
  - SCOUTER: 侦察者

- **AgentStatus**: Agent 状态枚举
  - IDLE: 空闲，可接受任务
  - THINKING: 思考中/推理中
  - WORKING: 执行任务中
  - COMPLETED: 任务完成
  - WAITING: 等待依赖
  - ERROR: 错误状态

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户能够在 2 秒内完成 Agent 列表查询（默认分页）
- **SC-002**: 用户能够成功创建 Agent 并立即在列表中看到新建的 Agent
- **SC-003**: 系统能够正确执行所有业务约束（单例、工作中保护、级联规则）
- **SC-004**: 所有 API 接口返回格式符合项目标准响应格式（code、success、message、data）
- **SC-005**: 系统提供的 5 种预定义模板能够正确返回并可用于创建 Agent

## Assumptions

1. **模型管理在其他系统实现**: config.model 字段仅存储模型标识字符串，不做有效性验证
2. **团队管理已存在**: Team 实体的 CRUD 已在其他模块实现，本功能仅处理 Agent 与 Team 的关联关系
3. **统计数据来源**: 执行次数、成功率、平均执行时间等统计数据来自 Agent 执行记录（ExecutionLog），该功能在未来扩展中实现
4. **软删除策略**: Agent 删除采用软删除，删除后不可恢复

## Out of Scope

1. **模型管理**: AI 模型的注册、查询、验证等功能（在其他系统实现）
2. **工具管理**: Agent 可用工具的管理功能（在其他系统实现）
3. **Agent 执行**: Agent 任务执行、日志记录等运行时功能
4. **Team CRUD**: 团队实体的创建、更新、删除功能
5. **权限控制**: 基于角色的访问控制（RBAC）
