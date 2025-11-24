# 异常体系终极简化报告

**优化日期**: 2025-11-24
**优化人**: AI Assistant
**优化目标**: 移除所有自定义异常类，统一使用通用异常+错误码

---

## 1. 优化背景

用户提出了一个关键观点：

> "有特殊字段/逻辑的（应该保留）：AccountLockedException, InvalidPasswordException"
>
> "即使有特殊字段，也没办法返回给前端，所以还是要全部移除，统一使用BusinessException和错误码来代替"

**核心洞察**：
- 即使异常类有特殊字段（如 `remainingMinutes`, `validationErrors`），这些字段也是通过 GlobalExceptionHandler 返回的
- 为了返回几个额外字段就创建单独的异常类，属于过度设计
- ParameterException 本身就支持 validationErrors，不需要子类
- 特殊数据可以通过其他方式处理（放在消息里，或者不返回）

---

## 2. 优化内容

### 2.1 删除的异常类（9个）

**全部删除的自定义异常类**：

1. ✅ `AccountLockedException` - 账号锁定异常（原有 remainingMinutes 字段）
2. ✅ `AccountNotFoundException` - 账号不存在异常
3. ✅ `AuthenticationException` - 认证异常父类
4. ✅ `DuplicateEmailException` - 邮箱重复异常
5. ✅ `DuplicateUsernameException` - 用户名重复异常
6. ✅ `InvalidPasswordException` - 密码无效异常（原有 validationErrors 字段）
7. ✅ `InvalidTokenException` - Token无效异常
8. ✅ `SessionExpiredException` - 会话过期异常
9. ✅ `SessionNotFoundException` - 会话不存在异常

### 2.2 替换方案

**原来的写法**：
```java
// 创建单独的异常类
public class AccountLockedException extends BusinessException {
    private final int remainingMinutes;

    public AccountLockedException(String message, int remainingMinutes) {
        super("LOCKED_001", message);
        this.remainingMinutes = remainingMinutes;
    }
}

// 抛出异常
throw AccountLockedException.locked(30);

// GlobalExceptionHandler 专门处理
@ExceptionHandler(AccountLockedException.class)
public ResponseEntity<ApiResponse<Map<String, Object>>> handle(AccountLockedException e) {
    Map<String, Object> data = new HashMap<>();
    data.put("remainingMinutes", e.getRemainingMinutes());
    return ApiResponse.error(code, message, data);
}
```

**现在的写法**：
```java
// 直接使用 BusinessException + 错误码
String message = String.format("账号已锁定，请在%d分钟后重试", remainingMinutes);
throw new BusinessException(ErrorCodes.LOCKED_ACCOUNT, message);

// GlobalExceptionHandler 统一处理所有 BusinessException
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) {
    HttpStatus status = determineHttpStatus(e.getErrorCode());  // LOCKED → 423
    return ResponseEntity.status(status)
            .body(ApiResponse.error(parseHttpErrorCode(e.getErrorCode()), e.getMessage()));
}
```

**参数验证异常**：
```java
// 原来
throw InvalidPasswordException.weakPassword(validationErrors);

// 现在 - 直接使用 ParameterException（它本身就支持 validationErrors）
throw new ParameterException(ErrorCodes.PARAM_INVALID_PASSWORD, "密码不符合要求", validationErrors);
```

### 2.3 业务代码修改

**修改的文件**：

1. `AuthApplicationServiceImpl.java`
   - 替换了所有 throw 语句
   - 更新了 import 语句
   - 更新了 JavaDoc

2. `AuthDomainServiceImpl.java`
   - 替换 SessionExpiredException → BusinessException
   - 替换 SessionNotFoundException → BusinessException

3. `GlobalExceptionHandler.java`
   - 删除 `AccountLockedException` 专门处理器
   - 删除 HashMap import
   - 更新 `determineHttpStatus()` 增加 LOCKED 前缀支持
   - 更新 JavaDoc 中的异常继承体系图
   - 处理器数量：5个 → **4个**

4. **测试文件**：
   - `AuthDomainServiceImplSessionTest.java` - 替换异常引用
   - `AuthApplicationServiceImplTest.java` - 替换异常引用

