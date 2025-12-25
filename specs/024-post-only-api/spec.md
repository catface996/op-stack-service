# Feature Specification: POST-Only API 重构

**Feature Branch**: `024-post-only-api`
**Created**: 2025-12-22
**Status**: Draft
**Input**: User description: "做大范围技术重构，将所有的http接口，全部更改为post，便于上游的网关做参数注入。"

## Clarifications

### Session 2025-12-22

- Q: 网关注入的参数应该放在请求体的什么位置？ → A: 根级平铺，直接在请求体根级添加 tenantId, traceId 等字段，与业务参数同级

## 背景与动机

当前系统的 HTTP 接口使用了多种 HTTP 方法（GET、POST、PUT、DELETE、PATCH），这在与上游网关集成时存在以下问题：

1. **参数注入限制**：GET 请求的参数在 URL 中，网关难以统一注入额外参数（如租户ID、追踪ID等）
2. **网关配置复杂**：不同 HTTP 方法需要不同的转发和参数处理规则
3. **安全审计困难**：GET 请求参数暴露在 URL 中，不便于日志脱敏处理

通过将所有接口统一为 POST 方法，可以：
- 统一请求体格式，便于网关统一注入参数
- 简化网关配置，降低运维复杂度
- 提高安全性，敏感参数不暴露在 URL 中

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 网关统一参数注入 (Priority: P1)

作为上游网关管理员，我需要能够在请求转发时统一注入额外参数（如租户ID、用户上下文、追踪ID等），以便实现多租户隔离和请求追踪。

**Why this priority**: 这是本次重构的核心目标，直接解决网关参数注入的技术障碍。

**Independent Test**: 可以通过网关向任意 API 发送请求，验证注入的参数能够正确传递到后端服务。

**Acceptance Scenarios**:

1. **Given** 网关配置了参数注入规则，**When** 客户端通过网关访问任意 API，**Then** 后端服务能够从请求体中获取网关注入的参数
2. **Given** 任意 API 端点，**When** 使用 POST 方法发送请求，**Then** 请求能够正常处理并返回预期结果
3. **Given** 旧的 GET/PUT/DELETE/PATCH 请求路径，**When** 客户端继续使用旧方法访问，**Then** 服务返回 405 Method Not Allowed 或引导使用新接口

---

### User Story 2 - API 客户端无缝迁移 (Priority: P2)

作为 API 客户端开发者，我需要清晰的迁移指南和足够的过渡期，以便平滑升级到新的 POST-Only API。

**Why this priority**: 确保现有客户端能够顺利迁移，避免服务中断。

**Independent Test**: 可以通过更新客户端 SDK 或调用代码，验证新旧接口的功能等价性。

**Acceptance Scenarios**:

1. **Given** 现有的查询类 API（原 GET），**When** 改用 POST 方法并将参数放入请求体，**Then** 返回结果与原接口完全一致
2. **Given** 现有的更新类 API（原 PUT/PATCH），**When** 改用 POST 方法，**Then** 更新操作正常执行
3. **Given** 现有的删除类 API（原 DELETE），**When** 改用 POST 方法，**Then** 删除操作正常执行

---

### User Story 3 - API 文档自动更新 (Priority: P3)

作为 API 使用者，我需要能够通过 Swagger/OpenAPI 文档查看新的接口规范，以便正确调用服务。

**Why this priority**: 文档是 API 可用性的重要保障，但不影响核心功能。

**Independent Test**: 可以通过访问 Swagger UI 验证所有接口都显示为 POST 方法。

**Acceptance Scenarios**:

1. **Given** 重构完成后，**When** 访问 Swagger UI，**Then** 所有业务接口都显示为 POST 方法
2. **Given** 任意 API 端点，**When** 查看其文档，**Then** 请求参数模型和响应模型与原接口保持一致

---

### Edge Cases

