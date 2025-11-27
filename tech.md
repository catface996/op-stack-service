# AIOps Service - 技术栈说明

## 技术栈概览

AIOps Service 采用现代化的 Java 技术栈，基于 Spring Boot 3.x 和 Spring Cloud 2025.x 构建，遵循 DDD（领域驱动设计）分层架构。

## 核心技术栈

### 1. 编程语言和运行时

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 21 (LTS) | 采用最新的 LTS 版本，支持虚拟线程、模式匹配等新特性 |
| **Maven** | 3.8+ | 项目构建和依赖管理工具 |

**选择理由**:
- Java 21 是最新的 LTS 版本，提供长期支持
- 虚拟线程（Virtual Threads）提升并发性能
- 现代化的语言特性提升开发效率

### 2. 应用框架

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.4.1 | 应用框架，提供自动配置和快速开发能力 |
| **Spring Cloud** | 2025.0.0 | 微服务框架，提供服务发现、配置管理等能力 |
| **Spring Web** | 6.2.x | Web 框架，提供 REST API 支持 |
| **Spring Data** | 3.4.x | 数据访问框架 |

**选择理由**:
- Spring Boot 3.x 基于 Spring Framework 6.x，支持 Java 17+
- Spring Cloud 2025.x 提供最新的微服务能力
- 成熟的生态系统和社区支持

### 3. 数据持久化

| 技术 | 版本 | 说明 |
|------|------|------|
| **MySQL** | 8.0+ | 关系型数据库，存储业务数据 |
| **MyBatis-Plus** | 3.5.7 | ORM 框架，简化数据库操作 |
| **Druid** | 1.2.20 | 数据库连接池，提供监控和性能优化 |

**选择理由**:
- MySQL 8.0 提供更好的性能和 JSON 支持
- MyBatis-Plus 提供强大的 CRUD 能力和代码生成
- Druid 提供完善的监控和 SQL 防火墙

**数据库设计原则**:
- 使用 InnoDB 存储引擎
- 合理使用索引优化查询性能
- 敏感数据加密存储
- 支持分库分表（未来扩展）

### 4. 缓存

| 技术 | 版本 | 说明 |
|------|------|------|
| **Redis** | 7.0+ | 内存数据库，用于缓存和会话管理 |
| **Spring Data Redis** | 3.4.x | Redis 客户端封装 |

**选择理由**:
- Redis 7.0 提供更好的性能和新特性
- 支持多种数据结构（String、Hash、List、Set、ZSet）
- 支持发布订阅、Lua 脚本

**使用场景**:
- 用户会话管理
- 登录失败计数和账号锁定
- 热点数据缓存
- 分布式锁
- 消息队列（辅助）

### 5. 消息队列

| 技术 | 版本 | 说明 |
|------|------|------|
| **AWS SQS** | 2.20.0 | 云端消息队列服务 |
| **AWS SDK for Java** | 2.20.0 | AWS 服务 SDK |

**选择理由**:
- 完全托管的消息队列服务
- 高可用、高可靠
- 按使用量付费，成本可控

**使用场景**:
- Agent 任务异步执行
- 事件驱动的任务触发
- 系统间异步通信
- 告警通知发送

### 6. 可观测性

#### 6.1 链路追踪

| 技术 | 版本 | 说明 |
|------|------|------|
| **Micrometer Tracing** | 1.3.5 | 分布式追踪抽象层 |
| **Brave** | 6.0.x | Zipkin 的 Java 客户端 |

**功能**:
- 自动生成 traceId 和 spanId
- 日志自动包含追踪信息
- 支持跨服务追踪

#### 6.2 监控指标

| 技术 | 版本 | 说明 |
|------|------|------|
| **Prometheus** | - | 监控指标收集和存储 |
| **Spring Boot Actuator** | 3.4.x | 应用监控端点 |
| **Micrometer** | 1.13.x | 指标收集抽象层 |

**监控指标**:
- JVM 指标（内存、GC、线程）
- HTTP 请求指标（QPS、延迟、错误率）
- 数据库连接池指标
- 自定义业务指标

#### 6.3 日志

| 技术 | 版本 | 说明 |
|------|------|------|
| **Logback** | 1.5.x | 日志框架 |
| **Logstash Encoder** | 7.4 | JSON 格式日志编码器 |
| **SLF4J** | 2.0.x | 日志门面 |

