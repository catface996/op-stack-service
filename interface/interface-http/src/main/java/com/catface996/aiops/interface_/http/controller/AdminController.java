package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.admin.AccountDTO;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.interface_.http.request.admin.QueryAccountsRequest;
import com.catface996.aiops.interface_.http.request.admin.UnlockAccountRequest;
import com.catface996.aiops.interface_.http.response.Result;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员功能控制器（POST-Only API）
 *
 * <p>提供管理员专用的 HTTP 接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/admin/accounts/unlock - 手动解锁账号</li>
 *   <li>POST /api/v1/admin/accounts/query - 查询用户列表</li>
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
 * <p>权限要求：</p>
 * <ul>
 *   <li>所有管理员接口都需要 ROLE_ADMIN 角色</li>
 *   <li>使用 @PreAuthorize 注解进行权限验证</li>
 *   <li>非管理员访问返回 403 Forbidden</li>
 * </ul>
 *
 * <p>HTTP 状态码规范：</p>
 * <ul>
 *   <li>200 OK - 操作成功</li>
 *   <li>400 Bad Request - 请求参数错误</li>
 *   <li>401 Unauthorized - 未认证或Token无效</li>
 *   <li>403 Forbidden - 权限不足（非管理员）</li>
 *   <li>404 Not Found - 账号不存在</li>
 *   <li>500 Internal Server Error - 服务器内部错误</li>
 * </ul>
 *
 * <p>审计日志：</p>
 * <ul>
 *   <li>所有管理员操作都会记录到审计日志</li>
 *   <li>包含管理员ID、操作类型、目标账号ID、操作时间</li>
 *   <li>便于安全审计和问题追踪</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-006: 管理员手动解锁</li>
 *   <li>Feature 024: POST-Only API 重构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "管理员功能", description = "管理员专用接口：账号解锁、账号查询、系统配置管理")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AuthApplicationService authApplicationService;

    /**
     * 管理员手动解锁账号
     *
     * <p>管理员手动解除账号锁定状态，包括以下流程：</p>
     * <ol>
     *   <li>验证管理员身份和权限（通过 @PreAuthorize）</li>
     *   <li>查询账号信息</li>
     *   <li>清除登录失败计数（Redis）</li>
     *   <li>如果账号状态为 LOCKED，更新为 ACTIVE</li>
     *   <li>记录审计日志（包含管理员ID和操作时间）</li>
     * </ol>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>用户账号被误锁定，需要紧急恢复访问</li>
     *   <li>用户忘记密码多次尝试后被锁定</li>
     *   <li>测试环境需要快速解锁测试账号</li>
     *   <li>客服接到用户反馈，需要协助解锁</li>
     * </ul>
     *
     * <p>安全机制：</p>
     * <ul>
     *   <li>仅 ROLE_ADMIN 角色可以调用此接口</li>
     *   <li>非管理员访问返回 403 Forbidden</li>
     *   <li>所有解锁操作记录到审计日志</li>
     *   <li>包含管理员ID，便于追溯责任</li>
     * </ul>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/admin/accounts/unlock
     * Content-Type: application/json
     *
     * {
     *   "accountId": 12345,
     *   "authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "账号解锁成功",
     *   "data": null
     * }
     * </pre>
     *
     * <p>错误响应示例（403 Forbidden - 非管理员）：</p>
     * <pre>
     * {
     *   "code": 403001,
     *   "message": "权限不足，需要管理员权限",
     *   "data": null
     * }
     * </pre>
     *
     * <p>错误响应示例（404 Not Found - 账号不存在）：</p>
     * <pre>
     * {
     *   "code": 404001,
     *   "message": "账号不存在",
     *   "data": null
     * }
     * </pre>
     *
     * <p>注意事项：</p>
     * <ul>
     *   <li>解锁操作会清除登录失败计数</li>
     *   <li>如果账号未被锁定，也会返回成功（幂等操作）</li>
     *   <li>解锁后用户可以立即尝试登录</li>
     *   <li>不会影响用户的密码和其他信息</li>
     * </ul>
     *
     * <p>验收标准：</p>
     * <ul>
     *   <li>AC1: 管理员点击解锁按钮，立即解除账号锁定状态</li>
     *   <li>AC2: 账号被手动解锁，失败登录尝试计数器重置为零</li>
     *   <li>AC3: 账号被手动解锁，记录解锁操作到审计日志</li>
     *   <li>AC4: 管理员尝试解锁未锁定的账号，显示提示消息</li>
     * </ul>
     *
     * @param request 解锁账号请求，包含 accountId 和 authorization
     * @return 操作结果
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.ForbiddenException 当非管理员尝试解锁时抛出
     * @throws com.catface996.aiops.common.exception.AccountNotFoundException 当账号不存在时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     */
    @PostMapping("/accounts/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> unlockAccount(
            @Valid @RequestBody UnlockAccountRequest request) {
        log.info("接收到管理员解锁账号请求: accountId={}", request.getAccountId());

        authApplicationService.unlockAccount(request.getAuthorization(), request.getAccountId());

        log.info("管理员解锁账号成功: accountId={}", request.getAccountId());
        return ResponseEntity.ok(Result.success("账号解锁成功", null));
    }

    /**
     * 获取用户列表
     *
     * <p>管理员查询用户列表，支持分页。</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/v1/admin/accounts/query
     * Content-Type: application/json
     *
     * {
     *   "page": 1,
     *   "size": 10
     * }
     * </pre>
     *
     * <p>成功响应示例（200 OK）：</p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "操作成功",
     *   "data": {
     *     "content": [
     *       {
     *         "userId": 1,
     *         "username": "admin",
     *         "email": "admin@example.com",
     *         "role": "ROLE_ADMIN",
     *         "status": "ACTIVE",
     *         "createdAt": "2025-01-01T00:00:00",
     *         "isLocked": false
     *       }
     *     ],
     *     "page": 1,
     *     "size": 10,
     *     "totalElements": 100,
     *     "totalPages": 10,
     *     "first": true,
     *     "last": false
     *   }
     * }
     * </pre>
     *
     * @param request 查询账号请求，包含分页参数
     * @return 分页的用户列表
     */
    @Operation(
            summary = "获取用户列表",
            description = "管理员查询用户列表，支持分页。页码从1开始。"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "Token无效"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/accounts/query")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResult<AccountDTO>>> getAccounts(
            @Valid @RequestBody QueryAccountsRequest request) {
        log.info("接收到获取用户列表请求: page={}, size={}", request.getPage(), request.getSize());

        // 边界处理：页码小于1时使用1，size超过100时限制为100
        int validPage = Math.max(1, request.getPage());
        int validSize = Math.min(Math.max(1, request.getSize()), 100);

        PageResult<AccountDTO> result = authApplicationService.getAccounts(validPage, validSize);

        log.info("获取用户列表成功: total={}, returned={}", result.getTotalElements(), result.getContent().size());
        return ResponseEntity.ok(Result.success(result));
    }
}
