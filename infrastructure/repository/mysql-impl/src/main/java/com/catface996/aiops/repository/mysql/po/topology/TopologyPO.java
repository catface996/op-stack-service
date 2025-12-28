package com.catface996.aiops.repository.mysql.po.topology;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拓扑图持久化对象
 *
 * <p>数据库表 topology 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@TableName("topology")
public class TopologyPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 拓扑图名称
     */
    @TableField("name")
    private String name;

    /**
     * 拓扑图描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态（RUNNING, STOPPED, MAINTENANCE, OFFLINE）
     */
    @TableField("status")
    private String status;

    /**
     * 协调 Agent ID
     */
    @TableField("coordinator_agent_id")
    private Long coordinatorAgentId;

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

    // ==================== 派生字段（不映射到数据库，用于统计查询结果）====================

    /**
     * 成员数量（用于 JOIN 查询填充）
     */
    @TableField(exist = false)
    private Integer memberCount;
}
