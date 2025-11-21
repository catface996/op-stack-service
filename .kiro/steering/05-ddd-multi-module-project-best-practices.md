---
inclusion: manual
---

# DDD 多模块 Maven 项目最佳实践

本文档专门针对基于 DDD（领域驱动设计）的多模块 Maven 项目开发的最佳实践。

## 适用范围

本文档适用于以下类型的项目：
- 使用 Maven 作为构建工具的多模块项目
- 采用 DDD 分层架构的 Java 项目
- 使用 Spring Boot 框架的应用

## 文档结构

本文档按照 Spec 开发流程组织内容：
1. **设计阶段**：ADR 示例和架构决策
2. **任务执行阶段**：渐进式模块声明和验证标准
3. **项目结构**：模块分层和依赖关系
4. **Maven 配置**：父 POM 管理和子模块配置
5. **Spring Boot 集成**：Actuator、日志、异常处理
6. **验收标准示例**：具体的验证方法

## 一、设计阶段指南

### ADR 示例

在 DDD 多模块 Maven 项目的设计阶段，以下是典型的架构决策记录示例。这些示例展示了如何记录重要的技术选型和架构决策。

**注意**：只有重要的架构级别决策才需要记录 ADR，不是所有技术选择都需要。

### ADR-001：选择 MyBatis-Plus 而非 JPA

- **状态**：已接受
- **背景**：需要选择 ORM 框架来实现数据持久化
- **决策**：选择 MyBatis-Plus 作为 ORM 框架
- **理由**：
  - 更灵活的 SQL 控制，便于性能优化
  - 更好的复杂查询支持
  - 团队对 MyBatis 熟悉度高
  - 简化 CRUD 操作，提供代码生成器
- **后果**：
  - 正面：SQL 可控性强，性能优化灵活，学习成本低
  - 负面：需要手动编写复杂查询的 SQL，相比 JPA 需要更多配置

**说明**：这个示例展示了如何记录技术选型决策，包括备选方案对比和权衡分析。

### ADR-002：采用 DDD 分层架构

- **状态**：已接受
- **背景**：需要确定项目的整体架构模式
- **决策**：采用 DDD（领域驱动设计）分层架构
- **理由**：
  - 业务逻辑复杂，需要清晰的领域模型
  - 分层架构便于职责划分和团队协作
  - 领域层独立，便于测试和维护
  - 符合团队技术栈和经验
- **后果**：
  - 正面：清晰的模块边界，高内聚低耦合，易于维护和扩展
  - 负面：初期搭建成本较高，需要团队理解 DDD 概念

**说明**：这个示例展示了如何记录架构模式决策，这类决策会影响整个项目的结构。

## 二、任务执行阶段指南

在 DDD 多模块 Maven 项目的任务执行阶段，需要特别注意以下事项：

### 渐进式模块声明的重要性

**问题场景**：

在多模块 Maven 项目中，如果在父 POM 中预先声明了所有子模块，但这些模块尚未创建，会导致编译失败：

```
[ERROR] Child module /path/to/module does not exist
```

**解决方案**：渐进式模块声明

#### 1. 只声明已创建的模块
- 在父 POM 的 `<modules>` 节中，只包含已经实际创建的模块
- 不要预先声明计划中但尚未创建的模块

#### 2. 创建模块时同步更新父 POM
- 每创建一个新模块后，立即在父 POM 中添加该模块的声明
- 确保模块声明与实际目录结构保持同步

#### 3. 多层级模块的处理
- 对于有子模块的父模块（如 infrastructure），同样遵循此原则
- 父模块的 `<modules>` 节也应该只声明已创建的子模块

### 实施步骤示例

以创建 infrastructure 父模块为例：

1. 创建 infrastructure/pom.xml，暂时不声明子模块
2. 在根 pom.xml 中添加 infrastructure 模块声明
3. 运行 `mvn clean compile` 验证成功
4. 后续创建子模块（repository、cache、mq）时，再逐个添加到 infrastructure/pom.xml 的 `<modules>` 节中

### 注意事项

1. 不要一次性声明所有计划中的模块
2. 每次修改 POM 后都要验证编译
3. 保持模块声明顺序与依赖关系一致
4. 对于父模块，先创建父 POM，再逐步添加子模块
5. 遇到编译错误时，优先检查模块声明和依赖配置

### 任务完成检查清单（Maven 项目特定）

- [ ] 代码已提交到正确的位置
- [ ] 相关 POM 文件已更新
- [ ] 项目可以成功编译（`mvn clean compile`）
- [ ] 没有编译错误或警告
- [ ] 模块声明与实际目录结构一致
- [ ] 依赖关系配置正确
- [ ] 所有依赖都未指定版本号（由父 POM 管理）
- [ ] **如果功能可运行验证，已通过运行应用进行验证**
- [ ] **如果是结构性变更，已通过编译验证**

## 三、项目结构规范

### 3.1 顶层模块结构

