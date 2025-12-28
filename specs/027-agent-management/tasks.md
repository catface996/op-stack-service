# Tasks: Agent Management API

**Input**: Design documents from `/specs/027-agent-management/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Not requested for this feature (as per plan.md)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions (DDD Multi-Module)

- **Bootstrap**: `bootstrap/src/main/` (config, migrations)
- **Domain Model**: `domain/domain-model/src/main/java/com/op/stack/domain/model/agent/`
- **Repository API**: `domain/repository-api/src/main/java/com/op/stack/repository/agent/`
- **Repository Impl**: `infrastructure/repository/mysql-impl/src/main/java/com/op/stack/repository/mysql/`
- **Application API**: `application/application-api/src/main/java/com/op/stack/application/api/`
- **Application Impl**: `application/application-impl/src/main/java/com/op/stack/application/impl/`
- **Interface HTTP**: `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/`
- **Common**: `common/src/main/java/com/op/stack/common/enums/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema and error codes

- [X] T001 Create database migration script `bootstrap/src/main/resources/db/migration/V15__create_agent_tables.sql` with agent and agent_2_team tables
- [X] T002 [P] Create AgentErrorCode enum in `common/src/main/java/com/catface996/aiops/common/enums/AgentErrorCode.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain models, enums, and repository interfaces that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

### Domain Model Layer

- [X] T003 [P] Create AgentRole enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentRole.java`
- [X] T004 [P] Create AgentStatus enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentStatus.java`
- [X] T005 [P] Create AgentConfig value object in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentConfig.java`
- [X] T006 [P] Create AgentFindings value object in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentFindings.java`
- [X] T007 Create Agent domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java` (depends on T003-T006)
- [X] T008 Create AgentTeamRelation domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentTeamRelation.java`

### Repository API Layer

- [X] T009 [P] Create AgentRepository interface in `domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentRepository.java`
- [X] T010 [P] Create AgentTeamRelationRepository interface in `domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentTeamRelationRepository.java`

### Infrastructure Layer (Persistence)

- [X] T011 [P] Create AgentPO persistence object in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentPO.java`
- [X] T012 [P] Create AgentTeamRelationPO persistence object in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentTeamRelationPO.java`
- [X] T013 [P] AgentConfig JSON conversion handled via ObjectMapper in AgentRepositoryImpl (no separate TypeHandler needed)
- [X] T014 [P] Create AgentMapper interface in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/AgentMapper.java`
- [X] T015 [P] Create AgentTeamRelationMapper interface in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/AgentTeamRelationMapper.java`
- [X] T016 Create AgentRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentRepositoryImpl.java` (depends on T011, T014)
- [X] T017 Create AgentTeamRelationRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentTeamRelationRepositoryImpl.java` (depends on T012, T015)

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - List Agents (Priority: P0)

**Goal**: Enable users to view paginated list of all Agents with filtering and search

**Independent Test**: Call `POST /api/service/v1/agents/list` with pagination params, verify paginated results with filters working

### Application Layer

- [X] T018 [P] [US1] Create ListAgentsRequest DTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/ListAgentsRequest.java`
- [X] T019 [P] [US1] Create AgentDTO response in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java` (also created AgentConfigDTO.java)
- [X] T020 [US1] Create AgentApplicationService interface with listAgents method in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/agent/AgentApplicationService.java`
- [X] T021 [US1] Implement listAgents in AgentApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [X] T022 [US1] Add listAgents endpoint to AgentController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 1 complete - can list agents with pagination and filters

---

## Phase 4: User Story 2 - Get Agent Details (Priority: P0)

**Goal**: Enable users to view complete details of a single Agent

**Independent Test**: Call `POST /api/service/v1/agents/get` with valid ID, verify full agent details returned

### Application Layer

- [X] T023 [P] [US2] Create GetAgentRequest DTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/GetAgentRequest.java`
- [X] T024 [US2] Add getAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/agent/AgentApplicationService.java`
- [X] T025 [US2] Implement getAgent in AgentApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [X] T026 [US2] Add getAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 2 complete - can view agent details

---

## Phase 5: User Story 3 - Create Agent (Priority: P1)

**Goal**: Enable users to create new Agents with configuration

**Independent Test**: Call `POST /api/service/v1/agents/create` with valid params, verify agent created with correct defaults

### Application Layer

