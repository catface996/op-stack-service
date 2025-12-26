package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新节点请求
 *
 * <p>用于更新已存在的资源节点。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供独立的节点管理接口</li>
 *   <li>US4: 节点管理（更新属于节点管理范畴）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新节点请求")
public class UpdateNodeRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点ID不能为空")
    private Long id;

    @Schema(description = "节点名称（可选，null表示不修改）", example = "web-server-02")
    @Size(max = 100, message = "节点名称最长100个字符")
    private String name;

    @Schema(description = "节点描述（可选，null表示不修改）", example = "更新后的描述")
    @Size(max = 500, message = "描述最长500个字符")
    private String description;

    @Schema(description = "Agent Team ID（可选，null表示不修改）")
    private Long agentTeamId;

    @Schema(description = "扩展属性（可选，null表示不修改）", example = "{\"ip\": \"192.168.1.101\"}")
    @Size(max = 4000, message = "扩展属性最长4000个字符")
    private String attributes;

    @Schema(description = "版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
