# Implementation Plan: Topology 绑定 Global Supervisor Agent

**Branch**: `035-topology-supervisor-agent` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/035-topology-supervisor-agent/spec.md`

## Summary

实现 Topology 与 Global Supervisor Agent 的一对一绑定关系管理功能。通过在 `topology` 表新增 `global_supervisor_agent_id` 字段存储绑定关系，提供绑定和解绑两个 API 端点。遵循项目现有的 DDD 分层架构和 POST-Only API 规范。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD layered architecture (bootstrap/interface/application/domain/infrastructure)
**Performance Goals**: < 200ms 响应时间
**Constraints**: 一对一关系，绑定时需校验 Agent 角色为 GLOBAL_SUPERVISOR
**Scale/Scope**: 简单字段更新，比 topology_2_report_template 更简单

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✅ PASS | 遵循 bootstrap/interface/application/domain/infrastructure 分层 |
| II. API URL Convention | ✅ PASS | 使用 `/api/service/v1/topologies/supervisor/{action}` 格式 |
| III. POST-Only API Design | ✅ PASS | 所有接口使用 POST 方法，JSON Body 传参 |
| IV. Database Migration | ✅ PASS | 使用 Flyway V27 迁移脚本添加字段 |
| V. Technology Stack | ✅ PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x, MySQL 8.0 |
| VI. Pagination Protocol | ⬜ N/A | 本功能不涉及分页查询 |
| VII. Database Design Standards | ✅ PASS | 字段命名遵循 `{关联表单数}_id` 格式 |

## Project Structure

### Documentation (this feature)

```text
specs/035-topology-supervisor-agent/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── checklists/          # Quality checklists
└── tasks.md             # Phase 2 output (by /speckit.tasks)
```

### Source Code (repository root)

```text
# DDD Layered Architecture

bootstrap/
└── src/main/resources/db/migration/
    └── V27__add_global_supervisor_agent_to_topology.sql   # 添加字段迁移脚本

interface/interface-http/
└── src/main/java/com/catface996/aiops/interface_/http/
    ├── controller/TopologyController.java                  # 扩展：添加 bind/unbind 端点
    └── request/topology/
        ├── BindSupervisorAgentRequest.java                 # 新建
        └── UnbindSupervisorAgentRequest.java               # 新建

application/application-api/
└── src/main/java/com/catface996/aiops/application/api/
    └── service/topology/TopologyApplicationService.java    # 扩展：添加方法

application/application-impl/
└── src/main/java/com/catface996/aiops/application/impl/
    └── service/topology/TopologyApplicationServiceImpl.java # 扩展：添加方法

domain/domain-api/
└── src/main/java/com/catface996/aiops/domain/
    └── service/topology/TopologyDomainService.java         # 扩展：添加方法

domain/domain-impl/
└── src/main/java/com/catface996/aiops/domain/impl/
    └── service/topology/TopologyDomainServiceImpl.java     # 扩展：添加方法

infrastructure/repository/mysql-impl/
└── src/main/java/com/catface996/aiops/repository/mysql/
    └── po/topology/TopologyPO.java                         # 扩展：添加字段

domain/repository-api/
└── src/main/java/com/catface996/aiops/repository/
    └── topology/TopologyRepository.java                    # 扩展：添加方法
```

**Structure Decision**: 由于是简单的一对一关系，只需要在现有类中添加字段和方法，不需要新建 Service 或 Repository 类。

## Complexity Tracking

> 无违规项需要说明，本功能完全遵循项目宪法规范。
