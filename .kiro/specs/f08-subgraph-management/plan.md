# Implementation Plan: Subgraph Management v2.0

**Branch**: `f08-subgraph-management-v2` | **Date**: 2025-12-22 | **Spec**: [requirements.md](./requirements.md)
**Input**: Feature specification from `.kiro/specs/f08-subgraph-management/requirements.md`

## Summary

将子图管理功能从独立实体模式 (v1.0) 重构为资源类型模式 (v2.0)。子图作为 `resource_type` 中的一种类型（code=SUBGRAPH）存储在统一的 `resource` 表中，复用资源的权限模型和管理机制，同时新增嵌套子图支持和循环检测功能。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, Spring Security 6.4.x
**Storage**: MySQL 8.0+, Redis 7.0+
**Testing**: JUnit 5, Mockito, TestContainers
**Target Platform**: Linux server (Docker/K8s)
**Project Type**: DDD 分层架构（单体应用）
**Performance Goals**:
- 列表查询 < 1s (1000 子图)
- 详情加载 < 2s (500 成员)
- 拓扑渲染 < 3s (500 节点 + 1000 关系)
- 操作响应 < 500ms

**Constraints**:
- 最大嵌套深度: 10 层
- 每子图最大成员数: 500
- 并发用户: 100

**Scale/Scope**: 中等规模重构，涉及 4 层架构变更

## Constitution Check

*GATE: Must pass before implementation*

| 原则 | 状态 | 说明 |
|------|------|------|
| DDD 分层架构 | ✅ PASS | 遵循 Interface → Application → Domain → Infrastructure |
| Application Service 只调用 Domain Service | ✅ PASS | 不直接调用 Repository |
| Repository 接口定义在 Domain 层 | ✅ PASS | SubgraphMemberRepository 在 repository-api |
| 单一职责 | ✅ PASS | 拆分为成员管理专用服务 |
| 复用优先 | ✅ PASS | 复用 Resource API 和权限模型 |

## Project Structure

### Documentation (this feature)

```text
.kiro/specs/f08-subgraph-management/
├── plan.md              # 本文件 - 实现计划
├── research.md          # Phase 0 - 技术研究
├── data-model.md        # Phase 1 - 数据模型
├── quickstart.md        # Phase 1 - 快速开始指南
├── contracts/           # Phase 1 - API 契约
│   └── subgraph-member-api.yaml
├── requirements.md      # 需求规格 (已存在)
├── design.md            # 设计文档 (已存在)
└── tasks.md             # Phase 2 - 任务列表 (待生成)
```

### Source Code (repository root)

```text
# 新增/修改的文件

domain/
├── domain-api/
│   └── src/main/java/com/catface996/aiops/domain/
│       ├── model/subgraph/
│       │   └── SubgraphMember.java            [NEW]
│       └── service/subgraph/
│           └── SubgraphMemberDomainService.java [NEW]
├── domain-impl/
│   └── src/main/java/com/catface996/aiops/domain/
│       └── service/subgraph/
│           └── SubgraphMemberDomainServiceImpl.java [NEW]
└── repository-api/
    └── src/main/java/com/catface996/aiops/repository/
        └── subgraph/
            └── SubgraphMemberRepository.java  [NEW]

application/
├── application-api/
│   └── src/main/java/com/catface996/aiops/application/
│       ├── dto/subgraph/
│       │   ├── AddMembersCommand.java         [NEW]
│       │   ├── RemoveMembersCommand.java      [NEW]
│       │   ├── SubgraphMemberDTO.java         [NEW]
│       │   └── TopologyQueryCommand.java      [NEW]
│       └── service/subgraph/
│           └── SubgraphMemberApplicationService.java [NEW]
└── application-impl/
    └── src/main/java/com/catface996/aiops/application/
        └── service/subgraph/
            └── SubgraphMemberApplicationServiceImpl.java [NEW]

interface/
└── interface-http/
    └── src/main/java/com/catface996/aiops/http/
        ├── controller/
        │   └── SubgraphMemberController.java  [NEW]
        ├── request/subgraph/
        │   ├── AddMembersRequest.java         [NEW]
        │   └── RemoveMembersRequest.java      [NEW]
        └── response/subgraph/
            ├── SubgraphMemberListResponse.java [NEW]
            ├── SubgraphMembersWithRelationsResponse.java [NEW]
            └── TopologyGraphResponse.java     [MODIFY]

infrastructure/
└── repository/
    └── mysql-impl/
        └── src/main/
            ├── java/com/catface996/aiops/repository/
            │   └── subgraph/
            │       ├── SubgraphMemberRepositoryImpl.java [NEW]
            │       ├── mapper/
            │       │   └── SubgraphMemberMapper.java [NEW]
            │       └── po/
            │           └── SubgraphMemberPO.java [NEW]
            └── resources/mapper/
                └── SubgraphMemberMapper.xml   [NEW]

bootstrap/
└── src/main/resources/
    └── db/migration/
        └── V7__Add_subgraph_member_table.sql  [NEW]

# 需要修改的现有文件
domain/domain-api/.../service/subgraph/SubgraphDomainService.java [MODIFY - deprecate]
application/application-api/.../service/subgraph/SubgraphApplicationService.java [MODIFY - deprecate]
interface/interface-http/.../controller/SubgraphController.java [MODIFY - redirect to Resource API]
```

