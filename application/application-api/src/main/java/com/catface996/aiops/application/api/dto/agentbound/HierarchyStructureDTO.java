package com.catface996.aiops.application.api.dto.agentbound;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 层级结构 DTO
 *
 * <p>表示 Topology 的完整层级化 Agent 团队结构，包含：</p>
 * <ul>
 *   <li>Topology 基本信息</li>
 *   <li>Global Supervisor: 拓扑图绑定的全局监管者</li>
 *   <li>Teams: 各资源节点对应的团队列表</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-009: 查询指定 Topology 的完整层级结构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "层级结构信息")
public class HierarchyStructureDTO {

    @Schema(description = "拓扑图 ID", example = "1")
    private Long topologyId;

    @Schema(description = "拓扑图名称", example = "生产环境")
    private String topologyName;

    @Schema(description = "全局监管者 Agent（可为 null）")
    private AgentDTO globalSupervisor;

    @Schema(description = "团队列表（每个节点对应一个团队）")
    private List<HierarchyTeamDTO> teams;
}
