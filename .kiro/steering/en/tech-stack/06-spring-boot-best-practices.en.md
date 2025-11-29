---
inclusion: manual
---

# Spring Boot Best Practices

This document guides AI on how to correctly and efficiently use Spring Boot framework for application development.

**Important Note**: This project adopts DDD multi-module architecture. For project structure and layering standards, please refer to `05-ddd-multi-module-project-best-practices.md`.

## Core Principles

### 1. Convention Over Configuration Principle

**You should comply with**:
- ✅ Use Spring Boot's default configurations
- ✅ Follow DDD multi-module project structure (see `05-ddd-multi-module-project-best-practices.md`)
- ✅ Use Starter dependencies to simplify configuration
- ❌ Don't over-customize configurations

### 2. Layered Architecture Principle

**You must comply with DDD layered architecture** (see `05-ddd-multi-module-project-best-practices.md`):
- ✅ Interface layer (HTTP/Consumer/Job): Provide external service entry
- ✅ Application layer: Orchestrate business flow, coordinate domain services
- ✅ Domain layer: Core business logic and domain model
- ✅ Infrastructure layer: Technical implementation and external dependencies
- ❌ No cross-layer calls

**Dependency Direction**: Interface → Application → Domain ← Infrastructure

### 3. Dependency Injection Principle

**You should comply with**:
- ✅ Use constructor injection (recommended)
- ⚠️ Avoid field injection (@Autowired on fields)
- ✅ Use @RequiredArgsConstructor (Lombok) to simplify constructor injection

## Dependency Injection Standards

### Injection methods you should use

**Recommended: Constructor injection**
- Use @RequiredArgsConstructor annotation from Lombok
- Declare dependencies as `private final` fields
- Spring automatically injects dependencies through constructor
- No @Autowired annotation needed

**Not recommended: Field injection**
- Don't use @Autowired on field declarations
- Makes testing difficult
- Hides dependencies
- May cause circular dependency issues

**Why constructor injection is recommended**:
- Dependencies are explicit and visible
- Easy to unit test (can inject mocks)
- Ensures dependencies are immutable (final fields)
- Avoids circular dependencies
- IDE provides better support

## Configuration Management Standards

### Configuration rules you should follow

**1. Use application.yml instead of application.properties**
- YAML format is clearer and more readable
- Supports hierarchical structure
- Better for complex configurations
- Easier to manage

**2. Multi-environment configuration**
Files you should create:
- `application.yml` - Common configuration shared across all environments
- `application-dev.yml` - Development environment specific settings
- `application-test.yml` - Test environment specific settings
- `application-prod.yml` - Production environment specific settings

Activation rule:
- Use `spring.profiles.active` to specify active profile

**Best practice for running the application**:
- ✅ **Recommended**: Run the jar file directly
  ```bash
  java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
  ```
- ⚠️ **Note**: `mvn spring-boot:run` may have classpath caching issues that prevent code changes from taking effect
  ```bash
  # If code changes don't take effect, use the jar file approach instead
  mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local
  ```

**3. Configuration property binding**
- Use @ConfigurationProperties for binding configuration groups
- Define configuration classes with proper validation
- Don't use @Value to inject multiple related configurations
- Configuration classes should be immutable (use final fields)

**4. Sensitive information handling**
Rules you must follow:
- Never store passwords in plain text in configuration files
- Use environment variables for sensitive data
- Use Spring Cloud Config or similar for centralized configuration
- Consider Jasypt for encrypting sensitive values in config files

### Configuration Class Standards

**Configuration class rules**:

**1. Annotation requirements**
- Use @Configuration to mark configuration classes
- Configuration classes should be in config package
- One @Bean method per bean to create

**2. Configuration class naming**
- Must end with `Config` suffix
- Name should be self-explanatory
- Examples: RedisConfig, MybatisPlusConfig, SecurityConfig

**3. Configuration class location**
- Placed in `config` package
- Organized by functional modules
- Keep related configurations together

**4. @Bean method rules**
- Method name should describe the bean being created
- Add @Bean annotation to methods that create beans
- Use method parameters for dependency injection

## Interface Layer Standards (HTTP Controller)

### Controller rules you should follow

**1. Controller location**
- Must be in Interface layer's http module
- Package pattern: `com.{company}.{system}.http.controller`
- Group controllers by business domain

**2. Use RESTful style**
Mapping rules:
- GET: Query resources (read-only operations)
- POST: Create new resources
- PUT: Update existing resources (full update)
- PATCH: Partial update (optional, use PUT if not needed)
- DELETE: Delete resources