```
project-root/
├── common/                    # 通用模块
├── domain/                    # 领域层（父模块）
│   ├── domain-api/           # 领域 API
│   └── domain-impl/          # 领域实现
├── application/              # 应用层（父模块）
│   ├── application-api/      # 应用 API
│   └── application-impl/     # 应用实现
├── infrastructure/           # 基础设施层（父模块）
│   ├── repository/          # 仓储层（父模块）
│   │   ├── repository-api/  # 仓储 API
│   │   └── mysql-impl/      # MySQL 实现
│   ├── cache/               # 缓存层（父模块，可选）
│   │   ├── cache-api/       # 缓存 API
│   │   └── redis-impl/      # Redis 实现
│   └── mq/                  # 消息队列层（父模块，可选）
│       ├── mq-api/          # 消息队列 API
│       └── rocketmq-impl/   # RocketMQ 实现
├── interface/               # 接口层（父模块）
│   ├── http/               # HTTP 接口（REST API）
│   ├── consumer/           # 消息消费者（MQ Consumer）
│   └── job/                # 定时任务（Scheduled Job）
└── bootstrap/              # 启动模块
```

### 3.2 模块类型说明

| 模块类型 | 说明 | 打包类型 | 示例 |
|---------|------|---------|------|
| **父模块** | 只包含 pom.xml，用于组织子模块 | pom | domain, infrastructure, interface |
| **API 模块** | 定义接口和契约，不包含实现 | jar | domain-api, repository-api, mq-api |
| **实现模块** | 实现 API 模块定义的接口 | jar | domain-impl, mysql-impl, redis-impl |
| **接口模块** | 对外提供服务的入口 | jar | http, consumer, job |
| **启动模块** | 应用程序入口，可执行 | jar | bootstrap |

### 3.3 Interface 层模块说明

Interface 层包含多种对外提供服务的方式：

| 模块 | 说明 | 典型场景 |
|------|------|---------|
| **http** | HTTP 接口，提供 REST API | 前端调用、第三方系统调用 |
| **consumer** | 消息消费者，处理 MQ 消息 | 异步任务处理、事件驱动 |
| **job** | 定时任务，执行周期性任务 | 数据同步、报表生成、清理任务 |
| **rpc**（可选） | RPC 接口，提供远程调用 | 微服务间调用 |
| **websocket**（可选） | WebSocket 接口，实时通信 | 实时消息推送、在线聊天 |

### 3.4 模块依赖关系

模块间的依赖应遵循 DDD 分层架构原则，依赖关系分为三种类型：

#### 3.4.1 依赖关系类型说明

| 依赖类型 | 说明 | Maven Scope | 示例 |
|---------|------|-------------|------|
| **编译依赖** | 为了调用接口，编译时需要 | compile | Interface 层依赖 Application API |
| **实现依赖** | 为了实现接口，编译时需要 | compile | Application Impl 依赖 Application API |
| **运行时依赖** | 为了打包启动，运行时需要 | runtime | Bootstrap 依赖所有 Impl 模块 |

#### 3.4.2 分层依赖关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      bootstrap (启动模块)                      │
│  运行时依赖: http, consumer, job, application-impl,          │
│             mysql-impl, redis-impl, rocketmq-impl, domain-impl│
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────────┐    ┌──────────────┐
│ Interface 层  │    │ Application 层    │    │Infrastructure│
│              │    │                  │    │     层       │
│ ┌──────────┐ │    │ ┌──────────────┐ │    │ ┌──────────┐ │
│ │   http   │ │    │ │application-  │ │    │ │mysql-impl│ │
│ │          │ │    │ │    impl      │ │    │ │          │ │
│ │ consumer │ │    │ │              │ │    │ │redis-impl│ │
│ │          │ │    │ │  (实现依赖)   │ │    │ │          │ │
│ │   job    │ │    │ │      ↓       │ │    │ │rocketmq- │ │
│ └──────────┘ │    │ │application-  │ │    │ │  impl    │ │
│              │    │ │    api       │ │    │ └──────────┘ │
│  (编译依赖)   │    │ └──────────────┘ │    │              │
│      ↓       │    │                  │    │  (实现依赖)   │
│application-  │    │  (编译依赖)       │    │      ↓       │
│    api       │    │      ↓           │    │ repository-  │
└──────────────┘    │  ┌───┴────┬─────┐│    │    api       │
                    │  ↓        ↓     ││    │              │
                    │domain- repository││    │   mq-api     │
                    │ api      api    ││    │              │
                    │          mq-api ││    │  cache-api   │
                    └──────────────────┘    └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │  Domain 层    │
                    │              │
                    │ ┌──────────┐ │
                    │ │ domain-  │ │
                    │ │  impl    │ │
                    │ │          │ │
                    │ │(实现依赖) │ │
                    │ │    ↓     │ │
                    │ │ domain-  │ │
                    │ │   api    │ │
                    │ └──────────┘ │
                    └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │    common     │
                    │  (所有模块)   │
                    └──────────────┘
