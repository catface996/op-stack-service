# Data Model: Resource 模型分离重构

**Feature**: [spec.md](./spec.md) | **Date**: 2025-12-26 | **Updated**: 2025-12-26

## 设计变更说明

根据用户反馈，对关联表设计进行优化：
- 原 `subgraph_member` 表替换为 `topology_2_node`（**不支持拓扑图嵌套**）
- 原 `resource_relationship` 表重命名为 `node_2_node`

**优化理由**：
1. 每个外键字段都能有明确的约束
2. 表名即语义，无需应用层判断关联类型
3. 各表可独立扩展字段（如 `topology_2_node` 可加位置信息）
4. 简化设计：不支持拓扑图嵌套，降低复杂度

---

## 实体关系图 (ERD)

```
┌─────────────────────────────┐          ┌─────────────────────────────┐
│         topology            │          │           node              │
├─────────────────────────────┤          ├─────────────────────────────┤
│ id (PK)                     │          │ id (PK)                     │
│ name                        │          │ name                        │
│ description                 │          │ description                 │
│ status                      │          │ node_type_id (FK)           │───┐
│ coordinator_agent_id        │          │ status                      │   │
│ attributes (JSON)           │          │ agent_team_id               │   │
│ created_by                  │          │ attributes (JSON)           │   │
│ version                     │          │ created_by                  │   │
│ created_at                  │          │ version                     │   │
│ updated_at                  │          │ created_at                  │   │
└──────────────┬──────────────┘          │ updated_at                  │   │
               │                         └──────────────┬──────────────┘   │
               │ 1:N                                    │                  │
               ▼                                        │ N:1              │
┌───────────────────┐                                  │                  │
│ topology_2_node   │                      ┌───────────▼───────────────┐  │
├───────────────────┤                      │         node_type         │  │
│ id (PK)           │                      ├───────────────────────────┤  │
│ topology_id (FK)  │────topology          │ id (PK)                   │  │
│ node_id (FK)      │────────────node      │ code (UK)                 │  │
│ position_x        │                      │ name                      │  │
│ position_y        │                      │ description               │  │
│ added_at          │                      │ icon                      │  │
│ added_by          │                      │ is_system                 │  │
└───────────────────┘                      │ attribute_schema (JSON)   │  │
                                           │ created_at                │  │
                                           │ updated_at                │  │
         node_2_node                       │ created_by                │  │
    ┌─────────────────────┐                └───────────────────────────┘  │
    │ id (PK)             │                                               │
    │ source_id (FK)      │───────────────────────────────────────────────┘
    │ target_id (FK)      │───────────────────────────────────────────────┘
    │ relationship_type   │
    │ direction           │
    │ strength            │
    │ status              │
    │ description         │
    │ created_at          │
    │ updated_at          │
    └─────────────────────┘
```

---

## 数据库表定义

### 1. topology（拓扑图表）

**用途**: 存储拓扑图（原 resource 表中 SUBGRAPH 类型记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID（保留原 resource.id） |
| name | VARCHAR(255) | NOT NULL | 拓扑图名称 |
| description | TEXT | NULL | 拓扑图描述 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'RUNNING' | 状态 |
| coordinator_agent_id | BIGINT | NULL | 协调 Agent ID（预留字段） |
| attributes | JSON | NULL | 扩展属性 |
| created_by | BIGINT | NULL | 创建者 ID |
| version | INT | DEFAULT 0 | 版本号（乐观锁） |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**DDL**:
```sql
CREATE TABLE topology (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '拓扑图名称',
    description TEXT COMMENT '拓扑图描述',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING, STOPPED, MAINTENANCE, OFFLINE',
    coordinator_agent_id BIGINT COMMENT '协调 Agent ID（预留字段）',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_updated_at (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑图表';
```

---

### 2. node（资源节点表）

