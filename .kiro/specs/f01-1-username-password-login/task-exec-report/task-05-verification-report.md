# 任务 5 验证报告：创建数据库表结构

**任务编号**: 5  
**任务名称**: 创建数据库表结构  
**执行日期**: 2025-11-24  
**执行人**: AI Assistant  
**验证状态**: ✅ 通过

---

## 1. 任务目标

创建用户认证所需的数据库表结构，包括：
- account 表（用户账号表）
- session 表（会话表，作为 Redis 降级方案）
- 使用 Flyway 进行数据库版本控制和迁移

---

## 2. 实施内容

### 2.1 添加 Flyway 依赖

**文件**: bootstrap/pom.xml

添加了 flyway-core 和 flyway-mysql 依赖。

### 2.2 配置 Flyway

**文件**: bootstrap/src/main/resources/application.yml

配置说明:
- enabled: true - 启用 Flyway
- baseline-on-migrate: true - 对已存在的数据库进行 baseline
- baseline-version: 0 - 从版本 0 开始 baseline，确保所有迁移脚本都执行
- locations - 迁移脚本位置
- validate-on-migrate: true - 迁移前验证脚本完整性

### 2.3 创建数据库迁移脚本

#### V1__Create_account_table.sql

**文件**: bootstrap/src/main/resources/db/migration/V1__Create_account_table.sql

创建 t_account 表，包含以下字段：
- id (BIGINT, 自增主键)
- username (VARCHAR(20), 唯一索引)
- email (VARCHAR(100), 唯一索引)
- password (VARCHAR(60))
- role (VARCHAR(20), 默认 ROLE_USER)
- status (VARCHAR(20), 默认 ACTIVE, 普通索引)
- created_at (DATETIME)
- updated_at (DATETIME)

#### V2__Create_session_table.sql

**文件**: bootstrap/src/main/resources/db/migration/V2__Create_session_table.sql

创建 t_session 表，包含以下字段：
- id (VARCHAR(36), 主键)
- user_id (BIGINT, 外键关联 t_account.id, 级联删除)
- token (TEXT)
- expires_at (DATETIME, 索引)
- device_info (TEXT)
- created_at (DATETIME)

### 2.4 命名规范修正

遵循项目现有规范，所有表名使用 t_ 前缀：
- account → t_account
- session → t_session
- 与现有的 t_node 表保持一致

---

## 3. 验证结果

### 3.1 构建验证

执行命令: mvn clean compile -DskipTests

结果: ✅ BUILD SUCCESS

### 3.2 运行时验证 - 应用启动成功

执行命令: mvn spring-boot:run -pl bootstrap

Flyway 执行日志显示：
- Successfully baselined schema with version: 0
- Migrating schema to version 1 - Create account table
- Migrating schema to version 2 - Create session table
- Successfully applied 2 migrations to schema aiops_local, now at version v2
- Started Application in 2.252 seconds

结果: ✅ 应用成功启动，Flyway 迁移执行成功

### 3.3 运行时验证 - 数据库表创建成功

查询命令: SHOW TABLES;

结果显示以下表：
- flyway_schema_history
- t_account
- t_node
- t_session

✅ t_account 和 t_session 表创建成功

### 3.4 运行时验证 - t_account 表结构

查询命令: DESCRIBE t_account;

验证结果：
- 所有字段类型、默认值、约束符合设计
- 主键索引 (id)
- 唯一索引 (username, email)
- 普通索引 (status)

✅ 表结构完全符合设计

### 3.5 运行时验证 - t_session 表结构

查询命令: DESCRIBE t_session; 和 SHOW CREATE TABLE t_session;

验证结果：
- 所有字段类型、约束符合设计
- 主键索引 (id)
- 普通索引 (user_id, expires_at)
- 外键约束 (user_id → t_account.id, CASCADE DELETE)

✅ 表结构和外键约束完全符合设计

### 3.6 运行时验证 - 唯一约束测试

**测试用例 1**: 插入正常数据
```sql
INSERT INTO t_account (username, email, password) 
VALUES ('test_user', 'test@example.com', 'hashed_password');
```
结果: ✅ 插入成功

**测试用例 2**: 插入重复邮箱
```sql
INSERT INTO t_account (username, email, password) 
VALUES ('test_user2', 'test@example.com', 'hashed_password');
```
结果: ✅ 正确抛出错误
```
ERROR 1062 (23000): Duplicate entry 'test@example.com' for key 't_account.uk_email'
```

**测试用例 3**: 插入重复用户名
```sql
INSERT INTO t_account (username, email, password) 
VALUES ('test_user', 'test2@example.com', 'hashed_password');
```
结果: ✅ 正确抛出错误
```
ERROR 1062 (23000): Duplicate entry 'test_user' for key 't_account.uk_username'
```

### 3.7 运行时验证 - Flyway 历史记录

查询命令: SELECT * FROM flyway_schema_history;

结果显示 3 条记录：
- version 0: Flyway Baseline
- version 1: Create account table (V1__Create_account_table.sql)
- version 2: Create session table (V2__Create_session_table.sql)

✅ Flyway 正确记录了所有迁移历史

---

## 4. 需求符合性检查

### 4.1 需求追溯

**相关需求**:
- REQ-FR-003: 账号注册
- REQ-FR-007: 会话管理

### 4.2 设计符合性

**设计文档**: .kiro/specs/username-password-login/design.md

