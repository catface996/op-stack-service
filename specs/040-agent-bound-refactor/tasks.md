# Tasks: Agent Binding Relationship Refactor

**Input**: Design documents from `/specs/040-agent-bound-refactor/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are NOT explicitly requested - will skip test tasks.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

DDD Multi-module architecture paths:
- **Domain model**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/`
- **Domain API**: `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/`
- **Domain Impl**: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/`
- **Repository API**: `infrastructure/repository/mysql-api/src/main/java/com/catface996/aiops/repository/`
- **Repository Impl**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`
- **Application API**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **Application Impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **Interface HTTP**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/`
- **Migrations**: `bootstrap/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema and core domain model setup

- [X] T001 Create agent_bound table migration in bootstrap/src/main/resources/db/migration/V29__create_agent_bound_table.sql
- [X] T002 [P] Create BoundEntityType enum in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agentbound/BoundEntityType.java
- [X] T003 [P] Create AgentBound domain entity in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agentbound/AgentBound.java
- [X] T004 [P] Create AgentBoundPO persistence object in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agentbound/AgentBoundPO.java
- [X] T005 Create AgentBoundMapper MyBatis mapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agentbound/AgentBoundMapper.java
- [X] T006 Create AgentBoundRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/agentbound/AgentBoundRepository.java
- [X] T007 Create AgentBoundRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agentbound/AgentBoundRepositoryImpl.java

**Checkpoint**: Database and repository layer ready - domain service implementation can begin

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain service that MUST be complete before ANY user story API can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T008 Create AgentBoundDomainService interface in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/agentbound/AgentBoundDomainService.java
- [X] T009 Create AgentBoundDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/agentbound/AgentBoundDomainServiceImpl.java
- [X] T010 [P] Create AgentBoundDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/AgentBoundDTO.java
- [X] T011 [P] Create HierarchyTeamDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/HierarchyTeamDTO.java
- [X] T012 [P] Create HierarchyStructureDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/HierarchyStructureDTO.java
- [X] T013 Create AgentBoundApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/agentbound/AgentBoundApplicationService.java
- [X] T014 Create AgentBoundApplicationServiceImpl (skeleton) in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agentbound/AgentBoundApplicationServiceImpl.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Bind Global Supervisor to Topology (Priority: P0) 🎯 MVP

**Goal**: Enable binding a Global Supervisor Agent to a Topology with automatic replacement on duplicate

**Independent Test**: Create binding between Global Supervisor Agent and Topology, verify binding exists via query-by-entity API

### Implementation for User Story 1

- [X] T015 [P] [US1] Create BindAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/request/BindAgentRequest.java
- [X] T016 [P] [US1] Create QueryByEntityRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/request/QueryByEntityRequest.java
- [X] T017 [US1] Implement bindAgent() method in AgentBoundDomainServiceImpl with TOPOLOGY/GLOBAL_SUPERVISOR validation and replace-on-duplicate logic
- [X] T018 [US1] Implement findByEntity() method in AgentBoundDomainServiceImpl for query-by-entity
- [X] T019 [US1] Implement bindAgent() method in AgentBoundApplicationServiceImpl with Agent existence validation
- [X] T020 [US1] Implement queryByEntity() method in AgentBoundApplicationServiceImpl
- [X] T021 [US1] Create AgentBoundController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AgentBoundController.java with /bind and /query-by-entity endpoints

**Checkpoint**: User Story 1 complete - Global Supervisor can be bound to Topology and queried

---

## Phase 4: User Story 2 - Bind Team Supervisor to Resource Node (Priority: P0)

**Goal**: Enable binding a Team Supervisor Agent to a Node with automatic replacement on duplicate

**Independent Test**: Create binding between Team Supervisor Agent and Node, verify binding exists via query-by-entity API

### Implementation for User Story 2

- [X] T022 [US2] Extend bindAgent() validation in AgentBoundDomainServiceImpl to support NODE/TEAM_SUPERVISOR with replace-on-duplicate
- [X] T023 [US2] Extend findByEntity() in AgentBoundDomainServiceImpl to filter by hierarchyLevel for Team Supervisor queries
- [X] T024 [US2] Update AgentBoundApplicationServiceImpl.bindAgent() to validate Agent hierarchyLevel matches request

