# Data Model - Subgraph Management v2.0

**Feature**: F08 - 子图管理
**Date**: 2025-12-22
**Version**: 2.0

## Overview

子图作为资源类型（SUBGRAPH）统一管理，复用 `resource` 表存储，新增 `subgraph_member` 表存储成员关联。

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        resource_type                             │
│  (SUBGRAPH type predefined)                                      │
└─────────────────────────┬───────────────────────────────────────┘
                          │ 1:N
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                          resource                                │
│  (Subgraph stored here with type=SUBGRAPH)                      │
└───────────┬─────────────────────────────────┬───────────────────┘
            │ 1:N                             │ 1:N
            ▼                                 ▼
┌───────────────────────────┐    ┌────────────────────────────────┐
│    resource_permission    │    │       subgraph_member          │
│  (Reused for subgraph)    │    │  (NEW: member associations)    │
└───────────────────────────┘    └──────────────┬─────────────────┘
                                                │ N:1
                                                ▼
                                 ┌────────────────────────────────┐
                                 │           resource              │
                                 │  (Member resource, can be any   │
                                 │   type including SUBGRAPH)      │
                                 └────────────────────────────────┘
```

## Tables

### 1. resource_type (SUBGRAPH 类型 - 预定义)

预定义的子图资源类型：

```sql
INSERT INTO resource_type (code, name, description, icon, is_system, created_at, updated_at)
VALUES ('SUBGRAPH', '子图', '资源分组容器，支持嵌套', 'folder-tree', true, NOW(), NOW());
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| code | VARCHAR(50) | 'SUBGRAPH' |
| name | VARCHAR(100) | '子图' |
| description | TEXT | '资源分组容器，支持嵌套' |
| icon | VARCHAR(50) | 'folder-tree' |
| is_system | BOOLEAN | true |

### 2. resource (复用 - 存储子图)

子图作为资源存储，复用现有 resource 表：

| 字段 | 类型 | 说明 | 子图使用 |
|------|------|------|----------|
| id | BIGINT | 主键 | ✅ |
| resource_type_id | BIGINT | FK → resource_type | 指向 SUBGRAPH 类型 |
| name | VARCHAR(255) | 名称 | 子图名称 |
| description | TEXT | 描述 | 子图描述 |
| tags | JSON | 标签数组 | ✅ |
| metadata | JSON | 元数据 | ✅ |
| created_by | BIGINT | 创建者 | ✅ |
| created_at | TIMESTAMP | 创建时间 | ✅ |
| updated_at | TIMESTAMP | 更新时间 | ✅ |
| version | INT | 乐观锁版本 | ✅ |

### 3. resource_permission (复用 - 子图权限)

复用资源权限表管理子图权限：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| resource_id | BIGINT | FK → resource | 子图 ID |
| user_id | BIGINT | 用户 ID |
| role | ENUM('OWNER','VIEWER') | 权限角色 |
| granted_at | TIMESTAMP | 授权时间 |
| granted_by | BIGINT | 授权人 |

### 4. subgraph_member (新增)

子图成员关联表：

```sql
CREATE TABLE subgraph_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subgraph_id BIGINT NOT NULL COMMENT '父子图资源ID',
    member_id BIGINT NOT NULL COMMENT '成员资源ID（可以是任意类型包括子图）',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',

    UNIQUE KEY uk_subgraph_member (subgraph_id, member_id),
    INDEX idx_subgraph_id (subgraph_id),
    INDEX idx_member_id (member_id),

    CONSTRAINT fk_subgraph_member_subgraph
        FOREIGN KEY (subgraph_id) REFERENCES resource(id) ON DELETE CASCADE,
    CONSTRAINT fk_subgraph_member_member
        FOREIGN KEY (member_id) REFERENCES resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子图成员关联表';
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| subgraph_id | BIGINT | NOT NULL, FK | 父子图资源 ID |
| member_id | BIGINT | NOT NULL, FK | 成员资源 ID |
| added_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 添加时间 |
| added_by | BIGINT | NOT NULL | 添加者用户 ID |

**索引**:
- `uk_subgraph_member (subgraph_id, member_id)` - 唯一约束，防止重复添加
- `idx_subgraph_id (subgraph_id)` - 查询子图成员
- `idx_member_id (member_id)` - 反向查询资源所属子图

**外键**:
- `subgraph_id → resource(id) ON DELETE CASCADE` - 子图删除时级联删除成员关联
- `member_id → resource(id) ON DELETE CASCADE` - 成员资源删除时自动移除关联

## Domain Entities

### 1. SubgraphMember (新实体)

```java
public class SubgraphMember {
    private Long id;
    private Long subgraphId;      // 父子图资源 ID
    private Long memberId;        // 成员资源 ID
    private LocalDateTime addedAt;
    private Long addedBy;

