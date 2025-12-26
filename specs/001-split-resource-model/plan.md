# Implementation Plan: Resource 模型分离重构

**Branch**: `001-split-resource-model` | **Date**: 2025-12-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-split-resource-model/spec.md`

## Summary

将现有的 `resource` 表拆分为独立的 `topology` 表和 `node` 表，实现拓扑图和资源节点的数据模型分离。主要变更包括：
1. 创建新的 `topology` 表存储拓扑图数据
2. 创建新的 `node` 表存储资源节点数据
3. 将 `resource_type` 表重命名为 `node_type`，移除 SUBGRAPH 类型
4. 更新 `subgraph_member` 表的外键关联
5. 提供数据迁移脚本和回滚方案
6. API 路径从 `/api/v1/resources/*` 变更为 `/api/v1/nodes/*`，保留旧路径的向后兼容

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Spring Boot Test, MockMvc
**Target Platform**: Linux server (Docker container)
**Project Type**: DDD multi-module architecture
**Performance Goals**: API 响应时间 ≤ 重构前的 1.1 倍
**Constraints**: 迁移停机时间 < 5 分钟，旧 API 路径保留 3 个月
**Scale/Scope**: 现有数据量：约 40 条资源记录（6 个拓扑图 + 33 个节点）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| DDD 分层架构 | ✅ PASS | 遵循现有 domain-api/domain-impl/repository-api 模式 |
| POST-Only API | ✅ PASS | 所有新 API 继续使用 POST 方法 |
| Flyway 迁移 | ✅ PASS | 使用 V12__ 前缀的迁移脚本 |
| 乐观锁 | ✅ PASS | topology 和 node 表保留 version 字段 |
| 审计日志 | ✅ PASS | 复用现有 AuditLogService |

## Project Structure

### Documentation (this feature)

```text
specs/001-split-resource-model/
├── spec.md              # 功能规格说明
├── plan.md              # 本文件（实施计划）
├── research.md          # Phase 0 研究产出
├── data-model.md        # Phase 1 数据模型设计
├── quickstart.md        # Phase 1 快速开始指南
├── contracts/           # Phase 1 API 契约定义
│   ├── topology-api.yaml
│   └── node-api.yaml
├── checklists/          # 检查清单
│   └── requirements.md
└── tasks.md             # Phase 2 任务清单（/speckit.tasks 生成）
```

### Source Code (repository root)

```text
# DDD 多模块架构
domain/
├── domain-api/          # 领域服务接口
│   └── src/main/java/.../domain/service/
│       ├── topology/TopologyDomainService.java    # 更新
│       └── node/NodeDomainService.java            # 新建
├── domain-impl/         # 领域服务实现
│   └── src/main/java/.../domain/impl/service/
│       ├── topology/TopologyDomainServiceImpl.java # 更新
│       └── node/NodeDomainServiceImpl.java         # 新建
├── domain-model/        # 领域模型
│   └── src/main/java/.../domain/model/
│       ├── topology/Topology.java                  # 新建
│       └── node/Node.java                          # 更新现有
└── repository-api/      # 仓储接口
    └── src/main/java/.../repository/
        ├── topology/TopologyRepository.java        # 新建
        └── node/NodeRepository.java                # 更新

infrastructure/
└── repository/mysql-impl/
    └── src/main/java/.../repository/mysql/
        ├── mapper/
        │   ├── topology/TopologyMapper.java        # 新建
        │   └── node/NodeMapper.java                # 更新
        ├── po/
        │   ├── topology/TopologyPO.java            # 新建
        │   └── node/NodePO.java                    # 更新
        └── impl/
            ├── topology/TopologyRepositoryImpl.java # 新建
            └── node/NodeRepositoryImpl.java         # 更新

application/
├── application-api/     # 应用服务接口和 DTO
│   └── src/main/java/.../application/api/
│       ├── dto/
│       │   ├── topology/                           # 更新
│       │   └── node/                               # 新建
│       └── service/
│           ├── topology/TopologyApplicationService.java # 更新
│           └── node/NodeApplicationService.java    # 新建
└── application-impl/    # 应用服务实现
    └── src/main/java/.../application/impl/service/
        ├── topology/TopologyApplicationServiceImpl.java # 更新
        └── node/NodeApplicationServiceImpl.java    # 新建

interface/
└── interface-http/      # HTTP 接口层
    └── src/main/java/.../interface_/http/
        ├── controller/
        │   ├── TopologyController.java             # 更新
        │   ├── NodeController.java                 # 新建
        │   └── ResourceController.java             # @Deprecated
        └── request/
            ├── topology/                           # 更新
            └── node/                               # 新建

bootstrap/
└── src/main/resources/db/migration/
    ├── V12__Split_resource_to_topology_and_node.sql  # 新建
    └── V12_1__Rollback_split_resource.sql            # 新建（回滚脚本）
```

**Structure Decision**: 遵循现有 DDD 多模块架构，在各层中新增 topology 和 node 相关包，复用现有基础设施代码（MyBatis-Plus、审计日志等）。

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| 无 | - | - |

本次重构不引入新的复杂性，是对现有架构的规范化改进。
