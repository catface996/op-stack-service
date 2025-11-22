# NodeRepository 接口契约

**接口名称**: NodeRepository
**模块**: infrastructure/repository/repository-api
**包路径**: com.catface996.aiops.repository.api
**作用**: 节点实体的数据访问接口，提供框架无关的持久化操作

## 概述

NodeRepository 定义了 NodeEntity（节点实体）的数据访问契约，包括基本 CRUD 操作、条件查询和分页查询。接口设计遵循 DDD Repository 模式，返回值为领域实体（Entity）而非持久化对象（PO），确保业务层不依赖持久化技术细节。

## 接口定义

### 保存节点

```java
/**
 * 保存节点
 *
 * @param entity   节点实体
 * @param operator 操作人（用于填充 createBy 和 updateBy）
 * @return 保存后的节点实体（包含生成的 ID 和时间戳）
 * @throws DuplicateKeyException 如果节点名称已存在
 */
NodeEntity save(NodeEntity entity, String operator);
```

**前置条件**:
- entity 不为 null
- entity.name 不为空，长度 1-100 字符
- entity.type 不为空，必须是 DATABASE/APPLICATION/API/REPORT/OTHER 之一
- entity.description 如果提供，最大 500 字符
- entity.properties 如果提供，必须是有效的 JSON 格式
- operator 不为空

**后置条件**:
- entity.id 被设置为自动生成的雪花算法 ID
- entity.createTime 和 entity.updateTime 被设置为当前时间
- entity.createBy 和 entity.updateBy 被设置为 operator
- entity.deleted 被设置为 0
- entity.version 被设置为 0
- 节点记录被插入到数据库

**异常**:
- `DuplicateKeyException`: 节点名称已存在（唯一约束冲突）
- `IllegalArgumentException`: 参数验证失败

**示例**:
```java
NodeEntity node = new NodeEntity();
node.setName("MySQL-Primary");
node.setType("DATABASE");
node.setDescription("主库");
node.setProperties("{\"host\":\"192.168.1.100\",\"port\":3306}");

NodeEntity saved = nodeRepository.save(node, "admin");
// saved.getId() -> 自动生成的 ID
// saved.getCreateTime() -> 当前时间
// saved.getVersion() -> 0
```

---

### 更新节点

```java
/**
 * 更新节点
 *
 * @param entity   节点实体（必须包含 id 和 version）
 * @param operator 操作人（用于填充 updateBy）
 * @throws OptimisticLockException 如果版本号不匹配（乐观锁冲突）
 * @throws NotFoundException        如果节点不存在
 */
void update(NodeEntity entity, String operator);
```

**前置条件**:
- entity 不为 null
- entity.id 不为 null，节点必须存在
- entity.version 不为 null，必须与数据库当前版本一致
- entity.name 如果修改，长度 1-100 字符，不能与其他节点重复
- entity.type 如果修改，必须是 DATABASE/APPLICATION/API/REPORT/OTHER 之一
- entity.description 如果修改，最大 500 字符
- entity.properties 如果修改，必须是有效的 JSON 格式
- operator 不为空

**后置条件**:
- entity.updateTime 被更新为当前时间
- entity.updateBy 被更新为 operator
- entity.version 自动递增
- 节点记录在数据库中被更新
- 返回的 entity 包含更新后的 updateTime 和 version

**异常**:
- `OptimisticLockException`: 版本号不匹配（并发冲突）
- `NotFoundException`: 节点不存在
- `DuplicateKeyException`: 更新后的名称与其他节点冲突
- `IllegalArgumentException`: 参数验证失败

**示例**:
```java
NodeEntity node = nodeRepository.findById(123L);
node.setDescription("更新后的描述");

nodeRepository.update(node, "admin");
// node.getUpdateTime() -> 当前时间
// node.getVersion() -> 原版本号 + 1
```

---

### 根据 ID 查询节点

```java
/**
 * 根据 ID 查询节点
 *
 * @param id 节点 ID
 * @return 节点实体，如果不存在或已删除返回 null
 */
NodeEntity findById(Long id);
```

**前置条件**:
- id 不为 null

**后置条件**:
- 如果节点存在且未删除（deleted = 0），返回节点实体
- 如果节点不存在或已删除，返回 null

**异常**:
- `IllegalArgumentException`: id 为 null

