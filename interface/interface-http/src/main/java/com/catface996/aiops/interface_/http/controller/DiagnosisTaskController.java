package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.diagnosis.DiagnosisTaskDTO;
import com.catface996.aiops.application.api.service.diagnosis.DiagnosisApplicationService;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 诊断任务管理控制器（POST-Only API）
 *
 * <p>提供诊断任务查询相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>注意：诊断任务的创建在 Trigger 执行流程中自动完成，无需单独创建接口。</p>
 *
 * <p>诊断任务接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/diagnosis-tasks/get - 查询诊断任务详情</li>
 *   <li>POST /api/service/v1/diagnosis-tasks/query-by-topology - 查询拓扑图的诊断历史</li>
 *   <li>POST /api/service/v1/diagnosis-tasks/query-running - 查询运行中的任务</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/diagnosis-tasks")
@Tag(name = "诊断任务管理", description = "诊断任务查询接口（POST-Only API）")
public class DiagnosisTaskController {

    private final DiagnosisApplicationService diagnosisApplicationService;

    public DiagnosisTaskController(DiagnosisApplicationService diagnosisApplicationService) {
        this.diagnosisApplicationService = diagnosisApplicationService;
    }

    /**
     * 查询诊断任务详情
     *
     * <p>根据ID查询诊断任务详情，包含Agent诊断过程列表。</p>
     */
    @PostMapping("/get")
    @Operation(summary = "查询诊断任务详情", description = "根据ID查询诊断任务详情，包含Agent诊断过程列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "诊断任务不存在")
    })
    public ResponseEntity<Result<DiagnosisTaskDTO>> getDiagnosisTask(
            @Valid @RequestBody QueryDiagnosisTaskByIdRequest request) {

        log.info("查询诊断任务详情，taskId: {}, operatorId: {}", request.taskId(), request.operatorId());

        DiagnosisTaskDTO task = diagnosisApplicationService.queryById(request.taskId());

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404001, "诊断任务不存在"));
        }

        return ResponseEntity.ok(Result.success(task));
    }

    /**
     * 查询拓扑图的诊断任务历史
     *
     * <p>分页查询指定拓扑图的诊断任务历史列表。</p>
     */
    @PostMapping("/query-by-topology")
    @Operation(summary = "查询诊断任务历史", description = "分页查询指定拓扑图的诊断任务历史列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<DiagnosisTaskDTO>>> queryByTopology(
            @Valid @RequestBody QueryDiagnosisTaskByTopologyRequest request) {

        log.info("查询诊断任务历史，topologyId: {}, page: {}, size: {}",
                request.topologyId(), request.getPage(), request.getSize());

        PageResult<DiagnosisTaskDTO> result = diagnosisApplicationService.queryByTopology(
                request.topologyId(), request.getPage(), request.getSize());

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 查询运行中的诊断任务
     *
     * <p>查询当前运行中的诊断任务列表。</p>
     */
    @PostMapping("/query-running")
    @Operation(summary = "查询运行中的任务", description = "查询当前运行中的诊断任务列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<DiagnosisTaskDTO>>> queryRunningTasks(
            @Valid @RequestBody QueryRunningTasksRequest request) {

        log.info("查询运行中的诊断任务，topologyId: {}, operatorId: {}",
                request.topologyId(), request.operatorId());

        List<DiagnosisTaskDTO> tasks = diagnosisApplicationService.queryRunningTasks(request.topologyId());

        return ResponseEntity.ok(Result.success(tasks));
    }

    // ==================== 内部请求类 ====================

    /**
     * 根据ID查询诊断任务请求
     */
    @Schema(description = "查询诊断任务详情请求")
    public record QueryDiagnosisTaskByIdRequest(
            @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long operatorId,

            @Schema(description = "诊断任务ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
            Long taskId
    ) {}

    /**
     * 根据拓扑图查询诊断任务请求
     */
    @Schema(description = "查询诊断任务历史请求")
    public record QueryDiagnosisTaskByTopologyRequest(
            @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long operatorId,

            @Schema(description = "拓扑图ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
            Long topologyId,

            @Schema(description = "页码", example = "1", defaultValue = "1")
            Integer page,

            @Schema(description = "每页大小", example = "10", defaultValue = "10")
            Integer size
    ) {
        public Integer getPage() {
            return page != null ? page : 1;
        }
        public Integer getSize() {
            return size != null ? size : 10;
        }
    }

    /**
     * 查询运行中任务请求
     */
    @Schema(description = "查询运行中任务请求")
    public record QueryRunningTasksRequest(
            @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long operatorId,

            @Schema(description = "拓扑图ID（可选，为空查询所有）", example = "101")
            Long topologyId
    ) {}
}
