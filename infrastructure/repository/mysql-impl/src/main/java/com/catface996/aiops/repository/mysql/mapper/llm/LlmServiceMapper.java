package com.catface996.aiops.repository.mysql.mapper.llm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.llm.LlmServicePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LLM 服务 MyBatis Mapper
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Mapper
public interface LlmServiceMapper extends BaseMapper<LlmServicePO> {

    /**
     * 根据名称查询
     *
     * @param name 服务名称
     * @return 服务 PO
     */
    LlmServicePO findByName(@Param("name") String name);

    /**
     * 根据启用状态查询（按优先级排序）
     *
     * @param enabled 是否启用
     * @return 服务列表
     */
    List<LlmServicePO> findByEnabled(@Param("enabled") boolean enabled);

    /**
     * 查询所有并按优先级排序
     *
     * @return 服务列表
     */
    List<LlmServicePO> findAllOrderByPriority();

    /**
     * 清除所有默认标记
     */
    void clearAllDefault();

    /**
     * 设置指定服务为默认
     *
     * @param id 服务 ID
     */
    void setDefault(@Param("id") Long id);

    /**
     * 统计启用且为默认的服务数量
     *
     * @return 数量
     */
    int countEnabledDefault();
}
