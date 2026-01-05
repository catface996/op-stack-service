# Implementation Plan: 诊断任务持久化

**Branch**: `044-diagnosis-task` | **Date**: 2026-01-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/044-diagnosis-task/spec.md`

## Summary

实现诊断任务的创建、流式数据收集和按Agent维度持久化功能。用户触发诊断后，系统创建任务记录，调用executor获取流式响应，将响应按agent_bound_id暂存到Redis，诊断完成后整合内容并持久化到MySQL数据库。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, Spring Data Redis, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (业务数据) + Redis (流式数据暂存)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (Docker)
**Project Type**: DDD multi-module backend service
**Performance Goals**: 任务创建 <1秒，流式事件暂存延迟 <100ms，持久化 <5秒
**Constraints**: 单Agent诊断内容建议 <100KB，诊断超时10分钟
**Scale/Scope**: 单任务支持20+Agent，30天历史数据保留

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✓ PASS | 遵循 bootstrap/interface/application/domain/infrastructure 分层 |
| II. API URL Convention | ✓ PASS | 使用 `/api/service/v1/diagnosis-tasks/{action}` |
| III. POST-Only API | ✓ PASS | 所有接口使用 POST 方法 |
| IV. Database Migration | ✓ PASS | 使用 Flyway V40 迁移脚本 |
| V. Technology Stack | ✓ PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| VI. Pagination Protocol | ✓ PASS | 历史查询使用 PageableRequest/PageResult |
| VII. Database Design Standards | ✓ PASS | 表设计遵循命名规范和通用字段 |
| VIII. SQL Query Standards | ✓ PASS | 不使用 SELECT *，明确列出字段 |
| IX. Process Management Standards | ✓ PASS | 使用端口号终止进程 |

## Project Structure

### Documentation (this feature)

```text
specs/044-diagnosis-task/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Technical research
├── data-model.md        # Entity definitions
├── quickstart.md        # Usage examples
├── contracts/           # API contracts
│   └── diagnosis-api.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Task breakdown (by /speckit.tasks)
```

### Source Code (repository root)

```text
# DDD multi-module structure

bootstrap/
└── src/main/resources/db/migration/
    └── V40__create_diagnosis_task_tables.sql

interface/interface-http/
└── src/main/java/.../interface/http/controller/
    └── DiagnosisTaskController.java

application/
├── application-api/
│   └── src/main/java/.../application/api/
│       ├── dto/diagnosis/
│       │   ├── DiagnosisTaskDTO.java
│       │   ├── AgentDiagnosisProcessDTO.java
│       │   ├── request/
│       │   │   ├── CreateDiagnosisTaskRequest.java
│       │   │   ├── QueryDiagnosisTaskByIdRequest.java
│       │   │   └── QueryDiagnosisTaskByTopologyRequest.java
│       │   └── DiagnosisTaskStatus.java
│       └── service/
│           └── DiagnosisApplicationService.java
└── application-impl/
    └── src/main/java/.../application/impl/service/
        └── diagnosis/
            ├── DiagnosisApplicationServiceImpl.java
            └── DiagnosisStreamCollector.java

domain/domain-model/
└── src/main/java/.../domain/model/
    └── diagnosis/
        ├── DiagnosisTask.java
        └── AgentDiagnosisProcess.java

infrastructure/
├── repository/mysql-impl/
│   └── src/main/java/.../repository/mysql/
│       ├── po/diagnosis/
│       │   ├── DiagnosisTaskPO.java
│       │   └── AgentDiagnosisProcessPO.java
│       ├── mapper/diagnosis/
│       │   ├── DiagnosisTaskMapper.java
│       │   └── AgentDiagnosisProcessMapper.java
│       └── impl/diagnosis/
│           ├── DiagnosisTaskRepositoryImpl.java
│           └── AgentDiagnosisProcessRepositoryImpl.java
└── cache/redis-impl/
    └── src/main/java/.../infrastructure/cache/redis/
        └── diagnosis/
            └── DiagnosisStreamCacheService.java
```

**Structure Decision**: 遵循项目现有DDD分层结构，新增 diagnosis 模块跨各层实现。

## Complexity Tracking

> **No violations - all designs align with constitution principles**

| Aspect | Approach | Rationale |
|--------|----------|-----------|
| 流式数据暂存 | Redis List | 简单、TTL支持好、符合场景需求 |
| 持久化触发 | SSE结束事件 + 超时兜底 | 实时性好、可靠性有保障 |
| Agent标识 | agent_bound_id | 与executor事件一致，便于追溯 |

## Key Design Decisions

1. **Redis Key 命名**: `diagnosis:task:{taskId}:agent:{agentBoundId}`
2. **Redis TTL**: 24小时，持久化完成后主动删除
3. **状态枚举**: RUNNING, COMPLETED, FAILED, TIMEOUT
4. **内容存储**: LONGTEXT 纯文本，无结构化需求
5. **冗余存储**: agent_name 冗余存储避免JOIN查询

## Generated Artifacts

- [research.md](./research.md) - 技术研究决策
- [data-model.md](./data-model.md) - 数据模型定义
- [contracts/diagnosis-api.md](./contracts/diagnosis-api.md) - API契约
- [quickstart.md](./quickstart.md) - 使用指南

## Next Steps

运行 `/speckit.tasks` 生成详细的任务分解。
