# Data Model: 诊断任务持久化

**Feature**: 044-diagnosis-task
**Date**: 2026-01-05

## Entity Relationship Diagram

```
┌─────────────────────┐         ┌─────────────────────────┐
│   DiagnosisTask     │ 1     n │  AgentDiagnosisProcess  │
├─────────────────────┤─────────├─────────────────────────┤
│ id                  │         │ id                      │
│ topology_id (FK)    │         │ task_id (FK)            │
│ user_question       │         │ agent_bound_id (FK)     │
│ status              │         │ agent_name              │
│ error_message       │         │ content                 │
│ run_id              │         │ started_at              │
│ created_at          │         │ ended_at                │
│ completed_at        │         │ created_at              │
│ deleted             │         │ deleted                 │
└─────────────────────┘         └─────────────────────────┘
         │                                  │
         │ n                                │ n
         ▼                                  ▼
┌─────────────────────┐         ┌─────────────────────────┐
│     Topology        │         │      AgentBound         │
│     (existing)      │         │      (existing)         │
└─────────────────────┘         └─────────────────────────┘
```

## Entities

### DiagnosisTask（诊断任务）

诊断任务是用户触发的一次诊断执行记录，关联到一个拓扑图。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| topology_id | BIGINT | NOT NULL, FK | 关联拓扑图ID |
| user_question | TEXT | NOT NULL | 用户输入的诊断问题 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'RUNNING' | 任务状态 |
| error_message | VARCHAR(500) | NULL | 错误信息（失败/超时时填写） |
| run_id | VARCHAR(100) | NULL | executor分配的运行ID |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 创建时间 |
| completed_at | DATETIME | NULL | 完成时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_at | DATETIME | NOT NULL, AUTO_UPDATE | 更新时间 |
| version | INT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除标记 |

**状态枚举 (DiagnosisTaskStatus)**:
- `RUNNING`: 运行中
- `COMPLETED`: 已完成
- `FAILED`: 失败
- `TIMEOUT`: 超时

**业务规则**:
1. 创建时状态为 `RUNNING`
2. 正常完成时状态变为 `COMPLETED`，设置 `completed_at`
3. executor错误时状态变为 `FAILED`，记录 `error_message`
4. 超过10分钟未完成时状态变为 `TIMEOUT`

### AgentDiagnosisProcess（Agent诊断过程）

记录单个Agent在一次诊断任务中的诊断过程，内容为整合后的完整文本。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| task_id | BIGINT | NOT NULL, FK | 关联诊断任务ID |
| agent_bound_id | BIGINT | NOT NULL, FK | 关联AgentBound ID |
| agent_name | VARCHAR(100) | NOT NULL | Agent名称（冗余存储） |
| content | LONGTEXT | NULL | 诊断内容（整合后的完整文本） |
| started_at | DATETIME | NULL | Agent开始诊断时间 |
| ended_at | DATETIME | NULL | Agent结束诊断时间 |
| created_at | DATETIME | NOT NULL, DEFAULT NOW() | 记录创建时间 |
| updated_at | DATETIME | NOT NULL, AUTO_UPDATE | 更新时间 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除标记 |

**业务规则**:
1. 一个诊断任务可关联多个Agent诊断过程
2. 每个Agent诊断过程通过 `agent_bound_id` 关联到具体的Agent绑定
3. `content` 为从Redis整合后的完整文本
4. `agent_name` 冗余存储，避免JOIN查询
5. 即使Agent无输出，也创建记录，`content` 标记为"无输出"

## Database Schema (Flyway Migration)

### V40__create_diagnosis_task_tables.sql

