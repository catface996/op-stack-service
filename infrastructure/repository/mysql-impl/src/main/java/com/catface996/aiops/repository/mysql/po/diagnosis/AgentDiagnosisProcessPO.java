package com.catface996.aiops.repository.mysql.po.diagnosis;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent诊断过程持久化对象
 *
 * <p>数据库表 agent_diagnosis_process 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Data
@TableName("agent_diagnosis_process")
public class AgentDiagnosisProcessPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联诊断任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 关联AgentBound ID
     */
    @TableField("agent_bound_id")
    private Long agentBoundId;

    /**
     * Agent名称（冗余存储）
     */
    @TableField("agent_name")
    private String agentName;

    /**
     * 诊断内容（整合后的完整文本）
     */
    @TableField("content")
    private String content;

    /**
     * Agent开始诊断时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * Agent结束诊断时间
     */
    @TableField("ended_at")
    private LocalDateTime endedAt;

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

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
