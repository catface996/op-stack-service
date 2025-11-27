# AIOps Service - 项目结构说明

## 项目概览

AIOps Service 采用 Maven 多模块项目结构，严格遵循 DDD（领域驱动设计）分层架构和六边形架构原则。

## 目录结构

```
aiops-service/
├── pom.xml                           # 父 POM，统一管理依赖版本
├── README.md                         # 项目说明文档
├── ARCHITECTURE_GUIDELINES.md        # 架构设计指南
├── product.md                        # 产品说明文档
├── tech.md                           # 技术栈说明文档
├── structure.md                      # 项目结构说明文档（本文档）
├── verify.sh                         # 生产就绪验证脚本
│
├── common/                           # 通用模块
│   ├── pom.xml
│   ├── src/
│   │   └── main/java/.../common/
│   │       ├── exception/            # 异常定义
│   │       │   ├── BusinessException.java
│   │       │   ├── SystemException.java
│   │       │   └── ErrorCode.java
│   │       ├── result/               # 统一响应对象
│   │       │   └── Result.java
│   │       └── util/                 # 工具类
│   └── target/
│
├── bootstrap/                        # 启动模块
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/.../bootstrap/
│   │   │   │   └── AiopsApplication.java  # 启动类
│   │   │   └── resources/
│   │   │       ├── application.yml        # 主配置文件
│   │   │       ├── application-local.yml  # 本地环境配置
│   │   │       ├── application-dev.yml    # 开发环境配置
│   │   │       ├── application-test.yml   # 测试环境配置
│   │   │       ├── application-staging.yml # 预发布环境配置
│   │   │       ├── application-prod.yml   # 生产环境配置
│   │   │       └── logback-spring.xml     # 日志配置
│   │   └── test/                          # 集成测试
│   │       └── java/.../integration/
│   │           ├── BaseIntegrationTest.java
│   │           └── AuthIntegrationTest.java
│   ├── logs/                         # 日志文件目录
│   └── target/
│       └── bootstrap-1.0.0-SNAPSHOT.jar   # 可执行 JAR
│
├── interface/                        # 接口层（聚合模块）
│   ├── pom.xml
│   ├── interface-http/               # HTTP REST 接口
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../http/
│   │           ├── controller/       # 控制器
│   │           │   └── auth/
│   │           │       └── AuthController.java
│   │           ├── request/          # 请求 DTO
│   │           │   └── auth/
│   │           │       ├── LoginRequest.java
│   │           │       └── RegisterRequest.java
│   │           ├── response/         # 响应 DTO
│   │           │   └── auth/
│   │           │       ├── LoginResponse.java
│   │           │       └── RegisterResponse.java
│   │           └── handler/          # 全局异常处理器
│   │               └── GlobalExceptionHandler.java
│   │
│   └── interface-consumer/           # 消息队列消费者
│       ├── pom.xml
│       └── src/
│           └── main/java/.../consumer/
│               └── agent/
│                   └── AgentTaskConsumer.java
│
├── application/                      # 应用层（聚合模块）
│   ├── pom.xml
│   ├── application-api/              # 应用服务接口
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../application/
│   │           ├── service/          # 应用服务接口
│   │           │   └── auth/
│   │           │       └── AuthApplicationService.java
│   │           └── dto/              # 内部 DTO
│   │               └── auth/
│   │                   ├── LoginCommand.java
│   │                   └── RegisterCommand.java
│   │
│   └── application-impl/             # 应用服务实现
│       ├── pom.xml
│       └── src/
│           ├── main/java/.../application/
│           │   └── service/
│           │       └── auth/
│           │           └── AuthApplicationServiceImpl.java
│           └── test/                 # 单元测试
│               └── java/.../application/
│                   └── service/
│                       └── auth/
│                           └── AuthApplicationServiceImplTest.java
│
├── domain/                           # 领域层（聚合模块）
│   ├── pom.xml
│   ├── domain-api/                   # 领域模型和领域服务接口
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../domain/
│   │           ├── model/            # 领域模型
│   │           │   └── auth/
│   │           │       ├── Account.java        # 聚合根
│   │           │       ├── Session.java        # 实体
│   │           │       └── AccountLockInfo.java # 值对象
│   │           └── service/          # 领域服务接口
│   │               └── auth/
│   │                   └── AuthDomainService.java
│   │
│   ├── domain-impl/                  # 领域服务实现
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/.../domain/
│   │       │   └── service/
│   │       │       └── auth/
│   │       │           └── AuthDomainServiceImpl.java
│   │       └── test/                 # 单元测试
│   │           └── java/.../domain/
│   │               └── service/
│   │                   └── auth/
│   │                       └── AuthDomainServiceImplTest.java
│   │
│   ├── repository-api/               # 仓储接口（Port）
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../repository/
│   │           └── auth/
│   │               ├── AccountRepository.java
│   │               ├── SessionRepository.java
│   │               └── entity/       # Entity（领域对象）
│   │                   ├── Account.java
│   │                   └── Session.java
│   │
│   ├── cache-api/                    # 缓存接口（Port）
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../cache/
│   │           └── auth/
│   │               └── LoginFailureCacheService.java
│   │
│   ├── mq-api/                       # 消息队列接口（Port）
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/.../mq/
│   │           └── auth/
│   │               └── AuthEventMqService.java
│   │
│   └── security-api/                 # 安全接口（Port）
│       ├── pom.xml
│       └── src/
│           └── main/java/.../security/
│               ├── PasswordEncoder.java
│               └── JwtTokenService.java
│
└── infrastructure/                   # 基础设施层（聚合模块）
    ├── pom.xml
    ├── repository/                   # 数据持久化
    │   ├── pom.xml
    │   └── mysql-impl/               # MySQL 实现（Adapter）
    │       ├── pom.xml
    │       └── src/
    │           └── main/
    │               ├── java/.../repository/
    │               │   └── auth/
    │               │       ├── AccountRepositoryImpl.java
    │               │       ├── SessionRepositoryImpl.java
    │               │       ├── mapper/       # MyBatis Mapper
    │               │       │   ├── AccountMapper.java
    │               │       │   └── SessionMapper.java
    │               │       └── po/           # PO（持久化对象）
    │               │           ├── AccountPO.java
    │               │           └── SessionPO.java
    │               └── resources/
    │                   └── mapper/           # MyBatis XML
    │                       ├── AccountMapper.xml
    │                       └── SessionMapper.xml
    │
    ├── cache/                        # 缓存
    │   ├── pom.xml
    │   └── redis-impl/               # Redis 实现（Adapter）
    │       ├── pom.xml
    │       └── src/
    │           └── main/java/.../cache/
    │               ├── auth/
    │               │   └── LoginFailureCacheServiceImpl.java
    │               └── config/
    │                   └── RedisConfig.java
    │
    ├── mq/                           # 消息队列
    │   ├── pom.xml
    │   └── sqs-impl/                 # AWS SQS 实现（Adapter）
    │       ├── pom.xml
    │       └── src/
    │           └── main/java/.../mq/
    │               ├── auth/
    │               │   └── AuthEventMqServiceImpl.java
    │               └── config/
    │                   └── SqsConfig.java
    │
    └── security/                     # 安全
        ├── pom.xml
        └── security-impl/            # 安全实现（Adapter）
            ├── pom.xml
            └── src/
                └── main/java/.../security/
                    ├── PasswordEncoderImpl.java
                    ├── JwtTokenServiceImpl.java
                    └── config/
                        └── SecurityConfig.java
```

