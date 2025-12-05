package com.catface996.aiops.domain.service.llm;

import com.catface996.aiops.domain.model.llm.LlmService;

/**
 * LLM 服务领域服务接口
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
public interface LlmServiceDomainService {

    /**
     * 创建 LLM 服务
     *
     * @param llmService 服务领域对象
     * @return 创建后的服务
     */
    LlmService create(LlmService llmService);

    /**
     * 更新 LLM 服务
     *
     * @param id 服务 ID
     * @param llmService 更新的服务信息
     * @return 更新后的服务
     */
    LlmService update(Long id, LlmService llmService);

    /**
     * 检查名称是否存在
     *
     * @param name 服务名称
     * @param excludeId 排除的 ID（更新时使用）
     * @return 是否存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 更新服务状态（启用/禁用）
     *
     * @param id 服务 ID
     * @param enabled 是否启用
     * @return 更新后的服务
     */
    LlmService updateStatus(Long id, boolean enabled);

    /**
     * 删除服务
     *
     * @param id 服务 ID
     * @param force 是否强制删除
     */
    void delete(Long id, boolean force);

    /**
     * 设置为默认服务
     *
     * @param id 服务 ID
     * @return 更新后的服务
     */
    LlmService setDefault(Long id);
}
