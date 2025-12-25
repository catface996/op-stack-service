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
import com.catface996.aiops.interface_.http.request.resource.GetResourceRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryAncestorsRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryAuditLogsRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryMembersRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryMembersWithRelationsRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryResourceTypesRequest;
import com.catface996.aiops.interface_.http.request.resource.QueryTopologyRequest;
import com.catface996.aiops.interface_.http.request.subgraph.AddMembersRequest;
import com.catface996.aiops.interface_.http.request.subgraph.RemoveMembersRequest;
import com.catface996.aiops.interface_.http.response.Result;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphAncestorsResponse;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphMemberListResponse;
import com.catface996.aiops.interface_.http.response.subgraph.SubgraphMembersWithRelationsResponse;
import com.catface996.aiops.interface_.http.response.subgraph.TopologyGraphResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 资源管理控制器（POST-Only API）
 *
 * <p>提供IT资源管理相关的HTTP接口，所有业务接口统一使用 POST 方法，便于网关参数注入。</p>
 *
 * <p>资源 CRUD 接口：</p>
 * <ul>
 *   <li>POST /api/v1/resources/create - 创建资源</li>
 *   <li>POST /api/v1/resources/query - 查询资源列表</li>
 *   <li>POST /api/v1/resources/get - 查询资源详情</li>
 *   <li>POST /api/v1/resources/update - 更新资源</li>
 *   <li>POST /api/v1/resources/delete - 删除资源</li>
 *   <li>POST /api/v1/resources/update-status - 更新资源状态</li>
 *   <li>POST /api/v1/resources/audit-logs/query - 查询审计日志</li>
 *   <li>POST /api/v1/resource-types/query - 查询资源类型列表</li>
 * </ul>
 *
 * <p>成员管理接口（仅适用于 SUBGRAPH 类型资源）：</p>
 * <ul>
 *   <li>POST /api/v1/resources/members/add - 添加成员</li>
 *   <li>POST /api/v1/resources/members/remove - 移除成员</li>
 *   <li>POST /api/v1/resources/members/query - 查询成员列表</li>
 *   <li>POST /api/v1/resources/members-with-relations/query - 获取成员及关系</li>
 *   <li>POST /api/v1/resources/topology/query - 获取拓扑图数据</li>
 *   <li>POST /api/v1/resources/ancestors/query - 获取祖先链</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 *   <li>F08: 子图管理功能 v2.0（成员管理）</li>
 *   <li>F024: POST-Only API 重构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "资源管理", description = "IT资源管理接口：创建、查询、更新、删除、状态管理、审计日志、成员管理（POST-Only API）")
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;
    private final SubgraphMemberApplicationService memberApplicationService;

    /**
     * 创建资源
     */
    @PostMapping("/resources/create")
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
        log.info("创建资源请求，name: {}, operatorId: {}", request.getName(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        ResourceDTO resource = resourceApplicationService.createResource(request, operatorId, operatorName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("资源创建成功", resource));
    }

    /**
     * 查询资源节点列表（自动排除拓扑图类型）
     *
     * <p>此接口返回资源节点（非 SUBGRAPH 类型），拓扑图请使用 /api/v1/topologies/query 接口查询。</p>
     */
    @PostMapping("/resources/query")
    @Operation(summary = "查询资源节点列表", description = "分页查询资源节点列表，支持按类型、状态、关键词过滤。自动排除拓扑图类型（SUBGRAPH），拓扑图请使用 /api/v1/topologies/query 接口")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<ResourceDTO>>> queryResources(
            @Valid @RequestBody ListResourcesRequest request) {

        PageResult<ResourceDTO> result = resourceApplicationService.listResources(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源详情
     */
    @PostMapping("/resources/get")
    @Operation(summary = "查询资源详情", description = "根据ID查询资源详细信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<ResourceDTO>> getResource(
            @Valid @RequestBody GetResourceRequest request) {

        ResourceDTO resource = resourceApplicationService.getResourceById(request.getId());

        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404001, "资源不存在"));
        }

        return ResponseEntity.ok(Result.success(resource));
    }

    /**
     * 更新资源
     */
    @PostMapping("/resources/update")
    @Operation(summary = "更新资源", description = "更新资源信息，需要Owner或Admin权限。ID通过请求体传递")
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
            @Valid @RequestBody UpdateResourceRequest request) {
        Long id = request.getId();
        log.info("更新资源请求，resourceId: {}, operatorId: {}", id, request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        // 权限检查
        if (!resourceApplicationService.checkPermission(id, operatorId, false)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        ResourceDTO resource = resourceApplicationService.updateResource(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源更新成功", resource));
    }

    /**
     * 删除资源
     */
    @PostMapping("/resources/delete")
    @Operation(summary = "删除资源", description = "删除资源，需要输入资源名称确认。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "资源名称确认不匹配"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Void>> deleteResource(
            @Valid @RequestBody DeleteResourceRequest request) {
        Long id = request.getId();
        log.info("删除资源请求，resourceId: {}, operatorId: {}", id, request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        // 权限检查
        if (!resourceApplicationService.checkPermission(id, operatorId, false)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403001, "无权限操作此资源"));
        }

        resourceApplicationService.deleteResource(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源删除成功", null));
    }

    /**
     * 更新资源状态
     */
    @PostMapping("/resources/update-status")
    @Operation(summary = "更新资源状态", description = "更新资源状态（RUNNING/STOPPED/MAINTENANCE/OFFLINE）。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "无效的状态值"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<ResourceDTO>> updateResourceStatus(
            @Valid @RequestBody UpdateResourceStatusRequest request) {
        Long id = request.getId();
        log.info("更新资源状态请求，resourceId: {}, newStatus: {}, operatorId: {}", id, request.getStatus(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        ResourceDTO resource = resourceApplicationService.updateResourceStatus(id, request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("资源状态更新成功", resource));
    }

    /**
     * 查询资源审计日志
     */
    @PostMapping("/resources/audit-logs/query")
    @Operation(summary = "查询审计日志", description = "查询资源的操作审计日志")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<PageResult<ResourceAuditLogDTO>>> queryAuditLogs(
            @Valid @RequestBody QueryAuditLogsRequest request) {

        PageResult<ResourceAuditLogDTO> result = resourceApplicationService.getResourceAuditLogs(
                request.getResourceId(), request.getPage(), request.getSize());

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询资源类型列表
     */
    @PostMapping("/resource-types/query")
    @Operation(summary = "查询资源类型列表", description = "获取所有可用的资源类型")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<ResourceTypeDTO>>> queryResourceTypes(
            @RequestBody(required = false) QueryResourceTypesRequest request) {
        List<ResourceTypeDTO> types = resourceApplicationService.getAllResourceTypes();

        return ResponseEntity.ok(Result.success(types));
    }

    // ==================== 成员管理接口（已废弃，请使用 /api/v1/topologies/* 接口） ====================

    /**
     * 添加成员到资源（仅适用于 SUBGRAPH 类型）
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/members/add 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/members/add")
    @Operation(summary = "[已废弃] 添加成员", description = "已废弃，请使用 /api/v1/topologies/members/add。添加资源作为成员。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员添加成功"),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理、成员已存在、或检测到循环引用"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限操作"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Map<String, Object>>> addMembers(
            @Valid @RequestBody AddMembersRequest request) {

        Long id = request.getResourceId();
        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

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
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/members/remove 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/members/remove")
    @Operation(summary = "[已废弃] 移除成员", description = "已废弃，请使用 /api/v1/topologies/members/remove。从资源中移除成员。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员移除成功"),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限操作"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<Void>> removeMembers(
            @Valid @RequestBody RemoveMembersRequest request) {

        Long id = request.getResourceId();
        Long operatorId = request.getOperatorId();

        memberApplicationService.removeMembers(id, request.getMemberIds(), operatorId);

        return ResponseEntity.ok(Result.success("成员移除成功", null));
    }

    /**
     * 查询资源成员列表（仅适用于 SUBGRAPH 类型）
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/members/query 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/members/query")
    @Operation(summary = "[已废弃] 查询成员列表", description = "已废弃，请使用 /api/v1/topologies/members/query。获取资源的成员列表，支持分页。仅适用于 SUBGRAPH 类型资源。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员列表获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphMemberListResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphMemberListResponse>> queryMembers(
            @Valid @RequestBody QueryMembersRequest request) {

        Long id = request.getResourceId();
        Integer page = request.getPage();
        Integer size = request.getSize();

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
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/members-with-relations/query 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/members-with-relations/query")
    @Operation(summary = "[已废弃] 获取成员及关系", description = "已废弃，请使用 /api/v1/topologies/members-with-relations/query。获取成员列表及成员之间的关系，支持嵌套展开。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成员及关系获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphMembersWithRelationsResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持成员管理"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphMembersWithRelationsResponse>> queryMembersWithRelations(
            @Valid @RequestBody QueryMembersWithRelationsRequest request) {

        TopologyQueryCommand command = new TopologyQueryCommand(
                request.getResourceId(),
                request.getExpandNested(),
                request.getMaxDepth()
        );
        SubgraphMembersWithRelationsDTO dto = memberApplicationService.getMembersWithRelations(command);
        SubgraphMembersWithRelationsResponse response = SubgraphMembersWithRelationsResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取资源拓扑图数据（仅适用于 SUBGRAPH 类型）
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/graph/query 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/topology/query")
    @Operation(summary = "[已废弃] 获取拓扑图数据", description = "已废弃，请使用 /api/v1/topologies/graph/query。获取用于图形渲染的拓扑数据，包含节点、边和子图边界。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "拓扑图数据获取成功",
                    content = @Content(schema = @Schema(implementation = TopologyGraphResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持拓扑查询"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<TopologyGraphResponse>> queryTopology(
            @Valid @RequestBody QueryTopologyRequest request) {

        TopologyQueryCommand command = new TopologyQueryCommand(
                request.getResourceId(),
                request.getExpandNested()
        );
        TopologyGraphDTO dto = memberApplicationService.getSubgraphTopology(command);
        TopologyGraphResponse response = TopologyGraphResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取资源祖先链（仅适用于 SUBGRAPH 类型）
     *
     * @deprecated 此接口已废弃，请使用 /api/v1/topologies/ancestors/query 接口
     */
    @Deprecated(since = "2025-12-25", forRemoval = true)
    @PostMapping("/resources/ancestors/query")
    @Operation(summary = "[已废弃] 获取祖先链", description = "已废弃，请使用 /api/v1/topologies/ancestors/query。获取资源的祖先链，用于导航和面包屑显示。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "祖先链获取成功",
                    content = @Content(schema = @Schema(implementation = SubgraphAncestorsResponse.class))),
            @ApiResponse(responseCode = "400", description = "资源类型不支持祖先查询"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "资源不存在")
    })
    public ResponseEntity<Result<SubgraphAncestorsResponse>> queryAncestors(
            @Valid @RequestBody QueryAncestorsRequest request) {

        SubgraphAncestorsDTO dto = memberApplicationService.getAncestors(request.getResourceId());
        SubgraphAncestorsResponse response = SubgraphAncestorsResponse.from(dto);

        return ResponseEntity.ok(Result.success(response));
    }
}
