package com.catface996.aiops.repository.mysql.po.node;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点类型持久化对象
 *
 * <p>数据库表 node_type 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("node_type")
public class NodeTypePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型编码（唯一）
     */
    @TableField("code")
    private String code;

    /**
     * 类型名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标 URL
     */
    @TableField("icon")
    private String icon;

    /**
     * 是否系统预置
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 属性定义 Schema（JSON格式）
     */
    @TableField("attribute_schema")
    private String attributeSchema;

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
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
