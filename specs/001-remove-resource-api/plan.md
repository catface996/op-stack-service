# Implementation Plan: 移除 Resource 资源管理接口

**Branch**: `001-remove-resource-api` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-resource-api/spec.md`

## Summary

移除已废弃的 Resource 资源管理接口及相关代码。在 `001-split-resource-model` 特性中，原 `resource` 表已被拆分为 `topology` 和 `node` 表，ResourceController 作为向后兼容保留的旧 API 现可移除。主要工作包括：
1. 删除 ResourceController 及所有相关代码（约 25 个文件）
2. 创建数据库迁移脚本删除 `resource` 和 `resource_type` 表
3. 验证 Node API 功能不受影响

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD multi-module architecture
**Performance Goals**: 移除后启动时间不超过之前的 1.1 倍
**Constraints**: 确保 Node API 功能不受影响
**Scale/Scope**: 删除约 25 个文件，1500+ 行代码

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| DDD 分层架构 | ✅ PASS | 按层级顺序删除文件，遵循依赖关系 |
| POST-Only API | ✅ N/A | 仅删除代码，不新增接口 |
| Flyway 迁移 | ✅ PASS | 使用 V23__ 前缀的迁移脚本删除表 |
| API URL Convention | ✅ N/A | 仅删除代码，不修改现有接口 |

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-resource-api/
├── spec.md              # 功能规格说明
├── plan.md              # 本文件（实施计划）
├── research.md          # Phase 0 研究产出
├── checklists/          # 检查清单
│   └── requirements.md
└── tasks.md             # Phase 2 任务清单
```

### Source Code - Files to Remove

```text
# 待删除文件清单

## Interface Layer (2 files)
interface/interface-http/src/main/java/.../controller/ResourceController.java
interface/interface-http/src/main/java/.../request/resource/
├── GetResourceRequest.java
└── QueryResourceTypesRequest.java

## Application Layer (8 files)
application/application-api/src/main/java/.../dto/resource/
├── ResourceDTO.java
├── ResourceTypeDTO.java
└── request/
    ├── CreateResourceRequest.java
    ├── DeleteResourceRequest.java
    ├── ListResourcesRequest.java
    ├── UpdateResourceRequest.java
    └── UpdateResourceStatusRequest.java
application/application-api/src/main/java/.../service/resource/ResourceApplicationService.java
application/application-impl/src/main/java/.../service/resource/ResourceApplicationServiceImpl.java

## Domain Layer (8 files)
domain/domain-model/src/main/java/.../model/resource/
├── Resource.java
├── ResourceStatus.java
├── ResourceType.java
└── OperationType.java
domain/domain-api/src/main/java/.../constant/ResourceTypeConstants.java
domain/domain-api/src/main/java/.../service/resource/ResourceDomainService.java
domain/domain-impl/src/main/java/.../service/resource/ResourceDomainServiceImpl.java

## Repository Layer (8 files)
domain/repository-api/src/main/java/.../repository/resource/
├── ResourceRepository.java
└── ResourceTypeRepository.java
infrastructure/repository/mysql-impl/src/main/java/.../impl/resource/
├── ResourceRepositoryImpl.java
└── ResourceTypeRepositoryImpl.java
infrastructure/repository/mysql-impl/src/main/java/.../mapper/resource/
├── ResourceMapper.java
└── ResourceTypeMapper.java
infrastructure/repository/mysql-impl/src/main/java/.../po/resource/
├── ResourcePO.java
└── ResourceTypePO.java

## Cache Layer (2 files)
domain/cache-api/src/main/java/.../cache/api/service/ResourceCacheService.java
infrastructure/cache/redis-impl/src/main/java/.../cache/redis/service/ResourceCacheServiceImpl.java

## Database Migration (1 new file)
bootstrap/src/main/resources/db/migration/V23__Drop_resource_tables.sql
```

**Structure Decision**: 按 DDD 层级从上到下删除文件，最后创建数据库迁移脚本

## Files to Remove Summary

| Layer | Files | Estimated Lines |
|-------|-------|-----------------|
| Interface | 3 | ~300 |
| Application | 9 | ~500 |
| Domain | 8 | ~400 |
| Infrastructure | 8 | ~400 |
| Cache | 2 | ~100 |
| **Total** | **30** | **~1700** |

## Database Changes

### V23__Drop_resource_tables.sql

```sql
-- 删除 Resource 相关表
-- 依赖关系：先删除有外键的表

-- 删除资源关系表（如果存在）
DROP TABLE IF EXISTS resource_relationship;

-- 删除资源表
DROP TABLE IF EXISTS resource;

-- 删除资源类型表
DROP TABLE IF EXISTS resource_type;
```

## Complexity Tracking

本特性为代码删除任务，复杂度低，无需特殊复杂性跟踪。

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| 外部系统依赖 | Medium | 确认无外部调用或已通知废弃 |
| 数据丢失 | Low | 数据已迁移到 node 表 |
| 编译错误 | Low | 按层级顺序删除，逐步验证 |

## Implementation Order

1. **Phase 1**: 删除 Interface Layer 文件
2. **Phase 2**: 删除 Application Layer 文件
3. **Phase 3**: 删除 Domain Layer 文件
4. **Phase 4**: 删除 Infrastructure Layer 文件
5. **Phase 5**: 删除 Cache Layer 文件
6. **Phase 6**: 创建数据库迁移脚本
7. **Phase 7**: 编译验证 & 启动测试
