# Implementation Plan: POST-Only API 重构

**Branch**: `024-post-only-api` | **Date**: 2025-12-22 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/024-post-only-api/spec.md`

## Summary

将所有业务 HTTP 接口从多种方法（GET/PUT/DELETE/PATCH）统一为 POST 方法，便于上游网关统一注入参数（tenantId, traceId 等）。涉及约 34 个接口的重构，保持响应格式和认证机制不变。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web MVC, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (通过 MyBatis-Plus)
**Testing**: JUnit 5, Mockito, Spring Test
**Target Platform**: Linux server (Docker/K8s)
**Project Type**: DDD 分层架构的后端服务
**Performance Goals**: 无性能变更要求，保持现有性能水平
**Constraints**: 接口功能必须等价，响应格式不变
**Scale/Scope**: 34 个接口需要重构，涉及 8 个 Controller 类

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| 代码风格一致性 | ✅ Pass | 遵循现有 Java 21 标准约定 |
| 测试覆盖 | ✅ Pass | 现有单元测试需更新，功能等价性验证 |
| API 文档 | ✅ Pass | SpringDoc OpenAPI 自动更新 |
| 向后兼容 | ⚠️ N/A | 明确不提供向后兼容层（见 Assumptions） |

## Project Structure

### Documentation (this feature)

```text
specs/024-post-only-api/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (minimal - no data model changes)
├── quickstart.md        # Phase 1 output
└── contracts/           # Phase 1 output - OpenAPI schemas
```

### Source Code (repository root)

```text
# DDD 分层架构（现有结构，本次重构仅涉及 interface 层）
interface/
├── interface-http/
│   └── src/main/java/.../controller/
│       ├── ResourceController.java      # 14 endpoints (12 need refactor)
│       ├── RelationshipController.java  # 8 endpoints (7 need refactor)
│       ├── SessionController.java       # 5 endpoints (3 need refactor)
│       ├── LlmServiceController.java    # 7 endpoints (6 need refactor)
│       ├── AdminController.java         # 2 endpoints (1 needs refactor)
│       ├── AuthController.java          # 4 endpoints (no change - already POST)
│       └── HealthController.java        # 1 endpoint (no change - keep GET)

# 请求体 DTO 需要新增/修改
interface/
├── interface-http/
│   └── src/main/java/.../request/
│       ├── resource/                    # 资源相关请求体
│       ├── relationship/                # 关系相关请求体
│       ├── session/                     # 会话相关请求体
│       ├── llm/                         # LLM 服务相关请求体
│       └── admin/                       # 管理相关请求体

# 测试更新
domain/domain-impl/src/test/java/
bootstrap/src/test/java/
```

**Structure Decision**: 保持现有 DDD 分层架构，仅修改 interface-http 模块中的 Controller 和 Request DTO。

## Complexity Tracking

> **无复杂性违规** - 这是一个纯技术重构，不引入新的架构模式或依赖。

## Implementation Phases

### Phase 0: Research
- 分析现有接口结构和参数模式
- 确定请求体命名约定
- 识别共享请求体复用机会

### Phase 1: Design & Contracts
- 定义新的请求体 DTO 结构
- 生成 OpenAPI 契约
- 创建迁移映射文档

### Phase 2: Implementation (via /speckit.tasks)
- 按 Controller 逐个重构
- 更新单元测试
- 验证功能等价性