- [X] T027 [P] [US3] Create CreateAgentRequest DTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/CreateAgentRequest.java`
- [X] T028 [P] [US3] AgentConfigDTO already created in US1 at `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentConfigDTO.java`
- [X] T029 [US3] Add createAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/agent/AgentApplicationService.java`
- [X] T030 [US3] Implement createAgent in AgentApplicationServiceImpl with GLOBAL_SUPERVISOR singleton check and name uniqueness validation in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [X] T031 [US3] Add createAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 3 complete - can create agents with validation

---

## Phase 6: User Story 4 - Update Agent Info (Priority: P1)

**Goal**: Enable users to update Agent basic info (name, specialty, status)

**Independent Test**: Call `POST /api/service/v1/agents/update` with valid ID and new name, verify updated

### Application Layer

- [ ] T032 [P] [US4] Create UpdateAgentRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/UpdateAgentRequest.java`
- [ ] T033 [US4] Add updateAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T034 [US4] Implement updateAgent in AgentApplicationServiceImpl with working status protection and role immutability in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T035 [US4] Add updateAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 4 complete - can update agent info

---

## Phase 7: User Story 5 - Update Agent Config (Priority: P1)

**Goal**: Enable users to update Agent AI configuration separately

**Independent Test**: Call `POST /api/service/v1/agents/config/update` with valid temperature, verify config updated

### Application Layer

- [ ] T036 [P] [US5] Create UpdateAgentConfigRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/UpdateAgentConfigRequest.java`
- [ ] T037 [US5] Add updateAgentConfig method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T038 [US5] Implement updateAgentConfig in AgentApplicationServiceImpl with temperature range validation in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T039 [US5] Add updateAgentConfig endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 5 complete - can update agent config

---

## Phase 8: User Story 6 - Delete Agent (Priority: P1)

**Goal**: Enable users to soft-delete Agents with business rule enforcement

**Independent Test**: Call `POST /api/service/v1/agents/delete` with valid ID, verify soft deleted

### Application Layer

- [ ] T040 [P] [US6] Create DeleteAgentRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/DeleteAgentRequest.java`
- [ ] T041 [US6] Add deleteAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T042 [US6] Implement deleteAgent in AgentApplicationServiceImpl with GLOBAL_SUPERVISOR and TEAM_SUPERVISOR constraints in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T043 [US6] Add deleteAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 6 complete - can delete agents with constraints

---

## Phase 9: User Story 7 - Assign Agent to Team (Priority: P2)

**Goal**: Enable users to assign Agents to Teams

**Independent Test**: Call `POST /api/service/v1/agents/assign` with agentId and teamId, verify relation created

### Application Layer

- [ ] T044 [P] [US7] Create AssignAgentRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/AssignAgentRequest.java`
- [ ] T045 [US7] Add assignAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T046 [US7] Implement assignAgent in AgentApplicationServiceImpl with duplicate assignment check in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T047 [US7] Add assignAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 7 complete - can assign agents to teams

---

## Phase 10: User Story 8 - Unassign Agent from Team (Priority: P2)

**Goal**: Enable users to remove Agents from Teams

**Independent Test**: Call `POST /api/service/v1/agents/unassign` with agentId and teamId, verify relation removed

### Application Layer

- [ ] T048 [P] [US8] Create UnassignAgentRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/UnassignAgentRequest.java`
- [ ] T049 [US8] Add unassignAgent method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T050 [US8] Implement unassignAgent in AgentApplicationServiceImpl with TEAM_SUPERVISOR constraint in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T051 [US8] Add unassignAgent endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 8 complete - can unassign agents from teams

---

## Phase 11: User Story 9 - List Agent Templates (Priority: P2)

**Goal**: Provide predefined Agent configuration templates

**Independent Test**: Call `POST /api/service/v1/agents/templates/list`, verify 5 predefined templates returned

### Domain Model Layer

- [ ] T052 [P] [US9] Create AgentTemplate enum with 5 predefined templates in `domain/domain-model/src/main/java/com/op/stack/domain/model/agent/AgentTemplate.java`

### Application Layer

- [ ] T053 [P] [US9] Create AgentTemplateDTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/AgentTemplateDTO.java`
- [ ] T054 [US9] Add listAgentTemplates method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T055 [US9] Implement listAgentTemplates in AgentApplicationServiceImpl in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T056 [US9] Add listAgentTemplates endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 9 complete - can list agent templates

---

## Phase 12: User Story 10 - Get Agent Stats (Priority: P2)

