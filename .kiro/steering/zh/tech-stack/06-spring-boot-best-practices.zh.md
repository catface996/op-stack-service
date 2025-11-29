---
inclusion: manual
---

# Spring Boot 最佳实践

本文档指导 AI 如何正确、高效地使用 Spring Boot 框架开发应用。

**重要说明**：本项目采用 DDD 多模块架构，项目结构和分层规范请参考 `05-ddd-multi-module-project-best-practices.md`。

## 核心原则

### 1. 约定优于配置原则

**你应该遵守**：
- ✅ 使用 Spring Boot 的默认配置
- ✅ 遵循 DDD 多模块项目结构（详见 `05-ddd-multi-module-project-best-practices.md`）
- ✅ 使用 Starter 依赖简化配置
- ❌ 不要过度自定义配置

### 2. 分层架构原则

**你必须遵守 DDD 分层架构**（详见 `05-ddd-multi-module-project-best-practices.md`）：
- ✅ Interface 层（HTTP/Consumer/Job）：对外提供服务入口
- ✅ Application 层：编排业务流程，协调领域服务
- ✅ Domain 层：核心业务逻辑和领域模型
- ✅ Infrastructure 层：技术实现和外部依赖
- ❌ 不要跨层调用

**依赖方向**：Interface → Application → Domain ← Infrastructure

### 3. 依赖注入原则

**你应该遵守**：
- ✅ 使用构造器注入（推荐）
- ⚠️ 避免使用字段注入（@Autowired 在字段上）
- ✅ 使用 @RequiredArgsConstructor（Lombok）简化构造器注入

## 依赖注入规范

### 你应该使用的注入方式

**推荐：构造器注入**
- 使用 Lombok 的 @RequiredArgsConstructor 注解
- 将依赖声明为 `private final` 字段
- Spring 自动通过构造器注入依赖
- 无需 @Autowired 注解

**不推荐：字段注入**
- 不要在字段声明上使用 @Autowired
- 使测试变得困难
- 隐藏依赖关系
- 可能导致循环依赖问题

**为什么推荐构造器注入**：
- 依赖关系明确可见
- 便于单元测试（可以注入 mock 对象）
- 确保依赖不可变（final 字段）
- 避免循环依赖
- IDE 提供更好的支持

## 配置管理规范

### 你应该遵循的配置规则

**1. 使用 application.yml 而不是 application.properties**
- YAML 格式更清晰易读
- 支持层级结构
- 更适合复杂配置
- 便于管理

**2. 多环境配置**
应该创建的文件：
- `application.yml` - 所有环境共享的通用配置
- `application-dev.yml` - 开发环境特定配置
- `application-test.yml` - 测试环境特定配置
- `application-prod.yml` - 生产环境特定配置

激活规则：
- 使用 `spring.profiles.active` 指定激活的 profile

**运行应用的最佳实践**：
- ✅ **推荐**：直接运行 jar 包
  ```bash
  java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
  ```
- ⚠️ **注意**：`mvn spring-boot:run` 可能因类路径缓存问题导致代码修改不生效
  ```bash
  # 如遇代码修改不生效问题，请使用 jar 包方式运行
  mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local
  ```

**3. 配置属性绑定**
- 使用 @ConfigurationProperties 绑定配置组
- 定义配置类并添加适当的验证
- 不要使用 @Value 注入多个相关配置
- 配置类应该是不可变的（使用 final 字段）

**4. 敏感信息处理**
必须遵守的规则：
- 永远不要在配置文件中明文存储密码
- 使用环境变量存储敏感数据
- 使用 Spring Cloud Config 或类似工具实现配置中心
- 考虑使用 Jasypt 加密配置文件中的敏感值

### 配置类规范

**配置类规则**：

**1. 注解要求**
- 使用 @Configuration 标注配置类
- 配置类应该放在 config 包中
- 每个 @Bean 方法创建一个 bean

**2. 配置类命名**
- 必须以 `Config` 后缀结尾
- 名称应该自解释
- 示例：RedisConfig、MybatisPlusConfig、SecurityConfig

**3. 配置类位置**
- 放在 `config` 包下
- 按功能模块组织
- 相关配置放在一起

