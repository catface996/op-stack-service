---
inclusion: manual
---

# 测试最佳实践

## 1. 测试分层策略

### 1.1 测试金字塔

```
        /\
       /  \      E2E 测试 (10%)
      /----\     - 验收测试
     /      \    - 冒烟测试
    /--------\   集成测试 (20%)
   /          \  - API 测试
  /------------\ - 组件协作测试
 /              \
/----------------\ 单元测试 (70%)
                   - 业务逻辑测试
                   - 边界条件测试
```

### 1.2 各层测试职责

| 测试类型 | 职责 | 执行频率 | 执行时机 |
|---------|------|---------|---------|
| 单元测试 | 验证单个类/方法的逻辑正确性 | 每次提交 | 开发时、CI |
| 集成测试 | 验证组件间协作和接口契约 | 每次构建 | CI/CD |
| 端到端测试 | 验证完整业务流程 | 每次发布 | 发布前、部署后 |

---

## 2. 端到端测试 (E2E Test)

### 2.1 定义

端到端测试模拟真实用户行为，从 HTTP 入口到数据库的完整链路验证。

### 2.2 特点

```yaml
环境要求: 完整的生产级环境（应用 + MySQL + Redis）
测试入口: HTTP 接口（curl、Postman、k6）
测试数据: 真实数据库中的数据
隔离性: 低（测试间可能相互影响）
执行速度: 慢（秒级）
```

### 2.3 最佳实践

#### 2.3.1 测试脚本组织

```
doc/04-testing/e2e/
├── scripts/
│   ├── auth-e2e-test.sh       # 认证模块 E2E 测试
│   ├── session-e2e-test.sh    # 会话模块 E2E 测试
│   └── full-flow-test.sh      # 完整业务流程测试
├── data/
│   ├── test-users.json        # 测试用户数据
│   └── test-scenarios.json    # 测试场景数据
└── README.md                  # 测试说明文档
```

#### 2.3.2 测试脚本模板

```bash
#!/bin/bash
# E2E 测试脚本模板

set -e  # 遇到错误立即退出

# 配置
BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMESTAMP=$(date +%s)

# 辅助函数
log_info() { echo "[INFO] $1"; }
log_pass() { echo "[PASS] $1"; }
log_fail() { echo "[FAIL] $1"; exit 1; }

# 检查响应
assert_success() {
    local response=$1
    local test_name=$2
    if echo "$response" | grep -q '"success":true'; then
        log_pass "$test_name"
    else
        log_fail "$test_name: $response"
    fi
}

assert_error_code() {
    local response=$1
    local expected_code=$2
    local test_name=$3
    if echo "$response" | grep -q "\"code\":$expected_code"; then
        log_pass "$test_name"
    else
        log_fail "$test_name: expected code $expected_code, got: $response"
    fi
}

# 测试用例
test_user_registration() {
    log_info "测试用户注册..."
    local user="e2e_user_$TIMESTAMP"
    local response=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$user\",\"email\":\"$user@test.com\",\"password\":\"SecureP@ss123\"}")
    assert_success "$response" "用户注册"
}

# 执行测试
main() {
    log_info "开始 E2E 测试..."
    test_user_registration
    # 添加更多测试...
    log_info "E2E 测试完成！"
}

main "$@"
```

#### 2.3.3 测试数据管理

```bash
# 使用唯一时间戳避免数据冲突
USERNAME="test_$(date +%s)_$(( RANDOM % 1000 ))"

# 测试前清理（可选）
# mysql -e "DELETE FROM t_account WHERE username LIKE 'test_%'"

# 测试后清理（可选）
cleanup() {
    # 清理测试数据
    redis-cli KEYS "login:fail:test_*" | xargs redis-cli DEL
}
trap cleanup EXIT
```

### 2.4 适用场景

- **验收测试**: 发布前验证所有功能
- **冒烟测试**: 部署后快速验证核心流程
- **回归测试**: 版本升级后验证兼容性
- **性能基准**: 作为性能测试的基础脚本

---

## 3. 集成测试 (Integration Test)

### 3.1 定义

集成测试验证多个组件/模块之间的协作是否正确，通常在隔离环境中运行。

### 3.2 特点

```yaml
环境要求: TestContainers 自动启动临时容器
测试入口: MockMvc 或 WebTestClient
测试数据: 临时数据库，测试后销毁
隔离性: 高（每次测试环境独立）
执行速度: 中等（容器启动需要 1-2 分钟）
```

### 3.3 最佳实践

#### 3.3.1 测试类组织

```
bootstrap/src/test/java/.../integration/
├── BaseIntegrationTest.java     # 基类（配置 TestContainers）
├── AuthIntegrationTest.java     # 认证集成测试
├── SessionIntegrationTest.java  # 会话集成测试
└── AdminIntegrationTest.java    # 管理员集成测试
```

