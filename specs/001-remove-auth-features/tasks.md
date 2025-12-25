# Tasks: 移除认证功能

**Input**: Design documents from `/specs/001-remove-auth-features/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 无需新增测试，但需移除认证相关的测试文件

**Organization**: 任务按用户故事组织，支持独立实现和测试

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

本项目采用 Multi-module DDD 架构：
- `bootstrap/` - 启动模块
- `interface/interface-http/` - HTTP 接口层
- `application/application-api/`, `application/application-impl/` - 应用层
- `domain/domain-api/`, `domain/domain-impl/`, `domain/repository-api/` - 领域层
- `infrastructure/repository/mysql-impl/` - 基础设施层

---

## Phase 1: Setup (准备工作)

**Purpose**: 分析依赖关系，确认移除范围

- [x] T001 分析业务代码是否引用认证相关类（搜索 AuthController, AuthApplicationService, AuthDomainService 的引用）
- [x] T002 确认 ResourcePermissionAspect 是否仅用于认证（检查 application/application-impl/src/main/java/.../aspect/ResourcePermissionAspect.java）
- [x] T003 确认 SessionController 和 SessionCompatController 的功能范围（检查 interface/interface-http/src/main/java/.../controller/）
- [x] T004 检查 Spring Security 和 JWT 依赖是否有其他功能使用（检查所有模块的 pom.xml）

---

## Phase 2: Foundational (基础准备)

**Purpose**: 创建数据库迁移脚本（必须在代码移除之前完成）

**⚠️ CRITICAL**: 数据库迁移脚本必须先创建，但在所有代码移除完成前不要执行

- [x] T005 创建 Flyway 迁移脚本 V10__Drop_auth_tables.sql 在 bootstrap/src/main/resources/db/migration/

**迁移脚本内容**:
```sql
-- Drop authentication tables
-- This migration removes the account and session tables as authentication
-- has been moved to an external system.

-- Step 1: Drop t_session first (has foreign key to t_account)
DROP TABLE IF EXISTS t_session;

