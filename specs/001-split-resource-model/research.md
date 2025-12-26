# Research: Resource 模型分离重构

**Feature**: [spec.md](./spec.md) | **Date**: 2025-12-26 | **Updated**: 2025-12-26

## 研究任务

### R1: 数据库迁移策略

**任务**: 研究 MySQL 表拆分的最佳实践，确保数据完整性和最小停机时间

**Decision**: 采用蓝绿部署兼容的分步迁移策略

**Rationale**:
1. **分步迁移而非原子迁移**：先创建新表并复制数据，再更新外键，最后删除旧表
2. **保留原 ID**：迁移时保持 resource.id → topology.id / node.id 的映射
3. **外键先解除后重建**：避免迁移过程中的约束冲突

**Alternatives considered**:
- **原子迁移（单个事务）**：风险高，大表迁移可能超时
- **使用触发器同步**：复杂度高，性能影响大

**迁移步骤**:
```sql
-- Step 1: 创建新表（topology, node, node_type, topology_2_node, node_2_node）
-- Step 2: 复制数据（INSERT INTO ... SELECT）
-- Step 3: 删除旧表（resource, resource_type, subgraph_member, resource_relationship）
```

---

### R2: 关联表设计（更新）

**任务**: 研究 topology-node 和 node-node 关联表的最佳设计方案

**Decision**: 采用分表设计，创建 `topology_2_node` 和 `node_2_node` 两张表

**Rationale**:
- **分表优于单表多态**：每个外键字段都能有明确的数据库层约束
- **不支持拓扑图嵌套**：简化设计，降低复杂度
- **表名即语义**：无需应用层判断关联类型
- **独立扩展**：`topology_2_node` 可扩展位置信息字段

**技术方案**:
```sql
-- topology_2_node: 拓扑图包含哪些节点
CREATE TABLE topology_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topology_id BIGINT NOT NULL,  -- FK → topology.id
    node_id BIGINT NOT NULL,      -- FK → node.id
    position_x INT,               -- 画布位置（扩展字段）
    position_y INT,
    added_at TIMESTAMP,
    added_by BIGINT,
    CONSTRAINT fk_t2n_topology FOREIGN KEY (topology_id) REFERENCES topology(id) ON DELETE CASCADE,
    CONSTRAINT fk_t2n_node FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE
);

-- node_2_node: 节点间依赖关系（原 resource_relationship 重命名）
CREATE TABLE node_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_id BIGINT NOT NULL,    -- FK → node.id
    target_id BIGINT NOT NULL,    -- FK → node.id
    relationship_type VARCHAR(50),
    direction VARCHAR(20),
    strength VARCHAR(20),
    status VARCHAR(20),
    description VARCHAR(500),
    CONSTRAINT fk_n2n_source FOREIGN KEY (source_id) REFERENCES node(id) ON DELETE CASCADE,
    CONSTRAINT fk_n2n_target FOREIGN KEY (target_id) REFERENCES node(id) ON DELETE CASCADE
);
```

**Alternatives considered**:
- **单表多态（subgraph_member）**：member_id 无法加外键约束，依赖应用层校验 → 拒绝
- **支持拓扑图嵌套（topology_2_topology）**：增加复杂度，当前不需要 → 简化掉

---

### R3: API 向后兼容方案

**任务**: 研究 Spring Boot 中 API 路径变更的向后兼容实现

**Decision**: 使用 @Deprecated 注解 + 重定向提示的组合方案

**Rationale**:
1. 保留原 `/api/v1/resources/*` 路径，标记 @Deprecated
2. 响应中添加 `X-Deprecated-API` header 和 `deprecation_notice` 字段
3. 3 个月后移除旧路径

**实现方式**:
```java
@Deprecated
@PostMapping("/resources/query")
public ResponseEntity<Result<PageResult<NodeDTO>>> queryResourcesDeprecated(...) {
    return ResponseEntity.ok()
        .header("X-Deprecated-API", "Use /api/v1/nodes/query instead")
        .body(nodeApplicationService.queryNodes(...));
}
```

---

### R4: 领域模型设计

**任务**: 研究 Topology 和 Node 领域模型的最佳设计

**Decision**: 完全分离的领域模型，不共享基类

**Rationale**:
1. **语义差异大**：Topology 是容器，Node 是资源实体
2. **字段差异**：
   - Topology: `coordinator_agent_id`（协调 Agent）
   - Node: `node_type_id`, `agent_team_id`（Team 绑定）
3. **避免过度抽象**：共享基类会增加理解成本，实际复用价值低

**模型设计**:
```java
// Topology 领域模型
public class Topology {
    Long id;
    String name;
    String description;
    TopologyStatus status;          // RUNNING, STOPPED, etc.
    Long coordinatorAgentId;        // 协调 Agent ID
    String attributes;              // JSON 扩展属性
    Long createdBy;
    Integer version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

// Node 领域模型
public class Node {
    Long id;
    String name;
    String description;
    Long nodeTypeId;                // 节点类型 ID
    NodeStatus status;              // RUNNING, STOPPED, etc.
    Long agentTeamId;               // Agent Team ID
    String attributes;              // JSON 扩展属性
    Long createdBy;
    Integer version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

---

### R5: 审计日志兼容性

**任务**: 研究现有 resource_audit_log 表的迁移方案

**Decision**: 复用现有 `resource_audit_log` 表

**Rationale**:
1. **历史可追溯**：旧的审计日志保留原样，关联关系通过 ID 可推断
2. **简化实现**：避免复杂的历史数据迁移
3. 审计日志主要用于历史追溯，不需要严格的外键约束

---

### R6: 性能优化策略

**任务**: 研究表拆分后的查询性能优化

**Decision**: 独立索引 + 移除类型过滤条件

**Rationale**:
1. **现状**：每次查询都需要 `WHERE resource_type_id = ?` 过滤
2. **优化后**：直接查询 topology 或 node 表，无需类型过滤
3. **索引策略**：
   - `topology`: `idx_name`, `idx_status`, `idx_created_at`
   - `node`: `idx_name`, `idx_type_id`, `idx_status`, `idx_created_at`
   - `topology_2_node`: `idx_topology_id`, `idx_node_id`
   - `node_2_node`: `idx_source`, `idx_target`, `idx_type`

**预期收益**:
- 查询路径缩短，索引更精准
- 单表数据量减少，扫描行数降低

---

## 研究结论

| 研究项 | 决策 | 风险等级 |
|-------|------|---------|
| R1 数据库迁移 | 分步迁移，保留原 ID | 低 |
| R2 关联表设计 | 分表设计：topology_2_node + node_2_node（不支持嵌套） | 低 |
| R3 API 兼容 | @Deprecated + 重定向提示 | 低 |
| R4 领域模型 | 完全分离，不共享基类 | 低 |
| R5 审计日志 | 复用现有 resource_audit_log 表 | 低 |
| R6 性能优化 | 独立索引，移除类型过滤 | 低 |

**总体风险评估**: 低风险，现有数据量小（约 40 条），迁移窗口充足

## 数据库表总览（最终设计）

| 表名 | 用途 | 替代 |
|-----|------|------|
| `topology` | 拓扑图实体 | resource (SUBGRAPH) |
| `node` | 资源节点实体 | resource (非 SUBGRAPH) |
| `node_type` | 节点类型 | resource_type (移除 SUBGRAPH) |
| `topology_2_node` | 拓扑图-节点关联 | subgraph_member |
| `node_2_node` | 节点间依赖关系 | resource_relationship |
