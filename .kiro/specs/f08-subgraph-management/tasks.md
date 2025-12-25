# Tasks: 子图管理 v2.0 (Subgraph as Resource Type)

**Feature**: F08 - Subgraph Management v2.0
**Input**: Design documents from `.kiro/specs/f08-subgraph-management/`
**Prerequisites**: plan.md, requirements.md, data-model.md, contracts/
**Previous Version**: v1.0 tasks completed (below line)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US5, US6, US7...)

## User Story Mapping (v2.0 新增功能)

| Story | 需求 | 描述 | 类型 |
|-------|------|------|------|
| US5 | 需求5 | 添加成员到子图（含循环检测） | 子图特有 |
| US6 | 需求6 | 从子图移除成员 | 子图特有 |
| US7 | 需求7 | 子图详情视图（含祖先导航） | 子图特有 |
| US8 | 需求8 | 成员列表查询（分页） | 子图特有 |
| US9 | 需求9 | 拓扑数据查询（嵌套展开） | 子图特有 |

> **Note**: 需求1-4（子图创建、列表、编辑、删除）复用 Resource API，无需新增代码。
> 仅需在删除时增加"空子图"校验逻辑和废弃旧 API。

---

## Phase 1: Setup (数据库和基础设施)

**Purpose**: 数据库迁移和 SUBGRAPH 资源类型预定义

- [x] T001 创建数据库迁移文件 `bootstrap/src/main/resources/db/migration/V8__Add_subgraph_member_table.sql`
- [x] T002 [P] 添加 SUBGRAPH 资源类型到 resource_type 表（INSERT 语句）
- [x] T003 [P] 创建 subgraph_member 表（DDL 语句，含索引和外键）
- [x] T004 验证迁移脚本可正常执行

**Checkpoint**: 数据库表结构就绪

---

## Phase 2: Foundational (领域层基础)

**Purpose**: 领域模型和仓储接口定义 - 所有用户故事的基础

**⚠️ CRITICAL**: 此阶段必须完成后才能开始用户故事实现

### Repository API (Ports)

- [x] T005 [P] 创建 SubgraphMemberEntity 实体类 `domain/repository-api/src/main/java/com/catface996/aiops/repository/subgraph/entity/SubgraphMemberEntity.java`
- [x] T006 [P] 创建 SubgraphMemberRepository 接口 `domain/repository-api/src/main/java/com/catface996/aiops/repository/subgraph/SubgraphMemberRepository.java`

### Repository Implementation (Adapters)

- [x] T007 [P] 创建 SubgraphMemberPO 持久化对象 `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/subgraph/SubgraphMemberPO.java`
- [x] T008 [P] 创建 SubgraphMemberMapper MyBatis 接口 `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/subgraph/SubgraphMemberMapper.java`
- [x] T009 创建 SubgraphMemberMapper.xml 映射文件 `infrastructure/repository/mysql-impl/src/main/resources/mapper/subgraph/SubgraphMemberMapper.xml`
- [x] T010 实现 SubgraphMemberRepositoryImpl `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/subgraph/SubgraphMemberRepositoryImpl.java`

### Domain Model

- [x] T011 [P] 创建 TopologyNode 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/TopologyNode.java`
- [x] T012 [P] 创建 TopologyEdge 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/TopologyEdge.java`
- [x] T013 [P] 创建 SubgraphBoundary 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/SubgraphBoundary.java`
- [x] T014 [P] 创建 AncestorInfo 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/AncestorInfo.java`
- [x] T015 [P] 创建 NestedSubgraphInfo 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/NestedSubgraphInfo.java`
- [x] T016 创建 SubgraphTopologyResult 聚合对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/subgraph/SubgraphTopologyResult.java`

### Domain Service Interface

- [x] T017 创建 SubgraphMemberDomainService 接口 `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/subgraph/SubgraphMemberDomainService.java`

**Checkpoint**: 领域层基础就绪 - 用户故事实现可以开始

---

## Phase 3: User Story 5 - 添加成员到子图 (Priority: P1) 🎯 MVP

