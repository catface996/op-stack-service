# Tasks: 移除废弃的API接口

**Input**: Design documents from `/specs/001-remove-deprecated-api/`
**Prerequisites**: plan.md (required), spec.md (required), research.md

**Tests**: 本功能为代码清理任务，不需要编写新测试，但需要验证现有测试通过。

**Organization**: 任务按用户故事组织，支持独立实现和测试。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **DDD 分层架构**:
  - `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/` - HTTP 接口层
  - `common/src/main/java/com/catface996/aiops/common/` - 通用模块

---

## Phase 1: Setup (准备工作)

**Purpose**: 确认清理范围，备份关键信息

- [ ] T001 确认当前分支为 001-remove-deprecated-api
- [ ] T002 运行 `mvn clean compile` 验证当前代码可编译

---

## Phase 2: Foundational (前置检查)

**Purpose**: 验证废弃代码的依赖关系，确保安全移除

**⚠️ CRITICAL**: 必须在开始移除前完成这些检查

- [ ] T003 检查 ResourceController 中废弃方法的所有引用，确认只有 Controller 层调用
- [ ] T004 [P] 检查 ErrorCodes.java 的所有引用，确认只在注释中使用
- [ ] T005 [P] 检查 request/subgraph/ 目录下类的引用，确认只被废弃接口使用
- [ ] T006 [P] 检查 response/subgraph/ 目录下类的引用，确认只被废弃接口使用

**Checkpoint**: 依赖检查完成 - 可以开始移除代码

---

## Phase 3: User Story 1 - 清理废弃的资源成员管理接口 (Priority: P1) 🎯 MVP

**Goal**: 从 ResourceController 中移除 6 个废弃的成员管理接口方法

**Independent Test**: 编译成功，调用废弃接口返回 404，替代接口正常工作

### Implementation for User Story 1

- [ ] T007 [US1] 移除 ResourceController 中的 addMembers 方法（L303-333）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T008 [US1] 移除 ResourceController 中的 removeMembers 方法（L340-360）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T009 [US1] 移除 ResourceController 中的 queryMembers 方法（L367-395）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T010 [US1] 移除 ResourceController 中的 queryMembersWithRelations 方法（L402-425）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T011 [US1] 移除 ResourceController 中的 queryTopology 方法（L432-454）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T012 [US1] 移除 ResourceController 中的 queryAncestors 方法（L461-479）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T013 [US1] 移除 ResourceController 中废弃接口相关的 import 语句 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T014 [US1] 更新 ResourceController 类级别 Javadoc，移除废弃接口的文档说明 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T015 [US1] 移除 memberApplicationService 字段（如果不再被其他方法使用）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [ ] T016 [US1] 运行 `mvn clean compile` 验证编译通过

**Checkpoint**: User Story 1 完成 - ResourceController 废弃方法已移除

---

## Phase 4: User Story 2 - 清理废弃的错误码常量类 (Priority: P2)

**Goal**: 删除已废弃的 ErrorCodes 常量类

**Independent Test**: 编译成功，无 ErrorCodes 引用错误

### Implementation for User Story 2

- [ ] T017 [US2] 删除 ErrorCodes.java in common/src/main/java/com/catface996/aiops/common/constants/ErrorCodes.java
- [ ] T018 [US2] 更新 GlobalExceptionHandler.java 中引用 ErrorCodes 的注释（改为使用新错误码枚举的说明）in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/exception/GlobalExceptionHandler.java
- [ ] T019 [US2] 运行 `mvn clean compile` 验证编译通过

**Checkpoint**: User Story 2 完成 - ErrorCodes 常量类已移除

---

## Phase 5: User Story 3 - 清理废弃接口依赖的请求/响应类 (Priority: P3)

**Goal**: 删除专用于废弃接口的 Request/Response 类

**Independent Test**: 编译成功，所有测试通过

### Implementation for User Story 3

