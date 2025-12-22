# Research Report - Subgraph Management v2.0

**Feature**: F08 - 子图管理
**Date**: 2025-12-22
**Status**: Complete

## Executive Summary

本研究报告分析了子图管理功能从 v1.0（独立实体）到 v2.0（子图作为资源类型）的架构重构需求。现有代码已完整实现 v1.0 设计，需要迁移到新的统一资源模型。

## Current State Analysis

### 1. Existing Implementation (v1.0)

现有实现基于独立的子图表结构：

| 组件 | 位置 | 状态 |
|------|------|------|
| SubgraphController | interface-http | ✅ 已实现 |
| SubgraphApplicationService | application-api/impl | ✅ 已实现 |
| SubgraphDomainService | domain-api/impl | ✅ 已实现 |
| SubgraphRepository | repository-api/mysql-impl | ✅ 已实现 |
| SubgraphResourceRepository | repository-api/mysql-impl | ✅ 已实现 |
| Database Tables | V6__Create_subgraph_tables.sql | ✅ 已实现 |

**v1.0 数据库表结构**:
- `subgraph` - 独立的子图表
- `subgraph_permission` - 独立的子图权限表
- `subgraph_resource` - 子图与资源的关联表
- `subgraph_audit_log` - 子图审计日志表

### 2. Target Architecture (v2.0)

v2.0 设计将子图作为资源类型统一管理：

| 变更项 | 旧设计 (v1.0) | 新设计 (v2.0) |
|--------|---------------|---------------|
| 子图存储 | `subgraph` 表 | `resource` 表 (type=SUBGRAPH) |
| 权限存储 | `subgraph_permission` 表 | `resource_permission` 表 |
| 成员关联 | `subgraph_resource` 表 | `subgraph_member` 表 (重命名) |
| CRUD API | `/api/v1/subgraphs/*` | `/api/v1/resources/*` (复用) |
| 嵌套支持 | ❌ 不支持 | ✅ 支持子图嵌套 |
| 循环检测 | ❌ 不需要 | ✅ 必须检测循环引用 |

### 3. Gap Analysis

#### 需要新增的功能

1. **SUBGRAPH 资源类型**: 在 `resource_type` 表中添加预定义类型
2. **循环引用检测**: 添加嵌套子图时检测循环
3. **嵌套展开/折叠**: 拓扑图支持多层嵌套展示
4. **SubgraphMemberRepository**: 新的成员关联仓储接口
5. **祖先/后代查询**: 支持导航和循环检测

#### 需要迁移的组件

1. **SubgraphController** → 拆分为资源 API (复用) + 成员管理 API (新)
2. **SubgraphApplicationService** → SubgraphMemberApplicationService
3. **SubgraphDomainService** → SubgraphMemberDomainService
4. **SubgraphRepository** → 移除 (复用 ResourceRepository)
5. **SubgraphPermissionRepository** → 移除 (复用 ResourcePermissionRepository)

### 4. Risk Assessment

| 风险 | 严重程度 | 缓解措施 |
|------|----------|----------|
| 数据迁移失败 | 高 | 编写可回滚的迁移脚本 |
| API 兼容性 | 中 | 提供 API 版本兼容层 |
| 循环检测性能 | 低 | 使用递归 CTE 优化 |
| 嵌套深度过深 | 低 | 限制最大深度 10 层 |

## Technical Decisions

### 决策 1: 复用 Resource API 进行子图 CRUD

**选择**: 复用现有的 `/api/v1/resources` API
**理由**:
- 减少代码重复
- 统一的用户体验
- 复用权限管理基础设施

### 决策 2: 新建 SubgraphMember 专用 API

**选择**: 创建 `/api/v1/subgraphs/{id}/members` 端点
**理由**:
- 成员管理是子图特有功能
- 需要循环检测等特殊逻辑
- 与资源 CRUD 分离更清晰

### 决策 3: 循环检测算法

**选择**: 应用层 DFS + 数据库递归 CTE
**理由**:
- 添加成员时在应用层执行 DFS 检测
- 查询祖先/后代时使用递归 CTE 提高效率
- 平衡性能与正确性

### 决策 4: 数据迁移策略

**选择**: 渐进式迁移
**步骤**:
1. 添加新表和类型定义
2. 迁移现有子图数据到 resource 表
3. 迁移权限数据到 resource_permission 表
4. 重命名 subgraph_resource 为 subgraph_member
5. 删除旧表（可选，保留用于回滚）

## Implementation Approach

### Phase 1: 基础设施准备

1. 添加 SUBGRAPH 类型到 resource_type 表
2. 创建 subgraph_member 表
3. 定义 SubgraphMemberRepository 接口

### Phase 2: 领域层重构

1. 创建 SubgraphMemberDomainService
2. 实现循环检测逻辑
3. 实现嵌套展开逻辑

### Phase 3: 应用层重构

1. 创建 SubgraphMemberApplicationService
2. 拆分现有 SubgraphApplicationService
3. 复用 ResourceApplicationService

### Phase 4: 接口层重构

1. 创建 SubgraphMemberController
2. 移除/重定向现有子图 CRUD 端点
3. 添加嵌套相关 API

### Phase 5: 数据迁移

1. 编写迁移脚本
2. 执行数据迁移
3. 验证数据完整性

## Dependencies

### 内部依赖

| 依赖 | 模块 | 状态 |
|------|------|------|
| Resource API | F03 | ✅ 已实现 |
| ResourcePermission | F03 | ✅ 已实现 |
| TopologyService | F04 | ✅ 已实现 |
| AuthenticationService | F01 | ✅ 已实现 |

### 外部依赖

无新增外部依赖。

## Conclusion

v2.0 重构将显著简化系统架构，通过将子图作为资源类型统一管理，可以：

1. **减少代码重复**: 复用 60%+ 的资源管理代码
2. **支持新功能**: 启用子图嵌套和层级组织
3. **提升一致性**: 统一的权限模型和 API 风格
4. **降低维护成本**: 减少需要维护的表和代码

建议采用渐进式迁移策略，确保现有功能的平滑过渡。

---

**Document Version**: 1.0
**Last Updated**: 2025-12-22
