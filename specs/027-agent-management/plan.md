# Implementation Plan: Agent Management API

**Branch**: `027-agent-management` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/027-agent-management/spec.md`

## Summary

实现 Agent 管理 API，提供 AI Agent 的完整生命周期管理能力，包括：
- Agent CRUD（创建、查询、更新、删除）
- Agent 配置管理（AI 模型、温度、系统指令）
- 团队分配与解除分配
- 预定义配置模板
- Agent 统计信息查询

技术方案采用 DDD 分层架构，遵循 POST-Only API 设计规范，使用 MyBatis-Plus 进行数据持久化。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0, Flyway migrations
**Testing**: JUnit 5, MockMvc (no tests requested for this feature)
**Target Platform**: Linux server (Docker)
**Project Type**: DDD multi-module architecture
**Performance Goals**: 2 秒内完成列表查询（默认分页）
**Constraints**: POST-Only API, 分页大小限制 1-100
**Scale/Scope**: 支持数百个 Agent 管理

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | PASS | 遵循 bootstrap/interface/application/domain/infrastructure 分层 |
| II. API URL Convention | PASS | 使用 `/api/service/v1/agents/*` 路径格式 |
| III. POST-Only API Design | PASS | 所有接口使用 POST 方法，返回 `Result<T>` |
| IV. Database Migration | PASS | 使用 Flyway 迁移脚本 `V15__create_agent_tables.sql` |
| V. Technology Stack | PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| VI. Pagination Protocol | PASS | 继承 `PageableRequest`，返回 `PageResult<T>` |

**Gate Status**: PASS - 可以继续执行

## Project Structure

### Documentation (this feature)

```text
specs/027-agent-management/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── agent-api.yaml   # OpenAPI specification
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD Multi-Module Architecture

bootstrap/
└── src/main/
    ├── java/.../bootstrap/
    │   └── config/
    │       └── OpenApiConfig.java          # 更新 API 文档描述
    └── resources/
        └── db/migration/
            └── V15__create_agent_tables.sql  # Agent 表和 agent_2_team 关联表迁移

domain/
├── domain-model/src/main/java/.../domain/model/agent/
│   ├── Agent.java                          # Agent 领域模型
│   ├── AgentTeamRelation.java              # Agent-Team 关联领域模型
│   ├── AgentConfig.java                    # Agent 配置值对象
│   ├── AgentFindings.java                  # Agent 发现统计值对象
│   ├── AgentRole.java                      # Agent 角色枚举
│   └── AgentStatus.java                    # Agent 状态枚举
└── repository-api/src/main/java/.../repository/agent/
    ├── AgentRepository.java                # Agent 仓储接口
    └── AgentTeamRelationRepository.java    # Agent-Team 关联仓储接口

infrastructure/
└── repository/mysql-impl/src/main/java/.../repository/mysql/
    ├── po/agent/
    │   ├── AgentPO.java                    # Agent 持久化对象
    │   └── AgentTeamRelationPO.java        # Agent-Team 关联持久化对象
    ├── mapper/agent/
    │   ├── AgentMapper.java                # Agent MyBatis Mapper
    │   └── AgentTeamRelationMapper.java    # Agent-Team 关联 Mapper
    └── impl/agent/
        ├── AgentRepositoryImpl.java        # Agent 仓储实现
        └── AgentTeamRelationRepositoryImpl.java  # 关联仓储实现

application/
├── application-api/src/main/java/.../application/api/
│   ├── dto/agent/
│   │   ├── AgentDTO.java                   # Agent 数据传输对象
│   │   ├── AgentTemplateDTO.java           # 模板 DTO
│   │   └── request/
│   │       ├── ListAgentsRequest.java
│   │       ├── GetAgentRequest.java
│   │       ├── CreateAgentRequest.java
│   │       ├── UpdateAgentRequest.java
│   │       ├── UpdateAgentConfigRequest.java
│   │       ├── DeleteAgentRequest.java
│   │       ├── AssignAgentRequest.java
│   │       ├── UnassignAgentRequest.java
│   │       └── AgentStatsRequest.java
│   └── service/agent/
│       └── AgentApplicationService.java    # 应用服务接口
└── application-impl/src/main/java/.../application/impl/service/agent/
    └── AgentApplicationServiceImpl.java    # 应用服务实现

interface/
└── interface-http/src/main/java/.../interface_/http/controller/
    └── AgentController.java                # HTTP 控制器

common/
└── src/main/java/.../common/enums/
    └── AgentErrorCode.java                 # Agent 错误码
```

**Structure Decision**: 遵循现有项目 DDD 多模块架构，新增 agent 子包存放 Agent 相关代码。

## Complexity Tracking

无违规项，无需记录。
