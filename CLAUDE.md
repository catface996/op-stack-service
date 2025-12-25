# aiops-service Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-11-21

## Active Technologies
- Java 21 (LTS) + Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI (001-remove-llm-service)
- MySQL 8.0 (via Flyway migrations) (001-remove-llm-service)

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
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
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
- 001-remove-auth-features: Added [if applicable, e.g., PostgreSQL, CoreData, files or N/A]
- 001-remove-llm-service: Added Java 21 (LTS) + Spring Boot 3.4.1, MyBatis-Plus 3.5.7, SpringDoc OpenAPI

- 024-post-only-api: Added Java 21 (LTS) + Spring Boot 3.4.1, Spring Web MVC, SpringDoc OpenAPI

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
