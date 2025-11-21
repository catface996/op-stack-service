# 需求文档

## 引言

本项目旨在构建一个基于 Spring Cloud 最新稳定版本的多模块 Maven 工程，采用领域驱动设计（DDD）思想。项目将实现清晰的层次划分与模块边界、技术与业务逻辑的解耦、可扩展可演进可替换的系统结构，以及对 Spring Cloud 微服务生态的良好支持。

## 术语表

- **System**：指整个多模块 DDD 架构工程系统
- **Parent POM**：Maven 父工程配置文件，用于管理依赖版本和聚合子模块
- **Aggregation Module**：POM 聚合模块，用于聚合子模块，体现 DDD 分层结构
- **Code Module**：包含实际代码的 JAR 模块
- **DDD**：领域驱动设计（Domain-Driven Design）
- **EARS**：Easy Approach to Requirements Syntax，需求语法规范
- **BOM**：Bill of Materials，依赖清单
- **Trace ID**：分布式链路追踪标识符
- **Span ID**：调用链片段标识符
- **MDC**：Mapped Diagnostic Context，日志上下文传递机制
- **Profile**：Spring 环境配置标识
- **GroupId**：Maven 项目组标识符，本项目使用 com.catface996.aiops
- **Project Package**：项目代码包，指 com.catface996.aiops 及其子包

## 需求

### 需求 1：项目结构初始化

**用户故事**：作为开发人员，我希望创建符合 DDD 分层架构的 Maven 多模块项目结构，以便实现清晰的层次划分和模块边界。

#### 验收标准

1. WHEN 创建新项目 THEN THE System SHALL 创建一个父 POM 文件，其 packaging 为 "pom"，groupId 为 "com.catface996.aiops"
2. WHEN 创建父 POM THEN THE System SHALL 在 modules 节中聚合 common、interface、application、domain、infrastructure、bootstrap 六个顶层模块
3. WHEN 创建 interface 聚合模块 THEN THE System SHALL 创建 interface-http 和 interface-consumer 两个子模块
4. WHEN 创建 application 聚合模块 THEN THE System SHALL 创建 application-api 和 application-impl 两个子模块
5. WHEN 创建 domain 聚合模块 THEN THE System SHALL 创建 domain-api 和 domain-impl 两个子模块
6. WHEN 创建 infrastructure 聚合模块 THEN THE System SHALL 创建 repository、cache、mq 三个子聚合模块
7. WHEN 创建 repository 聚合模块 THEN THE System SHALL 创建 repository-api 和 mysql-impl 两个子模块
8. WHEN 创建 cache 聚合模块 THEN THE System SHALL 创建 cache-api 和 redis-impl 两个子模块
9. WHEN 创建 mq 聚合模块 THEN THE System SHALL 创建 mq-api 和 sqs-impl 两个子模块
10. WHEN 创建聚合模块 THEN THE System SHALL 设置其 packaging 为 "pom"
11. WHEN 创建代码模块 THEN THE System SHALL 设置其 packaging 为 "jar"

### 需求 2：依赖版本统一管理

**用户故事**：作为开发人员，我希望在父 POM 中统一管理所有依赖的版本号，以便确保整个项目使用一致的依赖版本。

#### 验收标准

1. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中导入 spring-boot-dependencies BOM
2. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中导入 spring-cloud-dependencies BOM，版本为 2025.0.0
3. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中声明 MyBatis-Plus 版本为 3.5.7
4. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中声明 Druid 版本为 1.2.20
5. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中声明 Micrometer Tracing 版本为 1.3.5
6. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中声明 Logstash Logback Encoder 版本为 7.4
7. WHEN 配置父 POM THEN THE System SHALL 在 dependencyManagement 节中声明 AWS SDK for SQS 版本为 2.20.0
8. WHEN 子模块声明依赖 THEN THE System SHALL 仅指定 groupId 和 artifactId，不指定 version

### 需求 3：模块命名规范

**用户故事**：作为开发人员，我希望所有 Maven 模块使用规范的命名格式，以便构建日志输出清晰易读。

#### 验收标准

