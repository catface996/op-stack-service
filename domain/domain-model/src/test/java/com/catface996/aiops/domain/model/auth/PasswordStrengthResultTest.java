package com.catface996.aiops.domain.model.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordStrengthResult 单元测试
 *
 * 测试密码强度验证结果值对象的所有方法
 *
 * @author AI Assistant
 * @since 2025-01-26
 */
@DisplayName("密码强度验证结果测试")
class PasswordStrengthResultTest {

    @Test
    @DisplayName("应该创建有效的密码强度结果")
    void should_CreateValidResult_When_UsingValidFactory() {
        // When
        PasswordStrengthResult result = PasswordStrengthResult.valid();

        // Then
        assertTrue(result.isValid(), "结果应该是有效的");
        assertTrue(result.getErrors().isEmpty(), "错误列表应该为空");
        assertNull(result.getFirstError(), "第一个错误应该为空");
        assertEquals("", result.getAllErrorsAsString(), "所有错误字符串应该为空");
    }

    @Test
    @DisplayName("应该创建无效的密码强度结果（多个错误）")
    void should_CreateInvalidResult_When_UsingInvalidFactoryWithList() {
        // Given
        List<String> errors = Arrays.asList("密码长度至少为8个字符", "密码必须包含大写字母");

        // When
        PasswordStrengthResult result = PasswordStrengthResult.invalid(errors);

        // Then
        assertFalse(result.isValid(), "结果应该是无效的");
        assertEquals(2, result.getErrors().size(), "应该有2个错误");
        assertEquals("密码长度至少为8个字符", result.getFirstError(), "第一个错误应该匹配");
        assertEquals("密码长度至少为8个字符; 密码必须包含大写字母", result.getAllErrorsAsString());
    }

    @Test
    @DisplayName("应该创建无效的密码强度结果（单个错误）")
    void should_CreateInvalidResult_When_UsingInvalidFactoryWithSingleError() {
        // Given
        String error = "密码过于简单";

        // When
        PasswordStrengthResult result = PasswordStrengthResult.invalid(error);

        // Then
        assertFalse(result.isValid(), "结果应该是无效的");
        assertEquals(1, result.getErrors().size(), "应该有1个错误");
        assertEquals("密码过于简单", result.getFirstError(), "第一个错误应该匹配");
        assertEquals("密码过于简单", result.getAllErrorsAsString());
    }

    @Test
    @DisplayName("应该使用构造函数创建结果")
    void should_CreateResult_When_UsingConstructor() {
        // Given
        List<String> errors = Collections.singletonList("测试错误");

        // When
        PasswordStrengthResult result = new PasswordStrengthResult(false, errors);

        // Then
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("测试错误", result.getFirstError());
    }

    @Test
    @DisplayName("应该处理空错误列表")
    void should_HandleNullErrors_When_UsingConstructor() {
        // When
        PasswordStrengthResult result = new PasswordStrengthResult(true, null);

        // Then
        assertTrue(result.isValid());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("应该返回不可修改的错误列表")
    void should_ReturnUnmodifiableList_When_GettingErrors() {
        // Given
        List<String> errors = Arrays.asList("错误1", "错误2");
        PasswordStrengthResult result = PasswordStrengthResult.invalid(errors);

        // When & Then
        List<String> returnedErrors = result.getErrors();
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedErrors.add("新错误");
        }, "返回的错误列表应该是不可修改的");
    }

    @Test
    @DisplayName("应该正确生成toString")
    void should_GenerateCorrectString_When_CallingToString() {
        // Given
        PasswordStrengthResult result = PasswordStrengthResult.invalid("测试错误");

        // When
        String toString = result.toString();

        // Then
        assertTrue(toString.contains("valid=false"));
        assertTrue(toString.contains("测试错误"));
    }
}
