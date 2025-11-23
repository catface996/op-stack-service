# Spring Cloud 最佳实践

本文档指导 AI 如何正确、高效地使用 Spring Cloud 构建微服务架构。

## 核心原则

### 1. 服务拆分原则

**你应该遵守**：
- ✅ 按业务领域拆分服务（DDD）
- ✅ 单一职责原则
- ✅ 服务自治（独立部署、独立数据库）
- ❌ 不要过度拆分（避免分布式事务）

### 2. 服务通信原则

**你应该遵守**：
- ✅ 使用 REST API 或 RPC 通信
- ✅ 使用服务注册与发现
- ✅ 使用负载均衡
- ❌ 不要直接使用 IP 地址调用

### 3. 容错设计原则

**你应该遵守**：
- ✅ 服务降级（Fallback）
- ✅ 服务熔断（Circuit Breaker）
- ✅ 服务限流（Rate Limiting）
- ✅ 超时控制

## 微服务架构组件

### 你应该了解的核心组件

| 组件 | 作用 | 推荐方案 |
|------|------|---------|
| **服务注册与发现** | 服务注册、服务发现 | Nacos、Eureka |
| **配置中心** | 集中管理配置 | Nacos Config、Apollo |
| **API 网关** | 统一入口、路由、鉴权 | Spring Cloud Gateway |
| **负载均衡** | 客户端负载均衡 | Spring Cloud LoadBalancer |
| **服务调用** | 声明式 HTTP 客户端 | OpenFeign |
| **熔断降级** | 容错保护 | Sentinel、Resilience4j |
| **链路追踪** | 分布式追踪 | Sleuth + Zipkin |
| **消息总线** | 配置刷新、事件通知 | Spring Cloud Bus |

## 服务注册与发现

### 你应该遵循的注册发现规则

**1. 使用 Nacos 作为注册中心**
- 支持服务注册与发现
- 支持配置管理
- 支持健康检查

**2. 服务命名规范**
- 使用小写字母和连字符
- 见名知意
- 示例：`user-service`、`order-service`

**3. 服务实例配置**
- 配置服务名称
- 配置服务端口
- 配置健康检查路径

**4. 服务发现方式**
- 使用 OpenFeign 声明式调用
- 使用 RestTemplate + @LoadBalanced
- 使用 WebClient + @LoadBalanced

### Nacos 配置示例

**你应该这样配置 Nacos**：

```yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
```

## 配置中心

### 你应该遵循的配置中心规则

**1. 使用 Nacos Config 管理配置**
- 集中管理配置
- 支持动态刷新
- 支持多环境配置

**2. 配置文件命名规范**
- 格式：`${spring.application.name}-${profile}.${file-extension}`
- 示例：`user-service-dev.yml`

**3. 配置优先级**
- Nacos Config > application.yml
- profile 配置 > 默认配置

**4. 配置刷新**
- 使用 @RefreshScope 注解
- 支持动态刷新配置

### Nacos Config 配置示例

**你应该这样配置 Nacos Config**：

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yml
        refresh-enabled: true
