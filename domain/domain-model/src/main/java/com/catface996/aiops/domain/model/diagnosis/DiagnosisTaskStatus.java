package com.catface996.aiops.domain.model.diagnosis;

/**
 * 诊断任务状态枚举
 *
 * <p>状态转换规则：</p>
 * <ul>
 *   <li>RUNNING → COMPLETED: 诊断正常完成</li>
 *   <li>RUNNING → FAILED: executor错误或连接失败</li>
 *   <li>RUNNING → TIMEOUT: 超过10分钟未完成</li>
 *   <li>RUNNING → CANCELLED: 用户主动取消</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
public enum DiagnosisTaskStatus {

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 失败
     */
    FAILED("失败"),

    /**
     * 超时
     */
    TIMEOUT("超时"),

    /**
     * 已取消（用户主动取消）
     */
    CANCELLED("已取消");

    private final String description;

    DiagnosisTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为终态
     *
     * @return true 如果是 COMPLETED, FAILED, TIMEOUT 或 CANCELLED
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }

    /**
     * 检查是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return true 如果可以转换
     */
    public boolean canTransitionTo(DiagnosisTaskStatus target) {
        if (this == RUNNING) {
            return target == COMPLETED || target == FAILED || target == TIMEOUT || target == CANCELLED;
        }
        return false;
    }
}
