package com.catface996.aiops.application.impl.service.execution;

import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.execution.ExecutionEventDTO;
import com.catface996.aiops.application.api.dto.execution.request.CancelExecutionRequest;
import com.catface996.aiops.application.api.dto.execution.request.TriggerExecutionRequest;
import com.catface996.aiops.application.api.service.agentbound.AgentBoundApplicationService;
import com.catface996.aiops.application.api.service.execution.ExecutionApplicationService;
import com.catface996.aiops.application.impl.service.diagnosis.DiagnosisPersistenceService;
import com.catface996.aiops.application.impl.service.execution.client.ExecutorServiceClient;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest;
import com.catface996.aiops.application.impl.service.execution.client.dto.ExecutorEvent;
import com.catface996.aiops.application.impl.service.execution.client.dto.StartRunRequest;
import com.catface996.aiops.application.impl.service.execution.transformer.HierarchyTransformer;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTask;
import com.catface996.aiops.infrastructure.cache.redis.diagnosis.DiagnosisStreamCacheService;
import com.catface996.aiops.repository.diagnosis.DiagnosisTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private final DiagnosisTaskRepository diagnosisTaskRepository;
    private final DiagnosisPersistenceService persistenceService;
    private final DiagnosisStreamCacheService cacheService;

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

        // Step 3: 创建诊断任务（在调用 executor 之前）
        DiagnosisTask diagnosisTask = createDiagnosisTask(
                request.getTopologyId(),
                request.getUserMessage(),
                request.getUserId()
        );
        final Long taskId = diagnosisTask.getId();
        log.info("Created diagnosis task: {}", taskId);

        // Step 4: 转换为 Executor 格式
        CreateHierarchyRequest createRequest = hierarchyTransformer.transform(hierarchyStructure);

        log.info("Creating hierarchy '{}' with {} teams",
                createRequest.getName(),
                createRequest.getTeams() != null ? createRequest.getTeams().size() : 0);

        // Step 5: 调用 Executor 服务（异步流）
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

                                // 更新诊断任务的 runId
                                diagnosisTaskRepository.updateRunId(taskId, runId);

                                // 获取 Executor 的事件流
                                Flux<ExecutorEvent> eventStream = executorServiceClient.streamEvents(runId);

                                // 转换并收集流式数据到 Redis
                                return eventStream
                                        .map(this::transformEvent)
                                        .doOnNext(event -> collectDiagnosisEvent(taskId, event))
                                        .doOnComplete(() -> onDiagnosisComplete(taskId))
                                        .doOnError(error -> onDiagnosisError(taskId, error.getMessage()))
                                        .concatWith(Flux.defer(() -> {
                                            // 在流开始时发送包含 taskId 的 started 事件
                                            return Flux.empty();
                                        }))
                                        .startWith(createStartedEvent(runId, taskId));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Executor service error: {}", e.getMessage(), e);
                    onDiagnosisError(taskId, e.getMessage());
                    return Flux.just(ExecutionEventDTO.error("Executor service error: " + e.getMessage()));
                });
    }

    /**
     * 创建诊断任务
     */
    @Transactional
    public DiagnosisTask createDiagnosisTask(Long topologyId, String userQuestion, Long operatorId) {
        DiagnosisTask task = DiagnosisTask.create(topologyId, userQuestion, operatorId);
        return diagnosisTaskRepository.save(task);
    }

    /**
     * 创建 started 事件（包含 taskId）
     */
    private ExecutionEventDTO createStartedEvent(String runId, Long taskId) {
        return ExecutionEventDTO.builder()
                .type("started")
                .runId(runId)
                .taskId(taskId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 收集诊断事件到 Redis
     */
    private void collectDiagnosisEvent(Long taskId, ExecutionEventDTO event) {
        if (event == null || event.getAgentId() == null) {
            return;
        }
        try {
            Long agentBoundId = Long.parseLong(event.getAgentId());
            String agentName = event.getAgentName();
            String content = event.getContent();
            String type = event.getType();

            if ("llm.stream".equals(type) || "llm.reasoning".equals(type)) {
                if (content != null) {
                    cacheService.appendStreamContent(taskId, agentBoundId, agentName, content);
                }
            } else if ("lifecycle.started".equals(type)) {
                cacheService.appendStreamContent(taskId, agentBoundId, agentName != null ? agentName : "Unknown", "");
            } else if ("lifecycle.completed".equals(type)) {
                cacheService.markAgentEnded(taskId, agentBoundId);
            }
        } catch (NumberFormatException e) {
            log.debug("Ignoring event with non-numeric agentId: {}", event.getAgentId());
        }
    }

    /**
     * 诊断完成回调
     *
     * <p>触发异步持久化处理</p>
     */
    private void onDiagnosisComplete(Long taskId) {
        log.info("Diagnosis completed, triggering async persistence for taskId: {}", taskId);
        persistenceService.handleCompletionAsync(taskId);
    }

    /**
     * 诊断错误回调
     *
     * <p>触发异步错误处理</p>
     */
    private void onDiagnosisError(Long taskId, String errorMessage) {
        log.error("Diagnosis error for taskId: {}, error: {}", taskId, errorMessage);
        persistenceService.handleErrorAsync(taskId, errorMessage);
    }

    @Override
    public ExecutionEventDTO cancelExecution(CancelExecutionRequest request) {
        log.info("Cancelling execution for run: {}", request.getRunId());

        Boolean success = executorServiceClient.cancelRun(request.getRunId()).block();

        if (Boolean.TRUE.equals(success)) {
            log.info("Execution cancelled successfully: {}", request.getRunId());

            // 异步处理诊断任务取消（使用 Spring @Async）
            persistenceService.handleCancellationAsync(request.getRunId());

            return ExecutionEventDTO.cancelled(request.getRunId());
        } else {
            log.warn("Failed to cancel execution: {}", request.getRunId());
            return ExecutionEventDTO.error("Failed to cancel execution: " + request.getRunId());
        }
    }

    /**
     * 将 ExecutorEvent 转换为 ExecutionEventDTO
     *
     * <p>映射关系：</p>
     * <ul>
     *   <li>type: event.category.action 或 event.type</li>
     *   <li>runId: event.run_id</li>
     *   <li>agentId: source.agent_id（绑定关系 ID，用于追溯）</li>
     *   <li>agentName: source.agent_name 或 event.agent</li>
     *   <li>agentType: source.agent_type</li>
     *   <li>teamName: source.team_name</li>
     *   <li>content: data.content 或 event.content</li>
     * </ul>
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
                .type(event.getEventType())
                .runId(event.getRunId())
                .agentId(event.getAgentId())
                .agentName(event.getAgentName())
                .agentType(event.getAgentType())
                .teamName(event.getTeamName())
                .content(event.getEventContent())
                .timestamp(timestamp)
                .metadata(event.getData())
                .build();
    }
}
