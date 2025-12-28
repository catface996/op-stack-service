# Research: 移除 Agent-Tools 绑定功能

**Feature**: 001-remove-agent-tools
**Date**: 2025-12-28

## Overview

本功能为代码清理类任务，无需技术选型或架构研究。以下记录待移除的代码和文件清单。

## 待删除文件清单

### 1. 领域层 (domain)

| 文件 | 路径 | 说明 |
|------|------|------|
| AgentToolRelation.java | `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/agent/` | Agent-Tool 关联领域模型 |
| AgentToolRelationRepository.java | `domain/repository-api/src/main/java/com/catface996/aiops/repository/agent/` | 仓储接口 |

### 2. 基础设施层 (infrastructure)

| 文件 | 路径 | 说明 |
|------|------|------|
| AgentToolRelationPO.java | `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/agent/` | 持久化对象 |
| AgentToolRelationMapper.java | `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/agent/` | MyBatis Mapper |
| AgentToolRelationRepositoryImpl.java | `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/agent/` | 仓储实现 |

## 待修改文件清单

### 1. 领域层

| 文件 | 修改内容 |
|------|----------|
| Agent.java | 移除 `toolIds` 字段及其 getter/setter |

### 2. 应用层

| 文件 | 修改内容 |
|------|----------|
| AgentDTO.java | 移除 `toolIds` 字段 |
| CreateAgentRequest.java | 移除 `toolIds` 字段 |
| UpdateAgentRequest.java | 移除 `toolIds` 字段 |
| AgentApplicationServiceImpl.java | 移除 `AgentToolRelationRepository` 依赖、`updateAgentTools` 方法、所有 toolIds 相关逻辑 |

### 3. 接口层

| 文件 | 修改内容 |
|------|----------|
| AgentController.java | 移除类注释中的 Tools 绑定说明，移除方法日志中的 toolIds 参数 |

## 数据库迁移

### 需要删除的表

- `agent_2_tool`: Agent-Tool 关联表

### 需要删除的迁移脚本引用

无需删除历史迁移脚本（保持迁移历史完整性），只需创建新的 drop table 迁移脚本。

### 迁移脚本规划

```sql
-- V20__Drop_agent_tool_relation_table.sql
DROP TABLE IF EXISTS agent_2_tool;
```

## 依赖分析

### AgentToolRelationRepository 依赖方

- `AgentApplicationServiceImpl`: 构造器注入，用于 Tools 绑定查询和更新

### toolIds 字段依赖方

- `toDTO()` 方法：将 Agent 转换为 DTO 时填充 toolIds
- `listAgents()` 方法：批量查询 toolIds
- `getAgentById()` 方法：查询单个 Agent 的 toolIds
- `createAgent()` 方法：创建 Agent 时绑定 Tools
- `updateAgent()` 方法：更新 Agent 时全量替换 Tools

## 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| 编译失败 | 低 | 按依赖顺序删除，先删除使用方再删除定义方 |
| API 不兼容 | 低 | 已确认该功能移至其他服务，本服务不再需要 |
| 数据丢失 | 低 | `agent_2_tool` 表数据不再需要 |

## 结论

本次移除操作明确、风险可控，无需额外研究或技术决策。