    // 派生属性 (通过 JOIN 获取)
    private String memberName;     // 成员资源名称
    private String memberType;     // 成员资源类型 (e.g., SERVER, APPLICATION, SUBGRAPH)
    private String memberStatus;   // 成员资源状态
    private boolean isSubgraph;    // 成员是否为子图类型
    private Integer nestedMemberCount; // 如果是子图，其成员数量
}
```

### 2. SubgraphTopology (扩展)

```java
public class SubgraphTopology {
    private Long subgraphId;
    private String subgraphName;
    private List<TopologyNode> nodes;
    private List<TopologyEdge> edges;
    private List<SubgraphBoundary> boundaries; // 嵌套子图边界
    private int nodeCount;
    private int edgeCount;
    private int maxDepth;
}

public class TopologyNode {
    private Long id;
    private String name;
    private String type;
    private String status;
    private boolean isSubgraph;
    private boolean expanded;
    private Long parentSubgraphId;
}

public class SubgraphBoundary {
    private Long subgraphId;
    private String subgraphName;
    private List<Long> memberIds;
}
```

### 3. AncestorInfo (新实体)

```java
public class AncestorInfo {
    private Long subgraphId;
    private String subgraphName;
    private int depth;  // 距离查询子图的层数 (1 = 直接父级)
}
```

## Data Constraints

### 业务约束

| 约束 | 规则 | 实现方式 |
|------|------|----------|
| 子图名称唯一 | 系统范围内全局唯一 | resource 表唯一索引 |
| 至少一个 Owner | 子图必须有至少一个 Owner | 应用层校验 |
| 最多 10 个 Owner | 每个子图最多 10 个 Owner | 应用层校验 |
| 最多 500 个成员 | 每个子图最多 500 个直接成员 | 应用层校验 |
| 最大嵌套深度 10 | 子图嵌套不超过 10 层 | 应用层校验 |
| 禁止循环引用 | 不允许子图 A 包含 B，B 包含 A | 添加时 DFS 检测 |
| 删除前必须为空 | 子图删除前必须移除所有成员 | 应用层校验 |

### 数据完整性约束

| 约束 | SQL 实现 |
|------|----------|
| 子图必须是 SUBGRAPH 类型 | 应用层校验 resource_type |
| 成员必须存在 | FK → resource(id) |
| 唯一成员关联 | UNIQUE(subgraph_id, member_id) |
| 级联删除 | ON DELETE CASCADE |

## Query Patterns

### 1. 查询子图成员列表 (分页)

```sql
SELECT
    sm.id,
    sm.subgraph_id,
    sm.member_id,
    sm.added_at,
    sm.added_by,
    r.name AS member_name,
    rt.code AS member_type,
    r.status AS member_status,
    (rt.code = 'SUBGRAPH') AS is_subgraph,
    (SELECT COUNT(*) FROM subgraph_member WHERE subgraph_id = sm.member_id) AS nested_member_count
