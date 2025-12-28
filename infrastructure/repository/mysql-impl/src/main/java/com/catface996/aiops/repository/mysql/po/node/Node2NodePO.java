package com.catface996.aiops.repository.mysql.po.node;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点依赖关系持久化对象
 *
 * <p>数据库表 node_2_node 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("node_2_node")
public class Node2NodePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源节点ID
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 目标节点ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 关系类型
     */
    @TableField("relationship_type")
    private String relationshipType;

    /**
     * 关系方向
     */
    @TableField("direction")
    private String direction;

    /**
     * 关系强度
     */
    @TableField("strength")
    private String strength;

    /**
     * 关系状态
     */
    @TableField("status")
    private String status;

    /**
     * 关系描述
     */
    @TableField("description")
    private String description;

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

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 源节点名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String sourceName;

    /**
     * 目标节点名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String targetName;
}
