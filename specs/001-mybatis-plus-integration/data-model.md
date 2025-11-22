# 数据模型：MyBatis Plus 集成与节点管理仓储

**功能**: MyBatis Plus 集成与节点管理仓储
**日期**: 2025-11-22
**状态**: 已完成

## 概述

本文档定义 NodeEntity（节点实体）的数据模型，包括实体结构、字段约束、状态转换和验证规则。NodeEntity 用于管理系统节点（数据库、应用程序、API、报表等），支持基本 CRUD 操作、分页查询、逻辑删除和乐观锁并发控制。

## 核心实体

### NodeEntity（节点实体）

**用途**: 表示系统中的节点，可以是数据库服务器、业务应用、API 接口、报表系统等

**位置**: `infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/entity/NodeEntity.java`

**特性**: 纯 POJO，无框架注解，实现 Serializable 接口

#### 字段定义

| 字段名 | 类型 | 说明 | 约束 | 默认值 |
|--------|------|------|------|--------|
| id | Long | 主键 ID | 非空，由雪花算法自动生成 | - |
| name | String | 节点名称 | 非空，唯一，最大 100 字符 | - |
| type | String | 节点类型 | 非空，枚举值（DATABASE, APPLICATION, API, REPORT, OTHER） | - |
| description | String | 节点描述 | 可选，最大 500 字符 | null |
| properties | String | 节点属性 | 可选，JSON 格式字符串 | null |
| createTime | LocalDateTime | 创建时间 | 非空，系统自动填充 | 当前时间 |
| updateTime | LocalDateTime | 更新时间 | 非空，系统自动填充 | 当前时间 |
| createBy | String | 创建人 | 非空，通过方法参数传递 | - |
| updateBy | String | 更新人 | 非空，通过方法参数传递 | - |
| deleted | Integer | 逻辑删除标记 | 非空，0=活动，1=已删除 | 0 |
| version | Integer | 版本号 | 非空，用于乐观锁 | 0 |

#### 字段详解

**id（主键）**:
- 类型: Long
- 生成策略: 雪花算法（ASSIGN_ID）
- 说明: 全局唯一，分布式环境下保证唯一性

**name（节点名称）**:
- 类型: String
- 约束: 非空，唯一（数据库唯一索引）
- 长度: 1-100 字符
- 用途: 节点的唯一标识名称，如 "MySQL-Primary"、"OrderService"
- 验证: 不能为空白字符，不能包含特殊字符（可选）

**type（节点类型）**:
- 类型: String（枚举）
- 可选值:
  - `DATABASE`: 数据库节点（MySQL、PostgreSQL、Oracle 等）
  - `APPLICATION`: 业务应用节点（订单服务、用户服务等）
  - `API`: API 接口节点（REST API、GraphQL API 等）
  - `REPORT`: 报表系统节点（BI 报表、数据分析平台等）
  - `OTHER`: 其他类型节点
- 验证: 必须是上述 5 个值之一

**description（节点描述）**:
- 类型: String
- 约束: 可选
- 长度: 0-500 字符
- 用途: 节点的详细描述信息

**properties（节点属性）**:
- 类型: String（JSON 格式）
- 约束: 可选，必须是有效的 JSON 格式
- 用途: 存储节点的扩展属性，如连接信息、配置参数等
- 示例:
  ```json
  {
    "host": "192.168.1.100",
    "port": 3306,
    "database": "orders",
    "charset": "utf8mb4"
  }
  ```
- 验证: 应用层验证 JSON 格式的有效性

**createTime（创建时间）**:
- 类型: LocalDateTime
- 生成: 系统自动填充（INSERT 时）
- 时区: UTC 或配置的统一时区
- 格式: yyyy-MM-dd HH:mm:ss

**updateTime（更新时间）**:
- 类型: LocalDateTime
- 生成: 系统自动填充（INSERT 和 UPDATE 时）
- 说明: 每次更新记录时自动更新为当前时间

**createBy（创建人）**:
- 类型: String
- 来源: 通过 Repository 方法参数传递
- 说明: 记录哪个用户创建了该节点
- 示例: "admin"、"user123"

**updateBy（更新人）**:
- 类型: String
- 来源: 通过 Repository 方法参数传递
- 说明: 记录哪个用户最后更新了该节点

**deleted（逻辑删除标记）**:
- 类型: Integer
- 可选值: 0=活动，1=已删除
- 默认值: 0
- 说明: 逻辑删除，数据不会被物理删除
- 查询: 所有查询操作必须排除 deleted=1 的记录

**version（版本号）**:
- 类型: Integer
- 默认值: 0
- 用途: 乐观锁并发控制
- 行为: 每次更新时自动递增
- 冲突: 如果更新时 version 不匹配，抛出乐观锁异常

