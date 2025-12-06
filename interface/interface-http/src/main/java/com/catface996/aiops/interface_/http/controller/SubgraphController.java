package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDetailDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourceDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourcesWithRelationsDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphTopologyDTO;
import com.catface996.aiops.application.api.dto.subgraph.request.*;
import com.catface996.aiops.application.api.service.subgraph.SubgraphApplicationService;
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
 * 子图管理控制器
 *
 * <p>提供子图管理相关的HTTP接口，包括子图CRUD、权限管理、资源管理、拓扑查询。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务20-26: HTTP接口层实现</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/subgraphs")
@RequiredArgsConstructor
@Tag(name = "子图管理", description = "子图管理接口：创建、查询、更新、删除、权限管理、资源管理、拓扑查询")
public class SubgraphController {

    private final SubgraphApplicationService subgraphApplicationService;

    // ==================== 任务21: 创建子图接口 ====================

    /**
     * 创建子图
     */
    @PostMapping
    @Operation(summary = "创建子图", description = "创建新的资源子图，用于资源分组和权限隔离")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = SubgraphDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "子图名称冲突")
    })
    public ResponseEntity<Result<SubgraphDTO>> createSubgraph(
            @Valid @RequestBody CreateSubgraphRequest request) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        SubgraphDTO result = subgraphApplicationService.createSubgraph(request, operatorId, operatorName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(result));
    }

    // ==================== 任务22: 查询子图接口 ====================

    /**
     * 查询子图列表
     */
    @GetMapping
    @Operation(summary = "查询子图列表", description = "分页查询子图列表，支持关键词搜索、标签过滤、按创建者过滤")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<PageResult<SubgraphDTO>>> listSubgraphs(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "按标签过滤") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "按创建者ID过滤") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size) {

        ListSubgraphsRequest request = ListSubgraphsRequest.builder()
                .keyword(keyword)
                .tags(tags)
                .ownerId(ownerId)
                .page(page)
                .size(size)
                .build();

        Long userId = getCurrentUserId();
        PageResult<SubgraphDTO> result = subgraphApplicationService.listSubgraphs(request, userId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取子图详情
     */
    @GetMapping("/{subgraphId}")
    @Operation(summary = "获取子图详情", description = "获取子图的详细信息，包括权限列表和资源列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<SubgraphDetailDTO>> getSubgraphDetail(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId) {
        Long userId = getCurrentUserId();
        SubgraphDetailDTO result = subgraphApplicationService.getSubgraphDetail(subgraphId, userId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 任务23: 更新子图接口 ====================

    /**
     * 更新子图
     */
    @PutMapping("/{subgraphId}")
    @Operation(summary = "更新子图", description = "更新子图的基本信息，需要OWNER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<SubgraphDTO>> updateSubgraph(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Valid @RequestBody UpdateSubgraphRequest request) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        SubgraphDTO result = subgraphApplicationService.updateSubgraph(
                subgraphId, request, operatorId, operatorName);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 添加权限
     */
    @PostMapping("/{subgraphId}/permissions")
    @Operation(summary = "添加权限", description = "为用户添加子图访问权限，需要OWNER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<Void>> addPermission(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        subgraphApplicationService.addPermission(subgraphId, request, operatorId, operatorName);
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 移除权限
     */
    @DeleteMapping("/{subgraphId}/permissions/{userId}")
    @Operation(summary = "移除权限", description = "移除用户的子图访问权限，需要OWNER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "移除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Void> removePermission(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Parameter(description = "被移除权限的用户ID") @PathVariable Long userId) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        subgraphApplicationService.removePermission(subgraphId, userId, operatorId, operatorName);
        return ResponseEntity.noContent().build();
    }

    // ==================== 任务24: 删除子图接口 ====================

    /**
     * 删除子图
     */
    @DeleteMapping("/{subgraphId}")
    @Operation(summary = "删除子图", description = "删除子图，需要OWNER权限且子图必须为空")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在"),
            @ApiResponse(responseCode = "409", description = "子图非空，无法删除")
    })
    public ResponseEntity<Void> deleteSubgraph(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        subgraphApplicationService.deleteSubgraph(subgraphId, operatorId, operatorName);
        return ResponseEntity.noContent().build();
    }

    // ==================== 任务25: 资源管理接口 ====================

    /**
     * 添加资源到子图
     */
    @PostMapping("/{subgraphId}/resources")
    @Operation(summary = "添加资源", description = "向子图添加资源节点，需要OWNER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<Void>> addResources(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Valid @RequestBody AddResourcesRequest request) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        subgraphApplicationService.addResources(subgraphId, request, operatorId, operatorName);
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 从子图移除资源
     */
    @DeleteMapping("/{subgraphId}/resources")
    @Operation(summary = "移除资源", description = "从子图移除资源节点，需要OWNER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "移除成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<Void>> removeResources(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Valid @RequestBody RemoveResourcesRequest request) {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUserName();
        subgraphApplicationService.removeResources(subgraphId, request, operatorId, operatorName);
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 查询子图资源列表
     */
    @GetMapping("/{subgraphId}/resources")
    @Operation(summary = "查询子图资源列表", description = "分页查询子图包含的资源节点列表，需要OWNER或VIEWER权限")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<PageResult<SubgraphResourceDTO>>> getSubgraphResources(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size) {

        ListSubgraphResourcesRequest request = ListSubgraphResourcesRequest.builder()
                .page(page)
                .size(size)
                .build();

        Long userId = getCurrentUserId();
        PageResult<SubgraphResourceDTO> result = subgraphApplicationService.getSubgraphResources(
                subgraphId, request, userId);
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 任务26: 拓扑查询接口 ====================

    /**
     * 获取子图拓扑
     */
    @GetMapping("/{subgraphId}/topology")
    @Operation(summary = "获取子图拓扑", description = "获取子图内的资源节点和关系边，用于可视化展示")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<SubgraphTopologyDTO>> getSubgraphTopology(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId) {
        Long userId = getCurrentUserId();
        SubgraphTopologyDTO result = subgraphApplicationService.getSubgraphTopology(subgraphId, userId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取子图所有资源及关系（不分页）
     */
    @GetMapping("/{subgraphId}/resources-with-relations")
    @Operation(summary = "获取子图资源及关系", description = "获取子图内所有资源节点的完整信息及节点之间的关系，不分页，用于前端拓扑图展示")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "子图不存在")
    })
    public ResponseEntity<Result<SubgraphResourcesWithRelationsDTO>> getSubgraphResourcesWithRelations(
            @Parameter(description = "子图ID") @PathVariable Long subgraphId) {
        Long userId = getCurrentUserId();
        SubgraphResourcesWithRelationsDTO result = subgraphApplicationService.getSubgraphResourcesWithRelations(
                subgraphId, userId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", auth.getName());
            }
        }
        return null;
    }

    /**
     * 获取当前用户名称
     */
    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return "unknown";
    }
}
