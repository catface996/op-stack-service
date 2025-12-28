# Data Model: Report Management

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Report                               │
├─────────────────────────────────────────────────────────────┤
│ PK  id              BIGINT AUTO_INCREMENT                   │
│     title           VARCHAR(200) NOT NULL                   │
│     type            VARCHAR(20) NOT NULL [ReportType]       │
│     status          VARCHAR(20) NOT NULL [ReportStatus]     │
│     author          VARCHAR(100) NOT NULL                   │
│     summary         VARCHAR(500)                            │
│     content         TEXT                                    │
│     tags            JSON                                    │
│ FK  topology_id     BIGINT (nullable)                       │
│     deleted         TINYINT(1) DEFAULT 0                    │
│     created_at      DATETIME DEFAULT CURRENT_TIMESTAMP      │
├─────────────────────────────────────────────────────────────┤
│ INDEX: idx_type (type)                                      │
│ INDEX: idx_status (status)                                  │
│ INDEX: idx_created_at (created_at)                          │
│ FULLTEXT: idx_search (title, summary)                       │
└─────────────────────────────────────────────────────────────┘
          │
          │ 0..1
          ▼
┌─────────────────────────────────────────────────────────────┐
│                       Topology                               │
│               (existing table, reference only)               │
└─────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│                     ReportTemplate                           │
├─────────────────────────────────────────────────────────────┤
│ PK  id              BIGINT AUTO_INCREMENT                   │
│     name            VARCHAR(100) NOT NULL                   │
│     description     VARCHAR(500)                            │
│     category        VARCHAR(20) NOT NULL [TemplateCategory] │
│     content         TEXT NOT NULL                           │
│     tags            JSON                                    │
│     version         INT DEFAULT 0                           │
│     deleted         TINYINT(1) DEFAULT 0                    │
│     created_at      DATETIME DEFAULT CURRENT_TIMESTAMP      │
│     updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP    │
├─────────────────────────────────────────────────────────────┤
│ INDEX: idx_category (category)                              │
└─────────────────────────────────────────────────────────────┘
```

## Entities

### Report

报告实体，存储生成的报告。**创建后不可修改（immutable）**。

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | BIGINT | No | 主键，自增 |
| title | VARCHAR(200) | No | 报告标题 |
| type | VARCHAR(20) | No | 报告类型（枚举） |
| status | VARCHAR(20) | No | 报告状态（枚举） |
| author | VARCHAR(100) | No | 作者名称 |
| summary | VARCHAR(500) | Yes | 报告摘要 |
| content | TEXT | Yes | 报告内容（Markdown 格式） |
| tags | JSON | Yes | 标签数组 |
| topology_id | BIGINT | Yes | 关联的拓扑图 ID |
| deleted | TINYINT(1) | No | 逻辑删除标记，默认 0 |
| created_at | DATETIME | No | 创建时间 |

### ReportTemplate

报告模板实体，存储报告模板。**支持 CRUD 操作**。

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | BIGINT | No | 主键，自增 |
| name | VARCHAR(100) | No | 模板名称 |
| description | VARCHAR(500) | Yes | 模板描述 |
| category | VARCHAR(20) | No | 模板分类（枚举） |
| content | TEXT | No | 模板内容（含占位符的 Markdown） |
| tags | JSON | Yes | 标签数组 |
| version | INT | No | 乐观锁版本号，默认 0 |
| deleted | TINYINT(1) | No | 逻辑删除标记，默认 0 |
| created_at | DATETIME | No | 创建时间 |
| updated_at | DATETIME | No | 更新时间 |

## Enumerations

### ReportType

报告类型枚举：

| Value | Description |
|-------|-------------|
| Diagnosis | 系统诊断报告 |
| Audit | 合规审计报告 |
| Performance | 性能分析报告 |
| Security | 安全评估报告 |

### ReportStatus

报告状态枚举：

| Value | Description |
|-------|-------------|
| Draft | 草稿 |
| Final | 已定稿 |
| Archived | 已归档 |

### ReportTemplateCategory

模板分类枚举：

| Value | Description |
|-------|-------------|
| Incident | 事件报告模板 |
| Performance | 性能报告模板 |
| Security | 安全报告模板 |
| Audit | 审计报告模板 |

## Validation Rules

### Report

- **title**: 必填，最大 200 字符
- **type**: 必填，必须是有效的 ReportType 枚举值
- **status**: 必填，必须是有效的 ReportStatus 枚举值
- **author**: 必填，最大 100 字符
- **summary**: 可选，最大 500 字符
- **content**: 可选，TEXT 类型
- **tags**: 可选，JSON 数组格式
- **topology_id**: 可选，如提供必须是存在的拓扑图 ID

### ReportTemplate

- **name**: 必填，最大 100 字符
- **description**: 可选，最大 500 字符
- **category**: 必填，必须是有效的 ReportTemplateCategory 枚举值
- **content**: 必填，TEXT 类型，支持 `{{placeholder}}` 占位符语法
- **tags**: 可选，JSON 数组格式

## State Transitions

### Report

```
[New] ──create──> [Draft/Final/Archived] ──delete──> [Deleted]
                           │
                           └── (No update allowed)
```

Report 创建时可以直接指定任意状态，创建后不可修改。

### ReportTemplate

```
[New] ──create──> [Active] ──update──> [Active] ──delete──> [Deleted]
```

Template 支持完整的生命周期管理。

## Database Migration

```sql
-- V14__create_report_tables.sql

-- 报告表
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '报告标题',
    type VARCHAR(20) NOT NULL COMMENT '报告类型: Diagnosis, Audit, Performance, Security',
    status VARCHAR(20) NOT NULL DEFAULT 'Final' COMMENT '报告状态: Draft, Final, Archived',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    summary VARCHAR(500) COMMENT '报告摘要',
    content TEXT COMMENT '报告内容(Markdown)',
    tags JSON COMMENT '标签数组',
    topology_id BIGINT COMMENT '关联拓扑图ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_topology_id (topology_id),
    FULLTEXT INDEX idx_search (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告表';

-- 报告模板表
CREATE TABLE report_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description VARCHAR(500) COMMENT '模板描述',
    category VARCHAR(20) NOT NULL COMMENT '模板分类: Incident, Performance, Security, Audit',
    content TEXT NOT NULL COMMENT '模板内容(含占位符的Markdown)',
    tags JSON COMMENT '标签数组',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';
```
