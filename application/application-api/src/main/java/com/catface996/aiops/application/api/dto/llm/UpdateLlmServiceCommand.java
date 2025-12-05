package com.catface996.aiops.application.api.dto.llm;

import com.catface996.aiops.domain.model.llm.ProviderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新 LLM 服务命令
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLlmServiceCommand {

    /**
     * 服务名称
     */
    @Size(min = 1, max = 100, message = "服务名称长度为1-100字符")
    private String name;

    /**
     * 服务描述
     */
    @Size(max = 500, message = "服务描述最长500字符")
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
    @Valid
    private ModelParametersDTO modelParameters;

    /**
     * 优先级（1-999）
     */
    @Min(value = 1, message = "优先级最小为1")
    @Max(value = 999, message = "优先级最大为999")
    private Integer priority;
}
