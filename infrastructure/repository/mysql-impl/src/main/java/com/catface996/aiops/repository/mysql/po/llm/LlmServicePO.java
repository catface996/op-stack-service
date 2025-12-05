package com.catface996.aiops.repository.mysql.po.llm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LLM 服务 MyBatis-Plus 持久化对象
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("llm_service_config")
public class LlmServicePO {

    /**
     * 服务 ID
     */
    @TableId(type = IdType.AUTO)
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
    @TableField("provider_type")
    private String providerType;

    /**
     * API 端点地址
     */
    private String endpoint;

    /**
     * 模型参数配置（JSON 字符串）
     */
    @TableField("model_parameters")
    private String modelParameters;

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
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