1. WHEN 创建任何 Maven 模块 THEN THE System SHALL 在 name 标签中使用首字母大写的英文单词
2. WHEN 模块名称包含多个单词 THEN THE System SHALL 使用空格分隔单词

### 需求 4：模块依赖关系配置

**用户故事**：作为开发人员，我希望正确配置模块间的依赖关系，以便遵循 DDD 分层原则和单向依赖规则。

#### 验收标准

1. WHEN 配置 bootstrap 模块依赖 THEN THE System SHALL 添加对 interface-http、interface-consumer、application-impl、domain-impl、mysql-impl、redis-impl、sqs-impl、common 的依赖
2. WHEN 配置 interface-http 模块依赖 THEN THE System SHALL 添加对 application-api 和 common 的依赖
3. WHEN 配置 interface-consumer 模块依赖 THEN THE System SHALL 添加对 application-api 和 common 的依赖
4. WHEN 配置 application-impl 模块依赖 THEN THE System SHALL 添加对 application-api、domain-api、common 的依赖
5. WHEN 配置 domain-impl 模块依赖 THEN THE System SHALL 添加对 domain-api、repository-api、cache-api、mq-api、common 的依赖
6. WHEN 配置 mysql-impl 模块依赖 THEN THE System SHALL 添加对 repository-api 和 common 的依赖
7. WHEN 配置 redis-impl 模块依赖 THEN THE System SHALL 添加对 cache-api 和 common 的依赖
8. WHEN 配置 sqs-impl 模块依赖 THEN THE System SHALL 添加对 mq-api 和 common 的依赖

### 需求 5：技术栈集成

**用户故事**：作为开发人员，我希望集成项目所需的技术栈依赖，以便实现业务功能和技术能力。

#### 验收标准

1. WHEN 配置 bootstrap 模块 THEN THE System SHALL 添加 Spring Boot Starter Web 依赖
2. WHEN 配置 mysql-impl 模块 THEN THE System SHALL 添加 MyBatis-Plus Spring Boot 3 Starter 依赖
3. WHEN 配置 mysql-impl 模块 THEN THE System SHALL 添加 Druid Spring Boot 3 Starter 依赖
4. WHEN 配置 mysql-impl 模块 THEN THE System SHALL 添加 MySQL Connector/J 依赖
5. WHEN 配置 redis-impl 模块 THEN THE System SHALL 添加 Spring Boot Starter Data Redis 依赖
6. WHEN 配置 sqs-impl 模块 THEN THE System SHALL 添加 AWS SDK for SQS 依赖
7. WHEN 配置 bootstrap 模块 THEN THE System SHALL 添加 Micrometer Tracing 依赖
8. WHEN 配置 bootstrap 模块 THEN THE System SHALL 添加 Logstash Logback Encoder 依赖
9. WHEN 配置任何代码模块 THEN THE System SHALL 添加 Lombok 依赖，scope 为 provided
10. WHEN 配置任何代码模块 THEN THE System SHALL 添加 Spring Boot Starter Test 依赖，scope 为 test

### 需求 6：分布式链路追踪

**用户故事**：作为开发人员，我希望实现跨模块、跨请求的 Trace ID 自动生成与传播，以便追踪请求链路。

#### 验收标准

1. WHEN 系统接收 HTTP 请求 THEN THE System SHALL 自动生成 Trace ID
2. WHEN 系统接收 HTTP 请求 THEN THE System SHALL 自动生成 Span ID
3. WHEN 请求在模块间传播 THEN THE System SHALL 通过 MDC 传递 Trace ID 和 Span ID
4. WHEN 输出日志 THEN THE System SHALL 在日志中包含 traceId 字段
5. WHEN 输出日志 THEN THE System SHALL 在日志中包含 spanId 字段

### 需求 7：结构化日志输出

**用户故事**：作为运维人员，我希望系统输出结构化的 JSON 格式日志，以便日志收集系统解析和索引。

#### 验收标准

