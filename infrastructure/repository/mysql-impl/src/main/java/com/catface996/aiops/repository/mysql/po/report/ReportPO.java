package com.catface996.aiops.repository.mysql.po.report;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报告持久化对象
 *
 * <p>数据库表 report 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@TableName("report")
public class ReportPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报告标题
     */
    @TableField("title")
    private String title;

    /**
     * 报告类型
     */
    @TableField("type")
    private String type;

    /**
     * 报告状态
     */
    @TableField("status")
    private String status;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 报告摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 报告内容（Markdown格式）
     */
    @TableField("content")
    private String content;

    /**
     * 标签（JSON数组格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 关联的拓扑图ID
     */
    @TableField("topology_id")
    private Long topologyId;

    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /**
     * 修改人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

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
