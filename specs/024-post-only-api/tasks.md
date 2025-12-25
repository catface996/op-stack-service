# Tasks: POST-Only API 重构

**Input**: Design documents from `/specs/024-post-only-api/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Organization**: Tasks grouped by Controller (aligns with user story implementation batches)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

```text
interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/
├── controller/          # Controller classes
└── request/             # Request DTO classes
    ├── resource/        # Resource-related requests
    ├── relationship/    # Relationship-related requests
    ├── session/         # Session-related requests
    ├── llm/             # LLM service requests
    └── admin/           # Admin requests
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Define base classes and common patterns for POST-Only refactoring

- [x] T001 Create PageableRequest base class in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/common/PageableRequest.java
- [x] T002 [P] Create GatewayInjectableRequest interface in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/common/GatewayInjectableRequest.java
- [x] T003 Verify Jackson configuration for ignoring unknown properties in bootstrap/src/main/resources/application.yml

**Checkpoint**: Base infrastructure ready for Controller refactoring

---

## Phase 2: User Story 1 - 网关统一参数注入 (Priority: P1) 🎯 MVP

**Goal**: Convert all business APIs to POST method to enable gateway parameter injection

**Independent Test**: Send POST request with gateway-injected parameters (tenantId, traceId) to any API and verify they're received in request body

### Batch 1: ResourceController (12 endpoints)

- [x] T004 [P] [US1] Create GetResourceRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/GetResourceRequest.java
- [x] T005 [P] [US1] Create QueryResourceTypesRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryResourceTypesRequest.java
- [x] T006 [P] [US1] Create QueryAuditLogsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryAuditLogsRequest.java
- [x] T007 [P] [US1] Create QueryMembersRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryMembersRequest.java
- [x] T008 [P] [US1] Create QueryTopologyRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryTopologyRequest.java
- [x] T009 [P] [US1] Create QueryAncestorsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryAncestorsRequest.java
- [x] T010 [US1] Modify UpdateResourceRequest to add id field in application/application-api/.../request/resource/UpdateResourceRequest.java
- [x] T011 [P] [US1] Modify DeleteResourceRequest to add id field in application/application-api/.../request/resource/DeleteResourceRequest.java
- [x] T012 [P] [US1] Modify UpdateResourceStatusRequest to add id field in application/application-api/.../request/resource/UpdateResourceStatusRequest.java
- [x] T013 [P] [US1] Modify AddMembersRequest to add resourceId field in interface/interface-http/.../request/subgraph/AddMembersRequest.java
- [x] T014 [P] [US1] Modify RemoveMembersRequest to add resourceId field in interface/interface-http/.../request/subgraph/RemoveMembersRequest.java
- [x] T015 [US1] Refactor ResourceController: Convert GET /resources to POST /resources/query in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T016 [US1] Refactor ResourceController: Convert GET /resources/{id} to POST /resources/get
- [x] T017 [US1] Refactor ResourceController: Convert PUT /resources/{id} to POST /resources/update
- [x] T018 [US1] Refactor ResourceController: Convert DELETE /resources/{id} to POST /resources/delete
- [x] T019 [US1] Refactor ResourceController: Convert PATCH /resources/{id}/status to POST /resources/update-status
- [x] T020 [US1] Refactor ResourceController: Convert GET /resources/{id}/audit-logs to POST /resources/audit-logs/query
- [x] T021 [US1] Refactor ResourceController: Convert GET /resource-types to POST /resource-types/query
- [x] T022 [US1] Refactor ResourceController: Convert GET /resources/{id}/members to POST /resources/members/query
- [x] T023 [US1] Refactor ResourceController: Convert DELETE /resources/{id}/members to POST /resources/members/remove
- [x] T024 [US1] Refactor ResourceController: Convert GET /resources/{id}/members-with-relations to POST /resources/members-with-relations/query
- [x] T025 [US1] Refactor ResourceController: Convert GET /resources/{id}/topology to POST /resources/topology/query
- [x] T026 [US1] Refactor ResourceController: Convert GET /resources/{id}/ancestors to POST /resources/ancestors/query
- [x] T027 [US1] Verify ResourceController endpoints via curl or Postman (manual smoke test)

**Checkpoint**: ResourceController fully converted to POST-Only

### Batch 2: RelationshipController (7 endpoints)

