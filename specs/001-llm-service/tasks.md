# Tasks: LLM 服务配置管理

**Input**: Design documents from `/specs/001-llm-service/`
**Prerequisites**: design.md (required), spec.md (required), contracts/llm-service-api.yaml

**Tests**: 未明确要求测试任务，本任务列表聚焦于实现任务。

**Organization**: 任务按用户故事组织，支持独立实现和测试每个故事。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行执行（不同文件，无依赖）
- **[Story]**: 任务所属用户故事（US1, US2, US3）
- 描述中包含具体文件路径

## 项目结构

基于现有 DDD 分层架构：

```text
domain/domain-model/src/main/java/.../domain/model/llm/           # 领域模型
domain/domain-api/src/main/java/.../domain/service/llm/           # 领域服务接口
domain/domain-impl/src/main/java/.../domain/impl/service/llm/     # 领域服务实现
domain/repository-api/src/main/java/.../repository/llm/           # 仓储接口
infrastructure/repository/mysql-impl/src/main/java/.../mysql/     # MySQL 实现
application/application-api/src/main/java/.../application/api/    # 应用服务接口
application/application-impl/src/main/java/.../application/impl/  # 应用服务实现
interface/interface-http/src/main/java/.../interface_/http/       # HTTP 控制器
bootstrap/src/main/resources/db/migration/                        # 数据库迁移
```

---

## Phase 1: Setup (基础设施准备)

**Purpose**: 项目基础配置和数据库准备

- [x] T001 创建数据库迁移脚本 `bootstrap/src/main/resources/db/migration/V7__Create_llm_service_table.sql`
  - 创建 `llm_service_config` 表
  - 包含所有设计文档定义的字段：id, name, description, provider_type, endpoint, model_parameters (JSON), priority, enabled, is_default, created_at, updated_at
  - 创建 name 唯一索引
  - **验证方法**: 【构建验证】执行 `mvn compile -pl bootstrap`，确认迁移脚本语法正确

- [x] T002 [P] 创建 LLM 服务错误码枚举 `common/src/main/java/com/catface996/aiops/common/enums/LlmServiceErrorCode.java`
  - 定义错误码：LLM_SERVICE_NOT_FOUND, LLM_SERVICE_NAME_DUPLICATE, LLM_SERVICE_IN_USE, LLM_SERVICE_CANNOT_DISABLE, LLM_SERVICE_MUST_ENABLED, LLM_SERVICE_INVALID_PARAMS
  - 遵循现有 ErrorCode 接口规范
  - **验证方法**: 【构建验证】执行 `mvn compile -pl common`

---

## Phase 2: Foundational (基础层 - 阻塞性前置条件)

**Purpose**: 领域层和仓储层核心组件，所有用户故事依赖此阶段

**⚠️ CRITICAL**: 此阶段必须完成后才能开始用户故事实现

### 领域模型

- [x] T003 [P] 创建 ProviderType 枚举 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/ProviderType.java`
  - 枚举值：OPENAI, CLAUDE, LOCAL, CUSTOM
  - 包含描述字段
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-model`

- [x] T004 [P] 创建 ModelParameters 值对象 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/ModelParameters.java`
  - 属性：modelName (必填), temperature, maxTokens, topP, frequencyPenalty, presencePenalty
  - 包含参数验证逻辑（温度 0-2, maxTokens 1-128000 等）
  - 使用 @Builder 模式
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-model`

- [x] T005 创建 LlmService 领域模型 `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/LlmService.java`
  - 属性：id, name, description, providerType, endpoint, modelParameters, priority, enabled, isDefault, createdAt, updatedAt
  - 封装业务规则验证方法
  - 依赖 T003, T004
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-model`

### 仓储层

- [x] T006 [P] 创建 LlmServiceEntity `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/entity/LlmServiceEntity.java`
  - 持久化实体，与数据库表对应
  - model_parameters 使用 String 存储 JSON
  - 使用 MyBatis-Plus 注解
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/repository-api`

