# Feature Specification: 移除认证功能

**Feature Branch**: `001-remove-auth-features`
**Created**: 2025-12-25
**Status**: Draft
**Input**: User description: "当前项目中，有注册，登录，鉴权等功能，需要移除，这些功能已经在另外一个系统单独实现，当前系统只需要保留业务相关的特性即可。"

## Clarifications

### Session 2025-12-25

- Q: 移除本地认证后，业务接口是否需要从外部系统获取用户身份信息？如果需要，采用什么方式？ → A: 身份通过请求体中的 userId 字段传递
- Q: 移除 t_account 表时，现有的账户数据如何处理？ → A: 直接删除（数据不再需要或已迁移至外部系统）

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 移除认证相关接口 (Priority: P1)

作为系统维护者，我需要移除所有认证相关的接口（注册、登录、登出、令牌刷新），因为这些功能已经在独立的认证系统中实现，当前系统不再需要承担认证职责。

**Why this priority**: 这是核心需求，移除认证接口是整个功能简化的基础。只有先移除接口层，才能安全地移除底层的服务和数据层。

**Independent Test**: 可以通过访问原有的认证接口（如 /api/v1/auth/register、/api/v1/auth/login）验证其已不再可用，返回 404 或被完全移除。

**Acceptance Scenarios**:

1. **Given** 系统部署完成后, **When** 用户访问 /api/v1/auth/register 接口, **Then** 系统返回 404 Not Found 或连接被拒绝
2. **Given** 系统部署完成后, **When** 用户访问 /api/v1/auth/login 接口, **Then** 系统返回 404 Not Found 或连接被拒绝
3. **Given** 系统部署完成后, **When** 用户访问 /api/v1/auth/logout 接口, **Then** 系统返回 404 Not Found 或连接被拒绝
4. **Given** 系统部署完成后, **When** 用户访问 /api/v1/auth/refresh 接口, **Then** 系统返回 404 Not Found 或连接被拒绝

---

### User Story 2 - 移除安全拦截配置 (Priority: P2)

作为系统维护者，我需要移除与认证相关的安全配置（包括安全拦截器、JWT过滤器、权限校验等），使系统不再进行本地认证校验，而是依赖外部认证系统的校验结果。

**Why this priority**: 安全配置的移除需要在接口移除之后进行，以避免产生安全漏洞或功能冲突。

**Independent Test**: 可以通过验证系统启动时不再加载认证相关的安全组件，且业务接口可以正常访问来测试。

**Acceptance Scenarios**:

1. **Given** 系统启动后, **When** 查看系统加载的组件列表, **Then** 不包含 JWT 认证过滤器和认证入口点处理器
2. **Given** 系统启动后, **When** 访问业务相关接口, **Then** 系统不进行本地 JWT 令牌校验
3. **Given** 系统启动后, **When** 查看系统配置, **Then** 不存在本地认证相关的安全配置

---

### User Story 3 - 移除用户账户数据层 (Priority: P3)

作为系统维护者，我需要移除与用户账户相关的数据层代码（包括实体类、数据访问层、数据库表），因为用户数据将由独立的认证系统管理。

**Why this priority**: 数据层的移除应在接口层和服务层移除之后进行，确保没有其他代码依赖这些数据结构。

**Independent Test**: 可以通过验证数据库中不存在账户相关表，且代码中不包含账户相关的数据访问类来测试。

**Acceptance Scenarios**:

1. **Given** 数据库迁移完成后, **When** 查看数据库表结构, **Then** 不存在 t_account 表
2. **Given** 代码清理完成后, **When** 搜索代码库, **Then** 不存在 AccountPO 等账户相关的数据类
3. **Given** 代码清理完成后, **When** 搜索代码库, **Then** 不存在账户相关的 Mapper/Repository 接口

---

### User Story 4 - 清理认证相关依赖和配置 (Priority: P4)

作为系统维护者，我需要清理所有认证相关的依赖项和配置文件，减少系统的复杂度和维护成本。

**Why this priority**: 依赖清理是最后的收尾工作，需要在所有认证代码移除之后进行，以确保不会遗漏任何依赖项。

**Independent Test**: 可以通过构建系统并验证没有未使用的认证相关依赖来测试。

**Acceptance Scenarios**:

1. **Given** 依赖清理完成后, **When** 查看项目依赖配置, **Then** 不包含本地认证所需的特定依赖（如独立的 JWT 库，如果没有其他功能使用）
2. **Given** 配置清理完成后, **When** 查看配置文件, **Then** 不包含认证相关的配置项（如 JWT 密钥配置）
3. **Given** 系统构建完成后, **When** 运行系统, **Then** 系统正常启动且业务功能不受影响

---

### Edge Cases

- 如果有业务代码引用了认证相关的类或方法，移除时需要先解除这些引用
- 如果数据库表之间存在外键依赖，需要按正确的顺序移除或修改
- 如果配置文件中存在对认证组件的引用，需要一并移除以避免启动错误
- t_account 表数据直接删除，无需备份或迁移（数据已不再需要）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须移除用户注册接口（/api/v1/auth/register）
- **FR-002**: 系统必须移除用户登录接口（/api/v1/auth/login）
- **FR-003**: 系统必须移除用户登出接口（/api/v1/auth/logout）
- **FR-004**: 系统必须移除令牌刷新接口（/api/v1/auth/refresh）
- **FR-005**: 系统必须移除 AuthController 及其相关服务类
- **FR-006**: 系统必须移除 JWT 认证过滤器（JwtAuthenticationFilter）
- **FR-007**: 系统必须移除 JWT 认证入口点处理器（JwtAuthenticationEntryPoint）
- **FR-008**: 系统必须移除 JWT 访问拒绝处理器（JwtAccessDeniedHandler）
- **FR-009**: 系统必须移除安全配置类（SecurityConfig）中的本地认证配置
- **FR-010**: 系统必须移除账户实体类（AccountPO）及其数据访问层
- **FR-011**: 系统必须移除账户相关的数据库表（t_account）
- **FR-012**: 系统必须移除资源权限切面（ResourcePermissionAspect）如果其仅用于本地认证
- **FR-013**: 系统移除认证功能后，必须保持所有业务功能正常运行
- **FR-014**: 系统必须清理不再使用的认证相关配置项

### Key Entities

- **AccountPO / t_account**: 用户账户实体，包含用户名、邮箱、密码、角色、状态等字段，需要完全移除
- **AuthController**: 认证控制器，提供注册、登录、登出、令牌刷新等接口，需要完全移除
- **SecurityConfig**: 安全配置类，配置认证和授权规则，需要移除或简化
- **JWT相关组件**: 包括过滤器、入口点处理器、访问拒绝处理器，需要完全移除

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 认证相关代码行数减少 100%（所有认证相关代码被移除）
- **SC-002**: 系统启动时间保持不变或减少（移除不必要的组件加载）
- **SC-003**: 系统构建成功率达到 100%（无编译错误、无未解析的依赖）
- **SC-004**: 所有现有业务功能测试通过率达到 100%
- **SC-005**: 系统部署后，所有业务接口响应正常，无认证相关报错
- **SC-006**: 代码库中不存在认证相关的死代码或未使用的导入

## Assumptions

- 外部认证系统已经独立部署并正常运行
- 当前系统的业务功能不直接依赖本地的用户认证数据
- 业务功能需要用户身份信息时，通过请求体中的 userId 字段传递
- 数据库迁移将由独立的迁移脚本处理
- 移除操作不会影响系统的其他核心业务功能
