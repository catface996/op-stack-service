# Data Model: Agent Tools 绑定

**Feature**: 030-agent-tools | **Date**: 2025-12-28

## 实体关系图

```
┌─────────────┐         ┌─────────────────┐         ┌─────────────┐
│    Agent    │ 1     * │  AgentToolRel   │ *     1 │    Tool     │
│─────────────│─────────│─────────────────│─────────│─────────────│
│ id          │         │ id              │         │ id          │
│ name        │         │ agent_id (FK)   │         │ name        │
│ role        │         │ tool_id (FK)    │         │ description │
│ specialty   │         │ created_at      │         │ type        │
│ ...         │         │ deleted         │         │ ...         │
└─────────────┘         └─────────────────┘         └─────────────┘
```

**说明**：Agent 与 Tool 是多对多关系，通过 `agent_2_tool` 关联表实现。

## 数据库表设计

### 新增表：agent_2_tool

```sql
-- V18__Create_agent_tool_relation_table.sql
-- Agent-Tool 关联表
-- Feature: 030-agent-tools
-- Date: 2025-12-28

CREATE TABLE agent_2_tool (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    agent_id    BIGINT       NOT NULL COMMENT 'Agent ID',
    tool_id     BIGINT       NOT NULL COMMENT 'Tool ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_agent_2_tool_agent_id (agent_id),
    INDEX idx_agent_2_tool_tool_id (tool_id),
    UNIQUE INDEX uk_agent_tool (agent_id, tool_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 与 Tool 关联表';
```

### 字段说明

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| agent_id | BIGINT | NOT NULL, INDEX | Agent ID |
| tool_id | BIGINT | NOT NULL, INDEX | Tool ID |
| created_at | DATETIME | NOT NULL, DEFAULT NOW | 创建时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除标记 |

### 索引设计

| 索引名 | 字段 | 类型 | 用途 |
|--------|------|------|------|
| PRIMARY | id | 主键 | 唯一标识 |
| idx_agent_2_tool_agent_id | agent_id | 普通索引 | 按 Agent 查询 Tools |
| idx_agent_2_tool_tool_id | tool_id | 普通索引 | 按 Tool 查询 Agents |
| uk_agent_tool | agent_id, tool_id, deleted | 唯一索引 | 防止重复绑定 |

## 领域模型设计

### AgentToolRelation（新增）

**文件**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/AgentToolRelation.java`

```java
public class AgentToolRelation {
    private Long id;
    private Long agentId;
    private Long toolId;
    private LocalDateTime createdAt;
    private Boolean deleted;

    // 工厂方法
    public static AgentToolRelation create(Long agentId, Long toolId);

    // 业务方法
    public void markDeleted();
    public boolean isDeleted();
}
```

### Agent（修改）

**文件**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java`

**新增字段**：
```java
/**
 * 关联的 Tool ID 列表
 */
private List<Long> toolIds;
```

**新增方法**：
```java
public List<Long> getToolIds();
public void setToolIds(List<Long> toolIds);
```

## 持久化对象设计

### AgentToolRelationPO（新增）

**文件**: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/AgentToolRelationPO.java`

```java
@Data
@TableName("agent_2_tool")
public class AgentToolRelationPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long agentId;

    private Long toolId;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
```

## 仓储接口设计

### AgentToolRelationRepository（新增）

**文件**: `domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/AgentToolRelationRepository.java`

```java
public interface AgentToolRelationRepository {

    /**
     * 批量保存 Agent-Tool 关联
     */
    void saveAll(List<AgentToolRelation> relations);

    /**
     * 删除 Agent 的所有 Tool 关联
     */
    void deleteByAgentId(Long agentId);

    /**
     * 查询 Agent 绑定的所有 Tool ID
     */
    List<Long> findToolIdsByAgentId(Long agentId);

    /**
     * 批量查询多个 Agent 绑定的 Tool ID
     */
    Map<Long, List<Long>> findToolIdsByAgentIds(List<Long> agentIds);

    /**
     * 检查 Tool 是否存在（调用 Tool 仓储）
     */
    List<Long> filterExistingToolIds(List<Long> toolIds);
}
```

## DTO 修改

### CreateAgentRequest

**新增字段**：
```java
@Schema(description = "绑定的 Tool ID 列表")
private List<Long> toolIds;
```

### UpdateAgentRequest

**新增字段**：
```java
@Schema(description = "绑定的 Tool ID 列表（全量替换）")
private List<Long> toolIds;
```

### AgentDTO

**新增字段**：
```java
@Schema(description = "绑定的 Tool ID 列表")
private List<Long> toolIds;
```

## 数据流转

### 创建 Agent 时绑定 Tools

```
CreateAgentRequest.toolIds
    ↓
AgentApplicationServiceImpl.createAgent()
    ↓ 去重 + 过滤无效 ID
    ↓ 创建 Agent
    ↓ 批量创建 AgentToolRelation
AgentToolRelationRepository.saveAll()
```

### 更新 Agent 时全量替换 Tools

```
UpdateAgentRequest.toolIds
    ↓
AgentApplicationServiceImpl.updateAgent()
    ↓ 检查 Agent 状态（非 WORKING/THINKING）
    ↓ 删除现有 Tool 关联
AgentToolRelationRepository.deleteByAgentId()
    ↓ 去重 + 过滤无效 ID
    ↓ 批量创建新 AgentToolRelation
AgentToolRelationRepository.saveAll()
```

### 查询 Agent 时填充 toolIds

```
AgentRepository.findById()
    ↓
AgentToolRelationRepository.findToolIdsByAgentId()
    ↓ 填充到 Agent.toolIds
Agent.setToolIds(toolIds)
    ↓
AgentApplicationServiceImpl.toDTO()
    ↓
AgentDTO (含 toolIds)
```
