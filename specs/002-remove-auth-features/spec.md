# Feature Specification: 移除认证相关功能

**Feature Branch**: `002-remove-auth-features`
**Created**: 2025-12-27
**Status**: Completed
**Input**: User description: "用户与认证功能已迁移到 op-stack-auth 服务中，需要从当前项目移除相关代码并更新 Swagger 文档"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 移除认证相关的代码文件 (Priority: P1)

作为系统维护者，需要移除项目中认证相关的代码文件，因为这些功能已迁移到独立的 op-stack-auth 服务。移除后可以减少代码冗余，明确服务边界。

**Why this priority**: 这些文件是废弃代码，不再使用，需要清理以保持代码库整洁。

**Independent Test**: 可通过编译验证移除后代码无依赖问题，运行测试确保功能正常。

**Acceptance Scenarios**:

1. **Given** 代码库中存在 RefreshTokenResponse.java, **When** 执行清理, **Then** 该文件被删除
2. **Given** 代码库中存在 AuthErrorCode.java, **When** 执行清理, **Then** 该文件被删除
3. **Given** 代码库中存在 SessionErrorCode.java, **When** 执行清理, **Then** 该文件被删除
4. **Given** 代码库中存在 auth 相关的空目录（仅含 .gitkeep）, **When** 执行清理, **Then** 这些目录被删除
5. **Given** 清理完成后, **When** 执行 `mvn clean compile`, **Then** 编译成功

---

### User Story 2 - 更新 Swagger 文档配置 (Priority: P2)

作为 API 使用者，需要 Swagger 文档准确反映当前服务的功能范围，移除已迁移到其他服务的功能描述，避免产生误导。

**Why this priority**: 文档更新是清理工作的重要组成部分，确保 API 文档与实际功能一致。

**Independent Test**: 可通过访问 Swagger UI 验证文档内容正确更新。

**Acceptance Scenarios**:

1. **Given** OpenApiConfig 包含"用户与认证"功能描述, **When** 执行更新, **Then** 该描述被移除
2. **Given** 更新完成后, **When** 访问 Swagger UI, **Then** 不再显示认证相关的功能说明
3. **Given** 更新完成后, **When** 查看 API 描述, **Then** 仅显示资源管理、拓扑图、提示词模板等当前服务的功能

---

## Functional Requirements *(mandatory)*

### 代码清理要求

- **FR-001**: 系统 MUST 移除 `interface/interface-http/src/main/java/.../dto/auth/RefreshTokenResponse.java`
- **FR-002**: 系统 MUST 移除 `common/src/main/java/.../enums/AuthErrorCode.java`
- **FR-003**: 系统 MUST 移除 `common/src/main/java/.../enums/SessionErrorCode.java`
- **FR-004**: 系统 MUST 移除 `interface/interface-http/src/main/java/.../controller/auth/` 目录（仅含 .gitkeep）
- **FR-005**: 系统 MUST 移除 `interface/interface-http/src/main/java/.../dto/auth/` 目录
- **FR-006**: 系统 MUST 移除 `application/application-api/src/main/java/.../service/auth/` 目录（仅含 .gitkeep）
- **FR-007**: 系统 MUST 移除 `application/application-api/src/main/java/.../command/auth/` 目录（仅含 .gitkeep）
- **FR-008**: 系统 MUST 移除 `application/application-impl/src/main/java/.../service/auth/` 目录（仅含 .gitkeep）

### Swagger 文档更新要求

- **FR-009**: 系统 MUST 更新 `OpenApiConfig.java` 中的 API 描述，移除"用户与认证"功能模块说明
- **FR-010**: 系统 MUST 更新 API 描述中的认证方式说明，改为说明当前服务的认证机制（由网关统一处理）
- **FR-011**: 系统 MUST 保留资源管理、拓扑图管理、提示词模板管理等功能的描述

### 注释清理要求

- **FR-012**: 系统 MUST 更新 `BusinessException.java` 中引用 AuthErrorCode 的示例注释
- **FR-013**: 系统 MUST 更新 `BaseException.java` 中引用 AuthErrorCode 的示例注释
- **FR-014**: 系统 MUST 更新 `ErrorCode.java` 中引用 AuthErrorCode 的示例注释

---

## Success Criteria *(mandatory)*

- **SC-001**: 编译成功（`mvn clean compile` 无错误）
- **SC-002**: 打包成功（`mvn clean package -DskipTests` 无错误）
- **SC-003**: Swagger UI 正常访问且不显示认证相关功能描述
- **SC-004**: 代码中不存在任何认证相关的废弃文件（RefreshTokenResponse、AuthErrorCode、SessionErrorCode）
- **SC-005**: 所有标记为清理的空目录已被删除

---

## Scope *(mandatory)*

### In Scope

- 移除认证相关的 DTO 文件（RefreshTokenResponse.java）
- 移除认证相关的错误码枚举（AuthErrorCode.java、SessionErrorCode.java）
- 移除仅含 .gitkeep 的空 auth 目录
- 更新 OpenApiConfig.java 中的 Swagger 文档描述

### Out of Scope

- 不移除 GlobalExceptionHandler 中对错误码格式的通用处理逻辑
- 不修改其他服务（op-stack-auth）的代码
- 不添加新功能
- 不修改数据库结构

---

## Key Entities *(optional - include only if data is involved)*

| 实体 | 类型 | 说明 |
|------|------|------|
| RefreshTokenResponse | DTO | 令牌刷新响应，待删除 |
| AuthErrorCode | Enum | 认证错误码枚举，待删除 |
| SessionErrorCode | Enum | 会话错误码枚举，待删除 |
| OpenApiConfig | Config | Swagger 配置类，需更新 |

---

## Assumptions

- 认证功能已完全迁移到 op-stack-auth 服务，当前项目不再需要这些代码
- 错误码枚举（AuthErrorCode、SessionErrorCode）在当前项目中仅在注释中作为示例被引用，无实际代码依赖
- auth 相关目录仅包含 .gitkeep 占位文件，没有实际代码
- Swagger 文档更新后，API 使用者可以正确理解当前服务的功能范围
- 需要同步更新 BusinessException、BaseException、ErrorCode 等文件中的示例注释

---

## Dependencies

- 依赖前置任务：无
- 外部依赖：无（认证功能已迁移到独立服务）