## 模块说明

### 1. common（通用模块）

**职责**: 提供项目通用的基础设施代码

**包含内容**:
- 异常定义（BusinessException、SystemException）
- 统一响应对象（Result）
- 工具类（日期、字符串、集合等）
- 常量定义

**依赖关系**: 被所有其他模块依赖

### 2. bootstrap（启动模块）

**职责**: 应用启动和配置管理

**包含内容**:
- 启动类（AiopsApplication）
- 配置文件（application.yml、多环境配置）
- 日志配置（logback-spring.xml）
- 集成测试（BaseIntegrationTest）

**依赖关系**: 依赖所有实现模块（interface、application、domain、infrastructure）

**特点**:
- 唯一的可执行模块
- 打包成可执行 JAR
- 包含所有依赖

### 3. interface（接口层）

**职责**: 处理外部请求和消息

#### 3.1 interface-http

**职责**: 处理 HTTP REST 请求

**包含内容**:
- Controller（控制器）
- Request DTO（请求对象）
- Response DTO（响应对象）
- GlobalExceptionHandler（全局异常处理器）

**依赖关系**: 依赖 application-api

**职责边界**:
- ✅ 接收 HTTP 请求
- ✅ 参数校验
- ✅ 调用 Application Service
- ✅ 返回 HTTP 响应
- ❌ 不包含业务逻辑