- [x] T007 创建 LlmServiceRepository 接口 `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/LlmServiceRepository.java`
  - 方法：findById, findAll, findByEnabled, save, deleteById, findByName, clearAllDefault, setDefault
  - 依赖 T006
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/repository-api`

- [x] T008 [P] 创建 LlmServicePO `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/llm/LlmServicePO.java`
  - MyBatis-Plus 持久化对象
  - 使用 @TableName, @TableId 等注解
  - **验证方法**: 【构建验证】执行 `mvn compile -pl infrastructure/repository/mysql-impl`

- [x] T009 创建 LlmServiceMapper `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/llm/LlmServiceMapper.java`
  - 继承 BaseMapper<LlmServicePO>
  - 自定义方法在 XML 中实现
  - 依赖 T008
  - **验证方法**: 【构建验证】执行 `mvn compile -pl infrastructure/repository/mysql-impl`

- [x] T010 [P] 创建 LlmServiceMapper XML `infrastructure/repository/mysql-impl/src/main/resources/mapper/llm/LlmServiceMapper.xml`
  - 实现条件查询：findByName, findByEnabled, clearAllDefault, setDefault
  - 遵循项目宪法要求：条件查询在 XML 中实现
  - **验证方法**: 【静态检查】验证 XML 文件存在且格式正确

- [x] T011 创建 LlmServiceRepositoryImpl `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/llm/LlmServiceRepositoryImpl.java`
  - 实现 LlmServiceRepository 接口
  - 注入 LlmServiceMapper
  - 处理 Entity 与 PO 转换
  - 依赖 T007, T009, T010
  - **验证方法**: 【构建验证】执行 `mvn compile -pl infrastructure/repository/mysql-impl`

**Checkpoint**: 基础层完成 - 用户故事实现可以开始

---

## Phase 3: User Story 1 - 添加和配置 LLM 服务 (Priority: P1) 🎯 MVP

**Goal**: 系统管理员能够添加 LLM 服务配置，包括供应商类型、端点、模型参数等

**Independent Test**: 通过 POST /api/v1/llm-services 创建服务，GET 查询列表验证服务已保存

**需求追溯**: FR-001, FR-002, FR-003, FR-004, FR-010, FR-011

### 领域服务

- [x] T012 [P] [US1] 创建 LlmServiceDomainService 接口 `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/LlmServiceDomainService.java`
  - 方法：create, update, existsByName
  - 定义领域服务契约
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-api`

- [x] T013 [US1] 创建 LlmServiceDomainServiceImpl `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/LlmServiceDomainServiceImpl.java`
  - 实现 create 方法：验证名称唯一性、创建领域对象、保存
  - 实现 existsByName 方法
  - 实现 update 方法（基础更新）
  - 依赖 T012
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-impl`
  - _Requirements: FR-001, FR-002, FR-003, FR-004_

### 应用服务

- [x] T014 [P] [US1] 创建应用层 DTO `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/`
  - CreateLlmServiceCommand.java
  - UpdateLlmServiceCommand.java
  - LlmServiceDTO.java
  - ModelParametersDTO.java
  - 包含 Jakarta Validation 注解
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-api`

- [x] T015 [P] [US1] 创建 LlmServiceApplicationService 接口 `application/application-api/src/main/java/com/catface996/aiops/application/api/service/llm/LlmServiceApplicationService.java`
  - 方法：list, getById, create, update
  - 定义应用服务契约
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-api`

- [x] T016 [US1] 创建 LlmServiceApplicationServiceImpl `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/llm/LlmServiceApplicationServiceImpl.java`
  - 实现 create 方法：调用领域服务、DTO 转换
  - 实现 list 方法：查询服务列表、按优先级排序
  - 实现 getById 方法：查询单个服务详情
  - 实现 update 方法：基础更新功能
  - 依赖 T013, T014, T015
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-impl`
  - _Requirements: FR-001, FR-002, FR-003, FR-004_

### HTTP 接口

- [x] T017 [US1] 创建 LlmServiceController `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/LlmServiceController.java`
  - POST /api/v1/llm-services - 创建服务
  - GET /api/v1/llm-services - 获取列表（支持 enabledOnly 参数）
  - GET /api/v1/llm-services/{id} - 获取详情
  - PUT /api/v1/llm-services/{id} - 更新服务
  - 使用 @PreAuthorize 进行 ADMIN 权限控制
  - 依赖 T016
  - **验证方法**: 【运行时验证】启动应用，使用 curl 测试:
    ```bash
    # 创建服务
    curl -X POST http://localhost:8080/api/v1/llm-services \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer <admin_token>" \
      -d '{"name":"GPT-4 Test","providerType":"OPENAI","modelParameters":{"modelName":"gpt-4"}}'
    # 验证返回 201 Created

    # 获取列表
    curl http://localhost:8080/api/v1/llm-services -H "Authorization: Bearer <admin_token>"
    # 验证返回创建的服务
    ```
  - _Requirements: FR-001, FR-003, FR-010, FR-011_

**Checkpoint**: User Story 1 完成，可独立测试创建和查询 LLM 服务功能

---

## Phase 4: User Story 2 - 管理 LLM 服务生命周期 (Priority: P2)

**Goal**: 系统管理员能够启用/禁用、编辑和删除 LLM 服务

**Independent Test**: 对已有服务执行状态变更和删除操作

