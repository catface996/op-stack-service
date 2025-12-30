package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建拓扑图请求
 *
 * <p>用于创建新的拓扑图资源。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供独立的拓扑图创建接口</li>
 *   <li>US3: 创建拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建拓扑图请求")
public class CreateTopologyRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "拓扑图名称", example = "电商系统架构图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "拓扑图名称不能为空")
    @Size(max = 100, message = "拓扑图名称最长100个字符")
    private String name;

    @Schema(description = "拓扑图描述", example = "展示电商系统整体架构和组件关系")
    @Size(max = 500, message = "描述最长500个字符")
    private String description;
}