-- Step 2: Drop t_account
DROP TABLE IF EXISTS t_account;
```

**Checkpoint**: 迁移脚本准备就绪，但暂不执行

---

## Phase 3: User Story 1 - 移除认证相关接口 (Priority: P1)

**Goal**: 移除所有认证相关的 HTTP 接口和应用层服务

**Independent Test**: 访问 /api/v1/auth/* 接口返回 404

### Implementation for User Story 1

- [ ] T006 [US1] 删除 AuthController.java 在 interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/AuthController.java
- [ ] T007 [P] [US1] 删除 SessionController.java 在 interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/SessionController.java
- [ ] T008 [P] [US1] 删除 SessionCompatController.java 在 interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/SessionCompatController.java
- [ ] T009 [US1] 删除 AuthApplicationService.java 在 application/application-api/src/main/java/com/catface996/aiops/application/api/service/auth/AuthApplicationService.java
- [ ] T010 [US1] 删除 AuthApplicationServiceImpl.java 在 application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/auth/AuthApplicationServiceImpl.java
- [ ] T011 [P] [US1] 删除 AuthApplicationServiceImplTest.java 在 application/application-impl/src/test/java/com/catface996/aiops/application/impl/service/auth/AuthApplicationServiceImplTest.java
- [ ] T012 [US1] 删除或修改 ResourcePermissionAspect.java（如果仅用于认证）在 application/application-impl/src/main/java/com/catface996/aiops/application/impl/aspect/ResourcePermissionAspect.java
- [ ] T013 [US1] 运行 `mvn compile -pl interface/interface-http,application/application-api,application/application-impl` 验证编译通过

**Checkpoint**: 认证接口和应用层服务已移除，项目可编译

---

## Phase 4: User Story 2 - 移除安全拦截配置 (Priority: P2)

**Goal**: 移除 Spring Security 和 JWT 相关的安全配置组件

**Independent Test**: 系统启动时不加载 JWT 认证过滤器，业务接口无需认证即可访问

### Implementation for User Story 2

- [ ] T014 [US2] 删除 JwtAuthenticationFilter.java 在 bootstrap/src/main/java/com/catface996/aiops/bootstrap/security/JwtAuthenticationFilter.java
- [ ] T015 [P] [US2] 删除 JwtAuthenticationEntryPoint.java 在 bootstrap/src/main/java/com/catface996/aiops/bootstrap/security/JwtAuthenticationEntryPoint.java
- [ ] T016 [P] [US2] 删除 JwtAccessDeniedHandler.java 在 bootstrap/src/main/java/com/catface996/aiops/bootstrap/security/JwtAccessDeniedHandler.java
- [ ] T017 [US2] 删除或简化 SecurityConfig.java 在 bootstrap/src/main/java/com/catface996/aiops/bootstrap/config/SecurityConfig.java
- [ ] T018 [P] [US2] 删除 SecurityConfigTest.java 在 bootstrap/src/test/java/com/catface996/aiops/bootstrap/config/SecurityConfigTest.java
- [ ] T019 [US2] 删除 bootstrap/src/main/java/com/catface996/aiops/bootstrap/security/ 整个目录（如果为空）
- [ ] T020 [US2] 运行 `mvn compile -pl bootstrap` 验证编译通过

**Checkpoint**: 安全配置已移除，系统不再进行本地 JWT 验证

---

## Phase 5: User Story 3 - 移除用户账户数据层 (Priority: P3)

**Goal**: 移除账户和会话相关的领域层和基础设施层代码

**Independent Test**: 代码库中不存在 AccountPO, SessionPO, AccountRepository, SessionRepository 等类

### Implementation for User Story 3

#### 领域层移除

- [ ] T021 [US3] 删除 AuthDomainService.java 在 domain/domain-api/src/main/java/com/catface996/aiops/domain/api/service/auth/AuthDomainService.java
- [ ] T022 [US3] 删除 AuthDomainServiceImpl.java 在 domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImpl.java
- [ ] T023 [P] [US3] 删除 AuthDomainServiceImplTest.java 在 domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImplTest.java
- [ ] T024 [P] [US3] 删除 AuthDomainServiceImplSessionTest.java 在 domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImplSessionTest.java
- [ ] T025 [P] [US3] 删除 AuthDomainServiceImplLockTest.java 在 domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/auth/AuthDomainServiceImplLockTest.java
- [ ] T026 [US3] 删除 SessionDomainServiceImpl.java（如果存在）在 domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/session/
- [ ] T027 [P] [US3] 删除 SessionDomainServiceImplTest.java 在 domain/domain-impl/src/test/java/com/catface996/aiops/domain/impl/service/session/SessionDomainServiceImplTest.java
- [ ] T028 [P] [US3] 删除 AccountRepository.java 在 domain/repository-api/src/main/java/com/catface996/aiops/repository/auth/AccountRepository.java
- [ ] T029 [P] [US3] 删除 SessionRepository.java 在 domain/repository-api/src/main/java/com/catface996/aiops/repository/auth/SessionRepository.java

#### 基础设施层移除

- [ ] T030 [P] [US3] 删除 AccountPO.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/auth/AccountPO.java
- [ ] T031 [P] [US3] 删除 SessionPO.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/auth/SessionPO.java
- [ ] T032 [P] [US3] 删除 AccountMapper.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/auth/AccountMapper.java
- [ ] T033 [P] [US3] 删除 SessionMapper.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/auth/SessionMapper.java
- [ ] T034 [P] [US3] 删除 AccountRepositoryImpl.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/auth/AccountRepositoryImpl.java
- [ ] T035 [P] [US3] 删除 SessionRepositoryImpl.java 在 infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/auth/SessionRepositoryImpl.java
- [ ] T036 [P] [US3] 删除 AccountRepositoryImplTest.java 在 infrastructure/repository/mysql-impl/src/test/java/com/catface996/aiops/repository/mysql/impl/auth/AccountRepositoryImplTest.java
- [ ] T037 [P] [US3] 删除 SessionRepositoryImplTest.java 在 infrastructure/repository/mysql-impl/src/test/java/com/catface996/aiops/repository/mysql/impl/auth/SessionRepositoryImplTest.java

#### 清理空目录

- [ ] T038 [US3] 删除 domain/repository-api/src/main/java/.../repository/auth/ 目录（如果为空）
- [ ] T039 [P] [US3] 删除 domain/domain-api/src/main/java/.../service/auth/ 目录（如果为空）
- [ ] T040 [P] [US3] 删除 domain/domain-impl/src/main/java/.../service/auth/ 目录（如果为空）
- [ ] T041 [P] [US3] 删除 infrastructure/repository/mysql-impl/src/main/java/.../po/auth/ 目录（如果为空）
- [ ] T042 [P] [US3] 删除 infrastructure/repository/mysql-impl/src/main/java/.../mapper/auth/ 目录（如果为空）
- [ ] T043 [P] [US3] 删除 infrastructure/repository/mysql-impl/src/main/java/.../impl/auth/ 目录（如果为空）
- [ ] T044 [US3] 运行 `mvn compile` 验证全项目编译通过

**Checkpoint**: 所有账户和会话相关代码已移除，项目可编译

---

## Phase 6: User Story 4 - 清理认证相关依赖和配置 (Priority: P4)

**Goal**: 清理 Maven 依赖和配置文件中的认证相关项

**Independent Test**: 系统构建成功，无未使用的依赖，配置文件中无认证相关配置

### Implementation for User Story 4

#### 依赖清理

- [ ] T045 [US4] 检查并移除 spring-boot-starter-security 依赖（如果无其他功能使用）在相关 pom.xml 中
- [ ] T046 [P] [US4] 检查并移除 jjwt-api, jjwt-impl, jjwt-jackson 依赖（如果无其他功能使用）在相关 pom.xml 中

#### 配置清理

- [ ] T047 [US4] 移除 application.yml 中的 JWT 相关配置（如 jwt.secret, jwt.expiration）在 bootstrap/src/main/resources/application.yml
- [ ] T048 [P] [US4] 移除 application-local.yml 中的认证相关配置 在 bootstrap/src/main/resources/application-local.yml
- [ ] T049 [P] [US4] 移除 application-dev.yml 中的认证相关配置 在 bootstrap/src/main/resources/application-dev.yml
- [ ] T050 [P] [US4] 移除 application-test.yml 中的认证相关配置 在 bootstrap/src/main/resources/application-test.yml
- [ ] T051 [P] [US4] 移除 application-staging.yml 中的认证相关配置 在 bootstrap/src/main/resources/application-staging.yml
- [ ] T052 [P] [US4] 移除 application-prod.yml 中的认证相关配置 在 bootstrap/src/main/resources/application-prod.yml

#### 最终验证

- [ ] T053 [US4] 运行 `mvn clean package -DskipTests` 验证项目构建成功
- [ ] T054 [US4] 运行 `mvn test` 验证所有剩余测试通过

**Checkpoint**: 依赖和配置清理完成，项目构建成功

---

## Phase 7: Polish & 数据库迁移

**Purpose**: 执行数据库迁移并进行最终验证

- [ ] T055 启动应用执行 Flyway 迁移（删除 t_session 和 t_account 表）
- [ ] T056 验证数据库中 t_account 和 t_session 表已删除
- [ ] T057 运行 quickstart.md 中的验证步骤
- [ ] T058 验证业务接口正常工作（无认证相关报错）
- [ ] T059 [P] 清理代码中可能残留的未使用 import 语句
- [ ] T060 更新 CLAUDE.md 移除认证相关的技术描述

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖 - 立即开始
- **Foundational (Phase 2)**: 依赖 Setup 完成 - 创建迁移脚本但不执行
- **User Story 1 (Phase 3)**: 依赖 Setup 完成 - 移除接口层
- **User Story 2 (Phase 4)**: 依赖 US1 完成 - 移除安全配置
- **User Story 3 (Phase 5)**: 依赖 US2 完成 - 移除数据层
- **User Story 4 (Phase 6)**: 依赖 US3 完成 - 清理依赖和配置
- **Polish (Phase 7)**: 依赖 US4 完成 - 执行数据库迁移

### User Story Dependencies

```
Phase 1: Setup
    ↓
