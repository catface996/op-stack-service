# Implementation Tasks: 资源分类体系设计

**Feature**: 001-resource-category-design
**Date**: 2025-12-25
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Summary

本任务列表将资源分类功能的实现按用户故事组织，确保每个阶段都是可独立测试的增量交付。

**用户故事映射**:
- US1: 查询所有拓扑图 (P1)
- US2: 查询所有资源节点 (P1)
- US3: 创建拓扑图 (P2)
- US4: 创建资源节点时禁止SUBGRAPH (P2)

---

## Phase 1: Setup (基础设施)

> 项目初始化和共享基础设施

- [x] T001 Create ResourceTypeConstants with SUBGRAPH_CODE constant in `domain/domain-api/src/main/java/com/catface996/aiops/domain/constant/ResourceTypeConstants.java`
- [x] T002 [P] Create TopologyDTO in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/TopologyDTO.java`
- [x] T003 [P] Create TopologyController skeleton in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`

---

## Phase 2: Foundational (核心服务层)

> 拓扑图领域服务和应用服务 - 所有用户故事的前置依赖

- [x] T004 Create TopologyDomainService interface in `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainService.java`
- [x] T005 Create TopologyDomainServiceImpl in `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/topology/TopologyDomainServiceImpl.java`
- [x] T006 Create TopologyApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java`
- [x] T007 Create TopologyApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java`

---

## Phase 3: User Story 1 - 查询所有拓扑图 (P1)

> **目标**: 系统管理员能够单独查询所有拓扑图（不包含资源节点）
>
> **独立测试标准**: 调用拓扑图列表接口，验证返回结果只包含 SUBGRAPH 类型资源

### DTO

- [x] T008 [US1] Create QueryTopologiesRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/QueryTopologiesRequest.java`

### 领域层

- [x] T009 [US1] Add queryTopologies method to TopologyDomainService interface in `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainService.java`
- [x] T010 [US1] Implement queryTopologies in TopologyDomainServiceImpl - filter by resourceType.code='SUBGRAPH' in `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/topology/TopologyDomainServiceImpl.java`

### 应用层

- [x] T011 [US1] Add listTopologies method to TopologyApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java`
- [x] T012 [US1] Implement listTopologies in TopologyApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java`

### 接口层

- [x] T013 [US1] Add POST /api/v1/topologies/query endpoint to TopologyController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`

---

## Phase 4: User Story 2 - 查询所有资源节点 (P1)

> **目标**: 系统管理员能够查询所有资源节点（不包含拓扑图）
>
> **独立测试标准**: 调用资源节点列表接口，验证返回结果不包含 SUBGRAPH 类型

### 领域层

- [ ] T014 [US2] Modify ResourceDomainService to add excludeSubgraph filter logic in `domain/domain-impl/src/main/java/com/catface996/aiops/domain/service/resource/ResourceDomainServiceImpl.java`

### 应用层

- [ ] T015 [US2] Modify ListResourcesRequest to auto-exclude SUBGRAPH type in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/resource/request/ListResourcesRequest.java`
- [ ] T016 [US2] Modify ResourceApplicationServiceImpl.listResources to exclude SUBGRAPH in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/resource/ResourceApplicationServiceImpl.java`

### 接口层

- [ ] T017 [US2] Update ResourceController query endpoint documentation to clarify SUBGRAPH exclusion in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java`

---

## Phase 5: User Story 3 - 创建拓扑图 (P2)

> **目标**: 系统管理员通过专用接口创建拓扑图
>
> **独立测试标准**: 调用拓扑图创建接口，验证创建的资源自动被标记为 SUBGRAPH 类型

### DTO

- [ ] T018 [US3] Create CreateTopologyRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/CreateTopologyRequest.java`

### 领域层

- [ ] T019 [US3] Add createTopology method to TopologyDomainService interface in `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainService.java`
- [ ] T020 [US3] Implement createTopology in TopologyDomainServiceImpl - auto-set resourceTypeId to SUBGRAPH in `domain/domain-impl/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainServiceImpl.java`

### 应用层

- [ ] T021 [US3] Add createTopology method to TopologyApplicationService interface in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java`
- [ ] T022 [US3] Implement createTopology in TopologyApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java`

### 接口层

- [ ] T023 [US3] Add POST /api/v1/topologies/create endpoint to TopologyController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`

---

## Phase 6: User Story 4 - 创建资源节点时禁止SUBGRAPH (P2)

> **目标**: 创建资源节点时只能选择节点类型，不能选择 SUBGRAPH 类型
>
> **独立测试标准**: 尝试用资源创建接口创建 SUBGRAPH 类型，验证系统返回错误码 400010

### 应用层

- [ ] T024 [US4] Add SUBGRAPH type validation in ResourceApplicationServiceImpl.createResource in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/resource/ResourceApplicationServiceImpl.java`

### 接口层

