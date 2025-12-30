# 资源节点 Layer 字段前端适配指南

## 1. 功能概述

为资源节点新增 `layer` 字段，用于标注资源所属的架构层级。该字段帮助用户从架构视角对资源进行分类和筛选。

### 1.1 Layer 枚举值

| 枚举值 | 英文显示名 | 中文显示名 | 层级顺序 | 说明 |
|--------|-----------|-----------|---------|------|
| `BUSINESS_SCENARIO` | Business Scenario | 业务场景 | 1 | 最上层，如：电商下单流程、用户注册流程 |
| `BUSINESS_FLOW` | Business Flow | 业务流程 | 2 | 业务流程，如：订单处理流、支付流程 |
| `BUSINESS_APPLICATION` | Business Application | 业务应用 | 3 | 微服务应用，如：user-service、order-service |
| `MIDDLEWARE` | Middleware | 中间件 | 4 | 中间件及数据库，如：Redis、MySQL、Kafka |
| `INFRASTRUCTURE` | Infrastructure | 基础设施 | 5 | 底层基础设施，如：服务器、K8s 节点 |

---

## 2. API 变更

### 2.1 创建节点 - POST `/api/service/v1/nodes/create`

**Request 新增字段：**

```json
{
  "operatorId": 1,
  "name": "order-service",
  "description": "订单服务",
  "nodeTypeId": 1,
  "layer": "BUSINESS_APPLICATION",  // [新增] 可选，架构层级
  "agentTeamId": null,
  "attributes": "{\"port\": 8080}"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| `layer` | string | 否 | 架构层级，可选值见 1.1 枚举表 |

---

### 2.2 查询节点列表 - POST `/api/service/v1/nodes/query`

**Request 新增筛选字段：**

```json
{
  "nodeTypeId": null,
  "status": null,
  "layer": "MIDDLEWARE",  // [新增] 可选，按架构层级筛选
  "keyword": null,
  "topologyId": null,
  "page": 1,
  "size": 10
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| `layer` | string | 否 | 按架构层级筛选，可选值见 1.1 枚举表 |

**Response 新增字段：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 21,
        "name": "redis-cluster-prod",
        "description": "Redis 集群",
        "nodeTypeId": 4,
        "nodeTypeName": "中间件",
        "nodeTypeCode": "MIDDLEWARE",
        "status": "RUNNING",
        "statusDisplay": "运行中",
        "layer": "MIDDLEWARE",           // [新增] 架构层级枚举值
        "agentTeamId": null,
        "attributes": "{}",
        "version": 1,
        "createdBy": 1,
        "createdAt": "2025-12-30T10:00:00",
        "updatedAt": "2025-12-30T10:00:00"
      }
    ],
    "page": 1,
    "size": 10,
    "total": 34
  }
}
```

---

### 2.3 获取节点详情 - POST `/api/service/v1/nodes/get`

**Response 新增字段（同 2.2）：**

- `layer`: 架构层级枚举值（前端根据枚举映射表转换显示名）

---

### 2.4 更新节点 - POST `/api/service/v1/nodes/update`

**Request 新增字段：**

```json
{
  "operatorId": 1,
  "id": 21,
  "name": "redis-cluster-prod",
  "description": "Redis 生产集群",
  "layer": "MIDDLEWARE",  // [新增] 可选，null 表示不修改
  "agentTeamId": null,
  "attributes": "{}",
  "version": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| `layer` | string | 否 | 架构层级，null 表示不修改，可选值见 1.1 枚举表 |

---

### 2.5 查询拓扑图数据 - POST `/api/service/v1/topologies/graph/query`

**Response 节点数据新增字段：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "topology": {
      "id": 1,
      "name": "电商平台拓扑图"
    },
    "nodes": [
      {
        "id": 21,
        "name": "redis-cluster-prod",
        "nodeTypeCode": "MIDDLEWARE",
        "status": "RUNNING",
        "layer": "MIDDLEWARE",    // [新增] 架构层级枚举值
        "positionX": 100,
        "positionY": 200
      }
    ],
    "edges": [...]
  }
}
```

---

## 3. 前端适配清单

### 3.1 数据类型定义

```typescript
// 架构层级枚举
export enum NodeLayer {
  BUSINESS_SCENARIO = 'BUSINESS_SCENARIO',
  BUSINESS_FLOW = 'BUSINESS_FLOW',
  BUSINESS_APPLICATION = 'BUSINESS_APPLICATION',
  MIDDLEWARE = 'MIDDLEWARE',
  INFRASTRUCTURE = 'INFRASTRUCTURE',
}

// 架构层级配置（用于下拉选择、颜色标签等）
export const NODE_LAYER_CONFIG: Record<NodeLayer, {
  label: string;
  labelEn: string;
  color: string;
  order: number;
}> = {
  [NodeLayer.BUSINESS_SCENARIO]: {
    label: '业务场景',
    labelEn: 'Business Scenario',
    color: '#722ed1',  // 紫色
    order: 1,
  },
  [NodeLayer.BUSINESS_FLOW]: {
    label: '业务流程',
    labelEn: 'Business Flow',
    color: '#1890ff',  // 蓝色
    order: 2,
  },
  [NodeLayer.BUSINESS_APPLICATION]: {
    label: '业务应用',
    labelEn: 'Business Application',
    color: '#52c41a',  // 绿色
    order: 3,
  },
  [NodeLayer.MIDDLEWARE]: {
    label: '中间件',
    labelEn: 'Middleware',
    color: '#faad14',  // 橙色
    order: 4,
  },
  [NodeLayer.INFRASTRUCTURE]: {
    label: '基础设施',
    labelEn: 'Infrastructure',
    color: '#8c8c8c',  // 灰色
    order: 5,
  },
};

