---
inclusion: manual
---

# 异常处理最佳实践

本文档详细说明 Java/Spring Boot 应用中异常设计、继承体系和处理的最佳实践。

## 核心原则

### 1. 异常继承体系原则

**你必须遵守**：
- ✅ 使用继承体系组织异常类
- ✅ 通过父类统一处理同类异常
- ✅ 异常类必须包含 code 和 message 字段
- ✅ 使用类型安全的错误码枚举（ErrorCode）
- ❌ 不要使用 `catch (ExceptionA | ExceptionB | ExceptionC)` 方式
- ❌ 不要让每个异常直接继承基类
- ❌ 不要使用字符串常量作为错误码（应使用枚举）

**为什么这是最佳实践**：
- 符合面向对象设计原则（开闭原则、里氏替换原则）
- 代码更简洁，易于维护
- 新增异常不需要修改 catch 代码
- 便于统一处理同类异常
- 异常信息规范化，便于前端处理
- 类型安全，编译期检查，避免拼写错误

### 2. 异常分类原则

**你必须遵守的分类**：

```
BaseException（顶层异常基类）
├── BusinessException（业务异常）
│   ├── 认证相关异常
│   ├── 资源冲突异常
│   ├── 资源不存在异常
│   └── 其他业务异常...
├── ParameterException（参数异常）
│   └── 带 validationErrors 列表
└── SystemException（系统异常）
    ├── 数据库异常
    └── 外部服务异常
```

## 错误码枚举最佳实践

### 1. 使用 ErrorCode 接口

**设计原则**：
- ✅ 定义统一的 ErrorCode 接口
- ✅ 所有错误码枚举实现该接口
- ✅ 接口包含 `getCode()` 和 `getMessage()` 方法
- ✅ 错误码为 String 类型（如 "AUTH_001"）
- ✅ 消息为默认提示文本

**文件位置**：`common/src/main/java/.../enums/ErrorCode.java`

### 2. 错误码枚举分类

**你必须遵守的分类**：

| 枚举类 | 用途 | HTTP状态码 | 前缀 |
|--------|------|-----------|------|
| `AuthErrorCode` | 认证相关错误 | 401 | AUTH_ |
| `ParamErrorCode` | 参数验证错误 | 400 | PARAM_ |
| `ResourceErrorCode` | 资源相关错误 | 404/409/423 | NOT_FOUND_/CONFLICT_/LOCKED_ |
| `SystemErrorCode` | 系统错误 | 500 | SYS_ |

**命名约定**：
- 枚举常量名：大写+下划线（INVALID_CREDENTIALS）
- 错误码字符串：类别_序号（AUTH_001）
- 每个枚举值必须有清晰的 JavaDoc
- 必须注明使用场景和 HTTP 状态码
- 参数化消息需注明参数含义（{0} = 剩余分钟数）

### 3. 混合模式构造异常

**你必须支持的4种方式**：

**方式1：使用枚举默认消息**
- 适用场景：标准错误，无需自定义消息
- 优点：最简洁，消息统一管理
- 示例：`throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS)`

**方式2：枚举 + 自定义消息**
- 适用场景：需要覆盖默认消息
- 优点：保持错误码统一，消息可定制
- 示例：`throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "密码错误5次，账号已临时锁定")`

**方式3：枚举 + 参数化消息**
- 适用场景：消息包含动态内容（数字、名称等）
- 优点：消息模板统一，自动格式化
- 使用 MessageFormat.format() 实现
- 消息模板使用 {0}, {1}, {2} 等占位符
- 示例：`throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30)`
  → "账号已锁定，请在30分钟后重试"

**方式4：传统 String-based（向后兼容）**
- 适用场景：遗留代码，逐步迁移
- 不推荐新代码使用
- 示例：`throw new BusinessException("AUTH_001", "用户名或密码错误")`

## 异常基类设计

### BaseException（所有异常的顶层父类）

**文件位置**：`common/src/main/java/.../exception/BaseException.java`

