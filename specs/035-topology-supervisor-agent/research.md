# Research: Topology 绑定 Global Supervisor Agent

**Feature**: 035-topology-supervisor-agent
**Date**: 2025-12-29

## 1. 现有实现模式研究

### 1.1 Topology 表现有字段

**现状分析**:
- topology 表已有 `coordinator_agent_id` 字段（预留字段，当前为 NULL）
- 本功能新增 `global_supervisor_agent_id` 字段，与 coordinator 是不同的概念

**Decision**: 新增独立的 `global_supervisor_agent_id` 字段

**Rationale**:
- Global Supervisor 和 Coordinator 是不同角色，功能职责不同
- 保持字段独立，便于后续分别管理
- 遵循宪法外键字段命名规范 `{关联表单数}_id`

**Alternatives considered**:
- 复用 coordinator_agent_id：语义不同，不适合
- 使用关联表：一对一关系过于复杂

### 1.2 Agent 表角色定义

**现状分析**:
```sql
role VARCHAR(32) NOT NULL COMMENT '角色: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER'
```

**Decision**: 绑定时校验 Agent 的 role 必须为 `GLOBAL_SUPERVISOR`

**Rationale**:
- 确保绑定的 Agent 具有正确的角色
- 防止误绑定其他角色的 Agent

### 1.3 Controller 设计模式

**Decision**: 在现有 `TopologyController` 中添加 supervisor 相关端点

**Rationale**:
- 保持与 `/report-templates/{action}` 模式一致
- 使用 `/supervisor/{action}` 子路径，语义清晰

**Alternatives considered**:
- 独立 Controller：功能简单，不需要独立控制器
- AgentController 中实现：绑定关系属于 Topology 侧管理

## 2. 数据库设计研究

### 2.1 字段添加方案

**Decision**: 在 topology 表添加 `global_supervisor_agent_id` 字段

**DDL**:
```sql
ALTER TABLE topology
ADD COLUMN global_supervisor_agent_id BIGINT COMMENT 'Global Supervisor Agent ID';

ALTER TABLE topology
ADD INDEX idx_global_supervisor_agent_id (global_supervisor_agent_id);
```

**Rationale**:
- 字段可为 NULL（允许未绑定状态）
- 添加索引便于按 Agent 查询关联的拓扑图
- 不添加外键约束（与项目现有设计一致）

### 2.2 数据完整性

**Decision**: 通过应用层保证数据完整性

**Rationale**:
- 绑定时校验 Agent 存在且角色正确
- 不强制级联删除，Agent 删除后字段值保留

## 3. API 设计研究

### 3.1 接口命名

**Decision**: 使用 `/supervisor/{action}` 子路径

| 操作 | Endpoint | 说明 |
|------|----------|------|
| 绑定 | `/api/service/v1/topologies/supervisor/bind` | 绑定 Global Supervisor Agent |
| 解绑 | `/api/service/v1/topologies/supervisor/unbind` | 解绑 Global Supervisor Agent |

**Rationale**:
- 与现有 `/report-templates/{action}` 模式一致
- `supervisor` 简洁表达 Global Supervisor 概念

### 3.2 请求/响应设计

**Request 设计**:
- `BindSupervisorAgentRequest`: topologyId + agentId + operatorId
- `UnbindSupervisorAgentRequest`: topologyId + operatorId

**Response 设计**:
- 绑定/解绑：返回更新后的拓扑图详情（TopologyDTO）
- TopologyDTO 扩展：添加 globalSupervisorAgentId 和 Agent 基本信息

## 4. 分层实现研究

### 4.1 DDD 分层

**Decision**: 扩展现有类，不新建独立 Service

| 层 | 职责 | 文件 | 操作 |
|---|------|------|------|
| Interface | HTTP 控制器 | TopologyController | 扩展 |
| Application | 用例编排 | TopologyApplicationService | 扩展 |
| Domain | 业务逻辑 | TopologyDomainService | 扩展 |
| Infrastructure | 数据持久化 | TopologyRepository | 扩展 |

**Rationale**:
- 一对一关系简单，不需要独立的关联管理类
- 绑定/解绑只是更新 topology 表的一个字段

## 5. 业务规则研究

### 5.1 绑定规则

| 规则 | 说明 |
|------|------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 |
| Agent 存在性 | agent_id 必须对应未删除的 Agent |
| Agent 角色校验 | Agent 的 role 必须为 GLOBAL_SUPERVISOR |
| 替换绑定 | 已有绑定时，新绑定会替换旧绑定 |

### 5.2 解绑规则

| 规则 | 说明 |
|------|------|
| 拓扑图存在性 | topology_id 必须对应未删除的拓扑图 |
| 幂等操作 | 未绑定时解绑也返回成功 |

## 6. 总结

本功能实现简单，主要变更包括：

1. **数据库**: V27 迁移脚本添加 `global_supervisor_agent_id` 字段
2. **PO**: TopologyPO 添加新字段
3. **Repository**: 添加更新字段的方法
4. **Domain Service**: 添加绑定/解绑业务逻辑和校验
5. **Application Service**: 添加用例方法
6. **Controller**: 添加 2 个端点
7. **DTO**: TopologyDTO 扩展返回 Agent 信息
