package com.catface996.aiops.common.exception;

import com.catface996.aiops.common.enums.ErrorCode;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 基础异常类
 *
 * <p>支持三种使用方式：</p>
 * <ol>
 *   <li>传入 ErrorCode 枚举（使用默认消息）</li>
 *   <li>传入 ErrorCode + 自定义消息（覆盖默认消息）</li>
 *   <li>传入 ErrorCode + 消息参数（格式化默认消息）</li>
 *   <li>传入 String errorCode + String message（向后兼容）</li>
 * </ol>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 方式1：使用默认消息
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
 *
 * // 方式2：自定义消息
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "密码错误次数过多");
 *
 * // 方式3：参数化消息
 * throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30);  // "账号已锁定，请在30分钟后重试"
 *
 * // 方式4：传统方式（向后兼容）
 * throw new BusinessException("AUTH_001", "用户名或密码错误");
 * </pre>
 *
 * @author catface996
 * @since 2025-11-21
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误消息
     */
    private final String errorMessage;

    // ==================== 方式1：使用 ErrorCode 枚举（默认消息） ====================

    /**
     * 使用 ErrorCode 枚举创建异常（使用默认消息）
     *
     * @param errorCode 错误码枚举
     */
    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    /**
     * 使用 ErrorCode 枚举创建异常（使用默认消息 + 原因）
     *
     * @param errorCode 错误码枚举
     * @param cause 原因
     */
    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    // ==================== 方式2：ErrorCode + 自定义消息 ====================

    /**
     * 使用 ErrorCode 枚举 + 自定义消息创建异常
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息（覆盖默认消息）
     */
    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode.getCode();
        this.errorMessage = customMessage;
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息 + 原因创建异常
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息（覆盖默认消息）
     * @param cause 原因
     */
    public BaseException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode.getCode();
        this.errorMessage = customMessage;
    }

    // ==================== 方式3：ErrorCode + 消息参数（格式化） ====================

    /**
     * 使用 ErrorCode 枚举 + 消息参数创建异常（格式化默认消息）
     *
     * <p>消息模板使用 {0}, {1}, {2} 等占位符。</p>
     *
     * <p>示例：</p>
     * <pre>
     * // 错误码定义：ACCOUNT_LOCKED("LOCKED_001", "账号已锁定，请在{0}分钟后重试")
     * throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30);
     * // 结果消息："账号已锁定，请在30分钟后重试"
     * </pre>
     *
     * @param errorCode 错误码枚举
     * @param args 消息参数
     */
    public BaseException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.getMessage(), args));
        this.errorCode = errorCode.getCode();
        this.errorMessage = formatMessage(errorCode.getMessage(), args);
    }

    // ==================== 方式4：传统方式（向后兼容） ====================

    /**
     * 传统方式：使用 String 错误码 + String 消息创建异常
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     */
    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 传统方式：使用 String 错误码 + String 消息 + 原因创建异常
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param cause 原因
     */
    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化消息
     *
     * <p>使用 MessageFormat 格式化消息模板。</p>
     *
     * @param template 消息模板
     * @param args 参数
     * @return 格式化后的消息
     */
    private static String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            // 格式化失败时返回原始模板
            return template;
        }
    }
}
