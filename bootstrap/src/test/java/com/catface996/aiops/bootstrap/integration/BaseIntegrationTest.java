package com.catface996.aiops.bootstrap.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

/**
 * 集成测试基类
 *
 * 使用 TestContainers 启动 MySQL 和 Redis 容器，
 * 提供完整的 Spring Boot 应用上下文进行端到端测试。
 *
 * 遵循测试最佳实践：
 * - 使用 @SpringBootTest 获得完整上下文
 * - 使用 TestContainers 进行真实数据库测试
 * - 使用 MockMvc 测试 HTTP 接口
 * - 每个测试独立，无共享状态
 *
 * @author AI Assistant
 * @since 2025-01-26
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("aiops_test")
            .withUsername("root")
            .withPassword("root")
            .withCommand("--default-authentication-plugin=mysql_native_password");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired(required = false)
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * 动态配置数据源和 Redis 连接
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL 配置
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Redis 配置
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Flyway 配置
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @BeforeEach
    void setUp() {
        // 清理 Redis 中的登录失败计数，避免测试间干扰
        if (redisTemplate != null) {
            try {
                Set<String> loginFailKeys = redisTemplate.keys("login:fail:*");
                if (loginFailKeys != null && !loginFailKeys.isEmpty()) {
                    redisTemplate.delete(loginFailKeys);
                }
            } catch (Exception e) {
                // 忽略 Redis 清理错误
            }
        }
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 从 JSON 字符串解析对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
