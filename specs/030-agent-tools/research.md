# Research: Agent Tools 绑定

**Feature**: 030-agent-tools | **Date**: 2025-12-28

## 研究目标

分析现有代码库中 Agent 和关联关系的实现模式，确定 Agent-Tool 绑定的最佳实现方案。

## 现有架构分析

### 1. Agent 领域模型

**文件**: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/Agent.java`

当前 Agent 实体已包含：
- 基本属性：id, name, role, specialty
- LLM 配置：promptTemplateId, model, temperature, topP, maxTokens, maxRuntime
- 统计信息：warnings, critical
- 关联信息：`List<Long> teamIds`（通过关联表查询填充）
- 审计字段：createdAt, updatedAt, deleted

**设计决策**：`teamIds` 是通过 `AgentTeamRelation` 关联表查询后填充到领域模型的，而非直接存储在 `agent` 表中。`toolIds` 应采用相同模式。

### 2. Agent-Team 关联模式

**数据库表**: `agent_2_team`

```sql
CREATE TABLE agent_2_team (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    agent_id        BIGINT NOT NULL,
    team_id         BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    current_task    VARCHAR(500) NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_agent_team (agent_id, team_id, deleted)
) ENGINE=InnoDB;
```

**关键设计**：
- 软删除支持（deleted 字段）
- 唯一约束包含 deleted（允许重新分配）
- 额外状态字段（status, current_task）

**领域模型**: `AgentTeamRelation.java`
- 工厂方法 `create(agentId, teamId)`
- 状态管理方法（startWorking, complete, reset 等）
- 软删除方法 `markDeleted()`

**仓储接口**: `AgentTeamRelationRepository.java`
- `save(relation)` - 保存关联
- `deleteByAgentId(agentId)` - 按 Agent 删除所有关联
- `deleteByAgentIdAndTeamId(agentId, teamId)` - 删除特定关联
- `findTeamIdsByAgentId(agentId)` - 查询 Agent 的所有 Team ID
- `existsByAgentIdAndTeamId(agentId, teamId)` - 检查关联是否存在

### 3. Tool 实体现状

**发现**：当前代码库中 **尚未实现 Tool 实体**。需要假设 Tool 实体已存在或将来会实现。

**假设**：
- Tool 表存在，结构为：`id, name, description, type, ...`
- 本特性仅实现 Agent-Tool 绑定关系，不涉及 Tool 实体本身

## 技术决策

### D-001: 关联表设计

**问题**：Agent-Tool 关联表需要哪些字段？

**决策**：采用简化设计，仅保留核心字段

**理由**：
- Agent-Team 关联有状态（WORKING/THINKING），因为 Agent 在 Team 中执行任务
- Agent-Tool 关联是静态配置，无需运行时状态
- 简化设计降低复杂度

**结构**：
```sql
CREATE TABLE agent_2_tool (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    agent_id    BIGINT NOT NULL,
    tool_id     BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_agent_tool (agent_id, tool_id, deleted)
);
```

### D-002: 全量替换策略

**问题**：如何实现全量替换 toolIds？

**决策**：删除后批量插入

**算法**：
1. 软删除该 Agent 的所有现有 Tool 关联
2. 去重并过滤无效 Tool ID
3. 批量插入新的关联记录

**理由**：
- 简单直接，避免复杂的差异计算
- 事务保证原子性
- 性能可接受（单个 Agent 绑定的 Tool 数量有限）

### D-003: 无效 Tool ID 处理

**问题**：传入不存在的 Tool ID 时如何处理？

**决策**：静默过滤，仅绑定存在的 Tool

**实现**：
1. 查询 Tool 表验证哪些 ID 存在
2. 仅使用存在的 ID 创建关联
3. 不抛出错误

**理由**：
- 符合用户预期（spec.md 假设）
- 提高接口容错性
- 可在日志中记录过滤信息

### D-004: 查询优化

**问题**：如何高效查询 Agent 绑定的 toolIds？

**决策**：在 AgentRepositoryImpl 中批量查询

**实现**：
- 单个 Agent 查询：`SELECT tool_id FROM agent_2_tool WHERE agent_id = ? AND deleted = 0`
- 列表查询：批量查询后按 agentId 分组填充

**理由**：
- 避免 N+1 查询问题
- 与现有 teamIds 查询模式一致

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Tool 表不存在 | 高 | 关联表设计不依赖外键，延迟验证 |
| 并发更新冲突 | 低 | 事务 + 乐观锁（如需要） |
| 大量 Tool 绑定 | 低 | 批量操作 + 索引优化 |

## 参考实现

### 现有代码参考

- `AgentTeamRelation.java` - 领域模型设计模式
- `AgentTeamRelationRepository.java` - 仓储接口设计
- `AgentTeamRelationRepositoryImpl.java` - MyBatis-Plus 实现
- `AgentApplicationServiceImpl.java` - 应用服务协调逻辑

## 结论

Agent Tools 绑定功能应：
1. 新建 `agent_2_tool` 关联表（参考 `agent_2_team`）
2. 新建 `AgentToolRelation` 领域模型（简化版，无状态）
3. 新建 `AgentToolRelationRepository` 仓储接口和实现
4. 修改 `Agent` 领域模型添加 `toolIds` 属性
5. 修改 `CreateAgentRequest`、`UpdateAgentRequest`、`AgentDTO` 添加 `toolIds` 字段
6. 修改 `AgentApplicationServiceImpl` 处理 Tools 绑定逻辑
