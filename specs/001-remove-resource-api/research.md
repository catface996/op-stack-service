# Research: 移除 Resource 资源管理接口

**Feature**: 001-remove-resource-api
**Date**: 2025-12-28

## Research Summary

本特性为代码删除任务，无需深入技术研究。以下为确认事项和决策记录。

## Decision 1: 确认 Resource API 可以移除

**Decision**: Resource API 可以安全移除

**Rationale**:
- `001-split-resource-model` 特性已将 `resource` 表拆分为 `topology` 和 `node` 表
- `NodeController` 提供完整的 CRUD 功能，覆盖原 Resource API 所有功能
- ResourceController 注释明确说明是为向后兼容保留的旧 API

**Evidence**:
- ResourceController 第 92-94 行注释: "此接口返回资源节点（非 SUBGRAPH 类型），拓扑图请使用 /api/v1/topologies/query 接口查询"
- Node API 已有 create/query/get/update/delete 完整接口
- Node API 额外支持 Agent 绑定功能

**Alternatives Considered**:
- 保留 Resource API 作为别名: 拒绝，增加维护成本
- 逐步废弃: 已完成向后兼容期，现可直接移除

## Decision 2: 数据库表删除策略

**Decision**: 创建 V23 迁移脚本删除 resource 和 resource_type 表

**Rationale**:
- 当前最新迁移版本为 V22
- 数据已通过 V12__Split_resource_to_topology_and_node.sql 迁移
- 使用 `DROP TABLE IF EXISTS` 确保幂等性

**Dependencies**:
- 必须先删除代码层面的依赖
- 需要确认 resource_relationship 表状态

**Alternatives Considered**:
- 保留表结构: 拒绝，造成数据库膨胀
- 重命名表为 _deprecated: 拒绝，无实际价值

## Decision 3: 删除顺序

**Decision**: 按 DDD 层级从上到下删除

**Rationale**:
- Interface → Application → Domain → Infrastructure
- 上层依赖下层，从上往下删除可避免编译错误
- 最后创建数据库迁移脚本

**Implementation Order**:
1. Interface Layer (ResourceController, Request DTOs)
2. Application Layer (Service interface, impl, DTOs)
3. Domain Layer (Model, Service interface, impl)
4. Repository Layer (Repository interface, impl, Mapper, PO)
5. Cache Layer (Service interface, impl)
6. Database Migration (V23__Drop_resource_tables.sql)

## Files Inventory

通过代码搜索确认的待删除文件:

### Interface Layer (3 files)
- `ResourceController.java`
- `GetResourceRequest.java`
- `QueryResourceTypesRequest.java`

### Application Layer (9 files)
- `ResourceApplicationService.java`
- `ResourceApplicationServiceImpl.java`
- `ResourceDTO.java`
- `ResourceTypeDTO.java`
- `CreateResourceRequest.java`
- `DeleteResourceRequest.java`
- `ListResourcesRequest.java`
- `UpdateResourceRequest.java`
- `UpdateResourceStatusRequest.java`

### Domain Layer (8 files)
- `Resource.java`
- `ResourceStatus.java`
- `ResourceType.java`
- `OperationType.java`
- `ResourceTypeConstants.java`
- `ResourceDomainService.java`
- `ResourceDomainServiceImpl.java`
- `ResourceRepository.java` (repository-api)

### Infrastructure Layer (7 files)
- `ResourceTypeRepository.java`
- `ResourceRepositoryImpl.java`
- `ResourceTypeRepositoryImpl.java`
- `ResourceMapper.java`
- `ResourceTypeMapper.java`
- `ResourcePO.java`
- `ResourceTypePO.java`

### Cache Layer (2 files)
- `ResourceCacheService.java`
- `ResourceCacheServiceImpl.java`

**Total**: 29 files + 1 new migration file

## Potential Issues

### Issue 1: 外部依赖检查

**Status**: ✅ 已确认

需确认以下模块是否依赖 Resource:
- TopologyController: 不依赖 Resource API
- NodeController: 不依赖 Resource API
- AgentController: 不依赖 Resource API

### Issue 2: 数据库外键约束

**Status**: ✅ 已处理

`resource_relationship` 表可能有外键引用:
- 使用 `DROP TABLE IF EXISTS` 处理
- 按正确顺序删除表

## Conclusion

本特性为低风险的代码清理任务，所有技术决策已明确，可进入任务生成阶段。
