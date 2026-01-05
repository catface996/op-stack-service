<!--
Sync Impact Report
==================
Version change: 1.4.0 → 1.5.0 (MINOR: Added Async Implementation Standards)

Modified principles: None

Added sections:
- X. Async Implementation Standards (新增异步实现规范)

Removed sections: None

Templates requiring updates:
- /.specify/templates/plan-template.md: ✅ No updates required (generic)
- /.specify/templates/spec-template.md: ✅ No updates required (generic)
- /.specify/templates/tasks-template.md: ✅ No updates required (generic)

Follow-up TODOs: None
-->

# OP-Stack Service Constitution

## Core Principles

### I. DDD Architecture

本项目采用领域驱动设计（DDD）分层架构，代码组织 MUST 遵循以下层级：

- **bootstrap**: 应用启动层，负责配置和依赖注入
- **interface**: 接口层，负责 HTTP 控制器和请求/响应定义
- **application**: 应用层，负责用例编排和 DTO 转换
- **domain**: 领域层，负责业务逻辑和领域模型
- **infrastructure**: 基础设施层，负责数据持久化和外部服务集成

层级依赖 MUST 遵循：上层可依赖下层，下层 MUST NOT 依赖上层。

### II. API URL Convention

所有业务相关的 HTTP 接口 MUST 遵循以下 URL 规范：

- **业务接口路径格式**: `/api/service/{version}/{resource}/{action}`
  - 示例: `/api/service/v1/nodes/query`
  - 示例: `/api/service/v1/topologies/create`
  - 示例: `/api/service/v1/relationships/delete`

- **版本号规范**: 使用 `v1`, `v2` 等形式，当前版本为 `v1`

- **排除的路径**（不使用 `/api/service/` 前缀）:
  - 健康检查: `/health`, `/actuator/*`
  - API 文档: `/swagger-ui/*`, `/v3/api-docs`
  - 测试端点: `/test/*`

- **HTTP 方法**: 所有业务接口统一使用 POST 方法（POST-Only API），便于网关参数注入

### III. POST-Only API Design

所有业务接口 MUST 使用 POST 方法，遵循以下规范：

- 请求参数通过 JSON Body 传递
- 响应格式统一为 `Result<T>` 结构: `{code, message, data, success}`
- 分页接口使用 `PageResult<T>` 结构
- 所有接口 MUST 使用 SpringDoc OpenAPI 注解生成文档

### IV. Database Migration

数据库变更 MUST 通过 Flyway 迁移脚本管理：

- 迁移文件位置: `src/main/resources/db/migration/`
- 命名格式: `V{version}__{description}.sql`
- 禁止直接修改数据库 schema，所有变更 MUST 有对应的迁移脚本
- 迁移脚本 MUST 支持回滚

### V. Technology Stack

本项目技术栈 MUST 遵循以下版本要求：

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.x
- **MyBatis-Plus**: 3.5.x
- **MySQL**: 8.0
- **SpringDoc OpenAPI**: 用于 API 文档生成

### VI. Pagination Protocol

所有分页接口 MUST 遵循统一的分页协议：

#### 分页请求参数

分页请求 MUST 继承 `PageableRequest` 基类，包含以下标准字段：

```json
{
  "page": 1,          // 页码（从 1 开始），默认 1，最小 1
  "size": 20,         // 每页大小，默认 20，范围 1-100
  "tenantId": null,   // 租户ID（网关注入，hidden）
  "traceId": null,    // 追踪ID（网关注入，hidden）
  "userId": null      // 用户ID（网关注入，hidden）
}
```

- `page` MUST 从 1 开始计数，最小值为 1
- `size` MUST 限制在 1-100 范围内，默认值为 20
- 网关注入字段（tenantId, traceId, userId）在 Swagger 文档中 MUST 设置为 hidden

#### 分页响应结果

分页响应 MUST 使用 `PageResult<T>` 结构：

```json
{
  "code": 0,
  "message": "success",
  "success": true,
  "data": {
    "content": [],           // 数据列表
    "page": 1,               // 当前页码（从1开始）
    "size": 10,              // 每页大小
    "totalElements": 100,    // 总记录数
    "totalPages": 10,        // 总页数
    "first": true,           // 是否为第一页
    "last": false            // 是否为最后一页
  }
}
```

- `content` MUST 包含当前页的数据列表
- `totalPages` MUST 根据 `totalElements` 和 `size` 自动计算
- `first` 和 `last` MUST 正确标识边界状态

### VII. Database Design Standards

