# 会话互斥功能验证指南

**文档目的**: 详细说明如何通过多层次测试验证会话互斥功能
**创建日期**: 2025-11-24
**作者**: AI Assistant

---

## 1. 会话互斥功能需求

### 1.1 业务需求

**需求 ID**: REQ-FR-009

**用户故事**:
> 作为系统管理员，我希望同一用户只能有一个活跃会话，以便防止账号共享和提高安全性。

**验收标准**:
1. 当用户在新设备登录时，系统应使旧设备的会话失效
2. 旧设备的会话失效后，访问时应显示"您的账号已在其他设备登录"
3. 新设备登录时应记录登录信息到审计日志
4. 旧会话失效时应记录会话失效事件到审计日志

### 1.2 技术实现

**会话互斥逻辑**:
```java
public void handleSessionMutex(Account account, Session newSession) {
    // 1. 从 Redis 删除该用户的所有旧会话
    sessionCache.deleteByUserId(account.getId());

    // 2. 从 MySQL 删除该用户的所有旧会话（降级方案）
    sessionRepository.deleteByUserId(account.getId());

    // 注意: 新会话还未保存，只有在 Application 层保存新会话后，
    // 该用户才会有唯一的活跃会话
}
```

---

## 2. 多层次验证体系

会话互斥功能需要在 **3 个层次** 进行验证：

```
┌─────────────────────────────────────────────────────────┐
│  第 1 层: Domain 层单元测试                               │
│  验证内容: handleSessionMutex 核心业务逻辑                │
│  验证方法: Mock Repository/Cache，验证方法调用             │
│  测试数量: 5 个测试                                       │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  第 2 层: Application 层单元测试                          │
│  验证内容: forceLogoutOthers 流程编排                     │
│  验证方法: Mock Domain Service，验证方法调用顺序          │
│  测试数量: 2 个测试                                       │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  第 3 层: 集成测试（未实现）                              │
│  验证内容: 端到端业务流程                                 │
│  验证方法: 真实数据库/Redis，验证实际数据状态             │
│  测试数量: 待实现                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 3. 第 1 层：Domain 层验证（✅ 已实现）

### 3.1 测试位置

- **文件**: `domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImplSessionTest.java`
- **测试类**: `HandleSessionMutexTest`
- **测试数量**: 5 个

### 3.2 测试场景

#### 测试 1: 应该使旧会话失效

**测试方法**: `shouldInvalidateOldSession`

**测试代码**:
```java
@Test
@DisplayName("应该使旧会话失效")
void shouldInvalidateOldSession() {
    // Given - 准备新会话
    Session newSession = new Session(
        "new-session-id",
        testAccount.getId(),
        "new-jwt-token",
        LocalDateTime.now().plusHours(2),
        testDeviceInfo,
        LocalDateTime.now()
    );

    // When - 执行会话互斥
    authDomainService.handleSessionMutex(testAccount, newSession);

    // Then - 验证删除操作被调用
    verify(sessionCache, times(1)).deleteByUserId(testAccount.getId());
    verify(sessionRepository, times(1)).deleteByUserId(testAccount.getId());
}
```

**验证点**:
- ✅ 验证 `sessionCache.deleteByUserId` 被调用 1 次
- ✅ 验证 `sessionRepository.deleteByUserId` 被调用 1 次
- ✅ 验证传入的 userId 正确

**验证的业务逻辑**:
1. 会话互斥会删除 Redis 缓存中的旧会话
2. 会话互斥会删除 MySQL 数据库中的旧会话（降级方案）
3. 删除操作针对特定用户ID

#### 测试 2: 没有旧会话时的行为

**测试方法**: `shouldNotDeleteWhenNoOldSession`

**验证点**:
- ✅ 即使没有旧会话，删除方法仍然被调用（幂等性）
- ✅ 不会因为没有旧会话而抛出异常

#### 测试 3: 空账号应该抛出异常

**测试方法**: `shouldThrowExceptionForNullAccount`

**验证点**:
- ✅ 当 account 为 null 时，抛出 IllegalArgumentException
- ✅ 异常消息包含 "Account cannot be null"

#### 测试 4: 账号ID为空应该抛出异常

**测试方法**: `shouldThrowExceptionForAccountWithoutId`

**验证点**:
- ✅ 当 account.getId() 为 null 时，抛出 IllegalArgumentException
- ✅ 异常消息包含 "Account ID cannot be null"

#### 测试 5: 空会话应该抛出异常

**测试方法**: `shouldThrowExceptionForNullSession`

**验证点**:
- ✅ 当 session 为 null 时，抛出 IllegalArgumentException
- ✅ 异常消息包含 "Session cannot be null"

### 3.3 执行测试

```bash
cd /Users/catface/Documents/GitHub/AWS/aiops-service/domain/domain-impl
mvn test -Dtest=AuthDomainServiceImplSessionTest
```

**测试结果**:
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Time elapsed: 0.653 s
✅ HandleSessionMutexTest - 5 个测试全部通过
```

