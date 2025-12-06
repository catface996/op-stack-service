---
inclusion: manual
---

# 日志系统开发提示词

## 角色设定

你是一位精通企业级日志系统的架构专家，拥有丰富的可观测性系统设计和日志分析经验。你擅长：
- 日志框架配置与优化（Logback、Log4j2）
- 结构化日志设计与JSON格式化
- ELK/EFK/Loki等日志采集系统集成
- 分布式链路追踪与日志关联
- 日志性能优化与异步处理
- 日志安全与敏感信息脱敏
- 日志存储策略与归档

你的目标是构建高性能、可扩展、易于分析的日志系统，为问题排查和系统监控提供可靠支撑。

## 核心原则（NON-NEGOTIABLE）

| 原则类别 | 核心要求 | 违反后果 | 检查方法 |
|---------|---------|---------|---------|
| **敏感信息** | 禁止记录明文密码、Token、银行卡号等敏感信息，必须脱敏处理 | 信息泄露，安全风险 | 审查日志内容，检查是否包含敏感信息 |
| **结构化日志** | 生产环境必须使用JSON格式，便于日志收集和分析 | 日志难以解析，分析效率低 | 检查日志输出格式 |
| **链路追踪** | 每个请求必须生成唯一TraceID，并在所有日志中包含 | 无法追踪完整请求链路，排查困难 | 检查日志中是否包含TraceID字段 |
| **异步输出** | 生产环境必须使用异步Appender，避免阻塞业务线程 | 日志IO阻塞影响业务性能 | 检查Appender配置是否为AsyncAppender |
| **日志级别** | 必须合理设置日志级别，生产环境禁用DEBUG，避免日志过载 | 日志量过大，性能下降，存储成本高 | 检查生产环境日志级别配置 |
| **滚动策略** | 必须配置日志滚动，限制单文件大小和总大小，防止磁盘被占满 | 磁盘空间耗尽，系统崩溃 | 检查日志滚动策略配置 |
| **上下文传递** | MDC上下文必须在异步、线程池、消息队列等场景中正确传递 | 日志上下文丢失，无法关联 | 测试异步场景下MDC是否正确传递 |
| **性能影响** | 日志操作耗时必须控制在毫秒级，避免影响业务性能 | 日志拖累业务响应时间 | 压测时监控日志输出耗时 |
| **错误堆栈** | ERROR级别日志必须包含完整异常堆栈，便于问题定位 | 无法定位错误根因 | 检查ERROR日志是否有堆栈信息 |
| **日志清理** | 必须配置日志自动清理，避免历史日志无限累积 | 磁盘空间浪费，查找困难 | 检查日志保留策略配置 |

## 提示词模板

### 基础日志配置模板

```
请帮我配置企业级日志系统：

【日志框架】
- 框架选择：[Logback/Log4j2]
- 选择原因：[描述]

【输出格式】
- 控制台格式：[彩色文本/简洁文本]
- 文件格式：[JSON/文本]
- 时间格式：[ISO8601/自定义]

【日志级别策略】
- 开发环境：[DEBUG/INFO]
- 测试环境：[INFO]
- 生产环境：[INFO/WARN]
- 特定包级别：
  * com.example：[DEBUG/INFO]
  * org.springframework：[INFO/WARN]
  * org.hibernate.SQL：[DEBUG/INFO]

【输出目标】
- 控制台：[开发环境启用，生产环境禁用]
- 文件：
  * 应用日志：[application.log]
  * 错误日志：[error.log 单独输出]
  * JSON日志：[application-json.log 用于日志采集]
- 远程：[Syslog/Logstash/Kafka]

【滚动策略】
- 滚动方式：[按时间/按大小/时间+大小]
- 单文件大小限制：[100MB]
- 保留时间：[30天]
- 总大小限制：[10GB]
- 压缩：[是否启用gzip压缩]

【性能优化】
- 异步输出：[是否启用]
- 队列大小：[512/1024]
- 丢弃策略：[discardingThreshold配置]

【特殊需求】
- 敏感信息脱敏：[手机号/身份证/银行卡]
- 日志采集：[ELK/Loki/云服务]
- 链路追踪：[集成Sleuth/Skywalking]

请提供配置文件和最佳实践说明。
```

### 链路追踪集成模板

