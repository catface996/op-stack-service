# 前端集成指南：多智能体执行触发

## 概述

本文档指导前端集成多智能体执行功能。用户通过指定拓扑图 ID 和任务消息，触发多智能体协作执行，并通过 Server-Sent Events (SSE) 实时接收执行事件流。

## API 端点概览

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/service/v1/executions/trigger` | POST | 触发执行，返回 SSE 流 |
| `/api/service/v1/executions/cancel` | POST | 取消正在执行的运行 |

---

## 1. 触发执行

```
POST /api/service/v1/executions/trigger
Content-Type: application/json
Accept: text/event-stream
```

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `topologyId` | Long | 是 | 拓扑图 ID |
| `userMessage` | String | 是 | 用户任务消息 |

**请求示例：**
```json
{
  "topologyId": 4,
  "userMessage": "分析系统性能并给出优化建议"
}
```

### 响应格式

响应为 SSE (Server-Sent Events) 流，Content-Type 为 `text/event-stream`。

**事件格式：**
```
event:message
data:{"type":"...","agentName":"...","agentRole":"...","content":"...","timestamp":"...","metadata":null}
```

### 事件字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | String | 事件类型 |
| `runId` | String | 运行 ID（仅在 `started` 事件中返回，用于取消操作） |
| `agentName` | String | Agent 名称（可能为 null） |
| `agentRole` | String | Agent 角色（可能为 null） |
| `content` | String | 事件内容/消息文本 |
| `timestamp` | String | 时间戳 (ISO 8601 格式) |
| `metadata` | Object | 额外元数据（可能为 null） |

### 事件类型说明

| 类型 | 说明 |
|------|------|
| `started` | 执行已启动，**包含 runId**，前端应保存此 ID 用于取消操作 |
| `thinking` | Agent 思考中 |
| `message` | Agent 消息 |
| `tool_call` | Agent 调用工具 |
| `tool_result` | 工具执行结果 |
| `error` | 错误事件 |
| `complete` | 执行完成 |
| `cancelled` | 执行已取消 |

### 事件内容示例

```
// ⭐ 第一个事件：started（包含 runId，前端必须保存！）
{"type":"started","runId":"a1567309-4c03-43f8-bbae-9a2d75fd6d80","content":"Execution started","timestamp":"2025-12-29T13:34:08.000000"}

// 开始分析
{"content":"[Global Supervisor] 🎯 开始分析任务","timestamp":"2025-12-29T13:34:08.781159"}

// 思考过程
{"content":"[Global Supervisor] 💭 思考中...","timestamp":"2025-12-29T13:34:08.784707"}

// 选择团队
{"content":"[Global Supervisor] SELECT: Firestone\n子任务：分析应用层系统性能","timestamp":"2025-12-29T13:34:11.906500"}

// 团队协调
{"content":"[Team: Firestone | Supervisor] 👔 开始协调","timestamp":"2025-12-29T13:34:13.605675"}

// Worker 工作
{"content":"[Team: Firestone | Worker: Updated Agent Name] 🔬 开始工作","timestamp":"2025-12-29T13:34:18.943191"}

// 实际输出内容
{"content":"# 应用层系统性能分析完整指南\n\n## 1. 关键性能指标详解...","timestamp":"2025-12-29T13:34:21.152988"}

// 错误事件
{"type":"error","content":"Executor service error: 404 Not Found","timestamp":"2025-12-29T13:30:51.395213"}
```

---

## 2. 取消执行

### 端点

```
POST /api/service/v1/executions/cancel
Content-Type: application/json
```

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `runId` | String | 是 | 运行 ID（来自 started 事件） |

**请求示例：**
```json
{
  "runId": "a1567309-4c03-43f8-bbae-9a2d75fd6d80"
}
```

### 响应格式

**成功响应：**
```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "type": "cancelled",
    "runId": "a1567309-4c03-43f8-bbae-9a2d75fd6d80",
    "content": "Execution cancelled",
    "timestamp": "2025-12-29T13:35:00.000000"
  }
}
```

**失败响应：**
```json
{
  "code": "CANCEL_FAILED",
  "message": "Failed to cancel execution: a1567309-4c03-43f8-bbae-9a2d75fd6d80",
  "data": null
}
```

### 使用 curl 测试

```bash
curl -X POST "http://localhost:8081/api/service/v1/executions/cancel" \
  -H "Content-Type: application/json" \
  -d '{"runId": "a1567309-4c03-43f8-bbae-9a2d75fd6d80"}'
