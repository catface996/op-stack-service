# Implementation Plan: 提示词模板管理

**Branch**: `025-prompt-template` | **Date**: 2025-12-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/025-prompt-template/spec.md`

## Summary

实现提示词模板管理功能，支持 CRUD 操作、用户自定义用途类型、以及完整的版本控制。每次模板更新自动生成新版本，支持回滚到历史版本（通过创建新版本实现）。采用 DDD 分层架构，遵循现有项目的 POST-Only API 设计模式。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Mockito
**Target Platform**: Linux server (Spring Boot embedded Tomcat)
**Project Type**: DDD multi-module Maven project
**Performance Goals**: 列表查询 < 1s (1000 条记录), 创建/更新 < 5s
**Constraints**: 模板内容最大 64KB, 乐观锁并发控制
**Scale/Scope**: 单模板支持 1000+ 历史版本

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Constitution 文件为模板状态，无具体约束。遵循项目现有的 DDD 架构模式和 POST-Only API 设计。

**Pre-Phase 0 Check**: PASS (无违规)

## Project Structure

### Documentation (this feature)

```text
specs/025-prompt-template/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD Multi-Module Structure (existing)

domain/
├── domain-model/src/main/java/com/catface996/aiops/domain/model/
│   ├── prompt/                    # 新增：提示词模板领域模型
│   │   ├── PromptTemplate.java
│   │   ├── PromptTemplateVersion.java
│   │   └── TemplateUsage.java
│   └── ...
├── domain-api/src/main/java/com/catface996/aiops/domain/service/
│   └── prompt/                    # 新增：提示词模板领域服务接口
│       └── PromptTemplateDomainService.java
├── domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/
│   └── prompt/                    # 新增：提示词模板领域服务实现
│       └── PromptTemplateDomainServiceImpl.java
└── repository-api/src/main/java/com/catface996/aiops/repository/
    └── prompt/                    # 新增：提示词模板仓储接口
        ├── PromptTemplateRepository.java
        ├── PromptTemplateVersionRepository.java
        └── TemplateUsageRepository.java

infrastructure/repository/mysql-impl/src/main/
├── java/com/catface996/aiops/repository/mysql/
│   ├── mapper/prompt/             # 新增：MyBatis Mapper
│   │   ├── PromptTemplateMapper.java
│   │   ├── PromptTemplateVersionMapper.java
│   │   └── TemplateUsageMapper.java
│   ├── po/prompt/                 # 新增：持久化对象
│   │   ├── PromptTemplatePO.java
│   │   ├── PromptTemplateVersionPO.java
│   │   └── TemplateUsagePO.java
│   └── impl/prompt/               # 新增：仓储实现
│       ├── PromptTemplateRepositoryImpl.java
│       ├── PromptTemplateVersionRepositoryImpl.java
│       └── TemplateUsageRepositoryImpl.java
└── resources/mapper/prompt/       # 新增：MyBatis XML
    ├── PromptTemplateMapper.xml
    ├── PromptTemplateVersionMapper.xml
    └── TemplateUsageMapper.xml

application/
├── application-api/src/main/java/com/catface996/aiops/application/api/
│   ├── dto/prompt/                # 新增：应用层 DTO
│   │   ├── PromptTemplateDTO.java
│   │   ├── PromptTemplateVersionDTO.java
│   │   ├── TemplateUsageDTO.java
│   │   └── request/
│   │       ├── CreatePromptTemplateRequest.java
│   │       ├── UpdatePromptTemplateRequest.java
│   │       ├── RollbackTemplateRequest.java
│   │       ├── ListPromptTemplatesRequest.java
│   │       └── CreateTemplateUsageRequest.java
│   └── service/prompt/            # 新增：应用服务接口
│       ├── PromptTemplateApplicationService.java
│       └── TemplateUsageApplicationService.java
└── application-impl/src/main/java/com/catface996/aiops/application/impl/service/
    └── prompt/                    # 新增：应用服务实现
        ├── PromptTemplateApplicationServiceImpl.java
        └── TemplateUsageApplicationServiceImpl.java

interface/interface-http/src/main/java/com/catface996/aiops/interfaces/http/controller/
└── prompt/                        # 新增：HTTP 控制器
    ├── PromptTemplateController.java
    └── TemplateUsageController.java

bootstrap/src/main/resources/db/migration/
└── V025__create_prompt_template_tables.sql  # 新增：数据库迁移脚本
```

**Structure Decision**: 遵循现有 DDD 多模块架构，在各层对应位置新增 `prompt` 包。

## Complexity Tracking

无违规，无需记录复杂性说明。
