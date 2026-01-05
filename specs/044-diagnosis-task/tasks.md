# Tasks: 诊断任务持久化

**Input**: Design documents from `/specs/044-diagnosis-task/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: No tests requested.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

DDD multi-module structure:
- **bootstrap**: `bootstrap/src/main/resources/db/migration/`
- **interface**: `interface/interface-http/src/main/java/com/catface996/aiops/interface/http/controller/`
- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/`
- **domain**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/`
- **infrastructure-mysql**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`
- **infrastructure-redis**: `infrastructure/cache/redis-impl/src/main/java/com/catface996/aiops/infrastructure/cache/redis/`

---

## Phase 1: Setup (Database Migration)

**Purpose**: Create database tables for diagnosis task persistence

- [X] T001 Create V40 migration script `bootstrap/src/main/resources/db/migration/V40__create_diagnosis_task_tables.sql` with diagnosis_task and agent_diagnosis_process tables

**Checkpoint**: Database schema ready for code implementation

---

## Phase 2: Foundational (Domain & Infrastructure Layer)

**Purpose**: Core entities and repositories that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Domain Models

- [X] T002 [P] Create DiagnosisTaskStatus enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/diagnosis/DiagnosisTaskStatus.java`
- [X] T003 [P] Create DiagnosisTask domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/diagnosis/DiagnosisTask.java`
- [X] T004 [P] Create AgentDiagnosisProcess domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/diagnosis/AgentDiagnosisProcess.java`

### Persistence Objects (PO)

- [X] T005 [P] Create DiagnosisTaskPO in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/diagnosis/DiagnosisTaskPO.java`
- [X] T006 [P] Create AgentDiagnosisProcessPO in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/diagnosis/AgentDiagnosisProcessPO.java`

### MyBatis Mappers

- [X] T007 [P] Create DiagnosisTaskMapper interface in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/diagnosis/DiagnosisTaskMapper.java`
- [X] T008 [P] Create AgentDiagnosisProcessMapper interface in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/diagnosis/AgentDiagnosisProcessMapper.java`

### Repository Implementations

- [X] T009 Create DiagnosisTaskRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/diagnosis/DiagnosisTaskRepositoryImpl.java`
- [X] T010 Create AgentDiagnosisProcessRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/diagnosis/AgentDiagnosisProcessRepositoryImpl.java`

### Redis Cache Service

- [X] T011 Create DiagnosisStreamCacheService for Redis stream operations in `infrastructure/cache/redis-impl/src/main/java/com/catface996/aiops/infrastructure/cache/redis/diagnosis/DiagnosisStreamCacheService.java`

**Checkpoint**: Foundation ready - Domain models, POs, Mappers, Repositories and Redis cache service complete

---

## Phase 3: User Story 1 - 创建诊断任务 (Priority: P1) 🎯 MVP

**Goal**: 用户触发诊断后，系统创建诊断任务记录，状态为"运行中"

**Independent Test**: POST `/api/service/v1/diagnosis-tasks/create` 并验证数据库中存在对应记录

### DTOs for User Story 1

- [X] T012 [P] [US1] Create CreateDiagnosisTaskRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/diagnosis/request/CreateDiagnosisTaskRequest.java`
- [X] T013 [P] [US1] Create DiagnosisTaskDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/diagnosis/DiagnosisTaskDTO.java`

### Application Service Interface

- [X] T014 [US1] Create DiagnosisApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/diagnosis/DiagnosisApplicationService.java`

### Application Service Implementation

- [X] T015 [US1] Implement DiagnosisApplicationServiceImpl.createDiagnosisTask() in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/diagnosis/DiagnosisApplicationServiceImpl.java`

### Controller

- [X] T016 [US1] Create DiagnosisTaskController with create endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/DiagnosisTaskController.java`

**Checkpoint**: US1 complete - 可创建诊断任务并返回任务ID

---

## Phase 4: User Story 2 - 实时记录Agent诊断过程 (Priority: P1)

**Goal**: 接收executor流式响应，按agent_bound_id分类暂存到Redis

**Independent Test**: 模拟executor流式响应，验证Redis中按Agent维度正确存储数据

### Stream Collector

- [X] T017 [US2] Create DiagnosisStreamCollector for collecting SSE events in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/diagnosis/DiagnosisStreamCollector.java`

### Integrate with Executor

- [X] T018 [US2] Update DiagnosisApplicationServiceImpl to call executor and collect stream in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/diagnosis/DiagnosisApplicationServiceImpl.java`

**Checkpoint**: US2 complete - 流式数据按Agent维度暂存到Redis

---

## Phase 5: User Story 3 - 诊断完成后持久化Agent诊断过程 (Priority: P1)

**Goal**: 诊断结束后，将Redis暂存数据整合并持久化到数据库

**Independent Test**: 预填充Redis数据，触发持久化，验证数据库记录正确

### Persistence Logic

