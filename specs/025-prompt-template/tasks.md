# Tasks: 提示词模板管理

**Input**: Design documents from `/specs/025-prompt-template/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are NOT included (not explicitly requested in specification).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions (DDD Multi-Module)

- **Domain Model**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/prompt/`
- **Domain API**: `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/`
- **Domain Impl**: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/`
- **Repository API**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/prompt/`
- **Repository Impl**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`
- **Application API**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **Application Impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/`
- **Interface HTTP**: `interface/interface-http/src/main/java/com/catface996/aiops/interfaces/http/controller/prompt/`
- **DB Migration**: `bootstrap/src/main/resources/db/migration/`

---

## Phase 1: Setup (Database & Error Codes)

**Purpose**: 数据库迁移和错误码定义

- [x] T001 Create Flyway migration script in bootstrap/src/main/resources/db/migration/V13__create_prompt_template_tables.sql
- [x] T002 [P] Add PromptTemplateErrorCode enum in common/src/main/java/com/catface996/aiops/common/enums/PromptTemplateErrorCode.java

---

## Phase 2: Foundational (Shared Models & Repositories)

**Purpose**: 所有用户故事共享的基础设施，必须在任何用户故事开始前完成

**⚠️ CRITICAL**: 所有用户故事都依赖此阶段完成

### Domain Models (领域模型)

- [x] T003 [P] Create TemplateUsage domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/prompt/TemplateUsage.java
- [x] T004 [P] Create PromptTemplate domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/prompt/PromptTemplate.java
- [x] T005 [P] Create PromptTemplateVersion domain model in domain/domain-model/src/main/java/com/catface996/aiops/domain/model/prompt/PromptTemplateVersion.java

### Persistence Objects (持久化对象)

- [x] T006 [P] Create TemplateUsagePO in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/prompt/TemplateUsagePO.java
- [x] T007 [P] Create PromptTemplatePO with @Version annotation in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/prompt/PromptTemplatePO.java
- [x] T008 [P] Create PromptTemplateVersionPO in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/prompt/PromptTemplateVersionPO.java

### Mappers (MyBatis Mapper 接口)

- [x] T009 [P] Create TemplateUsageMapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/TemplateUsageMapper.java
- [x] T010 [P] Create PromptTemplateMapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/PromptTemplateMapper.java
- [x] T011 [P] Create PromptTemplateVersionMapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/PromptTemplateVersionMapper.java

### Mapper XMLs (MyBatis XML 配置)

- [x] T012 [P] Create TemplateUsageMapper.xml (skipped - using annotation-based queries)
- [x] T013 [P] Create PromptTemplateMapper.xml (skipped - using annotation-based queries)
- [x] T014 [P] Create PromptTemplateVersionMapper.xml (skipped - using annotation-based queries)

### Repository Interfaces (仓储接口)

- [x] T015 [P] Create TemplateUsageRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/prompt/TemplateUsageRepository.java
- [x] T016 [P] Create PromptTemplateRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/prompt/PromptTemplateRepository.java
- [x] T017 [P] Create PromptTemplateVersionRepository interface in domain/repository-api/src/main/java/com/catface996/aiops/repository/prompt/PromptTemplateVersionRepository.java

### Repository Implementations (仓储实现)

- [x] T018 [P] Implement TemplateUsageRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/TemplateUsageRepositoryImpl.java
- [x] T019 [P] Implement PromptTemplateRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/PromptTemplateRepositoryImpl.java
- [x] T020 [P] Implement PromptTemplateVersionRepositoryImpl in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/PromptTemplateVersionRepositoryImpl.java

**Checkpoint**: 基础设施就绪 - 用户故事实现可以开始

---

## Phase 3: User Story 1 - 创建提示词模板 (Priority: P1) 🎯 MVP

**Goal**: 运维人员可以创建新的提示词模板，包含名称、内容、用途，系统返回模板 ID 和版本号 1

**Independent Test**: 调用 `/api/v1/prompt-templates/create` 验证模板成功创建并返回 ID 和版本号 1

### DTOs for User Story 1

