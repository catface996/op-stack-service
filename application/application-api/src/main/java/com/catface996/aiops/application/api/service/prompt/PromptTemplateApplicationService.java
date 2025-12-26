package com.catface996.aiops.application.api.service.prompt;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDetailDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateVersionDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreatePromptTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.DeleteTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.ListPromptTemplatesRequest;
import com.catface996.aiops.application.api.dto.prompt.request.RollbackTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.UpdatePromptTemplateRequest;

/**
 * 提示词模板应用服务接口
 *
 * <p>提供提示词模板管理的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用领域服务完成业务逻辑</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface PromptTemplateApplicationService {

    /**
     * 创建提示词模板
     *
     * @param request 创建请求
     * @return 创建的模板 DTO
     */
    PromptTemplateDTO createPromptTemplate(CreatePromptTemplateRequest request);

    /**
     * 分页查询提示词模板列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<PromptTemplateDTO> listPromptTemplates(ListPromptTemplatesRequest request);

    /**
     * 获取模板详情（包含版本历史）
     *
     * @param templateId 模板ID
     * @return 模板详情 DTO
     */
    PromptTemplateDetailDTO getTemplateDetail(Long templateId);

    /**
     * 获取指定版本详情
     *
     * @param templateId    模板ID
     * @param versionNumber 版本号
     * @return 版本详情 DTO
     */
    PromptTemplateVersionDTO getVersionDetail(Long templateId, Integer versionNumber);

    /**
     * 更新模板内容（生成新版本）
     *
     * @param request 更新请求
     * @return 更新后的模板 DTO
     */
    PromptTemplateDTO updatePromptTemplate(UpdatePromptTemplateRequest request);

    /**
     * 回滚到历史版本
     *
     * @param request 回滚请求
     * @return 回滚后的模板 DTO
     */
    PromptTemplateDTO rollbackPromptTemplate(RollbackTemplateRequest request);

    /**
     * 删除模板（软删除）
     *
     * @param request 删除请求
     */
    void deletePromptTemplate(DeleteTemplateRequest request);
}
