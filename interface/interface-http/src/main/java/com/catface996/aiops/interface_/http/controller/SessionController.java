package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.auth.LoginResult;
import com.catface996.aiops.application.api.dto.auth.SessionValidationResult;
import com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest;
import com.catface996.aiops.application.api.dto.session.SessionDTO;
import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.application.api.service.session.SessionApplicationService;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import com.catface996.aiops.interface_.http.dto.session.SessionListResponse;
import com.catface996.aiops.interface_.http.dto.session.TerminateOthersResponse;
import com.catface996.aiops.interface_.http.request.session.QuerySessionsRequest;
import com.catface996.aiops.interface_.http.request.session.TerminateOthersRequest;
import com.catface996.aiops.interface_.http.request.session.TerminateSessionRequest;
import com.catface996.aiops.interface_.http.request.session.ValidateSessionRequest;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器（POST-Only API）
 *
 * <p>提供会话管理相关的 HTTP 接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/sessions/validate - 验证会话</li>
 *   <li>POST /api/v1/sessions/query - 获取当前用户的所有会话</li>
 *   <li>POST /api/v1/sessions/terminate - 终止指定会话</li>
 *   <li>POST /api/v1/sessions/terminate-others - 终止其他会话</li>
 *   <li>POST /api/v1/sessions/force-logout-others - 强制登出其他设备</li>
 * </ul>
 *
 * <p>统一响应格式：</p>
 * <pre>
 * {
 *   "code": 0,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>HTTP 状态码规范：</p>
 * <ul>
 *   <li>200 OK - 操作成功</li>
 *   <li>400 Bad Request - 请求参数错误</li>
 *   <li>401 Unauthorized - 未认证或Token无效</li>
 *   <li>403 Forbidden - 权限不足</li>
 *   <li>404 Not Found - 资源不存在</li>
 *   <li>500 Internal Server Error - 服务器内部错误</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-007: 会话管理</li>
 *   <li>REQ-FR-009: 会话互斥</li>
 *   <li>Feature 024: POST-Only API 重构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "会话管理", description = "会话验证、会话互斥、多设备会话管理等功能")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionController {

    private final AuthApplicationService authApplicationService;
    private final SessionApplicationService sessionApplicationService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 验证会话
     *
     * <p>验证用户会话是否有效，包括以下流程：</p>
     * <ol>
     *   <li>解析 JWT Token 获取会话ID</li>
     *   <li>从 Redis 查询会话信息（优先）</li>
     *   <li>如果 Redis 未命中，从 MySQL 查询（降级）</li>
     *   <li>检查会话是否过期</li>
     *   <li>返回会话验证结果</li>
     * </ol>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/sessions/validate
     * Content-Type: application/json
     *
     * {
     *   "authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "valid": true,
     *     "userInfo": {
     *       "accountId": 12345,
     *       "username": "john_doe",
     *       "email": "john@example.com",
     *       "role": "ROLE_USER",
     *       "status": "ACTIVE"
     *     },
     *     "sessionId": "550e8400-e29b-41d4-a716-446655440000",
     *     "expiresAt": "2025-11-25T14:30:00",
     *     "remainingSeconds": 7200,
     *     "message": "会话有效"
     *   }
     * }
     * </pre>
     *
     * <p>会话无效响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "valid": false,
     *     "userInfo": null,
     *     "sessionId": null,
     *     "expiresAt": null,
     *     "remainingSeconds": 0,
     *     "message": "会话已过期，请重新登录"
     *   }
     * }
     * </pre>
     *
     * <p>错误响应示例（401 Unauthorized）：</p>
     * <pre>
     * {
     *   "code": 401001,
     *   "message": "Token 格式错误",
     *   "data": null
     * }
     * </pre>
     *
     * @param request 验证会话请求，包含 authorization
     * @return 会话验证结果
     * @throws IllegalArgumentException 当 Token 无效时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     * @throws com.catface996.aiops.common.exception.SessionExpiredException 当会话已过期时抛出
     */
    @Operation(
            summary = "验证会话",
            description = "验证用户会话是否有效，返回会话状态和用户信息"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证完成"),
            @ApiResponse(responseCode = "401", description = "Token无效")
    })
    @PostMapping("/validate")
    public ResponseEntity<Result<SessionValidationResult>> validateSession(
            @Valid @RequestBody ValidateSessionRequest request) {
        log.info("接收到会话验证请求");
        SessionValidationResult result = authApplicationService.validateSession(request.getAuthorization());
        log.info("会话验证完成: valid={}, sessionId={}", result.isValid(), result.getSessionId());
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 强制登出其他设备
     *
     * <p>使当前用户在其他设备的会话失效，然后在当前设备重新登录，包括以下流程：</p>
     * <ol>
     *   <li>解析请求中的 JWT Token 获取用户ID</li>
     *   <li>验证密码是否正确（安全验证）</li>
     *   <li>查询该用户的所有活跃会话</li>
     *   <li>删除所有旧会话（包括当前会话）</li>
     *   <li>创建新会话并生成新的 JWT Token</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>用户发现账号在其他设备上登录，怀疑账号被盗用</li>
     *   <li>用户希望清除所有旧会话，重新开始</li>
     *   <li>用户在新设备上登录，但旧设备会话未过期</li>
     * </ul>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/session/force-logout-others
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * Content-Type: application/json
     *
     * {
     *   "token": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "password": "SecureP@ss123"
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "userInfo": {
     *       "accountId": 12345,
     *       "username": "john_doe",
     *       "email": "john@example.com",
     *       "role": "ROLE_USER",
     *       "status": "ACTIVE"
     *     },
     *     "sessionId": "660f9511-f3ac-52e5-b827-557766551111",
     *     "expiresAt": "2025-11-25T14:30:00",
     *     "deviceInfo": "Chrome 120.0 on Windows 11",
     *     "message": "已强制登出其他设备，请使用新 Token 进行访问"
     *   }
     * }
     * </pre>
     *
     * <p>错误响应示例（400 Bad Request - 密码错误）：</p>
     * <pre>
     * {
     *   "code": 400002,
     *   "message": "密码错误",
     *   "data": null
     * }
     * </pre>
     *
     * <p>错误响应示例（401 Unauthorized - Token无效）：</p>
     * <pre>
     * {
     *   "code": 401001,
     *   "message": "Token 无效",
     *   "data": null
     * }
     * </pre>
     *
     * <p>安全机制：</p>
     * <ul>
     *   <li>需要验证密码，防止他人滥用此功能</li>
     *   <li>需要提供当前有效的 JWT Token，证明当前会话有效</li>
     *   <li>操作会记录到审计日志，便于追踪</li>
     *   <li>连续5次密码错误会锁定账号30分钟</li>
     * </ul>
     *
     * @param authorization JWT Token（包含 Bearer 前缀），通过请求头传递
     * @param request 强制登出请求，包含 Token 和密码
     * @return 登录结果，包含新的 JWT Token、用户信息和会话信息
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.AuthenticationException 当密码验证失败时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     * @throws com.catface996.aiops.common.exception.AccountLockedException 当账号被锁定时抛出
     */
    @Operation(
            summary = "强制登出其他设备",
            description = "使当前用户在其他设备的会话失效，需要验证密码。操作成功后返回新的Token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "密码错误"),
            @ApiResponse(responseCode = "401", description = "Token无效")
    })
    @PostMapping("/force-logout-others")
    public ResponseEntity<Result<LoginResult>> forceLogoutOthers(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ForceLogoutRequest request) {
        log.info("接收到强制登出其他设备请求");

        // 使用请求头中的 Token，确保使用当前会话的 Token
        ForceLogoutRequest actualRequest = ForceLogoutRequest.of(authorization, request.getPassword());
        LoginResult result = authApplicationService.forceLogoutOthers(actualRequest);

        log.info("强制登出其他设备成功: accountId={}, username={}, newSessionId={}",
                result.getUserInfo().getAccountId(),
                result.getUserInfo().getUsername(),
                result.getSessionId());
        return ResponseEntity.ok(Result.success(result));
    }

    // ================== 会话管理接口（POST-Only API） ==================

    /**
     * 获取当前用户的所有会话
     *
     * <p>查询当前用户的所有活跃会话，用于多设备会话管理。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/sessions/query
     * Content-Type: application/json
     *
     * {
     *   "authorization": "Bearer eyJhbGciOiJIUzUxMiJ9..."
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "sessions": [
     *       {
     *         "sessionId": "550e8400-e29b-41d4-a716-446655440000",
     *         "ipAddress": "192.168.1.100",
     *         "deviceType": "Desktop",
     *         "operatingSystem": "Windows 10",
     *         "browser": "Chrome",
     *         "createdAt": "2025-01-28T10:00:00",
     *         "lastActivityAt": "2025-01-28T12:30:00",
     *         "expiresAt": "2025-01-28T18:00:00",
     *         "currentSession": true
     *       },
     *       {
     *         "sessionId": "660f9511-f3ac-52e5-b827-557766551111",
     *         "ipAddress": "10.0.0.50",
     *         "deviceType": "Mobile",
     *         "operatingSystem": "iOS 17",
     *         "browser": "Safari",
     *         "createdAt": "2025-01-28T09:00:00",
     *         "lastActivityAt": "2025-01-28T11:00:00",
     *         "expiresAt": "2025-01-28T17:00:00",
     *         "currentSession": false
     *       }
     *     ],
     *     "total": 2
     *   }
     * }
     * </pre>
     *
     * @param request 查询会话请求，包含 authorization
     * @return 会话列表响应
     */
    @Operation(
            summary = "获取当前用户的所有会话",
            description = "查询当前用户的所有活跃会话，用于多设备会话管理"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "Token无效")
    })
    @PostMapping("/query")
    public ResponseEntity<Result<SessionListResponse>> getUserSessions(
            @Valid @RequestBody QuerySessionsRequest request) {
        log.info("接收到查询用户会话列表请求");

        // 从Token中提取用户ID
        String token = extractToken(request.getAuthorization());
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String currentSessionId = jwtTokenProvider.getSessionIdFromToken(token);

        List<SessionDTO> sessions = sessionApplicationService.getUserSessions(userId);

        // 标记当前会话
        sessions.forEach(session ->
                session.setCurrentSession(session.getSessionId().equals(currentSessionId)));

        log.info("查询用户会话列表成功: userId={}, sessionCount={}", userId, sessions.size());
        return ResponseEntity.ok(Result.success(SessionListResponse.of(sessions)));
    }

    /**
     * 终止指定会话
     *
     * <p>终止指定的会话，只能终止自己的会话。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/sessions/terminate
     * Content-Type: application/json
     *
     * {
     *   "sessionId": "550e8400-e29b-41d4-a716-446655440000",
     *   "authorization": "Bearer eyJhbGciOiJIUzUxMiJ9..."
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "会话终止成功",
     *   "data": null
     * }
     * </pre>
     *
     * <p>错误响应示例（403 Forbidden）：</p>
     * <pre>
     * {
     *   "code": 403001,
     *   "message": "无权限终止该会话",
     *   "data": null
     * }
     * </pre>
     *
     * @param request 终止会话请求，包含 sessionId 和 authorization
     * @return 成功响应
     */
    @Operation(
            summary = "终止指定会话",
            description = "终止指定的会话，只能终止自己的会话。会话ID通过请求体传递"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "终止成功"),
            @ApiResponse(responseCode = "401", description = "Token无效"),
            @ApiResponse(responseCode = "403", description = "无权限终止该会话")
    })
    @PostMapping("/terminate")
    public ResponseEntity<Result<Void>> terminateSession(
            @Valid @RequestBody TerminateSessionRequest request) {
        log.info("接收到终止会话请求: sessionId={}", request.getSessionId());

        // 从Token中提取用户ID
        String token = extractToken(request.getAuthorization());
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        sessionApplicationService.terminateSession(request.getSessionId(), userId);

        log.info("会话终止成功: sessionId={}", request.getSessionId());
        return ResponseEntity.ok(Result.success("会话终止成功", null));
    }

    /**
     * 终止其他会话
     *
     * <p>终止当前用户除当前会话外的所有会话。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/sessions/terminate-others
     * Content-Type: application/json
     *
     * {
     *   "authorization": "Bearer eyJhbGciOiJIUzUxMiJ9..."
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "terminatedCount": 2,
     *     "message": "已终止2个其他会话"
     *   }
     * }
     * </pre>
     *
     * @param request 终止其他会话请求，包含 authorization
     * @return 终止结果响应
     */
    @Operation(
            summary = "终止其他会话",
            description = "终止当前用户除当前会话外的所有会话"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "Token无效")
    })
    @PostMapping("/terminate-others")
    public ResponseEntity<Result<TerminateOthersResponse>> terminateOtherSessions(
            @Valid @RequestBody TerminateOthersRequest request) {
        log.info("接收到终止其他会话请求");

        // 从Token中提取用户ID和当前会话ID
        String token = extractToken(request.getAuthorization());
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String currentSessionId = jwtTokenProvider.getSessionIdFromToken(token);

        int terminatedCount = sessionApplicationService.terminateOtherSessions(currentSessionId, userId);

        log.info("终止其他会话成功: userId={}, terminatedCount={}", userId, terminatedCount);
        return ResponseEntity.ok(Result.success(TerminateOthersResponse.of(terminatedCount)));
    }

    /**
     * 从Authorization头中提取Token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
