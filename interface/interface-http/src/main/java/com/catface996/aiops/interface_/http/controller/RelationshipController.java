package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.relationship.*;
import com.catface996.aiops.application.api.dto.relationship.request.CreateRelationshipRequest;
import com.catface996.aiops.application.api.dto.relationship.request.UpdateRelationshipRequest;
import com.catface996.aiops.application.api.service.relationship.RelationshipApplicationService;
import com.catface996.aiops.interface_.http.request.relationship.CycleDetectionRequest;
import com.catface996.aiops.interface_.http.request.relationship.DeleteRelationshipRequest;
import com.catface996.aiops.interface_.http.request.relationship.GetRelationshipRequest;
import com.catface996.aiops.interface_.http.request.relationship.QueryRelationshipsRequest;
import com.catface996.aiops.interface_.http.request.relationship.QueryResourceRelationshipsRequest;
import com.catface996.aiops.interface_.http.request.relationship.TraverseRelationshipsRequest;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
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
 * 资源关系管理控制器（POST-Only API）
 *
 * <p>提供资源关系管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
@Tag(name = "资源关系管理", description = "资源关系管理接口：创建、查询、更新、删除、图遍历（POST-Only API）")
public class RelationshipController {

    private final RelationshipApplicationService relationshipApplicationService;

    /**
     * 创建关系
     */
    @PostMapping("/create")
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
    @PostMapping("/query")
    @Operation(summary = "查询关系列表", description = "分页查询关系列表，支持多条件筛选")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<PageResult<RelationshipDTO>>> queryRelationships(
            @Valid @RequestBody QueryRelationshipsRequest request) {
        PageResult<RelationshipDTO> result = relationshipApplicationService.listRelationships(
                request.getSourceResourceId(),
                request.getTargetResourceId(),
                request.getRelationshipType(),
                request.getStatus(),
                request.getPage(),
                request.getSize());
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取资源的所有关系
     */
    @PostMapping("/resource/query")
    @Operation(summary = "获取资源的所有关系", description = "查询指定资源的上游和下游依赖关系")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<ResourceRelationshipsDTO>> queryResourceRelationships(
            @Valid @RequestBody QueryResourceRelationshipsRequest request) {
        ResourceRelationshipsDTO result = relationshipApplicationService.getResourceRelationships(request.getResourceId());
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取关系详情
     */
    @PostMapping("/get")
    @Operation(summary = "获取关系详情", description = "根据ID查询关系详情")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<RelationshipDTO>> getRelationship(
            @Valid @RequestBody GetRelationshipRequest request) {
        RelationshipDTO result = relationshipApplicationService.getRelationshipById(request.getRelationshipId());
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 更新关系
     */
    @PostMapping("/update")
    @Operation(summary = "更新关系", description = "更新关系的属性。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "关系不存在")
    })
    public ResponseEntity<Result<RelationshipDTO>> updateRelationship(
            @Valid @RequestBody UpdateRelationshipRequest request) {
        Long operatorId = getCurrentUserId();
        Long relationshipId = request.getRelationshipId();
        RelationshipDTO result = relationshipApplicationService.updateRelationship(
                relationshipId, request, operatorId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 删除关系
     */
    @PostMapping("/delete")
    @Operation(summary = "删除关系", description = "删除指定的关系。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "关系不存在")
    })
    public ResponseEntity<Result<Void>> deleteRelationship(
            @Valid @RequestBody DeleteRelationshipRequest request) {
        Long operatorId = getCurrentUserId();
        relationshipApplicationService.deleteRelationship(request.getRelationshipId(), operatorId);
        return ResponseEntity.ok(Result.success("关系删除成功", null));
    }

    /**
     * 检测循环依赖
     */
    @PostMapping("/resource/cycle-detection")
    @Operation(summary = "检测循环依赖", description = "检测从指定资源开始是否存在循环依赖")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<CycleDetectionDTO>> detectCycle(
            @Valid @RequestBody CycleDetectionRequest request) {
        CycleDetectionDTO result = relationshipApplicationService.detectCycle(request.getResourceId());
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 图遍历
     */
    @PostMapping("/resource/traverse")
    @Operation(summary = "图遍历", description = "从指定资源开始进行广度优先遍历")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<TraverseDTO>> traverse(
            @Valid @RequestBody TraverseRelationshipsRequest request) {
        TraverseDTO result = relationshipApplicationService.traverse(
                request.getResourceId(), request.getMaxDepth());
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