**3. Unified return format**
- Define Result class in common module
- All interfaces must return Result<T> type
- Result should include: code, message, data
- Use Result.success() and Result.error() factory methods

**4. Parameter validation**
- Use @Valid or @Validated on request parameters
- Use JSR-303 annotations in DTO classes:
  - @NotNull, @NotBlank, @NotEmpty for required fields
  - @Size for string length validation
  - @Min, @Max for number range validation
  - @Email for email format validation
  - @Pattern for regex validation

**5. Controller responsibilities**
What Controller SHOULD do:
- Receive HTTP requests and extract parameters
- Validate request parameters
- Call Application Service to handle business
- Convert DTO to VO for response
- Return unified Result format

What Controller SHOULD NOT do:
- Implement business logic
- Directly call Repository or Domain Service
- Handle transactions
- Perform data persistence operations

**6. Unified exception handling**
- Use @RestControllerAdvice for global exception handling
- Place exception handler in handler package of http module
- Handle different exception types with specific @ExceptionHandler methods
- Log exceptions appropriately
- Return friendly error messages to clients

### Controller Best Practices

**Annotation requirements**:
- @RestController on class level
- @RequestMapping for base path
- @RequiredArgsConstructor for dependency injection
- @Validated for enabling validation

**Method design**:
- Each method handles one HTTP endpoint
- Method names should be verbs (getUser, createOrder, updateProfile)
- Use @PathVariable for path parameters
- Use @RequestParam for query parameters
- Use @RequestBody for request body with @Valid

**Conversion rules**:
- Convert application layer DTO to presentation layer VO
- Don't expose internal domain models to clients
- Keep conversion logic in private methods or separate converter classes

## Application Layer Standards

### Application Service rules you should follow

**1. Application Service location**
- Interface: application-api module (defines contract)
- Implementation: application-impl module (implements logic)
- Package: `com.{company}.{system}.application.service`

**2. Separate interface and implementation**
Naming conventions:
- Interface naming: `XxxAppService`
- Implementation naming: `XxxAppServiceImpl`
- Interface in application-api module
- Implementation in application-impl module

**3. Transaction management**
- Use @Transactional annotation on service methods
- Specify `rollbackFor = Exception.class` for write operations
- Use `readOnly = true` for query operations
- Keep transaction scope as small as possible
- Avoid long-running transactions

**4. Application Service responsibilities**
What Application Service SHOULD do:
- Orchestrate business workflow (coordination)
- Call Domain Services for business logic
- Call Infrastructure layer (Repository, Cache, MQ)
- Convert between DTO and Domain Entity
- Handle transaction boundaries
- Record audit logs

What Application Service SHOULD NOT do:
- Implement core business rules (delegate to Domain layer)
- Directly operate databases (use Repository)
- Contain complex conditional logic (encapsulate in private methods)

