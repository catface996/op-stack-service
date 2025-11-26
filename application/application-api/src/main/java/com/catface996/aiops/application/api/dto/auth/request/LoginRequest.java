package com.catface996.aiops.application.api.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求
 *
 * <p>支持使用用户名或邮箱登录，系统会自动识别标识符类型。</p>
 *
 * <p>登录流程：</p>
 * <ol>
 *   <li>检查账号是否被锁定（连续5次失败锁定30分钟）</li>
 *   <li>根据标识符查找账号（用户名或邮箱）</li>
 *   <li>验证密码是否正确</li>
 *   <li>处理会话互斥（使旧会话失效）</li>
 *   <li>创建新会话并生成 JWT Token</li>
 *   <li>重置登录失败计数</li>
 * </ol>
 *
 * <p>登录失败处理：</p>
 * <ul>
 *   <li>记录登录失败次数到 Redis（TTL 30分钟）</li>
 *   <li>连续5次失败后自动锁定账号30分钟</li>
 *   <li>返回通用错误消息"用户名或密码错误"（避免泄露账号是否存在）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求")
public class LoginRequest {

    /**
     * 登录标识符（用户名或邮箱）
     *
     * <p>系统会自动识别标识符类型：</p>
     * <ul>
     *   <li>如果包含 @ 符号，识别为邮箱</li>
     *   <li>否则识别为用户名</li>
     * </ul>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>如果是用户名：3-20个字符，只能包含字母、数字和下划线</li>
     *   <li>如果是邮箱：必须符合邮箱格式，最大100个字符</li>
     * </ul>
     *
     * <p>示例：</p>
     * <ul>
     *   <li>"john_doe" - 用户名</li>
     *   <li>"john@example.com" - 邮箱</li>
     * </ul>
     */
    @Schema(description = "登录标识符（用户名或邮箱）", example = "john_doe")
    @NotBlank(message = "用户名或邮箱不能为空")
    private String identifier;

    /**
     * 密码
     *
     * <p>用户登录凭据，系统会使用 BCrypt 算法验证密码。</p>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     * </ul>
     *
     * <p>安全说明：</p>
     * <ul>
     *   <li>密码通过 HTTPS 传输（网关层卸载）</li>
     *   <li>密码不会以明文形式记录到日志</li>
     *   <li>BCrypt 验证使用恒定时间比较，防止时序攻击</li>
     * </ul>
     */
    @Schema(description = "密码", example = "SecureP@ss123")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 是否记住我
     *
     * <p>控制会话有效期：</p>
     * <ul>
     *   <li>false（默认）：会话有效期为 2 小时</li>
     *   <li>true：会话有效期为 30 天</li>
     * </ul>
     *
     * <p>实现方式：</p>
     * <ul>
     *   <li>JWT Token 的过期时间根据此字段设置</li>
     *   <li>Redis 会话缓存的 TTL 根据此字段设置</li>
     * </ul>
     *
     * <p>安全建议：</p>
     * <ul>
     *   <li>在公共设备上不建议使用"记住我"功能</li>
     *   <li>记住我功能不影响会话互斥，新设备登录仍会使旧设备失效</li>
     * </ul>
     */
    @Schema(description = "是否记住我（false=2小时，true=30天）", example = "false")
    @NotNull(message = "rememberMe 不能为空")
    private Boolean rememberMe;

    /**
     * 创建登录请求（不记住我）
     *
     * @param identifier 登录标识符（用户名或邮箱）
     * @param password 密码
     * @return 登录请求对象
     */
    public static LoginRequest of(String identifier, String password) {
        return new LoginRequest(identifier, password, false);
    }

    /**
     * 创建登录请求（指定是否记住我）
     *
     * @param identifier 登录标识符（用户名或邮箱）
     * @param password 密码
     * @param rememberMe 是否记住我
     * @return 登录请求对象
     */
    public static LoginRequest of(String identifier, String password, Boolean rememberMe) {
        return new LoginRequest(identifier, password, rememberMe != null ? rememberMe : false);
    }
}
