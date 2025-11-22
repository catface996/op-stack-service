# 快速开始：MyBatis Plus 集成与节点管理仓储

**功能**: MyBatis Plus 集成与节点管理仓储
**日期**: 2025-11-22
**预计完成时间**: 约 4-6 小时（不包括等待数据库凭据的时间）

## 概述

本文档提供 MyBatis-Plus 集成和 NodeEntity 持久化功能的快速开始指南，包括环境准备、依赖配置、代码实现和测试验证的完整流程。

## 前置条件

### 必需条件

1. **JDK 21** 已安装并配置
2. **Maven 3.6+** 已安装
3. **MySQL 8.x** Docker 容器已运行（local 环境）
4. **项目基础架构** 已搭建（DDD 多模块结构）
5. **Git 分支** `001-mybatis-plus-integration` 已创建并切换

### 验证环境

```bash
# 验证 JDK 版本
java -version
# 应显示：java version "21"

# 验证 Maven 版本
mvn -version
# 应显示：Apache Maven 3.6.x 或更高

# 验证 MySQL Docker 容器
docker ps | grep mysql
# 应显示正在运行的 MySQL 容器

# 验证当前分支
git branch --show-current
# 应显示：001-mybatis-plus-integration
```

## 实施步骤

### 阶段 0: 准备工作（5 分钟）

#### 1. 创建 Local 环境数据库

```bash
# 连接到 MySQL Docker 容器
docker exec -it <mysql-container-name> mysql -uroot -proot123

# 创建数据库
CREATE DATABASE IF NOT EXISTS aiops_local
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

# 验证数据库创建
SHOW DATABASES LIKE 'aiops_local';

# 退出 MySQL
EXIT;
```

#### 2. 创建数据库表

```sql
USE aiops_local;

CREATE TABLE `t_node` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `type` VARCHAR(20) NOT NULL COMMENT '节点类型（DATABASE/APPLICATION/API/REPORT/OTHER）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '节点描述',
  `properties` TEXT DEFAULT NULL COMMENT '节点属性（JSON格式）',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `create_by` VARCHAR(50) NOT NULL COMMENT '创建人',
  `update_by` VARCHAR(50) NOT NULL COMMENT '更新人',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统节点表';

-- 验证表创建
DESC t_node;
```

### 阶段 1: 依赖配置（10 分钟）

#### 1. 更新父 POM

文件：`pom.xml`

在 `<properties>` 中添加版本号：

```xml
<properties>
    <!-- 现有属性... -->
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <druid.version>1.2.20</druid.version>
</properties>
```

在 `<dependencyManagement>` 中添加依赖管理：

```xml
<dependencyManagement>
    <dependencies>
        <!-- 现有依赖... -->

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Druid 数据库连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 2. 更新 mysql-impl 模块 POM

文件：`infrastructure/repository/mysql-impl/pom.xml`

```xml
<dependencies>
    <!-- Repository API -->
    <dependency>
        <groupId>com.demo</groupId>
        <artifactId>repository-api</artifactId>
    </dependency>

    <!-- Common -->
    <dependency>
        <groupId>com.demo</groupId>
        <artifactId>common</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Druid -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>

    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### 3. 验证依赖配置

```bash
# 编译项目
mvn clean compile

# 检查依赖树（确认 MyBatis-Plus 版本）
mvn dependency:tree | grep mybatis-plus
# 应显示：com.baomidou:mybatis-plus-spring-boot3-starter:jar:3.5.7

# 检查 Druid 版本
mvn dependency:tree | grep druid
# 应显示：com.alibaba:druid-spring-boot-starter:jar:1.2.20
```

### 阶段 2: 配置文件（10 分钟）

#### 1. 更新 application.yml

文件：`bootstrap/src/main/resources/application.yml`

```yaml
# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.catface996.aiops.repository.mysql.po
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 2. 更新 application-local.yml

文件：`bootstrap/src/main/resources/application-local.yml`

```yaml
spring:
  # 移除数据源自动配置排除
  autoconfigure:
    exclude: []  # 清空排除列表

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/aiops_local?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=UTC&useSSL=false
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource

    # Druid 连接池配置
    druid:
      initial-size: 2
      min-idle: 1
      max-active: 5
      max-wait: 60000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