**Goal**: Owner 可以向子图添加资源（包括其他子图），系统执行循环检测防止循环引用

**Independent Test**: 创建子图后，添加资源成员，验证成员列表包含新添加的资源；添加嵌套子图时验证循环检测生效

### Domain Layer Implementation for US5

- [x] T018 [US5] 实现循环检测算法 (wouldCreateCycle) 在 SubgraphMemberDomainServiceImpl `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/subgraph/SubgraphMemberDomainServiceImpl.java`
- [x] T019 [US5] 实现获取祖先子图 (getAncestorSubgraphIds) 在 SubgraphMemberDomainServiceImpl
- [x] T020 [US5] 实现添加成员逻辑 (addMembers) 在 SubgraphMemberDomainServiceImpl

### Application Layer Implementation for US5

- [x] T021 [P] [US5] 创建 AddMembersCommand DTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/AddMembersCommand.java`
- [x] T022 [P] [US5] 创建 SubgraphMemberDTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/SubgraphMemberDTO.java`
- [x] T023 [US5] 创建 SubgraphMemberApplicationService 接口 (addMembers 方法) `application/application-api/src/main/java/com/catface996/aiops/application/service/subgraph/SubgraphMemberApplicationService.java`
- [x] T024 [US5] 实现 SubgraphMemberApplicationServiceImpl (addMembers 方法) `application/application-impl/src/main/java/com/catface996/aiops/application/service/subgraph/SubgraphMemberApplicationServiceImpl.java`

### Interface Layer Implementation for US5

- [x] T025 [P] [US5] 创建 AddMembersRequest `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/subgraph/AddMembersRequest.java`
- [x] T026 [US5] 创建 SubgraphMemberController (POST /subgraphs/{id}/members) `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/SubgraphMemberController.java`

### Unit Tests for US5

- [ ] T027 [P] [US5] 编写循环检测单元测试 `domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/subgraph/CycleDetectionTest.java`
- [ ] T028 [US5] 编写 SubgraphMemberDomainServiceImpl 单元测试 (addMembers) `domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/subgraph/SubgraphMemberDomainServiceImplTest.java`

**Checkpoint**: US5 完成 - 可以向子图添加成员，循环检测生效

---

## Phase 4: User Story 6 - 从子图移除成员 (Priority: P2)

**Goal**: Owner 可以从子图中移除成员资源，成员资源本身不被删除

**Independent Test**: 从已有成员的子图中移除成员，验证成员从列表中消失，但资源本身仍存在

### Domain Layer Implementation for US6

- [x] T029 [US6] 实现移除成员逻辑 (removeMembers) 在 SubgraphMemberDomainServiceImpl

### Application Layer Implementation for US6

- [ ] T030 [P] [US6] 创建 RemoveMembersCommand DTO (Skipped - using inline parameters)
- [x] T031 [US6] 扩展 SubgraphMemberApplicationService 接口 (removeMembers 方法)
- [x] T032 [US6] 实现 SubgraphMemberApplicationServiceImpl (removeMembers 方法)

### Interface Layer Implementation for US6

- [x] T033 [P] [US6] 创建 RemoveMembersRequest `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/subgraph/RemoveMembersRequest.java`
- [x] T034 [US6] 扩展 SubgraphMemberController (DELETE /subgraphs/{id}/members)

### Unit Tests for US6

- [ ] T035 [US6] 编写 SubgraphMemberDomainServiceImpl 单元测试 (removeMembers)

**Checkpoint**: US6 完成 - 可以从子图移除成员

---

## Phase 5: User Story 8 - 成员列表查询 (Priority: P3)

**Goal**: 用户可以分页查询子图的成员列表，包含成员详细信息和嵌套子图标识

**Independent Test**: 查询子图成员列表，验证返回分页数据和成员详情，嵌套子图显示 isSubgraph=true

### Domain Layer Implementation for US8

- [x] T036 [US8] 实现成员列表查询逻辑 (getMembersBySubgraphIdPaged) 在 SubgraphMemberDomainServiceImpl

### Application Layer Implementation for US8

