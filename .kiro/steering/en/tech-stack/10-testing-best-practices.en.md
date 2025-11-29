---
inclusion: manual
---

# Testing Best Practices

## 1. Testing Strategy Layers

### 1.1 Testing Pyramid

```
        /\
       /  \      E2E Tests (10%)
      /----\     - Acceptance Tests
     /      \    - Smoke Tests
    /--------\   Integration Tests (20%)
   /          \  - API Tests
  /------------\ - Component Collaboration Tests
 /              \
/----------------\ Unit Tests (70%)
                   - Business Logic Tests
                   - Boundary Condition Tests
```

### 1.2 Responsibilities by Layer

| Test Type | Responsibility | Frequency | When to Run |
|-----------|---------------|-----------|-------------|
| Unit Tests | Verify individual class/method logic | Every commit | Development, CI |
| Integration Tests | Verify component collaboration and contracts | Every build | CI/CD |
| E2E Tests | Verify complete business flows | Every release | Pre-release, Post-deploy |

---

## 2. End-to-End Testing (E2E)

### 2.1 Definition

E2E tests simulate real user behavior, validating the complete chain from HTTP entry to database.

### 2.2 Characteristics

```yaml
Environment: Full production-grade setup (App + MySQL + Redis)
Entry Point: HTTP interfaces (curl, Postman, k6)
Test Data: Real data in database
Isolation: Low (tests may affect each other)
Speed: Slow (seconds)
```

### 2.3 Best Practices

#### 2.3.1 Script Organization

```
doc/04-testing/e2e/
├── scripts/
│   ├── auth-e2e-test.sh       # Auth module E2E tests
│   ├── session-e2e-test.sh    # Session module E2E tests
│   └── full-flow-test.sh      # Complete flow tests
├── data/
│   ├── test-users.json        # Test user data
│   └── test-scenarios.json    # Test scenario data
└── README.md                  # Test documentation
```

#### 2.3.2 Script Template

```bash
#!/bin/bash
# E2E Test Script Template

set -e  # Exit on error

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMESTAMP=$(date +%s)

# Helper functions
log_info() { echo "[INFO] $1"; }
log_pass() { echo "[PASS] $1"; }
log_fail() { echo "[FAIL] $1"; exit 1; }

# Response assertions
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

# Test cases
test_user_registration() {
    log_info "Testing user registration..."
    local user="e2e_user_$TIMESTAMP"
    local response=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$user\",\"email\":\"$user@test.com\",\"password\":\"SecureP@ss123\"}")
    assert_success "$response" "User registration"
}

# Execute tests
main() {
    log_info "Starting E2E tests..."
    test_user_registration
    # Add more tests...
    log_info "E2E tests completed!"
}

main "$@"
```

#### 2.3.3 Test Data Management

```bash
# Use unique timestamp to avoid conflicts
USERNAME="test_$(date +%s)_$(( RANDOM % 1000 ))"

# Pre-test cleanup (optional)
# mysql -e "DELETE FROM t_account WHERE username LIKE 'test_%'"

# Post-test cleanup (optional)
cleanup() {
    redis-cli KEYS "login:fail:test_*" | xargs redis-cli DEL
}
trap cleanup EXIT
```

### 2.4 Use Cases

- **Acceptance Testing**: Pre-release validation
- **Smoke Testing**: Quick verification after deployment
- **Regression Testing**: Compatibility validation after upgrades
- **Performance Baseline**: Foundation for performance tests

---

## 3. Integration Testing

### 3.1 Definition

Integration tests verify collaboration between multiple components/modules, typically in an isolated environment.

### 3.2 Characteristics

```yaml
Environment: TestContainers auto-starts temporary containers
Entry Point: MockMvc or WebTestClient
Test Data: Temporary database, destroyed after tests
Isolation: High (independent environment per test)
Speed: Medium (1-2 minutes for container startup)
```

### 3.3 Best Practices

#### 3.3.1 Test Class Organization

```
bootstrap/src/test/java/.../integration/
├── BaseIntegrationTest.java     # Base class (TestContainers config)
├── AuthIntegrationTest.java     # Auth integration tests
├── SessionIntegrationTest.java  # Session integration tests
└── AdminIntegrationTest.java    # Admin integration tests
```

