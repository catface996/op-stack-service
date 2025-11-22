# Tasks: MyBatis Plus 集成与节点管理仓储

**Input**: Design documents from `/specs/001-mybatis-plus-integration/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/NodeRepository.md

**Tests**: Integration tests are included in Phase 6 after all user stories are implemented.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

本项目采用多模块 Maven 工程，DDD 分层架构：
- **Repository API 层**: `infrastructure/repository/repository-api/`
- **MySQL 实现层**: `infrastructure/repository/mysql-impl/`
- **Common 模块**: `common/`
- **Bootstrap 模块**: `bootstrap/`

---

## Phase 1: Setup (项目初始化)

**Purpose**: 数据库准备和依赖配置

- [ ] T001 [P] 创建 Local 环境数据库 aiops_local (执行 DDL 脚本)
- [ ] T002 [P] 创建数据库表 t_node (执行 CREATE TABLE 语句，包含唯一索引 uk_name)
- [ ] T003 更新父 POM pom.xml 添加版本号属性 mybatis-plus.version=3.5.7 和 druid.version=1.2.20
- [ ] T004 更新父 POM pom.xml 在 dependencyManagement 中添加 mybatis-plus-spring-boot3-starter 和 druid-spring-boot-starter
- [ ] T005 更新 mysql-impl 模块 POM infrastructure/repository/mysql-impl/pom.xml 添加所有必需依赖
- [ ] T006 验证依赖配置 (执行 mvn clean compile 和 mvn dependency:tree)

---

## Phase 2: Foundational (基础设施 - 阻塞性前置条件)

**Purpose**: 核心配置和通用组件，必须在所有用户故事之前完成

**⚠️ CRITICAL**: 所有用户故事工作必须等待此阶段完成

- [ ] T007 [P] 创建 PageResult 通用分页结果类 common/src/main/java/com/demo/common/dto/PageResult.java
- [ ] T008 [P] 配置 MyBatis-Plus 全局配置 bootstrap/src/main/resources/application.yml (mapper-locations, type-aliases-package, logic-delete 配置)
- [ ] T009 配置 Local 环境数据源 bootstrap/src/main/resources/application-local.yml (Druid连接池: initial-size=2/min-idle=1/max-active=5; MyBatis-Plus: DEBUG日志+SQL格式化(format-sql: true); Druid监控: 启用StatViewServlet)
- [ ] T010 [P] 创建 MybatisPlusConfig 配置类 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/MybatisPlusConfig.java (配置分页插件、乐观锁插件、防全表更新删除插件，设置 Mapper 扫描路径)
- [ ] T011 [P] 创建 CustomMetaObjectHandler 元数据填充处理器 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/config/CustomMetaObjectHandler.java (自动填充 createTime, updateTime, deleted, version)
- [ ] T012 验证基础配置 (执行 mvn clean compile，确保无编译错误)

**Checkpoint**: 基础设施就绪 - 用户故事实现现在可以并行开始

---

## Phase 3: User Story 1 - 持久化和检索系统节点信息 (Priority: P1) 🎯 MVP

**Goal**: 实现节点的基本保存和查询功能（按 ID 查询、按名称查询），支持自动生成 ID、时间戳和默认值

**Independent Test**: 创建包含所有必填字段的节点记录，验证可以通过 ID 和 name 检索，所有自动生成字段正确填充

### Implementation for User Story 1

- [ ] T013 [P] [US1] 创建 NodeEntity 领域实体 infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/entity/NodeEntity.java (纯 POJO，无框架注解，包含 11 个字段)
- [ ] T014 [P] [US1] 创建 NodePO 持久化对象 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/po/NodePO.java (包含 MyBatis-Plus 注解：@TableName, @TableId, @TableField, @TableLogic, @Version)
- [ ] T015 [US1] 创建 NodeMapper 接口 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/mapper/NodeMapper.java (继承 BaseMapper<NodePO>，定义 selectByName 方法)
- [ ] T016 [US1] 创建 NodeMapper.xml SQL 映射文件 infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml (定义 BaseResultMap 和 selectByName 查询，包含 deleted=0 条件)
- [ ] T017 [US1] 创建 NodeRepository 仓储接口 infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java (定义 save, findById, findByName 方法签名)
- [ ] T018 [US1] 实现 NodeRepositoryImpl 仓储实现类 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java (实现 save, findById, findByName 方法，包含 Entity/PO 转换逻辑：toEntity 和 toPO 私有方法；实现输入参数验证：type枚举验证、name/operator空值检查、name长度验证≤100字符、description长度验证≤500字符、properties JSON格式验证)
- [ ] T019 [US1] 验证 User Story 1 功能 (执行 mvn clean compile，确保 save, findById, findByName 编译通过)

**Checkpoint**: User Story 1 完全功能化，可独立测试 - 节点可以保存、按 ID 查询、按名称查询

---

## Phase 4: User Story 2 - 按类型查询节点和分页 (Priority: P2)

**Goal**: 实现按类型查询节点列表，支持分页查询（可按名称和类型过滤），按创建时间降序排列

**Independent Test**: 创建多个不同类型的节点，验证可以按类型过滤并获得分页结果，分页元数据正确

### Implementation for User Story 2

- [ ] T020 [US2] 扩展 NodeMapper 接口 infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/mapper/NodeMapper.java (添加 selectByType 和 selectPageByCondition 方法签名)
- [ ] T021 [US2] 扩展 NodeMapper.xml infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml (添加 selectByType 查询，ORDER BY create_time DESC)
- [ ] T022 [US2] 扩展 NodeMapper.xml 添加分页查询 infrastructure/repository/mysql-impl/src/main/resources/mapper/NodeMapper.xml (添加 selectPageByCondition 动态 SQL，支持 name 模糊匹配和 type 精确匹配)
- [ ] T023 [US2] 扩展 NodeRepository 接口 infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java (添加 findByType 和 findPage 方法签名)
- [ ] T024 [US2] 扩展 NodeRepositoryImpl infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java (实现 findByType 和 findPage 方法，包含 PageResult 转换逻辑)
- [ ] T025 [US2] 验证 User Story 2 功能 (执行 mvn clean compile，确保 findByType 和 findPage 编译通过)

**Checkpoint**: User Stories 1 和 2 都应该独立工作 - 支持按类型查询和分页查询

---

## Phase 5: User Story 3 - 更新节点信息 (Priority: P3)

**Goal**: 实现节点更新功能，支持修改描述、属性、类型，自动更新 updateTime 和递增 version

**Independent Test**: 创建节点、修改其字段，验证更改是否持久化，包括更新的时间戳和版本号

### Implementation for User Story 3

- [ ] T026 [US3] 扩展 NodeRepository 接口 infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java (添加 update 方法签名)
- [ ] T027 [US3] 扩展 NodeRepositoryImpl infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java (实现 update 方法，使用 NodeMapper.updateById，设置 updateBy 和 operator)
- [ ] T028 [US3] 验证 User Story 3 功能 (执行 mvn clean compile，确保 update 方法编译通过)

**Checkpoint**: User Stories 1, 2, 3 都应该独立工作 - 支持节点更新和乐观锁

---

## Phase 6: User Story 4 - 逻辑删除节点 (Priority: P3)

**Goal**: 实现节点逻辑删除功能，设置 deleted=1 而不是物理删除，更新 updateTime 和 updateBy

**Independent Test**: 创建节点、删除它，验证它不再出现在查询中但在数据库中保留删除标记

### Implementation for User Story 4

- [ ] T029 [US4] 扩展 NodeRepository 接口 infrastructure/repository/repository-api/src/main/java/com/demo/infrastructure/repository/api/NodeRepository.java (添加 deleteById 方法签名)
- [ ] T030 [US4] 扩展 NodeRepositoryImpl infrastructure/repository/mysql-impl/src/main/java/com/demo/infrastructure/repository/mysql/impl/NodeRepositoryImpl.java (实现 deleteById 方法，使用 NodeMapper.deleteById，MyBatis-Plus 自动处理逻辑删除)
- [ ] T031 [US4] 验证 User Story 4 功能 (执行 mvn clean compile，确保 deleteById 方法编译通过)

**Checkpoint**: 所有用户故事现在都应该独立功能化 - 完整的 CRUD 操作

---

## Phase 7: Integration Tests (集成测试)

**Purpose**: 验证所有用户故事的功能正确性，包括边界情况和错误处理

- [ ] T032 [P] 创建集成测试类 bootstrap/src/test/java/com/demo/bootstrap/repository/NodeRepositoryImplTest.java (@SpringBootTest, @ActiveProfiles("local"), @Transactional)
- [ ] T033 [P] [US1] 实现 testSave 测试方法 (验证 ID 生成、时间戳、默认值 deleted=0, version=0)
- [ ] T034 [P] [US1] 实现 testFindById 测试方法 (验证按 ID 查询返回正确实体)
- [ ] T035 [P] [US1] 实现 testFindByName 测试方法 (验证按名称查询返回正确实体)
- [ ] T036 [P] [US2] 实现 testFindByType 测试方法 (验证按类型查询返回列表，按 createTime 降序)
- [ ] T037 [P] [US2] 实现 testFindPage 测试方法 (验证分页查询返回 PageResult，包含 total, pages, records)
- [ ] T038 [P] [US2] 实现 testFindPageWithFilters 测试方法 (验证名称模糊匹配和类型精确匹配)
- [ ] T039 [P] [US3] 实现 testUpdate 测试方法 (验证 updateTime 刷新，version 递增到 1)
- [ ] T040 [P] [US3] 实现 testOptimisticLock 测试方法 (验证并发更新抛出 OptimisticLockException)
- [ ] T041 [P] [US4] 实现 testDeleteById 测试方法 (验证 deleted=1，updateTime 和 updateBy 更新)
- [ ] T042 [P] [US4] 实现 testDeletedNodeNotFound 测试方法 (验证逻辑删除的节点不出现在查询中)
- [ ] T043 [P] 实现 testDuplicateName 测试方法 (验证唯一约束冲突抛出 DuplicateKeyException)
- [ ] T044 [P] 实现 testInvalidType 测试方法 (验证无效类型参数验证)
- [ ] T045 [P] 实现 testInvalidPageParams 测试方法 (验证分页参数验证，size > 100 抛出异常)
- [ ] T045A [P] 实现 testInvalidJsonFormat 测试方法 (验证保存包含格式错误JSON的properties字段时抛出IllegalArgumentException，以及正确JSON格式可以正常保存)
- [ ] T045B [P] 实现 testUpdateDeletedNode 测试方法 (验证尝试更新已逻辑删除的节点时抛出NotFoundException或按业务规则处理)
- [ ] T046 运行所有集成测试 (执行 mvn test -Dtest=NodeRepositoryImplTest，确保所有测试通过)

---

## Phase 8: Multi-Environment Configuration (多环境配置)

**Purpose**: 配置其他环境的数据源和连接池，支持 dev/test/staging/prod 环境部署

- [ ] T047 [P] 创建 Dev 环境配置 bootstrap/src/main/resources/application-dev.yml (Druid: initial-size=5/min-idle=3/max-active=10; MyBatis-Plus: DEBUG日志+SQL格式化(format-sql: true); Druid监控: 启用StatViewServlet; 其他配置占位符等待实际凭据)
- [ ] T048 [P] 创建 Test 环境配置 bootstrap/src/main/resources/application-test.yml (Druid: initial-size=5/min-idle=3/max-active=10; MyBatis-Plus: WARN日志+无SQL格式化(format-sql: false); Druid监控: 启用StatViewServlet; 其他配置占位符等待实际凭据)
- [ ] T049 [P] 创建 Staging 环境配置 bootstrap/src/main/resources/application-staging.yml (Druid: initial-size=10/min-idle=5/max-active=20; MyBatis-Plus: WARN日志+无SQL格式化(format-sql: false); Druid监控: 启用StatViewServlet; 其他配置占位符等待实际凭据)
- [ ] T050 [P] 创建 Prod 环境配置 bootstrap/src/main/resources/application-prod.yml (Druid: initial-size=20/min-idle=10/max-active=50; MyBatis-Plus: WARN日志+无SQL格式化(format-sql: false); Druid监控: 启用StatViewServlet; 其他配置占位符等待实际凭据)

---

## Phase 9: Polish & Cross-Cutting Concerns (完善与横切关注点)

**Purpose**: 改进影响多个用户故事的功能

- [ ] T051 [P] 添加 Java 注释文档 (所有 public 方法添加 Javadoc 注释，中文描述)
- [ ] T052 [P] 代码格式化和清理 (执行代码格式化工具，移除未使用的导入)
- [ ] T053 验证 quickstart.md 文档 (按照 quickstart.md 步骤执行，确保所有步骤可执行)
- [ ] T054 性能验证 (验证性能目标：save < 100ms, findById < 50ms, findPage < 200ms)
- [ ] T055 安全性验证 (确认防全表更新删除插件生效，参数化查询防止 SQL 注入)
- [ ] T056 最终编译和打包 (执行 mvn clean package，确保构建成功)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖 - 可立即开始
- **Foundational (Phase 2)**: 依赖 Setup 完成 - 阻塞所有用户故事
- **User Stories (Phase 3-6)**: 所有依赖 Foundational 阶段完成
  - 用户故事可以并行执行（如果有人力）
  - 或按优先级顺序执行 (P1 → P2 → P3 → P3)
- **Integration Tests (Phase 7)**: 依赖所有用户故事完成
- **Multi-Environment (Phase 8)**: 可以与 Phase 3-7 并行，无依赖
- **Polish (Phase 9)**: 依赖所有期望的用户故事完成

### User Story Dependencies

- **User Story 1 (P1)**: 可在 Foundational (Phase 2) 完成后开始 - 无其他故事依赖
- **User Story 2 (P2)**: 可在 Foundational (Phase 2) 完成后开始 - 扩展 US1 但独立可测试
- **User Story 3 (P3)**: 可在 Foundational (Phase 2) 完成后开始 - 扩展 US1 但独立可测试
- **User Story 4 (P3)**: 可在 Foundational (Phase 2) 完成后开始 - 扩展 US1 但独立可测试

### Within Each User Story

- Entity 和 PO 优先创建（可并行）
- Mapper 接口和 XML 依赖 PO
- Repository 接口和实现依赖 Mapper
- 验证依赖实现完成

### Parallel Opportunities

- Phase 1 所有标记 [P] 的任务可并行执行
- Phase 2 所有标记 [P] 的任务可并行执行
- Foundational 阶段完成后，所有用户故事可以并行开始（如果团队容量允许）
- Phase 7 所有标记 [P] 的测试方法可并行编写
- Phase 8 所有配置文件可并行创建
- Phase 9 所有标记 [P] 的任务可并行执行

---

## Parallel Example: User Story 1

```bash
# 并行启动 User Story 1 的所有模型创建任务:
Task T013: "创建 NodeEntity 领域实体"
Task T014: "创建 NodePO 持久化对象"

