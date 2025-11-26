package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.auth.LoginResult;
import com.catface996.aiops.application.api.dto.auth.RegisterResult;
import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;
import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.interface_.http.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * <p>提供用户认证相关的 HTTP 接口，包括注册、登录和登出功能。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/auth/register - 用户注册</li>
 *   <li>POST /api/v1/auth/login - 用户登录</li>
 *   <li>POST /api/v1/auth/logout - 用户登出</li>
 * </ul>
 *
 * <p>统一响应格式：</p>
 * <pre>
 * {
 *   "code": 0,           // 0表示成功，其他表示错误码
 *   "message": "操作成功", // 响应消息
 *   "data": {}           // 响应数据（可选）
 * }
 * </pre>
 *
 * <p>认证说明：</p>
 * <ul>
 *   <li>注册和登录接口为公开接口，无需认证</li>
 *   <li>登出接口需要携带 JWT Token（Header: Authorization: Bearer {token}）</li>
 *   <li>登录成功后返回 JWT Token，客户端需保存并在后续请求中携带</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口：注册、登录、登出")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    /**
     * 用户注册
     *
     * <p>创建新的用户账号，注册成功后用户需要使用用户名/邮箱和密码登录。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/auth/register
     * Content-Type: application/json
     *
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "SecureP@ss123"
     * }
     * </pre>
     *
     * <p>成功响应（201 Created）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "accountId": 12345,
     *     "username": "john_doe",
     *     "email": "john@example.com",
     *     "role": "ROLE_USER",
     *     "createdAt": "2025-11-25T10:30:00",
     *     "message": "注册成功，请使用用户名或邮箱登录"
     *   }
     * }
     * </pre>
     *
     * <p>错误响应：</p>
     * <ul>
     *   <li>400 Bad Request - 请求参数无效（用户名格式错误、密码强度不足等）</li>
     *   <li>409 Conflict - 用户名或邮箱已存在</li>
     *   <li>500 Internal Server Error - 服务器内部错误</li>
     * </ul>
     *
     * @param request 注册请求，包含用户名、邮箱和密码
     * @return 注册结果，包含账号ID和基本信息
     */
    @Operation(
            summary = "用户注册",
            description = "创建新的用户账号，注册成功后需要使用用户名/邮箱和密码登录"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "注册成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "用户名或邮箱已存在")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResult>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("接收到用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());

        RegisterResult result = authApplicationService.register(request);

        log.info("用户注册成功: accountId={}, username={}", result.getAccountId(), result.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result));
    }

    /**
     * 用户登录
     *
     * <p>验证用户凭据并创建会话，支持使用用户名或邮箱登录。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/auth/login
     * Content-Type: application/json
     *
     * {
     *   "identifier": "john_doe",        // 用户名或邮箱
     *   "password": "SecureP@ss123",
     *   "rememberMe": false              // 是否记住我（false=2小时，true=30天）
     * }
     * </pre>
     *
     * <p>成功响应（200 OK）：</p>
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
     *     "sessionId": "550e8400-e29b-41d4-a716-446655440000",
     *     "expiresAt": "2025-11-25T12:30:00",
     *     "deviceInfo": "Chrome 120.0 on Windows 11",
     *     "message": "登录成功"
     *   }
     * }
     * </pre>
     *
     * <p>错误响应：</p>
     * <ul>
     *   <li>400 Bad Request - 请求参数无效</li>
     *   <li>401 Unauthorized - 用户名或密码错误</li>
     *   <li>423 Locked - 账号已被锁定（连续5次失败锁定30分钟）</li>
     *   <li>500 Internal Server Error - 服务器内部错误</li>
     * </ul>
     *
     * <p>重要说明：</p>
     * <ul>
     *   <li>登录成功后返回的 JWT Token 需要保存到客户端（LocalStorage或Cookie）</li>
     *   <li>后续请求需要在 HTTP Header 中携带 Token：Authorization: Bearer {token}</li>
     *   <li>连续5次登录失败会导致账号被锁定30分钟</li>
     *   <li>新设备登录会使旧设备的会话失效（会话互斥）</li>
     * </ul>
     *
     * @param request 登录请求，包含标识符（用户名或邮箱）、密码和是否记住我
     * @return 登录结果，包含 JWT Token、用户信息和会话信息
     */
    @Operation(
            summary = "用户登录",
            description = "验证用户凭据并创建会话，支持使用用户名或邮箱登录。登录成功返回JWT Token，客户端需保存并在后续请求中携带"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "账号已被锁定")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request) {
        log.info("接收到用户登录请求: identifier={}, rememberMe={}", request.getIdentifier(), request.getRememberMe());

        LoginResult result = authApplicationService.login(request);

        log.info("用户登录成功: accountId={}, username={}, sessionId={}",
                result.getUserInfo().getAccountId(),
                result.getUserInfo().getUsername(),
                result.getSessionId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 用户登出
     *
     * <p>使当前会话失效，登出后需要重新登录才能访问受保护的资源。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/auth/logout
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     *
     * <p>成功响应（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "登出成功",
     *   "data": null
     * }
     * </pre>
     *
     * <p>错误响应：</p>
     * <ul>
     *   <li>401 Unauthorized - Token 无效或已过期</li>
     *   <li>404 Not Found - 会话不存在（可能已经登出）</li>
     *   <li>500 Internal Server Error - 服务器内部错误</li>
     * </ul>
     *
     * <p>重要说明：</p>
     * <ul>
     *   <li>登出后 Token 会立即失效，无法再使用该 Token 访问受保护资源</li>
     *   <li>如果会话不存在，也会返回成功（幂等性）</li>
     *   <li>客户端应在登出成功后清除本地保存的 Token</li>
     * </ul>
     *
     * @param authorization JWT Token（Header: Authorization: Bearer {token}）
     * @return 成功响应
     */
    @Operation(
            summary = "用户登出",
            description = "使当前会话失效，登出后需要重新登录才能访问受保护的资源"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token无效或已过期")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzUxMiJ9...")
            @RequestHeader("Authorization") String authorization) {
        log.info("接收到用户登出请求");

        authApplicationService.logout(authorization);

        log.info("用户登出成功");
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }
}
