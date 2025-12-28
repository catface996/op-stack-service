package com.catface996.aiops.domain.model.agent;

/**
 * Agent 发现统计值对象
 *
 * <p>记录 Agent 发现的问题数量（全局累计）。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class AgentFindings {

    /**
     * 警告数量
     */
    private Integer warnings;

    /**
     * 严重问题数量
     */
    private Integer critical;

    public AgentFindings() {
        this.warnings = 0;
        this.critical = 0;
    }

    public AgentFindings(Integer warnings, Integer critical) {
        this.warnings = warnings != null ? warnings : 0;
        this.critical = critical != null ? critical : 0;
    }

    /**
     * 创建空的发现统计
     *
     * @return 空的发现统计实例
     */
    public static AgentFindings empty() {
        return new AgentFindings(0, 0);
    }

    /**
     * 增加警告数量
     */
    public void incrementWarnings() {
        this.warnings++;
    }

    /**
     * 增加严重问题数量
     */
    public void incrementCritical() {
        this.critical++;
    }

    /**
     * 增加指定数量的警告
     *
     * @param count 增加的数量
     */
    public void addWarnings(int count) {
        this.warnings += count;
    }

    /**
     * 增加指定数量的严重问题
     *
     * @param count 增加的数量
     */
    public void addCritical(int count) {
        this.critical += count;
    }

    /**
     * 获取总问题数
     *
     * @return 警告 + 严重问题的总数
     */
    public int getTotal() {
        return warnings + critical;
    }

    // Getters and Setters

    public Integer getWarnings() {
        return warnings;
    }

    public void setWarnings(Integer warnings) {
        this.warnings = warnings != null ? warnings : 0;
    }

    public Integer getCritical() {
        return critical;
    }

    public void setCritical(Integer critical) {
        this.critical = critical != null ? critical : 0;
    }

    @Override
    public String toString() {
        return "AgentFindings{" +
                "warnings=" + warnings +
                ", critical=" + critical +
                '}';
    }
}