**4. @Bean 方法规则**
- 方法名应该描述正在创建的 bean
- 在创建 bean 的方法上添加 @Bean 注解
- 使用方法参数进行依赖注入

## Interface 层规范（HTTP Controller）

### 你应该遵循的 Controller 规则

**1. Controller 位置**
- 必须在 Interface 层的 http 模块中
- Package 模式：`com.{company}.{system}.http.controller`
- 按业务领域分组 controller

**2. 使用 RESTful 风格**
映射规则：
- GET：查询资源（只读操作）
- POST：创建新资源
- PUT：更新现有资源（完整更新）
- PATCH：部分更新（可选，不需要可使用 PUT）
- DELETE：删除资源

**3. 统一返回格式**
- 在 common 模块定义 Result 类
- 所有接口必须返回 Result<T> 类型
- Result 应包含：code、message、data
- 使用 Result.success() 和 Result.error() 工厂方法

**4. 参数校验**
- 在请求参数上使用 @Valid 或 @Validated
- 在 DTO 类中使用 JSR-303 注解：
  - @NotNull、@NotBlank、@NotEmpty 用于必填字段
  - @Size 用于字符串长度验证
  - @Min、@Max 用于数字范围验证
  - @Email 用于邮箱格式验证
  - @Pattern 用于正则表达式验证

**5. Controller 职责**
Controller 应该做的：
- 接收 HTTP 请求并提取参数
- 验证请求参数
- 调用 Application Service 处理业务
- 将 DTO 转换为 VO 用于响应
- 返回统一的 Result 格式

Controller 不应该做的：
- 实现业务逻辑
- 直接调用 Repository 或 Domain Service
- 处理事务
- 执行数据持久化操作

**6. 统一异常处理**
- 使用 @RestControllerAdvice 进行全局异常处理
- 将异常处理器放在 http 模块的 handler 包中
- 使用特定的 @ExceptionHandler 方法处理不同异常类型
- 适当记录异常日志
- 向客户端返回友好的错误消息

### Controller 最佳实践

**注解要求**：
- 类级别使用 @RestController
- @RequestMapping 设置基础路径
- @RequiredArgsConstructor 用于依赖注入
- @Validated 启用验证

**方法设计**：
- 每个方法处理一个 HTTP 端点
- 方法名应该是动词（getUser、createOrder、updateProfile）
- 使用 @PathVariable 处理路径参数
- 使用 @RequestParam 处理查询参数
- 使用 @RequestBody 处理请求体，并配合 @Valid 使用

**转换规则**：
- 将应用层 DTO 转换为表示层 VO
- 不要向客户端暴露内部领域模型
- 将转换逻辑放在私有方法或独立的转换器类中

## Application 层规范

### 你应该遵循的 Application Service 规则

**1. Application Service 位置**
- 接口：application-api 模块（定义契约）
- 实现：application-impl 模块（实现逻辑）
- Package：`com.{company}.{system}.application.service`

**2. 接口与实现分离**
命名规范：
- 接口命名：`XxxAppService`
- 实现命名：`XxxAppServiceImpl`
- 接口在 application-api 模块
- 实现在 application-impl 模块

**3. 事务管理**
- 在服务方法上使用 @Transactional 注解
- 写操作指定 `rollbackFor = Exception.class`
- 查询操作使用 `readOnly = true`
- 尽可能缩小事务范围
- 避免长时间运行的事务

**4. Application Service 职责**
Application Service 应该做的：
- 编排业务工作流（协调）
- 调用 Domain Service 处理业务逻辑
- 调用 Infrastructure 层（Repository、Cache、MQ）
- 在 DTO 和 Domain Entity 之间转换
- 处理事务边界
- 记录审计日志

Application Service 不应该做的：
- 实现核心业务规则（委托给 Domain 层）
- 直接操作数据库（使用 Repository）
- 包含复杂的条件逻辑（封装到私有方法）

**5. 异常处理**
- 直接抛出业务异常（不要在主流程中捕获）
- 让异常传播到全局异常处理器
- 为不同错误场景使用自定义异常类
- 在异常消息中包含上下文信息

### Application Service 最佳实践