#### 3.2 interface-consumer

**职责**: 处理消息队列消息

**包含内容**:
- Consumer（消费者）
- 消息处理逻辑

**依赖关系**: 依赖 application-api

### 4. application（应用层）

**职责**: 用例编排和事务控制

#### 4.1 application-api

**职责**: 定义应用服务接口

**包含内容**:
- 应用服务接口
- 内部 DTO（Command、Query）

**依赖关系**: 无外部依赖（只依赖 common）

#### 4.2 application-impl

**职责**: 实现应用服务

**包含内容**:
- 应用服务实现
- 单元测试

**依赖关系**: 依赖 application-api、domain-api

**职责边界**:
- ✅ 事务边界控制（@Transactional）
- ✅ 编排 Domain Service
- ✅ DTO 转换（Request/Response → Domain Entity）
- ✅ 权限验证
- ✅ 审计日志记录
- ❌ 不包含业务逻辑
- ❌ 不直接调用 Repository/Cache/MQ

### 5. domain（领域层）

**职责**: 核心业务逻辑和领域模型

#### 5.1 domain-api

**职责**: 定义领域模型和领域服务接口

**包含内容**:
- 领域模型（聚合根、实体、值对象）
- 领域服务接口

**依赖关系**: 无外部依赖（只依赖 common）

**领域模型**:
- **聚合根（Aggregate Root）**: 如 Account
- **实体（Entity）**: 如 Session
- **值对象（Value Object）**: 如 AccountLockInfo

#### 5.2 domain-impl

**职责**: 实现领域服务

**包含内容**:
- 领域服务实现
- 单元测试

**依赖关系**: 依赖 domain-api、repository-api、cache-api、mq-api、security-api

**职责边界**:
- ✅ 复杂业务规则
- ✅ 领域对象创建（工厂方法）
- ✅ 密码加密/验证
- ✅ 会话管理逻辑
- ✅ 账号锁定逻辑
- ✅ 调用 Repository 进行数据访问
- ✅ 调用 Cache 进行缓存操作
- ✅ 调用 MQ 发送消息
- ❌ 不控制事务（由 Application Service 控制）
- ❌ 不处理 DTO 转换

#### 5.3 repository-api

**职责**: 定义仓储接口（Port）

**包含内容**:
- Repository 接口
- Entity（领域对象）

**依赖关系**: 依赖 domain-api

**为什么在 Domain 层**:
- Repository 是领域概念，描述如何持久化聚合根
- 遵循依赖倒置原则（DIP）
- 符合六边形架构（Port 在核心层）

#### 5.4 cache-api

**职责**: 定义缓存接口（Port）

**包含内容**:
- Cache Service 接口

**依赖关系**: 依赖 domain-api

#### 5.5 mq-api

**职责**: 定义消息队列接口（Port）

**包含内容**:
- MQ Service 接口

**依赖关系**: 依赖 domain-api

#### 5.6 security-api

**职责**: 定义安全接口（Port）

**包含内容**:
- PasswordEncoder 接口
- JwtTokenService 接口

**依赖关系**: 无外部依赖

### 6. infrastructure（基础设施层）

**职责**: 技术实现和外部系统集成

#### 6.1 mysql-impl

**职责**: 实现 Repository 接口（Adapter）

**包含内容**:
- Repository 实现
- MyBatis Mapper 接口
- MyBatis XML 映射文件
- PO（持久化对象）

**依赖关系**: 依赖 repository-api

**数据对象转换**:
- Entity（领域对象）↔ PO（持久化对象）
- Entity 在 repository-api 中定义
- PO 在 mysql-impl 中定义

