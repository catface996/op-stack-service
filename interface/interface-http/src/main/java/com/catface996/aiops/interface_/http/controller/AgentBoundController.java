package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.agentbound.AgentBoundDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.agentbound.request.BindAgentRequest;
import com.catface996.aiops.application.api.dto.agentbound.request.QueryByEntityRequest;
import com.catface996.aiops.application.api.service.agentbound.AgentBoundApplicationService;
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
 * Agent 绑定关系管理控制器（POST-Only API）
 *
 * <p>提供 Agent 与实体（Topology、Node）绑定关系的统一管理接口。</p>
 *
 * <p>API 端点：</p>
 * <ul>
 *   <li>POST /api/service/v1/agent-bounds/bind - 绑定 Agent 到实体</li>
 *   <li>POST /api/service/v1/agent-bounds/unbind - 解绑 Agent 与实体</li>
 *   <li>POST /api/service/v1/agent-bounds/query-by-entity - 按实体查询绑定</li>
 *   <li>POST /api/service/v1/agent-bounds/query-by-agent - 按 Agent 查询绑定</li>
 *   <li>POST /api/service/v1/agent-bounds/query-hierarchy - 查询层级结构</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 提供统一的 Agent 绑定 API</li>
 *   <li>FR-006: 支持绑定 Global Supervisor 到 Topology</li>
 *   <li>FR-007: 支持绑定 Team Supervisor/Worker 到 Node</li>
 *   <li>FR-009: 支持查询 Topology 的层级结构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/agent-bounds")
@RequiredArgsConstructor
@Tag(name = "Agent 绑定管理", description = "Agent 与实体（Topology、Node）绑定关系的统一管理接口（POST-Only API）")
public class AgentBoundController {

    private final AgentBoundApplicationService agentBoundApplicationService;

    /**
     * 绑定 Agent 到实体
     *
     * <p>将指定的 Agent 绑定到指定的实体（Topology 或 Node）。</p>
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>GLOBAL_SUPERVISOR 只能绑定到 TOPOLOGY</li>
     *   <li>TEAM_SUPERVISOR/TEAM_WORKER 只能绑定到 NODE</li>
     *   <li>Supervisor 绑定会替换已有绑定</li>
     *   <li>Worker 绑定为追加模式</li>
     * </ul>
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定 Agent 到实体", description = "将指定的 Agent 绑定到指定的实体（Topology 或 Node）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "绑定成功",
                    content = @Content(schema = @Schema(implementation = AgentBoundDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效或层级不匹配"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 或实体不存在")
    })
    public ResponseEntity<Result<AgentBoundDTO>> bindAgent(
            @Valid @RequestBody BindAgentRequest request) {

        log.info("绑定 Agent: agentId={}, entityId={}, entityType={}",
                request.getAgentId(), request.getEntityId(), request.getEntityType());

        AgentBoundDTO binding = agentBoundApplicationService.bindAgent(
                request.getAgentId(), request.getEntityId(), request.getEntityType());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("绑定成功", binding));
    }

    /**
     * 解绑 Agent 与实体
     *
     * <p>解除指定 Agent 与指定实体的绑定关系。</p>
     */
    @PostMapping("/unbind")
    @Operation(summary = "解绑 Agent 与实体", description = "解除指定 Agent 与指定实体的绑定关系")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "解绑成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<Void>> unbindAgent(
            @Valid @RequestBody BindAgentRequest request) {

        log.info("解绑 Agent: agentId={}, entityId={}, entityType={}",
                request.getAgentId(), request.getEntityId(), request.getEntityType());

        agentBoundApplicationService.unbindAgent(
                request.getAgentId(), request.getEntityId(), request.getEntityType());

        return ResponseEntity.ok(Result.success("解绑成功", null));
    }

    /**
     * 按实体查询绑定
     *
     * <p>查询指定实体（Topology 或 Node）绑定的 Agent 列表。</p>
     */
    @PostMapping("/query-by-entity")
    @Operation(summary = "按实体查询绑定", description = "查询指定实体绑定的 Agent 列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<AgentBoundDTO>>> queryByEntity(
            @Valid @RequestBody QueryByEntityRequest request) {

        log.info("按实体查询绑定: entityType={}, entityId={}, hierarchyLevel={}",
                request.getEntityType(), request.getEntityId(), request.getHierarchyLevel());

        List<AgentBoundDTO> bindings = agentBoundApplicationService.queryByEntity(
                request.getEntityType(), request.getEntityId(), request.getHierarchyLevel());

        return ResponseEntity.ok(Result.success(bindings));
    }

    /**
     * 按 Agent 查询绑定
     *
     * <p>查询指定 Agent 绑定的所有实体。</p>
     */
    @PostMapping("/query-by-agent")
    @Operation(summary = "按 Agent 查询绑定", description = "查询指定 Agent 绑定的所有实体")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<AgentBoundDTO>>> queryByAgent(
            @Valid @RequestBody QueryByAgentRequest request) {

        log.info("按 Agent 查询绑定: agentId={}, entityType={}",
                request.getAgentId(), request.getEntityType());

        List<AgentBoundDTO> bindings = agentBoundApplicationService.queryByAgent(
                request.getAgentId(), request.getEntityType());

        return ResponseEntity.ok(Result.success(bindings));
    }

    /**
     * 查询层级结构
     *
     * <p>查询指定 Topology 的完整层级团队结构。</p>
     */
    @PostMapping("/query-hierarchy")
    @Operation(summary = "查询层级结构", description = "查询指定 Topology 的完整层级团队结构")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Topology 不存在")
    })
    public ResponseEntity<Result<HierarchyStructureDTO>> queryHierarchy(
            @Valid @RequestBody QueryHierarchyRequest request) {

        log.info("查询层级结构: topologyId={}", request.getTopologyId());

        HierarchyStructureDTO hierarchy = agentBoundApplicationService.queryHierarchy(
                request.getTopologyId());

        return ResponseEntity.ok(Result.success(hierarchy));
    }

    // ===== 内部请求 DTO =====

    /**
     * 按 Agent 查询绑定请求
     */
    @Schema(description = "按 Agent 查询绑定请求")
    public static class QueryByAgentRequest {
        @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long agentId;

        @Schema(description = "实体类型过滤（可选）：TOPOLOGY 或 NODE", example = "NODE")
        private String entityType;

        public Long getAgentId() { return agentId; }
        public void setAgentId(Long agentId) { this.agentId = agentId; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
    }

    /**
     * 查询层级结构请求
     */
    @Schema(description = "查询层级结构请求")
    public static class QueryHierarchyRequest {
        @Schema(description = "Topology ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long topologyId;

        public Long getTopologyId() { return topologyId; }
        public void setTopologyId(Long topologyId) { this.topologyId = topologyId; }
    }
}
