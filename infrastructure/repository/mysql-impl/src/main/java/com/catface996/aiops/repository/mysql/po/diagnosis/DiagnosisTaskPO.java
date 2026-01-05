package com.catface996.aiops.repository.mysql.po.diagnosis;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 诊断任务持久化对象
 *
 * <p>数据库表 diagnosis_task 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Data
@TableName("diagnosis_task")
public class DiagnosisTaskPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联拓扑图ID
     */
    @TableField("topology_id")
    private Long topologyId;

    /**
     * 用户诊断问题
     */
    @TableField("user_question")
    private String userQuestion;

    /**
     * 状态: RUNNING, COMPLETED, FAILED, TIMEOUT
     */
    @TableField("status")
    private String status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * executor运行ID
     */
    @TableField("run_id")
    private String runId;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

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

    // ==================== 派生字段（不映射到数据库）====================

    /**
     * 拓扑图名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String topologyName;

    /**
     * Agent数量（统计查询填充）
     */
    @TableField(exist = false)
    private Integer agentCount;
}
