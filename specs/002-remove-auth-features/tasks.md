# Tasks: 移除认证相关功能

**Input**: Design documents from `/specs/002-remove-auth-features/`
**Prerequisites**: plan.md (required), spec.md (required), research.md

**Tests**: 本功能为代码清理任务，不需要编写新测试，但需要验证编译和 Swagger 文档。

**Organization**: 任务按用户故事组织，支持独立实现和测试。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

- **DDD 分层架构**:
  - `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/` - HTTP 接口层
  - `common/src/main/java/com/catface996/aiops/common/` - 通用模块
  - `application/application-api/src/main/java/com/catface996/aiops/application/api/` - 应用层 API
  - `application/application-impl/src/main/java/com/catface996/aiops/application/impl/` - 应用层实现
  - `bootstrap/src/main/java/com/catface996/aiops/bootstrap/` - 启动层

---

## Phase 1: Setup (准备工作)

**Purpose**: 确认清理范围，备份关键信息

- [x] T001 确认当前分支为 002-remove-auth-features
- [x] T002 运行 `mvn clean compile` 验证当前代码可编译

---

## Phase 2: Foundational (前置检查)

**Purpose**: 验证废弃代码的依赖关系，确保安全移除

**⚠️ CRITICAL**: 必须在开始移除前完成这些检查

- [x] T003 检查 AuthErrorCode 的所有引用，确认只在注释中使用
- [x] T004 [P] 检查 SessionErrorCode 的所有引用，确认无实际代码依赖
- [x] T005 [P] 检查 RefreshTokenResponse 的所有引用，确认无实际代码依赖

**Checkpoint**: 依赖检查完成 - 可以开始移除代码

---

## Phase 3: User Story 1 - 移除认证相关的代码文件 (Priority: P1) 🎯 MVP

**Goal**: 移除项目中认证相关的废弃代码文件和空目录

**Independent Test**: 编译成功，代码中不存在认证相关的废弃文件

### Implementation for User Story 1

#### 3.1 更新引用 AuthErrorCode 的注释文件

- [x] T006 [US1] 更新 BusinessException.java 中引用 AuthErrorCode 的示例注释，改用 ResourceErrorCode in common/src/main/java/com/catface996/aiops/common/exception/BusinessException.java
- [x] T007 [US1] 更新 BaseException.java 中引用 AuthErrorCode 的示例注释，改用 ResourceErrorCode in common/src/main/java/com/catface996/aiops/common/exception/BaseException.java
- [x] T008 [US1] 更新 ErrorCode.java 中引用 AuthErrorCode 的示例注释，改用 ResourceErrorCode in common/src/main/java/com/catface996/aiops/common/enums/ErrorCode.java
- [x] T009 [US1] 运行 `mvn clean compile` 验证注释更新后编译通过

#### 3.2 删除认证相关文件

- [x] T010 [P] [US1] 删除 RefreshTokenResponse.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/dto/auth/RefreshTokenResponse.java
- [x] T011 [P] [US1] 删除 AuthErrorCode.java in common/src/main/java/com/catface996/aiops/common/enums/AuthErrorCode.java
- [x] T012 [P] [US1] 删除 SessionErrorCode.java in common/src/main/java/com/catface996/aiops/common/enums/SessionErrorCode.java
- [x] T013 [US1] 运行 `mvn clean compile` 验证文件删除后编译通过

#### 3.3 删除空目录

- [x] T014 [P] [US1] 删除 controller/auth/ 目录（仅含 .gitkeep）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/auth/
- [x] T015 [P] [US1] 删除 dto/auth/ 目录 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/dto/auth/
- [x] T016 [P] [US1] 删除 service/auth/ 目录（仅含 .gitkeep）in application/application-api/src/main/java/com/catface996/aiops/application/api/service/auth/
- [x] T017 [P] [US1] 删除 command/auth/ 目录（仅含 .gitkeep）in application/application-api/src/main/java/com/catface996/aiops/application/api/command/auth/
- [x] T018 [P] [US1] 删除 service/auth/ 目录（仅含 .gitkeep）in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/auth/

**Checkpoint**: User Story 1 完成 - 认证相关代码文件已移除

---

## Phase 4: User Story 2 - 更新 Swagger 文档配置 (Priority: P2)

**Goal**: 更新 OpenApiConfig 中的 Swagger 文档描述，移除认证相关功能说明

**Independent Test**: Swagger UI 正常访问且不显示认证相关功能描述

### Implementation for User Story 2

- [x] T019 [US2] 更新 OpenApiConfig.java 移除"用户与认证"功能模块描述 in bootstrap/src/main/java/com/catface996/aiops/bootstrap/config/OpenApiConfig.java
- [x] T020 [US2] 更新 OpenApiConfig.java 中的认证方式说明，改为网关统一认证 in bootstrap/src/main/java/com/catface996/aiops/bootstrap/config/OpenApiConfig.java
- [x] T021 [US2] 运行 `mvn clean compile` 验证编译通过

**Checkpoint**: User Story 2 完成 - Swagger 文档已更新

---

## Phase 5: Polish & Verification (验证与清理)

**Purpose**: 最终验证和清理

- [x] T022 运行 `mvn clean package -DskipTests` 验证完整构建
- [x] T023 启动应用并访问 Swagger UI 验证文档不显示认证相关功能
- [x] T024 [P] 检查代码中是否还有 AuthErrorCode 或 SessionErrorCode 的引用
- [x] T025 [P] 更新 spec.md 状态为 Completed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - Can start after Phase 2
- **User Story 2 (Phase 4)**: Can run after User Story 1 or in parallel
- **Polish (Phase 5)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent of US1

### Within Each User Story

- Tasks T006-T009 must be sequential (update comments before deleting files)
- Tasks T010-T012 can run in parallel (different files)
- Tasks T014-T018 can run in parallel (different directories)

### Parallel Opportunities

- Phase 2: T003, T004, T005 can run in parallel (different files)
- Phase 3: T010, T011, T012 can run in parallel (different files)
- Phase 3: T014-T018 can all run in parallel (different directories)
- Phase 4 (US2) can run in parallel with Phase 3 (US1) after foundational checks

---

## Parallel Example: User Story 1 File Deletion

```bash
# Launch all delete tasks for User Story 1 files together:
Task: "Delete RefreshTokenResponse.java"
Task: "Delete AuthErrorCode.java"
Task: "Delete SessionErrorCode.java"
```

## Parallel Example: User Story 1 Directory Deletion

```bash
# Launch all directory delete tasks together:
Task: "Delete controller/auth/ directory"
Task: "Delete dto/auth/ directory"
Task: "Delete application-api service/auth/ directory"
Task: "Delete application-api command/auth/ directory"
Task: "Delete application-impl service/auth/ directory"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational checks
3. Complete Phase 3: User Story 1 (remove code files)
4. **STOP and VALIDATE**: Compile and verify no auth files remain
5. This alone delivers the core value - deprecated auth code is removed

### Incremental Delivery

1. Complete Setup + Foundational → Ready to clean
2. Add User Story 1 → Compile + Test → Core cleanup done
3. Add User Story 2 → Compile + Test → Swagger updated
4. Polish → Final verification

### Recommended Approach

Since this is a cleanup task with minimal dependencies:

1. **Execute Phase 1-3 first** (Setup + Foundational + US1)
2. **Execute Phase 4 (US2)** after or in parallel
3. **Execute Phase 5** for final verification

---

## Notes

- This is a **deletion-focused task** - no new code to write
- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- **Important**: Update comments BEFORE deleting files to avoid compilation errors
- Commit after each phase completion for easy rollback
- Total estimated tasks: 25
