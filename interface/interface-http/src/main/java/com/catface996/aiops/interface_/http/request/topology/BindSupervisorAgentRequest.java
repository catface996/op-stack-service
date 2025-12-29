package com.catface996.aiops.interface_.http.request.topology;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 绑定 Global Supervisor Agent 请求
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Schema(description = "绑定 Global Supervisor Agent 请求")
public class BindSupervisorAgentRequest {

    @Schema(description = "拓扑图ID", example = "43", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "Agent ID（必须为 GLOBAL_SUPERVISOR 角色）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID不能为空")
    private Long agentId;

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    // ==================== Getters and Setters ====================

    public Long getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(Long topologyId) {
        this.topologyId = topologyId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
}
