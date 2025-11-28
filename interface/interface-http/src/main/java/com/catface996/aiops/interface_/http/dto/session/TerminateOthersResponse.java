package com.catface996.aiops.interface_.http.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 终止其他会话响应
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "终止其他会话响应")
public class TerminateOthersResponse {

    /**
     * 终止的会话数量
     */
    @Schema(description = "终止的会话数量", example = "2")
    private int terminatedCount;

    /**
     * 操作消息
     */
    @Schema(description = "操作消息", example = "已终止2个其他会话")
    private String message;

    /**
     * 创建响应
     */
    public static TerminateOthersResponse of(int count) {
        return TerminateOthersResponse.builder()
                .terminatedCount(count)
                .message(count > 0 ? "已终止" + count + "个其他会话" : "没有其他会话需要终止")
                .build();
    }
}
