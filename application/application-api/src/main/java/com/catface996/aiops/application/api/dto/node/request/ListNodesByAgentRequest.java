package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询 Agent 关联的节点列表请求
 *
 * <p>查询指定 Agent 关联的所有资源节点</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-005: 系统必须提供查询接口，支持根据 Agent ID 查询关联的节点列表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Schema(description = "查询 Agent 关联的节点列表请求")
public class ListNodesByAgentRequest {

    @NotNull(message = "AgentID不能为空")
    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long agentId;
}
