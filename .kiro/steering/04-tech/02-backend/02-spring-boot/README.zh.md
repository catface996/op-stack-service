---
inclusion: manual
---

# Spring Boot 开发最佳实践

## 角色设定

你是一位精通 Spring Boot 3.x 的后端开发专家，擅长自动配置、Web 开发、数据访问和微服务架构。

---

## 核心原则 (NON-NEGOTIABLE)

| 原则 | 要求 | 违反后果 |
|------|------|----------|
| 分层架构 | MUST 遵循 Controller → Service → Repository 分层 | 代码耦合、难以测试 |
| 依赖注入 | MUST 使用构造器注入，禁止字段注入 | 难以测试、循环依赖难排查 |
| 事务边界 | MUST 在 Service 层声明事务，Controller 禁止事务 | 事务失效、数据不一致 |
| 配置外部化 | MUST 敏感配置使用环境变量或配置中心 | 安全风险、部署困难 |

---

## 提示词模板

### 项目搭建

```
请帮我搭建 Spring Boot 项目：
- Spring Boot 版本：[3.x]
- Java 版本：[17/21]
- 构建工具：[Maven/Gradle]
- 需要的功能：[Web API/数据库/缓存/安全认证/消息队列]
- 数据库类型：[MySQL/PostgreSQL/MongoDB]
```

### 功能开发

```
请帮我实现 Spring Boot 功能：
- 功能描述：[描述功能]
- 涉及实体：[列出实体及关系]
- API 设计：[RESTful 端点]
- 是否需要事务：[是/否]
- 是否需要缓存：[是/否]
```

### 问题排查

```
请帮我排查 Spring Boot 问题：
- 错误现象：[描述错误]
- 错误日志：[关键日志信息]
- 发生场景：[什么操作触发]
- 已尝试方案：[已做的排查]
```

---

## 决策指南

### 数据访问方案选择

```
数据访问需求？
├─ 简单 CRUD → Spring Data JPA
├─ 复杂查询多
│   ├─ 动态条件 → QueryDSL / Specification
│   └─ 原生SQL → MyBatis / JDBC Template
├─ 多数据源 → 配置多个 DataSource + @Qualifier
└─ 读写分离 → ShardingSphere / 动态数据源
```

### 缓存方案选择

```
缓存需求？
├─ 本地缓存（单机）→ Caffeine
├─ 分布式缓存
│   ├─ 简单KV → Redis String
│   ├─ 复杂数据结构 → Redis Hash/Set/ZSet
│   └─ 多级缓存 → Caffeine + Redis
└─ 无缓存需求 → 不引入（简洁优先）
```

### 异步处理方案

```
异步需求？
├─ 简单异步任务 → @Async + ThreadPoolTaskExecutor
├─ 定时任务
│   ├─ 单机 → @Scheduled
│   └─ 分布式 → XXL-Job / Elastic-Job
├─ 消息驱动 → RocketMQ / Kafka
└─ 工作流 → Camunda / Flowable
```

---

## 正反对比示例

### 依赖注入

| ❌ 错误做法 | ✅ 正确做法 | 原因 |
|------------|------------|------|
| @Autowired 字段注入 | 构造器注入 (推荐 @RequiredArgsConstructor) | 字段注入无法用于单元测试 |
| 循环依赖用 @Lazy 解决 | 重构设计，提取公共服务 | @Lazy 掩盖设计问题 |
| 注入具体实现类 | 注入接口类型 | 降低耦合，便于 Mock |

### 事务管理

| ❌ 错误做法 | ✅ 正确做法 | 原因 |
|------------|------------|------|
| 在 Controller 加 @Transactional | 在 Service 层加事务 | 事务边界应在业务逻辑层 |
| 所有方法都加事务 | 只在需要的方法加事务 | 不必要的事务开销 |
| 忽略事务传播行为 | 明确指定 propagation | 默认行为可能不符合预期 |
| 同类方法内部调用期望事务生效 | 通过代理调用或拆分到不同类 | 内部调用不走代理 |

### 异常处理

| ❌ 错误做法 | ✅ 正确做法 | 原因 |
|------------|------------|------|
| 捕获 Exception 统一处理 | 区分业务异常和系统异常 | 便于定位问题 |
| 在 catch 中只打日志不抛出 | 向上抛出或返回错误响应 | 吞掉异常导致问题难排查 |
| 手动在每个 Controller 处理异常 | 使用 @RestControllerAdvice 全局处理 | 减少重复代码 |

