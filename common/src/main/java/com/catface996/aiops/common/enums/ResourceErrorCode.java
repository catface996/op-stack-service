package com.catface996.aiops.common.enums;

/**
 * 资源相关错误码
 *
 * <p>包括资源不存在、资源冲突、资源锁定等。</p>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public enum ResourceErrorCode implements ErrorCode {

    // ==================== 资源不存在 (404) ====================

    /**
     * 账号不存在
     */
    ACCOUNT_NOT_FOUND("NOT_FOUND_001", "账号不存在"),

    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND("NOT_FOUND_002", "资源不存在"),

    /**
     * 资源类型不存在
     */
    RESOURCE_TYPE_NOT_FOUND("NOT_FOUND_003", "资源类型不存在"),

    // ==================== 资源冲突 (409) ====================

    /**
     * 用户名已存在
     */
    USERNAME_CONFLICT("CONFLICT_001", "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_CONFLICT("CONFLICT_002", "邮箱已存在"),

    /**
     * 资源名称已存在
     */
    RESOURCE_NAME_CONFLICT("CONFLICT_004", "同类型下资源名称已存在"),

    /**
     * 资源名称确认不匹配
     */
    RESOURCE_NAME_MISMATCH("CONFLICT_005", "资源名称确认不匹配"),

    // ==================== 认证授权 (401/403) ====================

    /**
     * 未认证（由网关统一处理认证）
     */
    UNAUTHORIZED("AUTH_001", "用户未认证"),

    /**
     * 禁止访问（已认证但无权限）
     */
    FORBIDDEN("AUTHZ_001", "您没有权限执行此操作"),

    // ==================== 参数错误 (400) ====================

    /**
     * 无效参数
     */
    INVALID_PARAMETER("PARAM_001", "请求参数无效"),

    // ==================== 资源锁定 (423) ====================

    /**
     * 账号已锁定（支持参数化消息）
     *
     * <p>消息模板：账号已锁定，请在{0}分钟后重试</p>
     */
    ACCOUNT_LOCKED("LOCKED_001", "账号已锁定，请在{0}分钟后重试"),

    // ==================== 乐观锁冲突 (409) ====================

    /**
     * 资源版本冲突（乐观锁）
     */
    VERSION_CONFLICT("CONFLICT_003", "资源已被其他用户修改，请刷新后重试"),

    // ==================== 子图错误 (400) ====================

    /**
     * 子图不为空，不能删除
     */
    SUBGRAPH_NOT_EMPTY("SUBGRAPH_001", "子图不为空，请先移除所有成员后再删除");

    private final String code;
    private final String message;

    ResourceErrorCode(String code, String message) {
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
