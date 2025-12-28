# Quickstart: 数据库表结构合规性重构

**Feature**: 033-database-schema-compliance
**Date**: 2025-12-28

## 前置条件

- MySQL 8.0 数据库正在运行
- 已备份现有数据（推荐）
- Java 21 环境已配置

## 快速验证步骤

### Step 1: 执行数据库迁移

```bash
# 方式 1: 应用启动时自动执行 Flyway 迁移
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 方式 2: 手动执行迁移（仅用于测试）
mvn flyway:migrate -pl bootstrap -Dflyway.configFiles=bootstrap/src/main/resources/application-local.yml
```

### Step 2: 验证表结构变更

```bash
# 连接数据库
mysql -h 127.0.0.1 -P 3306 -u root -proot123 op_stack_service

# 验证表重命名
SHOW TABLES LIKE 'report%';
-- 预期结果: report, report_template

# 验证 node 表新增字段
DESCRIBE node;
-- 预期结果: 包含 deleted, updated_by 字段

# 验证 topology 表新增字段
DESCRIBE topology;
-- 预期结果: 包含 deleted, updated_by 字段
```

### Step 3: 验证应用功能

```bash
# 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 访问 Swagger UI 验证 API
open http://localhost:8081/swagger-ui/index.html
```

**验证清单**:

- [ ] 报告管理 API (`/api/service/v1/reports/*`) 正常工作
- [ ] 报告模板 API (`/api/service/v1/report-templates/*`) 正常工作
- [ ] 节点管理 API (`/api/service/v1/nodes/*`) 正常工作
- [ ] 拓扑图管理 API (`/api/service/v1/topologies/*`) 正常工作

### Step 4: 验证软删除功能

```bash
# 在 Swagger UI 中测试删除节点
# 1. 创建一个测试节点
# 2. 删除该节点
# 3. 查询节点列表，确认已删除节点不显示
# 4. 直接查询数据库，确认 deleted = 1
```

```sql
-- 数据库验证
SELECT id, name, deleted FROM node WHERE id = <测试节点ID>;
-- 预期结果: deleted = 1
```

### Step 5: 验证审计字段填充

```bash
# 在 Swagger UI 中测试更新操作
# 1. 更新一个现有记录
# 2. 查询数据库验证 updated_at 和 updated_by 已更新
```

```sql
-- 数据库验证
SELECT id, name, updated_by, updated_at FROM node WHERE id = <测试节点ID>;
-- 预期结果: updated_by 和 updated_at 已填充
```

## 回滚步骤（如需）

如果迁移出现问题，可以执行以下回滚操作：

```sql
-- 警告：回滚会丢失新增字段的数据
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 恢复表名
RENAME TABLE report TO reports;
RENAME TABLE report_template TO report_templates;

-- 2. 删除新增字段（按需）
ALTER TABLE node DROP COLUMN deleted;
ALTER TABLE node DROP COLUMN updated_by;
-- ... 其他表类似

SET FOREIGN_KEY_CHECKS = 1;
```

## 常见问题

### Q1: 迁移脚本执行失败怎么办？

检查 Flyway 历史记录：
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
```

如果 V24 迁移失败，删除对应记录后重试：
```sql
DELETE FROM flyway_schema_history WHERE version = '24';
```

### Q2: 应用启动报 "Table not found" 错误？

确保 PO 类的 `@TableName` 注解已更新：
- `ReportPO`: `@TableName("report")`
- `ReportTemplatePO`: `@TableName("report_template")`

### Q3: 软删除不生效？

检查 PO 类是否添加了 `@TableLogic` 注解：
```java
@TableField("deleted")
@TableLogic
private Integer deleted;
```

## 验证完成标准

- [ ] 所有表结构符合宪法 Database Design Standards
- [ ] 应用启动无报错
- [ ] 所有 API 端点功能正常
- [ ] 软删除功能正常工作
- [ ] 审计字段自动填充正常
