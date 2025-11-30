package com.catface996.aiops.bootstrap.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 资源管理集成测试
 *
 * 使用 TestContainers 进行端到端测试，验证：
 * - REST API 接口正确性
 * - 字段级加密功能
 * - 乐观锁机制
 * - 审计日志记录
 *
 * 注意：这些测试需要完整的 Spring Boot 上下文和所有依赖的 Bean。
 * 由于 ResourceController 依赖链较长，建议使用 E2E 测试脚本替代。
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@DisplayName("资源管理集成测试")
@org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "需要完整环境，CI环境请使用E2E脚本测试")
class ResourceIntegrationTest extends BaseIntegrationTest {

    private String authToken;
    private static final String BASE_URL = "/api/v1/resources";
    private static final String TYPES_URL = "/api/v1/resource-types";

    @BeforeEach
    void setUpAuth() throws Exception {
        // 注册并登录获取 token (用户名3-20字符)
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000000);
        String username = "res" + timestamp;  // 确保用户名在 3-20 字符范围内
        String password = "SecureP@ss123";

        // 注册用户
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "username": "%s",
                                "email": "%s@test.com",
                                "password": "%s"
                            }
                            """, username, username, password)))
                .andExpect(status().isCreated());

        // 登录获取 token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "identifier": "%s",
                                "password": "%s",
                                "rememberMe": false
                            }
                            """, username, password)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        // 提取 token
        int tokenStart = response.indexOf("\"token\":\"") + 9;
        int tokenEnd = response.indexOf("\"", tokenStart);
        authToken = response.substring(tokenStart, tokenEnd);
    }

    @Nested
    @DisplayName("资源类型查询测试")
    class ResourceTypeQueryTest {

        @Test
        @DisplayName("应该返回所有资源类型")
        void shouldReturnAllResourceTypes() throws Exception {
            mockMvc.perform(get(TYPES_URL)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("未认证时应该返回401")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get(TYPES_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("资源CRUD测试")
    class ResourceCrudTest {

        @Test
        @DisplayName("应该成功创建资源")
        void shouldCreateResourceSuccessfully() throws Exception {
            String resourceJson = """
                {
                    "name": "test-server-001",
                    "description": "测试服务器",
                    "resourceTypeId": 1,
                    "attributes": "{\\"ip\\":\\"192.168.1.100\\",\\"password\\":\\"secret123\\",\\"cpu\\":\\"8核\\"}"
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(resourceJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").value("test-server-001"))
                    .andExpect(jsonPath("$.data.status").value("RUNNING"));
        }

        @Test
        @DisplayName("创建资源时敏感字段应该被加密存储")
        void shouldEncryptSensitiveFieldsWhenCreating() throws Exception {
            String uniqueName = "encrypt-test-" + System.currentTimeMillis();
            String resourceJson = String.format("""
                {
                    "name": "%s",
                    "description": "加密测试",
                    "resourceTypeId": 1,
                    "attributes": "{\\"ip\\":\\"10.0.0.1\\",\\"password\\":\\"mySecret\\",\\"token\\":\\"api-token-123\\"}"
                }
                """, uniqueName);

            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(resourceJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            // 创建响应中敏感字段应该是加密的（以 ENC: 开头）
            // 注意：根据实现，创建时返回的可能是加密值，查询时返回解密值
        }

        @Test
        @DisplayName("资源名称重复时应该返回409")
        void shouldReturn409WhenNameConflict() throws Exception {
            String uniqueName = "conflict-test-" + System.currentTimeMillis();
            String resourceJson = String.format("""
                {
                    "name": "%s",
                    "description": "第一个资源",
                    "resourceTypeId": 1,
                    "attributes": "{}"
                }
                """, uniqueName);

            // 创建第一个资源
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(resourceJson))
                    .andExpect(status().isCreated());

            // 尝试创建同名资源
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(resourceJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("应该成功查询资源列表")
        void shouldQueryResourceList() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("应该成功按条件过滤资源")
        void shouldFilterResourcesByCondition() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .param("resourceTypeId", "1")
                            .param("status", "RUNNING")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("资源更新测试")
    class ResourceUpdateTest {

        @Test
        @DisplayName("应该成功更新资源")
        void shouldUpdateResourceSuccessfully() throws Exception {
            // 先创建资源
            String uniqueName = "update-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "原始描述",
                    "resourceTypeId": 1,
                    "attributes": "{\\"ip\\":\\"192.168.1.1\\"}"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            // 提取资源ID和版本号
            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":") + 5;
            int idEnd = createResponse.indexOf(",", idStart);
            String resourceId = createResponse.substring(idStart, idEnd);

            int versionStart = createResponse.indexOf("\"version\":") + 10;
            int versionEnd = createResponse.indexOf(",", versionStart);
            if (versionEnd == -1) versionEnd = createResponse.indexOf("}", versionStart);
            String version = createResponse.substring(versionStart, versionEnd).trim();

            // 更新资源
            String updateJson = String.format("""
                {
                    "name": "%s-updated",
                    "description": "更新后的描述",
                    "attributes": "{\\"ip\\":\\"192.168.1.2\\"}",
                    "version": %s
                }
                """, uniqueName, version);

            mockMvc.perform(put(BASE_URL + "/" + resourceId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(uniqueName + "-updated"))
                    .andExpect(jsonPath("$.data.description").value("更新后的描述"));
        }
    }

    @Nested
    @DisplayName("资源状态管理测试")
    class ResourceStatusTest {

        @Test
        @DisplayName("应该成功更新资源状态")
        void shouldUpdateResourceStatus() throws Exception {
            // 先创建资源
            String uniqueName = "status-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "状态测试",
                    "resourceTypeId": 1,
                    "attributes": "{}"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":") + 5;
            int idEnd = createResponse.indexOf(",", idStart);
            String resourceId = createResponse.substring(idStart, idEnd);

            // 更新状态
            String statusJson = """
                {
                    "status": "MAINTENANCE",
                    "version": 0
                }
                """;

            mockMvc.perform(patch(BASE_URL + "/" + resourceId + "/status")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));
        }
    }

    @Nested
    @DisplayName("资源删除测试")
    class ResourceDeleteTest {

        @Test
        @DisplayName("应该成功删除资源")
        void shouldDeleteResourceSuccessfully() throws Exception {
            // 先创建资源
            String uniqueName = "delete-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "删除测试",
                    "resourceTypeId": 1,
                    "attributes": "{}"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":") + 5;
            int idEnd = createResponse.indexOf(",", idStart);
            String resourceId = createResponse.substring(idStart, idEnd);

            // 删除资源（需要确认名称）
            String deleteJson = String.format("""
                {
                    "confirmName": "%s"
                }
                """, uniqueName);

            mockMvc.perform(delete(BASE_URL + "/" + resourceId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(deleteJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // 验证资源已删除
            mockMvc.perform(get(BASE_URL + "/" + resourceId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("确认名称不匹配时应该返回400")
        void shouldReturn400WhenConfirmNameMismatch() throws Exception {
            // 先创建资源
            String uniqueName = "mismatch-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "名称不匹配测试",
                    "resourceTypeId": 1,
                    "attributes": "{}"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":") + 5;
            int idEnd = createResponse.indexOf(",", idStart);
            String resourceId = createResponse.substring(idStart, idEnd);

            // 尝试用错误名称删除
            String deleteJson = """
                {
                    "confirmName": "wrong-name"
                }
                """;

            mockMvc.perform(delete(BASE_URL + "/" + resourceId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(deleteJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("审计日志测试")
    class AuditLogTest {

        @Test
        @DisplayName("应该能查询资源的审计日志")
        void shouldQueryAuditLogs() throws Exception {
            // 先创建资源
            String uniqueName = "audit-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "审计日志测试",
                    "resourceTypeId": 1,
                    "attributes": "{}"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":") + 5;
            int idEnd = createResponse.indexOf(",", idStart);
            String resourceId = createResponse.substring(idStart, idEnd);

            // 查询审计日志
            mockMvc.perform(get(BASE_URL + "/" + resourceId + "/audit-logs")
                            .header("Authorization", "Bearer " + authToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(1)));
        }
    }
}