- [ ] T025 [US4] Add error code 400010 for SUBGRAPH type rejection in ResourceController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java`

---

## Phase 7: Polish & Cross-Cutting Concerns

> 完善拓扑图完整CRUD、迁移成员管理接口、向后兼容处理

### 拓扑图 CRUD 完善

- [ ] T026 [P] Create GetTopologyRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/GetTopologyRequest.java`
- [ ] T027 [P] Create UpdateTopologyRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/UpdateTopologyRequest.java`
- [ ] T028 [P] Create DeleteTopologyRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/DeleteTopologyRequest.java`
- [ ] T029 Add getTopology, updateTopology, deleteTopology to TopologyDomainService in `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainService.java`
- [ ] T030 Implement getTopology, updateTopology, deleteTopology in TopologyDomainServiceImpl in `domain/domain-impl/src/main/java/com/catface996/aiops/domain/service/topology/TopologyDomainServiceImpl.java`
- [ ] T031 Add CRUD methods to TopologyApplicationService in `application/application-api/src/main/java/com/catface996/aiops/application/api/service/topology/TopologyApplicationService.java`
- [ ] T032 Implement CRUD methods in TopologyApplicationServiceImpl in `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/topology/TopologyApplicationServiceImpl.java`
- [ ] T033 Add POST /api/v1/topologies/get endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [ ] T034 Add POST /api/v1/topologies/update endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [ ] T035 Add POST /api/v1/topologies/delete endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`

### 成员管理接口迁移到 /topologies

- [ ] T036 [P] Create AddTopologyMembersRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/AddTopologyMembersRequest.java`
- [ ] T037 [P] Create RemoveTopologyMembersRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/RemoveTopologyMembersRequest.java`
- [ ] T038 [P] Create QueryTopologyMembersRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/QueryTopologyMembersRequest.java`
- [ ] T039 [P] Create QueryTopologyGraphRequest in `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/topology/request/QueryTopologyGraphRequest.java`
- [ ] T040 Add POST /api/v1/topologies/members/add endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [ ] T041 Add POST /api/v1/topologies/members/remove endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [ ] T042 Add POST /api/v1/topologies/members/query endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`
- [ ] T043 Add POST /api/v1/topologies/graph/query endpoint in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/TopologyController.java`

### 向后兼容 - 标记旧接口为 Deprecated

- [ ] T044 Add @Deprecated annotation to /resources/members/* endpoints in ResourceController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java`
- [ ] T045 Add @Deprecated annotation to /resources/topology/* endpoints in ResourceController in `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/ResourceController.java`

---

## Dependencies

```
Phase 1 (Setup)
    │
    ▼
Phase 2 (Foundational) ─────────────────────────────────────┐
    │                                                        │
    ├──────────────────┬──────────────────┐                  │
    ▼                  ▼                  ▼                  │
Phase 3 (US1)    Phase 4 (US2)    Phase 5 (US3)             │
查询拓扑图        查询资源节点       创建拓扑图               │
    │                  │                  │                  │
    │                  │                  ▼                  │
    │                  │           Phase 6 (US4)             │
    │                  │           禁止SUBGRAPH创建          │
    │                  │                  │                  │
    └──────────────────┴──────────────────┴──────────────────┘
                              │
                              ▼
                    Phase 7 (Polish)
                    完善CRUD + 接口迁移
```

### 用户故事依赖关系

| 用户故事 | 依赖 | 可并行 |
|----------|------|--------|
| US1 (查询拓扑图) | Phase 2 | US2 |
| US2 (查询资源节点) | Phase 2 | US1 |
| US3 (创建拓扑图) | Phase 2 | US1, US2 |
| US4 (禁止SUBGRAPH) | Phase 2 | US1, US2, US3 |

---

## Parallel Execution Examples

### 并行组 1: Setup 阶段 (T002, T003)
```bash
# 可同时执行，无依赖
T002: Create TopologyDTO
T003: Create TopologyController skeleton
```

### 并行组 2: US1 & US2 (独立用户故事)
```bash
# Phase 3 和 Phase 4 可并行执行
# US1: T008 → T009 → T010 → T011 → T012 → T013
# US2: T014 → T015 → T016 → T017
```

### 并行组 3: Polish 阶段 DTO 创建
```bash
# T026, T027, T028, T036, T037, T038, T039 可并行
```

---

## Implementation Strategy

### MVP Scope (最小可行产品)

**推荐 MVP**: Phase 1 + Phase 2 + Phase 3 (US1) + Phase 4 (US2)

MVP 包含:
- 拓扑图查询接口 (核心功能)
- 资源节点查询自动排除 SUBGRAPH (修复混淆问题)

MVP 验收标准:
1. 调用 `/api/v1/topologies/query` 只返回 SUBGRAPH 类型
2. 调用 `/api/v1/resources/query` 不返回 SUBGRAPH 类型

### 增量交付计划

1. **交付 1 (MVP)**: US1 + US2 - 分离查询
2. **交付 2**: US3 - 拓扑图创建
3. **交付 3**: US4 - 资源节点创建校验
4. **交付 4**: Phase 7 - 完善 CRUD 和接口迁移

---

## Task Summary

| Phase | 任务数 | 用户故事 |
|-------|--------|----------|
| Phase 1 (Setup) | 3 | - |
| Phase 2 (Foundational) | 4 | - |
| Phase 3 (US1) | 6 | 查询所有拓扑图 |
| Phase 4 (US2) | 4 | 查询所有资源节点 |
| Phase 5 (US3) | 6 | 创建拓扑图 |
| Phase 6 (US4) | 2 | 禁止SUBGRAPH创建 |
| Phase 7 (Polish) | 20 | 完善 CRUD + 迁移 |
| **总计** | **45** | 4 个用户故事 |

### 并行机会

- Phase 1: 2 个任务可并行 (T002, T003)
- Phase 3 & Phase 4: 整个阶段可并行
- Phase 7: 8 个 DTO 创建任务可并行 (T026-T028, T036-T039)
