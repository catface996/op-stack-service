# Feature Specification: 移除 Resource 资源管理接口

**Feature Branch**: `001-remove-resource-api`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "资源管理 IT资源管理接口：创建、查询、更新、删除、状态管理、审计日志（POST-Only API） 是否可以移除，已经有资源节点管理相关endpoint"

## Background

在 `001-split-resource-model` 特性中，原 `resource` 表已被拆分为：
- `topology` 表 - 存储拓扑图数据
- `node` 表 - 存储资源节点数据

当前存在两套功能重叠的 API：
1. **ResourceController** (`/api/service/v1/resources/*`) - 旧的资源管理接口
2. **NodeController** (`/api/service/v1/nodes/*`) - 新的资源节点管理接口

`ResourceController` 是为向后兼容保留的旧 API，现可以移除以简化系统架构。

## User Scenarios & Testing

### User Story 1 - 移除 Resource 接口代码 (Priority: P1)

作为开发者，我需要移除 ResourceController 及其相关代码，以减少代码冗余和维护成本。

**Why this priority**: 核心任务，移除冗余代码是本特性的主要目标

**Independent Test**: 移除后编译通过，系统正常启动，Node API 功能不受影响

**Acceptance Scenarios**:

1. **Given** ResourceController 及相关代码存在，**When** 移除所有 Resource 相关代码，**Then** 项目编译成功且无错误
2. **Given** 服务启动，**When** 访问 `/api/service/v1/resources/*` 任意接口，**Then** 返回 404 Not Found
3. **Given** 服务启动，**When** 访问 `/api/service/v1/nodes/*` 接口，**Then** 功能正常，响应符合预期

---

### User Story 2 - 清理 Resource 数据库表结构 (Priority: P2)

作为系统管理员，我需要清理不再使用的 `resource` 和 `resource_type` 表，以保持数据库结构整洁。

**Why this priority**: 依赖 US1 完成后执行，确保代码层面无依赖后再清理数据库

**Independent Test**: 数据库迁移执行成功，相关表被删除，系统正常运行

**Acceptance Scenarios**:

1. **Given** `resource` 表存在，**When** 执行数据库迁移脚本，**Then** `resource` 表被删除
2. **Given** `resource_type` 表存在，**When** 执行数据库迁移脚本，**Then** `resource_type` 表被删除
3. **Given** 数据库迁移完成，**When** 启动服务，**Then** 服务正常启动，无数据库相关错误

---

### Edge Cases

- 如果有外部系统仍在调用 Resource API，会收到 404 响应（需提前通知）
- 如果数据库中有未迁移的数据，需要在删表前确认数据已迁移到 node 表

## Requirements

### Functional Requirements

- **FR-001**: 系统必须移除 `ResourceController` 控制器类及其所有端点
- **FR-002**: 系统必须移除 `ResourceApplicationService` 接口及其实现
- **FR-003**: 系统必须移除 `ResourceDomainService` 接口及其实现
- **FR-004**: 系统必须移除 `ResourceRepository` 接口及其实现
- **FR-005**: 系统必须移除所有 Resource 相关的 DTO、Request、PO 类
- **FR-006**: 系统必须移除 `resource` 数据库表
- **FR-007**: 系统必须移除 `resource_type` 数据库表
- **FR-008**: 移除后 Node API (`/api/service/v1/nodes/*`) 必须保持正常功能

### Key Entities

- **Resource** (将被删除): 旧的 IT 资源实体，包含 name、description、type、status 等属性
- **ResourceType** (将被删除): 资源类型定义，包含 code、name、icon 等属性
- **Node** (保留): 新的资源节点实体，已包含原 Resource 的所有功能

## Success Criteria

### Measurable Outcomes

- **SC-001**: 移除后项目编译成功，无编译错误
- **SC-002**: 移除后系统启动时间不超过之前的 1.1 倍
- **SC-003**: Node API 所有功能测试通过率 100%
- **SC-004**: 代码量减少（预计删除 15+ 个文件，1000+ 行代码）

## Assumptions

- 所有原 Resource 数据已通过 `001-split-resource-model` 迁移到 Node 表
- 无外部系统依赖 Resource API（或已提前通知废弃）
- 向后兼容期（3 个月）已过或用户确认可以提前移除

## Scope

### In Scope
- 移除 ResourceController 及所有相关代码
- 移除 resource 和 resource_type 数据库表
- 清理相关的单元测试（如有）

### Out of Scope
- Node API 的任何修改或增强
- Topology API 的任何修改
- 其他模块的重构
