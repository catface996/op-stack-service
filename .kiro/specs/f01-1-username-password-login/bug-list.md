# Bug 清单 - 用户名密码登录功能

**项目名称**: 用户名密码登录
**文档版本**: v1.0.0
**创建日期**: 2025-11-26
**最后更新**: 2025-11-26

---

## Bug 状态说明

| 状态 | 说明 |
|-----|------|
| 🔴 NEW | 新发现，待处理 |
| 🟡 IN_PROGRESS | 修复中 |
| 🟢 FIXED | 已修复，待验证 |
| ✅ VERIFIED | 已验证通过 |
| ⬜ WONTFIX | 不修复 |

## Bug 优先级说明

| 优先级 | 说明 |
|-------|------|
| P0 | 致命 - 系统无法使用 |
| P1 | 严重 - 核心功能不可用 |
| P2 | 一般 - 功能受影响但有替代方案 |
| P3 | 轻微 - 不影响主要功能 |

---

## Bug 列表

### BUG-001: 账号锁定检查逻辑错误导致所有账号无法登录

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P0 |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |
| **发现人** | AI Assistant |
| **修复人** | AI Assistant |

**问题描述**:

`AuthApplicationServiceImpl.checkAccountNotLocked()` 方法中的锁定检查逻辑有误。方法使用 `lockInfo.isPresent()` 来判断账号是否被锁定，但 `AuthDomainService.checkAccountLock()` 方法的设计是：
- 账号未锁定时返回 `Optional.of(AccountLockInfo.notLocked())`
- 账号已锁定时返回 `Optional.of(AccountLockInfo.locked(...))`

因此 `isPresent()` 始终返回 `true`，导致所有账号都被误判为已锁定，无法登录。

**复现步骤**:

1. 注册一个新用户
2. 使用正确的用户名和密码登录
3. 系统返回 "账号已锁定，请在0分钟后重试"

**期望行为**:

未被锁定的账号应该能够正常登录。

**实际行为**:

所有账号登录时都提示 "账号已锁定，请在0分钟后重试"。

**根本原因**:

```java
// 错误代码
if (lockInfo.isPresent()) {
    // 所有情况都会进入这里
    throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, ...);
}
```

**修复方案**:

```java
// 正确代码
if (lockInfo.isPresent() && lockInfo.get().isLocked()) {
    throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, ...);
}
```

**影响文件**:

- `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/auth/AuthApplicationServiceImpl.java:343-351`

**验证方法**:

```bash
# 1. 注册新用户
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Xk9#mNp2Qw"}'

# 2. 登录（应返回成功）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser","password":"Xk9#mNp2Qw","rememberMe":false}'
```

---

### BUG-002: 会话验证使用临时sessionId导致验证失败

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P1 |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |
| **发现人** | AI Assistant |
| **修复人** | AI Assistant |

**问题描述**:

会话验证和登出操作时，系统使用硬编码的 `temp-session-id` 而非从 JWT Token 中提取的实际 sessionId。这导致：
1. 会话验证始终返回 "会话无效或已过期"
2. 登出操作无法正确删除实际会话

**复现步骤**:

1. 注册并登录用户，获取 JWT Token
2. 使用该 Token 调用会话验证接口
3. 系统返回 "会话无效或已过期"

**期望行为**:

使用有效 Token 调用会话验证接口应返回用户信息和会话详情。

