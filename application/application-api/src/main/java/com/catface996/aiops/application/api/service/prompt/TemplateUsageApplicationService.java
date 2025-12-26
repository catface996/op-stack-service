package com.catface996.aiops.application.api.service.prompt;

import com.catface996.aiops.application.api.dto.prompt.TemplateUsageDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreateTemplateUsageRequest;

import java.util.List;

/**
 * 模板用途应用服务接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface TemplateUsageApplicationService {

    /**
     * 创建用途
     *
     * @param request 创建请求
     * @return 创建的用途 DTO
     */
    TemplateUsageDTO createUsage(CreateTemplateUsageRequest request);

    /**
     * 获取所有用途
     *
     * @return 用途列表
     */
    List<TemplateUsageDTO> listUsages();

    /**
     * 删除用途
     *
     * @param usageId 用途ID
     */
    void deleteUsage(Long usageId);
}
