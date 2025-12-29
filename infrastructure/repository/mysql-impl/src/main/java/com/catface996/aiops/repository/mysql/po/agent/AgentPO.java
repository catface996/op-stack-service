package com.catface996.aiops.repository.mysql.po.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 持久化对象
 *
 * <p>数据库表 agent 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@TableName("agent")
public class AgentPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Agent 名称
     */
    @TableField("name")
    private String name;

    /**
     * Agent 角色（专业领域）
     */
    @TableField("role")
    private String role;

    /**
     * Agent 层级（团队位置）
     */
    @TableField("hierarchy_level")
    private String hierarchyLevel;

    /**
     * 专业领域
     */
    @TableField("specialty")
    private String specialty;

    // ===== LLM 配置（扁平化） =====

    /**
     * 关联的提示词模板ID
     */
    @TableField("prompt_template_id")
    private Long promptTemplateId;

    /**
     * AI 模型标识
     */
    @TableField("model")
    private String model;

    /**
     * 温度参数 (0.0-2.0)
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * Top P 参数 (0.0-1.0)
     */
    @TableField("top_p")
    private Double topP;

    /**
     * 最大输出 token 数
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 最长运行时间（秒）
     */
    @TableField("max_runtime")
    private Integer maxRuntime;

    // ===== 统计信息 =====

    /**
     * 警告数量（全局累计）
     */
    @TableField("warnings")
    private Integer warnings;

    /**
     * 严重问题数量（全局累计）
     */
    @TableField("critical")
    private Integer critical;

    // ===== 审计字段 =====

    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 修改人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