**详细的 Application Service 编码规范，请参考**：`08-application-layer-best-practices.md`

**关键原则**：
- 主方法应该有 5-10 个清晰的步骤
- 主方法中没有 if/else
- 主方法中没有 try/catch（除特殊情况）
- 将验证、转换、日志提取到私有方法
- 每个私有方法应该有单一职责

## Domain 层规范

### 你应该遵循的 Domain 规则

**详见**：`05-ddd-multi-module-project-best-practices.md`

**关键原则**：
- Domain 层使用纯业务语言（不使用技术后缀）
- Domain 层不依赖任何框架
- Domain 层不依赖 Infrastructure 层
- 核心业务逻辑在 Domain Service 中实现
- 领域实体应该是富模型（不是贫血模型）

## Infrastructure 层规范（Repository）

### 你应该遵循的 Repository 规则

**详见**：`05-ddd-multi-module-project-best-practices.md`

**1. Repository 位置**
- 接口：domain-api 或 repository-api 模块
- 实现：mysql-impl 模块（或其他持久化实现）

**2. Entity 和 PO 分离**
- Entity：在 repository-api，纯 POJO，无框架注解
- PO（持久化对象）：在 mysql-impl，包含 MyBatis-Plus 注解
- RepositoryImpl 负责 Entity 和 PO 之间的转换

**3. 使用 MyBatis-Plus**
- Mapper 接口操作 PO 对象
- Repository 接口操作 Entity 对象
- 配置类在 mysql-impl 模块
- 使用 BaseMapper 进行常见 CRUD 操作

**4. Repository 职责**
Repository 应该做的：
- 为领域实体提供 CRUD 操作
- 在 Entity 和 PO 之间转换
- 执行数据库查询
- 处理持久化关注点

Repository 不应该做的：
- 实现业务逻辑
- 执行数据验证（应在 Domain 层）
- 处理事务（在 Application 层处理）

## 异常处理规范

### 你应该遵循的异常处理规则

**1. 自定义业务异常**
要求：
- 继承 RuntimeException（非受检异常）
- 包含错误代码字段
- 包含错误消息
- 提供多个构造函数以增加灵活性
- 在同一个包中组织相关异常

**2. 全局异常处理**
实现规则：
- 使用 @RestControllerAdvice 注解
- 放在 http 模块的 handler 包中
- 分别处理不同的异常类型：
  - 业务异常（返回特定错误代码和消息）
  - 验证异常（返回验证错误）
  - 系统异常（记录日志并返回通用错误）
- 始终使用适当的级别记录异常

**3. 异常处理原则**
- 不要吞掉异常（始终记录或重新抛出）
- 记录异常时包含完整上下文信息
- 向用户返回友好的错误消息
- 不要向客户端暴露敏感信息或堆栈跟踪
- 包含请求 ID/trace ID 以便调试

**4. 异常层次结构**
建议的结构：
- BaseException（所有业务异常的根）
- DomainException（领域特定错误）
- InfrastructureException（持久化、网络错误）
- ApplicationException（应用层错误）

## 日志规范

### 你应该遵循的日志规则

**1. 使用 SLF4J + Logback**
- Spring Boot 默认集成
- 在需要日志的类上使用 @Slf4j 注解（Lombok）
- 永远不要使用 System.out.println 或 printStackTrace()

**2. 日志级别**
使用指南：
- **ERROR**：系统异常，需要立即关注的操作失败
- **WARN**：潜在问题，业务警告，可恢复的错误
- **INFO**：重要业务操作，状态变更，主要里程碑
- **DEBUG**：用于调试的详细过程信息
- **TRACE**：非常详细的信息，通常在生产环境禁用

**3. 日志内容原则**
必须记录的内容：
- 关键业务操作（登录、注册、支付、订单创建）
- 方法入口及重要参数（敏感数据除外）
- 方法出口及结果（敏感数据除外）
- 异常信息及完整堆栈跟踪
- 性能指标（关键操作的执行时间）
- 重要业务状态变更
- 安全相关事件

不得记录的内容：
- 密码、令牌或 API 密钥
- 个人身份证号（如身份证、护照等）
- 信用卡或银行账号
- 任何敏感个人信息