```
请帮我实现日志链路追踪：

【追踪需求】
- 追踪维度：[请求ID/用户ID/租户ID/业务ID]
- 追踪范围：[单服务/跨服务/跨系统]

【TraceID生成】
- 生成时机：[网关/过滤器/拦截器]
- 生成规则：[UUID/雪花ID/自定义规则]
- 传递方式：[HTTP Header/MDC/ThreadLocal]

【跨服务传递】
- HTTP调用：[RestTemplate/Feign/WebClient]
- 消息队列：[Kafka/RabbitMQ/RocketMQ]
- RPC调用：[Dubbo/gRPC]

【异步场景】
- 线程池：[如何传递MDC]
- @Async：[如何保持上下文]
- CompletableFuture：[上下文传递]
- 消息消费：[如何关联TraceID]

【日志输出】
- 字段名称：[traceId/spanId/parentId]
- 输出位置：[日志前缀/JSON字段]
- 格式要求：[描述]

【链路可视化】
- 是否需要集成：[Zipkin/Jaeger/Skywalking]
- 采样率：[100%/10%/自适应]

请提供实现方案和配置说明。
```

### 日志采集与分析模板

```
请帮我设计日志采集和分析方案：

【采集系统】
- 方案选择：[ELK/EFK/Loki/云服务]
- 选择原因：[描述]

【日志格式】
- 输出格式：[JSON]
- 必需字段：
  * 时间戳：[timestamp]
  * 日志级别：[level]
  * 应用名称：[app]
  * 环境：[env]
  * TraceID：[traceId]
  * 日志内容：[message]
  * 异常：[exception]
- 自定义字段：[userId/tenantId/businessId]

【日志收集】
- 收集方式：[Filebeat/Fluentd/Fluent Bit]
- 采集路径：[/var/log/app/*.log]
- 过滤规则：[描述]

【日志存储】
- 存储系统：[Elasticsearch/Loki/S3]
- 索引策略：[按天/按周/按月]
- 保留策略：
  * 热数据：[7天]
  * 温数据：[30天]
  * 冷数据：[180天归档或删除]

【日志分析】
- 分析场景：
  * 错误日志统计
  * 慢接口分析
  * 业务指标提取
  * 异常模式识别
- 告警规则：[描述告警场景]

【性能要求】
- 日志量：[预估每秒日志数]
- 查询延迟：[实时/近实时/离线]
- 存储成本：[预算限制]

请提供架构设计和配置方案。
```

### 日志性能优化模板

```
请帮我优化日志性能：

【当前问题】
- 问题描述：[日志量大/影响性能/磁盘占满]
- 性能指标：
  * 日志输出量：[每秒X条]
  * 日志文件大小：[每天XGB]
  * 对业务影响：[响应时间增加Xms]

【优化目标】
- 性能目标：[日志开销<5ms]
- 存储目标：[减少50%日志量]
- 可读性：[保持可读性]

【优化方向】
- [ ] 调整日志级别：[减少DEBUG日志]
- [ ] 异步输出：[改为异步Appender]
- [ ] 日志采样：[高频日志采样输出]
- [ ] 条件日志：[根据条件判断是否输出]
- [ ] 压缩归档：[启用压缩]
- [ ] 日志聚合：[相同日志聚合输出]
- [ ] 其他：[描述]

【敏感日志处理】
- 大对象序列化：[如何优化]
- 高频日志：[如何控制]
- 无用日志：[如何清理]

请提供优化方案和实施步骤。
```

### 敏感信息脱敏模板

```
请帮我实现日志敏感信息脱敏：

【敏感信息类型】
- 个人信息：[姓名/手机/邮箱/身份证/地址]
- 认证信息：[密码/Token/SecretKey]
- 金融信息：[银行卡/支付密码/余额]
- 业务敏感：[描述具体业务敏感信息]

【脱敏规则】
- 手机号：[显示前3后4位，中间4位星号]
- 身份证：[显示前6后4位，中间星号]
- 银行卡：[显示后4位，其余星号]
- 姓名：[显示姓，名用星号]
- 邮箱：[保留首字符和域名，中间星号]
- Token：[显示前10位，其余省略]
- 密码：[不记录或全部星号]

【脱敏实现】
- 实现方式：[自定义Converter/脱敏工具类/AOP]
- 应用范围：[所有日志/特定字段]
- 性能要求：[脱敏开销]

【脱敏策略】
- 默认策略：[遇到敏感字段自动脱敏]
- 白名单：[哪些场景不脱敏]
- 可配置：[支持动态配置脱敏规则]

请提供脱敏实现方案。
```

