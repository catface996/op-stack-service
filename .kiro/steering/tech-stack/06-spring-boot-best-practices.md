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
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Spring 自动注入，无需 @Autowired
}
```

**不推荐：字段注入**
```java
@Service
public class UserService {
    @Autowired  // 不推荐
    private UserRepository userRepository;
}
```

**为什么推荐构造器注入**：
- 依赖关系明确
- 便于单元测试
- 保证依赖不可变
- 避免循环依赖

## 配置管理规范

### 你应该遵循的配置规则

**1. 使用 application.yml 而不是 application.properties**
- YAML 格式更清晰
- 支持层级结构
- 便于管理

**2. 多环境配置**
- application.yml（公共配置）
- application-dev.yml（开发环境）
- application-test.yml（测试环境）
- application-prod.yml（生产环境）

**3. 配置属性绑定**
- 使用 @ConfigurationProperties 绑定配置
- 不要使用 @Value 注入大量配置

**4. 敏感信息处理**
- 不要在配置文件中明文存储密码
- 使用环境变量或配置中心
- 使用 Jasypt 加密敏感信息

### 配置类规范

**你应该遵循的配置类规则**：

**1. 使用 @Configuration 标注配置类**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 配置 RedisTemplate
    }
}
```

**2. 配置类命名**
- 以 Config 结尾
- 见名知意
- 示例：RedisConfig、MybatisPlusConfig

**3. 配置类位置**
- 放在 config 包下
- 按功能模块组织

## Interface 层规范（HTTP Controller）

### 你应该遵循的 Controller 规则

**1. Controller 位置**
- 放在 Interface 层的 http 模块
- Package：`com.{company}.{system}.http.controller`

**2. 使用 RESTful 风格**
- GET：查询
- POST：创建
- PUT：更新
- DELETE：删除

**3. 统一返回格式**
- 定义在 common 模块
- 所有接口使用统一的 Result 格式

**4. 参数校验**
- 使用 @Valid 或 @Validated 校验参数
- 使用 JSR-303 注解（@NotNull、@NotBlank、@Size 等）

**5. Controller 职责**
- 只负责接收请求和返回响应
- 不包含业务逻辑
- 调用 Application Service 处理业务
- 进行 VO 和 DTO 之间的转换

**6. 统一异常处理**
- 使用 @RestControllerAdvice
- 放在 http 模块的 handler 包下

### Controller 最佳实践

**你应该这样编写 Controller**：

```java
// Interface 层 - http 模块
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserAppService userAppService;  // 调用 Application 层
    
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserDTO dto = userAppService.getUserById(id);
        return Result.success(convertToVO(dto));  // DTO → VO
    }
    
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userAppService.createUser(request);
        return Result.success();
    }
}
```

## Application 层规范

### 你应该遵循的 Application Service 规则

**1. Application Service 位置**
- 接口：application-api 模块
- 实现：application-impl 模块
- Package：`com.{company}.{system}.application.service`

**2. 接口与实现分离**
- 接口命名：`XxxAppService`
- 实现命名：`XxxAppServiceImpl`

**3. 事务管理**
- 使用 @Transactional 管理事务
- 事务范围最小化
- 避免长事务

**4. Application Service 职责**
- 编排业务流程
- 调用 Domain Service（领域服务）
- 调用 Infrastructure 层（Repository、Cache、MQ）
- 进行 DTO 和 Domain Entity 之间的转换
- 不包含核心业务逻辑（业务逻辑在 Domain 层）

**5. 异常处理**
- 抛出业务异常
- 不要吞掉异常
- 使用自定义异常

### Application Service 最佳实践

**你应该这样编写 Application Service**：

```java
// Application API 模块
public interface UserAppService {
    UserDTO getUserById(Long id);
    void createUser(CreateUserRequest request);
}

// Application Impl 模块
@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {
    private final UserRepository userRepository;  // Infrastructure 层
    private final RedisTemplate<String, Object> redisTemplate;  // Infrastructure 层
    private final UserDomainService userDomainService;  // Domain 层（如需要）
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        // 先查缓存
        String key = "user:info:" + id;
        UserDTO cached = (UserDTO) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        
        // 查数据库
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        
        UserDTO dto = convertToDTO(entity);
        
        // 写缓存
        redisTemplate.opsForValue().set(key, dto, 30, TimeUnit.MINUTES);
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // 编排业务流程
        // 1. 调用 Domain Service 处理核心业务逻辑
        // 2. 调用 Repository 保存数据
        UserEntity entity = new UserEntity();
        // 设置属性
        userRepository.save(entity);
    }
}
```

