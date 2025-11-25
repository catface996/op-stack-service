package com.catface996.aiops.domain.impl.service.auth;

import com.catface996.aiops.domain.model.auth.PasswordStrengthResult;
import com.catface996.aiops.repository.auth.AccountRepository;
import com.catface996.aiops.repository.auth.SessionRepository;
import com.catface996.aiops.infrastructure.cache.api.service.LoginAttemptCache;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 认证领域服务实现类测试
 *
 * 测试任务10：密码管理领域服务
 * - 密码加密和验证
 * - 密码强度验证
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@DisplayName("认证领域服务测试")
class AuthDomainServiceImplTest {

    private AuthDomainServiceImpl authDomainService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 使用BCryptPasswordEncoder，Work Factor = 10（与生产环境一致）
        passwordEncoder = new BCryptPasswordEncoder(10);
        // Mock依赖
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        SessionCache sessionCache = mock(SessionCache.class);
        SessionRepository sessionRepository = mock(SessionRepository.class);
        LoginAttemptCache loginAttemptCache = mock(LoginAttemptCache.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        authDomainService = new AuthDomainServiceImpl(passwordEncoder, jwtTokenProvider, sessionCache, sessionRepository, loginAttemptCache, accountRepository);
    }

    @Nested
    @DisplayName("密码加密测试")
    class EncryptPasswordTest {

        @Test
        @DisplayName("应该成功加密密码")
        void shouldEncryptPassword() {
            // Given
            String rawPassword = "SecureP@ss123";

            // When
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // Then
            assertNotNull(encryptedPassword);
            assertEquals(60, encryptedPassword.length(), "BCrypt加密后的密码应该是60个字符");
            assertTrue(encryptedPassword.startsWith("$2a$") || encryptedPassword.startsWith("$2b$"),
                    "BCrypt加密后的密码应该以$2a$或$2b$开头");
        }

        @Test
        @DisplayName("相同密码加密后应该得到不同的结果（盐值生效）")
        void shouldGenerateDifferentHashesForSamePassword() {
            // Given
            String rawPassword = "SecureP@ss123";

            // When
            String encrypted1 = authDomainService.encryptPassword(rawPassword);
            String encrypted2 = authDomainService.encryptPassword(rawPassword);

            // Then
            assertNotEquals(encrypted1, encrypted2, "相同密码加密后应该得到不同的结果（盐值生效）");
        }

        @Test
        @DisplayName("密码加密后应该无法反向解密")
        void shouldNotBeAbleToDecryptPassword() {
            // Given
            String rawPassword = "SecureP@ss123";

            // When
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // Then
            assertNotEquals(rawPassword, encryptedPassword, "加密后的密码不应该等于原始密码");
            assertFalse(encryptedPassword.contains(rawPassword), "加密后的密码不应该包含原始密码");
        }

