# Implementation Plan: 移除认证相关功能

**Branch**: `002-remove-auth-features` | **Date**: 2025-12-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-remove-auth-features/spec.md`

## Summary

移除项目中已迁移到 op-stack-auth 服务的认证相关代码，包括：
- RefreshTokenResponse DTO 文件
- AuthErrorCode、SessionErrorCode 错误码枚举
- 空的 auth 目录（仅含 .gitkeep）
- 更新 OpenApiConfig 中的 Swagger 文档描述
- 更新异常类中引用 AuthErrorCode 的示例注释

本次清理为纯删除和更新操作，不涉及新功能开发。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, SpringDoc OpenAPI
**Storage**: N/A（本次清理不涉及数据库）
**Testing**: Maven Compile/Package
**Target Platform**: Linux/macOS Server
**Project Type**: DDD 分层架构的 Web 服务
**Performance Goals**: N/A（清理任务）
**Constraints**: 确保编译通过、Swagger 文档正确更新
**Scale/Scope**: 删除 3 个 Java 文件，5 个空目录，更新 4 个文件

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 检查项 | 状态 |
|------|--------|------|
| I. DDD Architecture | 仅清理 interface/common/application 层代码，不影响架构 | ✅ 通过 |
| II. API URL Convention | N/A（不涉及 API 路径变更） | ✅ 通过 |
| III. POST-Only API | N/A（不涉及 API 变更） | ✅ 通过 |
| IV. Database Migration | 无数据库变更 | ✅ 通过 |
| V. Technology Stack | 无技术栈变更 | ✅ 通过 |

**结论**: 所有宪法原则检查通过，可以继续执行。

## Project Structure

### Documentation (this feature)

```text
specs/002-remove-auth-features/
├── spec.md              # 功能规格说明
├── plan.md              # 本文件 - 实施计划
├── research.md          # 依赖关系分析
├── checklists/
│   └── requirements.md  # 规格质量检查清单
└── quickstart.md        # 快速验证指南
```

### Source Code (待删除的文件)

```text
# 需要删除的文件
interface/interface-http/src/main/java/.../dto/auth/RefreshTokenResponse.java
common/src/main/java/.../enums/AuthErrorCode.java
common/src/main/java/.../enums/SessionErrorCode.java

# 需要删除的目录（仅含 .gitkeep）
interface/interface-http/src/main/java/.../controller/auth/
interface/interface-http/src/main/java/.../dto/auth/
application/application-api/src/main/java/.../service/auth/
application/application-api/src/main/java/.../command/auth/
application/application-impl/src/main/java/.../service/auth/

# 需要更新的文件
bootstrap/src/main/java/.../config/OpenApiConfig.java         # Swagger 文档
common/src/main/java/.../exception/BusinessException.java     # 示例注释
common/src/main/java/.../exception/BaseException.java         # 示例注释
common/src/main/java/.../enums/ErrorCode.java                 # 示例注释
```

**Structure Decision**: 本次为代码清理任务，不改变现有项目结构。

## Complexity Tracking

> 无宪法违规，本节为空

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Implementation Phases

### Phase 1: 更新注释文件

**目标**: 更新引用 AuthErrorCode 的示例注释

**步骤**:
1. 更新 BusinessException.java 中的示例注释
2. 更新 BaseException.java 中的示例注释
3. 更新 ErrorCode.java 中的示例注释
4. 验证编译通过

### Phase 2: 删除认证相关文件

**目标**: 删除废弃的认证相关 Java 文件

**步骤**:
1. 删除 RefreshTokenResponse.java
2. 删除 AuthErrorCode.java
3. 删除 SessionErrorCode.java
4. 验证编译通过

### Phase 3: 删除空目录

**目标**: 删除仅含 .gitkeep 的空 auth 目录

**步骤**:
1. 删除 interface/.../controller/auth/ 目录
2. 删除 interface/.../dto/auth/ 目录
3. 删除 application-api/.../service/auth/ 目录
4. 删除 application-api/.../command/auth/ 目录
5. 删除 application-impl/.../service/auth/ 目录

### Phase 4: 更新 OpenApiConfig

**目标**: 更新 Swagger 文档描述

**步骤**:
1. 移除"用户与认证"功能模块描述
2. 更新认证方式说明
3. 保留资源管理相关功能描述

### Phase 5: 验证与清理

**目标**: 确保系统正常运行

**步骤**:
1. 运行 `mvn clean compile` 验证编译
2. 运行 `mvn clean package -DskipTests` 验证打包
3. 启动应用验证 Swagger 文档
4. 确认不再显示认证相关功能描述

## Risk Assessment

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|---------|
| 编译错误 | 低 | 中 | 先更新注释再删除文件 |
| 遗漏引用 | 低 | 低 | grep 全面搜索确认 |
| Swagger 异常 | 低 | 低 | 启动应用验证 |

## Verification Checklist

- [ ] 注释文件已更新（BusinessException、BaseException、ErrorCode）
- [ ] RefreshTokenResponse.java 已删除
- [ ] AuthErrorCode.java 已删除
- [ ] SessionErrorCode.java 已删除
- [ ] 所有空 auth 目录已删除
- [ ] OpenApiConfig.java 已更新
- [ ] 编译成功（无错误）
- [ ] Swagger 不显示认证相关功能描述