```

#### 3.4.3 各模块依赖说明

**1. bootstrap（启动模块）**
- 运行时依赖：http, consumer, job, application-impl, mysql-impl, redis-impl, rocketmq-impl, domain-impl
- 说明：启动模块依赖所有实现模块，用于打包启动

**2. http（Interface 层 - HTTP 接口）**
- 编译依赖：application-api, common
- 说明：调用应用服务 API

**3. consumer（Interface 层 - 消息消费者）**
- 编译依赖：application-api, mq-api, common
- 说明：调用应用服务 API，使用消息定义

**4. job（Interface 层 - 定时任务）**
- 编译依赖：application-api, common
- 说明：调用应用服务 API

**5. application-impl（Application 层实现）**
- 实现依赖：application-api
- 编译依赖：domain-api, repository-api, cache-api, mq-api, common
- 说明：实现应用服务接口，调用领域服务和基础设施服务

**6. mysql-impl（Infrastructure 层 - 数据库实现）**
- 实现依赖：repository-api
- 编译依赖：common, mybatis-plus-boot-starter
- 说明：实现仓储接口

**7. redis-impl（Infrastructure 层 - 缓存实现）**
- 实现依赖：cache-api
- 编译依赖：common, spring-boot-starter-data-redis
- 说明：实现缓存接口

**8. rocketmq-impl（Infrastructure 层 - 消息队列实现）**
- 实现依赖：mq-api
- 编译依赖：common, rocketmq-spring-boot-starter
- 说明：实现消息队列接口

**9. domain-impl（Domain 层实现）**
- 实现依赖：domain-api
- 编译依赖：common
- 说明：实现领域服务接口

**10. 所有 API 模块**
- 编译依赖：common
- 说明：API 模块只依赖 common，保持纯净

#### 3.4.4 依赖原则总结

**编译依赖原则**：
1. **Interface 层**：只依赖 Application API（调用应用服务）
2. **Application 层**：依赖 Domain API 和所有 Infrastructure API（调用领域服务和基础设施）
3. **Infrastructure 层**：只依赖对应的 API 模块（实现接口）
4. **Domain 层**：只依赖 Domain API（实现领域服务）
5. **所有模块**：都可以依赖 common

**运行时依赖原则**：
1. **Bootstrap 模块**：依赖所有实现模块（打包启动）
2. **其他模块**：不需要运行时依赖

**禁止的依赖**：
- ❌ API 模块不能依赖实现模块
- ❌ Interface 层不能直接依赖 Domain 或 Infrastructure
- ❌ Domain 层不能依赖 Infrastructure 层
- ❌ 下层不能依赖上层
- ❌ 不能有循环依赖

**关键点**：
- ✅ **编译依赖**：为了调用接口，scope = compile
- ✅ **实现依赖**：为了实现接口，scope = compile
- ✅ **运行时依赖**：为了打包启动，只在 bootstrap 中声明
- ✅ **依赖传递**：Maven 会自动传递依赖，无需重复声明

## 四、Maven 配置规范

### 父 POM 管理

#### 1. 统一版本管理

- 在父 POM 的 `<dependencyManagement>` 中统一管理所有依赖版本
- 包括 Spring Boot、第三方库、内部模块的版本
- 使用 `<properties>` 定义版本号变量

#### 2. 渐进式模块声明

- 在 `<modules>` 节中只声明已创建的模块
- 不要预先声明计划中但尚未创建的模块
- 创建新模块后立即添加声明

### 子模块配置

#### 1. 引用父 POM

- 每个子模块的 `<parent>` 节必须引用父 POM
- 指定正确的 groupId、artifactId 和 version

#### 2. 依赖版本管理

- 子模块引用依赖时不指定版本号
- 版本由父 POM 的 `<dependencyManagement>` 统一管理
- 确保依赖版本的一致性

## 五、模块创建流程

### 标准流程

1. **创建模块目录和 pom.xml**
2. **在父 POM 中添加模块声明**
3. **验证编译**：`mvn clean compile`
4. **创建源代码结构**
5. **实现功能**
6. **再次验证编译**

### 多层级模块处理

对于有子模块的父模块（如 infrastructure），遵循相同原则：

1. 先创建父模块（infrastructure）
2. 在根 POM 中声明 infrastructure
3. 验证编译
4. 创建子模块（repository、cache、mq）
5. 在 infrastructure/pom.xml 中逐个声明子模块
6. 每次添加后都验证编译

## 六、验证标准

### 编译验证

每个任务完成后必须执行 `mvn clean compile`，确保：
- 输出包含 "BUILD SUCCESS"
- Reactor Build Order 列出所有已声明的模块
- 没有任何 ERROR 信息

### 打包验证

对于 bootstrap 模块，执行 `mvn clean package`，确保：
- bootstrap/target 目录下生成可执行 JAR
- JAR 文件名包含版本号

### 运行时验证

对于可运行的功能，启动应用验证：
- 使用 `mvn spring-boot:run` 或运行 JAR 文件
- 访问相关端点验证功能
- 检查日志输出和响应结果

## 七、常见问题处理

### 模块未找到错误

```
[ERROR] Child module /path/to/module does not exist
```

**原因**：在 POM 中声明了尚未创建的模块

**解决**：
1. 从 POM 中移除该模块声明
2. 创建模块后再添加声明

### 依赖版本冲突

```
[WARNING] Some problems were encountered while building the effective model
```

**原因**：子模块指定了与父 POM 不同的版本

**解决**：
1. 移除子模块中的版本号声明
2. 在父 POM 的 dependencyManagement 中统一管理

### 循环依赖

```
[ERROR] The projects in the reactor contain a cyclic reference
```

**原因**：模块间存在循环依赖

**解决**：
1. 检查模块依赖关系
2. 重新设计模块边界
3. 遵循 DDD 分层原则

## 八、Spring Boot 集成规范

### Actuator 配置

在 bootstrap 模块的 application.yml 中配置监控端点：
- 暴露必要的端点（health、info、prometheus、metrics）
- 配置健康检查详细信息显示级别
- 注意端点安全配置，避免信息泄露

### 日志配置

配置结构化日志：
- 使用 JSON 格式输出日志
- 包含 timestamp、level、traceId、spanId、message 等字段
- 支持分布式追踪

### 异常处理

在 http 模块实现全局异常处理器：
- 使用 `@RestControllerAdvice` 统一处理异常
- 区分业务异常和系统异常
- 返回统一的响应格式（Result 对象）
- HTTP 状态码为 200，业务错误通过 code 字段表示

## 九、验收标准编写指南

以下是 DDD 多模块 Maven 项目中常见任务的验收标准编写要点：

### 模块结构验证

- 执行 `mvn clean compile` 验证编译成功
- 检查 Reactor Build Order 确认模块顺序
- 确认所有模块的 pom.xml 引用父 POM
- 确认依赖版本由父 POM 统一管理

### Prometheus 集成验证

- 启动应用后访问 Prometheus 端点
- 确认返回正确格式的指标数据
- 确认包含必要的监控指标

### 日志格式验证

- 启动应用后检查日志输出
- 确认日志格式符合要求（JSON 格式）
- 确认包含必要的追踪字段（traceId、spanId）

### 异常处理验证

- 触发业务异常验证响应格式
- 确认响应包含必要字段（code、message、data）
- 确认 HTTP 状态码和业务错误码的使用规范

## 十、Package 结构和命名规范

### 10.1 基础 Package 规范

**基础 Package 格式**：`com.{company}.{system}.{layer}.{module}`

**示例**：
- `com.demo.ordercore.domain.entity`
- `com.demo.ordercore.infrastructure.repository.api`

**说明**：
- `{company}`：公司名称（如 demo）
- `{system}`：系统名称（如 ordercore）
- `{layer}`：分层名称（如 domain, application, infrastructure）
- `{module}`：模块名称（如 entity, service, repository）

### 10.2 各层 Package 结构详解

#### 10.2.1 Common 模块

```
com.demo.ordercore.common/
├── dto/                      # 数据传输对象
│   ├── Result.java          # 统一响应类
│   └── PageResult.java      # 分页结果类
├── exception/               # 异常体系
│   ├── BaseException.java
│   ├── BusinessException.java
│   └── SystemException.java
├── constant/                # 常量定义
│   └── ErrorCode.java
└── util/                    # 工具类
    └── JsonUtil.java