```

## API 网关

### 你应该遵循的网关规则

**1. 使用 Spring Cloud Gateway**
- 基于 WebFlux，性能更好
- 支持动态路由
- 支持过滤器链

**2. 网关职责**
- 路由转发
- 统一鉴权
- 限流熔断
- 日志记录
- 跨域处理

**3. 路由配置**
- 使用配置文件配置路由
- 使用断言（Predicate）匹配请求
- 使用过滤器（Filter）处理请求

**4. 网关安全**
- JWT 令牌验证
- 黑白名单
- 防止 SQL 注入、XSS 攻击

### Gateway 路由配置示例

**你应该这样配置路由**：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## 服务调用（OpenFeign）

### 你应该遵循的 Feign 规则

**1. 使用 OpenFeign 声明式调用**
- 简化服务间调用
- 支持负载均衡
- 支持熔断降级

**2. Feign 接口定义**
- 接口命名：`XxxFeignClient`
- 使用 @FeignClient 注解
- 指定服务名称

**3. Feign 配置**
- 配置超时时间
- 配置重试策略
- 配置日志级别

**4. Feign 降级**
- 使用 fallback 或 fallbackFactory
- 提供降级逻辑

### Feign 接口示例

**你应该这样定义 Feign 接口**：

```java
@FeignClient(
    name = "user-service",
    fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient {
    
    @GetMapping("/api/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
    
    @PostMapping("/api/users")
    Result<Void> createUser(@RequestBody CreateUserRequest request);
}

@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    
    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override
            public Result<UserDTO> getUserById(Long id) {
                log.error("调用用户服务失败", cause);
                return Result.error("用户服务暂时不可用");
            }
            
            @Override
            public Result<Void> createUser(CreateUserRequest request) {
                log.error("调用用户服务失败", cause);
                return Result.error("用户服务暂时不可用");
            }
        };
    }
}
```

## 熔断降级（Sentinel）

### 你应该遵循的熔断降级规则

**1. 使用 Sentinel 实现熔断降级**
- 流量控制
- 熔断降级
- 系统负载保护

**2. 熔断策略**
- 慢调用比例：响应时间超过阈值
- 异常比例：异常比例超过阈值
- 异常数：异常数超过阈值

**3. 降级策略**
- 返回默认值
- 返回缓存数据
- 返回友好提示

**4. 限流策略**
- QPS 限流
- 线程数限流
- 热点参数限流

### Sentinel 配置示例

**你应该这样配置 Sentinel**：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-sentinel
            groupId: DEFAULT_GROUP
            rule-type: flow
```

## 分布式事务

### 你应该遵循的分布式事务规则

**1. 尽量避免分布式事务**
- 合理拆分服务
- 使用最终一致性

**2. 分布式事务方案**
- Seata（AT、TCC、SAGA 模式）
- 本地消息表
- 可靠消息最终一致性
- 最大努力通知

**3. Seata 使用建议**
- AT 模式：适合简单场景
- TCC 模式：适合对一致性要求高的场景
- SAGA 模式：适合长事务

**4. 最终一致性方案**
- 使用消息队列（RocketMQ、Kafka）
- 使用定时任务补偿
- 使用事件驱动

## 链路追踪

### 你应该遵循的链路追踪规则

**1. 使用 Sleuth + Zipkin**
- Sleuth：生成 TraceId 和 SpanId
- Zipkin：收集和展示链路数据

**2. TraceId 传递**
- 自动在 HTTP Header 中传递
- 自动在日志中打印

**3. 链路数据采集**
- 配置采样率（生产环境建议 0.1）
- 配置数据上报方式

**4. 链路分析**
- 分析慢请求
- 分析异常请求
- 分析服务依赖关系

### Sleuth 配置示例

**你应该这样配置 Sleuth**：

```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # 采样率 10%
  zipkin:
    base-url: http://localhost:9411
    sender:
      type: web
```

## 服务监控

### 你应该遵循的监控规则

**1. 使用 Spring Boot Actuator**
- 暴露健康检查端点
- 暴露指标端点
- 暴露信息端点

**2. 使用 Prometheus + Grafana**
- Prometheus：采集指标
- Grafana：可视化展示

**3. 监控指标**
- JVM 指标（内存、GC、线程）
- HTTP 指标（QPS、响应时间、错误率）
- 数据库指标（连接数、慢查询）
- Redis 指标（命中率、连接数）

**4. 告警规则**
- CPU 使用率 > 80%
- 内存使用率 > 80%
- 错误率 > 1%
- 响应时间 > 1s

### Actuator 配置示例

**你应该这样配置 Actuator**：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## 日志管理

### 你应该遵循的日志规则

**1. 统一日志格式**
- 包含 TraceId 和 SpanId
- 包含服务名称
- 包含时间戳

**2. 日志收集**
- 使用 ELK（Elasticsearch + Logstash + Kibana）
- 使用 Loki + Grafana

**3. 日志级别**
- ERROR：错误日志
- WARN：警告日志
- INFO：重要业务日志
- DEBUG：调试日志

