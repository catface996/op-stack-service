package com.catface996.aiops.application.api.service.report;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportTemplateDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportTemplatesRequest;
import com.catface996.aiops.application.api.dto.report.request.UpdateReportTemplateRequest;

/**
 * 报告模板应用服务接口
 *
 * <p>提供报告模板管理的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用仓储层完成数据操作</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface ReportTemplateApplicationService {

    /**
     * 分页查询模板列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ReportTemplateDTO> listTemplates(ListReportTemplatesRequest request);

    /**
     * 获取模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情 DTO
     */
    ReportTemplateDTO getTemplateById(Long templateId);

    /**
     * 创建模板
     *
     * @param request 创建请求
     * @return 创建的模板 DTO
     */
    ReportTemplateDTO createTemplate(CreateReportTemplateRequest request);

    /**
     * 更新模板（使用乐观锁）
     *
     * @param request 更新请求
     * @return 更新后的模板 DTO
     */
    ReportTemplateDTO updateTemplate(UpdateReportTemplateRequest request);

    /**
     * 删除模板（软删除）
     *
     * @param request 删除请求
     */
    void deleteTemplate(DeleteReportTemplateRequest request);
}
