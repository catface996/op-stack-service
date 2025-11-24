package com.catface996.aiops.bootstrap.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCryptPasswordEncoder 专项测试
 * 
 * 验证需求：
 * - REQ-FR-004: 密码安全存储
 * - REQ-NFR-PERF-003: BCrypt 性能要求（< 500ms）
 * 
 * 测试内容：
 * 1. 加密后的密码长度为 60 字符
 * 2. 相同密码加密结果不同（盐值生效）
 * 3. 单次加密/验证时间 < 500ms
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@SpringBootTest
class BCryptPasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 测试1：验证加密后的密码长度为 60 字符
     * 
     * BCrypt 算法生成的哈希值固定为 60 个字符
     */
    @Test
    void testEncodedPasswordLengthIs60Characters() {
        // Given: 一个原始密码
        String rawPassword = "SecureP@ss123";
        
        // When: 加密密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then: 加密后的密码长度应该为 60 字符
        assertEquals(60, encodedPassword.length(), 
            "BCrypt encoded password must be exactly 60 characters");
        
        // 验证多个不同的密码，确保长度都是 60
        String[] testPasswords = {
            "short",
            "MediumLength123!",
            "VeryLongPasswordWithManyCharacters123456789!@#$%^&*()",
            "P@ssw0rd",
            "Test123!@#"
        };
        
        for (String password : testPasswords) {
            String encoded = passwordEncoder.encode(password);
            assertEquals(60, encoded.length(), 
                "All BCrypt encoded passwords must be 60 characters, failed for: " + password);
        }
    }

    /**
     * 测试2：验证相同密码加密结果不同（盐值生效）
     * 
     * BCrypt 每次加密都会生成不同的盐值，因此相同密码的加密结果应该不同
     */
    @Test
    void testSamePasswordGeneratesDifferentHashes() {
        // Given: 同一个原始密码
        String rawPassword = "SecureP@ss123";
        
        // When: 对同一密码加密多次
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        String encoded3 = passwordEncoder.encode(rawPassword);
        
        // Then: 每次加密结果都应该不同（因为盐值不同）
        assertNotEquals(encoded1, encoded2, 
            "BCrypt should generate different hashes for the same password (salt1 vs salt2)");
        assertNotEquals(encoded2, encoded3, 
            "BCrypt should generate different hashes for the same password (salt2 vs salt3)");
        assertNotEquals(encoded1, encoded3, 
            "BCrypt should generate different hashes for the same password (salt1 vs salt3)");
        
        // 但所有加密结果都应该能匹配原始密码
        assertTrue(passwordEncoder.matches(rawPassword, encoded1), 
            "First encoded password should match raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encoded2), 
            "Second encoded password should match raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encoded3), 
            "Third encoded password should match raw password");
    }

    /**
     * 测试3：验证单次加密时间 < 500ms
     * 
     * 需求：REQ-NFR-PERF-003 - BCrypt 单次验证时间应小于 500ms
     */
    @Test
    void testEncryptionPerformance() {
        // Given: 一个测试密码
        String rawPassword = "SecureP@ss123";
        
        // When: 测量加密时间
        long startTime = System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        long encryptionTime = System.currentTimeMillis() - startTime;
        
        // Then: 加密时间应该小于 500ms
        assertTrue(encryptionTime < 500, 
            String.format("Encryption time should be < 500ms, but was %dms", encryptionTime));
        
        System.out.println("BCrypt encryption time: " + encryptionTime + "ms");
    }

    /**
     * 测试4：验证单次验证时间 < 500ms
     * 
     * 需求：REQ-NFR-PERF-003 - BCrypt 单次验证时间应小于 500ms
     */
    @Test
    void testVerificationPerformance() {
        // Given: 一个已加密的密码
        String rawPassword = "SecureP@ss123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // When: 测量验证时间
        long startTime = System.currentTimeMillis();
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        long verificationTime = System.currentTimeMillis() - startTime;
        
        // Then: 验证时间应该小于 500ms
        assertTrue(verificationTime < 500, 
            String.format("Verification time should be < 500ms, but was %dms", verificationTime));
        assertTrue(matches, "Password should match");
        
        System.out.println("BCrypt verification time: " + verificationTime + "ms");
    }

    /**
     * 测试5：综合性能测试 - 多次加密和验证
     * 
     * 验证在多次操作下，平均性能仍然满足要求
     */
    @Test
    void testAveragePerformance() {
        String rawPassword = "SecureP@ss123";
        int iterations = 10;
        
        // 测试加密性能
        long totalEncryptionTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            passwordEncoder.encode(rawPassword);
            totalEncryptionTime += (System.currentTimeMillis() - start);
        }
        long avgEncryptionTime = totalEncryptionTime / iterations;
        
        // 测试验证性能
        String encodedPassword = passwordEncoder.encode(rawPassword);
        long totalVerificationTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            passwordEncoder.matches(rawPassword, encodedPassword);
            totalVerificationTime += (System.currentTimeMillis() - start);
        }
        long avgVerificationTime = totalVerificationTime / iterations;
        
        // 验证平均时间都小于 500ms
        assertTrue(avgEncryptionTime < 500, 
            String.format("Average encryption time should be < 500ms, but was %dms", avgEncryptionTime));
        assertTrue(avgVerificationTime < 500, 
            String.format("Average verification time should be < 500ms, but was %dms", avgVerificationTime));
        
        System.out.println("Average BCrypt encryption time: " + avgEncryptionTime + "ms");
        System.out.println("Average BCrypt verification time: " + avgVerificationTime + "ms");
    }

    /**
     * 测试6：验证 BCrypt Work Factor 配置
     * 
     * 确保使用的是 Work Factor = 10
     */
    @Test
    void testBCryptWorkFactor() {
        // BCrypt 加密结果的格式：$2a$10$...
        // 其中 $2a$ 是算法标识，$10$ 是 Work Factor
        String rawPassword = "SecureP@ss123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 验证加密结果以 $2a$10$ 或 $2b$10$ 开头（不同版本的 BCrypt）
        assertTrue(encodedPassword.startsWith("$2a$10$") || encodedPassword.startsWith("$2b$10$"), 
            "BCrypt should use Work Factor 10, encoded password: " + encodedPassword);
    }

    /**
     * 测试7：验证密码加密的基本功能
     * 
     * 确保加密和验证功能正常工作
     */
    @Test
    void testBasicEncryptionAndVerification() {
        String rawPassword = "SecureP@ss123";
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 验证加密后的密码不等于原始密码
        assertNotEquals(rawPassword, encodedPassword, 
            "Encoded password should not equal raw password");
        
        // 验证正确的密码能匹配
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), 
            "Correct password should match");
        
        // 验证错误的密码不能匹配
        assertFalse(passwordEncoder.matches("WrongPassword", encodedPassword), 
            "Wrong password should not match");
        assertFalse(passwordEncoder.matches("securep@ss123", encodedPassword), 
            "Case-sensitive: wrong case should not match");
        assertFalse(passwordEncoder.matches("SecureP@ss124", encodedPassword), 
            "Different password should not match");
    }
}
