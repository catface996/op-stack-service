# AIOps Service

基于 Spring Boot 3.4.1 和 Spring Cloud 2025.0.0 的 DDD 分层架构微服务项目。

![AI运筹运营运维一体化](doc/image/AI运筹运营运维一体化.png)

## 技术栈

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.1
- **Spring Cloud**: 2025.0.0
- **ORM**: MyBatis-Plus 3.5.7
- **数据库连接池**: Druid 1.2.20
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **消息队列**: AWS SQS
- **链路追踪**: Micrometer Tracing + Brave
- **监控**: Prometheus + Spring Boot Actuator
- **日志**: Logback + Logstash Encoder (JSON)

## 项目结构

```
aiops-service/
├── common/                    # 通用模块 (异常、工具类、Result)
├── bootstrap/                 # 启动模块
├── interface/                 # 接口层
│   ├── interface-http/        #   - HTTP REST 接口
│   └── interface-consumer/    #   - 消息队列消费者
├── application/               # 应用层
│   ├── application-api/       #   - 应用服务接口
│   └── application-impl/      #   - 应用服务实现
├── domain/                    # 领域层
│   ├── domain-api/            #   - 领域模型定义
│   └── domain-impl/           #   - 领域服务实现
└── infrastructure/            # 基础设施层
    ├── repository/            #   - 数据持久化
    │   ├── repository-api/    #     * 仓储接口 + Entity
    │   └── mysql-impl/        #     * MySQL 实现 + PO
    ├── cache/                 #   - 缓存
    │   ├── cache-api/         #     * 缓存接口
    │   └── redis-impl/        #     * Redis 实现
    └── mq/                    #   - 消息队列
        ├── mq-api/            #     * 消息队列接口
        └── sqs-impl/          #     * AWS SQS 实现
```

**模块总数**: 22 个 (1个父POM + 7个聚合模块 + 14个代码模块)

## 快速开始

### 前置要求

- JDK 21 (LTS)
- Maven 3.8+
- Git 2.30+

### 编译项目

```bash
mvn clean compile
```

### 打包应用

```bash
mvn clean package -DskipTests
```

### 启动应用

```bash
# 使用 local profile (默认)
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 使用其他环境
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 验证

```bash
# 健康检查
curl http://localhost:8080/health

# Actuator 健康检查
curl http://localhost:8080/actuator/health

# Prometheus 监控指标
curl http://localhost:8080/actuator/prometheus
```

## 核心功能

### 1. DDD 分层架构

项目严格遵循 DDD (领域驱动设计) 分层架构：

- **接口层 (Interface)**: 处理 HTTP 请求和消息队列消费
- **应用层 (Application)**: 编排业务用例
- **领域层 (Domain)**: 核心业务逻辑
- **基础设施层 (Infrastructure)**: 技术实现

### 2. 统一异常处理

- 业务异常 (BusinessException)
- 系统异常 (SystemException)
- 全局异常处理器
- 统一响应对象 (Result)

### 3. 分布式链路追踪

- Micrometer Tracing + Brave
- 自动生成 traceId 和 spanId
- 日志自动包含追踪信息

### 4. 结构化日志

- 本地开发: 控制台彩色日志
- 其他环境: JSON 格式文件日志
- 支持日志级别配置
- 支持日志滚动和保留策略

### 5. Prometheus 监控

- JVM 指标 (内存、GC、线程)
- HTTP 请求指标
- 自定义业务指标
- `/actuator/prometheus` 端点

### 6. 多环境支持

- local: 本地开发
- dev: 开发环境
- test: 测试环境
- staging: 预发布环境
- prod: 生产环境

## 文档

### 核心文档
- **文档目录索引**: [doc/README.md](doc/README.md) - 所有文档的组织说明
- **快速开始**: [specs/001-init-ddd-architecture/quickstart.md](specs/001-init-ddd-architecture/quickstart.md)
- **依赖管理**: [doc/01-init-backend/DEPENDENCIES.md](doc/01-init-backend/DEPENDENCIES.md)
- **POM 配置规范**: [specs/001-init-ddd-architecture/contracts/pom-structure.md](specs/001-init-ddd-architecture/contracts/pom-structure.md)
- **架构决策**: [specs/001-init-ddd-architecture/research.md](specs/001-init-ddd-architecture/research.md)
- **环境配置**: [bootstrap/src/main/resources/README.md](bootstrap/src/main/resources/README.md)

### 生产就绪验证
- **验证目录**: [doc/02-verification/](doc/02-verification/) - 按 spec 编号组织的验证文档
- **完整验证报告**: [doc/02-verification/001-init-ddd-architecture/PRODUCTION_READINESS_VERIFICATION.md](doc/02-verification/001-init-ddd-architecture/PRODUCTION_READINESS_VERIFICATION.md) - 基于 10 条成功标准的详细验证
- **快速验证清单**: [doc/02-verification/001-init-ddd-architecture/QUICK_VERIFICATION.md](doc/02-verification/001-init-ddd-architecture/QUICK_VERIFICATION.md) - 5 分钟快速验证指南
- **自动化验证脚本**: `./verify-production-ready.sh` - 一键运行所有验证测试

## 开发规范

### 依赖管理

- 所有版本在父 POM 统一管理
- 子模块不指定版本号
- 使用 BOM 导入 Spring 生态

### 日志规范

- 使用 SLF4J API
- 项目包使用 DEBUG/INFO 级别
- 框架包使用 WARN 级别
- 禁止使用 System.out.println()

### 异常处理

- 业务异常使用 BusinessException
- 系统异常使用 SystemException
- 所有异常在接口层统一处理

## 性能指标

- 编译时间: < 3 秒
- 启动时间: < 3 秒
- JAR 文件大小: 54MB
- JDK 版本: 21

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (git checkout -b feature/AmazingFeature)
3. 提交更改 (git commit -m 'Add some AmazingFeature')
4. 推送到分支 (git push origin feature/AmazingFeature)
5. 开启 Pull Request

## 许可证

[MIT License](LICENSE)

## 联系方式

- Author: catface996
- Email: your.email@example.com
- Project Link: https://github.com/yourname/aiops-service