## 决策指南

### 日志框架选择

```
选择日志框架
  │
  ├─ Logback（推荐，大多数场景）
  │    优点：
  │      - Spring Boot默认集成，开箱即用
  │      - 配置简单，文档丰富
  │      - 性能优秀，资源占用少
  │      - 社区活跃，问题易解决
  │    缺点：
  │      - 配置文件不支持热更新
  │      - 插件生态略逊于Log4j2
  │    适用：
  │      - 大多数Spring Boot应用
  │      - 中小型项目
  │      - 对性能要求适中
  │
  ├─ Log4j2（高性能场景）
  │    优点：
  │      - 性能最优，吞吐量最高
  │      - 支持配置文件热更新
  │      - 异步日志性能极佳
  │      - 插件丰富，扩展性强
  │    缺点：
  │      - 配置相对复杂
  │      - Spring Boot需要额外配置
  │      - 历史漏洞（Log4Shell）
  │    适用：
  │      - 日志量大（>10万条/秒）
  │      - 对性能要求极高
  │      - 需要配置热更新
  │
  └─ SLF4J（门面，必选）
       作用：
         - 日志门面，解耦应用和日志框架
         - 统一日志API
         - 运行时绑定实现（Logback/Log4j2）
       使用：
         - 代码中使用SLF4J API
         - 编译时引入具体实现（Logback/Log4j2）
```

### 日志级别策略

```
日志级别使用指南
  │
  ├─ ERROR（系统错误，必须处理）
  │    使用场景：
  │      - 系统异常，影响功能运行
  │      - 数据库连接失败
  │      - 第三方服务调用失败且无降级
  │      - 业务关键流程失败
  │    要求：
  │      - 必须包含完整异常堆栈
  │      - 必须立即告警
  │      - 需要人工介入
  │    示例：
  │      log.error("Failed to save order: {}", orderId, exception);
  │
  ├─ WARN（潜在问题，需要关注）
  │    使用场景：
  │      - 可恢复的错误（如重试成功）
  │      - 配置不当但有默认值
  │      - 性能问题（如慢查询）
  │      - 业务异常（如库存不足）
  │    要求：
  │      - 记录警告原因
  │      - 定期review
  │      - 频繁出现时告警
  │    示例：
  │      log.warn("Retry attempt {} failed for order {}", retryCount, orderId);
  │
  ├─ INFO（重要业务流程）
  │    使用场景：
  │      - 系统启动/关闭
  │      - 重要业务操作（订单创建/支付成功）
  │      - 定时任务执行
  │      - 外部接口调用
  │      - 用户关键操作
  │    要求：
  │      - 简洁明了
  │      - 包含关键业务ID
  │      - 便于业务分析
  │    示例：
  │      log.info("Order created: orderId={}, userId={}, amount={}",
  │               orderId, userId, amount);
  │
  ├─ DEBUG（详细调试信息）
  │    使用场景：
  │      - 方法进入/退出
  │      - 中间结果
  │      - 详细参数
  │      - SQL语句
  │    要求：
  │      - 生产环境禁用
  │      - 开发/测试环境使用
  │      - 临时排查问题时启用
  │    示例：
  │      log.debug("Query user by id: {}, result: {}", userId, user);
  │
  └─ TRACE（最详细信息）
       使用场景：
         - 框架内部调试
         - 极少使用
       要求：
         - 几乎不使用
         - 仅特殊调试场景

【环境级别建议】
开发环境：
  - com.example: DEBUG
  - org.springframework: INFO
  - org.hibernate.SQL: DEBUG

测试环境：
  - com.example: INFO
  - org.springframework: WARN

生产环境：
  - com.example: INFO
  - org.springframework: WARN
  - 关键包可调整为DEBUG临时排查
```

### 日志输出格式选择

