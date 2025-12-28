package com.catface996.aiops.domain.model.agent;

/**
 * Agent 配置模板枚举
 *
 * <p>预定义的 Agent 配置模板，简化 Agent 创建流程。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum AgentTemplate {

    STANDARD_COORDINATOR(
            "Standard Coordinator",
            "标准协调者模板，适用于一般任务分配和协调",
            AgentRole.TEAM_SUPERVISOR,
            "你是一个专业的团队协调者。你的职责是：\n1. 分析任务并合理分配给团队成员\n2. 监控任务执行进度\n3. 汇总各成员的工作成果\n4. 发现问题时及时协调和调整",
            "gemini-2.0-flash",
            0.3
    ),

    STRICT_SECURITY_AUDITOR(
            "Strict Security Auditor",
            "严格的安全审计模板，专注于安全漏洞检测和合规性检查",
            AgentRole.WORKER,
            "你是一个严格的安全审计专家。你的职责是：\n1. 检测系统中的安全漏洞\n2. 评估安全风险等级\n3. 检查安全合规性\n4. 提供修复建议\n\n注意：任何可疑问题都应标记为警告或严重问题，宁可误报也不能漏报。",
            "gemini-2.0-flash",
            0.1
    ),

    PERFORMANCE_OPTIMIZER(
            "Performance Optimizer",
            "性能优化专家模板，专注于系统性能分析和优化建议",
            AgentRole.WORKER,
            "你是一个性能优化专家。你的职责是：\n1. 分析系统性能指标（CPU、内存、IO、网络）\n2. 识别性能瓶颈\n3. 提供具体的优化建议\n4. 评估优化效果",
            "gemini-2.0-flash",
            0.2
    ),

    ROOT_CAUSE_ANALYST(
            "Root Cause Analyst",
            "根因分析专家模板，专注于问题根因分析和故障排查",
            AgentRole.WORKER,
            "你是一个根因分析专家。你的职责是：\n1. 分析问题现象和影响范围\n2. 追溯问题发生的时间线\n3. 识别根本原因\n4. 提供修复方案和预防措施",
            "gemini-2.0-flash",
            0.2
    ),

    CONCISE_REPORTER(
            "Concise Reporter",
            "简洁报告生成器模板，专注于生成简洁明了的分析报告",
            AgentRole.WORKER,
            "你是一个专业的报告撰写专家。你的职责是：\n1. 汇总分析结果\n2. 提取关键发现\n3. 生成简洁明了的报告\n4. 突出重点和行动建议\n\n报告要求：简洁、准确、可操作，避免冗长描述。",
            "gemini-2.0-flash",
            0.4
    );

    private final String name;
    private final String description;
    private final AgentRole recommendedRole;
    private final String systemInstruction;
    private final String recommendedModel;
    private final double recommendedTemperature;

    AgentTemplate(String name, String description, AgentRole recommendedRole,
                  String systemInstruction, String recommendedModel, double recommendedTemperature) {
        this.name = name;
        this.description = description;
        this.recommendedRole = recommendedRole;
        this.systemInstruction = systemInstruction;
        this.recommendedModel = recommendedModel;
        this.recommendedTemperature = recommendedTemperature;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AgentRole getRecommendedRole() {
        return recommendedRole;
    }

    public String getSystemInstruction() {
        return systemInstruction;
    }

    public String getRecommendedModel() {
        return recommendedModel;
    }

    public double getRecommendedTemperature() {
        return recommendedTemperature;
    }

    /**
     * 根据模板创建配置
     */
    public AgentConfig toConfig() {
        return new AgentConfig(recommendedModel, recommendedTemperature, systemInstruction, null);
    }
}
