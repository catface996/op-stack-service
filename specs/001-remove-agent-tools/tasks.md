# Tasks: 移除 Agent-Tools 绑定功能

**Input**: Design documents from `/specs/001-remove-agent-tools/`
**Prerequisites**: plan.md, spec.md, research.md, quickstart.md

**Tests**: Tests are NOT included - not explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 DDD 多模块架构：
- **domain/domain-model**: 领域模型
- **domain/repository-api**: 仓储接口
- **infrastructure/repository/mysql-impl**: 仓储实现、Mapper、PO
- **application/application-api**: DTO 和服务接口
- **application/application-impl**: 服务实现
- **interface/interface-http**: HTTP 控制器
- **bootstrap**: 配置和数据库迁移

---

## Phase 1: User Story 1 - 清理 Agent-Tools 绑定代码 (Priority: P1) 🎯 MVP

**Goal**: 移除所有 Agent-Tools 绑定相关的代码，确保服务能够正常编译和运行

**Independent Test**:
- 服务能够正常编译 (`mvn clean package -DskipTests`)
- 服务能够正常启动
- Agent CRUD 接口正常工作（不含 toolIds 字段）

### Step 1.1: 修改应用层（先移除使用方）

- [X] T001 [US1] Remove toolIds field and Tools binding imports from application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/agent/AgentApplicationServiceImpl.java
- [X] T002 [P] [US1] Remove toolIds field from application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/AgentDTO.java
- [X] T003 [P] [US1] Remove toolIds field from application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/CreateAgentRequest.java
- [X] T004 [P] [US1] Remove toolIds field from application/application-api/src/main/java/com/catface996/aiops/application/api/dto/agent/request/UpdateAgentRequest.java

### Step 1.2: 修改领域层

- [X] T005 [US1] Remove toolIds field from domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java

### Step 1.3: 删除基础设施层文件

- [X] T006 [P] [US1] Delete infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentToolRelationRepositoryImpl.java
- [X] T007 [P] [US1] Delete infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/AgentToolRelationMapper.java
- [X] T008 [P] [US1] Delete infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentToolRelationPO.java

### Step 1.4: 删除领域层文件

- [X] T009 [P] [US1] Delete domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentToolRelationRepository.java
- [X] T010 [P] [US1] Delete domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentToolRelation.java

### Step 1.5: 更新接口层文档

- [X] T011 [US1] Remove Tools binding documentation from interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AgentController.java

### Step 1.6: 验证编译

- [X] T012 [US1] Run `mvn clean package -DskipTests` and verify compilation succeeds

**Checkpoint**: At this point, User Story 1 should be fully functional:
- 代码成功编译
- 所有 Agent-Tools 相关代码已移除
- Agent CRUD 接口正常工作（不含 toolIds）

---

## Phase 2: User Story 2 - 清理数据库表结构 (Priority: P2)

**Goal**: 创建数据库迁移脚本删除 `agent_2_tool` 表

**Independent Test**:
- 执行迁移脚本后 `agent_2_tool` 表被删除
- 服务正常启动并通过健康检查

- [X] T013 [US2] Create Flyway migration script bootstrap/src/main/resources/db/migration/V20__Drop_agent_tool_relation_table.sql
- [X] T014 [US2] Run application and verify migration executes successfully

**Checkpoint**: Database cleanup complete:
- `agent_2_tool` 表已删除
- 服务正常运行

---

## Phase 3: Polish & Verification

**Purpose**: Final verification and cleanup

- [X] T015 Run application and test Agent CRUD APIs per quickstart.md test scenarios
- [X] T016 Verify Swagger documentation no longer shows toolIds field

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (US1)**: No dependencies - can start immediately
- **Phase 2 (US2)**: Depends on Phase 1 completion (code must be removed before dropping table)
- **Phase 3 (Polish)**: Depends on all phases complete

### Within Phase 1

```text
Sequential: T001 (remove service impl dependencies first)
Parallel after T001: T002, T003, T004 (different DTO files)
After T001-T004: T005 (domain model)
After T005: T006, T007, T008 can run in parallel (infrastructure files)
After T006-T008: T009, T010 can run in parallel (domain files)
After T009-T010: T011 (controller docs)
Finally: T012 (verify compilation)
```

### Parallel Opportunities

Phase 1 - After T001:
```bash
# These can run together:
Task: "T002 Remove toolIds from AgentDTO"
Task: "T003 Remove toolIds from CreateAgentRequest"
Task: "T004 Remove toolIds from UpdateAgentRequest"
```

Phase 1 - After T005:
```bash
# These can run together:
Task: "T006 Delete AgentToolRelationRepositoryImpl"
Task: "T007 Delete AgentToolRelationMapper"
Task: "T008 Delete AgentToolRelationPO"
Task: "T009 Delete AgentToolRelationRepository interface"
Task: "T010 Delete AgentToolRelation domain model"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Remove all code
2. **STOP and VALIDATE**: Run `mvn clean package -DskipTests`
3. Test Agent CRUD APIs
4. If working → MVP complete!

### Full Delivery

1. Complete Phase 1 → Code removal complete
2. Complete Phase 2 → Database cleanup complete
3. Complete Phase 3 → Full verification

### Key Files Summary

| File | Action | Phase |
|------|--------|-------|
| AgentApplicationServiceImpl.java | Modify (remove Tools logic) | 1 |
| AgentDTO.java | Modify (remove toolIds) | 1 |
| CreateAgentRequest.java | Modify (remove toolIds) | 1 |
| UpdateAgentRequest.java | Modify (remove toolIds) | 1 |
| Agent.java | Modify (remove toolIds) | 1 |
| AgentToolRelationRepositoryImpl.java | Delete | 1 |
| AgentToolRelationMapper.java | Delete | 1 |
| AgentToolRelationPO.java | Delete | 1 |
| AgentToolRelationRepository.java | Delete | 1 |
| AgentToolRelation.java | Delete | 1 |
| AgentController.java | Modify (remove docs) | 1 |
| V20__Drop_agent_tool_relation_table.sql | Create | 2 |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Execution order is critical: remove usage before removing definitions
- Commit after each phase to ensure rollback capability