**你必须包含的字段**：
- `errorCode` (String, final) - 错误码
- `errorMessage` (String, final) - 错误消息

**你必须提供的构造函数**：
- `BaseException(ErrorCode errorCode)` - 使用枚举+默认消息
- `BaseException(ErrorCode errorCode, String customMessage)` - 枚举+自定义消息
- `BaseException(ErrorCode errorCode, Object... args)` - 枚举+参数化消息
- `BaseException(String errorCode, String errorMessage)` - 传统方式（兼容）
- 以上所有方法的 Throwable cause 重载版本

**设计要点**：
- ✅ 继承 RuntimeException（非受检异常）
- ✅ 字段设为 final（不可变）
- ✅ 使用 @Getter 注解
- ✅ 提供 formatMessage() 工具方法（使用 MessageFormat）
- ✅ 格式化失败时返回原始模板（容错）

### BusinessException（业务异常）

**文件位置**：`common/src/main/java/.../exception/BusinessException.java`

**设计要点**：
- 继承 BaseException
- 提供与 BaseException 相同的构造函数重载
- 用于可预期的业务错误
- 记录 WARN 级别日志

**使用场景**：
- ✅ 认证失败（用户名密码错误）
- ✅ 资源冲突（用户名已存在）
- ✅ 资源不存在（账号不存在）
- ✅ 账号被锁定、会话过期
- ❌ 不要用于系统级错误

### ParameterException（参数异常）

**文件位置**：`common/src/main/java/.../exception/ParameterException.java`

**特殊字段**：
- `validationErrors` (List<String>) - 验证错误详情列表

**构造函数要求**：
- 支持 ErrorCode 枚举的所有方式
- 支持传入 validationErrors 列表
- 无验证错误时使用空列表

**使用场景**：
- ✅ 密码强度不符合要求（带详细错误列表）
- ✅ 邮箱/手机号格式错误
- ✅ 参数长度超限
- ✅ 枚举值不在允许范围

### SystemException（系统异常）

**文件位置**：`common/src/main/java/.../exception/SystemException.java`

**设计要点**：
- 继承 BaseException
- 提供与 BaseException 相同的构造函数重载
- 用于不可预期的系统错误
- 记录 ERROR 级别日志
- 向用户返回通用错误消息（不暴露内部细节）

**使用场景**：
- ✅ 数据库连接失败
- ✅ Redis/MQ 连接失败
- ✅ 第三方 API 超时
- ❌ 不要用于业务规则验证失败

## 全局异常处理器设计

### 核心原则

**1. 使用继承体系统一处理**
- ✅ 捕获父类自动处理所有子类
- ✅ 减少重复代码
- ✅ 新增异常自动被处理
- ❌ 不要使用多个异常类型的 OR 组合

**2. 动态 HTTP 状态码映射**
- 根据错误码前缀自动判断 HTTP 状态码
- AUTH_ → 401, AUTHZ_ → 403, PARAM_ → 400
- NOT_FOUND_ → 404, CONFLICT_ → 409, LOCKED_ → 423
- SYS_ → 500, 其他业务异常 → 200

**3. 处理器数量最小化**
- 本项目仅需 4 个处理器：
  - `ParameterException` - 返回 validationErrors 列表
  - `MethodArgumentNotValidException` - Spring Validation
  - `BusinessException` - 动态状态码
  - `SystemException` + `Exception` - 系统异常和兜底

### 异常处理器职责

**应该做的**：
- ✅ 捕获所有未处理的异常
- ✅ 记录适当级别的日志
- ✅ 将异常转换为统一的响应格式
- ✅ 映射异常到适当的 HTTP 状态码
- ✅ 保护敏感信息（不暴露内部细节）

**不应该做的**：
- ❌ 实现业务逻辑
- ❌ 调用外部服务
- ❌ 修改数据库
- ❌ 向用户暴露堆栈跟踪

### 日志级别规则

