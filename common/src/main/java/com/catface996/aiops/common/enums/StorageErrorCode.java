package com.catface996.aiops.common.enums;

/**
 * 存储相关错误码
 *
 * <p>包含Redis、MySQL等存储层相关的错误码。</p>
 *
 * <p>错误码格式: SYS_{序号}</p>
 * <p>HTTP状态码: 500 Internal Server Error</p>
 *
 * <p>需求追溯:</p>
 * <ul>
 *   <li>F01-4: 会话管理功能 - 存储层错误处理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public enum StorageErrorCode implements ErrorCode {

    /**
     * Redis连接失败
     *
     * <p>无法连接到Redis服务器。</p>
     * <p>系统将自动降级到MySQL存储。</p>
     */
    REDIS_CONNECTION_FAILED("SYS_001", "缓存服务暂时不可用"),

    /**
     * MySQL连接失败
     *
     * <p>无法连接到MySQL数据库。</p>
     * <p>这是严重错误，会话管理功能将不可用。</p>
     */
    MYSQL_CONNECTION_FAILED("SYS_002", "系统暂时不可用，请稍后重试"),

    /**
     * 数据序列化失败
     *
     * <p>会话数据序列化或反序列化失败。</p>
     */
    SERIALIZATION_FAILED("SYS_003", "数据处理异常");

    private final String code;
    private final String message;

    StorageErrorCode(String code, String message) {
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
