# DDD 分层架构指南

## 核心架构原则

本项目遵循**严格的 DDD（领域驱动设计）分层架构**和**六边形架构（Hexagonal Architecture）**原则。

### 关键规则

1. ✅ **Repository/Cache/MQ 接口定义在 Domain 层**（作为独立子模块）
2. ✅ **Application Service 只能调用 Domain Service**（禁止直接调用 Repository/Cache/MQ）
3. ✅ **Domain Service 是数据访问的唯一入口**
4. ✅ **遵循依赖倒置原则（DIP）**：高层模块定义接口，低层模块实现接口

---

## 1. 模块结构

### Domain 层子模块

```
domain/
├── domain-api/              (领域模型和领域服务接口)
│   ├── model/              (聚合根、实体、值对象)
│   └── service/            (领域服务接口)
├── repository-api/          (仓储接口 - 独立子模块)
│   └── repository/         (Repository 接口定义)
├── cache-api/              (缓存接口 - 独立子模块)
│   └── cache/              (Cache 接口定义)
├── mq-api/                 (消息队列接口 - 独立子模块)
│   └── mq/                 (MQ 接口定义)
├── domain-impl/            (领域服务实现)
│   └── service/            (领域服务实现)
└── pom.xml                 (domain 父模块)
```

**为什么这样设计？**

- **Repository/Cache/MQ 接口是领域概念**：描述了如何持久化聚合根、缓存数据、发送消息
- **遵循依赖倒置原则（DIP）**：Domain 层定义接口（Port），Infrastructure 层实现接口（Adapter）
- **符合六边形架构**：Domain 是核心，Infrastructure 是外层适配器
- **独立子模块而非子包**：便于独立管理和版本控制

### Infrastructure 层模块

```
infrastructure/
├── repository/
│   └── mysql-impl/         (Repository 实现 - Adapter)
├── cache/
│   └── redis-impl/         (Cache 实现 - Adapter)
└── mq/
    └── sqs-impl/           (MQ 实现 - Adapter)
```

---

## 2. 依赖关系

### 模块依赖图

```
┌─────────────────────────────────────────────┐
│         Interface Layer (HTTP/MQ/Job)       │
└────────────────┬────────────────────────────┘
                 │ 依赖
                 ▼
┌─────────────────────────────────────────────┐
│         Application Layer                   │
│         (application-impl)                  │
│  ✅ 只依赖 domain-api                        │
│  ❌ 禁止依赖 repository-api/cache-api/mq-api│
└────────────────┬────────────────────────────┘
                 │ 依赖
                 ▼
┌─────────────────────────────────────────────┐
│         Domain API Layer                    │
│         (domain-api)                        │
└─────────────────────────────────────────────┘
                 ▲
                 │ 依赖
┌────────────────┴────────────────────────────┐
│         Domain Impl Layer                   │
│         (domain-impl)                       │
│  ✅ 依赖 repository-api + cache-api + mq-api │
└────┬────────────────────────┬───────────────┘
     │ 依赖                    │ 依赖
     ▼                        ▼
┌────────────────┐      ┌──────────────────────┐
│ Repository API │      │ Cache API / MQ API   │
│ (Port 接口)     │      │ (Port 接口)           │
└────────┬───────┘      └──────────┬───────────┘
         │ 实现                     │ 实现
         ▼                         ▼
┌────────────────┐      ┌──────────────────────┐
│ mysql-impl     │      │ redis-impl/sqs-impl  │
│ (Adapter 实现)  │      │ (Adapter 实现)        │
└────────────────┘      └──────────────────────┘
```

### 依赖规则（严格遵守）

| 层次 | 允许依赖 | 禁止依赖 |
|------|---------|---------|
| **application-impl** | ✅ domain-api | ❌ repository-api, cache-api, mq-api |
| **domain-impl** | ✅ domain-api, repository-api, cache-api, mq-api | ❌ application, interface |
| **mysql-impl** | ✅ repository-api | ❌ domain-impl, application |
| **redis-impl** | ✅ cache-api | ❌ domain-impl, application |
| **sqs-impl** | ✅ mq-api | ❌ domain-impl, application |

---

## 3. Application Service 与 Domain Service 职责边界

### Application Service（用例编排层）

**定位**：编排用例流程，控制事务边界

**✅ 允许的职责**：
1. 事务边界控制（@Transactional）
2. 编排 Domain Service（调用多个 Domain Service 完成用例）
3. DTO 转换（Request/Response → Domain Entity）
4. 权限验证
5. 审计日志记录
6. 异常处理和转换

**❌ 禁止的职责**：
1. 直接调用 Repository（必须通过 Domain Service）
2. 直接调用 Cache（必须通过 Domain Service）
3. 直接调用 MQ（必须通过 Domain Service）
4. 包含复杂业务逻辑（应该在 Domain Service 中）
5. 领域对象的创建逻辑（应该在 Domain Service 中）
6. 密码加密/验证逻辑（应该在 Domain Service 中）

**正确示例**：

```java
@Service
public class AuthApplicationServiceImpl implements AuthApplicationService {
    // ✅ 只依赖 Domain Service
    private final AuthDomainService authDomainService;

    @Override
    @Transactional
    public RegisterResult register(RegisterRequest request) {
        // 1. DTO 转换（Application 层职责）
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();

        // 2. 调用 Domain Service 创建账号（✅ 正确）
        Account account = authDomainService.createAccount(username, email, password);

        // 3. 调用 Domain Service 保存账号（✅ 正确）
        Account savedAccount = authDomainService.saveAccount(account);

        // 4. 记录审计日志（Application 层职责）
        auditLogger.log("USER_REGISTERED", savedAccount.getId());

        // 5. DTO 转换（Application 层职责）
        return RegisterResult.from(savedAccount);
    }
}
```

