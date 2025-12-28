# Data Model: 数据库表结构合规性重构

**Feature**: 033-database-schema-compliance
**Date**: 2025-12-28

## 变更概述

本文档详细描述所有表结构变更，确保符合宪法 VII. Database Design Standards 规范。

## 表重命名

### 1. reports → report

| 变更前 | 变更后 |
|--------|--------|
| `reports` | `report` |

**SQL**:
```sql
RENAME TABLE reports TO report;
```

### 2. report_templates → report_template

| 变更前 | 变更后 |
|--------|--------|
| `report_templates` | `report_template` |

**SQL**:
```sql
RENAME TABLE report_templates TO report_template;
```

---

## 字段变更详情

### 1. report (原 reports)

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `created_by` | BIGINT | NULL | NULL | 创建人ID |
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |
| `updated_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**修复 COMMENT**:

| 字段名 | 修复内容 |
|--------|----------|
| `id` | 添加 COMMENT '主键ID' |

**SQL**:
```sql
-- 添加字段
ALTER TABLE report ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER topology_id;
ALTER TABLE report ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE report ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER updated_by;

-- 修复主键 COMMENT
ALTER TABLE report MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
```

---

### 2. report_template (原 report_templates)

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `created_by` | BIGINT | NULL | NULL | 创建人ID |
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |

**修复 COMMENT**:

| 字段名 | 修复内容 |
|--------|----------|
| `id` | 添加 COMMENT '主键ID' |

**SQL**:
```sql
-- 添加字段
ALTER TABLE report_template ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER tags;
ALTER TABLE report_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

-- 修复主键 COMMENT
ALTER TABLE report_template MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
```

---

### 3. node

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |
| `deleted` | TINYINT | NOT NULL | 0 | 软删除标记: 0-未删除, 1-已删除 |

**修复 COMMENT**:

| 字段名 | 修复内容 |
|--------|----------|
| `id` | 添加 COMMENT '主键ID' |

**SQL**:
```sql
-- 添加字段
ALTER TABLE node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;

-- 修复主键 COMMENT
ALTER TABLE node MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
```

---

### 4. node_type

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |
| `deleted` | TINYINT | NOT NULL | 0 | 软删除标记: 0-未删除, 1-已删除 |

**SQL**:
```sql
ALTER TABLE node_type ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node_type ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;
```

---

### 5. node_2_node

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `created_by` | BIGINT | NULL | NULL | 创建人ID |
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |
| `deleted` | TINYINT | NOT NULL | 0 | 软删除标记: 0-未删除, 1-已删除 |

**SQL**:
```sql
ALTER TABLE node_2_node ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
ALTER TABLE node_2_node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;
```

---

### 6. topology

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |
| `deleted` | TINYINT | NOT NULL | 0 | 软删除标记: 0-未删除, 1-已删除 |

**修复 COMMENT**:

| 字段名 | 修复内容 |
|--------|----------|
| `id` | 添加 COMMENT '主键ID' |

**SQL**:
```sql
-- 添加字段
ALTER TABLE topology ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE topology ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;

