# Tasks: SQL 重构 - 从 Annotation + Lambda 迁移到 mapper.xml

**Input**: Design documents from `/specs/036-refactor-sql-to-xml/`
**Prerequisites**: plan.md, spec.md, research.md, quickstart.md

**Tests**: 不需要编写新测试，使用现有测试验证功能等价性

**Organization**: 任务按功能需求（FR）组织，每个模块可独立实现和验证

## Format: `[ID] [P?] [FR?] Description`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[FR]**: 对应的功能需求编号

## Path Conventions

```
infrastructure/repository/mysql-impl/
├── src/main/java/.../mapper/     # Mapper 接口
├── src/main/java/.../impl/       # Repository 实现
└── src/main/resources/mapper/    # XML 文件
```

---

## Phase 1: Setup (基础设施准备)

**Purpose**: 配置 MyBatis mapper-locations 和创建 XML 目录结构

- [x] T001 配置 MyBatis mapper-locations in bootstrap/src/main/resources/application.yml
- [x] T002 配置 MyBatis mapper-locations in bootstrap/src/main/resources/application-local.yml
- [x] T003 创建 XML 目录结构: infrastructure/repository/mysql-impl/src/main/resources/mapper/{agent,node,topology,prompt,report}

**Checkpoint**: 基础设施就绪，可以开始模块重构

---

## Phase 2: Agent 模块重构 (FR-001/FR-002/FR-003)

**Goal**: 迁移 Agent 模块的 11 个注解 SQL 和 1 处 Lambda

**Independent Test**: 启动应用后测试 Agent 相关 API

### XML 创建

- [x] T004 [P] [FR-001] 创建 AgentMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/agent/AgentMapper.xml
  - 迁移 selectPageByCondition (动态查询)
  - 迁移 countByCondition (条件计数)
  - 迁移 selectByName (按名称查询)
  - 迁移 countByNameExcludeId (排除ID计数)
  - 迁移 countGlobalSupervisor (统计 GLOBAL_SUPERVISOR)
  - 迁移 countGroupByRole (按角色分组统计)
  - 迁移 sumFindings (汇总警告和严重问题)
  - 迁移 sumFindingsById (按ID汇总)
  - 迁移 selectPageUnbound (未绑定分页查询)
  - 迁移 countUnbound (未绑定计数)
  - 新增 softDeleteById (替换 Lambda 软删除)

### Mapper 接口修改

- [x] T005 [FR-002] 移除 AgentMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/AgentMapper.java

### Repository 实现修改

- [x] T006 [FR-003] 移除 AgentRepositoryImpl.java 中的 LambdaUpdateWrapper，改用 softDeleteById in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/AgentRepositoryImpl.java

**Checkpoint**: Agent 模块重构完成，可独立验证

---

## Phase 3: Node 模块重构 (FR-001/FR-002/FR-003)

**Goal**: 迁移 Node 模块的 25 个注解 SQL 和 3 处 Lambda

**Independent Test**: 启动应用后测试 Node 相关 API

### XML 创建

- [x] T007 [P] [FR-001] 创建 NodeMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/node/NodeMapper.xml
  - 迁移 selectPageWithTypeInfo (分页查询带类型)
  - 迁移 countByCondition (条件计数)
  - 迁移 selectByIdWithTypeInfo (ID查询带类型)
  - 迁移 selectByTypeIdAndName (按类型和名称查询)
  - 迁移 selectByName (按名称查询)
  - 新增 findExistingIds (替换 Lambda IN 查询)

- [x] T008 [P] [FR-001] 创建 NodeTypeMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/node/NodeTypeMapper.xml
  - 迁移 selectAll (查询所有)
  - 迁移 selectByCode (按编码查询)

- [x] T009 [P] [FR-001] 创建 NodeAgentRelationMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/node/NodeAgentRelationMapper.xml
  - 迁移 selectAgentIdsByNodeId (按节点查代理)
  - 迁移 selectNodeIdsByAgentId (按代理查节点)
  - 迁移 softDeleteById (软删除)
  - 迁移 softDeleteByNodeId (按节点软删除)
  - 迁移 softDeleteByAgentId (按代理软删除)
  - 新增 selectByNodeIdAndAgentId (替换 Lambda)
  - 新增 countByNodeId (替换 Lambda 计数)

- [x] T010 [P] [FR-001] 创建 Node2NodeMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/node/Node2NodeMapper.xml
  - 迁移 selectOutgoingRelations (出向关系)
  - 迁移 selectIncomingRelations (入向关系)
  - 迁移 deleteByNodeId (删除关系)
  - 迁移 selectPageByCondition (分页条件查询)
  - 迁移 countExistingRelation (统计已存在关系)
  - 迁移 findRelationshipsByNodeIds (批量查询关系)
  - 迁移 countByCondition (条件计数)
  - 迁移 selectBySourceId (按源查询)
  - 迁移 selectByTargetId (按目标查询)
  - 迁移 deleteRelation (删除指定关系)

### Mapper 接口修改

- [x] T011 [FR-002] 移除 NodeMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/NodeMapper.java

- [x] T012 [P] [FR-002] 移除 NodeTypeMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/NodeTypeMapper.java