# 然后顺序执行:
Task T015: "创建 NodeMapper 接口" (依赖 T014)
Task T016: "创建 NodeMapper.xml" (依赖 T015)
Task T017: "创建 NodeRepository 接口" (依赖 T013)
Task T018: "实现 NodeRepositoryImpl" (依赖 T015, T016, T017)
```

---

## Implementation Strategy

### MVP First (仅 User Story 1)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational (CRITICAL - 阻塞所有故事)
3. 完成 Phase 3: User Story 1
4. **STOP and VALIDATE**: 独立测试 User Story 1
5. 如果就绪，部署/演示

### Incremental Delivery

1. 完成 Setup + Foundational → 基础就绪
2. 添加 User Story 1 → 独立测试 → 部署/演示 (MVP!)
3. 添加 User Story 2 → 独立测试 → 部署/演示
4. 添加 User Story 3 → 独立测试 → 部署/演示
5. 添加 User Story 4 → 独立测试 → 部署/演示
6. 每个故事在不破坏以前故事的情况下增加价值

### Parallel Team Strategy

多开发者并行:

1. 团队共同完成 Setup + Foundational
2. Foundational 完成后:
   - Developer A: User Story 1 (T013-T019)
   - Developer B: User Story 2 (T020-T025)
   - Developer C: User Story 3 + 4 (T026-T031)
3. 故事独立完成并集成

---

## Notes

- [P] 任务 = 不同文件，无依赖关系，可并行
- [Story] 标签将任务映射到特定用户故事，便于追溯
- 每个用户故事应该独立可完成和可测试
- 每完成一个任务或逻辑组提交代码
- 在任何检查点停止以独立验证故事
- Entity/PO 分离：Entity 无框架注解，PO 包含 MyBatis-Plus 注解
- 所有条件查询必须在 XML 中定义，不使用 QueryWrapper
- 每次编译验证：mvn clean compile
- 避免：模糊任务、相同文件冲突、破坏独立性的跨故事依赖

---

## Task Count Summary

- **Phase 1 (Setup)**: 6 tasks
- **Phase 2 (Foundational)**: 6 tasks (阻塞性前置条件)
- **Phase 3 (User Story 1 - P1)**: 7 tasks 🎯 MVP
- **Phase 4 (User Story 2 - P2)**: 6 tasks
- **Phase 5 (User Story 3 - P3)**: 3 tasks
- **Phase 6 (User Story 4 - P3)**: 3 tasks
- **Phase 7 (Integration Tests)**: 17 tasks (新增T045A JSON验证测试, T045B更新已删除节点测试)
- **Phase 8 (Multi-Environment)**: 4 tasks
- **Phase 9 (Polish)**: 6 tasks

**Total**: 58 tasks (修复后新增2个测试任务)

**Parallel Opportunities**: 27 tasks 标记为 [P] 可并行执行 (新增2个并行测试任务)

**MVP Scope** (最小可行产品): Phase 1 + Phase 2 + Phase 3 = 19 tasks

**Independent Test Criteria**:
- US1: 可以保存节点，按 ID 和名称查询，验证自动生成字段
- US2: 可以按类型查询和分页查询，验证排序和过滤
- US3: 可以更新节点，验证时间戳和版本号
- US4: 可以逻辑删除节点，验证查询排除已删除节点
