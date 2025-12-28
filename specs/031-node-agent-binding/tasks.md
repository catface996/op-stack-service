# Tasks: Node-Agent 绑定功能

**Input**: Design documents from `/specs/031-node-agent-binding/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Tests are NOT included - not explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 DDD 多模块架构：
- **domain/domain-model**: 领域模型
- **domain/repository-api**: 仓储接口
- **infrastructure/repository/mysql-impl**: 仓储实现、Mapper、PO
- **application/application-api**: DTO 和服务接口
- **application/application-impl**: 服务实现
- **interface/interface-http**: HTTP 控制器
- **bootstrap**: 配置和数据库迁移

---

## Phase 1: Setup (Database Migration)

**Goal**: 创建 node_2_agent 关联表

- [X] T001 Create Flyway migration script bootstrap/src/main/resources/db/migration/V21__Create_node_agent_relation_table.sql per data-model.md DDL

---

## Phase 2: Foundational (Domain & Infrastructure Layer)

**Goal**: 创建共享的领域模型、仓储接口和基础设施层实现，所有 User Story 都依赖这些组件

### Step 2.1: Domain Layer

- [X] T002 Create NodeAgentRelation domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/node/NodeAgentRelation.java per data-model.md
- [X] T003 Create NodeAgentRelationRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/node/NodeAgentRelationRepository.java per data-model.md

### Step 2.2: Infrastructure Layer (can run in parallel)

- [X] T004 [P] Create NodeAgentRelationPO persistent object in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/node/NodeAgentRelationPO.java
- [X] T005 [P] Create NodeAgentRelationMapper MyBatis interface in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/NodeAgentRelationMapper.java
- [X] T006 Create NodeAgentRelationRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/node/NodeAgentRelationRepositoryImpl.java

### Step 2.3: Application Layer DTO (can run in parallel)

- [X] T007 [P] Create NodeAgentRelationDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/node/NodeAgentRelationDTO.java
- [X] T008 [P] Create BindAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/node/request/BindAgentRequest.java per contracts/bind-agent.md
- [X] T009 [P] Create UnbindAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/node/request/UnbindAgentRequest.java per contracts/unbind-agent.md
- [X] T010 [P] Create ListAgentsByNodeRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/node/request/ListAgentsByNodeRequest.java per contracts/list-agents.md
- [X] T011 [P] Create ListNodesByAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/node/request/ListNodesByAgentRequest.java per contracts/list-nodes-by-agent.md

**Checkpoint**: Foundation complete - all domain, infrastructure, and DTO classes created.

---

## Phase 3: User Story 1 - 绑定 Agent 到资源节点 (Priority: P1) 🎯 MVP

**Goal**: 实现绑定功能，将 Agent 绑定到 Node

**Independent Test**:
- 调用 POST /api/service/v1/nodes/bindAgent 绑定成功
- 绑定不存在的 Node/Agent 返回 404
- 重复绑定返回 409

### Step 3.1: Application Layer

- [X] T012 [US1] Add bindAgent method signature to NodeApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/node/NodeApplicationService.java
- [X] T013 [US1] Implement bindAgent method in NodeApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/node/NodeApplicationServiceImpl.java (includes Node/Agent validation, duplicate check)

### Step 3.2: Interface Layer

- [X] T014 [US1] Add bindAgent endpoint to NodeController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/NodeController.java per contracts/bind-agent.md

**Checkpoint**: User Story 1 complete - bindAgent endpoint functional.

---

## Phase 4: User Story 2 - 查询节点关联的 Agent 列表 (Priority: P1) 🎯 MVP

**Goal**: 实现按节点查询关联 Agent 列表功能

**Independent Test**:
- 调用 POST /api/service/v1/nodes/listAgents 返回 Agent 列表
- 无关联时返回空列表

### Step 4.1: Application Layer

- [X] T015 [US2] Add listAgentsByNode method signature to NodeApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/node/NodeApplicationService.java
- [X] T016 [US2] Implement listAgentsByNode method in NodeApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/node/NodeApplicationServiceImpl.java (query relation, load Agent details)

### Step 4.2: Interface Layer

- [X] T017 [US2] Add listAgents endpoint to NodeController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/NodeController.java per contracts/list-agents.md

**Checkpoint**: User Story 2 complete - listAgents endpoint functional.

---

## Phase 5: User Story 3 - 查询 Agent 关联的节点列表 (Priority: P2)

**Goal**: 实现按 Agent 查询关联节点列表功能

**Independent Test**:
- 调用 POST /api/service/v1/nodes/listNodesByAgent 返回 Node 列表
- 无关联时返回空列表

### Step 5.1: Application Layer

- [X] T018 [US3] Add listNodesByAgent method signature to NodeApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/node/NodeApplicationService.java
- [X] T019 [US3] Implement listNodesByAgent method in NodeApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/node/NodeApplicationServiceImpl.java (query relation, load Node details)

### Step 5.2: Interface Layer

- [X] T020 [US3] Add listNodesByAgent endpoint to NodeController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/NodeController.java per contracts/list-nodes-by-agent.md

**Checkpoint**: User Story 3 complete - listNodesByAgent endpoint functional.

---

## Phase 6: User Story 4 - 解除绑定关系 (Priority: P2)

**Goal**: 实现解绑功能，解除 Agent 与 Node 的绑定关系

**Independent Test**:
- 调用 POST /api/service/v1/nodes/unbindAgent 解绑成功
- 解绑不存在的关系返回 404

### Step 6.1: Application Layer

- [X] T021 [US4] Add unbindAgent method signature to NodeApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/node/NodeApplicationService.java
- [X] T022 [US4] Implement unbindAgent method in NodeApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/node/NodeApplicationServiceImpl.java (soft delete relation)

### Step 6.2: Interface Layer

- [X] T023 [US4] Add unbindAgent endpoint to NodeController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/NodeController.java per contracts/unbind-agent.md

**Checkpoint**: User Story 4 complete - unbindAgent endpoint functional.

---

## Phase 7: Polish & Verification

**Purpose**: Final verification and cleanup

- [X] T024 Run `mvn clean package -DskipTests` and verify compilation succeeds
- [X] T025 Start application and verify V21 migration executes successfully
- [X] T026 Test all 4 endpoints per quickstart.md test scenarios
- [X] T027 Verify Swagger documentation shows all new endpoints

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - can start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 (need database table for testing)
- **Phase 3 (US1)**: Depends on Phase 2 (need domain, repository, DTOs)
- **Phase 4 (US2)**: Depends on Phase 2 (need domain, repository, DTOs)
- **Phase 5 (US3)**: Depends on Phase 2 (need domain, repository, DTOs)
- **Phase 6 (US4)**: Depends on Phase 2 (need domain, repository, DTOs)
- **Phase 7 (Polish)**: Depends on all phases complete

### Within Phase 2

```text
Sequential: T002 → T003 (domain model before repository interface)
After T003: T004, T005 can run in parallel (PO and Mapper)
After T004, T005: T006 (repository impl needs PO and Mapper)
Parallel: T007, T008, T009, T010, T011 (independent DTO files)
```

### User Story Independence

US1, US2, US3, US4 can be implemented independently after Phase 2:
- Each story only adds methods to existing service/controller
- Each story can be tested independently
- Recommended: US1 → US2 (绑定后验证查询) → US4 (解绑) → US3

---

## Parallel Opportunities

### Phase 2 Infrastructure (after T003):
```bash
# These can run together:
Task: "T004 Create NodeAgentRelationPO"
Task: "T005 Create NodeAgentRelationMapper"
```

### Phase 2 DTOs (after T006):
```bash
# These can run together:
Task: "T007 Create NodeAgentRelationDTO"
Task: "T008 Create BindAgentRequest"
Task: "T009 Create UnbindAgentRequest"
Task: "T010 Create ListAgentsByNodeRequest"
Task: "T011 Create ListNodesByAgentRequest"
```

### Multiple User Stories (after Phase 2):
```bash
# Can implement in parallel if needed (different method additions):
Phase 3 (US1): bindAgent
Phase 4 (US2): listAgentsByNode
Phase 5 (US3): listNodesByAgent
Phase 6 (US4): unbindAgent
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2 Only)

