package com.catface996.aiops.domain.model.auth;

/**
 * 设备信息值对象
 * 
 * 用于记录用户登录时的设备信息
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
    
    // Getters and Setters
    
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
