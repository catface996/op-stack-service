# Implementation Plan: 清理数据库废弃字段

**Branch**: `041-cleanup-obsolete-fields` | **Date**: 2025-12-30 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/041-cleanup-obsolete-fields/spec.md`

## Summary

清理数据库中 4 个废弃字段/表：
1. `node.agent_team_id` - 预留字段，从未使用（0条数据）
2. `topology.coordinator_agent_id` - 预留字段，从未使用（0条数据）
3. `topology.global_supervisor_agent_id` - 已迁移到 agent_bound 表（3条数据）
4. `node_2_agent` 表 - 已迁移到 agent_bound 表（11条数据）

技术方案：分阶段清理，每阶段包含代码修改、数据库迁移脚本和验证测试。按风险从低到高执行（P1→P4）。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, API 功能测试
**Target Platform**: Linux server (Docker)
**Project Type**: DDD multi-module (bootstrap, interface, application, domain, infrastructure)
**Performance Goals**: 系统响应时间保持不变或改善
**Constraints**: 每个迁移步骤必须独立可回滚
**Scale/Scope**: 影响 ~28 个 Java 文件，4 个数据库迁移脚本

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | PASS | 清理代码遵循分层架构，从 interface → application → domain → infrastructure |
| II. API URL Convention | PASS | 无新增 API，现有 API 移除废弃字段 |
| III. POST-Only API Design | PASS | 无 API 设计变更 |
| IV. Database Migration | PASS | 使用 Flyway 迁移脚本管理所有数据库变更 |
| V. Technology Stack | PASS | 使用项目既定技术栈 |
| VI. Pagination Protocol | N/A | 无分页接口变更 |
| VII. Database Design Standards | PASS | 清理后的表结构符合规范 |
| VIII. SQL Query Standards | PASS | 更新 Mapper XML 移除废弃字段，遵循禁止 SELECT * 规范 |

**Constitution Gate**: ✅ PASSED

## Project Structure

### Documentation (this feature)

```text
specs/041-cleanup-obsolete-fields/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (entity changes)
├── quickstart.md        # Phase 1 output (verification guide)
├── contracts/           # Phase 1 output (N/A - no new contracts)
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
# DDD Multi-Module Structure (existing)
bootstrap/
├── src/main/resources/db/migration/   # Flyway migrations (V33-V36)

interface/interface-http/
└── src/main/java/.../controller/      # No changes needed

application/
├── application-api/src/main/java/.../dto/
│   ├── node/
│   │   ├── NodeDTO.java               # Remove agentTeamId
│   │   └── request/
│   │       ├── CreateNodeRequest.java # Remove agentTeamId
│   │       └── UpdateNodeRequest.java # Remove agentTeamId
│   └── topology/
│       ├── TopologyDTO.java           # Remove coordinatorAgentId, globalSupervisorAgentId
│       └── request/
│           ├── CreateTopologyRequest.java # Remove coordinatorAgentId
│           └── UpdateTopologyRequest.java # Remove coordinatorAgentId
└── application-impl/src/main/java/.../service/
    ├── node/NodeApplicationServiceImpl.java
    └── topology/TopologyApplicationServiceImpl.java

domain/
├── domain-api/src/main/java/.../service/
│   ├── node/NodeDomainService.java
│   └── topology2/TopologyDomainService.java
├── domain-model/src/main/java/.../model/
│   ├── node/Node.java                 # Remove agentTeamId
│   ├── node/NodeAgentRelation.java    # DELETE file
│   └── topology/Topology.java         # Remove coordinatorAgentId, globalSupervisorAgentId
├── domain-impl/src/main/java/.../service/
│   ├── node/NodeDomainServiceImpl.java
│   └── topology2/TopologyDomainServiceImpl.java
└── repository-api/src/main/java/.../repository/
    └── node/NodeAgentRelationRepository.java  # DELETE file

infrastructure/repository/mysql-impl/
├── src/main/java/.../
│   ├── po/node/
│   │   ├── NodePO.java                # Remove agentTeamId
│   │   └── NodeAgentRelationPO.java   # DELETE file
│   ├── po/topology/TopologyPO.java    # Remove coordinatorAgentId, globalSupervisorAgentId
│   ├── mapper/node/
│   │   ├── NodeMapper.java            # Update Base_Column_List
│   │   └── NodeAgentRelationMapper.java  # DELETE file
│   ├── mapper/topology/TopologyMapper.java  # Update Base_Column_List
│   └── impl/node/
│       └── NodeAgentRelationRepositoryImpl.java  # DELETE file
└── src/main/resources/mapper/
    ├── node/
    │   ├── NodeMapper.xml             # Update Base_Column_List
    │   └── NodeAgentRelationMapper.xml  # DELETE file
    └── topology/TopologyMapper.xml    # Update Base_Column_List
```

**Structure Decision**: 使用现有 DDD 多模块结构，按层级清理代码，从外到内（interface → application → domain → infrastructure）再执行数据库迁移。

## Complexity Tracking

无宪法违规需要说明。清理工作遵循所有既定规范。

## Implementation Phases

### Phase 1 (P1): 移除 node.agent_team_id

**风险等级**: 低（0条数据）

1. 代码清理顺序：
   - DTO: CreateNodeRequest, UpdateNodeRequest, NodeDTO
   - Application: NodeApplicationServiceImpl
   - Domain API: NodeDomainService
   - Domain Model: Node.java
   - Domain Impl: NodeDomainServiceImpl
   - Infrastructure: NodePO, NodeMapper, NodeMapper.xml

2. 数据库迁移：V33__drop_node_agent_team_id.sql

### Phase 2 (P2): 移除 topology.coordinator_agent_id

**风险等级**: 低（0条数据）

1. 代码清理顺序：同 P1 模式
2. 数据库迁移：V34__drop_topology_coordinator_agent_id.sql

### Phase 3 (P3): 移除 topology.global_supervisor_agent_id

**风险等级**: 中（3条数据，已迁移）

1. 验证 agent_bound 表数据完整性
2. 代码清理：移除 TopologyDTO, Topology 中的字段引用
3. 数据库迁移：V35__drop_topology_global_supervisor_agent_id.sql

### Phase 4 (P4): 删除 node_2_agent 表

**风险等级**: 中（11条数据，已迁移）

1. 验证 agent_bound 表数据完整性
2. 切换代码使用 agent_bound（如有遗漏）
3. 删除相关代码文件：
   - NodeAgentRelation.java
   - NodeAgentRelationRepository.java
   - NodeAgentRelationRepositoryImpl.java
   - NodeAgentRelationPO.java
   - NodeAgentRelationMapper.java
   - NodeAgentRelationMapper.xml
4. 数据库迁移：V36__drop_node_2_agent_table.sql