```

---

## 3. 错误处理

### HTTP 错误码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功，返回 SSE 流 |
| 400 | 请求参数无效（缺少必填字段） |
| 404 | 拓扑图不存在 |
| 500 | 服务器内部错误 |

### SSE 错误事件

当执行过程中发生错误时，会通过 SSE 发送错误事件：

```json
{
  "type": "error",
  "agentName": null,
  "agentRole": null,
  "content": "错误描述信息",
  "timestamp": "2025-12-29T13:30:51.395213",
  "metadata": null
}
```

**常见错误：**
- `Topology not found: {id}` - 拓扑图不存在
- `Topology {name} has no Global Supervisor configured` - 未配置全局监督者
- `Topology {name} has no teams configured` - 未配置团队
- `Executor service unavailable` - Executor 服务不可用

---

## 4. 前端集成代码示例

### JavaScript (原生实现)

```javascript
class ExecutionManager {
  constructor() {
    this.currentRunId = null;
    this.abortController = null;
  }

  /**
   * 触发执行
   */
  async trigger(topologyId, userMessage, callbacks = {}) {
    const { onStarted, onEvent, onError, onComplete } = callbacks;

    this.abortController = new AbortController();

    try {
      const response = await fetch('/api/service/v1/executions/trigger', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify({ topologyId, userMessage }),
        signal: this.abortController.signal,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          onComplete?.();
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event = JSON.parse(line.slice(5));

              // ⭐ 捕获 runId（第一个事件）
              if (event.type === 'started' && event.runId) {
                this.currentRunId = event.runId;
                onStarted?.(event.runId);
              }

              onEvent?.(event);
            } catch (e) {
              console.warn('Failed to parse event:', line);
            }
          }
        }
      }
    } catch (error) {
      if (error.name !== 'AbortError') {
        onError?.(error);
      }
    }
  }

  /**
   * 取消执行
   */
  async cancel() {
    // 1. 中断 SSE 连接
    this.abortController?.abort();

    // 2. 调用取消 API
    if (!this.currentRunId) {
      console.warn('No runId available for cancellation');
      return false;
    }

    try {
      const response = await fetch('/api/service/v1/executions/cancel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ runId: this.currentRunId }),
      });

      const result = await response.json();
      return result.code === 'SUCCESS';
    } catch (error) {
      console.error('Failed to cancel:', error);
      return false;
    }
  }

  /**
   * 获取当前 runId
   */
  getRunId() {
    return this.currentRunId;
  }
}

// 使用示例
const executor = new ExecutionManager();

// 开始执行
executor.trigger(4, '分析系统性能', {
  onStarted: (runId) => {
    console.log('Execution started, runId:', runId);
    // 显示取消按钮
    showCancelButton();
  },
  onEvent: (event) => {
    if (event.type === 'error') {
      console.error('Error:', event.content);
    } else {
      appendToOutput(event.content);
    }
  },
  onError: (error) => {
    console.error('Connection error:', error);
  },
  onComplete: () => {
    console.log('Execution completed');
    hideCancelButton();
  },
});

// 取消按钮点击事件
cancelButton.onclick = async () => {
  const success = await executor.cancel();
  if (success) {
    console.log('Cancelled successfully');
  }
};
```

### TypeScript + React Hook

```typescript
import { useState, useCallback, useRef } from 'react';

interface ExecutionEvent {
  type: string | null;
  runId?: string;  // 仅在 started 事件中存在
  agentName: string | null;
  agentRole: string | null;
  content: string | null;
  timestamp: string;
  metadata: Record<string, unknown> | null;
}

interface UseExecutionOptions {
  onEvent?: (event: ExecutionEvent) => void;
  onError?: (error: Error) => void;
  onComplete?: () => void;
}