**4. 日志格式规范**

**使用占位符，不要字符串拼接**：
- 错误：`"用户登录，用户名：" + username`
- 正确：`"用户登录，用户名：{}"` 配合占位符

**包含上下文信息**：
必需的上下文字段：
- 用户 ID（如果可用）
- 用户名（如果可用）
- 操作类型
- 业务对象 ID
- 时间戳（日志框架自动添加）

可选的上下文字段：
- IP 地址（在 Controller 层）
- 请求 ID / Trace ID
- 设备信息
- 会话 ID

**异常日志**：
- 始终将异常对象作为最后一个参数
- 不要只记录 e.getMessage() - 包含完整堆栈跟踪
- 模式：`log.error("操作失败，上下文：{}", context, exception)`

**性能日志**：
- 在操作前记录开始时间
- 操作后计算持续时间
- 记录操作名称和持续时间
- 模式：`"操作完成，耗时：{}ms"`

**5. 审计日志格式**

**审计日志是用于关键业务操作的结构化日志**：

格式要求：
```
[审计日志] 操作描述 | 字段1=值1 | 字段2=值2 | timestamp=时间戳
```

规则：
- 使用 `[审计日志]` 前缀标识
- 使用管道符 `|` 分隔字段
- 每个字段使用 `key=value` 格式
- 始终包含 timestamp 字段
- 提取到私有日志方法（logRegistrationSuccess、logLoginFailure 等）

需要审计日志的操作：
- 用户注册、登录、登出
- 账号锁定、解锁
- 权限变更
- 敏感数据访问
- 关键配置修改
- 财务操作（支付、转账、退款）

**6. 日志检查清单**

提交代码前，验证：
- [ ] 在类上使用 @Slf4j 注解
- [ ] 关键业务操作已记录日志（开始、成功、失败）
- [ ] 记录了方法输入（敏感信息除外）
- [ ] 异常日志包含完整堆栈跟踪
- [ ] 使用占位符而不是字符串拼接
- [ ] 选择了适当的日志级别
- [ ] 包含了完整的上下文信息
- [ ] 日志中没有敏感信息
- [ ] 循环中没有打印过多日志
- [ ] 记录了重要状态变更
- [ ] 审计日志使用正确的结构化格式

**7. 常见日志错误**

| 错误 | 错误做法 | 正确做法 | 原因 |
|------|---------|---------|------|
| 没有日志 | 关键操作没有日志 | 在关键操作前后记录日志 | 无法调试问题 |
| 字符串拼接 | `"用户：" + username` | `"用户：{}"` 配合占位符 | 性能影响，难以阅读 |
| 丢失堆栈 | `log.error(e.getMessage())` | `log.error("失败", e)` | 没有堆栈无法调试 |
| 记录敏感信息 | `"密码：{}"` | 不记录密码 | 安全违规 |
| 循环日志 | 每次迭代都记录 | 汇总后记录一次 | 性能影响，日志泛滥 |
| 级别错误 | INFO 记录调试细节 | DEBUG 记录调试细节 | 污染生产日志 |

**8. 异常日志级别选择原则**

**核心原则**：客户端/用户导致的错误 = WARN，系统内部错误 = ERROR

**使用 WARN 的场景**（可预期的业务错误）：
- 令牌过期、无效、格式错误、签名验证失败
- 参数验证失败
- 认证失败（用户名密码错误）
- 资源不存在、权限不足
- 可降级的依赖失败（如 Redis 不可用但可降级到数据库）

**使用 ERROR 的场景**（系统级错误，需要运维关注）：
- 数据库连接失败
- 必要的外部服务不可用
- 未捕获的未知异常

## 参数校验规范

### 你应该遵循的校验规则

**1. 在 DTO 中使用 JSR-303 注解**
常用注解：
- @NotNull：字段不能为 null
- @NotBlank：字符串不能为 null、空或空白
- @NotEmpty：集合不能为 null 或空
- @Size(min, max)：字符串长度或集合大小验证
- @Min、@Max：数字范围验证
- @Email：邮箱格式验证
- @Pattern：正则表达式验证

