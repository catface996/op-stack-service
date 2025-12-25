# Implementation Plan: 移除认证功能

**Branch**: `001-remove-auth-features` | **Date**: 2025-12-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-auth-features/spec.md`

## Summary

移除当前项目中的注册、登录、鉴权等功能，因为这些功能已经在另外一个系统单独实现。本系统将只保留业务相关的特性，用户身份通过请求体中的 userId 字段传递。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Framework**: Spring Boot 3.4.1, Spring Cloud 2025.0.0
**Primary Dependencies**: MyBatis-Plus 3.5.7, Spring Security (待移除), JWT jjwt 0.12.6 (待移除)
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, JaCoCo 0.8.12
**Target Platform**: Linux server / Docker container
**Project Type**: Multi-module DDD architecture
**Performance Goals**: N/A (移除功能，无新增性能需求)
**Constraints**: 确保业务功能不受影响
**Scale/Scope**: 移除约 30+ 个文件，2 个数据库表

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

宪法文件为模板状态（未配置），无特定约束需要检查。移除功能符合简化系统的原则。

**Pre-Phase 0 Check**: PASS
**Post-Phase 1 Check**: PASS

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-auth-features/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output - research findings
├── data-model.md        # Phase 1 output - entities to remove
├── quickstart.md        # Phase 1 output - verification guide
├── contracts/           # Phase 1 output - APIs to remove
│   └── removed-apis.md
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
# Multi-module DDD architecture (existing)
bootstrap/                    # 应用启动模块
├── src/main/java/.../
│   ├── config/
│   │   └── SecurityConfig.java         # 待移除
│   └── security/
│       ├── JwtAuthenticationFilter.java    # 待移除
│       ├── JwtAuthenticationEntryPoint.java # 待移除
│       └── JwtAccessDeniedHandler.java      # 待移除
└── src/main/resources/
    └── db/migration/
        └── V10__Drop_auth_tables.sql   # 新增

interface/interface-http/     # HTTP 接口层
└── src/main/java/.../controller/
    └── AuthController.java             # 待移除

application/                  # 应用层
├── application-api/
│   └── src/main/java/.../service/auth/
│       └── AuthApplicationService.java # 待移除
└── application-impl/
    └── src/main/java/.../
        ├── service/auth/
        │   └── AuthApplicationServiceImpl.java # 待移除
        └── aspect/
            └── ResourcePermissionAspect.java   # 待评估

domain/                       # 领域层
├── domain-api/
│   └── src/main/java/.../service/auth/
│       └── AuthDomainService.java      # 待移除
├── domain-impl/
│   └── src/main/java/.../service/auth/
│       └── AuthDomainServiceImpl.java  # 待移除
└── repository-api/
    └── src/main/java/.../repository/auth/
        ├── AccountRepository.java      # 待移除
        └── SessionRepository.java      # 待移除

infrastructure/repository/mysql-impl/  # 基础设施层
└── src/main/java/.../
    ├── po/auth/
    │   ├── AccountPO.java              # 待移除
    │   └── SessionPO.java              # 待移除
    ├── mapper/auth/
    │   ├── AccountMapper.java          # 待移除
    │   └── SessionMapper.java          # 待移除
    └── impl/auth/
        ├── AccountRepositoryImpl.java  # 待移除
        └── SessionRepositoryImpl.java  # 待移除
```

**Structure Decision**: 保持现有 DDD 多模块结构不变，仅移除认证相关的组件。

## Complexity Tracking

> 本功能为简化变更（移除代码），无违规需要说明。

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Phase Summary

### Phase 0: Research (已完成)
- 确认了技术栈详情
- 识别了所有需要移除的组件（30+ 文件）
- 确定了数据库迁移策略
- 确定了移除顺序

### Phase 1: Design (已完成)
- 创建了 data-model.md（待删除的实体）
- 创建了 contracts/removed-apis.md（待删除的 API）
- 创建了 quickstart.md（验证指南）

### Phase 2: Tasks (下一步)
运行 `/speckit.tasks` 生成详细的任务清单。

## Generated Artifacts

| 文件 | 描述 |
|------|------|
| `research.md` | 技术研究和组件清单 |
| `data-model.md` | 待删除的数据实体文档 |
| `contracts/removed-apis.md` | 待删除的 API 端点文档 |
| `quickstart.md` | 验证和故障排除指南 |
