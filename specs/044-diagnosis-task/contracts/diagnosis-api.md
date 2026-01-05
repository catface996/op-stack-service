# API Contract: 诊断任务管理

**Feature**: 044-diagnosis-task
**Base Path**: `/api/service/v1/diagnosis-tasks`
**Date**: 2026-01-05

## Overview

诊断任务管理 API，支持创建诊断任务、查询历史、查看详情等操作。所有接口使用 POST 方法。

## Endpoints

### 1. 创建诊断任务

**POST** `/api/service/v1/diagnosis-tasks/create`

创建新的诊断任务并开始执行诊断流程。

#### Request

```json
{
  "topologyId": 43,
  "userQuestion": "分析性能瓶颈"
}
```

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| topologyId | Long | Yes | > 0 | 拓扑图ID |
| userQuestion | String | Yes | 1-65535 chars | 诊断问题 |

#### Response (Success)

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "id": 1,
    "topologyId": 43,
    "userQuestion": "分析性能瓶颈",
    "status": "RUNNING",
    "runId": "run-abc-123",
    "createdAt": "2026-01-05T10:00:00"
  }
}
```

#### Response (Error - Empty Question)

```json
{
  "code": 400001,
  "message": "请输入诊断问题",
  "success": false,
  "data": null
}
```

#### Response (Error - Topology Not Found)

```json
{
  "code": 404001,
  "message": "拓扑图不存在: 999",
  "success": false,
  "data": null
}
```

---

### 2. 查询诊断任务详情

**POST** `/api/service/v1/diagnosis-tasks/query-by-id`

查询单个诊断任务的详细信息，包括所有Agent诊断过程。

#### Request

```json
{
  "id": 1
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Yes | 诊断任务ID |

#### Response

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "id": 1,
    "topologyId": 43,
    "topologyName": "生产环境拓扑",
    "userQuestion": "分析性能瓶颈",
    "status": "COMPLETED",
    "errorMessage": null,
    "runId": "run-abc-123",
    "createdAt": "2026-01-05T10:00:00",
    "completedAt": "2026-01-05T10:05:30",
    "agentProcesses": [
      {
        "id": 1,
        "agentBoundId": 100,
        "agentName": "全局监督Agent",
        "content": "开始分析系统性能...\n检测到CPU使用率异常...",
        "startedAt": "2026-01-05T10:00:05",
        "endedAt": "2026-01-05T10:02:30"
      },
      {
        "id": 2,
        "agentBoundId": 101,
        "agentName": "数据库诊断Agent",
        "content": "分析数据库连接池...\n发现慢查询...",
        "startedAt": "2026-01-05T10:02:35",
        "endedAt": "2026-01-05T10:05:00"
      }
    ]
  }
}
```

---

### 3. 查询拓扑图诊断历史

**POST** `/api/service/v1/diagnosis-tasks/query-by-topology`

查询指定拓扑图的诊断任务历史列表，支持分页。

#### Request

```json
{
  "topologyId": 43,
  "page": 1,
  "size": 20
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| topologyId | Long | Yes | - | 拓扑图ID |
| page | Integer | No | 1 | 页码（从1开始） |
| size | Integer | No | 20 | 每页大小（1-100） |

#### Response

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "content": [
      {
        "id": 3,
        "topologyId": 43,
        "userQuestion": "检查内存泄漏",
        "status": "COMPLETED",
        "createdAt": "2026-01-05T14:00:00",
        "completedAt": "2026-01-05T14:08:20",
        "agentCount": 3
      },
      {
        "id": 2,
        "topologyId": 43,
        "userQuestion": "网络延迟分析",
        "status": "FAILED",
        "errorMessage": "executor服务连接失败",
        "createdAt": "2026-01-05T12:00:00",
        "completedAt": null,
        "agentCount": 1
      },
      {
        "id": 1,
        "topologyId": 43,
        "userQuestion": "分析性能瓶颈",
        "status": "COMPLETED",
        "createdAt": "2026-01-05T10:00:00",
        "completedAt": "2026-01-05T10:05:30",
        "agentCount": 5
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 4. 查询运行中的诊断任务

**POST** `/api/service/v1/diagnosis-tasks/query-running`

查询当前运行中的诊断任务列表。

#### Request

```json
{
  "topologyId": 43
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| topologyId | Long | No | 拓扑图ID（可选，不传则查询所有） |

#### Response

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": [
    {
      "id": 5,
      "topologyId": 43,
      "topologyName": "生产环境拓扑",
      "userQuestion": "实时监控分析",
      "status": "RUNNING",
      "runId": "run-xyz-456",
      "createdAt": "2026-01-05T15:30:00",
      "runningDuration": 120
    }
  ]
}
```

---

## Data Types

### DiagnosisTaskDTO

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 任务ID |
| topologyId | Long | 拓扑图ID |
| topologyName | String | 拓扑图名称 |
| userQuestion | String | 诊断问题 |
| status | String | 状态: RUNNING, COMPLETED, FAILED, TIMEOUT |
| errorMessage | String | 错误信息（失败时） |
| runId | String | executor运行ID |
| createdAt | LocalDateTime | 创建时间 |
| completedAt | LocalDateTime | 完成时间 |
| agentCount | Integer | 参与Agent数量（列表查询时） |
| agentProcesses | List | Agent诊断过程列表（详情查询时） |
| runningDuration | Long | 运行时长（秒，运行中任务） |

### AgentDiagnosisProcessDTO

| Field | Type | Description |
|-------|------|-------------|
| id | Long | 过程记录ID |
| agentBoundId | Long | AgentBound ID |
| agentName | String | Agent名称 |
| content | String | 诊断内容 |
| startedAt | LocalDateTime | 开始时间 |
| endedAt | LocalDateTime | 结束时间 |

### DiagnosisTaskStatus (Enum)

| Value | Description |
|-------|-------------|
| RUNNING | 运行中 |
| COMPLETED | 已完成 |
| FAILED | 失败 |
| TIMEOUT | 超时 |

---

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400001 | 请输入诊断问题 | userQuestion为空 |
| 400002 | 拓扑图ID不能为空 | topologyId为空 |
| 404001 | 拓扑图不存在: {id} | 指定的拓扑图不存在 |
| 404002 | 诊断任务不存在: {id} | 指定的诊断任务不存在 |
| 500001 | 诊断服务暂不可用 | executor服务不可用 |

---

## Notes

1. 所有接口使用 POST 方法，遵循 POST-Only API 规范
2. 响应格式统一为 `Result<T>` 结构
3. 分页接口使用 `PageResult<T>` 结构
4. 时间字段使用 ISO 8601 格式
5. 诊断任务创建后立即开始执行，通过SSE接收流式响应