### 配置管理

| ❌ 错误做法 | ✅ 正确做法 | 原因 |
|------------|------------|------|
| 硬编码密码/密钥 | 使用环境变量或配置中心 | 安全风险 |
| 直接用 @Value 注入 | 使用 @ConfigurationProperties 类型安全绑定 | 类型安全，可验证 |
| 一个 application.yml 包含所有配置 | 按环境拆分 application-{profile}.yml | 环境隔离 |

---

## 验证清单 (Validation Checklist)

### 开发阶段

- [ ] 是否遵循分层架构？（Controller → Service → Repository）
- [ ] 是否使用构造器注入？
- [ ] 事务注解是否在 Service 层？
- [ ] 敏感配置是否外部化？
- [ ] 是否有统一响应格式？
- [ ] 是否有全局异常处理？

### 安全阶段

- [ ] 是否有参数校验 (@Valid)？
- [ ] 是否防范 SQL 注入？（使用参数化查询）
- [ ] 是否有 XSS 防护？（输入输出转义）
- [ ] 是否有接口限流？
- [ ] 敏感数据是否加密存储？

### 部署阶段

- [ ] 是否配置健康检查端点？
- [ ] 是否配置优雅停机？
- [ ] 是否有监控指标暴露？
- [ ] 日志格式是否统一？
- [ ] 是否有 API 文档？

---

## 护栏约束 (Guardrails)

**允许 (✅)**：
- 使用 Spring Boot 3.x + Java 17+
- 使用 Lombok 简化代码
- 使用 MapStruct 对象映射
- 使用 Swagger/OpenAPI 文档

**禁止 (❌)**：
- NEVER 在 Controller 层直接操作数据库
- NEVER 在事务方法中调用外部 HTTP 接口
- NEVER 使用 @Autowired 字段注入
- NEVER 将异常信息直接暴露给前端
- NEVER 在 application.yml 中硬编码密码

**需澄清 (⚠️)**：
- 数据库类型：[NEEDS CLARIFICATION: MySQL/PostgreSQL/MongoDB?]
- 是否需要缓存：[NEEDS CLARIFICATION: 使用场景?]
- 认证方案：[NEEDS CLARIFICATION: Session/JWT/OAuth2?]

---

## 常见问题诊断

| 症状 | 可能原因 | 解决方案 |
|------|----------|----------|
| 事务不回滚 | 捕获异常后未抛出、RuntimeException 以外异常 | 配置 rollbackFor，不吞异常 |
| 循环依赖 | Bean 相互依赖 | 重构设计，提取公共服务 |
| Bean 注入为 null | 未被 Spring 管理、静态方法中使用 | 检查注解、避免静态上下文 |
| 配置不生效 | profile 不对、配置文件名错误 | 检查 spring.profiles.active |
| 接口响应慢 | 数据库慢查询、外部调用阻塞 | 加索引、异步化、超时设置 |
| 内存溢出 | 大对象未释放、查询全量数据 | 分页查询、流式处理 |

---

## 项目结构规范

```
src/main/java/com/example/
├── Application.java           # 启动类，MUST 放在根包下
├── config/                    # 配置类 (Security, Redis, Swagger...)
├── controller/                # 控制器，只做参数校验和响应封装
├── service/                   # 业务逻辑，事务在这层
│   └── impl/                  # 实现类
├── repository/                # 数据访问层
├── entity/                    # 数据库实体
├── dto/                       # 数据传输对象
│   ├── request/               # 请求对象
│   └── response/              # 响应对象
├── exception/                 # 自定义异常
├── common/                    # 公共组件 (Result, PageResult...)
└── util/                      # 工具类 (SHOULD 尽量少)
```

---

## 输出格式要求

当生成 Spring Boot 功能时，MUST 遵循以下结构：

```
## 功能说明
- 功能名称：[名称]
- 涉及接口：[列出 API 端点]
- 涉及实体：[列出实体]

## 实现要点
1. [关键实现点1]
2. [关键实现点2]

## 配置说明
- [需要的配置项]

## 注意事项
- [边界情况和约束]
```
