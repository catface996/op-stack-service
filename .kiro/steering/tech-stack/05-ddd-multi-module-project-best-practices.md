# DDD 多模块项目使用规范

本文档指导 AI 如何在基于 DDD（领域驱动设计）的多模块 Maven 项目中正确地组织代码、命名和管理依赖。

## 核心原则

### 1. 严格分层原则

**依赖方向**：Interface → Application → Domain ← Infrastructure

**你必须遵守的依赖规则**：
- ❌ Domain 层不能依赖 Infrastructure 层
- ❌ 下层不能依赖上层
- ❌ API 模块不能依赖实现模块
- ❌ 不能有循环依赖

**检查方法**：在添加依赖前，确认依赖方向符合上述规则。

### 2. 业务语言优先原则

Domain 层使用纯粹的业务语言（Ubiquitous Language），不使用技术后缀。

**你应该这样命名**：
- ✅ Domain 层：`User`, `Order`（纯业务名词）
- ❌ Domain 层：`UserEntity`, `OrderEntity`（不要加技术后缀）

### 3. 框架无关原则

领域层应该独立于任何技术框架，保持纯粹的业务逻辑。

**你应该确保**：
- Domain 层的类不包含任何框架注解（如 @Entity, @Table）
- Domain 层不依赖任何持久化框架

## 模块分层结构

### 各层职责说明

| 层次 | 职责 | 典型模块 | 打包类型 |
|------|------|---------|---------|
| **Interface 层** | 对外提供服务入口 | http, consumer, job | jar |
| **Application 层** | 编排业务流程，协调领域服务 | application-api, application-impl | jar |
| **Domain 层** | 核心业务逻辑和领域模型 | domain-api, domain-impl | jar |
| **Infrastructure 层** | 技术实现和外部依赖 | repository, cache, mq | jar |
| **Bootstrap 层** | 应用启动和配置 | bootstrap | jar (可执行) |
| **Common 层** | 通用工具和基础设施 | common | jar |

### Interface 层模块说明

| 模块 | 职责 | 使用场景 |
|------|------|---------|
| **http** | HTTP 接口，提供 REST API | 前端调用、第三方系统调用 |
| **consumer** | 消息消费者，处理 MQ 消息 | 异步任务处理、事件驱动 |
| **job** | 定时任务，执行周期性任务 | 数据同步、报表生成、清理任务 |
| **rpc**（可选） | RPC 接口，提供远程调用 | 微服务间调用 |
| **websocket**（可选） | WebSocket 接口，实时通信 | 实时消息推送、在线聊天 |

### Infrastructure 层模块说明

| 模块 | 职责 | 技术选型示例 |
|------|------|-------------|
| **repository** | 数据持久化 | MySQL, PostgreSQL, MongoDB |
| **cache** | 缓存服务 | Redis, Memcached |
| **mq** | 消息队列 | RocketMQ, Kafka, RabbitMQ |

## 模块依赖关系

### 依赖类型说明

| 依赖类型 | Maven Scope | 说明 | 使用场景 |
|---------|-------------|------|---------|
| **编译依赖** | compile | 为了调用接口，编译时需要 | Interface 层依赖 Application API |
| **实现依赖** | compile | 为了实现接口，编译时需要 | Application Impl 依赖 Application API |
| **运行时依赖** | runtime | 为了打包启动，运行时需要 | Bootstrap 依赖所有 Impl 模块 |

### 各层依赖规则

**Interface 层**：
- 只依赖 Application API（调用应用服务）
- 不能直接依赖 Domain 或 Infrastructure

**Application 层**：
- 依赖 Domain API 和所有 Infrastructure API（调用领域服务和基础设施）
- 不能依赖 Interface 层

**Domain 层**：
- 只依赖 Domain API（实现领域服务）
- 不能依赖任何其他层

**Infrastructure 层**：
- 只依赖对应的 API 模块（实现接口）
- 不能依赖 Application 或 Interface 层

