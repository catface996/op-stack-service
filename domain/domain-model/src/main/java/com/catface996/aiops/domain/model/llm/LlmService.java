package com.catface996.aiops.domain.model.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * LLM 服务领域模型
 *
 * <p>封装 LLM 服务配置的业务属性和行为</p>
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmService {

    /**
     * 服务 ID
     */
    private Long id;

    /**
     * 服务名称（全局唯一，1-100字符）
     */
    private String name;

    /**
     * 服务描述（最长500字符）
     */
    private String description;

    /**
     * 供应商类型
     */
    private ProviderType providerType;

    /**
     * API 端点地址
     */
    private String endpoint;

    /**
     * 模型参数配置
     */
    private ModelParameters modelParameters;

    /**
     * 优先级（1-999，数字越小优先级越高，默认100）
     */
    @Builder.Default
    private Integer priority = 100;

    /**
     * 是否启用（默认true）
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 是否为默认服务（默认false）
     */
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 验证服务名称是否有效
     *
     * @return 名称是否有效
     */
    public boolean isNameValid() {
        return name != null && !name.isBlank() && name.length() <= 100;
    }

    /**
     * 验证描述是否有效
     *
     * @return 描述是否有效
     */
    public boolean isDescriptionValid() {
        return description == null || description.length() <= 500;
    }

    /**
     * 验证优先级是否有效
     *
     * @return 优先级是否有效
     */
    public boolean isPriorityValid() {
        return priority != null && priority >= 1 && priority <= 999;
    }

    /**
     * 验证模型参数是否有效
     *
     * @return 参数是否有效
     */
    public boolean isModelParametersValid() {
        return modelParameters != null && modelParameters.isValid();
    }

    /**
     * 验证整个服务配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return isNameValid()
                && isDescriptionValid()
                && isPriorityValid()
                && isModelParametersValid()
                && providerType != null;
    }

    /**
     * 启用服务
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * 禁用服务
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * 设置为默认服务
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * 取消默认服务标记
     */
    public void clearDefault() {
        this.isDefault = false;
    }
}