#### 6.2 redis-impl

**职责**: 实现 Cache 接口（Adapter）

**包含内容**:
- Cache Service 实现
- Redis 配置

**依赖关系**: 依赖 cache-api

#### 6.3 sqs-impl

**职责**: 实现 MQ 接口（Adapter）

**包含内容**:
- MQ Service 实现
- SQS 配置

**依赖关系**: 依赖 mq-api

#### 6.4 security-impl

**职责**: 实现安全接口（Adapter）

**包含内容**:
- PasswordEncoder 实现（BCrypt）
- JwtTokenService 实现
- Security 配置

**依赖关系**: 依赖 security-api

## 模块总数

- **父 POM**: 1 个
- **聚合模块**: 7 个（common、bootstrap、interface、application、domain、infrastructure、repository）
- **代码模块**: 14 个
- **总计**: 22 个模块

## 依赖关系图

```
bootstrap
  ├─> interface-http
  │     └─> application-api
  ├─> interface-consumer
  │     └─> application-api
  ├─> application-impl
  │     ├─> application-api
  │     └─> domain-api
  ├─> domain-impl
  │     ├─> domain-api
  │     ├─> repository-api
  │     ├─> cache-api
  │     ├─> mq-api
  │     └─> security-api
  ├─> mysql-impl
  │     └─> repository-api
  │           └─> domain-api
  ├─> redis-impl
  │     └─> cache-api
  │           └─> domain-api
  ├─> sqs-impl
  │     └─> mq-api
  │           └─> domain-api
  └─> security-impl
        └─> security-api
```

## 数据对象转换

### 对象类型

| 对象类型 | 位置 | 说明 |
|---------|------|------|
| **Request/Response** | interface-http | HTTP 请求/响应对象 |
| **Command/Query** | application-api | 应用层内部 DTO |
| **Entity** | domain-api | 领域对象（聚合根、实体、值对象） |
| **Entity** | repository-api | 仓储层的领域对象（与 domain-api 一致） |
| **PO** | mysql-impl | 持久化对象（数据库表映射） |

### 转换链路

```
HTTP Request (interface-http)
  ↓ Controller 转换
Command/Query (application-api)
  ↓ Application Service 转换
Entity (domain-api)
  ↓ Domain Service 调用 Repository
Entity (repository-api)
  ↓ Repository 实现转换
PO (mysql-impl)
  ↓ MyBatis 映射
Database
```

## 包命名规范

### 基础包名

```
com.catface996.aiops
```

### 模块包名

```
com.catface996.aiops.common          # 通用模块
com.catface996.aiops.bootstrap       # 启动模块
com.catface996.aiops.http            # HTTP 接口
com.catface996.aiops.consumer        # 消息消费者
com.catface996.aiops.application     # 应用层
com.catface996.aiops.domain          # 领域层
com.catface996.aiops.repository      # 仓储层
com.catface996.aiops.cache           # 缓存层
com.catface996.aiops.mq              # 消息队列层
com.catface996.aiops.security        # 安全层
```

### 业务域包名

```
com.catface996.aiops.domain.auth     # 认证域
com.catface996.aiops.domain.resource # 资源域
com.catface996.aiops.domain.agent    # Agent 域
com.catface996.aiops.domain.llm      # LLM 域
```

## 文件命名规范

### Java 类命名

| 类型 | 命名规范 | 示例 |
|------|---------|------|
| Controller | XxxController | AuthController |
| Application Service | XxxApplicationService | AuthApplicationService |
| Domain Service | XxxDomainService | AuthDomainService |
| Repository | XxxRepository | AccountRepository |
| Cache Service | XxxCacheService | LoginFailureCacheService |
| MQ Service | XxxMqService | AuthEventMqService |
| Entity | 名词 | Account, Session |
| PO | XxxPO | AccountPO, SessionPO |
| Request | XxxRequest | LoginRequest |
| Response | XxxResponse | LoginResponse |
| Command | XxxCommand | LoginCommand |
| Query | XxxQuery | AccountQuery |

### 配置文件命名