```

**命名规则**：
- DTO 类：`XxxDTO`（如 `UserDTO`）
- 异常类：`XxxException`（如 `BusinessException`）
- 常量类：`XxxConstant` 或直接使用名词（如 `ErrorCode`）
- 工具类：`XxxUtil`（如 `JsonUtil`）

#### 10.2.2 Domain 层

**domain-api 模块**：

```
com.demo.ordercore.domain/
├── entity/                  # 领域实体（纯 POJO）
│   ├── User.java           # ✅ 直接使用名词，不加后缀
│   └── Order.java
├── vo/                      # 值对象
│   ├── Address.java        # ✅ 值对象，不可变
│   └── Money.java
└── service/                 # 领域服务接口
    └── OrderDomainService.java
```

**domain-impl 模块**：

```
com.demo.ordercore.domain/
└── service/
    └── impl/
        └── OrderDomainServiceImpl.java
```

**命名规则**：
- 领域实体：直接使用名词（如 `User`、`Order`）- **不加后缀**
- 值对象：直接使用名词（如 `Address`、`Money`）
- 领域服务接口：`XxxDomainService`
- 领域服务实现：`XxxDomainServiceImpl`

**重要说明**：Domain 层使用纯粹的业务语言（Ubiquitous Language），不使用技术后缀

#### 10.2.3 Infrastructure 层 - Repository

**repository-api 模块**：

```
com.demo.ordercore.infrastructure.repository/
├── api/                     # 仓储接口
│   ├── UserRepository.java
│   └── OrderRepository.java
└── entity/                  # 领域实体（纯 POJO，无框架注解）
    ├── UserEntity.java     # ✅ 加 Entity 后缀，区分于 Domain 层
    └── OrderEntity.java
```

**mysql-impl 模块**：

```
com.demo.ordercore.infrastructure.repository.mysql/
├── config/                  # 配置类
│   ├── MybatisPlusConfig.java
│   └── CustomMetaObjectHandler.java
├── po/                      # 持久化对象（包含框架注解）
│   ├── UserPO.java         # ✅ 加 PO 后缀，明确是数据库映射
│   └── OrderPO.java
├── mapper/                  # MyBatis Mapper 接口
│   ├── UserMapper.java
│   └── OrderMapper.java
└── impl/                    # 仓储实现
    ├── UserRepositoryImpl.java
    └── OrderRepositoryImpl.java
