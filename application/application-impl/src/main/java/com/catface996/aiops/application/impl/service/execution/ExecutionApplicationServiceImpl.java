package com.catface996.aiops.application.impl.service.execution;

import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.execution.ExecutionEventDTO;
import com.catface996.aiops.application.api.dto.execution.request.CancelExecutionRequest;
import com.catface996.aiops.application.api.dto.execution.request.TriggerExecutionRequest;
import com.catface996.aiops.application.api.service.agentbound.AgentBoundApplicationService;
import com.catface996.aiops.application.api.service.execution.ExecutionApplicationService;
import com.catface996.aiops.application.impl.service.execution.client.ExecutorServiceClient;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest;
import com.catface996.aiops.application.impl.service.execution.client.dto.ExecutorEvent;
import com.catface996.aiops.application.impl.service.execution.client.dto.StartRunRequest;
import com.catface996.aiops.application.impl.service.execution.transformer.HierarchyTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 执行应用服务实现
 *
 * <p>实现多智能体执行的核心业务逻辑。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionApplicationServiceImpl implements ExecutionApplicationService {

    private final AgentBoundApplicationService agentBoundApplicationService;
    private final ExecutorServiceClient executorServiceClient;
    private final HierarchyTransformer hierarchyTransformer;

    @Override
    public Flux<ExecutionEventDTO> triggerExecution(TriggerExecutionRequest request) {
        log.info("Triggering execution for topology: {}, message: {}",
                request.getTopologyId(),
                request.getUserMessage().substring(0, Math.min(50, request.getUserMessage().length())) + "...");

        // Step 1: 查询层级结构（使用新的 AgentBoundApplicationService）
        HierarchyStructureDTO hierarchyStructure;
        try {
            hierarchyStructure = agentBoundApplicationService.queryHierarchy(request.getTopologyId());
        } catch (IllegalArgumentException e) {
            log.error("Topology not found: {}", request.getTopologyId());
            return Flux.just(ExecutionEventDTO.error("Topology not found: " + request.getTopologyId()));
        }

        // Step 2: 验证配置
        if (hierarchyStructure.getGlobalSupervisor() == null) {
            log.error("Topology {} has no Global Supervisor", request.getTopologyId());
            return Flux.just(ExecutionEventDTO.error("Topology does not have a bound Global Supervisor Agent"));
        }

        if (hierarchyStructure.getTeams() == null || hierarchyStructure.getTeams().isEmpty()) {
            log.error("Topology {} has no teams", request.getTopologyId());
            return Flux.just(ExecutionEventDTO.error("Topology has no teams configured"));
        }

        // Step 3: 转换为 Executor 格式
        CreateHierarchyRequest createRequest = hierarchyTransformer.transform(hierarchyStructure);

        log.info("Creating hierarchy '{}' with {} teams",
                createRequest.getName(),
                createRequest.getTeams() != null ? createRequest.getTeams().size() : 0);

        // Step 4: 调用 Executor 服务（异步流）
        return executorServiceClient.createHierarchy(createRequest)
                .flatMapMany(createResponse -> {
                    log.info("Hierarchy created: {}", createResponse.getHierarchyId());

                    StartRunRequest startRequest = StartRunRequest.builder()
                            .hierarchyId(createResponse.getHierarchyId())
                            .task(request.getUserMessage())
                            .build();

                    return executorServiceClient.startRun(startRequest)
                            .flatMapMany(startResponse -> {
                                log.info("Run started: {}", startResponse.getRunId());
                                String runId = startResponse.getRunId();

                                // 先发送 started 事件（包含 runId），然后发送后续事件流
                                Flux<ExecutionEventDTO> startedEvent = Flux.just(ExecutionEventDTO.started(runId));
                                Flux<ExecutionEventDTO> eventStream = executorServiceClient.streamEvents(runId)
                                        .map(this::transformEvent);

                                return Flux.concat(startedEvent, eventStream);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Executor service error: {}", e.getMessage(), e);
                    return Flux.just(ExecutionEventDTO.error("Executor service error: " + e.getMessage()));
                });
    }

    @Override
    public ExecutionEventDTO cancelExecution(CancelExecutionRequest request) {
        log.info("Cancelling execution for run: {}", request.getRunId());

        Boolean success = executorServiceClient.cancelRun(request.getRunId()).block();

        if (Boolean.TRUE.equals(success)) {
            log.info("Execution cancelled successfully: {}", request.getRunId());
            return ExecutionEventDTO.cancelled(request.getRunId());
        } else {
            log.warn("Failed to cancel execution: {}", request.getRunId());
            return ExecutionEventDTO.error("Failed to cancel execution: " + request.getRunId());
        }
    }

    /**
     * 将 ExecutorEvent 转换为 ExecutionEventDTO
     */
    private ExecutionEventDTO transformEvent(ExecutorEvent event) {
        LocalDateTime timestamp;
        try {
            timestamp = event.getTimestamp() != null
                    ? LocalDateTime.parse(event.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME)
                    : LocalDateTime.now();
        } catch (Exception e) {
            timestamp = LocalDateTime.now();
        }

        return ExecutionEventDTO.builder()
                .type(event.getType())
                .agentName(event.getAgent())
                .content(event.getContent())
                .timestamp(timestamp)
                .metadata(event.getData())
                .build();
    }
}
