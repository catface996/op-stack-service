# Research: 清理数据库废弃字段

**Feature**: 041-cleanup-obsolete-fields
**Date**: 2025-12-30

## 1. 数据迁移完整性验证

### 1.1 agent_bound 表数据完整性

**研究目标**: 验证 agent_bound 表是否包含所有需要的绑定关系

**发现**:

| 数据源 | 记录数 | 状态 |
|--------|--------|------|
| agent_bound (总计) | 15 条 | 目标表 |
| topology.global_supervisor_agent_id | 3 条 | 已迁移 |
| node_2_agent | 11 条 | 已迁移 |

**验证 SQL**:

```sql
-- 验证 topology.global_supervisor_agent_id 迁移完整性
SELECT COUNT(*) FROM topology t
WHERE t.global_supervisor_agent_id IS NOT NULL
  AND t.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.entity_type = 'TOPOLOGY'
      AND ab.entity_id = t.id
      AND ab.hierarchy_level = 'GLOBAL_SUPERVISOR'
      AND ab.deleted = 0
  );
-- 预期结果: 0

-- 验证 node_2_agent 迁移完整性
SELECT COUNT(*) FROM node_2_agent n2a
WHERE n2a.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM agent_bound ab
    WHERE ab.entity_type = 'NODE'
      AND ab.entity_id = n2a.node_id
      AND ab.agent_id = n2a.agent_id
      AND ab.deleted = 0
  );
-- 预期结果: 0
```

**决策**: 在执行 P3、P4 迁移脚本前，MUST 先运行验证 SQL 确认数据完整性。

### 1.2 代码依赖分析

**研究目标**: 确认废弃字段的代码引用范围

**node.agent_team_id 引用**:

| 文件 | 引用类型 | 需要修改 |
|------|----------|----------|
| CreateNodeRequest.java | 字段定义 | 删除字段 |
| UpdateNodeRequest.java | 字段定义 | 删除字段 |
| NodeDTO.java | 字段定义 | 删除字段 |
| Node.java | 字段定义 + getter/setter | 删除字段及方法 |
| NodeDomainService.java | 方法参数 | 移除参数 |
| NodeDomainServiceImpl.java | 方法实现 | 移除参数使用 |
| NodeApplicationServiceImpl.java | DTO 映射 | 移除映射 |
| NodePO.java | 字段定义 | 删除字段 |
| NodeMapper.xml | Base_Column_List | 移除字段 |

**topology.coordinator_agent_id 引用**:

| 文件 | 引用类型 | 需要修改 |
|------|----------|----------|
| CreateTopologyRequest.java | 字段定义 | 删除字段 |
| UpdateTopologyRequest.java | 字段定义 | 删除字段 |
| TopologyDTO.java | 字段定义 | 删除字段 |
| Topology.java | 字段定义 + getter/setter | 删除字段及方法 |
| TopologyDomainService.java | 方法参数 | 移除参数 |
| TopologyDomainServiceImpl.java | 方法实现 | 移除参数使用 |
| TopologyApplicationServiceImpl.java | DTO 映射 | 移除映射 |
| TopologyPO.java | 字段定义 | 删除字段 |
| TopologyMapper.xml | Base_Column_List | 移除字段 |

**topology.global_supervisor_agent_id 引用**:

| 文件 | 引用类型 | 需要修改 |
|------|----------|----------|
| TopologyDTO.java | 字段定义 | 删除字段 |
| Topology.java | 字段定义 + getter/setter | 删除字段及方法 |
| TopologyApplicationServiceImpl.java | DTO 映射 | 移除映射 |
| TopologyPO.java | 字段定义 | 删除字段 |

**node_2_agent 表相关代码**:

| 文件 | 需要操作 |
|------|----------|
| NodeAgentRelation.java | DELETE |
| NodeAgentRelationRepository.java | DELETE |
| NodeAgentRelationRepositoryImpl.java | DELETE |
| NodeAgentRelationPO.java | DELETE |
| NodeAgentRelationMapper.java | DELETE |
| NodeAgentRelationMapper.xml | DELETE |
| TopologyApplicationServiceImpl.java | 切换到使用 AgentBoundRepository |

**决策**: 按 DDD 分层顺序清理代码，确保编译通过后再执行数据库迁移。

## 2. Flyway 迁移最佳实践

### 2.1 字段删除迁移脚本模板

**决策**: 使用以下模板确保迁移可回滚：

```sql
-- V{N}__drop_{table}_{field}.sql
-- Description: Remove obsolete field {field} from {table} table

-- 1. 验证字段存在
SET @column_exists = (SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = '{table}'
    AND COLUMN_NAME = '{field}');

-- 2. 仅当字段存在时删除
SET @sql = IF(@column_exists > 0,
    'ALTER TABLE {table} DROP COLUMN {field}',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

### 2.2 表删除迁移脚本模板

**决策**: 删除表前验证数据已迁移：

```sql
-- V{N}__drop_{table}_table.sql
-- Description: Drop obsolete table {table}

-- 1. 验证数据已完全迁移（应返回 0）
SELECT COUNT(*) AS unMigrated FROM {table}
WHERE deleted = 0
  AND NOT EXISTS (/* migration check query */);

-- 2. 删除表
DROP TABLE IF EXISTS {table};
```

## 3. 清理顺序决策

### 3.1 代码清理顺序

**决策**: 采用"由外到内"的清理顺序，确保每步编译通过：

1. **DTO 层** (application-api): 移除请求/响应字段
2. **Application 层** (application-impl): 移除服务中的字段映射
3. **Domain API 层** (domain-api): 移除接口方法参数
4. **Domain Model 层** (domain-model): 移除实体字段
5. **Domain Impl 层** (domain-impl): 更新服务实现
6. **Infrastructure 层** (mysql-impl): 移除 PO 字段、Mapper 更新

**原因**:
- 避免编译错误级联
- 每层修改后可独立验证
- 便于代码审查

### 3.2 迁移执行顺序

**决策**: 按风险等级从低到高执行：

| 阶段 | 目标 | 风险 | 验证要求 |
|------|------|------|----------|
| P1 | node.agent_team_id | 低 | 编译通过 + API 测试 |
| P2 | topology.coordinator_agent_id | 低 | 编译通过 + API 测试 |
| P3 | topology.global_supervisor_agent_id | 中 | 数据完整性验证 + API 测试 |
| P4 | node_2_agent 表 | 中 | 数据完整性验证 + 功能测试 |

**原因**:
- 0 数据字段先清理，验证流程正确
- 有数据的字段/表后清理，确保迁移完整

## 4. 未解决问题

无。所有技术决策已明确。