**5. Exception handling**
- Throw business exceptions directly (don't catch in main flow)
- Let exceptions propagate to global exception handler
- Use custom exception classes for different error scenarios
- Include context information in exception messages

### Application Service Best Practices

**For detailed Application Service coding standards, see**: `08-application-layer-best-practices.md`

**Key principles**:
- Main methods should have 5-10 clear steps
- No if/else in main method flow
- No try/catch in main method (except special cases)
- Extract validation, conversion, logging to private methods
- Each private method should have single responsibility

## Domain Layer Standards

### Domain rules you should follow

**See**: `05-ddd-multi-module-project-best-practices.md`

**Key principles**:
- Domain layer uses pure business language (no technical suffixes)
- Domain layer doesn't depend on any framework
- Domain layer doesn't depend on Infrastructure layer
- Core business logic implemented in Domain Service
- Domain entities should be rich models (not anemic)

## Infrastructure Layer Standards (Repository)

### Repository rules you should follow

**See**: `05-ddd-multi-module-project-best-practices.md`

**1. Repository location**
- Interface: domain-api or repository-api module
- Implementation: mysql-impl module (or other persistence implementation)

**2. Separate Entity and PO**
- Entity: in repository-api, pure POJO, no framework annotations
- PO (Persistent Object): in mysql-impl, contains MyBatis-Plus annotations
- RepositoryImpl responsible for conversion between Entity and PO

**3. Use MyBatis-Plus**
- Mapper interface operates on PO objects
- Repository interface operates on Entity objects
- Configuration classes in mysql-impl module
- Use BaseMapper for common CRUD operations

**4. Repository responsibilities**
What Repository SHOULD do:
- Provide CRUD operations for domain entities
- Convert between Entity and PO
- Execute database queries
- Handle persistence concerns

What Repository SHOULD NOT do:
- Implement business logic
- Perform data validation (should be in Domain layer)
- Handle transactions (handled in Application layer)

## Exception Handling Standards

### Exception handling rules you should follow

**1. Custom business exceptions**
Requirements:
- Extend RuntimeException (unchecked exception)
- Include error code field
- Include error message
- Provide multiple constructors for flexibility
- Group related exceptions in same package

**2. Global exception handling**
Implementation rules:
- Use @RestControllerAdvice annotation
- Place in handler package of http module
- Handle different exception types separately:
  - Business exceptions (return specific error code and message)
  - Validation exceptions (return validation errors)
  - System exceptions (log and return generic error)
- Always log exceptions with appropriate level

**3. Exception handling principles**
- Don't swallow exceptions (always log or rethrow)
- Log exceptions with full context information
- Return friendly error messages to users
- Don't expose sensitive information or stack traces to clients
- Include request ID/trace ID for debugging

**4. Exception hierarchy**
Suggested structure:
- BaseException (root of all business exceptions)
- DomainException (domain-specific errors)
- InfrastructureException (persistence, network errors)
- ApplicationException (application layer errors)

## Logging Standards

### Logging rules you should follow

**1. Use SLF4J + Logback**
- Spring Boot integrates by default
- Use @Slf4j annotation (Lombok) on classes that need logging
- Never use System.out.println or printStackTrace()

**2. Log levels**
Usage guide:
- **ERROR**: System exceptions, operation failures that need immediate attention
- **WARN**: Potential issues, business warnings, recoverable errors
- **INFO**: Important business operations, state changes, major milestones
- **DEBUG**: Detailed process information for debugging
- **TRACE**: Very detailed information, typically disabled in production

**3. Logging content principles**
What you MUST log:
- Key business operations (login, registration, payment, order creation)
- Method entry with important parameters (except sensitive data)
- Method exit with results (except sensitive data)
- Exception information with full stack traces
- Performance metrics (execution time for critical operations)
- Important business state changes
- Security-related events

What you MUST NOT log:
- Passwords, tokens, or API keys
- Personal identification numbers (SSN, passport, etc.)
- Credit card or bank account numbers
- Any sensitive personal information

**4. Log format standards**

**Use placeholders, not string concatenation**:
- Wrong: `"User login, username: " + username`
- Correct: `"User login, username: {}"` with placeholder

**Include context information**:
Required context fields:
- User ID (if available)
- Username (if available)
- Operation type
- Business object IDs
- Timestamp (auto-added by logging framework)

Optional context fields:
- IP address (in Controller layer)
- Request ID / Trace ID
- Device information
- Session ID

**Exception logging**:
- Always include exception object as last parameter
- Don't just log e.getMessage() - include full stack trace
- Pattern: `log.error("Operation failed, context: {}", context, exception)`

**Performance logging**:
- Record start time before operation
- Calculate duration after operation
- Log with operation name and duration
- Pattern: `"Operation completed, timeCost: {}ms"`

**5. Audit log format**

**Audit logs are structured logs for critical business operations**:

Format requirement:
```
[Audit Log] Operation Description | field1=value1 | field2=value2 | timestamp=timestamp
```

Rules:
- Use `[Audit Log]` prefix for identification
- Use pipe symbol `|` to separate fields
- Each field uses `key=value` format
- Always include timestamp field
- Extract to private log methods (logRegistrationSuccess, logLoginFailure, etc.)

Operations requiring audit logs:
- User registration, login, logout
- Account lock, unlock
- Permission changes
- Sensitive data access
- Critical configuration changes
- Financial operations (payment, transfer, refund)

**6. Logging checklist**

Before committing code, verify:
- [ ] Using @Slf4j annotation on classes
- [ ] Key business operations logged (start, success, failure)
- [ ] Method inputs recorded (except sensitive info)
- [ ] Exception logs include full stack traces
- [ ] Using placeholders instead of string concatenation
- [ ] Appropriate log level selected
- [ ] Complete context information included
- [ ] No sensitive information in logs
- [ ] Not printing excessive logs in loops
- [ ] Important state changes recorded
- [ ] Audit logs use correct structured format

**7. Common logging mistakes**

| Mistake | Wrong Approach | Correct Approach | Why |
|---------|---------------|------------------|-----|
| No logging | Key operations without logs | Log before and after critical operations | Impossible to debug issues |
| String concatenation | `"User: " + username` | `"User: {}"` with placeholder | Performance impact, harder to read |
| Lost stack trace | `log.error(e.getMessage())` | `log.error("Failed", e)` | Can't debug without stack trace |
| Logging sensitive info | `"Password: {}"` | Don't log passwords | Security violation |
| Loop logging | Log in every iteration | Aggregate and log once | Performance impact, log spam |
| Wrong level | INFO for debug details | DEBUG for debug details | Clutters production logs |

**8. Exception Log Level Selection Principles**

**Core Principle**: Client/user-caused errors = WARN, system internal errors = ERROR

**Use WARN for** (expected business errors):
- Token expired, invalid, malformed, signature verification failed
- Parameter validation failed
- Authentication failed (wrong username/password)
- Resource not found, insufficient permissions
- Fallback-capable dependency failures (e.g., Redis unavailable but can fallback to database)

**Use ERROR for** (system-level errors, need ops attention):
- Database connection failure
- Required external service unavailable
- Uncaught unknown exceptions

## Parameter Validation Standards

### Validation rules you should follow

**1. Use JSR-303 annotations in DTOs**
Common annotations:
- @NotNull: Field cannot be null
- @NotBlank: String cannot be null, empty, or whitespace
- @NotEmpty: Collection cannot be null or empty
- @Size(min, max): String length or collection size validation
- @Min, @Max: Number range validation
- @Email: Email format validation
- @Pattern: Regex pattern validation

**2. Enable validation in Controller**
- Add @Valid or @Validated on @RequestBody parameters
- Add @Validated on controller class for method parameter validation
- Use @PathVariable, @RequestParam with validation annotations

**3. Custom validation annotations**
When to create custom validators:
- Complex validation logic that JSR-303 cannot express
- Business rule validation
- Cross-field validation

Implementation:
- Create annotation with @Constraint
- Implement ConstraintValidator interface
- Add validation logic in isValid method

**4. Validation error handling**
- Global exception handler catches MethodArgumentNotValidException
- Extract field errors and error messages
- Return validation errors in unified Result format
- Include field name and error message for each violation

## Transaction Management Standards

### Transaction rules you should follow

**1. Use @Transactional annotation**
Placement:
- On service layer methods (Application Service or Domain Service)
- Never on Controller methods
- Never on Repository methods

**2. Transaction properties**
Required attributes:
- `rollbackFor = Exception.class` for write operations (ensures all exceptions trigger rollback)
- `readOnly = true` for query operations (optimization)

Optional attributes:
- `propagation`: Transaction propagation behavior (default REQUIRED is usually fine)
- `isolation`: Transaction isolation level (use default unless specific requirement)
- `timeout`: Transaction timeout in seconds (for long-running operations)

**3. Transaction scope best practices**
- Keep transactions as short as possible
- Don't call remote services inside transactions
- Don't perform heavy computations inside transactions
- Don't do file I/O inside transactions
- Minimize database operations within transaction

**4. Transaction failure scenarios**
Common reasons why @Transactional doesn't work:
- Method is not public (Spring AOP requires public methods)
- Same class method call (this.method()) bypasses proxy
- Exception is caught and not rethrown
- Database doesn't support transactions (some storage engines)
- Wrong exception type (checked exceptions need explicit rollbackFor)

## Performance Optimization Standards

### Performance optimization rules you should follow

**1. Use caching strategically**
When to cache:
- Frequently accessed data
- Expensive computation results
- Database query results that change infrequently

Caching strategies:
- Use @Cacheable for read operations
- Use @CachePut for update operations
- Use @CacheEvict for delete operations
- Set appropriate TTL (Time To Live)
- Implement cache warming for critical data

Cache considerations:
- Prevent cache penetration (cache null values with short TTL)
- Prevent cache breakdown (use locks for hot keys)
- Prevent cache avalanche (randomize expiration times)

**2. Asynchronous processing**
Use @Async for:
- Sending emails or SMS
- File upload/download processing
- Log recording (non-critical)
- Notification dispatching

Requirements:
- Enable async with @EnableAsync
- Configure custom thread pool
- Handle async exceptions properly
- Don't use async for critical business logic

**3. Batch operations**
When to use batching:
- Inserting multiple records
- Updating multiple records
- Bulk delete operations

Benefits:
- Reduces database round trips
- Improves throughput
- Better resource utilization

Implementation:
- Use MyBatis-Plus batch methods
- Use JDBC batch updates
- Consider batch size (typically 100-1000 records)

**4. Database optimization**
- Use appropriate indexes
- Avoid N+1 query problems
- Use lazy loading for associations
- Optimize slow queries
- Use read-write separation if needed

**5. Connection pool configuration**
Properly configure:
- Database connection pool (HikariCP)
- Redis connection pool
- HTTP connection pool (RestTemplate, WebClient)

Key settings:
- Maximum pool size (based on expected load)
- Minimum idle connections
- Connection timeout
- Idle timeout
- Max lifetime

## Security Standards

### Security rules you should follow

**1. Input validation**
- Validate all external inputs (parameters, headers, files)
- Use whitelist validation (not blacklist)
- Sanitize user input to prevent injection attacks
- Validate file uploads (type, size, content)

**2. Authentication and authorization**
- Use Spring Security or JWT for authentication
- Implement role-based access control (RBAC)
- Use @PreAuthorize or @Secured for method-level security
- Protect sensitive endpoints
- Implement proper session management

**3. Sensitive information protection**
- Encrypt passwords with BCrypt (never plain text or MD5)
- Don't log sensitive information
- Don't return sensitive data in API responses
- Mask sensitive data when displayed
- Use HTTPS for data transmission

**4. SQL injection prevention**
- Always use parameterized queries
- Never concatenate SQL strings with user input
- Use MyBatis-Plus or JPA (they prevent SQL injection)
- Validate and sanitize inputs

**5. XSS prevention**
- Escape user input when rendering in HTML
- Use Content Security Policy (CSP) headers
- Validate and sanitize rich text content
- Use appropriate encoding for different contexts

**6. CSRF protection**
- Enable CSRF protection in Spring Security
- Use CSRF tokens for state-changing operations
- Verify CSRF tokens on server side

## Testing Standards

### Testing rules you should follow

**1. Unit testing**
- Use JUnit 5 (Jupiter)
- Use Mockito for mocking dependencies
- Aim for >80% code coverage
- Test one unit (method/class) in isolation

**2. Test naming**
Pattern: `should_expectedResult_when_condition`
- Example: `should_returnUser_when_userExists`
- Example: `should_throwException_when_userNotFound`

**3. Test structure**
Follow AAA pattern:
- Arrange: Set up test data and mocks
- Act: Execute the method under test
- Assert: Verify the results

**4. Integration testing**
- Use @SpringBootTest for full context
- Test complete business flows
- Test with actual database (testcontainers)
- Verify integrations between layers

**5. Testing checklist**
- [ ] All public methods have unit tests
- [ ] Edge cases are tested (null, empty, boundary values)
- [ ] Exception scenarios are tested
- [ ] Mocks are used appropriately (don't test mocks)
- [ ] Tests are independent (no shared state)
- [ ] Tests are deterministic (always same result)
- [ ] Test names clearly describe what is tested

## Code Quality Checklist

When writing Spring Boot code, verify:

### Architecture Check
- [ ] Following layered architecture (Controller → Service → Repository)
- [ ] Using constructor injection (not field injection)
- [ ] Configuration classes in config package
- [ ] Proper package organization

### Controller Check
- [ ] Using RESTful style
- [ ] Unified return format (Result<T>)
- [ ] Parameter validation (@Valid)
- [ ] No business logic in Controller
- [ ] Proper exception handling

### Service Check
- [ ] Interface and implementation separated
- [ ] Using @Transactional with correct settings
- [ ] Transaction scope minimized
- [ ] Exceptions handled properly
- [ ] Business logic in appropriate layer

### Configuration Check
- [ ] Using application.yml (not .properties)
- [ ] Multi-environment configuration set up
- [ ] Sensitive information encrypted or externalized
- [ ] Configuration property binding used

### Logging Check
- [ ] Using @Slf4j annotation
- [ ] Appropriate log levels
- [ ] No sensitive information logged
- [ ] Placeholders used (not string concatenation)
- [ ] Exceptions logged with stack traces

### Security Check
- [ ] Input validation implemented
- [ ] Authentication and authorization configured
- [ ] Passwords encrypted (BCrypt)
- [ ] SQL injection prevented
- [ ] Sensitive data protected

### Performance Check
- [ ] Caching strategy implemented
- [ ] Asynchronous processing where appropriate
- [ ] Batch operations for bulk data
- [ ] Connection pools configured
- [ ] Database queries optimized

## Summary

Following these Spring Boot best practices ensures:
- ✅ Clean and maintainable code structure
- ✅ Consistent development standards across team
- ✅ High-performance applications
- ✅ Secure systems protecting user data
- ✅ Easy-to-test codebase
- ✅ Fast development with fewer bugs
