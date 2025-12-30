# aiops-service Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-11-21

## Active Technologies
- Java 21 (LTS) + Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI (001-remove-llm-service)
- MySQL 8.0 (via Flyway migrations) (001-remove-llm-service)
- Java 21 (LTS) + Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI (026-report-management)
- MySQL 8.0, Flyway migrations (027-agent-management)
- MySQL 8.0 (通过 Flyway 迁移) (001-remove-relationship)
- Java 21 (LTS) + Spring Boot 3.4.x, MyBatis-Plus 3.5.x, Flyway (033-database-schema-compliance)
- MySQL 8.0 (existing tables: topology, node, agent, topology_2_node, node_2_agent) (038-hierarchical-team-query)
- Java 21 (LTS) + Spring Boot 3.4.x, Spring WebFlux (for WebClient SSE), MyBatis-Plus 3.5.x, SpringDoc OpenAPI (039-trigger-multiagent-execution)
- MySQL 8.0 (existing topology/agent data) (039-trigger-multiagent-execution)

- Java 21 (LTS) + Spring Boot 3.4.1, Spring Web MVC, SpringDoc OpenAPI (024-post-only-api)
- MySQL 8.0 (通过 MyBatis-Plus) (024-post-only-api)
- Java 21 (LTS) (001-init-ddd-architecture)

## Project Structure

```text
src/
tests/
```

## Commands

### Build
```bash
mvn clean package -DskipTests
```

### Run Application (Recommended)
```bash
# 推荐: 直接运行 jar 包，避免 mvn spring-boot:run 的类路径缓存问题
java -jar bootstrap/target/aiops-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

### Run Application (Alternative)
```bash
# 注意: mvn spring-boot:run 可能因类路径缓存导致代码修改不生效
# 如遇此问题，请使用上述 jar 包方式运行
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local
```

### Test
```bash
mvn test
```

## Code Style

Java 21 (LTS): Follow standard conventions

## Recent Changes
- 041-cleanup-obsolete-fields: Added Java 21 (LTS) + Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
- 040-agent-bound-refactor: Added Java 21 (LTS) + Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
- 039-trigger-multiagent-execution: Added Java 21 (LTS) + Spring Boot 3.4.x, Spring WebFlux (for WebClient SSE), MyBatis-Plus 3.5.x, SpringDoc OpenAPI


<!-- MANUAL ADDITIONS START -->
## Constitution (项目宪法)

以下规则为项目强制性约束，禁止修改：

### 端口配置
- **开发环境端口必须是 8081**，禁止修改
- local profile 服务端口: `server.port=8081`

### SQL 定义规则
- **禁止使用注解方式定义 SQL**（@Select, @Update, @Delete, @Insert）
- **禁止使用 Lambda 表达式查询**（LambdaQueryWrapper, LambdaUpdateWrapper）
- **所有 SQL 必须定义在 mapper.xml 文件中**
- Mapper 接口仅声明方法签名，SQL 实现在对应的 XML 文件中
- BaseMapper 方法仅允许使用：selectById, selectBatchIds, insert, updateById, deleteById
- XML 文件位置：`infrastructure/repository/mysql-impl/src/main/resources/mapper/{module}/*.xml`
<!-- MANUAL ADDITIONS END -->