1. WHEN 系统输出日志 THEN THE System SHALL 在非 local 环境使用 JSON 格式
2. WHEN 输出 JSON 日志 THEN THE System SHALL 包含 timestamp 字段
3. WHEN 输出 JSON 日志 THEN THE System SHALL 包含 level 字段
4. WHEN 输出 JSON 日志 THEN THE System SHALL 包含 thread 字段
5. WHEN 输出 JSON 日志 THEN THE System SHALL 包含 logger 字段
6. WHEN 输出 JSON 日志 THEN THE System SHALL 包含 message 字段
7. WHEN 发生异常 THEN THE System SHALL 在日志中包含 exception 字段

### 需求 8：多环境日志配置

**用户故事**：作为运维人员，我希望系统根据不同环境采用差异化的日志配置，以便满足不同场景的需求。

#### 验收标准

1. WHEN Profile 为 local THEN THE System SHALL 输出日志到控制台
2. WHEN Profile 为 local THEN THE System SHALL 使用默认格式输出日志
3. WHEN Profile 为 dev、test、staging、prod THEN THE System SHALL 输出日志到文件
4. WHEN Profile 为 dev、test、staging、prod THEN THE System SHALL 使用 JSON 格式输出日志
5. WHEN Profile 为 local、dev、test THEN THE System SHALL 设置项目包（com.catface996.aiops）日志级别为 DEBUG
6. WHEN Profile 为 staging、prod THEN THE System SHALL 设置项目包（com.catface996.aiops）日志级别为 INFO
7. WHEN 任何 Profile THEN THE System SHALL 设置框架包（org.springframework、com.baomidou、com.amazonaws 等）日志级别为 WARN
8. WHEN 输出日志到文件 THEN THE System SHALL 按日期滚动日志文件
9. WHEN 日志文件超过 100MB THEN THE System SHALL 自动分割日志文件
10. WHEN Profile 为 dev、test、staging THEN THE System SHALL 保留最近 30 天的日志
11. WHEN Profile 为 prod THEN THE System SHALL 保留最近 90 天的日志
12. WHEN Profile 为 prod THEN THE System SHALL 使用异步 Appender 输出日志

### 需求 9：日志配置管理

**用户故事**：作为开发人员，我希望所有日志相关配置在 logback-spring.xml 中统一管理，以便降低维护成本。

#### 验收标准

1. WHEN 配置日志 THEN THE System SHALL 在 logback-spring.xml 中配置日志级别
2. WHEN 配置日志 THEN THE System SHALL 在 logback-spring.xml 中配置输出格式
3. WHEN 配置日志 THEN THE System SHALL 在 logback-spring.xml 中配置文件路径
4. WHEN 配置日志 THEN THE System SHALL 在 logback-spring.xml 中配置滚动策略
5. WHEN 配置多环境日志 THEN THE System SHALL 使用 springProfile 标签区分环境

### 需求 10：异常处理体系

**用户故事**：作为开发人员，我希望建立统一的异常处理机制，以便规范化错误传播和处理流程。

#### 验收标准

1. WHEN 创建 common 模块 THEN THE System SHALL 定义异常基类
2. WHEN 创建 common 模块 THEN THE System SHALL 定义 BusinessException 类
3. WHEN 创建 common 模块 THEN THE System SHALL 定义 SystemException 类
4. WHEN 创建 interface-http 模块 THEN THE System SHALL 实现全局异常处理器
5. WHEN HTTP 接口抛出异常 THEN THE System SHALL 捕获异常并转换为统一的 Result 响应
6. WHEN 创建 interface-consumer 模块 THEN THE System SHALL 实现全局异常处理器
7. WHEN Consumer 接口抛出异常 THEN THE System SHALL 捕获异常并记录日志
8. WHEN 返回错误响应 THEN THE System SHALL 包含错误码字段
9. WHEN 返回错误响应 THEN THE System SHALL 包含错误消息字段
10. WHEN 返回错误响应 THEN THE System SHALL 包含时间戳字段

### 需求 11：多环境配置支持

**用户故事**：作为运维人员，我希望系统支持多环境配置，以便在不同环境使用不同的配置参数。

#### 验收标准

