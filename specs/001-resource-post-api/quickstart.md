# Quickstart: 资源管理接口统一改为POST方式

**Feature Branch**: `001-resource-post-api`
**Date**: 2025-12-25

## 概述

本指南帮助开发者快速了解和使用改造后的资源管理 API。所有接口统一使用 POST 方法，参数通过请求体传递。

## 前置条件

- 启动 AIOps Service 应用
- 获取有效的 JWT Token（通过登录接口）

## API 端点一览

| 操作 | 端点 | 说明 |
|------|------|------|
| 创建资源 | `POST /api/v1/resources/create` | 创建新资源 |
| 查询列表 | `POST /api/v1/resources/list` | 分页查询资源 |
| 查询详情 | `POST /api/v1/resources/detail` | 查询单个资源 |
| 更新资源 | `POST /api/v1/resources/update` | 更新资源信息 |
| 删除资源 | `POST /api/v1/resources/delete` | 删除资源 |
| 更新状态 | `POST /api/v1/resources/update-status` | 更新资源状态 |
| 审计日志 | `POST /api/v1/resources/audit-logs` | 查询审计日志 |
| 资源类型 | `POST /api/v1/resource-types/list` | 查询资源类型 |

## 快速示例

### 1. 创建资源

```bash
curl -X POST http://localhost:8080/api/v1/resources/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "name": "web-server-01",
    "description": "生产环境Web服务器",
    "resourceTypeId": 1,
    "attributes": "{\"ip\": \"192.168.1.100\", \"port\": 8080}"
  }'
```

### 2. 查询资源列表

```bash
curl -X POST http://localhost:8080/api/v1/resources/list \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "status": "RUNNING",
    "page": 1,
    "size": 10
  }'
```

### 3. 查询资源详情

```bash
curl -X POST http://localhost:8080/api/v1/resources/detail \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "id": 1
  }'
```

### 4. 更新资源

```bash
curl -X POST http://localhost:8080/api/v1/resources/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "id": 1,
    "name": "web-server-01-updated",
    "description": "更新后的描述",
    "version": 1
  }'
```

### 5. 删除资源

```bash
curl -X POST http://localhost:8080/api/v1/resources/delete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "id": 1,
    "confirmName": "web-server-01"
  }'
```

### 6. 更新资源状态

```bash
curl -X POST http://localhost:8080/api/v1/resources/update-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "id": 1,
    "status": "STOPPED",
    "version": 1
  }'
```

### 7. 查询审计日志

```bash
curl -X POST http://localhost:8080/api/v1/resources/audit-logs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "id": 1,
    "page": 1,
    "size": 10
  }'
```

### 8. 查询资源类型列表

```bash
curl -X POST http://localhost:8080/api/v1/resource-types/list \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{}'
```

## 响应格式

所有接口返回统一的响应格式：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误响应

```json
{
  "code": 400001,
  "message": "参数无效",
  "data": null
}
```

### 常见错误码

| 错误码 | 说明 |
|--------|------|
| 400001 | 参数无效 |
| 401001 | 未认证 |
| 403001 | 无权限 |
| 404001 | 资源不存在 |
| 409001 | 版本冲突 |

## 迁移指南

### 从旧接口迁移

如果您之前使用 RESTful 风格的接口，请按以下方式迁移：

| 旧接口 | 新接口 | 变更说明 |
|--------|--------|----------|
| `POST /api/v1/resources` | `POST /api/v1/resources/create` | URL 路径变更 |
| `GET /api/v1/resources?page=1&size=10` | `POST /api/v1/resources/list` | 方法和参数位置变更 |
| `GET /api/v1/resources/1` | `POST /api/v1/resources/detail` | ID 移入请求体 |
| `PUT /api/v1/resources/1` | `POST /api/v1/resources/update` | ID 移入请求体 |
| `DELETE /api/v1/resources/1` | `POST /api/v1/resources/delete` | ID 移入请求体 |

### 主要变更点

1. **HTTP 方法**: 所有接口统一使用 `POST`
2. **URL 路径**: 使用动词后缀（`/create`, `/list`, `/detail` 等）
3. **路径参数**: 原 `{id}` 移入请求体的 `id` 字段
4. **查询参数**: 原 `?page=1&size=10` 移入请求体

## 常见问题

### Q: 为什么要统一使用 POST 方法？

A: 统一 POST 方法有以下优势：
- 请求体传参，避免 URL 长度限制
- 敏感参数不暴露在 URL 中
- 便于封装通用的 API 调用方法
- 支持更复杂的查询条件

### Q: 请求体为空怎么办？

A: 对于查询资源类型列表接口，可以传递空对象 `{}`。

### Q: 旧接口还能用吗？

A: 旧接口已被移除，请迁移到新接口。
