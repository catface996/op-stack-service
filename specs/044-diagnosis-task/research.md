# Research: 诊断任务持久化

**Feature**: 044-diagnosis-task
**Date**: 2026-01-05

## Research Summary

本功能涉及诊断任务的创建、流式数据暂存、以及按Agent维度持久化诊断过程。以下是技术研究成果。

## 1. 现有架构分析

### 1.1 ExecutorEvent 数据结构

已有 `ExecutorEvent` 类定义了executor流式响应格式：

```java
// application/application-impl/.../client/dto/ExecutorEvent.java
{
  "run_id": "abc-123",
  "timestamp": "2025-12-31T10:00:00Z",
  "sequence": 1,
  "source": {
    "agent_id": "100",        // 对应 AgentBound.id
    "agent_type": "worker",
    "agent_name": "Agent A",
    "team_name": "Team 1"
  },
  "event": {"category": "llm", "action": "stream"},
  "data": {"content": "Hello..."}
}
```

**Decision**: 复用现有 `ExecutorEvent` 结构，`source.agent_id` 对应 `AgentBound.id`
**Rationale**: 已有完整的事件解析支持，无需重新定义

### 1.2 AgentBound 绑定关系

`agent_bound` 表结构：
- `id`: 绑定记录ID（executor事件中的 agent_id 对应此值）
- `agent_id`: 关联的Agent ID
- `hierarchy_level`: 层级（GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER）
- `entity_id`: 绑定实体ID（Topology或Node）
- `entity_type`: 实体类型

**Decision**: 使用 `agent_bound.id` 作为诊断过程记录的关联键
**Rationale**: 与executor事件的 `source.agent_id` 一致，便于追溯

### 1.3 Redis 配置

项目已配置 Redis，位于：
- `infrastructure/cache/redis-impl/src/main/java/.../config/RedisConfig.java`
- 使用 Jackson JSON 序列化
- Key 命名规范：`aiops:{domain}:{id}`

**Decision**: 复用现有 Redis 配置，新增诊断流式数据的 Key 命名空间
**Rationale**: 保持一致性，无需额外配置

## 2. 技术方案决策

### 2.1 Redis 数据结构选择

| 选项 | 数据结构 | 优势 | 劣势 |
|------|----------|------|------|
| A | Redis List (LPUSH/LRANGE) | 简单，顺序写入 | 无法按时间范围查询 |
| B | Redis Stream (XADD/XREAD) | 天然支持流式数据，有序 | 稍复杂 |
| C | Redis Hash + List | 灵活查询 | 管理复杂 |

**Decision**: 使用 Redis List
**Rationale**:
1. 诊断过程按Agent分组，每个Agent一个List
2. 事件按时间顺序追加，最后批量读取整合
3. 实现简单，TTL设置直接
**Alternatives Rejected**:
- Redis Stream 功能更强大但本场景不需要消费组特性
- Hash+List 增加不必要的复杂度

### 2.2 Redis Key 命名规范

```
diagnosis:task:{taskId}:agent:{agentBoundId}
```

示例：
- `diagnosis:task:123:agent:456` - 任务123中Agent绑定456的诊断流

**Decision**: 采用层级命名，便于按任务批量清理
**Rationale**:
1. 支持通配符查询 `diagnosis:task:123:*`
2. TTL统一设置为24小时
3. 持久化完成后主动删除

### 2.3 诊断任务状态机

```
RUNNING -> COMPLETED (正常完成)
RUNNING -> FAILED (executor错误)
RUNNING -> TIMEOUT (超时)
```

**Decision**: 使用枚举状态，不使用状态机库
**Rationale**: 状态转换简单，无需复杂状态机

### 2.4 持久化触发机制

| 选项 | 触发方式 | 优势 | 劣势 |
|------|----------|------|------|
| A | SSE结束事件触发 | 实时性高 | 需处理异常情况 |
| B | 定时任务轮询 | 可靠 | 延迟高 |
| C | 两者结合 | 可靠+实时 | 复杂度增加 |

**Decision**: SSE结束事件触发 + 超时兜底
**Rationale**:
1. 正常情况由executor的lifecycle.completed事件触发持久化
2. 超时机制作为兜底，防止任务永久停滞
3. 持久化过程异步执行，不阻塞主流程

## 3. 数据库设计决策

### 3.1 表结构设计

**诊断任务表 (diagnosis_task)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| topology_id | BIGINT | 关联拓扑图 |
| user_question | TEXT | 用户诊断问题 |
| status | VARCHAR(20) | 状态: RUNNING, COMPLETED, FAILED, TIMEOUT |
| error_message | VARCHAR(500) | 错误信息 |
| run_id | VARCHAR(100) | executor运行ID |
| created_at | DATETIME | 创建时间 |
| completed_at | DATETIME | 完成时间 |

**Agent诊断过程表 (agent_diagnosis_process)**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联诊断任务 |
| agent_bound_id | BIGINT | 关联AgentBound |
| agent_name | VARCHAR(100) | Agent名称（冗余） |
| content | LONGTEXT | 诊断内容（整合后） |
| started_at | DATETIME | 开始时间 |
| ended_at | DATETIME | 结束时间 |

**Decision**: 分两张表存储，任务表与过程表一对多关系
**Rationale**:
1. 任务级别信息与Agent级别信息分离
2. 支持按任务查询和按Agent查询
3. content使用LONGTEXT支持长文本

### 3.2 索引设计

```sql
-- diagnosis_task
INDEX idx_topology_id (topology_id, deleted)
INDEX idx_status (status, deleted)
INDEX idx_created_at (created_at DESC)

-- agent_diagnosis_process
INDEX idx_task_id (task_id, deleted)
INDEX idx_agent_bound_id (agent_bound_id, deleted)
```

## 4. DDD层级职责

| 层级 | 职责 |
|------|------|
| interface | DiagnosisController - 触发诊断、查询历史 |
| application | DiagnosisApplicationService - 协调流程 |
| domain | DiagnosisTask, AgentDiagnosisProcess - 领域模型 |
| infrastructure | DiagnosisTaskRepository, Redis缓存服务 |

**Decision**: 遵循现有DDD架构分层
**Rationale**: 与项目现有结构保持一致

## 5. 风险评估

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| Redis数据丢失 | Low | High | 24小时TTL + 持久化后删除 |
| 超大诊断内容 | Low | Medium | 单Agent内容100KB限制 |
| 并发诊断任务 | Medium | Low | 每个任务独立Redis Key |
| Executor服务不可用 | Medium | High | 超时机制 + 状态更新 |

## 6. Decisions Log

| Decision | Choice | Alternatives | Rationale |
|----------|--------|--------------|-----------|
| 流式数据存储 | Redis List | Redis Stream, Hash | 简单够用，TTL支持好 |
| Agent标识 | agent_bound_id | agent_id, agent_name | 与executor事件一致 |
| 持久化触发 | SSE结束事件+超时兜底 | 定时轮询 | 实时性好，可靠性有保障 |
| 内容存储格式 | LONGTEXT纯文本 | JSON结构化 | 诊断过程是连续文本，无需结构化 |
| TTL策略 | 24小时 | 1小时, 7天 | 平衡内存占用和排查时间 |