- [ ] T020 [P] [US3] 删除 AddMembersRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/subgraph/AddMembersRequest.java
- [ ] T021 [P] [US3] 删除 RemoveMembersRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/subgraph/RemoveMembersRequest.java
- [ ] T022 [P] [US3] 删除 QueryMembersRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryMembersRequest.java
- [ ] T023 [P] [US3] 删除 QueryMembersWithRelationsRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryMembersWithRelationsRequest.java
- [ ] T024 [P] [US3] 删除 QueryTopologyRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryTopologyRequest.java
- [ ] T025 [P] [US3] 删除 QueryAncestorsRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/resource/QueryAncestorsRequest.java
- [ ] T026 [P] [US3] 删除 SubgraphMemberListResponse.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphMemberListResponse.java
- [ ] T027 [P] [US3] 删除 SubgraphMembersWithRelationsResponse.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphMembersWithRelationsResponse.java
- [ ] T028 [P] [US3] 删除 TopologyGraphResponse.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/TopologyGraphResponse.java
- [ ] T029 [P] [US3] 删除 SubgraphAncestorsResponse.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphAncestorsResponse.java
- [ ] T030 [US3] 删除空的 request/subgraph/ 目录 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/subgraph/
- [ ] T031 [US3] 删除空的 response/subgraph/ 目录 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/
- [ ] T032 [US3] 运行 `mvn clean compile` 验证编译通过

**Checkpoint**: User Story 3 完成 - 废弃 Request/Response 类已移除

---

## Phase 6: Polish & Verification (验证与清理)

**Purpose**: 最终验证和清理

- [ ] T033 运行 `mvn clean package -DskipTests` 验证完整构建
- [ ] T034 运行 `mvn test` 验证所有测试通过
- [ ] T035 启动应用并访问 Swagger UI 验证废弃接口不再显示
- [ ] T036 验证 API 接口总数为 45 个（从 51 减少 6 个）
- [ ] T037 [P] 检查代码中是否还有 `@Deprecated(forRemoval = true)` 标记
- [ ] T038 [P] 更新 spec.md 状态为 Completed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - Can start after Phase 2
- **User Story 2 (Phase 4)**: Can run in parallel with User Story 1
- **User Story 3 (Phase 5)**: Depends on User Story 1 (需要先移除 Controller 方法才能删除其依赖的类)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent of US1
- **User Story 3 (P3)**: Depends on User Story 1 (request/response classes are used by controller methods)

### Within Each User Story

- Tasks T007-T012 can be executed sequentially (same file, similar edits)
- Tasks T020-T029 can run in parallel (different files)

### Parallel Opportunities

- Phase 2: T003, T004, T005, T006 can run in parallel (different files)
- Phase 4 (US2) can run in parallel with Phase 3 (US1)
- Phase 5 (US3) T020-T029 can all run in parallel

---

## Parallel Example: User Story 3

```bash
# Launch all delete tasks for User Story 3 together:
Task: "Delete AddMembersRequest.java"
Task: "Delete RemoveMembersRequest.java"
Task: "Delete QueryMembersRequest.java"
Task: "Delete QueryMembersWithRelationsRequest.java"
Task: "Delete QueryTopologyRequest.java"
Task: "Delete QueryAncestorsRequest.java"
Task: "Delete SubgraphMemberListResponse.java"
Task: "Delete SubgraphMembersWithRelationsResponse.java"
Task: "Delete TopologyGraphResponse.java"
Task: "Delete SubgraphAncestorsResponse.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational checks
3. Complete Phase 3: User Story 1 (remove controller methods)
4. **STOP and VALIDATE**: Compile and verify 404 on deprecated endpoints
5. This alone delivers the core value - deprecated APIs are no longer accessible

### Incremental Delivery

1. Complete Setup + Foundational → Ready to clean
2. Add User Story 1 → Compile + Test → Core cleanup done
3. Add User Story 2 → Compile + Test → ErrorCodes removed
4. Add User Story 3 → Compile + Test → Full cleanup complete
5. Polish → Final verification

### Recommended Approach

Since this is a cleanup task with dependencies between stories:

1. **Execute Phase 1-3 first** (Setup + Foundational + US1)
2. **Execute Phase 4 (US2)** in parallel or after
3. **Execute Phase 5 (US3)** after US1 is complete
4. **Execute Phase 6** for final verification

---

## Notes

- This is a **deletion-focused task** - no new code to write
- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- **Important**: User Story 3 depends on User Story 1 because the request/response classes are imported by the controller methods
- Commit after each phase completion for easy rollback
- Total estimated tasks: 38
