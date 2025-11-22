# 技术研究：MyBatis Plus 集成与节点管理仓储

**功能**: MyBatis Plus 集成与节点管理仓储
**日期**: 2025-11-22
**状态**: 已完成

## 研究目标

1. 确认 MyBatis-Plus 3.5.7 与 Spring Boot 3.4.1 的兼容性
2. 研究 Entity/PO 分离的最佳实践
3. 确定多环境数据源配置方案
4. 研究 MyBatis-Plus 插件配置（分页、乐观锁、防全表更新）
5. 确定连接池（Druid）配置策略

## 研究发现

### 1. MyBatis-Plus 与 Spring Boot 3 兼容性

**决策**: 使用 MyBatis-Plus 3.5.7 + mybatis-plus-spring-boot3-starter

**理由**:
- Spring Boot 3.x 基于 Jakarta EE 9+，与 Spring Boot 2.x 的 javax.* 包不兼容
- MyBatis-Plus 3.5.7 专门为 Spring Boot 3 提供了 `mybatis-plus-spring-boot3-starter`
- 该版本完全支持 JDK 21 和 Spring Boot 3.4.1

**替代方案考虑**:
- ❌ `mybatis-plus-boot-starter`: 仅支持 Spring Boot 2.x，不兼容 Spring Boot 3
- ❌ MyBatis 原生: 需要手动配置，功能不如 MyBatis-Plus 丰富
- ❌ JPA/Hibernate: 学习曲线较陡，且 MyBatis 在国内企业更流行

**参考**:
- MyBatis-Plus 官方文档: https://baomidou.com/
- Spring Boot 3 迁移指南

### 2. Entity/PO 分离架构

**决策**: 严格分离 Entity（领域实体）和 PO（持久化对象）

**架构设计**:

```
Repository API 层 (repository-api)
├── entity/NodeEntity.java     # 纯 POJO，无框架注解
└── api/NodeRepository.java    # 仓储接口，返回 Entity

MySQL 实现层 (mysql-impl)
├── po/NodePO.java             # 包含 MyBatis-Plus 注解
├── mapper/NodeMapper.java     # 继承 BaseMapper<NodePO>
└── impl/NodeRepositoryImpl.java   # Entity ↔ PO 转换
```

**理由**:
1. **框架无关**: Entity 不依赖 MyBatis-Plus，易于单元测试
2. **可替换性**: 可轻松切换到 JPA、MongoDB 等其他持久化方案
3. **职责单一**: Entity 表达业务概念，PO 负责数据库映射
4. **符合 DDD**: 领域层不应依赖基础设施层的技术细节

**替代方案考虑**:
- ❌ Entity 和 PO 合并: 违反 DDD 原则，领域层被持久化框架污染
- ❌ 使用 DTO 代替 Entity: 增加不必要的转换层，Entity 本身已是领域模型

**实施要点**:
- Entity 和 PO 字段保持一致，简化转换逻辑
- RepositoryImpl 内部实现 `toEntity(NodePO)` 和 `toPO(NodeEntity)` 私有方法
- 对外只暴露 Entity，隐藏 PO 细节

### 3. 多环境数据源配置

**决策**: 使用 Spring Profile 机制 + application-{profile}.yml 分环境配置

**配置方案**:

| 环境 | 配置文件 | 连接池配置（初始/最小/最大） |
|------|---------|---------------------------|
| local | application-local.yml | 2 / 1 / 5 |
| dev | application-dev.yml | 5 / 3 / 10 |
| test | application-test.yml | 5 / 3 / 10 |
| staging | application-staging.yml | 10 / 5 / 20 |
| prod | application-prod.yml | 20 / 10 / 50 |

**理由**:
1. **资源优化**: Local 环境小连接池降低开发机资源占用
2. **性能保证**: Prod 环境大连接池支持高并发访问
3. **环境隔离**: 每个环境独立配置，互不影响
4. **渐进式扩展**: 从 dev 到 staging 再到 prod 逐步增加资源

