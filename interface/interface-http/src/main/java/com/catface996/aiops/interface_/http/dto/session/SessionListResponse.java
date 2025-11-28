package com.catface996.aiops.interface_.http.dto.session;

import com.catface996.aiops.application.api.dto.session.SessionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会话列表响应
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话列表响应")
public class SessionListResponse {

    /**
     * 会话列表
     */
    @Schema(description = "会话列表")
    private List<SessionDTO> sessions;

    /**
     * 会话总数
     */
    @Schema(description = "会话总数", example = "3")
    private int total;

    /**
     * 从会话列表创建响应
     */
    public static SessionListResponse of(List<SessionDTO> sessions) {
        return SessionListResponse.builder()
                .sessions(sessions)
                .total(sessions != null ? sessions.size() : 0)
                .build();
    }
}
