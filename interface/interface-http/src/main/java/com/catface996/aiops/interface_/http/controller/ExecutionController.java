package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.execution.ExecutionEventDTO;
import com.catface996.aiops.application.api.dto.execution.request.CancelExecutionRequest;
import com.catface996.aiops.application.api.dto.execution.request.TriggerExecutionRequest;
import com.catface996.aiops.application.api.service.execution.ExecutionApplicationService;
import com.catface996.aiops.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 执行控制器
 *
 * <p>提供多智能体执行相关的 HTTP 接口。</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /trigger - 触发多智能体执行（SSE 流式响应）</li>
 *   <li>POST /cancel - 取消正在执行的运行</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/executions")
@RequiredArgsConstructor
@Tag(name = "执行管理", description = "多智能体执行相关接口（POST-Only API）")
public class ExecutionController {

    private final ExecutionApplicationService executionApplicationService;

    /**
     * 用于 SSE 事件推送的线程池
     */
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    /**
     * 触发多智能体执行
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>验证拓扑图存在且配置正确</li>
     *   <li>查询层级团队结构</li>
     *   <li>创建 Executor 层级结构</li>
     *   <li>启动执行运行</li>
     *   <li>通过 SSE 流式返回执行事件</li>
     * </ol>
     *
     * @param request 触发请求
     * @return SSE 事件流
     */
    @Operation(
            summary = "触发多智能体执行",
            description = """
                    触发基于拓扑图的多智能体协作执行。

                    **执行流程**：
                    1. 验证拓扑图存在且已绑定 Global Supervisor
                    2. 查询层级团队结构
                    3. 在 Executor 服务创建层级结构
                    4. 启动执行运行
                    5. 通过 SSE 流式返回执行事件

                    **事件类型**：
                    - `thinking`: Agent 思考中
                    - `message`: Agent 消息
                    - `tool_call`: Agent 调用工具
                    - `tool_result`: 工具执行结果
                    - `error`: 错误事件
                    - `complete`: 执行完成
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "SSE 事件流",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(implementation = ExecutionEventDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误或拓扑图配置不完整",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Result.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "拓扑图不存在",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Result.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Executor 服务不可用",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Result.class)
                    )
            )
    })
    @PostMapping(value = "/trigger", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter triggerExecution(@Valid @RequestBody TriggerExecutionRequest request) {
        log.info("Received execution request for topology: {}", request.getTopologyId());

        // 创建 SSE Emitter，设置较长的超时时间（10分钟）
        SseEmitter emitter = new SseEmitter(600000L);

        // 设置完成和错误回调
        emitter.onCompletion(() -> log.info("SSE connection completed for topology: {}", request.getTopologyId()));
        emitter.onTimeout(() -> log.warn("SSE connection timed out for topology: {}", request.getTopologyId()));
        emitter.onError(e -> log.error("SSE connection error for topology {}: {}", request.getTopologyId(), e.getMessage()));

        // 在后台线程中处理事件流
        sseExecutor.execute(() -> {
            try {
                executionApplicationService.triggerExecution(request)
                        .subscribe(
                                event -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(event, MediaType.APPLICATION_JSON));
                                        log.debug("Sent event: type={}, agent={}", event.getType(), event.getAgentName());
                                    } catch (IOException e) {
                                        log.error("Failed to send SSE event: {}", e.getMessage());
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> {
                                    log.error("Execution error: {}", error.getMessage());
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(ExecutionEventDTO.error(error.getMessage()), MediaType.APPLICATION_JSON));
                                    } catch (IOException e) {
                                        log.error("Failed to send error event: {}", e.getMessage());
                                    }
                                    emitter.completeWithError(error);
                                },
                                () -> {
                                    log.info("Execution completed for topology: {}", request.getTopologyId());
                                    emitter.complete();
                                }
                        );
            } catch (Exception e) {
                log.error("Failed to start execution: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(ExecutionEventDTO.error("Failed to start execution: " + e.getMessage()),
                                    MediaType.APPLICATION_JSON));
                } catch (IOException ex) {
                    log.error("Failed to send error event: {}", ex.getMessage());
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 取消执行
     *
     * @param request 取消请求（包含 runId）
     * @return 取消结果
     */
    @Operation(
            summary = "取消执行",
            description = """
                    取消正在执行的多智能体运行。

                    **注意事项**：
                    - runId 来自触发执行时返回的 started 事件
                    - 如果运行已完成或已取消，取消操作将失败
                    - 取消操作是异步的，可能需要一些时间才能完全停止
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "取消结果",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Result.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Result.class)
                    )
            )
    })
    @PostMapping("/cancel")
    public Result<ExecutionEventDTO> cancelExecution(@Valid @RequestBody CancelExecutionRequest request) {
        log.info("Received cancel request for run: {}", request.getRunId());

        ExecutionEventDTO result = executionApplicationService.cancelExecution(request);

        if ("cancelled".equals(result.getType())) {
            return Result.success(result);
        } else {
            return Result.failure("CANCEL_FAILED", result.getContent());
        }
    }
}
