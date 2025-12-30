package com.catface996.aiops.repository.mysql.po.node;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源节点持久化对象
 *
 * <p>数据库表 node 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("node")
public class NodePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 节点名称
     */
    @TableField("name")
    private String name;

    /**
     * 节点描述
     */
    @TableField("description")
    private String description;

    /**
     * 节点类型ID
     */
    @TableField("node_type_id")
    private Long nodeTypeId;

    /**
     * 状态（RUNNING, STOPPED, MAINTENANCE, OFFLINE）
     */
    @TableField("status")
    private String status;

    /**
     * 架构层级（BUSINESS_SCENARIO, BUSINESS_FLOW, BUSINESS_APPLICATION, MIDDLEWARE, INFRASTRUCTURE）
     */
    @TableField("layer")
    private String layer;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("attributes")
    private String attributes;

    /**
     * 创建者ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

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

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 节点类型名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeTypeName;

    /**
     * 节点类型编码（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeTypeCode;
}
