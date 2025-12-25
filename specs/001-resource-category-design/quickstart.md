# Quick Start: 资源分类体系设计

**Feature**: 001-resource-category-design
**Date**: 2025-12-25

## 概述

本功能将资源分为两大类：**拓扑图（Topology）** 和 **资源节点（Resource Node）**，通过独立的 API 接口分别管理。

## 快速理解

### 资源分类

```
Resource（资源）
├── Topology（拓扑图）
│   └── resource_type.code = 'SUBGRAPH'
│   └── 用途：组织和展示其他资源的拓扑关系
│
└── Resource Node（资源节点）
    └── resource_type.code ≠ 'SUBGRAPH'
    └── 类型：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT
    └── 用途：代表实际的IT基础设施资源
```

### API 路径

| 操作类型 | 拓扑图 | 资源节点 |
|----------|--------|----------|
| 创建 | POST `/api/v1/topologies/create` | POST `/api/v1/resources/create` |
| 查询列表 | POST `/api/v1/topologies/query` | POST `/api/v1/resources/query` |
| 获取详情 | POST `/api/v1/topologies/get` | POST `/api/v1/resources/get` |
| 更新 | POST `/api/v1/topologies/update` | POST `/api/v1/resources/update` |
| 删除 | POST `/api/v1/topologies/delete` | POST `/api/v1/resources/delete` |

## 使用示例

### 1. 创建拓扑图

```bash
curl -X POST http://localhost:8080/api/v1/topologies/create \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "name": "电商平台拓扑图",
    "description": "展示电商平台核心组件的关系"
  }'
```

### 2. 创建资源节点

```bash
curl -X POST http://localhost:8080/api/v1/resources/create \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "name": "订单服务",
    "description": "处理订单业务逻辑",
    "resourceTypeId": 2
  }'
```

### 3. 添加成员到拓扑图

```bash
curl -X POST http://localhost:8080/api/v1/topologies/members/add \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "topologyId": 101,
    "memberIds": [201, 202, 203]
  }'
```

### 4. 查询拓扑图数据（包含节点和边）

```bash
curl -X POST http://localhost:8080/api/v1/topologies/graph/query \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "topologyId": 101,
    "depth": 2
  }'
```

## 关键变更

### 接口变更

| 变更类型 | 接口 | 说明 |
|----------|------|------|
| 新增 | `/api/v1/topologies/*` | 全新的拓扑图管理接口 |
| 修改 | `/api/v1/resources/create` | 禁止创建 SUBGRAPH 类型 |
| 修改 | `/api/v1/resources/query` | 自动排除 SUBGRAPH 类型 |
| 迁移 | `/api/v1/resources/members/*` | 迁移到 `/api/v1/topologies/members/*` |
| 迁移 | `/api/v1/resources/topology/*` | 迁移到 `/api/v1/topologies/graph/*` |

### 向后兼容

- 现有数据无需迁移
- 旧接口暂时保留，标记为 `@Deprecated`
- 建议客户端尽快迁移到新接口

## 典型场景

### 场景1：管理员查看所有拓扑图

```bash
# 只返回拓扑图，不返回服务器、应用等资源节点
curl -X POST http://localhost:8080/api/v1/topologies/query \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "pageNum": 1,
    "pageSize": 20
  }'
```

### 场景2：管理员查看所有服务器

```bash
# 只返回 SERVER 类型的资源节点
curl -X POST http://localhost:8080/api/v1/resources/query \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "resourceTypeId": 1,
    "pageNum": 1,
    "pageSize": 20
  }'
```

### 场景3：构建拓扑图视图

```bash
# 1. 创建拓扑图
# 2. 创建资源节点
# 3. 将资源节点添加到拓扑图
# 4. 获取完整拓扑数据用于可视化
curl -X POST http://localhost:8080/api/v1/topologies/graph/query \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "topologyId": 101
  }'
```

## 常见问题

### Q: 如何判断一个资源是拓扑图还是资源节点？

A: 通过 `resource_type.code` 判断：
- `code = 'SUBGRAPH'` → 拓扑图
- `code ≠ 'SUBGRAPH'` → 资源节点

### Q: 拓扑图可以嵌套吗？

A: 可以。拓扑图的成员可以是另一个拓扑图，支持多层嵌套结构。

### Q: 删除拓扑图会删除其成员资源吗？

A: 不会。删除拓扑图只会解除与成员的关联关系，成员资源本身不受影响。

### Q: 一个资源节点可以属于多个拓扑图吗？

A: 可以。资源节点与拓扑图是多对多关系。

## 相关文档

- [API Contract: 拓扑图管理接口](./contracts/topology-api.md)
- [API Contract: 资源节点管理接口](./contracts/resource-node-api.md)
- [数据模型设计](./data-model.md)
- [技术研究](./research.md)
