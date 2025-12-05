package com.catface996.aiops.application.api.dto.llm;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型参数 DTO
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelParametersDTO {

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 温度参数（0-2）
     */
    @DecimalMin(value = "0", message = "温度参数最小为0")
    @DecimalMax(value = "2", message = "温度参数最大为2")
    @Builder.Default
    private Double temperature = 1.0;

    /**
     * 最大 Token 数（1-128000）
     */
    @Min(value = 1, message = "最大Token数最小为1")
    @Max(value = 128000, message = "最大Token数最大为128000")
    @Builder.Default
    private Integer maxTokens = 4096;

    /**
     * Top-P 参数（0-1）
     */
    @DecimalMin(value = "0", message = "Top-P参数最小为0")
    @DecimalMax(value = "1", message = "Top-P参数最大为1")
    @Builder.Default
    private Double topP = 1.0;

    /**
     * 频率惩罚（-2到2）
     */
    @DecimalMin(value = "-2", message = "频率惩罚最小为-2")
    @DecimalMax(value = "2", message = "频率惩罚最大为2")
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    /**
     * 存在惩罚（-2到2）
     */
    @DecimalMin(value = "-2", message = "存在惩罚最小为-2")
    @DecimalMax(value = "2", message = "存在惩罚最大为2")
    @Builder.Default
    private Double presencePenalty = 0.0;
}
