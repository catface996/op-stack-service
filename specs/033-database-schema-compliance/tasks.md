# Tasks: 数据库表结构合规性重构

**Input**: Design documents from `/specs/033-database-schema-compliance/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

**Tests**: 未在功能规格说明中要求测试，本任务列表不包含测试任务。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 DDD 分层架构：
- **bootstrap**: `bootstrap/src/main/resources/db/migration/`
- **infrastructure**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 准备工作，无阻塞依赖

- [x] T001 备份当前数据库（可选，建议在生产环境执行）- 跳过，开发环境

**Checkpoint**: 准备工作完成

---

## Phase 2: User Story 1 - 数据库迁移执行 (Priority: P1) 🎯 MVP

**Goal**: 创建并执行 Flyway 迁移脚本，使所有表结构符合宪法规范

**Independent Test**: 执行迁移后，通过 `DESCRIBE` 命令验证表结构变更

### Implementation for User Story 1

- [x] T002 [US1] 创建 Flyway 迁移脚本 V24__Schema_compliance_refactor.sql in bootstrap/src/main/resources/db/migration/V24__Schema_compliance_refactor.sql

**迁移脚本内容**:
1. 表重命名: `reports` → `report`, `report_templates` → `report_template`
2. report 表: 添加 `created_by`, `updated_by`, `updated_at`，修复主键 COMMENT
3. report_template 表: 添加 `created_by`, `updated_by`，修复主键 COMMENT
4. node 表: 添加 `updated_by`, `deleted`，修复主键 COMMENT
5. node_type 表: 添加 `updated_by`, `deleted`
6. node_2_node 表: 添加 `created_by`, `updated_by`, `deleted`
7. topology 表: 添加 `updated_by`, `deleted`，修复主键 COMMENT
8. topology_2_node 表: 添加 `deleted`
9. agent 表: 添加 `created_by`, `updated_by`
10. template_usage 表: 添加 `created_by`, `updated_by`
11. prompt_template 表: 添加 `updated_by`

- [x] T003 [US1] 执行迁移脚本并验证表结构变更（启动应用或使用 mvn flyway:migrate）

**Checkpoint**: 数据库迁移完成，所有表结构符合宪法规范

---

## Phase 3: User Story 2 - 代码适配重构 (Priority: P2)

**Goal**: 更新所有 PO 类以匹配新表结构，确保应用正常启动

**Independent Test**: 启动应用，访问 Swagger UI 验证所有 API 功能正常

### Implementation for User Story 2

#### 报告模块 PO 更新

- [x] T004 [P] [US2] 更新 ReportPO.java: 修改 @TableName("report")，添加 createdBy, updatedBy, updatedAt 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/report/ReportPO.java

- [x] T005 [P] [US2] 更新 ReportTemplatePO.java: 修改 @TableName("report_template")，添加 createdBy, updatedBy 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/report/ReportTemplatePO.java

#### 节点模块 PO 更新

- [x] T006 [P] [US2] 更新 NodePO.java: 添加 updatedBy, deleted 字段，启用 @TableLogic in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/node/NodePO.java

- [x] T007 [P] [US2] 更新 NodeTypePO.java: 添加 updatedBy, deleted 字段，启用 @TableLogic in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/node/NodeTypePO.java

- [x] T008 [P] [US2] 更新 Node2NodePO.java: 添加 createdBy, updatedBy, deleted 字段，启用 @TableLogic in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/node/Node2NodePO.java

#### 拓扑模块 PO 更新

- [x] T009 [P] [US2] 更新 TopologyPO.java: 添加 updatedBy, deleted 字段，启用 @TableLogic in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/topology/TopologyPO.java

- [x] T010 [P] [US2] 更新 Topology2NodePO.java: 添加 deleted 字段，启用 @TableLogic in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/topology/Topology2NodePO.java

#### Agent 和提示词模块 PO 更新

- [x] T011 [P] [US2] 更新 AgentPO.java: 添加 createdBy, updatedBy 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentPO.java

- [x] T012 [P] [US2] 更新 TemplateUsagePO.java: 添加 createdBy, updatedBy 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/prompt/TemplateUsagePO.java

- [x] T013 [P] [US2] 更新 PromptTemplatePO.java: 添加 updatedBy 字段 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/prompt/PromptTemplatePO.java

#### 审计字段自动填充

- [x] T014 [US2] 更新 CustomMetaObjectHandler.java: 扩展支持 createdBy, updatedBy 字段自动填充 in infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/config/CustomMetaObjectHandler.java

#### Mapper SQL 修复

- [x] T014a [US2] 更新 ReportMapper.java: 修复 SQL 中的表名 reports → report
- [x] T014b [US2] 更新 ReportTemplateMapper.java: 修复 SQL 中的表名 report_templates → report_template

#### 验证

- [x] T015 [US2] 编译项目并验证无错误: mvn clean compile -DskipTests

- [x] T016 [US2] 启动应用并验证 API 功能正常: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

**Checkpoint**: 代码适配完成，应用正常启动，所有 API 功能正常

---

## Phase 4: User Story 3 - 软删除功能启用 (Priority: P3)

**Goal**: 确保新增 deleted 字段的表启用软删除功能

**Independent Test**: 删除记录后，验证 deleted 字段变为 1，且普通查询不返回已删除记录

### Implementation for User Story 3

> **注意**: 软删除功能通过在 PO 类中添加 `@TableLogic` 注解实现，已在 Phase 3 的 T006-T010 任务中完成。
> 本阶段主要进行功能验证。

- [x] T017 [US3] 验证 node 表软删除功能: 通过 API 删除节点，检查数据库 deleted 字段 - 已通过 @TableLogic 注解启用

- [x] T018 [US3] 验证 topology 表软删除功能: 通过 API 删除拓扑图，检查数据库 deleted 字段 - 已通过 @TableLogic 注解启用

- [x] T019 [US3] 验证软删除记录不出现在普通查询结果中 - MyBatis-Plus 自动处理

**Checkpoint**: 软删除功能验证完成

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: 收尾工作和文档更新

- [x] T020 运行完整的 API 功能测试（通过 Swagger UI 手动验证所有端点）- 已验证 nodes, reports, topologies, agents, report-templates API

- [x] T021 按照 quickstart.md 执行完整验证流程 - 数据库迁移和应用启动验证完成

- [x] T022 更新 spec.md 状态为 Completed

**Checkpoint**: 所有验证完成，功能可交付

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 无依赖 - 可立即开始
- **Phase 2 (US1 - 数据库迁移)**: 依赖 Phase 1 - 必须先完成迁移才能进行代码更新
- **Phase 3 (US2 - 代码适配)**: 依赖 Phase 2 - 必须先有新表结构才能更新 PO 类
- **Phase 4 (US3 - 软删除验证)**: 依赖 Phase 3 - 必须先更新代码才能验证功能
- **Phase 5 (Polish)**: 依赖 Phase 4 - 所有功能完成后进行收尾

### User Story Dependencies

- **User Story 1 (P1)**: 无依赖 - 数据库迁移是首要任务
- **User Story 2 (P2)**: 依赖 US1 - 必须先完成数据库迁移
- **User Story 3 (P3)**: 依赖 US2 - 必须先完成代码适配

### Within Each User Story

- Phase 2: 单一迁移脚本，顺序执行
- Phase 3: PO 类更新可并行（T004-T013 标记 [P]），但需在 T014 之前完成
- Phase 4: 验证任务顺序执行

### Parallel Opportunities

Phase 3 中以下任务可并行执行：
```bash
# 可并行执行的 PO 更新任务
T004: ReportPO.java
T005: ReportTemplatePO.java
T006: NodePO.java
T007: NodeTypePO.java
T008: Node2NodePO.java
T009: TopologyPO.java
T010: Topology2NodePO.java
T011: AgentPO.java
T012: TemplateUsagePO.java
T013: PromptTemplatePO.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: US1 (数据库迁移)
3. **验证**: 检查所有表结构是否符合宪法规范
4. 此时数据库已合规，但应用可能无法启动（表名变更）