**2. 在 Controller 中启用验证**
- 在 @RequestBody 参数上添加 @Valid 或 @Validated
- 在 controller 类上添加 @Validated 以启用方法参数验证
- 在 @PathVariable、@RequestParam 上使用验证注解

**3. 自定义验证注解**
何时创建自定义验证器：
- JSR-303 无法表达的复杂验证逻辑
- 业务规则验证
- 跨字段验证

实现：
- 创建带有 @Constraint 的注解
- 实现 ConstraintValidator 接口
- 在 isValid 方法中添加验证逻辑

**4. 验证错误处理**
- 全局异常处理器捕获 MethodArgumentNotValidException
- 提取字段错误和错误消息
- 以统一的 Result 格式返回验证错误
- 包含每个违规的字段名和错误消息

## 事务管理规范

### 你应该遵循的事务规则

**1. 使用 @Transactional 注解**
放置位置：
- 在服务层方法上（Application Service 或 Domain Service）
- 永远不要在 Controller 方法上
- 永远不要在 Repository 方法上

**2. 事务属性**
必需的属性：
- 写操作使用 `rollbackFor = Exception.class`（确保所有异常都触发回滚）
- 查询操作使用 `readOnly = true`（优化）

可选属性：
- `propagation`：事务传播行为（默认 REQUIRED 通常就够了）
- `isolation`：事务隔离级别（除非有特定要求，否则使用默认值）
- `timeout`：事务超时时间（秒）（用于长时间运行的操作）

**3. 事务范围最佳实践**
- 尽可能缩短事务时间
- 不要在事务中调用远程服务
- 不要在事务中执行重计算
- 不要在事务中进行文件 I/O
- 最小化事务内的数据库操作

**4. 事务失效场景**
@Transactional 不起作用的常见原因：
- 方法不是 public（Spring AOP 需要 public 方法）
- 同类方法调用（this.method()）绕过代理
- 异常被捕获且未重新抛出
- 数据库不支持事务（某些存储引擎）
- 错误的异常类型（受检异常需要显式 rollbackFor）

## 性能优化规范

### 你应该遵循的性能优化规则

**1. 策略性使用缓存**
何时缓存：
- 频繁访问的数据
- 昂贵的计算结果
- 不经常变化的数据库查询结果

缓存策略：
- 读操作使用 @Cacheable
- 更新操作使用 @CachePut
- 删除操作使用 @CacheEvict
- 设置适当的 TTL（生存时间）
- 为关键数据实现缓存预热

缓存注意事项：
- 防止缓存穿透（缓存 null 值，设置短 TTL）
- 防止缓存击穿（对热点键使用锁）
- 防止缓存雪崩（随机化过期时间）

**2. 异步处理**
使用 @Async 的场景：
- 发送邮件或短信
- 文件上传/下载处理
- 日志记录（非关键）
- 通知分发

要求：
- 使用 @EnableAsync 启用异步
- 配置自定义线程池
- 正确处理异步异常
- 不要将异步用于关键业务逻辑

**3. 批量操作**
何时使用批量操作：
- 插入多条记录
- 更新多条记录
- 批量删除操作

好处：
- 减少数据库往返次数
- 提高吞吐量
- 更好的资源利用

实现：
- 使用 MyBatis-Plus 批量方法
- 使用 JDBC 批量更新
- 考虑批量大小（通常 100-1000 条记录）

**4. 数据库优化**
- 使用适当的索引
- 避免 N+1 查询问题
- 对关联使用延迟加载
- 优化慢查询
- 如需要可使用读写分离

**5. 连接池配置**
正确配置：
- 数据库连接池（HikariCP）
- Redis 连接池
- HTTP 连接池（RestTemplate、WebClient）

关键设置：
- 最大池大小（基于预期负载）
- 最小空闲连接数
- 连接超时
- 空闲超时
- 最大生命周期

## 安全规范

### 你应该遵循的安全规则

**1. 输入验证**
- 验证所有外部输入（参数、头部、文件）
- 使用白名单验证（不是黑名单）
- 清理用户输入以防止注入攻击
- 验证文件上传（类型、大小、内容）

