package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新资源请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-010: 编辑资源基本信息</li>
 *   <li>REQ-FR-013: 乐观锁机制</li>
 *   <li>REQ-FR-014: 敏感信息重新加密</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新资源请求")
public class UpdateResourceRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;

    @Schema(description = "资源名称（null表示不修改）", example = "web-server-02")
    @Size(max = 100, message = "资源名称最长100个字符")
    private String name;

    @Schema(description = "资源描述（null表示不修改）", example = "测试环境Web服务器")
    @Size(max = 500, message = "资源描述最长500个字符")
    private String description;

    @Schema(description = "扩展属性（JSON格式，null表示不修改）", example = "{\"ip\": \"192.168.1.101\", \"port\": 8081}")
    private String attributes;

    @Schema(description = "当前版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
