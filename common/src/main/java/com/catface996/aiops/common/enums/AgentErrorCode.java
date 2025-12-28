package com.catface996.aiops.common.enums;

/**
 * Agent 相关错误码
 *
 * <p>包括 Agent 不存在、冲突、参数无效、锁定等错误。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum AgentErrorCode implements ErrorCode {

    // ==================== 资源不存在 (404) ====================

    /**
     * Agent 不存在
     */
    AGENT_NOT_FOUND("AGT_NOT_FOUND_001", "Agent 不存在: {0}"),

    /**
     * Team 不存在
     */
    TEAM_NOT_FOUND("AGT_NOT_FOUND_002", "Team 不存在: {0}"),

    /**
     * Agent-Team 关联不存在
     */
    AGENT_TEAM_RELATION_NOT_FOUND("AGT_NOT_FOUND_003", "Agent 未分配到该 Team: agentId={0}, teamId={1}"),

    // ==================== 冲突错误 (409) ====================

    /**
     * Agent 名称已存在
     */
    AGENT_NAME_EXISTS("AGT_CONFLICT_001", "Agent 名称已存在: {0}"),

    /**
     * GLOBAL_SUPERVISOR 已存在（单例约束）
     */
    GLOBAL_SUPERVISOR_EXISTS("AGT_CONFLICT_002", "系统中已存在 GLOBAL_SUPERVISOR，不能创建第二个"),

    /**
     * Agent 已分配到该 Team
     */
    AGENT_ALREADY_ASSIGNED("AGT_CONFLICT_003", "Agent 已分配到该 Team: agentId={0}, teamId={1}"),

    // ==================== 参数错误 (400) ====================

    /**
     * 无效的 Agent 角色
     */
    INVALID_AGENT_ROLE("AGT_PARAM_001", "无效的 Agent 角色: {0}"),

    /**
     * 无效的 Agent 状态
     */
    INVALID_AGENT_STATUS("AGT_PARAM_002", "无效的 Agent 状态: {0}"),

    /**
     * temperature 参数超出范围
     */
    INVALID_TEMPERATURE("AGT_PARAM_003", "temperature 参数必须在 0.0-1.0 范围内: {0}"),

    /**
     * 不允许删除 GLOBAL_SUPERVISOR
     */
    CANNOT_DELETE_GLOBAL_SUPERVISOR("AGT_PARAM_004", "不允许删除 GLOBAL_SUPERVISOR"),

    /**
     * 不允许删除有成员的 TEAM_SUPERVISOR
     */
    CANNOT_DELETE_SUPERVISOR_WITH_MEMBERS("AGT_PARAM_005", "不允许删除有成员的 TEAM_SUPERVISOR: {0}"),

    /**
     * TEAM_SUPERVISOR 不能取消团队分配
     */
    SUPERVISOR_CANNOT_UNASSIGN("AGT_PARAM_006", "TEAM_SUPERVISOR 不能取消团队分配"),

    // ==================== 锁定错误 (423) ====================

    /**
     * Agent 正在工作中，无法更新或删除
     */
    AGENT_IS_BUSY("AGT_LOCKED_001", "Agent 正在工作中 (status: {0})，无法更新或删除");

    private final String code;
    private final String message;

    AgentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