**替代方案考虑**:
- ❌ 所有环境统一配置: 浪费开发环境资源，生产环境可能不足
- ❌ 动态连接池: 增加复杂度，收益不明显
- ❌ 配置中心管理: 对于初期项目过度设计

**Local 环境具体配置**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiops_local?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=UTC
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 2
      min-idle: 1
      max-active: 5
      max-wait: 60000
```

### 4. MyBatis-Plus 插件配置

**决策**: 配置分页插件、乐观锁插件、防全表更新删除插件

**插件配置顺序**（重要）:
1. **PaginationInnerInterceptor** (分页插件) - 必须放在第一位
2. **OptimisticLockerInnerInterceptor** (乐观锁插件)
3. **BlockAttackInnerInterceptor** (防全表更新删除插件)

**配置要点**:

**分页插件**:
```java
PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
paginationInterceptor.setMaxLimit(100L); // 每页最大100条
paginationInterceptor.setOverflow(false); // 不处理溢出
```

**理由**:
- 限制每页最大100条防止大查询影响性能
- 使用 MySQL 数据库类型优化 SQL 生成

**乐观锁插件**:
```java
OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
```

**理由**:
- NodeEntity 的 version 字段需要乐观锁支持
- 防止并发更新时数据覆盖

**防全表更新删除插件**:
```java
BlockAttackInnerInterceptor blockAttackInterceptor = new BlockAttackInnerInterceptor();
```

**理由**:
- 防止误操作导致全表更新或删除
- 生产环境安全保护

**替代方案考虑**:
- ❌ 不使用插件: 需要手动实现分页、乐观锁，容易出错
- ❌ 只用分页插件: 缺少并发控制和安全保护

### 5. 数据操作规范

**决策**: 简单操作使用 MyBatis-Plus API，条件查询使用 Mapper XML

**规范定义**:

**允许使用 API**:
- ✅ `save()`, `saveBatch()`, `saveOrUpdate()` - 插入操作
- ✅ `updateById()`, `updateBatchById()` - 根据主键更新
- ✅ `getById()`, `listByIds()` - 根据主键查询

**必须使用 XML**:
- ❌ 所有条件查询（不使用 QueryWrapper）
- ❌ 所有条件更新（不使用 UpdateWrapper）
- ❌ 所有复杂查询（多表关联、子查询、聚合）

**理由**:
1. **统一管理**: 所有 SQL 集中在 XML，便于查找和维护
2. **代码审查**: DBA 可快速审查所有 SQL，发现性能问题
3. **性能分析**: 便于使用工具分析 SQL 性能，添加索引
4. **可维护性**: SQL 语句清晰可见，避免 Wrapper 难以追踪

**示例**:

```xml
<!-- NodeMapper.xml -->
<select id="selectByName" resultMap="BaseResultMap">
    SELECT * FROM t_node
    WHERE name = #{name}
      AND deleted = 0
</select>

<select id="selectByType" resultMap="BaseResultMap">
    SELECT * FROM t_node
    WHERE type = #{type}
      AND deleted = 0
    ORDER BY create_time DESC
</select>

<select id="selectPageByCondition" resultMap="BaseResultMap">
    SELECT * FROM t_node
    WHERE deleted = 0
    <if test="name != null and name != ''">
        AND name LIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="type != null and type != ''">
        AND type = #{type}
    </if>
    ORDER BY create_time DESC
</select>
```

**替代方案考虑**:
- ❌ 全部使用 Wrapper: SQL 分散在代码中，难以管理和优化
- ❌ 全部使用 XML: 简单操作也要写 XML，增加维护成本

### 6. 元数据自动填充策略

**决策**: 使用 CustomMetaObjectHandler 自动填充 createTime、updateTime、deleted、version

**实现方案**:

```java
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

**不自动填充的字段**:
- `createBy` 和 `updateBy`: 通过方法参数传递，由调用方提供