```

**Mapper XML 文件位置**：
```
mysql-impl/src/main/resources/
└── mapper/
    ├── UserMapper.xml
    └── OrderMapper.xml
```

**命名规则**：
- 领域实体：`XxxEntity`（如 `UserEntity`）- 纯 POJO，无框架注解
- 持久化对象：`XxxPO`（如 `UserPO`）- 包含 MyBatis-Plus 注解
- 仓储接口：`XxxRepository`（如 `UserRepository`）
- 仓储实现：`XxxRepositoryImpl`（如 `UserRepositoryImpl`）
- Mapper 接口：`XxxMapper`（如 `UserMapper`）
- Mapper XML：`XxxMapper.xml`（如 `UserMapper.xml`）
- 配置类：`XxxConfig`（如 `MybatisPlusConfig`）

**关键原则**：
- ✅ Entity 在 repository-api，作为领域实体，不依赖任何框架
- ✅ PO 在 mysql-impl，作为持久化对象，包含框架注解
- ✅ RepositoryImpl 负责 Entity 和 PO 之间的转换
- ✅ 使用 `mysql` 作为 package 名，明确技术选型（而不是 `sql`）

#### 10.2.4 Application 层

**application-api 模块**：

```
com.demo.ordercore.application/
├── dto/                     # 应用层 DTO
│   ├── UserDTO.java        # ✅ 用于应用服务间传输
│   ├── OrderDTO.java
│   └── CreateOrderDTO.java
└── service/                 # 应用服务接口
    ├── UserAppService.java
    └── OrderAppService.java
```

**application-impl 模块**：

```
com.demo.ordercore.application/
└── service/
    └── impl/
        ├── UserAppServiceImpl.java
        └── OrderAppServiceImpl.java
```

**命名规则**：
- 应用 DTO：`XxxDTO`（如 `UserDTO`）
- 应用服务接口：`XxxAppService` 或 `XxxService`
- 应用服务实现：`XxxAppServiceImpl` 或 `XxxServiceImpl`

#### 10.2.5 Interface 层 - HTTP

```
com.demo.ordercore.http/
├── controller/              # 控制器
│   ├── UserController.java
│   └── OrderController.java
├── vo/                      # 视图对象
│   ├── UserVO.java         # ✅ 用于返回给前端
│   └── OrderVO.java
├── request/                 # 请求对象
│   ├── CreateUserRequest.java  # ✅ 接收前端请求
│   └── UpdateUserRequest.java
├── response/                # 响应对象（可选）
│   └── UserDetailResponse.java
└── handler/                 # 异常处理器
    └── GlobalExceptionHandler.java
```

**命名规则**：
- 控制器：`XxxController`（如 `UserController`）
- 视图对象：`XxxVO`（如 `UserVO`）
- 请求对象：`XxxRequest` 或 `XxxCommand`（如 `CreateUserRequest`）
- 响应对象：`XxxResponse`（如 `UserDetailResponse`）
- 异常处理器：`GlobalExceptionHandler`

#### 10.2.6 Interface 层 - Consumer

```
com.demo.ordercore.consumer/
├── listener/                # 消息监听器
│   ├── OrderCreatedListener.java
│   └── PaymentCompletedListener.java
├── handler/                 # 消息处理器
│   ├── OrderMessageHandler.java
│   └── PaymentMessageHandler.java
└── config/                  # 消费者配置
    └── ConsumerConfig.java
```

**命名规则**：
- 监听器：`XxxListener`（如 `OrderCreatedListener`）
- 处理器：`XxxMessageHandler` 或 `XxxHandler`（如 `OrderMessageHandler`）
- 配置类：`ConsumerConfig`

**职责说明**：
- Listener：监听 MQ 消息，负责消息接收
- Handler：处理业务逻辑，调用 Application Service
- Config：配置消费者参数（队列、Topic、消费组等）

#### 10.2.7 Interface 层 - Job

```
com.demo.ordercore.job/
├── task/                    # 定时任务
│   ├── DataSyncTask.java
│   └── ReportGenerateTask.java
├── handler/                 # 任务处理器
│   ├── DataSyncHandler.java
│   └── ReportGenerateHandler.java
└── config/                  # 任务配置
    └── JobConfig.java
```

**命名规则**：
- 任务类：`XxxTask` 或 `XxxJob`（如 `DataSyncTask`）
- 处理器：`XxxHandler`（如 `DataSyncHandler`）
- 配置类：`JobConfig`

**职责说明**：
- Task：定时任务入口，使用 @Scheduled 注解
- Handler：处理业务逻辑，调用 Application Service
- Config：配置任务参数（执行时间、线程池等）

#### 10.2.8 Bootstrap 模块

```
com.demo.ordercore.bootstrap/
├── OrderCoreApplication.java    # 启动类
└── config/                      # 全局配置（可选）
    └── WebConfig.java