- [ ] T037 [P] [US8] 创建 ListMembersQuery DTO (Skipped - using inline parameters)
- [x] T038 [US8] 扩展 SubgraphMemberApplicationService 接口 (listMembers 方法)
- [x] T039 [US8] 实现 SubgraphMemberApplicationServiceImpl (listMembers 方法)

### Interface Layer Implementation for US8

- [x] T040 [P] [US8] 创建 SubgraphMemberListResponse `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphMemberListResponse.java`
- [x] T041 [US8] 扩展 SubgraphMemberController (GET /subgraphs/{id}/members)

**Checkpoint**: US8 完成 - 可以分页查询成员列表

---

## Phase 6: User Story 9 - 拓扑数据查询 (Priority: P4)

**Goal**: 用户可以获取子图完整拓扑数据，支持嵌套子图展开/折叠

**Independent Test**: 查询包含嵌套子图的拓扑数据，验证 expandNested=true 时递归展开，expandNested=false 时子图显示为单节点

### Domain Layer Implementation for US9

- [x] T042 [US9] 实现嵌套子图展开逻辑 (expandNestedSubgraphs) 在 SubgraphMemberDomainServiceImpl
- [x] T043 [US9] 实现拓扑数据查询 (getSubgraphTopology) 在 SubgraphMemberDomainServiceImpl
- [x] T044 [US9] 实现成员及关系查询 (getMembersWithRelations) 在 SubgraphMemberDomainServiceImpl

### Application Layer Implementation for US9

- [x] T045 [P] [US9] 创建 TopologyQueryCommand DTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/TopologyQueryCommand.java`
- [x] T046 [P] [US9] 创建 SubgraphMembersWithRelationsDTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/SubgraphMembersWithRelationsDTO.java`
- [x] T047 [P] [US9] 创建 TopologyGraphDTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/TopologyGraphDTO.java`
- [x] T048 [US9] 扩展 SubgraphMemberApplicationService 接口 (getSubgraphTopology, getMembersWithRelations 方法)
- [x] T049 [US9] 实现 SubgraphMemberApplicationServiceImpl (getSubgraphTopology, getMembersWithRelations 方法)

### Interface Layer Implementation for US9

- [x] T050 [P] [US9] 创建 SubgraphMembersWithRelationsResponse `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphMembersWithRelationsResponse.java`
- [x] T051 [P] [US9] 创建 TopologyGraphResponse 支持 subgraphBoundaries `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/TopologyGraphResponse.java`
- [x] T052 [US9] 扩展 SubgraphMemberController (GET /subgraphs/{id}/topology)
- [x] T053 [US9] 扩展 SubgraphMemberController (GET /subgraphs/{id}/members-with-relations)

### Unit Tests for US9

- [ ] T054 [US9] 编写嵌套展开单元测试 `domain/domain-impl/src/test/java/com/catface996/aiops/domain/service/subgraph/NestedExpansionTest.java`

**Checkpoint**: US9 完成 - 可以查询完整拓扑数据，支持嵌套展开

---

## Phase 7: User Story 7 - 子图详情视图增强 (Priority: P5)

**Goal**: 用户可以查看子图祖先链用于导航

**Independent Test**: 查询嵌套子图的祖先列表，验证返回正确的祖先链和深度信息

### Domain Layer Implementation for US7

- [x] T055 [US7] 实现祖先子图查询 (getAncestors) 在 SubgraphMemberDomainServiceImpl

### Application Layer Implementation for US7

- [x] T056 [P] [US7] 创建 SubgraphAncestorsDTO `application/application-api/src/main/java/com/catface996/aiops/application/dto/subgraph/SubgraphAncestorsDTO.java`
- [x] T057 [US7] 扩展 SubgraphMemberApplicationService 接口 (getAncestors 方法)
- [x] T058 [US7] 实现 SubgraphMemberApplicationServiceImpl (getAncestors 方法)

### Interface Layer Implementation for US7

- [x] T059 [P] [US7] 创建 SubgraphAncestorsResponse `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/response/subgraph/SubgraphAncestorsResponse.java`
- [x] T060 [US7] 扩展 SubgraphMemberController (GET /subgraphs/{id}/ancestors)

**Checkpoint**: US7 完成 - 可以查询祖先子图用于导航

---

## Phase 8: 子图删除校验 (补充需求4)

**Goal**: 子图删除前必须为空（无成员），集成到现有删除流程

### Domain Layer Implementation

- [x] T061 实现子图是否为空检查 (isSubgraphEmpty) 在 SubgraphMemberDomainServiceImpl
- [x] T062 扩展 SubgraphMemberApplicationService 接口 (isSubgraphEmpty 方法)

### Application Layer Integration

- [x] T063 在 ResourceDomainServiceImpl 删除子图资源前调用 isSubgraphEmpty 校验 `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/resource/ResourceDomainServiceImpl.java`

**Checkpoint**: 子图删除校验生效

---

## Phase 9: Integration Testing

**Purpose**: 集成测试验证完整流程

- [ ] T064 编写子图成员管理集成测试 `bootstrap/src/test/java/com/catface996/aiops/integration/SubgraphMemberIntegrationTest.java`
- [ ] T065 [P] 编写循环检测集成测试场景
- [ ] T066 [P] 编写嵌套展开集成测试场景
- [ ] T067 [P] 编写权限验证集成测试场景

**Checkpoint**: 所有集成测试通过

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: 文档、清理和废弃旧 API

- [ ] T068 [P] 在旧 SubgraphController 添加 @Deprecated 注解并添加重定向说明 `interface/interface-http/src/main/java/com/catface996/aiops/http/controller/SubgraphController.java`
- [ ] T069 [P] 在旧 SubgraphDomainService 添加 @Deprecated 注解 `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/subgraph/SubgraphDomainService.java`
- [ ] T070 [P] 在旧 SubgraphApplicationService 添加 @Deprecated 注解 `application/application-api/src/main/java/com/catface996/aiops/application/service/subgraph/SubgraphApplicationService.java`
- [ ] T071 [P] 更新 API 文档（Swagger/OpenAPI）添加 v2.0 端点
- [ ] T072 运行 quickstart.md 验证
- [ ] T073 性能验证（列表查询 < 1s，拓扑渲染 < 3s，嵌套展开 < 3s）

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) → Phase 2 (Foundational) → User Stories (Phase 3-7) → Phase 8-10
```

