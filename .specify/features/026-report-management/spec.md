# Feature Specification: Report Management

**Feature Branch**: `026-report-management`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "../op-stack-web 项目中，docs/report-management-api-requirements.md 是一份需求文档，实现后端相关功能。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - List Reports with Pagination (Priority: P0)

用户需要查看报告列表，支持分页、按类型筛选、按状态筛选和关键词搜索，以便快速找到所需的报告。

**Why this priority**: 报告列表是用户访问报告管理功能的入口，前端已实现分页和筛选 UI，后端 API 是必须有的基础功能。

**Independent Test**: 可以通过调用 `/api/service/v1/reports/list` 接口验证分页、筛选和排序功能是否正常工作。

**Acceptance Scenarios**:

1. **Given** 系统中存在多个报告, **When** 用户请求第一页（page=1, size=10）, **Then** 返回前 10 条报告和分页信息
2. **Given** 系统中存在不同类型的报告, **When** 用户按类型 "Security" 筛选, **Then** 只返回类型为 Security 的报告
3. **Given** 系统中存在报告, **When** 用户输入关键词 "audit" 搜索, **Then** 返回标题、摘要或标签中包含 "audit" 的报告
4. **Given** 系统中存在报告, **When** 用户请求按 created_at 降序排序, **Then** 返回按创建时间倒序排列的报告

---

### User Story 2 - Get Report Detail (Priority: P0)

用户需要查看报告的详细内容，包括完整的 Markdown 内容，以便阅读完整报告。

**Why this priority**: 查看报告详情是报告管理的核心功能，与列表一起构成 P0 必须实现的功能。

**Independent Test**: 可以通过调用 `/api/service/v1/reports/get` 接口验证能否正确返回报告的所有字段。

**Acceptance Scenarios**:

1. **Given** 存在 ID 为 "rpt-001" 的报告, **When** 用户请求该报告详情, **Then** 返回报告的所有字段包括完整的 content
2. **Given** 不存在 ID 为 "rpt-999" 的报告, **When** 用户请求该报告详情, **Then** 返回 404 错误（报告不存在）

---

### User Story 3 - Create Report (Priority: P1)

用户需要创建新的报告，填写标题、类型、状态、作者、摘要、内容等信息。

**Why this priority**: 创建报告是用户生成报告的基本操作，是 P1 应该实现的功能。

**Independent Test**: 可以通过调用 `/api/service/v1/reports/create` 接口验证报告创建功能。

**Acceptance Scenarios**:

1. **Given** 用户填写了所有必填字段, **When** 用户提交创建请求, **Then** 系统创建报告并返回新报告的完整信息
2. **Given** 用户未填写标题字段, **When** 用户提交创建请求, **Then** 返回 400 错误（标题是必填项）
3. **Given** 用户指定了无效的类型值, **When** 用户提交创建请求, **Then** 返回 400 错误（无效的报告类型）
4. **Given** 用户指定了关联的 topology_id, **When** topology 不存在, **Then** 返回 404 错误（拓扑不存在）

---

### User Story 4 - Delete Report (Priority: P1)

用户需要删除不再需要的报告。

**Why this priority**: 删除报告是报告管理的基本操作，与创建一起构成 P1 功能。

**Independent Test**: 可以通过调用 `/api/service/v1/reports/delete` 接口验证报告删除功能。

**Acceptance Scenarios**:

1. **Given** 存在 ID 为 "rpt-001" 的报告, **When** 用户请求删除该报告, **Then** 报告被删除，返回成功
2. **Given** 不存在 ID 为 "rpt-999" 的报告, **When** 用户请求删除该报告, **Then** 返回 404 错误（报告不存在）

---

### User Story 5 - List Report Templates (Priority: P2)

用户需要查看报告模板列表，支持分页、按分类筛选和关键词搜索。

**Why this priority**: 模板管理是报告管理的辅助功能，P2 优先级。