**需求追溯**: FR-005, FR-006, FR-007

### 领域服务扩展

- [x] T018 [US2] 扩展 LlmServiceDomainService 添加生命周期方法 `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/LlmServiceDomainService.java`
  - 添加方法：updateStatus, delete
  - 依赖 T012
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-api`

- [x] T019 [US2] 实现 LlmServiceDomainServiceImpl 生命周期方法
  - 实现 updateStatus：检查是否为唯一默认服务，禁止禁用
  - 实现 delete：支持 force 参数强制删除
  - 业务规则：唯一默认服务不能禁用
  - 依赖 T018
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-impl`
  - _Requirements: FR-005, FR-007_

### 应用服务扩展

- [x] T020 [P] [US2] 创建 UpdateStatusCommand DTO `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/UpdateStatusCommand.java`
  - 属性：enabled (必填)
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-api`

- [x] T021 [US2] 扩展 LlmServiceApplicationService 添加生命周期方法
  - 添加方法：updateStatus, delete
  - 依赖 T015
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-api`

- [x] T022 [US2] 实现 LlmServiceApplicationServiceImpl 生命周期方法
  - 实现 updateStatus：调用领域服务
  - 实现 delete：调用领域服务，支持 force 参数
  - 依赖 T019, T20, T21
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-impl`
  - _Requirements: FR-005, FR-006, FR-007_

### HTTP 接口扩展

- [x] T023 [US2] 扩展 LlmServiceController 添加生命周期端点
  - PUT /api/v1/llm-services/{id}/status - 启用/禁用服务
  - DELETE /api/v1/llm-services/{id}?force=false - 删除服务
  - 处理业务异常并返回正确 HTTP 状态码
  - 依赖 T22
  - **验证方法**: 【运行时验证】启动应用，使用 curl 测试:
    ```bash
    # 禁用服务
    curl -X PUT http://localhost:8080/api/v1/llm-services/1/status \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer <admin_token>" \
      -d '{"enabled":false}'
    # 验证返回 200 OK 且 enabled=false

    # 删除服务
    curl -X DELETE "http://localhost:8080/api/v1/llm-services/1?force=true" \
      -H "Authorization: Bearer <admin_token>"
    # 验证返回 204 No Content
    ```
  - _Requirements: FR-005, FR-007_

**Checkpoint**: User Story 2 完成，可独立测试服务启用/禁用和删除功能

---

## Phase 5: User Story 3 - 设置默认服务和优先级 (Priority: P2)

**Goal**: 系统管理员能够设置默认 LLM 服务并配置服务优先级

**Independent Test**: 配置多个服务后设置默认服务，验证优先级排序

**需求追溯**: FR-008, FR-009

### 领域服务扩展

- [x] T024 [US3] 扩展 LlmServiceDomainService 添加默认服务方法
  - 添加方法：setDefault
  - 依赖 T012
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-api`

- [x] T025 [US3] 实现 LlmServiceDomainServiceImpl setDefault 方法
  - 业务规则：只有已启用的服务才能设为默认
  - 原子操作：清除其他默认标记，设置新默认
  - 使用事务保证一致性
  - 依赖 T24
  - **验证方法**: 【构建验证】执行 `mvn compile -pl domain/domain-impl`
  - _Requirements: FR-008_

### 应用服务扩展

