# Research: Node-Agent 绑定功能

**Feature**: 031-node-agent-binding
**Date**: 2025-12-28
**Status**: Complete

## Executive Summary

本功能实现 Agent 与 ResourceNode 的多对多关联。通过分析项目现有架构和类似功能（如 `agent_2_team`, `topology_2_node`），确定采用关联表 `node_2_agent` 实现，所有 HTTP 接口放在 NodeController 下。

## Existing Patterns Analysis

### 1. 关联表设计模式

项目已有两个多对多关联表实现：

#### agent_2_team (V15 migration)
```sql
CREATE TABLE agent_2_team (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    agent_id        BIGINT          NOT NULL,
    team_id         BIGINT          NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'IDLE',
    current_task    VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_agent_team (agent_id, team_id, deleted)
);
```

#### topology_2_node (V12 migration)
```sql
CREATE TABLE topology_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topology_id BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    position_x INT,
    position_y INT,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by BIGINT NOT NULL,
    UNIQUE KEY uk_topology_node (topology_id, node_id)
);
```

**结论**: `node_2_agent` 应采用类似结构，包含 `id`, `node_id`, `agent_id`, `created_at`, `deleted` 字段。

### 2. 软删除机制

项目普遍使用 `deleted TINYINT DEFAULT 0` 字段实现软删除：
- `0`: 未删除
- `1`: 已删除

唯一约束包含 `deleted` 字段以支持"删除后可重新创建"的场景。

### 3. DDD 分层结构

现有 Node 模块结构：

```
domain/
├── domain-model/.../model/node/
│   ├── Node.java
│   ├── NodeStatus.java
│   └── NodeType.java
└── repository-api/.../repository/node/
    └── NodeRepository.java

infrastructure/repository/mysql-impl/.../
├── impl/node/NodeRepositoryImpl.java
├── mapper/node/NodeMapper.java
└── po/node/NodePO.java

application/
├── application-api/.../dto/node/
│   ├── NodeDTO.java
│   └── request/*.java
└── application-impl/.../service/node/
    └── NodeApplicationServiceImpl.java

interface/interface-http/.../controller/
    └── NodeController.java
```

### 4. POST-Only API 设计

所有业务接口统一使用 POST 方法，请求/响应格式：

**请求**: JSON Body
```java
@PostMapping("/create")
public ResponseEntity<Result<NodeDTO>> createNode(
    @Valid @RequestBody CreateNodeRequest request)
```

**响应**: `Result<T>` 包装
```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": { ... }
}
```

### 5. Controller 注解规范

```java
@Slf4j
@RestController
@RequestMapping("/api/service/v1/nodes")
@RequiredArgsConstructor
@Tag(name = "资源节点管理", description = "...")
public class NodeController {
    @PostMapping("/action")
    @Operation(summary = "...", description = "...")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {...})
    public ResponseEntity<Result<T>> action(@Valid @RequestBody Request req) {}
}
```

## Technical Decisions

### Decision 1: 关联表结构

**选择**: 简化版关联表（无状态字段）

```sql
CREATE TABLE node_2_agent (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    node_id     BIGINT NOT NULL,
    agent_id    BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_node_id (node_id),
    INDEX idx_agent_id (agent_id),
    UNIQUE INDEX uk_node_agent (node_id, agent_id, deleted)
);
```

**理由**:
- 不需要 `agent_2_team` 的 `status`, `current_task` 字段（监控关系不需要状态跟踪）
- 不需要 `topology_2_node` 的位置字段（无可视化需求）
- 保持最简设计

### Decision 2: 级联删除策略

**选择**: 应用层软删除（非数据库级联）

**理由**:
- 项目使用软删除，数据库级联会物理删除记录
- 在 NodeApplicationService 和 AgentApplicationService 删除方法中添加关联记录软删除逻辑
- 保持数据可追溯性

### Decision 3: 接口归属

**选择**: 所有接口放在 NodeController（非 AgentController）

**接口列表**:
| 接口 | 路径 | 说明 |
|------|------|------|
| 绑定 | POST /api/service/v1/nodes/bindAgent | 将 Agent 绑定到 Node |
| 解绑 | POST /api/service/v1/nodes/unbindAgent | 解除绑定关系 |
| 按节点查询 | POST /api/service/v1/nodes/listAgents | 查询节点关联的 Agent 列表 |
| 按 Agent 查询 | POST /api/service/v1/nodes/listNodesByAgent | 查询 Agent 关联的节点列表 |

**理由**: 用户明确要求接口放在 Node 模块下

### Decision 4: 查询返回格式

**选择**: 返回完整 DTO 对象而非仅 ID

- `listAgents` 返回 `List<AgentDTO>` 而非 `List<Long>`
- `listNodesByAgent` 返回 `List<NodeDTO>` 而非 `List<Long>`

**理由**: 减少前端额外请求，一次查询获取完整信息

### Decision 5: 批量操作支持

**选择**: 首期仅支持单个绑定/解绑

**理由**:
- 满足基本需求
- 保持接口简单
- 如需批量操作，可后续扩展 `bindAgents`, `unbindAgents` 接口

## Risk Analysis

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 大量关联查询性能 | 中 | 添加索引，必要时分页 |
| 并发重复绑定 | 低 | 数据库唯一约束保证 |
| 级联删除遗漏 | 中 | 单元测试覆盖删除场景 |

## Dependencies

### 内部依赖
- `Node` 领域模型 (domain-model)
- `Agent` 领域模型 (domain-model)
- `NodeRepository` (repository-api)
- `AgentRepository` (repository-api)

### 外部依赖
- MyBatis-Plus 3.5.x
- SpringDoc OpenAPI
- Flyway (migration)

## Conclusion

功能实现方案明确：
1. 创建 `node_2_agent` 关联表（V21 迁移）
2. 在 domain 层添加 `NodeAgentRelation` 领域模型
3. 在 infrastructure 层添加 Repository 实现
4. 在 application 层扩展 `NodeApplicationService`
5. 在 interface 层扩展 `NodeController`

预计新增文件 8 个，修改文件 3 个，符合项目架构规范。