- [X] T019 [US3] Implement persistAgentDiagnosisProcesses() in DiagnosisApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/diagnosis/DiagnosisApplicationServiceImpl.java`

### Status Update

- [X] T020 [US3] Implement updateTaskStatus() for COMPLETED/FAILED/TIMEOUT transitions in DiagnosisApplicationServiceImpl

### Cleanup Redis

- [X] T021 [US3] Implement cleanupRedisData() after successful persistence in DiagnosisStreamCacheService

**Checkpoint**: US3 complete - 诊断完成后自动持久化并清理Redis

---

## Phase 6: User Story 4 - 查询诊断任务历史 (Priority: P2)

**Goal**: 用户可查看拓扑图的历史诊断任务列表和任务详情

**Independent Test**: 预创建诊断任务记录，调用查询API验证返回结果

### DTOs for User Story 4

- [X] T022 [P] [US4] Create QueryDiagnosisTaskByIdRequest in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/DiagnosisTaskController.java` (内部record类)
- [X] T023 [P] [US4] Create QueryDiagnosisTaskByTopologyRequest in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/DiagnosisTaskController.java` (内部record类)
- [X] T024 [P] [US4] Create AgentDiagnosisProcessDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/diagnosis/AgentDiagnosisProcessDTO.java`

### Query Methods

- [X] T025 [US4] Implement queryById() in DiagnosisApplicationServiceImpl (returns task with agent processes)
- [X] T026 [US4] Implement queryByTopology() with pagination in DiagnosisApplicationServiceImpl

### Controller Endpoints

- [X] T027 [US4] Add query-by-id endpoint to DiagnosisTaskController
- [X] T028 [US4] Add query-by-topology endpoint to DiagnosisTaskController

**Checkpoint**: US4 complete - 可查询历史任务列表和详情

---

## Phase 7: User Story 5 - 诊断任务异常处理 (Priority: P2)

**Goal**: executor错误或超时时，正确更新任务状态并记录错误信息

**Independent Test**: 模拟executor错误/超时，验证任务状态和错误信息正确

### Error Handling

- [X] T029 [US5] Implement handleExecutorError() in DiagnosisStreamCollector for connection failures
- [X] T030 [US5] Implement timeout detection and handling in DiagnosisApplicationServiceImpl

### Query Running Tasks

- [X] T031 [P] [US5] Create QueryRunningTasksRequest in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/DiagnosisTaskController.java` (内部record类)
- [X] T032 [US5] Implement queryRunningTasks() in DiagnosisApplicationServiceImpl
- [X] T033 [US5] Add query-running endpoint to DiagnosisTaskController

**Checkpoint**: US5 complete - 异常情况正确处理并可查询运行中任务

---

## Phase 8: Polish & Validation

**Purpose**: Final verification and integration

- [X] T034 Compile and verify no errors: run `mvn clean compile`
- [X] T035 Run database migration and verify tables created
- [X] T036 Restart service and verify application starts
- [X] T037 Verify API endpoints using quickstart.md test commands
- [X] T038 Test complete diagnosis flow: create → stream → persist → query

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - create migration first
- **Foundational (Phase 2)**: Depends on Phase 1 - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Phase 2 completion
  - US1 (创建任务) must complete before US2 (流式收集)
  - US2 (流式收集) must complete before US3 (持久化)
  - US1-3 (P1) should complete before US4-5 (P2)
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P1)**: Depends on US1 (needs task creation)
- **User Story 3 (P1)**: Depends on US2 (needs stream data to persist)
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - independent query functionality
- **User Story 5 (P2)**: Depends on US2 (needs stream handling infrastructure)

### Within Each Phase

- Domain models can be created in parallel [P]
- PO files can be created in parallel [P]
- Mapper files can be created in parallel [P]
- DTOs can be created in parallel [P]
- Repository implementations depend on PO/Mapper
- Application services depend on DTOs/Repositories
- Controllers depend on Application services

### Parallel Opportunities

- T002, T003, T004 (Domain models) can run in parallel
- T005, T006 (POs) can run in parallel
- T007, T008 (Mappers) can run in parallel
- T012, T013 (US1 DTOs) can run in parallel
- T022, T023, T024 (US4 DTOs) can run in parallel

---

## Implementation Strategy

### MVP First (User Story 1-3)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002-T011)
3. Complete Phase 3: User Story 1 - 创建诊断任务 (T012-T016)
4. Complete Phase 4: User Story 2 - 实时记录 (T017-T018)
5. Complete Phase 5: User Story 3 - 持久化 (T019-T021)
6. **STOP and VALIDATE**: Test complete diagnosis flow
7. Proceed to US4/US5 if needed

### Full Implementation

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → 可创建诊断任务
3. Add User Story 2 → 可实时收集流式数据
4. Add User Story 3 → 可持久化诊断过程
5. Add User Story 4 → 可查询历史
6. Add User Story 5 → 异常处理完善
7. Polish → Final verification

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- US1-3 是 P1 优先级，形成 MVP 核心流程
- US4-5 是 P2 优先级，增强功能
- Redis Key 命名: `diagnosis:task:{taskId}:agent:{agentBoundId}`
- Redis TTL: 24小时
- Commit after each phase for easy rollback