**Independent Test**: 可以通过调用 `/api/service/v1/report-templates/list` 接口验证模板列表功能。

**Acceptance Scenarios**:

1. **Given** 系统中存在多个模板, **When** 用户请求模板列表, **Then** 返回模板列表和分页信息
2. **Given** 系统中存在不同分类的模板, **When** 用户按分类 "Security" 筛选, **Then** 只返回该分类的模板

---

### User Story 6 - Get Template Detail (Priority: P2)

用户需要查看模板的详细内容，包括完整的模板内容（含占位符）。

**Why this priority**: 与模板列表一起构成 P2 模板管理功能。

**Independent Test**: 可以通过调用 `/api/service/v1/report-templates/get` 接口验证。

**Acceptance Scenarios**:

1. **Given** 存在 ID 为 "tpl-001" 的模板, **When** 用户请求该模板详情, **Then** 返回模板的所有字段包括 content
2. **Given** 不存在 ID 为 "tpl-999" 的模板, **When** 用户请求该模板详情, **Then** 返回 404 错误

---

### User Story 7 - Create Report Template (Priority: P2)

用户需要创建新的报告模板。

**Why this priority**: 模板 CRUD 属于 P2 功能。

**Independent Test**: 可以通过调用 `/api/service/v1/report-templates/create` 接口验证。

**Acceptance Scenarios**:

1. **Given** 用户填写了所有必填字段, **When** 用户提交创建请求, **Then** 系统创建模板并返回新模板信息
2. **Given** 用户未填写名称字段, **When** 用户提交创建请求, **Then** 返回 400 错误

---

### User Story 8 - Update Report Template (Priority: P2)

用户需要更新现有的报告模板。

**Why this priority**: 模板 CRUD 属于 P2 功能。

**Independent Test**: 可以通过调用 `/api/service/v1/report-templates/update` 接口验证。

**Acceptance Scenarios**:

1. **Given** 存在 ID 为 "tpl-001" 的模板, **When** 用户更新模板名称, **Then** 模板名称被更新，updated_at 自动更新
2. **Given** 不存在 ID 为 "tpl-999" 的模板, **When** 用户请求更新, **Then** 返回 404 错误

---

### User Story 9 - Delete Report Template (Priority: P2)

用户需要删除不再使用的报告模板。

**Why this priority**: 模板 CRUD 属于 P2 功能。

**Independent Test**: 可以通过调用 `/api/service/v1/report-templates/delete` 接口验证。

**Acceptance Scenarios**:

1. **Given** 存在 ID 为 "tpl-001" 的模板, **When** 用户请求删除, **Then** 模板被删除
2. **Given** 不存在 ID 为 "tpl-999" 的模板, **When** 用户请求删除, **Then** 返回 404 错误

---

### User Story 10 - Auto-Generate Report (Priority: P3)

用户需要基于模板和拓扑数据自动生成报告（AI 集成功能）。

**Why this priority**: AI 自动生成功能属于 P3 未来功能，可以在后续版本实现。

**Independent Test**: 可以通过调用 `/api/service/v1/reports/generate` 接口验证（返回任务 ID）。

**Acceptance Scenarios**:

1. **Given** 存在模板和拓扑, **When** 用户请求自动生成报告, **Then** 返回任务 ID 和处理状态

---

### Edge Cases

- **分页边界**: 请求超出总页数的页码时，应返回空列表而非错误
- **空搜索结果**: 搜索关键词无匹配时，返回空列表
- **特殊字符搜索**: 搜索关键词包含特殊字符时的处理
- **长内容处理**: 报告 content 字段包含大量 Markdown 内容时的性能
- **并发删除**: 同时删除同一报告时的处理
- **无效枚举值**: type 或 status 字段传入无效枚举值时的校验

## Requirements *(mandatory)*

### Functional Requirements

**报告管理 (Reports)**

