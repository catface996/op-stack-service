<!--
Sync Impact Report:
- Version change: 初始版本 → 1.0.0
- New constitution created with 8 core principles
- Added sections: Core Principles, Development Workflow, Quality Standards, Governance
- Templates requiring updates:
  ✅ plan-template.md - Constitution Check 已对齐
  ✅ spec-template.md - 需求结构已对齐
  ✅ tasks-template.md - 任务结构已对齐
- Follow-up TODOs: None
-->

# AIOps Service 项目宪法

## 核心原则

### I. 渐进式开发 (Incremental Development)

所有功能开发 MUST 遵循渐进式流程,每个阶段完成后必须验证通过才能进入下一阶段:

```
需求分析 → 验证 → 架构设计 → 验证 → 任务拆分 → 验证 → 实现 → 验证
```

**强制要求**:
- 每个阶段 MUST 有明确的交付物和验证标准
- 阶段验证 MUST 包含: 完整性、一致性、准确性、合理性检查
- 禁止跳过验证环节直接进入下一阶段
- 禁止在前一阶段未完成时开始下一阶段

**理由**: 分阶段推进可以及早发现问题,降低修复成本。需求阶段多花 1 小时,可以节省设计和实现阶段的 10 小时。

### II. DDD 分层架构 (DDD Layered Architecture)

本项目 MUST 严格遵循领域驱动设计 (Domain-Driven Design) 的分层架构:

- **接口层 (Interface)**: 处理外部请求 (HTTP、MQ Consumer、定时任务),负责输入输出转换
- **应用层 (Application)**: 编排业务用例,协调领域服务完成业务流程
- **领域层 (Domain)**: 封装核心业务规则和领域逻辑,保持技术无关性
- **基础设施层 (Infrastructure)**: 提供技术实现 (数据库、缓存、消息队列)

**依赖方向 MUST 遵循**: 外层依赖内层,内层永不反向依赖
- Interface → Application → Domain
- Application → Infrastructure API (而非 Implementation)

**理由**: 清晰的层次边界确保职责单一、模块解耦、技术可替换,支持长期演进和微服务拆分。

### III. 持续编译验证 (Continuous Compilation Validation)

每个任务完成后 MUST 确保整个工程可以成功编译:

**强制要求**:
- 每次修改 POM 配置后 MUST 立即运行 `mvn clean compile` 验证
- 创建新模块时 MUST 同步更新父 POM 的 `<modules>` 声明
- 只声明已创建的模块,禁止预先声明尚未创建的模块 (渐进式模块声明)
- 模块依赖关系 MUST 正确配置,避免循环依赖

**验证优先级**:
1. **运行时验证** (最优先): 能通过实际运行验证的功能必须运行验证
2. **编译验证** (次优先): 结构性变更通过编译验证
3. **静态检查** (最后): 仅在无法通过上述方式时使用

**理由**: 在多模块 Maven 项目中,编译失败会阻塞所有开发工作,持续验证确保项目始终处于可构建状态。

### IV. 中文优先 (Chinese-First Communication)

所有对话、文档和注释 MUST 使用中文,除非明确属于以下例外情况:

**必须使用中文**:
- 与用户的所有对话交流
- 需求文档、设计文档、任务文档
- 代码注释和文档说明
- 问题讨论和方案说明

**允许使用英文**:
- 代码本身 (变量名、函数名、类名)
- 技术术语 (API、JSON、Maven、POM、DDD)
- EARS 语法关键字 (THE、SHALL、WHEN、WHILE、IF、THEN、WHERE)
- 引用的技术文档和规范

**理由**: 确保沟通清晰准确,提高文档可读性,符合团队语言习惯,避免语言障碍。

### V. 依赖版本统一管理 (Unified Dependency Management)

所有 Maven 依赖版本 MUST 在父 POM 的 `<dependencyManagement>` 中统一管理:

**强制要求**:
- 父 POM MUST 导入 Spring Boot BOM 和 Spring Cloud BOM
- 第三方库版本 (MyBatis-Plus、Druid、AWS SDK 等) MUST 在父 POM 中声明
- 子模块声明依赖时 MUST NOT 指定 `<version>`,由父 POM 统一管理
- 模块命名 MUST 使用首字母大写的英文单词,单词间用空格分隔 (如 "Domain API")

**版本要求**:
- **JDK**: 21 (LTS 版本)
- **Spring Boot**: 3.4.1 (最新稳定版)
- **Spring Cloud**: 2025.0.0
- **MyBatis-Plus**: 3.5.7 (MUST 使用 `mybatis-plus-spring-boot3-starter`)

**理由**: 统一版本管理避免依赖冲突,确保项目依赖一致性,简化依赖升级维护。

### VI. Entity/PO 分离 (Entity/PO Separation)

领域实体 (Entity) 和持久化对象 (PO) MUST 严格分离:

**架构规范**:
- **Entity**: 位于 `repository-api` 模块,纯 POJO,无框架注解,表示业务概念
- **PO**: 位于 `mysql-impl` 模块,包含 MyBatis-Plus 注解,映射数据库表
- **转换职责**: RepositoryImpl MUST 负责 Entity ↔ PO 转换

**命名规范**:
- Domain 层: `User`, `Order` (纯业务概念,无技术后缀)
- Repository API 层: `UserEntity`, `OrderEntity` (领域实体)
- MySQL 实现层: `UserPO`, `OrderPO` (持久化对象)

**配置规范**:
- `MybatisPlusConfig` MUST 放在 `mysql-impl` 模块 (配置内聚原则)
- `@MapperScan` MUST 扫描具体技术路径 (如 `com.demo.infrastructure.repository.mysql.mapper`)
- `type-aliases-package` MUST 指向 PO 类路径 (而非 Entity)

**理由**: 框架无关的领域实体易于测试和维护,可轻松切换持久化实现,符合 DDD 原则。

### VII. MyBatis-Plus 数据操作规范 (MyBatis-Plus Data Operation Standards)

为了便于统一管理、代码审查和性能分析,数据操作 MUST 遵循以下规范:

**允许使用 MyBatis-Plus API**:
- ✅ 插入操作: `save()`, `saveBatch()`, `saveOrUpdate()` 等
- ✅ 根据主键更新: `updateById()`, `updateBatchById()` 等
- ✅ 根据主键查询: `getById()`, `listByIds()` 等

**必须在 Mapper XML 中实现**:
- ❌ 所有条件查询 (不使用 Wrapper)
- ❌ 所有条件更新 (不使用 UpdateWrapper)
- ❌ 所有条件删除 (不使用 QueryWrapper)
- ❌ 所有复杂查询 (多表关联、子查询、聚合等)

**XML 组织规范**:
- 所有 Mapper XML MUST 放在 `mysql-impl/src/main/resources/mapper/` 目录
- 每个 SQL 语句 MUST 有清晰的注释说明
- 使用 `<if test>` 实现动态 SQL,而非 Wrapper
- namespace MUST 与 Mapper 接口全限定名一致
- resultMap type MUST 与 PO 类全限定名一致

**规范理由**:
1. 统一管理: 所有 SQL 集中在 XML,便于查找、维护和优化
2. 代码审查: DBA 可快速审查所有 SQL,及时发现性能问题
3. 性能分析: 便于使用工具分析 SQL 性能,添加索引
4. 可维护性: SQL 语句清晰可见,避免动态 SQL 难以追踪

**理由**: 规范化数据操作提高代码质量,降低维护成本,便于性能优化和问题排查。

### VIII. ADR 架构决策记录 (Architecture Decision Record)

重要的架构决策 MUST 通过 ADR 记录:

**ADR 结构**:
- **标题**: 简短描述决策内容
- **状态**: 提议中、已接受、已废弃、已替代
- **背景**: 为什么需要做这个决策?面临什么问题?
- **决策**: 具体的决策内容是什么?
- **理由**: 为什么选择这个方案?考虑了哪些因素?
- **后果**: 这个决策带来的影响 (正面和负面)

**记录时机**:
- 技术栈选型 (如选择 MyBatis-Plus 而非 JPA)
- 架构模式选择 (如选择分层架构而非微服务)
- 数据库选型 (如选择 MySQL 而非 NoSQL)
- 重要的设计决策 (如 Entity/PO 分离)

**理由**: 记录决策的上下文和理由,帮助团队理解技术选择,为未来的架构演进提供参考。

## 开发工作流程

### 需求阶段

**必须执行的步骤**:
1. 准备详细的原始需求文档
2. 使用 EARS 语法编写结构化需求 (关键字保留英文大写)
3. 进行多维度验证 (完整性、一致性、准确性、必要性)
4. 识别关键实体和领域概念
5. 与用户确认所有模糊点和不确定内容