**Goal**: Provide Agent statistics (counts, distribution, findings)

**Independent Test**: Call `POST /api/service/v1/agents/stats`, verify stats returned with role/status distribution

### Application Layer

- [ ] T057 [P] [US10] Create AgentStatsRequest DTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/request/AgentStatsRequest.java`
- [ ] T058 [P] [US10] Create AgentStatsDTO in `application/application-api/src/main/java/com/op/stack/application/api/dto/agent/AgentStatsDTO.java`
- [ ] T059 [US10] Add getAgentStats method to AgentApplicationService interface in `application/application-api/src/main/java/com/op/stack/application/api/service/agent/AgentApplicationService.java`
- [ ] T060 [US10] Implement getAgentStats in AgentApplicationServiceImpl with aggregation queries in `application/application-impl/src/main/java/com/op/stack/application/impl/service/agent/AgentApplicationServiceImpl.java`

### Interface Layer

- [ ] T061 [US10] Add getAgentStats endpoint to AgentController in `interface/interface-http/src/main/java/com/op/stack/interface_/http/controller/AgentController.java`

**Checkpoint**: User Story 10 complete - can view agent stats

---

## Phase 13: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T062 [P] Add Swagger/OpenAPI annotations to AgentController for API documentation
- [ ] T063 [P] Run quickstart.md validation - test all curl examples
- [ ] T064 Verify all error codes and messages match specification

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-12)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P0 → P1 → P2)
- **Polish (Phase 13)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P0)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P0)**: Can start after Foundational (Phase 2) - Shares AgentDTO with US1
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Shares AgentDTO with US1
- **User Story 4 (P1)**: Can start after US3 (needs agent to exist)
- **User Story 5 (P1)**: Can start after US3 (needs agent to exist)
- **User Story 6 (P1)**: Can start after US3 (needs agent to exist)
- **User Story 7 (P2)**: Can start after US3 (needs agent to exist)
- **User Story 8 (P2)**: Can start after US7 (needs assignment to exist)
- **User Story 9 (P2)**: Can start after Foundational (Phase 2) - Independent
- **User Story 10 (P2)**: Can start after US3 (needs agents for stats)

### Within Each User Story

- DTOs before service interfaces
- Service interfaces before implementations
- Implementations before controllers
- Core implementation before validation logic

### Parallel Opportunities

- All Foundational domain models (T003-T006) can run in parallel
- All Foundational PO objects (T011-T012) can run in parallel
- All Foundational mappers (T014-T015) can run in parallel
- All repository interfaces (T009-T010) can run in parallel
- Request DTOs across different stories can run in parallel
- Once Foundational phase completes, US1, US2, US3, US9 can start in parallel

---

## Parallel Example: Foundational Phase

```bash
# Launch all domain enums/value objects together:
Task T003: "Create AgentRole enum"
Task T004: "Create AgentStatus enum"
Task T005: "Create AgentConfig value object"
Task T006: "Create AgentFindings value object"

# After enums complete, launch repository layer in parallel:
Task T009: "Create AgentRepository interface"
Task T010: "Create AgentTeamRelationRepository interface"
Task T011: "Create AgentPO persistence object"
Task T012: "Create AgentTeamRelationPO persistence object"
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2 Only)

1. Complete Phase 1: Setup (migrations, error codes)
2. Complete Phase 2: Foundational (domain models, repositories)
3. Complete Phase 3: User Story 1 (List Agents)
4. Complete Phase 4: User Story 2 (Get Agent Details)
5. **STOP and VALIDATE**: Test list and get independently
6. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US1 + US2 → Test independently → Deploy (Read-Only MVP!)
3. Add US3 → Test create independently → Deploy (CRUD base)
4. Add US4 + US5 + US6 → Test independently → Deploy (Full CRUD)
5. Add US7 + US8 → Test team assignment → Deploy
6. Add US9 + US10 → Test templates/stats → Deploy (Full Feature)

---

## Summary

- **Total Tasks**: 64
- **Phase 1 (Setup)**: 2 tasks
- **Phase 2 (Foundational)**: 15 tasks
- **User Stories**: 44 tasks (US1: 5, US2: 4, US3: 5, US4: 4, US5: 4, US6: 4, US7: 4, US8: 4, US9: 5, US10: 5)
- **Polish**: 3 tasks
- **Parallel Opportunities**: 28 tasks marked [P]
- **MVP Scope**: US1 + US2 (List + Get = Read-Only operations)