| 异常类型 | 日志级别 | 原因 |
|---------|---------|------|
| BusinessException | WARN | 可预期的业务错误 |
| ParameterException | WARN | 客户端输入错误 |
| SystemException | ERROR | 系统级错误，需要运维关注 |
| Exception（未知异常） | ERROR | 未预期的错误，需要紧急处理 |

### HTTP 状态码映射规则

| 错误码前缀 | HTTP 状态码 | 说明 |
|-----------|------------|------|
| AUTH_ | 401 Unauthorized | 认证失败 |
| AUTHZ_ | 403 Forbidden | 权限不足 |
| PARAM_ | 400 Bad Request | 参数错误 |
| NOT_FOUND_ | 404 Not Found | 资源不存在 |
| CONFLICT_ | 409 Conflict | 资源冲突 |
| LOCKED_ | 423 Locked | 资源被锁定 |
| BIZ_/其他 | 200 OK | 业务异常（通过 code 区分） |
| SYS_ | 500 Internal Server Error | 系统异常 |

## 错误码设计规范

### 错误码格式

**String 格式**（用于异常类）：
- 格式：`{类别}_{顺序号}`
- 示例：`AUTH_001`、`PARAM_001`、`NOT_FOUND_001`

**Integer 格式**（用于 HTTP 响应）：
- 格式：`{HTTP状态码}{3位顺序号}`
- 转换规则：AUTH_001 → 401001, PARAM_001 → 400001
- 示例：`401001`、`400001`、`404001`

### 错误码分类表

| 类别前缀 | HTTP错误码范围 | 说明 |
|---------|---------------|------|
| AUTH_ | 401001-401999 | 认证相关错误 |
| AUTHZ_ | 403001-403999 | 授权相关错误 |
| PARAM_ | 400001-400999 | 参数验证错误 |
| NOT_FOUND_ | 404001-404999 | 资源不存在 |
| CONFLICT_ | 409001-409999 | 资源冲突 |
| LOCKED_ | 423001-423999 | 资源被锁定 |
| BIZ_ | 200001-200999 | 业务异常 |
| SYS_ | 500001-500999 | 系统异常 |

### 错误码管理建议

**废弃的方式**（已标记 @Deprecated）：
- ❌ ErrorCodes 字符串常量类
- ❌ 硬编码错误码字符串
- 仅用于向后兼容，新代码禁止使用

**推荐的方式**：
- ✅ 使用 ErrorCode 枚举
- ✅ 每个枚举值包含 code 和 message
- ✅ 在枚举类中集中管理相关错误
- ✅ 提供清晰的 JavaDoc 文档

## 异常使用最佳实践

### 抛出异常规则

**在 Domain 层**：
- ✅ 直接抛出领域异常
- ✅ 使用错误码枚举
- ✅ 优先使用默认消息
- ✅ 动态内容使用参数化消息
- ✅ 验证失败提供详细错误列表
- ❌ 不要 catch 后重新抛出

**在 Application 层**：
- ✅ 直接抛出异常，不要 catch
- ✅ 让异常自动传播到 GlobalExceptionHandler
- ❌ 不要在主流程中使用 try-catch
- ❌ 不要只为记录日志而 catch

**在 Repository 层**：
- ✅ 转换底层异常为领域异常
- ✅ 保留原始异常作为 cause
- ✅ 添加业务上下文信息
- 示例：DuplicateKeyException → BusinessException(ResourceErrorCode.USERNAME_CONFLICT)

**在 Infrastructure 层（JWT、Redis、HTTP Client 等）**：
- ✅ 必须捕获第三方库异常并转换为业务异常
- ✅ 使用父类统一捕获同类异常（如 JwtException）
- ✅ 保留原始异常作为 cause
- ✅ 区分可预期异常（WARN）和系统异常（ERROR）
- ❌ 不要直接抛出第三方库异常（如 ExpiredJwtException、RedisConnectionFailureException）

### 异常处理规则

