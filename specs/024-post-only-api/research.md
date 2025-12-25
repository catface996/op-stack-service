# Research: POST-Only API 重构

**Feature**: 024-post-only-api
**Date**: 2025-12-22

## 1. 现有接口清单分析

### 1.1 需要重构的接口（按 Controller 分组）

#### ResourceController (`/api/v1`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | /resources | /resources/query | Query params → Body |
| 2 | GET | /resources/{id} | /resources/get | Path param → Body |
| 3 | PUT | /resources/{id} | /resources/update | Path + Body → Body |
| 4 | DELETE | /resources/{id} | /resources/delete | Path + Body → Body |
| 5 | PATCH | /resources/{id}/status | /resources/update-status | Path + Body → Body |
| 6 | GET | /resources/{id}/audit-logs | /resources/audit-logs/query | Path + Query → Body |
| 7 | GET | /resource-types | /resource-types/query | None → Body (空或分页) |
| 8 | DELETE | /resources/{id}/members | /resources/members/remove | Path + Body → Body |
| 9 | GET | /resources/{id}/members | /resources/members/query | Path + Query → Body |
| 10 | GET | /resources/{id}/members-with-relations | /resources/members-with-relations/query | Path + Query → Body |
| 11 | GET | /resources/{id}/topology | /resources/topology/query | Path + Query → Body |
| 12 | GET | /resources/{id}/ancestors | /resources/ancestors/query | Path → Body |

#### RelationshipController (`/api/v1/relationships`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | / | /query | Query params → Body |
| 2 | GET | /resource/{resourceId} | /resource/query | Path → Body |
| 3 | GET | /{relationshipId} | /get | Path → Body |
| 4 | PUT | /{relationshipId} | /update | Path + Body → Body |
| 5 | DELETE | /{relationshipId} | /delete | Path → Body |
| 6 | GET | /resource/{resourceId}/cycle-detection | /resource/cycle-detection | Path → Body |
| 7 | GET | /resource/{resourceId}/traverse | /resource/traverse | Path + Query → Body |

#### SessionController (`/api/v1/sessions`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | /validate | /validate | Header (Token) → Body |
| 2 | GET | / | /query | Query params → Body |
| 3 | DELETE | /{sessionId} | /terminate | Path → Body |

#### SessionCompatController (`/api/v1/session`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | /validate | /validate | Header (Token) → Body |

#### LlmServiceController (`/api/v1/llm-services`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | / | /query | Query params → Body |
| 2 | GET | /{id} | /get | Path → Body |
| 3 | PUT | /{id} | /update | Path + Body → Body |
| 4 | DELETE | /{id} | /delete | Path → Body |
| 5 | PUT | /{id}/status | /update-status | Path + Body → Body |
| 6 | PUT | /{id}/default | /set-default | Path → Body |

#### AdminController (`/api/v1/admin`)
| # | 原方法 | 原路径 | 新路径 | 参数来源 |
|---|--------|--------|--------|----------|
| 1 | GET | /accounts, /users | /accounts/query | Query params → Body |

### 1.2 保持不变的接口

