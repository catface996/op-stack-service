package com.catface996.aiops.repository.diagnosis;

import com.catface996.aiops.domain.model.diagnosis.AgentDiagnosisProcess;

import java.util.List;

/**
 * Agent诊断过程仓储接口
 *
 * <p>提供Agent诊断过程实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
public interface AgentDiagnosisProcessRepository {

    /**
     * 批量保存Agent诊断过程
     *
     * @param processes Agent诊断过程列表
     * @return 保存的记录数
     */
    int batchSave(List<AgentDiagnosisProcess> processes);

    /**
     * 根据诊断任务ID查询所有Agent诊断过程
     *
     * @param taskId 诊断任务ID
     * @return Agent诊断过程列表
     */
    List<AgentDiagnosisProcess> findByTaskId(Long taskId);

    /**
     * 统计诊断任务的Agent数量
     *
     * @param taskId 诊断任务ID
     * @return Agent数量
     */
    int countByTaskId(Long taskId);
}
