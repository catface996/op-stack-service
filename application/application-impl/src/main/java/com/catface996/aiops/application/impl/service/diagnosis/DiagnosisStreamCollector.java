package com.catface996.aiops.application.impl.service.diagnosis;

import com.catface996.aiops.application.api.dto.execution.ExecutionEventDTO;
import com.catface996.aiops.infrastructure.cache.redis.diagnosis.DiagnosisStreamCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * 诊断流式数据收集器
 *
 * <p>收集 executor 返回的 SSE 流式事件，按 agent_bound_id 分类存储到 Redis。</p>
 *
 * <p>事件处理逻辑：</p>
 * <ul>
 *   <li>llm.stream: Agent 流式输出内容，追加到 Redis List</li>
 *   <li>lifecycle.started: Agent 开始执行</li>
 *   <li>lifecycle.completed: Agent 执行完成，标记结束时间</li>
 *   <li>error/system.error: 错误事件，记录并标记任务失败</li>
 *   <li>complete: 执行完成事件</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Component
public class DiagnosisStreamCollector {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosisStreamCollector.class);

    private final DiagnosisStreamCacheService cacheService;

    public DiagnosisStreamCollector(DiagnosisStreamCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 收集流式事件并存储到 Redis
     *
     * @param taskId      诊断任务ID
     * @param eventStream 事件流
     * @param onComplete  完成回调
     * @param onError     错误回调
     */
    public void collectStream(Long taskId,
                              Flux<ExecutionEventDTO> eventStream,
                              Runnable onComplete,
                              Consumer<Throwable> onError) {
        logger.info("开始收集诊断流式数据，taskId: {}", taskId);

        eventStream.subscribe(
                event -> handleEvent(taskId, event),
                error -> {
                    logger.error("诊断流式数据收集错误，taskId: {}, error: {}", taskId, error.getMessage());
                    handleExecutorError(taskId, error.getMessage());
                    if (onError != null) {
                        onError.accept(error);
                    }
                },
                () -> {
                    logger.info("诊断流式数据收集完成，taskId: {}", taskId);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 处理单个事件
     */
    private void handleEvent(Long taskId, ExecutionEventDTO event) {
        if (event == null) {
            return;
        }

        String type = event.getType();
        String agentId = event.getAgentId();
        String agentName = event.getAgentName();
        String content = event.getContent();

        logger.debug("处理诊断事件，taskId: {}, type: {}, agentId: {}, agentName: {}",
                taskId, type, agentId, agentName);

        if (type == null) {
            return;
        }

        switch (type) {
            case "llm.stream" -> handleLlmStream(taskId, agentId, agentName, content);
            case "llm.reasoning" -> handleLlmStream(taskId, agentId, agentName, content);
            case "lifecycle.started" -> handleAgentStarted(taskId, agentId, agentName);
            case "lifecycle.completed" -> handleAgentCompleted(taskId, agentId);
            case "lifecycle.failed" -> handleAgentFailed(taskId, agentId, content);
            case "error", "system.error" -> handleError(taskId, content);
            case "complete" -> handleComplete(taskId);
            // 其他事件类型忽略
            default -> logger.debug("忽略事件类型: {}", type);
        }
    }

    /**
     * 处理 LLM 流式输出
     */
    private void handleLlmStream(Long taskId, String agentId, String agentName, String content) {
        if (agentId == null || content == null) {
            return;
        }
        try {
            Long agentBoundId = Long.parseLong(agentId);
            cacheService.appendStreamContent(taskId, agentBoundId, agentName, content);
        } catch (NumberFormatException e) {
            logger.warn("无法解析 agentId: {}", agentId);
        }
    }

    /**
     * 处理 Agent 开始事件
     */
    private void handleAgentStarted(Long taskId, String agentId, String agentName) {
        if (agentId == null) {
            return;
        }
        try {
            Long agentBoundId = Long.parseLong(agentId);
            // 追加一个空字符串来触发元数据创建（如果尚未存在）
            cacheService.appendStreamContent(taskId, agentBoundId, agentName != null ? agentName : "Unknown", "");
            logger.info("Agent 开始诊断，taskId: {}, agentBoundId: {}, agentName: {}",
                    taskId, agentBoundId, agentName);
        } catch (NumberFormatException e) {
            logger.warn("无法解析 agentId: {}", agentId);
        }
    }

    /**
     * 处理 Agent 完成事件
     */
    private void handleAgentCompleted(Long taskId, String agentId) {
        if (agentId == null) {
            return;
        }
        try {
            Long agentBoundId = Long.parseLong(agentId);
            cacheService.markAgentEnded(taskId, agentBoundId);
            logger.info("Agent 诊断完成，taskId: {}, agentBoundId: {}", taskId, agentBoundId);
        } catch (NumberFormatException e) {
            logger.warn("无法解析 agentId: {}", agentId);
        }
    }

    /**
     * 处理 Agent 失败事件
     */
    private void handleAgentFailed(Long taskId, String agentId, String errorMessage) {
        if (agentId == null) {
            return;
        }
        try {
            Long agentBoundId = Long.parseLong(agentId);
            // 追加错误信息
            cacheService.appendStreamContent(taskId, agentBoundId, null, "\n[ERROR] " + errorMessage);
            cacheService.markAgentEnded(taskId, agentBoundId);
            logger.warn("Agent 诊断失败，taskId: {}, agentBoundId: {}, error: {}",
                    taskId, agentBoundId, errorMessage);
        } catch (NumberFormatException e) {
            logger.warn("无法解析 agentId: {}", agentId);
        }
    }

    /**
     * 处理错误事件
     */
    private void handleError(Long taskId, String errorMessage) {
        logger.error("诊断执行错误，taskId: {}, error: {}", taskId, errorMessage);
    }

    /**
     * 处理完成事件
     */
    private void handleComplete(Long taskId) {
        logger.info("诊断执行完成，taskId: {}", taskId);
    }

    /**
     * 处理 Executor 连接错误
     *
     * @param taskId       诊断任务ID
     * @param errorMessage 错误信息
     */
    public void handleExecutorError(Long taskId, String errorMessage) {
        logger.error("Executor 连接错误，taskId: {}, error: {}", taskId, errorMessage);
        // 错误处理逻辑将在 DiagnosisApplicationServiceImpl 中处理
    }
}