**实际行为（修复前）**:

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "valid": false,
    "userInfo": null,
    "sessionId": null,
    "expiresAt": null,
    "remainingSeconds": 0,
    "message": "会话无效或已过期"
  },
  "success": true
}
```

**日志证据（修复前）**:

```
会话不存在于Redis，sessionId: temp-session-id
SELECT ... FROM t_session WHERE id=?
Parameters: temp-session-id(String)
```

**根本原因**:

1. `AuthApplicationServiceImpl.parseSessionId()` 方法返回硬编码的 `"temp-session-id"`
2. JWT Token 生成时未包含 sessionId claim

**修复方案（已实施）**:

采用方案1: 在 JWT Token 中添加 sessionId claim，并通过 Domain API 接口提取

**修改文件**:

1. `domain/security-api/.../JwtTokenProvider.java` - 添加新接口方法：
   ```java
   String generateToken(Long userId, String username, String role, String sessionId, boolean rememberMe);
   String getSessionIdFromToken(String token);
   ```

2. `infrastructure/security/jwt-impl/.../JwtTokenProviderImpl.java` - 实现新方法：
   - 在生成Token时添加 sessionId claim
   - 实现从Token提取sessionId的方法

3. `domain/domain-api/.../AuthDomainService.java` - 添加接口方法：
   ```java
   String getSessionIdFromToken(String token);
   ```

4. `domain/domain-impl/.../AuthDomainServiceImpl.java` - 实现接口方法并修改createSession()

5. `application/application-impl/.../AuthApplicationServiceImpl.java` - 修改parseSessionId()：
   ```java
   private String parseSessionId(String token) {
       String actualToken = token;
       if (token != null && token.startsWith("Bearer ")) {
           actualToken = token.substring(7);
       }
       String sessionId = authDomainService.getSessionIdFromToken(actualToken);
       if (sessionId == null) {
           log.warn("无法从 Token 中提取 sessionId，Token 可能不包含 sessionId claim");
       }
       return sessionId;
   }
   ```

**验证结果（修复后）**:

```bash
# 1. 注册用户
curl -X POST http://localhost:8080/api/v1/auth/register ...
# 返回: {"code":0,"message":"操作成功","data":{"accountId":14,...},"success":true}

# 2. 登录用户
curl -X POST http://localhost:8080/api/v1/auth/login ...
# 返回: {"code":0,"data":{"token":"eyJ...", "sessionId":"f15231a5-229b-4e2d-b030-161402bb03b4",...},"success":true}

# 3. 验证会话
curl -X GET http://localhost:8080/api/v1/session/validate -H "Authorization: Bearer eyJ..."
# 返回: {"code":0,"data":{"valid":true,"userInfo":{...},"sessionId":"f15231a5-229b-4e2d-b030-161402bb03b4",...},"success":true}

# 4. 登出
curl -X POST http://localhost:8080/api/v1/auth/logout -H "Authorization: Bearer eyJ..."
# 返回: {"code":0,"message":"登出成功","data":null,"success":true}

# 5. 验证会话（登出后）
curl -X GET http://localhost:8080/api/v1/session/validate -H "Authorization: Bearer eyJ..."
# 返回: {"code":0,"data":{"valid":false,...,"message":"会话无效或已过期"},"success":true}
```

**架构说明**:

修复过程中严格遵循DDD分层架构：
- application-impl 仅依赖 domain-api
- domain-impl 依赖 domain-api 和 security-api
- 通过 Domain Service 接口暴露 Token 解析功能给应用层

---

### BUG-003: 密码强度验证拒绝包含连续数字的密码

| 属性 | 值 |
|-----|-----|
| **状态** | ✅ VERIFIED |
| **优先级** | P3 |
| **发现日期** | 2025-11-26 |
| **修复日期** | 2025-11-26 |
| **发现人** | AI Assistant |
| **修复人** | AI Assistant |

**问题描述**:

密码强度验证过于严格，包含任何4位连续数字（如 "1234", "2345"）的密码都会被拒绝，即使密码整体强度足够。

**复现步骤（修复前）**:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test@1234Ab"}'
```

**期望行为**:

密码 "Test@1234Ab" 包含大写字母、小写字母、数字、特殊字符，长度足够，应该被接受。

**实际行为（修复前）**:

```json
{
  "code": 400001,
  "message": "密码不符合强度要求",
  "data": ["密码过于简单，请使用更复杂的密码"],
  "success": false
}
```

