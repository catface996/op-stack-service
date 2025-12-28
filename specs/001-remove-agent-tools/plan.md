# Implementation Plan: 移除 Agent-Tools 绑定功能

**Branch**: `001-remove-agent-tools` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-agent-tools/spec.md`

## Summary

移除当前服务中所有与 Agent-Tools 绑定相关的功能代码和数据库表，因为该功能已在另一个服务中实现。主要工作包括：删除 AgentToolRelation 相关的领域模型、持久化层、仓储层代码，以及从 Agent 相关的 DTO 和服务层移除 toolIds 字段。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: Maven test (JUnit)
**Target Platform**: Linux server
**Project Type**: DDD multi-module architecture
**Performance Goals**: N/A (功能移除，无性能目标)
**Constraints**: N/A
**Scale/Scope**: 删除 6 个文件，修改 5 个文件，创建 1 个迁移脚本

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | ✅ PASS | 移除操作遵循 DDD 分层架构，按层级删除代码 |
| II. API URL Convention | ✅ PASS | Agent 接口保持 `/api/service/v1/agents/*` 格式 |
| III. POST-Only API Design | ✅ PASS | Agent 接口继续使用 POST 方法 |
| IV. Database Migration | ✅ PASS | 通过 Flyway 迁移脚本删除表 |
| V. Technology Stack | ✅ PASS | 无技术栈变更 |
| VI. Pagination Protocol | ✅ PASS | 分页接口不受影响 |

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-agent-tools/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── quickstart.md        # Phase 1 output (测试验证指南)
└── tasks.md             # Phase 2 output (由 /speckit.tasks 生成)
```

### Source Code (repository root)

本项目采用 DDD 多模块架构：

```text
domain/
├── domain-model/src/main/java/.../agent/
│   ├── Agent.java                    # 移除 toolIds 字段
│   └── AgentToolRelation.java        # 删除整个文件
└── repository-api/src/main/java/.../agent/
    └── AgentToolRelationRepository.java  # 删除整个文件

infrastructure/repository/mysql-impl/src/main/java/.../
├── impl/agent/
│   └── AgentToolRelationRepositoryImpl.java  # 删除整个文件
├── mapper/agent/
│   └── AgentToolRelationMapper.java          # 删除整个文件
└── po/agent/
    └── AgentToolRelationPO.java              # 删除整个文件

application/
├── application-api/src/main/java/.../dto/agent/
│   ├── AgentDTO.java                    # 移除 toolIds 字段
│   └── request/
│       ├── CreateAgentRequest.java      # 移除 toolIds 字段
│       └── UpdateAgentRequest.java      # 移除 toolIds 字段
└── application-impl/src/main/java/.../service/agent/
    └── AgentApplicationServiceImpl.java # 移除 Tools 绑定逻辑

interface/interface-http/src/main/java/.../controller/
    └── AgentController.java             # 移除 Tools 文档说明

bootstrap/src/main/resources/db/migration/
    └── V20__Drop_agent_tool_relation_table.sql  # 新增删除表脚本
```

**Structure Decision**: 遵循现有 DDD 多模块架构，按层级删除/修改相关代码文件。

## Complexity Tracking

> 无 Constitution 违规，无需记录复杂度变更。
