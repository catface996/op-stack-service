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
import com.catface996.aiops.application.dto.subgraph.AddMembersCommand;
import com.catface996.aiops.application.dto.subgraph.SubgraphAncestorsDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMemberDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMembersWithRelationsDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyGraphDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyQueryCommand;
import com.catface996.aiops.application.service.subgraph.SubgraphMemberApplicationService;
import com.catface996.aiops.interface_.http.request.subgraph.AddMembersRequest;
import com.catface996.aiops.interface_.http.request.subgraph.RemoveMembersRequest;
import com.catface996.aiops.interface_.http.response.Result;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphAncestorsResponse;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphMemberListResponse;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphMembersWithRelationsResponse;
import com.catface996.aiops.interface_.http.response.subgraph.TopologyGraphResponse;
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
import java.util.Map;

/**
 * 资源管理控制器
 *
 * <p>提供IT资源管理相关的HTTP接口，包括资源 CRUD 和成员管理。</p>
 *
 * <p>资源 CRUD 接口：</p>
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
 * <p>成员管理接口（仅适用于 SUBGRAPH 类型资源）：</p>
 * <ul>
 *   <li>POST /api/v1/resources/{id}/members - 添加成员</li>
 *   <li>DELETE /api/v1/resources/{id}/members - 移除成员</li>
 *   <li>GET /api/v1/resources/{id}/members - 查询成员列表</li>
 *   <li>GET /api/v1/resources/{id}/members-with-relations - 获取成员及关系</li>
 *   <li>GET /api/v1/resources/{id}/topology - 获取拓扑图数据</li>
 *   <li>GET /api/v1/resources/{id}/ancestors - 获取祖先链</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 *   <li>F08: 子图管理功能 v2.0（成员管理）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "资源管理", description = "IT资源管理接口：创建、查询、更新、删除、状态管理、审计日志、成员管理")
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;
    private final SubgraphMemberApplicationService memberApplicationService;

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

    // ==================== 成员管理接口（仅适用于 SUBGRAPH 类型） ====================

    /**
     * 添加成员到资源（仅适用于 SUBGRAPH 类型）
     */
    @PostMapping("/resources/{id}/members")
    @Operation(summary = "添加成员", description = "添加资源作为成员。仅适用于 SUBGRAPH 类型资源。添加子图时会执行循环检测。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员添加成功"),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理、成员已存在、或检测到循环引用"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限操作"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Map<String, Object>>> addMembers(
            @Parameter(description = "资源 ID") @PathVariable Long id,
            @Valid @RequestBody AddMembersRequest request) {

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        AddMembersCommand command = new AddMembersCommand(id, request.getMemberIds(), operatorId);
        command.setOperatorName(operatorName);

        int addedCount = memberApplicationService.addMembers(command);

        Map<String, Object> response = Map.of(
                "success", true,
                "message", String.format("成功添加 %d 个成员", addedCount),
                "addedCount", addedCount
        );

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 从资源移除成员（仅适用于 SUBGRAPH 类型）
     */
    @DeleteMapping("/resources/{id}/members")
    @Operation(summary = "移除成员", description = "从资源中移除成员。仅适用于 SUBGRAPH 类型资源。成员资源本身不被删除。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "成员移除成功"),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限操作"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Void> removeMembers(
            @Parameter(description = "资源 ID") @PathVariable Long id,
            @Valid @RequestBody RemoveMembersRequest request) {

        Long operatorId = getCurrentUserId();

        memberApplicationService.removeMembers(id, request.getMemberIds(), operatorId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 查询资源成员列表（仅适用于 SUBGRAPH 类型）
     */
    @GetMapping("/resources/{id}/members")
    @Operation(summary = "查询成员列表", description = "获取资源的成员列表，支持分页。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员列表获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphMemberListResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphMemberListResponse>> listMembers(
            @Parameter(description = "资源 ID") @PathVariable Long id,
            @Parameter(description = "页码（从 1 开始）") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        if (size > 100) {
            size = 100;
        }

        List<SubgraphMemberDTO> members = memberApplicationService.listMembers(id, page, size);
        int totalCount = memberApplicationService.countMembers(id);

        SubgraphMemberListResponse response = SubgraphMemberListResponse.of(members, page, size, totalCount);

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取资源成员及其关系（仅适用于 SUBGRAPH 类型）
     */
    @GetMapping("/resources/{id}/members-with-relations")
    @Operation(summary = "获取成员及关系", description = "获取资源的成员列表及成员之间的关系，支持嵌套展开。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员及关系获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphMembersWithRelationsResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphMembersWithRelationsResponse>> getMembersWithRelations(
            @Parameter(description = "资源 ID") @PathVariable Long id,
            @Parameter(description = "是否展开嵌套子图") @RequestParam(defaultValue = "false") Boolean expandNested,
            @Parameter(description = "最大展开深度（1-10）") @RequestParam(defaultValue = "3") Integer maxDepth) {

        TopologyQueryCommand command = new TopologyQueryCommand(id, expandNested, maxDepth);
        SubgraphMembersWithRelationsDTO dto = memberApplicationService.getMembersWithRelations(command);
        SubgraphMembersWithRelationsResponse response = SubgraphMembersWithRelationsResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取资源拓扑图数据（仅适用于 SUBGRAPH 类型）
     */
    @GetMapping("/resources/{id}/topology")
    @Operation(summary = "获取拓扑图数据", description = "获取用于图形渲染的拓扑数据，包含节点、边和子图边界。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "拓扑图数据获取成功",
                    content = @Content(schema = @Schema(implementation = TopologyGraphResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持拓扑查询"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<TopologyGraphResponse>> getResourceTopology(
            @Parameter(description = "资源 ID") @PathVariable Long id,
            @Parameter(description = "是否展开嵌套子图") @RequestParam(defaultValue = "false") Boolean expandNested) {

        TopologyQueryCommand command = new TopologyQueryCommand(id, expandNested);
        TopologyGraphDTO dto = memberApplicationService.getSubgraphTopology(command);
        TopologyGraphResponse response = TopologyGraphResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取资源祖先链（仅适用于 SUBGRAPH 类型）
     */
    @GetMapping("/resources/{id}/ancestors")
    @Operation(summary = "获取祖先链", description = "获取资源的祖先链，用于导航和面包屑显示。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "祖先链获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphAncestorsResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持祖先查询"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphAncestorsResponse>> getResourceAncestors(
            @Parameter(description = "资源 ID") @PathVariable Long id) {

        SubgraphAncestorsDTO dto = memberApplicationService.getAncestors(id);
        SubgraphAncestorsResponse response = SubgraphAncestorsResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
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
