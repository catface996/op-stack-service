# Tasks: 移除LLM服务管理功能

**Input**: Design documents from `/specs/001-remove-llm-service/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 本功能为移除操作，不新增测试，但需验证现有测试通过。

**Organization**: 任务按删除的依赖顺序组织，确保每步删除后编译仍能通过。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

本项目为 DDD 多模块架构：
- `interface/interface-http/src/main/java/` - Interface 层
- `application/application-api/src/main/java/` - Application API 层
- `application/application-impl/src/main/java/` - Application 实现层
- `domain/domain-api/src/main/java/` - Domain API 层
- `domain/domain-impl/src/main/java/` - Domain 实现层
- `domain/domain-model/src/main/java/` - Domain 模型层
- `domain/repository-api/src/main/java/` - Repository API 层
- `infrastructure/repository/mysql-impl/src/main/java/` - Repository 实现层
- `common/src/main/java/` - Common 层
- `bootstrap/src/main/resources/db/migration/` - 数据库迁移

---

## Phase 1: 依赖检查

**Purpose**: 确认无外部依赖，可以安全删除

- [ ] T001 执行全量代码搜索确认无外部依赖: `grep -r "LlmService\|llm_service" --include="*.java" --include="*.xml" --include="*.yml" .`
- [ ] T002 检查 Spring 配置文件中是否有 LLM 相关配置: `bootstrap/src/main/resources/application*.yml`

**Checkpoint**: 确认无外部依赖，可以开始删除

---

## Phase 2: User Story 1 - 系统管理员移除无用模块 (Priority: P1) 🎯 MVP

**Goal**: 移除所有 LLM 服务管理相关的 HTTP 接口和 Swagger 文档

**Independent Test**: 访问 `/api/v1/llm-services/*` 任意端点返回 404

### 删除 Interface 层代码

- [ ] T003 [P] [US1] 删除控制器: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/LlmServiceController.java`
- [ ] T004 [P] [US1] 删除请求对象: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/DeleteLlmServiceRequest.java`
- [ ] T005 [P] [US1] 删除请求对象: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/GetLlmServiceRequest.java`
- [ ] T006 [P] [US1] 删除请求对象: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/QueryLlmServicesRequest.java`
- [ ] T007 [P] [US1] 删除请求对象: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/SetDefaultLlmServiceRequest.java`
- [ ] T008 [US1] 删除请求对象目录（如已空）: `interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/`

### 删除 Application 层代码

- [ ] T009 [P] [US1] 删除应用服务实现: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/llm/LlmServiceApplicationServiceImpl.java`
- [ ] T010 [P] [US1] 删除应用服务接口: `application/application-api/src/main/java/com/catface996/aiops/application/api/service/llm/LlmServiceApplicationService.java`
- [ ] T011 [P] [US1] 删除 DTO: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/CreateLlmServiceCommand.java`
- [ ] T012 [P] [US1] 删除 DTO: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/UpdateLlmServiceCommand.java`
- [ ] T013 [P] [US1] 删除 DTO: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/LlmServiceDTO.java`
- [ ] T014 [US1] 删除 DTO 目录（如已空）: `application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/`
- [ ] T015 [US1] 删除服务目录（如已空）: `application/application-api/src/main/java/com/catface996/aiops/application/api/service/llm/`
- [ ] T016 [US1] 删除实现目录（如已空）: `application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/llm/`

### 删除 Domain 层代码

- [ ] T017 [P] [US1] 删除领域服务实现: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/LlmServiceDomainServiceImpl.java`
- [ ] T018 [P] [US1] 删除领域服务接口: `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/LlmServiceDomainService.java`
- [ ] T019 [P] [US1] 删除领域模型: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/LlmService.java`
- [ ] T020 [US1] 删除领域服务接口目录（如已空）: `domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/`
- [ ] T021 [US1] 删除领域服务实现目录（如已空）: `domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/`
- [ ] T022 [US1] 删除领域模型目录（如已空）: `domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/`

### 删除 Repository 层代码

- [ ] T023 [P] [US1] 删除仓储实现: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/llm/LlmServiceRepositoryImpl.java`
- [ ] T024 [P] [US1] 删除 Mapper 接口: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/llm/LlmServiceMapper.java`
- [ ] T025 [P] [US1] 删除 PO: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/llm/LlmServicePO.java`
- [ ] T026 [P] [US1] 删除 XML Mapper: `infrastructure/repository/mysql-impl/src/main/resources/mapper/llm/LlmServiceMapper.xml`
- [ ] T027 [P] [US1] 删除仓储接口: `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/LlmServiceRepository.java`
- [ ] T028 [P] [US1] 删除仓储实体: `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/entity/LlmServiceEntity.java`
- [ ] T029 [US1] 删除仓储实现目录（如已空）: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/llm/`
- [ ] T030 [US1] 删除 Mapper 目录（如已空）: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/llm/`
- [ ] T031 [US1] 删除 PO 目录（如已空）: `infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/llm/`
- [ ] T032 [US1] 删除 XML Mapper 目录（如已空）: `infrastructure/repository/mysql-impl/src/main/resources/mapper/llm/`
- [ ] T033 [US1] 删除仓储接口目录（如已空）: `domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/`

### 删除 Common 层代码

- [ ] T034 [US1] 删除错误码枚举: `common/src/main/java/com/catface996/aiops/common/enums/LlmServiceErrorCode.java`

### 创建数据库迁移脚本

- [ ] T035 [US1] 创建 Flyway 迁移脚本删除表: `bootstrap/src/main/resources/db/migration/V9__Drop_llm_service_table.sql`

**Checkpoint**: User Story 1 完成 - LLM 服务接口已移除，Swagger 文档自动更新

---

## Phase 3: User Story 2 - 开发人员代码清理 (Priority: P2)

**Goal**: 确保代码库整洁，编译和测试通过

**Independent Test**: `mvn clean compile` 成功，`mvn test` 全部通过，`grep -r "LlmService" --include="*.java" .` 返回空

### 验证任务

- [ ] T036 [US2] 执行项目编译验证: `mvn clean compile`
- [ ] T037 [US2] 执行所有单元测试: `mvn test`
- [ ] T038 [US2] 验证代码库无残留引用: `grep -r "LlmService\|llm_service" --include="*.java" --include="*.xml" .`

**Checkpoint**: User Story 2 完成 - 代码库清洁，无编译错误

---

## Phase 4: Polish & 最终验证

**Purpose**: 端到端验证和文档更新

- [ ] T039 启动应用验证正常运行: `java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local`
- [ ] T040 验证 LLM 服务接口返回 404: `curl -X POST http://localhost:8080/api/v1/llm-services/query -H "Content-Type: application/json" -d "{}"`
- [ ] T041 验证 Swagger 文档无 LLM 服务相关内容: 访问 `http://localhost:8080/swagger-ui/index.html`
- [ ] T042 运行 quickstart.md 验证清单

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (依赖检查)**: 无依赖 - 可立即开始
- **Phase 2 (US1 - 移除代码)**: 依赖 Phase 1 完成
- **Phase 3 (US2 - 验证清理)**: 依赖 Phase 2 完成
- **Phase 4 (最终验证)**: 依赖 Phase 3 完成

### User Story Dependencies

- **User Story 1 (P1)**: 无外部依赖 - 核心删除操作
- **User Story 2 (P2)**: 依赖 User Story 1 完成 - 验证性质

### Within User Story 1 (删除顺序)

**重要**: 必须按依赖关系逆序删除，确保每步删除后编译通过

1. Interface 层（无被依赖）→ 先删除
2. Application 层（仅被 Interface 依赖）→ 次删除
3. Domain 层（被 Application 依赖）→ 再删除
4. Repository 层（被 Domain 依赖）→ 然后删除
5. Common 层（被多层依赖）→ 最后删除
6. Database Migration → 最终执行

### Parallel Opportunities

```text
# Interface 层可并行删除 (T003-T007)
# Application 层 DTO 可并行删除 (T011-T013)
# Domain 层可并行删除 (T017-T019)
# Repository 层可并行删除 (T023-T028)
```

---

## Parallel Example: User Story 1 Interface 层

```bash
# 启动所有 Interface 层删除任务:
Task: "删除控制器: interface/interface-http/.../LlmServiceController.java"
Task: "删除请求对象: interface/interface-http/.../DeleteLlmServiceRequest.java"
Task: "删除请求对象: interface/interface-http/.../GetLlmServiceRequest.java"
Task: "删除请求对象: interface/interface-http/.../QueryLlmServicesRequest.java"
Task: "删除请求对象: interface/interface-http/.../SetDefaultLlmServiceRequest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: 依赖检查
2. 完成 Phase 2: User Story 1 - 移除代码
3. **STOP and VALIDATE**: 编译通过，接口返回 404
4. 如需继续，进入 Phase 3

### Incremental Delivery

1. Phase 1 → 确认无依赖
2. Phase 2 (US1) → 移除代码 → 编译验证 → Swagger 验证
3. Phase 3 (US2) → 测试验证 → 代码搜索验证
4. Phase 4 → 端到端验证 → 完成

---

## Notes

- [P] 任务 = 不同文件，无依赖，可并行
- [Story] 标签映射任务到特定用户故事
- 每步删除后建议执行 `mvn compile` 确认
- 使用 `rm -rf` 删除目录时确保目录为空
- 提交时按逻辑分组（如：按层级提交）
