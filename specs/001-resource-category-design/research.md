# Research: 资源分类体系设计

**Feature**: 001-resource-category-design
**Date**: 2025-12-25

## 1. 现有系统分析

### 1.1 当前资源类型定义

**Decision**: 复用现有 `resource_type` 表，通过 `code` 字段区分资源类别

**Rationale**:
- 现有表结构已包含 SUBGRAPH 类型（id=7, code='SUBGRAPH'）
- 其他类型（SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT）为资源节点
- 无需修改数据库，仅在代码层面区分

**Alternatives considered**:
- 方案A：新增 `category` 字段到 `resource_type` 表 → 需要数据迁移，增加复杂度
- 方案B：新增 `resource_category` 表 → 过度设计，增加join查询
- 方案C（选中）：代码层面通过 `code='SUBGRAPH'` 判断 → 简单、零迁移

### 1.2 现有接口分析

**现有资源管理接口**:
| 接口 | 路径 | 说明 |
|------|------|------|
| 创建资源 | POST /api/v1/resources/create | 支持所有类型，包括 SUBGRAPH |
| 查询资源列表 | POST /api/v1/resources/query | 返回所有类型混合 |
| 获取资源详情 | POST /api/v1/resources/get | 支持所有类型 |
| 更新资源 | POST /api/v1/resources/update | 支持所有类型 |
| 删除资源 | POST /api/v1/resources/delete | 支持所有类型 |

**子图成员管理接口**（仅适用于 SUBGRAPH）:
| 接口 | 路径 | 说明 |
|------|------|------|
| 添加成员 | POST /api/v1/resources/members/add | 仅 SUBGRAPH |
| 移除成员 | POST /api/v1/resources/members/remove | 仅 SUBGRAPH |
| 查询成员 | POST /api/v1/resources/members/query | 仅 SUBGRAPH |
| 获取拓扑 | POST /api/v1/resources/topology/query | 仅 SUBGRAPH |

## 2. 设计决策

### 2.1 API 路径设计

**Decision**: 新增 `/api/v1/topologies/` 路径用于拓扑图管理

**Rationale**:
- 语义清晰，用户通过路径即可理解操作对象
- 与现有 `/api/v1/resources/` 接口解耦
- 便于后续独立演进

**New API Structure**:
```
/api/v1/topologies/
├── create          # 创建拓扑图
├── query           # 查询拓扑图列表
├── get             # 获取拓扑图详情
├── update          # 更新拓扑图
├── delete          # 删除拓扑图
├── members/add     # 添加成员（从 resources 迁移）
├── members/remove  # 移除成员（从 resources 迁移）
├── members/query   # 查询成员（从 resources 迁移）
└── graph/query     # 获取拓扑图数据（从 resources/topology 迁移）
```

### 2.2 资源节点接口修改

**Decision**: 修改现有 `/api/v1/resources/` 接口，排除 SUBGRAPH 类型

**Changes**:
1. `POST /resources/create`: 校验 `resourceTypeId` 不为 SUBGRAPH (id=7)
2. `POST /resources/query`: 查询条件自动排除 SUBGRAPH 类型
3. 移除 `/resources/members/*` 和 `/resources/topology/*` 相关接口（迁移到 topologies）

### 2.3 SUBGRAPH 类型 ID 处理

**Decision**: 通过常量定义 SUBGRAPH 类型的 code，避免硬编码 ID

**Rationale**:
- `id` 可能因环境不同而变化
- `code='SUBGRAPH'` 是稳定的业务标识

**Implementation**:
```java
public class ResourceTypeConstants {
    public static final String SUBGRAPH_CODE = "SUBGRAPH";
}
```

## 3. 向后兼容性

### 3.1 现有接口保留策略

**Decision**: 保留现有接口一段时间，标记为 Deprecated

**Rationale**:
- 允许客户端平滑迁移
- 避免突然破坏性变更

**Deprecation Plan**:
1. 阶段一：新增拓扑图接口，现有接口继续可用
2. 阶段二：现有接口标记 @Deprecated，返回警告头
3. 阶段三：移除旧接口（根据实际迁移进度决定）

### 3.2 数据兼容性

**Decision**: 现有数据无需迁移

**Rationale**:
- 拓扑图通过 `resource_type.code='SUBGRAPH'` 自动识别
- 资源节点为非 SUBGRAPH 的所有类型
- `subgraph_member` 表关联关系保持不变

## 4. 测试策略

### 4.1 单元测试

- `TopologyApplicationServiceTest`: 拓扑图服务测试
- `ResourceApplicationServiceTest`: 资源节点服务测试（验证排除 SUBGRAPH）

### 4.2 集成测试

- `TopologyControllerIntegrationTest`: 拓扑图接口集成测试
- `ResourceControllerIntegrationTest`: 资源节点接口集成测试

### 4.3 契约测试

- 验证新接口响应格式符合 OpenAPI 规范
- 验证错误码和错误消息一致性
