package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.request.AddMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.DeleteTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.GetTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologyGraphRequest;
import com.catface996.aiops.application.api.dto.topology.request.RemoveMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.TopologyGraphDTO;
import com.catface996.aiops.application.api.service.topology.TopologyApplicationService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拓扑图管理控制器（POST-Only API）
 *
 * <p>提供拓扑图管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>拓扑图 CRUD 接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/topologies/create - 创建拓扑图</li>
 *   <li>POST /api/service/v1/topologies/query - 查询拓扑图列表</li>
 *   <li>POST /api/service/v1/topologies/get - 查询拓扑图详情</li>
 *   <li>POST /api/service/v1/topologies/update - 更新拓扑图</li>
 *   <li>POST /api/service/v1/topologies/delete - 删除拓扑图</li>
 * </ul>
 *
 * <p>成员管理接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/topologies/members/add - 添加成员</li>
 *   <li>POST /api/service/v1/topologies/members/remove - 移除成员</li>
 *   <li>POST /api/service/v1/topologies/members/query - 查询成员列表</li>
 *   <li>POST /api/service/v1/topologies/graph/query - 获取拓扑图数据</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持将资源分为拓扑图和资源节点两大类</li>
 *   <li>FR-002: 系统必须提供独立的拓扑图列表查询接口</li>
 *   <li>FR-004: 系统必须提供独立的拓扑图创建接口</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/topologies")
@RequiredArgsConstructor
@Tag(name = "拓扑图管理", description = "拓扑图管理接口：创建、查询、更新、删除、成员管理（POST-Only API）")
public class TopologyController {

    private final TopologyApplicationService topologyApplicationService;

    /**
     * 创建拓扑图
     *
     * <p>创建新的拓扑图资源，自动设置为 SUBGRAPH 类型。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建拓扑图", description = "创建新的拓扑图资源，自动设置为 SUBGRAPH 类型")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = TopologyDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "拓扑图名称已存在")
    })
    public ResponseEntity<Result<TopologyDTO>> createTopology(
            @Valid @RequestBody CreateTopologyRequest request) {

        log.info("创建拓扑图，name: {}, operatorId: {}", request.getName(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        TopologyDTO topology = topologyApplicationService.createTopology(request, operatorId, operatorName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("拓扑图创建成功", topology));
    }

    /**
     * 查询拓扑图列表
     *
     * <p>只返回 SUBGRAPH 类型的资源，不返回其他资源节点。</p>
     */
    @PostMapping("/query")
    @Operation(summary = "查询拓扑图列表", description = "分页查询拓扑图列表，支持按名称和状态筛选。只返回拓扑图类型，不包含资源节点。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<TopologyDTO>>> queryTopologies(
            @Valid @RequestBody QueryTopologiesRequest request) {

        log.info("查询拓扑图列表，operatorId: {}", request.getOperatorId());

        PageResult<TopologyDTO> result = topologyApplicationService.listTopologies(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取拓扑图详情
     *
     * <p>根据ID获取拓扑图详细信息，包含成员数量。</p>
     */
    @PostMapping("/get")
    @Operation(summary = "获取拓扑图详情", description = "根据ID获取拓扑图详细信息，包含成员数量")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在")
    })
    public ResponseEntity<Result<TopologyDTO>> getTopology(
            @Valid @RequestBody GetTopologyRequest request) {

        log.info("获取拓扑图详情，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        TopologyDTO topology = topologyApplicationService.getTopologyById(request.getId());

        if (topology == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404001, "拓扑图不存在"));
        }