- [x] T013 [P] [FR-002] 移除 NodeAgentRelationMapper.java 中的所有注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/NodeAgentRelationMapper.java

- [x] T014 [P] [FR-002] 移除 Node2NodeMapper.java 中的所有注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/node/Node2NodeMapper.java

### Repository 实现修改

- [x] T015 [FR-003] 移除 NodeRepositoryImpl.java 中的 LambdaQueryWrapper，改用 findExistingIds in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/node/NodeRepositoryImpl.java

- [x] T016 [P] [FR-003] 移除 NodeAgentRelationRepositoryImpl.java 中的 LambdaQueryWrapper in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/node/NodeAgentRelationRepositoryImpl.java

**Checkpoint**: Node 模块重构完成，可独立验证

---

## Phase 4: Topology 模块重构 (FR-001/FR-002/FR-003)

**Goal**: 迁移 Topology 模块的 15 个注解 SQL 和 4 处 Lambda

**Independent Test**: 启动应用后测试 Topology 相关 API

### XML 创建

- [x] T017 [P] [FR-001] 创建 TopologyMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/topology/TopologyMapper.xml
  - 迁移 selectPageWithMemberCount (分页查询带成员数)
  - 迁移 selectByIdWithMemberCount (ID查询带成员数)
  - 迁移 selectByName (按名称查询)
  - 新增 countByCondition (替换 Lambda 计数)
  - 新增 updateGlobalSupervisorAgentId (替换 Lambda 更新)

- [x] T018 [P] [FR-001] 创建 Topology2NodeMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/topology/Topology2NodeMapper.xml
  - 迁移 findMembersByTopologyId (查询成员)
  - 迁移 selectByTopologyIdAndNodeId (按拓扑和节点查询)
  - 迁移 deleteByTopologyId (按拓扑删除)
  - 迁移 deleteByNodeId (按节点删除)
  - 迁移 countByNodeId (按节点计数)
  - 新增 countByTopologyId (替换 Lambda 计数)
  - 新增 deleteByTopologyIdAndNodeIds (替换 Lambda 批量删除)

- [x] T019 [P] [FR-001] 创建 TopologyReportTemplateMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/topology/TopologyReportTemplateMapper.xml
  - 迁移 selectByTopologyIdAndTemplateId
  - 迁移 deleteByTopologyIdAndTemplateIds
  - 迁移 selectPageByTopologyId
  - 迁移 countByTopologyId
  - 迁移 selectTemplateIdsByTopologyId
  - 迁移 countByTemplateId

### Mapper 接口修改

- [x] T020 [FR-002] 移除 TopologyMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/topology/TopologyMapper.java

- [x] T021 [P] [FR-002] 移除 Topology2NodeMapper.java 中的所有注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/topology/Topology2NodeMapper.java

- [x] T022 [P] [FR-002] 移除 TopologyReportTemplateMapper.java 中的所有注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/topology/TopologyReportTemplateMapper.java

### Repository 实现修改

- [x] T023 [FR-003] 移除 TopologyRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/topology/TopologyRepositoryImpl.java

- [x] T024 [P] [FR-003] 移除 Topology2NodeRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/topology/Topology2NodeRepositoryImpl.java

**Checkpoint**: Topology 模块重构完成，可独立验证

---

## Phase 5: Prompt 模块重构 (FR-001/FR-002/FR-003)

**Goal**: 迁移 Prompt 模块的 12 个注解 SQL 和 2 处 Lambda

**Independent Test**: 启动应用后测试 Prompt 相关 API

### XML 创建

- [x] T025 [P] [FR-001] 创建 PromptTemplateMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/prompt/PromptTemplateMapper.xml
  - 迁移 selectByName (按名称查询)
  - 迁移 selectByIdWithDetails (ID查询带详情)
  - 迁移 selectPageByCondition (分页条件查询)
  - 迁移 countByCondition (条件计数)
  - 新增 softDeleteById (替换 Lambda 软删除)

- [x] T026 [P] [FR-001] 创建 PromptTemplateVersionMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/prompt/PromptTemplateVersionMapper.xml
  - 迁移 selectByTemplateIdAndVersion (按模板和版本查询)
  - 迁移 selectByTemplateId (按模板查询)
  - 迁移 selectLatestByTemplateId (查询最新版本)
  - 迁移 countByTemplateId (按模板计数)
  - 迁移 deleteByTemplateId (按模板删除)

- [x] T027 [P] [FR-001] 创建 TemplateUsageMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/prompt/TemplateUsageMapper.xml
  - 迁移 selectByCode (按编码查询)
  - 迁移 selectByName (按名称查询)
  - 迁移 countTemplatesByUsageId (统计模板数)
  - 新增 selectPage (替换 Lambda 分页)

### Mapper 接口修改

- [x] T028 [FR-002] 移除 PromptTemplateMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/PromptTemplateMapper.java

- [x] T029 [P] [FR-002] 移除 PromptTemplateVersionMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/PromptTemplateVersionMapper.java

- [x] T030 [P] [FR-002] 移除 TemplateUsageMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/prompt/TemplateUsageMapper.java

