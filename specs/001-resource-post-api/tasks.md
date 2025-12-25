# Tasks: 资源管理接口统一改为POST方式

**Input**: Design documents from `/specs/001-resource-post-api/`
**Prerequisites**: plan.md (required), spec.md (required), data-model.md, contracts/

**Tests**: 本功能为接口重构，不涉及新业务逻辑，未明确要求测试任务。

**Organization**: 任务按用户故事组织，支持独立实现和测试。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[Story]**: 任务所属用户故事（如 US1）
- 描述中包含确切文件路径

## Path Conventions

本项目为 DDD 多模块结构：
- **接口层**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/`
- **应用层API**: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 本项目已初始化，无需额外设置

> 本次变更为接口重构，项目结构已存在，跳过 Setup 阶段。

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 创建/修改请求对象，为 Controller 改造做准备

**⚠️ CRITICAL**: 必须先完成请求类变更，Controller 才能引用新字段

### 新增请求类

- [x] T001 [P] 创建 GetResourceRequest.java in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/GetResourceRequest.java
- [x] T002 [P] 创建 GetResourceAuditLogsRequest.java in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/GetResourceAuditLogsRequest.java

### 修改现有请求类（添加 id 字段）

- [x] T003 [P] 修改 UpdateResourceRequest.java 添加 id 字段 in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/UpdateResourceRequest.java
- [x] T004 [P] 修改 DeleteResourceRequest.java 添加 id 字段 in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/DeleteResourceRequest.java
- [x] T005 [P] 修改 UpdateResourceStatusRequest.java 添加 id 字段 in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/UpdateResourceStatusRequest.java

### 验证

- [x] T006 执行 mvn clean compile 验证请求类编译通过

**Checkpoint**: 所有请求类准备就绪，可以开始 Controller 改造

---

## Phase 3: User Story 1 - API调用者使用统一POST方式调用接口 (Priority: P1) 🎯 MVP

**Goal**: 将 ResourceController 的 8 个接口统一改为 POST 方式，使用动词后缀 URL 路径

**Independent Test**: 使用 curl 或 Swagger UI 调用任意改造后的接口，验证 POST 方法和请求体参数正确解析

### Implementation for User Story 1

#### 创建资源接口改造

- [x] T007 [US1] 修改 createResource 方法：URL 从 `/resources` 改为 `/resources/create` in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java

#### 查询类接口改造

- [x] T008 [US1] 修改 listResources 方法：从 @GetMapping 改为 @PostMapping `/resources/list`，参数改为 @RequestBody in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T009 [US1] 修改 getResourceById 方法：从 @GetMapping 改为 @PostMapping `/resources/detail`，参数改为 @RequestBody GetResourceRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T010 [US1] 修改 getResourceAuditLogs 方法：从 @GetMapping 改为 @PostMapping `/resources/audit-logs`，参数改为 @RequestBody GetResourceAuditLogsRequest in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T011 [US1] 修改 getAllResourceTypes 方法：从 @GetMapping 改为 @PostMapping `/resource-types/list` in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java

#### 更新类接口改造

- [x] T012 [US1] 修改 updateResource 方法：从 @PutMapping 改为 @PostMapping `/resources/update`，移除 @PathVariable，从 request.getId() 获取 id in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T013 [US1] 修改 updateResourceStatus 方法：从 @PatchMapping 改为 @PostMapping `/resources/update-status`，移除 @PathVariable，从 request.getId() 获取 id in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java

#### 删除类接口改造

- [x] T014 [US1] 修改 deleteResource 方法：从 @DeleteMapping 改为 @PostMapping `/resources/delete`，移除 @PathVariable，从 request.getId() 获取 id in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java

### 验证

- [x] T015 [US1] 执行 mvn clean compile 验证 Controller 编译通过
- [ ] T016 [US1] 启动应用验证 Swagger UI 正确显示新接口定义

**Checkpoint**: 所有 8 个接口已改为 POST 方式，可独立测试验证

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: 完善文档和注释

- [x] T017 [P] 更新 ResourceController 类注释中的接口列表 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java
- [x] T018 执行 mvn clean package -DskipTests 验证完整构建通过
- [ ] T019 运行应用并测试所有 8 个接口功能正常

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 跳过（项目已存在）
- **Foundational (Phase 2)**: 无依赖，立即开始 - **BLOCKS** User Story 1
- **User Story 1 (Phase 3)**: 依赖 Phase 2 完成
- **Polish (Phase 4)**: 依赖 Phase 3 完成

### Task Dependencies within Phases

**Phase 2 (Foundational)**:
- T001-T005: 可并行执行（不同文件）
- T006: 依赖 T001-T005 全部完成

**Phase 3 (User Story 1)**:
- T007-T014: 同一文件，建议按顺序执行
- T015: 依赖 T007-T014 全部完成
- T016: 依赖 T015 完成

**Phase 4 (Polish)**:
- T017: 依赖 Phase 3 完成
- T018: 依赖 T017 完成
- T019: 依赖 T018 完成

### Parallel Opportunities

**Phase 2 内部并行**:
```text
可并行: T001, T002, T003, T004, T005 (不同文件)
```

**Phase 3 串行执行**:
```text
T007 → T008 → T009 → T010 → T011 → T012 → T013 → T014 (同一文件)
```

---

## Parallel Example: Phase 2 (Foundational)

```bash
# 同时创建/修改所有请求类:
Task: "创建 GetResourceRequest.java"
Task: "创建 GetResourceAuditLogsRequest.java"
Task: "修改 UpdateResourceRequest.java 添加 id 字段"
Task: "修改 DeleteResourceRequest.java 添加 id 字段"
Task: "修改 UpdateResourceStatusRequest.java 添加 id 字段"

# 完成后统一验证编译:
Task: "执行 mvn clean compile 验证编译通过"
```

---

## Implementation Strategy

### MVP First (仅完成核心功能)

1. 完成 Phase 2: Foundational（请求类）
2. 完成 Phase 3: User Story 1（Controller 改造）
3. **STOP and VALIDATE**: 启动应用测试所有接口
4. 确认功能正常后继续 Polish

### Incremental Delivery

1. Phase 2 → 请求类准备就绪
2. Phase 3 → 8 个接口全部改造完成
3. Phase 4 → 文档更新，完整验证

### 验证清单

- [x] 所有 8 个接口使用 POST 方法
- [x] 所有接口使用动词后缀 URL 路径
- [x] 所有参数通过请求体传递
- [ ] Swagger UI 正确显示新接口定义
- [x] mvn clean package 构建成功

---

## Notes

- [P] 任务 = 不同文件，可并行
- [US1] = User Story 1 任务
- 每个任务完成后建议 commit
- Phase 2 完成后可验证请求类编译
- Phase 3 完成后可启动应用测试
- 所有 Controller 改造任务在同一文件，建议串行执行避免冲突
