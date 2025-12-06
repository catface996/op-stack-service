package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.auth.SessionValidationResult;
import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 会话管理兼容控制器
 *
 * <p>提供兼容性的会话管理接口，用于支持前端使用的 /api/v1/session (单数) 路径。</p>
 * <p>注意：新开发请使用 /api/v1/sessions (复数) 路径。</p>
 *
 * @author AI Assistant
 * @since 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
@Tag(name = "会话管理(兼容)", description = "会话验证接口的兼容路径")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionCompatController {

    private final AuthApplicationService authApplicationService;

    /**
     * 验证会话（兼容路径）
     *
     * <p>验证用户会话是否有效，委托给 AuthApplicationService 处理。</p>
     * <p>此接口是为了兼容前端使用的 /api/v1/session/validate 路径。</p>
     *
     * @param authorization JWT Token（包含 Bearer 前缀）
     * @return 会话验证结果
     */
    @Operation(
            summary = "验证会话",
            description = "验证用户会话是否有效，返回会话状态和用户信息。此接口是兼容路径，建议使用 /api/v1/sessions/validate"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证完成"),
            @ApiResponse(responseCode = "401", description = "Token无效")
    })
    @GetMapping("/validate")
    public ResponseEntity<Result<SessionValidationResult>> validateSession(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        log.info("接收到会话验证请求 (兼容路径 /api/v1/session/validate)");
        SessionValidationResult result = authApplicationService.validateSession(authorization);
        log.info("会话验证完成: valid={}, sessionId={}", result.isValid(), result.getSessionId());
        return ResponseEntity.ok(Result.success(result));
    }
}