-- 修复主键 COMMENT
ALTER TABLE topology MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';
```

---

### 7. topology_2_node

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `deleted` | TINYINT | NOT NULL | 0 | 软删除标记: 0-未删除, 1-已删除 |

**SQL**:
```sql
ALTER TABLE topology_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER added_by;
```

---

### 8. agent

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `created_by` | BIGINT | NULL | NULL | 创建人ID |
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |

**SQL**:
```sql
ALTER TABLE agent ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER critical;
ALTER TABLE agent ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
```

---

### 9. template_usage

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `created_by` | BIGINT | NULL | NULL | 创建人ID |
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |

**SQL**:
```sql
ALTER TABLE template_usage ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
ALTER TABLE template_usage ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
```

---

### 10. prompt_template

**新增字段**:

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `updated_by` | BIGINT | NULL | NULL | 修改人ID |

**SQL**:
```sql
ALTER TABLE prompt_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
```

---

## 变更汇总表

| 表名 | 重命名 | +deleted | +created_by | +updated_by | +updated_at | 修复COMMENT |
|------|--------|----------|-------------|-------------|-------------|-------------|
| report | ✅ | - | ✅ | ✅ | ✅ | ✅ |
| report_template | ✅ | - | ✅ | ✅ | - | ✅ |
| node | - | ✅ | - | ✅ | - | ✅ |
| node_type | - | ✅ | - | ✅ | - | - |
| node_2_node | - | ✅ | ✅ | ✅ | - | - |
| topology | - | ✅ | - | ✅ | - | ✅ |
| topology_2_node | - | ✅ | - | - | - | - |
| agent | - | - | ✅ | ✅ | - | - |
| template_usage | - | - | ✅ | ✅ | - | - |
| prompt_template | - | - | - | ✅ | - | - |

---

## 完整迁移脚本

详见 `bootstrap/src/main/resources/db/migration/V24__Schema_compliance_refactor.sql`

```sql
-- ==============================================
-- 数据库表结构合规性重构迁移脚本
-- Feature: 033-database-schema-compliance
-- Date: 2025-12-28
-- ==============================================

SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 表重命名 ==========
RENAME TABLE reports TO report;
RENAME TABLE report_templates TO report_template;

-- ========== 2. report 表变更 ==========
ALTER TABLE report ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER topology_id;
ALTER TABLE report ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER deleted;
ALTER TABLE report ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at;
ALTER TABLE report MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 3. report_template 表变更 ==========
ALTER TABLE report_template ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER tags;
ALTER TABLE report_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE report_template MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 4. node 表变更 ==========
ALTER TABLE node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;
ALTER TABLE node MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 5. node_type 表变更 ==========
ALTER TABLE node_type ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node_type ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;

-- ========== 6. node_2_node 表变更 ==========
ALTER TABLE node_2_node ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
ALTER TABLE node_2_node ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE node_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER updated_at;

-- ========== 7. topology 表变更 ==========
ALTER TABLE topology ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;
ALTER TABLE topology ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER version;
ALTER TABLE topology MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID';

-- ========== 8. topology_2_node 表变更 ==========
ALTER TABLE topology_2_node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除' AFTER added_by;

-- ========== 9. agent 表变更 ==========
ALTER TABLE agent ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER critical;
ALTER TABLE agent ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

-- ========== 10. template_usage 表变更 ==========
ALTER TABLE template_usage ADD COLUMN created_by BIGINT COMMENT '创建人ID' AFTER description;
ALTER TABLE template_usage ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

-- ========== 11. prompt_template 表变更 ==========
ALTER TABLE prompt_template ADD COLUMN updated_by BIGINT COMMENT '修改人ID' AFTER created_at;

SET FOREIGN_KEY_CHECKS = 1;
```

---

## PO 类变更清单

以下 PO 类需要更新以匹配新表结构：

| PO 类 | 变更内容 |
|-------|----------|
| `ReportPO` | 更新 `@TableName("report")`, 添加 `createdBy`, `updatedBy`, `updatedAt` 字段 |
| `ReportTemplatePO` | 更新 `@TableName("report_template")`, 添加 `createdBy`, `updatedBy` 字段 |
| `NodePO` | 添加 `updatedBy`, `deleted` 字段，启用 `@TableLogic` |
| `NodeTypePO` | 添加 `updatedBy`, `deleted` 字段，启用 `@TableLogic` |
| `Node2NodePO` | 添加 `createdBy`, `updatedBy`, `deleted` 字段，启用 `@TableLogic` |
| `TopologyPO` | 添加 `updatedBy`, `deleted` 字段，启用 `@TableLogic` |
| `Topology2NodePO` | 添加 `deleted` 字段，启用 `@TableLogic` |
| `AgentPO` | 添加 `createdBy`, `updatedBy` 字段 |
| `TemplateUsagePO` | 添加 `createdBy`, `updatedBy` 字段 |
| `PromptTemplatePO` | 添加 `updatedBy` 字段 |
