package com.catface996.aiops.domain.model.auth;

import java.util.Objects;

/**
 * 设备信息值对象
 *
 * <p>用于记录用户登录时的设备信息，包括IP地址、User-Agent、设备类型等。</p>
 *
 * <p>设计原则：值对象应该是不可变的，但为了兼容JSON序列化，保留setter方法。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.2: 多设备会话管理</li>
 *   <li>REQ 3.4: IP地址变化检测</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class DeviceInfo {

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User Agent
     */
    private String userAgent;

    /**
     * 设备类型（如：Desktop, Mobile, Tablet）
     */
    private String deviceType;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * 浏览器
     */
    private String browser;

    // 构造函数
    public DeviceInfo() {
    }

    public DeviceInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public DeviceInfo(String ipAddress, String userAgent, String deviceType,
                      String operatingSystem, String browser) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.operatingSystem = operatingSystem;
        this.browser = browser;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查是否为同一设备
     *
     * <p>基于User-Agent判断是否为同一设备。</p>
     *
     * @param other 另一个设备信息
     * @return true if same device, false otherwise
     */
    public boolean isSameDevice(DeviceInfo other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(this.userAgent, other.userAgent);
    }

    /**
     * 检查是否为同一IP
     *
     * @param ip IP地址
     * @return true if same IP, false otherwise
     */
    public boolean isSameIp(String ip) {
        return Objects.equals(this.ipAddress, ip);
    }

    /**
     * 检查IP地址是否发生变化
     *
     * @param other 另一个设备信息
     * @return true if IP address changed, false otherwise
     */
    public boolean isIpChanged(DeviceInfo other) {
        if (other == null) {
            return true;
        }
        return !isSameIp(other.ipAddress);
    }

    /**
     * 创建一个带有新IP地址的DeviceInfo副本
     *
     * @param newIpAddress 新的IP地址
     * @return 新的DeviceInfo实例
     */
    public DeviceInfo withNewIpAddress(String newIpAddress) {
        return new DeviceInfo(newIpAddress, this.userAgent, this.deviceType,
                this.operatingSystem, this.browser);
    }

    // ==================== Getters and Setters ====================

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(deviceType, that.deviceType) &&
                Objects.equals(operatingSystem, that.operatingSystem) &&
                Objects.equals(browser, that.browser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, userAgent, deviceType, operatingSystem, browser);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", browser='" + browser + '\'' +
                '}';
    }
}