1. WHEN 创建配置文件 THEN THE System SHALL 创建 application.yml 作为通用配置
2. WHEN 创建配置文件 THEN THE System SHALL 创建 application-local.yml 作为本地环境配置
3. WHEN 创建配置文件 THEN THE System SHALL 创建 application-dev.yml 作为开发环境配置
4. WHEN 创建配置文件 THEN THE System SHALL 创建 application-test.yml 作为测试环境配置
5. WHEN 创建配置文件 THEN THE System SHALL 创建 application-staging.yml 作为预发布环境配置
6. WHEN 创建配置文件 THEN THE System SHALL 创建 application-prod.yml 作为生产环境配置
7. WHEN 创建配置文件 THEN THE System SHALL 创建 bootstrap.yml 作为引导配置
8. WHEN 启动应用 THEN THE System SHALL 根据 spring.profiles.active 加载对应环境配置

### 需求 12：Prometheus 监控集成

**用户故事**：作为运维人员，我希望系统集成 Prometheus 监控，以便收集应用指标数据。

#### 验收标准

1. WHEN 配置 bootstrap 模块 THEN THE System SHALL 添加 Spring Boot Actuator 依赖
2. WHEN 配置 bootstrap 模块 THEN THE System SHALL 添加 Micrometer Registry Prometheus 依赖
3. WHEN 启动应用 THEN THE System SHALL 暴露 /actuator/prometheus 端点
4. WHEN 访问 Prometheus 端点 THEN THE System SHALL 返回 Prometheus 格式的指标数据
5. WHEN 收集指标 THEN THE System SHALL 包含 JVM 内存指标
6. WHEN 收集指标 THEN THE System SHALL 包含 JVM GC 指标
7. WHEN 收集指标 THEN THE System SHALL 包含 JVM 线程指标
8. WHEN 收集指标 THEN THE System SHALL 包含 HTTP 请求指标
9. WHEN 收集指标 THEN THE System SHALL 包含数据库连接池指标

### 需求 13：启动模块配置

**用户故事**：作为开发人员，我希望配置 bootstrap 模块作为应用启动入口，以便启动整个应用。

#### 验收标准

1. WHEN 配置 bootstrap 模块 THEN THE System SHALL 创建包含 @SpringBootApplication 注解的主类
2. WHEN 配置 bootstrap 模块 THEN THE System SHALL 使用 Spring Boot Maven Plugin 打包
3. WHEN 打包 bootstrap 模块 THEN THE System SHALL 生成可执行 JAR 文件
4. WHEN 执行可执行 JAR THEN THE System SHALL 启动 Spring Boot 应用

### 需求 14：性能要求

**用户故事**：作为运维人员，我希望系统满足基本的性能要求，以便支持正常的业务运行。

#### 验收标准

1. WHEN 系统启动 THEN THE System SHALL 在 30 秒内完成启动
2. WHEN 处理 HTTP 请求 THEN THE System SHALL 在 200ms 内返回响应（P95）
3. WHEN 处理 HTTP 请求 THEN THE System SHALL 在 500ms 内返回响应（P99）
4. WHEN 系统运行 THEN THE System SHALL 单实例内存使用不超过 2GB
5. WHEN 系统运行 THEN THE System SHALL 单实例 CPU 使用率不超过 80%

### 需求 15：安全要求

**用户故事**：作为安全管理员，我希望系统具备基本的安全防护能力，以便保护系统和数据安全。

#### 验收标准

1. WHEN 传输敏感数据 THEN THE System SHALL 使用 HTTPS 加密传输
2. WHEN 存储密码 THEN THE System SHALL 使用 BCrypt 算法加密存储
3. WHEN 用户登录 THEN THE System SHALL 支持 JWT Token 认证
4. WHEN JWT Token 生成 THEN THE System SHALL 设置有效期为 2 小时
5. WHEN 发生异常 THEN THE System SHALL 不在错误响应中暴露系统内部实现细节
6. WHEN 记录日志 THEN THE System SHALL 不记录敏感信息（如密码、Token）

### 需求 16：项目编译验证

**用户故事**：作为开发人员，我希望在每个任务完成后都能成功编译整个工程，以便确保代码质量。

#### 验收标准

1. WHEN 执行 mvn clean compile THEN THE System SHALL 成功编译所有模块
2. WHEN 模块间存在依赖 THEN THE System SHALL 按正确顺序编译模块
3. WHEN 编译失败 THEN THE System SHALL 输出清晰的错误信息