### 3.4 Domain 层验证的局限性

**已验证** ✅:
- deleteByUserId 方法被正确调用
- 参数传递正确
- 异常处理正确

**未验证** ❌:
- 实际的数据库删除操作（使用了 Mock）
- 实际的 Redis 删除操作（使用了 Mock）
- 多个旧会话是否都被删除（Mock 无法验证）

---

## 4. 第 2 层：Application 层验证（✅ 已实现）

### 4.1 测试位置

- **文件**: `application/application-impl/src/test/java/com/catface996/aiops/application/impl/service/auth/AuthApplicationServiceImplTest.java`
- **测试方法**: `testForceLogoutOthersSuccess`
- **测试数量**: 2 个

### 4.2 测试场景

#### 测试 1: 强制登出其他设备成功

**测试代码**:
```java
@Test
@DisplayName("强制登出其他设备成功")
void testForceLogoutOthersSuccess() {
    // Given - 准备测试数据
    String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    String password = "SecureP@ss123";

    // Mock 各层依赖
    when(authDomainService.validateSession(anyString())).thenReturn(testSession);
    when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
    when(authDomainService.verifyPassword(eq(password), eq(testAccount.getPassword())))
            .thenReturn(true);

    Session newSession = createNewSession();
    when(authDomainService.createSession(eq(testAccount), eq(false), any(DeviceInfo.class)))
            .thenReturn(newSession);
    when(sessionRepository.save(any(Session.class))).thenReturn(newSession);

    // When - 执行强制登出
    ForceLogoutRequest request = ForceLogoutRequest.of(token, password);
    LoginResult result = authApplicationService.forceLogoutOthers(request);

    // Then - 验证返回结果
    assertThat(result).isNotNull();
    assertThat(result.getToken()).isEqualTo("new_jwt_token");
    assertThat(result.getSessionId()).isEqualTo(newSession.getId());

    // 验证方法调用顺序
    InOrder inOrder = inOrder(authDomainService, sessionRepository);
    inOrder.verify(authDomainService).validateSession(anyString());
    inOrder.verify(authDomainService).verifyPassword(eq(password), any());
    inOrder.verify(authDomainService).createSession(any(), eq(false), any());
    inOrder.verify(authDomainService).handleSessionMutex(any(), any());
    inOrder.verify(sessionRepository).save(any());
}
```

**验证点**:
- ✅ 验证方法调用顺序正确（先验证 Token，再验证密码，再创建会话，最后会话互斥）
- ✅ 验证 handleSessionMutex 被调用
- ✅ 验证新会话被保存
- ✅ 验证返回新的 JWT Token

#### 测试 2: 密码错误场景

**测试方法**: `testForceLogoutOthersWithInvalidPassword`

**验证点**:
- ✅ 密码错误时抛出 AuthenticationException
- ✅ 不会调用 createSession
- ✅ 不会调用 handleSessionMutex
- ✅ 不会保存新会话

### 4.3 执行测试

```bash
cd /Users/catface/Documents/GitHub/AWS/aiops-service/application/application-impl
mvn test -Dtest=AuthApplicationServiceImplTest
```

**测试结果**:
```
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
✅ 所有测试通过，包括 2 个强制登出其他设备的测试
```

### 4.4 Application 层验证的局限性

**已验证** ✅:
- 方法调用顺序正确
- handleSessionMutex 被调用
- 流程编排正确
- 异常处理正确

**未验证** ❌:
- 实际的会话互斥效果（使用了 Mock）
- 旧会话是否真的失效
- 新会话是否是唯一活跃的会话

---

## 5. 第 3 层：集成测试验证（❌ 未实现，推荐方案）

### 5.1 为什么需要集成测试？

**单元测试的局限**:
- 使用 Mock，只验证方法调用，不验证实际效果
- 无法验证多个组件协同工作的正确性
- 无法验证真实的数据库/Redis 操作

**集成测试的价值**:
- 使用真实的数据库和 Redis（或 TestContainers）
- 验证端到端的业务流程
- 验证实际的数据状态变化

### 5.2 推荐的集成测试方案

#### 方案 1: 使用 TestContainers

