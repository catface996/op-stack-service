# Data Model: Agent Management API

**Feature**: 027-agent-management
**Date**: 2025-12-28

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Agent                                │
│                    (Agent 实体)                              │
├─────────────────────────────────────────────────────────────┤
│ id: Long (PK, auto-increment)                               │
│ name: String (1-100 chars)                                  │
│ role: AgentRole (enum)                                      │
│ specialty: String (optional, max 200 chars)                 │
│ warnings: Integer (default: 0)        ← 全局累计            │
│ critical: Integer (default: 0)        ← 全局累计            │
│ config: JSON (AgentConfig)                                  │
│ created_at: LocalDateTime                                   │
│ updated_at: LocalDateTime                                   │
│ deleted: Integer (0=active, 1=deleted)                      │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ 1:N
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     agent_2_team                             │
│                (Agent-Team 关联表)                           │
├─────────────────────────────────────────────────────────────┤
│ id: Long (PK, auto-increment)                               │
│ agent_id: Long (FK -> agent.id)                             │
│ team_id: Long (FK -> team.id)                               │
│ status: AgentStatus (enum, default: IDLE)  ← 按 Team 区分   │
│ current_task: String (optional, max 500 chars)              │
│ created_at: LocalDateTime                                   │
│ deleted: Integer (0=active, 1=deleted)                      │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ N:1
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                         Team                                 │
│                   (existing entity)                          │
├─────────────────────────────────────────────────────────────┤
│ id: Long (PK)                                               │
│ ...                                                          │
└─────────────────────────────────────────────────────────────┘
```

**关系说明**: Agent 与 Team 是多对多关系，一个 Agent 可以被分配到多个 Team。

## Enumerations

### AgentRole

| Value | Display Name | Description |
|-------|--------------|-------------|
| GLOBAL_SUPERVISOR | 全局监管者 | 系统级协调者（单例） |
| TEAM_SUPERVISOR | 团队监管者 | 团队内 Agent 协调者 |
| WORKER | 工作者 | 执行具体任务 |
| SCOUTER | 侦察者 | 发现类 Agent |

### AgentStatus

| Value | Display Name | Description |
|-------|--------------|-------------|
| IDLE | 空闲 | 可接受新任务 |
| THINKING | 思考中 | 推理/分析中 |
| WORKING | 工作中 | 执行任务中 |
| COMPLETED | 已完成 | 任务执行完成 |
| WAITING | 等待中 | 等待依赖项 |
| ERROR | 错误 | 执行出错 |

## Value Objects

### AgentConfig (JSON)

```json
{
  "model": "gemini-2.0-flash",           // AI 模型标识符
  "temperature": 0.3,                     // 创造性参数 (0.0-1.0)
  "systemInstruction": "You are...",      // 系统指令
  "defaultContext": ""                    // 默认上下文
}
```

| Field | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| model | String | No | "gemini-2.0-flash" | Max 100 chars |
| temperature | Double | No | 0.3 | Range: 0.0-1.0 |
| systemInstruction | String | No | "You are a specialized worker agent." | Max 10000 chars |
| defaultContext | String | No | "" | Max 10000 chars |

### AgentFindings (Embedded)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| warnings | Integer | 0 | 警告数量 |
| critical | Integer | 0 | 严重问题数量 |

## Database Schema

### Table: agent

```sql
CREATE TABLE agent (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'Agent ID',
    name            VARCHAR(100)    NOT NULL COMMENT 'Agent 名称',
    role            VARCHAR(32)     NOT NULL COMMENT 'Agent 角色',
    specialty       VARCHAR(200)    NULL COMMENT '专业领域',
    warnings        INT             NOT NULL DEFAULT 0 COMMENT '警告数量（全局累计）',
    critical        INT             NOT NULL DEFAULT 0 COMMENT '严重问题数量（全局累计）',
    config          JSON            NULL COMMENT 'AI 配置 (JSON)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记',
    PRIMARY KEY (id),
    INDEX idx_agent_role (role),
    INDEX idx_agent_deleted (deleted),
    UNIQUE INDEX uk_agent_name (name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 表';
```

### Table: agent_2_team

```sql
CREATE TABLE agent_2_team (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
    agent_id        BIGINT          NOT NULL COMMENT 'Agent ID',
    team_id         BIGINT          NOT NULL COMMENT 'Team ID',
    status          VARCHAR(32)     NOT NULL DEFAULT 'IDLE' COMMENT 'Agent 在该团队的工作状态',
    current_task    VARCHAR(500)    NULL COMMENT '当前任务描述',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记',
    PRIMARY KEY (id),
    INDEX idx_agent_2_team_agent_id (agent_id),
    INDEX idx_agent_2_team_team_id (team_id),
    INDEX idx_agent_2_team_status (status),
    UNIQUE INDEX uk_agent_team (agent_id, team_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 与 Team 关联表';
```

## Domain Model

### Agent.java

```java
public class Agent {
    private Long id;
    private String name;
    private AgentRole role;
    private String specialty;
    private AgentFindings findings;  // 全局累计的发现统计
    private AgentConfig config;
    private List<Long> teamIds;      // 关联的团队 ID 列表
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method
    public static Agent create(String name, AgentRole role, String specialty,
                               AgentConfig config) {
        Agent agent = new Agent();
        agent.name = name;
        agent.role = role;
        agent.specialty = specialty;
        agent.config = config != null ? config : AgentConfig.defaults();
        agent.findings = AgentFindings.empty();
        agent.teamIds = new ArrayList<>();
        agent.createdAt = LocalDateTime.now();
        agent.updatedAt = LocalDateTime.now();
        return agent;
    }

    // Business methods
    public void update(String name, String specialty) { ... }
    public void updateConfig(AgentConfig config) { ... }
    public void incrementWarnings() { findings.incrementWarnings(); }
    public void incrementCritical() { findings.incrementCritical(); }

    // Validation
    public boolean canBeDeleted() {
        return role != AgentRole.GLOBAL_SUPERVISOR;
    }
}
```

### AgentTeamRelation.java

```java
public class AgentTeamRelation {
    private Long id;
    private Long agentId;
    private Long teamId;
    private AgentStatus status;      // Agent 在该 Team 的工作状态
    private String currentTask;
    private LocalDateTime createdAt;

    public static AgentTeamRelation create(Long agentId, Long teamId) {
        AgentTeamRelation relation = new AgentTeamRelation();
        relation.agentId = agentId;
        relation.teamId = teamId;
        relation.status = AgentStatus.IDLE;
        relation.currentTask = null;
        relation.createdAt = LocalDateTime.now();
        return relation;
    }

    // Status management
    public void startWorking(String task) {
        this.status = AgentStatus.WORKING;
        this.currentTask = task;
    }

    public void startThinking() {
        this.status = AgentStatus.THINKING;
    }

    public void complete() {
        this.status = AgentStatus.COMPLETED;
        this.currentTask = null;
    }

    public void reset() {
        this.status = AgentStatus.IDLE;
        this.currentTask = null;
    }

    public void setError() {
        this.status = AgentStatus.ERROR;
    }

    public void setWaiting() {
        this.status = AgentStatus.WAITING;
    }

    // Validation
    public boolean canBeUpdated() {
        return status != AgentStatus.WORKING && status != AgentStatus.THINKING;
    }

    public boolean isWorking() {
        return status == AgentStatus.WORKING || status == AgentStatus.THINKING;
    }
}
```

### AgentConfig.java

```java
public class AgentConfig {
    private String model;
    private Double temperature;
    private String systemInstruction;
    private String defaultContext;

    public static AgentConfig defaults() {
        return new AgentConfig(
            "gemini-2.0-flash",
            0.3,
            "You are a specialized worker agent.",
            ""
        );
    }
}
```

### AgentFindings.java

```java
public class AgentFindings {
    private Integer warnings;
    private Integer critical;

    public static AgentFindings empty() {
        return new AgentFindings(0, 0);
    }
}
```

## Validation Rules

| Entity | Field | Rule |
|--------|-------|------|
| Agent | name | Required, 1-100 chars, globally unique |
| Agent | role | Required, valid enum value |
| Agent | specialty | Optional, max 200 chars |
| Agent | config.temperature | Range 0.0-1.0 |
| Agent | config.model | Max 100 chars |
| Agent | config.systemInstruction | Max 10000 chars |
| AgentTeamRelation | agent_id + team_id | Unique combination |
| AgentTeamRelation | status | Required, valid enum value, default IDLE |
| AgentTeamRelation | current_task | Optional, max 500 chars |

## Business Constraints

| Constraint | Description | Implementation |
|------------|-------------|----------------|
| GLOBAL_SUPERVISOR Singleton | 系统中仅允许一个 GLOBAL_SUPERVISOR | 创建时检查 |
| Role Immutability | 角色创建后不可更改 | 更新时忽略 role 字段 |
| Working Protection | 某 Team 中 WORKING/THINKING 状态的关联关系不可更新/删除 | 操作前检查 AgentTeamRelation.status |
| Team Supervisor Constraint | TEAM_SUPERVISOR 在某团队有成员时不可从该团队移除 | 取消分配前检查 |
| Name Uniqueness | Agent 名称全局唯一 | 创建/更新时检查 |
| Duplicate Assignment | 同一 Agent 不能重复分配到同一 Team | 唯一索引保证 |

## State Transitions (Per Team)

状态转换是**按 Team 区分**的，同一个 Agent 在不同 Team 中可以有不同的状态。

```
                    ┌──────────────────┐
                    │                  │
                    ▼                  │
┌──────┐  assign  ┌─────────┐  complete  ┌───────────┐
│ IDLE │ ───────► │ WORKING │ ─────────► │ COMPLETED │
└──────┘          └─────────┘            └───────────┘
   │                  │                       │
   │                  │ thinking              │ reset
   │                  ▼                       │
   │              ┌──────────┐                │
   │              │ THINKING │                │
   │              └──────────┘                │
   │                  │                       │
   │                  │ wait                  │
   │                  ▼                       │
   │              ┌─────────┐                 │
   │              │ WAITING │                 │
   │              └─────────┘                 │
   │                  │                       │
   │                  │ error                 │
   │                  ▼                       │
   │              ┌───────┐                   │
   └──────────────│ ERROR │◄──────────────────┘
                  └───────┘
```

**示例**：
- Agent-001 在 Team-A: status=WORKING, current_task="分析日志"
- Agent-001 在 Team-B: status=IDLE, current_task=null

注：状态转换由 Agent 执行引擎控制，本功能仅支持手动设置状态。

## Agent Templates (Static Data)

| ID | Name | Description | Recommended Model | Temperature |
|----|------|-------------|-------------------|-------------|
| 1 | Standard Coordinator | 通用团队协调模板 | gemini-2.0-flash | 0.3 |
| 2 | Strict Security Auditor | 安全审计专用模板 | gemini-2.5-flash | 0.1 |
| 3 | Performance Optimizer | 性能优化专用模板 | gemini-2.0-flash | 0.3 |
| 4 | Root Cause Analyst | 根因分析专用模板 | gemini-2.5-flash-thinking | 0.2 |
| 5 | Concise Reporter | 简洁报告专用模板 | gemini-2.0-flash | 0.1 |
