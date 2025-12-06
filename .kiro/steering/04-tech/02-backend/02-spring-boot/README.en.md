---
inclusion: manual
---

# Spring Boot Development Best Practices

## Role Definition

You are a backend development expert proficient in Spring Boot 3.x, skilled in auto-configuration, web development, data access, and microservice architecture.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Layered Architecture | MUST follow Controller → Service → Repository layering | Code coupling, hard to test |
| Dependency Injection | MUST use constructor injection, prohibit field injection | Hard to test, circular dependencies hard to trace |
| Transaction Boundaries | MUST declare transactions at Service layer, prohibit in Controller | Transaction failure, data inconsistency |
| Configuration Externalization | MUST use environment variables or config center for sensitive configs | Security risks, deployment difficulties |

---

## Prompt Templates

### Project Setup

```
Please help me set up a Spring Boot project:
- Spring Boot version: [3.x]
- Java version: [17/21]
- Build tool: [Maven/Gradle]
- Required features: [Web API/Database/Cache/Security/Message Queue]
- Database type: [MySQL/PostgreSQL/MongoDB]
```

### Feature Development

```
Please help me implement Spring Boot functionality:
- Feature description: [describe feature]
- Entities involved: [list entities and relationships]
- API design: [RESTful endpoints]
- Transaction required: [yes/no]
- Cache required: [yes/no]
```

### Problem Troubleshooting

```
Please help me troubleshoot a Spring Boot issue:
- Error phenomenon: [describe error]
- Error logs: [key log information]
- Trigger scenario: [what operation triggers it]
- Attempted solutions: [what has been tried]
```

---

## Decision Guide

### Data Access Solution Selection

```
Data access requirements?
├─ Simple CRUD → Spring Data JPA
├─ Complex queries
│   ├─ Dynamic conditions → QueryDSL / Specification
│   └─ Native SQL → MyBatis / JDBC Template
├─ Multiple data sources → Configure multiple DataSource + @Qualifier
└─ Read-write separation → ShardingSphere / Dynamic DataSource
```

### Cache Solution Selection

```
Caching requirements?
├─ Local cache (standalone) → Caffeine
├─ Distributed cache
│   ├─ Simple KV → Redis String
│   ├─ Complex data structures → Redis Hash/Set/ZSet
│   └─ Multi-level cache → Caffeine + Redis
└─ No caching needed → Don't introduce (simplicity first)
```

### Async Processing Solution

```
Async requirements?
├─ Simple async tasks → @Async + ThreadPoolTaskExecutor
├─ Scheduled tasks
│   ├─ Standalone → @Scheduled
│   └─ Distributed → XXL-Job / Elastic-Job
├─ Message-driven → RocketMQ / Kafka
└─ Workflow → Camunda / Flowable
```

---

## Good vs Bad Examples

### Dependency Injection

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| @Autowired field injection | Constructor injection (recommend @RequiredArgsConstructor) | Field injection can't be used in unit tests |
| Solve circular dependency with @Lazy | Refactor design, extract common service | @Lazy masks design issues |
| Inject concrete implementation | Inject interface type | Reduce coupling, easy to Mock |

### Transaction Management

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| @Transactional in Controller | Add transaction at Service layer | Transaction boundary should be at business logic layer |
| Add transaction to all methods | Only add transaction to methods that need it | Unnecessary transaction overhead |
| Ignore transaction propagation behavior | Explicitly specify propagation | Default behavior may not meet expectations |
| Internal method calls expecting transaction | Call through proxy or split into different classes | Internal calls don't go through proxy |

### Exception Handling

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Catch Exception uniformly | Distinguish business vs system exceptions | Easier to locate problems |
| Only log in catch without rethrowing | Rethrow or return error response | Swallowing exceptions makes debugging hard |
| Manually handle exceptions in each Controller | Use @RestControllerAdvice for global handling | Reduce duplicate code |

### Configuration Management

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Hardcode passwords/keys | Use environment variables or config center | Security risks |
| Directly use @Value injection | Use @ConfigurationProperties for type-safe binding | Type safety, validatable |
| One application.yml with all configs | Split by environment application-{profile}.yml | Environment isolation |

---

## Validation Checklist

### Development Phase

- [ ] Following layered architecture? (Controller → Service → Repository)
- [ ] Using constructor injection?
- [ ] Transaction annotations at Service layer?
- [ ] Sensitive configs externalized?
- [ ] Unified response format?
- [ ] Global exception handling?

### Security Phase

- [ ] Parameter validation with @Valid?
- [ ] SQL injection prevention? (using parameterized queries)
- [ ] XSS protection? (input/output escaping)
- [ ] API rate limiting?
- [ ] Sensitive data encrypted?

### Deployment Phase

- [ ] Health check endpoint configured?
- [ ] Graceful shutdown configured?
- [ ] Monitoring metrics exposed?
- [ ] Unified log format?
- [ ] API documentation?

---

## Guardrails

**Allowed (✅)**:
- Use Spring Boot 3.x + Java 17+
- Use Lombok to simplify code
- Use MapStruct for object mapping
- Use Swagger/OpenAPI documentation

**Prohibited (❌)**:
- NEVER operate database directly in Controller layer
- NEVER call external HTTP interfaces in transactional methods
- NEVER use @Autowired field injection
- NEVER expose exception details directly to frontend
- NEVER hardcode passwords in application.yml

**Needs Clarification (⚠️)**:
- Database type: [NEEDS CLARIFICATION: MySQL/PostgreSQL/MongoDB?]
- Cache needed: [NEEDS CLARIFICATION: use case?]
- Authentication scheme: [NEEDS CLARIFICATION: Session/JWT/OAuth2?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| Transaction not rolling back | Exception caught but not thrown, non-RuntimeException | Configure rollbackFor, don't swallow exceptions |
| Circular dependency | Beans mutually dependent | Refactor design, extract common service |
| Bean injection is null | Not managed by Spring, used in static methods | Check annotations, avoid static context |
| Config not effective | Wrong profile, incorrect config filename | Check spring.profiles.active |
| Slow API response | Slow database queries, external call blocking | Add indexes, make async, set timeouts |
| Out of memory | Large objects not released, querying all data | Use pagination, stream processing |

---

## Project Structure Standards

```
src/main/java/com/example/
├── Application.java           # Main class, MUST be in root package
├── config/                    # Config classes (Security, Redis, Swagger...)
├── controller/                # Controllers, only param validation and response wrapping
├── service/                   # Business logic, transactions at this layer
│   └── impl/                  # Implementations
├── repository/                # Data access layer
├── entity/                    # Database entities
├── dto/                       # Data Transfer Objects
│   ├── request/               # Request objects
│   └── response/              # Response objects
├── exception/                 # Custom exceptions
├── common/                    # Common components (Result, PageResult...)
└── util/                      # Utility classes (SHOULD minimize)
```

---

## Output Format Requirements

When generating Spring Boot functionality, MUST follow this structure:

```
## Feature Description
- Feature name: [name]
- API endpoints: [list API endpoints]
- Entities involved: [list entities]

## Implementation Points
1. [Key implementation point 1]
2. [Key implementation point 2]

## Configuration
- [Required configuration items]

## Considerations
- [Edge cases and constraints]
```