```sql
-- =====================================================
-- V40: 创建诊断任务相关表
-- Feature: 044-diagnosis-task
-- Date: 2026-01-05
-- Description: 创建诊断任务表和Agent诊断过程表
-- =====================================================

-- 诊断任务表
CREATE TABLE diagnosis_task (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务核心字段
    topology_id     BIGINT          NOT NULL COMMENT '关联拓扑图ID',
    user_question   TEXT            NOT NULL COMMENT '用户诊断问题',
    status          VARCHAR(20)     NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING, COMPLETED, FAILED, TIMEOUT',
    error_message   VARCHAR(500)    DEFAULT NULL COMMENT '错误信息',
    run_id          VARCHAR(100)    DEFAULT NULL COMMENT 'executor运行ID',
    completed_at    DATETIME        DEFAULT NULL COMMENT '完成时间',

    -- 审计字段
    created_by      BIGINT          COMMENT '创建人ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          COMMENT '修改人ID',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 版本控制
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_topology_id (topology_id, deleted),
    INDEX idx_status (status, deleted),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断任务表';

-- Agent诊断过程表
CREATE TABLE agent_diagnosis_process (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务核心字段
    task_id         BIGINT          NOT NULL COMMENT '关联诊断任务ID',
    agent_bound_id  BIGINT          NOT NULL COMMENT '关联AgentBound ID',
    agent_name      VARCHAR(100)    NOT NULL COMMENT 'Agent名称（冗余存储）',
    content         LONGTEXT        DEFAULT NULL COMMENT '诊断内容（整合后的完整文本）',
    started_at      DATETIME        DEFAULT NULL COMMENT 'Agent开始诊断时间',
    ended_at        DATETIME        DEFAULT NULL COMMENT 'Agent结束诊断时间',

    -- 审计字段
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 软删除
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    INDEX idx_task_id (task_id, deleted),
    INDEX idx_agent_bound_id (agent_bound_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent诊断过程表';
```

## State Transitions

### DiagnosisTask State Machine

```
     ┌─────────────────────────────────────────────┐
     │                                             │
     ▼                                             │
┌─────────┐    executor完成    ┌───────────┐       │
│ RUNNING │───────────────────▶│ COMPLETED │       │
└─────────┘                    └───────────┘       │
     │                                             │
     │ executor错误                                │
     ├─────────────────────────┐                   │
     │                         ▼                   │
     │                    ┌─────────┐              │
     │                    │ FAILED  │              │
     │                    └─────────┘              │
     │                                             │
     │ 超时(10分钟)                                │
     └─────────────────────────┐                   │
                               ▼                   │
                          ┌─────────┐              │
                          │ TIMEOUT │              │
                          └─────────┘              │
```

## Validation Rules

### DiagnosisTask

| Field | Validation |
|-------|------------|
| topology_id | 必填，必须存在对应的拓扑图 |
| user_question | 必填，最大长度65535字符 |
| status | 枚举值校验 |
| error_message | 最大长度500字符 |
| run_id | 最大长度100字符 |

### AgentDiagnosisProcess

| Field | Validation |
|-------|------------|
| task_id | 必填，必须存在对应的诊断任务 |
| agent_bound_id | 必填，必须存在对应的AgentBound |
| agent_name | 必填，最大长度100字符 |
| content | 建议单个Agent内容不超过100KB |

## Redis Data Model

### Key Pattern

```
diagnosis:task:{taskId}:agent:{agentBoundId}
```

### Data Structure

使用 Redis List 存储流式事件：

```
Key: diagnosis:task:123:agent:456
Type: List
TTL: 24 hours

Value (each element):
{
  "timestamp": "2026-01-05T10:00:00Z",
  "sequence": 1,
  "eventType": "llm.stream",
  "content": "正在分析性能瓶颈..."
}
```

### Operations

| Operation | Redis Command | Description |
|-----------|---------------|-------------|
| 追加事件 | RPUSH | 按时间顺序追加到列表末尾 |
| 读取所有 | LRANGE 0 -1 | 读取全部事件用于整合 |
| 设置过期 | EXPIRE | 设置24小时TTL |
| 删除 | DEL | 持久化完成后删除 |

### Key Naming Examples

```
# 任务123中Agent绑定456的诊断流
diagnosis:task:123:agent:456

# 任务123中Agent绑定789的诊断流
diagnosis:task:123:agent:789

# 通配符查询任务123的所有Agent诊断流
diagnosis:task:123:*
```