// 节点 DTO 类型更新
export interface NodeDTO {
  id: number;
  name: string;
  description: string;
  nodeTypeId: number;
  nodeTypeName: string;
  nodeTypeCode: string;
  status: string;
  statusDisplay: string;
  layer: NodeLayer | null;       // [新增] 前端根据 NODE_LAYER_CONFIG 转换显示名
  agentTeamId: number | null;
  attributes: string;
  version: number;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}

// 工具函数：获取 Layer 显示名
export function getLayerLabel(layer: NodeLayer | null): string {
  if (!layer) return '未设置';
  return NODE_LAYER_CONFIG[layer]?.label ?? layer;
}

// 创建节点请求
export interface CreateNodeRequest {
  operatorId: number;
  name: string;
  description?: string;
  nodeTypeId: number;
  layer?: NodeLayer;  // [新增]
  agentTeamId?: number;
  attributes?: string;
}

// 查询节点请求
export interface QueryNodesRequest {
  nodeTypeId?: number;
  status?: string;
  layer?: NodeLayer;  // [新增]
  keyword?: string;
  topologyId?: number;
  page: number;
  size: number;
}

// 更新节点请求
export interface UpdateNodeRequest {
  operatorId: number;
  id: number;
  name?: string;
  description?: string;
  layer?: NodeLayer;  // [新增]
  agentTeamId?: number;
  attributes?: string;
  version: number;
}
```

---

### 3.2 UI 适配点

#### 3.2.1 节点列表页

1. **新增 Layer 筛选下拉框**
   - 位置：与 nodeTypeId、status 筛选并列
   - 选项：全部 + 5 个层级枚举值
   - 支持清空

2. **列表表格新增 Layer 列**
   - 显示内容：使用 `getLayerLabel(layer)` 转换为中文名
   - 建议使用 Tag 组件，不同层级不同颜色（从 `NODE_LAYER_CONFIG` 获取）
   - 列宽：约 100px
   - 支持排序

3. **列表表格示例**

| 名称 | 类型 | 架构层级 | 状态 | 操作 |
|-----|------|---------|-----|------|
| order-service | 应用 | <span style="background:#52c41a;color:white;padding:2px 8px;border-radius:4px">业务应用</span> | 运行中 | 编辑 删除 |
| redis-cluster | 中间件 | <span style="background:#faad14;color:white;padding:2px 8px;border-radius:4px">中间件</span> | 运行中 | 编辑 删除 |
| k8s-node-01 | 服务器 | <span style="background:#8c8c8c;color:white;padding:2px 8px;border-radius:4px">基础设施</span> | 运行中 | 编辑 删除 |

#### 3.2.2 创建/编辑节点弹窗

1. **新增 Layer 选择字段**
   - 组件：Select 下拉框
   - 位置：nodeTypeId 下方
   - 必填：否（可选）
   - Placeholder：请选择架构层级
   - 选项：5 个层级枚举值

2. **表单字段顺序建议**
   ```
   节点名称（必填）
   节点描述
   节点类型（必填）
   架构层级（可选）  // [新增]
   Agent Team
   扩展属性
   ```

#### 3.2.3 节点详情页

1. **新增 Layer 展示**
   - 使用 Tag 或描述列表展示
   - 使用 `getLayerLabel(layer)` 转换显示名

---

### 3.3 拓扑图适配（如有）

如果拓扑图中展示节点，建议：

1. **节点颜色按 Layer 区分**
   - 使用 `NODE_LAYER_CONFIG` 中定义的颜色

2. **图例说明**
   - 展示 5 个层级的颜色含义

3. **分层布局（可选）**
   - 按 `order` 字段从上到下排列节点
   - BUSINESS_SCENARIO 在最上层
   - INFRASTRUCTURE 在最下层

---

## 4. 兼容性说明

1. **向后兼容**
   - `layer` 字段可选，不传则为 `null`
   - 旧数据的 `layer` 可能为 `null`
   - 前端需处理 `null` 情况，可显示为 "未设置" 或不显示

2. **现有数据已设置**
   - 数据库中现有节点已根据 `nodeTypeCode` 自动设置了 Layer：
     - APPLICATION, API → BUSINESS_APPLICATION
     - MIDDLEWARE, DATABASE → MIDDLEWARE
     - SERVER → INFRASTRUCTURE

---

## 5. 测试要点

1. 创建节点时选择/不选择 Layer
2. 编辑节点时修改/清空 Layer
3. 列表筛选单个 Layer
4. 列表筛选 Layer + 其他条件组合
5. 节点详情展示 Layer
6. 处理 Layer 为 null 的情况

---

## 6. 相关 API 文档

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
