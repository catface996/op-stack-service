package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agent.AgentStatsDTO;
import com.catface996.aiops.application.api.dto.agent.request.*;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.service.agent.AgentApplicationService;
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
 * Agent 管理控制器（POST-Only API）
 *
 * <p>提供 Agent 管理相关的 HTTP 接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>Agent 管理接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/agents/list - 查询 Agent 列表</li>
 *   <li>POST /api/service/v1/agents/get - 查询 Agent 详情</li>
 *   <li>POST /api/service/v1/agents/create - 创建 Agent</li>
 *   <li>POST /api/service/v1/agents/update - 更新 Agent（基本信息 + LLM 配置）</li>
 *   <li>POST /api/service/v1/agents/delete - 删除 Agent</li>
 *   <li>POST /api/service/v1/agents/assign - 分配 Agent 到团队</li>
 *   <li>POST /api/service/v1/agents/unassign - 取消 Agent 团队分配</li>
 *   <li>POST /api/service/v1/agents/stats - 查询 Agent 统计信息</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agent 管理", description = "Agent 管理接口：查询、创建、更新、删除、配置、团队分配、模板和统计（POST-Only API）")
public class AgentController {

    private final AgentApplicationService agentApplicationService;

    /**
     * 查询 Agent 列表
     *
     * <p>分页查询 Agent 列表，支持多维度筛选：</p>
     * <ul>
     *   <li>按角色筛选：GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER</li>
     *   <li>按团队筛选：指定 teamId 查询该团队的 Agent</li>
     *   <li>关键词搜索：模糊匹配名称和专业领域</li>
     * </ul>
     */
    @PostMapping("/list")
    @Operation(summary = "查询 Agent 列表",
            description = "分页查询 Agent 列表，支持按角色（GLOBAL_SUPERVISOR/TEAM_SUPERVISOR/WORKER/SCOUTER）、团队筛选和关键词搜索（模糊匹配名称、专业领域）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = PageResult.class))),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<AgentDTO>>> listAgents(
            @Valid @RequestBody ListAgentsRequest request) {

        log.info("查询 Agent 列表，role: {}, teamId: {}, keyword: {}, page: {}, size: {}",
                request.getRole(), request.getTeamId(), request.getKeyword(),
                request.getPage(), request.getSize());

        PageResult<AgentDTO> result = agentApplicationService.listAgents(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取 Agent 详情
     *
     * <p>查看 Agent 详情，包括：</p>
     * <ul>
     *   <li>基本信息：名称、角色、专业领域</li>
     *   <li>AI 配置：模型、温度、系统指令、默认上下文</li>
     *   <li>发现统计：warnings 和 critical 数量</li>
     *   <li>团队关联：所属团队 ID 列表</li>
     * </ul>
     */
    @PostMapping("/get")
    @Operation(summary = "获取 Agent 详情",
            description = "查看 Agent 详情，包含基本信息、AI 配置（模型、温度、系统指令）、发现统计（warnings/critical）、所属团队列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = AgentDTO.class))),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 不存在")
    })
    public ResponseEntity<Result<AgentDTO>> getAgent(
            @Valid @RequestBody GetAgentRequest request) {

        log.info("获取 Agent 详情，id: {}", request.getId());

        AgentDTO agent = agentApplicationService.getAgentById(request.getId());

        return ResponseEntity.ok(Result.success(agent));
    }