**测试代码示例**:
```java
@SpringBootTest
@Testcontainers
@DisplayName("会话互斥集成测试")
class SessionMutexIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @Autowired
    private AuthApplicationService authApplicationService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionCache sessionCache;

    @Test
    @DisplayName("强制登出其他设备应该使所有旧会话失效")
    void shouldInvalidateAllOldSessions() {
        // Given - 用户在 3 个设备上登录
        RegisterRequest registerReq = new RegisterRequest(
            "testuser", "test@example.com", "SecureP@ss123"
        );
        authApplicationService.register(registerReq);

        // 设备 1 登录
        LoginRequest device1Login = LoginRequest.of("testuser", "SecureP@ss123", false);
        LoginResult device1Result = authApplicationService.login(device1Login);
        String device1Token = device1Result.getToken();

        // 设备 2 登录（会使设备 1 失效）
        LoginResult device2Result = authApplicationService.login(device1Login);
        String device2Token = device2Result.getToken();

        // 设备 3 登录（会使设备 2 失效）
        LoginResult device3Result = authApplicationService.login(device1Login);
        String device3Token = device3Result.getToken();

        // 验证初始状态：只有设备 3 的会话有效
        assertSessionValid(device3Token);
        assertSessionInvalid(device1Token);
        assertSessionInvalid(device2Token);

        // When - 在设备 4 上强制登出其他设备
        ForceLogoutRequest forceLogoutReq = ForceLogoutRequest.of(
            device3Token,
            "SecureP@ss123"
        );
        LoginResult device4Result = authApplicationService.forceLogoutOthers(forceLogoutReq);
        String device4Token = device4Result.getToken();

        // Then - 验证结果
        // 1. 所有旧设备的会话都应该失效
        assertSessionInvalid(device1Token);
        assertSessionInvalid(device2Token);
        assertSessionInvalid(device3Token);

        // 2. 只有设备 4 的会话有效
        assertSessionValid(device4Token);

        // 3. 数据库中只有 1 个活跃会话
        Long userId = device4Result.getUserInfo().getAccountId();
        List<Session> activeSessions = sessionRepository.findByUserId(userId);
        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getToken()).isEqualTo(device4Token);

        // 4. Redis 中只有 1 个活跃会话
        Optional<Session> cachedSession = sessionCache.get(device4Result.getSessionId());
        assertThat(cachedSession).isPresent();
        assertThat(cachedSession.get().getToken()).isEqualTo(device4Token);
    }

    private void assertSessionValid(String token) {
        SessionValidationResult result = authApplicationService.validateSession(token);
        assertThat(result.isValid()).isTrue();
    }

    private void assertSessionInvalid(String token) {
        SessionValidationResult result = authApplicationService.validateSession(token);
        assertThat(result.isValid()).isFalse();
    }
}
```

**验证点**:
- ✅ 多个旧会话都被失效
- ✅ 新会话是唯一活跃的会话
- ✅ 数据库中只有 1 个活跃会话
- ✅ Redis 中只有 1 个活跃会话
- ✅ 旧 Token 无法验证通过

#### 方案 2: 手动测试（临时方案）

如果暂时无法编写集成测试，可以通过以下手动步骤验证：

**步骤 1**: 启动应用
```bash
mvn spring-boot:run
```

**步骤 2**: 用户注册
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecureP@ss123"
  }'
```

**步骤 3**: 设备 1 登录
```bash
TOKEN1=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "SecureP@ss123",
    "rememberMe": false
  }' | jq -r '.data.token')

echo "Device 1 Token: $TOKEN1"
```

**步骤 4**: 验证设备 1 会话有效
```bash
curl -X GET http://localhost:8080/api/v1/session/validate \
  -H "Authorization: Bearer $TOKEN1"

# 预期: {"code":0,"message":"success","data":{"valid":true,...}}
```

**步骤 5**: 设备 2 登录（应该使设备 1 失效）
```bash
TOKEN2=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "SecureP@ss123",
    "rememberMe": false
  }' | jq -r '.data.token')

echo "Device 2 Token: $TOKEN2"
```

**步骤 6**: 验证设备 1 会话已失效
```bash
curl -X GET http://localhost:8080/api/v1/session/validate \
  -H "Authorization: Bearer $TOKEN1"

# 预期: {"code":401,"message":"会话已失效或过期","data":{"valid":false}}
```

**步骤 7**: 验证设备 2 会话有效
```bash
curl -X GET http://localhost:8080/api/v1/session/validate \
  -H "Authorization: Bearer $TOKEN2"

# 预期: {"code":0,"message":"success","data":{"valid":true,...}}
```

**步骤 8**: 强制登出其他设备
```bash
TOKEN3=$(curl -X POST http://localhost:8080/api/v1/session/force-logout-others \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "SecureP@ss123"
  }' | jq -r '.data.token')

