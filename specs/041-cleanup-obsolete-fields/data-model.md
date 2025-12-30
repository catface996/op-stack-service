# Data Model Changes: 清理数据库废弃字段

**Feature**: 041-cleanup-obsolete-fields
**Date**: 2025-12-30

## 实体变更概览

| 实体 | 变更类型 | 移除字段 | 影响层级 |
|------|----------|----------|----------|
| Node | 字段移除 | agentTeamId | Domain, DTO, PO |
| Topology | 字段移除 | coordinatorAgentId, globalSupervisorAgentId | Domain, DTO, PO |
| NodeAgentRelation | 实体删除 | (整个实体) | Domain, Repository, PO |

## 1. Node 实体

### 当前结构 (Before)

```java
public class Node {
    private Long id;
    private String name;
    private String description;
    private Long nodeTypeId;
    private NodeStatus status;
    private NodeLayer layer;
    private Long agentTeamId;        // ❌ 移除
    private String attributes;
    private Long createdBy;
    private Integer version;
    // ...
}
```

### 目标结构 (After)

```java
public class Node {
    private Long id;
    private String name;
    private String description;
    private Long nodeTypeId;
    private NodeStatus status;
    private NodeLayer layer;
    // agentTeamId 已移除
    private String attributes;
    private Long createdBy;
    private Integer version;
    // ...
}
```

### 数据库表变更

```sql
-- V33: 移除 node.agent_team_id
ALTER TABLE node DROP COLUMN agent_team_id;
```

## 2. Topology 实体

### 当前结构 (Before)

```java
public class Topology {
    private Long id;
    private String name;
    private String description;
    private TopologyStatus status;
    private Long coordinatorAgentId;       // ❌ 移除 (P2)
    private Long globalSupervisorAgentId;  // ❌ 移除 (P3)
    private String attributes;
    private Long createdBy;
    private Integer version;
    // ...
}
```

### 目标结构 (After)

```java
public class Topology {
    private Long id;
    private String name;
    private String description;
    private TopologyStatus status;
    // coordinatorAgentId 已移除
    // globalSupervisorAgentId 已移除
    private String attributes;
    private Long createdBy;
    private Integer version;
    // ...
}
```

### 数据库表变更

```sql
-- V34: 移除 topology.coordinator_agent_id
ALTER TABLE topology DROP COLUMN coordinator_agent_id;

-- V35: 移除 topology.global_supervisor_agent_id
ALTER TABLE topology DROP COLUMN global_supervisor_agent_id;
```

## 3. NodeAgentRelation 实体 (删除)

### 当前结构 (Before)

```java
public class NodeAgentRelation {
    private Long id;
    private Long nodeId;
    private Long agentId;
    private LocalDateTime createdAt;
}
```

### 目标结构 (After)

**整个实体删除**。Node-Agent 绑定关系通过 `AgentBound` 实体管理。

### 数据库表变更

```sql
-- V36: 删除 node_2_agent 表
DROP TABLE IF EXISTS node_2_agent;
```

## 4. AgentBound 实体 (保留，无变更)

AgentBound 实体是统一的 Agent 绑定管理表，已包含所有需要的绑定关系：

```java
public class AgentBound {
    private Long id;
    private Long agentId;
    private String hierarchyLevel;  // GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
    private Long entityId;
    private String entityType;      // TOPOLOGY, NODE
    private LocalDateTime createdAt;
    private Integer deleted;
}
```

## 5. DTO 变更

### NodeDTO

```java
// Before
public class NodeDTO {
    private Long id;
    private String name;
    private Long agentTeamId;  // ❌ 移除
    // ...
}

// After
public class NodeDTO {
    private Long id;
    private String name;
    // agentTeamId 已移除
    // ...
}
```

### TopologyDTO

```java
// Before
public class TopologyDTO {
    private Long id;
    private String name;
    private Long coordinatorAgentId;       // ❌ 移除
    private Long globalSupervisorAgentId;  // ❌ 移除
    // ...
}

// After
public class TopologyDTO {
    private Long id;
    private String name;
    // coordinatorAgentId 已移除
    // globalSupervisorAgentId 已移除
    // ...
}
```

### CreateNodeRequest / UpdateNodeRequest

```java
// Before
public class CreateNodeRequest {
    private Long operatorId;
    private String name;
    private Long agentTeamId;  // ❌ 移除
    // ...
}

// After
public class CreateNodeRequest {
    private Long operatorId;
    private String name;
    // agentTeamId 已移除
    // ...
}
```

### CreateTopologyRequest / UpdateTopologyRequest

```java
// Before
public class CreateTopologyRequest {
    private Long operatorId;
    private String name;
    private Long coordinatorAgentId;  // ❌ 移除
    // ...
}

// After
public class CreateTopologyRequest {
    private Long operatorId;
    private String name;
    // coordinatorAgentId 已移除
    // ...
}
```

## 6. Mapper XML 变更

### NodeMapper.xml

```xml
<!-- Before -->
<sql id="Base_Column_List">
    id, name, description, node_type_id, status, layer, agent_team_id, attributes,
    created_by, created_at, updated_by, updated_at, version, deleted
</sql>

<!-- After -->
<sql id="Base_Column_List">
    id, name, description, node_type_id, status, layer, attributes,
    created_by, created_at, updated_by, updated_at, version, deleted
</sql>
```

### TopologyMapper.xml

```xml
<!-- Before -->
<sql id="Base_Column_List">
    id, name, description, status, coordinator_agent_id, global_supervisor_agent_id,
    attributes, created_by, created_at, updated_by, updated_at, version, deleted
</sql>

<!-- After -->
<sql id="Base_Column_List">
    id, name, description, status,
    attributes, created_by, created_at, updated_by, updated_at, version, deleted
</sql>
```

## 7. 删除的文件清单

| 层级 | 文件路径 |
|------|----------|
| Domain Model | `domain/domain-model/.../model/node/NodeAgentRelation.java` |
| Repository API | `domain/repository-api/.../repository/node/NodeAgentRelationRepository.java` |
| Repository Impl | `infrastructure/repository/mysql-impl/.../impl/node/NodeAgentRelationRepositoryImpl.java` |
| PO | `infrastructure/repository/mysql-impl/.../po/node/NodeAgentRelationPO.java` |
| Mapper Interface | `infrastructure/repository/mysql-impl/.../mapper/node/NodeAgentRelationMapper.java` |
| Mapper XML | `infrastructure/repository/mysql-impl/.../resources/mapper/node/NodeAgentRelationMapper.xml` |
