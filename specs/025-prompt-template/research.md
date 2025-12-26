# Research: 提示词模板管理

**Feature**: 025-prompt-template
**Date**: 2025-12-26

## 1. 版本控制策略

### Decision
采用**版本快照模式**：每次更新创建完整的新版本记录，版本号递增。回滚通过创建新版本实现（复制历史版本内容到新版本）。

### Rationale
- 实现简单，避免复杂的 diff/patch 逻辑
- 每个版本独立完整，查询性能好
- 回滚操作语义清晰：创建新版本而非指针切换
- 符合审计需求：所有变更都有记录

### Alternatives Considered
1. **Git-like 存储**: 存储 diff，节省空间但实现复杂
2. **指针回滚**: 直接修改当前版本指针，可能丢失回滚操作的审计记录
3. **时间旅行模式**: 通过时间戳查询历史状态，需要额外的时间索引

## 2. 并发控制机制

### Decision
采用**乐观锁**：使用 `version` 字段（数据库行版本）配合 MyBatis-Plus 的 `@Version` 注解实现。

### Rationale
- 读多写少场景下性能优于悲观锁
- MyBatis-Plus 原生支持，实现成本低
- 冲突时返回明确错误，用户可重试

### Alternatives Considered
1. **悲观锁**: 使用 `SELECT FOR UPDATE`，写入频繁时性能差
2. **分布式锁**: 使用 Redis，增加系统复杂度
3. **无锁**: 依赖数据库唯一约束，可能导致数据不一致

## 3. 用途类型管理

### Decision
用途作为**独立实体存储**，系统预置常用类型，用户可自定义添加。

### Rationale
- 灵活性：用户可根据业务需求扩展
- 可维护性：用途类型集中管理
- 查询友好：支持按用途筛选

### Alternatives Considered
1. **硬编码枚举**: 扩展需要代码修改和部署
2. **字符串自由输入**: 无法保证一致性，难以做统计
3. **配置文件**: 修改需要重启服务

## 4. 软删除实现

### Decision
在 `prompt_template` 和 `template_usage` 表添加 `deleted` 字段（Boolean），查询时自动过滤已删除记录。版本历史表不单独标记删除。

### Rationale
- 数据安全：误删可恢复
- 实现简单：MyBatis-Plus 支持逻辑删除注解
- 版本历史随主表删除状态隐藏

### Alternatives Considered
1. **物理删除**: 数据不可恢复
2. **归档表**: 删除时移动到归档表，增加复杂度
3. **状态字段**: 使用 status 字段包含删除状态，语义不清晰

## 5. 内容存储

### Decision
使用 MySQL `TEXT` 类型存储模板内容，应用层限制最大 64KB。

### Rationale
- TEXT 类型最大 64KB，满足需求
- 无需额外的对象存储依赖
- 查询和更新操作简单

### Alternatives Considered
1. **MEDIUMTEXT**: 16MB 上限，超出实际需求
2. **对象存储 (OSS/S3)**: 增加系统复杂度，小内容存取效率低
3. **分表存储**: 将内容单独存表，增加 JOIN 复杂度

## 6. API 设计模式

### Decision
遵循项目现有的 **POST-Only API** 设计模式，所有操作使用 POST 方法。

### Rationale
- 与现有 API 风格一致
- 请求体可携带复杂参数
- 避免 URL 长度限制问题

### Alternatives Considered
1. **RESTful**: 使用 GET/POST/PUT/DELETE，与现有风格不一致
2. **GraphQL**: 学习成本高，项目未采用

## 7. 版本号生成策略

### Decision
使用**模板级自增**：每个模板的版本号从 1 开始独立递增，存储在 `prompt_template` 表的 `current_version` 字段。

### Rationale
- 版本号对用户友好，易于理解
- 支持快速查询最新版本
- 实现简单，无需全局序列

### Alternatives Considered
1. **全局递增**: 所有模板共享版本序列，版本号跳跃
2. **UUID**: 无序，用户不友好
3. **时间戳**: 精度问题，不适合做版本标识

## 8. 数据库表设计要点

### Decision
三表设计：`prompt_template`（主表）、`prompt_template_version`（版本表）、`template_usage`（用途表）

### Key Indexes
- `prompt_template`: `name` (UNIQUE), `usage_id`, `deleted`
- `prompt_template_version`: `template_id + version` (UNIQUE), `template_id + created_at`
- `template_usage`: `code` (UNIQUE), `deleted`

### Rationale
- 主表存储模板元数据，版本表存储内容历史
- 用途表独立，支持复用和扩展
- 索引设计支持常用查询场景