echo "Device 3 Token: $TOKEN3"
```

**步骤 9**: 验证设备 2 会话已失效
```bash
curl -X GET http://localhost:8080/api/v1/session/validate \
  -H "Authorization: Bearer $TOKEN2"

# 预期: {"code":401,"message":"会话已失效或过期","data":{"valid":false}}
```

**步骤 10**: 验证设备 3 会话有效
```bash
curl -X GET http://localhost:8080/api/v1/session/validate \
  -H "Authorization: Bearer $TOKEN3"

# 预期: {"code":0,"message":"success","data":{"valid":true,...}}
```

**步骤 11**: 查询数据库验证
```sql
-- 查询该用户的所有会话
SELECT * FROM session WHERE user_id = (
    SELECT id FROM account WHERE username = 'testuser'
);

-- 预期: 只有 1 条记录，对应 TOKEN3
```

**步骤 12**: 查询 Redis 验证
```bash
# 连接 Redis
redis-cli

# 查询该用户的会话
KEYS session:*

# 预期: 只有 1 个 key
```

---

## 6. 验证体系总结

### 6.1 验证覆盖矩阵

| 验证内容 | Domain 层 | Application 层 | 集成测试 |
|---------|-----------|---------------|---------|
| deleteByUserId 被调用 | ✅ | ✅ | ✅ |
| 方法调用顺序正确 | N/A | ✅ | ✅ |
| 旧会话实际被删除 | ❌ | ❌ | ✅ |
| 新会话是唯一活跃的 | ❌ | ❌ | ✅ |
| 多个旧会话都被删除 | ❌ | ❌ | ✅ |
| Redis 数据状态正确 | ❌ | ❌ | ✅ |
| MySQL 数据状态正确 | ❌ | ❌ | ✅ |
| 端到端业务流程 | ❌ | ❌ | ✅ |

### 6.2 当前验证状态

**已实现** ✅:
- Domain 层：5 个单元测试，验证核心逻辑
- Application 层：2 个单元测试，验证流程编排

**未实现** ❌:
- 集成测试：验证实际的数据状态和端到端流程

### 6.3 验证充分性评估

**单元测试层面**: ⭐⭐⭐⭐⭐ (5/5)
- 覆盖了所有代码路径
- 验证了方法调用
- 验证了异常处理
- 验证了参数传递

**业务逻辑验证**: ⭐⭐⭐ (3/5)
- 验证了方法被调用（✅）
- 验证了调用顺序（✅）
- 未验证实际效果（❌）
- 未验证数据状态（❌）

**总体评估**: ⭐⭐⭐⭐ (4/5)
- 单元测试覆盖充分
- 建议补充集成测试以验证端到端效果

---

## 7. 改进建议

### 7.1 短期改进（推荐）

1. **添加集成测试**
   - 使用 TestContainers 启动真实的 MySQL 和 Redis
   - 验证端到端的会话互斥效果
   - 验证实际的数据状态

2. **增强单元测试**
   - 在 Domain 层测试中，使用 ArgumentCaptor 捕获传入的参数
   - 验证 deleteByUserId 的参数值正确

### 7.2 长期优化

1. **性能测试**
   - 测试并发场景下的会话互斥
   - 测试多设备同时登录的竞态条件

2. **可观测性**
   - 添加会话互斥的监控指标
   - 记录旧会话数量到审计日志

---

## 8. 总结

### 8.1 当前验证方法的优点

✅ **优点**:
1. **快速执行**: 单元测试运行速度快（< 1 秒）
2. **稳定可靠**: 不依赖外部环境，测试结果稳定
3. **易于调试**: 问题定位清晰，容易修复
4. **覆盖充分**: 覆盖了所有代码路径和异常场景

### 8.2 当前验证方法的局限

❌ **局限**:
1. **使用 Mock**: 只验证方法调用，不验证实际效果
2. **无法验证数据状态**: 无法验证 Redis/MySQL 中的数据是否正确
3. **无法验证端到端流程**: 无法验证多个组件协同工作的正确性
4. **无法发现集成问题**: 可能存在单元测试通过但集成失败的情况

### 8.3 推荐的验证策略

```
单元测试（已实现）+ 集成测试（推荐实现）= 完整的验证体系
```

**比例建议**:
- 单元测试：70%（快速反馈，覆盖所有路径）
- 集成测试：30%（验证端到端，覆盖关键场景）

---

**文档版本**: v1.0.0
**最后更新**: 2025-11-24
**审核状态**: 待审核
