# Feature 036: SQL 重构 - 从 Annotation + Lambda 迁移到 mapper.xml

## 概述

将项目中所有 MyBatis SQL 实现从注解和 Lambda 方式统一迁移到 mapper.xml 文件，规范化 SQL 管理方式。

## Clarifications

### Session 2025-12-29
- Q: BaseMapper 方法的使用边界（是否允许使用需要 Wrapper 参数的方法如 selectCount、selectList）？ → A: 完全禁止使用 Lambda/Wrapper，所有需要动态条件的查询都写 XML
- Q: 重构失败时的回滚策略？ → A: 不设回滚策略，问题直接修复，不回退

## 背景

当前项目中 SQL 实现方式混乱：
- 13 个 Mapper 接口使用 @Select/@Update/@Delete 注解（共 69 处）
- 10 个 RepositoryImpl 使用 LambdaQueryWrapper/LambdaUpdateWrapper（共 13 处）
- 没有任何 mapper.xml 文件

这种混合模式导致：
1. SQL 分散在多处，难以统一管理和审查
2. 复杂 SQL 在注解中可读性差
3. 缺乏统一的 SQL 编写规范

## 功能需求

### FR-001: 创建 mapper.xml 文件
- 为每个 Mapper 接口创建对应的 XML 文件
- XML 文件存放在 `infrastructure/repository/mysql-impl/src/main/resources/mapper/` 目录下
- 按模块分子目录：agent/, node/, topology/, prompt/, report/

### FR-002: 迁移注解 SQL 到 XML
- 将所有 @Select/@Insert/@Update/@Delete 注解中的 SQL 迁移到对应 XML 文件
- 保持 SQL 逻辑不变
- 移除 Mapper 接口中的注解，保留方法签名

### FR-003: 完全消除 Lambda/Wrapper 表达式
- 将 RepositoryImpl 中的 LambdaQueryWrapper 替换为 Mapper 方法调用
- 将 RepositoryImpl 中的 LambdaUpdateWrapper 替换为 Mapper 方法调用
- 在 XML 中定义对应的 SQL
- **禁止使用任何需要 Wrapper 参数的 BaseMapper 方法**（如 selectCount(wrapper)、selectList(wrapper)、update(entity, wrapper)）

### FR-004: 保留 BaseMapper 无参/单参基础方法
- 继续继承 MyBatis-Plus BaseMapper
- **仅允许使用不需要 Wrapper 参数的方法**：selectById, selectBatchIds, insert, updateById, deleteById
- 所有需要动态条件的查询必须在 XML 中定义

### FR-005: 更新宪法规则
- 在 constitution.md 中添加 SQL 实现规范
- 禁止使用注解 SQL
- 禁止使用 Lambda 构建查询
- 要求所有自定义 SQL 在 XML 中定义

## 非功能需求

### NFR-001: 功能等价性
- 重构后所有 API 功能必须与重构前完全一致
- 不得改变任何业务逻辑

### NFR-002: 编译通过
- `mvn clean compile` 必须成功
- 无编译警告

### NFR-003: 测试通过
- 所有现有单元测试必须通过
- 所有 API 功能验证必须通过

## 影响范围

### 需创建的文件（13 个 XML）
| 文件 | SQL 数量 |
|------|----------|
| AgentMapper.xml | 11 |
| NodeMapper.xml | 5 |
| NodeTypeMapper.xml | 2 |
| NodeAgentRelationMapper.xml | 6 |
| Node2NodeMapper.xml | 12 |
| TopologyMapper.xml | 3 |
| Topology2NodeMapper.xml | 6 |
| TopologyReportTemplateMapper.xml | 6 |
| PromptTemplateMapper.xml | 4 |
| PromptTemplateVersionMapper.xml | 5 |
| TemplateUsageMapper.xml | 3 |
| ReportMapper.xml | 2 |
| ReportTemplateMapper.xml | 4 |

### 需修改的文件
- 13 个 Mapper.java（移除注解）
- 10 个 RepositoryImpl.java（移除 Lambda）
- 1 个 application.yml（添加配置）
- 1 个 constitution.md（添加规则）

## 验收标准

- [ ] 所有 Mapper 接口中无 @Select/@Insert/@Update/@Delete 注解
- [ ] 所有 RepositoryImpl 中无 LambdaQueryWrapper/LambdaUpdateWrapper
- [ ] 所有自定义 SQL 在 mapper.xml 中有对应定义
- [ ] `mvn clean compile` 编译通过
- [ ] 所有单元测试通过
- [ ] API 功能验证通过
- [ ] 宪法规则已更新
