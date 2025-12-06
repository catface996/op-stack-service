package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.resource.ResourceAuditLogDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceTypeDTO;
import com.catface996.aiops.application.api.dto.resource.request.CreateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.DeleteResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.ListResourcesRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceStatusRequest;
import com.catface996.aiops.application.api.service.resource.ResourceApplicationService;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 *
 * <p>提供IT资源管理相关的HTTP接口。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/resources - 创建资源</li>
 *   <li>GET /api/v1/resources - 查询资源列表</li>
 *   <li>GET /api/v1/resources/{id} - 查询资源详情</li>
 *   <li>PUT /api/v1/resources/{id} - 更新资源</li>
 *   <li>DELETE /api/v1/resources/{id} - 删除资源</li>
 *   <li>PATCH /api/v1/resources/{id}/status - 更新资源状态</li>
 *   <li>GET /api/v1/resources/{id}/audit-logs - 查询审计日志</li>
 *   <li>GET /api/v1/resource-types - 查询资源类型列表</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "资源管理", description = "IT资源管理接口：创建、查询、更新、删除、状态管理、审计日志")
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;

    /**
     * 创建资源
     */
    @PostMapping("/resources")
    @Operation(summary = "创建资源", description = "创建新的IT资源，敏感配置将自动加密存储")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ResourceDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "资源名称已存在")
    })
    public ResponseEntity<Result<ResourceDTO>> createResource(
            @Valid @RequestBody CreateResourceRequest request) {
        log.info("创建资源请求，name: {}", request.getName());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        ResourceDTO resource = resourceApplicationService.createResource(request, operatorId, operatorName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("资源创建成功", resource));
    }

    /**
     * 查询资源列表
     */
    @GetMapping("/resources")
    @Operation(summary = "查询资源列表", description = "分页查询资源列表，支持按类型、状态、关键词过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<ResourceDTO>>> listResources(
            @Parameter(description = "资源类型ID") @RequestParam(required = false) Long resourceTypeId,
            @Parameter(description = "资源状态") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        ListResourcesRequest request = new ListResourcesRequest();
        request.setResourceTypeId(resourceTypeId);
        request.setStatus(status);
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);

        PageResult<ResourceDTO> result = resourceApplicationService.listResources(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源详情
     */
    @GetMapping("/resources/{id}")
    @Operation(summary = "查询资源详情", description = "根据ID查询资源详细信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<ResourceDTO>> getResourceById(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id) {

        ResourceDTO resource = resourceApplicationService.getResourceById(id);

        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404001, "资源不存在"));
        }

        return ResponseEntity.ok(Result.success(resource));
    }

    /**
     * 更新资源
     */
    @PutMapping("/resources/{id}")
    @Operation(summary = "更新资源", description = "更新资源信息，需要Owner或Admin权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "资源不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<ResourceDTO>> updateResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request) {
        log.info("更新资源请求，resourceId: {}", id);

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        // 权限检查
        if (!resourceApplicationService.checkPermission(id, operatorId, isAdmin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        ResourceDTO resource = resourceApplicationService.updateResource(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源更新成功", resource));
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/resources/{id}")
    @Operation(summary = "删除资源", description = "删除资源，需要输入资源名称确认")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "资源名称确认不匹配"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Void>> deleteResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DeleteResourceRequest request) {
        log.info("删除资源请求，resourceId: {}", id);

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        // 权限检查
        if (!resourceApplicationService.checkPermission(id, operatorId, isAdmin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        resourceApplicationService.deleteResource(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源删除成功", null));
    }

    /**
     * 更新资源状态
     */
    @PatchMapping("/resources/{id}/status")
    @Operation(summary = "更新资源状态", description = "更新资源状态（RUNNING/STOPPED/MAINTENANCE/OFFLINE）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "无效的状态值"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<ResourceDTO>> updateResourceStatus(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateResourceStatusRequest request) {
        log.info("更新资源状态请求，resourceId: {}, newStatus: {}", id, request.getStatus());

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        ResourceDTO resource = resourceApplicationService.updateResourceStatus(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源状态更新成功", resource));
    }

    /**
     * 查询资源审计日志
     */
    @GetMapping("/resources/{id}/audit-logs")
    @Operation(summary = "查询审计日志", description = "查询资源的操作审计日志")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<PageResult<ResourceAuditLogDTO>>> getResourceAuditLogs(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        PageResult<ResourceAuditLogDTO> result = resourceApplicationService.getResourceAuditLogs(id, page, size);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源类型列表
     */
    @GetMapping("/resource-types")
    @Operation(summary = "查询资源类型列表", description = "获取所有可用的资源类型")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<ResourceTypeDTO>>> getAllResourceTypes() {
        List<ResourceTypeDTO> types = resourceApplicationService.getAllResourceTypes();

        return ResponseEntity.ok(Result.success(types));
    }

    // ===== 私有辅助方法 =====

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", authentication.getName());
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        }
        return "unknown";
    }

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        }
        return false;
    }
}
