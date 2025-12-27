# Feature Specification: 移除废弃的API接口

**Feature Branch**: `001-remove-deprecated-api`
**Created**: 2025-12-27
**Status**: Completed
**Input**: User description: "移除所有废弃的接口，以及接口依赖的相关废弃的代码"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 清理废弃的资源成员管理接口 (Priority: P1)

作为系统维护者，需要移除 ResourceController 中标记为 `@Deprecated` 的成员管理接口，这些接口已被 TopologyController 中的新接口替代。移除后可以减少代码维护负担，避免用户使用废弃接口。

**Why this priority**: 这些接口数量最多（6个），是主要的清理目标，且已有明确的替代方案。

**Independent Test**: 可通过调用废弃接口路径验证返回 404，同时验证新的 TopologyController 接口仍然正常工作。

**Acceptance Scenarios**:

1. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/members/add`, **Then** 返回 404 Not Found
2. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/members/remove`, **Then** 返回 404 Not Found
3. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/members/query`, **Then** 返回 404 Not Found
4. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/members-with-relations/query`, **Then** 返回 404 Not Found
5. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/topology/query`, **Then** 返回 404 Not Found
6. **Given** 系统运行中, **When** 调用 `/api/service/v1/resources/ancestors/query`, **Then** 返回 404 Not Found
7. **Given** 系统运行中, **When** 调用 `/api/service/v1/topologies/members/add`, **Then** 接口正常响应

---

### User Story 2 - 清理废弃的错误码常量类 (Priority: P2)

作为系统维护者，需要移除 `ErrorCodes` 常量类，该类已被新的错误码枚举（如 `AuthErrorCode`）替代。移除后可以统一错误码使用方式，避免混淆。

**Why this priority**: 这是一个工具类的清理，影响范围相对较小，但有助于代码一致性。

**Independent Test**: 可通过编译验证该类已被移除，且没有其他代码依赖它。

**Acceptance Scenarios**:

1. **Given** 代码库中存在 ErrorCodes.java, **When** 执行清理, **Then** 该文件被删除
2. **Given** 清理完成后, **When** 执行编译, **Then** 编译成功，无引用错误
3. **Given** 清理完成后, **When** 搜索 ErrorCodes 的引用, **Then** 没有任何代码引用该类

---

### User Story 3 - 清理废弃接口依赖的请求/响应类 (Priority: P3)

作为系统维护者，需要移除与废弃接口关联的请求和响应类，包括专门为废弃接口设计的 DTO、Request、Response 类。

**Why this priority**: 这些类是废弃接口的附属品，在接口移除后成为无用代码。

**Independent Test**: 可通过编译验证这些类已被移除，且没有其他代码依赖它们。

**Acceptance Scenarios**:

1. **Given** 废弃接口已移除, **When** 执行编译, **Then** 编译成功
2. **Given** 废弃接口已移除, **When** 运行所有测试, **Then** 测试全部通过

---

### Edge Cases

- 如果废弃的代码被其他非废弃代码引用，需要先解除依赖再删除
- 如果存在测试用例专门测试废弃接口，这些测试用例也需要一并移除
- 确保 Swagger/OpenAPI 文档中不再显示废弃接口

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 移除 ResourceController 中所有标记为 `@Deprecated` 的接口方法
- **FR-002**: 系统 MUST 移除 `ErrorCodes` 常量类（common/src/main/java/com/catface996/aiops/common/constants/ErrorCodes.java）
- **FR-003**: 系统 MUST 移除与废弃接口关联的专用请求/响应类（如果存在）
- **FR-004**: 系统 MUST 移除与废弃接口关联的测试用例（如果存在）
- **FR-005**: 系统 MUST 确保移除后编译成功且所有保留的测试通过
- **FR-006**: 系统 MUST 更新 API 文档，确保废弃接口不再显示

### 需要移除的接口清单

| 序号 | 接口路径 | 替代接口 |
|------|---------|---------|
| 1 | POST /api/service/v1/resources/members/add | /api/service/v1/topologies/members/add |
| 2 | POST /api/service/v1/resources/members/remove | /api/service/v1/topologies/members/remove |
| 3 | POST /api/service/v1/resources/members/query | /api/service/v1/topologies/members/query |
| 4 | POST /api/service/v1/resources/members-with-relations/query | 无直接替代 |
| 5 | POST /api/service/v1/resources/topology/query | /api/service/v1/topologies/graph/query |
| 6 | POST /api/service/v1/resources/ancestors/query | 无直接替代 |

### 需要移除的类清单

| 序号 | 类路径 | 说明 |
|------|--------|------|
| 1 | common/.../constants/ErrorCodes.java | 已废弃的错误码常量类 |

### Key Entities

- **ResourceController**: 包含废弃接口的控制器，需要移除部分方法
- **ErrorCodes**: 废弃的错误码常量类，需要完整移除
- **SubgraphMemberApplicationService**: 可能是废弃接口依赖的服务层，需评估是否有其他使用

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 移除后系统编译成功，无编译错误
- **SC-002**: 移除后所有保留的单元测试和集成测试通过
- **SC-003**: API 接口总数从 51 个减少到 45 个（移除 6 个废弃接口）
- **SC-004**: Swagger 文档中不再显示任何带有"已废弃"标记的接口
- **SC-005**: 代码中不再存在任何 `@Deprecated(forRemoval = true)` 标记的类或方法

## Assumptions

- TopologyController 中的替代接口功能完整，可以完全替代废弃接口
- 没有外部系统依赖这些废弃接口（内部使用已迁移到新接口）
- 废弃接口的测试用例可以安全移除，因为新接口有对应的测试覆盖
