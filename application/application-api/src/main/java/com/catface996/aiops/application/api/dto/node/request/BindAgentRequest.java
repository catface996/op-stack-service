package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绑定 Agent 请求
 *
 * <p>将指定的 Agent 绑定到指定的资源节点</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-002: 系统必须提供绑定接口，允许将一个 Agent 绑定到一个 ResourceNode</li>
 *   <li>FR-008: 系统必须在绑定时验证 Agent 和 ResourceNode 的有效性</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Schema(description = "绑定 Agent 请求")
public class BindAgentRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nodeId;

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long agentId;

    @Schema(description = "操作人ID（网关注入）", hidden = true)
    private Long operatorId;
}