#### 3.3.2 TestContainers Configuration

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // Shared containers (reduce startup time)
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

#### 3.3.3 Test Naming Convention

```java
// Format: should_ExpectedResult_when_Condition
@Test
@DisplayName("Should login successfully when valid credentials")
void should_LoginSuccessfully_when_ValidCredentials() {
    // Arrange - Prepare test data
    // Act - Execute operation under test
    // Assert - Verify results
}

@Test
@DisplayName("Should reject login when wrong password")
void should_RejectLogin_when_WrongPassword() {
    // ...
}
```

#### 3.3.4 Test Data Isolation

```java
// Each test uses unique username
private String generateUniqueUsername(String prefix) {
    return prefix.substring(0, Math.min(3, prefix.length()))
            + System.nanoTime() % 10000000;
}

@Test
void should_RegisterSuccessfully_when_ValidRequest() {
    String uniqueUsername = generateUniqueUsername("reg");
    // Use uniqueUsername in test...
}
```

### 3.4 Use Cases

- **CI/CD Pipeline**: Automated regression testing
- **PR Validation**: Ensure functionality before merge
- **Coverage Reporting**: Combined with JaCoCo

---

## 4. Unit Testing

### 4.1 Definition

Unit tests verify the logic of individual classes or methods, using Mocks to isolate external dependencies.

### 4.2 Characteristics

```yaml
Environment: No external dependencies
Entry Point: Direct method calls
Test Data: In-memory Mock data
Isolation: Highest
Speed: Fastest (milliseconds)
```

### 4.3 Best Practices

#### 4.3.1 Test Class Organization

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

#### 4.3.2 Mock Usage

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

#### 4.3.3 Test Coverage Focus

```java
// 1. Happy path
@Test void should_Succeed_when_ValidInput() { }

// 2. Boundary conditions
@Test void should_HandleEmpty_when_InputIsEmpty() { }
@Test void should_HandleNull_when_InputIsNull() { }
@Test void should_HandleMax_when_InputAtMaxBoundary() { }

// 3. Error paths
@Test void should_ThrowException_when_InvalidInput() { }
@Test void should_ReturnError_when_DependencyFails() { }

// 4. Business rules
@Test void should_LockAccount_when_5ConsecutiveFailures() { }
```

---

## 5. Execution Strategy

### 5.1 During Development

```bash
# Quick unit tests
mvn test -DskipITs

# Run specific test class
mvn test -Dtest=AuthApplicationServiceImplTest
```

### 5.2 Before Commit

```bash
# Full unit tests
mvn test

# Or use Git Hook (pre-commit)
```

### 5.3 CI/CD Pipeline

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

### 5.4 Before Release

```bash
# E2E test script
./doc/04-testing/e2e/scripts/full-flow-test.sh

# Or against test environment
BASE_URL=https://test.example.com ./full-flow-test.sh
```

---

## 6. Test Reports

### 6.1 Unit Test Report

```bash
# Generate Surefire report
mvn surefire-report:report

# Report location
target/site/surefire-report.html
```

### 6.2 Coverage Report

```bash
# Generate JaCoCo report
mvn jacoco:report

# Report location
target/site/jacoco/index.html
```

### 6.3 E2E Test Report

```bash
# Script output to log file
./e2e-test.sh 2>&1 | tee test-report-$(date +%Y%m%d).log
```

---

## 7. Common Issues

### 7.1 TestContainers Slow Startup

```java
// Solution: Use shared containers
@Testcontainers
class BaseIntegrationTest {
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);  // Enable container reuse
}
```

### 7.2 Test Data Pollution

```java
// Solution: Use unique identifiers
String uniqueId = UUID.randomUUID().toString().substring(0, 8);
String username = "test_" + uniqueId;
```

### 7.3 Stale Build Artifacts

```bash
# Solution: Rebuild
mvn clean install -DskipTests

# Verify jar timestamp
ls -la ~/.m2/repository/com/your/module/*.jar
```

---

## 8. References

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [TestContainers](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://site.mockito.org/)
