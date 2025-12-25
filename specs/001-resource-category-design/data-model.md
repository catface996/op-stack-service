# Data Model: 资源分类体系设计

**Feature**: 001-resource-category-design
**Date**: 2025-12-25

## 1. 实体关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Resource                                 │
│  (所有资源的基础实体，通过 resource_type 区分类别)               │
├─────────────────────────────────────────────────────────────────┤
│  id: Long (PK)                                                  │
│  name: String                                                   │
│  description: String                                            │
│  resource_type_id: Long (FK → resource_type.id)                │
│  status: Enum (RUNNING, STOPPED, MAINTENANCE, OFFLINE)         │
│  attributes: JSON                                               │
│  created_by: Long                                               │
│  created_at: Timestamp                                          │
│  updated_at: Timestamp                                          │
│  version: Integer (乐观锁)                                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           │ resource_type_id
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ResourceType                                │
│  (资源类型定义，区分拓扑图和资源节点)                           │
├─────────────────────────────────────────────────────────────────┤
│  id: Long (PK)                                                  │
│  code: String (UNIQUE) ← 关键字段                               │
│  name: String                                                   │
│  description: String                                            │
│  icon: String                                                   │
│  is_system: Boolean                                             │
│  attribute_schema: JSON                                         │
└─────────────────────────────────────────────────────────────────┘

资源类型分类:
┌────────────────────────────────────────────────────────────────┐
│  code = 'SUBGRAPH'  →  Topology (拓扑图)                       │
│  code ≠ 'SUBGRAPH'  →  Resource Node (资源节点)                │
│    - SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT    │
└────────────────────────────────────────────────────────────────┘
```

## 2. 拓扑图成员关系

```
┌───────────────────────┐         ┌───────────────────────┐
│   Resource (拓扑图)    │         │   Resource (成员)      │
│   type = SUBGRAPH     │◄────────│   type ≠ SUBGRAPH     │
└───────────────────────┘         └───────────────────────┘
            │                               ▲
            │         subgraph_member       │
            └───────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     SubgraphMember                               │
│  (拓扑图与成员资源的关联关系)                                   │
├─────────────────────────────────────────────────────────────────┤
│  id: Long (PK)                                                  │
│  subgraph_id: Long (FK → resource.id, 必须是 SUBGRAPH 类型)    │
│  member_id: Long (FK → resource.id, 可以是任意类型含 SUBGRAPH) │
│  created_at: Timestamp                                          │
│  created_by: Long                                               │
└─────────────────────────────────────────────────────────────────┘

约束:
- subgraph_id 对应的资源必须是 SUBGRAPH 类型
- member_id 可以是任意类型（支持嵌套子图）
- (subgraph_id, member_id) 唯一约束
```

## 3. 领域模型分类

### 3.1 拓扑图 (Topology)

| 属性 | 类型 | 说明 |
|------|------|------|
| id | Long | 唯一标识 |
| name | String | 拓扑图名称 |
| description | String | 描述 |
| status | ResourceStatus | 状态 |
| attributes | Map<String, Object> | 扩展属性 |
| members | List<Resource> | 成员资源列表 |
| createdBy | Long | 创建者ID |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**业务规则**:
- 拓扑图的 resource_type.code 固定为 'SUBGRAPH'
- 拓扑图可以包含任意类型的资源作为成员
- 拓扑图可以嵌套（成员可以是另一个拓扑图）

### 3.2 资源节点 (ResourceNode)

| 属性 | 类型 | 说明 |
|------|------|------|
| id | Long | 唯一标识 |
| name | String | 资源名称 |
| description | String | 描述 |
| resourceType | ResourceType | 资源类型 (非 SUBGRAPH) |
| status | ResourceStatus | 状态 |
| attributes | Map<String, Object> | 扩展属性 |
| createdBy | Long | 创建者ID |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**业务规则**:
- 资源节点的 resource_type.code 不能为 'SUBGRAPH'
- 资源节点可以属于多个拓扑图
- 资源节点可以独立存在（不属于任何拓扑图）

## 4. DTO 设计

### 4.1 拓扑图 DTO

```
TopologyDTO
├── id: Long
├── name: String
├── description: String
├── status: String
├── statusDisplay: String
├── attributes: String (JSON)
├── memberCount: Integer (成员数量)
├── createdBy: Long
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime

CreateTopologyRequest
├── operatorId: Long (必填)
├── name: String (必填, max 100)
└── description: String (可选, max 500)

UpdateTopologyRequest
├── operatorId: Long (必填)
├── topologyId: Long (必填)
├── name: String (可选)
├── description: String (可选)
└── version: Integer (必填, 乐观锁)
```

### 4.2 资源节点 DTO

```
ResourceNodeDTO (复用现有 ResourceDTO)
├── id: Long
├── name: String
├── description: String
├── resourceTypeId: Long
├── resourceTypeName: String
├── resourceTypeCode: String
├── status: String
├── statusDisplay: String
├── attributes: String (JSON)
├── createdBy: Long
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime

CreateResourceNodeRequest (修改现有 CreateResourceRequest)
├── operatorId: Long (必填)
├── name: String (必填)
├── description: String (可选)
├── resourceTypeId: Long (必填, 不能是 SUBGRAPH)
└── attributes: String (可选, JSON)
```

## 5. 验证规则

| 规则 | 应用场景 | 错误码 |
|------|----------|--------|
| 拓扑图名称不能为空 | 创建/更新拓扑图 | 400001 |
| 拓扑图名称最长100字符 | 创建/更新拓扑图 | 400002 |
| 资源节点类型不能是SUBGRAPH | 创建资源节点 | 400010 |
| 成员资源必须存在 | 添加拓扑图成员 | 404001 |
| 不能添加循环引用的成员 | 添加嵌套子图 | 400020 |

## 6. 状态转换

资源状态（拓扑图和资源节点共用）:

```
           ┌─────────┐
    创建 → │ RUNNING │ ← 启动
           └────┬────┘
                │ 停止/维护
                ▼
           ┌─────────┐
           │ STOPPED │
           └────┬────┘
                │ 下线
                ▼
           ┌─────────┐
           │ OFFLINE │
           └─────────┘
                │ 删除
                ▼
           [已删除]
```
