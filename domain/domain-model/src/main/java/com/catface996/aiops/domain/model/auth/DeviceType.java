package com.catface996.aiops.domain.model.auth;

/**
 * 设备类型枚举
 *
 * <p>定义用户登录设备的类型。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.2: 多设备会话管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public enum DeviceType {

    /**
     * 桌面设备
     */
    DESKTOP("Desktop"),

    /**
     * 移动设备
     */
    MOBILE("Mobile"),

    /**
     * 平板设备
     */
    TABLET("Tablet"),

    /**
     * 未知设备
     */
    UNKNOWN("Unknown");

    private final String displayName;

    DeviceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据User-Agent解析设备类型
     *
     * @param userAgent User-Agent字符串
     * @return 设备类型
     */
    public static DeviceType fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        // 检测移动设备
        if (ua.contains("mobile") || ua.contains("android") && !ua.contains("tablet")) {
            return MOBILE;
        }

        // 检测平板设备
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return TABLET;
        }

        // 检测桌面设备
        if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) {
            return DESKTOP;
        }

        return UNKNOWN;
    }
}
