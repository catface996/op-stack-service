package com.catface996.aiops.repository.mysql.po.topology;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拓扑图-报告模板关联持久化对象
 *
 * <p>数据库表 topology_2_report_template 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@TableName("topology_2_report_template")
public class TopologyReportTemplatePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 拓扑图ID
     */
    @TableField("topology_id")
    private Long topologyId;

    /**
     * 报告模板ID
     */
    @TableField("report_template_id")
    private Long reportTemplateId;

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

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 报告模板名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String templateName;

    /**
     * 报告模板描述（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String templateDescription;

    /**
     * 报告模板分类（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String templateCategory;
}