**示例**:
```java
NodeEntity node = nodeRepository.findById(123L);
if (node != null) {
    System.out.println("节点名称: " + node.getName());
} else {
    System.out.println("节点不存在");
}
```

---

### 根据名称查询节点

```java
/**
 * 根据名称查询节点
 *
 * @param name 节点名称（精确匹配）
 * @return 节点实体，如果不存在或已删除返回 null
 */
NodeEntity findByName(String name);
```

**前置条件**:
- name 不为空

**后置条件**:
- 如果节点存在且未删除（deleted = 0），返回节点实体
- 如果节点不存在或已删除，返回 null
- 只查询 name 完全匹配的节点（不是模糊查询）

**异常**:
- `IllegalArgumentException`: name 为空

**示例**:
```java
NodeEntity node = nodeRepository.findByName("MySQL-Primary");
if (node != null) {
    System.out.println("节点 ID: " + node.getId());
}
```

---

### 根据类型查询节点列表

```java
/**
 * 根据类型查询节点列表
 *
 * @param type 节点类型（DATABASE/APPLICATION/API/REPORT/OTHER）
 * @return 节点列表，按 createTime 降序排列，如果无结果返回空列表
 */
List<NodeEntity> findByType(String type);
```

**前置条件**:
- type 不为空
- type 必须是 DATABASE/APPLICATION/API/REPORT/OTHER 之一

**后置条件**:
- 返回所有匹配类型且未删除（deleted = 0）的节点
- 按 createTime 降序排列（最新创建的在前）
- 如果无结果，返回空列表（不返回 null）

**异常**:
- `IllegalArgumentException`: type 为空或不是有效的枚举值

**示例**:
```java
List<NodeEntity> databases = nodeRepository.findByType("DATABASE");
System.out.println("数据库节点数量: " + databases.size());
```

---

### 分页查询节点

```java
/**
 * 分页查询节点
 *
 * @param current 当前页码（从 1 开始）
 * @param size    每页记录数（最大 100）
 * @param name    节点名称过滤（可选，支持模糊匹配）
 * @param type    节点类型过滤（可选，精确匹配）
 * @return 分页结果
 */
PageResult<NodeEntity> findPage(Integer current, Integer size, String name, String type);
```

**前置条件**:
- current >= 1
- size >= 1 且 size <= 100
- name 可为 null（不过滤）或非空字符串（模糊匹配）
- type 可为 null（不过滤）或有效的枚举值（精确匹配）

**后置条件**:
- 返回分页结果，包含：
  - current: 当前页码
  - size: 每页记录数
  - total: 总记录数
  - pages: 总页数
  - records: 当前页的节点列表
- 只查询未删除（deleted = 0）的节点
- 按 createTime 降序排列
- name 过滤为模糊匹配（LIKE '%name%'）
- type 过滤为精确匹配
- 如果 current 超出总页数，返回空记录列表

**异常**:
- `IllegalArgumentException`: current < 1 或 size < 1 或 size > 100

**示例**:
```java
// 查询第 1 页，每页 10 条，名称包含 "MySQL"，类型为 "DATABASE"
PageResult<NodeEntity> page = nodeRepository.findPage(1, 10, "MySQL", "DATABASE");
System.out.println("总记录数: " + page.getTotal());
System.out.println("总页数: " + page.getPages());
page.getRecords().forEach(node -> {
    System.out.println("节点名称: " + node.getName());
});

// 查询所有节点（不过滤）
PageResult<NodeEntity> allNodes = nodeRepository.findPage(1, 20, null, null);
```

---

### 逻辑删除节点

```java
/**
 * 逻辑删除节点
 *
 * @param id       节点 ID
 * @param operator 操作人（用于填充 updateBy）
 * @throws NotFoundException 如果节点不存在
 */
void deleteById(Long id, String operator);
```

**前置条件**:
- id 不为 null
- operator 不为空
- 节点必须存在

**后置条件**:
- 节点的 deleted 字段被设置为 1
- 节点的 updateTime 被更新为当前时间
- 节点的 updateBy 被更新为 operator
- 节点不会被物理删除，记录仍在数据库中
- 后续查询不会返回该节点

**异常**:
- `NotFoundException`: 节点不存在
- `IllegalArgumentException`: id 或 operator 为空

**示例**:
```java
nodeRepository.deleteById(123L, "admin");
// 节点仍在数据库中，但 deleted = 1
// findById(123L) 将返回 null
```