**Structure Decision**: 采用 DDD 分层架构，新增 SubgraphMember 相关组件，复用 Resource 相关组件进行子图 CRUD。

## Implementation Phases

### Phase 1: 基础设施准备 (Day 1)

**目标**: 数据库迁移和基础类定义

1. 创建数据库迁移文件 `V7__Add_subgraph_member_table.sql`
   - 添加 SUBGRAPH 资源类型到 `resource_type` 表
   - 创建 `subgraph_member` 表
   - 添加必要索引和外键约束

2. 定义领域模型
   - 创建 `SubgraphMember` 实体类
   - 创建 `SubgraphMemberRepository` 接口

3. 创建持久化实现
   - 创建 `SubgraphMemberPO` 持久化对象
   - 创建 `SubgraphMemberMapper` MyBatis 接口
   - 创建 `SubgraphMemberMapper.xml` 映射文件
   - 实现 `SubgraphMemberRepositoryImpl`

### Phase 2: 领域层实现 (Day 2)

**目标**: 实现核心业务逻辑

1. 创建 `SubgraphMemberDomainService` 接口
   - 成员添加/移除方法
   - 循环检测方法
   - 嵌套展开方法
   - 拓扑查询方法

2. 实现 `SubgraphMemberDomainServiceImpl`
   - 实现循环检测算法 (DFS)
   - 实现嵌套子图展开逻辑
   - 实现成员管理逻辑
   - 权限验证（复用 ResourceDomainService）

3. 单元测试
   - 循环检测测试
   - 嵌套展开测试
   - 边界条件测试

### Phase 3: 应用层实现 (Day 3)

**目标**: 实现应用服务和 DTO 转换

1. 创建 DTO 类
   - `AddMembersCommand`
   - `RemoveMembersCommand`
   - `SubgraphMemberDTO`
   - `TopologyQueryCommand`

2. 创建 `SubgraphMemberApplicationService` 接口
   - 成员管理命令
   - 成员查询
   - 拓扑查询

3. 实现 `SubgraphMemberApplicationServiceImpl`
   - 事务管理
   - DTO 转换
   - 调用领域服务

4. 单元测试

### Phase 4: 接口层实现 (Day 4)

**目标**: 实现 REST API 端点

1. 创建 Request/Response 类
   - `AddMembersRequest`
   - `RemoveMembersRequest`
   - `SubgraphMemberListResponse`
   - `SubgraphMembersWithRelationsResponse`

2. 创建 `SubgraphMemberController`
   - `POST /subgraphs/{id}/members` - 添加成员
   - `DELETE /subgraphs/{id}/members` - 移除成员
   - `GET /subgraphs/{id}/members` - 查询成员列表
   - `GET /subgraphs/{id}/members-with-relations` - 查询成员和关系
   - `GET /subgraphs/{id}/topology` - 获取拓扑数据
   - `GET /subgraphs/{id}/ancestors` - 获取祖先子图

3. 更新 `SubgraphController`
   - 将子图 CRUD 操作重定向到 Resource API
   - 添加废弃注解

### Phase 5: 测试和验证 (Day 5)

**目标**: 完整的测试覆盖

1. 集成测试
   - 完整的成员管理流程
   - 循环检测场景
   - 嵌套展开场景
   - 权限验证场景

2. E2E 测试脚本
   - 子图创建（通过 Resource API）
   - 成员管理
   - 拓扑查询

3. 性能验证
   - 列表查询性能
   - 拓扑渲染性能
   - 嵌套展开性能

### Phase 6: 数据迁移（可选）

**目标**: 迁移 v1.0 现有数据

1. 创建数据迁移脚本
   - 迁移 subgraph → resource
   - 迁移 subgraph_permission → resource_permission
   - 迁移 subgraph_resource → subgraph_member

2. 验证数据完整性

3. 清理旧表（可选，保留用于回滚）

## Complexity Tracking

> Constitution Check 已通过，无违规需要记录。

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 新增模块数 | 0 | 仅在现有模块内添加类 |
| 新增表 | 1 | subgraph_member 表 |
| 修改表 | 1 | resource_type (添加记录) |
| 新增 API 端点 | 6 | 成员管理专用端点 |
| 复用 API 端点 | 5+ | Resource API 用于子图 CRUD |

## Risk Mitigation

| 风险 | 缓解措施 |
|------|----------|
| 数据迁移失败 | 编写可回滚脚本，先在测试环境验证 |
| 循环检测性能 | 限制最大深度，使用递归 CTE 优化 |
| API 兼容性 | 旧 API 标记废弃但保留一段时间 |
| 嵌套深度过深 | 硬限制 10 层，超出返回错误 |

## Next Steps

运行 `/speckit.tasks` 生成详细的任务列表 (`tasks.md`)。

---

**Plan Version**: 2.0
**Created**: 2025-12-22
**Status**: Ready for Task Generation