所有数据库表设计 MUST 遵循以下规范：

#### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 表名 | 小写蛇形，单数形式 | `node`, `agent`, `prompt_template` |
| 关联表 | `{主表}_2_{从表}` | `node_2_agent`, `topology_2_node` |
| 字段名 | 小写蛇形 | `created_at`, `node_type_id` |
| 主键 | 统一使用 `id` | `id BIGINT` |
| 外键字段 | `{关联表单数}_id` | `node_id`, `agent_id` |
| 布尔字段 | `is_` 前缀或动词过去式 | `is_system`, `deleted` |
| 时间字段 | `_at` 后缀 | `created_at`, `updated_at` |
| 外键约束 | `fk_{表}_{字段}` | `fk_node_type` |
| 唯一约束 | `uk_{字段}` | `uk_code`, `uk_type_name` |
| 普通索引 | `idx_{字段}` | `idx_status`, `idx_created_at` |

#### 通用字段定义

所有业务表 MUST 包含以下通用字段：

```sql
-- 主键（必须）
id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

-- 审计字段（推荐）
created_by      BIGINT          COMMENT '创建人ID',
created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_by      BIGINT          COMMENT '修改人ID',
updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

-- 版本控制（按需）
version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

-- 软删除（业务表必须）
deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
```

#### 字段类型规范

| 场景 | 类型 | 长度建议 |
|------|------|----------|
| 主键/外键 | `BIGINT` | - |
| 编码/标识 | `VARCHAR` | 20-50 |
| 名称 | `VARCHAR` | 100-255 |
| 描述 | `VARCHAR` / `TEXT` | 500-1000 / 无限 |
| 状态/枚举 | `VARCHAR` | 20-32 |
| 布尔值 | `TINYINT(1)` | - |
| 时间 | `DATETIME` | - |
| 扩展属性 | `JSON` | - |

#### COMMENT 规范

所有字段 MUST 添加 COMMENT，枚举字段 MUST 列出所有可选值：

```sql
-- 普通字段
name            VARCHAR(100)    NOT NULL COMMENT '名称',

-- 枚举字段（MUST 列出所有可选值）
status          VARCHAR(20)     NOT NULL DEFAULT 'RUNNING' COMMENT '状态: RUNNING, STOPPED, MAINTENANCE, OFFLINE',
role            VARCHAR(32)     NOT NULL COMMENT '角色: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER',

-- 布尔字段（MUST 说明 0/1 含义）
deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
is_system       TINYINT(1)      DEFAULT 1 COMMENT '是否系统预置: 0-否, 1-是',

-- 范围字段（MUST 说明取值范围）
temperature     DECIMAL(3,2)    DEFAULT 0.30 COMMENT '温度参数 (0.0-2.0)',

-- 单位字段（MUST 说明单位）
max_runtime     INT             DEFAULT 300 COMMENT '最长运行时间（秒）',
```

#### 表属性规范

所有表 MUST 使用以下属性：

```sql
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表的中文描述';
```

#### 业务主表模板

```sql
CREATE TABLE {table_name} (
    -- 主键
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务字段
    name            VARCHAR(100)    NOT NULL COMMENT '名称',
    code            VARCHAR(50)     NOT NULL COMMENT '编码',
    description     VARCHAR(500)    DEFAULT NULL COMMENT '描述',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE, PENDING',
    attributes      JSON            DEFAULT NULL COMMENT '扩展属性（JSON格式）',

    -- 审计字段
    created_by      BIGINT          COMMENT '创建人ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT          COMMENT '修改人ID',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 版本控制
    version         INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    -- 约束
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表注释';
```

#### 关联表模板

```sql
CREATE TABLE {tableA}_2_{tableB} (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    {tableA}_id     BIGINT          NOT NULL COMMENT '{TableA} ID',
    {tableB}_id     BIGINT          NOT NULL COMMENT '{TableB} ID',

    -- 审计字段
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_{tableA}_{tableB} ({tableA}_id, {tableB}_id, deleted),
    INDEX idx_{tableA}_id ({tableA}_id),
    INDEX idx_{tableB}_id ({tableB}_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='{TableA}-{TableB}关联表';
```

#### 字段顺序规范

字段 MUST 按以下顺序排列：

1. 主键字段 (`id`)
2. 业务核心字段 (`name`, `code`, `type`, `status` ...)
3. 业务扩展字段 (`description`, `attributes`, `config` ...)
4. 关联外键字段 (`xxx_id`)
5. 创建审计字段 (`created_by`, `created_at`)
6. 修改审计字段 (`updated_by`, `updated_at`)
7. 版本控制字段 (`version`)
8. 软删除字段 (`deleted`)