Phase 2: Foundational (创建迁移脚本)
    ↓
Phase 3: US1 - 移除接口层 (P1)
    ↓
Phase 4: US2 - 移除安全配置 (P2)
    ↓
Phase 5: US3 - 移除数据层 (P3)
    ↓
Phase 6: US4 - 清理依赖配置 (P4)
    ↓
Phase 7: Polish - 数据库迁移 & 验证
```

**注意**: 由于是移除功能，用户故事必须按顺序执行（从高层到底层），不能并行

### Within Each User Story

- 先移除依赖方（使用者），再移除被依赖方（提供者）
- 每个阶段结束后运行编译验证
- 移除文件后检查是否产生空目录

### Parallel Opportunities

- Phase 3 中的 SessionController 和 SessionCompatController 可并行删除（T007, T008）
- Phase 4 中的 JWT 处理器可并行删除（T014, T015, T016）
- Phase 5 中的 PO、Mapper、Repository 实现可并行删除（T030-T037）
- Phase 6 中的配置文件清理可并行进行（T047-T052）

---

## Parallel Example: User Story 3 数据层移除

```bash
# 可并行执行的任务组 1：Repository 接口删除
Task: "删除 AccountRepository.java"
Task: "删除 SessionRepository.java"

# 可并行执行的任务组 2：PO 删除
Task: "删除 AccountPO.java"
Task: "删除 SessionPO.java"

