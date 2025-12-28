# Research: Report Management

## Overview

本文档记录报告管理功能实现前的技术调研结果。

## Research Items

### 1. Report 不可变性设计

**Decision**: Report 实体创建后不可修改，不提供 update API

**Rationale**:
- 报告作为正式文档，一旦生成需保持历史记录完整性
- 避免报告被意外修改导致审计问题
- 简化数据模型，无需 updated_at 和 version 字段

**Alternatives considered**:
- 提供 update API 但记录修改历史 - 增加复杂度，不符合需求文档要求
- 软删除后重新创建 - 不符合正常用户操作习惯

### 2. 全文搜索实现方案

**Decision**: 使用 MySQL FULLTEXT INDEX 实现关键词搜索

**Rationale**:
- 项目已使用 MySQL 8.0，原生支持 FULLTEXT 索引
- 搜索范围（title, summary, tags）数据量有限，FULLTEXT 足够
- 无需引入额外搜索引擎依赖

**Alternatives considered**:
- Elasticsearch - 过度设计，当前需求不需要
- LIKE 查询 - 性能较差，不支持分词搜索

### 3. Tags 字段存储方案

**Decision**: 使用 JSON 字段存储标签数组

**Rationale**:
- MySQL 8.0 原生支持 JSON 类型
- 标签数量通常较少（<20个），JSON 存储效率高
- 简化数据模型，无需额外的关联表
- 与项目现有模式一致（参考 resource 表的 tags 字段）

**Alternatives considered**:
- 独立标签表 + 关联表 - 增加复杂度，当前需求不需要
- 逗号分隔字符串 - 不支持数组操作，查询不便

### 4. ID 生成策略

**Decision**: 使用 MySQL AUTO_INCREMENT BIGINT 作为主键

**Rationale**:
- 与项目现有表结构一致
- MyBatis-Plus 默认支持
- 性能好，索引效率高

**Alternatives considered**:
- UUID (VARCHAR 64) - 需求文档建议，但项目统一使用 BIGINT
- 业务前缀 + 序列号（如 rpt-001）- 前端展示用，可在 DTO 层转换

### 5. 分页查询排序支持

**Decision**: 支持 sort_by 和 sort_order 参数，默认按 created_at DESC

**Rationale**:
- 符合需求文档要求
- 常见的报告查询场景是查看最新报告
- 支持灵活的排序需求

**Implementation**:
- sort_by 白名单：created_at, title, type, status
- sort_order 枚举：asc, desc
- 防止 SQL 注入风险

### 6. Topology 关联校验

**Decision**: 创建报告时，如果提供了 topology_id，校验该拓扑是否存在

**Rationale**:
- 保证数据引用完整性
- 需求文档明确要求（FR-024）

**Implementation**:
- 在 Application Service 层调用 TopologyRepository 检查
- 不存在时返回 404003 错误码（Topology not found）

### 7. ReportTemplate 与 PromptTemplate 区分

**Decision**: ReportTemplate 是独立实体，不复用 PromptTemplate

**Rationale**:
- 两者用途不同：ReportTemplate 用于报告生成，PromptTemplate 用于 AI 提示词
- 字段不同：ReportTemplate 有 category，PromptTemplate 有 usageId 和版本管理
- 避免混淆，保持领域边界清晰

### 8. 错误码设计

**Decision**: 遵循项目错误码规范，新增报告管理相关错误码

**Error Codes**:
| Code | HTTP Status | Description |
|------|-------------|-------------|
| 400001 | 400 | Invalid request parameters |
| 400002 | 400 | Report title is required |
| 400003 | 400 | Invalid report type |
| 400004 | 400 | Invalid report status |
| 404001 | 404 | Report not found |
| 404002 | 404 | Report template not found |
| 404003 | 404 | Topology not found |
| 409001 | 409 | Report name already exists |
| 500001 | 500 | Internal server error |

## Summary

所有技术决策已明确，无需进一步澄清。实现将遵循项目现有架构模式和最佳实践。