**理由**:
1. **一致性**: 确保时间戳总是由系统生成，避免客户端时间不准
2. **简化代码**: 不需要在每次保存/更新时手动设置时间戳
3. **防止遗漏**: 自动填充确保字段不会被遗忘

**替代方案考虑**:
- ❌ 手动填充: 容易遗漏，代码冗余
- ❌ 数据库默认值: 不支持 update 时自动更新 updateTime
- ❌ AOP 拦截: 过度设计，MyBatis-Plus 已提供现成机制

### 7. 数据库索引策略

**决策**: 初期只在 name 字段创建唯一索引

**索引设计**:

```sql
-- name 字段唯一索引
CREATE UNIQUE INDEX uk_name ON t_node(name);
```

**理由**:
1. **唯一性保证**: name 字段有唯一约束要求
2. **查询优化**: 根据 name 查询是常见操作，索引提升性能
3. **简化维护**: 初期避免过多索引，根据实际查询需求后续添加

**未创建的索引**（按需添加）:
- type 字段: 如果按类型查询频繁，可添加
- deleted 字段: 如果查询未删除记录很慢，可添加
- createTime 字段: 如果按时间排序很慢，可添加
- type + deleted 组合索引: 如果同时按类型和删除标记查询频繁，可添加

**替代方案考虑**:
- ❌ 所有查询字段都建索引: 写入性能下降，维护成本高
- ❌ 不建索引: name 查询慢，且无法保证唯一性

### 8. 测试策略

**决策**: 使用 Spring Boot Test + @Transactional 确保测试隔离

**测试配置**:
```java
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class NodeRepositoryImplTest {
    @Autowired
    private NodeRepository nodeRepository;

    // 测试方法...
}
```

**理由**:
1. **真实环境**: 使用实际 MySQL 数据库，而非 H2 内存数据库
2. **自动回滚**: @Transactional 确保测试后数据自动回滚
3. **环境隔离**: 使用 local 环境配置，不影响其他环境

**测试覆盖**:
- 保存节点（ID 生成、时间戳、默认值）
- 查询节点（ID、name、type、分页）
- 更新节点（时间戳、版本号）
- 逻辑删除（deleted 标记）
- 唯一约束冲突
- 乐观锁并发更新
- JSON 格式验证

**替代方案考虑**:
- ❌ H2 内存数据库: 与 MySQL 行为可能不一致
- ❌ 不自动回滚: 测试数据污染数据库
- ❌ Mock 测试: 无法验证真实的数据库交互

## 风险与缓解

### 风险 1: 多环境数据库凭据管理

**风险**: dev/test/staging/prod 环境的数据库凭据尚未提供

**缓解措施**:
- 先完成 local 环境配置和测试
- 预留占位符配置文件，等待凭据提供
- 文档说明部署时需要配置的环境变量

### 风险 2: 数据库表未创建

**风险**: 应用启动前数据库表可能未创建

**缓解措施**:
- 提供 DDL 脚本供 DBA 执行
- 在文档中明确说明表创建是启动前提条件
- 考虑未来使用 Flyway 或 Liquibase 进行数据库版本管理

### 风险 3: 并发更新冲突处理

**风险**: 乐观锁冲突时的错误处理策略

**缓解措施**:
- 配置乐观锁插件
- 在 Repository 实现中捕获乐观锁异常
- 返回友好的错误信息给调用方
- 考虑重试机制（由调用方决定）

## 总结

本研究确认了 MyBatis-Plus 3.5.7 与 Spring Boot 3.4.1 的完全兼容性，并制定了符合 DDD 原则的 Entity/PO 分离架构。多环境数据源配置方案根据实际负载差异化连接池大小，既优化了开发环境资源使用，又保证了生产环境的高并发支持。数据操作规范明确了简单操作使用 API、复杂查询使用 XML 的策略，确保 SQL 可管理、可审查、可优化。所有技术决策均已充分考虑替代方案，选择了最适合当前项目阶段的方案。

**下一步**: 进入 Phase 1 设计阶段，生成数据模型和接口契约。
