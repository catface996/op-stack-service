# API Contract: 资源节点管理接口

**Feature**: 001-resource-category-design
**Date**: 2025-12-25
**Base Path**: `/api/v1/resources`

## 概述

资源节点接口在现有 `/api/v1/resources` 基础上进行修改，**排除 SUBGRAPH 类型**。所有资源节点操作（创建、查询、更新、删除）都不涉及拓扑图类型。

## 1. 创建资源节点

**POST** `/api/v1/resources/create`

### Request

```json
{
  "operatorId": 1,
  "name": "订单服务",
  "description": "处理订单业务逻辑",
  "resourceTypeId": 2,
  "attributes": "{\"version\": \"1.0.0\", \"port\": 8080}"
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| name | String | 是 | 1-100字符 | 资源名称 |
| description | String | 否 | 最长500字符 | 资源描述 |
| resourceTypeId | Long | 是 | **不能是 SUBGRAPH** | 资源类型ID |
| attributes | String | 否 | 有效JSON | 扩展属性 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 201,
    "name": "订单服务",
    "description": "处理订单业务逻辑",
    "resourceTypeId": 2,
    "resourceTypeName": "应用",
    "resourceTypeCode": "APPLICATION",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "attributes": "{\"version\": \"1.0.0\", \"port\": 8080}",
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  }
}
```

**400 Bad Request** - 类型不允许

```json
{
  "code": 400010,
  "message": "资源节点类型不能是SUBGRAPH，请使用拓扑图接口创建",
  "data": null
}
```

---

## 2. 查询资源节点列表

**POST** `/api/v1/resources/query`

### Request

```json
{
  "operatorId": 1,
  "name": "订单",
  "resourceTypeId": 2,
  "status": "RUNNING",
  "pageNum": 1,
  "pageSize": 10
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| name | String | 否 | - | 名称模糊查询 |
| resourceTypeId | Long | 否 | **不能是 SUBGRAPH** | 资源类型筛选 |
| resourceTypeCodes | List<String> | 否 | 多类型筛选 | 资源类型代码列表 |
| status | String | 否 | RUNNING/STOPPED/MAINTENANCE/OFFLINE | 状态筛选 |
| pageNum | Integer | 否 | 默认1 | 页码 |
| pageSize | Integer | 否 | 默认10，最大100 | 每页条数 |

**注意**：查询结果**自动排除 SUBGRAPH 类型**，无需额外指定。

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pages": 10,
    "current": 1,
    "records": [
      {
        "id": 201,
        "name": "订单服务",
        "description": "处理订单业务逻辑",
        "resourceTypeId": 2,
        "resourceTypeName": "应用",
        "resourceTypeCode": "APPLICATION",
        "status": "RUNNING",
        "statusDisplay": "运行中",
        "createdBy": 1,
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      }
    ]
  }
}
```

---

## 3. 获取资源节点详情

**POST** `/api/v1/resources/get`

### Request

```json
{
  "operatorId": 1,
  "resourceId": 201
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| resourceId | Long | 是 | > 0 | 资源ID |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 201,
    "name": "订单服务",
    "description": "处理订单业务逻辑",
    "resourceTypeId": 2,
    "resourceTypeName": "应用",
    "resourceTypeCode": "APPLICATION",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "attributes": "{\"version\": \"1.0.0\", \"port\": 8080}",
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  }
}
```

**404 Not Found** - 资源不存在或为拓扑图类型

```json
{
  "code": 404002,
  "message": "资源节点不存在",
  "data": null
}
```

---

## 4. 更新资源节点

**POST** `/api/v1/resources/update`

### Request

```json
{
  "operatorId": 1,
  "resourceId": 201,
  "name": "订单服务V2",
  "description": "升级后的订单服务",
  "attributes": "{\"version\": \"2.0.0\", \"port\": 8080}",
  "version": 1
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| resourceId | Long | 是 | > 0 | 资源ID |
| name | String | 否 | 1-100字符 | 新名称 |
| description | String | 否 | 最长500字符 | 新描述 |
| attributes | String | 否 | 有效JSON | 新扩展属性 |
| version | Integer | 是 | - | 乐观锁版本号 |

**注意**：不允许修改 `resourceTypeId`，且不能操作 SUBGRAPH 类型资源。

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 201,
    "name": "订单服务V2",
    "description": "升级后的订单服务",
    "resourceTypeId": 2,
    "resourceTypeName": "应用",
    "resourceTypeCode": "APPLICATION",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "attributes": "{\"version\": \"2.0.0\", \"port\": 8080}",
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T11:00:00"
  }
}
```

---

## 5. 删除资源节点

**POST** `/api/v1/resources/delete`

### Request

```json
{
  "operatorId": 1,
  "resourceId": 201
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| resourceId | Long | 是 | > 0 | 资源ID |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务规则**：
- 删除资源节点时，自动解除与所有拓扑图的关联关系
- 不允许通过此接口删除 SUBGRAPH 类型资源

---

## 6. 更新资源状态

**POST** `/api/v1/resources/status/update`

### Request

```json
{
  "operatorId": 1,
  "resourceId": 201,
  "status": "STOPPED",
  "version": 1
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| resourceId | Long | 是 | > 0 | 资源ID |
| status | String | 是 | RUNNING/STOPPED/MAINTENANCE/OFFLINE | 目标状态 |
| version | Integer | 是 | - | 乐观锁版本号 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 201,
    "status": "STOPPED",
    "statusDisplay": "已停止"
  }
}
```

---

## 可用资源类型

以下资源类型可用于创建资源节点：

| ID | Code | Name | 说明 |
|----|------|------|------|
| 1 | SERVER | 服务器 | 物理或虚拟服务器 |
| 2 | APPLICATION | 应用 | 应用程序或微服务 |
| 3 | DATABASE | 数据库 | 数据库实例 |
| 4 | API | API接口 | REST/RPC接口 |
| 5 | MIDDLEWARE | 中间件 | 消息队列、缓存等 |
| 6 | REPORT | 报表 | 数据报表 |

**注意**：`SUBGRAPH` (ID=7) **不可用于**资源节点接口。

---

## 错误码汇总

| 错误码 | 说明 |
|--------|------|
| 400010 | 资源节点类型不能是SUBGRAPH |
| 400011 | 资源名称不能为空 |
| 400012 | 资源名称过长 |
| 400013 | 扩展属性JSON格式无效 |
| 404002 | 资源节点不存在 |
| 409002 | 版本冲突 |

---

## 与拓扑图接口的区别

| 功能 | 资源节点接口 (`/resources`) | 拓扑图接口 (`/topologies`) |
|------|----------------------------|---------------------------|
| 创建 | 只能创建非SUBGRAPH类型 | 自动创建SUBGRAPH类型 |
| 查询 | 自动排除SUBGRAPH类型 | 只返回SUBGRAPH类型 |
| 成员管理 | 无 | 支持添加/移除成员 |
| 图数据 | 无 | 支持获取拓扑结构 |