```

**命名规则**：
- 启动类：`{ProjectName}Application`（如 `OrderCoreApplication`）
- 配置类：`XxxConfig`（如 `WebConfig`）

### 10.3 各层数据对象命名规范总结

| 层次 | 对象类型 | 命名规则 | 示例 | 说明 |
|------|---------|---------|------|------|
| **Domain** | 领域实体 | 名词（无后缀） | `User`, `Order` | 纯业务概念，使用业务语言 |
| **Domain** | 值对象 | 名词（无后缀） | `Address`, `Money` | 不可变对象 |
| **Infrastructure** | 领域实体 | `XxxEntity` | `UserEntity` | Repository 接口使用，纯 POJO |
| **Infrastructure** | 持久化对象 | `XxxPO` | `UserPO` | 数据库映射，包含框架注解 |
| **Application** | 数据传输对象 | `XxxDTO` | `UserDTO` | 应用层传输，面向用例 |
| **Interface** | 视图对象 | `XxxVO` | `UserVO` | 前端展示 |
| **Interface** | 请求对象 | `XxxRequest` | `CreateUserRequest` | 接收输入 |
| **Interface** | 响应对象 | `XxxResponse` | `UserDetailResponse` | 返回结果 |
| **Interface** | 控制器 | `XxxController` | `UserController` | HTTP 接口 |
| **Interface** | 消息监听器 | `XxxListener` | `OrderCreatedListener` | MQ 消息监听 |
| **Interface** | 消息处理器 | `XxxMessageHandler` | `OrderMessageHandler` | MQ 消息处理 |
| **Interface** | 定时任务 | `XxxTask` | `DataSyncTask` | 定时任务 |
| **Interface** | 任务处理器 | `XxxHandler` | `DataSyncHandler` | 任务处理 |

### 10.4 完整的数据流转示例

#### 写操作流程（创建用户）

```
前端
  ↓ CreateUserRequest
Controller (HTTP 层 - http-impl)
  ↓ 转换为 UserDTO
Application Service (应用层 - application-impl)
  ↓ 转换为 User (领域对象)
Domain Service (领域层 - domain-impl)
  ↓ 返回 User
Application Service
  ↓ 调用 Repository.save(UserEntity, operator)
Repository Impl (仓储层 - mysql-impl)
  ↓ 转换为 UserPO，设置 operator
Mapper (持久化层)
  ↓ INSERT SQL
数据库
```

#### 读操作流程（查询用户）

```
前端
  ↓ GET /users/{id}
Controller (HTTP 层)
  ↓ 调用 Application Service
Application Service (应用层)
  ↓ 调用 Repository.findById(id)
Repository Impl (仓储层)
  ↓ 调用 Mapper.selectById(id)
Mapper
  ↓ SELECT SQL
数据库
  ↓ 返回 UserPO
Repository Impl
  ↓ 转换为 UserEntity
Application Service
  ↓ 转换为 UserDTO
Controller
  ↓ 转换为 UserVO
前端
```

**关键转换点**：
1. **HTTP → Application**：Request/VO → DTO
2. **Application → Domain**：DTO → Domain Entity (User)
3. **Application → Repository**：Domain Entity → Repository Entity (UserEntity)
4. **Repository → Mapper**：Repository Entity → PO (UserPO)

**注意**：
- Domain 层的 `User` 是纯业务概念，不依赖任何框架
- Repository 层的 `UserEntity` 是用于数据访问的领域实体
- Mapper 层的 `UserPO` 是数据库映射对象，包含框架注解

### 10.5 Entity/PO 分离架构规范

#### 为什么需要 Entity/PO 分离

1. **框架无关**：领域实体不依赖任何持久化框架
2. **易于测试**：纯 POJO 易于单元测试
3. **易于替换**：可以轻松切换持久化实现（JPA、MongoDB 等）
4. **符合 DDD**：Entity 是领域概念，PO 是技术实现细节

#### Entity 和 PO 的区别

| 特性 | Entity（领域实体） | PO（持久化对象） |
|------|------------------|-----------------|
| **位置** | repository-api 模块 | mysql-impl 模块 |
| **Package** | `com.demo.ordercore.infrastructure.repository.entity` | `com.demo.ordercore.infrastructure.repository.mysql.po` |
| **注解** | 无框架注解 | 包含 MyBatis-Plus 注解 |
| **职责** | 表示业务概念 | 映射数据库表 |
| **依赖** | 不依赖任何框架 | 依赖 MyBatis-Plus |
| **使用场景** | 业务层、应用层 | 仅在 Repository 实现中使用 |

#### 转换规范

**在 RepositoryImpl 中进行转换**：

```java
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    @Autowired
    private UserMapper userMapper;
    
    // Entity -> PO
    private UserPO toPO(UserEntity entity) {
        if (entity == null) return null;
        UserPO po = new UserPO();
        BeanUtils.copyProperties(entity, po);
        return po;
    }
    
    // PO -> Entity
    private UserEntity toEntity(UserPO po) {
        if (po == null) return null;
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(po, entity);
        return entity;
    }
    
    // 批量转换
    private List<UserEntity> toEntityList(List<UserPO> poList) {
        return poList.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
    
    // 分页转换
    private IPage<UserEntity> toEntityPage(IPage<UserPO> poPage) {
        IPage<UserEntity> entityPage = new Page<>();
        BeanUtils.copyProperties(poPage, entityPage, "records");
        entityPage.setRecords(toEntityList(poPage.getRecords()));
        return entityPage;
    }
    
    @Override
    public void save(UserEntity entity, String operator) {
        UserPO po = toPO(entity);
        po.setCreateBy(operator);
        po.setUpdateBy(operator);
        userMapper.insert(po);
    }
    
    @Override
    public UserEntity findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return toEntity(po);
    }
}
```

### 10.6 MyBatis-Plus 配置规范

#### 配置类位置

**原则**：配置内聚，MybatisPlusConfig 应该放在 mysql-impl 模块

**位置**：`infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/MybatisPlusConfig.java`

**理由**：
- 配置与实现在一起，便于维护
- Spring Boot 会自动扫描到此配置类（scanBasePackages = "com.demo"）
- 符合模块内聚原则

#### Mapper 扫描路径

**@MapperScan 注解**：
```java
@Configuration
@MapperScan("com.demo.ordercore.infrastructure.repository.mysql.mapper")
public class MybatisPlusConfig {
    // ...
}
```

**验证要点**：
- Mapper 扫描路径与 Mapper 接口的实际 package 一致
- 使用 `mysql` 而不是 `sql`，明确技术选型

#### 类型别名配置

**application.yml 配置**：
```yaml
mybatis-plus:
  type-aliases-package: com.demo.ordercore.infrastructure.repository.mysql.po