**Bootstrap 层**：
- 运行时依赖所有实现模块（打包启动）
- 不需要编译依赖

**Common 层**：
- 所有模块都可以依赖 common
- common 不依赖任何其他模块

## Package 结构和命名规范

### Package 命名格式

**基础格式**：`com.{company}.{system}.{layer}.{module}`

**你应该使用的 Package 结构**：

| 层次 | 模块 | Package 结构 | 关键子包 |
|------|------|-------------|---------|
| **Common** | common | `com.{company}.{system}.common` | dto, exception, constant, util |
| **Domain** | domain-api | `com.{company}.{system}.domain` | entity, vo, service |
| **Domain** | domain-impl | `com.{company}.{system}.domain.service.impl` | - |
| **Infrastructure** | repository-api | `com.{company}.{system}.infrastructure.repository` | api, entity |
| **Infrastructure** | mysql-impl | `com.{company}.{system}.infrastructure.repository.mysql` | config, po, mapper, impl |
| **Application** | application-api | `com.{company}.{system}.application` | dto, service |
| **Application** | application-impl | `com.{company}.{system}.application.service.impl` | - |
| **Interface** | http | `com.{company}.{system}.http` | controller, vo, request, response, handler |
| **Interface** | consumer | `com.{company}.{system}.consumer` | listener, handler, config |
| **Interface** | job | `com.{company}.{system}.job` | task, handler, config |
| **Bootstrap** | bootstrap | `com.{company}.{system}.bootstrap` | config |

### 关键 Package 原则

**你必须遵守**：
- ✅ Entity 在 repository-api，不包含任何框架注解
- ✅ PO 在 mysql-impl，包含框架注解（如 @TableName, @TableId）
- ✅ RepositoryImpl 在 mysql-impl，负责 Entity 和 PO 之间的转换
- ✅ 使用具体技术名称（`mysql` 而不是 `sql`）

**Interface 层职责划分**：
- **Consumer 模块**：Listener 监听消息，Handler 处理业务逻辑
- **Job 模块**：Task 定义定时任务，Handler 处理业务逻辑

## 命名规范

### 类命名规范

| 层次 | 对象类型 | 命名规则 | 示例 | 说明 |
|------|---------|---------|------|------|
| **Domain** | 领域实体 | 名词（无后缀） | `User`, `Order` | 纯业务概念，使用业务语言 |
| **Domain** | 值对象 | 名词（无后缀） | `Address`, `Money` | 不可变对象 |
| **Domain** | 领域服务 | `XxxDomainService` | `OrderDomainService` | 领域服务接口 |
| **Infrastructure** | 领域实体 | `XxxEntity` | `UserEntity` | Repository 接口使用，纯 POJO |
| **Infrastructure** | 持久化对象 | `XxxPO` | `UserPO` | 数据库映射，包含框架注解 |
| **Infrastructure** | 仓储接口 | `XxxRepository` | `UserRepository` | 仓储接口 |
| **Infrastructure** | 仓储实现 | `XxxRepositoryImpl` | `UserRepositoryImpl` | 仓储实现 |
| **Infrastructure** | Mapper 接口 | `XxxMapper` | `UserMapper` | MyBatis Mapper |
| **Application** | 数据传输对象 | `XxxDTO` | `UserDTO` | 应用层传输 |
| **Application** | 应用服务 | `XxxAppService` | `UserAppService` | 应用服务接口 |
| **Interface** | 视图对象 | `XxxVO` | `UserVO` | 前端展示 |
| **Interface** | 请求对象 | `XxxRequest` | `CreateUserRequest` | 接收输入 |
| **Interface** | 响应对象 | `XxxResponse` | `UserDetailResponse` | 返回结果 |
| **Interface** | 控制器 | `XxxController` | `UserController` | HTTP 接口 |
| **Interface** | 消息监听器 | `XxxListener` | `OrderCreatedListener` | MQ 消息监听 |
| **Interface** | 消息处理器 | `XxxMessageHandler` | `OrderMessageHandler` | MQ 消息处理 |
| **Interface** | 定时任务 | `XxxTask` | `DataSyncTask` | 定时任务 |
| **Interface** | 任务处理器 | `XxxHandler` | `DataSyncHandler` | 任务处理 |
| **Common** | 异常类 | `XxxException` | `BusinessException` | 异常 |
| **Common** | 工具类 | `XxxUtil` | `JsonUtil` | 工具类 |
| **Common** | 配置类 | `XxxConfig` | `MybatisPlusConfig` | 配置类 |

