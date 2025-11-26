package com.catface996.aiops.bootstrap.integration;

import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证功能集成测试
 *
 * 测试用户注册、登录、登出的完整业务流程。
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
@DisplayName("认证功能集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    @Nested
    @DisplayName("用户注册测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RegisterTests {

        @Test
        @Order(1)
        @DisplayName("应该成功注册新用户")
        void should_RegisterSuccessfully_when_ValidRequest() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(
                    "testuser001",
                    "testuser001@example.com",
                    "SecureP@ss123"
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())  // 201 Created
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value("testuser001"))
                    .andExpect(jsonPath("$.data.email").value("testuser001@example.com"))
                    .andExpect(jsonPath("$.data.accountId").isNumber())
                    .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
        }

        @Test
        @Order(2)
        @DisplayName("应该拒绝重复用户名注册")
        void should_RejectRegistration_when_DuplicateUsername() throws Exception {
            // Arrange - 先注册一个用户
            RegisterRequest firstRequest = new RegisterRequest(
                    "duplicateuser",
                    "first@example.com",
                    "SecureP@ss123"
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(firstRequest)));

            // Arrange - 使用相同用户名再次注册
            RegisterRequest duplicateRequest = new RegisterRequest(
                    "duplicateuser",
                    "second@example.com",
                    "SecureP@ss123"
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(duplicateRequest)))
                    .andExpect(status().isConflict())  // 409 Conflict
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("用户名已存在")));
        }

        @Test
        @Order(3)
        @DisplayName("应该拒绝重复邮箱注册")
        void should_RejectRegistration_when_DuplicateEmail() throws Exception {
            // Arrange - 先注册一个用户
            RegisterRequest firstRequest = new RegisterRequest(
                    "emailuser1",
                    "duplicate@example.com",
                    "SecureP@ss123"
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(firstRequest)));

            // Arrange - 使用相同邮箱再次注册
            RegisterRequest duplicateRequest = new RegisterRequest(
                    "emailuser2",
                    "duplicate@example.com",
                    "SecureP@ss123"
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(duplicateRequest)))
                    .andExpect(status().isConflict())  // 409 Conflict
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("邮箱已存在")));
        }

        @Test
        @Order(4)
        @DisplayName("应该拒绝弱密码注册")
        void should_RejectRegistration_when_WeakPassword() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(
                    "weakpwduser",
                    "weakpwd@example.com",
                    "123456"  // 弱密码
            );

            // Act & Assert - 弱密码会被参数验证拦截
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
                    // 注：弱密码错误可能在参数验证层被拦截，message 可能是 "请求参数无效"
        }

        @Test
        @Order(5)
        @DisplayName("应该拒绝无效请求参数")
        void should_RejectRegistration_when_InvalidParameters() throws Exception {
            // Arrange - 空用户名
            RegisterRequest request = new RegisterRequest(
                    "",
                    "valid@example.com",
                    "SecureP@ss123"
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        private static final String TEST_PASSWORD = "SecureP@ss123";

        @Test
        @DisplayName("应该使用用户名成功登录并返回JWT Token")
        void should_LoginSuccessfully_when_ValidUsernameAndPassword() throws Exception {
            // Arrange - 先注册用户
            String uniqueUsername = "u" + (System.currentTimeMillis() % 100000000);
            String uniqueEmail = uniqueUsername + "@example.com";
            RegisterRequest registerRequest = new RegisterRequest(
                    uniqueUsername,
                    uniqueEmail,
                    TEST_PASSWORD
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(registerRequest)));

            // Arrange - 登录请求
            LoginRequest loginRequest = LoginRequest.of(uniqueUsername, TEST_PASSWORD, false);

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.token").value(startsWith("eyJ")))
                    .andExpect(jsonPath("$.data.sessionId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userInfo.username").value(uniqueUsername))
                    .andExpect(jsonPath("$.data.expiresIn").isNumber());
        }

        @Test
        @DisplayName("应该使用邮箱成功登录")
        void should_LoginSuccessfully_when_ValidEmailAndPassword() throws Exception {
            // Arrange - 先注册用户
            String uniqueUsername = "em" + (System.currentTimeMillis() % 100000000);
            String uniqueEmail = uniqueUsername + "@example.com";
            RegisterRequest registerRequest = new RegisterRequest(
                    uniqueUsername,
                    uniqueEmail,
                    TEST_PASSWORD
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(registerRequest)));

            // Arrange - 使用邮箱登录
            LoginRequest loginRequest = LoginRequest.of(uniqueEmail, TEST_PASSWORD, false);

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty());
        }

        @Test
        @DisplayName("应该拒绝错误密码登录")
        void should_RejectLogin_when_WrongPassword() throws Exception {
            // Arrange - 先注册用户
            String uniqueUsername = "wp" + (System.currentTimeMillis() % 100000000);
            RegisterRequest registerRequest = new RegisterRequest(
                    uniqueUsername,
                    uniqueUsername + "@example.com",
                    TEST_PASSWORD
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(registerRequest)));

            // Arrange - 使用错误密码登录
            LoginRequest loginRequest = LoginRequest.of(uniqueUsername, "WrongPassword123!", false);

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("用户名或密码错误")));
        }

        @Test
        @DisplayName("应该拒绝不存在的用户登录")
        void should_RejectLogin_when_UserNotExists() throws Exception {
            // Arrange
            LoginRequest loginRequest = LoginRequest.of("nonexistentuser", TEST_PASSWORD, false);

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("用户名或密码错误")));
        }

        @Test
        @DisplayName("应该在连续5次登录失败后锁定账号")
        void should_LockAccount_when_5ConsecutiveFailures() throws Exception {
            // Arrange - 先注册用户
            String uniqueUsername = "lk" + (System.currentTimeMillis() % 100000000);
            RegisterRequest registerRequest = new RegisterRequest(
                    uniqueUsername,
                    uniqueUsername + "@example.com",
                    TEST_PASSWORD
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(registerRequest)));

            // Act - 连续5次错误密码登录
            LoginRequest wrongLoginRequest = LoginRequest.of(uniqueUsername, "WrongPassword123!", false);
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(wrongLoginRequest)));
            }

            // Assert - 第6次尝试应该显示账号锁定，返回 423 Locked
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(wrongLoginRequest)))
                    .andExpect(status().isLocked())  // 423 Locked
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("锁定")));
        }
    }

    @Nested
    @DisplayName("用户登出测试")
    class LogoutTests {

        @Test
        @DisplayName("应该成功登出")
        void should_LogoutSuccessfully_when_ValidToken() throws Exception {
            // Arrange - 注册并登录获取 Token
            String uniqueUsername = "lo" + (System.currentTimeMillis() % 100000000);
            RegisterRequest registerRequest = new RegisterRequest(
                    uniqueUsername,
                    uniqueUsername + "@example.com",
                    "SecureP@ss123"
            );
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(registerRequest)));

            LoginRequest loginRequest = LoginRequest.of(uniqueUsername, "SecureP@ss123", false);
            MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andReturn();

            String responseJson = loginResult.getResponse().getContentAsString();
            String token = objectMapper.readTree(responseJson).path("data").path("token").asText();

            // Act & Assert - 登出
            mockMvc.perform(post(LOGOUT_URL)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("应该拒绝无Token的登出请求")
        void should_RejectLogout_when_NoToken() throws Exception {
            // Act & Assert
            mockMvc.perform(post(LOGOUT_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("完整业务流程测试")
    class FullFlowTests {

        @Test
        @DisplayName("完整的注册-登录-登出流程")
        void should_CompleteFullAuthFlow() throws Exception {
            String uniqueUsername = "flow" + (System.currentTimeMillis() % 100000000);
            String email = uniqueUsername + "@example.com";
            String password = "SecureP@ss123";

            // Step 1: 注册
            RegisterRequest registerRequest = new RegisterRequest(uniqueUsername, email, password);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registerRequest)))
                    .andExpect(status().isCreated())  // 201 Created
                    .andExpect(jsonPath("$.success").value(true));

            // Step 2: 登录
            LoginRequest loginRequest = LoginRequest.of(uniqueUsername, password, false);
            MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .path("data").path("token").asText();

            // Step 3: 登出
            mockMvc.perform(post(LOGOUT_URL)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 4: 验证登出后 Token 失效（访问受保护接口）
            mockMvc.perform(get("/api/v1/session/validate")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(jsonPath("$.data.valid").value(false));
        }
    }
}
