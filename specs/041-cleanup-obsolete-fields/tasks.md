# Tasks: 清理数据库废弃字段

**Input**: Design documents from `/specs/041-cleanup-obsolete-fields/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md

**Tests**: 无单独测试任务，验证通过 API 功能测试和编译检查完成。

**Organization**: Tasks are grouped by user story (P1→P4) to enable independent implementation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions (DDD Multi-Module)

- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **domain-api**: `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/`
- **domain-model**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/`
- **domain-impl**: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/`
- **repository-api**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/`
- **mysql-impl**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`
- **mapper-xml**: `infrastructure/repository/mysql-impl/src/main/resources/mapper/`
- **migration**: `bootstrap/src/main/resources/db/migration/`

---

## Phase 1: Setup (Verification)

**Purpose**: 验证清理前的环境状态

- [X] T001 验证项目当前编译状态（执行 `mvn clean compile -DskipTests`）
- [X] T002 验证 Docker MySQL 容器运行状态
- [X] T003 记录当前数据库表结构（node, topology, node_2_agent, agent_bound）

---

## Phase 2: User Story 1 - 移除 node.agent_team_id (Priority: P1) 🎯 MVP

**Goal**: 移除 node 表中从未使用的 `agent_team_id` 字段

**Independent Test**: Node 相关 API（创建、查询、更新）功能正常

### Implementation for User Story 1

**代码清理（由外到内）：**

- [X] T004 [P] [US1] 移除 CreateNodeRequest.java 中的 agentTeamId 字段 in `application/application-api/.../dto/node/request/CreateNodeRequest.java`
- [X] T005 [P] [US1] 移除 UpdateNodeRequest.java 中的 agentTeamId 字段 in `application/application-api/.../dto/node/request/UpdateNodeRequest.java`
- [X] T006 [P] [US1] 移除 NodeDTO.java 中的 agentTeamId 字段 in `application/application-api/.../dto/node/NodeDTO.java`
- [X] T007 [US1] 更新 NodeApplicationServiceImpl.java 移除 agentTeamId 映射 in `application/application-impl/.../service/node/NodeApplicationServiceImpl.java`
- [X] T008 [US1] 更新 NodeDomainService.java 移除 agentTeamId 方法参数 in `domain/domain-api/.../service/node/NodeDomainService.java`
- [X] T009 [US1] 更新 Node.java 移除 agentTeamId 字段和方法 in `domain/domain-model/.../model/node/Node.java`
- [X] T010 [US1] 更新 NodeDomainServiceImpl.java 移除 agentTeamId 使用 in `domain/domain-impl/.../service/node/NodeDomainServiceImpl.java`
- [X] T011 [US1] 更新 NodePO.java 移除 agentTeamId 字段 in `infrastructure/repository/mysql-impl/.../po/node/NodePO.java`
- [X] T012 [US1] 更新 NodeMapper.xml 移除 Base_Column_List 中的 agent_team_id in `infrastructure/repository/mysql-impl/.../resources/mapper/node/NodeMapper.xml`
- [X] T013 [US1] 验证编译通过（执行 `mvn clean compile -DskipTests`）

**数据库迁移：**

- [X] T014 [US1] 创建 V33__drop_node_agent_team_id.sql in `bootstrap/src/main/resources/db/migration/`
- [X] T015 [US1] 执行迁移并验证字段已移除

**Checkpoint**: Node API 功能正常，agentTeamId 字段已完全移除

---

## Phase 3: User Story 2 - 移除 topology.coordinator_agent_id (Priority: P2)

**Goal**: 移除 topology 表中从未使用的 `coordinator_agent_id` 字段

**Independent Test**: Topology 相关 API（创建、查询、更新）功能正常

### Implementation for User Story 2

**代码清理（由外到内）：**

- [X] T016 [P] [US2] 移除 CreateTopologyRequest.java 中的 coordinatorAgentId 字段 in `application/application-api/.../dto/topology/request/CreateTopologyRequest.java`
- [X] T017 [P] [US2] 移除 UpdateTopologyRequest.java 中的 coordinatorAgentId 字段 in `application/application-api/.../dto/topology/request/UpdateTopologyRequest.java`
- [X] T018 [P] [US2] 移除 TopologyDTO.java 中的 coordinatorAgentId 字段 in `application/application-api/.../dto/topology/TopologyDTO.java`
- [X] T019 [US2] 更新 TopologyApplicationServiceImpl.java 移除 coordinatorAgentId 映射 in `application/application-impl/.../service/topology/TopologyApplicationServiceImpl.java`
- [X] T020 [US2] 更新 TopologyDomainService.java 移除 coordinatorAgentId 方法参数 in `domain/domain-api/.../service/topology2/TopologyDomainService.java`
- [X] T021 [US2] 更新 Topology.java 移除 coordinatorAgentId 字段和方法 in `domain/domain-model/.../model/topology/Topology.java`
- [X] T022 [US2] 更新 TopologyDomainServiceImpl.java 移除 coordinatorAgentId 使用 in `domain/domain-impl/.../service/topology2/TopologyDomainServiceImpl.java`
- [X] T023 [US2] 更新 TopologyPO.java 移除 coordinatorAgentId 字段 in `infrastructure/repository/mysql-impl/.../po/topology/TopologyPO.java`
- [X] T024 [US2] 更新 TopologyMapper.xml 移除 Base_Column_List 中的 coordinator_agent_id in `infrastructure/repository/mysql-impl/.../resources/mapper/topology/TopologyMapper.xml`
- [X] T025 [US2] 验证编译通过（执行 `mvn clean compile -DskipTests`）

**数据库迁移：**

- [X] T026 [US2] 创建 V34__drop_topology_coordinator_agent_id.sql in `bootstrap/src/main/resources/db/migration/`
- [X] T027 [US2] 执行迁移并验证字段已移除

**Checkpoint**: Topology API 功能正常，coordinatorAgentId 字段已完全移除

---

## Phase 4: User Story 3 - 移除 topology.global_supervisor_agent_id (Priority: P3)

**Goal**: 移除 topology 表中已迁移到 agent_bound 的 `global_supervisor_agent_id` 字段

**Independent Test**: Global Supervisor 绑定功能通过 agent_bound 表正常工作

### Implementation for User Story 3

**数据完整性验证：**

- [X] T028 [US3] 验证 agent_bound 表包含所有 global_supervisor_agent_id 数据（运行验证 SQL）

**代码清理：**

- [X] T029 [P] [US3] 移除 TopologyDTO.java 中的 globalSupervisorAgentId 字段 in `application/application-api/.../dto/topology/TopologyDTO.java`
- [X] T030 [US3] 更新 TopologyApplicationServiceImpl.java 移除 globalSupervisorAgentId 映射 in `application/application-impl/.../service/topology/TopologyApplicationServiceImpl.java`
- [X] T031 [US3] 更新 Topology.java 移除 globalSupervisorAgentId 字段和方法 in `domain/domain-model/.../model/topology/Topology.java`
- [X] T032 [US3] 更新 TopologyPO.java 移除 globalSupervisorAgentId 字段 in `infrastructure/repository/mysql-impl/.../po/topology/TopologyPO.java`
- [X] T033 [US3] 更新 TopologyMapper.xml 移除 Base_Column_List 中的 global_supervisor_agent_id in `infrastructure/repository/mysql-impl/.../resources/mapper/topology/TopologyMapper.xml`
- [X] T034 [US3] 验证编译通过（执行 `mvn clean compile -DskipTests`）

**数据库迁移：**

- [X] T035 [US3] 创建 V35__drop_topology_global_supervisor_agent_id.sql in `bootstrap/src/main/resources/db/migration/`
- [X] T036 [US3] 执行迁移并验证字段已移除

**Checkpoint**: Global Supervisor 绑定通过 agent_bound 表正常工作，globalSupervisorAgentId 已移除

---

## Phase 5: User Story 4 - 删除 node_2_agent 表 (Priority: P4)

**Goal**: 删除已迁移到 agent_bound 表的 `node_2_agent` 表及相关代码

**Independent Test**: Node-Agent 绑定功能完全通过 agent_bound 表正常工作

### Implementation for User Story 4

**数据完整性验证：**

- [X] T037 [US4] 验证 agent_bound 表包含所有 node_2_agent 数据（运行验证 SQL）

**代码切换（确保使用 agent_bound）：**

- [X] T038 [US4] 检查并更新 TopologyApplicationServiceImpl.java 使用 AgentBoundRepository 替代 NodeAgentRelationRepository in `application/application-impl/.../service/topology/TopologyApplicationServiceImpl.java`

**删除相关代码：**

- [X] T039 [P] [US4] 删除 NodeAgentRelation.java in `domain/domain-model/.../model/node/NodeAgentRelation.java`
- [X] T040 [P] [US4] 删除 NodeAgentRelationRepository.java in `domain/repository-api/.../repository/node/NodeAgentRelationRepository.java`
- [X] T041 [P] [US4] 删除 NodeAgentRelationRepositoryImpl.java in `infrastructure/repository/mysql-impl/.../impl/node/NodeAgentRelationRepositoryImpl.java`
- [X] T042 [P] [US4] 删除 NodeAgentRelationPO.java in `infrastructure/repository/mysql-impl/.../po/node/NodeAgentRelationPO.java`
- [X] T043 [P] [US4] 删除 NodeAgentRelationMapper.java in `infrastructure/repository/mysql-impl/.../mapper/node/NodeAgentRelationMapper.java`
- [X] T044 [P] [US4] 删除 NodeAgentRelationMapper.xml in `infrastructure/repository/mysql-impl/.../resources/mapper/node/NodeAgentRelationMapper.xml`
- [X] T045 [US4] 验证编译通过（执行 `mvn clean compile -DskipTests`）

**数据库迁移：**

- [X] T046 [US4] 创建 V36__drop_node_2_agent_table.sql in `bootstrap/src/main/resources/db/migration/`
- [X] T047 [US4] 执行迁移并验证表已删除

**Checkpoint**: node_2_agent 表及相关代码已完全移除，所有功能通过 agent_bound 正常工作

---

## Phase 6: Polish & Verification

**Purpose**: 最终验证和文档更新

- [X] T048 验证所有 API 功能测试通过（Node、Topology、AgentBound）
- [X] T049 运行完整编译和打包（`mvn clean package -DskipTests`）
- [ ] T050 启动应用验证无异常（`java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local`）
- [ ] T051 验证数据库结构符合预期（node, topology 字段已移除，node_2_agent 表已删除）
- [X] T052 更新前端适配文档（如需要）in `docs/frontend-adaptation/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖，可立即开始
- **US1 (Phase 2)**: 依赖 Setup 完成
- **US2 (Phase 3)**: 依赖 US1 完成（验证清理流程）
- **US3 (Phase 4)**: 依赖 US2 完成，需数据完整性验证
- **US4 (Phase 5)**: 依赖 US3 完成，需数据完整性验证
- **Polish (Phase 6)**: 依赖所有 US 完成