**需要 catch 的场景**：
- ✅ 转换异常类型（SQLException → SystemException）
- ✅ 添加上下文信息
- ✅ 释放资源（优先使用 try-with-resources）
- ✅ 降级处理（第三方服务失败使用缓存）

**不应该 catch 的场景**：
- ❌ 只是为了记录日志
- ❌ catch 后原样重新抛出
- ❌ catch 后什么都不做（吞掉异常）
- ❌ catch 后丢失原始异常

**catch 后必须**：
- ✅ 记录日志（包含关键上下文）
- ✅ 保留原始异常（传递 cause）
- ✅ 提供有意义的错误消息
- ❌ 不要暴露敏感信息

## 常见使用模式

### 模式1：资源不存在
- 使用 Optional.orElseThrow()
- 使用 ResourceErrorCode.ACCOUNT_NOT_FOUND 等枚举
- HTTP 404 状态码

### 模式2：参数验证失败
- 收集所有验证错误
- 使用 ParameterException + validationErrors 列表
- 使用 ParamErrorCode 枚举
- HTTP 400 状态码

### 模式3：业务规则检查
- 条件判断后直接抛出
- 使用对应的业务异常枚举
- 动态内容使用参数化消息
- 根据错误码前缀自动映射 HTTP 状态码

### 模式4：数据库异常包装
- catch DataAccessException
- 检查具体错误类型（唯一约束、外键等）
- 转换为合适的 BusinessException 或 SystemException
- 保留原始异常作为 cause

## 迁移指南

### 从字符串常量到枚举

**迁移步骤**：
1. 找到使用 ErrorCodes 常量的地方
2. 导入对应的 ErrorCode 枚举
3. 将字符串常量替换为枚举值
4. 移除 String message 参数（如果使用默认消息）
5. 动态内容改为使用参数化消息

**迁移收益**：
- 编译期类型检查
- IDE 智能提示和重构支持
- 消息模板统一管理
- 代码更简洁

### 向后兼容性

- 保留所有 String-based 构造函数
- ErrorCodes 类标记为 @Deprecated
- 旧代码继续运行，不会 break
- 新代码强制使用枚举

## 与前端集成规范

### 统一响应格式

**ApiResponse 必须包含的字段**：
- `code` (Integer) - 响应码，0表示成功，非0为错误码
- `message` (String) - 响应消息
- `data` (T) - 响应数据（泛型）

**前端处理规则**：
- 判断 code === 0 表示成功
- 根据 code 显示对应的错误提示
- 特殊错误码（如 423001）可触发特殊处理
- HTTP 错误（网络问题）显示通用提示

### 错误信息安全

**你必须遵守**：
- ✅ 业务异常返回友好的用户消息
- ✅ 系统异常返回通用错误消息
- ❌ 不要暴露数据库结构
- ❌ 不要暴露内部路径
- ❌ 不要暴露技术栈细节
- ❌ 不要返回堆栈跟踪给前端

## 检查清单

### 代码审查时验证

**异常定义**：
- [ ] 所有业务异常继承 BusinessException
- [ ] 参数异常继承 ParameterException
- [ ] 系统异常继承 SystemException
- [ ] 不直接继承 BaseException
- [ ] 使用 ErrorCode 枚举而非字符串常量
- [ ] 枚举值有完整的 JavaDoc

**异常使用**：
- [ ] Domain 层直接抛出异常
- [ ] Application 层不在主流程使用 try-catch
- [ ] catch 后保留原始异常（传递 cause）
- [ ] 没有吞掉异常
- [ ] 使用枚举的默认消息或参数化消息
- [ ] 动态内容使用参数而非字符串拼接

**异常处理器**：
- [ ] 使用 @RestControllerAdvice
- [ ] 通过父类统一处理同类异常
- [ ] 记录了适当级别的日志
- [ ] 返回统一的 ApiResponse 格式
- [ ] 不暴露敏感信息
- [ ] 系统异常返回通用消息
- [ ] HTTP 状态码映射正确

