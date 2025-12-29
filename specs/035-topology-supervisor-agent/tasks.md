# Tasks: Topology 绑定 Global Supervisor Agent

**Input**: Design documents from `/specs/035-topology-supervisor-agent/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: 未在功能规格说明中要求测试，本任务列表不包含测试任务。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 DDD 分层架构：
- **bootstrap**: `bootstrap/src/main/resources/db/migration/`
- **interface**: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/`
- **application-api**: `application/application-api/src/main/java/com/catface996/aiops/application/api/`
- **application-impl**: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/`
- **domain-api**: `domain/domain-api/src/main/java/com/catface996/aiops/domain/`
- **domain-impl**: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/`
- **repository-api**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/`
- **mysql-impl**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`

---

## Phase 1: Setup (Database Migration)

**Purpose**: 在 topology 表添加 global_supervisor_agent_id 字段

- [X] T001 创建 Flyway 迁移脚本 V27__add_global_supervisor_agent_to_topology.sql in bootstrap/src/main/resources/db/migration/V27__add_global_supervisor_agent_to_topology.sql

**Checkpoint**: 执行 `mvn flyway:migrate` 或启动应用后，topology 表包含 `global_supervisor_agent_id` 字段

---

## Phase 2: Foundational (Infrastructure & Domain Layer)

**Purpose**: 扩展现有类，添加字段和基础方法

### PO 扩展

- [X] T002 在 TopologyPO.java 中添加 globalSupervisorAgentId 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/topology/TopologyPO.java

### Repository 扩展

- [X] T003 在 TopologyRepository.java 接口中添加 updateGlobalSupervisorAgentId 方法 in domain/repository-api/src/main/java/com/catface996/aiops/repository/topology2/TopologyRepository.java

- [X] T004 在 TopologyRepositoryImpl.java 中实现 updateGlobalSupervisorAgentId 方法 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/topology/TopologyRepositoryImpl.java

### Agent Repository 扩展

- [X] T005 在 AgentRepository.java 接口中添加 findByIdAndRole 方法（校验 Agent 角色）in domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentRepository.java

- [X] T006 在 AgentRepositoryImpl.java 中实现 findByIdAndRole 方法 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentRepositoryImpl.java

**Checkpoint**: 编译通过，无错误 `mvn clean compile -DskipTests`

---

## Phase 3: User Story 1 - 绑定 Global Supervisor Agent (Priority: P1) 🎯 MVP

**Goal**: 实现绑定功能，支持校验 Agent 角色和替换绑定

**Independent Test**:
- 调用绑定接口成功后，查询拓扑图详情能看到 globalSupervisorAgentId 已更新
- 绑定非 GLOBAL_SUPERVISOR 角色的 Agent 返回错误

### Domain Layer

- [X] T007 [US1] 在 TopologyDomainService.java 接口中添加 bindGlobalSupervisorAgent 方法 in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology2/TopologyDomainService.java

- [X] T008 [US1] 在 TopologyDomainServiceImpl.java 中实现 bindGlobalSupervisorAgent 方法（含 Agent 角色校验）in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/topology2/TopologyDomainServiceImpl.java

### Application Layer

- [X] T009 [US1] 在 TopologyApplicationService.java 接口中添加 bindGlobalSupervisorAgent 方法 in application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java

- [X] T010 [US1] 在 TopologyApplicationServiceImpl.java 中实现 bindGlobalSupervisorAgent 方法 in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java

### Interface Layer

- [X] T011 [P] [US1] 创建 BindSupervisorAgentRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/BindSupervisorAgentRequest.java

- [X] T012 [US1] 扩展 TopologyController.java 添加 /supervisor/bind 端点 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java

**Checkpoint**:
- 启动应用，通过 Swagger UI 调用绑定接口成功
- 绑定 GLOBAL_SUPERVISOR 角色的 Agent 成功
- 绑定其他角色的 Agent 返回错误

---

## Phase 4: User Story 2 - 解绑 Global Supervisor Agent (Priority: P1)

**Goal**: 实现解绑功能，支持幂等操作

**Independent Test**:
- 调用解绑接口成功后，查询拓扑图详情 globalSupervisorAgentId 为 null
- 对未绑定的拓扑图执行解绑也返回成功

### Domain Layer

- [X] T013 [US2] 在 TopologyDomainService.java 接口中添加 unbindGlobalSupervisorAgent 方法 in domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology2/TopologyDomainService.java

- [X] T014 [US2] 在 TopologyDomainServiceImpl.java 中实现 unbindGlobalSupervisorAgent 方法 in domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/topology2/TopologyDomainServiceImpl.java

### Application Layer