### Repository 实现修改

- [x] T031 [FR-003] 移除 PromptTemplateRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/PromptTemplateRepositoryImpl.java

- [x] T032 [P] [FR-003] 移除 TemplateUsageRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/prompt/TemplateUsageRepositoryImpl.java

**Checkpoint**: Prompt 模块重构完成，可独立验证

---

## Phase 6: Report 模块重构 (FR-001/FR-002/FR-003)

**Goal**: 迁移 Report 模块的 6 个注解 SQL 和 2 处 Lambda

**Independent Test**: 启动应用后测试 Report 相关 API

### XML 创建

- [x] T033 [P] [FR-001] 创建 ReportMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/report/ReportMapper.xml
  - 迁移 selectPageByCondition (分页条件查询)
  - 迁移 countByCondition (条件计数)
  - 新增 softDeleteById (替换 Lambda 软删除)

- [x] T034 [P] [FR-001] 创建 ReportTemplateMapper.xml in infrastructure/repository/mysql-impl/src/main/resources/mapper/report/ReportTemplateMapper.xml
  - 迁移 selectByName (按名称查询)
  - 迁移 selectPageByCondition (分页条件查询)
  - 迁移 countByCondition (条件计数)
  - 迁移 selectByIds (批量ID查询)
  - 新增 softDeleteById (替换 Lambda 软删除)

### Mapper 接口修改

- [x] T035 [FR-002] 移除 ReportMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/report/ReportMapper.java

- [x] T036 [P] [FR-002] 移除 ReportTemplateMapper.java 中的所有 @Select 注解 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/report/ReportTemplateMapper.java

### Repository 实现修改

- [x] T037 [FR-003] 移除 ReportRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/report/ReportRepositoryImpl.java

- [x] T038 [P] [FR-003] 移除 ReportTemplateRepositoryImpl.java 中的 Lambda in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/report/ReportTemplateRepositoryImpl.java

**Checkpoint**: Report 模块重构完成，可独立验证

---

## Phase 7: 验证与宪法更新 (FR-004/FR-005)

**Purpose**: 全量验证和规范更新

### 编译验证

- [x] T039 执行 mvn clean compile 确保编译通过

### 功能验证

- [x] T040 启动应用并验证 Agent API 功能正常
- [x] T041 [P] 验证 Node API 功能正常
- [x] T042 [P] 验证 Topology API 功能正常
- [x] T043 [P] 验证 Prompt API 功能正常
- [x] T044 [P] 验证 Report API 功能正常

### 代码清理

- [x] T045 移除所有 Mapper 和 RepositoryImpl 中未使用的 import

### 宪法更新

- [x] T046 [FR-005] 更新 CLAUDE.md 添加 SQL 实现规范

**Checkpoint**: 全部重构完成，所有验收标准达成

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 无依赖，立即开始
- **Phase 2-6 (模块重构)**: 依赖 Phase 1 完成
- **Phase 7 (验证)**: 依赖 Phase 2-6 全部完成

### 模块间依赖

- **Agent/Node/Topology/Prompt/Report**: 相互独立，可并行执行

### 模块内依赖

每个模块内执行顺序：
1. 创建 XML 文件（可并行）
2. 修改 Mapper 接口（可并行）
3. 修改 Repository 实现（依赖 XML 和 Mapper 完成）

### Parallel Opportunities

```bash
# Phase 2-6 可以并行（不同模块）
Agent 模块 || Node 模块 || Topology 模块 || Prompt 模块 || Report 模块

# 每个模块内 XML 创建可以并行
T007 || T008 || T009 || T010  # Node 模块的 4 个 XML

# 每个模块内 Mapper 修改可以并行
T011 || T012 || T013 || T014  # Node 模块的 4 个 Mapper
```

---

## Implementation Strategy

### 推荐执行顺序

1. **Phase 1**: Setup（必须先完成）
2. **Phase 2**: Agent 模块（最简单，11 个 SQL）
3. **Phase 3**: Node 模块（最复杂，25 个 SQL）
4. **Phase 4**: Topology 模块（15 个 SQL）
5. **Phase 5**: Prompt 模块（12 个 SQL）
6. **Phase 6**: Report 模块（6 个 SQL）
7. **Phase 7**: 验证与宪法更新

### MVP 策略

如果需要分批交付：
1. MVP1: Phase 1 + Phase 2（Agent）→ 验证核心流程
2. MVP2: + Phase 3（Node）→ 最复杂模块完成
3. MVP3: + Phase 4-6 → 全部模块完成
4. Final: Phase 7 → 验证和规范化

---

## Summary

| 统计项 | 数量 |
|--------|------|
| 总任务数 | 46 |
| Phase 1 (Setup) | 3 |
| Phase 2 (Agent) | 3 |
| Phase 3 (Node) | 10 |
| Phase 4 (Topology) | 8 |
| Phase 5 (Prompt) | 8 |
| Phase 6 (Report) | 6 |
| Phase 7 (验证) | 8 |
| 可并行任务 [P] | 28 |
| XML 文件创建 | 13 |
| Mapper 修改 | 13 |
| Repository 修改 | 10 |
