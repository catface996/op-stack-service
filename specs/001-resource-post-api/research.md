# Research: 资源管理接口统一改为POST方式

**Feature Branch**: `001-resource-post-api`
**Date**: 2025-12-25

## 研究概述

本文档记录了在实现"资源管理接口统一改为POST方式"功能前进行的技术研究和决策。

## 决策记录

### 1. URL路径命名规范

**Decision**: 采用动词后缀方式命名URL路径

**Rationale**:
- 所有接口使用 POST 方法后，需要通过 URL 路径区分操作类型
- 动词后缀（如 `/create`, `/list`, `/detail`）清晰表达操作意图
- 符合项目现有命名风格，易于理解和维护

**Alternatives Considered**:
- RESTful 风格（使用 HTTP 方法区分）- 已弃用，本次需求明确要求统一使用 POST
- 前缀命名（如 `/get-resource`）- 可读性较差，未采用

**URL映射表**:

| 操作 | 原URL | 新URL |
|------|-------|-------|
| 创建资源 | `POST /api/v1/resources` | `POST /api/v1/resources/create` |
| 查询列表 | `GET /api/v1/resources` | `POST /api/v1/resources/list` |
| 查询详情 | `GET /api/v1/resources/{id}` | `POST /api/v1/resources/detail` |
| 更新资源 | `PUT /api/v1/resources/{id}` | `POST /api/v1/resources/update` |
| 删除资源 | `DELETE /api/v1/resources/{id}` | `POST /api/v1/resources/delete` |
| 更新状态 | `PATCH /api/v1/resources/{id}/status` | `POST /api/v1/resources/update-status` |
| 审计日志 | `GET /api/v1/resources/{id}/audit-logs` | `POST /api/v1/resources/audit-logs` |
| 资源类型 | `GET /api/v1/resource-types` | `POST /api/v1/resource-types/list` |

### 2. 请求类设计

**Decision**: 为需要资源ID的操作创建或修改请求类，将ID字段移入请求体

**Rationale**:
- 统一的请求体格式便于前端封装通用API调用方法
- 敏感参数不暴露在URL中
- 支持更复杂的查询条件

**实现方案**:

| 请求类 | 操作 | 变更 |
|--------|------|------|
| `CreateResourceRequest` | 无需修改 | 已有完整字段 |
| `ListResourcesRequest` | 无需修改 | 已有分页和过滤字段 |
| `GetResourceRequest` | 新增 | 包含 `id` 字段 |
| `UpdateResourceRequest` | 添加字段 | 添加 `id` 字段 |
| `DeleteResourceRequest` | 添加字段 | 添加 `id` 字段 |
| `UpdateResourceStatusRequest` | 添加字段 | 添加 `id` 字段 |
| `GetResourceAuditLogsRequest` | 新增 | 包含 `id`, `page`, `size` 字段 |

### 3. 向后兼容策略

**Decision**: 不保留旧接口，直接替换为新接口

**Rationale**:
- 用户明确确认不需要向后兼容
- 减少代码维护复杂度
- 避免新旧接口并存带来的混淆

**Alternatives Considered**:
- 保留旧接口并标记 @Deprecated - 用户已否决
- 设置3-6个月过渡期 - 用户已否决

### 4. Spring MVC 注解使用

**Decision**: 使用 `@PostMapping` 替换原有的 `@GetMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`

**Rationale**:
- Spring MVC 原生支持，无需额外配置
- 与现有代码风格一致

**实现细节**:
- 移除 `@PathVariable` 注解，改为从 `@RequestBody` 获取参数
- 移除查询接口的 `@RequestParam` 注解，参数移入请求体
- 保持 `@Valid` 注解用于请求体校验

### 5. OpenAPI 文档更新

**Decision**: 更新 Swagger 注解以反映新的接口定义

**Rationale**:
- API文档必须与实际接口保持一致
- 便于前端开发者和集成方理解接口变更

**需要更新的注解**:
- `@Operation` - 更新 summary 和 description
- `@ApiResponses` - 保持不变（响应格式未改变）
- 移除 `@Parameter` 注解（路径参数和查询参数）

## 技术风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 现有API调用方需要修改 | 中 | 提前通知，提供迁移文档 |
| Swagger文档不同步 | 低 | 实现时同步更新注解 |
| 编译错误 | 低 | 每个任务完成后执行编译验证 |

## 结论

本次重构是一个低风险的接口层变更：
- 不涉及业务逻辑修改
- 不涉及数据库变更
- 仅修改 Controller 和 Request DTO
- 预计影响文件数：2个新增，4个修改
