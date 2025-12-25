# Data Model: POST-Only API 重构

**Feature**: 024-post-only-api
**Date**: 2025-12-22

## Overview

本次重构**不涉及数据模型变更**。这是一个纯接口层（interface-http）的技术重构，仅改变 HTTP 方法和请求参数传递方式，不影响：

- 数据库表结构
- 实体类定义
- 领域模型
- 数据传输对象（响应 DTO）

## 受影响的层级

```
┌─────────────────────────────────────────────────────┐
│ Interface Layer (interface-http)                    │ ← 本次重构范围
│  ├── Controllers (HTTP 方法变更)                     │
│  └── Request DTOs (新增/修改请求体)                  │
├─────────────────────────────────────────────────────┤
│ Application Layer                                   │ 不变
├─────────────────────────────────────────────────────┤
│ Domain Layer                                        │ 不变
├─────────────────────────────────────────────────────┤
│ Infrastructure Layer                                │ 不变
└─────────────────────────────────────────────────────┘
```

## Request DTO 变更清单

以下是需要新增或修改的请求体 DTO：

### 新增 Request DTOs

| DTO 名称 | 包路径 | 用途 |
|----------|--------|------|
| GetResourceRequest | request.resource | 获取单个资源 |
| QueryResourcesRequest | request.resource | 查询资源列表（可复用现有 ListResourcesRequest） |
| QueryResourceTypesRequest | request.resource | 查询资源类型列表 |
| QueryAuditLogsRequest | request.resource | 查询审计日志 |
| QueryMembersRequest | request.resource | 查询资源成员 |
| QueryTopologyRequest | request.resource | 查询拓扑结构 |
| QueryAncestorsRequest | request.resource | 查询祖先链 |
| GetRelationshipRequest | request.relationship | 获取单个关系 |
| QueryRelationshipsRequest | request.relationship | 查询关系列表 |
| TraverseRelationshipsRequest | request.relationship | 关系遍历 |
| CycleDetectionRequest | request.relationship | 循环检测 |
| ValidateSessionRequest | request.session | 验证会话 |
| QuerySessionsRequest | request.session | 查询会话列表 |
| TerminateSessionRequest | request.session | 终止会话 |
| GetLlmServiceRequest | request.llm | 获取 LLM 服务 |
| QueryLlmServicesRequest | request.llm | 查询 LLM 服务列表 |
| SetDefaultLlmServiceRequest | request.llm | 设置默认 LLM 服务 |
| QueryAccountsRequest | request.admin | 查询账户列表 |

### 修改现有 Request DTOs

| DTO 名称 | 修改内容 |
|----------|----------|
| UpdateResourceRequest | 添加 `id` 字段 |
| DeleteResourceRequest | 添加 `id` 字段 |
| UpdateResourceStatusRequest | 添加 `id` 字段 |
| AddMembersRequest | 添加 `resourceId` 字段 |
| RemoveMembersRequest | 添加 `resourceId` 字段 |
| UpdateRelationshipRequest | 添加 `relationshipId` 字段 |
| DeleteRelationshipRequest | 添加 `relationshipId` 字段 |
| UpdateLlmServiceRequest | 添加 `id` 字段 |
| DeleteLlmServiceRequest | 添加 `id` 字段 |
| UpdateLlmServiceStatusRequest | 添加 `id` 字段 |

## 网关注入字段支持

所有 Request DTO 应支持以下可选字段（由网关注入）：

```java
// 这些字段由 Jackson 自动忽略未知属性特性支持
// 无需在每个 DTO 中显式定义，除非需要使用
private Long tenantId;      // 租户ID
private String traceId;     // 追踪ID
private String userId;      // 用户ID
```

## 响应模型（不变）

所有响应 DTO 保持不变，继续使用 `Result<T>` 包装：

- ResourceDTO
- RelationshipDTO
- SessionDTO
- LlmServiceDTO
- 等其他现有响应 DTO
