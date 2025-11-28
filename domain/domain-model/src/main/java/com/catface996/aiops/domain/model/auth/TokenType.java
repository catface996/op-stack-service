package com.catface996.aiops.domain.model.auth;

/**
 * 令牌类型枚举
 *
 * <p>定义JWT令牌的类型。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 2.1, 2.2, 2.3: JWT令牌管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public enum TokenType {

    /**
     * 访问令牌
     *
     * <p>短期令牌，用于API认证。默认有效期15分钟。</p>
     */
    ACCESS("access"),

    /**
     * 刷新令牌
     *
     * <p>长期令牌，用于获取新的访问令牌。默认有效期30天。</p>
     */
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 令牌类型值
     * @return TokenType枚举，未找到返回null
     */
    public static TokenType fromValue(String value) {
        for (TokenType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
