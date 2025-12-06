package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.relationship.*;
import com.catface996.aiops.application.api.dto.relationship.request.CreateRelationshipRequest;
import com.catface996.aiops.application.api.dto.relationship.request.UpdateRelationshipRequest;
import com.catface996.aiops.application.api.service.relationship.RelationshipApplicationService;
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

/**
 * 资源关系管理控制器
 *
 * <p>提供资源关系管理相关的HTTP接口。</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
@Tag(name = "资源关系管理", description = "资源关系管理接口：创建、查询、更新、删除、图遍历")
public class RelationshipController {

    private final RelationshipApplicationService relationshipApplicationService;

    /**
     * 创建关系
     */
    @PostMapping
    @Operation(summary = "创建关系", description = "创建两个资源之间的关系")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = RelationshipDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "409", description = "关系已存在")
    })
    public ResponseEntity<Result<RelationshipDTO>> createRelationship(
            @Valid @RequestBody CreateRelationshipRequest request) {
        Long operatorId = getCurrentUserId();
        RelationshipDTO result = relationshipApplicationService.createRelationship(request, operatorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(result));
    }

    /**
     * 查询关系列表
     */
    @GetMapping
    @Operation(summary = "查询关系列表", description = "分页查询关系列表，支持多条件筛选")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<PageResult<RelationshipDTO>>> listRelationships(
            @Parameter(description = "源资源ID") @RequestParam(required = false) Long sourceResourceId,
            @Parameter(description = "目标资源ID") @RequestParam(required = false) Long targetResourceId,
            @Parameter(description = "关系类型") @RequestParam(required = false) String relationshipType,
            @Parameter(description = "关系状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<RelationshipDTO> result = relationshipApplicationService.listRelationships(
                sourceResourceId, targetResourceId, relationshipType, status, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取资源的所有关系
     */
    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "获取资源的所有关系", description = "查询指定资源的上游和下游依赖关系")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<ResourceRelationshipsDTO>> getResourceRelationships(
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        ResourceRelationshipsDTO result = relationshipApplicationService.getResourceRelationships(resourceId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取关系详情
     */
    @GetMapping("/{relationshipId}")
    @Operation(summary = "获取关系详情", description = "根据ID查询关系详情")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<RelationshipDTO>> getRelationshipById(
            @Parameter(description = "关系ID") @PathVariable Long relationshipId) {
        RelationshipDTO result = relationshipApplicationService.getRelationshipById(relationshipId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 更新关系
     */
    @PutMapping("/{relationshipId}")
    @Operation(summary = "更新关系", description = "更新关系的属性")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "关系不存在")
    })
    public ResponseEntity<Result<RelationshipDTO>> updateRelationship(
            @Parameter(description = "关系ID") @PathVariable Long relationshipId,
            @Valid @RequestBody UpdateRelationshipRequest request) {
        Long operatorId = getCurrentUserId();
        RelationshipDTO result = relationshipApplicationService.updateRelationship(
                relationshipId, request, operatorId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 删除关系
     */
    @DeleteMapping("/{relationshipId}")
    @Operation(summary = "删除关系", description = "删除指定的关系")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "关系不存在")
    })
    public ResponseEntity<Void> deleteRelationship(
            @Parameter(description = "关系ID") @PathVariable Long relationshipId) {
        Long operatorId = getCurrentUserId();
        relationshipApplicationService.deleteRelationship(relationshipId, operatorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 检测循环依赖
     */
    @GetMapping("/resource/{resourceId}/cycle-detection")
    @Operation(summary = "检测循环依赖", description = "检测从指定资源开始是否存在循环依赖")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<CycleDetectionDTO>> detectCycle(
            @Parameter(description = "资源ID") @PathVariable Long resourceId) {
        CycleDetectionDTO result = relationshipApplicationService.detectCycle(resourceId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 图遍历
     */
    @GetMapping("/resource/{resourceId}/traverse")
    @Operation(summary = "图遍历", description = "从指定资源开始进行广度优先遍历")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<TraverseDTO>> traverse(
            @Parameter(description = "资源ID") @PathVariable Long resourceId,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "10") Integer maxDepth) {
        TraverseDTO result = relationshipApplicationService.traverse(resourceId, maxDepth);
        return ResponseEntity.ok(Result.success(result));
    }

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
}