- [X] T015 [US2] 在 TopologyApplicationService.java 接口中添加 unbindGlobalSupervisorAgent 方法 in application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java

- [X] T016 [US2] 在 TopologyApplicationServiceImpl.java 中实现 unbindGlobalSupervisorAgent 方法 in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java

### Interface Layer

- [X] T017 [P] [US2] 创建 UnbindSupervisorAgentRequest.java in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/topology/UnbindSupervisorAgentRequest.java

- [X] T018 [US2] 扩展 TopologyController.java 添加 /supervisor/unbind 端点 in interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java

**Checkpoint**:
- 启动应用，通过 Swagger UI 调用解绑接口成功
- 对已绑定的拓扑图解绑后，globalSupervisorAgentId 变为 null
- 对未绑定的拓扑图解绑也返回成功（幂等）

---

## Phase 5: DTO 扩展 - 返回 Agent 信息

**Purpose**: 在拓扑图详情中返回绑定的 Agent 基本信息

- [X] T019 在 TopologyDTO.java 中添加 globalSupervisorAgentId, globalSupervisorAgentName, globalSupervisorAgentRole 字段 in application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/TopologyDTO.java

- [X] T020 在 TopologyApplicationServiceImpl.java 中扩展查询逻辑，填充 Agent 信息 in application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java

**Checkpoint**: 查询拓扑图详情时，返回 globalSupervisorAgentId 和 Agent 名称、角色

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 验证和收尾工作

- [X] T021 编译项目并验证无错误: mvn clean compile -DskipTests

- [X] T022 启动应用并验证 API 功能正常: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

- [X] T023 按照 quickstart.md 执行完整验证流程

- [X] T024 更新 Swagger 文档标签，确保新接口在"拓扑图管理"下正确显示

**Checkpoint**: 所有验证完成，功能可交付

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 无依赖 - 可立即开始
- **Phase 2 (Foundational)**: 依赖 Phase 1 - 必须先有数据库字段
- **Phase 3 (US1)**: 依赖 Phase 2 - 必须先有基础方法
- **Phase 4 (US2)**: 依赖 Phase 2 - 可与 Phase 3 并行实现
- **Phase 5 (DTO 扩展)**: 依赖 Phase 2 - 可与 Phase 3/4 并行实现
- **Phase 6 (Polish)**: 依赖 Phase 3-5 - 所有功能完成后进行收尾

### User Story Dependencies

- **User Story 1 (绑定)**: 依赖 Phase 2 - 核心功能
- **User Story 2 (解绑)**: 依赖 Phase 2 - 可与 US1 并行实现

### Parallel Opportunities

Phase 3 和 Phase 4 中以下任务可并行执行：
```bash
# 可并行执行的 Request 创建任务
T011: BindSupervisorAgentRequest.java
T017: UnbindSupervisorAgentRequest.java
```

Phase 3, 4, 5 可以并行进行（都只依赖 Phase 2）

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Setup (数据库迁移)
2. 完成 Phase 2: Foundational (基础方法)
3. 完成 Phase 3: US1 (绑定功能)
4. **验证**: 通过 Swagger UI 测试绑定功能
5. 此时核心功能可用，可以进行初步演示

### Incremental Delivery

1. 完成 Setup + Foundational + US1 → 核心绑定可用
2. 完成 US2 → 解绑功能可用
3. 完成 Phase 5 → DTO 返回 Agent 信息
4. 完成 Polish → 功能完整验证

### 推荐执行顺序

由于本功能较简单，建议按顺序执行：

```
T001 → T002 → T003 → T004 → T005 → T006 →
(T007, T013 并行) → (T008, T014 并行) → (T009, T015 并行) → (T010, T016 并行) →
(T011, T017 并行) → T012 → T018 → T019 → T020 →
T021 → T022 → T023 → T024
```

---

## Notes

- [P] tasks = 不同文件，无依赖，可并行执行
- [Story] label = 任务归属的 User Story
- 本功能是简单的一对一关系，只需扩展现有类，不需要新建独立的 Service 或 Repository
- 绑定时必须校验 Agent 角色为 GLOBAL_SUPERVISOR
- 所有 API 接口遵循 POST-Only 规范

---

## Summary

| 统计项 | 数量 |
|--------|------|
| 总任务数 | 24 |
| Phase 1 (Setup) | 1 |
| Phase 2 (Foundational) | 5 |
| Phase 3 (US1 - 绑定) | 6 |
| Phase 4 (US2 - 解绑) | 6 |
| Phase 5 (DTO 扩展) | 2 |
| Phase 6 (Polish) | 4 |
| 可并行任务 | 2 |

**MVP 范围**: Phase 1-3（数据库迁移 + 基础方法 + 绑定功能）
