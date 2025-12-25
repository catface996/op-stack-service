package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建资源请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001: 创建资源</li>
 *   <li>REQ-FR-004: 敏感配置加密存储</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建资源请求")
public class CreateResourceRequest {

    @Schema(description = "操作者ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作者ID不能为空")
    private Long operatorId;

    @Schema(description = "资源名称", example = "web-server-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "资源名称不能为空")
    @Size(max = 100, message = "资源名称最长100个字符")
    private String name;

    @Schema(description = "资源描述", example = "生产环境Web服务器")
    @Size(max = 500, message = "资源描述最长500个字符")
    private String description;

    @Schema(description = "资源类型ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源类型ID不能为空")
    private Long resourceTypeId;

    @Schema(description = "扩展属性（JSON格式）", example = "{\"ip\": \"192.168.1.100\", \"port\": 8080}")
    private String attributes;
}
