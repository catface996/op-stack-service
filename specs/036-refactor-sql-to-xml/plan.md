# Implementation Plan: SQL 重构 - 从 Annotation + Lambda 迁移到 mapper.xml

**Branch**: `036-refactor-sql-to-xml` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/036-refactor-sql-to-xml/spec.md`

## Summary

将项目中所有 MyBatis SQL 实现从注解（@Select/@Update/@Delete）和 Lambda（LambdaQueryWrapper/LambdaUpdateWrapper）方式统一迁移到 mapper.xml 文件，实现 SQL 集中管理和规范化。

**关键决策**：
- 完全禁止使用 Lambda/Wrapper，所有需要动态条件的查询都写 XML
- 不设回滚策略，问题直接修复

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x
**Storage**: MySQL 8.0
**Testing**: mvn test (JUnit)
**Target Platform**: Linux server / Docker
**Project Type**: DDD 分层架构 (bootstrap/interface/application/domain/infrastructure)
**Performance Goals**: N/A（纯重构，功能等价）
**Constraints**: 所有 API 功能必须与重构前完全一致
**Scale/Scope**: 13 个 Mapper 接口，10 个 RepositoryImpl，69 处注解 SQL，13 处 Lambda

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 状态 | 说明 |
|------|------|------|
| I. DDD Architecture | ✅ PASS | 仅修改 infrastructure 层，不影响层级依赖 |
| II. API URL Convention | ✅ N/A | 不涉及 API URL 变更 |
| III. POST-Only API Design | ✅ N/A | 不涉及 API 设计变更 |
| IV. Database Migration | ✅ N/A | 不涉及数据库 schema 变更 |
| V. Technology Stack | ✅ PASS | 使用 MyBatis-Plus 3.5.x，符合规范 |
| VI. Pagination Protocol | ✅ N/A | 不涉及分页协议变更 |
| VII. Database Design Standards | ✅ N/A | 不涉及数据库表设计变更 |

**宪法扩展需求**：本次重构完成后，需要在宪法中新增 SQL 实现规范（FR-005）

## Project Structure

### Documentation (this feature)

```text
specs/036-refactor-sql-to-xml/
├── plan.md              # This file
├── research.md          # MyBatis XML 最佳实践研究
├── quickstart.md        # 重构快速指南
└── tasks.md             # 任务分解（由 /speckit.tasks 生成）
```

### Source Code (repository root)

```text
infrastructure/repository/mysql-impl/
├── src/main/java/com/catface996/aiops/repository/mysql/
│   ├── mapper/                    # Mapper 接口（移除注解）
│   │   ├── agent/
│   │   │   └── AgentMapper.java
│   │   ├── node/
│   │   │   ├── NodeMapper.java
│   │   │   ├── NodeTypeMapper.java
│   │   │   ├── NodeAgentRelationMapper.java
│   │   │   └── Node2NodeMapper.java
│   │   ├── topology/
│   │   │   ├── TopologyMapper.java
│   │   │   ├── Topology2NodeMapper.java
│   │   │   └── TopologyReportTemplateMapper.java
│   │   ├── prompt/
│   │   │   ├── PromptTemplateMapper.java
│   │   │   ├── PromptTemplateVersionMapper.java
│   │   │   └── TemplateUsageMapper.java
│   │   └── report/
│   │       ├── ReportMapper.java
│   │       └── ReportTemplateMapper.java
│   └── impl/                      # Repository 实现（移除 Lambda）
│       ├── agent/
│       ├── node/
│       ├── topology/
│       ├── prompt/
│       └── report/
└── src/main/resources/
    └── mapper/                    # 新增 XML 文件
        ├── agent/
        │   └── AgentMapper.xml
        ├── node/
        │   ├── NodeMapper.xml
        │   ├── NodeTypeMapper.xml
        │   ├── NodeAgentRelationMapper.xml
        │   └── Node2NodeMapper.xml
        ├── topology/
        │   ├── TopologyMapper.xml
        │   ├── Topology2NodeMapper.xml
        │   └── TopologyReportTemplateMapper.xml
        ├── prompt/
        │   ├── PromptTemplateMapper.xml
        │   ├── PromptTemplateVersionMapper.xml
        │   └── TemplateUsageMapper.xml
        └── report/
            ├── ReportMapper.xml
            └── ReportTemplateMapper.xml
```

**Structure Decision**: 在 infrastructure 模块的 resources 目录下新增 mapper 目录，按模块分组存放 XML 文件，与 Java Mapper 接口目录结构保持一致。

## Complexity Tracking

> 无宪法违规，无需记录

## Implementation Phases

### Phase 1: 基础设施准备
1. 配置 MyBatis mapper-locations
2. 创建 XML 目录结构

### Phase 2: 核心模块重构（Agent/Node/Topology）
- 创建 8 个 XML 文件
- 修改 8 个 Mapper.java
- 修改 6 个 RepositoryImpl.java

### Phase 3: 辅助模块重构（Prompt/Report）
- 创建 5 个 XML 文件
- 修改 5 个 Mapper.java
- 修改 4 个 RepositoryImpl.java

### Phase 4: 验证与宪法更新
1. 编译验证
2. 功能验证
3. 更新宪法规则
