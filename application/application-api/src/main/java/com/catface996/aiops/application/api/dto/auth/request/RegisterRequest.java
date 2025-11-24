package com.catface996.aiops.application.api.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求
 *
 * <p>包含用户注册所需的基本信息，所有字段都经过验证以确保数据有效性。</p>
 *
 * <p>验证规则：</p>
 * <ul>
 *   <li>用户名：3-20个字符，只能包含字母、数字和下划线</li>
 *   <li>邮箱：必须是有效的邮箱格式，最大100个字符</li>
 *   <li>密码：8-64个字符，必须包含大写字母、小写字母、数字、特殊字符中的至少3类</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * 用户名
     *
     * <p>用于唯一标识用户，登录时可以使用用户名或邮箱。</p>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>长度：3-20个字符</li>
     *   <li>格式：只能包含字母（大小写）、数字和下划线</li>
     *   <li>唯一性：系统中不能存在相同的用户名</li>
     * </ul>
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 邮箱地址
     *
     * <p>用于接收系统通知和密码找回（后续功能），登录时可以使用用户名或邮箱。</p>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>必须符合邮箱格式（如：user@example.com）</li>
     *   <li>最大长度：100个字符</li>
     *   <li>唯一性：系统中不能存在相同的邮箱</li>
     * </ul>
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 密码
     *
     * <p>用户登录凭据，系统会使用 BCrypt 算法加密存储。</p>
     *
     * <p>验证规则（在应用层额外验证）：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>长度：8-64个字符</li>
     *   <li>复杂度：必须包含大写字母、小写字母、数字、特殊字符中的至少3类</li>
     *   <li>不能包含用户名或邮箱的任何部分</li>
     *   <li>不能是常见弱密码（如：password123、admin123等）</li>
     * </ul>
     *
     * <p>注意：此字段只进行基本的非空和长度验证，详细的密码强度验证在应用层完成。</p>
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在8-64个字符之间")
    private String password;
}
