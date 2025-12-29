package com.catface996.aiops.application.api.dto.agentbound;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 层级团队 DTO
 *
 * <p>表示资源节点对应的团队结构，包含：</p>
 * <ul>
 *   <li>节点基本信息（ID、名称）</li>
 *   <li>Team Supervisor: 团队监管者</li>
 *   <li>Team Workers: 团队工作者列表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "层级团队信息")
public class HierarchyTeamDTO {

    @Schema(description = "资源节点 ID", example = "10")
    private Long nodeId;

    @Schema(description = "资源节点名称", example = "Web Server")
    private String nodeName;

    @Schema(description = "团队监管者 Agent（可为 null）")
    private AgentDTO supervisor;

    @Schema(description = "团队工作者 Agent 列表（可为空）")
    private List<AgentDTO> workers;
}
