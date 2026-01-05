# Quickstart: 诊断任务持久化

**Feature**: 044-diagnosis-task
**Date**: 2026-01-05

## Overview

本功能实现诊断任务的创建、流式数据收集、以及按Agent维度持久化诊断过程。

## Quick Verification

### 1. Database Migration Verification

```sql
-- 验证表已创建
DESCRIBE diagnosis_task;
DESCRIBE agent_diagnosis_process;

-- 应该看到:
-- diagnosis_task: id, topology_id, user_question, status, error_message, run_id, ...
-- agent_diagnosis_process: id, task_id, agent_bound_id, agent_name, content, ...
```

### 2. Create Diagnosis Task

```bash
# 创建诊断任务
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/create' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 4,
    "userQuestion": "分析系统性能瓶颈"
  }'

# 预期响应:
# {
#   "code": 0,
#   "message": "success",
#   "success": true,
#   "data": {
#     "id": 1,
#     "topologyId": 4,
#     "userQuestion": "分析系统性能瓶颈",
#     "status": "RUNNING",
#     "runId": "run-xxx-xxx",
#     "createdAt": "2026-01-05T10:00:00"
#   }
# }
```

### 3. Query Task Details

```bash
# 查询诊断任务详情（包含所有Agent诊断过程）
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/query-by-id' \
  -H 'Content-Type: application/json' \
  -d '{"id": 1}'

# 预期响应包含:
# - 任务基本信息
# - status: "COMPLETED" 或 "RUNNING"
# - agentProcesses: Agent诊断过程数组
```

### 4. Query Topology History

```bash
# 查询拓扑图的诊断历史
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/query-by-topology' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 4,
    "page": 1,
    "size": 10
  }'

# 预期响应:
# - 分页结果
# - 按创建时间倒序排列
# - 每条记录包含 agentCount（参与Agent数量）
```

### 5. Query Running Tasks

```bash
# 查询运行中的诊断任务
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/query-running' \
  -H 'Content-Type: application/json' \
  -d '{"topologyId": 4}'

# 预期响应:
# - 运行中的任务列表
# - 包含 runningDuration（运行时长秒数）
```

### 6. Redis Data Verification

```bash
# 连接 Redis 查看暂存数据（诊断进行中时）
redis-cli

# 查看某任务的所有Agent诊断流
KEYS diagnosis:task:1:*

# 查看某Agent的诊断事件
LRANGE diagnosis:task:1:agent:100 0 -1

# 查看Key的TTL
TTL diagnosis:task:1:agent:100
# 应该显示剩余秒数（最大86400秒=24小时）
```

## Usage Examples

### Example 1: Complete Diagnosis Flow

```bash
# Step 1: 创建诊断任务
TASK_ID=$(curl -s -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/create' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 4,
    "userQuestion": "为什么API响应时间变慢？"
  }' | jq -r '.data.id')

echo "Created task: $TASK_ID"

# Step 2: 等待诊断完成（轮询状态）
while true; do
  STATUS=$(curl -s -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/query-by-id' \
    -H 'Content-Type: application/json' \
    -d "{\"id\": $TASK_ID}" | jq -r '.data.status')

  echo "Task status: $STATUS"

  if [ "$STATUS" != "RUNNING" ]; then
    break
  fi

  sleep 5
done

# Step 3: 查看诊断结果
curl -s -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/query-by-id' \
  -H 'Content-Type: application/json' \
  -d "{\"id\": $TASK_ID}" | jq '.data.agentProcesses[] | {agent: .agentName, content: .content[:100]}'
```

### Example 2: Error Handling

```bash
# 空问题 - 应返回错误
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/create' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 4,
    "userQuestion": ""
  }'
# 预期: {"code": 400001, "message": "请输入诊断问题", ...}

# 不存在的拓扑图 - 应返回错误
curl -X POST 'http://localhost:8081/api/service/v1/diagnosis-tasks/create' \
  -H 'Content-Type: application/json' \
  -d '{
    "topologyId": 99999,
    "userQuestion": "测试问题"
  }'
# 预期: {"code": 404001, "message": "拓扑图不存在: 99999", ...}
```

## Data Flow

```
1. 用户触发诊断
   └── POST /diagnosis-tasks/create
       └── 创建 DiagnosisTask (status=RUNNING)
       └── 调用 executor 创建层级团队

2. 流式数据收集
   └── SSE 接收 ExecutorEvent
       └── 按 source.agent_id 分类
       └── RPUSH diagnosis:task:{id}:agent:{agentBoundId}
       └── 设置 24小时 TTL

3. 诊断完成
   └── 收到 lifecycle.completed 事件
       └── LRANGE 读取所有事件
       └── 按 Agent 整合内容
       └── 创建 AgentDiagnosisProcess 记录
       └── 更新 DiagnosisTask (status=COMPLETED)
       └── DEL Redis keys

4. 超时处理
   └── 定时检查 RUNNING 状态任务
       └── 超过10分钟未完成
       └── 更新 DiagnosisTask (status=TIMEOUT)
       └── 持久化已收集的数据
```

## Status Reference

| Status | Description | Next Actions |
|--------|-------------|--------------|
| RUNNING | 诊断进行中 | 等待完成或超时 |
| COMPLETED | 诊断完成 | 可查看详情 |
| FAILED | 诊断失败 | 查看errorMessage |
| TIMEOUT | 诊断超时 | 可查看已收集的部分数据 |

## Default Configuration

| Config | Default | Description |
|--------|---------|-------------|
| 诊断超时时间 | 10分钟 | 超过此时间标记为TIMEOUT |
| Redis TTL | 24小时 | 流式数据过期时间 |
| 历史保留时间 | 30天 | 数据库记录保留时间 |
| 单Agent内容上限 | 100KB | 建议的内容大小限制 |

## Troubleshooting

### 诊断任务一直处于 RUNNING 状态

1. 检查 executor 服务是否可用
2. 查看 Redis 中是否有数据累积
3. 检查应用日志中的错误信息

### Agent诊断内容为空

1. 确认 executor 返回了该 Agent 的事件
2. 检查 Redis 中对应 Key 是否有数据
3. 验证 agent_bound_id 是否正确

### Redis 数据未被清理

1. 确认任务状态已变为非 RUNNING
2. 检查持久化流程是否正常完成
3. 手动清理: `redis-cli DEL diagnosis:task:{id}:*`