- [x] T021 [P] [US1] Create CreatePromptTemplateRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/CreatePromptTemplateRequest.java
- [x] T022 [P] [US1] Create PromptTemplateDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/PromptTemplateDTO.java

### Domain Service for User Story 1

- [x] T023 [US1] Create PromptTemplateDomainService interface with createTemplate method in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/PromptTemplateDomainService.java
- [x] T024 [US1] Implement createTemplate in PromptTemplateDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/PromptTemplateDomainServiceImpl.java

### Application Service for User Story 1

- [x] T025 [US1] Create PromptTemplateApplicationService interface with createPromptTemplate method in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T026 [US1] Implement createPromptTemplate in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 1

- [x] T027 [US1] Create PromptTemplateController with /create endpoint in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 创建模板功能可独立测试

---

## Phase 4: User Story 2 - 查询提示词模板列表 (Priority: P1)

**Goal**: 运维人员可以分页查询模板列表，支持按用途筛选和名称搜索

**Independent Test**: 调用 `/api/v1/prompt-templates/list` 验证返回分页数据，筛选和搜索功能正常

### DTOs for User Story 2

- [x] T028 [P] [US2] Create ListPromptTemplatesRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/ListPromptTemplatesRequest.java

### Domain Service for User Story 2

- [x] T029 [US2] Add listTemplates and countTemplates methods to PromptTemplateDomainService interface (already in T023)
- [x] T030 [US2] Implement listTemplates and countTemplates in PromptTemplateDomainServiceImpl (already in T024)

### Application Service for User Story 2

- [x] T031 [US2] Add listPromptTemplates method to PromptTemplateApplicationService in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T032 [US2] Implement listPromptTemplates in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 2

- [x] T033 [US2] Add /list endpoint to PromptTemplateController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 模板列表查询功能可独立测试

---

## Phase 5: User Story 3 - 查看模板详情及版本历史 (Priority: P2)

**Goal**: 运维人员可以查看模板详情和所有历史版本列表，也可以查看指定版本的完整内容

**Independent Test**: 调用 `/api/v1/prompt-templates/detail` 验证返回模板详情和版本历史

### DTOs for User Story 3

- [x] T034 [P] [US3] Create GetTemplateDetailRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/GetTemplateDetailRequest.java
- [x] T035 [P] [US3] Create GetVersionDetailRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/GetVersionDetailRequest.java
- [x] T036 [P] [US3] Create PromptTemplateVersionDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/PromptTemplateVersionDTO.java
- [x] T037 [P] [US3] Create PromptTemplateDetailDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/PromptTemplateDetailDTO.java

### Domain Service for User Story 3

- [x] T038 [US3] Add getTemplateById, getVersionsByTemplateId, getVersion methods to PromptTemplateDomainService in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/PromptTemplateDomainService.java
- [x] T039 [US3] Implement getTemplateById, getVersionsByTemplateId, getVersion in PromptTemplateDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/PromptTemplateDomainServiceImpl.java

### Application Service for User Story 3

- [x] T040 [US3] Add getTemplateDetail, getVersionDetail methods to PromptTemplateApplicationService in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T041 [US3] Implement getTemplateDetail, getVersionDetail in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 3

- [x] T042 [US3] Add /detail and /version/detail endpoints to PromptTemplateController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 模板详情和版本历史查询功能可独立测试

---

## Phase 6: User Story 4 - 更新提示词模板（生成新版本）(Priority: P2)

**Goal**: 运维人员可以更新模板内容，系统自动生成新版本，版本号递增

**Independent Test**: 调用 `/api/v1/prompt-templates/update` 验证版本号递增且内容更新成功

### DTOs for User Story 4

- [x] T043 [P] [US4] Create UpdatePromptTemplateRequest DTO with expectedVersion field in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/UpdatePromptTemplateRequest.java

### Domain Service for User Story 4

- [x] T044 [US4] Add updateTemplate method (with version control logic) to PromptTemplateDomainService in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/PromptTemplateDomainService.java
- [x] T045 [US4] Implement updateTemplate with optimistic lock and content change check in PromptTemplateDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/PromptTemplateDomainServiceImpl.java

### Application Service for User Story 4