FROM subgraph_member sm
JOIN resource r ON sm.member_id = r.id
JOIN resource_type rt ON r.resource_type_id = rt.id
WHERE sm.subgraph_id = ?
ORDER BY sm.added_at DESC
LIMIT ? OFFSET ?;
```

### 2. 查询子图拓扑 (含关系)

```sql
-- Step 1: 获取所有成员 ID
SELECT member_id FROM subgraph_member WHERE subgraph_id = ?;

-- Step 2: 获取成员间的关系
SELECT * FROM relationship
WHERE source_resource_id IN (?) AND target_resource_id IN (?);
```

### 3. 循环检测 - 查询祖先子图

```sql
WITH RECURSIVE ancestors AS (
    -- 直接父级
    SELECT sm.subgraph_id, 1 AS depth
    FROM subgraph_member sm
    WHERE sm.member_id = ?  -- 当前子图 ID

    UNION ALL

    -- 递归查询更高层级的祖先
    SELECT sm.subgraph_id, a.depth + 1
    FROM subgraph_member sm
    JOIN ancestors a ON sm.member_id = a.subgraph_id
    WHERE a.depth < 10  -- 防止无限递归
)
SELECT DISTINCT subgraph_id, depth FROM ancestors;
```

### 4. 递归展开嵌套子图

```sql
WITH RECURSIVE nested_members AS (
    -- 直接成员
    SELECT
        sm.member_id,
        sm.subgraph_id AS parent_id,
        0 AS depth
    FROM subgraph_member sm
    WHERE sm.subgraph_id = ?  -- 根子图 ID

    UNION ALL

    -- 递归展开嵌套子图的成员
    SELECT
        sm.member_id,
        sm.subgraph_id AS parent_id,
        nm.depth + 1
    FROM subgraph_member sm
    JOIN nested_members nm ON sm.subgraph_id = nm.member_id
    JOIN resource r ON nm.member_id = r.id
    JOIN resource_type rt ON r.resource_type_id = rt.id
    WHERE rt.code = 'SUBGRAPH' AND nm.depth < ?  -- maxDepth 参数
)
SELECT DISTINCT member_id, parent_id, depth FROM nested_members;
```

## Migration Strategy

### Step 1: 添加 SUBGRAPH 资源类型

```sql
INSERT INTO resource_type (code, name, description, icon, is_system, created_at, updated_at)
VALUES ('SUBGRAPH', '子图', '资源分组容器，支持嵌套', 'folder-tree', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();
```

### Step 2: 创建 subgraph_member 表

```sql
CREATE TABLE IF NOT EXISTS subgraph_member (...);
```

### Step 3: 迁移现有数据 (可选，如果有 v1.0 数据)

```sql
-- 迁移子图到 resource 表
INSERT INTO resource (resource_type_id, name, description, tags, metadata, created_by, created_at, updated_at, version)
SELECT
    (SELECT id FROM resource_type WHERE code = 'SUBGRAPH'),
    s.name, s.description, s.tags, s.metadata, s.created_by, s.created_at, s.updated_at, s.version
FROM subgraph s;

-- 迁移权限到 resource_permission 表
INSERT INTO resource_permission (resource_id, user_id, role, granted_at, granted_by)
SELECT
    (SELECT r.id FROM resource r
     JOIN resource_type rt ON r.resource_type_id = rt.id
     WHERE rt.code = 'SUBGRAPH' AND r.name = s.name),
    sp.user_id, sp.role, sp.granted_at, sp.granted_by
FROM subgraph_permission sp
JOIN subgraph s ON sp.subgraph_id = s.id;

-- 迁移成员关联到 subgraph_member 表
INSERT INTO subgraph_member (subgraph_id, member_id, added_at, added_by)
SELECT
    (SELECT r.id FROM resource r
     JOIN resource_type rt ON r.resource_type_id = rt.id
     WHERE rt.code = 'SUBGRAPH' AND r.name = s.name),
    sr.resource_id, sr.added_at, sr.added_by
FROM subgraph_resource sr
JOIN subgraph s ON sr.subgraph_id = s.id;
```

---

**Document Version**: 2.0
**Last Updated**: 2025-12-22
