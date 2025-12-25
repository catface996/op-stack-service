# Research: 移除认证功能

**Feature**: 001-remove-auth-features
**Date**: 2025-12-25

## 技术栈确认

### Decision: 项目技术栈
- **Java Version**: 21 (LTS)
- **Spring Boot Version**: 3.4.1
- **Spring Cloud Version**: 2025.0.0
- **ORM**: MyBatis-Plus 3.5.7
- **Database**: MySQL 8.0
- **Migration Tool**: Flyway
- **Security**: Spring Security + JWT (jjwt 0.12.6)

### Rationale
通过分析 pom.xml 和项目结构确认的实际技术栈。

### Alternatives Considered
N/A - 这是现有项目的技术栈确认，非选择性决策。

---

## 认证组件清单

### Decision: 需要移除的组件完整清单

#### 1. 接口层 (interface)
| 文件路径 | 说明 |
|---------|------|
| `interface/interface-http/src/main/java/.../controller/AuthController.java` | 认证控制器（注册、登录、登出、刷新） |

#### 2. 应用层 (application)
| 文件路径 | 说明 |
|---------|------|
| `application/application-api/src/main/java/.../service/auth/AuthApplicationService.java` | 认证应用服务接口 |
| `application/application-impl/src/main/java/.../service/auth/AuthApplicationServiceImpl.java` | 认证应用服务实现 |
| `application/application-impl/src/test/java/.../service/auth/AuthApplicationServiceImplTest.java` | 测试类 |
| `application/application-impl/src/main/java/.../aspect/ResourcePermissionAspect.java` | 资源权限切面（需确认是否仅用于认证） |

#### 3. 领域层 (domain)
| 文件路径 | 说明 |
|---------|------|
| `domain/domain-api/src/main/java/.../service/auth/AuthDomainService.java` | 认证领域服务接口 |
| `domain/domain-impl/src/main/java/.../service/auth/AuthDomainServiceImpl.java` | 认证领域服务实现 |
| `domain/domain-impl/src/test/java/.../service/auth/AuthDomainServiceImplTest.java` | 测试类 |
| `domain/domain-impl/src/test/java/.../service/auth/AuthDomainServiceImplSessionTest.java` | 测试类 |
| `domain/domain-impl/src/test/java/.../service/auth/AuthDomainServiceImplLockTest.java` | 测试类 |
| `domain/repository-api/src/main/java/.../repository/auth/AccountRepository.java` | 账户仓储接口 |
| `domain/repository-api/src/main/java/.../repository/auth/SessionRepository.java` | 会话仓储接口 |

#### 4. 基础设施层 (infrastructure)
| 文件路径 | 说明 |
|---------|------|
| `infrastructure/repository/mysql-impl/src/main/java/.../po/auth/AccountPO.java` | 账户持久化对象 |
| `infrastructure/repository/mysql-impl/src/main/java/.../po/auth/SessionPO.java` | 会话持久化对象 |
| `infrastructure/repository/mysql-impl/src/main/java/.../mapper/auth/AccountMapper.java` | 账户 Mapper |
| `infrastructure/repository/mysql-impl/src/main/java/.../mapper/auth/SessionMapper.java` | 会话 Mapper |
| `infrastructure/repository/mysql-impl/src/main/java/.../impl/auth/AccountRepositoryImpl.java` | 账户仓储实现 |
| `infrastructure/repository/mysql-impl/src/main/java/.../impl/auth/SessionRepositoryImpl.java` | 会话仓储实现 |
| `infrastructure/repository/mysql-impl/src/test/java/.../impl/auth/AccountRepositoryImplTest.java` | 测试类 |
| `infrastructure/repository/mysql-impl/src/test/java/.../impl/auth/SessionRepositoryImplTest.java` | 测试类 |

#### 5. 启动模块 (bootstrap) - 安全配置
| 文件路径 | 说明 |
|---------|------|
| `bootstrap/src/main/java/.../config/SecurityConfig.java` | Spring Security 配置 |
| `bootstrap/src/main/java/.../security/JwtAuthenticationFilter.java` | JWT 认证过滤器 |
| `bootstrap/src/main/java/.../security/JwtAuthenticationEntryPoint.java` | JWT 认证入口点 |
| `bootstrap/src/main/java/.../security/JwtAccessDeniedHandler.java` | JWT 访问拒绝处理器 |
| `bootstrap/src/test/java/.../config/SecurityConfigTest.java` | 测试类 |

#### 6. 数据库迁移 (需要创建回滚迁移)
| 文件路径 | 说明 |
|---------|------|
| `V1__Create_account_table.sql` | 创建 t_account 表 |
| `V2__Create_session_table.sql` | 创建 t_session 表（外键依赖 t_account） |
| `V4__Add_session_columns.sql` | 添加会话列（需检查内容） |

### Rationale
通过代码搜索确认的完整组件清单，涵盖 DDD 架构的各层。Session 表有外键依赖于 Account 表，必须一并移除。

### Alternatives Considered
- 仅移除接口层：不可行，会留下大量死代码
- 保留 Session 功能：不可行，因为 t_session 有外键依赖 t_account

---

## 数据库迁移策略

### Decision: 使用 Flyway 新迁移脚本删除表

创建新的迁移脚本 `V10__Drop_auth_tables.sql`：
1. 先删除 t_session 表（因为有外键依赖）
2. 再删除 t_account 表

### Rationale
- Flyway 要求按版本号顺序执行迁移
- 不能修改已执行的迁移脚本
- 需要创建新的迁移脚本来删除表
- 删除顺序必须考虑外键约束

### Alternatives Considered
- 手动删除表：不可行，无法与 Flyway 版本控制同步
- 修改原迁移脚本：不可行，违反 Flyway 原则

---

## 依赖清理策略

### Decision: 条件性移除 Spring Security 和 JWT 依赖

需要检查以下依赖是否有其他功能使用：
- `spring-boot-starter-security`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

如果仅用于本地认证，则可以移除。

### Rationale
移除不再使用的依赖可以减少构建时间和潜在的安全漏洞。

### Alternatives Considered
- 保留依赖：可能造成不必要的依赖和安全风险
- 仅移除 JWT 依赖：如果 Spring Security 有其他用途则保留

---

## Session 相关组件处理

### Decision: 完全移除 Session 相关组件

Session 组件列表：
- SessionPO, SessionMapper, SessionRepository, SessionRepositoryImpl
- SessionDomainServiceImpl 及相关测试
- SessionController, SessionCompatController（需确认）

### Rationale
Session 表有外键依赖 t_account，且 Session 功能是认证系统的一部分，应该一并移除。

### Alternatives Considered
- 保留 Session 但移除外键：增加复杂度，且 Session 功能本身是认证的一部分

---

## 移除顺序

### Decision: 按层级依赖关系移除

推荐移除顺序：
1. **接口层**：AuthController, SessionController, SessionCompatController
2. **应用层**：AuthApplicationService 及实现, ResourcePermissionAspect
3. **领域层**：AuthDomainService, SessionDomainService 及实现、仓储接口
4. **基础设施层**：PO, Mapper, Repository 实现
5. **安全配置**：SecurityConfig, JWT 相关组件
6. **数据库**：创建 Flyway 迁移脚本删除表
7. **依赖清理**：移除不再使用的 Maven 依赖
8. **配置清理**：移除配置文件中的相关配置项

### Rationale
从高层向底层移除可以在每一步验证编译是否通过，减少一次性移除带来的问题定位困难。

### Alternatives Considered
- 一次性移除所有文件：风险较高，难以定位问题
- 从底层开始移除：会导致编译失败，难以验证