## 数据一致性保证

### 唯一性保证

- **name 字段**: 通过数据库唯一索引保证，重复插入或更新会抛出 `DuplicateKeyException`

### 并发控制

- **乐观锁**: 使用 version 字段，并发更新时自动检测冲突，抛出 `OptimisticLockException`

### 逻辑删除

- **软删除**: 所有查询操作自动排除 deleted = 1 的记录，保证数据一致性

## 性能要求

| 操作 | 目标性能 |
|------|---------|
| save | < 100ms |
| update | < 100ms |
| findById | < 50ms |
| findByName | < 50ms |
| findByType | < 100ms |
| findPage | < 200ms |
| deleteById | < 100ms |

## 事务要求

- **save**: 需要事务保护，确保数据一致性
- **update**: 需要事务保护，确保数据一致性
- **deleteById**: 需要事务保护，确保数据一致性
- **findById**: 只读操作，无需事务
- **findByName**: 只读操作，无需事务
- **findByType**: 只读操作，无需事务
- **findPage**: 只读操作，无需事务

## 异常处理

### 标准异常

| 异常类型 | 抛出场景 | HTTP 状态码建议 |
|---------|---------|---------------|
| `IllegalArgumentException` | 参数验证失败 | 400 Bad Request |
| `NotFoundException` | 资源不存在 | 404 Not Found |
| `DuplicateKeyException` | 唯一约束冲突 | 409 Conflict |
| `OptimisticLockException` | 乐观锁冲突 | 409 Conflict |

### 错误消息示例

```java
// 参数验证失败
throw new IllegalArgumentException("节点名称不能为空");
throw new IllegalArgumentException("每页记录数不能超过 100");

// 资源不存在
throw new NotFoundException("节点不存在: id=" + id);

// 唯一约束冲突
throw new DuplicateKeyException("节点名称已存在: " + name);

// 乐观锁冲突
throw new OptimisticLockException("节点已被其他用户修改，请刷新后重试");
```

## 测试契约

### 单元测试要求

每个方法必须包含以下测试用例：

**save**:
- 成功保存：验证 ID、时间戳、默认值
- 名称重复：抛出 DuplicateKeyException
- 参数为空：抛出 IllegalArgumentException

**update**:
- 成功更新：验证 updateTime、version 递增
- 版本号不匹配：抛出 OptimisticLockException
- 节点不存在：抛出 NotFoundException
- 名称冲突：抛出 DuplicateKeyException

**findById**:
- 查询存在的节点：返回正确的实体
- 查询不存在的节点：返回 null
- 查询已删除的节点：返回 null

**findByName**:
- 查询存在的节点：返回正确的实体
- 查询不存在的节点：返回 null

**findByType**:
- 查询特定类型：返回匹配的列表
- 查询不存在的类型：返回空列表
- 验证排序：按 createTime 降序

**findPage**:
- 基本分页：验证 total、pages、records
- 名称过滤：验证模糊匹配
- 类型过滤：验证精确匹配
- 边界条件：current 超出范围返回空列表
- 参数验证：size > 100 抛出异常

**deleteById**:
- 成功删除：验证 deleted = 1
- 删除后查询：返回 null
- 节点不存在：抛出 NotFoundException

## 实现注意事项

### MySQL 实现层（NodeRepositoryImpl）

1. **Entity/PO 转换**: 实现 toEntity() 和 toPO() 私有方法
2. **Mapper 调用**: 使用 NodeMapper 进行数据库操作
3. **异常转换**: 将数据库异常转换为领域异常
4. **空值处理**: 确保不返回 null Entity（除非业务要求）
5. **自动填充**: createBy 和 updateBy 通过参数传递，不依赖自动填充

### Mapper XML

1. **命名空间**: 与 NodeMapper 接口全限定名一致
2. **ResultMap**: type 属性指向 NodePO
3. **SQL 语句**: 所有条件查询必须包含 `deleted = 0` 条件
4. **动态 SQL**: 使用 `<if test>` 实现可选的过滤条件

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| 1.0.0 | 2025-11-22 | 初始版本，定义基本 CRUD 和分页查询接口 |

## 参考

- 功能规格说明: [spec.md](../spec.md)
- 数据模型: [data-model.md](../data-model.md)
- 实施计划: [plan.md](../plan.md)