        @Test
        @DisplayName("空密码应该抛出异常")
        void shouldThrowExceptionForEmptyPassword() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.encryptPassword("");
            }, "空密码应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null密码应该抛出异常")
        void shouldThrowExceptionForNullPassword() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.encryptPassword(null);
            }, "null密码应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("单次加密时间应该小于500ms")
        void shouldEncryptWithinPerformanceLimit() {
            // Given
            String rawPassword = "SecureP@ss123";

            // When
            long startTime = System.currentTimeMillis();
            authDomainService.encryptPassword(rawPassword);
            long endTime = System.currentTimeMillis();

            // Then
            long duration = endTime - startTime;
            assertTrue(duration < 500, "单次加密时间应该小于500ms，实际耗时：" + duration + "ms");
        }
    }

    @Nested
    @DisplayName("密码验证测试")
    class VerifyPasswordTest {

        @Test
        @DisplayName("相同密码验证应该成功")
        void shouldVerifySamePassword() {
            // Given
            String rawPassword = "SecureP@ss123";
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // When
            boolean matches = authDomainService.verifyPassword(rawPassword, encryptedPassword);

            // Then
            assertTrue(matches, "相同密码验证应该成功");
        }

        @Test
        @DisplayName("不同密码验证应该失败")
        void shouldFailForDifferentPassword() {
            // Given
            String rawPassword = "SecureP@ss123";
            String wrongPassword = "WrongP@ss456";
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // When
            boolean matches = authDomainService.verifyPassword(wrongPassword, encryptedPassword);

            // Then
            assertFalse(matches, "不同密码验证应该失败");
        }

        @Test
        @DisplayName("大小写敏感验证")
        void shouldBeCaseSensitive() {
            // Given
            String rawPassword = "SecureP@ss123";
            String wrongCasePassword = "securep@ss123";
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // When
            boolean matches = authDomainService.verifyPassword(wrongCasePassword, encryptedPassword);

            // Then
            assertFalse(matches, "密码验证应该区分大小写");
        }

        @Test
        @DisplayName("空原始密码应该抛出异常")
        void shouldThrowExceptionForEmptyRawPassword() {
            // Given
            String encryptedPassword = authDomainService.encryptPassword("SecureP@ss123");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.verifyPassword("", encryptedPassword);
            }, "空原始密码应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("空加密密码应该抛出异常")
        void shouldThrowExceptionForEmptyEncodedPassword() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.verifyPassword("SecureP@ss123", "");
            }, "空加密密码应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("单次验证时间应该小于500ms")
        void shouldVerifyWithinPerformanceLimit() {
            // Given
            String rawPassword = "SecureP@ss123";
            String encryptedPassword = authDomainService.encryptPassword(rawPassword);

            // When
            long startTime = System.currentTimeMillis();
            authDomainService.verifyPassword(rawPassword, encryptedPassword);
            long endTime = System.currentTimeMillis();

            // Then
            long duration = endTime - startTime;
            assertTrue(duration < 500, "单次验证时间应该小于500ms，实际耗时：" + duration + "ms");
        }
    }

    @Nested
    @DisplayName("密码强度验证测试")
    class ValidatePasswordStrengthTest {

        private static final String VALID_USERNAME = "john_doe";
        private static final String VALID_EMAIL = "john@example.com";

        @Test
        @DisplayName("合法密码应该验证通过")
        void shouldAcceptValidPassword() {
            // Given
            String validPassword = "SecureP@ss123";

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    validPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            if (!result.isValid()) {
                System.out.println("验证失败，错误信息：" + result.getAllErrorsAsString());
            }
            assertTrue(result.isValid(), "合法密码应该验证通过，但实际错误：" + result.getAllErrorsAsString());
            assertTrue(result.getErrors().isEmpty(), "合法密码不应该有错误信息");
        }

        @Test
        @DisplayName("密码长度小于8个字符应该验证失败")
        void shouldRejectPasswordShorterThan8Characters() {
            // Given
            String shortPassword = "Ab@1";

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    shortPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "密码长度小于8个字符应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("至少为8个字符")),
                    "应该包含长度不足的错误信息");
        }

        @Test
        @DisplayName("密码长度大于64个字符应该验证失败")
        void shouldRejectPasswordLongerThan64Characters() {
            // Given
            String longPassword = "A".repeat(65) + "bC@1";

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    longPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "密码长度大于64个字符应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("最多为64个字符")),
                    "应该包含长度过长的错误信息");
        }

        @Test
        @DisplayName("密码只包含2类字符应该验证失败")
        void shouldRejectPasswordWithOnly2CharacterTypes() {
            // Given
            String password = "abcdefgh123";  // 只包含小写字母和数字

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    password, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "密码只包含2类字符应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("至少3类")),
                    "应该包含字符类型不足的错误信息");
        }

        @Test
        @DisplayName("密码包含用户名应该验证失败")
        void shouldRejectPasswordContainingUsername() {
            // Given
            String username = "john_doe";
            String password = "John_doe@123";  // 包含用户名

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    password, username, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "密码包含用户名应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("不能包含用户名")),
                    "应该包含包含用户名的错误信息");
        }

        @Test
        @DisplayName("密码包含邮箱前缀应该验证失败")
        void shouldRejectPasswordContainingEmailPrefix() {
            // Given
            String email = "john@example.com";
            String password = "John@Pass123";  // 包含邮箱前缀john

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    password, VALID_USERNAME, email);

            // Then
            assertFalse(result.isValid(), "密码包含邮箱前缀应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("不能包含邮箱")),
                    "应该包含包含邮箱的错误信息");
        }

        @Test
        @DisplayName("常见弱密码应该验证失败 - password123")
        void shouldRejectCommonWeakPassword_password123() {
            // Given
            String weakPassword = "Password123@";

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    weakPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "常见弱密码password123应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("过于简单")),
                    "应该包含密码过于简单的错误信息");
        }

        @Test
        @DisplayName("常见弱密码应该验证失败 - 12345678")
        void shouldRejectCommonWeakPassword_12345678() {
            // Given
            String weakPassword = "Abc@12345678";

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    weakPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "常见弱密码12345678应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("过于简单")),
                    "应该包含密码过于简单的错误信息");
        }

        @Test
        @DisplayName("连续字符密码应该验证失败")
        void shouldRejectPasswordWithConsecutiveCharacters() {
            // Given
            String weakPassword = "Abc@1234567";  // 包含1234567（超过4位连续数字）

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    weakPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "连续字符密码应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("过于简单")),
                    "应该包含密码过于简单的错误信息");
        }

        @Test
        @DisplayName("重复字符密码应该验证失败")
        void shouldRejectPasswordWithRepeatedCharacters() {
            // Given
            String weakPassword = "Aaaaaaa@123";  // 包含7个重复字符（>= 6个）

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    weakPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            if (result.isValid()) {
                System.out.println("ERROR: 重复字符密码应该被拒绝，但被接受了。密码：" + weakPassword);
                System.out.println("转换为小写：" + weakPassword.toLowerCase());
            }
            assertFalse(result.isValid(), "重复字符密码应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("过于简单")),
                    "应该包含密码过于简单的错误信息");
        }

        @Test
        @DisplayName("键盘序列密码应该验证失败")
        void shouldRejectPasswordWithKeyboardSequence() {
            // Given
            String weakPassword = "Qwerty@123";  // 包含qwerty

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    weakPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "键盘序列密码应该验证失败");
            assertTrue(result.getErrors().stream()
                    .anyMatch(error -> error.contains("过于简单")),
                    "应该包含密码过于简单的错误信息");
        }

        @Test
        @DisplayName("多个错误应该全部返回")
        void shouldReturnAllErrors() {
            // Given
            String badPassword = "abc";  // 太短、字符类型不足

            // When
            PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                    badPassword, VALID_USERNAME, VALID_EMAIL);

            // Then
            assertFalse(result.isValid(), "不合法密码应该验证失败");
            assertTrue(result.getErrors().size() >= 2, "应该返回多个错误信息");
        }

        @Test
        @DisplayName("null密码应该抛出异常")
        void shouldThrowExceptionForNullPassword() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.validatePasswordStrength(null, VALID_USERNAME, VALID_EMAIL);
            }, "null密码应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null用户名应该抛出异常")
        void shouldThrowExceptionForNullUsername() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.validatePasswordStrength("SecureP@ss123", null, VALID_EMAIL);
            }, "null用户名应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null邮箱应该抛出异常")
        void shouldThrowExceptionForNullEmail() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.validatePasswordStrength("SecureP@ss123", VALID_USERNAME, null);
            }, "null邮箱应该抛出IllegalArgumentException");
        }
    }
}
