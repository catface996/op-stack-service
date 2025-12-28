# Research: 数据库表结构合规性重构

**Feature**: 033-database-schema-compliance
**Date**: 2025-12-28

## 研究概述

本功能无需外部技术调研，所有技术方案基于现有项目规范和 MyBatis-Plus 最佳实践。

## 决策记录

### Decision 1: 表重命名策略

**Decision**: 使用 MySQL `RENAME TABLE` 语句直接重命名

**Rationale**:
- MySQL 8.0 的 `RENAME TABLE` 是原子操作，无数据丢失风险
- 不需要复制数据，性能最优
- 外键约束会自动更新（如果表被其他表引用）

**Alternatives considered**:
- 创建新表 + 复制数据 + 删除旧表：操作复杂，风险较高
- 使用视图别名：增加复杂度，不推荐

**Implementation**:
```sql
RENAME TABLE reports TO report;
RENAME TABLE report_templates TO report_template;
```

### Decision 2: 添加字段策略

**Decision**: 使用 `ALTER TABLE ADD COLUMN` 添加字段，设置合理默认值

**Rationale**:
- MySQL 8.0 支持快速 `ALTER TABLE`（Instant DDL），对于添加末尾字段性能优秀
- 新增字段设置 `DEFAULT NULL` 或 `DEFAULT 0`，不影响现有数据

**Alternatives considered**:
- 重建表：对于大表性能差，不推荐
- 分批添加：当前数据量不大，无需分批

**Implementation**:
```sql
ALTER TABLE node ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除';
ALTER TABLE node ADD COLUMN updated_by BIGINT COMMENT '修改人ID';
```

### Decision 3: 软删除实现方案

**Decision**: 使用 MyBatis-Plus `@TableLogic` 注解实现软删除

**Rationale**:
- 项目已有使用该注解的先例（如 `AgentPO`, `ReportPO`）
- 自动过滤已删除记录，无需修改查询逻辑
- 支持全局配置和局部覆盖

**Configuration**: 在 PO 类中添加：
```java
@TableField("deleted")
@TableLogic
private Integer deleted;
```

**Alternatives considered**:
- 手动过滤：需要修改所有查询，维护成本高
- 物理删除：不满足数据保留需求

### Decision 4: 审计字段自动填充方案

**Decision**: 扩展现有 `CustomMetaObjectHandler` 实现审计字段自动填充

**Rationale**:
- 项目已有 `CustomMetaObjectHandler` 处理 `createdAt`, `updatedAt`
- 只需扩展支持 `createdBy`, `updatedBy`
- 需要结合 Spring Security 或请求上下文获取当前用户 ID

**Implementation**:
```java
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "createdBy", Long.class, getCurrentUserId());
    this.strictInsertFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
    this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
}

@Override
public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    this.strictUpdateFill(metaObject, "updatedBy", Long.class, getCurrentUserId());
}
```

**获取当前用户 ID**:
- 方案 A: 从 Request Header 获取（网关注入的 `userId`）
- 方案 B: 从 ThreadLocal 获取（需要在请求过滤器中设置）

**选择方案 B**：更通用，不依赖 HTTP 请求上下文

### Decision 5: 迁移脚本执行顺序

**Decision**: 单个迁移脚本，按依赖顺序执行

**Execution Order**:
1. 禁用外键检查
2. 重命名表（先处理被引用的表）
3. 添加缺失字段
4. 修改主键 COMMENT
5. 启用外键检查

**Rationale**:
- 单脚本便于版本管理和回滚
- 禁用外键检查避免依赖问题
- 顺序执行确保数据一致性

## 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| 表重命名导致外键失效 | 低 | MySQL RENAME TABLE 自动更新外键 |
| 迁移过程中断 | 中 | 使用事务包裹，支持回滚 |
| PO 类与新表结构不匹配 | 中 | 迁移脚本和代码变更同步提交 |
| 审计字段无法获取用户 ID | 低 | 设置为 NULL，历史数据允许为空 |

## 验收标准

1. 所有 13 张业务表符合宪法 Database Design Standards
2. 应用启动无报错
3. 所有现有 API 功能正常
4. 软删除功能正常工作
5. 新增/更新记录时审计字段正确填充

## 参考资料

- [MyBatis-Plus 逻辑删除](https://baomidou.com/pages/6b03c5/)
- [MyBatis-Plus 自动填充](https://baomidou.com/pages/4c6bcf/)
- [MySQL 8.0 Instant DDL](https://dev.mysql.com/doc/refman/8.0/en/innodb-online-ddl-operations.html)
