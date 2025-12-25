package com.catface996.aiops.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 *
 * <p>配置 API 文档的基本信息、安全认证方案等。</p>
 *
 * <p>访问地址：</p>
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI JSON: http://localhost:8080/v3/api-docs</li>
 *   <li>OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-26
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * 配置 OpenAPI 文档
     *
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("AIOps Service API")
                .description("AIOps 服务 API 文档 - AI 运筹运营运维一体化平台\n\n" +
                        "## 功能模块\n\n" +
                        "### 用户与认证\n" +
                        "- **认证管理**: 用户注册、登录、登出、令牌刷新\n" +
                        "- **会话管理**: 会话验证、会话互斥、多设备会话管理、强制登出\n" +
                        "- **管理员功能**: 账号解锁、账号查询、系统配置管理\n\n" +
                        "### 资源管理\n" +
                        "- **资源管理**: IT 资源 CRUD、状态管理、审计日志\n" +
                        "- **资源关系管理**: 资源关系 CRUD、图遍历查询\n" +
                        "- **子图管理**: 子图 CRUD、权限管理、资源关联、拓扑查询\n\n" +
                        "## 认证方式\n\n" +
                        "除注册和登录接口外，所有接口需要在请求头中携带 JWT Token：\n" +
                        "```\n" +
                        "Authorization: Bearer <token>\n" +
                        "```")
                .version("1.0.0")
                .contact(new Contact()
                        .name("AIOps Team")
                        .email("aiops@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * 服务器配置
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("本地开发环境"),
                new Server()
                        .url("https://api.aiops.example.com")
                        .description("生产环境")
        );
    }

    /**
     * 安全认证组件配置
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 认证，请在登录接口获取 Token 后填入"));
    }
}