**错误示例**：

```java
@Service
public class AuthApplicationServiceImpl implements AuthApplicationService {
    // ❌ 错误：不能依赖 Repository
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public RegisterResult register(RegisterRequest request) {
        // ❌ 错误：Application Service 直接调用 Repository
        Account account = accountRepository.findByUsername(request.getUsername());

        // ❌ 错误：Application Service 直接保存数据
        Account savedAccount = accountRepository.save(account);

        return RegisterResult.from(savedAccount);
    }
}
```

### Domain Service（业务逻辑层）

**定位**：实现核心业务逻辑，处理复杂业务规则

**✅ 允许的职责**：
1. 复杂业务规则（跨多个聚合的逻辑）
2. 领域对象创建（工厂方法）
3. 密码加密/验证
4. 会话管理逻辑
5. 账号锁定逻辑
6. 调用 Repository 进行数据访问
7. 调用 Cache 进行缓存操作
8. 调用 MQ 发送消息

**❌ 禁止的职责**：
1. 事务控制（由 Application Service 控制）
2. DTO 转换（由 Application Service 处理）
3. 审计日志记录（由 Application Service 处理）

**正确示例**：

```java
@Service
public class AuthDomainServiceImpl implements AuthDomainService {
    // ✅ 允许：依赖 Repository
    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;

    // ✅ 允许：依赖 Cache
    private final LoginFailureCacheService loginFailureCacheService;

    // ✅ 允许：依赖 MQ
    private final AuthEventMqService authEventMqService;

    // ✅ 允许：依赖密码编码器
    private final PasswordEncoder passwordEncoder;

    @Override
    public Account createAccount(String username, String email, String rawPassword) {
        // 1. 检查用户名是否已存在（✅ 调用 Repository）
        if (accountRepository.existsByUsername(username)) {
            throw new BusinessException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. 密码强度检查（✅ 业务规则）
        PasswordStrengthResult strengthResult = checkPasswordStrength(rawPassword);
        if (!strengthResult.isStrong()) {
            throw new BusinessException(AuthErrorCode.PASSWORD_TOO_WEAK);
        }

        // 3. 加密密码（✅ 领域逻辑）
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 4. 创建领域对象（✅ 工厂方法）
        return Account.create(username, email, encodedPassword);
    }

    @Override
    public Account saveAccount(Account account) {
        // 保存账号（✅ 调用 Repository）
        Account savedAccount = accountRepository.save(account);

        // 发送账号创建事件（✅ 调用 MQ）
        authEventMqService.sendAccountCreatedEvent(savedAccount.getId());

        return savedAccount;
    }
}
```

---

## 4. 为什么要严格分离？

### 1. 清晰的职责边界

- **Application Service**: 编排用例流程
- **Domain Service**: 实现业务逻辑

### 2. 符合六边形架构（Hexagonal Architecture）

- **Domain** 是核心，定义 Port（Repository/Cache/MQ 接口）
- **Infrastructure** 是外层，实现 Adapter
- **Application** 不应该知道外层的存在

### 3. 易于测试

- 测试 Application Service: Mock Domain Service 即可
- 测试 Domain Service: Mock Repository/Cache/MQ 即可

### 4. 易于替换实现

- 切换数据库：只需修改 Infrastructure 层
- Application 和 Domain 无需改动

### 5. 避免逻辑泄漏

- 如果 Application 直接调用 Repository，业务逻辑容易泄漏到 Application 层
- 通过 Domain Service 封装，确保业务逻辑集中管理

---

## 5. 简单 CRUD 如何处理？

**即使是简单查询，也必须通过 Domain Service**：

```java
// ✅ 正确：Application Service 调用 Domain Service
@Override
public AccountInfo getAccountById(Long accountId) {
    Account account = authDomainService.findAccountById(accountId);
    return AccountInfo.from(account);
}

// Domain Service 中实现
@Override
public Account findAccountById(Long accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.ACCOUNT_NOT_FOUND));
}
```

**原因**：
- 统一数据访问入口，便于添加缓存、审计等横切关注点
- 即使当前是简单查询，未来可能需要添加业务规则
- 保持架构一致性，避免混乱

---

## 6. 参考资料

1. **Eric Evans《领域驱动设计》**
   - Repository 是领域模型的一部分
   - Repository 接口定义在 Domain 层

2. **Vaughn Vernon《实现领域驱动设计》**
   - Repository 接口在 Domain 层定义
   - Application Service 通过 Domain Service 访问数据

3. **Hexagonal Architecture (Alistair Cockburn)**
   - Ports（接口）在核心层定义
   - Adapters（实现）在外层实现

4. **Clean Architecture (Robert C. Martin)**
   - 依赖倒置原则（DIP）
   - 高层模块定义接口，低层模块实现接口

---

## 7. 总结

**核心原则**：

1. ✅ **Repository/Cache/MQ 接口在 Domain 层**（作为独立子模块）
2. ✅ **Application Service 只调用 Domain Service**
3. ✅ **Domain Service 是数据访问的唯一入口**
4. ✅ **遵循依赖倒置原则**：Domain 定义 Port，Infrastructure 实现 Adapter
5. ❌ **Application Service 禁止直接调用 Repository/Cache/MQ**

**记住**：好的架构是为业务服务的，严格的分层有助于保持代码的清晰度和可维护性。
