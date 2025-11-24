package com.catface996.aiops.common.exception;

import com.catface996.aiops.common.enums.ErrorCode;

/**
 * 系统异常
 *
 * <p>用于系统级错误，如数据库连接失败、第三方服务调用失败等。</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>系统基础设施故障</li>
 *   <li>外部依赖服务异常</li>
 *   <li>不可预期的技术错误</li>
 *   <li>需要技术团队介入处理</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>数据库连接失败</li>
 *   <li>Redis连接失败</li>
 *   <li>第三方API调用失败</li>
 *   <li>文件系统IO错误</li>
 *   <li>配置加载失败</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 方式1：使用枚举（默认消息）
 * throw new SystemException(SystemErrorCode.DATABASE_ERROR);
 *
 * // 方式2：使用枚举 + 原因
 * throw new SystemException(SystemErrorCode.DATABASE_ERROR, sqlException);
 *
 * // 方式3：使用枚举 + 自定义消息
 * throw new SystemException(SystemErrorCode.DATABASE_ERROR, "用户表查询失败");
 *
 * // 方式4：传统方式（向后兼容）
 * throw new SystemException("SYS_001", "数据库连接失败");
 * </pre>
 *
 * @author catface996
 * @since 2025-11-21
 */
public class SystemException extends BaseException {

    // ==================== 使用 ErrorCode 枚举 ====================

    /**
     * 使用 ErrorCode 枚举（使用默认消息）
     *
     * @param errorCode 错误码枚举
     */
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用 ErrorCode 枚举 + 原因（使用默认消息）
     *
     * @param errorCode 错误码枚举
     * @param cause 原因
     */
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息（覆盖默认消息）
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     */
    public SystemException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息 + 原因
     *
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     * @param cause 原因
     */
    public SystemException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }

    /**
     * 使用 ErrorCode 枚举 + 消息参数（格式化默认消息）
     *
     * <p>消息模板使用 {0}, {1}, {2} 等占位符。</p>
     *
     * @param errorCode 错误码枚举
     * @param args 消息参数
     */
    public SystemException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    // ==================== 传统方式（向后兼容） ====================

    /**
     * 传统方式：String 错误码 + String 消息
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     */
    public SystemException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * 传统方式：String 错误码 + String 消息 + 原因
     *
     * @param errorCode 错误码字符串
     * @param errorMessage 错误消息
     * @param cause 原因
     */
    public SystemException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}
