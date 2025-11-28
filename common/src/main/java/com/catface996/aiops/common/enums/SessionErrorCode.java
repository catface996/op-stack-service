package com.catface996.aiops.common.enums;

/**
 * 会话管理相关错误码
 *
 * <p>包含会话相关错误码(AUTH_101-AUTH_104)、令牌相关错误码(AUTH_201-AUTH_203)和权限相关错误码(AUTHZ_001)。</p>
 *
 * <p>错误码格式: {类别}_{序号}</p>
 * <ul>
 *   <li>AUTH_ → 401 Unauthorized</li>
 *   <li>AUTHZ_ → 403 Forbidden</li>
 * </ul>
 *
 * <p>需求追溯:</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public enum SessionErrorCode implements ErrorCode {

    // ==================== 会话相关错误码 (AUTH_1XX) ====================

    /**
     * 会话已过期（绝对超时）
     *
     * <p>会话达到最大存在时间（默认8小时）后自动过期。</p>
     */
    SESSION_EXPIRED("AUTH_101", "您的会话已过期。请重新登录。"),

    /**
     * 会话空闲超时
     *
     * <p>会话在指定时间内（默认30分钟）没有活动后自动过期。</p>
     */
    SESSION_IDLE_TIMEOUT("AUTH_102", "您的会话已过期。请重新登录。"),

    /**
     * 会话不存在
     *
     * <p>指定的会话ID在存储中不存在。</p>
     */
    SESSION_NOT_FOUND("AUTH_103", "会话不存在或已失效。请重新登录。"),

    /**
     * 会话数据损坏
     *
     * <p>会话数据无法正确反序列化或数据格式异常。</p>
     */
    SESSION_CORRUPTED("AUTH_104", "会话数据异常。请重新登录。"),

    // ==================== 令牌相关错误码 (AUTH_2XX) ====================

    /**
     * 令牌已过期
     *
     * <p>JWT令牌已超过有效期（访问令牌15分钟，刷新令牌30天）。</p>
     */
    TOKEN_EXPIRED("AUTH_201", "令牌已过期。请刷新令牌或重新登录。"),

    /**
     * 令牌无效
     *
     * <p>JWT令牌签名验证失败或格式不正确。</p>
     */
    TOKEN_INVALID("AUTH_202", "令牌无效。请重新登录。"),

    /**
     * 令牌已被列入黑名单
     *
     * <p>用户登出后，令牌被加入黑名单，不再有效。</p>
     */
    TOKEN_BLACKLISTED("AUTH_203", "令牌已失效。请重新登录。"),

    // ==================== 权限相关错误码 (AUTHZ_XXX) ====================

    /**
     * 无权限操作
     *
     * <p>用户试图执行没有权限的操作，如终止其他用户的会话。</p>
     */
    FORBIDDEN("AUTHZ_001", "您无权执行此操作。");

    private final String code;
    private final String message;

    SessionErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
