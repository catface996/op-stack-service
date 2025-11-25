package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.interface_.http.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员功能控制器
 *
 * <p>提供管理员专用的 HTTP 接口，包括账号管理和系统管理功能。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/admin/accounts/{accountId}/unlock - 手动解锁账号</li>
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
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
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
     * POST /api/v1/admin/accounts/12345/unlock
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
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
     * @param authorization JWT Token（包含 Bearer 前缀），通过请求头传递
     * @param accountId 待解锁的账号ID
     * @return 操作结果
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.ForbiddenException 当非管理员尝试解锁时抛出
     * @throws com.catface996.aiops.common.exception.AccountNotFoundException 当账号不存在时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     */
    @PostMapping("/accounts/{accountId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long accountId) {
        log.info("接收到管理员解锁账号请求: accountId={}", accountId);

        authApplicationService.unlockAccount(authorization, accountId);

        log.info("管理员解锁账号成功: accountId={}", accountId);
        return ResponseEntity.ok(ApiResponse.success("账号解锁成功", null));
    }
}