# 可并行执行的任务组 3：Mapper 删除
Task: "删除 AccountMapper.java"
Task: "删除 SessionMapper.java"

# 可并行执行的任务组 4：Repository 实现删除
Task: "删除 AccountRepositoryImpl.java"
Task: "删除 SessionRepositoryImpl.java"

# 可并行执行的任务组 5：测试类删除
Task: "删除 AccountRepositoryImplTest.java"
Task: "删除 SessionRepositoryImplTest.java"
```

---

## Implementation Strategy

### 执行顺序（推荐）

1. **Phase 1**: Setup - 分析依赖关系
2. **Phase 2**: Foundational - 创建迁移脚本（不执行）
3. **Phase 3**: User Story 1 - 移除接口层 → 编译验证
4. **Phase 4**: User Story 2 - 移除安全配置 → 编译验证
5. **Phase 5**: User Story 3 - 移除数据层 → 编译验证
6. **Phase 6**: User Story 4 - 清理依赖配置 → 构建验证
7. **Phase 7**: Polish - 执行数据库迁移 → 运行验证

### 风险控制

- 每个 Phase 结束后验证编译/构建
- 使用 Git 在每个 Phase 结束后创建提交
- 如遇问题可回滚到上一个 Phase 的提交

### 回滚策略

如果某个 Phase 出现问题：
1. `git stash` 保存当前更改
2. `git checkout .` 恢复到上一个提交
3. 分析问题原因
4. 重新执行该 Phase

---

## Notes

- [P] tasks = 不同文件，无依赖关系
- [Story] label 将任务映射到特定用户故事以便追踪
- 由于是移除功能，用户故事必须按顺序执行（从高层到底层）
- 每个 Phase 结束后运行编译验证
- 使用 Git 进行版本控制，便于回滚
- 数据库迁移在代码移除完成后执行
