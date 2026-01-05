package com.catface996.aiops.domain.model.diagnosis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 诊断任务领域实体
 *
 * <p>代表一次诊断执行的记录，关联到一个拓扑图。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>创建时状态为 RUNNING</li>
 *   <li>正常完成时状态变为 COMPLETED，设置 completedAt</li>
 *   <li>executor错误时状态变为 FAILED，记录 errorMessage</li>
 *   <li>超过10分钟未完成时状态变为 TIMEOUT</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosisTask {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联拓扑图ID
     */
    private Long topologyId;

    /**
     * 用户诊断问题
     */
    private String userQuestion;

    /**
     * 任务状态
     */
    private DiagnosisTaskStatus status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * executor运行ID
     */
    private String runId;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    // ==================== 派生字段 ====================

    /**
     * 拓扑图名称（JOIN 查询填充）
     */
    private String topologyName;

    /**
     * Agent诊断过程列表
     */
    @Builder.Default
    private List<AgentDiagnosisProcess> agentProcesses = new ArrayList<>();

    // ==================== 工厂方法 ====================

    /**
     * 创建新的诊断任务
     *
     * @param topologyId   拓扑图ID
     * @param userQuestion 用户问题
     * @param createdBy    创建人ID
     * @return 新的诊断任务
     */
    public static DiagnosisTask create(Long topologyId, String userQuestion, Long createdBy) {
        return DiagnosisTask.builder()
                .topologyId(topologyId)
                .userQuestion(userQuestion)
                .status(DiagnosisTaskStatus.RUNNING)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .agentProcesses(new ArrayList<>())
                .build();
    }

    // ==================== 状态转换方法 ====================

    /**
     * 设置executor运行ID
     *
     * @param runId executor分配的运行ID
     */
    public void setRunId(String runId) {
        this.runId = runId;
    }

    /**
     * 标记为已完成
     */
    public void markCompleted() {
        if (!status.canTransitionTo(DiagnosisTaskStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot transition from " + status + " to COMPLETED");
        }
        this.status = DiagnosisTaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 标记为失败
     *
     * @param errorMessage 错误信息
     */
    public void markFailed(String errorMessage) {
        if (!status.canTransitionTo(DiagnosisTaskStatus.FAILED)) {
            throw new IllegalStateException("Cannot transition from " + status + " to FAILED");
        }
        this.status = DiagnosisTaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 标记为超时
     */
    public void markTimeout() {
        if (!status.canTransitionTo(DiagnosisTaskStatus.TIMEOUT)) {
            throw new IllegalStateException("Cannot transition from " + status + " to TIMEOUT");
        }
        this.status = DiagnosisTaskStatus.TIMEOUT;
        this.errorMessage = "诊断任务执行超时";
        this.completedAt = LocalDateTime.now();
    }

    // ==================== 查询方法 ====================

    /**
     * 检查任务是否仍在运行中
     *
     * @return true 如果状态为 RUNNING
     */
    public boolean isRunning() {
        return status == DiagnosisTaskStatus.RUNNING;
    }

    /**
     * 检查任务是否已完成（包括成功、失败、超时）
     *
     * @return true 如果状态为终态
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * 获取运行时长（秒）
     *
     * @return 运行时长秒数，如果已完成则返回总时长
     */
    public long getRunningDurationSeconds() {
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(createdAt, endTime).getSeconds();
    }

    /**
     * 设置拓扑图名称
     *
     * @param topologyName 拓扑图名称
     */
    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    /**
     * 设置Agent诊断过程列表
     *
     * @param agentProcesses Agent诊断过程列表
     */
    public void setAgentProcesses(List<AgentDiagnosisProcess> agentProcesses) {
        this.agentProcesses = agentProcesses != null ? agentProcesses : new ArrayList<>();
    }

    /**
     * 添加Agent诊断过程
     *
     * @param process Agent诊断过程
     */
    public void addAgentProcess(AgentDiagnosisProcess process) {
        if (this.agentProcesses == null) {
            this.agentProcesses = new ArrayList<>();
        }
        this.agentProcesses.add(process);
    }

    /**
     * 获取参与的Agent数量
     *
     * @return Agent数量
     */
    public int getAgentCount() {
        return agentProcesses != null ? agentProcesses.size() : 0;
    }
}