**差异说明**:
1. ✅ 表名: 添加 t_ 前缀，符合项目命名规范
2. ✅ 索引优化: username 和 email 使用 UNIQUE KEY 而非普通 INDEX，更符合业务需求
3. ✅ 额外索引: 添加 status 索引，优化账号状态查询性能

**结论**: ✅ 实现符合设计意图，并进行了合理优化

---

## 5. 技术方案说明

### 5.1 为什么选择 Flyway？

**Flyway** 是一个数据库版本控制和迁移工具，类似于代码的 Git 版本控制。

**核心优势**:
1. 版本控制: 每个数据库变更都有版本号（V1, V2, V3...），可追溯
2. 自动化: 应用启动时自动执行未运行的迁移脚本
3. 团队协作: 所有开发者使用相同的数据库结构
4. 环境一致性: 开发、测试、生产环境的数据库结构保持一致
5. Spring Boot 集成: 原生支持，零配置即可使用

**与 Liquibase 对比**:
- 学习曲线: Flyway 简单，Liquibase 复杂
- 文件格式: Flyway 使用 SQL，Liquibase 使用 XML/YAML/SQL
- 配置复杂度: Flyway 低，Liquibase 高

**选择理由**: Flyway 更简单、直观，使用纯 SQL 文件，符合任务要求。

### 5.2 Flyway 工作原理

```
1. 应用启动
   ↓
2. Flyway 检查数据库中的 flyway_schema_history 表
   ↓
3. 对比 classpath:db/migration 目录中的脚本
   ↓
4. 执行未运行的迁移脚本（按版本号顺序）
   ↓
5. 记录执行历史到 flyway_schema_history 表
```

### 5.3 迁移脚本命名规范

```
V1__Create_account_table.sql
│ │  └─ 描述（用下划线分隔）
│ └─ 双下划线分隔符
└─ 版本号（V + 数字）
```

**规则**:
- 版本号必须唯一且递增
- 使用双下划线分隔版本号和描述
- 描述使用下划线分隔单词
- 文件名一旦创建不可修改

---

## 6. 遇到的问题和解决方案

### 问题 1: YAML 配置重复键错误

**错误信息**: DuplicateKeyException: found duplicate key spring

**原因**: 在 application.yml 中有两个 spring: 键

**解决方案**: 将 Flyway 配置合并到第一个 spring: 块中

### 问题 2: baseline-on-migrate 导致 V1 脚本被跳过

**错误信息**: Failed to open the referenced table 'account'

**原因**: baseline-on-migrate: true 默认将第一个迁移作为 baseline，导致 V1 被跳过

**解决方案**: 添加 baseline-version: 0，从版本 0 开始 baseline，确保所有脚本都执行

### 问题 3: 表名命名规范不一致

**问题**: 新建表使用 account、session，而现有表使用 t_node

**解决方案**: 
- 修改表名为 t_account、t_session
- 更新外键引用
- 重新执行迁移脚本

---

## 7. 验收标准检查

### 任务验收标准

- [x] 运行时验证 - 执行数据库迁移脚本，表创建成功
  - ✅ Flyway 成功执行 V1 和 V2 迁移脚本
  - ✅ t_account 和 t_session 表创建成功

- [x] 运行时验证 - 查询数据库，验证表结构和索引符合设计
  - ✅ 所有字段类型、默认值、约束正确
  - ✅ 主键、唯一索引、普通索引创建成功
  - ✅ 外键约束创建成功

- [x] 运行时验证 - 执行 INSERT INTO account 测试语句，验证约束生效
  - ✅ 正常数据插入成功
  - ✅ 重复用户名被拒绝
  - ✅ 重复邮箱被拒绝

### 需求验收标准

- [x] REQ-FR-003: 账号注册
  - ✅ t_account 表支持存储用户名、邮箱、密码
  - ✅ 用户名和邮箱唯一性约束生效

- [x] REQ-FR-007: 会话管理
  - ✅ t_session 表支持存储会话信息
  - ✅ 外键约束确保数据一致性

---

## 8. 总结

### 8.1 完成情况

✅ **任务完成度**: 100%

所有验收标准均已通过，数据库表结构创建成功，Flyway 迁移机制正常工作。

### 8.2 关键成果

1. ✅ 成功集成 Flyway 数据库迁移工具
2. ✅ 创建 t_account 和 t_session 表
3. ✅ 所有索引和约束正常工作
4. ✅ 表名遵循项目命名规范（t_ 前缀）
5. ✅ 建立了数据库版本控制机制

### 8.3 技术亮点

1. **自动化迁移**: 应用启动时自动执行数据库迁移，无需手动操作
2. **版本追踪**: Flyway 记录所有迁移历史，便于追溯和回滚
3. **团队协作**: 统一的迁移脚本确保所有环境数据库结构一致
4. **命名规范**: 遵循项目现有规范，保持代码库一致性

### 8.4 后续建议

1. **生产环境部署**: 
   - 在生产环境首次部署时，建议先备份数据库
   - 可以考虑使用 Flyway 的 baseline 命令处理已存在的数据库

2. **迁移脚本管理**:
   - 迁移脚本一旦执行不可修改
   - 如需修改表结构，创建新的迁移脚本（如 V3, V4...）

3. **性能优化**:
   - 后续可根据实际查询需求添加复合索引
   - 监控慢查询，优化索引策略

---

**验证人**: AI Assistant  
**验证日期**: 2025-11-24  
**验证结论**: ✅ 通过