| Controller | 路径 | 原因 |
|------------|------|------|
| HealthController | GET /health | 监控系统兼容性 |
| AuthController | POST /api/v1/auth/* | 已经是 POST |
| ExceptionTestController | GET /test/* | 仅测试用途，不影响生产 |

**统计**: 需重构 **31 个接口**（原估计 34 个，排除测试接口后）

## 2. 请求体命名约定

### Decision: 统一命名模式

采用 `{动作}{实体}Request` 的命名模式：

| 动作类型 | 命名模式 | 示例 |
|----------|----------|------|
| 查询列表 | Query{Entity}Request | QueryResourcesRequest |
| 获取单个 | Get{Entity}Request | GetResourceRequest |
| 创建 | Create{Entity}Request | CreateResourceRequest (已有) |
| 更新 | Update{Entity}Request | UpdateResourceRequest (已有) |
| 删除 | Delete{Entity}Request | DeleteResourceRequest (已有) |
| 状态变更 | Update{Entity}StatusRequest | UpdateResourceStatusRequest (已有) |

### Rationale
- 与现有命名风格一致
- 清晰表达请求意图
- 便于代码生成和维护

### Alternatives Considered
1. `{Entity}{Action}Params` - 不符合现有风格
2. `{Entity}Dto` - 与响应 DTO 混淆

## 3. 请求体结构设计

### 3.1 ID 传递方式

**Decision**: 路径参数中的 ID 移入请求体 `id` 字段

```java
// 原: GET /resources/{id}
// 新: POST /resources/get
public class GetResourceRequest {
    @NotNull
    private Long id;
    // 网关注入字段（可选）
    private Long tenantId;
    private String traceId;
}
```

### 3.2 分页参数标准化

**Decision**: 统一使用 `page` + `size` 模式

```java
public abstract class PageableRequest {
    @Min(1)
    private Integer page = 1;

    @Min(1) @Max(100)
    private Integer size = 20;

    // 网关注入字段
    private Long tenantId;
    private String traceId;
}
```

### 3.3 现有请求体复用

以下现有请求体可直接复用，仅需添加 `id` 字段（如适用）：

| 现有请求体 | 用途 | 修改 |
|-----------|------|------|
| CreateResourceRequest | 创建资源 | 无需修改 |
| UpdateResourceRequest | 更新资源 | 添加 id 字段 |
| DeleteResourceRequest | 删除资源 | 添加 id 字段 |
| UpdateResourceStatusRequest | 更新状态 | 添加 id 字段 |
| ListResourcesRequest | 查询列表 | 已符合要求 |
| AddMembersRequest | 添加成员 | 添加 resourceId 字段 |
| RemoveMembersRequest | 移除成员 | 添加 resourceId 字段 |

## 4. 网关参数注入支持

### Decision: 根级平铺字段

根据澄清结果，网关注入的参数直接在请求体根级平铺：

```json
{
  "id": 123,
  "name": "my-resource",
  "tenantId": "tenant-001",
  "traceId": "trace-abc-123",
  "userId": "user-456"
}
```

### 实现方式

1. **定义基类或接口**（可选）：
```java
public interface GatewayInjectable {
    void setTenantId(Long tenantId);
    void setTraceId(String traceId);
}
```

2. **Jackson 忽略未知字段**（已配置）：
   - 现有配置 `spring.jackson.deserialization.fail-on-unknown-properties=false`
   - 网关注入的字段即使未在 DTO 中定义也不会报错

3. **按需提取**：
   - 需要租户隔离的服务可从请求体提取 `tenantId`
   - 日志追踪可从请求体提取 `traceId`

## 5. Swagger/OpenAPI 更新策略

### Decision: 自动更新 + 手动补充

- SpringDoc OpenAPI 会自动生成新接口文档
- 需要补充的内容：
  - API 分组说明
  - 废弃接口标注（如有兼容期）
  - 示例请求体

### 实现
- 使用 `@Operation(deprecated = true)` 标注旧接口（如保留兼容期）
- 使用 `@Schema(example = "...")` 提供示例值

## 6. 实施顺序建议

### Decision: 按依赖关系排序

1. **第一批（基础）**: ResourceController - 核心业务接口
2. **第二批（关联）**: RelationshipController - 依赖 Resource
3. **第三批（会话）**: SessionController, SessionCompatController - 独立模块
4. **第四批（配置）**: LlmServiceController - 独立模块
5. **第五批（管理）**: AdminController - 管理功能

### Rationale
- 按模块独立性分批
- 核心接口优先，便于快速验证
- 会话和管理接口可并行

## 7. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 客户端破坏性变更 | 高 | 提前通知，提供迁移文档 |
| 测试覆盖不足 | 中 | 先补充集成测试再重构 |
| 遗漏接口 | 低 | 代码扫描确认完整性 |
| 响应格式变化 | 高 | 严格保持 Result 包装不变 |