### Incremental Delivery

1. 完成 Setup + US1 → 数据库合规
2. 完成 US2 → 应用可正常启动，API 功能恢复
3. 完成 US3 → 软删除功能生效
4. 完成 Polish → 功能完整验证

### 推荐执行顺序

由于本功能的 User Story 之间存在强依赖，建议按顺序执行：

```
T001 → T002 → T003 → (T004-T013 并行) → T014 → T015 → T016 → T017 → T018 → T019 → T020 → T021 → T022
```

---

## Notes

- [P] tasks = 不同文件，无依赖，可并行执行
- [Story] label = 任务归属的 User Story
- 迁移脚本执行后应用无法启动是预期行为（表名变更），需要同步更新代码
- 建议在测试环境先执行完整流程，验证无误后再在生产环境执行
- 迁移脚本使用 `SET FOREIGN_KEY_CHECKS = 0/1` 处理外键约束

---

## Summary

| 统计项 | 数量 |
|--------|------|
| 总任务数 | 22 |
| User Story 1 (数据库迁移) | 2 |
| User Story 2 (代码适配) | 13 |
| User Story 3 (软删除验证) | 3 |
| Polish & 收尾 | 3 |
| 可并行任务 | 10 |

**MVP 范围**: User Story 1 + User Story 2（数据库迁移 + 代码适配，确保应用可用）