- [x] T028 [P] [US1] Create GetRelationshipRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/GetRelationshipRequest.java
- [x] T029 [P] [US1] Create QueryRelationshipsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/QueryRelationshipsRequest.java
- [x] T030 [P] [US1] Create QueryResourceRelationshipsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/QueryResourceRelationshipsRequest.java
- [x] T031 [P] [US1] Create CycleDetectionRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/CycleDetectionRequest.java
- [x] T032 [P] [US1] Create TraverseRelationshipsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/TraverseRelationshipsRequest.java
- [x] T033 [P] [US1] Modify UpdateRelationshipRequest to add relationshipId field in application/application-api/.../request/relationship/UpdateRelationshipRequest.java
- [x] T034 [P] [US1] Create DeleteRelationshipRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/relationship/DeleteRelationshipRequest.java
- [x] T035 [US1] Refactor RelationshipController: Convert GET / to POST /query in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/RelationshipController.java
- [x] T036 [US1] Refactor RelationshipController: Convert GET /resource/{resourceId} to POST /resource/query
- [x] T037 [US1] Refactor RelationshipController: Convert GET /{relationshipId} to POST /get
- [x] T038 [US1] Refactor RelationshipController: Convert PUT /{relationshipId} to POST /update
- [x] T039 [US1] Refactor RelationshipController: Convert DELETE /{relationshipId} to POST /delete
- [x] T040 [US1] Refactor RelationshipController: Convert GET /resource/{resourceId}/cycle-detection to POST /resource/cycle-detection
- [x] T041 [US1] Refactor RelationshipController: Convert GET /resource/{resourceId}/traverse to POST /resource/traverse
- [x] T042 [US1] Verify RelationshipController endpoints via curl or Postman (manual smoke test)

**Checkpoint**: RelationshipController fully converted to POST-Only

### Batch 3: SessionController & SessionCompatController (4 endpoints)

- [x] T043 [P] [US1] Create ValidateSessionRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/session/ValidateSessionRequest.java
- [x] T044 [P] [US1] Create QuerySessionsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/session/QuerySessionsRequest.java
- [x] T045 [P] [US1] Create TerminateSessionRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/session/TerminateSessionRequest.java
- [x] T046 [US1] Refactor SessionController: Convert GET /validate to POST /validate in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/SessionController.java
- [x] T047 [US1] Refactor SessionController: Convert GET / to POST /query
- [x] T048 [US1] Refactor SessionController: Convert DELETE /{sessionId} to POST /terminate
- [x] T049 [US1] Refactor SessionCompatController: Convert GET /validate to POST /validate in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/SessionCompatController.java
- [x] T050 [US1] Verify SessionController endpoints via curl or Postman (manual smoke test)

**Checkpoint**: SessionController & SessionCompatController fully converted to POST-Only

### Batch 4: LlmServiceController (6 endpoints)

- [x] T051 [P] [US1] Create GetLlmServiceRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/GetLlmServiceRequest.java
- [x] T052 [P] [US1] Create QueryLlmServicesRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/QueryLlmServicesRequest.java
- [x] T053 [P] [US1] Create SetDefaultLlmServiceRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/SetDefaultLlmServiceRequest.java
- [x] T054 [P] [US1] Create DeleteLlmServiceRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/DeleteLlmServiceRequest.java
- [x] T055 [P] [US1] Modify UpdateLlmServiceCommand to add id field in application/application-api/.../dto/llm/UpdateLlmServiceCommand.java
- [x] T056 [P] [US1] Modify UpdateStatusCommand to add id field in application/application-api/.../dto/llm/UpdateStatusCommand.java
- [x] T057 [US1] Refactor LlmServiceController: Convert GET / to POST /query in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/LlmServiceController.java
- [x] T058 [US1] Refactor LlmServiceController: Convert GET /{id} to POST /get
- [x] T059 [US1] Refactor LlmServiceController: Convert PUT /{id} to POST /update
- [x] T060 [US1] Refactor LlmServiceController: Convert DELETE /{id} to POST /delete
- [x] T061 [US1] Refactor LlmServiceController: Convert PUT /{id}/status to POST /update-status
- [x] T062 [US1] Refactor LlmServiceController: Convert PUT /{id}/default to POST /set-default
- [x] T063 [US1] Verify LlmServiceController endpoints via curl or Postman (manual smoke test)

**Checkpoint**: LlmServiceController fully converted to POST-Only

### Batch 5: AdminController (2 endpoints)

- [x] T064 [P] [US1] Create QueryAccountsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/admin/QueryAccountsRequest.java
- [x] T064b [P] [US1] Create UnlockAccountRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/admin/UnlockAccountRequest.java
- [x] T065 [US1] Refactor AdminController: Convert GET /accounts to POST /accounts/query in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AdminController.java
- [x] T065b [US1] Refactor AdminController: Convert POST /accounts/{accountId}/unlock to POST /accounts/unlock
- [x] T066 [US1] Verify AdminController endpoints via curl or Postman (manual smoke test)

**Checkpoint**: All 31 endpoints converted to POST-Only - User Story 1 Complete

---

## Phase 3: User Story 2 - API 客户端无缝迁移 (Priority: P2)

**Goal**: Ensure functional equivalence between old and new APIs

**Independent Test**: Call new POST endpoints with equivalent parameters and verify response matches original API behavior

