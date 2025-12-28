# Data Model: Node-Agent 绑定功能

**Feature**: 031-node-agent-binding
**Date**: 2025-12-28

## Entity Relationship Diagram

```
+----------------+          +------------------+          +----------------+
|     Node       |          |  NodeAgentRelation|          |     Agent      |
+----------------+          +------------------+          +----------------+
| id (PK)        |<-------->| id (PK)          |<-------->| id (PK)        |
| name           |    1:N   | node_id (FK)     |   N:1    | name           |
| description    |          | agent_id (FK)    |          | role           |
| node_type_id   |          | created_at       |          | specialty      |
| status         |          | deleted          |          | ...            |
| ...            |          +------------------+          +----------------+
+----------------+
```

## Entities

### NodeAgentRelation (新增)

表示 Node 与 Agent 之间的多对多关联关系。

#### 数据库表: `node_2_agent`

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 关联记录 ID |
| node_id | BIGINT | NOT NULL, INDEX | 资源节点 ID |
| agent_id | BIGINT | NOT NULL, INDEX | Agent ID |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除标记 |

#### 索引

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | PRIMARY KEY | id | 主键 |
| idx_node_id | INDEX | node_id | 按节点查询优化 |
| idx_agent_id | INDEX | agent_id | 按 Agent 查询优化 |
| uk_node_agent | UNIQUE | (node_id, agent_id, deleted) | 防止重复绑定 |

#### DDL

```sql
-- V21__Create_node_agent_relation_table.sql
CREATE TABLE node_2_agent (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联记录 ID',
    node_id     BIGINT          NOT NULL COMMENT '资源节点 ID',
    agent_id    BIGINT          NOT NULL COMMENT 'Agent ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_node_id (node_id),
    INDEX idx_agent_id (agent_id),
    UNIQUE INDEX uk_node_agent (node_id, agent_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Node-Agent 关联表';
```

### Node (已存在)

资源节点实体，表示实际的 IT 资源。

**表名**: `node`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(255) | 节点名称 |
| description | TEXT | 节点描述 |
| node_type_id | BIGINT | 节点类型 ID |
| status | VARCHAR(20) | 状态 |
| agent_team_id | BIGINT | Agent Team ID (预留) |
| attributes | JSON | 扩展属性 |
| created_by | BIGINT | 创建者 ID |
| version | INT | 版本号（乐观锁） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### Agent (已存在)

AI Agent 实体，用于执行自动化诊断、监控和分析任务。

**表名**: `agent`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | Agent 名称 |
| role | VARCHAR(32) | Agent 角色 |
| specialty | VARCHAR(200) | 专业领域 |
| prompt_template_id | BIGINT | 提示词模板 ID |
| model | VARCHAR(100) | AI 模型标识 |
| temperature | DOUBLE | 温度参数 |
| top_p | DOUBLE | Top P 参数 |
| max_tokens | INT | 最大 token 数 |
| max_runtime | INT | 最长运行时间 |
| warnings | INT | 警告数量 |
| critical | INT | 严重问题数量 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 软删除标记 |

## Domain Model

### NodeAgentRelation.java

```java
package com.catface996.aiops.domain.model.node;

import java.time.LocalDateTime;

/**
 * Node-Agent 关联领域模型
 *
 * 表示 ResourceNode 与 Agent 之间的绑定关系
 */
public class NodeAgentRelation {

    /** 关联记录 ID */
    private Long id;

    /** 资源节点 ID */
    private Long nodeId;

    /** Agent ID */
    private Long agentId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 软删除标记 */
    private Boolean deleted;

    // 构造函数
    public NodeAgentRelation() {
        this.deleted = false;
    }

    // 工厂方法
    public static NodeAgentRelation create(Long nodeId, Long agentId) {
        NodeAgentRelation relation = new NodeAgentRelation();
        relation.setNodeId(nodeId);
        relation.setAgentId(agentId);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setDeleted(false);
        return relation;
    }

    // 业务方法
    public void markDeleted() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    // Getters and Setters...
}
```

## Repository Interface

### NodeAgentRelationRepository.java

```java
package com.catface996.aiops.repository.node;

import com.catface996.aiops.domain.model.node.NodeAgentRelation;
import java.util.List;
import java.util.Optional;

/**
 * Node-Agent 关联仓储接口
 */
public interface NodeAgentRelationRepository {

    /**
     * 保存关联关系
     */
    void save(NodeAgentRelation relation);

    /**
     * 根据 nodeId 和 agentId 查找关联（未删除）
     */
    Optional<NodeAgentRelation> findByNodeIdAndAgentId(Long nodeId, Long agentId);

    /**
     * 根据 nodeId 查询所有关联的 agentId 列表（未删除）
     */
    List<Long> findAgentIdsByNodeId(Long nodeId);

    /**
     * 根据 agentId 查询所有关联的 nodeId 列表（未删除）
     */
    List<Long> findNodeIdsByAgentId(Long agentId);

    /**
     * 软删除关联关系
     */
    void softDelete(Long id);

    /**
     * 根据 nodeId 软删除所有关联（级联删除）
     */
    void softDeleteByNodeId(Long nodeId);

    /**
     * 根据 agentId 软删除所有关联（级联删除）
     */
    void softDeleteByAgentId(Long agentId);

    /**
     * 检查关联是否存在（未删除）
     */
    boolean existsByNodeIdAndAgentId(Long nodeId, Long agentId);
}
```

## DTO Classes

### NodeAgentRelationDTO.java

```java
package com.catface996.aiops.application.api.dto.node;

import java.time.LocalDateTime;

/**
 * Node-Agent 关联 DTO
 */
public class NodeAgentRelationDTO {

    private Long id;
    private Long nodeId;
    private Long agentId;
    private LocalDateTime createdAt;

    // Getters and Setters...
}
```

### Request DTOs

#### BindAgentRequest.java

```java
@Schema(description = "绑定 Agent 请求")
public class BindAgentRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", required = true)
    private Long nodeId;

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", required = true)
    private Long agentId;

    @Schema(description = "操作人ID（网关注入）", hidden = true)
    private Long operatorId;
}
```

#### UnbindAgentRequest.java

```java
@Schema(description = "解绑 Agent 请求")
public class UnbindAgentRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", required = true)
    private Long nodeId;

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", required = true)
    private Long agentId;

    @Schema(description = "操作人ID（网关注入）", hidden = true)
    private Long operatorId;
}
```

#### ListAgentsByNodeRequest.java

```java
@Schema(description = "查询节点关联的 Agent 列表请求")
public class ListAgentsByNodeRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", required = true)
    private Long nodeId;
}
```

#### ListNodesByAgentRequest.java

```java
@Schema(description = "查询 Agent 关联的节点列表请求")
public class ListNodesByAgentRequest {

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", required = true)
    private Long agentId;
}
```

## Data Constraints

### 业务约束

1. **唯一性约束**: 同一 Node 和 Agent 只能存在一条有效绑定记录
2. **有效性验证**: 绑定时必须验证 Node 和 Agent 都存在且未被删除
3. **软删除**: 删除关联不物理删除，仅标记 `deleted = 1`

### 容量约束

- 单个 Node 最多关联 100+ Agent
- 单个 Agent 最多关联 1000+ Node
- 查询响应时间 < 1 秒

### 级联删除规则

- 删除 Node 时：软删除所有 `node_id = ?` 的关联记录
- 删除 Agent 时：软删除所有 `agent_id = ?` 的关联记录