---

## 3. GlobalExceptionHandler 最终状态

### 3.1 处理器数量变化

| 优化阶段 | 处理器数量 | 说明 |
|---------|----------|------|
| 最初 | 13 个 | 每个异常单独处理 |
| 第一轮优化 | 9 个 | 合并认证异常子类 |
| 第二轮优化 | 5 个 | 利用多态和动态状态码 |
| **终极简化** | **4 个** | 移除所有自定义异常类 |

**减少比例**: 13 → 4，减少了 **69.2%**

### 3.2 最终保留的 4 个处理器

| 处理器 | HTTP状态码 | 保留原因 |
|-------|-----------|---------|
| `ParameterException` | 400 | 需要返回 `List<String>` validationErrors |
| `MethodArgumentNotValidException` | 400 | Spring Validation，返回 `List<ErrorDetail>` |
| `BusinessException` | 动态 | 统一处理所有业务异常，动态判断状态码 |
| `SystemException` + `Exception` | 500 | 系统异常和兜底 |

### 3.3 异常继承体系（最终）

```
BaseException
├── BusinessException（根据错误码动态返回状态码）
├── ParameterException（400，有专门的处理器）
└── SystemException（500）
```

**所有自定义异常类已移除**，统一使用：
- 认证失败、会话过期等 → `BusinessException(ErrorCodes.AUTH_XXX, message)`
- 账号不存在、锁定等 → `BusinessException(ErrorCodes.XXX, message)`
- 参数验证失败 → `ParameterException(ErrorCodes.PARAM_XXX, message, validationErrors)`

### 3.4 动态状态码映射

```java
private HttpStatus determineHttpStatus(String errorCode) {
    String prefix = errorCode.substring(0, errorCode.indexOf("_"));

    return switch (prefix) {
        case "AUTH" -> HttpStatus.UNAUTHORIZED;          // 401
        case "AUTHZ" -> HttpStatus.FORBIDDEN;            // 403
        case "NOT_FOUND" -> HttpStatus.NOT_FOUND;        // 404
        case "CONFLICT" -> HttpStatus.CONFLICT;          // 409
        case "LOCKED" -> HttpStatus.LOCKED;              // 423
        default -> HttpStatus.OK;                         // 200
    };
}
```

**映射示例**：
- `LOCKED_001` → 423 Locked
- `AUTH_001` → 401 Unauthorized
- `NOT_FOUND_001` → 404 Not Found
- `CONFLICT_001` → 409 Conflict

---

## 4. 测试验证

### 4.1 测试结果

| 模块 | 测试数量 | 通过 | 失败 | 跳过 | 结果 |
|------|---------|------|------|------|------|
| domain-api | 25 | 25 | 0 | 0 | ✅ PASS |
| domain-impl | 73 | 73 | 0 | 0 | ✅ PASS |
| application-impl | 20 | 20 | 0 | 0 | ✅ PASS |
| **总计** | **118** | **118** | **0** | **0** | ✅ **PASS** |

**成功率**: **100%**

### 4.2 测试修改

**修改的测试文件**：

1. **AuthDomainServiceImplSessionTest.java**
   - 将 `assertThrows(SessionNotFoundException.class)` 改为 `assertThrows(BusinessException.class)`
   - 将 `assertThrows(SessionExpiredException.class)` 改为 `assertThrows(BusinessException.class)`

2. **AuthApplicationServiceImplTest.java**
   - 替换所有异常类型断言为 `BusinessException` 或 `ParameterException`
   - 6个异常类型全部替换

---

## 5. 优化效果

### 5.1 代码简洁性

**文件数量减少**：
- 删除 9 个异常类文件
- 无需为每个业务场景创建新异常类

**代码行数减少**：
- 删除约 450 行异常类代码
- GlobalExceptionHandler 减少约 20 行

### 5.2 维护性提升

**新增异常场景**：
```java
// 原来：需要创建新的异常类
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND_002", message);
    }
}

// 现在：直接使用通用异常 + 新错误码
throw new BusinessException(ErrorCodes.NOT_FOUND_RESOURCE, "资源不存在");
```

