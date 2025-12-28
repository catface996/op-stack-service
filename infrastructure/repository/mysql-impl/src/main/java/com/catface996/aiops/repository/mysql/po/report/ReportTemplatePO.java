package com.catface996.aiops.repository.mysql.po.report;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报告模板持久化对象
 *
 * <p>数据库表 report_templates 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@TableName("report_templates")
public class ReportTemplatePO implements Serializable {

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
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 模板分类
     */
    @TableField("category")
    private String category;

    /**
     * 模板内容（含占位符的Markdown）
     */
    @TableField("content")
    private String content;

    /**
     * 标签（JSON数组格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 乐观锁版本号
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
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