### NodePO（持久化对象）

**用途**: 数据库表映射对象，包含 MyBatis-Plus 注解

**位置**: `infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/po/NodePO.java`

**特性**: 包含 MyBatis-Plus 注解（@TableName、@TableId、@TableField、@TableLogic、@Version）

#### MyBatis-Plus 注解配置

```java
@TableName("t_node")
public class NodePO implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("description")
    private String description;

    @TableField("properties")
    private String properties;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
```

#### 注解说明

- `@TableName("t_node")`: 映射到数据库表 `t_node`
- `@TableId(type = IdType.ASSIGN_ID)`: 主键，使用雪花算法自动生成
- `@TableField(fill = FieldFill.INSERT)`: INSERT 时自动填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`: INSERT 和 UPDATE 时自动填充
- `@TableLogic`: 逻辑删除标记，0=未删除，1=已删除
- `@Version`: 乐观锁版本号

### PageResult（分页结果）

**用途**: 通用分页结果容器

**位置**: `common/src/main/java/com/demo/common/dto/PageResult.java`

**特性**: 泛型类，支持任意类型的数据分页

#### 字段定义

| 字段名 | 类型 | 说明 |
|--------|------|------|
| current | Long | 当前页码（从 1 开始） |
| size | Long | 每页记录数 |
| total | Long | 总记录数 |
| pages | Long | 总页数 |
| records | List<T> | 当前页数据列表 |

#### 方法定义

```java
public class PageResult<T> implements Serializable {
    private Long current;
    private Long size;
    private Long total;
    private Long pages;
    private List<T> records;

    // 类型转换方法
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = records.stream()
            .map(converter)
            .collect(Collectors.toList());

        PageResult<R> result = new PageResult<>();
        result.setCurrent(this.current);
        result.setSize(this.size);
        result.setTotal(this.total);
        result.setPages(this.pages);
        result.setRecords(convertedRecords);
        return result;
    }
}
```

## 数据库设计

### 表结构

**表名**: `t_node`

**存储引擎**: InnoDB

**字符集**: UTF8MB4

**DDL 语句**:

```sql
CREATE TABLE `t_node` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `type` VARCHAR(20) NOT NULL COMMENT '节点类型（DATABASE/APPLICATION/API/REPORT/OTHER）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '节点描述',
  `properties` TEXT DEFAULT NULL COMMENT '节点属性（JSON格式）',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `create_by` VARCHAR(50) NOT NULL COMMENT '创建人',
  `update_by` VARCHAR(50) NOT NULL COMMENT '更新人',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统节点表';
```

### 索引设计

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| uk_name | 唯一索引 | name | 保证节点名称唯一，提升按名称查询性能 |

**未创建的索引**（按需添加）:
- `idx_type`: 如果按类型查询频繁
- `idx_deleted`: 如果查询未删除记录很慢
- `idx_create_time`: 如果按时间排序很慢
- `idx_type_deleted`: 如果同时按类型和删除标记查询频繁

## 实体关系

当前功能为单表操作，NodeEntity 暂无关联实体。

**未来扩展**（不在本功能范围）:
- NodeRelationship（节点关系）: 记录节点之间的依赖关系
- NodeAttribute（节点属性）: 如果 properties 字段需要结构化存储

## 状态转换

### 生命周期状态

NodeEntity 通过 `deleted` 字段区分两种状态：

1. **活动状态** (deleted = 0): 节点正常可用
2. **已删除状态** (deleted = 1): 节点已逻辑删除

### 状态转换图

```
    创建
     ↓
[活动状态] ←——— 恢复（不在当前功能范围）
(deleted=0)
     ↓ 删除