**日志策略**:
- 本地开发：控制台彩色日志
- 其他环境：JSON 格式文件日志
- 日志级别：项目包 DEBUG/INFO，框架包 WARN
- 日志滚动：按天滚动，保留 30 天

### 7. 安全

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Security** | 6.4.x | 安全框架 |
| **JJWT** | 0.12.6 | JWT 令牌生成和验证 |
| **BCrypt** | - | 密码加密算法 |

**安全机制**:
- JWT 令牌认证
- BCrypt 密码加密
- 账号锁定机制（5 次失败锁定 15 分钟）
- HTTPS 传输加密
- SQL 注入防护
- XSS 防护

### 8. LLM 集成

| 技术 | 版本 | 说明 |
|------|------|------|
| **OpenAI SDK** | - | OpenAI API 客户端 |
| **Anthropic SDK** | - | Claude API 客户端 |

**支持的 LLM**:
- OpenAI GPT-4、GPT-3.5
- Anthropic Claude-3
- 本地部署模型（未来支持）

**功能**:
- 统一的 LLM 调用接口
- 支持流式响应
- 成本控制和统计
- 多服务切换和降级

### 9. 开发工具

| 技术 | 版本 | 说明 |
|------|------|------|
| **Lombok** | 1.18.36 | 简化 Java 代码 |
| **MapStruct** | - | 对象映射工具（可选） |
| **Validation** | 3.1.x | 参数校验 |

### 10. 测试

| 技术 | 版本 | 说明 |
|------|------|------|
| **JUnit 5** | 5.11.x | 单元测试框架 |
| **Mockito** | 5.14.x | Mock 框架 |
| **Spring Boot Test** | 3.4.1 | Spring Boot 测试支持 |
| **TestContainers** | 1.20.x | 容器化集成测试 |
| **JaCoCo** | 0.8.12 | 代码覆盖率工具 |

**测试策略**:
- 单元测试：70%（快速、隔离）
- 集成测试：20%（TestContainers）
- E2E 测试：10%（Shell 脚本）

## 架构设计

### DDD 分层架构

```
┌─────────────────────────────────────────────────────────┐
│                    接口层 (Interface)                    │
│  - interface-http: REST API 接口                        │
│  - interface-consumer: 消息队列消费者                   │
└────────────────────┬────────────────────────────────────┘
                     │ 依赖
┌────────────────────┴────────────────────────────────────┐
│                   应用层 (Application)                   │
│  - application-api: 应用服务接口                        │
│  - application-impl: 应用服务实现                       │
│  职责: 用例编排、事务控制、DTO 转换                     │
└────────────────────┬────────────────────────────────────┘
                     │ 依赖
┌────────────────────┴────────────────────────────────────┐
│                    领域层 (Domain)                       │
│  - domain-api: 领域模型和领域服务接口                   │
│  - domain-impl: 领域服务实现                            │
│  - repository-api: 仓储接口（Port）                     │
│  - cache-api: 缓存接口（Port）                          │
│  - mq-api: 消息队列接口（Port）                         │
│  职责: 核心业务逻辑、领域规则                           │
└────────────────────┬────────────────────────────────────┘
                     │ 实现
┌────────────────────┴────────────────────────────────────┐
│               基础设施层 (Infrastructure)                │
│  - mysql-impl: MySQL 实现（Adapter）                    │
│  - redis-impl: Redis 实现（Adapter）                    │
│  - sqs-impl: AWS SQS 实现（Adapter）                    │
│  职责: 技术实现、外部系统集成                           │
└─────────────────────────────────────────────────────────┘
```

### 依赖规则

1. ✅ **Application Service 只能调用 Domain Service**
2. ✅ **Domain Service 是数据访问的唯一入口**
3. ✅ **Repository/Cache/MQ 接口定义在 Domain 层**
4. ✅ **Infrastructure 层实现 Domain 层定义的接口**
5. ❌ **Application Service 禁止直接调用 Repository/Cache/MQ**

### 模块依赖关系