**Checkpoint**: User Story 2 complete - Team Supervisor can be bound to Node and queried

---

## Phase 5: User Story 3 - Bind Workers to Resource Node (Priority: P0)

**Goal**: Enable binding multiple Worker Agents to a Node (no replacement, additive)

**Independent Test**: Create multiple Worker bindings for a Node, verify all bindings exist via query-by-entity API

### Implementation for User Story 3

- [X] T025 [US3] Extend bindAgent() in AgentBoundDomainServiceImpl to allow multiple TEAM_WORKER bindings per Node
- [X] T026 [US3] Add duplicate check in AgentBoundDomainServiceImpl to prevent same Agent binding twice to same entity
- [X] T027 [US3] Verify AgentBoundApplicationServiceImpl handles Worker binding correctly (no supervisor replacement logic)

**Checkpoint**: User Story 3 complete - Workers can be bound to Node and queried

---

## Phase 6: User Story 4 - Query Hierarchical Team Structure (Priority: P0)

**Goal**: Enable querying complete team hierarchy for a Topology (Global Supervisor + all Node teams)

**Independent Test**: Set up complete hierarchy and verify query-hierarchy returns all bindings grouped by team

### Implementation for User Story 4

- [X] T028 [P] [US4] Create QueryHierarchyRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/request/QueryHierarchyRequest.java
- [X] T029 [US4] Implement findHierarchyByTopologyId() method in AgentBoundRepository with optimized JOIN query
- [X] T030 [US4] Implement queryHierarchyByTopology() method in AgentBoundDomainServiceImpl
- [X] T031 [US4] Implement queryHierarchy() method in AgentBoundApplicationServiceImpl with HierarchyStructureDTO assembly
- [X] T032 [US4] Add /query-hierarchy endpoint to AgentBoundController

**Checkpoint**: User Story 4 complete - Hierarchical team structure can be queried in single operation

---

## Phase 7: User Story 5 - Migrate Existing Bindings (Priority: P1)

**Goal**: Migrate existing bindings from topology.global_supervisor_agent_id and node_2_agent table to agent_bound

**Independent Test**: Verify migration script copies all existing bindings correctly, query-hierarchy returns migrated data

### Implementation for User Story 5

- [X] T033 [US5] Create data migration script in bootstrap/src/main/resources/db/migration/V30__migrate_agent_bindings.sql
- [X] T034 [US5] Test migration idempotency (safe to run multiple times)
- [X] T035 [US5] Verify migrated data via query-hierarchy API returns same structure as Feature 038

**Checkpoint**: User Story 5 complete - All existing bindings migrated to unified table

---

## Phase 8: Additional APIs (Unbind and Query-by-Agent)

**Purpose**: Complete remaining FR-008, FR-010 APIs

- [X] T036 [P] Create UnbindAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/request/UnbindAgentRequest.java
- [X] T037 [P] Create QueryByAgentRequest in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agentbound/request/QueryByAgentRequest.java
- [X] T038 Implement unbind() method in AgentBoundDomainServiceImpl (soft delete)
- [X] T039 Implement findByAgentId() method in AgentBoundDomainServiceImpl
- [X] T040 Implement unbindAgent() method in AgentBoundApplicationServiceImpl
- [X] T041 Implement queryByAgent() method in AgentBoundApplicationServiceImpl
- [X] T042 Add /unbind and /query-by-agent endpoints to AgentBoundController

**Checkpoint**: All CRUD APIs complete

---

## Phase 9: Remove Legacy APIs

**Purpose**: Remove old binding APIs, frontend will be refactored to use new unified API

- [X] T043 Remove bind-supervisor endpoint from TopologyController (interface/interface-http/.../controller/TopologyController.java)
- [X] T044 Remove unbind-supervisor endpoint from TopologyController
- [X] T045 Remove BindSupervisorAgentRequest from interface/interface-http/.../request/topology/
- [X] T046 Remove UnbindSupervisorAgentRequest from interface/interface-http/.../request/topology/
- [X] T047 Remove bindAgent endpoint from NodeController (interface/interface-http/.../controller/NodeController.java)
- [X] T048 Remove unbindAgent endpoint from NodeController
- [X] T049 Remove BindAgentRequest from application/application-api/.../dto/node/request/
- [X] T050 Remove UnbindAgentRequest from application/application-api/.../dto/node/request/
- [X] T051 Remove bindAgent/unbindAgent methods from NodeApplicationService interface and impl
- [X] T052 Remove bindGlobalSupervisorAgent/unbindGlobalSupervisorAgent methods from TopologyApplicationService interface and impl
- [X] T053 Remove bindSupervisorAgent/unbindSupervisorAgent methods from TopologyDomainService interface and impl

