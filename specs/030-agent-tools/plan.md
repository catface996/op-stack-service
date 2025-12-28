# Implementation Plan: Agent Tools 绑定

**Branch**: `030-agent-tools` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/030-agent-tools/spec.md`

## Summary

实现 Agent 与 Tool 的多对多绑定关系管理。核心功能包括：
1. 在创建/更新 Agent 时指定 toolIds 列表
2. 全量替换模式 —— 每次更新 toolIds 完全替换原有绑定
3. 在 Agent 查询接口中返回已绑定的 toolIds
4. 边界处理：无效 ID 过滤、重复 ID 去重、空列表清空绑定

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, MockMvc
**Target Platform**: Linux server
**Project Type**: DDD 多模块架构（domain/application/interface/infrastructure）
**Performance Goals**: Agent Tools 绑定操作 < 3 秒完成
**Constraints**: Agent 处于 WORKING/THINKING 状态时禁止更新
**Scale/Scope**: 单个 Agent 可绑定数十个 Tools

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] 开发环境端口必须是 8081（已配置）
- [x] POST-Only API 设计模式
- [x] DDD 多模块架构
- [x] 全量替换绑定模式（非增量）

## Project Structure

### Documentation (this feature)

```text
specs/030-agent-tools/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── agent-tools-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# DDD 多模块架构

domain/
├── domain-model/src/main/java/com/catface996/aiops/domain/model/
│   └── agent/
│       ├── Agent.java                    # 领域模型（添加 toolIds 属性）
│       └── AgentToolRelation.java        # 新增：Agent-Tool 关联领域模型
├── repository-api/src/main/java/com/catface996/aiops/repository/
│   └── agent/
│       └── AgentToolRelationRepository.java  # 新增：关联仓储接口

infrastructure/
└── repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/
    ├── impl/agent/
    │   └── AgentToolRelationRepositoryImpl.java  # 新增：关联仓储实现
    ├── mapper/agent/
    │   └── AgentToolRelationMapper.java          # 新增：MyBatis Mapper
    └── po/agent/
        └── AgentToolRelationPO.java              # 新增：持久化对象

application/
├── application-api/src/main/java/com/catface996/aiops/application/api/
│   ├── dto/agent/
│   │   ├── AgentDTO.java                 # 修改：添加 toolIds 字段
│   │   └── request/
│   │       ├── CreateAgentRequest.java   # 修改：添加 toolIds 字段
│   │       └── UpdateAgentRequest.java   # 修改：添加 toolIds 字段
│   └── service/agent/
│       └── AgentApplicationService.java  # 接口不变
└── application-impl/src/main/java/com/catface996/aiops/application/impl/
    └── service/agent/
        └── AgentApplicationServiceImpl.java  # 修改：处理 Tools 绑定逻辑

interface/
└── interface-http/src/main/java/com/catface996/aiops/interface_/http/
    └── controller/
        └── AgentController.java          # 无需修改（复用现有 create/update 端点）

bootstrap/
└── src/main/resources/db/migration/
    └── V18__Create_agent_tool_relation_table.sql  # 新增：迁移脚本
```

**Structure Decision**: 采用现有 DDD 多模块架构，参考 `agent_2_team` 关联表模式，新建 `agent_2_tool` 关联表实现多对多关系。

## Complexity Tracking

> **无违规项** — 本特性遵循现有架构模式，复杂度适中。

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 模块数量 | ✅ | 复用现有模块，无新增 |
| 关联表设计 | ✅ | 参考 agent_2_team 模式 |
| API 变更 | ✅ | 复用现有端点，仅扩展字段 |
