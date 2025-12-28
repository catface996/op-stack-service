# Tasks: Report Management

**Input**: Design documents from `/specs/026-report-management/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: No tests requested - implementation only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

DDD multi-module architecture:
- **Domain Models**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/`
- **Repository API**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/report/`
- **Infrastructure**: `infrastructure/persistence/mybatis-plus-impl/src/main/java/com/catface996/aiops/infrastructure/persistence/mybatisplus/report/`
- **Application API**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **Application Impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/report/`
- **Controller**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/`
- **Migration**: `bootstrap/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and shared enumerations/models

- [X] T001 Create database migration file `bootstrap/src/main/resources/db/migration/V14__create_report_tables.sql`
- [X] T002 [P] Create ReportType enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/ReportType.java`
- [X] T003 [P] Create ReportStatus enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/ReportStatus.java`
- [X] T004 [P] Create ReportTemplateCategory enum in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/ReportTemplateCategory.java`
- [X] T005 [P] Create Report domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/Report.java`
- [X] T006 [P] Create ReportTemplate domain model in `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/report/ReportTemplate.java`

---

## Phase 2: Foundational (Repository Layer)

**Purpose**: Repository interfaces and implementations that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 [P] Create ReportRepository interface in `domain/repository-api/src/main/java/com/catface996/aiops/repository/report/ReportRepository.java`
- [X] T008 [P] Create ReportTemplateRepository interface in `domain/repository-api/src/main/java/com/catface996/aiops/repository/report/ReportTemplateRepository.java`
- [X] T009 [P] Create ReportPO in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/report/ReportPO.java`
- [X] T010 [P] Create ReportTemplatePO in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/report/ReportTemplatePO.java`
- [X] T011 [P] Create ReportMapper in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/report/ReportMapper.java`
- [X] T012 [P] Create ReportTemplateMapper in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/report/ReportTemplateMapper.java`
- [X] T013 [P] Converter logic included in ReportRepositoryImpl (inline conversion methods)
- [X] T014 [P] Converter logic included in ReportTemplateRepositoryImpl (inline conversion methods)
- [X] T015 Create ReportRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/report/ReportRepositoryImpl.java`
- [X] T016 Create ReportTemplateRepositoryImpl in `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/report/ReportTemplateRepositoryImpl.java`

**Checkpoint**: Repository layer ready - user story implementation can now begin

---

## Phase 3: User Stories 1 & 2 - List Reports & Get Report Detail (Priority: P0)

**Goal**: Deliver P0 functionality - users can view report list and report details

**Independent Test**:
- Call `POST /api/service/v1/reports/list` to verify pagination, filtering, and search
- Call `POST /api/service/v1/reports/get` to verify report detail retrieval

### DTOs for User Stories 1 & 2

- [X] T017 [P] [US1] Create ReportDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/ReportDTO.java`
- [X] T018 [P] [US1] Create ListReportsRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/ListReportsRequest.java`
- [X] T019 [P] [US2] Create GetReportRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/GetReportRequest.java`

### Application Service for User Stories 1 & 2

- [X] T020 [US1] Create ReportApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/report/ReportApplicationService.java`
- [X] T021 [US1] Implement ReportApplicationServiceImpl (listReports, getReportById methods) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/report/ReportApplicationServiceImpl.java`

### Controller for User Stories 1 & 2

- [X] T022 [US1] Create ReportController with list and get endpoints in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ReportController.java`

**Checkpoint**: P0 functionality complete - users can list and view reports

---

## Phase 4: User Stories 3 & 4 - Create & Delete Report (Priority: P1)

**Goal**: Deliver P1 functionality - users can create and delete reports

**Independent Test**:
- Call `POST /api/service/v1/reports/create` to verify report creation
- Call `POST /api/service/v1/reports/delete` to verify report deletion

### DTOs for User Stories 3 & 4

- [X] T023 [P] [US3] Create CreateReportRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/CreateReportRequest.java`
- [X] T024 [P] [US4] Create DeleteReportRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/DeleteReportRequest.java`

### Application Service Extensions for User Stories 3 & 4

- [X] T025 [US3] Add createReport method to ReportApplicationService interface
- [X] T026 [US3] Implement createReport in ReportApplicationServiceImpl (includes topology_id validation)
- [X] T027 [US4] Add deleteReport method to ReportApplicationService interface
- [X] T028 [US4] Implement deleteReport in ReportApplicationServiceImpl

### Controller Extensions for User Stories 3 & 4

- [X] T029 [US3] Add create endpoint to ReportController
- [X] T030 [US4] Add delete endpoint to ReportController

**Checkpoint**: P1 functionality complete - full report CRUD (without update) available

---

## Phase 5: User Stories 5 & 6 - List & Get Template (Priority: P2)

**Goal**: Deliver template list and detail functionality

**Independent Test**:
- Call `POST /api/service/v1/report-templates/list` to verify template list
- Call `POST /api/service/v1/report-templates/get` to verify template detail

### DTOs for User Stories 5 & 6

- [X] T031 [P] [US5] Create ReportTemplateDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/ReportTemplateDTO.java`
- [X] T032 [P] [US5] Create ListReportTemplatesRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/ListReportTemplatesRequest.java`
- [X] T033 [P] [US6] Create GetReportTemplateRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/GetReportTemplateRequest.java`

### Application Service for User Stories 5 & 6

- [X] T034 [US5] Create ReportTemplateApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/report/ReportTemplateApplicationService.java`
- [X] T035 [US5] Implement ReportTemplateApplicationServiceImpl (listTemplates, getTemplateById methods) in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/reporttemplate/ReportTemplateApplicationServiceImpl.java`

