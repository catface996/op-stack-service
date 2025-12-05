package com.catface996.aiops.repository.llm;

import com.catface996.aiops.repository.llm.entity.LlmServiceEntity;

import java.util.List;
import java.util.Optional;

/**
 * LLM 服务仓储接口
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
public interface LlmServiceRepository {

    /**
     * 根据 ID 查询服务
     *
     * @param id 服务 ID
     * @return 服务实体
     */
    Optional<LlmServiceEntity> findById(Long id);

    /**
     * 查询所有服务（按优先级排序）
     *
     * @return 服务列表
     */
    List<LlmServiceEntity> findAll();

    /**
     * 根据启用状态查询服务（按优先级排序）
     *
     * @param enabled 是否启用
     * @return 服务列表
     */
    List<LlmServiceEntity> findByEnabled(boolean enabled);

    /**
     * 保存服务
     *
     * @param entity 服务实体
     * @return 保存后的实体
     */
    LlmServiceEntity save(LlmServiceEntity entity);

    /**
     * 根据 ID 删除服务
     *
     * @param id 服务 ID
     */
    void deleteById(Long id);

    /**
     * 根据名称查询服务
     *
     * @param name 服务名称
     * @return 服务实体
     */
    Optional<LlmServiceEntity> findByName(String name);

    /**
     * 清除所有服务的默认标记
     */
    void clearAllDefault();

    /**
     * 设置指定服务为默认
     *
     * @param id 服务 ID
     */
    void setDefault(Long id);

    /**
     * 统计启用且为默认的服务数量
     *
     * @return 数量
     */
    int countEnabledDefault();
}
