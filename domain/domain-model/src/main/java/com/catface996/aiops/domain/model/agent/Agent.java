package com.catface996.aiops.domain.model.agent;

import java.time.LocalDateTime;

/**
 * Agent 领域模型
 *
 * <p>AI Agent 实体，用于执行自动化诊断、监控和分析任务。</p>
 * <p>配置采用扁平化设计，通过 promptTemplateId 关联提示词模板。</p>
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

    // ===== LLM 配置（扁平化） =====

    /**
     * 关联的提示词模板ID
     */
    private Long promptTemplateId;

    /**
     * 提示词模板名称（关联查询，非持久化）
     */
    private String promptTemplateName;

    /**
     * AI 模型标识（如 claude-3-opus, gemini-2.0-flash）
     */
    private String model;

    /**
     * 温度参数 (0.0-2.0)，控制输出随机性
     */
    private Double temperature;

    /**
     * Top P 参数 (0.0-1.0)，核采样阈值
     */
    private Double topP;

    /**
     * 最大输出 token 数
     */
    private Integer maxTokens;

    /**
     * 最长运行时间（秒）
     */
    private Integer maxRuntime;

    // ===== 统计信息 =====

    /**
     * 警告数量（全局累计）
     */
    private Integer warnings;

    /**
     * 严重问题数量（全局累计）
     */
    private Integer critical;

    // ===== 审计字段 =====

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

    // ===== 默认值常量 =====

    public static final String DEFAULT_MODEL = "gemini-2.0-flash";
    public static final double DEFAULT_TEMPERATURE = 0.3;
    public static final double DEFAULT_TOP_P = 0.9;
    public static final int DEFAULT_MAX_TOKENS = 4096;
    public static final int DEFAULT_MAX_RUNTIME = 300;

    public Agent() {
        this.deleted = false;
        this.warnings = 0;
        this.critical = 0;
        // 设置默认值
        this.temperature = DEFAULT_TEMPERATURE;
        this.topP = DEFAULT_TOP_P;
        this.maxTokens = DEFAULT_MAX_TOKENS;
        this.maxRuntime = DEFAULT_MAX_RUNTIME;
    }

    // ===== 工厂方法 =====

    /**
     * 创建新 Agent 的工厂方法
     *
     * @param name             Agent 名称
     * @param role             Agent 角色
     * @param specialty        专业领域
     * @param promptTemplateId 提示词模板ID（可选）
     * @param model            模型标识（可选）
     * @return 新创建的 Agent 实例
     */
    public static Agent create(String name, AgentRole role, String specialty,
                               Long promptTemplateId, String model) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setRole(role);
        agent.setSpecialty(specialty);
        agent.setPromptTemplateId(promptTemplateId);
        agent.setModel(model != null ? model : DEFAULT_MODEL);
        agent.setTemperature(DEFAULT_TEMPERATURE);
        agent.setTopP(DEFAULT_TOP_P);
        agent.setMaxTokens(DEFAULT_MAX_TOKENS);
        agent.setMaxRuntime(DEFAULT_MAX_RUNTIME);
        agent.setWarnings(0);
        agent.setCritical(0);
        agent.setDeleted(false);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        return agent;
    }

    // ===== 业务方法 =====

    /**
     * 更新 Agent 基本信息
     *
     * @param name      新名称（为 null 时不更新）
     * @param specialty 新专业领域（为 null 时不更新）
     */
    public void updateBasicInfo(String name, String specialty) {
        if (name != null) {
            this.name = name;
        }
        if (specialty != null) {
            this.specialty = specialty;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新 LLM 配置
     *
     * @param promptTemplateId 提示词模板ID（为 null 时不更新）
     * @param model            模型标识（为 null 时不更新）
     * @param temperature      温度参数（为 null 时不更新）
     * @param topP             Top P 参数（为 null 时不更新）
     * @param maxTokens        最大 token 数（为 null 时不更新）
     * @param maxRuntime       最长运行时间（为 null 时不更新）
     */
    public void updateLlmConfig(Long promptTemplateId, String model, Double temperature,
                                Double topP, Integer maxTokens, Integer maxRuntime) {
        if (promptTemplateId != null) {
            this.promptTemplateId = promptTemplateId;
        }
        if (model != null) {
            this.model = model;
        }
        if (temperature != null) {
            this.temperature = temperature;
        }
        if (topP != null) {
            this.topP = topP;
        }
        if (maxTokens != null) {
            this.maxTokens = maxTokens;
        }
        if (maxRuntime != null) {
            this.maxRuntime = maxRuntime;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加警告数量
     */
    public void incrementWarnings() {
        this.warnings = (this.warnings == null) ? 1 : this.warnings + 1;
    }

    /**
     * 增加严重问题数量
     */
    public void incrementCritical() {
        this.critical = (this.critical == null) ? 1 : this.critical + 1;
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

    // ===== 验证方法 =====

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

    // ===== Getters and Setters =====

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

    public Long getPromptTemplateId() {
        return promptTemplateId;
    }

    public void setPromptTemplateId(Long promptTemplateId) {
        this.promptTemplateId = promptTemplateId;
    }

    public String getPromptTemplateName() {
        return promptTemplateName;
    }

    public void setPromptTemplateName(String promptTemplateName) {
        this.promptTemplateName = promptTemplateName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(Integer maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public Integer getWarnings() {
        return warnings;
    }

    public void setWarnings(Integer warnings) {
        this.warnings = warnings;
    }

    public Integer getCritical() {
        return critical;
    }

    public void setCritical(Integer critical) {
        this.critical = critical;
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
                ", promptTemplateId=" + promptTemplateId +
                ", model='" + model + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