```

**注意**：
- 配置的是 PO 类路径，不是 Entity 类路径
- MyBatis-Plus 直接操作 PO，而不是 Entity

#### Mapper XML 配置

**application.yml 配置**：
```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
```

**XML namespace**：必须与 Mapper 接口的全限定名一致
```xml
<mapper namespace="com.demo.ordercore.infrastructure.repository.mysql.mapper.UserMapper">
```

**resultMap type**：必须与 PO 类的全限定名一致
```xml
<resultMap id="BaseResultMap" type="com.demo.ordercore.infrastructure.repository.mysql.po.UserPO">
```

### 10.7 文件命名规范

#### Java 类命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| **领域实体** | 名词 | `User`, `Order` |
| **领域实体（Repository）** | `XxxEntity` | `UserEntity`, `OrderEntity` |
| **持久化对象** | `XxxPO` | `UserPO`, `OrderPO` |
| **DTO** | `XxxDTO` | `UserDTO`, `OrderDTO` |
| **VO** | `XxxVO` | `UserVO`, `OrderVO` |
| **请求对象** | `XxxRequest` | `CreateUserRequest` |
| **响应对象** | `XxxResponse` | `UserDetailResponse` |
| **接口** | `Xxx` | `UserRepository`, `UserService` |
| **实现类** | `XxxImpl` | `UserRepositoryImpl`, `UserServiceImpl` |
| **控制器** | `XxxController` | `UserController` |
| **服务类** | `XxxService` | `UserService`, `UserAppService` |
| **Mapper** | `XxxMapper` | `UserMapper` |
| **控制器** | `XxxController` | `UserController` |
| **消息监听器** | `XxxListener` | `OrderCreatedListener` |
| **消息处理器** | `XxxMessageHandler` | `OrderMessageHandler` |
| **定时任务** | `XxxTask` 或 `XxxJob` | `DataSyncTask` |
| **任务处理器** | `XxxHandler` | `DataSyncHandler` |
| **配置类** | `XxxConfig` | `MybatisPlusConfig` |
| **异常类** | `XxxException` | `BusinessException` |
| **工具类** | `XxxUtil` | `JsonUtil`, `DateUtil` |
| **常量类** | `XxxConstant` | `ErrorCode`, `SystemConstant` |

#### 配置文件命名

| 文件类型 | 命名规则 | 示例 |
|---------|---------|------|
| **应用配置** | `application-{profile}.yml` | `application-local.yml` |
| **Mapper XML** | `XxxMapper.xml` | `UserMapper.xml` |
| **数据库脚本** | `schema.sql`, `data.sql` | `schema.sql` |
| **日志配置** | `logback-spring.xml` | `logback-spring.xml` |

#### 目录命名

| 目录类型 | 命名规则 | 说明 |
|---------|---------|------|
| **模块目录** | 小写，连字符分隔 | `domain-api`, `mysql-impl` |
| **Package 目录** | 小写，单词连接 | `controller`, `service`, `repository` |
| **资源目录** | 小写 | `mapper`, `static`, `templates` |

### 10.8 常见错误示例

#### 错误 1：Package 路径错误

❌ **错误**：使用 `sql` 而不是 `mysql`
```java
com.demo.ordercore.infrastructure.repository.sql.mapper
```

✅ **正确**：使用 `mysql` 明确技术选型
```java
com.demo.ordercore.infrastructure.repository.mysql.mapper
```

#### 错误 2：Entity/PO 位置错误

❌ **错误**：Entity 和 PO 都在 mysql-impl
```
mysql-impl/
└── entity/
    └── UserEntity.java  // 错误：包含框架注解