    /**
     * 创建 Agent
     *
     * <p>创建新的 Agent，业务规则：</p>
     * <ul>
     *   <li>名称必须唯一，不能与已有 Agent 重复</li>
     *   <li>角色必须是有效值：GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER</li>
     *   <li>GLOBAL_SUPERVISOR 全局唯一，只能存在一个</li>
     *   <li>AI 配置可选，不提供则使用系统默认配置</li>
     * </ul>
     */
    @PostMapping("/create")
    @Operation(summary = "创建 Agent",
            description = "创建新的 Agent。名称必须唯一；角色支持 GLOBAL_SUPERVISOR（全局唯一）、TEAM_SUPERVISOR、WORKER、SCOUTER；可选配置 AI 模型、温度、系统指令")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = AgentDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效（名称为空、角色无效等）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "名称冲突或 GLOBAL_SUPERVISOR 已存在")
    })
    public ResponseEntity<Result<AgentDTO>> createAgent(
            @Valid @RequestBody CreateAgentRequest request) {

        log.info("创建 Agent，name: {}, role: {}", request.getName(), request.getRole());

        AgentDTO agent = agentApplicationService.createAgent(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("Agent 创建成功", agent));
    }

    /**
     * 更新 Agent
     *
     * <p>更新 Agent 的基本信息和 LLM 配置，支持部分更新（仅更新非 null 字段）：</p>
     * <ul>
     *   <li>基本信息：name（名称）、specialty（专业领域）</li>
     *   <li>LLM 配置：promptTemplateId（提示词模板）、model（模型）、temperature、topP、maxTokens、maxRuntime</li>
     * </ul>
     * <p>业务规则：</p>
     * <ul>
     *   <li>角色（role）创建后不可变</li>
     *   <li>新名称必须唯一，不能与其他 Agent 重复</li>
     *   <li>Agent 处于 WORKING/THINKING 状态时不能更新</li>
     * </ul>
     */
    @PostMapping("/update")
    @Operation(summary = "更新 Agent",
            description = "更新 Agent 的基本信息和 LLM 配置。可更新：name、specialty、promptTemplateId、model、temperature、topP、maxTokens、maxRuntime。角色不可变；名称须唯一；工作中禁止更新")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = AgentDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 不存在"),
            @ApiResponse(responseCode = "409", description = "名称冲突"),
            @ApiResponse(responseCode = "423", description = "Agent 正在工作中，无法更新")
    })
    public ResponseEntity<Result<AgentDTO>> updateAgent(
            @Valid @RequestBody UpdateAgentRequest request) {

        log.info("更新 Agent，id: {}, name: {}, model: {}", request.getId(), request.getName(), request.getModel());

        AgentDTO agent = agentApplicationService.updateAgent(request);

        return ResponseEntity.ok(Result.success("Agent 更新成功", agent));
    }

    /**
     * 删除 Agent
     *
     * <p>软删除 Agent，业务规则：</p>
     * <ul>
     *   <li>GLOBAL_SUPERVISOR 不能删除（系统唯一）</li>
     *   <li>TEAM_SUPERVISOR 有成员时不能删除（需先移除成员）</li>
     *   <li>Agent 处于 WORKING/THINKING 状态时不能删除</li>
     *   <li>删除时会同时清除所有团队关联关系</li>
     * </ul>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除 Agent",
            description = "软删除 Agent。GLOBAL_SUPERVISOR 禁止删除；有成员的 TEAM_SUPERVISOR 禁止删除；正在工作的 Agent 禁止删除；删除时自动清除团队关联")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "不允许删除（如 GLOBAL_SUPERVISOR 或有成员的 TEAM_SUPERVISOR）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 不存在"),
            @ApiResponse(responseCode = "423", description = "Agent 正在工作中，无法删除")
    })
    public ResponseEntity<Result<Void>> deleteAgent(
            @Valid @RequestBody DeleteAgentRequest request) {

        log.info("删除 Agent，id: {}", request.getId());

        agentApplicationService.deleteAgent(request);

        return ResponseEntity.ok(Result.success("Agent 删除成功", null));
    }

    /**
     * 分配 Agent 到团队
     *
     * <p>将 Agent 分配到指定团队，业务规则：</p>
     * <ul>
     *   <li>Agent 和 Team 必须存在</li>
     *   <li>同一 Agent 可以分配到多个团队</li>
     *   <li>不能重复分配到同一团队</li>
     *   <li>分配后 Agent 初始状态为 IDLE</li>
     * </ul>
     */
    @PostMapping("/assign")
    @Operation(summary = "分配 Agent 到团队",
            description = "将 Agent 分配到指定团队。同一 Agent 可分配到多个团队；不能重复分配；分配后初始状态为 IDLE")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "分配成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 或 Team 不存在"),
            @ApiResponse(responseCode = "409", description = "Agent 已分配到该团队")
    })
    public ResponseEntity<Result<Void>> assignAgent(
            @Valid @RequestBody AssignAgentRequest request) {

        log.info("分配 Agent 到团队，agentId: {}, teamId: {}", request.getAgentId(), request.getTeamId());

        agentApplicationService.assignAgent(request);

        return ResponseEntity.ok(Result.success("Agent 分配成功", null));
    }

    /**
     * 取消 Agent 团队分配
     *
     * <p>将 Agent 从指定团队中移除，业务规则：</p>
     * <ul>
     *   <li>TEAM_SUPERVISOR 不能取消分配（需通过删除操作）</li>
     *   <li>Agent 必须已分配到该团队</li>
     *   <li>取消分配后，该团队中的 Agent 状态记录会被清除</li>
     * </ul>
     */
    @PostMapping("/unassign")
    @Operation(summary = "取消 Agent 团队分配",
            description = "将 Agent 从团队中移除。TEAM_SUPERVISOR 禁止取消分配；Agent 必须已分配到该团队；取消后清除该团队的状态记录")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消分配成功"),
            @ApiResponse(responseCode = "400", description = "TEAM_SUPERVISOR 不能取消分配"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "Agent 不存在或未分配到该团队")
    })
    public ResponseEntity<Result<Void>> unassignAgent(
            @Valid @RequestBody UnassignAgentRequest request) {

        log.info("取消 Agent 团队分配，agentId: {}, teamId: {}", request.getAgentId(), request.getTeamId());

        agentApplicationService.unassignAgent(request);

        return ResponseEntity.ok(Result.success("Agent 取消分配成功", null));
    }

    /**
     * 查询 Agent 统计信息
     *
     * <p>获取 Agent 统计信息，支持两种模式：</p>
     * <ul>
     *   <li>整体统计（不传 agentId）：总数、按角色分布、按状态分布、总发现数</li>
     *   <li>单个统计（传 agentId）：该 Agent 的 warnings 和 critical 数量</li>
     * </ul>
     * <p>可选时间范围过滤（startTime、endTime）。</p>
     */
    @PostMapping("/stats")
    @Operation(summary = "查询 Agent 统计信息",
            description = "获取 Agent 统计信息。不传 agentId 返回整体统计（总数、按角色/状态分布、总发现数）；传 agentId 返回单个 Agent 统计。支持时间范围过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = AgentStatsDTO.class))),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<AgentStatsDTO>> getAgentStats(
            @Valid @RequestBody AgentStatsRequest request) {

        log.info("查询 Agent 统计信息，agentId: {}", request.getAgentId());

        AgentStatsDTO stats = agentApplicationService.getAgentStats(request);

        return ResponseEntity.ok(Result.success(stats));
    }
}