- **Setup (Phase 1)**: No dependencies - 可立即开始
- **Foundational (Phase 2)**: 依赖 Phase 1 完成 - **BLOCKS** 所有用户故事
- **User Stories (Phase 3-7)**: 依赖 Phase 2 完成后可并行执行
- **子图删除校验 (Phase 8)**: 依赖 Phase 2
- **Integration Testing (Phase 9)**: 依赖 Phase 3-8 完成
- **Polish (Phase 10)**: 依赖所有功能完成

### User Story Dependencies

- **US5 (添加成员)**: 无依赖 - MVP 核心功能
- **US6 (移除成员)**: 可独立于 US5
- **US8 (成员列表)**: 可独立执行
- **US9 (拓扑数据)**: 可独立执行
- **US7 (祖先查询)**: 可独立执行

### Parallel Opportunities

**Phase 2 内并行**:
```
T005 (SubgraphMember Entity)  ─┐
T006 (Repository Interface)    │
T007 (PO)                      ├─→ T010 (Repository Impl)
T008 (Mapper Interface)        │
T011-T015 (Domain Models)     ─┘
```

**User Stories 间并行**:
```
US5 (添加成员) ─┐
US6 (移除成员)  │
US8 (成员列表)  ├─→ Phase 9 (Integration Test)
US9 (拓扑数据)  │
US7 (祖先查询) ─┘
```

---

## Parallel Example: Phase 2

```bash
# 可并行执行的任务
Task T005: Create SubgraphMember entity
Task T006: Create SubgraphMemberRepository interface
Task T007: Create SubgraphMemberPO
Task T008: Create SubgraphMemberMapper interface
Task T011: Create TopologyNode
Task T012: Create TopologyEdge
Task T013: Create SubgraphBoundary
Task T014: Create AncestorInfo
Task T015: Create NestedSubgraphInfo
```

