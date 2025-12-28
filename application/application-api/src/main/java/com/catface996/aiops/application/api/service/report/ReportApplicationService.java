package com.catface996.aiops.application.api.service.report;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportsRequest;

/**
 * 报告应用服务接口
 *
 * <p>提供报告管理的应用层接口，协调领域层完成业务逻辑。</p>
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
public interface ReportApplicationService {

    /**
     * 分页查询报告列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ReportDTO> listReports(ListReportsRequest request);

    /**
     * 获取报告详情
     *
     * @param reportId 报告ID
     * @return 报告详情 DTO
     */
    ReportDTO getReportById(Long reportId);

    /**
     * 创建报告
     *
     * @param request 创建请求
     * @return 创建的报告 DTO
     */
    ReportDTO createReport(CreateReportRequest request);

    /**
     * 删除报告（软删除）
     *
     * @param request 删除请求
     */
    void deleteReport(DeleteReportRequest request);
}
