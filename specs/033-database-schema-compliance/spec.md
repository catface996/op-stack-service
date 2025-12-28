# Feature Specification: 数据库表结构合规性重构

**Feature Branch**: `033-database-schema-compliance`
**Created**: 2025-12-28
**Status**: Draft
**Input**: 根据宪法合规性检查发现，重构数据库表结构以符合 Database Design Standards 规范

## 背景

根据宪法 VII. Database Design Standards 的合规性检查，发现以下违规情况需要修复：

1. **表名违规** (2处)：`reports` 和 `report_templates` 使用了复数形式
2. **缺少 `deleted` 字段** (5处)：`node`, `node_2_node`, `node_type`, `topology`, `topology_2_node`
3. **缺少 `updated_by` 字段** (9处)：多数业务表缺少修改人字段
4. **缺少 `created_by` 字段** (5处)：部分业务表缺少创建人字段
5. **缺少 `updated_at` 字段** (1处)：`reports` 表
6. **主键缺少 COMMENT** (4处)：`node`, `report_templates`, `reports`, `topology`

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 数据库迁移执行 (Priority: P1)

作为运维人员，我需要执行数据库迁移脚本，使表结构符合宪法规范，同时保证现有数据不丢失、功能不受影响。

**Why this priority**: 这是整个重构的基础，必须首先完成数据库层面的变更，后续的代码重构才能进行。

**Independent Test**: 可以通过执行迁移脚本后检查表结构是否符合规范来验证。

**Acceptance Scenarios**:

1. **Given** 现有数据库包含不合规的表结构, **When** 执行 Flyway 迁移脚本, **Then** 所有表结构符合宪法规范且现有数据完整保留
2. **Given** 表 `reports` 存在数据, **When** 重命名为 `report`, **Then** 所有数据完整迁移，外键关系保持正确
3. **Given** 表缺少 `deleted` 字段, **When** 添加该字段, **Then** 现有记录的 `deleted` 值默认为 0（未删除）
4. **Given** 表缺少审计字段, **When** 添加 `created_by`/`updated_by` 字段, **Then** 现有记录这些字段为 NULL

---

### User Story 2 - 代码适配重构 (Priority: P2)

作为开发人员，我需要重构相关的 Java 代码（Entity、Mapper、Service），使其适配新的表结构，保证所有 API 功能正常工作。

**Why this priority**: 数据库变更后，必须同步更新代码才能保证系统可用。

**Independent Test**: 可以通过启动应用并调用所有相关 API 来验证功能正常。

**Acceptance Scenarios**:

1. **Given** 表名从 `reports` 改为 `report`, **When** 修改 Entity 类的 `@TableName` 注解, **Then** MyBatis-Plus 能正确映射到新表
2. **Given** 新增了 `updated_by` 字段, **When** 更新记录时, **Then** 系统自动填充当前操作用户 ID
3. **Given** 应用启动后, **When** 调用报告管理相关 API, **Then** 所有 CRUD 操作正常工作

---

### User Story 3 - 软删除功能启用 (Priority: P3)

作为系统，对于新增 `deleted` 字段的表，我需要启用软删除功能，使查询时自动过滤已删除记录。

**Why this priority**: 这是数据安全的增强功能，依赖于前两个 User Story 完成。

**Independent Test**: 可以通过创建记录、删除记录、查询记录来验证软删除是否生效。

**Acceptance Scenarios**:

1. **Given** `node` 表启用软删除, **When** 删除一条节点记录, **Then** 该记录的 `deleted` 字段变为 1，而非物理删除
2. **Given** 存在已软删除的记录, **When** 执行普通查询, **Then** 软删除记录不会出现在结果中
3. **Given** 需要查看所有记录（含已删除）, **When** 使用特殊查询方法, **Then** 能够查询到软删除的记录

---

### Edge Cases

- 迁移过程中数据库连接中断：需要支持迁移脚本的幂等性
- 存在外键引用的表重命名：需要先删除外键约束再重建
- 大数据量表添加字段：可能需要分批执行以避免锁表时间过长
- 并发写入时的字段填充：审计字段填充需要线程安全

## Requirements *(mandatory)*

### Functional Requirements

**数据库迁移**:

- **FR-001**: 系统 MUST 将 `reports` 表重命名为 `report`
- **FR-002**: 系统 MUST 将 `report_templates` 表重命名为 `report_template`
- **FR-003**: 系统 MUST 为 `node`, `node_2_node`, `node_type`, `topology`, `topology_2_node` 表添加 `deleted` 字段
- **FR-004**: 系统 MUST 为缺少 `updated_by` 字段的表添加该字段
- **FR-005**: 系统 MUST 为缺少 `created_by` 字段的表添加该字段
- **FR-006**: 系统 MUST 为 `report` 表添加 `updated_at` 字段
- **FR-007**: 系统 MUST 为缺少 COMMENT 的主键字段添加 COMMENT

**代码重构**:

- **FR-008**: 系统 MUST 更新所有受影响的 Entity 类以匹配新表结构
- **FR-009**: 系统 MUST 更新所有受影响的 Mapper 接口
- **FR-010**: 系统 MUST 启用新增 `deleted` 字段的表的软删除功能
- **FR-011**: 系统 MUST 实现审计字段（`created_by`, `updated_by`）的自动填充

**数据完整性**:

- **FR-012**: 迁移过程 MUST 保证现有数据不丢失
- **FR-013**: 迁移脚本 MUST 支持幂等执行
- **FR-014**: 所有 API 功能在迁移后 MUST 保持正常工作

### Key Entities

- **report** (原 `reports`): 报告记录，包含标题、类型、状态、内容等信息，关联拓扑图
- **report_template** (原 `report_templates`): 报告模板，包含名称、分类、模板内容
- **node**: 资源节点，新增软删除支持
- **node_type**: 节点类型，新增软删除支持
- **node_2_node**: 节点关系，新增软删除支持
- **topology**: 拓扑图，新增软删除支持
- **topology_2_node**: 拓扑图节点关联，新增软删除支持

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 迁移完成后，所有 13 张业务表 100% 符合宪法 Database Design Standards 规范
- **SC-002**: 迁移过程中数据零丢失，所有现有记录完整保留
- **SC-003**: 应用启动后，所有现有 API 端点功能正常（可通过 Swagger UI 验证）
- **SC-004**: 软删除功能正常工作，删除操作不再物理删除数据
- **SC-005**: 审计字段自动填充，更新操作时 `updated_by` 和 `updated_at` 正确记录

## Assumptions

1. 迁移可以在维护窗口期执行，允许短暂停机
2. 现有数据量在可接受范围内，不需要分批迁移
3. 审计字段 `created_by`/`updated_by` 对于历史数据可以为 NULL
4. 所有外键约束可以临时禁用后重建

## Out of Scope

- 数据回填（为历史记录填充 `created_by`/`updated_by`）
- 性能优化
- 单元测试覆盖