---

## Implementation Strategy

### MVP First (US5 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US5 (添加成员 + 循环检测)
4. **STOP and VALIDATE**: 测试添加成员功能
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → 基础就绪
2. US5 (添加成员) → MVP! 🎯
3. US6 (移除成员) → 成员管理完整
4. US8 (成员列表) → 查询功能
5. US9 (拓扑数据) → 可视化支持
6. US7 (祖先查询) → 导航支持
7. Phase 8-10 → 完善和测试

---

## Task Summary

| Phase | 任务数 | 并行任务 | 说明 |
|-------|--------|----------|------|
| Phase 1 (Setup) | 4 | 2 | 数据库迁移 |
| Phase 2 (Foundational) | 13 | 11 | 领域层基础 |
| Phase 3 (US5) | 11 | 4 | 添加成员 |
| Phase 4 (US6) | 7 | 2 | 移除成员 |
| Phase 5 (US8) | 6 | 2 | 成员列表 |
| Phase 6 (US9) | 13 | 4 | 拓扑数据 |
| Phase 7 (US7) | 6 | 2 | 祖先查询 |
| Phase 8 | 3 | 0 | 删除校验 |
| Phase 9 | 4 | 3 | 集成测试 |
| Phase 10 | 6 | 4 | 文档清理 |
| **Total** | **73** | **34** | |

---

## Notes

- [P] tasks = 不同文件，无依赖，可并行
- [USx] label = 任务归属的用户故事
- 需求1-4 复用 Resource API，本任务列表不包含
- 子图删除校验是对 Resource API 的扩展
- 验证测试失败后再实现
- 每个任务或逻辑组完成后提交

---

**Generated**: 2025-12-22
**Version**: 2.0
**Total Tasks**: 73
**Parallel Opportunities**: 34 tasks (46.6%)
**MVP Scope**: Phase 1 + Phase 2 + Phase 3 (US5)

---

## v1.0 任务完成记录 (2024-12-04)

> 以下是 v1.0 版本（独立子图实体）的任务列表，已全部完成。
> v2.0 是在 v1.0 基础上的重构，将子图作为资源类型统一管理。

<details>
<summary>v1.0 任务列表（已完成）</summary>

### 阶段1：数据模型和仓储层

- [x] 1. 创建领域模型和实体
- [x] 2. 定义 Repository 接口
- [x] 3. 创建数据库表结构
- [x] 4. 实现 SubgraphRepository（包含权限操作）
- [x] 5. 实现 SubgraphResourceRepository

### 阶段2：领域服务层

- [x] 6. 实现子图创建功能
- [x] 7. 实现子图查询功能
- [x] 8. 实现子图更新功能
- [x] 9. 实现子图删除功能
- [x] 10. 实现权限管理功能
- [x] 11. 实现资源节点管理功能
- [x] 12. 实现子图拓扑查询功能

### 阶段3：应用服务层

- [x] 13. 定义应用服务接口和 DTO
- [x] 14. 实现子图创建应用服务
- [x] 15. 实现子图查询应用服务
- [x] 16. 实现子图更新应用服务
- [x] 17. 实现子图删除应用服务
- [x] 18. 实现资源节点管理应用服务
- [x] 19. 实现拓扑查询应用服务

### 阶段4：接口层

- [x] 20. 定义 REST API 接口
- [x] 21. 实现子图创建 API
- [x] 22. 实现子图查询 API
- [x] 23. 实现子图更新 API
- [x] 24. 实现子图删除 API
- [x] 25. 实现资源节点管理 API
- [x] 26. 实现拓扑查询 API
- [x] 27. 实现全局异常处理器

### 阶段5：集成测试和端到端测试

- [x] 28. 编写集成测试
- [x] 29. 编写端到端测试脚本

### 阶段6：文档和部署

- [x] 30. 生成 API 文档
- [x] 31. 更新数据库迁移脚本
- [x] 32. 最终验证和性能测试

</details>
