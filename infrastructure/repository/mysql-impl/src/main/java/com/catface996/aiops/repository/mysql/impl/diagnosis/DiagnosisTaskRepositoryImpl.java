package com.catface996.aiops.repository.mysql.impl.diagnosis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTask;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTaskStatus;
import com.catface996.aiops.repository.diagnosis.DiagnosisTaskRepository;
import com.catface996.aiops.repository.mysql.mapper.diagnosis.DiagnosisTaskMapper;
import com.catface996.aiops.repository.mysql.po.diagnosis.DiagnosisTaskPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 诊断任务仓储实现
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Repository
public class DiagnosisTaskRepositoryImpl implements DiagnosisTaskRepository {

    private final DiagnosisTaskMapper diagnosisTaskMapper;

    public DiagnosisTaskRepositoryImpl(DiagnosisTaskMapper diagnosisTaskMapper) {
        this.diagnosisTaskMapper = diagnosisTaskMapper;
    }

    @Override
    public DiagnosisTask save(DiagnosisTask task) {
        DiagnosisTaskPO po = toPO(task);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        diagnosisTaskMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public Optional<DiagnosisTask> findById(Long id) {
        DiagnosisTaskPO po = diagnosisTaskMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<DiagnosisTask> findByIdWithTopologyName(Long id) {
        DiagnosisTaskPO po = diagnosisTaskMapper.selectByIdWithTopologyName(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<DiagnosisTask> findByTopologyId(Long topologyId, int page, int size) {
        Page<DiagnosisTaskPO> pageParam = new Page<>(page, size);
        return diagnosisTaskMapper.selectPageByTopologyId(pageParam, topologyId)
                .getRecords()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByTopologyId(Long topologyId) {
        LambdaQueryWrapper<DiagnosisTaskPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiagnosisTaskPO::getTopologyId, topologyId);
        return diagnosisTaskMapper.selectCount(queryWrapper);
    }

    @Override
    public List<DiagnosisTask> findRunningTasks(Long topologyId) {
        return diagnosisTaskMapper.selectRunningTasks(topologyId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Long id, DiagnosisTaskStatus status, String errorMessage, LocalDateTime completedAt) {
        int rows = diagnosisTaskMapper.updateStatus(id, status.name(), errorMessage, completedAt);
        return rows > 0;
    }

    @Override
    public boolean updateRunId(Long id, String runId) {
        int rows = diagnosisTaskMapper.updateRunId(id, runId);
        return rows > 0;
    }

    @Override
    public boolean existsById(Long id) {
        return diagnosisTaskMapper.selectById(id) != null;
    }

    @Override
    public Optional<DiagnosisTask> findByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<DiagnosisTaskPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiagnosisTaskPO::getRunId, runId);
        DiagnosisTaskPO po = diagnosisTaskMapper.selectOne(queryWrapper);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    // ==================== 转换方法 ====================

    private DiagnosisTask toDomain(DiagnosisTaskPO po) {
        if (po == null) {
            return null;
        }
        DiagnosisTask task = DiagnosisTask.builder()
                .id(po.getId())
                .topologyId(po.getTopologyId())
                .userQuestion(po.getUserQuestion())
                .status(DiagnosisTaskStatus.valueOf(po.getStatus()))
                .errorMessage(po.getErrorMessage())
                .runId(po.getRunId())
                .completedAt(po.getCompletedAt())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .version(po.getVersion())
                .deleted(po.getDeleted() != null && po.getDeleted() == 1)
                .build();

        // 设置派生字段
        if (po.getTopologyName() != null) {
            task.setTopologyName(po.getTopologyName());
        }

        return task;
    }

    private DiagnosisTaskPO toPO(DiagnosisTask domain) {
        if (domain == null) {
            return null;
        }
        DiagnosisTaskPO po = new DiagnosisTaskPO();
        po.setId(domain.getId());
        po.setTopologyId(domain.getTopologyId());
        po.setUserQuestion(domain.getUserQuestion());
        po.setStatus(domain.getStatus() != null ? domain.getStatus().name() : DiagnosisTaskStatus.RUNNING.name());
        po.setErrorMessage(domain.getErrorMessage());
        po.setRunId(domain.getRunId());
        po.setCompletedAt(domain.getCompletedAt());
        po.setCreatedBy(domain.getCreatedBy());
        po.setVersion(domain.getVersion());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        po.setDeleted(domain.getDeleted() != null && domain.getDeleted() ? 1 : 0);
        return po;
    }
}
