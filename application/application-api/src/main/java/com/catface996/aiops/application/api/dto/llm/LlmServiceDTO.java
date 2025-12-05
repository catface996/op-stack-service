package com.catface996.aiops.application.api.dto.llm;

import com.catface996.aiops.domain.model.llm.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LLM 服务 DTO
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmServiceDTO {

    /**
     * 服务 ID
     */
    private Long id;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务描述
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
     * 模型参数
     */
    private ModelParametersDTO modelParameters;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否为默认服务
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
