package com.catface996.aiops.domain.api.model.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 密码强度验证结果值对象
 * 
 * 用于返回密码强度验证的结果和错误信息
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public class PasswordStrengthResult {
    
    /**
     * 是否有效
     */
    private final boolean valid;
    
    /**
     * 错误信息列表
     */
    private final List<String> errors;
    
    // 构造函数
    public PasswordStrengthResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }
    
    /**
     * 创建一个有效的密码强度结果
     * 
     * @return valid password strength result
     */
    public static PasswordStrengthResult valid() {
        return new PasswordStrengthResult(true, Collections.emptyList());
    }
    
    /**
     * 创建一个无效的密码强度结果
     * 
     * @param errors error messages
     * @return invalid password strength result
     */
    public static PasswordStrengthResult invalid(List<String> errors) {
        return new PasswordStrengthResult(false, errors);
    }
    
    /**
     * 创建一个无效的密码强度结果（单个错误）
     * 
     * @param error error message
     * @return invalid password strength result
     */
    public static PasswordStrengthResult invalid(String error) {
        return new PasswordStrengthResult(false, Collections.singletonList(error));
    }
    
    // Getters
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * 获取第一个错误信息
     * 
     * @return first error message, or null if no errors
     */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }
    
    /**
     * 获取所有错误信息的字符串表示
     * 
     * @return all error messages joined by semicolon
     */
    public String getAllErrorsAsString() {
        return String.join("; ", errors);
    }
    
    @Override
    public String toString() {
        return "PasswordStrengthResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
