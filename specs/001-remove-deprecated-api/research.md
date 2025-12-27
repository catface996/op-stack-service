# Research: 移除废弃的API接口

**Feature**: 001-remove-deprecated-api
**Date**: 2025-12-27

## 研究摘要

本文档记录了移除废弃 API 接口前的依赖关系分析和清理策略研究。

## 1. 废弃接口依赖分析

### 1.1 ResourceController 中的废弃接口

**废弃接口列表**（共 6 个）:

| 接口 | 位置 | 依赖的服务 |
|------|------|-----------|
| `/resources/members/add` | L303-333 | SubgraphMemberApplicationService.addMembers() |
| `/resources/members/remove` | L340-360 | SubgraphMemberApplicationService.removeMembers() |
| `/resources/members/query` | L367-395 | SubgraphMemberApplicationService.listMembers(), countMembers() |
| `/resources/members-with-relations/query` | L402-425 | SubgraphMemberApplicationService.getMembersWithRelations() |
| `/resources/topology/query` | L432-454 | SubgraphMemberApplicationService.getSubgraphTopology() |
| `/resources/ancestors/query` | L461-479 | SubgraphMemberApplicationService.getAncestors() |

### 1.2 废弃接口依赖的 Request/Response 类

**专用于废弃接口的类**（需要移除）:

| 类型 | 类名 | 路径 |
|------|------|------|
| Request | AddMembersRequest | interface-http/.../request/subgraph/AddMembersRequest.java |
| Request | RemoveMembersRequest | interface-http/.../request/subgraph/RemoveMembersRequest.java |
| Request | QueryMembersRequest | interface-http/.../request/resource/QueryMembersRequest.java |
| Request | QueryMembersWithRelationsRequest | interface-http/.../request/resource/QueryMembersWithRelationsRequest.java |
| Request | QueryTopologyRequest | interface-http/.../request/resource/QueryTopologyRequest.java |
| Request | QueryAncestorsRequest | interface-http/.../request/resource/QueryAncestorsRequest.java |
| Response | SubgraphMemberListResponse | interface-http/.../response/subgraph/SubgraphMemberListResponse.java |
| Response | SubgraphMembersWithRelationsResponse | interface-http/.../response/subgraph/SubgraphMembersWithRelationsResponse.java |
| Response | TopologyGraphResponse | interface-http/.../response/subgraph/TopologyGraphResponse.java |
| Response | SubgraphAncestorsResponse | interface-http/.../response/subgraph/SubgraphAncestorsResponse.java |

### 1.3 SubgraphMemberApplicationService 分析

**Decision**: 保留 SubgraphMemberApplicationService

**Rationale**:
- TopologyController 仍然依赖此服务
- 只是 ResourceController 中的废弃接口调用了此服务，服务本身不是废弃的

**使用情况**:
- ResourceController（废弃接口，将移除）
- TopologyController（活跃接口，继续使用）
- TopologyApplicationServiceImpl（活跃服务，继续使用）

## 2. ErrorCodes 常量类分析

### 2.1 依赖情况

**Decision**: 安全移除 ErrorCodes.java

**Rationale**:
- 唯一引用位置是 GlobalExceptionHandler.java 的**注释**中（L40-44）
- 注释仅作为旧方式的说明示例，不影响实际代码运行
- 新代码已使用错误码枚举替代

**需要清理**:
- 删除 `common/src/main/java/com/catface996/aiops/common/constants/ErrorCodes.java`
- 更新 GlobalExceptionHandler.java 中的注释（可选）

## 3. 清理策略

### 3.1 清理顺序

1. **第一步**: 移除 ResourceController 中的废弃方法
   - 移除 6 个标记为 `@Deprecated` 的方法
   - 移除相关 import 语句
   - 更新类级别的 Javadoc 注释

2. **第二步**: 移除 interface-http 层的废弃 Request/Response 类
   - 移除 `request/subgraph/` 目录下的类
   - 移除 `request/resource/` 目录下仅被废弃接口使用的类
   - 移除 `response/subgraph/` 目录下的类

3. **第三步**: 移除 ErrorCodes 常量类
   - 删除 ErrorCodes.java 文件

4. **第四步**: 验证和清理
   - 执行编译确保无错误
   - 运行测试确保无回归
   - 检查是否有测试用例需要移除

### 3.2 保留的代码

以下代码**不应移除**（被活跃代码使用）:

| 类型 | 说明 |
|------|------|
| SubgraphMemberApplicationService | 被 TopologyController 使用 |
| SubgraphMemberApplicationServiceImpl | 服务实现 |
| application/dto/subgraph/* | DTO 类，被多个服务使用 |
| TopologyApplicationService | 拓扑图服务 |

### 3.3 潜在风险

| 风险 | 缓解措施 |
|------|---------|
| 编译错误 | 按依赖顺序移除，先移除 Controller 方法再移除类 |
| 测试失败 | 同步移除相关测试用例 |
| 文档过时 | 更新 Swagger 注解和 Javadoc |

## 4. 替代方案对比

| 原废弃接口 | 新接口 | 功能对等 |
|-----------|--------|---------|
| /resources/members/add | /topologies/members/add | ✅ 完全对等 |
| /resources/members/remove | /topologies/members/remove | ✅ 完全对等 |
| /resources/members/query | /topologies/members/query | ✅ 完全对等 |
| /resources/members-with-relations/query | 无直接替代 | ⚠️ 功能已合并到其他接口 |
| /resources/topology/query | /topologies/graph/query | ✅ 完全对等 |
| /resources/ancestors/query | 无直接替代 | ⚠️ 功能已合并到其他接口 |

## 5. 结论

本次清理任务风险可控，主要工作是代码删除而非修改。关键要点：

1. SubgraphMemberApplicationService 服务层保留，只移除 Controller 层的废弃接口
2. ErrorCodes 可安全删除
3. 需要同步移除专用的 Request/Response 类
4. 替代接口已在 TopologyController 中实现，功能完整