```

### 阶段 3: 实现代码（90 分钟）

#### 任务清单

按以下顺序实现代码，每完成一个任务后执行 `mvn clean compile` 验证：

1. **Common 模块** (10 分钟)
   - [ ] 创建 PageResult 通用分页结果类

2. **Repository API 模块** (15 分钟)
   - [ ] 创建 NodeEntity 领域实体
   - [ ] 创建 NodeRepository 仓储接口

3. **MySQL 实现模块 - 配置** (20 分钟)
   - [ ] 创建 MybatisPlusConfig 配置类
   - [ ] 创建 CustomMetaObjectHandler 元数据填充处理器

4. **MySQL 实现模块 - 数据访问** (45 分钟)
   - [ ] 创建 NodePO 持久化对象
   - [ ] 创建 NodeMapper 接口
   - [ ] 创建 NodeMapper.xml SQL 映射文件
   - [ ] 创建 NodeRepositoryImpl 仓储实现类

#### 详细实现指南

参考以下文档获取详细的代码实现：

- **数据模型**: [data-model.md](./data-model.md)
- **接口契约**: [contracts/NodeRepository.md](./contracts/NodeRepository.md)
- **研究文档**: [research.md](./research.md)

### 阶段 4: 测试验证（60 分钟）

#### 1. 创建集成测试

文件：`bootstrap/src/test/java/com/demo/bootstrap/repository/NodeRepositoryImplTest.java`

```java
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class NodeRepositoryImplTest {

    @Autowired
    private NodeRepository nodeRepository;

    @Test
    public void testSave() {
        // 创建节点
        NodeEntity node = new NodeEntity();
        node.setName("MySQL-Primary-Test");
        node.setType("DATABASE");
        node.setDescription("测试数据库");
        node.setProperties("{\"host\":\"localhost\",\"port\":3306}");

        // 保存节点
        NodeEntity saved = nodeRepository.save(node, "test-user");

        // 验证
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());
        assertEquals("test-user", saved.getCreateBy());
        assertEquals("test-user", saved.getUpdateBy());
        assertEquals(0, saved.getDeleted());
        assertEquals(0, saved.getVersion());
    }

    // 更多测试方法...
}
```

#### 2. 运行测试

```bash
# 运行所有测试
mvn test

# 只运行 NodeRepositoryImplTest
mvn test -Dtest=NodeRepositoryImplTest

# 查看测试报告
cat target/surefire-reports/com.demo.bootstrap.repository.NodeRepositoryImplTest.txt
```

#### 3. 测试覆盖清单

- [ ] 保存节点（ID 生成、时间戳、默认值）
- [ ] 根据 ID 查询节点
- [ ] 根据名称查询节点
- [ ] 根据类型查询节点列表
- [ ] 分页查询节点
- [ ] 更新节点（时间戳、版本号递增）
- [ ] 逻辑删除节点
- [ ] 唯一约束冲突（名称重复）
- [ ] 乐观锁并发更新冲突
- [ ] JSON 格式验证

### 阶段 5: 手动验证（可选，15 分钟）

#### 启动应用

```bash
# 启动 Spring Boot 应用
mvn spring-boot:run -pl bootstrap

# 或使用 IDE 运行 BootstrapApplication
```

#### 验证应用启动

检查日志中的关键信息：

```
✅ MyBatis-Plus 初始化成功
✅ Mapper 扫描成功（com.catface996.aiops.repository.mysql.mapper）
✅ 数据源连接成功（jdbc:mysql://localhost:3306/aiops_local）
✅ Druid 连接池初始化成功（初始2/最小1/最大5）
```

#### 使用 MySQL 客户端验证

```sql
-- 查询节点表（应该能看到测试数据，如果测试没有回滚）
SELECT * FROM t_node;

-- 验证逻辑删除
SELECT * FROM t_node WHERE deleted = 0;  -- 只看活动节点
SELECT * FROM t_node WHERE deleted = 1;  -- 只看已删除节点

