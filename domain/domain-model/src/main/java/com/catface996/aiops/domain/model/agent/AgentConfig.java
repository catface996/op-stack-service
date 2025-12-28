package com.catface996.aiops.domain.model.agent;

/**
 * Agent AI 配置值对象
 *
 * <p>存储 Agent 的 AI 模型配置，以 JSON 形式持久化到 agent.config 字段。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class AgentConfig {

    /**
     * 默认模型
     */
    public static final String DEFAULT_MODEL = "gemini-2.0-flash";

    /**
     * 默认温度
     */
    public static final Double DEFAULT_TEMPERATURE = 0.3;

    /**
     * 默认系统指令
     */
    public static final String DEFAULT_SYSTEM_INSTRUCTION = "You are a specialized worker agent.";

    /**
     * AI 模型标识符
     */
    private String model;

    /**
     * 创造性参数 (0.0-1.0)
     */
    private Double temperature;

    /**
     * 系统指令
     */
    private String systemInstruction;

    /**
     * 默认上下文
     */
    private String defaultContext;

    public AgentConfig() {
    }

    public AgentConfig(String model, Double temperature, String systemInstruction, String defaultContext) {
        this.model = model;
        this.temperature = temperature;
        this.systemInstruction = systemInstruction;
        this.defaultContext = defaultContext;
    }

    /**
     * 创建默认配置
     *
     * @return 默认配置实例
     */
    public static AgentConfig defaults() {
        return new AgentConfig(
            DEFAULT_MODEL,
            DEFAULT_TEMPERATURE,
            DEFAULT_SYSTEM_INSTRUCTION,
            ""
        );
    }

    /**
     * 验证温度参数是否在有效范围内
     *
     * @return true 如果 temperature 在 0.0-1.0 范围内或为 null
     */
    public boolean isTemperatureValid() {
        return temperature == null || (temperature >= 0.0 && temperature <= 1.0);
    }

    // Getters and Setters

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getSystemInstruction() {
        return systemInstruction;
    }

    public void setSystemInstruction(String systemInstruction) {
        this.systemInstruction = systemInstruction;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                '}';
    }
}