export function useExecution(options: UseExecutionOptions = {}) {
  const [isExecuting, setIsExecuting] = useState(false);
  const [events, setEvents] = useState<ExecutionEvent[]>([]);
  const [error, setError] = useState<Error | null>(null);
  const [runId, setRunId] = useState<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const trigger = useCallback(async (topologyId: number, userMessage: string) => {
    setIsExecuting(true);
    setEvents([]);
    setError(null);
    setRunId(null);

    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch('/api/service/v1/executions/trigger', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify({ topologyId, userMessage }),
        signal: abortControllerRef.current.signal,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body!.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          options.onComplete?.();
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ExecutionEvent = JSON.parse(line.slice(5));

              // 捕获 runId
              if (event.type === 'started' && event.runId) {
                setRunId(event.runId);
              }

              setEvents((prev) => [...prev, event]);
              options.onEvent?.(event);
            } catch (e) {
              // Skip invalid JSON
            }
          }
        }
      }
    } catch (err) {
      if ((err as Error).name !== 'AbortError') {
        const error = err instanceof Error ? err : new Error(String(err));
        setError(error);
        options.onError?.(error);
      }
    } finally {
      setIsExecuting(false);
    }
  }, [options]);

  const cancel = useCallback(async () => {
    // 1. 中断 SSE 连接
    abortControllerRef.current?.abort();

    // 2. 调用取消 API
    if (!runId) return false;

    try {
      const response = await fetch('/api/service/v1/executions/cancel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ runId }),
      });
      const result = await response.json();
      return result.code === 'SUCCESS';
    } catch {
      return false;
    }
  }, [runId]);

  return {
    trigger,
    cancel,
    isExecuting,
    events,
    error,
    runId,  // 暴露 runId
  };
}

// 使用示例
function ExecutionPanel() {
  const { trigger, cancel, isExecuting, events, error, runId } = useExecution({
    onEvent: (event) => {
      if (event.type === 'error') {
        console.error('Execution error:', event.content);
      }
    },
    onComplete: () => {
      console.log('Execution completed');
    },
  });

  const handleExecute = () => {
    trigger(4, '分析系统性能');
  };

  const handleCancel = async () => {
    const success = await cancel();
    if (success) {
      console.log('Cancelled');
    }
  };

  return (
    <div>
      <button onClick={handleExecute} disabled={isExecuting}>
        {isExecuting ? '执行中...' : '开始执行'}
      </button>

      {isExecuting && runId && (
        <button onClick={handleCancel} className="cancel-btn">
          取消执行
        </button>
      )}

      {runId && <div className="run-id">Run ID: {runId}</div>}
      {error && <div className="error">{error.message}</div>}

      <div className="output">
        {events.map((event, index) => (
          <div key={index} className="event">
            {event.content}
          </div>
        ))}
      </div>
    </div>
  );
}
```

### Vue 3 Composition API

```typescript
import { ref, readonly } from 'vue';

interface ExecutionEvent {
  type: string | null;
  agentName: string | null;
  agentRole: string | null;
  content: string | null;
  timestamp: string;
  metadata: Record<string, unknown> | null;
}