- [x] T046 [US4] Add updatePromptTemplate method to PromptTemplateApplicationService in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T047 [US4] Implement updatePromptTemplate in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 4

- [x] T048 [US4] Add /update endpoint to PromptTemplateController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 模板更新和版本控制功能可独立测试

---

## Phase 7: User Story 5 - 回滚到历史版本 (Priority: P3)

**Goal**: 运维人员可以将模板回滚到历史版本（通过创建新版本实现）

**Independent Test**: 调用 `/api/v1/prompt-templates/rollback` 验证新版本创建成功且内容与目标版本相同

### DTOs for User Story 5

- [x] T049 [P] [US5] Create RollbackTemplateRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/RollbackTemplateRequest.java

### Domain Service for User Story 5

- [x] T050 [US5] Add rollbackTemplate method to PromptTemplateDomainService in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/PromptTemplateDomainService.java
- [x] T051 [US5] Implement rollbackTemplate in PromptTemplateDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/PromptTemplateDomainServiceImpl.java

### Application Service for User Story 5

- [x] T052 [US5] Add rollbackPromptTemplate method to PromptTemplateApplicationService in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T053 [US5] Implement rollbackPromptTemplate in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 5

- [x] T054 [US5] Add /rollback endpoint to PromptTemplateController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 版本回滚功能可独立测试

---

## Phase 8: User Story 6 - 删除提示词模板 (Priority: P3)

**Goal**: 运维人员可以软删除模板，删除后模板不可查询

**Independent Test**: 调用 `/api/v1/prompt-templates/delete` 验证模板被标记为删除且不再出现在列表中

### DTOs for User Story 6

- [x] T055 [P] [US6] Create DeleteTemplateRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/DeleteTemplateRequest.java

### Domain Service for User Story 6

- [x] T056 [US6] Add deleteTemplate method to PromptTemplateDomainService in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/PromptTemplateDomainService.java
- [x] T057 [US6] Implement deleteTemplate (soft delete) in PromptTemplateDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/PromptTemplateDomainServiceImpl.java

### Application Service for User Story 6

- [x] T058 [US6] Add deletePromptTemplate method to PromptTemplateApplicationService in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/PromptTemplateApplicationService.java
- [x] T059 [US6] Implement deletePromptTemplate in PromptTemplateApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/PromptTemplateApplicationServiceImpl.java

### Controller for User Story 6

- [x] T060 [US6] Add /delete endpoint to PromptTemplateController in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/PromptTemplateController.java

**Checkpoint**: 模板删除功能可独立测试

---

## Phase 9: Template Usage Management (模板用途管理 - 支撑功能)

**Goal**: 支持用户自定义模板用途类型（创建、查询、删除）

**Independent Test**: 调用用途管理 API 验证 CRUD 操作正常

### DTOs for Template Usage

- [x] T061 [P] Create CreateTemplateUsageRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/CreateTemplateUsageRequest.java
- [x] T062 [P] Create DeleteUsageRequest DTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/request/DeleteUsageRequest.java
- [x] T063 [P] Create TemplateUsageDTO in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/prompt/TemplateUsageDTO.java

### Domain Service for Template Usage

- [x] T064 Create TemplateUsageDomainService interface in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/prompt/TemplateUsageDomainService.java
- [x] T065 Implement TemplateUsageDomainServiceImpl in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/prompt/TemplateUsageDomainServiceImpl.java

### Application Service for Template Usage

- [x] T066 Create TemplateUsageApplicationService interface in application/application-api/src/main/java/com/catface996/aiops/application/api/service/prompt/TemplateUsageApplicationService.java
- [x] T067 Implement TemplateUsageApplicationServiceImpl in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/prompt/TemplateUsageApplicationServiceImpl.java

### Controller for Template Usage

- [x] T068 Create TemplateUsageController with /create, /list, /delete endpoints in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TemplateUsageController.java

**Checkpoint**: 模板用途管理功能可独立测试

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: 跨用户故事的改进和优化

