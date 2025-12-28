package com.catface996.aiops.application.impl.service.report;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportsRequest;
import com.catface996.aiops.application.api.service.report.ReportApplicationService;
import com.catface996.aiops.common.enums.ReportErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.report.Report;
import com.catface996.aiops.domain.model.report.ReportStatus;
import com.catface996.aiops.domain.model.report.ReportType;
import com.catface996.aiops.repository.report.ReportRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报告应用服务实现
 *
 * <p>协调仓储层完成报告管理业务逻辑，负责 DTO 转换。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Service
public class ReportApplicationServiceImpl implements ReportApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportApplicationServiceImpl.class);

    private final ReportRepository reportRepository;
    private final TopologyRepository topologyRepository;

    public ReportApplicationServiceImpl(ReportRepository reportRepository, TopologyRepository topologyRepository) {
        this.reportRepository = reportRepository;
        this.topologyRepository = topologyRepository;
    }

    @Override
    public PageResult<ReportDTO> listReports(ListReportsRequest request) {
        logger.info("查询报告列表，type: {}, status: {}, keyword: {}, page: {}, size: {}",
                request.getType(), request.getStatus(), request.getKeyword(),
                request.getPage(), request.getSize());

        // 解析枚举类型
        ReportType type = ReportType.fromName(request.getType());
        ReportStatus status = ReportStatus.fromName(request.getStatus());

        // 查询报告列表
        List<Report> reports = reportRepository.findByCondition(
                type,
                status,
                request.getKeyword(),
                request.getSortBy(),
                request.getSortOrder(),
                request.getPage(),
                request.getSize()
        );

        // 查询总数
        long total = reportRepository.countByCondition(type, status, request.getKeyword());

        // 转换为 DTO
        List<ReportDTO> dtos = reports.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public ReportDTO getReportById(Long reportId) {
        logger.info("获取报告详情，reportId: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ReportErrorCode.REPORT_NOT_FOUND, reportId));

        return toDTO(report);
    }

    @Override
    @Transactional
    public ReportDTO createReport(CreateReportRequest request) {
        logger.info("创建报告，title: {}, type: {}, author: {}",
                request.getTitle(), request.getType(), request.getAuthor());

        // 解析并验证类型
        ReportType type = ReportType.fromName(request.getType());
        if (type == null) {
            throw new BusinessException(ReportErrorCode.INVALID_REPORT_TYPE, request.getType());
        }

        // 解析并验证状态
        ReportStatus status = ReportStatus.fromName(request.getStatus());
        if (status == null) {
            throw new BusinessException(ReportErrorCode.INVALID_REPORT_STATUS, request.getStatus());
        }

        // 验证关联的拓扑是否存在
        if (request.getTopologyId() != null && !topologyRepository.existsById(request.getTopologyId())) {
            throw new BusinessException(ReportErrorCode.TOPOLOGY_NOT_FOUND, request.getTopologyId());
        }

        // 创建报告
        Report report = Report.create(
                request.getTitle(),
                type,
                status,
                request.getAuthor(),
                request.getSummary(),
                request.getContent(),
                request.getTags(),
                request.getTopologyId()
        );

        // 保存报告
        report = reportRepository.save(report);

        return toDTO(report);
    }

    @Override
    @Transactional
    public void deleteReport(DeleteReportRequest request) {
        logger.info("删除报告，id: {}", request.getId());

        // 验证报告是否存在
        if (!reportRepository.existsById(request.getId())) {
            throw new BusinessException(ReportErrorCode.REPORT_NOT_FOUND, request.getId());
        }

        // 软删除
        reportRepository.deleteById(request.getId());
    }

    // ===== DTO 转换方法 =====

    private ReportDTO toDTO(Report report) {
        if (report == null) {
            return null;
        }

        return ReportDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .type(report.getType() != null ? report.getType().name() : null)
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .author(report.getAuthor())
                .summary(report.getSummary())
                .content(report.getContent())
                .tags(report.getTags())
                .topologyId(report.getTopologyId())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