        return ResponseEntity.ok(Result.success(topology));
    }

    /**
     * 更新拓扑图
     *
     * <p>更新拓扑图的名称和描述，使用乐观锁防止并发冲突。</p>
     */
    @PostMapping("/update")
    @Operation(summary = "更新拓扑图", description = "更新拓扑图的名称和描述，使用乐观锁防止并发冲突")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<TopologyDTO>> updateTopology(
            @Valid @RequestBody UpdateTopologyRequest request) {

        log.info("更新拓扑图，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        TopologyDTO topology = topologyApplicationService.updateTopology(
                request.getId(), request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("拓扑图更新成功", topology));
    }

    /**
     * 删除拓扑图
     *
     * <p>删除拓扑图及其所有成员关系。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除拓扑图", description = "删除拓扑图及其所有成员关系")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在")
    })
    public ResponseEntity<Result<Void>> deleteTopology(
            @Valid @RequestBody DeleteTopologyRequest request) {

        log.info("删除拓扑图，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        topologyApplicationService.deleteTopology(request.getId(), operatorId, operatorName);

        return ResponseEntity.ok(Result.success("拓扑图删除成功", null));
    }

    // ===== 成员管理接口 =====

    /**
     * 添加成员到拓扑图
     *
     * <p>向拓扑图中添加资源节点成员。</p>
     */
    @PostMapping("/members/add")
    @Operation(summary = "添加成员", description = "向拓扑图中添加资源节点成员")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图或节点不存在")
    })
    public ResponseEntity<Result<Void>> addMembers(
            @Valid @RequestBody AddMembersRequest request) {

        log.info("添加成员到拓扑图，topologyId: {}, nodeIds: {}, operatorId: {}",
                request.getTopologyId(), request.getNodeIds(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        topologyApplicationService.addMembers(
                request.getTopologyId(), request.getNodeIds(), operatorId, operatorName);

        return ResponseEntity.ok(Result.success("成员添加成功", null));
    }

    /**
     * 从拓扑图移除成员
     *
     * <p>从拓扑图中移除资源节点成员。</p>
     */
    @PostMapping("/members/remove")
    @Operation(summary = "移除成员", description = "从拓扑图中移除资源节点成员")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "移除成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在")
    })
    public ResponseEntity<Result<Void>> removeMembers(
            @Valid @RequestBody RemoveMembersRequest request) {

        log.info("从拓扑图移除成员，topologyId: {}, nodeIds: {}, operatorId: {}",
                request.getTopologyId(), request.getNodeIds(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        topologyApplicationService.removeMembers(
                request.getTopologyId(), request.getNodeIds(), operatorId, operatorName);

        return ResponseEntity.ok(Result.success("成员移除成功", null));
    }

    /**
     * 查询拓扑图成员列表
     *
     * <p>分页查询拓扑图中的资源节点成员。</p>
     */
    @PostMapping("/members/query")
    @Operation(summary = "查询成员列表", description = "分页查询拓扑图中的资源节点成员")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在")
    })
    public ResponseEntity<Result<PageResult<NodeDTO>>> queryMembers(
            @Valid @RequestBody QueryMembersRequest request) {

        log.info("查询拓扑图成员，topologyId: {}", request.getTopologyId());

        PageResult<NodeDTO> result = topologyApplicationService.queryMembers(request);

        return ResponseEntity.ok(Result.success(result));
    }

    // ===== 拓扑图数据接口 =====

    /**
     * 获取拓扑图数据
     *
     * <p>获取拓扑图的节点和边数据，用于图形渲染。</p>
     */
    @PostMapping("/graph/query")
    @Operation(summary = "获取拓扑图数据", description = "获取拓扑图的节点和边数据，用于图形渲染")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = TopologyGraphDTO.class))),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "拓扑图不存在")
    })
    public ResponseEntity<Result<TopologyGraphDTO>> getTopologyGraph(
            @Valid @RequestBody QueryTopologyGraphRequest request) {

        log.info("获取拓扑图数据，topologyId: {}, depth: {}", request.getTopologyId(), request.getDepth());

        TopologyGraphDTO result = topologyApplicationService.getTopologyGraph(request);

        return ResponseEntity.ok(Result.success(result));
    }
}