**Checkpoint**: All legacy binding APIs removed

---

## Phase 10: Polish & Integration

**Purpose**: Final cleanup and integration

- [X] T054 Add Swagger/OpenAPI annotations to AgentBoundController
- [X] T055 Add logging for all AgentBound operations in AgentBoundApplicationServiceImpl
- [X] T056 Verify error handling for non-existent Agent and Entity scenarios
- [X] T057 Verify soft delete cascade when Agent or Entity is deleted
- [X] T058 Run quickstart.md validation scenarios manually
- [X] T059 Update Feature 039 ExecutionApplicationServiceImpl to use new AgentBoundApplicationService.queryHierarchy()

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - US1-US3 share core bind/query logic, recommend sequential execution
  - US4 (hierarchy query) independent of US1-US3 data model, can parallelize
  - US5 (migration) can start after US4 is complete
- **Additional APIs (Phase 8)**: Can run after Foundational, in parallel with user stories
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P0)**: Can start after Foundational (Phase 2) - Core binding mechanism
- **User Story 2 (P0)**: Can start after US1 - Extends bind validation
- **User Story 3 (P0)**: Can start after US2 - Extends bind for multiple workers
- **User Story 4 (P0)**: Can start after US1 - Independent hierarchy query
- **User Story 5 (P1)**: Should start after US4 - Requires query-hierarchy for verification

### Within Each User Story

- Models before services
- Domain service before application service
- Application service before controller
- Core implementation before integration

### Parallel Opportunities

- T002, T003, T004 can run in parallel (different files)
- T010, T011, T012 can run in parallel (different DTOs)
- T015, T016 can run in parallel (different request DTOs)
- T028, T036, T037 can run in parallel (different request DTOs)
- Phase 8 can run in parallel with User Stories 2-5

---

## Parallel Example: Phase 1 Setup

```bash
# Launch all enums and entities in parallel:
Task: "Create BoundEntityType enum in domain/.../agentbound/BoundEntityType.java"
Task: "Create AgentBound domain entity in domain/.../agentbound/AgentBound.java"
Task: "Create AgentBoundPO persistence object in infrastructure/.../agentbound/AgentBoundPO.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test Global Supervisor binding via curl
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy (MVP!)
3. Add User Story 2 + 3 → Test binding Team Supervisor and Workers
4. Add User Story 4 → Test hierarchy query → Integrate with Feature 039
5. Add User Story 5 → Run migration → Verify existing data preserved
6. Polish → Complete remaining APIs, documentation

### Task Summary

| Phase | User Story | Task Count | Key Deliverable |
|-------|-----------|------------|-----------------|
| 1 | Setup | 7 | Database + Repository layer |
| 2 | Foundational | 7 | Domain + Application services (skeleton) |
| 3 | US1 (P0) | 7 | Bind Global Supervisor API |
| 4 | US2 (P0) | 3 | Bind Team Supervisor API |
| 5 | US3 (P0) | 3 | Bind Workers API |
| 6 | US4 (P0) | 5 | Query Hierarchy API |
| 7 | US5 (P1) | 3 | Data Migration |
| 8 | Additional | 7 | Unbind + Query-by-Agent APIs |
| 9 | Remove Legacy | 11 | Remove old binding APIs |
| 10 | Polish | 6 | Documentation, logging, integration |

**Total Tasks**: 59

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Reuse existing `AgentHierarchyLevel` enum from `domain/domain-model/.../agent/AgentHierarchyLevel.java`
- Follow existing patterns in NodeAgentRelation and TopologyDomainService for binding logic
- Migration script must be idempotent per NFR-002
- Query-hierarchy must meet <100ms performance per NFR-001