### User Story Dependencies

- **US1 (node.agent_team_id)**: 独立，0 数据风险最低，先执行验证流程
- **US2 (topology.coordinator_agent_id)**: 独立，0 数据，复用 US1 验证的清理流程
- **US3 (topology.global_supervisor_agent_id)**: 独立但需先验证数据迁移完整性
- **US4 (node_2_agent 表)**: 独立但需先验证数据迁移完整性，涉及代码切换

### Parallel Opportunities per Story

**US1 并行任务**:
- T004, T005, T006 可并行（不同 DTO 文件）

**US2 并行任务**:
- T016, T017, T018 可并行（不同 DTO 文件）

**US3 并行任务**:
- T029 单独（仅 TopologyDTO）

**US4 并行任务**:
- T039, T040, T041, T042, T043, T044 可并行（删除不同文件）

---

## Parallel Example: User Story 4 Delete Phase

```bash
# Launch all delete tasks for US4 together:
Task: "删除 NodeAgentRelation.java"
Task: "删除 NodeAgentRelationRepository.java"
Task: "删除 NodeAgentRelationRepositoryImpl.java"
Task: "删除 NodeAgentRelationPO.java"
Task: "删除 NodeAgentRelationMapper.java"
Task: "删除 NodeAgentRelationMapper.xml"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: User Story 1 (node.agent_team_id)
3. **STOP and VALIDATE**: 验证 Node API 功能正常
4. 可独立部署，验证清理流程正确

### Incremental Delivery

1. US1 完成 → 验证 Node API → 可部署
2. US2 完成 → 验证 Topology API → 可部署
3. US3 完成 → 验证 Global Supervisor 功能 → 可部署
4. US4 完成 → 验证 Node-Agent 绑定功能 → 可部署
5. 每个 Story 独立增量，不影响已完成功能

---

## Notes

- [P] tasks = 不同文件，无依赖，可并行
- [US#] 标签映射到 spec.md 中的 User Story
- 每个 US 可独立完成和验证
- 按风险从低到高执行（0 数据 → 有数据）
- 每个 Checkpoint 后验证 API 功能
- 代码清理顺序：DTO → Application → Domain API → Domain Model → Domain Impl → Infrastructure