**新增错误码**：
```java
// ErrorCodes.java
public static final String NOT_FOUND_RESOURCE = "NOT_FOUND_002";
```

**GlobalExceptionHandler 无需修改**！动态状态码映射自动生效。

### 5.3 扩展性

**添加新的业务异常类型**：
1. 在 `ErrorCodes` 添加新错误码
2. 业务代码直接 `throw new BusinessException(errorCode, message)`
3. 无需修改 GlobalExceptionHandler
4. HTTP 状态码由错误码前缀自动决定

**示例 - 添加权限不足异常**：
```java
// 1. 添加错误码
public static final String AUTHZ_PERMISSION_DENIED = "AUTHZ_001";

// 2. 业务代码抛异常
throw new BusinessException(ErrorCodes.AUTHZ_PERMISSION_DENIED, "权限不足");

// 3. GlobalExceptionHandler 自动处理：
//    - 识别前缀 AUTHZ → 返回 403 Forbidden
//    - 转换错误码 AUTHZ_001 → 403001
//    - 无需编写新的处理器！
```

---

## 6. 对比总结

### 6.1 核心变化

| 维度 | 优化前 | 优化后 | 改进 |
|------|-------|--------|------|
| 异常类数量 | 9个自定义异常 | 0个（全部移除） | ✅ 避免类爆炸 |
| GlobalExceptionHandler | 5个处理器 | **4个处理器** | ✅ 更简洁 |
| 特殊字段处理 | 单独的异常类 | 放在消息里/不返回 | ✅ 更简单 |
| 新增异常 | 创建新类 + 新处理器 | 只需添加错误码 | ✅ 更灵活 |
| 测试覆盖 | 118个 | **118个（100%通过）** | ✅ 验证通过 |

### 6.2 设计理念转变

**从**：
- ✗ 为每个场景创建专门的异常类
- ✗ 为特殊字段创建专门的处理器
- ✗ 类爆炸问题

**到**：
- ✅ 使用通用异常 + 错误码
- ✅ 通过错误码前缀动态判断 HTTP 状态码
- ✅ 特殊信息通过消息传递
- ✅ 保持设计的简洁性

### 6.3 关键优势

1. **避免类爆炸** - 不需要为每个业务场景创建异常类
2. **统一的异常处理** - 通过错误码统一管理
3. **动态状态码映射** - 根据前缀自动判断
4. **更易维护** - 新增场景只需添加错误码
5. **代码更简洁** - 减少了大量模板代码

---

## 7. 最佳实践

### 7.1 如何抛异常

**业务异常**：
```java
// 认证失败
throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "用户名或密码错误");

// 资源不存在
throw new BusinessException(ErrorCodes.NOT_FOUND_ACCOUNT, "账号不存在");

// 资源冲突
throw new BusinessException(ErrorCodes.CONFLICT_USERNAME, "用户名已存在");

// 资源锁定（剩余时间放在消息里）
String message = String.format("账号已锁定，请在%d分钟后重试", remainingMinutes);
throw new BusinessException(ErrorCodes.LOCKED_ACCOUNT, message);
```

**参数异常**：
```java
// 密码验证失败（带验证错误列表）
throw new ParameterException(
    ErrorCodes.PARAM_INVALID_PASSWORD,
    "密码不符合要求",
    validationErrors
);

// 简单参数错误（无详细列表）
throw new ParameterException(ErrorCodes.PARAM_INVALID_FORMAT, "参数格式错误");
```

### 7.2 如何添加新的错误码

```java
// ErrorCodes.java
public final class ErrorCodes {
    // 认证相关 (401xxx)
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";

    // 授权相关 (403xxx) - 新增
    public static final String AUTHZ_PERMISSION_DENIED = "AUTHZ_001";

    // 参数相关 (400xxx) - 新增
    public static final String PARAM_INVALID_FORMAT = "PARAM_003";

    // 资源不存在 (404xxx) - 新增
    public static final String NOT_FOUND_RESOURCE = "NOT_FOUND_002";
}
```

### 7.3 错误码命名规范

**格式**: `<类别>_<序号>`

