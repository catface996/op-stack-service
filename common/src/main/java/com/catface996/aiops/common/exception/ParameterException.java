package com.catface996.aiops.common.exception;

import com.catface996.aiops.common.enums.ErrorCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 参数异常
 *
 * <p>用于请求参数验证失败的场景。</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>输入验证失败</li>
 *   <li>参数格式错误</li>
 *   <li>参数取值范围错误</li>
 *   <li>需要返回详细的字段错误信息</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>密码强度不符合要求</li>
 *   <li>邮箱格式错误</li>
 *   <li>手机号格式错误</li>
 *   <li>参数长度超出限制</li>
 *   <li>枚举值不在允许范围</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 方式1：使用枚举，无验证错误详情
 * throw new ParameterException(ParamErrorCode.INVALID_FORMAT);
 *
 * // 方式2：使用枚举 + 验证错误详情
 * throw new ParameterException(ParamErrorCode.INVALID_PASSWORD, validationErrors);
 *
 * // 方式3：使用枚举 + 自定义消息 + 验证错误详情
 * throw new ParameterException(ParamErrorCode.INVALID_PASSWORD, "密码太弱", validationErrors);
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
@Getter
public class ParameterException extends BaseException {

    /**
     * 验证错误详情（可选，用于字段级别错误）
     */
    private final List<String> validationErrors;

    // ==================== 使用 ErrorCode 枚举 ====================

    /**
     * 使用 ErrorCode 枚举（无验证错误详情）
     *
     * @param errorCode 错误码枚举
     */
    public ParameterException(ErrorCode errorCode) {
        super(errorCode);
        this.validationErrors = Collections.emptyList();
    }

    /**
     * 使用 ErrorCode 枚举 + 验证错误详情
     *
     * @param errorCode 错误码枚举
     * @param validationErrors 验证错误详情
     */
    public ParameterException(ErrorCode errorCode, List<String> validationErrors) {
        super(errorCode);
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyList();
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息 + 验证错误详情
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     * @param validationErrors 验证错误详情
     */
    public ParameterException(ErrorCode errorCode, String customMessage, List<String> validationErrors) {
        super(errorCode, customMessage);
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyList();
    }

    // ==================== 传统方式（向后兼容） ====================

    /**
     * 传统方式：String 错误码 + String 消息
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     */
    public ParameterException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.validationErrors = Collections.emptyList();
    }

    /**
     * 传统方式：String 错误码 + String 消息 + 验证错误详情
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param validationErrors 验证错误详情
     */
    public ParameterException(String errorCode, String errorMessage, List<String> validationErrors) {
        super(errorCode, errorMessage);
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyList();
    }

    /**
     * 传统方式：String 错误码 + String 消息 + 原因
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param cause 原因
     */
    public ParameterException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
        this.validationErrors = Collections.emptyList();
    }

    /**
     * 传统方式：String 错误码 + String 消息 + 验证错误详情 + 原因
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param validationErrors 验证错误详情
     * @param cause 原因
     */
    public ParameterException(String errorCode, String errorMessage, List<String> validationErrors, Throwable cause) {
        super(errorCode, errorMessage, cause);
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyList();
    }
}
