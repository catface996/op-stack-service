---
inclusion: manual
---

# DDD Multi-Module Project Best Practices

This document guides AI on how to correctly organize code, naming, and manage dependencies in DDD (Domain-Driven Design) based multi-module Maven projects.

## Core Principles

### 1. Strict Layering Principle

**Dependency Direction**: Interface → Application → Domain ← Infrastructure

**Dependency rules you must comply with**:
- ❌ Domain layer cannot depend on Infrastructure layer
- ❌ Lower layers cannot depend on upper layers
- ❌ API modules cannot depend on implementation modules
- ❌ No circular dependencies

**Checking method**: Before adding dependencies, confirm dependency direction follows above rules.

### 2. Business Language Priority Principle

Domain layer uses pure business language (Ubiquitous Language), no technical suffixes.

**You should name like this**:
- ✅ Domain layer: `User`, `Order` (pure business nouns)
- ❌ Domain layer: `UserEntity`, `OrderEntity` (don't add technical suffixes)

### 3. Framework Independence Principle

Domain layer should be independent of any technical framework, maintaining pure business logic.

**You should ensure**:
- Domain layer classes don't contain any framework annotations (such as @Entity, @Table)
- Domain layer doesn't depend on any persistence framework

## Module Layering Structure

### Layer Responsibilities

| Layer | Responsibilities | Typical Modules | Package Type |
|------|------|---------|---------|
| **Interface Layer** | Provide external service entry | http, consumer, job | jar |
| **Application Layer** | Orchestrate business flow, coordinate domain services | application-api, application-impl | jar |
| **Domain Layer** | Core business logic and domain model | domain-api, domain-impl | jar |
| **Infrastructure Layer** | Technical implementation and external dependencies | repository, cache, mq | jar |
| **Bootstrap Layer** | Application startup and configuration | bootstrap | jar (executable) |
| **Common Layer** | Common utilities and infrastructure | common | jar |

### Interface Layer Module Description

| Module | Responsibilities | Use Cases |
|------|------|---------|
| **http** | HTTP interface, provide REST API | Frontend calls, third-party system calls |
| **consumer** | Message consumer, process MQ messages | Async task processing, event-driven |
| **job** | Scheduled tasks, execute periodic tasks | Data sync, report generation, cleanup tasks |
| **rpc** (optional) | RPC interface, provide remote calls | Inter-microservice calls |
| **websocket** (optional) | WebSocket interface, real-time communication | Real-time message push, online chat |

### Infrastructure Layer Module Description

| Module | Responsibilities | Technology Examples |
|------|------|-------------|
| **repository/mysql-impl** | Data persistence implementation | MySQL, PostgreSQL, MongoDB |
| **cache/redis-impl** | Cache service implementation | Redis, Memcached |
| **mq/sqs-impl** | Message queue implementation | SQS, RocketMQ, Kafka, RabbitMQ |

### Domain Layer Submodules (Important)

**Key Principle**: Repository-API, Cache-API, MQ-API interface definitions belong to domain layer concepts and should be managed as submodules of Domain

| Module | Responsibilities | Description |
|------|------|------|
| **domain-api** | Domain models and domain service interfaces | Aggregates, entities, value objects, domain service interfaces |
| **repository-api** | Repository interface definitions (Ports) | Define data persistence contracts, follow dependency inversion principle |
| **cache-api** | Cache interface definitions (Ports) | Define cache service contracts |
| **mq-api** | Message queue interface definitions (Ports) | Define message send/receive contracts |
| **domain-impl** | Domain service implementations | Implement complex business logic, call Repository/Cache/MQ |

**Module Structure Example**:
```
domain/
├── domain-api/              (domain models and service interfaces)
│   ├── model/              (aggregates, entities, value objects)
│   └── service/            (domain service interfaces)
├── repository-api/          (repository interfaces - independent submodule)
│   └── repository/         (Repository interface definitions)
├── cache-api/              (cache interfaces - independent submodule)
│   └── cache/              (Cache interface definitions)
├── mq-api/                 (MQ interfaces - independent submodule)
│   └── mq/                 (MQ interface definitions)
├── domain-impl/            (domain service implementations)
│   └── service/            (domain service implementations)
└── pom.xml                 (domain parent module)
```

## Module Dependency Relationships

### Dependency Type Description

| Dependency Type | Maven Scope | Description | Use Cases |
|---------|-------------|------|---------|
| **Compile dependency** | compile | For calling interfaces, needed at compile time | Interface layer depends on Application API |
| **Implementation dependency** | compile | For implementing interfaces, needed at compile time | Application Impl depends on Application API |
| **Runtime dependency** | runtime | For packaging startup, needed at runtime | Bootstrap depends on all Impl modules |

### Dependency Rules by Layer (Strictly Follow)

**Interface Layer**:
- ✅ Only depends on Application API (call application services)
- ❌ Cannot directly depend on Domain or Infrastructure

**Application Layer (Key Rules)**:
- ✅ **Only depends on Domain API** (call domain services)
- ❌ **Forbidden to depend on repository-api, cache-api, mq-api**
- ❌ Cannot depend on Interface layer
- **Reason**: Application Service is use case orchestration layer, all data access must go through Domain Service

**Domain Layer**:
- **domain-api**: No dependencies on other modules (pure domain models and interfaces)
- **repository-api**: No dependencies on other modules (pure interface definitions)
- **cache-api**: No dependencies on other modules (pure interface definitions)
- **mq-api**: No dependencies on other modules (pure interface definitions)
- **domain-impl**: Depends on domain-api + repository-api + cache-api + mq-api

**Infrastructure Layer**:
- ✅ Only depends on corresponding API modules (implement interfaces)
- Example: mysql-impl depends on repository-api
- Example: redis-impl depends on cache-api
- Example: sqs-impl depends on mq-api
- ❌ Cannot depend on Application or Interface layers

**Bootstrap Layer**:
- Runtime depends on all implementation modules (packaging startup)
- No compile dependencies needed

**Common Layer**:
- All modules can depend on common
- common doesn't depend on any other modules

**Dependency Diagram**:
```
┌─────────────────────────────────────────────┐
│         Interface Layer (HTTP/MQ/Job)       │
└────────────────┬────────────────────────────┘
                 │ depends on
                 ▼
┌─────────────────────────────────────────────┐
│         Application Layer                   │
│         (application-impl)                  │
│  ✅ Only depends on domain-api               │
│  ❌ Forbidden: repository-api/cache-api/mq-api│
└────────────────┬────────────────────────────┘
                 │ depends on
                 ▼
┌─────────────────────────────────────────────┐
│         Domain API Layer                    │
│         (domain-api)                        │
└─────────────────────────────────────────────┘
                 ▲
                 │ depends on
┌────────────────┴────────────────────────────┐
│         Domain Impl Layer                   │
│         (domain-impl)                       │
│  ✅ Depends on repository-api + cache-api + mq-api │
└────┬────────────────────────┬───────────────┘
     │ depends on              │ depends on
     ▼                        ▼
┌────────────────┐      ┌──────────────────────┐
│ Repository API │      │ Cache API / MQ API   │
│ (Port Interface)│      │ (Port Interface)     │
└────────┬───────┘      └──────────┬───────────┘
         │ implements              │ implements
         ▼                         ▼
┌────────────────┐      ┌──────────────────────┐
│ mysql-impl     │      │ redis-impl/sqs-impl  │
│ (Adapter Impl) │      │ (Adapter Impl)       │
└────────────────┘      └──────────────────────┘
```

## Application Service vs Domain Service Responsibilities (Key)

### Core Principles

**Application Service is use case orchestration layer, Domain Service is business logic layer**

- Application Service **can only** call Domain Service
- Application Service **forbidden** to directly call Repository/Cache/MQ
- Domain Service is the **only entry point** for data access

### Application Service Responsibilities

**Positioning**: Use case orchestration layer, organize business flows

**Allowed responsibilities**:
1. ✅ **Transaction boundary control** (@Transactional)
2. ✅ **Orchestrate Domain Services** (call multiple Domain Services to complete use case)
3. ✅ **DTO conversion** (Request/Response → Domain Entity)
4. ✅ **Permission validation** (can be done at this layer)
5. ✅ **Audit logging**
6. ✅ **Exception handling and conversion**

**Forbidden responsibilities**:
- ❌ Direct call to Repository (must go through Domain Service)
- ❌ Direct call to Cache (must go through Domain Service)
- ❌ Direct call to MQ (must go through Domain Service)
- ❌ Contains complex business logic (should be in Domain Service)
- ❌ Domain object creation logic (should be in Domain Service)
- ❌ Password encryption/verification logic (should be in Domain Service)

**Dependency List**:
```java
@Service
public class AuthApplicationServiceImpl implements AuthApplicationService {
    // ✅ Allowed: depend on Domain Service
    private final AuthDomainService authDomainService;

    // ❌ Forbidden: cannot depend on Repository
    // private final AccountRepository accountRepository;

    // ❌ Forbidden: cannot depend on Cache
    // private final CacheService cacheService;

    // ❌ Forbidden: cannot depend on MQ
    // private final MqService mqService;
}
```

**Correct Example**:
```java
@Override
@Transactional
public RegisterResult register(RegisterRequest request) {
    // 1. DTO conversion (Application layer responsibility)
    String username = request.getUsername();
    String email = request.getEmail();
    String password = request.getPassword();

    // 2. Call Domain Service to create account (✅ Correct)
    Account account = authDomainService.createAccount(username, email, password);

    // 3. Call Domain Service to save account (✅ Correct)
    Account savedAccount = authDomainService.saveAccount(account);

    // 4. Record audit log (Application layer responsibility)
    auditLogger.log("USER_REGISTERED", savedAccount.getId());

    // 5. DTO conversion (Application layer responsibility)
    return RegisterResult.from(savedAccount);
}
```

**Wrong Example**:
```java
@Override
@Transactional
public RegisterResult register(RegisterRequest request) {
    // ❌ Wrong: Application Service directly calls Repository
    Account account = accountRepository.findByUsername(request.getUsername());

    // ❌ Wrong: Application Service directly creates domain object
    Account newAccount = Account.create(
        request.getUsername(),
        request.getEmail(),
        passwordEncoder.encode(request.getPassword())
    );

    // ❌ Wrong: Application Service directly saves data
    Account savedAccount = accountRepository.save(newAccount);

    return RegisterResult.from(savedAccount);
}
```

### Domain Service Responsibilities

**Positioning**: Business logic layer, handle complex business rules

**Allowed responsibilities**:
1. ✅ **Complex business rules** (logic across multiple aggregates)
2. ✅ **Domain object creation** (factory methods)
3. ✅ **Password encryption/verification**
4. ✅ **Session management logic**
5. ✅ **Account locking logic**
6. ✅ **Call Repository for data access**
7. ✅ **Call Cache for cache operations**
8. ✅ **Call MQ to send messages**

**Forbidden responsibilities**:
- ❌ Transaction control (controlled by Application Service)
- ❌ DTO conversion (handled by Application Service)
- ❌ Audit logging (handled by Application Service)

**Dependency List**:
```java
@Service
public class AuthDomainServiceImpl implements AuthDomainService {
    // ✅ Allowed: depend on Repository
    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;

    // ✅ Allowed: depend on Cache
    private final LoginFailureCacheService loginFailureCacheService;

    // ✅ Allowed: depend on MQ
    private final AuthEventMqService authEventMqService;

    // ✅ Allowed: depend on password encoder
    private final PasswordEncoder passwordEncoder;
}
```

**Correct Example**:
```java
@Override
public Account createAccount(String username, String email, String rawPassword) {
    // 1. Check if username exists (✅ Call Repository)
    if (accountRepository.existsByUsername(username)) {
        throw new BusinessException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // 2. Password strength check (✅ Business rule)
    PasswordStrengthResult strengthResult = checkPasswordStrength(rawPassword);
    if (!strengthResult.isStrong()) {
        throw new BusinessException(AuthErrorCode.PASSWORD_TOO_WEAK);
    }

    // 3. Encrypt password (✅ Domain logic)
    String encodedPassword = passwordEncoder.encode(rawPassword);

    // 4. Create domain object (✅ Factory method)
    return Account.create(username, email, encodedPassword);
}

@Override
public Account saveAccount(Account account) {
    // Save account (✅ Call Repository)
    Account savedAccount = accountRepository.save(account);

    // Send account created event (✅ Call MQ)
    authEventMqService.sendAccountCreatedEvent(savedAccount.getId());

    return savedAccount;
}
```

### Why Strict Separation?

**1. Clear responsibility boundaries**:
- Application Service: Orchestrate use case flows
- Domain Service: Implement business logic

**2. Comply with Hexagonal Architecture**:
- Domain is core, defines Ports (Repository/Cache/MQ interfaces)
- Infrastructure is outer layer, implements Adapters
- Application should not know about outer layer

**3. Easy to test**:
- Test Application Service: Mock Domain Service only
- Test Domain Service: Mock Repository/Cache/MQ only

**4. Easy to replace implementation**:
- Switch database: Only modify Infrastructure layer
- Application and Domain need no changes

**5. Avoid logic leakage**:
- If Application directly calls Repository, business logic easily leaks to Application layer
- Through Domain Service encapsulation, ensure business logic is centrally managed

### How to Handle Simple CRUD?

**Even for simple queries, must go through Domain Service**:

```java
// ✅ Correct: Application Service calls Domain Service
@Override
public AccountInfo getAccountById(Long accountId) {
    Account account = authDomainService.findAccountById(accountId);
    return AccountInfo.from(account);
}

// Implemented in Domain Service
@Override
public Account findAccountById(Long accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.ACCOUNT_NOT_FOUND));
}
```

**Reasons**:
- Unified data access entry point, easy to add cache, audit and other cross-cutting concerns
- Even if currently simple query, may need to add business rules in future
- Maintain architecture consistency, avoid confusion

## Package Structure and Naming Standards

### Package Naming Format

**Basic format**: `com.{company}.{system}.{layer}.{module}`

**Package structure you should use**:

| Layer | Module | Package Structure | Key Subpackages |
|------|------|-------------|---------|
| **Common** | common | `com.{company}.{system}.common` | dto, exception, constant, util |
| **Domain** | domain-api | `com.{company}.{system}.domain` | entity, vo, service |
| **Domain** | domain-impl | `com.{company}.{system}.domain.service.impl` | - |
| **Infrastructure** | repository-api | `com.{company}.{system}.infrastructure.repository` | api, entity |
| **Infrastructure** | mysql-impl | `com.{company}.{system}.infrastructure.repository.mysql` | config, po, mapper, impl |
| **Application** | application-api | `com.{company}.{system}.application` | dto, service |
| **Application** | application-impl | `com.{company}.{system}.application.service.impl` | - |
| **Interface** | http | `com.{company}.{system}.http` | controller, vo, request, response, handler |
| **Interface** | consumer | `com.{company}.{system}.consumer` | listener, handler, config |
| **Interface** | job | `com.{company}.{system}.job` | task, handler, config |
| **Bootstrap** | bootstrap | `com.{company}.{system}.bootstrap` | config |

### Key Package Principles

**You must comply with**:
- ✅ Entity in repository-api, no framework annotations
- ✅ PO in mysql-impl, contains framework annotations (such as @TableName, @TableId)
- ✅ RepositoryImpl in mysql-impl, responsible for conversion between Entity and PO
- ✅ Use specific technology names (`mysql` instead of `sql`)

**Interface layer responsibility division**:
- **Consumer module**: Listener listens to messages, Handler processes business logic
- **Job module**: Task defines scheduled tasks, Handler processes business logic

## Naming Standards

### Class Naming Standards

| Layer | Object Type | Naming Rule | Example | Description |
|------|---------|---------|------|------|
| **Domain** | Domain entity | Noun (no suffix) | `User`, `Order` | Pure business concept, use business language |
| **Domain** | Value object | Noun (no suffix) | `Address`, `Money` | Immutable object |
| **Domain** | Domain service | `XxxDomainService` | `OrderDomainService` | Domain service interface |
| **Infrastructure** | Domain entity | `XxxEntity` | `UserEntity` | Repository interface use, pure POJO |
| **Infrastructure** | Persistent object | `XxxPO` | `UserPO` | Database mapping, contains framework annotations |
| **Infrastructure** | Repository interface | `XxxRepository` | `UserRepository` | Repository interface |
| **Infrastructure** | Repository implementation | `XxxRepositoryImpl` | `UserRepositoryImpl` | Repository implementation |
| **Infrastructure** | Mapper interface | `XxxMapper` | `UserMapper` | MyBatis Mapper |
| **Application** | Data transfer object | `XxxDTO` | `UserDTO` | Application layer transfer |
| **Application** | Application service | `XxxAppService` | `UserAppService` | Application service interface |
| **Interface** | View object | `XxxVO` | `UserVO` | Frontend display |
| **Interface** | Request object | `XxxRequest` | `CreateUserRequest` | Receive input |
| **Interface** | Response object | `XxxResponse` | `UserDetailResponse` | Return result |
| **Interface** | Controller | `XxxController` | `UserController` | HTTP interface |
| **Interface** | Message listener | `XxxListener` | `OrderCreatedListener` | MQ message listener |
| **Interface** | Message handler | `XxxMessageHandler` | `OrderMessageHandler` | MQ message handler |
| **Interface** | Scheduled task | `XxxTask` | `DataSyncTask` | Scheduled task |
| **Interface** | Task handler | `XxxHandler` | `DataSyncHandler` | Task handler |
| **Common** | Exception class | `XxxException` | `BusinessException` | Exception |
| **Common** | Utility class | `XxxUtil` | `JsonUtil` | Utility class |
| **Common** | Configuration class | `XxxConfig` | `MybatisPlusConfig` | Configuration class |

### Key Naming Principles

1. **Domain layer uses pure business language**:
   - ✅ `User`, `Order` (directly use nouns, no suffix)
   - ❌ `UserEntity`, `OrderEntity` (don't use technical suffixes in Domain layer)

2. **Infrastructure layer clearly distinguishes Entity and PO**:
   - ✅ `UserEntity` (repository-api, pure POJO)
   - ✅ `UserPO` (mysql-impl, contains annotations)

3. **Use specific technology names**:
   - ✅ `com.demo.ordercore.infrastructure.repository.mysql`
   - ❌ `com.demo.ordercore.infrastructure.repository.sql`

## Entity/PO Separation Architecture

### Why Separation Is Needed

1. **Framework independence**: Domain entities don't depend on any persistence framework
2. **Easy to test**: Pure POJOs easy to unit test
3. **Easy to replace**: Can easily switch persistence implementations
4. **DDD compliant**: Entity is domain concept, PO is technical implementation detail

### Differences Between Entity and PO

| Feature | Entity (Domain Entity) | PO (Persistent Object) |
|------|------------------|-----------------|
| **Location** | repository-api module | mysql-impl module |
| **Annotations** | No framework annotations | Contains MyBatis-Plus annotations |
| **Responsibility** | Represent business concept | Map database table |
| **Dependencies** | No framework dependencies | Depends on MyBatis-Plus |
| **Use Cases** | Business layer, application layer | Only used in Repository implementation |

### Conversion Standards

**Convert in RepositoryImpl**:
- Entity → PO: convert when saving data
- PO → Entity: convert when querying data
- Use BeanUtils.copyProperties or specialized conversion tools

## Data Flow Standards

### Data conversion rules you should follow

**Write operation flow**:
Frontend Request → Controller → DTO → Application Service → Domain Entity → Domain Service → Repository Entity → PO → Database

**Read operation flow**:
Database → PO → Repository Entity → Domain Entity → DTO → VO → Frontend

### Key Conversion Points (you must convert at these places)

1. **Controller → Application**: Request/VO → DTO
2. **Application → Domain**: DTO → Domain Entity
3. **Application → Repository**: Domain Entity → Repository Entity
4. **Repository → Mapper**: Repository Entity → PO

**Conversion tools**: Use BeanUtils.copyProperties or specialized conversion tool classes

## MyBatis-Plus Configuration Standards

### How you should configure

**Configuration class location**:
- MybatisPlusConfig placed in config package of mysql-impl module
- Reason: configuration cohesive with implementation, easy to maintain

**Mapper scan path**:
- @MapperScan annotation path must match actual package of Mapper interface
- Use `mysql` instead of `sql` (clarify technology choice)

**Type alias configuration**:
- type-aliases-package configures PO class path, not Entity class path
- MyBatis-Plus directly operates PO, not Entity

**Mapper XML configuration**:
- XML namespace must match fully qualified name of Mapper interface
- resultMap type must match fully qualified name of PO class

## Common Errors and Corrections

### Errors you should avoid

| Error Type | Wrong Approach | Correct Approach | Checking Method |
|---------|---------|---------|---------|
| **Application directly calls Repository** | Application Service depends on Repository | Application Service only depends on Domain Service | Check if application-impl's pom.xml depends on repository-api |
| **Repository-API location** | repository-api in infrastructure | repository-api as submodule of domain | Check if domain/pom.xml contains repository-api |
| **Cache-API location** | cache-api in infrastructure | cache-api as submodule of domain | Check if domain/pom.xml contains cache-api |
| **MQ-API location** | mq-api in infrastructure | mq-api as submodule of domain | Check if domain/pom.xml contains mq-api |
| **Package path** | Using `sql` | Using `mysql` | Check if package name uses specific technology name |
| **Entity/PO location** | Entity and PO both in mysql-impl | Entity in repository-api, PO in mysql-impl | Check if Entity is in api module |
| **Configuration class location** | MybatisPlusConfig in bootstrap | MybatisPlusConfig in mysql-impl | Check if configuration class is cohesive with implementation |
| **Type alias configuration** | type-aliases-package points to Entity | type-aliases-package points to PO | Check if configuration points to PO package |
| **Domain naming** | Domain layer uses `UserEntity` | Domain layer uses `User` | Check if Domain layer class names have technical suffixes |
| **Cross-layer dependency** | Interface layer depends on Domain layer | Interface layer only depends on Application layer | Check if dependency direction follows layering principle |

## Your Checklist

When creating or modifying code, you should check:

### Architecture Layering Check (Most Important)
- [ ] repository-api, cache-api, mq-api as submodules of domain (not in infrastructure)
- [ ] application-impl's pom.xml only depends on domain-api, not repository-api/cache-api/mq-api
- [ ] domain-impl's pom.xml depends on domain-api + repository-api + cache-api + mq-api
- [ ] Application Service only calls Domain Service, not directly call Repository/Cache/MQ
- [ ] Domain Service is the only entry point for data access

### Module and Package Check
- [ ] Module directory structure follows DDD layered architecture
- [ ] Package naming uses specific technology names (mysql instead of sql)
- [ ] Entity in repository-api (pure POJO, no annotations)
- [ ] PO in mysql-impl (contains framework annotations)
- [ ] Configuration classes in corresponding implementation modules (e.g., MybatisPlusConfig in mysql-impl)

### Naming Check
- [ ] Domain layer entities use pure business language (User instead of UserEntity)
- [ ] Infrastructure layer entities use Entity suffix (UserEntity)
- [ ] Persistent objects use PO suffix (UserPO)
- [ ] Application layer uses DTO suffix (UserDTO)
- [ ] Interface layer uses VO/Request/Response suffix

### Dependency Check
- [ ] Dependency direction follows: Interface → Application → Domain ← Infrastructure
- [ ] Application layer doesn't depend on repository-api, cache-api, mq-api (Key)
- [ ] No circular dependencies
- [ ] API modules don't depend on implementation modules
- [ ] Domain layer doesn't depend on Infrastructure layer

### Configuration Check
- [ ] Mapper scan path matches actual package
- [ ] type-aliases-package points to PO class path
- [ ] Mapper XML namespace matches interface fully qualified name

## Key Principles Summary

### DDD Architecture Principles

1. **Strict layering**: Follow DDD layering principles, avoid cross-layer dependencies
2. **Single responsibility**: Maintain single module responsibility
3. **Dependency direction**: Dependency relationships from outer to inner layers
4. **Business language**: Domain layer uses pure business language

### Naming Standard Principles

1. **Domain layer**: Use pure business concepts, no technical suffixes
2. **Infrastructure layer**: Clearly distinguish Entity (domain entity) and PO (persistent object)
3. **Application layer**: Use DTO suffix, design for use cases
4. **Interface layer**: Use VO/Request/Response suffix, design for frontend
5. **Package naming**: Use specific technology names, clarify technology choices

### Entity/PO Separation Principles

1. **Entity in repository-api**: Pure POJO, no framework dependencies
2. **PO in mysql-impl**: Contains framework annotations, only used in Repository implementation
3. **RepositoryImpl responsible for conversion**: Entity ↔ PO conversion completed in Repository implementation
4. **Configuration cohesion**: MybatisPlusConfig placed in mysql-impl module

### Dependency Management Principles

1. **Compile dependency**: For calling interfaces, scope = compile
2. **Implementation dependency**: For implementing interfaces, scope = compile
3. **Runtime dependency**: For packaging startup, only declared in bootstrap
4. **Dependency transitive**: Maven automatically transfers dependencies, no need to declare repeatedly

## Key Benefits

Following these standards can achieve:

- ✅ Clear module boundaries and responsibility division
- ✅ Comply with DDD layered architecture principles
- ✅ Framework-independent domain layer, easy to test and maintain
- ✅ Clear naming standards, high code readability
- ✅ Entity/PO separation, easy to replace persistence implementation
- ✅ Reduce coupling between modules
- ✅ Improve code quality and maintainability
