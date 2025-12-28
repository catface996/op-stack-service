package com.catface996.aiops.domain.model.agent;

import java.time.LocalDateTime;

/**
 * Agent-Team 关联领域模型
 *
 * <p>Agent 与 Team 的多对多关系实体，包含按 Team 区分的状态和当前任务。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class AgentTeamRelation {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * Team ID
     */
    private Long teamId;

    /**
     * Agent 在该 Team 的工作状态
     */
    private AgentStatus status;

    /**
     * 当前任务描述
     */
    private String currentTask;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    public AgentTeamRelation() {
        this.deleted = false;
    }

    // 工厂方法

    /**
     * 创建新的 Agent-Team 关联
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 新创建的关联实例
     */
    public static AgentTeamRelation create(Long agentId, Long teamId) {
        AgentTeamRelation relation = new AgentTeamRelation();
        relation.setAgentId(agentId);
        relation.setTeamId(teamId);
        relation.setStatus(AgentStatus.IDLE);
        relation.setCurrentTask(null);
        relation.setDeleted(false);
        relation.setCreatedAt(LocalDateTime.now());
        return relation;
    }

    // 状态管理方法

    /**
     * 开始工作
     *
     * @param task 任务描述
     */
    public void startWorking(String task) {
        this.status = AgentStatus.WORKING;
        this.currentTask = task;
    }

    /**
     * 开始思考
     */
    public void startThinking() {
        this.status = AgentStatus.THINKING;
    }

    /**
     * 完成任务
     */
    public void complete() {
        this.status = AgentStatus.COMPLETED;
        this.currentTask = null;
    }

    /**
     * 重置状态
     */
    public void reset() {
        this.status = AgentStatus.IDLE;
        this.currentTask = null;
    }

    /**
     * 设置错误状态
     */
    public void setError() {
        this.status = AgentStatus.ERROR;
    }

    /**
     * 设置等待状态
     */
    public void setWaiting() {
        this.status = AgentStatus.WAITING;
    }

    /**
     * 更新状态
     *
     * @param status 新状态
     */
    public void updateStatus(AgentStatus status) {
        this.status = status;
    }

    /**
     * 软删除
     */
    public void markDeleted() {
        this.deleted = true;
    }

    // 验证方法

    /**
     * 检查是否可以更新（非工作状态）
     *
     * @return true 如果状态不是 WORKING 或 THINKING
     */
    public boolean canBeUpdated() {
        return status == null || !status.isBusy();
    }

    /**
     * 检查是否正在工作
     *
     * @return true 如果状态是 WORKING 或 THINKING
     */
    public boolean isWorking() {
        return status != null && status.isBusy();
    }

    /**
     * 检查是否已删除
     *
     * @return true 如果已删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "AgentTeamRelation{" +
                "id=" + id +
                ", agentId=" + agentId +
                ", teamId=" + teamId +
                ", status=" + status +
                ", currentTask='" + currentTask + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
