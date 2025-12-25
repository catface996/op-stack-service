# Implementation Plan: 资源管理接口统一改为POST方式

**Branch**: `001-resource-post-api` | **Date**: 2025-12-25 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-resource-post-api/spec.md`

## Summary

将资源管理相关的8个HTTP接口统一改为POST方式，采用动词后缀URL路径（如 `/create`, `/list`, `/detail`），原路径参数和查询参数移至请求体中。这是一个纯接口层重构，不涉及业务逻辑变更。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web MVC, OpenAPI 3 (springdoc)
**Storage**: N/A (本次变更不涉及存储层)
**Testing**: JUnit 5 + Spring Boot Test
**Target Platform**: Linux Server (Spring Boot 应用)
**Project Type**: DDD 多模块 Web 应用
**Performance Goals**: N/A (接口重构，保持原有性能)
**Constraints**: 所有接口统一使用 POST 方法，参数通过 RequestBody 传递
**Scale/Scope**: 8个接口改造，3个新增请求类，3个现有请求类修改

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 状态 | 说明 |
|------|------|------|
| I. 渐进式开发 | ✅ PASS | 按阶段执行：需求 → 设计 → 实现 → 验证 |
| II. DDD 分层架构 | ✅ PASS | 仅修改接口层 (Interface)，不涉及其他层 |
| III. 持续编译验证 | ✅ PASS | 每个任务完成后执行 mvn clean compile |
| IV. 中文优先 | ✅ PASS | 所有文档和注释使用中文 |
| V. 依赖版本统一管理 | ✅ PASS | 无新增依赖 |
| VI. Entity/PO 分离 | ✅ N/A | 本次变更不涉及实体层 |
| VII. MyBatis-Plus 规范 | ✅ N/A | 本次变更不涉及数据操作 |
| VIII. ADR 架构决策记录 | ✅ PASS | 无重大架构决策，仅接口重构 |

**结论**: 所有适用的宪法原则均通过检查，可以继续进行。

## Project Structure

### Documentation (this feature)

```text
specs/001-resource-post-api/
├── spec.md              # 功能规格说明书
├── plan.md              # 本文件 - 实现计划
├── research.md          # Phase 0 - 研究文档
├── data-model.md        # Phase 1 - 数据模型
├── quickstart.md        # Phase 1 - 快速开始指南
├── contracts/           # Phase 1 - API 契约
│   └── openapi.yaml     # OpenAPI 3.0 规范
└── tasks.md             # Phase 2 - 任务列表 (/speckit.tasks 生成)
```

### Source Code (repository root)

```text
# DDD 多模块结构 (本次变更涉及的文件)
interface/
└── interface-http/
    └── src/main/java/com/catface996/aiops/interface_/http/
        └── controller/
            └── ResourceController.java    # 主要修改文件

application/
└── application-api/
    └── src/main/java/com/catface996/aiops/application/api/dto/resource/
        └── request/
            ├── CreateResourceRequest.java       # 已存在，无需修改
            ├── ListResourcesRequest.java        # 已存在，无需修改
            ├── UpdateResourceRequest.java       # 需添加 id 字段
            ├── DeleteResourceRequest.java       # 需添加 id 字段
            ├── UpdateResourceStatusRequest.java # 需添加 id 字段
            ├── GetResourceRequest.java          # 新增
            └── GetResourceAuditLogsRequest.java # 新增
```

**Structure Decision**: 遵循现有 DDD 分层架构，所有变更集中在接口层 (interface-http) 和应用层 API (application-api) 的 DTO 请求类。

## Complexity Tracking

> 无宪法违规，无需记录复杂度权衡。
