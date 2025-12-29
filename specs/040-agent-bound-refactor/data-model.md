# Data Model: Agent Binding Relationship Refactor

**Feature**: 040-agent-bound-refactor
**Date**: 2025-12-29

## Entity Overview

```
┌─────────────────┐         ┌─────────────────┐
│    Topology     │         │      Node       │
│  (existing)     │         │   (existing)    │
└────────┬────────┘         └────────┬────────┘
         │                           │
         │ entityType=TOPOLOGY       │ entityType=NODE
         │ hierarchyLevel=           │ hierarchyLevel=
         │ GLOBAL_SUPERVISOR         │ TEAM_SUPERVISOR
         │                           │ or TEAM_WORKER
         ▼                           ▼
┌─────────────────────────────────────────────┐
│              agent_bound (NEW)              │
│                                             │
│  id, agent_id, hierarchy_level,             │
│  entity_id, entity_type, created_at, deleted│
└─────────────────────┬───────────────────────┘
                      │
                      │
                      ▼
              ┌───────────────┐
              │     Agent     │
              │  (existing)   │
              └───────────────┘
```

## Database Schema

### New Table: agent_bound

```sql
-- V29__create_agent_bound_table.sql
-- Agent Binding Relationship Refactor
-- Feature: 040-agent-bound-refactor
-- Date: 2025-12-29

CREATE TABLE agent_bound (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '绑定记录 ID',

    -- 业务核心字段
    agent_id        BIGINT          NOT NULL COMMENT 'Agent ID',
    hierarchy_level VARCHAR(20)     NOT NULL COMMENT '层级: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER',
    entity_id       BIGINT          NOT NULL COMMENT '绑定实体 ID',
    entity_type     VARCHAR(20)     NOT NULL COMMENT '实体类型: TOPOLOGY, NODE',

    -- 审计字段
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 软删除
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_entity (entity_type, entity_id, deleted),
    INDEX idx_agent_id (agent_id, deleted),
    INDEX idx_hierarchy_level (hierarchy_level, deleted),
    UNIQUE INDEX uk_agent_entity (agent_id, entity_id, entity_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 绑定关系表';
```

### Migration Script: Data Migration

```sql
-- V30__migrate_agent_bindings.sql
-- Migrate existing bindings to unified agent_bound table
-- Feature: 040-agent-bound-refactor
-- Date: 2025-12-29

-- Step 1: Migrate Global Supervisor bindings from topology table
INSERT INTO agent_bound (agent_id, hierarchy_level, entity_id, entity_type, created_at, deleted)
SELECT
    t.global_supervisor_agent_id,
    'GLOBAL_SUPERVISOR',
    t.id,
    'TOPOLOGY',
    COALESCE(t.updated_at, t.created_at, NOW()),
    0
FROM topology t
WHERE t.global_supervisor_agent_id IS NOT NULL
  AND t.deleted = 0;

-- Step 2: Migrate Node-Agent bindings from node_2_agent table
-- Need to join with agent table to get hierarchy_level
INSERT INTO agent_bound (agent_id, hierarchy_level, entity_id, entity_type, created_at, deleted)
SELECT
    n2a.agent_id,
    a.hierarchy_level,
    n2a.node_id,
    'NODE',
    n2a.created_at,
    n2a.deleted
FROM node_2_agent n2a
JOIN agent a ON n2a.agent_id = a.id;
```

## Domain Entities

### AgentBound (New Domain Entity)

```java
/**
 * Agent 绑定关系领域实体
 *
 * <p>表示 Agent 与各类实体（Topology、Node）的绑定关系。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>每个 Topology 只能绑定一个 GLOBAL_SUPERVISOR</li>
 *   <li>每个 Node 只能绑定一个 TEAM_SUPERVISOR</li>
 *   <li>每个 Node 可以绑定多个 TEAM_WORKER</li>
 *   <li>绑定的 Agent 层级必须与 hierarchy_level 匹配</li>
 * </ul>
 */
public class AgentBound {
    // 字段
    private Long id;
    private Long agentId;
    private BoundHierarchyLevel hierarchyLevel;
    private Long entityId;
    private BoundEntityType entityType;
    private LocalDateTime createdAt;
    private Boolean deleted;

    // 工厂方法
    public static AgentBound create(Long agentId, BoundHierarchyLevel hierarchyLevel,
                                     Long entityId, BoundEntityType entityType);

    // 业务方法
    public boolean isGlobalSupervisorBinding();
    public boolean isTeamSupervisorBinding();
    public boolean isWorkerBinding();
    public void markDeleted();
}
```

