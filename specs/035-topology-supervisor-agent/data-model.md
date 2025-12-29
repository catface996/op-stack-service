# Data Model: Topology 绑定 Global Supervisor Agent

**Feature**: 035-topology-supervisor-agent
**Date**: 2025-12-29

## 1. 实体关系图

```
┌─────────────────────────────┐              ┌─────────────────────┐
│         topology            │              │       agent         │
├─────────────────────────────┤              ├─────────────────────┤
│ id (PK)                     │              │ id (PK)             │
│ name                        │              │ name                │
│ description                 │              │ role                │
│ status                      │      1     1 │ specialty           │
│ coordinator_agent_id (FK)   │──────────────│ config              │
│ global_supervisor_agent_id  │──────────────│ ...                 │
│   (FK, NEW)                 │              │                     │
│ ...                         │              │                     │
└─────────────────────────────┘              └─────────────────────┘
```

**关系说明**:
- Topology : Agent = 1 : 0..1 (一对一可选关系)
- 一个拓扑图最多绑定一个 Global Supervisor Agent
- Agent 可以被多个拓扑图引用（无约束）

## 2. 字段变更

### 2.1 topology 表 - 新增字段

**表描述**: 拓扑图表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| global_supervisor_agent_id | BIGINT | NULL | Global Supervisor Agent ID |

**索引**:
- INDEX idx_global_supervisor_agent_id (global_supervisor_agent_id)

**约束说明**:
- 字段可为 NULL，表示未绑定状态
- 不添加外键约束（与项目现有设计一致），通过应用层保证数据完整性
- 添加索引便于按 Agent 查询关联的拓扑图

## 3. 现有实体引用

### 3.1 topology (拓扑图)

**当前结构** (来自 V12__Split_resource_to_topology_and_node.sql):
```sql
CREATE TABLE topology (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '拓扑图名称',
    description TEXT COMMENT '拓扑图描述',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态',
    coordinator_agent_id BIGINT COMMENT '协调 Agent ID（预留字段）',
    attributes JSON COMMENT '扩展属性',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ...
);
```

### 3.2 agent (Agent)

**关键字段**:
- `id`: 主键，用于关联
- `name`: 名称，用于显示
- `role`: 角色，值包括 GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER
- `deleted`: 软删除标记

## 4. 验证规则

### 4.1 绑定操作验证

| 规则 | 说明 | 错误码 |
|------|------|--------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 | 404001 |
| Agent 存在性 | agent_id 必须对应未删除的 Agent | 404002 |
| Agent 角色校验 | Agent 的 role 必须为 GLOBAL_SUPERVISOR | 400001 |

### 4.2 解绑操作验证

| 规则 | 说明 | 错误码 |
|------|------|--------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 | 404001 |

## 5. 状态转换

绑定关系只是字段值的变化：

```
未绑定状态 (global_supervisor_agent_id = NULL)
         │
         │ 绑定操作 (传入 agent_id)
         ▼
已绑定状态 (global_supervisor_agent_id = agent_id)
         │
         │ 解绑操作
         ▼
未绑定状态 (global_supervisor_agent_id = NULL)
         │
         │ 重新绑定 (传入 new_agent_id)
         ▼
已绑定状态 (global_supervisor_agent_id = new_agent_id)
```

**说明**:
- 绑定操作直接更新字段值，支持替换绑定
- 解绑操作将字段值设为 NULL
- 无需软删除机制，因为只是字段更新

## 6. 数据迁移

### 6.1 Flyway 迁移脚本

**文件**: `V27__add_global_supervisor_agent_to_topology.sql`

```sql
-- =====================================================
-- V27: 为 topology 表添加 global_supervisor_agent_id 字段
-- Feature: 035-topology-supervisor-agent
-- Date: 2025-12-29
-- =====================================================

-- 添加 Global Supervisor Agent ID 字段
ALTER TABLE topology
ADD COLUMN global_supervisor_agent_id BIGINT COMMENT 'Global Supervisor Agent ID';

-- 添加索引
ALTER TABLE topology
ADD INDEX idx_global_supervisor_agent_id (global_supervisor_agent_id);
```

## 7. PO 类变更

### 7.1 TopologyPO 扩展

```java
// 在现有 TopologyPO 类中添加字段

/**
 * Global Supervisor Agent ID
 */
@TableField("global_supervisor_agent_id")
private Long globalSupervisorAgentId;
```

## 8. DTO 变更

### 8.1 TopologyDTO 扩展

```java
// 在现有 TopologyDTO 中添加字段

/**
 * Global Supervisor Agent ID
 */
private Long globalSupervisorAgentId;

/**
 * Global Supervisor Agent 名称（关联查询填充）
 */
private String globalSupervisorAgentName;

/**
 * Global Supervisor Agent 角色（关联查询填充）
 */
private String globalSupervisorAgentRole;
```

## 9. 查询模式

### 9.1 查询拓扑图详情（带 Agent 信息）

```sql
SELECT t.*,
       a.name AS global_supervisor_agent_name,
       a.role AS global_supervisor_agent_role
FROM topology t
LEFT JOIN agent a ON t.global_supervisor_agent_id = a.id AND a.deleted = 0
WHERE t.id = ?
  AND t.deleted = 0
```

### 9.2 校验 Agent 角色

```sql
SELECT id, name, role
FROM agent
WHERE id = ?
  AND deleted = 0
  AND role = 'GLOBAL_SUPERVISOR'
```
