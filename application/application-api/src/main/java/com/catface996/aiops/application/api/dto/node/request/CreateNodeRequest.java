package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建节点请求
 *
 * <p>用于创建新的资源节点。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供独立的节点创建接口</li>
 *   <li>US4: 创建节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建节点请求")
public class CreateNodeRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "节点名称", example = "web-server-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节点名称不能为空")
    @Size(max = 100, message = "节点名称最长100个字符")
    private String name;

    @Schema(description = "节点描述", example = "生产环境Web服务器")
    @Size(max = 500, message = "描述最长500个字符")
    private String description;

    @Schema(description = "节点类型ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点类型ID不能为空")
    private Long nodeTypeId;

    @Schema(description = "架构层级", example = "BUSINESS_APPLICATION",
            allowableValues = {"BUSINESS_SCENARIO", "BUSINESS_FLOW", "BUSINESS_APPLICATION", "MIDDLEWARE", "INFRASTRUCTURE"})
    private String layer;

    @Schema(description = "扩展属性（JSON格式）", example = "{\"ip\": \"192.168.1.100\"}")
    @Size(max = 4000, message = "扩展属性最长4000个字符")
    private String attributes;
}
