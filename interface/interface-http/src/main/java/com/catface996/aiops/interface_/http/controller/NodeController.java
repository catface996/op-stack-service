package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeAgentRelationDTO;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.node.NodeTypeDTO;
import com.catface996.aiops.application.api.dto.node.request.BindAgentRequest;
import com.catface996.aiops.application.api.dto.node.request.CreateNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.DeleteNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.GetNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.ListAgentsByNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.ListNodesByAgentRequest;
import com.catface996.aiops.application.api.dto.node.request.ListUnboundAgentsRequest;
import com.catface996.aiops.application.api.dto.node.request.QueryNodesRequest;
import com.catface996.aiops.application.api.dto.node.request.UnbindAgentRequest;
import com.catface996.aiops.application.api.dto.node.request.UpdateNodeRequest;
import com.catface996.aiops.application.api.service.node.NodeApplicationService;
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

import java.util.List;

/**
 * 资源节点管理控制器（POST-Only API）
 *
 * <p>提供资源节点管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>节点 CRUD 接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/nodes/create - 创建节点</li>
 *   <li>POST /api/service/v1/nodes/query - 查询节点列表</li>
 *   <li>POST /api/service/v1/nodes/get - 查询节点详情</li>
 *   <li>POST /api/service/v1/nodes/update - 更新节点</li>
 *   <li>POST /api/service/v1/nodes/delete - 删除节点</li>
 *   <li>POST /api/service/v1/nodes/types/query - 查询节点类型列表</li>
 * </ul>
 *
 * <p>Node-Agent 绑定接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/nodes/bindAgent - 绑定 Agent 到节点</li>
 *   <li>POST /api/service/v1/nodes/unbindAgent - 解绑 Agent 与节点</li>
 *   <li>POST /api/service/v1/nodes/listAgents - 查询节点关联的 Agent 列表</li>
 *   <li>POST /api/service/v1/nodes/listNodesByAgent - 查询 Agent 关联的节点列表</li>
 *   <li>POST /api/service/v1/nodes/listUnboundAgents - 查询未绑定到指定节点的 Agent 列表</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-005: 节点 API 保持接口契约不变</li>
 *   <li>FR-007: 资源节点 API 路径变更为 /api/service/v1/nodes/*</li>
 *   <li>US2: 查询所有节点</li>
 *   <li>US4: 节点管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/nodes")
@RequiredArgsConstructor
@Tag(name = "资源节点管理", description = "资源节点管理接口：创建、查询、更新、删除（POST-Only API）")
public class NodeController {

    private final NodeApplicationService nodeApplicationService;

    /**
     * 创建节点
     *
     * <p>创建新的资源节点。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建节点", description = "创建新的资源节点")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = NodeDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "节点名称已存在")
    })
    public ResponseEntity<Result<NodeDTO>> createNode(
            @Valid @RequestBody CreateNodeRequest request) {

        log.info("创建节点，name: {}, nodeTypeId: {}, operatorId: {}",
                request.getName(), request.getNodeTypeId(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        NodeDTO node = nodeApplicationService.createNode(request, operatorId, operatorName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("节点创建成功", node));
    }

    /**
     * 查询节点列表
     *
     * <p>分页查询资源节点列表，支持按类型、状态和关键词筛选。</p>
     */
    @PostMapping("/query")
    @Operation(summary = "查询节点列表", description = "分页查询资源节点列表，支持按类型、状态和关键词筛选。可选指定拓扑图ID筛选属于该拓扑图的节点。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<NodeDTO>>> queryNodes(
            @Valid @RequestBody QueryNodesRequest request) {

        log.info("查询节点列表，nodeTypeId: {}, status: {}, keyword: {}, topologyId: {}",
                request.getNodeTypeId(), request.getStatus(), request.getKeyword(), request.getTopologyId());

        PageResult<NodeDTO> result = nodeApplicationService.listNodes(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取节点详情
     *
     * <p>根据ID获取节点详细信息。</p>
     */
    @PostMapping("/get")
    @Operation(summary = "获取节点详情", description = "根据ID获取节点详细信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "节点不存在")
    })
    public ResponseEntity<Result<NodeDTO>> getNode(
            @Valid @RequestBody GetNodeRequest request) {

        log.info("获取节点详情，id: {}", request.getId());

        NodeDTO node = nodeApplicationService.getNodeById(request.getId());

        if (node == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404002, "节点不存在"));
        }

        return ResponseEntity.ok(Result.success(node));
    }

    /**
     * 更新节点
     *
     * <p>更新节点信息，使用乐观锁防止并发冲突。</p>
     */
    @PostMapping("/update")
    @Operation(summary = "更新节点", description = "更新节点信息，使用乐观锁防止并发冲突")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "节点不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<NodeDTO>> updateNode(
            @Valid @RequestBody UpdateNodeRequest request) {

        log.info("更新节点，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        NodeDTO node = nodeApplicationService.updateNode(
                request.getId(), request, operatorId, operatorName);

        return ResponseEntity.ok(Result.success("节点更新成功", node));
    }

    /**
     * 删除节点
     *
     * <p>删除资源节点。注意：会同时删除该节点在拓扑图中的成员关系。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除节点", description = "删除资源节点。注意：会同时删除该节点在拓扑图中的成员关系。")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "节点不存在")
    })
    public ResponseEntity<Result<Void>> deleteNode(
            @Valid @RequestBody DeleteNodeRequest request) {

        log.info("删除节点，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        Long operatorId = request.getOperatorId();
        String operatorName = "operator-" + operatorId;

        nodeApplicationService.deleteNode(request.getId(), operatorId, operatorName);

        return ResponseEntity.ok(Result.success("节点删除成功", null));
    }

    /**
     * 查询节点类型列表
     *
     * <p>获取所有可用的节点类型。</p>
     */
    @PostMapping("/types/query")
    @Operation(summary = "查询节点类型列表", description = "获取所有可用的节点类型")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<NodeTypeDTO>>> queryNodeTypes() {

        log.info("查询节点类型列表");

        List<NodeTypeDTO> nodeTypes = nodeApplicationService.listNodeTypes();

        return ResponseEntity.ok(Result.success(nodeTypes));
    }

    // ==================== Node-Agent 绑定接口 ====================

    /**
     * 绑定 Agent 到节点
     *
     * <p>将指定的 Agent 绑定到指定的资源节点。</p>
     *
     * <p>需求追溯：FR-002, US1</p>
     */
    @PostMapping("/bindAgent")
    @Operation(summary = "绑定 Agent 到节点", description = "将指定的 Agent 绑定到指定的资源节点")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "绑定成功",
                    content = @Content(schema = @Schema(implementation = NodeAgentRelationDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "节点或Agent不存在"),
            @ApiResponse(responseCode = "409", description = "绑定关系已存在")
    })
    public ResponseEntity<Result<NodeAgentRelationDTO>> bindAgent(
            @Valid @RequestBody BindAgentRequest request) {

        log.info("绑定 Agent 到节点，nodeId: {}, agentId: {}", request.getNodeId(), request.getAgentId());

        try {
            NodeAgentRelationDTO relation = nodeApplicationService.bindAgent(
                    request.getNodeId(), request.getAgentId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("绑定成功", relation));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("节点")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404001, e.getMessage()));
            } else if (e.getMessage().contains("Agent")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.error(404002, e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400001, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Result.error(409001, e.getMessage()));
        }
    }

    /**
     * 解绑 Agent 与节点
     *
     * <p>解除指定 Agent 与指定资源节点的绑定关系。</p>
     *
     * <p>需求追溯：FR-003, US4</p>
     */
    @PostMapping("/unbindAgent")
    @Operation(summary = "解绑 Agent 与节点", description = "解除指定 Agent 与指定资源节点的绑定关系")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "解绑成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "绑定关系不存在")
    })
    public ResponseEntity<Result<Void>> unbindAgent(
            @Valid @RequestBody UnbindAgentRequest request) {

        log.info("解绑 Agent 与节点，nodeId: {}, agentId: {}", request.getNodeId(), request.getAgentId());

        try {
            nodeApplicationService.unbindAgent(request.getNodeId(), request.getAgentId());
            return ResponseEntity.ok(Result.success("解绑成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404003, e.getMessage()));
        }
    }

    /**
     * 查询节点关联的 Agent 列表
     *
     * <p>查询指定资源节点关联的所有 Agent。</p>
     *
     * <p>需求追溯：FR-004, US2</p>
     */
    @PostMapping("/listAgents")
    @Operation(summary = "查询节点关联的 Agent 列表", description = "查询指定资源节点关联的所有 Agent")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<AgentDTO>>> listAgentsByNode(
            @Valid @RequestBody ListAgentsByNodeRequest request) {

        log.info("查询节点关联的 Agent 列表，nodeId: {}", request.getNodeId());

        List<AgentDTO> agents = nodeApplicationService.listAgentsByNode(request.getNodeId());

        return ResponseEntity.ok(Result.success(agents));
    }

    /**
     * 查询 Agent 关联的节点列表
     *
     * <p>查询指定 Agent 关联的所有资源节点。</p>
     *
     * <p>需求追溯：FR-005, US3</p>
     */
    @PostMapping("/listNodesByAgent")
    @Operation(summary = "查询 Agent 关联的节点列表", description = "查询指定 Agent 关联的所有资源节点")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<NodeDTO>>> listNodesByAgent(
            @Valid @RequestBody ListNodesByAgentRequest request) {

        log.info("查询 Agent 关联的节点列表，agentId: {}", request.getAgentId());

        List<NodeDTO> nodes = nodeApplicationService.listNodesByAgent(request.getAgentId());

        return ResponseEntity.ok(Result.success(nodes));
    }

    /**
     * 查询未绑定到指定节点的 Agent 列表
     *
     * <p>查询未与指定资源节点绑定的 Agent，支持分页和关键词过滤。</p>
     */
    @PostMapping("/listUnboundAgents")
    @Operation(summary = "查询未绑定到指定节点的 Agent 列表", description = "查询未与指定资源节点绑定的 Agent，支持分页和关键词过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<AgentDTO>>> listUnboundAgents(
            @Valid @RequestBody ListUnboundAgentsRequest request) {

        log.info("查询未绑定到节点的 Agent 列表，nodeId: {}, keyword: {}", request.getNodeId(), request.getKeyword());

        PageResult<AgentDTO> result = nodeApplicationService.listUnboundAgents(request);

        return ResponseEntity.ok(Result.success(result));
    }
}
