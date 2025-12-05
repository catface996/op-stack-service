package com.catface996.aiops.application.api.service.llm;

import com.catface996.aiops.application.api.dto.llm.CreateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.LlmServiceDTO;
import com.catface996.aiops.application.api.dto.llm.UpdateLlmServiceCommand;

import java.util.List;

/**
 * LLM 服务应用服务接口
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
public interface LlmServiceApplicationService {

    /**
     * 获取服务列表
     *
     * @param enabledOnly 是否只返回启用的服务
     * @return 服务列表
     */
    List<LlmServiceDTO> list(boolean enabledOnly);

    /**
     * 根据 ID 获取服务详情
     *
     * @param id 服务 ID
     * @return 服务详情
     */
    LlmServiceDTO getById(Long id);

    /**
     * 创建服务
     *
     * @param command 创建命令
     * @return 创建后的服务
     */
    LlmServiceDTO create(CreateLlmServiceCommand command);

    /**
     * 更新服务
     *
     * @param id 服务 ID
     * @param command 更新命令
     * @return 更新后的服务
     */
    LlmServiceDTO update(Long id, UpdateLlmServiceCommand command);

    /**
     * 删除服务
     *
     * @param id 服务 ID
     * @param force 是否强制删除
     */
    void delete(Long id, boolean force);

    /**
     * 更新服务状态
     *
     * @param id 服务 ID
     * @param enabled 是否启用
     * @return 更新后的服务
     */
    LlmServiceDTO updateStatus(Long id, boolean enabled);

    /**
     * 设置为默认服务
     *
     * @param id 服务 ID
     * @return 更新后的服务
     */
    LlmServiceDTO setDefault(Long id);
}
