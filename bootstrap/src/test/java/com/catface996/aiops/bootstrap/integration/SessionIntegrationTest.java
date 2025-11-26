package com.catface996.aiops.bootstrap.integration;

import com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest;
import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 会话管理集成测试
 *
 * 测试会话验证、强制登出其他设备等业务流程。
 * 使用 TestContainers 启动真实的 MySQL 和 Redis 进行测试。
 *
 * 遵循测试最佳实践：
 * - 测试命名：should_期望结果_when_条件
 * - 测试结构：AAA 模式（Arrange-Act-Assert）
 * - 测试独立性：每个测试独立，无共享状态
 *
 * @author AI Assistant
 * @since 2025-01-26
 */
@DisplayName("会话管理集成测试")
class SessionIntegrationTest extends BaseIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String VALIDATE_URL = "/api/v1/session/validate";
    private static final String FORCE_LOGOUT_URL = "/api/v1/session/force-logout-others";

    /**
     * 辅助方法：注册用户并返回用户名
     */
    private String registerUser(String usernamePrefix) throws Exception {
        String username = usernamePrefix.substring(0, Math.min(2, usernamePrefix.length()))
                + (System.currentTimeMillis() % 100000000);
        RegisterRequest request = new RegisterRequest(
                username,
                username + "@example.com",
                "SecureP@ss123"
        );
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
        return username;
    }

    /**
     * 辅助方法：登录并返回 Token
     */
    private String loginAndGetToken(String username, String password, boolean rememberMe) throws Exception {
        LoginRequest loginRequest = LoginRequest.of(username, password, rememberMe);
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).path("data").path("token").asText();
    }

    @Nested
    @DisplayName("会话验证测试")
    class ValidateSessionTests {

        @Test
        @DisplayName("应该成功验证有效会话")
        void should_ValidateSuccessfully_when_ValidSession() throws Exception {
            // Arrange - 注册并登录
            String username = registerUser("validate");
            String token = loginAndGetToken(username, "SecureP@ss123", false);

            // Act & Assert
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.valid").value(true))
                    .andExpect(jsonPath("$.data.sessionId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userInfo.username").value(username))
                    .andExpect(jsonPath("$.data.remainingSeconds").isNumber());
        }

        @Test
        @DisplayName("应该返回会话无效当登出后验证")
        void should_ReturnInvalid_when_ValidatingAfterLogout() throws Exception {
            // Arrange - 注册、登录、登出
            String username = registerUser("logoutvalidate");
            String token = loginAndGetToken(username, "SecureP@ss123", false);

            // 登出
            mockMvc.perform(post(LOGOUT_URL)
                    .header("Authorization", "Bearer " + token));

            // Act & Assert - 验证应该返回无效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.valid").value(false));
        }

        @Test
        @DisplayName("应该拒绝无效Token的验证请求")
        void should_RejectValidation_when_InvalidToken() throws Exception {
            // Act & Assert
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("应该拒绝缺少Authorization头的验证请求")
        void should_RejectValidation_when_NoAuthorizationHeader() throws Exception {
            // Act & Assert
            mockMvc.perform(get(VALIDATE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("强制登出其他设备测试")
    class ForceLogoutOthersTests {

        @Test
        @DisplayName("应该成功强制登出其他设备")
        void should_ForceLogoutSuccessfully_when_ValidRequest() throws Exception {
            // Arrange - 注册并登录
            String username = registerUser("forcelogout");
            String password = "SecureP@ss123";
            String token = loginAndGetToken(username, password, false);

            // Act & Assert - 强制登出其他设备
            ForceLogoutRequest request = ForceLogoutRequest.of("Bearer " + token, password);
            mockMvc.perform(post(FORCE_LOGOUT_URL)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.sessionId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userInfo.username").value(username));
        }

        @Test
        @DisplayName("应该使旧Token失效")
        void should_InvalidateOldToken_when_ForceLogoutOthers() throws Exception {
            // Arrange - 注册并登录获取初始 Token
            String username = registerUser("invalidateold");
            String password = "SecureP@ss123";
            String oldToken = loginAndGetToken(username, password, false);

            // Act - 强制登出其他设备获取新 Token
            ForceLogoutRequest request = ForceLogoutRequest.of("Bearer " + oldToken, password);
            MvcResult result = mockMvc.perform(post(FORCE_LOGOUT_URL)
                            .header("Authorization", "Bearer " + oldToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String newToken = objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data").path("token").asText();

            // Assert - 旧 Token 应该失效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + oldToken))
                    .andExpect(jsonPath("$.data.valid").value(false));

            // Assert - 新 Token 应该有效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + newToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.valid").value(true));
        }

        @Test
        @DisplayName("应该拒绝错误密码的强制登出请求")
        void should_RejectForceLogout_when_WrongPassword() throws Exception {
            // Arrange - 注册并登录
            String username = registerUser("wrongpwdforce");
            String token = loginAndGetToken(username, "SecureP@ss123", false);

            // Act & Assert
            ForceLogoutRequest request = ForceLogoutRequest.of("Bearer " + token, "WrongP@ss123");
            mockMvc.perform(post(FORCE_LOGOUT_URL)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("密码")));
        }

        @Test
        @DisplayName("应该拒绝无Token的强制登出请求")
        void should_RejectForceLogout_when_NoToken() throws Exception {
            // Act & Assert
            ForceLogoutRequest request = ForceLogoutRequest.of("", "SecureP@ss123");
            mockMvc.perform(post(FORCE_LOGOUT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("记住我功能测试")
    class RememberMeTests {

        @Test
        @DisplayName("记住我登录应该返回更长的过期时间")
        void should_ReturnLongerExpiration_when_RememberMeEnabled() throws Exception {
            // Arrange - 注册用户
            String username = registerUser("rememberme");
            String password = "SecureP@ss123";

            // Act - 普通登录
            LoginRequest normalLogin = LoginRequest.of(username, password, false);
            MvcResult normalResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(normalLogin)))
                    .andExpect(status().isOk())
                    .andReturn();

            long normalExpiresIn = objectMapper.readTree(normalResult.getResponse().getContentAsString())
                    .path("data").path("expiresIn").asLong();

            // 注册另一个用户用于记住我测试
            String rememberMeUsername = registerUser("rememberme2");

            // Act - 记住我登录
            LoginRequest rememberMeLogin = LoginRequest.of(rememberMeUsername, password, true);
            MvcResult rememberMeResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(rememberMeLogin)))
                    .andExpect(status().isOk())
                    .andReturn();

            long rememberMeExpiresIn = objectMapper.readTree(rememberMeResult.getResponse().getContentAsString())
                    .path("data").path("expiresIn").asLong();

            // Assert - 记住我的过期时间应该更长
            assert rememberMeExpiresIn > normalExpiresIn :
                    "记住我的过期时间应该更长: normal=" + normalExpiresIn + ", rememberMe=" + rememberMeExpiresIn;
        }
    }

    @Nested
    @DisplayName("会话互斥测试")
    class SessionMutexTests {

        @Test
        @DisplayName("同一用户多次登录应该创建多个会话")
        void should_CreateMultipleSessions_when_SameUserLoginsMultipleTimes() throws Exception {
            // Arrange - 注册用户
            String username = registerUser("multiplesession");
            String password = "SecureP@ss123";

            // Act - 第一次登录
            String token1 = loginAndGetToken(username, password, false);

            // Act - 第二次登录
            String token2 = loginAndGetToken(username, password, false);

            // Assert - 两个 Token 都应该有效（不同的会话）
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.valid").value(true));

            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.valid").value(true));

            // Assert - 两个 Token 不相同
            assert !token1.equals(token2) : "两次登录应该产生不同的 Token";
        }

        @Test
        @DisplayName("强制登出后所有旧会话应该失效")
        void should_InvalidateAllOldSessions_when_ForceLogoutOthers() throws Exception {
            // Arrange - 注册用户并创建多个会话
            String username = registerUser("forcelogoutall");
            String password = "SecureP@ss123";

            // 创建两个会话
            String token1 = loginAndGetToken(username, password, false);
            String token2 = loginAndGetToken(username, password, false);

            // 验证两个会话都有效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(jsonPath("$.data.valid").value(true));
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token2))
                    .andExpect(jsonPath("$.data.valid").value(true));

            // Act - 使用 token1 强制登出其他设备
            ForceLogoutRequest request = ForceLogoutRequest.of("Bearer " + token1, password);
            MvcResult result = mockMvc.perform(post(FORCE_LOGOUT_URL)
                            .header("Authorization", "Bearer " + token1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String newToken = objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data").path("token").asText();

            // Assert - 所有旧会话都应该失效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(jsonPath("$.data.valid").value(false));

            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + token2))
                    .andExpect(jsonPath("$.data.valid").value(false));

            // Assert - 新 Token 应该有效
            mockMvc.perform(get(VALIDATE_URL)
                            .header("Authorization", "Bearer " + newToken))
                    .andExpect(jsonPath("$.data.valid").value(true));
        }
    }
}