- **FR-001**: System MUST 提供分页查询报告列表的 API（`POST /api/service/v1/reports/list`）
- **FR-002**: System MUST 支持按 type（类型）筛选报告
- **FR-003**: System MUST 支持按 status（状态）筛选报告
- **FR-004**: System MUST 支持按 keyword（关键词）搜索报告（搜索范围：title, summary, tags）
- **FR-005**: System MUST 支持按 sort_by 和 sort_order 排序报告列表
- **FR-006**: System MUST 提供获取单个报告详情的 API（`POST /api/service/v1/reports/get`）
- **FR-007**: System MUST 提供创建报告的 API（`POST /api/service/v1/reports/create`）
- **FR-008**: System MUST 提供删除报告的 API（`POST /api/service/v1/reports/delete`）
- **FR-009**: Reports MUST 创建后不可修改（immutable），系统不提供更新报告的 API

**报告模板管理 (ReportTemplates)**

- **FR-010**: System MUST 提供分页查询模板列表的 API（`POST /api/service/v1/report-templates/list`）
- **FR-011**: System MUST 支持按 category（分类）筛选模板
- **FR-012**: System MUST 支持按 keyword（关键词）搜索模板
- **FR-013**: System MUST 提供获取单个模板详情的 API（`POST /api/service/v1/report-templates/get`）
- **FR-014**: System MUST 提供创建模板的 API（`POST /api/service/v1/report-templates/create`）
- **FR-015**: System MUST 提供更新模板的 API（`POST /api/service/v1/report-templates/update`）
- **FR-016**: System MUST 提供删除模板的 API（`POST /api/service/v1/report-templates/delete`）

**数据校验**

- **FR-017**: System MUST 校验 Report 的 type 字段为有效枚举值：`Diagnosis`, `Audit`, `Performance`, `Security`
- **FR-018**: System MUST 校验 Report 的 status 字段为有效枚举值：`Draft`, `Final`, `Archived`
- **FR-019**: System MUST 校验 ReportTemplate 的 category 字段为有效枚举值：`Incident`, `Performance`, `Security`, `Audit`
- **FR-020**: System MUST 校验 title 字段不超过 200 字符
- **FR-021**: System MUST 校验 summary 字段不超过 500 字符
- **FR-022**: System MUST 校验 template name 字段不超过 100 字符
- **FR-023**: System MUST 校验 template description 字段不超过 500 字符

**关联校验**

- **FR-024**: 创建报告时，如果提供了 topology_id，System SHOULD 校验该拓扑是否存在

**未来功能 (P3)**

- **FR-025**: System MAY 提供自动生成报告的 API（`POST /api/service/v1/reports/generate`），返回异步任务 ID

### Key Entities

- **Report**: 报告实体，包含 id、title、type、status、author、summary、content、tags、topology_id、created_at。报告创建后不可修改。
  - type: Diagnosis | Audit | Performance | Security
  - status: Draft | Final | Archived
  - content: Markdown 格式的完整报告内容
  - tags: 用于分类的标签数组

- **ReportTemplate**: 报告模板实体，包含 id、name、description、category、content、tags、created_at、updated_at。
  - category: Incident | Performance | Security | Audit
  - content: 包含 `{{placeholder}}` 语法的 Markdown 模板

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 所有 P0 API（list, get）能够正常工作，前端能够成功加载报告列表和详情
- **SC-002**: 所有 P1 API（create, delete）能够正常工作，前端能够成功创建和删除报告
- **SC-003**: 所有 P2 API（模板 CRUD）能够正常工作，前端能够管理报告模板
- **SC-004**: API 响应格式符合项目标准的 `Result<T>` 和 `PageResult<T>` 结构
- **SC-005**: 分页接口符合 constitution.md 中定义的分页协议
- **SC-006**: 所有 API 遵循 POST-Only 设计模式
- **SC-007**: API 文档通过 SpringDoc OpenAPI 正确生成
