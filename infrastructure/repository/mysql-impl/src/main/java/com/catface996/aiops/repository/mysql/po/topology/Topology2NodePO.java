package com.catface996.aiops.repository.mysql.po.topology;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拓扑图-节点关联持久化对象
 *
 * <p>数据库表 topology_2_node 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("topology_2_node")
public class Topology2NodePO implements Serializable {

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
     * 节点ID
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 节点在画布上的X坐标
     */
    @TableField("position_x")
    private Integer positionX;

    /**
     * 节点在画布上的Y坐标
     */
    @TableField("position_y")
    private Integer positionY;

    /**
     * 添加时间
     */
    @TableField("added_at")
    private LocalDateTime addedAt;

    /**
     * 添加者用户ID
     */
    @TableField("added_by")
    private Long addedBy;

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 节点名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeName;

    /**
     * 节点类型编码（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeTypeCode;

    /**
     * 节点类型名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeTypeName;

    /**
     * 节点状态（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeStatus;

    /**
     * 节点架构层级（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String nodeLayer;
}