**2. 认证和授权**
- 使用 Spring Security 或 JWT 进行认证
- 实现基于角色的访问控制（RBAC）
- 使用 @PreAuthorize 或 @Secured 进行方法级安全控制
- 保护敏感端点
- 实现适当的会话管理

**3. 敏感信息保护**
- 使用 BCrypt 加密密码（永远不要明文或 MD5）
- 不要在日志中记录敏感信息
- 不要在 API 响应中返回敏感数据
- 显示时屏蔽敏感数据
- 使用 HTTPS 进行数据传输

**4. SQL 注入防护**
- 始终使用参数化查询
- 永远不要用用户输入拼接 SQL 字符串
- 使用 MyBatis-Plus 或 JPA（它们防止 SQL 注入）
- 验证和清理输入

**5. XSS 防护**
- 在 HTML 中渲染时转义用户输入
- 使用内容安全策略（CSP）头
- 验证和清理富文本内容
- 在不同上下文使用适当的编码

**6. CSRF 保护**
- 在 Spring Security 中启用 CSRF 保护
- 对状态改变操作使用 CSRF 令牌
- 在服务器端验证 CSRF 令牌

## 测试规范

### 你应该遵循的测试规则

**1. 单元测试**
- 使用 JUnit 5（Jupiter）
- 使用 Mockito 模拟依赖
- 目标代码覆盖率 >80%
- 隔离测试单个单元（方法/类）

**2. 测试命名**
模式：`should_期望结果_when_条件`
- 示例：`should_returnUser_when_userExists`
- 示例：`should_throwException_when_userNotFound`

**3. 测试结构**
遵循 AAA 模式：
- Arrange：设置测试数据和 mock
- Act：执行被测方法
- Assert：验证结果

**4. 集成测试**
- 使用 @SpringBootTest 获得完整上下文
- 测试完整的业务流程
- 使用真实数据库测试（testcontainers）
- 验证各层之间的集成

**5. 测试检查清单**
- [ ] 所有 public 方法都有单元测试
- [ ] 测试了边界情况（null、空、边界值）
- [ ] 测试了异常场景
- [ ] 适当使用了 mock（不要测试 mock）
- [ ] 测试是独立的（无共享状态）
- [ ] 测试是确定性的（总是相同结果）
- [ ] 测试名称清楚描述了测试内容

## 代码质量检查清单

编写 Spring Boot 代码时，请验证：

### 架构检查
- [ ] 遵循分层架构（Controller → Service → Repository）
- [ ] 使用构造器注入（不是字段注入）
- [ ] 配置类在 config 包中
- [ ] 适当的包组织

### Controller 检查
- [ ] 使用 RESTful 风格
- [ ] 统一返回格式（Result<T>）
- [ ] 参数验证（@Valid）
- [ ] Controller 中没有业务逻辑
- [ ] 适当的异常处理

### Service 检查
- [ ] 接口与实现分离
- [ ] 使用正确设置的 @Transactional
- [ ] 事务范围最小化
- [ ] 异常处理正确
- [ ] 业务逻辑在适当的层

### 配置检查
- [ ] 使用 application.yml（不是 .properties）
- [ ] 设置了多环境配置
- [ ] 敏感信息加密或外部化
- [ ] 使用了配置属性绑定

### 日志检查
- [ ] 使用 @Slf4j 注解
- [ ] 适当的日志级别
- [ ] 没有记录敏感信息
- [ ] 使用占位符（不是字符串拼接）
- [ ] 异常日志包含堆栈跟踪

### 安全检查
- [ ] 实现了输入验证
- [ ] 配置了认证和授权
- [ ] 密码加密（BCrypt）
- [ ] 防止了 SQL 注入
- [ ] 保护了敏感数据

### 性能检查
- [ ] 实现了缓存策略
- [ ] 适当使用了异步处理
- [ ] 批量数据使用批量操作
- [ ] 配置了连接池
- [ ] 优化了数据库查询

## 总结

遵循这些 Spring Boot 最佳实践可以确保：
- ✅ 清晰可维护的代码结构
- ✅ 团队间一致的开发标准
- ✅ 高性能的应用程序
- ✅ 保护用户数据的安全系统
- ✅ 易于测试的代码库
- ✅ 快速开发且少出错