```

✅ **正确**：Entity 在 repository-api，PO 在 mysql-impl
```
repository-api/
└── entity/
    └── UserEntity.java  // 纯 POJO

mysql-impl/
└── po/
    └── UserPO.java      // 包含注解
```

#### 错误 3：配置类位置错误

❌ **错误**：MybatisPlusConfig 在 bootstrap 模块
```
bootstrap/
└── config/
    └── MybatisPlusConfig.java  // 错误位置
```

✅ **正确**：MybatisPlusConfig 在 mysql-impl 模块
```
mysql-impl/
└── config/
    └── MybatisPlusConfig.java  // 正确位置
```

#### 错误 4：type-aliases-package 配置错误

❌ **错误**：配置 Entity 路径
```yaml
mybatis-plus:
  type-aliases-package: com.demo.ordercore.infrastructure.repository.entity
```

✅ **正确**：配置 PO 路径
```yaml
mybatis-plus:
  type-aliases-package: com.demo.ordercore.infrastructure.repository.mysql.po
```

#### 错误 5：Domain 层实体命名错误

❌ **错误**：Domain 层使用技术后缀
```java
// domain-api 模块
public class UserEntity {  // 错误：不应该有技术后缀
}
```

✅ **正确**：Domain 层使用纯业务语言
```java
// domain-api 模块
public class User {  // 正确：纯业务概念
}
```

### 10.9 检查清单

#### 模块结构检查

- [ ] 模块目录结构符合 DDD 分层架构
- [ ] 父模块和子模块关系正确
- [ ] 模块命名使用小写和连字符

#### Package 结构检查

- [ ] Package 命名符合规范
- [ ] Entity 在 repository-api 模块（纯 POJO）
- [ ] PO 在 mysql-impl 模块（包含注解）
- [ ] 配置类在 mysql-impl 模块
- [ ] Domain 层实体不使用技术后缀

#### 类命名检查

- [ ] 类命名符合规范（Entity、PO、DTO、VO 等）
- [ ] Domain 层实体使用纯业务语言（无后缀）
- [ ] Mapper 接口和 XML 文件命名一致
- [ ] 配置类命名以 Config 结尾

#### MyBatis-Plus 配置检查

- [ ] Mapper 扫描路径正确（使用 mysql 而不是 sql）
- [ ] type-aliases-package 指向 PO 类
- [ ] Mapper XML namespace 与接口一致
- [ ] resultMap type 与 PO 类一致

#### 依赖关系检查

- [ ] 模块依赖方向正确
- [ ] 无循环依赖
- [ ] API 模块不依赖实现模块

## 十一、关键原则总结

### DDD 架构原则

1. **严格分层**：遵循 DDD 分层原则，避免跨层依赖
2. **单一职责**：保持模块职责单一，避免模块过于庞大
3. **依赖方向**：依赖关系应该从外层指向内层，领域层不依赖基础设施层
4. **业务语言**：Domain 层使用纯粹的业务语言（Ubiquitous Language），不使用技术后缀

### Maven 管理原则

1. **统一版本管理**：使用父 POM 管理所有依赖版本
2. **渐进式声明**：只声明已创建的模块，避免预先声明
3. **持续验证**：每次修改 POM 后都要验证编译

### 命名规范原则

1. **Domain 层**：使用纯业务概念，不加技术后缀（`User` 而不是 `UserEntity`）
2. **Infrastructure 层**：明确区分 Entity（领域实体）和 PO（持久化对象）
3. **Application 层**：使用 DTO 后缀，面向用例设计
4. **Interface 层**：使用 VO/Request/Response 后缀，面向前端设计
5. **Package 命名**：使用具体技术名称（`mysql` 而不是 `sql`），明确技术选型

### Entity/PO 分离原则

1. **Entity 在 repository-api**：纯 POJO，不依赖任何框架
2. **PO 在 mysql-impl**：包含框架注解，仅在 Repository 实现中使用
3. **RepositoryImpl 负责转换**：Entity ↔ PO 转换在 Repository 实现中完成
4. **配置内聚**：MybatisPlusConfig 放在 mysql-impl 模块，与实现在一起

### 验证原则

1. **优先级顺序**：运行时验证 > 构建验证 > 静态检查
2. **及时验证**：每个任务完成后立即验证
3. **完整验证**：确保所有验收标准都通过

### 代码质量原则

1. **编码规范**：遵循统一的代码风格和命名规范
2. **错误处理**：实现统一的异常处理机制
3. **可观测性**：配置日志、监控、链路追踪

## 关键收益

遵循这些最佳实践，可以获得：

- ✅ 清晰的模块边界和职责划分
- ✅ 统一的依赖版本管理
- ✅ 符合 DDD 分层架构原则
- ✅ 框架无关的领域层，易于测试和维护
- ✅ 清晰的命名规范，代码可读性高
- ✅ Entity/PO 分离，易于替换持久化实现
- ✅ 便于持续集成和自动化测试
- ✅ 支持增量开发和迭代交付
- ✅ 降低模块间耦合度
- ✅ 提高代码质量和可维护性

---

**最后更新**：2024-11-11