**需求质量标准**:
- 所有需求 MUST 使用 EARS 语法
- 每个功能需求 MUST 有明确的验收标准 (至少 2-3 个)
- 考虑边界场景和异常处理 (输入边界、并发、网络异常、权限边界等)
- 使用 MoSCoW 方法分类需求优先级 (Must/Should/Could/Won't)

### 设计阶段

**必须执行的步骤**:
1. 充分理解需求文档
2. 进行必要的技术调研和选型
3. 创建完整的设计文档 (架构设计、详细设计、非功能性设计)
4. 使用 ADR 记录重要的架构决策
5. 设计验证 (与需求一致性、设计内部一致性、合理性、可实施性)
6. 与用户确认设计方案

**设计文档要求**:
- MUST 包含系统架构图和模块划分
- MUST 定义清晰的接口和数据结构
- MUST 说明非功能性设计 (性能、安全、可观测性)
- MUST 识别潜在风险并制定应对策略

### 任务拆分阶段

**必须执行的步骤**:
1. 按用户故事 (User Story) 组织任务
2. 每个用户故事 MUST 独立可测试、可交付
3. 标识任务间的依赖关系和并行机会
4. 定义明确的验收标准

**任务组织要求**:
- MUST 区分 Setup → Foundational → User Story 1 → User Story 2 的阶段
- Foundational 阶段 MUST 完成后才能开始用户故事实现
- 每个用户故事 MUST 标注优先级 (P1/P2/P3)

### 实现阶段

**必须执行的步骤**:
1. 遵循渐进式模块声明原则
2. 每个任务完成后 MUST 验证编译成功
3. MUST 在实现后立即验证功能
4. 及时提交代码,避免大批量提交

**代码质量要求**:
- MUST 遵循命名规范和代码规范
- MUST 实现统一的异常处理
- MUST 配置结构化日志 (包含 traceId、spanId)
- MUST 区分项目包和框架包的日志级别

## 质量标准

### 命名规范

**Package 命名**: `com.{company}.{system}.{layer}.{module}`

**Domain 层**:
- 纯业务语言,无技术后缀: `User`, `Order`

**Infrastructure 层**:
- 明确技术选型: 使用 `mysql`、`redis`、`sqs` (而非 `sql`、`cache`、`mq`)
- 领域实体: `UserEntity`, `OrderEntity`
- 持久化对象: `UserPO`, `OrderPO`

**Application 层**:
- 数据传输对象: `UserDTO`, `OrderDTO`

**Interface 层**:
- 视图对象: `UserVO`
- 请求对象: `CreateUserRequest`
- 响应对象: `UserDetailResponse`

### 配置规范

**多环境配置**:
- MUST 支持 local/dev/test/staging/prod 环境
- 配置文件命名: `application-{profile}.yml`

**日志配置**:
- MUST 在 `logback-spring.xml` 中管理,禁止在 `application.yml` 中配置日志
- 使用 `<springProfile>` 标签区分环境
- MUST 区分项目包和框架包的日志级别:
  - 项目包 (`com.catface`): 开发/测试 DEBUG,生产 INFO
  - 框架包 (`org.springframework`, `com.baomidou`, `com.amazonaws`): 所有环境 WARN
- Local 环境输出到控制台,其他环境输出到文件 (JSON 格式)

**依赖配置**:
- API 模块 MUST NOT 依赖实现模块
- 下层 MUST NOT 依赖上层
- MUST NOT 存在循环依赖

### 验证标准

**编译验证**:
- MUST 执行 `mvn clean compile` 确保构建成功
- MUST 检查 Reactor Build Order 确认模块顺序正确

**运行时验证**:
- 可运行的功能 MUST 通过实际运行验证
- MUST 检查日志输出格式和内容
- MUST 验证监控端点可访问 (`/actuator/prometheus`)
- MUST 验证异常处理返回正确格式

## 治理

### 宪法权威

本宪法 (Constitution) 是 AIOps Service 项目的最高开发规范,所有代码实现、架构决策、开发流程 MUST 遵守本宪法定义的原则。

### 修订流程

**修订触发条件**:
- 发现现有原则存在根本性缺陷
- 技术栈发生重大变更 (如从 Spring Boot 2 升级到 3)
- 团队达成共识需要调整开发规范

**修订流程**:
1. 提出修订提案,说明修订理由和影响范围
2. 团队评审和讨论,达成共识
3. 更新本宪法文档,按语义化版本规则递增版本号
4. 同步更新所有依赖宪法的模板文件和文档
5. 记录修订历史在文件头部的 Sync Impact Report 中

**版本递增规则**:
- **MAJOR**: 向后不兼容的原则变更 (如移除某个核心原则)
- **MINOR**: 新增原则或大幅扩展现有原则
- **PATCH**: 措辞优化、错误修正、非语义性调整

### 合规性检查

**代码审查 (Code Review)**:
- 所有 Pull Request MUST 验证是否符合本宪法原则
- 违反宪法的代码 MUST 拒绝合并,直到修正

**架构审查 (Architecture Review)**:
- 新增模块、变更依赖关系 MUST 经过架构审查
- 确认是否符合 DDD 分层原则和依赖方向规则

**复杂度审查**:
- 如果需要违反宪法原则 (如增加第 4 个聚合模块),MUST 在设计文档中明确说明:
  - 为什么需要这个复杂度
  - 为什么更简单的方案不适用
  - 如何控制复杂度增长

### 宪法解释权

当宪法条款存在歧义或需要具体化时,由项目架构师或技术负责人进行解释和裁定。

**Version**: 1.0.0 | **Ratified**: 2025-11-21 | **Last Amended**: 2025-11-21