## Domain 层规范

### 你应该遵循的 Domain 规则

**详见**：`05-ddd-multi-module-project-best-practices.md`

**关键原则**：
- Domain 层使用纯业务语言（不使用技术后缀）
- Domain 层不依赖任何框架
- Domain 层不依赖 Infrastructure 层
- 核心业务逻辑在 Domain Service 中实现

## Infrastructure 层规范（Repository）

### 你应该遵循的 Repository 规则

**详见**：`05-ddd-multi-module-project-best-practices.md`

**1. Repository 位置**
- 接口：repository-api 模块
- 实现：mysql-impl 模块（或其他持久化实现）

**2. Entity 和 PO 分离**
- Entity：在 repository-api，纯 POJO，无框架注解
- PO：在 mysql-impl，包含框架注解
- RepositoryImpl 负责 Entity 和 PO 之间的转换

**3. 使用 MyBatis-Plus**
- Mapper 接口操作 PO
- Repository 接口操作 Entity
- 配置类在 mysql-impl 模块

**4. Repository 职责**
- 只负责数据访问
- 不包含业务逻辑
- 提供领域对象的持久化和查询

## 异常处理规范

### 你应该遵循的异常处理规则

**1. 自定义业务异常**
```java
public class BusinessException extends RuntimeException {
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

**2. 全局异常处理**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常");
    }
}
```

**3. 异常处理原则**
- 不要吞掉异常
- 记录异常日志
- 返回友好的错误信息
- 不要暴露敏感信息

## 日志规范

### 你应该遵循的日志规则

**1. 使用 SLF4J + Logback**
- Spring Boot 默认集成
- 不要使用 System.out.println

**2. 日志级别**
- ERROR：错误信息
- WARN：警告信息
- INFO：重要信息
- DEBUG：调试信息
- TRACE：详细信息

**3. 日志内容**
- 记录关键业务操作
- 记录异常信息
- 记录性能指标
- 不要记录敏感信息（密码、身份证号等）

**4. 日志格式**
```java
@Slf4j
@Service
public class UserService {
    public void createUser(CreateUserRequest request) {
        log.info("创建用户，用户名：{}", request.getUsername());
        try {
            // 业务逻辑
            log.info("用户创建成功，用户ID：{}", userId);
        } catch (Exception e) {
            log.error("用户创建失败，用户名：{}", request.getUsername(), e);
            throw e;
        }
    }
}
```

## 参数校验规范

### 你应该遵循的校验规则

**1. 使用 JSR-303 注解**
```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150")
    private Integer age;
}
```

**2. Controller 中启用校验**
```java
@PostMapping
public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
    userService.createUser(request);
    return Result.success();
}
```

**3. 自定义校验注解**
- 复杂校验逻辑使用自定义注解
- 实现 ConstraintValidator 接口

## 事务管理规范

### 你应该遵循的事务规则

**1. 使用 @Transactional 注解**
```java
@Transactional(rollbackFor = Exception.class)
public void createOrder(CreateOrderRequest request) {
    // 业务逻辑
}
```

**2. 事务属性**
- rollbackFor：指定回滚的异常类型（建议 Exception.class）
- readOnly：只读事务（查询操作）
- propagation：事务传播行为
- isolation：事务隔离级别

**3. 事务范围**
- 事务范围最小化
- 避免长事务
- 不要在事务中调用远程服务

**4. 事务失效场景**
- 方法不是 public
- 同类方法调用（使用 this 调用）
- 异常被捕获未抛出
- 数据库不支持事务

## 性能优化规范

### 你应该遵循的性能优化原则

**1. 使用缓存**
- 使用 @Cacheable、@CachePut、@CacheEvict 注解
- 合理设置缓存过期时间
- 防止缓存穿透、击穿、雪崩

**2. 异步处理**
- 使用 @Async 注解
- 配置线程池
- 适合：发送邮件、短信、日志记录

**3. 批量操作**
- 批量插入、批量更新
- 减少数据库交互次数