### 关键命名原则

1. **Domain 层使用纯业务语言**：
   - ✅ `User`, `Order`（直接使用名词，无后缀）
   - ❌ `UserEntity`, `OrderEntity`（不要在 Domain 层使用技术后缀）

2. **Infrastructure 层明确区分 Entity 和 PO**：
   - ✅ `UserEntity`（repository-api，纯 POJO）
   - ✅ `UserPO`（mysql-impl，包含注解）

3. **使用具体技术名称**：
   - ✅ `com.demo.ordercore.infrastructure.repository.mysql`
   - ❌ `com.demo.ordercore.infrastructure.repository.sql`

## Entity/PO 分离架构

### 为什么需要分离

1. **框架无关**：领域实体不依赖任何持久化框架
2. **易于测试**：纯 POJO 易于单元测试
3. **易于替换**：可以轻松切换持久化实现
4. **符合 DDD**：Entity 是领域概念，PO 是技术实现细节

### Entity 和 PO 的区别

| 特性 | Entity（领域实体） | PO（持久化对象） |
|------|------------------|-----------------|
| **位置** | repository-api 模块 | mysql-impl 模块 |
| **注解** | 无框架注解 | 包含 MyBatis-Plus 注解 |
| **职责** | 表示业务概念 | 映射数据库表 |
| **依赖** | 不依赖任何框架 | 依赖 MyBatis-Plus |
| **使用场景** | 业务层、应用层 | 仅在 Repository 实现中使用 |

### 转换规范

**在 RepositoryImpl 中进行转换**：
- Entity → PO：保存数据时转换
- PO → Entity：查询数据时转换
- 使用 BeanUtils.copyProperties 或专门的转换工具

## 数据流转规范

### 你应该遵循的数据转换规则

**写操作流程**：
前端 Request → Controller → DTO → Application Service → Domain Entity → Domain Service → Repository Entity → PO → 数据库

**读操作流程**：
数据库 → PO → Repository Entity → Domain Entity → DTO → VO → 前端

### 关键转换点（你必须在这些地方进行转换）

1. **Controller → Application**：Request/VO → DTO
2. **Application → Domain**：DTO → Domain Entity
3. **Application → Repository**：Domain Entity → Repository Entity
4. **Repository → Mapper**：Repository Entity → PO

**转换工具**：使用 BeanUtils.copyProperties 或专门的转换工具类

## MyBatis-Plus 配置规范

### 你应该如何配置

**配置类位置**：
- MybatisPlusConfig 放在 mysql-impl 模块的 config 包下
- 原因：配置与实现内聚，便于维护

**Mapper 扫描路径**：
- @MapperScan 注解的路径必须与 Mapper 接口的实际 package 一致
- 使用 `mysql` 而不是 `sql`（明确技术选型）

**类型别名配置**：
- type-aliases-package 配置 PO 类路径，不是 Entity 类路径
- MyBatis-Plus 直接操作 PO，而不是 Entity

**Mapper XML 配置**：
- XML namespace 必须与 Mapper 接口的全限定名一致
- resultMap type 必须与 PO 类的全限定名一致

## 常见错误和纠正方法

### 你应该避免的错误

