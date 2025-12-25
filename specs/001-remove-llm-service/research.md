# Research: 移除LLM服务管理功能

**Date**: 2025-12-25
**Feature**: 001-remove-llm-service

## 依赖分析

### 代码依赖检查

执行全量代码搜索 `LlmService|llm_service|LLM_SERVICE`，结果如下：

**Java 文件 (19 files)** - 全部在 LLM 模块内部，无外部依赖：

| 层级 | 文件数 | 文件列表 |
|-----|-------|---------|
| Interface | 5 | LlmServiceController.java, 4 个 Request 类 |
| Application | 5 | LlmServiceApplicationService.java (接口), LlmServiceApplicationServiceImpl.java (实现), 3 个 DTO/Command 类 |
| Domain | 3 | LlmServiceDomainService.java, LlmServiceDomainServiceImpl.java, LlmService.java |
| Repository | 5 | LlmServiceRepository.java, LlmServiceEntity.java, LlmServiceRepositoryImpl.java, LlmServiceMapper.java, LlmServicePO.java |
| Common | 1 | LlmServiceErrorCode.java |

**XML Mapper 文件 (1 file)**：
- `infrastructure/repository/mysql-impl/src/main/resources/mapper/llm/LlmServiceMapper.xml`

**配置文件 (0 files)**：
- 无 YML 配置文件引用 LLM 服务

### Decision: 无外部依赖

**Rationale**: 代码搜索确认所有 LLM 服务相关引用均在 LLM 模块内部，可以安全删除。

**Alternatives considered**: 无

## 数据库表分析

### 现有表结构

表名: `llm_service_config` (V7__Create_llm_service_table.sql)

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 服务名称（唯一） |
| description | VARCHAR(500) | 服务描述 |
| provider_type | VARCHAR(20) | 供应商类型 |
| endpoint | VARCHAR(500) | API 端点 |
| model_parameters | JSON | 模型参数 |
| priority | INT | 优先级 |
| enabled | BOOLEAN | 是否启用 |
| is_default | BOOLEAN | 是否默认 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### Decision: 使用 Flyway 迁移脚本删除表

**Rationale**:
- 项目使用 Flyway 管理数据库版本
- 当前最新版本为 V8
- 新建 V9 迁移脚本执行 DROP TABLE

**Alternatives considered**:
- 直接执行 SQL：不推荐，破坏版本历史
- 保留空表：不推荐，增加维护负担

## 删除顺序分析

### Decision: 按依赖关系逆序删除

```
Controller → Application Service → Domain Service → Repository → Domain Model → Common
```

**Rationale**: 按 DDD 分层的依赖方向逆序删除，确保每次删除后编译仍能通过。

**删除顺序**:
1. Interface 层 (Controller, Request) - 无被依赖
2. Application 层 (Service, DTO) - 仅被 Interface 依赖
3. Domain 层 (Service, Model) - 被 Application 依赖
4. Repository 层 (Repository, Entity, Mapper, PO) - 被 Domain 依赖
5. Common 层 (ErrorCode) - 被多层依赖
6. Database Migration - 最后执行

## 风险评估

| 风险项 | 评估 | 缓解措施 |
|-------|------|---------|
| 遗漏文件 | 低 | 已执行全量搜索，文件清单完整 |
| 编译失败 | 低 | 按依赖顺序删除，每步验证 |
| 运行时异常 | 低 | 无外部依赖，删除后不影响其他模块 |
| 数据丢失 | 低 | 规范已确认数据可删除 |

## 结论

- **外部依赖**: 无
- **文件总数**: 20 个 (19 Java + 1 XML)
- **数据库表**: 1 个 (llm_service_config)
- **建议**: 可以安全执行删除操作