### VIII. SQL Query Standards

所有 SQL 查询 MUST 遵循以下规范：

#### 禁止使用 SELECT *

SQL 查询中 MUST NOT 使用 `SELECT *` 来获取所有字段，MUST 明确列出需要的字段名。

**原因**：
- 避免查询不需要的字段，减少网络传输和内存消耗
- 表结构变更时不会意外返回新字段
- 提高代码可读性和可维护性
- JOIN 查询时避免字段名冲突

**错误示例**：

```sql
-- MUST NOT: 禁止使用 SELECT *
SELECT * FROM node WHERE id = 1;

SELECT t.*, n.* FROM topology t JOIN node n ON t.id = n.topology_id;
```

**正确示例**：

```sql
-- MUST: 明确列出需要的字段
SELECT id, name, status, created_at FROM node WHERE id = 1;

SELECT
    t.id AS topology_id,
    t.name AS topology_name,
    n.id AS node_id,
    n.name AS node_name,
    n.status AS node_status
FROM topology t
JOIN node n ON t.id = n.topology_id;
```

#### MyBatis Mapper XML 规范

在 MyBatis Mapper XML 中，MUST 使用 `<sql>` 片段定义常用字段列表：

```xml
<!-- 定义基础字段列表 -->
<sql id="Base_Column_List">
    id, name, description, status, layer, created_at, updated_at, version, deleted
</sql>

<!-- 使用字段列表 -->
<select id="selectById" resultType="...">
    SELECT <include refid="Base_Column_List"/>
    FROM node
    WHERE id = #{id} AND deleted = 0
</select>

<!-- JOIN 查询时明确指定表别名和字段 -->
<select id="selectWithType" resultType="...">
    SELECT
        n.id, n.name, n.status, n.layer,
        nt.code AS node_type_code, nt.name AS node_type_name
    FROM node n
    JOIN node_type nt ON n.node_type_id = nt.id
    WHERE n.id = #{id} AND n.deleted = 0
</select>
```

#### 例外情况

以下情况可以使用 `SELECT *`，但 MUST 添加注释说明原因：

1. **临时调试查询**（MUST NOT 提交到代码库）
2. **数据迁移脚本**（一次性脚本，需注释说明）
3. **动态 schema 场景**（如 EAV 模式，需架构评审）

```sql
-- 例外：数据迁移脚本，仅执行一次
-- INSERT INTO node_backup SELECT * FROM node WHERE created_at < '2025-01-01';
```

### IX. Process Management Standards

终止应用进程 MUST 使用基于端口号的方式，MUST NOT 使用其他方式（如 pkill、pgrep 等基于进程名的方式）。

#### 正确方式（MUST）

使用 `lsof` 根据端口号查找并终止进程：

```bash
# 终止占用 8081 端口的进程
lsof -ti :8081 | xargs kill 2>/dev/null || echo "No process on port 8081"

# 强制终止（慎用）
lsof -ti :8081 | xargs kill -9 2>/dev/null || echo "No process on port 8081"
```

#### 禁止的方式（MUST NOT）

```bash
# MUST NOT: 禁止使用基于进程名的方式
pkill -f "bootstrap-1.0.0-SNAPSHOT.jar"
pgrep -f "bootstrap-1.0.0-SNAPSHOT.jar" | xargs kill
ps aux | grep bootstrap | grep -v grep | awk '{print $2}' | xargs kill
```

#### 原因

- **精确性**: 端口号精确标识服务，避免误杀同名进程
- **可预测性**: 端口号在配置中明确定义（如 `server.port=8081`），行为可预测
- **安全性**: 避免因进程名匹配错误导致误杀其他进程
- **一致性**: 团队统一使用相同的进程管理方式

#### 常用端口

| 环境 | 端口 | 说明 |
|------|------|------|
| local | 8081 | 本地开发环境 |

### X. Async Implementation Standards

异步任务执行 MUST 使用 Spring 提供的异步机制，MUST NOT 直接使用 `CompletableFuture.runAsync()` 等 JDK 原生方式。

#### 推荐方案（按优先级排序）

| 方案 | 适用场景 | 说明 |
|------|----------|------|
| **Spring @Async** | 简单异步任务 | **首选**，可读性最强 |
| Spring ApplicationEvent | 需要解耦的场景 | 事件驱动，符合 DDD |
| 注入 TaskExecutor | 需要细粒度控制 | 复杂调度场景 |