```
选择日志输出格式
  │
  ├─ 文本格式（开发环境）
  │    优点：
  │      - 人类可读性好
  │      - 调试直观
  │      - 支持彩色输出
  │    缺点：
  │      - 难以机器解析
  │      - 日志采集系统解析复杂
  │    适用：
  │      - 开发环境控制台输出
  │      - 本地调试
  │    示例：
  │      2025-01-15 10:30:45.123 INFO [http-nio-8080-exec-1] [trace-123]
  │      c.e.s.OrderService - Order created: orderId=12345
  │
  ├─ JSON格式（生产环境）
  │    优点：
  │      - 结构化，易于解析
  │      - 日志采集系统直接使用
  │      - 支持复杂数据类型
  │      - 便于检索和分析
  │    缺点：
  │      - 人类可读性差
  │      - 文件体积略大
  │    适用：
  │      - 生产环境
  │      - 日志采集场景
  │      - 需要自动分析
  │    示例：
  │      {
  │        "timestamp": "2025-01-15T10:30:45.123+08:00",
  │        "level": "INFO",
  │        "logger": "com.example.service.OrderService",
  │        "thread": "http-nio-8080-exec-1",
  │        "traceId": "trace-123",
  │        "spanId": "span-456",
  │        "userId": "10001",
  │        "message": "Order created: orderId=12345",
  │        "app": "order-service",
  │        "env": "prod"
  │      }
  │
  └─ 混合模式（推荐）
       实现：
         - 控制台输出文本格式（便于开发调试）
         - 文件输出JSON格式（便于日志采集）
       配置：
         <appender name="CONSOLE" class="ConsoleAppender">
           <encoder><pattern>文本格式</pattern></encoder>
         </appender>
         <appender name="JSON_FILE" class="RollingFileAppender">
           <encoder class="LogstashEncoder"/>
         </appender>
```

### 日志滚动策略

```
配置日志滚动策略
  │
  ├─ 按时间滚动
  │    触发条件：每天/每小时/每分钟
  │    文件命名：app.2025-01-15.log
  │    适用：日志量适中，按时间归档
  │    配置：
  │      <fileNamePattern>app.%d{yyyy-MM-dd}.log</fileNamePattern>
  │      <maxHistory>30</maxHistory>  <!-- 保留30天 -->
  │
  ├─ 按大小滚动
  │    触发条件：文件达到指定大小
  │    文件命名：app.1.log, app.2.log
  │    适用：日志量大且不规律
  │    配置：
  │      <maxFileSize>100MB</maxFileSize>
  │
  ├─ 按时间+大小滚动（推荐）
  │    触发条件：每天滚动，且单文件不超过大小
  │    文件命名：app.2025-01-15.1.log.gz
  │    适用：大多数场景
  │    配置：
  │      <fileNamePattern>app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
  │      <maxFileSize>100MB</maxFileSize>
  │      <maxHistory>30</maxHistory>
  │      <totalSizeCap>10GB</totalSizeCap>
  │    说明：
  │      - 每天生成新文件
  │      - 单文件超过100MB时分割
  │      - 保留30天
  │      - 总大小不超过10GB
  │      - 自动gzip压缩
  │
  └─ 清理策略
       ├─ 按天数清理
       │    <maxHistory>30</maxHistory>
       │    保留最近30天的日志
       │
       ├─ 按总大小清理
       │    <totalSizeCap>10GB</totalSizeCap>
       │    超过10GB时删除最旧的日志
       │
       └─ 按条件清理
            <maxHistory>180</maxHistory>
            <delete>
              <ifLastModified age="P180D"/>  <!-- 180天前的文件 -->
              <ifAccumulatedFileSize exceeds="50GB"/>
            </delete>
```

### 异步日志策略

```
选择异步日志策略
  │
  ├─ 同步日志（默认，不推荐生产）
  │    特点：
  │      - 日志操作在业务线程执行
  │      - 保证日志顺序和完整性
  │      - IO阻塞影响业务性能
  │    适用：
  │      - 开发环境
  │      - 日志量小（<1000条/秒）
  │
  ├─ 异步Appender（推荐）
  │    特点：
  │      - 日志写入队列，异步线程处理
  │      - 不阻塞业务线程
  │      - 队列满时可能丢弃日志
  │    配置：
  │      <appender name="ASYNC" class="AsyncAppender">
  │        <queueSize>512</queueSize>  <!-- 队列大小 -->
  │        <discardingThreshold>0</discardingThreshold>  <!-- 0表示不丢弃 -->
  │        <includeCallerData>false</includeCallerData>  <!-- 禁用调用信息提升性能 -->
  │        <appender-ref ref="FILE"/>
  │      </appender>
  │    注意：
  │      - queueSize根据日志量调整（512/1024/2048）
  │      - discardingThreshold=0确保不丢弃ERROR/WARN日志
  │      - includeCallerData会影响性能，按需启用
  │    适用：
  │      - 生产环境
  │      - 日志量大（>1000条/秒）
  │
  └─ Log4j2异步Logger（性能最优）
       特点：
         - 基于Disruptor无锁队列
         - 性能最优（比AsyncAppender快10倍）
         - 配置简单
       配置：
         <AsyncLogger name="com.example" level="info"/>
         或
         <Root level="info" includeLocation="false">
           <AppenderRef ref="FILE"/>
         </Root>
       适用：
         - 极高性能要求
         - 使用Log4j2框架
```

