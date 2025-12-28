# Research: Agent Management API

**Feature**: 027-agent-management
**Date**: 2025-12-28
**Status**: Complete

## Research Summary

本功能无需外部研究，所有技术决策基于项目现有架构和宪法规范。

## Technical Decisions

### 1. Agent ID 生成策略

**Decision**: 使用 MySQL 自增 Long 类型作为主键

**Rationale**:
- 项目现有实体（Topology、Node、Report 等）均使用 Long 类型自增 ID
- 保持一致性，简化关联查询
- MyBatis-Plus 对 Long ID 有良好支持

**Alternatives Considered**:
- UUID: 需求文档建议使用，但与项目现有风格不一致
- 带前缀的 ID（如 `agent-xxx`）：增加复杂度，无实际收益

### 2. AgentConfig 存储策略

**Decision**: 将 AgentConfig 作为 JSON 字段存储在 Agent 表中

**Rationale**:
- 配置字段（model、temperature、systemInstruction、defaultContext）属于 Agent 的内聚属性
- JSON 存储避免额外的关联表
- MyBatis-Plus TypeHandler 支持 JSON 自动转换

**Alternatives Considered**:
- 独立的 agent_config 表：增加关联复杂度，无必要
- 扁平化存储：systemInstruction 和 defaultContext 可能较长，JSON 更灵活

### 3. AgentFindings 存储策略

**Decision**: 将 findings（warnings、critical）作为独立字段存储

**Rationale**:
- 仅包含两个简单数值字段
- 便于查询和聚合统计
- 避免 JSON 解析开销

### 4. 团队关联设计

**Decision**: 使用独立的 `agent_2_team` 关联表实现 Agent 与 Team 的多对多关系

**Rationale**:
- Agent 可以被分配到多个 Team，是多对多关系
- 关联表支持软删除，便于追踪分配历史
- 唯一索引 (agent_id, team_id, deleted) 防止重复分配

**Alternatives Considered**:
- Agent 表存储 team_id 外键：仅支持多对一关系，不满足需求

### 5. Agent 模板实现方式

**Decision**: 使用枚举类 + 静态方法返回预定义模板

**Rationale**:
- 5 种固定模板，无需动态管理
- 枚举保证类型安全
- 无需数据库存储

**Alternatives Considered**:
- 数据库存储模板：增加管理复杂度，需求未要求模板 CRUD
- 配置文件：不如枚举直观

### 6. 单例约束（GLOBAL_SUPERVISOR）实现

**Decision**: 在应用层创建时检查 + 数据库唯一约束

**Rationale**:
- 双重保障：应用层快速失败 + 数据库最终一致性
- 使用 `UNIQUE INDEX` 在 role='GLOBAL_SUPERVISOR' 条件下保证唯一

**Implementation**:
```sql
-- 部分索引：仅对 GLOBAL_SUPERVISOR 角色生效
CREATE UNIQUE INDEX uk_agent_global_supervisor
ON agent(role)
WHERE role = 'GLOBAL_SUPERVISOR' AND deleted = 0;
```

注：MySQL 8.0 不支持部分索引，改用应用层检查。

### 7. 名称唯一性约束

**Decision**: 同一团队内名称唯一，通过应用层检查实现

**Rationale**:
- 需求指定"同一团队内"唯一
- 包含 NULL team_id（未分配团队）的 Agent 名称也应唯一
- MySQL 处理 NULL 值的唯一约束较复杂，应用层检查更可控

### 8. 软删除实现

**Decision**: 使用 `deleted` 字段 + MyBatis-Plus `@TableLogic` 注解

**Rationale**:
- 项目现有实体统一使用此模式
- 自动过滤已删除记录
- 保留历史数据便于审计

### 9. 统计数据实现

**Decision**: 第一阶段仅实现基于 Agent 表的统计（总数、角色分布、状态分布、findings 汇总）

**Rationale**:
- ExecutionLog 功能在 Out of Scope，无法提供执行次数、成功率、平均执行时间
- 返回可用数据，执行相关统计字段返回默认值（0）
- 后续扩展 ExecutionLog 后可补充

## Existing Patterns Reference

### 参考现有实现

| 模块 | 参考文件 | 参考内容 |
|------|----------|----------|
| Report | `ReportController.java` | POST-Only API 风格、Swagger 注解 |
| Report | `ReportApplicationServiceImpl.java` | 应用服务实现模式 |
| Report | `ReportRepositoryImpl.java` | 仓储实现、PO 转换 |
| PromptTemplate | `PromptTemplateController.java` | 分页查询实现 |
| Common | `PageableRequest.java` | 分页请求基类 |
| Common | `PageResult.java` | 分页响应结构 |
| Common | `Result.java` | 统一响应格式 |

## Risk Assessment

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Team 实体不存在 | 团队分配功能无法验证 | 分配时仅验证 teamId 非空，不做存在性检查 |
| 单例约束并发问题 | 可能创建多个 GLOBAL_SUPERVISOR | 使用数据库唯一索引作为最终保障 |
| 统计数据不完整 | 用户无法查看执行统计 | 文档说明当前返回可用数据 |

## Conclusion

所有技术决策已确定，无需进一步研究。可以进入 Phase 1: Design & Contracts。