- 健康检查接口（/actuator/health）是否需要保持 GET 方法？（通常监控系统依赖 GET）
- 文件上传/下载接口是否有特殊处理需求？
- 原 DELETE 请求中 URL 路径参数（如 /resources/{id}）如何迁移到请求体？

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须将所有业务 API 的 HTTP 方法统一为 POST
- **FR-002**: 系统必须保留健康检查类接口（/actuator/*）的原有 HTTP 方法，以兼容标准监控工具
- **FR-003**: 原 GET 请求的 URL 查询参数必须迁移到 POST 请求体中
- **FR-004**: 原 PUT/PATCH 请求的路径参数和请求体必须合并到新的 POST 请求体中
- **FR-005**: 原 DELETE 请求的路径参数必须迁移到 POST 请求体中
- **FR-006**: 系统必须更新 OpenAPI/Swagger 文档以反映新的接口规范
- **FR-007**: 系统必须为每个接口定义明确的请求体结构（即使原本无请求体）
- **FR-008**: 系统响应格式和状态码语义必须保持不变
- **FR-009**: 网关注入参数（tenantId, traceId 等）必须支持在请求体根级与业务参数平铺存放

### 接口命名约定

为保持 API 的可读性和语义清晰，重构后的接口应遵循以下命名约定：

| 原方法 | 原路径示例 | 新路径建议 | 说明 |
|--------|-----------|-----------|------|
| GET    | /resources | /resources/query | 查询操作 |
| GET    | /resources/{id} | /resources/get | ID 移入请求体 |
| POST   | /resources | /resources/create | 创建操作（保持） |
| PUT    | /resources/{id} | /resources/update | ID 移入请求体 |
| DELETE | /resources/{id} | /resources/delete | ID 移入请求体 |
| PATCH  | /resources/{id}/status | /resources/update-status | ID 移入请求体 |

### 受影响的接口清单

基于当前代码分析，需要重构的接口包括：

**ResourceController** (12 个非 POST 接口):
- GET /resources → POST /resources/query
- GET /resources/{id} → POST /resources/get
- PUT /resources/{id} → POST /resources/update
- DELETE /resources/{id} → POST /resources/delete
- PATCH /resources/{id}/status → POST /resources/update-status
- GET /resources/{id}/audit-logs → POST /resources/audit-logs
- GET /resource-types → POST /resource-types/query
- GET /resources/{id}/members → POST /resources/members/query
- DELETE /resources/{id}/members → POST /resources/members/remove
- GET /resources/{id}/members-with-relations → POST /resources/members-with-relations
- GET /resources/{id}/topology → POST /resources/topology
- GET /resources/{id}/ancestors → POST /resources/ancestors

**RelationshipController** (7 个非 POST 接口)

**SessionController** (3 个非 POST 接口)

**LlmServiceController** (6 个非 POST 接口)

**AdminController** (1 个非 POST 接口)

**AuthController** (保持不变，已全部为 POST)

**HealthController** (保持 GET，兼容监控)

### Key Entities

- **API Endpoint**: 表示一个 HTTP 接口，包含路径、方法、请求参数、响应结构
- **Request Body Schema**: 每个 POST 接口的请求体结构定义
- **Migration Mapping**: 新旧接口的映射关系，用于客户端迁移

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的业务 API 使用 POST 方法（健康检查除外）
- **SC-002**: 所有重构后的接口功能与原接口完全等价，单元测试和集成测试通过率 100%
- **SC-003**: Swagger 文档完整更新，所有接口可通过文档直接测试
- **SC-004**: 网关能够统一向所有 API 请求体注入额外参数

## Assumptions

1. 客户端迁移由调用方自行完成，本次重构不提供向后兼容层
2. 所有接口的认证机制（JWT Bearer Token）保持不变
3. 响应格式（Result 包装）保持不变
4. 健康检查接口 /actuator/* 保持原有方法，不在本次重构范围内

## Out of Scope

- 客户端 SDK 更新
- 向后兼容适配层
- 性能优化
- 新功能添加
