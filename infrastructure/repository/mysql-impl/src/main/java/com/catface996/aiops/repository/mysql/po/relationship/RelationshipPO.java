package com.catface996.aiops.repository.mysql.po.relationship;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源关系持久化对象
 *
 * <p>数据库表 node_2_node 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@TableName("node_2_node")
public class RelationshipPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源资源ID
     */
    @TableField("source_id")
    private Long sourceResourceId;

    /**
     * 目标资源ID
     */
    @TableField("target_id")
    private Long targetResourceId;

    /**
     * 关系类型（DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION）
     */
    @TableField("relationship_type")
    private String relationshipType;

    /**
     * 关系方向（UNIDIRECTIONAL, BIDIRECTIONAL）
     */
    @TableField("direction")
    private String direction;

    /**
     * 关系强度（STRONG, WEAK）
     */
    @TableField("strength")
    private String strength;

    /**
     * 关系状态（NORMAL, ABNORMAL）
     */
    @TableField("status")
    private String status;

    /**
     * 关系描述
     */
    @TableField("description")
    private String description;

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
