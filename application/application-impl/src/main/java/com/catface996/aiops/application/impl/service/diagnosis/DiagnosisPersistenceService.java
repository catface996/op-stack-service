package com.catface996.aiops.application.impl.service.diagnosis;

import com.catface996.aiops.domain.model.diagnosis.AgentDiagnosisProcess;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTask;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTaskStatus;
import com.catface996.aiops.infrastructure.cache.redis.diagnosis.DiagnosisStreamCacheService;
import com.catface996.aiops.repository.diagnosis.AgentDiagnosisProcessRepository;
import com.catface996.aiops.repository.diagnosis.DiagnosisTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 诊断数据持久化服务
 *
 * <p>负责将 Redis 中的流式诊断数据持久化到数据库。</p>
 * <p>提供同步和异步两种持久化方式。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisPersistenceService {

    private final DiagnosisTaskRepository diagnosisTaskRepository;
    private final AgentDiagnosisProcessRepository agentDiagnosisProcessRepository;
    private final DiagnosisStreamCacheService cacheService;

    /**
     * 异步处理诊断完成
     */
    @Async("diagnosisExecutor")
    public void handleCompletionAsync(Long taskId) {
        log.info("异步处理诊断完成，taskId: {}", taskId);
        try {
            persistAndComplete(taskId, DiagnosisTaskStatus.COMPLETED, null);
        } catch (Exception e) {
            log.error("诊断完成处理失败，taskId: {}, error: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, DiagnosisTaskStatus.FAILED, "持久化失败: " + e.getMessage());
        }
    }

    /**
     * 异步处理诊断错误
     */
    @Async("diagnosisExecutor")
    public void handleErrorAsync(Long taskId, String errorMessage) {
        log.info("异步处理诊断错误，taskId: {}", taskId);
        try {
            persistAndComplete(taskId, DiagnosisTaskStatus.FAILED, errorMessage);
        } catch (Exception e) {
            log.error("诊断错误处理失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 异步处理诊断取消
     */
    @Async("diagnosisExecutor")
    public void handleCancellationAsync(String runId) {
        log.info("异步处理诊断取消，runId: {}", runId);
        try {
            diagnosisTaskRepository.findByRunId(runId).ifPresent(task -> {
                persistAndComplete(task.getId(), DiagnosisTaskStatus.CANCELLED, "用户主动取消");
            });
        } catch (Exception e) {
            log.error("诊断取消处理失败，runId: {}, error: {}", runId, e.getMessage(), e);
        }
    }

    /**
     * 持久化并完成任务
     */
    @Transactional
    public void persistAndComplete(Long taskId, DiagnosisTaskStatus status, String message) {
        // 持久化已收集的流式数据
        if (cacheService.hasTaskData(taskId)) {
            persistAgentDiagnosisProcesses(taskId);
        }

        // 更新任务状态
        updateTaskStatus(taskId, status, message);

        // 清理 Redis 缓存
        cacheService.cleanupTaskData(taskId);

        log.info("诊断任务处理完成，taskId: {}, status: {}", taskId, status);
    }

    /**
     * 持久化 Agent 诊断过程
     */
    @Transactional
    public void persistAgentDiagnosisProcesses(Long taskId) {
        Set<Long> agentBoundIds = cacheService.getAgentBoundIds(taskId);
        if (agentBoundIds.isEmpty()) {
            log.warn("没有找到 Agent 诊断数据，taskId: {}", taskId);
            return;
        }

        List<AgentDiagnosisProcess> processes = new ArrayList<>();
        for (Long agentBoundId : agentBoundIds) {
            String content = cacheService.getAgentContent(taskId, agentBoundId);
            Map<String, String> meta = cacheService.getAgentMeta(taskId, agentBoundId);
            String agentName = meta.getOrDefault("agentName", "Unknown");
            LocalDateTime startedAt = cacheService.getAgentStartedAt(taskId, agentBoundId);
            LocalDateTime endedAt = cacheService.getAgentEndedAt(taskId, agentBoundId);

            AgentDiagnosisProcess process = (content == null || content.isEmpty())
                    ? AgentDiagnosisProcess.createNoOutput(taskId, agentBoundId, agentName)
                    : AgentDiagnosisProcess.create(taskId, agentBoundId, agentName);
            process.setContent(content);
            process.setStartedAt(startedAt);
            process.setEndedAt(endedAt);
            processes.add(process);
        }

        if (!processes.isEmpty()) {
            int saved = agentDiagnosisProcessRepository.batchSave(processes);
            log.info("已保存 {} 条 Agent 诊断过程记录，taskId: {}", saved, taskId);
        }
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(Long taskId, DiagnosisTaskStatus status, String errorMessage) {
        LocalDateTime completedAt = status.isTerminal() ? LocalDateTime.now() : null;
        diagnosisTaskRepository.updateStatus(taskId, status, errorMessage, completedAt);
    }

    /**
     * 创建诊断任务
     */
    @Transactional
    public DiagnosisTask createTask(Long topologyId, String userQuestion, Long operatorId) {
        DiagnosisTask task = DiagnosisTask.create(topologyId, userQuestion, operatorId);
        return diagnosisTaskRepository.save(task);
    }

    /**
     * 更新 runId
     */
    public void updateRunId(Long taskId, String runId) {
        diagnosisTaskRepository.updateRunId(taskId, runId);
    }
}
