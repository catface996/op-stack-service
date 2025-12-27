# Implementation Plan: 移除废弃的API接口

**Branch**: `001-remove-deprecated-api` | **Date**: 2025-12-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-remove-deprecated-api/spec.md`

## Summary

移除项目中所有标记为 `@Deprecated(forRemoval = true)` 的废弃代码，包括：
- ResourceController 中的 6 个废弃成员管理接口
- ErrorCodes 常量类
- 专用于废弃接口的 Request/Response 类

本次清理为纯删除操作，不涉及新功能开发，替代功能已在 TopologyController 中实现。

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (通过 Flyway 迁移)
**Testing**: JUnit 5, Maven Surefire
**Target Platform**: Linux/macOS Server
**Project Type**: DDD 分层架构的 Web 服务
**Performance Goals**: N/A（清理任务）
**Constraints**: 确保编译通过、测试通过、API 文档正确更新
**Scale/Scope**: 移除约 200 行 Controller 代码，10 个类文件

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 检查项 | 状态 |
|------|--------|------|
| I. DDD Architecture | 仅移除 interface 层代码，不影响架构 | ✅ 通过 |
| II. API URL Convention | 移除的是旧路径，保留的接口符合新规范 | ✅ 通过 |
| III. POST-Only API | N/A（移除操作） | ✅ 通过 |
| IV. Database Migration | 无数据库变更 | ✅ 通过 |
| V. Technology Stack | 无技术栈变更 | ✅ 通过 |

**结论**: 所有宪法原则检查通过，可以继续执行。

## Project Structure

### Documentation (this feature)

```text
specs/001-remove-deprecated-api/
├── spec.md              # 功能规格说明
├── plan.md              # 本文件 - 实施计划
├── research.md          # 依赖关系分析
├── checklists/
│   └── requirements.md  # 规格质量检查清单
└── tasks.md             # Phase 2 输出 (/speckit.tasks)
```

### Source Code (待移除的文件)

```text
# 需要修改的文件
interface/interface-http/src/main/java/.../controller/ResourceController.java
  - 移除 6 个废弃方法（L296-479）
  - 移除相关 import
  - 更新类注释

# 需要删除的文件
common/src/main/java/.../constants/ErrorCodes.java

interface/interface-http/src/main/java/.../request/subgraph/
├── AddMembersRequest.java
└── RemoveMembersRequest.java

interface/interface-http/src/main/java/.../request/resource/
├── QueryMembersRequest.java
├── QueryMembersWithRelationsRequest.java
├── QueryTopologyRequest.java
└── QueryAncestorsRequest.java

interface/interface-http/src/main/java/.../response/subgraph/
├── SubgraphMemberListResponse.java
├── SubgraphMembersWithRelationsResponse.java
├── TopologyGraphResponse.java
└── SubgraphAncestorsResponse.java
```

### 保留的代码（不应移除）

```text
# 被活跃代码使用，需要保留
application/application-api/src/main/java/.../service/subgraph/SubgraphMemberApplicationService.java
application/application-impl/src/main/java/.../service/subgraph/SubgraphMemberApplicationServiceImpl.java
application/application-api/src/main/java/.../dto/subgraph/*  # DTO 类
```

**Structure Decision**: 本次为代码清理任务，不改变现有项目结构。

## Complexity Tracking

> 无宪法违规，本节为空

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |

## Implementation Phases

### Phase 1: 移除 ResourceController 废弃方法

**目标**: 从 ResourceController 中移除所有 `@Deprecated` 方法

**步骤**:
1. 移除 L296-479 的 6 个废弃方法
2. 移除不再需要的 import 语句
3. 更新类级别 Javadoc，移除废弃接口的文档说明
4. 验证编译通过

### Phase 2: 移除废弃的 Request/Response 类

**目标**: 删除专用于废弃接口的类文件

**步骤**:
1. 删除 `request/subgraph/` 目录及其内容
2. 删除 `request/resource/` 目录下的废弃请求类
3. 删除 `response/subgraph/` 目录及其内容
4. 验证编译通过

### Phase 3: 移除 ErrorCodes 常量类

**目标**: 删除已废弃的错误码常量类

**步骤**:
1. 删除 ErrorCodes.java 文件
2. 验证编译通过（注释引用不影响编译）

### Phase 4: 验证与清理

**目标**: 确保系统正常运行

**步骤**:
1. 运行 `mvn clean package -DskipTests` 验证编译
2. 运行 `mvn test` 验证测试
3. 启动应用验证 Swagger 文档
4. 验证接口数量从 51 减少到 45

## Risk Assessment

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|---------|
| 编译错误 | 低 | 中 | 按依赖顺序移除 |
| 测试失败 | 低 | 低 | 同步移除废弃测试 |
| 运行时错误 | 极低 | 中 | 删除前验证无其他引用 |

## Verification Checklist

- [ ] ResourceController 废弃方法已移除
- [ ] 废弃 Request/Response 类已删除
- [ ] ErrorCodes.java 已删除
- [ ] 编译成功（无错误）
- [ ] 测试通过
- [ ] Swagger 不显示废弃接口
- [ ] API 接口数量为 45 个