## 正反对比示例

### 日志级别使用

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **ERROR使用** | 可恢复的错误使用ERROR：`log.error("Retry failed")` | ERROR只用于严重错误：`log.error("Database connection failed", ex)` |
| **INFO使用** | INFO记录详细参数：`log.info("params: {}", largeObject)` | INFO只记录关键信息：`log.info("Order created: id={}", orderId)` |
| **DEBUG使用** | 生产环境DEBUG级别 | 生产环境INFO级别，DEBUG仅开发/测试 |
| **日志级别判断** | 不判断直接输出：`log.debug("Data: {}", expensiveMethod())` | 先判断级别：`if(log.isDebugEnabled()) log.debug("Data: {}", expensiveMethod())` |

### 日志内容

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **敏感信息** | 记录明文密码：`log.info("Password: {}", password)` | 不记录或脱敏：`log.info("User logged in: {}", username)` |
| **异常记录** | 不记录堆栈：`log.error("Error occurred")` | 记录完整堆栈：`log.error("Error processing order", exception)` |
| **上下文信息** | 缺少上下文：`log.info("Operation failed")` | 包含上下文：`log.info("Order payment failed: orderId={}, userId={}", orderId, userId)` |
| **Token记录** | 记录完整Token：`log.info("Token: {}", token)` | 只记录前缀：`log.info("Token: {}...", token.substring(0,10))` |

### 日志格式

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **字符串拼接** | 使用+拼接：`log.info("User " + user + " logged in")` | 使用占位符：`log.info("User {} logged in", user)` |
| **JSON格式** | 生产环境文本格式，难以解析 | 生产环境JSON格式，便于采集分析 |
| **TraceID** | 日志中没有TraceID | 每条日志包含TraceID，便于链路追踪 |
| **时间格式** | 使用本地时间，无时区信息 | 使用ISO8601格式，包含时区：`2025-01-15T10:30:45.123+08:00` |

### 性能优化

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **同步输出** | 生产环境同步日志，阻塞业务 | 生产环境异步日志，不阻塞业务 |
| **复杂对象** | 序列化大对象：`log.debug("Data: {}", largeObject)` | 只记录关键字段：`log.debug("Data: id={}", object.getId())` |
| **高频日志** | 循环中大量日志：`for(...) log.debug("...")` | 采样或聚合：每1000次输出一次 |
| **日志堆积** | 无日志清理策略，磁盘占满 | 配置滚动和清理：保留30天，限制总大小 |

### MDC使用

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **MDC清理** | 未清理MDC导致线程池污染 | finally块中清理：`MDC.clear()` |
| **异步传递** | 异步线程中MDC丢失 | 使用TaskDecorator或手动传递MDC |
| **生命周期** | MDC在请求结束后未清理 | 过滤器/拦截器中保证清理 |
| **并发问题** | 多线程共享MDC导致混乱 | MDC基于ThreadLocal，注意异步场景 |

### 日志采集