**用途**: 存储资源节点（原 resource 表中非 SUBGRAPH 类型记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| name | VARCHAR(255) | NOT NULL | 节点名称 |
| description | TEXT | NULL | 节点描述 |
| node_type_id | BIGINT | NOT NULL, FK | 节点类型 ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'RUNNING' | 状态 |
| agent_team_id | BIGINT | NULL | Agent Team ID（预留字段） |
| attributes | JSON | NULL | 扩展属性 |
| created_by | BIGINT | NULL | 创建者 ID |
| version | INT | DEFAULT 0 | 版本号（乐观锁） |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**DDL**:
```sql
CREATE TABLE node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '节点名称',
    description TEXT COMMENT '节点描述',
    node_type_id BIGINT NOT NULL COMMENT '节点类型ID',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态',
    agent_team_id BIGINT COMMENT 'Agent Team ID（预留字段）',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (node_type_id) REFERENCES node_type(id),
    INDEX idx_name (name),
    INDEX idx_type (node_type_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    UNIQUE KEY uk_type_name (node_type_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源节点表';
```

---

### 3. node_type（节点类型表）

**用途**: 原 resource_type 表重命名，移除 SUBGRAPH 类型

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| code | VARCHAR(50) | UK, NOT NULL | 类型编码 |
| name | VARCHAR(100) | NOT NULL | 类型名称 |
| description | TEXT | NULL | 类型描述 |
| icon | VARCHAR(100) | NULL | 图标 URL |
| is_system | BOOLEAN | DEFAULT TRUE | 是否系统预置 |
| attribute_schema | JSON | NULL | 属性定义 Schema |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人 ID |

**类型值域**（不含 SUBGRAPH）:
- SERVER（服务器）
- APPLICATION（应用）
- DATABASE（数据库）
- API（API接口）
- MIDDLEWARE（中间件）
- REPORT（报表）

---

### 4. topology_2_node（拓扑图-节点关联表）🆕

**用途**: 管理拓扑图包含哪些节点（替代原 subgraph_member 的节点部分）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 关联ID |
| topology_id | BIGINT | NOT NULL, FK→topology | 拓扑图 ID |
| node_id | BIGINT | NOT NULL, FK→node | 节点 ID |
| position_x | INT | NULL | 节点在画布上的 X 坐标（可选） |
| position_y | INT | NULL | 节点在画布上的 Y 坐标（可选） |
| added_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 添加时间 |
| added_by | BIGINT | NOT NULL | 添加者 ID |

**DDL**:
```sql
CREATE TABLE topology_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    topology_id BIGINT NOT NULL COMMENT '拓扑图ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    position_x INT COMMENT '节点在画布上的X坐标',
    position_y INT COMMENT '节点在画布上的Y坐标',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',

    UNIQUE KEY uk_topology_node (topology_id, node_id) COMMENT '每个节点在拓扑图中唯一',
    INDEX idx_topology_id (topology_id) COMMENT '按拓扑图查询节点',
    INDEX idx_node_id (node_id) COMMENT '按节点查询所属拓扑图',

    CONSTRAINT fk_t2n_topology FOREIGN KEY (topology_id) REFERENCES topology(id) ON DELETE CASCADE,
    CONSTRAINT fk_t2n_node FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拓扑图-节点关联表';
```

---

### 5. node_2_node（节点依赖关系表）🆕

**用途**: 管理节点间的依赖关系（原 resource_relationship 表重命名并调整）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 关系ID |
| source_id | BIGINT | NOT NULL, FK→node | 源节点 ID |
| target_id | BIGINT | NOT NULL, FK→node | 目标节点 ID |
| relationship_type | VARCHAR(50) | NOT NULL | 关系类型 |
| direction | VARCHAR(20) | NOT NULL | 关系方向 |
| strength | VARCHAR(20) | NOT NULL | 关系强度 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'NORMAL' | 关系状态 |
| description | VARCHAR(500) | NULL | 关系描述 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**关系类型值域**:
- DEPENDENCY（依赖）
- CALL（调用）
- DEPLOYMENT（部署）
- OWNERSHIP（归属）
- ASSOCIATION（关联）

**方向值域**:
- UNIDIRECTIONAL（单向）
- BIDIRECTIONAL（双向）

**强度值域**:
- STRONG（强依赖）
- WEAK（弱依赖）

