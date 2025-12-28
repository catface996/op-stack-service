package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 解绑 Agent 请求
 *
 * <p>解除指定 Agent 与指定资源节点的绑定关系</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-003: 系统必须提供解绑接口，允许解除 Agent 与 ResourceNode 的绑定关系</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Schema(description = "解绑 Agent 请求")
public class UnbindAgentRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nodeId;

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long agentId;

    @Schema(description = "操作人ID（网关注入）", hidden = true)
    private Long operatorId;
}