| 文件 | 说明 |
|------|------|
| application.yml | 主配置文件 |
| application-{env}.yml | 环境配置文件 |
| logback-spring.xml | 日志配置文件 |
| XxxMapper.xml | MyBatis 映射文件 |

## 测试结构

### 单元测试

```
src/test/java/.../
├── application/
│   └── service/
│       └── auth/
│           └── AuthApplicationServiceImplTest.java
└── domain/
    └── service/
        └── auth/
            └── AuthDomainServiceImplTest.java
```

**特点**:
- 使用 Mockito Mock 依赖
- 快速执行（毫秒级）
- 高隔离性

### 集成测试

```
bootstrap/src/test/java/.../integration/
├── BaseIntegrationTest.java
├── AuthIntegrationTest.java
└── ResourceIntegrationTest.java
```

**特点**:
- 使用 TestContainers 启动临时容器
- 测试完整链路
- 执行较慢（秒级）

### E2E 测试

```
doc/04-testing/e2e/
├── scripts/
│   ├── auth-e2e-test.sh
│   └── resource-e2e-test.sh
└── data/
    └── test-users.json
```

**特点**:
- 使用 Shell 脚本和 curl
- 测试真实环境
- 手动执行或 CI/CD 执行

## 构建和打包

### 编译

```bash
mvn clean compile
```

### 打包

```bash
mvn clean package -DskipTests
```

**输出**:
- `bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar`（可执行 JAR）
- 其他模块的 JAR（作为依赖）

### 运行

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=local
```

## 开发工作流

### 1. 添加新功能

1. 在 `domain-api` 中定义领域模型和领域服务接口
2. 在 `domain-impl` 中实现领域服务
3. 在 `application-api` 中定义应用服务接口
4. 在 `application-impl` 中实现应用服务
5. 在 `interface-http` 中添加 Controller
6. 编写单元测试和集成测试

### 2. 添加新的数据访问

1. 在 `repository-api` 中定义 Repository 接口和 Entity
2. 在 `mysql-impl` 中实现 Repository
3. 创建 MyBatis Mapper 接口和 XML
4. 在 `domain-impl` 中调用 Repository

### 3. 添加新的缓存

1. 在 `cache-api` 中定义 Cache Service 接口
2. 在 `redis-impl` 中实现 Cache Service
3. 在 `domain-impl` 中调用 Cache Service

### 4. 添加新的消息队列

1. 在 `mq-api` 中定义 MQ Service 接口
2. 在 `sqs-impl` 中实现 MQ Service
3. 在 `domain-impl` 中调用 MQ Service
4. 在 `interface-consumer` 中添加 Consumer

## 常见问题

### Q1: 为什么 Repository 接口在 Domain 层？

**A**: 遵循依赖倒置原则（DIP）和六边形架构。Repository 是领域概念，描述如何持久化聚合根。Domain 层定义接口（Port），Infrastructure 层实现接口（Adapter）。

### Q2: Application Service 和 Domain Service 的区别？

**A**:
- **Application Service**: 用例编排、事务控制、DTO 转换
- **Domain Service**: 核心业务逻辑、领域规则

### Q3: 为什么 Application Service 不能直接调用 Repository？

**A**: 保持职责清晰。Application Service 负责编排，Domain Service 负责业务逻辑和数据访问。这样便于测试和维护。

### Q4: Entity 和 PO 的区别？

**A**:
- **Entity**: 领域对象，包含业务逻辑，在 domain-api 和 repository-api 中定义
- **PO**: 持久化对象，只包含数据，在 mysql-impl 中定义

### Q5: 如何添加新的业务域？

**A**: 按照现有结构，在各层添加对应的包和类：
1. `domain-api` 中添加领域模型和服务接口
2. `domain-impl` 中添加领域服务实现
3. `repository-api` 中添加 Repository 接口
4. `mysql-impl` 中添加 Repository 实现
5. 其他层类似

## 相关文档

- **产品说明**: [product.md](./product.md)
- **技术栈说明**: [tech.md](./tech.md)
- **架构指南**: [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md)
- **快速开始**: [README.md](./README.md)

---

**创建日期**: 2024-11-27  
**最后更新**: 2024-11-27  
**文档版本**: v1.0