**DDL**:
```sql
CREATE TABLE node_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    source_id BIGINT NOT NULL COMMENT '源节点ID',
    target_id BIGINT NOT NULL COMMENT '目标节点ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型：DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION',
    direction VARCHAR(20) NOT NULL COMMENT '关系方向：UNIDIRECTIONAL, BIDIRECTIONAL',
    strength VARCHAR(20) NOT NULL COMMENT '关系强度：STRONG, WEAK',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '关系状态：NORMAL, ABNORMAL',
    description VARCHAR(500) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_source_target_type (source_id, target_id, relationship_type) COMMENT '防止重复关系',
    INDEX idx_source (source_id) COMMENT '加速下游查询',
    INDEX idx_target (target_id) COMMENT '加速上游查询',
    INDEX idx_type (relationship_type) COMMENT '加速类型筛选',
    INDEX idx_status (status) COMMENT '加速状态筛选',

    CONSTRAINT fk_n2n_source FOREIGN KEY (source_id) REFERENCES node(id) ON DELETE CASCADE,
    CONSTRAINT fk_n2n_target FOREIGN KEY (target_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点依赖关系表';
```

---

## 领域模型映射

### Topology 领域模型

```java
package com.catface996.aiops.domain.model.topology;

public class Topology {
    private Long id;
    private String name;
    private String description;
    private TopologyStatus status;
    private Long coordinatorAgentId;
    private String attributes;
    private Long createdBy;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联集合（延迟加载）
    private List<TopologyNodeRef> nodeRefs;      // 包含的节点
}

// 拓扑图-节点引用
public class TopologyNodeRef {
    private Long id;
    private Long topologyId;
    private Long nodeId;
    private Integer positionX;
    private Integer positionY;
    private LocalDateTime addedAt;
    private Long addedBy;
}
```

### Node 领域模型

```java
package com.catface996.aiops.domain.model.node;

public class Node {
    private Long id;
    private String name;
    private String description;
    private Long nodeTypeId;
    private NodeType nodeType;
    private NodeStatus status;
    private Long agentTeamId;
    private String attributes;
    private Long createdBy;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联集合（延迟加载）
    private List<NodeRelationship> outgoingRelationships;  // 出边
    private List<NodeRelationship> incomingRelationships;  // 入边
}

// 节点关系
public class NodeRelationship {
    private Long id;
    private Long sourceId;
    private Long targetId;
    private RelationshipType type;
    private RelationshipDirection direction;
    private RelationshipStrength strength;
    private RelationshipStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 数据迁移脚本

### V12__Split_resource_to_topology_and_node.sql

```sql
-- ==============================================
-- Resource 模型分离迁移脚本
-- 需求追溯: FR-001 ~ FR-014
-- 更新: 采用分表设计（topology_2_node, topology_2_topology, node_2_node）
-- ==============================================