**4. 日志内容**
- 记录请求入参和出参
- 记录异常堆栈
- 不记录敏感信息

## 服务安全

### 你应该遵循的安全规则

**1. 认证授权**
- 使用 JWT 令牌
- 网关统一鉴权
- 服务间调用使用内部令牌

**2. 接口安全**
- 参数校验
- 防止 SQL 注入
- 防止 XSS 攻击
- 防止 CSRF 攻击

**3. 数据安全**
- 敏感数据加密
- HTTPS 传输
- 数据脱敏

**4. 限流防护**
- 网关限流
- 服务限流
- 接口限流

## 服务部署

### 你应该遵循的部署规则

**1. 容器化部署**
- 使用 Docker 容器化
- 使用 Kubernetes 编排

**2. 配置外部化**
- 使用配置中心
- 使用环境变量
- 不在镜像中硬编码配置

**3. 健康检查**
- 配置 Liveness Probe
- 配置 Readiness Probe

**4. 滚动更新**
- 灰度发布
- 蓝绿部署
- 金丝雀发布

## 常见错误和纠正方法

### 你应该避免的错误

| 错误类型 | 错误做法 | 正确做法 | 原因 |
|---------|---------|---------|------|
| **过度拆分** | 拆分过细的服务 | 按业务领域合理拆分 | 增加复杂度 |
| **直接调用** | 使用 IP 地址调用 | 使用服务名称调用 | 不支持负载均衡 |
| **无熔断降级** | 不处理服务异常 | 配置熔断降级 | 雪崩效应 |
| **无超时控制** | 不设置超时时间 | 设置合理的超时时间 | 资源耗尽 |
| **分布式事务** | 过度使用分布式事务 | 使用最终一致性 | 性能问题 |
| **无链路追踪** | 不配置链路追踪 | 配置 Sleuth + Zipkin | 问题难以排查 |
| **无监控告警** | 不配置监控 | 配置 Prometheus + Grafana | 问题发现不及时 |
| **配置硬编码** | 配置写在代码中 | 使用配置中心 | 不便于维护 |

## 你的检查清单

在构建微服务时，你应该检查：

### 架构检查
- [ ] 服务拆分合理（按业务领域）
- [ ] 服务自治（独立部署、独立数据库）
- [ ] 服务通信使用注册中心
- [ ] 配置使用配置中心

### 容错检查
- [ ] 配置了熔断降级
- [ ] 配置了超时控制
- [ ] 配置了重试策略
- [ ] 配置了限流保护

### 监控检查
- [ ] 配置了链路追踪
- [ ] 配置了服务监控
- [ ] 配置了日志收集
- [ ] 配置了告警规则

### 安全检查
- [ ] 配置了认证授权
- [ ] 配置了接口安全
- [ ] 配置了数据加密
- [ ] 配置了限流防护

### 部署检查
- [ ] 使用容器化部署
- [ ] 配置外部化
- [ ] 配置健康检查
- [ ] 支持滚动更新

## 关键原则总结

### 架构原则
1. **合理拆分**：按业务领域拆分，避免过度拆分
2. **服务自治**：独立部署、独立数据库
3. **服务通信**：使用注册中心和负载均衡
4. **配置管理**：使用配置中心集中管理

### 容错原则
1. **熔断降级**：防止雪崩效应
2. **超时控制**：避免资源耗尽
3. **限流保护**：保护系统稳定
4. **重试策略**：提高成功率

### 监控原则
1. **链路追踪**：快速定位问题
2. **服务监控**：实时掌握系统状态
3. **日志收集**：便于问题排查
4. **告警通知**：及时发现问题

### 安全原则
1. **认证授权**：保护接口安全
2. **数据加密**：保护敏感数据
3. **限流防护**：防止恶意攻击
4. **参数校验**：防止非法输入

## 关键收益

遵循这些规范，可以获得：

- ✅ 高可用的微服务架构
- ✅ 快速定位和解决问题
- ✅ 灵活的服务扩展能力
- ✅ 安全可靠的系统
- ✅ 便于维护和升级
- ✅ 提高开发效率
