# Tasks: 移除 Resource 资源管理接口

**Input**: Design documents from `/specs/001-remove-resource-api/`
**Prerequisites**: plan.md (required), spec.md (required), research.md

**Tests**: Not required - this is a code removal task

**Organization**: Tasks grouped by user story to enable independent verification

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2)
- Include exact file paths in descriptions

## Path Conventions

- **Project type**: DDD multi-module architecture
- **Base path**: Repository root

---

## Phase 1: Setup

**Purpose**: Backup and preparation before deletion

- [X] T001 Verify current branch is `001-remove-resource-api`
- [X] T002 Ensure project compiles before starting: `mvn clean compile -q`

---

## Phase 2: User Story 1 - 移除 Resource 接口代码 (Priority: P1)

**Goal**: 删除 ResourceController 及所有相关代码，项目编译成功

**Independent Test**: `mvn clean compile -q` 成功，无编译错误

### Interface Layer (3 files)

- [X] T003 [P] [US1] Delete `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java`
- [X] T004 [P] [US1] Delete `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/GetResourceRequest.java`
- [X] T005 [P] [US1] Delete `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryResourceTypesRequest.java`

### Application Layer - DTOs (7 files)

- [X] T006 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/ResourceDTO.java`
- [X] T007 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/ResourceTypeDTO.java`
- [X] T008 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/CreateResourceRequest.java`
- [X] T009 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/DeleteResourceRequest.java`
- [X] T010 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/ListResourcesRequest.java`
- [X] T011 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/UpdateResourceRequest.java`
- [X] T012 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/UpdateResourceStatusRequest.java`

### Application Layer - Services (2 files)

- [X] T013 [P] [US1] Delete `application/application-api/src/main/java/com/catface996/aiops/application/api/service/resource/ResourceApplicationService.java`
- [X] T014 [P] [US1] Delete `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/resource/ResourceApplicationServiceImpl.java`

### Domain Layer - Models (4 files)

- [X] T015 [P] [US1] Delete `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/resource/Resource.java`
- [X] T016 [P] [US1] Delete `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/resource/ResourceStatus.java`
- [X] T017 [P] [US1] Delete `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/resource/ResourceType.java`
- [X] T018 [P] [US1] Delete `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/resource/OperationType.java`

### Domain Layer - Services & Constants (3 files)

- [X] T019 [P] [US1] Delete `domain/domain-api/src/main/java/com/catface996/aiops/domain/constant/ResourceTypeConstants.java`
- [X] T020 [P] [US1] Delete `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/resource/ResourceDomainService.java`
- [X] T021 [P] [US1] Delete `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/resource/ResourceDomainServiceImpl.java`

### Repository Layer - Interfaces (2 files)

- [X] T022 [P] [US1] Delete `domain/repository-api/src/main/java/com/catface996/aiops/repository/resource/ResourceRepository.java`
- [X] T023 [P] [US1] Delete `domain/repository-api/src/main/java/com/catface996/aiops/repository/resource/ResourceTypeRepository.java`

### Infrastructure Layer - Repository Impl (2 files)

- [X] T024 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/resource/ResourceRepositoryImpl.java`
- [X] T025 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/resource/ResourceTypeRepositoryImpl.java`

### Infrastructure Layer - Mappers (2 files)

- [X] T026 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/resource/ResourceMapper.java`
- [X] T027 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/resource/ResourceTypeMapper.java`

### Infrastructure Layer - PO (2 files)

- [X] T028 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/resource/ResourcePO.java`
- [X] T029 [P] [US1] Delete `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/resource/ResourceTypePO.java`

### Cache Layer (2 files)

- [X] T030 [P] [US1] Delete `domain/cache-api/src/main/java/com/catface996/aiops/infrastructure/cache/api/service/ResourceCacheService.java`
- [X] T031 [P] [US1] Delete `infrastructure/cache/redis-impl/src/main/java/com/catface996/aiops/infrastructure/cache/redis/service/ResourceCacheServiceImpl.java`

### Verification

- [X] T032 [US1] Compile project to verify no errors: `mvn clean compile -q`
- [X] T033 [US1] Fix any compilation errors from remaining dependencies

**Checkpoint**: US1 complete - all Resource code removed, project compiles successfully

---

## Phase 3: User Story 2 - 清理 Resource 数据库表结构 (Priority: P2)

**Goal**: 删除 resource 和 resource_type 数据库表

**Independent Test**: 服务启动成功，Flyway 迁移执行成功

### Database Migration

- [X] T034 [US2] Create migration script `bootstrap/src/main/resources/db/migration/V23__Drop_resource_tables.sql`

### Verification

- [X] T035 [US2] Build project: `mvn clean package -DskipTests -q`
- [ ] T036 [US2] Start service and verify V23 migration executes successfully
- [ ] T037 [US2] Verify Node API still works: `curl -X POST http://localhost:8081/api/service/v1/nodes/query -H "Content-Type: application/json" -d '{"page":1,"size":10}'`

**Checkpoint**: US2 complete - database tables dropped, service runs normally

---

## Phase 4: Polish & Final Verification

**Purpose**: Final cleanup and verification

- [X] T038 Clean up empty directories after file deletion
- [ ] T039 Verify Resource API returns 404: `curl -X POST http://localhost:8081/api/service/v1/resources/query -H "Content-Type: application/json" -d '{}'`
- [X] T040 Run full build: `mvn clean package -DskipTests`
- [ ] T041 Commit changes with message: "feat: 移除 Resource 资源管理接口"

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - verify project state
- **Phase 2 (US1)**: Depends on Phase 1 - delete all code files
- **Phase 3 (US2)**: Depends on Phase 2 completion - create migration after code removal
- **Phase 4 (Polish)**: Depends on Phase 3 - final verification

### User Story Dependencies

- **US1 (P1)**: No dependencies - can start after setup
- **US2 (P2)**: MUST complete after US1 - database migration requires code removal first

### Within US1 (Parallel Deletion)

All deletion tasks (T003-T031) can run in parallel as they are independent file deletions:

```bash
# All these tasks can run simultaneously:
rm -f interface/.../ResourceController.java
rm -f interface/.../GetResourceRequest.java
rm -f application/.../ResourceDTO.java
# ... etc
```

---

## Parallel Example: User Story 1 File Deletions

```bash
# Launch all file deletions together (all marked [P]):
# Interface Layer (T003-T005)
# Application Layer (T006-T014)
# Domain Layer (T015-T023)
# Infrastructure Layer (T024-T029)
# Cache Layer (T030-T031)

# Then verify:
mvn clean compile -q
```

---

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1: Setup verification
2. Complete Phase 2: US1 - Delete all code files
3. **STOP and VALIDATE**: `mvn clean compile` succeeds
4. Can deploy with code removed, tables still present

### Full Cleanup

1. Complete US1 → Project compiles
2. Complete US2 → Database tables dropped
3. Complete Phase 4 → Final verification and commit

---

## Summary

| Phase | User Story | Tasks | Files Affected |
|-------|------------|-------|----------------|
| 1 | Setup | 2 | 0 |
| 2 | US1 - 移除代码 | 31 | 29 deleted |
| 3 | US2 - 清理数据库 | 4 | 1 created |
| 4 | Polish | 4 | 0 |
| **Total** | | **41** | **30** |

---

## Notes

- [P] tasks = can run in parallel (all file deletions)
- [US1] = User Story 1: 移除 Resource 接口代码
- [US2] = User Story 2: 清理 Resource 数据库表结构
- US2 MUST wait for US1 to complete (database depends on code removal)
- Commit after T041 with proper commit message
