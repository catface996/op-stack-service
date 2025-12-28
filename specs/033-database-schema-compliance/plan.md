# Implementation Plan: 数据库表结构合规性重构

**Branch**: `033-database-schema-compliance` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/033-database-schema-compliance/spec.md`

## Summary

根据宪法 VII. Database Design Standards 的合规性检查结果，本功能将：
1. 重命名违规表名（`reports` → `report`, `report_templates` → `report_template`）
2. 为 5 张表添加缺失的 `deleted` 软删除字段
3. 为 9 张表添加缺失的 `updated_by` 审计字段
4. 为 5 张表添加缺失的 `created_by` 审计字段
5. 更新所有相关 Java 代码（PO、Mapper、Repository）以适配新表结构
6. 启用软删除和审计字段自动填充功能

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, Flyway
**Storage**: MySQL 8.0
**Testing**: mvn test
**Target Platform**: Linux server / Docker
**Project Type**: DDD 分层架构 (bootstrap/interface/application/domain/infrastructure)
**Performance Goals**: 迁移期间允许短暂停机，不影响正常业务性能
**Constraints**: 数据零丢失，API 功能保持正常
**Scale/Scope**: 13 张业务表，12 个 PO 类，12 个 Mapper 接口

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 要求 | 状态 | 说明 |
|------|------|------|------|
| I. DDD Architecture | 代码组织遵循分层架构 | ✅ 通过 | PO 类在 infrastructure 层 |
| II. API URL Convention | 业务接口路径格式 | ✅ 不适用 | 本功能不新增 API |
| III. POST-Only API Design | 所有业务接口使用 POST | ✅ 不适用 | 本功能不新增 API |
| IV. Database Migration | 通过 Flyway 管理 | ✅ 通过 | 将创建 V24 迁移脚本 |
| V. Technology Stack | 技术栈版本要求 | ✅ 通过 | 使用现有技术栈 |
| VI. Pagination Protocol | 分页协议 | ✅ 不适用 | 本功能不涉及分页 |
| VII. Database Design Standards | 表设计规范 | 🔧 修复中 | 本功能目标就是修复违规 |

## Project Structure

### Documentation (this feature)

```text
specs/033-database-schema-compliance/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output - 表结构变更详情
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output - 不适用（无新 API）
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD 分层架构
bootstrap/                           # 应用启动层
├── src/main/resources/
│   └── db/migration/
│       └── V24__Schema_compliance_refactor.sql  # 新增迁移脚本

infrastructure/                      # 基础设施层
├── repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/
│   ├── config/
│   │   └── CustomMetaObjectHandler.java  # 需更新：添加审计字段填充
│   ├── po/
│   │   ├── report/
│   │   │   ├── ReportPO.java             # 需更新：表名 + 新增字段
│   │   │   └── ReportTemplatePO.java     # 需更新：表名 + 新增字段
│   │   ├── node/
│   │   │   ├── NodePO.java               # 需更新：新增 deleted, updated_by
│   │   │   ├── NodeTypePO.java           # 需更新：新增 deleted, updated_by
│   │   │   └── Node2NodePO.java          # 需更新：新增 deleted, created_by, updated_by
│   │   └── topology/
│   │       ├── TopologyPO.java           # 需更新：新增 deleted, updated_by
│   │       └── Topology2NodePO.java      # 需更新：新增 deleted
│   └── mapper/
│       └── ... (无需更新，MyBatis-Plus 自动适配)
```

**Structure Decision**: 使用现有 DDD 分层架构，主要变更集中在 infrastructure 层的 PO 类和数据库迁移脚本。

## Complexity Tracking

> 无违规需要辩护，所有变更符合宪法规范。

| 变更类型 | 影响范围 | 风险等级 |
|----------|----------|----------|
| 表重命名 | 2 张表 + 对应 PO 类 | 中 |
| 添加字段 | 9 张表 + 对应 PO 类 | 低 |
| 软删除启用 | 5 张表 + 对应 PO 类 | 低 |
| 审计字段填充 | MetaObjectHandler | 低 |