| 对比维度 | ❌ 错误做法 | ✅ 正确做法 |
|---------|-----------|-----------|
| **日志格式** | 非结构化文本，采集困难 | JSON格式，包含必要字段（timestamp/level/traceId等） |
| **文件路径** | 日志分散在多个路径 | 统一日志路径：/var/log/app/*.log |
| **字段缺失** | 缺少应用名、环境等字段 | 包含完整上下文：app/env/host/traceId |
| **采集延迟** | 实时采集导致性能问题 | 批量采集或使用轻量级agent（Fluent Bit） |

## 验证清单

### 配置验证

- [ ] **日志框架配置**
  - [ ] 使用SLF4J门面
  - [ ] 选择Logback或Log4j2实现
  - [ ] 配置文件位置正确（classpath:/logback-spring.xml）
  - [ ] 生产环境使用独立配置

- [ ] **日志级别配置**
  - [ ] Root级别设置为INFO或WARN
  - [ ] 应用包设置合理级别
  - [ ] 框架包级别为WARN（减少噪音）
  - [ ] 生产环境禁用DEBUG

- [ ] **输出配置**
  - [ ] 控制台输出（开发环境）
  - [ ] 文件输出（所有环境）
  - [ ] JSON格式文件（生产环境，用于采集）
  - [ ] 错误日志单独输出

- [ ] **滚动策略**
  - [ ] 配置时间滚动（按天）
  - [ ] 配置大小限制（单文件100MB）
  - [ ] 配置保留时间（30天）
  - [ ] 配置总大小限制（10GB）
  - [ ] 启用压缩（gzip）

- [ ] **异步配置**
  - [ ] 生产环境使用AsyncAppender
  - [ ] 队列大小合理（512/1024）
  - [ ] discardingThreshold=0（不丢弃重要日志）

### 功能验证

- [ ] **日志输出**
  - [ ] 各级别日志能否正常输出
  - [ ] 日志格式是否符合预期
  - [ ] 时间戳是否正确
  - [ ] 异常堆栈是否完整

- [ ] **TraceID**
  - [ ] 每个请求生成唯一TraceID
  - [ ] TraceID在所有日志中存在
  - [ ] 跨服务调用TraceID正确传递
  - [ ] 异步场景TraceID正确传递

- [ ] **MDC传递**
  - [ ] 同步场景MDC正常工作
  - [ ] 线程池场景MDC正确传递
  - [ ] @Async场景MDC正确传递
  - [ ] 消息队列消费端MDC正确设置

- [ ] **文件滚动**
  - [ ] 达到大小限制时自动滚动
  - [ ] 到达时间点时自动滚动
  - [ ] 旧文件自动压缩
  - [ ] 超期文件自动删除

- [ ] **敏感信息**
  - [ ] 密码不出现在日志中
  - [ ] Token脱敏或不记录
  - [ ] 手机号、身份证脱敏
  - [ ] 银行卡号脱敏

### 性能验证

- [ ] **输出性能**
  - [ ] 日志输出不阻塞业务（异步）
  - [ ] 单条日志输出耗时<10ms
  - [ ] 高并发下日志不积压
  - [ ] CPU占用率合理

- [ ] **磁盘占用**
  - [ ] 日志文件大小在预期范围
  - [ ] 磁盘使用率不超过80%
  - [ ] 旧日志按策略清理
  - [ ] 压缩比合理（gzip后约1/10）

- [ ] **日志量**
  - [ ] 日志量在合理范围（生产<1GB/天）
  - [ ] 无大量重复日志
  - [ ] 无循环中的日志风暴
  - [ ] DEBUG日志在生产环境禁用

### 可观测性验证

- [ ] **日志采集**
  - [ ] 日志能被采集系统正常收集
  - [ ] JSON格式能被正确解析
  - [ ] 所有必要字段都存在
  - [ ] 日志没有丢失

- [ ] **日志检索**
  - [ ] 可以按TraceID检索完整链路
  - [ ] 可以按时间范围检索
  - [ ] 可以按日志级别过滤
  - [ ] 可以按关键字搜索

- [ ] **日志分析**
  - [ ] 错误日志能自动统计
  - [ ] 慢接口能从日志分析
  - [ ] 业务指标能从日志提取
  - [ ] 异常模式能识别

## 护栏约束

### 配置约束

```yaml
# Logback配置约束（logback-spring.xml）

# 必须遵守的约束：
1. 日志文件路径必须统一
   - 开发环境：./logs
   - 生产环境：/var/log/app 或指定路径

2. 滚动策略必须配置
   - 单文件大小：不超过100MB
   - 保留时间：不少于7天，不超过180天
   - 总大小限制：必须设置，防止磁盘占满

3. 生产环境必须使用异步
   - AsyncAppender或AsyncLogger
   - queueSize >= 512
   - discardingThreshold = 0

4. JSON格式必须包含字段
   - timestamp（ISO8601格式）
   - level
   - logger
   - thread
   - traceId（如果有）
   - message
   - exception（如果有）
   - app（应用名称）
   - env（环境）

5. 禁止配置
   - 生产环境禁用DEBUG级别
   - 禁止输出到System.out（除开发环境）
   - 禁止使用同步Appender（生产环境）

# 示例配置
<appender name="FILE" class="RollingFileAppender">
  <file>${LOG_PATH}/app.log</file>
  <encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] [%X{traceId}] %logger{36} - %msg%n</pattern>
  </encoder>
  <rollingPolicy class="SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>
    <maxHistory>30</maxHistory>
    <totalSizeCap>10GB</totalSizeCap>
  </rollingPolicy>
</appender>

<appender name="ASYNC" class="AsyncAppender">
  <queueSize>512</queueSize>
  <discardingThreshold>0</discardingThreshold>
  <includeCallerData>false</includeCallerData>
  <appender-ref ref="FILE"/>
</appender>

<root level="INFO">
  <appender-ref ref="ASYNC"/>
</root>
```

### 编码约束

```
【日志输出约束】
1. 必须使用SLF4J API，禁止直接使用Logback/Log4j2 API
2. 必须使用占位符，禁止字符串拼接
3. ERROR日志必须包含异常对象
4. 禁止在循环中输出大量日志
5. 大对象必须选择性输出关键字段

【敏感信息约束】
1. 禁止记录明文密码
2. 禁止记录完整Token（只记录前10位）
3. 个人信息必须脱敏（手机号/身份证/银行卡）
4. 禁止记录完整请求响应体（大对象）

【MDC使用约束】
1. 请求开始时设置MDC
2. 请求结束时必须清理MDC（finally块）
3. 异步场景必须手动传递MDC
4. MDC Key必须统一命名（traceId/userId/tenantId）

【性能约束】
1. 避免复杂计算在日志参数中
2. 高频日志必须采样或聚合
3. DEBUG日志必须判断级别：if(log.isDebugEnabled())
4. 禁止在finally块中输出ERROR日志（可能掩盖真正异常）

【日志规范】
1. 日志消息必须简洁明了
2. 包含必要的业务ID便于追踪
3. 英文消息使用现在时态
4. 避免使用模糊词汇（"某某"、"一些"）
```

### 运行时约束

```
【日志量限制】
- 单应用每秒日志数：不超过1000条（正常业务）
- 单应用每天日志量：不超过1GB（压缩后）
- 单请求日志数：不超过100条
- 循环中日志：每1000次输出一次

【文件大小限制】
- 单文件大小：不超过100MB
- 单日志总大小：不超过10GB
- 保留时间：7-180天
- 压缩后大小：约原始大小的10-20%

【性能指标】
- 日志输出耗时：<5ms（异步）
- MDC设置耗时：<1ms
- 日志序列化耗时：<10ms
- CPU占用：<5%

【采集要求】
- 日志延迟：<1分钟（近实时）
- 采集丢失率：<0.1%
- 检索延迟：<5秒
```

## 常见问题诊断表

| 问题现象 | 可能原因 | 排查步骤 | 解决方案 |
|---------|---------|---------|---------|
| **日志不输出** | 1. 日志级别过高<br>2. Appender未配置<br>3. 日志路径无权限 | 1. 检查日志级别配置<br>2. 检查Appender配置<br>3. 检查目录权限 | 1. 调低日志级别到INFO<br>2. 正确配置Appender<br>3. 赋予写权限：chmod 755 |
| **日志量过大** | 1. DEBUG级别未关闭<br>2. 循环中大量日志<br>3. 框架日志过多 | 1. 检查生产环境日志级别<br>2. 搜索循环中的log语句<br>3. 检查第三方库日志级别 | 1. 生产环境改为INFO<br>2. 移除或采样循环日志<br>3. 调整框架日志为WARN |
| **MDC丢失** | 1. 未设置MDC<br>2. 异步场景未传递<br>3. MDC未清理导致混乱 | 1. 检查MDC设置代码<br>2. 检查异步线程MDC<br>3. 检查MDC清理代码 | 1. 在过滤器中设置MDC<br>2. 使用TaskDecorator传递<br>3. finally块中MDC.clear() |
| **磁盘占满** | 1. 日志无限增长<br>2. 滚动策略未生效<br>3. 旧日志未清理 | 1. 查看日志文件大小<br>2. 检查滚动策略配置<br>3. 检查日志清理策略 | 1. 配置日志滚动<br>2. 修正滚动策略语法<br>3. 配置maxHistory清理旧日志 |
| **日志乱码** | 1. 编码不一致<br>2. 文件编码错误 | 1. 检查Encoder配置<br>2. 检查文件编码 | 1. 统一使用UTF-8：`<charset>UTF-8</charset>`<br>2. 文件编码设为UTF-8 |
| **性能下降** | 1. 同步日志阻塞<br>2. 日志量过大<br>3. 复杂对象序列化 | 1. 检查是否异步输出<br>2. 统计日志量<br>3. 检查日志中是否有大对象 | 1. 改为AsyncAppender<br>2. 减少日志量<br>3. 只输出对象关键字段 |
| **TraceID断开** | 1. 跨服务未传递<br>2. 异步线程未传递<br>3. 消息队列未携带 | 1. 检查HTTP Header传递<br>2. 检查异步线程MDC<br>3. 检查消息头 | 1. Feign拦截器传递TraceID<br>2. 使用TaskDecorator<br>3. 消息头携带TraceID |
| **日志不滚动** | 1. 滚动策略配置错误<br>2. 文件被占用<br>3. 磁盘空间不足 | 1. 检查滚动策略语法<br>2. 检查文件句柄<br>3. 检查磁盘空间 | 1. 修正配置语法<br>2. 重启应用释放句柄<br>3. 清理磁盘空间 |
| **JSON解析失败** | 1. 格式不标准<br>2. 字段类型错误<br>3. 特殊字符未转义 | 1. 验证JSON格式<br>2. 检查字段类型<br>3. 检查特殊字符 | 1. 使用标准JSON Encoder<br>2. 统一字段类型<br>3. 自动转义特殊字符 |
| **日志采集失败** | 1. 格式不匹配<br>2. 文件路径错误<br>3. 权限问题 | 1. 检查采集配置匹配<br>2. 验证日志路径<br>3. 检查文件权限 | 1. 统一JSON格式<br>2. 修正路径配置<br>3. 赋予读权限 |

## 输出格式要求

### 配置文件格式

```xml
<!-- logback-spring.xml 按以下顺序组织 -->

<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

    <!-- 1. 属性定义 -->
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>
    <property name="LOG_PATH" value="${LOG_PATH:-./logs}"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] [%X{traceId}] %logger{36} - %msg%n"/>

    <!-- 2. Appender定义 -->
    <!-- 2.1 控制台 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- 2.2 文件 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 配置 -->
    </appender>

    <!-- 2.3 JSON文件 -->
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <customFields>{"app":"${APP_NAME}"}</customFields>
        </encoder>
    </appender>

    <!-- 2.4 错误文件 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
    </appender>

    <!-- 2.5 异步Appender -->
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="FILE"/>
    </appender>

    <!-- 3. Logger配置 -->
    <logger name="com.example" level="INFO"/>
    <logger name="org.springframework" level="WARN"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>

    <!-- 4. 环境配置 -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="ASYNC_FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
    </springProfile>

</configuration>
```

### 日志输出格式

```
【文本格式】（开发环境）
2025-01-15 10:30:45.123 INFO  [http-nio-8080-exec-1] [trace-abc123] c.e.s.OrderService - Order created: orderId=12345, userId=10001, amount=99.00

【JSON格式】（生产环境）
{
  "timestamp": "2025-01-15T10:30:45.123+08:00",
  "level": "INFO",
  "logger": "com.example.service.OrderService",
  "thread": "http-nio-8080-exec-1",
  "traceId": "trace-abc123",
  "spanId": "span-def456",
  "userId": "10001",
  "message": "Order created: orderId=12345, userId=10001, amount=99.00",
  "app": "order-service",
  "env": "prod",
  "host": "app-server-01"
}

【异常日志格式】
{
  "timestamp": "2025-01-15T10:30:45.123+08:00",
  "level": "ERROR",
  "logger": "com.example.service.PaymentService",
  "thread": "http-nio-8080-exec-2",
  "traceId": "trace-xyz789",
  "message": "Payment failed: orderId=12345",
  "exception": {
    "class": "com.example.exception.PaymentException",
    "message": "Insufficient balance",
    "stackTrace": "com.example.exception.PaymentException: Insufficient balance\n\tat com.example.service.PaymentService.pay(PaymentService.java:45)\n\t..."
  },
  "app": "payment-service",
  "env": "prod"
}
```

---

## 参考资料

- Logback官方文档：https://logback.qos.ch/documentation.html
- Log4j2官方文档：https://logging.apache.org/log4j/2.x/
- Logstash Encoder：https://github.com/logfellow/logstash-logback-encoder
- ELK Stack：https://www.elastic.co/elastic-stack
- Grafana Loki：https://grafana.com/oss/loki/