**错误码管理**：
- [ ] 错误码使用类型安全的枚举
- [ ] 命名遵循 CATEGORY_SEQUENCE 格式
- [ ] 错误码无重复
- [ ] 枚举按功能分类（Auth/Param/Resource/System）
- [ ] 参数化消息注明参数含义

## 最佳实践总结

**类型安全**：
- 使用 ErrorCode 枚举替代字符串常量
- 编译期检查，避免拼写错误
- IDE 智能提示和重构支持

**简洁易用**：
- 支持 4 种使用方式（默认消息、自定义、参数化、兼容）
- 优先使用默认消息
- 参数化消息自动格式化

**统一规范**：
- 清晰的异常继承体系
- 统一的错误响应格式
- 规范化的错误码管理
- 自动的 HTTP 状态码映射

**安全可靠**：
- 保护系统内部细节
- 区分用户友好消息和技术日志
- 保留完整的异常链
- 向后兼容

遵循这些原则，确保异常处理的一致性、可维护性和用户体验。

## Infrastructure 层异常处理规则

### 通用规则

**你必须遵守**：
- ✅ 捕获第三方库异常后，转换为 BusinessException 或 SystemException
- ✅ 使用父类统一捕获同类异常（如 JwtException 捕获所有 JWT 相关异常）
- ✅ 保留原始异常作为 cause：`throw new BusinessException(ErrorCode.XXX, e)`
- ✅ 需要单独处理的异常放在父类 catch 之前
- ❌ 不要直接 rethrow 第三方库异常

### JWT 令牌异常处理

**异常转换规则**：
| 第三方异常 | 转换为 | ErrorCode |
|-----------|-------|-----------|
| ExpiredJwtException | BusinessException | TOKEN_EXPIRED |
| JwtException（父类，统一捕获其他） | BusinessException | TOKEN_INVALID |
| IllegalArgumentException | BusinessException | TOKEN_INVALID |

**处理顺序**：先捕获 ExpiredJwtException（需单独处理），再用 JwtException 父类捕获其余。

### Redis 缓存异常处理

**降级模式规则**：
- Redis 异常不应阻塞主流程
- 捕获异常后记录 WARN 日志，降级到数据库查询
- 常见异常：RedisConnectionFailureException、RedisCommandTimeoutException

### HTTP Client 异常处理

**异常分类规则**：
| 第三方异常 | 转换为 | 说明 |
|-----------|-------|------|
| HttpClientErrorException (4xx) | BusinessException | 客户端/请求错误 |
| HttpServerErrorException (5xx) | SystemException | 服务端错误 |
| ResourceAccessException | SystemException | 网络连接失败 |

### 数据库异常处理

**异常转换规则**：
| 第三方异常 | 转换为 | ErrorCode |
|-----------|-------|-----------|
| DuplicateKeyException | BusinessException | USERNAME_CONFLICT / EMAIL_CONFLICT |
| DataAccessException（父类） | SystemException | DATABASE_ERROR |

**唯一约束冲突处理**：根据异常消息判断具体是哪个字段冲突，转换为对应的业务错误码。

## Infrastructure 层异常处理检查清单

**代码审查时验证**：

**异常处理**：
- [ ] 第三方库异常已转换为 BusinessException 或 SystemException
- [ ] 使用 ErrorCode 枚举，不使用字符串常量
- [ ] 保留原始异常作为 cause（传递给异常构造函数）
- [ ] 使用父类统一捕获同类异常（如 JwtException、DataAccessException）
- [ ] 可降级的操作（如缓存）不阻塞主流程

**日志规范**（详见 `06-spring-boot-best-practices.zh.md` 日志规范章节）：
- [ ] 日志级别正确：业务错误用 WARN，系统错误用 ERROR
- [ ] 异常日志包含完整堆栈（异常对象作为最后一个参数）
- [ ] 日志包含关键上下文信息（userId、sessionId 等）
- [ ] 不记录敏感信息（密码、完整 token 等）