```
bootstrap (启动模块)
  ├─> interface-http
  ├─> interface-consumer
  ├─> application-impl
  ├─> domain-impl
  ├─> mysql-impl
  ├─> redis-impl
  └─> sqs-impl

interface-http
  └─> application-api

application-impl
  └─> domain-api

domain-impl
  ├─> domain-api
  ├─> repository-api
  ├─> cache-api
  └─> mq-api

mysql-impl
  └─> repository-api

redis-impl
  └─> cache-api

sqs-impl
  └─> mq-api
```

## 性能指标

### 应用性能

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 编译时间 | < 3 秒 | 快速编译，提升开发效率 |
| 启动时间 | < 3 秒 | 快速启动，支持快速迭代 |
| JAR 文件大小 | ~54MB | 包含所有依赖的可执行 JAR |

### 接口性能

| 接口类型 | 响应时间 | 说明 |
|---------|---------|------|
| 查询接口 | < 100ms | 简单查询 |
| 列表接口 | < 500ms | 带分页和过滤 |
| 创建/更新接口 | < 200ms | 数据写入 |
| 复杂查询 | < 1s | 多表关联查询 |

### 系统容量

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 并发用户数 | 1000+ | 同时在线用户 |
| QPS | 5000+ | 每秒请求数 |
| 资源数量 | 10000+ | 支持管理的资源数量 |
| 数据库连接池 | 20-50 | 根据负载动态调整 |

## 环境配置

### 支持的环境

| 环境 | 说明 | 配置文件 |
|------|------|---------|
| **local** | 本地开发环境 | application-local.yml |
| **dev** | 开发环境 | application-dev.yml |
| **test** | 测试环境 | application-test.yml |
| **staging** | 预发布环境 | application-staging.yml |
| **prod** | 生产环境 | application-prod.yml |

### 环境差异

| 配置项 | local | dev/test | staging/prod |
|--------|-------|----------|--------------|
| 日志格式 | 控制台彩色 | JSON 文件 | JSON 文件 |
| 日志级别 | DEBUG | INFO | WARN |
| 数据库 | 本地 MySQL | 云端 RDS | 云端 RDS（主从） |
| Redis | 本地 Redis | 云端 ElastiCache | 云端 ElastiCache（集群） |
| 监控 | 关闭 | 开启 | 开启 |

## 部署方式

### 1. 单体部署（推荐用于 MVP）

```bash
# 构建
mvn clean package -DskipTests

# 运行
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

**适用场景**:
- 小规模团队（< 100 资源）
- 快速验证和迭代
- 降低运维复杂度

### 2. 容器化部署

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建镜像
docker build -t aiops-service:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  aiops-service:1.0.0
```

### 3. Kubernetes 部署（未来）

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aiops-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: aiops-service
  template:
    metadata:
      labels:
        app: aiops-service
    spec:
      containers:
      - name: aiops-service
        image: aiops-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## 技术债务和未来优化

### 短期优化（1-3 个月）

- [ ] 引入 API 网关（Spring Cloud Gateway）
- [ ] 实现服务注册和发现（Nacos/Consul）
- [ ] 引入配置中心（Nacos/Apollo）
- [ ] 实现分布式事务（Seata）

### 中期优化（3-6 个月）

- [ ] 微服务拆分（按业务域拆分）
- [ ] 引入服务网格（Istio）
- [ ] 实现读写分离和分库分表
- [ ] 引入 Elasticsearch 支持全文搜索

### 长期优化（6-12 个月）

- [ ] 支持多云部署
- [ ] 引入 Serverless 架构
- [ ] 实现智能弹性伸缩
- [ ] 引入 AI 模型训练平台

## 技术选型原则

1. **成熟稳定**: 优先选择成熟、稳定、社区活跃的技术
2. **性能优先**: 在满足功能的前提下，优先考虑性能
3. **易于维护**: 选择易于理解、易于维护的技术
4. **生态完善**: 优先选择生态完善、文档齐全的技术
5. **成本可控**: 考虑技术的学习成本和运维成本

## 相关文档

- **产品说明**: [product.md](./product.md)
- **项目结构说明**: [structure.md](./structure.md)
- **架构指南**: [ARCHITECTURE_GUIDELINES.md](./ARCHITECTURE_GUIDELINES.md)
- **快速开始**: [README.md](./README.md)

---

**创建日期**: 2024-11-27  
**最后更新**: 2024-11-27  
**文档版本**: v1.0
