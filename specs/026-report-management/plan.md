# Implementation Plan: Report Management

**Branch**: `026-report-management` | **Date**: 2025-12-28 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/026-report-management/spec.md`

## Summary

实现报告管理模块的后端 API，包括报告（Report）和报告模板（ReportTemplate）的 CRUD 功能。报告创建后不可修改（immutable），模板支持完整的 CRUD 操作。遵循项目的 DDD 分层架构和 POST-Only API 设计模式。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, MockMvc
**Target Platform**: Linux server / Docker container
**Project Type**: DDD multi-module architecture
**Performance Goals**: Standard CRUD operations, no special performance requirements
**Constraints**: 遵循项目现有架构模式和 API 规范
**Scale/Scope**: 报告和模板的标准管理功能，P0-P2 优先级功能

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | PASS | 遵循 bootstrap/interface/application/domain/infrastructure 分层 |
| II. API URL Convention | PASS | `/api/service/v1/reports/*` 和 `/api/service/v1/report-templates/*` |
| III. POST-Only API Design | PASS | 所有接口使用 POST 方法，JSON Body 传参 |
| IV. Database Migration | PASS | 使用 Flyway 迁移脚本 V14__create_report_tables.sql |
| V. Technology Stack | PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x, MySQL 8.0 |
| VI. Pagination Protocol | PASS | 分页请求继承 PageableRequest，响应使用 PageResult<T> |

## Project Structure

### Documentation (this feature)

```text
specs/026-report-management/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── report-api.yaml
│   └── report-template-api.yaml
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
# DDD multi-module architecture (existing project structure)

domain/
├── domain-model/src/main/java/com/catface996/aiops/domain/model/
│   └── report/                          # NEW: Report domain models
│       ├── Report.java                  # Report 领域模型
│       ├── ReportType.java              # Report 类型枚举
│       ├── ReportStatus.java            # Report 状态枚举
│       ├── ReportTemplate.java          # ReportTemplate 领域模型
│       └── ReportTemplateCategory.java  # Template 分类枚举
├── repository-api/src/main/java/com/catface996/aiops/repository/
│   └── report/                          # NEW: Repository interfaces
│       ├── ReportRepository.java
│       └── ReportTemplateRepository.java

infrastructure/
└── persistence/mybatis-plus-impl/src/main/java/com/catface996/aiops/infrastructure/persistence/mybatisplus/
    └── report/                          # NEW: Repository implementations
        ├── entity/
        │   ├── ReportEntity.java
        │   └── ReportTemplateEntity.java
        ├── mapper/
        │   ├── ReportMapper.java
        │   └── ReportTemplateMapper.java
        ├── converter/
        │   ├── ReportConverter.java
        │   └── ReportTemplateConverter.java
        └── impl/
            ├── ReportRepositoryImpl.java
            └── ReportTemplateRepositoryImpl.java

application/
├── application-api/src/main/java/com/catface996/aiops/application/api/
│   ├── dto/report/                      # NEW: DTOs
│   │   ├── ReportDTO.java
│   │   ├── ReportTemplateDTO.java
│   │   └── request/
│   │       ├── ListReportsRequest.java
│   │       ├── GetReportRequest.java
│   │       ├── CreateReportRequest.java
│   │       ├── DeleteReportRequest.java
│   │       ├── ListReportTemplatesRequest.java
│   │       ├── GetReportTemplateRequest.java
│   │       ├── CreateReportTemplateRequest.java
│   │       ├── UpdateReportTemplateRequest.java
│   │       └── DeleteReportTemplateRequest.java
│   └── service/report/                  # NEW: Application service interface
│       ├── ReportApplicationService.java
│       └── ReportTemplateApplicationService.java
└── application-impl/src/main/java/com/catface996/aiops/application/impl/
    └── report/                          # NEW: Application service implementation
        ├── ReportApplicationServiceImpl.java
        └── ReportTemplateApplicationServiceImpl.java

interface/
└── interface-http/src/main/java/com/catface996/aiops/interface_/http/
    └── controller/                      # NEW: Controllers
        ├── ReportController.java
        └── ReportTemplateController.java

bootstrap/
└── src/main/resources/db/migration/
    └── V14__create_report_tables.sql    # NEW: Database migration
```

**Structure Decision**: 遵循项目现有的 DDD 多模块架构，在各层添加 report 子包存放报告管理相关代码。

## Complexity Tracking

> No violations - implementation follows existing patterns exactly.

N/A - 本功能实现完全遵循项目现有架构模式，无需额外复杂度。
