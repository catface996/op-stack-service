# Tasks: Agent Tools 绑定

**Input**: Design documents from `/specs/030-agent-tools/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are NOT included - not explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
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

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and base structure

- [X] T001 Create Flyway migration script in bootstrap/src/main/resources/db/migration/V18__Create_agent_tool_relation_table.sql

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 [P] Create AgentToolRelation domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentToolRelation.java
- [X] T003 [P] Create AgentToolRelationPO persistence object in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentToolRelationPO.java
- [X] T004 [P] Create AgentToolRelationMapper MyBatis mapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/AgentToolRelationMapper.java
- [X] T005 Create AgentToolRelationRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentToolRelationRepository.java
- [X] T006 Create AgentToolRelationRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentToolRelationRepositoryImpl.java
- [X] T007 Add toolIds field to Agent domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 & 2 - 绑定和查询 Tools (Priority: P1) 🎯 MVP

**Goal**:
- US1: 管理员可以通过更新接口为 Agent 绑定 Tools，支持全量替换
- US2: 查询 Agent 时返回 toolIds 列表

**Independent Test**:
- 调用 Agent 更新接口传入 toolIds，然后查询 Agent 详情验证绑定是否正确
- 验证全量替换：更新 toolIds 后原有绑定被完全替换
- 验证清空：传入空列表清空所有绑定

### Implementation for User Story 1 & 2

- [X] T008 [P] [US1] Add toolIds field to UpdateAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/UpdateAgentRequest.java
- [X] T009 [P] [US2] Add toolIds field to AgentDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java
- [X] T010 [US1] Implement Tools binding logic in updateAgent method of application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java
- [X] T011 [US2] Implement toolIds population in toDTO and query methods of application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java
- [X] T012 [US2] Add batch toolIds query for list operations in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentRepositoryImpl.java

**Checkpoint**: At this point, User Stories 1 & 2 should be fully functional:
- Agent 更新接口支持 toolIds 全量替换
- Agent 查询接口返回 toolIds
- 支持空列表清空绑定

---

## Phase 4: User Story 3 - 创建 Agent 时指定 Tools (Priority: P2)

**Goal**: 管理员在创建 Agent 时可以同时指定 toolIds

**Independent Test**: 调用 Agent 创建接口并传入 toolIds，然后查询验证绑定结果

### Implementation for User Story 3

- [X] T013 [P] [US3] Add toolIds field to CreateAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/CreateAgentRequest.java
- [X] T014 [US3] Implement Tools binding logic in createAgent method of application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java

**Checkpoint**: All user stories should now be independently functional:
- 创建 Agent 时可指定 toolIds
- 更新 Agent 时可全量替换 toolIds
- 查询 Agent 时返回 toolIds

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Edge cases, validation, and improvements

- [X] T015 Implement toolIds deduplication in AgentApplicationServiceImpl
- [X] T016 Implement invalid Tool ID filtering (verify Tool exists before binding) in AgentApplicationServiceImpl
- [X] T017 Add logging for Tools binding operations in AgentApplicationServiceImpl
- [X] T018 Run application and verify with quickstart.md test scenarios

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3, 4)**: All depend on Foundational phase completion
- **Polish (Phase 5)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 & 2 (P1)**: Can start after Foundational (Phase 2) - Core MVP
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Independent from US1/US2

### Within Each Phase

- T002-T004 can run in parallel (different files)
- T005 depends on T002 (repository interface references domain model)
- T006 depends on T003, T004, T005 (implementation references PO, Mapper, interface)
- T007 must complete before Phase 3 begins
- T008-T009 can run in parallel (different DTO files)
- T010-T012 are sequential (service logic, then repository integration)
- T013-T014 can run after T010 (references same service patterns)
- T015-T018 are polish tasks, sequential by nature

### Parallel Opportunities

Within Phase 2:
```
Parallel: T002, T003, T004 (different files, no dependencies)
Sequential: T005 → T006 → T007
```

Within Phase 3:
```
Parallel: T008, T009 (different DTO files)
Sequential: T010 → T011 → T012
```

---

## Parallel Example: Phase 2 Foundation

```bash
# Launch these three tasks together:
Task: "T002 Create AgentToolRelation domain model"
Task: "T003 Create AgentToolRelationPO persistence object"
Task: "T004 Create AgentToolRelationMapper"

# Then sequential:
Task: "T005 Create AgentToolRelationRepository interface"
Task: "T006 Create AgentToolRelationRepositoryImpl"
Task: "T007 Add toolIds to Agent domain model"
```

## Parallel Example: Phase 3 DTOs

```bash
# Launch these two tasks together:
Task: "T008 Add toolIds to UpdateAgentRequest"
Task: "T009 Add toolIds to AgentDTO"
```

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

1. Complete Phase 1: Setup (migration script)
2. Complete Phase 2: Foundational (domain model, repository, PO, mapper)
3. Complete Phase 3: User Stories 1 & 2
4. **STOP and VALIDATE**: Test binding and query via curl/API
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 & US2 → Test binding/query → **MVP!**
3. Add US3 → Test create with tools → Full feature
4. Polish → Edge cases, logging → Production ready

### Key Files Summary

| File | Action | Phase |
|------|--------|-------|
| V18__Create_agent_tool_relation_table.sql | Create | 1 |
| AgentToolRelation.java | Create | 2 |
| AgentToolRelationPO.java | Create | 2 |
| AgentToolRelationMapper.java | Create | 2 |
| AgentToolRelationRepository.java | Create | 2 |
| AgentToolRelationRepositoryImpl.java | Create | 2 |
| Agent.java | Modify | 2 |
| UpdateAgentRequest.java | Modify | 3 |
| AgentDTO.java | Modify | 3 |
| AgentApplicationServiceImpl.java | Modify | 3, 4, 5 |
| AgentRepositoryImpl.java | Modify | 3 |
| CreateAgentRequest.java | Modify | 4 |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Tool ID validation requires Tool table to exist (assumption: Tool table exists)
