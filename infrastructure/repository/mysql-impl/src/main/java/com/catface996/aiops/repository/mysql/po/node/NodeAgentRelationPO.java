package com.catface996.aiops.repository.mysql.po.node;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Node-Agent 关联持久化对象
 *
 * <p>数据库表 node_2_agent 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@TableName("node_2_agent")
public class NodeAgentRelationPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联记录 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资源节点 ID
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * Agent ID
     */
    @TableField("agent_id")
    private Long agentId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
