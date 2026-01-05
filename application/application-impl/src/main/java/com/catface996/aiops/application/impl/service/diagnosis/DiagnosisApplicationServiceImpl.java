package com.catface996.aiops.application.impl.service.diagnosis;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.diagnosis.AgentDiagnosisProcessDTO;
import com.catface996.aiops.application.api.dto.diagnosis.DiagnosisTaskDTO;
import com.catface996.aiops.application.api.service.diagnosis.DiagnosisApplicationService;
import com.catface996.aiops.domain.model.diagnosis.AgentDiagnosisProcess;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTask;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTaskStatus;
import com.catface996.aiops.repository.diagnosis.AgentDiagnosisProcessRepository;
import com.catface996.aiops.repository.diagnosis.DiagnosisTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 诊断任务应用服务实现
 *
 * <p>提供诊断任务查询功能。</p>
 *
 * <p>注意：诊断任务的创建、流式数据收集和持久化逻辑
 * 已整合到 ExecutionApplicationServiceImpl.triggerExecution() 中。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Service
public class DiagnosisApplicationServiceImpl implements DiagnosisApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosisApplicationServiceImpl.class);

    private final DiagnosisTaskRepository diagnosisTaskRepository;
    private final AgentDiagnosisProcessRepository agentDiagnosisProcessRepository;

    public DiagnosisApplicationServiceImpl(DiagnosisTaskRepository diagnosisTaskRepository,
                                           AgentDiagnosisProcessRepository agentDiagnosisProcessRepository) {
        this.diagnosisTaskRepository = diagnosisTaskRepository;
        this.agentDiagnosisProcessRepository = agentDiagnosisProcessRepository;
    }

    // ==================== 查询诊断任务历史 ====================

    @Override
    public DiagnosisTaskDTO queryById(Long taskId) {
        logger.info("查询诊断任务详情，taskId: {}", taskId);

        return diagnosisTaskRepository.findByIdWithTopologyName(taskId)
                .map(task -> {
                    DiagnosisTaskDTO dto = toDTO(task);
                    // 加载Agent诊断过程
                    List<AgentDiagnosisProcess> processes = agentDiagnosisProcessRepository.findByTaskId(taskId);
                    dto.setAgentProcesses(processes.stream()
                            .map(this::toProcessDTO)
                            .collect(Collectors.toList()));
                    dto.setAgentCount(processes.size());
                    return dto;
                })
                .orElse(null);
    }

    @Override
    public PageResult<DiagnosisTaskDTO> queryByTopology(Long topologyId, int page, int size) {
        logger.info("分页查询诊断任务历史，topologyId: {}, page: {}, size: {}", topologyId, page, size);

        List<DiagnosisTask> tasks = diagnosisTaskRepository.findByTopologyId(topologyId, page, size);
        long total = diagnosisTaskRepository.countByTopologyId(topologyId);

        List<DiagnosisTaskDTO> dtos = tasks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, page, size, total);
    }

    // ==================== 查询运行中的任务 ====================

    @Override
    public List<DiagnosisTaskDTO> queryRunningTasks(Long topologyId) {
        logger.info("查询运行中的诊断任务，topologyId: {}", topologyId);

        return diagnosisTaskRepository.findRunningTasks(topologyId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== 转换方法 ====================

    private DiagnosisTaskDTO toDTO(DiagnosisTask task) {
        if (task == null) {
            return null;
        }
        return DiagnosisTaskDTO.builder()
                .id(task.getId())
                .topologyId(task.getTopologyId())
                .topologyName(task.getTopologyName())
                .userQuestion(task.getUserQuestion())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .statusDisplay(getStatusDisplay(task.getStatus()))
                .errorMessage(task.getErrorMessage())
                .runId(task.getRunId())
                .agentCount(task.getAgentCount())
                .completedAt(task.getCompletedAt())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .durationSeconds(task.getRunningDurationSeconds())
                .build();
    }

    private AgentDiagnosisProcessDTO toProcessDTO(AgentDiagnosisProcess process) {
        if (process == null) {
            return null;
        }
        return AgentDiagnosisProcessDTO.builder()
                .id(process.getId())
                .taskId(process.getTaskId())
                .agentBoundId(process.getAgentBoundId())
                .agentName(process.getAgentName())
                .content(process.getContent())
                .startedAt(process.getStartedAt())
                .endedAt(process.getEndedAt())
                .durationSeconds(process.getDurationSeconds())
                .contentLength(process.getContentLength())
                .hasOutput(process.hasOutput())
                .createdAt(process.getCreatedAt())
                .build();
    }

    private String getStatusDisplay(DiagnosisTaskStatus status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case RUNNING -> "运行中";
            case COMPLETED -> "已完成";
            case FAILED -> "失败";
            case TIMEOUT -> "超时";
            case CANCELLED -> "已取消";
        };
    }
}
