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
     * Agent 角色
     */
    @TableField("role")
    private String role;

    /**
     * 专业领域
     */
    @TableField("specialty")
    private String specialty;

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

    /**
     * AI 配置（JSON格式）
     */
    @TableField("config")
    private String config;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

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
