# Quickstart: 移除 Agent-Tools 绑定功能验证

**Feature**: 001-remove-agent-tools
**Date**: 2025-12-28

## 验证步骤

### 1. 编译验证

```bash
# 清理并编译项目
mvn clean package -DskipTests

# 预期结果：BUILD SUCCESS
```

### 2. 启动验证

```bash
# 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 预期结果：
# - 应用正常启动
# - Flyway 迁移执行成功（V20 删除 agent_2_tool 表）
# - 无启动报错
```

### 3. API 功能验证

#### 3.1 查询 Agent 列表

```bash
curl -X POST "http://localhost:8081/api/service/v1/agents/list" \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}'
```

**预期结果**：
- 返回成功
- 响应 JSON 中的 Agent 对象**不包含** `toolIds` 字段

#### 3.2 查询 Agent 详情

```bash
curl -X POST "http://localhost:8081/api/service/v1/agents/get" \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

**预期结果**：
- 返回成功
- 响应 JSON 中**不包含** `toolIds` 字段

#### 3.3 创建 Agent

```bash
curl -X POST "http://localhost:8081/api/service/v1/agents/create" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Agent After Removal",
    "role": "WORKER",
    "specialty": "测试"
  }'
```

**预期结果**：
- 创建成功
- 响应 JSON 中**不包含** `toolIds` 字段

#### 3.4 更新 Agent

```bash
curl -X POST "http://localhost:8081/api/service/v1/agents/update" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "Updated Agent Name"
  }'
```

**预期结果**：
- 更新成功
- 响应 JSON 中**不包含** `toolIds` 字段

### 4. 数据库验证

```sql
-- 连接 MySQL 数据库执行
USE aiops_local;

-- 验证表已删除
SHOW TABLES LIKE 'agent_2_tool';
-- 预期结果：Empty set (0 rows)

-- 验证迁移记录
SELECT version, description FROM flyway_schema_history WHERE version = '20';
-- 预期结果：显示 V20 迁移记录
```

### 5. Swagger 文档验证

访问 Swagger UI：`http://localhost:8081/swagger-ui/index.html`

**验证项**：
- Agent 相关接口的请求和响应 Schema 中不再显示 `toolIds` 字段
- Agent Controller 的描述中不再包含 Tools 绑定相关说明

## 验证清单

| 验证项 | 预期结果 | 实际结果 |
|--------|----------|----------|
| 编译成功 | BUILD SUCCESS | ☐ |
| 应用启动正常 | 无报错启动 | ☐ |
| V20 迁移执行 | agent_2_tool 表已删除 | ☐ |
| Agent 列表查询 | 不含 toolIds 字段 | ☐ |
| Agent 详情查询 | 不含 toolIds 字段 | ☐ |
| Agent 创建 | 成功，不含 toolIds | ☐ |
| Agent 更新 | 成功，不含 toolIds | ☐ |
| Swagger 文档 | 无 toolIds 字段 | ☐ |

## 回滚指南

如需回滚，请：

1. 恢复已删除的代码文件（从 Git 历史）
2. 恢复已修改的文件中的 toolIds 相关代码
3. 创建新的迁移脚本重建 `agent_2_tool` 表

**注意**：回滚后原有的 Tool 绑定数据将丢失。