**根本原因**:

`AuthDomainServiceImpl.isWeakPassword()` 方法中的正则表达式过于宽泛：

```java
// 原代码：只要包含4位连续字符就拒绝
private static final Pattern CONSECUTIVE_CHARS_PATTERN =
    Pattern.compile(".*(0123|1234|2345|3456|4567|5678|6789|...).*");
```

**修复方案（已实施）**:

1. 将连续数字/字母的检测阈值从4位提高到6位
2. 将重复字符的检测阈值从6次保持不变
3. 调整常见弱密码的匹配规则，只有当密码等于或以弱密码开头时才拒绝
4. 添加调试日志便于问题排查

**修改文件**:

`domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImpl.java`

```java
// 修复后的代码
// 连续字符检测：只有当连续序列达到6位或以上才拒绝（如 "123456", "abcdef"）
private static final Pattern CONSECUTIVE_DIGITS_PATTERN =
    Pattern.compile(".*(012345|123456|234567|345678|456789|567890).*");
private static final Pattern CONSECUTIVE_LETTERS_PATTERN =
    Pattern.compile(".*(abcdef|bcdefg|...).*");
// 重复字符检测：同一字符连续重复6次以上才拒绝
private static final Pattern REPEATED_CHARS_PATTERN = Pattern.compile(".*(.)\\1{5,}.*");
// 键盘序列检测：常见的键盘行序列
private static final Pattern KEYBOARD_SEQUENCE_PATTERN =
    Pattern.compile(".*(qwerty|qwertyui|asdfgh|asdfghjk|zxcvbn|zxcvbnm).*");
```

**验证结果（修复后）**:

| 测试场景 | 密码示例 | 预期结果 | 实际结果 |
|---------|---------|---------|---------|
| 4位连续数字 | `Test@1234Ab` | ✅ 通过 | ✅ 通过 |
| 5位连续数字 | `Test@12345Ab` | ✅ 通过 | ✅ 通过 |
| 6位连续数字 | `Test@123456A` | ❌ 拒绝 | ❌ 拒绝 |
| 4位重复字符 | `Test@aaaaB1` | ✅ 通过 | ✅ 通过 |
| 6位重复字符 | `Test@aaaaaaB1` | ❌ 拒绝 | ❌ 拒绝 |
| 键盘序列 | `Qwerty@123` | ❌ 拒绝 | ❌ 拒绝 |
| 常见弱密码 | `Admin123@x` | ❌ 拒绝 | ❌ 拒绝 |
| 强密码 | `Xk9#mNp2Qw` | ✅ 通过 | ✅ 通过 |

**新的弱密码规则说明**:

- 包含6位或以上连续数字（如 `123456`）→ 拒绝
- 包含6位或以上连续字母（如 `abcdef`）→ 拒绝
- 包含6次或以上重复字符（如 `aaaaaa`）→ 拒绝
- 包含常见键盘序列（如 `qwerty`）→ 拒绝
- 密码等于或以常见弱密码词开头（如 `password`）→ 拒绝

---

## Bug 统计

| 状态 | 数量 |
|-----|------|
| 🔴 NEW | 0 |
| 🟡 IN_PROGRESS | 0 |
| 🟢 FIXED | 0 |
| ✅ VERIFIED | 3 |
| **总计** | **3** |

| 优先级 | 数量 |
|-------|------|
| P0 (致命) | 1 (已修复) |
| P1 (严重) | 1 (已修复) |
| P2 (一般) | 0 |
| P3 (轻微) | 1 (已修复) |

---

## 修复历史