-- 1. 创建 topology 表
CREATE TABLE topology (
    id BIGINT PRIMARY KEY COMMENT '主键ID（保留原 resource.id）',
    name VARCHAR(255) NOT NULL COMMENT '拓扑图名称',
    description TEXT COMMENT '拓扑图描述',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态',
    coordinator_agent_id BIGINT COMMENT '协调 Agent ID',
    attributes JSON COMMENT '扩展属性',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑图表';

-- 2. 迁移 SUBGRAPH 数据到 topology 表
INSERT INTO topology (id, name, description, status, attributes, created_by, version, created_at, updated_at)
SELECT r.id, r.name, r.description, r.status, r.attributes, r.created_by, r.version, r.created_at, r.updated_at
FROM resource r
JOIN resource_type rt ON r.resource_type_id = rt.id
WHERE rt.code = 'SUBGRAPH';

-- 3. 创建 node 表
CREATE TABLE node (
    id BIGINT PRIMARY KEY COMMENT '主键ID（保留原 resource.id）',
    name VARCHAR(255) NOT NULL COMMENT '节点名称',
    description TEXT COMMENT '节点描述',
    node_type_id BIGINT NOT NULL COMMENT '节点类型ID',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态',
    agent_team_id BIGINT COMMENT 'Agent Team ID',
    attributes JSON COMMENT '扩展属性',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_type (node_type_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源节点表';

-- 4. 迁移非 SUBGRAPH 数据到 node 表
INSERT INTO node (id, name, description, node_type_id, status, attributes, created_by, version, created_at, updated_at)
SELECT r.id, r.name, r.description, r.resource_type_id, r.status, r.attributes, r.created_by, r.version, r.created_at, r.updated_at
FROM resource r
JOIN resource_type rt ON r.resource_type_id = rt.id
WHERE rt.code != 'SUBGRAPH';

-- 5. 重命名 resource_type 为 node_type
RENAME TABLE resource_type TO node_type;

-- 6. 删除 SUBGRAPH 类型记录
DELETE FROM node_type WHERE code = 'SUBGRAPH';

-- 7. 为 node 表添加外键约束
ALTER TABLE node ADD CONSTRAINT fk_node_type FOREIGN KEY (node_type_id) REFERENCES node_type(id);

-- 8. 添加唯一约束
ALTER TABLE node ADD UNIQUE KEY uk_type_name (node_type_id, name);

-- 9. 创建 topology_2_node 表
CREATE TABLE topology_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    topology_id BIGINT NOT NULL COMMENT '拓扑图ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    position_x INT COMMENT '节点在画布上的X坐标',
    position_y INT COMMENT '节点在画布上的Y坐标',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',
    UNIQUE KEY uk_topology_node (topology_id, node_id),
    INDEX idx_topology_id (topology_id),
    INDEX idx_node_id (node_id),
    CONSTRAINT fk_t2n_topology FOREIGN KEY (topology_id) REFERENCES topology(id) ON DELETE CASCADE,
    CONSTRAINT fk_t2n_node FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑图-节点关联表';

-- 10. 从 subgraph_member 迁移数据到 topology_2_node
-- 注意：只迁移 node 类型的成员，不支持拓扑图嵌套
INSERT INTO topology_2_node (topology_id, node_id, added_at, added_by)
SELECT sm.subgraph_id, sm.member_id, sm.added_at, sm.added_by
FROM subgraph_member sm
JOIN node n ON sm.member_id = n.id;

-- 11. 创建 node_2_node 表（重命名 resource_relationship）
CREATE TABLE node_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    source_id BIGINT NOT NULL COMMENT '源节点ID',
    target_id BIGINT NOT NULL COMMENT '目标节点ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型',
    direction VARCHAR(20) NOT NULL COMMENT '关系方向',
    strength VARCHAR(20) NOT NULL COMMENT '关系强度',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '关系状态',
    description VARCHAR(500) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_source_target_type (source_id, target_id, relationship_type),
    INDEX idx_source (source_id),
    INDEX idx_target (target_id),
    INDEX idx_type (relationship_type),
    INDEX idx_status (status),
    CONSTRAINT fk_n2n_source FOREIGN KEY (source_id) REFERENCES node(id) ON DELETE CASCADE,
    CONSTRAINT fk_n2n_target FOREIGN KEY (target_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点依赖关系表';

-- 12. 从 resource_relationship 迁移数据
INSERT INTO node_2_node (source_id, target_id, relationship_type, direction, strength, status, description, created_at, updated_at)
SELECT rr.source_resource_id, rr.target_resource_id, rr.relationship_type, rr.direction, rr.strength, rr.status, rr.description, rr.created_at, rr.updated_at
FROM resource_relationship rr
JOIN node n1 ON rr.source_resource_id = n1.id
JOIN node n2 ON rr.target_resource_id = n2.id;

-- 13. 删除旧表
DROP TABLE subgraph_member;
DROP TABLE resource_relationship;
DROP TABLE resource;
```

---

## 验证查询

```sql
-- 1. 验证 topology 表
SELECT COUNT(*) as topology_count FROM topology;

-- 2. 验证 node 表
SELECT COUNT(*) as node_count FROM node;

-- 3. 验证 topology_2_node 关联
SELECT COUNT(*) as t2n_count FROM topology_2_node;

-- 4. 验证 node_2_node 关系
SELECT COUNT(*) as n2n_count FROM node_2_node;

-- 5. 验证 node_type 中无 SUBGRAPH
SELECT COUNT(*) FROM node_type WHERE code = 'SUBGRAPH';  -- 预期: 0

-- 6. 验证外键约束
SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND TABLE_NAME IN ('topology_2_node', 'node_2_node', 'node');
```
