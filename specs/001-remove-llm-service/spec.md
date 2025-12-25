# Feature Specification: 移除LLM服务管理功能

**Feature Branch**: `001-remove-llm-service`
**Created**: 2025-12-25
**Status**: Draft
**Input**: User description: "移除所有LLM服务管理相关的接口"

## 背景与动机

当前系统中存在 LLM 服务管理模块，提供了 LLM 服务配置的增删改查、启用/禁用、设置默认服务等功能。该模块目前不再需要，需要从系统中完整移除以简化系统架构、降低维护成本。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 系统管理员移除无用模块 (Priority: P1)

作为系统管理员，我希望移除不再使用的 LLM 服务管理模块，以便：
- 简化系统架构，减少代码维护工作量
- 消除不必要的 API 端点，降低安全攻击面
- 减少数据库表数量，简化数据管理

**Why this priority**: 这是本次需求的核心目标，直接影响系统的整洁性和可维护性。

**Independent Test**: 可以通过访问原有 LLM 服务管理接口，验证其已不可用（返回 404）。

**Acceptance Scenarios**:

1. **Given** LLM 服务管理接口已被移除, **When** 客户端访问 `/api/v1/llm-services/*` 任意端点, **Then** 系统返回 404 Not Found
2. **Given** LLM 服务相关代码已被移除, **When** 系统启动, **Then** 系统能够正常启动且无 LLM 相关的错误日志
3. **Given** LLM 服务功能已被移除, **When** 查看 Swagger API 文档, **Then** 文档中不再显示 LLM 服务管理相关的接口
4. **Given** LLM 服务数据库表已被移除, **When** 检查数据库结构, **Then** 不存在 LLM 服务相关的表

---

### User Story 2 - 开发人员代码清理 (Priority: P2)

作为开发人员，我需要确保 LLM 服务相关的代码被完整移除，不留下孤立的引用或依赖。

**Why this priority**: 确保代码库的整洁性，避免编译错误或运行时异常。

**Independent Test**: 可以通过编译项目验证无编译错误，通过运行测试验证无引用缺失。

**Acceptance Scenarios**:

1. **Given** LLM 服务代码已被移除, **When** 执行项目编译, **Then** 编译成功无错误
2. **Given** LLM 服务代码已被移除, **When** 执行所有单元测试, **Then** 测试全部通过
3. **Given** LLM 服务代码已被移除, **When** 搜索代码库中的 "LlmService" 关键词, **Then** 无任何匹配结果

---

### Edge Cases

- 是否有其他模块依赖 LLM 服务功能？需要检查并处理这些依赖
- 数据库中已存在的 LLM 服务配置数据如何处理？
- 是否需要保留数据库迁移记录以便追溯？

## Requirements *(mandatory)*

### Functional Requirements

**接口移除**
- **FR-001**: 系统必须移除 `/api/v1/llm-services/query` 接口（查询 LLM 服务列表）
- **FR-002**: 系统必须移除 `/api/v1/llm-services/create` 接口（创建 LLM 服务）
- **FR-003**: 系统必须移除 `/api/v1/llm-services/get` 接口（获取 LLM 服务详情）
- **FR-004**: 系统必须移除 `/api/v1/llm-services/update` 接口（更新 LLM 服务）
- **FR-005**: 系统必须移除 `/api/v1/llm-services/delete` 接口（删除 LLM 服务）
- **FR-006**: 系统必须移除 `/api/v1/llm-services/update-status` 接口（更新服务状态）
- **FR-007**: 系统必须移除 `/api/v1/llm-services/set-default` 接口（设置默认服务）

**代码清理**
- **FR-008**: 系统必须移除 LLM 服务相关的控制器代码
- **FR-009**: 系统必须移除 LLM 服务相关的应用服务代码
- **FR-010**: 系统必须移除 LLM 服务相关的领域服务代码
- **FR-011**: 系统必须移除 LLM 服务相关的领域模型代码
- **FR-012**: 系统必须移除 LLM 服务相关的仓储接口和实现代码
- **FR-013**: 系统必须移除 LLM 服务相关的请求/响应 DTO 代码
- **FR-014**: 系统必须移除 LLM 服务相关的错误码定义

**数据库清理**
- **FR-015**: 系统必须提供数据库迁移脚本以删除 LLM 服务相关表

**文档更新**
- **FR-016**: API 文档必须自动更新，不再显示已移除的接口

### 受影响的代码清单

基于代码分析，需要移除的文件包括：

**Interface 层**:
- `LlmServiceController.java`
- `DeleteLlmServiceRequest.java`
- `GetLlmServiceRequest.java`
- `QueryLlmServicesRequest.java`
- `SetDefaultLlmServiceRequest.java`

**Application 层**:
- `LlmServiceApplicationService.java`（接口）
- `LlmServiceApplicationServiceImpl.java`（实现）
- `CreateLlmServiceCommand.java`
- `UpdateLlmServiceCommand.java`
- `LlmServiceDTO.java`

**Domain 层**:
- `LlmServiceDomainService.java`（接口）
- `LlmServiceDomainServiceImpl.java`（实现）
- `LlmService.java`（领域模型）

**Repository 层**:
- `LlmServiceRepository.java`（接口）
- `LlmServiceEntity.java`
- `LlmServiceRepositoryImpl.java`（实现）
- `LlmServiceMapper.java`
- `LlmServicePO.java`

**Common 层**:
- `LlmServiceErrorCode.java`

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的 LLM 服务相关 API 端点已不可访问（返回 404）
- **SC-002**: 代码库中无任何 LlmService 相关的类文件
- **SC-003**: 项目编译成功，无编译错误
- **SC-004**: 所有现有单元测试和集成测试通过率 100%
- **SC-005**: Swagger 文档中不显示 LLM 服务管理相关接口
- **SC-006**: 系统能够正常启动并处理其他业务请求

## Assumptions

1. LLM 服务管理功能当前没有被其他模块依赖（如果有依赖，需要先处理依赖关系）
2. 数据库中已存在的 LLM 服务配置数据可以直接删除（无需迁移或备份）
3. 不需要保留向后兼容性，可以直接移除而无需废弃过渡期
4. 移除操作通过数据库迁移脚本执行，保留迁移历史记录

## Out of Scope

- LLM 功能的替代方案设计
- 历史数据的迁移或归档
- 客户端代码的更新（由调用方自行处理）
