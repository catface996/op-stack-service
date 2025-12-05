package com.catface996.aiops.domain.model.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 模型参数值对象
 *
 * <p>封装 LLM 模型的配置参数</p>
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelParameters {

    /**
     * 模型名称（必填）
     */
    private String modelName;

    /**
     * 温度参数，控制输出随机性（0-2，默认1.0）
     */
    @Builder.Default
    private Double temperature = 1.0;

    /**
     * 最大 Token 数（1-128000，默认4096）
     */
    @Builder.Default
    private Integer maxTokens = 4096;

    /**
     * Top-P 参数（核采样）（0-1，默认1.0）
     */
    @Builder.Default
    private Double topP = 1.0;

    /**
     * 频率惩罚，降低重复内容（-2到2，默认0）
     */
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    /**
     * 存在惩罚，增加话题多样性（-2到2，默认0）
     */
    @Builder.Default
    private Double presencePenalty = 0.0;

    /**
     * 验证参数是否有效
     *
     * @return 参数是否有效
     */
    public boolean isValid() {
        if (modelName == null || modelName.isBlank()) {
            return false;
        }
        if (temperature != null && (temperature < 0 || temperature > 2)) {
            return false;
        }
        if (maxTokens != null && (maxTokens < 1 || maxTokens > 128000)) {
            return false;
        }
        if (topP != null && (topP < 0 || topP > 1)) {
            return false;
        }
        if (frequencyPenalty != null && (frequencyPenalty < -2 || frequencyPenalty > 2)) {
            return false;
        }
        if (presencePenalty != null && (presencePenalty < -2 || presencePenalty > 2)) {
            return false;
        }
        return true;
    }
}