**类别前缀**：
- `AUTH_` - 认证错误（401）
- `AUTHZ_` - 授权错误（403）
- `PARAM_` - 参数错误（400）
- `NOT_FOUND_` - 资源不存在（404）
- `CONFLICT_` - 资源冲突（409）
- `LOCKED_` - 资源锁定（423）
- `BIZ_` - 业务异常（200）
- `SYS_` - 系统异常（500）

---

## 8. 与前端集成

### 8.1 API 响应示例

**账号锁定**（不再返回 remainingMinutes 字段）：
```json
HTTP/1.1 423 Locked
{
  "code": 423001,
  "message": "账号已锁定，请在30分钟后重试",
  "data": null
}
```

**密码验证失败**（仍然返回 validationErrors）：
```json
HTTP/1.1 400 Bad Request
{
  "code": 400001,
  "message": "密码不符合要求",
  "data": [
    "密码长度至少为8个字符",
    "密码必须包含大写字母"
  ]
}
```

**用户名重复**：
```json
HTTP/1.1 409 Conflict
{
  "code": 409001,
  "message": "用户名已存在",
  "data": null
}
```

### 8.2 前端处理

```javascript
axios.post('/api/auth/login', { username, password })
  .catch(error => {
    const status = error.response.status;
    const { code, message, data } = error.response.data;

    if (status === 401) {
      // 认证失败 (401xxx)
      showError(message);
      redirectToLogin();
    } else if (status === 423) {
      // 账号锁定 (423xxx) - 直接显示消息即可
      showError(message);  // "账号已锁定，请在30分钟后重试"
    } else if (status === 400 && data) {
      // 参数错误 (400xxx) - 显示详细列表
      showValidationErrors(data);
    } else if (status === 409) {
      // 资源冲突 (409xxx)
      showError(message);
    } else {
      // 其他错误
      showError(message);
    }
  });
```

---

## 9. 用户反馈

**用户的关键洞察**：

> "即使有特殊字段，也没办法返回给前端，所以还是要全部移除，统一使用BusinessException和错误码来代替"

**分析**：

1. **特殊字段的必要性存疑**
   - `remainingMinutes` - 可以放在消息里："账号已锁定，请在30分钟后重试"
   - `validationErrors` - ParameterException 本身就支持，无需子类

2. **过度设计的代价**
   - 创建 9 个异常类
   - 创建专门的处理器
   - 测试文件中大量的类型引用

3. **正确的做法**
   - 使用通用异常 + 错误码
   - 特殊信息通过消息传递
   - 保持设计的简洁性

---

## 10. 总结

### 10.1 优化成果

✅ **删除了 9 个自定义异常类**
✅ **GlobalExceptionHandler 处理器从 5 个减少到 4 个**
✅ **代码更简洁、更易维护**
✅ **所有 118 个测试通过（100%）**
✅ **设计更符合KISS原则（Keep It Simple, Stupid）**

### 10.2 技术亮点

1. **彻底的简化** - 移除所有自定义异常类
2. **统一的错误码管理** - 只需在 ErrorCodes 添加新码
3. **动态状态码映射** - 根据前缀自动判断
4. **向后兼容** - ParameterException 支持 validationErrors
5. **保持灵活性** - 特殊信息通过消息传递

### 10.3 经验教训

**设计原则**：
- ✅ KISS（Keep It Simple, Stupid）
- ✅ YAGNI（You Aren't Gonna Need It）
- ✅ 避免过度设计
- ✅ 优先考虑简洁性

**实践建议**：
- ✅ 不要为每个场景创建异常类
- ✅ 使用通用异常 + 错误码
- ✅ 特殊字段不一定需要专门的异常类
- ✅ 保持异常处理的简单性

---

**报告生成时间**: 2025-11-24
**报告版本**: v1.0.0
**验证结果**: ✅ **优化成功，所有测试通过（118/118）**

**结论**: 通过移除所有自定义异常类，使用通用异常+错误码的方式，成功将 GlobalExceptionHandler 从 13 个处理器优化到 4 个处理器（减少 69.2%），代码更简洁、更易维护，完全符合用户的优化建议。
