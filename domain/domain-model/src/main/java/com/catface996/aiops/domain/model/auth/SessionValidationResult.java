package com.catface996.aiops.domain.model.auth;

/**
 * 会话验证结果值对象
 *
 * <p>包含会话验证的结果信息，包括验证是否成功、会话对象、错误码和警告信息。</p>
 *
 * <p>设计原则：值对象应该是不可变的。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.2: 会话验证</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public class SessionValidationResult {

    /**
     * 验证是否成功
     */
    private final boolean valid;

    /**
     * 会话对象（验证成功时返回）
     */
    private final Session session;

    /**
     * 错误码（验证失败时返回）
     */
    private final String errorCode;

    /**
     * 错误消息（验证失败时返回）
     */
    private final String errorMessage;

    /**
     * 警告标志（会话即将过期时为true）
     */
    private final boolean warningFlag;

    /**
     * 剩余时间（秒，警告时返回）
     */
    private final int remainingTime;

    /**
     * 私有构造函数
     */
    private SessionValidationResult(boolean valid, Session session, String errorCode,
                                     String errorMessage, boolean warningFlag, int remainingTime) {
        this.valid = valid;
        this.session = session;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.warningFlag = warningFlag;
        this.remainingTime = remainingTime;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建验证成功的结果
     *
     * @param session 会话对象
     * @return 验证成功的结果
     */
    public static SessionValidationResult success(Session session) {
        boolean warning = session != null && session.isAboutToExpire();
        int remaining = session != null ? session.getRemainingTime() : 0;
        return new SessionValidationResult(true, session, null, null, warning, remaining);
    }

    /**
     * 创建验证成功但带警告的结果
     *
     * @param session 会话对象
     * @param remainingTime 剩余时间（秒）
     * @return 带警告的验证成功结果
     */
    public static SessionValidationResult successWithWarning(Session session, int remainingTime) {
        return new SessionValidationResult(true, session, null, null, true, remainingTime);
    }

    /**
     * 创建验证失败的结果
     *
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     * @return 验证失败的结果
     */
    public static SessionValidationResult failure(String errorCode, String errorMessage) {
        return new SessionValidationResult(false, null, errorCode, errorMessage, false, 0);
    }

    /**
     * 创建会话过期的失败结果
     *
     * @return 会话过期的失败结果
     */
    public static SessionValidationResult expired() {
        return failure("AUTH_101", "您的会话已过期。请重新登录。");
    }

    /**
     * 创建会话空闲超时的失败结果
     *
     * @return 会话空闲超时的失败结果
     */
    public static SessionValidationResult idleTimeout() {
        return failure("AUTH_102", "您的会话已过期。请重新登录。");
    }

    /**
     * 创建会话不存在的失败结果
     *
     * @return 会话不存在的失败结果
     */
    public static SessionValidationResult notFound() {
        return failure("AUTH_103", "会话不存在或已失效。请重新登录。");
    }

    /**
     * 创建会话数据损坏的失败结果
     *
     * @return 会话数据损坏的失败结果
     */
    public static SessionValidationResult corrupted() {
        return failure("AUTH_104", "会话数据异常。请重新登录。");
    }

    // ==================== 业务方法 ====================

    /**
     * 检查验证是否成功
     *
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 检查是否有警告
     *
     * @return true if has warning
     */
    public boolean hasWarning() {
        return warningFlag;
    }

    // ==================== Getters ====================

    public Session getSession() {
        return session;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isWarningFlag() {
        return warningFlag;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    @Override
    public String toString() {
        return "SessionValidationResult{" +
                "valid=" + valid +
                ", session=" + (session != null ? session.getId() : "null") +
                ", errorCode='" + errorCode + '\'' +
                ", warningFlag=" + warningFlag +
                ", remainingTime=" + remainingTime +
                '}';
    }
}