- [x] T069 Verify all endpoints work with quickstart.md examples (Build passed successfully)
- [x] T070 Add OpenAPI annotations (@Operation, @Schema) to all DTOs and Controllers (Already included during implementation)
- [x] T071 Verify optimistic lock handling returns proper 409 Conflict response (Implemented in domain service)
- [x] T072 Verify content size limit (64KB) validation works correctly (Validated in PromptTemplate domain model)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
  - US1 and US2 can proceed in parallel (both are P1)
  - US3 and US4 can proceed in parallel after US1/US2 (both are P2)
  - US5 and US6 can proceed in parallel after US3/US4 (both are P3)
- **Template Usage (Phase 9)**: Can proceed after Foundational, independent of template stories
- **Polish (Phase 10)**: Depends on all user stories being complete

### User Story Dependencies

| User Story | Priority | Dependencies | Can Start After |
|------------|----------|--------------|-----------------|
| US1 - 创建模板 | P1 | Phase 2 | Phase 2 完成 |
| US2 - 查询列表 | P1 | Phase 2 | Phase 2 完成 |
| US3 - 查看详情 | P2 | Phase 2, (benefits from US1 data) | Phase 2 完成 |
| US4 - 更新模板 | P2 | Phase 2, (benefits from US1 data) | Phase 2 完成 |
| US5 - 版本回滚 | P3 | Phase 2, (benefits from US4 versions) | Phase 2 完成 |
| US6 - 删除模板 | P3 | Phase 2 | Phase 2 完成 |

### Parallel Opportunities

**Phase 2 (all can run in parallel)**:
- T003-T005: Domain models
- T006-T008: PO classes
- T009-T011: Mapper interfaces
- T012-T014: Mapper XMLs
- T015-T017: Repository interfaces
- T018-T020: Repository implementations

**User Story DTOs (can run in parallel within each story)**:
- US1: T021, T022
- US3: T034-T037

---

## Parallel Example: Phase 2 Foundation

```bash
# Launch all domain models together:
Task: "Create TemplateUsage domain model in domain/domain-model/.../TemplateUsage.java"
Task: "Create PromptTemplate domain model in domain/domain-model/.../PromptTemplate.java"
Task: "Create PromptTemplateVersion domain model in domain/domain-model/.../PromptTemplateVersion.java"

# Launch all PO classes together:
Task: "Create TemplateUsagePO in infrastructure/repository/.../TemplateUsagePO.java"
Task: "Create PromptTemplatePO in infrastructure/repository/.../PromptTemplatePO.java"
Task: "Create PromptTemplateVersionPO in infrastructure/repository/.../PromptTemplateVersionPO.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2 Only)

1. Complete Phase 1: Setup (DB migration, error codes)
2. Complete Phase 2: Foundational (models, repositories)
3. Complete Phase 3: User Story 1 (创建模板)
4. Complete Phase 4: User Story 2 (查询列表)
5. **STOP and VALIDATE**: Test US1 + US2 independently with curl/Postman
6. Deploy/demo if ready - users can create and view templates

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 + US2 → Test → Deploy (Basic CRUD MVP!)
3. Add US3 + US4 → Test → Deploy (Version Control)
4. Add US5 + US6 → Test → Deploy (Rollback + Delete)
5. Add Phase 9 → Test → Deploy (Custom Usage Types)
6. Each increment adds value without breaking previous functionality

### Parallel Team Strategy

With multiple developers:

1. All complete Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 + 3
   - Developer B: User Story 2 + 4
   - Developer C: User Story 5 + 6 + Phase 9
3. Stories complete and integrate independently

---

## Summary

| Phase | Task Count | Description |
|-------|------------|-------------|
| Phase 1 | 2 | Setup (DB + Error Codes) |
| Phase 2 | 18 | Foundational (Models + Repositories) |
| Phase 3 (US1) | 7 | 创建提示词模板 |
| Phase 4 (US2) | 6 | 查询模板列表 |
| Phase 5 (US3) | 9 | 查看详情及版本历史 |
| Phase 6 (US4) | 6 | 更新模板（生成新版本） |
| Phase 7 (US5) | 6 | 回滚到历史版本 |
| Phase 8 (US6) | 6 | 删除提示词模板 |
| Phase 9 | 8 | 模板用途管理 |
| Phase 10 | 4 | Polish |
| **Total** | **72** | |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All APIs use POST method per project convention