[已删除状态]
(deleted=1)
```

### 转换规则

| 操作 | 前置状态 | 后置状态 | 说明 |
|------|---------|---------|------|
| 创建 (save) | 无 | 活动状态 | deleted 默认为 0 |
| 更新 (update) | 活动状态 | 活动状态 | 只能更新活动状态的节点 |
| 删除 (deleteById) | 活动状态 | 已删除状态 | 设置 deleted = 1 |
| 查询 (find*) | 活动状态 | 活动状态 | 只查询 deleted = 0 的节点 |

**注意**: 当前功能不支持恢复已删除的节点，未来如需支持，需要添加 `restore(Long id)` 方法。

## 验证规则

### 创建时验证

1. **name**: 非空，1-100 字符，唯一性（数据库唯一索引保证）
2. **type**: 非空，必须是 5 个枚举值之一
3. **description**: 如果提供，最大 500 字符
4. **properties**: 如果提供，必须是有效的 JSON 格式
5. **createBy**: 非空
6. **updateBy**: 非空（初始值同 createBy）

### 更新时验证

1. **id**: 非空，节点必须存在
2. **name**: 如果修改，1-100 字符，唯一性
3. **type**: 如果修改，必须是 5 个枚举值之一
4. **description**: 如果修改，最大 500 字符
5. **properties**: 如果修改，必须是有效的 JSON 格式
6. **updateBy**: 非空
7. **version**: 必须与数据库当前版本一致（乐观锁）

### 删除时验证

1. **id**: 非空，节点必须存在
2. **updateBy**: 非空（记录删除人）

### 查询时验证

1. **分页参数**: current >= 1, size >= 1, size <= 100
2. **name 过滤**: 如果提供，支持部分匹配
3. **type 过滤**: 如果提供，必须是 5 个枚举值之一

## Entity 和 PO 转换

### 转换职责

`NodeRepositoryImpl` 负责 Entity 和 PO 之间的双向转换，对外只暴露 Entity。

### 转换方法

```java
// Entity → PO
private NodePO toPO(NodeEntity entity) {
    if (entity == null) {
        return null;
    }
    NodePO po = new NodePO();
    po.setId(entity.getId());
    po.setName(entity.getName());
    po.setType(entity.getType());
    po.setDescription(entity.getDescription());
    po.setProperties(entity.getProperties());
    po.setCreateTime(entity.getCreateTime());
    po.setUpdateTime(entity.getUpdateTime());
    po.setCreateBy(entity.getCreateBy());
    po.setUpdateBy(entity.getUpdateBy());
    po.setDeleted(entity.getDeleted());
    po.setVersion(entity.getVersion());
    return po;
}

// PO → Entity
private NodeEntity toEntity(NodePO po) {
    if (po == null) {
        return null;
    }
    NodeEntity entity = new NodeEntity();
    entity.setId(po.getId());
    entity.setName(po.getName());
    entity.setType(po.getType());
    entity.setDescription(po.getDescription());
    entity.setProperties(po.getProperties());
    entity.setCreateTime(po.getCreateTime());
    entity.setUpdateTime(po.getUpdateTime());
    entity.setCreateBy(po.getCreateBy());
    entity.setUpdateBy(po.getUpdateBy());
    entity.setDeleted(po.getDeleted());
    entity.setVersion(po.getVersion());
    return entity;
}
```

### 转换原则

1. **字段一一对应**: Entity 和 PO 字段保持一致，简化转换逻辑
2. **空值处理**: 如果 PO 为 null，返回 null Entity（反之亦然）
3. **类型安全**: 所有字段类型完全匹配，无需类型转换
4. **封装隐藏**: 转换方法为 private，外部不可见

## 数据完整性

### 唯一性约束

- **name 字段**: 通过数据库唯一索引保证，重复插入或更新会抛出 `DuplicateKeyException`

### 乐观锁并发控制

- **version 字段**: MyBatis-Plus 乐观锁插件自动处理
- **更新流程**:
  1. 查询时获取当前 version
  2. 更新时传入 version
  3. 执行 SQL: `UPDATE t_node SET ... WHERE id = ? AND version = ?`
  4. 如果 version 不匹配（记录已被其他事务更新），返回影响行数为 0
  5. MyBatis-Plus 抛出 `OptimisticLockerException`

### 逻辑删除保护

- **deleted 字段**: MyBatis-Plus 逻辑删除插件自动处理
- **查询**: 自动添加 `WHERE deleted = 0` 条件
- **删除**: 执行 `UPDATE t_node SET deleted = 1 WHERE id = ?` 而非物理删除

## 数据量级估算

**初期规模**:
- 节点数量: 100-1000 个
- 单表大小: < 1MB
- 查询 QPS: < 100

**中期规模** (6个月后):
- 节点数量: 1000-10000 个
- 单表大小: < 10MB
- 查询 QPS: 100-1000

**扩展策略**:
- 如果节点数量超过 10 万，考虑分表或分库
- 如果查询性能下降，添加必要的索引（type、deleted、createTime）
- 如果 properties 字段频繁查询，考虑拆分到独立的属性表

## 总结

NodeEntity 数据模型设计遵循 DDD Entity/PO 分离原则，Entity 为纯 POJO 表达业务概念，PO 负责数据库映射。数据库设计采用单表结构，通过唯一索引保证名称唯一性，通过逻辑删除和乐观锁保证数据完整性和并发安全。分页结果使用通用的 PageResult 容器，支持类型转换。所有验证规则明确，状态转换清晰，为后续实现提供了完整的数据模型基础。
