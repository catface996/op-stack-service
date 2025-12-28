package com.catface996.aiops.repository.mysql.po.agent;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent-Team 关联持久化对象
 *
 * <p>数据库表 agent_2_team 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@TableName("agent_2_team")
public class AgentTeamRelationPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Agent ID
     */
    @TableField("agent_id")
    private Long agentId;

    /**
     * Team ID
     */
    @TableField("team_id")
    private Long teamId;

    /**
     * Agent 在该团队的工作状态
     */
    @TableField("status")
    private String status;

    /**
     * 当前任务描述
     */
    @TableField("current_task")
    private String currentTask;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
