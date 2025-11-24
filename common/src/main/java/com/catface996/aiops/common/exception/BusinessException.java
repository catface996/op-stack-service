package com.catface996.aiops.common.exception;

import com.catface996.aiops.common.enums.ErrorCode;

/**
 * 业务异常
 *
 * <p>用于业务逻辑验证失败、业务规则不满足等场景。</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>业务规则不满足</li>
 *   <li>资源状态不符合操作要求</li>
 *   <li>权限验证失败</li>
 *   <li>资源冲突或不存在</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户名或密码错误</li>
 *   <li>账号已锁定</li>
 *   <li>资源不存在</li>
 *   <li>用户名/邮箱已存在</li>
 *   <li>Token无效或会话过期</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 方式1：使用枚举（默认消息）
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
 *
 * // 方式2：使用枚举 + 自定义消息
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "密码错误次数过多");
 *
 * // 方式3：使用枚举 + 参数化消息
 * throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30);  // "账号已锁定，请在30分钟后重试"
 *
 * // 方式4：传统方式（向后兼容）
 * throw new BusinessException("AUTH_001", "用户名或密码错误");
 * </pre>
 *
 * @author catface996
 * @since 2025-11-21
 */
public class BusinessException extends BaseException {

    // ==================== 使用 ErrorCode 枚举 ====================

    /**
     * 使用 ErrorCode 枚举（使用默认消息）
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用 ErrorCode 枚举 + 原因（使用默认消息）
     *
     * @param errorCode 错误码枚举
     * @param cause 原因
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息（覆盖默认消息）
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息 + 原因
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     * @param cause 原因
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }

    /**
     * 使用 ErrorCode 枚举 + 消息参数（格式化默认消息）
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
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    // ==================== 传统方式（向后兼容） ====================

    /**
     * 传统方式：String 错误码 + String 消息
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     */
    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * 传统方式：String 错误码 + String 消息 + 原因
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param cause 原因
     */
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