- [x] T026 [US3] 扩展 LlmServiceApplicationService 添加 setDefault 方法
  - 依赖 T15
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-api`

- [x] T027 [US3] 实现 LlmServiceApplicationServiceImpl setDefault 方法
  - 调用领域服务设置默认
  - 返回更新后的服务 DTO
  - 依赖 T25, T26
  - **验证方法**: 【构建验证】执行 `mvn compile -pl application/application-impl`
  - _Requirements: FR-008_

### HTTP 接口扩展

- [x] T028 [US3] 扩展 LlmServiceController 添加默认服务端点
  - PUT /api/v1/llm-services/{id}/default - 设置默认服务
  - 处理业务异常（服务未启用时返回 400）
  - 依赖 T27
  - **验证方法**: 【运行时验证】启动应用，使用 curl 测试:
    ```bash
    # 设置默认服务
    curl -X PUT http://localhost:8080/api/v1/llm-services/1/default \
      -H "Authorization: Bearer <admin_token>"
    # 验证返回 200 OK 且 isDefault=true

    # 获取列表验证排序
    curl http://localhost:8080/api/v1/llm-services -H "Authorization: Bearer <admin_token>"
    # 验证列表按 priority 排序，默认服务 isDefault=true
    ```
  - _Requirements: FR-008, FR-009_

**Checkpoint**: User Story 3 完成，可独立测试默认服务和优先级功能

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 完善、日志和集成验证

- [x] T029 [P] 添加 LLM 服务操作日志 `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/LlmServiceDomainServiceImpl.java`
  - 创建服务：INFO 级别，记录 id, name, provider
  - 更新服务：INFO 级别，记录 id 和变更字段
  - 删除服务：WARN 级别，记录 id 和 force 参数
  - 状态变更：INFO 级别，记录 id 和 enabled 状态
  - 设置默认：INFO 级别，记录 id
  - **验证方法**: 【运行时验证】执行操作后检查日志输出包含预期格式

- [x] T030 [P] 验证全部 API 端点符合 OpenAPI 契约
  - 对照 `specs/001-llm-service/contracts/llm-service-api.yaml` 验证
  - 检查请求/响应格式、状态码、错误码
  - **验证方法**: 【运行时验证】使用 curl 或 Postman 测试所有 7 个端点

- [x] T031 执行完整功能验收测试
  - 创建 2-3 个 LLM 服务
  - 测试启用/禁用功能
  - 测试设置默认服务
  - 测试优先级排序
  - 测试删除功能
  - **验证方法**: 【运行时验证】完成端到端验收场景

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖 - 可立即开始
- **Foundational (Phase 2)**: 依赖 Phase 1 - 阻塞所有用户故事
- **User Story 1 (Phase 3)**: 依赖 Phase 2 - MVP 核心功能
- **User Story 2 (Phase 4)**: 依赖 Phase 2 - 可与 US1 并行（如有多人）
- **User Story 3 (Phase 5)**: 依赖 Phase 2 - 可与 US1/US2 并行
- **Polish (Phase 6)**: 依赖所有用户故事完成

### User Story Dependencies

- **User Story 1 (P1)**: 无其他故事依赖，MVP 基础
- **User Story 2 (P2)**: 与 US1 独立，可并行开发
- **User Story 3 (P2)**: 与 US1/US2 独立，可并行开发

### Within Each Phase

- 带 [P] 标记的任务可并行执行
- 领域层 → 应用层 → 接口层顺序
- 接口 → 实现顺序

### Parallel Opportunities

**Phase 2 可并行任务**:
```bash
# 领域模型可并行
Task T003: "创建 ProviderType 枚举"
Task T004: "创建 ModelParameters 值对象"

# 仓储层可并行
Task T006: "创建 LlmServiceEntity"
Task T008: "创建 LlmServicePO"
Task T010: "创建 LlmServiceMapper XML"
```

**用户故事间并行**:
```bash
# 如果多人开发，Phase 2 完成后：
Developer A: User Story 1 (T012-T017)
Developer B: User Story 2 (T018-T023)
Developer C: User Story 3 (T024-T028)
```

---

## Implementation Strategy

### MVP First (仅 User Story 1)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational（关键阻塞点）
3. 完成 Phase 3: User Story 1
4. **STOP and VALIDATE**: 独立测试创建、查询 LLM 服务功能
5. 可部署/演示 MVP

### Incremental Delivery

1. Setup + Foundational → 基础层就绪
2. 添加 User Story 1 → 独立测试 → 部署 (MVP!)
3. 添加 User Story 2 → 独立测试 → 部署
4. 添加 User Story 3 → 独立测试 → 部署
5. 每个故事独立增加价值

### Estimated Effort

| Phase | 任务数 | 预估工时 |
|-------|--------|----------|
| Setup | 2 | 1-2h |
| Foundational | 9 | 4-6h |
| User Story 1 | 6 | 3-4h |
| User Story 2 | 6 | 2-3h |
| User Story 3 | 5 | 2-3h |
| Polish | 3 | 1-2h |
| **Total** | **31** | **13-20h** |

---

## Requirements Traceability

| Requirement | Tasks | User Story |
|-------------|-------|------------|
| FR-001 | T013, T016, T017 | US1 |
| FR-002 | T013, T016 | US1 |
| FR-003 | T013, T016, T017 | US1 |
| FR-004 | T013, T016 | US1 |
| FR-005 | T019, T022, T023 | US2 |
| FR-006 | T022 | US2 |
| FR-007 | T019, T022, T023 | US2 |
| FR-008 | T025, T027, T028 | US3 |
| FR-009 | T028 | US3 |
| FR-010 | T017 | US1 |
| FR-011 | T017 | US1 |

---

## Notes

- [P] 任务 = 不同文件，无依赖，可并行
- [Story] 标签映射到具体用户故事
- 每个用户故事可独立完成和测试
- 完成每个任务后提交代码
- 在任何 Checkpoint 停下来验证功能
- 避免：模糊任务、同文件冲突、破坏独立性的跨故事依赖
