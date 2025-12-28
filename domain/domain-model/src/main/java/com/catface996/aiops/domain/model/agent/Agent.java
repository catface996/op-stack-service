package com.catface996.aiops.domain.model.agent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 领域模型
 *
 * <p>AI Agent 实体，用于执行自动化诊断、监控和分析任务。</p>
 * <p>注意：status 字段已移至 AgentTeamRelation，按 Team 区分。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class Agent {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * Agent 名称
     */
    private String name;

    /**
     * Agent 角色
     */
    private AgentRole role;

    /**
     * 专业领域
     */
    private String specialty;

    /**
     * 发现统计（全局累计）
     */
    private AgentFindings findings;

    /**
     * AI 配置
     */
    private AgentConfig config;

    /**
     * 关联的团队 ID 列表
     */
    private List<Long> teamIds;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    public Agent() {
        this.deleted = false;
        this.teamIds = new ArrayList<>();
    }

    // 工厂方法

    /**
     * 创建新 Agent 的工厂方法
     *
     * @param name      Agent 名称
     * @param role      Agent 角色
     * @param specialty 专业领域
     * @param config    AI 配置（可选，为 null 时使用默认配置）
     * @return 新创建的 Agent 实例
     */
    public static Agent create(String name, AgentRole role, String specialty, AgentConfig config) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setRole(role);
        agent.setSpecialty(specialty);
        agent.setConfig(config != null ? config : AgentConfig.defaults());
        agent.setFindings(AgentFindings.empty());
        agent.setTeamIds(new ArrayList<>());
        agent.setDeleted(false);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        return agent;
    }

    // 业务方法

    /**
     * 更新 Agent 基本信息
     *
     * @param name      新名称（为 null 时不更新）
     * @param specialty 新专业领域（为 null 时不更新）
     */
    public void update(String name, String specialty) {
        if (name != null) {
            this.name = name;
        }
        if (specialty != null) {
            this.specialty = specialty;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新 AI 配置
     *
     * @param config 新配置
     */
    public void updateConfig(AgentConfig config) {
        if (config != null) {
            this.config = config;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 增加警告数量
     */
    public void incrementWarnings() {
        if (this.findings == null) {
            this.findings = AgentFindings.empty();
        }
        this.findings.incrementWarnings();
    }

    /**
     * 增加严重问题数量
     */
    public void incrementCritical() {
        if (this.findings == null) {
            this.findings = AgentFindings.empty();
        }
        this.findings.incrementCritical();
    }

    /**
     * 软删除
     */
    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查 Agent 是否已删除
     *
     * @return true 如果已删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    // 验证方法

    /**
     * 检查 Agent 是否可以被删除
     *
     * @return true 如果可以删除（非 GLOBAL_SUPERVISOR）
     */
    public boolean canBeDeleted() {
        return role != AgentRole.GLOBAL_SUPERVISOR;
    }

    /**
     * 检查是否为 GLOBAL_SUPERVISOR
     *
     * @return true 如果是 GLOBAL_SUPERVISOR
     */
    public boolean isGlobalSupervisor() {
        return role == AgentRole.GLOBAL_SUPERVISOR;
    }

    /**
     * 检查是否为 TEAM_SUPERVISOR
     *
     * @return true 如果是 TEAM_SUPERVISOR
     */
    public boolean isTeamSupervisor() {
        return role == AgentRole.TEAM_SUPERVISOR;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AgentRole getRole() {
        return role;
    }

    public void setRole(AgentRole role) {
        this.role = role;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public AgentFindings getFindings() {
        return findings;
    }

    public void setFindings(AgentFindings findings) {
        this.findings = findings;
    }

    public AgentConfig getConfig() {
        return config;
    }

    public void setConfig(AgentConfig config) {
        this.config = config;
    }

    public List<Long> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<Long> teamIds) {
        this.teamIds = teamIds != null ? teamIds : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", specialty='" + specialty + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
