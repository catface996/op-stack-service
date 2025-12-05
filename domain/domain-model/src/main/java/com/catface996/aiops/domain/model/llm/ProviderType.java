package com.catface996.aiops.domain.model.llm;

/**
 * LLM 供应商类型枚举
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
public enum ProviderType {

    /**
     * OpenAI 服务
     */
    OPENAI("OpenAI 服务"),

    /**
     * Anthropic Claude 服务
     */
    CLAUDE("Anthropic Claude 服务"),

    /**
     * 本地部署模型
     */
    LOCAL("本地部署模型"),

    /**
     * 自定义供应商
     */
    CUSTOM("自定义供应商");

    private final String description;

    ProviderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
