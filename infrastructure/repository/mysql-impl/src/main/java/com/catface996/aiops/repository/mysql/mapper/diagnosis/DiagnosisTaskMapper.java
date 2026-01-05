package com.catface996.aiops.repository.mysql.mapper.diagnosis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.diagnosis.DiagnosisTaskPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 诊断任务 Mapper 接口
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Mapper
public interface DiagnosisTaskMapper extends BaseMapper<DiagnosisTaskPO> {

    /**
     * 根据ID查询诊断任务（带拓扑图名称）
     *
     * @param id 诊断任务ID
     * @return 诊断任务信息
     */
    DiagnosisTaskPO selectByIdWithTopologyName(@Param("id") Long id);

    /**
     * 分页查询拓扑图的诊断任务历史（带Agent数量统计）
     *
     * @param page       分页参数
     * @param topologyId 拓扑图ID
     * @return 分页结果
     */
    IPage<DiagnosisTaskPO> selectPageByTopologyId(Page<DiagnosisTaskPO> page,
                                                   @Param("topologyId") Long topologyId);

    /**
     * 查询运行中的诊断任务
     *
     * @param topologyId 拓扑图ID（可选，为null则查询所有）
     * @return 运行中的任务列表
     */
    List<DiagnosisTaskPO> selectRunningTasks(@Param("topologyId") Long topologyId);

    /**
     * 更新诊断任务状态
     *
     * @param id           任务ID
     * @param status       新状态
     * @param errorMessage 错误信息（可选）
     * @param completedAt  完成时间（可选）
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage,
                     @Param("completedAt") java.time.LocalDateTime completedAt);

    /**
     * 更新executor运行ID
     *
     * @param id    任务ID
     * @param runId executor运行ID
     * @return 影响行数
     */
    int updateRunId(@Param("id") Long id, @Param("runId") String runId);
}