| 日期 | Bug ID | 操作 | 说明 |
|-----|--------|------|------|
| 2025-11-26 | BUG-001 | 修复 | 修改 checkAccountNotLocked() 方法的判断逻辑 |
| 2025-11-26 | BUG-001 | 验证 | 重新构建并测试登录功能，验证通过 |
| 2025-11-26 | BUG-002 | 新建 | 发现会话验证使用临时sessionId问题 |
| 2025-11-26 | BUG-003 | 新建 | 发现密码强度验证过于严格问题 |
| 2025-11-26 | BUG-002 | 修复 | 在JWT Token中添加sessionId claim，通过Domain API暴露解析功能 |
| 2025-11-26 | BUG-002 | 验证 | 完整验证注册→登录→会话验证→登出→会话失效流程，全部通过 |
| 2025-11-26 | BUG-003 | 修复 | 调整连续字符检测阈值从4位提高到6位，优化弱密码检测规则 |
| 2025-11-26 | BUG-003 | 验证 | 测试多种密码场景，4/5位连续数字可通过，6位以上正确拒绝 |

---

### BUG-004: /health 端点需要认证但应该公开访问

| 属性 | 值 |
|-----|-----|
| **状态** | 🔴 NEW |
| **优先级** | P2 |
| **发现日期** | 2025-11-26 |
| **修复日期** | - |
| **发现人** | 用户 |
| **修复人** | - |

**问题描述**:

`/health` 端点（自定义健康检查接口）在访问时需要认证，但根据设计该端点应该是公开访问的，不需要 JWT Token。

**复现步骤**:

```bash
curl -s http://localhost:8080/health
```

**期望行为**:

返回健康检查状态，无需认证。

**实际行为**:

返回 401 未授权错误，提示需要认证。

**根本原因**:

`SecurityConfig.java` 中的 `permitAll()` 配置只包含了 `/actuator/health`，但没有包含自定义的 `/health` 端点。

**修复方案**:

在 `SecurityConfig.java` 中添加 `/health` 到公开接口列表：

```java
.requestMatchers("/actuator/health", "/actuator/prometheus", "/health").permitAll()
```

**影响文件**:

- `bootstrap/src/main/java/com/catface996/aiops/bootstrap/config/SecurityConfig.java`

---

## Bug 统计

| 状态 | 数量 |
|-----|------|
| 🔴 NEW | 1 |
| 🟡 IN_PROGRESS | 0 |
| 🟢 FIXED | 0 |
| ✅ VERIFIED | 3 |
| **总计** | **4** |

| 优先级 | 数量 |
|-------|------|
| P0 (致命) | 1 (已修复) |
| P1 (严重) | 1 (已修复) |
| P2 (一般) | 1 (待修复) |
| P3 (轻微) | 1 (已修复) |

---

## 修复历史

| 日期 | Bug ID | 操作 | 说明 |
|-----|--------|------|------|
| 2025-11-26 | BUG-001 | 修复 | 修改 checkAccountNotLocked() 方法的判断逻辑 |
| 2025-11-26 | BUG-001 | 验证 | 重新构建并测试登录功能，验证通过 |
| 2025-11-26 | BUG-002 | 新建 | 发现会话验证使用临时sessionId问题 |
| 2025-11-26 | BUG-003 | 新建 | 发现密码强度验证过于严格问题 |
| 2025-11-26 | BUG-002 | 修复 | 在JWT Token中添加sessionId claim，通过Domain API暴露解析功能 |
| 2025-11-26 | BUG-002 | 验证 | 完整验证注册→登录→会话验证→登出→会话失效流程，全部通过 |
| 2025-11-26 | BUG-003 | 修复 | 调整连续字符检测阈值从4位提高到6位，优化弱密码检测规则 |
| 2025-11-26 | BUG-003 | 验证 | 测试多种密码场景，4/5位连续数字可通过，6位以上正确拒绝 |
| 2025-11-26 | BUG-004 | 新建 | /health 端点需要认证但应该公开访问 |

---

## 下一步行动

- [ ] 修复 BUG-004: 在 SecurityConfig 中添加 /health 到公开接口列表

---

**文档维护人**: AI Assistant
**最后更新**: 2025-11-26