#### Spring @Async 规范（MUST）

1. 异步方法 MUST 定义在独立的 Service 类中（避免 self-injection 问题）
2. MUST 使用自定义线程池，MUST NOT 使用默认的 SimpleAsyncTaskExecutor
3. MUST 配置合理的线程池参数（核心线程数、最大线程数、队列容量）
4. 异步方法 MUST 使用 `@Async("executorName")` 指定线程池名称

#### 线程池配置规范

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

#### 线程池命名规范

| 业务场景 | Bean 名称 | 线程前缀 |
|----------|-----------|----------|
| 诊断任务 | `diagnosisExecutor` | `diagnosis-` |
| 通用任务 | `taskExecutor` | `async-task-` |
| 事件处理 | `eventExecutor` | `event-` |

#### 异步 Service 类规范

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MyAsyncService {

    // 正确：指定线程池名称
    @Async("taskExecutor")
    public void processAsync(Long id) {
        log.info("异步处理任务，id: {}", id);
        // 业务逻辑
    }

    // 正确：有返回值的异步方法
    @Async("taskExecutor")
    public CompletableFuture<Result> processWithResult(Long id) {
        // 业务逻辑
        return CompletableFuture.completedFuture(result);
    }
}
```

#### 禁止的方式（MUST NOT）

```java
// MUST NOT: 直接使用 CompletableFuture.runAsync()
CompletableFuture.runAsync(() -> {
    // 使用默认 ForkJoinPool，不便于监控
});

// MUST NOT: 使用 new Thread()
new Thread(() -> {
    // 无法复用线程，资源浪费
}).start();

// MUST NOT: 未指定线程池的 @Async
@Async  // 缺少线程池名称
public void badAsync() { }

// MUST NOT: 同一类中调用异步方法（代理失效）
public void caller() {
    this.asyncMethod();  // 代理不生效，同步执行
}
```

#### 原因

- **可观测性**: 自定义线程池便于监控线程状态和调优
- **可控性**: 线程池参数可配置，避免资源耗尽
- **可读性**: @Async 声明式编程，代码意图清晰
- **可测试性**: 便于 mock 和单元测试
- **可追踪性**: 自定义线程名前缀便于日志追踪

## API Design Standards

### Request/Response 规范

- 请求类命名: `{Action}{Resource}Request`，如 `CreateNodeRequest`
- 响应类命名: `{Resource}DTO`，如 `NodeDTO`
- 控制器类命名: `{Resource}Controller`，如 `NodeController`

### Controller 注解规范

每个 Controller MUST 包含以下注解：

```java
@Slf4j
@RestController
@RequestMapping("/api/service/v1/{resource}")
@RequiredArgsConstructor
@Tag(name = "资源描述", description = "接口描述（POST-Only API）")
```

### Swagger 文档规范

每个接口 MUST 包含：

- `@Operation`: 接口说明和描述
- `@ApiResponses`: 响应状态码说明
- `@Valid`: 请求参数校验

## Development Workflow

### 构建与运行

```bash
# 构建
mvn clean package -DskipTests

# 运行（推荐）
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 测试
mvn test
```

### 代码提交

- 提交前 MUST 确保代码编译通过
- 提交前 SHOULD 运行相关单元测试
- 提交信息 MUST 遵循 Conventional Commits 规范

## Governance

本宪法规定了 OP-Stack Service 项目的核心原则和规范，所有开发活动 MUST 遵循。

### 修订流程

1. 修订提案 MUST 以文档形式提交
2. 修订 MUST 包含变更原因和影响分析
3. 重大变更（MAJOR）需要团队评审
4. 修订后 MUST 更新版本号和日期

### 版本规则

- **MAJOR**: 原则删除或重新定义（不兼容变更）
- **MINOR**: 新增原则或实质性扩展
- **PATCH**: 澄清、措辞修正、非语义性改进

### 合规检查

- 代码评审 MUST 验证是否符合宪法原则
- 新功能 MUST 遵循 API URL Convention
- 数据库变更 MUST 遵循 Database Migration 原则
- 分页接口 MUST 遵循 Pagination Protocol
- 数据库表设计 MUST 遵循 Database Design Standards
- SQL 查询 MUST 遵循 SQL Query Standards
- 进程管理 MUST 遵循 Process Management Standards（使用端口号终止进程）
- 异步任务 MUST 遵循 Async Implementation Standards（使用 Spring @Async）

**Version**: 1.5.0 | **Ratified**: 2025-12-27 | **Last Amended**: 2026-01-05
