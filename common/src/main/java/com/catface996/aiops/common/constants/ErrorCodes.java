package com.catface996.aiops.common.constants;

/**
 * 错误码常量（已废弃）
 *
 * <p><strong>此类已废弃，请使用新的错误码枚举代替：</strong></p>
 * <ul>
 *   <li>{@link com.catface996.aiops.common.enums.AuthErrorCode} - 认证相关错误</li>
 *   <li>{@link com.catface996.aiops.common.enums.ParamErrorCode} - 参数验证错误</li>
 *   <li>{@link com.catface996.aiops.common.enums.ResourceErrorCode} - 资源相关错误</li>
 *   <li>{@link com.catface996.aiops.common.enums.SystemErrorCode} - 系统错误</li>
 * </ul>
 *
 * <p>新的使用方式：</p>
 * <pre>
 * // 旧方式（已废弃）
 * throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "用户名或密码错误");
 *
 * // 新方式（推荐）
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);  // 使用默认消息
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "自定义消息");  // 覆盖消息
 * throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30);  // 参数化消息
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-24
 * @deprecated 使用新的错误码枚举代替，此类将在下一个主版本中移除
 */
@Deprecated(since = "2025-11-24", forRemoval = true)
public final class ErrorCodes {

    private ErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== 认证相关 (401xxx) ====================

    /**
     * 认证失败（用户名或密码错误）
     */
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";

    /**
     * Token 无效
     */
    public static final String AUTH_TOKEN_INVALID = "AUTH_002";

    /**
     * 会话已过期
     */
    public static final String AUTH_SESSION_EXPIRED = "AUTH_003";

    /**
     * 会话不存在或已失效
     */
    public static final String AUTH_SESSION_NOT_FOUND = "AUTH_004";

    // ==================== 参数相关 (400xxx) ====================

    /**
     * 密码格式不符合要求
     */
    public static final String PARAM_INVALID_PASSWORD = "PARAM_001";

    /**
     * 参数验证失败
     */
    public static final String PARAM_VALIDATION_FAILED = "PARAM_002";

    // ==================== 资源不存在 (404xxx) ====================

    /**
     * 账号不存在
     */
    public static final String NOT_FOUND_ACCOUNT = "NOT_FOUND_001";

    // ==================== 资源冲突 (409xxx) ====================

    /**
     * 用户名已存在
     */
    public static final String CONFLICT_USERNAME = "CONFLICT_001";

    /**
     * 邮箱已存在
     */
    public static final String CONFLICT_EMAIL = "CONFLICT_002";

    // ==================== 资源锁定 (423xxx) ====================

    /**
     * 账号被锁定
     */
    public static final String LOCKED_ACCOUNT = "LOCKED_001";

    // ==================== 系统异常 (500xxx) ====================

    /**
     * 数据库异常
     */
    public static final String SYS_DATABASE_ERROR = "SYS_001";

    /**
     * 未知异常
     */
    public static final String SYS_UNKNOWN_ERROR = "SYS_002";
}
