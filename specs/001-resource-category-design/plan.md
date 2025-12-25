# Implementation Plan: 资源分类体系设计

**Branch**: `001-resource-category-design` | **Date**: 2025-12-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-resource-category-design/spec.md`

## Summary

本功能将资源（Resource）分为两大类：**拓扑图**（Topology）和**资源节点**（Resource Node），通过独立的API接口分别管理。技术方案是在现有 `resource_type` 表的基础上，通过代码层面识别 SUBGRAPH 类型为拓扑图，其他类型为资源节点，并提供独立的查询和创建接口。

## Technical Context

**Language/Version**: Java 21 (LTS) + Spring Boot 3.4.1
**Primary Dependencies**: MyBatis-Plus 3.5.7, SpringDoc OpenAPI, Lombok
**Storage**: MySQL 8.0 (现有 `resource` 和 `resource_type` 表)
**Testing**: JUnit 5, Spring Boot Test, MockMvc
**Target Platform**: Linux server (Docker/K8s)
**Project Type**: Multi-module DDD architecture (已有)
**Performance Goals**: 标准CRUD接口，无特殊性能要求
**Constraints**: 不修改现有数据库表结构，保持向后兼容
**Scale/Scope**: 现有系统扩展，影响资源管理模块

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| Library-First | N/A | 现有系统扩展，非新建库 |
| Test-First | PASS | 将为新接口编写单元测试和集成测试 |
| Integration Testing | PASS | 新接口需要契约测试 |
| Simplicity | PASS | 不引入新表，仅接口层重构 |

**Gate Status**: ✅ PASS

## Project Structure

### Documentation (this feature)

```text
specs/001-resource-category-design/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── topology-api.md
│   └── resource-node-api.md
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
# DDD Multi-module Architecture (现有结构)

interface/interface-http/src/main/java/.../controller/
├── ResourceController.java      # 修改：资源节点接口
└── TopologyController.java      # 新增：拓扑图接口

application/application-api/src/main/java/.../dto/
├── resource/                    # 修改：资源节点DTO
└── topology/                    # 新增：拓扑图DTO

application/application-impl/src/main/java/.../service/
├── resource/                    # 修改：资源节点服务
└── topology/                    # 新增：拓扑图服务

domain/domain-api/src/main/java/.../service/
├── resource/                    # 修改：资源领域服务接口
└── topology/                    # 新增：拓扑图领域服务接口

domain/domain-impl/src/main/java/.../service/
├── resource/                    # 修改：资源领域服务实现
└── topology/                    # 新增：拓扑图领域服务实现
```

**Structure Decision**: 复用现有DDD多模块架构，在各层新增拓扑图相关类，修改资源相关类以排除SUBGRAPH类型。

## Complexity Tracking

> 无违规需要说明，设计符合简洁性原则。
