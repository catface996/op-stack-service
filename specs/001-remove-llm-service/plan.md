# Implementation Plan: 移除LLM服务管理功能

**Branch**: `001-remove-llm-service` | **Date**: 2025-12-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-llm-service/spec.md`

## Summary

移除系统中不再使用的 LLM 服务管理模块，包括 7 个 HTTP 接口、19 个代码文件（跨越所有 DDD 分层）以及 1 个数据库表。目标是简化系统架构、降低维护成本、减少安全攻击面。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Maven Surefire
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD Multi-Module (common, infrastructure, domain, application, interface, bootstrap)
**Performance Goals**: N/A (removal operation)
**Constraints**: 不破坏现有功能，编译和测试必须通过
**Scale/Scope**: 移除 19 个 Java 文件 + 1 个数据库表

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

由于项目 constitution.md 为模板状态（未配置具体规则），本功能默认通过以下通用检查：

| Gate | Status | Notes |
|------|--------|-------|
| 代码完整性 | PASS | 移除操作不引入新代码，仅删除现有代码 |
| 测试覆盖 | PASS | 移除操作后现有测试应继续通过 |
| 向后兼容 | N/A | 规范明确声明不保留向后兼容性 |
| 安全性 | PASS | 移除代码减少攻击面 |

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-llm-service/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (N/A for removal)
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (to be removed)

```text
# Interface 层 (5 files)
interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/
├── controller/LlmServiceController.java
└── request/llm/
    ├── DeleteLlmServiceRequest.java
    ├── GetLlmServiceRequest.java
    ├── QueryLlmServicesRequest.java
    └── SetDefaultLlmServiceRequest.java

# Application 层 (5 files)
application/application-api/src/main/java/com/catface996/aiops/application/api/
├── dto/llm/
│   ├── CreateLlmServiceCommand.java
│   ├── UpdateLlmServiceCommand.java
│   └── LlmServiceDTO.java
└── service/llm/LlmServiceApplicationService.java

application/application-impl/src/main/java/com/catface996/aiops/application/impl/
└── service/llm/LlmServiceApplicationServiceImpl.java

# Domain 层 (3 files)
domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/
└── LlmServiceDomainService.java

domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/
└── LlmServiceDomainServiceImpl.java

domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/
└── LlmService.java

# Repository 层 (5 files)
domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/
├── LlmServiceRepository.java
└── entity/LlmServiceEntity.java

infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/
├── impl/llm/LlmServiceRepositoryImpl.java
├── mapper/llm/LlmServiceMapper.java
└── po/llm/LlmServicePO.java

# Common 层 (1 file)
common/src/main/java/com/catface996/aiops/common/enums/
└── LlmServiceErrorCode.java

# Database (1 migration file to add)
bootstrap/src/main/resources/db/migration/
└── V9__Drop_llm_service_table.sql  # NEW
```

**Structure Decision**: 遵循现有 DDD 多模块架构，按分层顺序删除代码：Interface → Application → Domain → Repository → Common → Database Migration

## Complexity Tracking

> **No violations - removal operation**

本功能为纯删除操作，不引入新的架构复杂度。

## Implementation Strategy

### Phase 1: 依赖检查
1. 搜索代码库确认无其他模块引用 LLM 服务相关类
2. 检查 Spring 配置文件中的相关配置

### Phase 2: 代码删除（按依赖顺序）
1. 删除 Interface 层代码（控制器和请求对象）
2. 删除 Application 层代码（服务接口、实现、DTO）
3. 删除 Domain 层代码（领域服务、领域模型）
4. 删除 Repository 层代码（仓储接口、实现、Mapper、PO）
5. 删除 Common 层代码（错误码枚举）

### Phase 3: 数据库清理
1. 创建 Flyway 迁移脚本 V9__Drop_llm_service_table.sql
2. 删除 llm_service_config 表

### Phase 4: 验证
1. 执行 `mvn clean compile` 确认编译通过
2. 执行 `mvn test` 确认测试通过
3. 启动应用确认正常运行
4. 验证 Swagger 文档不显示 LLM 服务接口

## Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| 遗漏依赖导致编译失败 | Low | Medium | 删除前执行全量搜索，删除后立即编译 |
| 生产数据丢失 | Low | Low | 规范已确认数据可删除，迁移脚本保留历史 |
| 回滚困难 | Low | Low | Git 版本控制可回滚代码，数据库备份可恢复表 |
