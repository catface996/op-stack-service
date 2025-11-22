# 实施计划：MyBatis Plus 集成与节点管理仓储

**分支**: `001-mybatis-plus-integration` | **日期**: 2025-11-22 | **规格说明**: [spec.md](./spec.md)
**输入**: 功能规格说明来自 `/specs/001-mybatis-plus-integration/spec.md`

## 概要

本功能实现 MyBatis-Plus ORM 框架集成，并完成 NodeEntity（节点实体）的持久化功能。NodeEntity 用于管理系统节点（数据库、应用程序、API、报表等），支持 CRUD 操作、分页查询、逻辑删除和乐观锁并发控制。技术方案采用 Entity/PO 分离架构，Repository 层提供框架无关的数据访问接口，MySQL 实现层负责具体的持久化逻辑。

## 技术上下文

**语言/版本**: Java 21 (LTS)
**主要依赖**: Spring Boot 3.4.1, Spring Cloud 2025.0.0, MyBatis-Plus 3.5.7 (mybatis-plus-spring-boot3-starter), Druid 1.2.20
**存储**: MySQL 8.x (UTF8MB4字符集), 数据库表 t_node
**测试**: Spring Boot Test + JUnit 5, 使用 @SpringBootTest + @Transactional 确保测试隔离
**目标平台**: JVM 服务端应用，支持多环境部署 (local/dev/test/staging/prod)
**项目类型**: 多模块 Maven 工程，DDD 分层架构
**性能目标**:
- 单条记录保存 < 100ms
- 按 ID 查询 < 50ms
- 分页查询 < 200ms
- 支持 100 个并发保存/更新操作
**约束**:
- 分页每页最大 100 条记录
- name 字段唯一约束
- 使用雪花算法生成 ID
- 所有条件查询必须在 Mapper XML 中定义（不使用 Wrapper）
**规模/范围**:
- 1 个核心实体 (NodeEntity)
- 基础 CRUD + 分页查询功能
- 5 个环境配置（不同连接池大小）

## 宪法检查

*关卡：必须在 Phase 0 研究前通过。在 Phase 1 设计后重新检查。*

### I. 渐进式开发 ✅

- **状态**: 通过
- **验证**: 功能规格说明已完成并澄清配置细节，现在进入架构设计阶段，遵循渐进式流程

### II. DDD 分层架构 ✅

- **状态**: 通过
- **设计**:
  - **Repository API 层** (`infrastructure/repository/repository-api`): 定义 NodeEntity 和 NodeRepository 接口（框架无关）
  - **MySQL 实现层** (`infrastructure/repository/mysql-impl`): 实现 NodeRepositoryImpl、NodePO、NodeMapper、SQL XML
  - **依赖方向**: Application → Repository API ← MySQL Impl，符合 DDD 原则

### III. 持续编译验证 ✅

- **状态**: 通过
- **计划**: 每个任务完成后执行 `mvn clean compile` 验证，只声明已创建的模块

### IV. 中文优先 ✅

- **状态**: 通过
- **执行**: 所有文档、注释使用中文，代码和技术术语使用英文

### V. 依赖版本统一管理 ✅

- **状态**: 通过
- **实施**:
  - 父 POM 的 `<properties>` 定义版本号: `mybatis-plus.version=3.5.7`, `druid.version=1.2.20`
  - 父 POM 的 `<dependencyManagement>` 声明依赖
  - 子模块不指定版本号，继承自父 POM

### VI. Entity/PO 分离 ✅

- **状态**: 通过
- **架构**:
  - **NodeEntity**: `repository-api` 模块，纯 POJO，无框架注解
  - **NodePO**: `mysql-impl` 模块，包含 MyBatis-Plus 注解（@TableName、@TableId、@TableField、@TableLogic、@Version）
  - **转换**: NodeRepositoryImpl 负责 Entity ↔ PO 双向转换

### VII. MyBatis-Plus 数据操作规范 ✅

- **状态**: 通过
- **规范**:
  - ✅ 使用 API: `save()`, `updateById()`, `selectById()` 等简单操作
  - ✅ XML 实现: `findByName`, `findByType`, `findPage` 等条件查询
  - ✅ 所有 SQL 在 `mysql-impl/src/main/resources/mapper/NodeMapper.xml` 中管理

### VIII. ADR 架构决策记录 ⚠️

- **状态**: 不适用当前阶段
- **说明**: 本功能为框架集成，无重大架构决策。技术选型（MyBatis-Plus、Entity/PO 分离）已在项目宪法中确立

## 项目结构

### 文档（本功能）

```text
specs/001-mybatis-plus-integration/
├── spec.md              # 功能规格说明
├── plan.md              # 本文件 (/speckit.plan 命令输出)
├── research.md          # Phase 0 输出 (/speckit.plan 命令)
├── data-model.md        # Phase 1 输出 (/speckit.plan 命令)
├── quickstart.md        # Phase 1 输出 (/speckit.plan 命令)
├── contracts/           # Phase 1 输出 (/speckit.plan 命令)
│   └── NodeRepository.md  # Repository 接口契约
└── tasks.md             # Phase 2 输出 (/speckit.tasks 命令 - 不由 /speckit.plan 创建)
```

### 源代码（仓库根目录）

```text
infrastructure/repository/
├── repository-api/      # 仓储API模块（框架无关）
│   └── src/main/java/com/demo/infrastructure/repository/
│       ├── api/
│       │   └── NodeRepository.java        # 仓储接口
│       └── entity/
│           └── NodeEntity.java            # 领域实体（纯POJO）
│
└── mysql-impl/          # MySQL实现模块（MyBatis-Plus）
    ├── pom.xml          # 依赖: mybatis-plus-spring-boot3-starter, druid, mysql-connector-j
    └── src/main/
        ├── java/com/demo/infrastructure/repository/mysql/
        │   ├── config/
        │   │   ├── MybatisPlusConfig.java           # MyBatis-Plus配置（插件注册）
        │   │   └── CustomMetaObjectHandler.java     # 元数据自动填充
        │   ├── impl/
        │   │   └── NodeRepositoryImpl.java          # 仓储实现（Entity/PO转换）
        │   ├── mapper/
        │   │   └── NodeMapper.java                  # Mapper接口（继承BaseMapper）
        │   └── po/
        │       └── NodePO.java                      # 持久化对象（MyBatis-Plus注解）
        └── resources/
            └── mapper/
                └── NodeMapper.xml                    # SQL语句管理

common/                  # 通用模块
└── src/main/java/com/demo/common/dto/
    └── PageResult.java  # 通用分页结果类（已存在或需创建）

bootstrap/               # 启动模块
└── src/main/resources/
    ├── application.yml                    # MyBatis-Plus全局配置
    ├── application-local.yml              # Local环境数据源配置
    ├── application-dev.yml                # Dev环境数据源配置（待提供）
    ├── application-test.yml               # Test环境数据源配置（待提供）
    ├── application-staging.yml            # Staging环境数据源配置（待提供）
    └── application-prod.yml               # Prod环境数据源配置（待提供）

bootstrap/src/test/      # 集成测试
└── java/com/demo/bootstrap/repository/
    └── NodeRepositoryImplTest.java        # 仓储层集成测试
```

**结构决策**: 采用 DDD 多模块分层架构，Repository API 定义抽象接口和领域实体，MySQL 实现层封装所有持久化细节。这种分离确保业务层不依赖具体的持久化技术，支持未来切换到其他数据库或 ORM 框架。

## 复杂度跟踪

> 本节仅在宪法检查有需要说明的违规时填写

本功能完全符合项目宪法要求，无需复杂度说明。