1. Complete Phase 1: Database migration
2. Complete Phase 2: Foundational components
3. Complete Phase 3 (US1): Bind Agent
4. Complete Phase 4 (US2): List Agents by Node
5. **STOP and VALIDATE**: Test bind + query flow
6. If working → MVP complete!

### Full Delivery

1. Complete Phase 1 → Database ready
2. Complete Phase 2 → Foundation ready
3. Complete Phase 3 (US1) → Bind functional
4. Complete Phase 4 (US2) → List by Node functional
5. Complete Phase 5 (US3) → List by Agent functional
6. Complete Phase 6 (US4) → Unbind functional
7. Complete Phase 7 → Full verification

### Key Files Summary

| File | Action | Phase |
|------|--------|-------|
| V21__Create_node_agent_relation_table.sql | Create | 1 |
| NodeAgentRelation.java | Create | 2 |
| NodeAgentRelationRepository.java | Create | 2 |
| NodeAgentRelationPO.java | Create | 2 |
| NodeAgentRelationMapper.java | Create | 2 |
| NodeAgentRelationRepositoryImpl.java | Create | 2 |
| NodeAgentRelationDTO.java | Create | 2 |
| BindAgentRequest.java | Create | 2 |
| UnbindAgentRequest.java | Create | 2 |
| ListAgentsByNodeRequest.java | Create | 2 |
| ListNodesByAgentRequest.java | Create | 2 |
| NodeApplicationService.java | Modify | 3-6 |
| NodeApplicationServiceImpl.java | Modify | 3-6 |
| NodeController.java | Modify | 3-6 |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- All HTTP interfaces are in NodeController (not AgentController) as per spec
- Soft delete mechanism consistent with project patterns
- No automated tests required per specification