#### 3.3.2 TestContainers 配置

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // 共享容器（减少启动时间）
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("aiops_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
```

#### 3.3.3 测试命名规范

```java
// 命名格式: should_期望结果_when_条件
@Test
@DisplayName("应该成功登录当用户名和密码正确")
void should_LoginSuccessfully_when_ValidCredentials() {
    // Arrange - 准备测试数据
    // Act - 执行被测操作
    // Assert - 验证结果
}

@Test
@DisplayName("应该拒绝登录当密码错误")
void should_RejectLogin_when_WrongPassword() {
    // ...
}
```

#### 3.3.4 测试数据隔离

```java
// 每个测试使用唯一用户名
private String generateUniqueUsername(String prefix) {
    return prefix.substring(0, Math.min(3, prefix.length()))
            + System.nanoTime() % 10000000;
}

@Test
void should_RegisterSuccessfully_when_ValidRequest() {
    String uniqueUsername = generateUniqueUsername("reg");
    // 使用 uniqueUsername 进行测试...
}
```

### 3.4 适用场景

- **CI/CD 流水线**: 自动化回归测试
- **PR 验证**: 合并前确保功能正常
- **覆盖率统计**: 结合 JaCoCo 生成报告

---

## 4. 单元测试 (Unit Test)

### 4.1 定义

单元测试验证单个类或方法的逻辑正确性，使用 Mock 隔离外部依赖。

### 4.2 特点

```yaml
环境要求: 无外部依赖
测试入口: 直接调用方法
测试数据: 内存中的 Mock 数据
隔离性: 最高
执行速度: 最快（毫秒级）
```

### 4.3 最佳实践

#### 4.3.1 测试类组织

```
domain/domain-impl/src/test/java/.../
├── service/
│   └── auth/
│       └── AuthDomainServiceImplTest.java
application/application-impl/src/test/java/.../
├── service/
│   └── auth/
│       └── AuthApplicationServiceImplTest.java
```

#### 4.3.2 Mock 使用

```java
@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthApplicationServiceImpl authApplicationService;

    @Test
    void should_ReturnToken_when_LoginWithValidCredentials() {
        // Arrange
        when(authDomainService.checkAccountLock(anyString()))
                .thenReturn(Optional.of(AccountLockInfo.notLocked()));
        when(authDomainService.findAccountByUsername(anyString()))
                .thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString()))
                .thenReturn(true);

        // Act
        LoginResult result = authApplicationService.login(loginRequest);

        // Assert
        assertThat(result.getToken()).isNotNull();
        verify(authDomainService).resetLoginFailureCount(anyString());
    }
}
```

#### 4.3.3 测试覆盖重点

```java
// 1. 正常路径
@Test void should_Succeed_when_ValidInput() { }

// 2. 边界条件
@Test void should_HandleEmpty_when_InputIsEmpty() { }
@Test void should_HandleNull_when_InputIsNull() { }
@Test void should_HandleMax_when_InputAtMaxBoundary() { }

// 3. 异常路径
@Test void should_ThrowException_when_InvalidInput() { }
@Test void should_ReturnError_when_DependencyFails() { }

// 4. 业务规则
@Test void should_LockAccount_when_5ConsecutiveFailures() { }
```

---

## 5. 测试执行策略

### 5.1 开发阶段

```bash
# 快速单元测试
mvn test -DskipITs

# 指定类测试
mvn test -Dtest=AuthApplicationServiceImplTest
```

### 5.2 提交前

```bash
# 完整单元测试
mvn test

# 或使用 Git Hook（pre-commit）
```

### 5.3 CI/CD 流水线

```yaml
# .github/workflows/test.yml
jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Unit Tests
        run: mvn test -DskipITs

  integration-test:
    runs-on: ubuntu-latest
    needs: unit-test
    steps:
      - uses: actions/checkout@v3
      - name: Run Integration Tests
        run: mvn verify -DskipUTs
```

### 5.4 发布前

```bash
# E2E 测试脚本
./doc/04-testing/e2e/scripts/full-flow-test.sh

# 或使用测试环境
BASE_URL=https://test.example.com ./full-flow-test.sh
```

---

## 6. 测试报告

### 6.1 单元测试报告

```bash
# 生成 Surefire 报告
mvn surefire-report:report

# 报告位置
target/site/surefire-report.html
```

### 6.2 覆盖率报告

```bash
# 生成 JaCoCo 报告
mvn jacoco:report

# 报告位置
target/site/jacoco/index.html
```

### 6.3 E2E 测试报告

```bash
# 脚本输出到日志文件
./e2e-test.sh 2>&1 | tee test-report-$(date +%Y%m%d).log
```

---

## 7. 常见问题

### 7.1 TestContainers 启动慢

```java
// 解决方案：使用共享容器
@Testcontainers
class BaseIntegrationTest {
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);  // 启用容器复用
}
```

### 7.2 测试数据污染

```java
// 解决方案：使用唯一标识
String uniqueId = UUID.randomUUID().toString().substring(0, 8);
String username = "test_" + uniqueId;
```

### 7.3 构建产物过期

```bash
# 解决方案：重新构建
mvn clean install -DskipTests

# 验证 jar 时间戳
ls -la ~/.m2/repository/com/your/module/*.jar
```

---

## 8. 参考资源

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [TestContainers](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://site.mockito.org/)
