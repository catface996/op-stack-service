package com.catface996.aiops.repository.mysql.po.prompt;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提示词模板持久化对象
 *
 * <p>数据库表 prompt_template 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("prompt_template")
public class PromptTemplatePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("name")
    private String name;

    /**
     * 用途ID
     */
    @TableField("usage_id")
    private Long usageId;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 当前版本号
     */
    @TableField("current_version")
    private Integer currentVersion;

    /**
     * 乐观锁版本
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

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

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 用途名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String usageName;

    /**
     * 当前版本内容（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String content;
}