-- 验证唯一索引
SHOW INDEX FROM t_node;
-- 应看到 uk_name 唯一索引
```

## 常见问题

### 问题 1: 编译失败 - 找不到 MyBatis-Plus 类

**症状**: 编译时报错 `cannot find symbol: class BaseMapper`

**原因**: 依赖配置错误或使用了错误的启动器

**解决**:
1. 确认父 POM 中版本号定义正确
2. 确认使用 `mybatis-plus-spring-boot3-starter`（不是 `mybatis-plus-boot-starter`）
3. 运行 `mvn clean compile -U` 强制更新依赖

### 问题 2: 应用启动失败 - 数据源配置错误

**症状**: 启动时报错 `Failed to configure a DataSource`

**原因**: application-local.yml 配置错误或数据库未启动

**解决**:
1. 验证 MySQL Docker 容器正在运行
2. 验证数据库名称、用户名、密码正确
3. 验证 `spring.autoconfigure.exclude` 已清空

### 问题 3: Mapper 扫描失败

**症状**: 启动时报错 `Invalid bound statement (not found)`

**原因**: Mapper 扫描路径配置错误或 XML namespace 不匹配

**解决**:
1. 验证 `MybatisPlusConfig` 中 `@MapperScan` 路径正确
2. 验证 `NodeMapper.xml` 的 namespace 与 `NodeMapper` 接口全限定名一致
3. 验证 XML 文件在 `src/main/resources/mapper/` 目录下

### 问题 4: 测试失败 - 事务未回滚

**症状**: 测试后数据库中有残留数据

**原因**: 测试类缺少 `@Transactional` 注解

**解决**:
1. 在测试类上添加 `@Transactional` 注解
2. 确认测试方法没有手动提交事务

### 问题 5: 乐观锁不生效

**症状**: 并发更新没有抛出 `OptimisticLockException`

**原因**: 乐观锁插件未正确配置

**解决**:
1. 验证 `MybatisPlusConfig` 中注册了 `OptimisticLockerInnerInterceptor`
2. 验证 `NodePO` 的 version 字段有 `@Version` 注解
3. 验证更新时传入了正确的 version 值

## 验收标准

完成以下所有检查项，功能才算完成：

### 编译验证 ✅
- [ ] `mvn clean compile` 成功
- [ ] 无编译错误或警告

### 配置验证 ✅
- [ ] 父 POM 依赖管理正确
- [ ] mysql-impl 模块依赖声明正确
- [ ] application.yml MyBatis-Plus 配置正确
- [ ] application-local.yml 数据源配置正确

### 功能验证 ✅
- [ ] 可以成功创建节点（ID 自动生成、时间戳自动填充）
- [ ] 可以根据 ID 查询节点
- [ ] 可以根据名称查询节点
- [ ] 可以根据类型查询节点列表
- [ ] 可以分页查询节点（支持过滤）
- [ ] 可以更新节点（updateTime 和 version 自动更新）
- [ ] 可以逻辑删除节点（deleted = 1）
- [ ] 唯一约束有效（名称重复抛出异常）
- [ ] 乐观锁有效（并发更新抛出异常）

### 测试验证 ✅
- [ ] 所有单元测试通过
- [ ] 测试覆盖率 >= 80%
- [ ] 测试后数据自动回滚

### 代码质量验证 ✅
- [ ] Entity 为纯 POJO（无框架注解）
- [ ] PO 包含 MyBatis-Plus 注解
- [ ] 所有条件查询在 XML 中定义
- [ ] 代码有适当的注释
- [ ] 遵循项目命名规范

## 下一步

功能完成并验收通过后，继续：

1. **生成任务列表**: 运行 `/speckit.tasks` 命令生成详细的任务分解
2. **实施任务**: 按照 tasks.md 中的任务顺序逐个实现
3. **代码审查**: 提交 Pull Request 进行代码审查
4. **部署准备**: 准备其他环境的数据库凭据和配置

## 参考资料

- **功能规格说明**: [spec.md](./spec.md)
- **实施计划**: [plan.md](./plan.md)
- **技术研究**: [research.md](./research.md)
- **数据模型**: [data-model.md](./data-model.md)
- **接口契约**: [contracts/NodeRepository.md](./contracts/NodeRepository.md)
- **MyBatis-Plus 官方文档**: https://baomidou.com/
- **Spring Boot 官方文档**: https://spring.io/projects/spring-boot