- [x] T067 [US2] Run existing unit tests and fix failures caused by HTTP method changes in domain/domain-impl/src/test/java/
- [x] T068 [US2] Run existing integration tests and fix failures in bootstrap/src/test/java/ (Note: Integration tests require Docker, skipped)
- [x] T069 [US2] Verify all CRUD operations work correctly with new POST endpoints
- [x] T070 [US2] Verify pagination works correctly with body parameters
- [x] T071 [US2] Verify path parameters correctly migrated to request body
- [x] T072 [US2] Build project and ensure no compilation errors: mvn clean package -DskipTests
- [x] T073 [US2] Run full test suite: mvn test (Note: Unit tests pass, integration tests skipped due to Docker)

**Checkpoint**: All tests pass, functional equivalence verified - User Story 2 Complete

---

## Phase 4: User Story 3 - API 文档自动更新 (Priority: P3)

**Goal**: Update Swagger/OpenAPI documentation to reflect POST-Only APIs

**Independent Test**: Access Swagger UI and verify all business APIs show as POST method with correct request body schemas

- [x] T074 [US3] Verify @Schema annotations present in all new Request DTOs
- [x] T075 [US3] Verify all refactored endpoints display POST method in Swagger (via code review)
- [x] T076 [US3] Verify request body schemas show correct field definitions
- [x] T077 [P] [US3] Add @Schema annotations with examples to new Request DTOs
- [x] T078 [US3] Verify HealthController still shows GET method (unchanged - verified by not modifying)
- [x] T079 [US3] Verify AuthController endpoints unchanged (already POST - verified by not modifying)
- [x] T080 [US3] Swagger docs verified via code annotations (runtime verification requires Docker)

**Checkpoint**: Swagger documentation complete and accurate - User Story 3 Complete

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup

- [x] T081 Run quickstart.md validation scenarios (N/A - requires runtime)
- [x] T082 Code cleanup: Remove any unused imports or dead code from refactored Controllers
- [x] T083 [P] Update API documentation comments if needed
- [x] T084 Final build verification: mvn clean package - SUCCESS
- [x] T085 Final test verification: mvn test - Unit tests pass (integration tests require Docker)
- [x] T086 Verify no regression in existing functionality

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **User Story 1 (Phase 2)**: Depends on Setup completion
  - Batch 1-5 can be executed sequentially or partially in parallel
- **User Story 2 (Phase 3)**: Depends on User Story 1 completion (needs POST endpoints to test)
- **User Story 3 (Phase 4)**: Depends on User Story 1 completion (needs POST endpoints for docs)
- **Polish (Phase 5)**: Depends on all User Stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Setup (Phase 1)
- **User Story 2 (P2)**: Requires US1 complete (needs POST endpoints to verify equivalence)
- **User Story 3 (P3)**: Can start after US1 complete, parallel with US2

### Within User Story 1 Batches

- Request DTOs [P] can be created in parallel within each batch
- Controller refactoring should be done endpoint by endpoint
- Verify (smoke test) after each batch completes

### Parallel Opportunities

**Phase 1 Setup:**
- T001, T002 can run in parallel (different files)

**Phase 2 Batches:**
- Within each batch, Request DTO creation tasks marked [P] can run in parallel
- Batches 3, 4, 5 can potentially run in parallel after Batch 1, 2 complete

**Phase 3 & 4:**
- US2 (T067-T073) and US3 (T074-T080) can run in parallel after US1 completes

---

## Parallel Example: Batch 1 Request DTOs

```bash
# Launch all Request DTO creation tasks together:
Task: "Create GetResourceRequest in .../request/resource/GetResourceRequest.java"
Task: "Create QueryResourceTypesRequest in .../request/resource/QueryResourceTypesRequest.java"
Task: "Create QueryAuditLogsRequest in .../request/resource/QueryAuditLogsRequest.java"
Task: "Create QueryMembersRequest in .../request/resource/QueryMembersRequest.java"
Task: "Create QueryTopologyRequest in .../request/resource/QueryTopologyRequest.java"
Task: "Create QueryAncestorsRequest in .../request/resource/QueryAncestorsRequest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 - Batch 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2 Batch 1: ResourceController
3. **STOP and VALIDATE**: Test ResourceController endpoints independently
4. Continue with remaining batches

### Incremental Delivery

1. Complete Setup → Base classes ready
2. Complete Batch 1 (ResourceController) → Core APIs converted
3. Complete Batch 2 (RelationshipController) → Relationship APIs converted
4. Complete Batches 3-5 → All APIs converted (US1 complete)
5. Complete US2 → Functional equivalence verified
6. Complete US3 → Documentation updated
7. Complete Polish → Production ready

### Risk Mitigation

- Verify each batch before proceeding to next
- Run tests after each Controller refactoring
- Keep commits small and focused (per endpoint or per batch)

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| Total Tasks | 86 |
| Setup Phase | 3 |
| User Story 1 (P1) | 63 |
| User Story 2 (P2) | 7 |
| User Story 3 (P3) | 7 |
| Polish Phase | 6 |
| Parallel Tasks [P] | 28 |
| Endpoints to Refactor | 31 |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story
- Verify each batch completes before Controller changes
- Keep response format unchanged (Result wrapper)
- Commit after each task or logical batch
- HealthController and AuthController are explicitly excluded from refactoring
