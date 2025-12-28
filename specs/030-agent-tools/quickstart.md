# Quickstart: Agent Tools 绑定

**Feature**: 030-agent-tools | **Date**: 2025-12-28

## 快速验证指南

本文档提供快速验证 Agent Tools 绑定功能的步骤。

## 前置条件

1. 本地 MySQL 数据库运行中（端口 3306）
2. 数据库 `aiops_local` 已创建
3. 应用已启动（端口 8081）

```bash
# 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

## 验证场景

### 场景 1: 创建 Agent 并绑定 Tools

**目标**：验证创建 Agent 时可以同时指定 toolIds

```bash
# 1. 创建带 Tools 的 Agent
curl -X POST http://localhost:8081/api/service/v1/agents/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "诊断助手",
    "role": "WORKER",
    "specialty": "系统诊断",
    "toolIds": [1, 2, 3]
  }'

# 预期响应：
# - code: 0
# - data.toolIds: [1, 2, 3]（或过滤后的有效 ID）
```

### 场景 2: 查询 Agent 包含 toolIds

**目标**：验证查询 Agent 详情时返回 toolIds

```bash
# 2. 查询 Agent 详情（假设 id=1）
curl -X POST http://localhost:8081/api/service/v1/agents/get \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'

# 预期响应：
# - data.toolIds 包含已绑定的 Tool ID 列表
```

### 场景 3: 全量替换 Tools 绑定

**目标**：验证更新 Agent 时 toolIds 被全量替换

```bash
# 3. 更新 Agent，替换 Tools
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [4, 5]
  }'

# 预期响应：
# - data.toolIds: [4, 5]（原有的 [1,2,3] 被完全替换）
```

### 场景 4: 清空 Tools 绑定

**目标**：验证传入空列表可清空绑定

```bash
# 4. 清空 Tools 绑定
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": []
  }'

# 预期响应：
# - data.toolIds: []
```

### 场景 5: 验证边界处理

**目标**：验证去重和无效 ID 过滤

```bash
# 5a. 重复 ID 去重
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [1, 2, 1, 3, 2]
  }'

# 预期：toolIds 被去重为 [1, 2, 3]

# 5b. 无效 ID 过滤（假设 Tool ID 999 不存在）
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [1, 999, 2]
  }'

# 预期：toolIds 仅包含存在的 ID [1, 2]
```

### 场景 6: 工作状态限制

**目标**：验证 Agent 处于 WORKING/THINKING 状态时无法更新

```bash
# 前置：将 Agent 设为 WORKING 状态（通过 agent_2_team 表）

# 尝试更新
curl -X POST http://localhost:8081/api/service/v1/agents/update \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "toolIds": [1, 2]
  }'

# 预期响应：
# - HTTP 423
# - code: AGENT_IS_BUSY
```

## 数据库验证

```sql
-- 查看 Agent-Tool 关联表
SELECT * FROM agent_2_tool WHERE deleted = 0;

-- 查看特定 Agent 绑定的 Tools
SELECT tool_id FROM agent_2_tool
WHERE agent_id = 1 AND deleted = 0;

-- 查看某个 Tool 被哪些 Agent 使用
SELECT agent_id FROM agent_2_tool
WHERE tool_id = 1 AND deleted = 0;
```

## 常见问题

### Q1: toolIds 为 null 和空列表有什么区别？

- `toolIds: null` 或不传：不更新 Tools 绑定，保持原状
- `toolIds: []`：清空所有 Tools 绑定

### Q2: 如何验证 Tool ID 是否有效？

系统会查询 Tool 表验证 ID 是否存在：
- 存在的 ID：创建关联
- 不存在的 ID：静默忽略，不报错

### Q3: 绑定操作是否有事务保证？

是的，整个绑定操作在一个事务中完成：
1. 删除旧关联
2. 插入新关联
3. 任一步骤失败则回滚

## 下一步

完成基本验证后，可以：

1. 运行 `/speckit.tasks` 生成详细任务列表
2. 运行 `/speckit.implement` 执行实现
3. 编写集成测试用例