**4. 懒加载**
- JPA 使用懒加载
- 避免 N+1 查询问题

**5. 连接池配置**
- 数据库连接池（HikariCP）
- Redis 连接池
- HTTP 连接池

## 安全规范

### 你应该遵循的安全规则

**1. 参数校验**
- 所有外部输入都要校验
- 防止 SQL 注入、XSS 攻击

**2. 认证授权**
- 使用 Spring Security 或 JWT
- 接口权限控制

**3. 敏感信息**
- 密码加密存储（BCrypt）
- 不要在日志中记录敏感信息
- 不要在响应中返回敏感信息

**4. HTTPS**
- 生产环境使用 HTTPS
- 配置 SSL 证书

## 测试规范

### 你应该遵循的测试规则

**1. 单元测试**
- 使用 JUnit 5
- 使用 Mockito 模拟依赖
- 测试覆盖率 > 80%

**2. 集成测试**
- 使用 @SpringBootTest
- 测试完整的业务流程

**3. 测试命名**
- 方法名：should_期望结果_when_条件
- 示例：should_returnUser_when_userExists

## 常见错误和纠正方法

### 你应该避免的错误

| 错误类型 | 错误做法 | 正确做法 | 原因 |
|---------|---------|---------|------|
| **字段注入** | @Autowired 在字段上 | 构造器注入 | 不便于测试 |
| **跨层调用** | Controller 直接调用 Repository | Controller → Service → Repository | 违反分层原则 |
| **事务失效** | 同类方法调用 | 注入自身或使用 AopContext | 代理失效 |
| **长事务** | 事务中调用远程服务 | 缩小事务范围 | 性能问题 |
| **异常吞掉** | catch 后不处理 | 记录日志并抛出 | 问题难以排查 |
| **配置硬编码** | 配置写在代码中 | 使用配置文件 | 不便于维护 |
| **不设置过期时间** | 缓存永久有效 | 设置合理的过期时间 | 内存溢出 |
| **SQL 注入** | 字符串拼接 SQL | 使用参数化查询 | 安全风险 |

## 你的检查清单

在编写 Spring Boot 代码时，你应该检查：

### 架构检查
- [ ] 遵循分层架构（Controller → Service → Repository）
- [ ] 使用构造器注入而不是字段注入
- [ ] 启动类在根包下
- [ ] 配置类在 config 包下

### Controller 检查
- [ ] 使用 RESTful 风格
- [ ] 统一返回格式
- [ ] 参数校验（@Valid）
- [ ] 不包含业务逻辑

### Service 检查
- [ ] 接口与实现分离
- [ ] 使用 @Transactional 管理事务
- [ ] 事务范围最小化
- [ ] 异常处理正确

### 配置检查
- [ ] 使用 application.yml
- [ ] 多环境配置
- [ ] 敏感信息加密
- [ ] 配置属性绑定

### 安全检查
- [ ] 参数校验
- [ ] 认证授权
- [ ] 密码加密
- [ ] 不记录敏感信息

### 性能检查
- [ ] 使用缓存
- [ ] 异步处理
- [ ] 批量操作
- [ ] 连接池配置

## 关键原则总结

### 架构原则
1. **约定优于配置**：使用 Spring Boot 默认配置
2. **分层架构**：Controller → Service → Repository
3. **依赖注入**：使用构造器注入
4. **接口与实现分离**：便于扩展和测试

### 开发原则
1. **统一返回格式**：便于前端处理
2. **统一异常处理**：提高代码可维护性
3. **参数校验**：保证数据安全
4. **事务管理**：保证数据一致性

### 性能原则
1. **使用缓存**：减少数据库压力
2. **异步处理**：提高响应速度
3. **批量操作**：减少网络开销
4. **连接池配置**：提高资源利用率

### 安全原则
1. **参数校验**：防止恶意输入
2. **认证授权**：保护接口安全
3. **敏感信息加密**：保护用户隐私
4. **使用 HTTPS**：保证传输安全

## 关键收益

遵循这些规范，可以获得：

- ✅ 清晰的代码结构，便于维护
- ✅ 统一的开发规范，便于团队协作
- ✅ 高性能的应用，提升用户体验
- ✅ 安全的系统，保护用户数据
- ✅ 易于测试的代码，保证质量
- ✅ 快速的开发效率，降低成本
