package com.catface996.aiops.repository.diagnosis;

import com.catface996.aiops.domain.model.diagnosis.DiagnosisTask;
import com.catface996.aiops.domain.model.diagnosis.DiagnosisTaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 诊断任务仓储接口
 *
 * <p>提供诊断任务实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
public interface DiagnosisTaskRepository {

    /**
     * 保存诊断任务
     *
     * @param task 诊断任务实体
     * @return 保存后的实体（含ID）
     */
    DiagnosisTask save(DiagnosisTask task);

    /**
     * 根据ID查询诊断任务
     *
     * @param id 诊断任务ID
     * @return 诊断任务实体
     */
    Optional<DiagnosisTask> findById(Long id);

    /**
     * 根据ID查询诊断任务（带拓扑图名称）
     *
     * @param id 诊断任务ID
     * @return 诊断任务实体
     */
    Optional<DiagnosisTask> findByIdWithTopologyName(Long id);

    /**
     * 分页查询拓扑图的诊断任务历史
     *
     * @param topologyId 拓扑图ID
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 诊断任务列表（带Agent数量统计）
     */
    List<DiagnosisTask> findByTopologyId(Long topologyId, int page, int size);

    /**
     * 统计拓扑图的诊断任务数量
     *
     * @param topologyId 拓扑图ID
     * @return 任务数量
     */
    long countByTopologyId(Long topologyId);

    /**
     * 查询运行中的诊断任务
     *
     * @param topologyId 拓扑图ID（可选，为null则查询所有）
     * @return 运行中的任务列表
     */
    List<DiagnosisTask> findRunningTasks(Long topologyId);

    /**
     * 更新诊断任务状态
     *
     * @param id           任务ID
     * @param status       新状态
     * @param errorMessage 错误信息（可选）
     * @param completedAt  完成时间（可选）
     * @return 更新是否成功
     */
    boolean updateStatus(Long id, DiagnosisTaskStatus status, String errorMessage, LocalDateTime completedAt);

    /**
     * 更新executor运行ID
     *
     * @param id    任务ID
     * @param runId executor运行ID
     * @return 更新是否成功
     */
    boolean updateRunId(Long id, String runId);

    /**
     * 检查诊断任务是否存在
     *
     * @param id 任务ID
     * @return true if task exists
     */
    boolean existsById(Long id);

    /**
     * 根据 executor runId 查询诊断任务
     *
     * @param runId executor 运行ID
     * @return 诊断任务实体
     */
    Optional<DiagnosisTask> findByRunId(String runId);
}
