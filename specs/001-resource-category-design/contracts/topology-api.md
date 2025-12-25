# API Contract: 拓扑图管理接口

**Feature**: 001-resource-category-design
**Date**: 2025-12-25
**Base Path**: `/api/v1/topologies`

## 1. 创建拓扑图

**POST** `/api/v1/topologies/create`

### Request

```json
{
  "operatorId": 1,
  "name": "电商平台拓扑图",
  "description": "展示电商平台核心组件的关系"
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| name | String | 是 | 1-100字符 | 拓扑图名称 |
| description | String | 否 | 最长500字符 | 拓扑图描述 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "name": "电商平台拓扑图",
    "description": "展示电商平台核心组件的关系",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "memberCount": 0,
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  }
}
```

**400 Bad Request** - 参数校验失败

```json
{
  "code": 400001,
  "message": "拓扑图名称不能为空",
  "data": null
}
```

---

## 2. 查询拓扑图列表

**POST** `/api/v1/topologies/query`

### Request

```json
{
  "operatorId": 1,
  "name": "电商",
  "status": "RUNNING",
  "pageNum": 1,
  "pageSize": 10
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| name | String | 否 | - | 名称模糊查询 |
| status | String | 否 | RUNNING/STOPPED/MAINTENANCE/OFFLINE | 状态筛选 |
| pageNum | Integer | 否 | 默认1 | 页码 |
| pageSize | Integer | 否 | 默认10，最大100 | 每页条数 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "pages": 3,
    "current": 1,
    "records": [
      {
        "id": 101,
        "name": "电商平台拓扑图",
        "description": "展示电商平台核心组件的关系",
        "status": "RUNNING",
        "statusDisplay": "运行中",
        "memberCount": 15,
        "createdBy": 1,
        "createdAt": "2025-12-25T10:30:00",
        "updatedAt": "2025-12-25T10:30:00"
      }
    ]
  }
}
```

---

## 3. 获取拓扑图详情

**POST** `/api/v1/topologies/get`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "name": "电商平台拓扑图",
    "description": "展示电商平台核心组件的关系",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "attributes": "{}",
    "memberCount": 15,
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T10:30:00"
  }
}
```

**404 Not Found** - 拓扑图不存在

```json
{
  "code": 404001,
  "message": "拓扑图不存在",
  "data": null
}
```

---

## 4. 更新拓扑图

**POST** `/api/v1/topologies/update`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101,
  "name": "电商平台核心拓扑图",
  "description": "更新后的描述",
  "version": 1
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |
| name | String | 否 | 1-100字符 | 新名称 |
| description | String | 否 | 最长500字符 | 新描述 |
| version | Integer | 是 | - | 乐观锁版本号 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "name": "电商平台核心拓扑图",
    "description": "更新后的描述",
    "status": "RUNNING",
    "statusDisplay": "运行中",
    "memberCount": 15,
    "createdBy": 1,
    "createdAt": "2025-12-25T10:30:00",
    "updatedAt": "2025-12-25T11:00:00"
  }
}
```

**409 Conflict** - 版本冲突

```json
{
  "code": 409001,
  "message": "数据已被修改，请刷新后重试",
  "data": null
}
```

---

## 5. 删除拓扑图

**POST** `/api/v1/topologies/delete`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务规则**：删除拓扑图时，自动解除与所有成员资源的关联关系，但不删除成员资源本身。

---

## 6. 添加拓扑图成员

**POST** `/api/v1/topologies/members/add`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101,
  "memberIds": [201, 202, 203]
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |
| memberIds | List<Long> | 是 | 非空 | 成员资源ID列表 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "addedCount": 3,
    "skippedCount": 0
  }
}
```

**400 Bad Request** - 循环引用

```json
{
  "code": 400020,
  "message": "不能添加循环引用的成员",
  "data": null
}
```

---

## 7. 移除拓扑图成员

**POST** `/api/v1/topologies/members/remove`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101,
  "memberIds": [201, 202]
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |
| memberIds | List<Long> | 是 | 非空 | 要移除的成员ID列表 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "removedCount": 2
  }
}
```

---

## 8. 查询拓扑图成员

**POST** `/api/v1/topologies/members/query`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101,
  "resourceTypeId": 1,
  "pageNum": 1,
  "pageSize": 20
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |
| resourceTypeId | Long | 否 | - | 按资源类型筛选 |
| pageNum | Integer | 否 | 默认1 | 页码 |
| pageSize | Integer | 否 | 默认20 | 每页条数 |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "pages": 1,
    "current": 1,
    "records": [
      {
        "id": 201,
        "name": "订单服务",
        "description": "处理订单逻辑",
        "resourceTypeId": 2,
        "resourceTypeName": "应用",
        "resourceTypeCode": "APPLICATION",
        "status": "RUNNING",
        "statusDisplay": "运行中",
        "createdAt": "2025-12-20T09:00:00"
      }
    ]
  }
}
```

---

## 9. 获取拓扑图数据

**POST** `/api/v1/topologies/graph/query`

### Request

```json
{
  "operatorId": 1,
  "topologyId": 101,
  "depth": 2
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| operatorId | Long | 是 | > 0 | 操作人ID |
| topologyId | Long | 是 | > 0 | 拓扑图ID |
| depth | Integer | 否 | 默认1，最大5 | 展开深度（用于嵌套子图） |

### Response

**200 OK**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "topology": {
      "id": 101,
      "name": "电商平台拓扑图"
    },
    "nodes": [
      {
        "id": 201,
        "name": "订单服务",
        "resourceTypeCode": "APPLICATION",
        "status": "RUNNING"
      },
      {
        "id": 202,
        "name": "订单数据库",
        "resourceTypeCode": "DATABASE",
        "status": "RUNNING"
      }
    ],
    "edges": [
      {
        "sourceId": 201,
        "targetId": 202,
        "relationshipType": "DEPENDS_ON"
      }
    ]
  }
}
```

---

## 错误码汇总

| 错误码 | 说明 |
|--------|------|
| 400001 | 拓扑图名称不能为空 |
| 400002 | 拓扑图名称过长 |
| 400020 | 不能添加循环引用的成员 |
| 404001 | 拓扑图不存在 |
| 409001 | 版本冲突 |