### BoundEntityType (New Enum)

```java
/**
 * 可绑定实体类型
 */
public enum BoundEntityType {
    TOPOLOGY("拓扑图"),
    NODE("资源节点");

    // 验证方法
    public boolean supportsHierarchyLevel(BoundHierarchyLevel level);
}
```

### BoundHierarchyLevel (New Enum or Reuse)

```java
/**
 * 绑定层级（可复用 AgentHierarchyLevel）
 *
 * 建议：直接使用现有的 AgentHierarchyLevel 枚举
 * 位置: domain/domain-model/.../agent/AgentHierarchyLevel.java
 */
// 现有值: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
```

## Entity Relationships

| Source Entity | Relationship | Target Entity | Cardinality | Constraint |
|--------------|--------------|---------------|-------------|------------|
| AgentBound | belongs_to | Agent | N:1 | agent_id NOT NULL |
| AgentBound | binds_to | Topology | N:1 | when entity_type=TOPOLOGY |
| AgentBound | binds_to | Node | N:1 | when entity_type=NODE |
| Topology | has | AgentBound (GS) | 1:0..1 | One GLOBAL_SUPERVISOR max |
| Node | has | AgentBound (TS) | 1:0..1 | One TEAM_SUPERVISOR max |
| Node | has | AgentBound (TW) | 1:N | Multiple TEAM_WORKER allowed |

## Validation Rules

### VR-001: Agent Hierarchy Match
- Binding hierarchy_level MUST match Agent's hierarchy_level
- Enforced at application service layer

### VR-002: Entity Type Hierarchy Compatibility
| entity_type | Allowed hierarchy_level |
|-------------|------------------------|
| TOPOLOGY | GLOBAL_SUPERVISOR only |
| NODE | TEAM_SUPERVISOR, TEAM_WORKER |

### VR-003: Single Supervisor Constraint
- One GLOBAL_SUPERVISOR per Topology
- One TEAM_SUPERVISOR per Node
- Enforced at application service layer with replace-on-duplicate

### VR-004: Entity Existence
- agent_id MUST reference existing Agent (not soft-deleted)
- entity_id MUST reference existing Topology or Node (based on entity_type)

## State Transitions

```
┌─────────────┐     create()      ┌─────────────┐
│  (none)     │ ───────────────►  │   ACTIVE    │
└─────────────┘                   └──────┬──────┘
                                         │
                                         │ markDeleted()
                                         │ (soft delete)
                                         ▼
                                  ┌─────────────┐
                                  │   DELETED   │
                                  └─────────────┘
```

## Index Strategy

| Index Name | Columns | Purpose |
|------------|---------|---------|
| PRIMARY KEY | id | Unique identifier |
| idx_entity | entity_type, entity_id, deleted | Fast entity binding lookup |
| idx_agent_id | agent_id, deleted | Reverse lookup by agent |
| idx_hierarchy_level | hierarchy_level, deleted | Filter by hierarchy |
| uk_agent_entity | agent_id, entity_id, entity_type, deleted | Prevent duplicate bindings |

## Query Patterns

### Q1: Get bindings for an entity

```sql
SELECT * FROM agent_bound
WHERE entity_type = ? AND entity_id = ? AND deleted = 0
ORDER BY hierarchy_level;
```

### Q2: Get bindings for an agent

```sql
SELECT * FROM agent_bound
WHERE agent_id = ? AND deleted = 0
ORDER BY entity_type, entity_id;
```

### Q3: Get hierarchical team structure for topology

```sql
SELECT ab.*, a.name AS agent_name, a.role AS agent_role
FROM agent_bound ab
JOIN agent a ON ab.agent_id = a.id AND a.deleted = 0
WHERE ab.deleted = 0
  AND (
    (ab.entity_type = 'TOPOLOGY' AND ab.entity_id = :topologyId)
    OR
    (ab.entity_type = 'NODE' AND ab.entity_id IN (
      SELECT node_id FROM topology_2_node
      WHERE topology_id = :topologyId AND deleted = 0
    ))
  )
ORDER BY ab.entity_type DESC, ab.hierarchy_level, ab.entity_id;
```

### Q4: Check if supervisor already bound

```sql
SELECT COUNT(*) FROM agent_bound
WHERE entity_type = ? AND entity_id = ? AND hierarchy_level = ? AND deleted = 0;
```
