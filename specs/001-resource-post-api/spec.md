# Feature Specification: 资源管理接口统一改为POST方式

**Feature Branch**: `001-resource-post-api`
**Created**: 2025-12-25
**Status**: Draft
**Input**: User description: "将资源管理相关的http接口，统一改为POST方式"

## Clarifications

### Session 2025-12-25

- Q: 向后兼容策略 → A: 不保留旧接口，直接替换为新的POST接口
- Q: 创建资源接口的URL路径 → A: 将创建接口也改为 `POST /api/v1/resources/create` 以保持一致

## User Scenarios & Testing *(mandatory)*

### User Story 1 - API调用者使用统一POST方式调用接口 (Priority: P1)

作为一名前端开发者或API集成方，我希望所有资源管理相关的HTTP接口都使用POST方法，这样我可以：
- 在请求体中传递所有参数，避免URL长度限制
- 统一的请求格式便于封装通用的API调用方法
- 敏感数据不会暴露在URL中（如查询条件）
- 更好地支持复杂的查询条件和过滤参数

**Why this priority**: 这是本次需求的核心目标，直接影响所有API消费者的调用方式。

**Independent Test**: 可以通过调用任意一个已改造的接口验证POST方法是否生效，并确认请求体参数正确解析。

**Acceptance Scenarios**:

1. **Given** 我需要创建资源, **When** 我发送POST请求到 `/api/v1/resources/create` 并在请求体中传递资源信息, **Then** 系统创建资源并返回创建后的资源信息
2. **Given** 我需要查询资源列表, **When** 我发送POST请求到 `/api/v1/resources/list` 并在请求体中传递过滤条件, **Then** 系统返回符合条件的资源列表
3. **Given** 我需要查询单个资源详情, **When** 我发送POST请求到 `/api/v1/resources/detail` 并在请求体中传递资源ID, **Then** 系统返回该资源的详细信息
4. **Given** 我需要更新资源信息, **When** 我发送POST请求到 `/api/v1/resources/update` 并在请求体中传递资源ID和更新内容, **Then** 系统更新资源并返回更新后的信息
5. **Given** 我需要删除资源, **When** 我发送POST请求到 `/api/v1/resources/delete` 并在请求体中传递资源ID和确认名称, **Then** 系统删除资源并返回成功信息
6. **Given** 我需要更新资源状态, **When** 我发送POST请求到 `/api/v1/resources/update-status` 并在请求体中传递资源ID和新状态, **Then** 系统更新状态并返回更新后的资源信息
7. **Given** 我需要查询资源审计日志, **When** 我发送POST请求到 `/api/v1/resources/audit-logs` 并在请求体中传递资源ID和分页参数, **Then** 系统返回该资源的审计日志列表
8. **Given** 我需要查询资源类型列表, **When** 我发送POST请求到 `/api/v1/resource-types/list`, **Then** 系统返回所有可用的资源类型

---

### Edge Cases

- 当请求体为空或缺少必要字段时，系统应返回明确的错误信息
- 当请求体中的资源ID不存在时，系统应返回404错误
- 当使用错误的HTTP方法（如GET）访问新的POST端点时，系统应返回405 Method Not Allowed
- 当请求体JSON格式错误时，系统应返回400 Bad Request并提示格式错误

## Requirements *(mandatory)*

### Functional Requirements

**接口方法变更**
- **FR-001**: 系统必须将创建资源接口从 `POST /api/v1/resources` 改为 `POST /api/v1/resources/create`
- **FR-002**: 系统必须将查询资源列表接口从 `GET /api/v1/resources` 改为 `POST /api/v1/resources/list`
- **FR-003**: 系统必须将查询资源详情接口从 `GET /api/v1/resources/{id}` 改为 `POST /api/v1/resources/detail`
- **FR-004**: 系统必须将更新资源接口从 `PUT /api/v1/resources/{id}` 改为 `POST /api/v1/resources/update`
- **FR-005**: 系统必须将删除资源接口从 `DELETE /api/v1/resources/{id}` 改为 `POST /api/v1/resources/delete`
- **FR-006**: 系统必须将更新资源状态接口从 `PATCH /api/v1/resources/{id}/status` 改为 `POST /api/v1/resources/update-status`
- **FR-007**: 系统必须将查询审计日志接口从 `GET /api/v1/resources/{id}/audit-logs` 改为 `POST /api/v1/resources/audit-logs`
- **FR-008**: 系统必须将查询资源类型列表接口从 `GET /api/v1/resource-types` 改为 `POST /api/v1/resource-types/list`

**请求参数调整**
- **FR-009**: 原路径参数 `{id}` 必须移至请求体中，作为必填字段
- **FR-010**: 原查询参数（如分页参数、过滤条件）必须移至请求体中
- **FR-011**: 系统必须对请求体中的必填字段进行校验，缺少时返回明确错误信息

**向后兼容**
- **FR-012**: 原有RESTful风格接口将被直接替换，不保留旧接口

**文档更新**
- **FR-013**: API文档（Swagger/OpenAPI）必须反映新的接口定义
- **FR-014**: 接口注释必须更新为新的URL路径和HTTP方法

### Key Entities

- **GetResourceRequest**: 用于查询资源详情的请求对象，包含资源ID
- **GetResourceAuditLogsRequest**: 用于查询审计日志的请求对象，包含资源ID和分页参数
- **ListResourceTypesRequest**: 用于查询资源类型列表的请求对象（可为空对象）

*注：现有的请求对象（UpdateResourceRequest、DeleteResourceRequest、UpdateResourceStatusRequest）需要添加资源ID字段*

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有8个资源管理接口（创建、列表、详情、更新、删除、状态更新、审计日志、资源类型）均采用统一的POST方式和动词后缀URL路径
- **SC-002**: 所有接口的请求参数均通过请求体传递，不再使用URL路径参数或查询参数
- **SC-003**: API文档正确显示新的接口定义，包括请求方法、URL路径和请求体结构
- **SC-004**: 现有功能不受影响，所有原有业务逻辑保持正常工作
- **SC-005**: 接口响应时间与改造前保持一致，无性能下降

## Assumptions

1. 所有接口URL路径采用动词后缀方式（如 `/create`, `/list`, `/detail`, `/update`）区分操作类型（已确认）
2. 不保留旧接口，直接替换为新的POST接口（已确认）
3. 改造范围仅限于ResourceController中的接口，不涉及其他Controller