export function useExecution() {
  const isExecuting = ref(false);
  const events = ref<ExecutionEvent[]>([]);
  const error = ref<Error | null>(null);

  async function trigger(topologyId: number, userMessage: string) {
    isExecuting.value = true;
    events.value = [];
    error.value = null;

    try {
      const response = await fetch('/api/service/v1/executions/trigger', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify({ topologyId, userMessage }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body!.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();

        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const event: ExecutionEvent = JSON.parse(line.slice(5));
              events.value.push(event);
            } catch (e) {
              // Skip invalid JSON
            }
          }
        }
      }
    } catch (err) {
      error.value = err instanceof Error ? err : new Error(String(err));
    } finally {
      isExecuting.value = false;
    }
  }

  return {
    trigger,
    isExecuting: readonly(isExecuting),
    events: readonly(events),
    error: readonly(error),
  };
}
```

---

## 内容解析建议

### 识别 Agent 角色

从 `content` 字段解析 Agent 信息：

```javascript
function parseAgentInfo(content) {
  if (!content) return null;

  // Global Supervisor
  if (content.startsWith('[Global Supervisor]')) {
    return { role: 'global_supervisor', name: 'Global Supervisor' };
  }

  // Team Supervisor: [Team: TeamName | Supervisor]
  const teamSupervisorMatch = content.match(/\[Team: (.+?) \| Supervisor\]/);
  if (teamSupervisorMatch) {
    return { role: 'team_supervisor', team: teamSupervisorMatch[1] };
  }

  // Worker: [Team: TeamName | Worker: WorkerName]
  const workerMatch = content.match(/\[Team: (.+?) \| Worker: (.+?)\]/);
  if (workerMatch) {
    return { role: 'worker', team: workerMatch[1], name: workerMatch[2] };
  }

  return null;
}
```

### 识别事件类型

```javascript
function getEventType(content) {
  if (!content) return 'unknown';

  if (content.includes('🎯 开始分析任务')) return 'task_start';
  if (content.includes('💭 思考中')) return 'thinking';
  if (content.includes('SELECT:')) return 'team_selection';
  if (content.includes('👔 开始协调')) return 'coordination';
  if (content.includes('🔬 开始工作')) return 'work_start';
  if (content.includes('THINKING:')) return 'agent_thinking';

  return 'output';
}
```

---

## UI 展示建议

### 推荐的 UI 结构

```
┌─────────────────────────────────────────────────────────┐
│ 执行控制区                                                │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 拓扑图: [下拉选择 ▼]                                   │ │
│ │ 任务描述: [输入框...                              ]   │ │
│ │ [开始执行] [停止]                                     │ │
│ └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│ 执行状态: ● 执行中 (已运行 15s)                          │
├─────────────────────────────────────────────────────────┤
│ 执行过程                                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 🎯 Global Supervisor                                 │ │
│ │    开始分析任务                                       │ │
│ │    💭 思考中...                                      │ │
│ │    ├─ 选择团队: Firestone                           │ │
│ │    └─ 子任务: 分析应用层系统性能                      │ │
│ │                                                      │ │
│ │ 👔 Team: Firestone | Supervisor                     │ │
│ │    开始协调                                          │ │
│ │    └─ 分配给: Updated Agent Name                    │ │
│ │                                                      │ │
│ │ 🔬 Worker: Updated Agent Name                       │ │
│ │    # 应用层系统性能分析完整指南                       │ │
│ │    ## 1. 关键性能指标详解                            │ │
│ │    ...                                              │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 样式建议

- **Global Supervisor**: 使用蓝色标识
- **Team Supervisor**: 使用绿色标识
- **Worker**: 使用橙色标识
- **错误**: 使用红色背景
- **思考过程**: 使用灰色/斜体显示
- **实际输出**: 使用 Markdown 渲染

---

## 测试

### 使用 curl 测试

```bash
curl -X POST "http://localhost:8081/api/service/v1/executions/trigger" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"topologyId": 4, "userMessage": "分析系统性能"}'
```

### 前置条件

1. 确保拓扑图已创建并配置了 Global Supervisor
2. 确保至少有一个团队配置了 Worker Agent
3. 确保 Executor 服务已启动 (端口 8082)

---

## 相关 API

### 查询层级团队结构

如需在触发执行前预览团队结构：

```
POST /api/service/v1/topologies/hierarchical-team
Content-Type: application/json

{
  "topologyId": 4
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "topologyId": 4,
    "topologyName": "系统运维拓扑",
    "globalSupervisor": {
      "id": 1,
      "name": "运维总监",
      "role": "GLOBAL_SUPERVISOR",
      "specialty": "..."
    },
    "teams": [
      {
        "nodeId": 10,
        "nodeName": "Firestone",
        "supervisor": { ... },
        "workers": [ ... ]
      }
    ]
  }
}
```

---

## 常见问题

### Q: SSE 连接超时怎么办？
A: 执行时间较长时可能超过默认超时。建议前端设置较长的超时时间或不设置超时。

### Q: 如何取消正在执行的任务？
A: 分两步：
1. 从 `started` 事件中获取 `runId`
2. 调用 `POST /api/service/v1/executions/cancel` 并传入 `runId`

注意：取消是异步的，Agent 可能需要一些时间才能完全停止。

### Q: 事件内容是流式的吗？
A: 是的，内容按 token 级别流式返回，可实现打字机效果。

### Q: 如何处理断线重连？
A: 当前版本不支持断线重连。如果连接断开，需要重新触发执行。