### Controller for User Stories 5 & 6

- [X] T036 [US5] Create ReportTemplateController with list and get endpoints in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ReportTemplateController.java`

**Checkpoint**: Template list and detail functionality complete

---

## Phase 6: User Stories 7, 8, 9 - Template CRUD (Priority: P2)

**Goal**: Deliver full template CRUD functionality

**Independent Test**:
- Call template create/update/delete endpoints to verify full template management

### DTOs for User Stories 7, 8, 9

- [X] T037 [P] [US7] Create CreateReportTemplateRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/CreateReportTemplateRequest.java`
- [X] T038 [P] [US8] Create UpdateReportTemplateRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/UpdateReportTemplateRequest.java`
- [X] T039 [P] [US9] Create DeleteReportTemplateRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/report/request/DeleteReportTemplateRequest.java`

### Application Service Extensions for User Stories 7, 8, 9

- [X] T040 [US7] Add createTemplate method to ReportTemplateApplicationService interface
- [X] T041 [US7] Implement createTemplate in ReportTemplateApplicationServiceImpl
- [X] T042 [US8] Add updateTemplate method to ReportTemplateApplicationService interface
- [X] T043 [US8] Implement updateTemplate in ReportTemplateApplicationServiceImpl (with optimistic locking)
- [X] T044 [US9] Add deleteTemplate method to ReportTemplateApplicationService interface
- [X] T045 [US9] Implement deleteTemplate in ReportTemplateApplicationServiceImpl

### Controller Extensions for User Stories 7, 8, 9

- [X] T046 [US7] Add create endpoint to ReportTemplateController
- [X] T047 [US8] Add update endpoint to ReportTemplateController
- [X] T048 [US9] Add delete endpoint to ReportTemplateController

**Checkpoint**: P2 functionality complete - full template CRUD available

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and OpenAPI documentation update

- [X] T049 Update OpenApiConfig to include Report and Template management in API description in `bootstrap/src/main/java/com/catface996/aiops/bootstrap/config/OpenApiConfig.java`
- [ ] T050 Verify all endpoints are accessible via Swagger UI
- [ ] T051 Run quickstart.md curl examples to validate all APIs

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - can start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 (T001 migration must be applied first)
- **Phase 3 (P0)**: Depends on Phase 2 completion
- **Phase 4 (P1)**: Depends on Phase 3 completion (uses ReportApplicationService)
- **Phase 5 (P2 Templates)**: Depends on Phase 2 completion (can run parallel to Phase 3/4)
- **Phase 6 (P2 Template CRUD)**: Depends on Phase 5 completion
- **Phase 7 (Polish)**: Depends on all previous phases

### User Story Dependencies

- **US1 & US2 (P0)**: Can start after Phase 2 - Core report viewing
- **US3 & US4 (P1)**: Depend on Phase 3 - Extend report management
- **US5 & US6 (P2)**: Can start after Phase 2 - Independent of report stories
- **US7, US8, US9 (P2)**: Depend on Phase 5 - Extend template management

### Parallel Opportunities

**Phase 1 (after T001):**
```
T002, T003, T004, T005, T006 - All enums and domain models can be created in parallel
```

**Phase 2 (all marked [P]):**
```
T007, T008 - Repository interfaces in parallel
T009, T010 - Entities in parallel
T011, T012 - Mappers in parallel
T013, T014 - Converters in parallel
```

**Phase 3 (DTOs):**
```
T017, T018, T019 - All DTOs can be created in parallel
```

**Phase 4 (DTOs):**
```
T023, T024 - Create and Delete request DTOs in parallel
```

**Phase 5 (DTOs):**
```
T031, T032, T033 - All template DTOs in parallel
```

**Phase 6 (DTOs):**
```
T037, T038, T039 - All template request DTOs in parallel
```

---

## Implementation Strategy

### MVP First (P0 Only)

1. Complete Phase 1: Setup (migration, enums, domain models)
2. Complete Phase 2: Foundational (repository layer)
3. Complete Phase 3: US1 & US2 (list & get reports)
4. **STOP and VALIDATE**: Test report list and detail APIs
5. Deploy/demo if ready - Frontend can already show reports

### P0 + P1 Delivery

1. Complete Setup + Foundational + Phase 3 → P0 ready
2. Complete Phase 4 → Full report management (no update per design)
3. **VALIDATE**: Test all report APIs
4. Deploy - Frontend can now create/delete reports

### Full Feature Delivery

1. Complete all P0 + P1 phases
2. Complete Phase 5 & 6 → Template management
3. Complete Phase 7 → Polish
4. **VALIDATE**: All APIs working per quickstart.md

---

## Summary

| Phase | User Stories | Priority | Task Count |
|-------|--------------|----------|------------|
| Phase 1 | Setup | - | 6 |
| Phase 2 | Foundational | - | 10 |
| Phase 3 | US1, US2 | P0 | 6 |
| Phase 4 | US3, US4 | P1 | 8 |
| Phase 5 | US5, US6 | P2 | 6 |
| Phase 6 | US7, US8, US9 | P2 | 12 |
| Phase 7 | Polish | - | 3 |
| **Total** | | | **51** |

**MVP Scope**: Phase 1 + Phase 2 + Phase 3 = 22 tasks
**P1 Scope**: MVP + Phase 4 = 30 tasks
**Full Scope**: All phases = 51 tasks

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Report entity is immutable - no update API per design
- Template entity supports full CRUD with optimistic locking
- All APIs follow POST-Only pattern per constitution
- Pagination follows PageableRequest/PageResult pattern
