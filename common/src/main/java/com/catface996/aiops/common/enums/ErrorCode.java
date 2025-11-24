package com.catface996.aiops.common.enums;

/**
 * 错误码接口
 *
 * <p>所有错误码枚举都应该实现这个接口。</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>每个错误码包含唯一的 code 和默认的 message</li>
 *   <li>支持消息模板，通过 {0}, {1}, {2} 等占位符支持参数化</li>
 *   <li>使用接口而不是具体枚举类，支持按业务领域分类</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 定义错误码枚举
 * public enum AuthErrorCode implements ErrorCode {
 *     INVALID_CREDENTIALS("AUTH_001", "用户名或密码错误"),
 *     ACCOUNT_LOCKED("LOCKED_001", "账号已锁定，请在{0}分钟后重试");
 *
 *     private final String code;
 *     private final String message;
 *
 *     AuthErrorCode(String code, String message) {
 *         this.code = code;
 *         this.message = message;
 *     }
 *
 *     &#64;Override
 *     public String getCode() {
 *         return code;
 *     }
 *
 *     &#64;Override
 *     public String getMessage() {
 *         return message;
 *     }
 * }
 *
 * // 使用错误码
 * throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
 * throw new BusinessException(AuthErrorCode.ACCOUNT_LOCKED, 30);  // 参数化消息
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * <p>格式：{类别}_{序号}，例如：AUTH_001, PARAM_001</p>
     *
     * <p>类别前缀决定了 HTTP 状态码：</p>
     * <ul>
     *   <li>AUTH_ → 401 Unauthorized</li>
     *   <li>AUTHZ_ → 403 Forbidden</li>
     *   <li>PARAM_ → 400 Bad Request</li>
     *   <li>NOT_FOUND_ → 404 Not Found</li>
     *   <li>CONFLICT_ → 409 Conflict</li>
     *   <li>LOCKED_ → 423 Locked</li>
     *   <li>SYS_ → 500 Internal Server Error</li>
     * </ul>
     *
     * @return 错误码字符串
     */
    String getCode();

    /**
     * 获取默认错误消息
     *
     * <p>可以包含占位符 {0}, {1}, {2} 等，用于参数化消息。</p>
     *
     * <p>示例：</p>
     * <ul>
     *   <li>"用户名或密码错误" - 固定消息</li>
     *   <li>"账号已锁定，请在{0}分钟后重试" - 参数化消息</li>
     * </ul>
     *
     * @return 默认错误消息
     */
    String getMessage();
}
