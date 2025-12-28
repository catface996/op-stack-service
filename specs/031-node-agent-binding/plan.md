# Implementation Plan: Node-Agent 绑定功能

**Branch**: `031-node-agent-binding` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/031-node-agent-binding/spec.md`

## Summary

实现 Agent 与 ResourceNode 的多对多关联功能。创建 `node_2_agent` 关联表，提供绑定、解绑、按节点查询关联 Agent、按 Agent 查询关联节点的 HTTP 接口。所有接口放在 NodeController 下，遵循 POST-Only API 设计规范。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: 手动测试 (未明确要求自动化测试)
**Target Platform**: Linux server / Docker container
**Project Type**: DDD multi-module architecture
**Performance Goals**: 响应时间 < 3 秒 (绑定/解绑), < 1 秒 (查询)
**Constraints**: 单节点支持 100+ Agent 关联, 单 Agent 支持 1000+ 节点关联
**Scale/Scope**: 企业级运维平台

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| DDD Architecture | ✅ PASS | 遵循 domain/application/interface/infrastructure 分层 |
| API URL Convention | ✅ PASS | 路径: `/api/service/v1/nodes/{action}` |
| POST-Only API | ✅ PASS | 所有接口使用 POST 方法 |
| Database Migration | ✅ PASS | 使用 Flyway V21 迁移脚本 |
| Technology Stack | ✅ PASS | Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| Pagination Protocol | ✅ PASS | 查询列表使用 PageResult<T> (如需分页) |

## Project Structure

### Documentation (this feature)

```text
specs/031-node-agent-binding/
├── plan.md              # This file (/speckit.plan command output)
├── spec.md              # Feature specification
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── bind-agent.md
│   ├── unbind-agent.md
│   ├── list-agents.md
│   └── list-nodes-by-agent.md
├── checklists/
│   └── requirements.md  # Quality checklist
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD Multi-module Architecture

domain/
├── domain-model/src/main/java/com/catface996/aiops/domain/model/node/
│   └── NodeAgentRelation.java                    # 新增：领域模型
└── repository-api/src/main/java/com/catface996/aiops/repository/node/
    └── NodeAgentRelationRepository.java          # 新增：仓储接口

infrastructure/
└── repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/
    ├── impl/node/
    │   └── NodeAgentRelationRepositoryImpl.java  # 新增：仓储实现
    ├── mapper/node/
    │   └── NodeAgentRelationMapper.java          # 新增：Mapper接口
    └── po/node/
        └── NodeAgentRelationPO.java              # 新增：持久化对象

application/
├── application-api/src/main/java/com/catface996/aiops/application/api/
│   ├── dto/node/
│   │   ├── request/
│   │   │   ├── BindAgentRequest.java             # 新增
│   │   │   ├── UnbindAgentRequest.java           # 新增
│   │   │   ├── ListAgentsByNodeRequest.java      # 新增
│   │   │   └── ListNodesByAgentRequest.java      # 新增
│   │   └── NodeAgentRelationDTO.java             # 新增
│   └── service/node/
│       └── NodeApplicationService.java           # 修改：添加绑定相关方法
└── application-impl/src/main/java/com/catface996/aiops/application/impl/service/node/
    └── NodeApplicationServiceImpl.java           # 修改：实现绑定相关方法

interface/
└── interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/
    └── NodeController.java                       # 修改：添加绑定相关接口

bootstrap/
└── src/main/resources/db/migration/
    └── V21__Create_node_agent_relation_table.sql # 新增：数据库迁移
```

**Structure Decision**: 采用 DDD 多模块架构，所有绑定功能集成到现有 Node 模块下，遵循项目现有的代码组织方式。

## Complexity Tracking

> 无复杂度违规，功能实现符合项目宪法规定。

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Design Decisions

### 1. 关联表设计

采用 `node_2_agent` 命名，遵循项目现有关联表命名规范（如 `agent_2_team`, `topology_2_node`）。

### 2. HTTP 接口位置

所有绑定接口放在 **NodeController** 下（非 AgentController），路径前缀 `/api/service/v1/nodes/...`：
- `POST /api/service/v1/nodes/bindAgent` - 绑定 Agent
- `POST /api/service/v1/nodes/unbindAgent` - 解绑 Agent
- `POST /api/service/v1/nodes/listAgents` - 查询节点关联的 Agent 列表
- `POST /api/service/v1/nodes/listNodesByAgent` - 查询 Agent 关联的节点列表

### 3. 软删除机制

关联表使用 `deleted` 字段实现软删除，与项目其他实体一致（如 Agent、Topology）。

### 4. 级联删除策略

当删除 Node 或 Agent 时，关联记录自动软删除。通过应用层事件监听或数据库触发器实现。

## Next Steps

1. ✅ Phase 0: Research (research.md) - COMPLETE
2. ✅ Phase 1: Design (data-model.md, contracts/, quickstart.md) - COMPLETE
3. ✅ Phase 2: Tasks (/speckit.tasks) - COMPLETE (27 tasks generated)
4. ⬜ Phase 3: Implementation (/speckit.implement)
