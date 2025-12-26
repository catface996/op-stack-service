package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新拓扑图请求
 *
 * <p>用于更新已存在的拓扑图资源。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供独立的拓扑图创建接口</li>
 *   <li>US3: 创建拓扑图（更新属于拓扑图管理范畴）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新拓扑图请求")
public class UpdateTopologyRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long id;

    @Schema(description = "拓扑图名称（可选，null表示不修改）", example = "电商系统架构图v2")
    @Size(max = 100, message = "拓扑图名称最长100个字符")
    private String name;

    @Schema(description = "拓扑图描述（可选，null表示不修改）", example = "更新后的电商系统架构描述")
    @Size(max = 500, message = "描述最长500个字符")
    private String description;

    @Schema(description = "协调 Agent ID（可选，null表示不修改）")
    private Long coordinatorAgentId;

    @Schema(description = "版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
