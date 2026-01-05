package com.catface996.aiops.repository.mysql.impl.diagnosis;

import com.catface996.aiops.domain.model.diagnosis.AgentDiagnosisProcess;
import com.catface996.aiops.repository.diagnosis.AgentDiagnosisProcessRepository;
import com.catface996.aiops.repository.mysql.mapper.diagnosis.AgentDiagnosisProcessMapper;
import com.catface996.aiops.repository.mysql.po.diagnosis.AgentDiagnosisProcessPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent诊断过程仓储实现
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Repository
public class AgentDiagnosisProcessRepositoryImpl implements AgentDiagnosisProcessRepository {

    private final AgentDiagnosisProcessMapper agentDiagnosisProcessMapper;

    public AgentDiagnosisProcessRepositoryImpl(AgentDiagnosisProcessMapper agentDiagnosisProcessMapper) {
        this.agentDiagnosisProcessMapper = agentDiagnosisProcessMapper;
    }

    @Override
    public int batchSave(List<AgentDiagnosisProcess> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0;
        }
        List<AgentDiagnosisProcessPO> poList = processes.stream()
                .map(this::toPO)
                .collect(Collectors.toList());
        return agentDiagnosisProcessMapper.batchInsert(poList);
    }

    @Override
    public List<AgentDiagnosisProcess> findByTaskId(Long taskId) {
        return agentDiagnosisProcessMapper.selectByTaskId(taskId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int countByTaskId(Long taskId) {
        return agentDiagnosisProcessMapper.countByTaskId(taskId);
    }

    // ==================== 转换方法 ====================

    private AgentDiagnosisProcess toDomain(AgentDiagnosisProcessPO po) {
        if (po == null) {
            return null;
        }
        AgentDiagnosisProcess process = AgentDiagnosisProcess.builder()
                .id(po.getId())
                .taskId(po.getTaskId())
                .agentBoundId(po.getAgentBoundId())
                .agentName(po.getAgentName())
                .content(po.getContent())
                .startedAt(po.getStartedAt())
                .endedAt(po.getEndedAt())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted() != null && po.getDeleted() == 1)
                .build();
        return process;
    }

    private AgentDiagnosisProcessPO toPO(AgentDiagnosisProcess domain) {
        if (domain == null) {
            return null;
        }
        AgentDiagnosisProcessPO po = new AgentDiagnosisProcessPO();
        po.setId(domain.getId());
        po.setTaskId(domain.getTaskId());
        po.setAgentBoundId(domain.getAgentBoundId());
        po.setAgentName(domain.getAgentName());
        po.setContent(domain.getContent());
        po.setStartedAt(domain.getStartedAt());
        po.setEndedAt(domain.getEndedAt());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        po.setDeleted(domain.getDeleted() != null && domain.getDeleted() ? 1 : 0);
        return po;
    }
}
