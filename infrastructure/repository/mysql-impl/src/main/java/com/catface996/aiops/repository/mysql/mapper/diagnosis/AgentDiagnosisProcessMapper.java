package com.catface996.aiops.repository.mysql.mapper.diagnosis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.diagnosis.AgentDiagnosisProcessPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent诊断过程 Mapper 接口
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Mapper
public interface AgentDiagnosisProcessMapper extends BaseMapper<AgentDiagnosisProcessPO> {

    /**
     * 根据诊断任务ID查询所有Agent诊断过程
     *
     * @param taskId 诊断任务ID
     * @return Agent诊断过程列表
     */
    List<AgentDiagnosisProcessPO> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计诊断任务的Agent数量
     *
     * @param taskId 诊断任务ID
     * @return Agent数量
     */
    int countByTaskId(@Param("taskId") Long taskId);

    /**
     * 批量插入Agent诊断过程
     *
     * @param processes Agent诊断过程列表
     * @return 插入行数
     */
    int batchInsert(@Param("list") List<AgentDiagnosisProcessPO> processes);
}