| 错误类型 | 错误做法 | 正确做法 | 检查方法 |
|---------|---------|---------|---------|
| **Package 路径** | 使用 `sql` | 使用 `mysql` | 检查 package 名称是否使用具体技术名称 |
| **Entity/PO 位置** | Entity 和 PO 都在 mysql-impl | Entity 在 repository-api，PO 在 mysql-impl | 检查 Entity 是否在 api 模块 |
| **配置类位置** | MybatisPlusConfig 在 bootstrap | MybatisPlusConfig 在 mysql-impl | 检查配置类是否与实现内聚 |
| **类型别名配置** | type-aliases-package 指向 Entity | type-aliases-package 指向 PO | 检查配置是否指向 PO 包 |
| **Domain 命名** | Domain 层使用 `UserEntity` | Domain 层使用 `User` | 检查 Domain 层类名是否有技术后缀 |
| **跨层依赖** | Interface 层依赖 Domain 层 | Interface 层只依赖 Application 层 | 检查依赖方向是否符合分层原则 |

## 你的检查清单

在创建或修改代码时，你应该检查：

### 模块和 Package 检查
- [ ] 模块目录结构符合 DDD 分层架构
- [ ] Package 命名使用具体技术名称（mysql 而不是 sql）
- [ ] Entity 在 repository-api（纯 POJO，无注解）
- [ ] PO 在 mysql-impl（包含框架注解）
- [ ] 配置类在对应的实现模块（如 MybatisPlusConfig 在 mysql-impl）

### 命名检查
- [ ] Domain 层实体使用纯业务语言（User 而不是 UserEntity）
- [ ] Infrastructure 层实体使用 Entity 后缀（UserEntity）
- [ ] 持久化对象使用 PO 后缀（UserPO）
- [ ] 应用层使用 DTO 后缀（UserDTO）
- [ ] 接口层使用 VO/Request/Response 后缀

### 依赖检查
- [ ] 依赖方向符合：Interface → Application → Domain ← Infrastructure
- [ ] 无循环依赖
- [ ] API 模块不依赖实现模块
- [ ] Domain 层不依赖 Infrastructure 层

### 配置检查
- [ ] Mapper 扫描路径与实际 package 一致
- [ ] type-aliases-package 指向 PO 类路径
- [ ] Mapper XML namespace 与接口全限定名一致

## 关键原则总结

### DDD 架构原则

1. **严格分层**：遵循 DDD 分层原则，避免跨层依赖
2. **单一职责**：保持模块职责单一
3. **依赖方向**：依赖关系从外层指向内层
4. **业务语言**：Domain 层使用纯粹的业务语言

### 命名规范原则

1. **Domain 层**：使用纯业务概念，不加技术后缀
2. **Infrastructure 层**：明确区分 Entity（领域实体）和 PO（持久化对象）
3. **Application 层**：使用 DTO 后缀，面向用例设计
4. **Interface 层**：使用 VO/Request/Response 后缀，面向前端设计
5. **Package 命名**：使用具体技术名称，明确技术选型

### Entity/PO 分离原则

1. **Entity 在 repository-api**：纯 POJO，不依赖任何框架
2. **PO 在 mysql-impl**：包含框架注解，仅在 Repository 实现中使用
3. **RepositoryImpl 负责转换**：Entity ↔ PO 转换在 Repository 实现中完成
4. **配置内聚**：MybatisPlusConfig 放在 mysql-impl 模块

### 依赖管理原则

1. **编译依赖**：为了调用接口，scope = compile
2. **实现依赖**：为了实现接口，scope = compile
3. **运行时依赖**：为了打包启动，只在 bootstrap 中声明
4. **依赖传递**：Maven 会自动传递依赖，无需重复声明

## 关键收益

遵循这些规范，可以获得：

- ✅ 清晰的模块边界和职责划分
- ✅ 符合 DDD 分层架构原则
- ✅ 框架无关的领域层，易于测试和维护
- ✅ 清晰的命名规范，代码可读性高
- ✅ Entity/PO 分离，易于替换持久化实现
- ✅ 降低模块间耦合度
- ✅ 提高代码质量和可维护性
