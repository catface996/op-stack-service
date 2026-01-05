package com.catface996.aiops.application.api.service.diagnosis;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.diagnosis.DiagnosisTaskDTO;

import java.util.List;

/**
 * 诊断任务应用服务接口
 *
 * <p>提供诊断任务查询的应用层接口。</p>
 *
 * <p>注意：诊断任务的创建在 ExecutionApplicationService.triggerExecution() 中自动完成。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>查询诊断任务历史</li>
 *   <li>查询诊断任务详情</li>
 *   <li>查询运行中的任务</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
public interface DiagnosisApplicationService {

    // ==================== 查询诊断任务历史 ====================

    /**
     * 根据ID查询诊断任务详情（含Agent诊断过程）
     *
     * @param taskId 诊断任务ID
     * @return 诊断任务 DTO（包含 agentProcesses），如不存在返回 null
     */
    DiagnosisTaskDTO queryById(Long taskId);

    /**
     * 分页查询拓扑图的诊断任务历史
     *
     * @param topologyId 拓扑图ID
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 分页结果
     */
    PageResult<DiagnosisTaskDTO> queryByTopology(Long topologyId, int page, int size);

    // ==================== User Story 5: 查询运行中的任务 ====================

    /**
     * 查询运行中的诊断任务
     *
     * @param topologyId 拓扑图ID（可选，为null则查询所有）
     * @return 运行中的任务列表
     */
    List<DiagnosisTaskDTO> queryRunningTasks(Long topologyId);
}
