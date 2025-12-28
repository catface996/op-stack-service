package com.catface996.aiops.repository.mysql.impl.topology;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.mapper.topology.TopologyReportTemplateMapper;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import com.catface996.aiops.repository.mysql.po.topology.TopologyReportTemplatePO;
import com.catface996.aiops.repository.topology2.TopologyReportTemplateRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拓扑图-报告模板关联仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Repository
public class TopologyReportTemplateRepositoryImpl implements TopologyReportTemplateRepository {

    private final TopologyReportTemplateMapper topologyReportTemplateMapper;

    public TopologyReportTemplateRepositoryImpl(TopologyReportTemplateMapper topologyReportTemplateMapper) {
        this.topologyReportTemplateMapper = topologyReportTemplateMapper;
    }

    @Override
    public void bind(Long topologyId, Long reportTemplateId, Long createdBy) {
        TopologyReportTemplatePO po = new TopologyReportTemplatePO();
        po.setTopologyId(topologyId);
        po.setReportTemplateId(reportTemplateId);
        po.setCreatedBy(createdBy);
        po.setCreatedAt(LocalDateTime.now());
        topologyReportTemplateMapper.insert(po);
    }

    @Override
    public int bindBatch(Long topologyId, List<Long> reportTemplateIds, Long createdBy) {
        int successCount = 0;
        for (Long reportTemplateId : reportTemplateIds) {
            if (!existsByTopologyIdAndTemplateId(topologyId, reportTemplateId)) {
                bind(topologyId, reportTemplateId, createdBy);
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public void unbind(Long topologyId, Long reportTemplateId) {
        topologyReportTemplateMapper.batchDelete(topologyId, List.of(reportTemplateId));
    }

    @Override
    public int unbindBatch(Long topologyId, List<Long> reportTemplateIds) {
        return topologyReportTemplateMapper.batchDelete(topologyId, reportTemplateIds);
    }

    @Override
    public boolean existsByTopologyIdAndTemplateId(Long topologyId, Long reportTemplateId) {
        return topologyReportTemplateMapper.selectByTopologyIdAndTemplateId(topologyId, reportTemplateId) != null;
    }

    @Override
    public List<Long> findBoundTemplateIds(Long topologyId) {
        return topologyReportTemplateMapper.selectBoundTemplateIds(topologyId);
    }

    @Override
    public int countByTopologyId(Long topologyId) {
        return topologyReportTemplateMapper.countByTopologyId(topologyId);
    }

    @Override
    public PageResult<BoundTemplateInfo> findBoundTemplates(Long topologyId, String keyword, int page, int size) {
        Page<TopologyReportTemplatePO> pageParam = new Page<>(page, size);
        IPage<TopologyReportTemplatePO> result = topologyReportTemplateMapper.selectBoundTemplates(pageParam, topologyId, keyword);

        List<BoundTemplateInfo> content = result.getRecords().stream()
                .map(po -> new BoundTemplateInfo(
                        po.getId(),
                        po.getTopologyId(),
                        po.getReportTemplateId(),
                        po.getTemplateName(),
                        po.getTemplateDescription(),
                        po.getTemplateCategory(),
                        po.getCreatedAt(),
                        po.getCreatedBy()
                ))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) result.getTotal() / size);
        return new PageResult<>(
                content,
                page,
                size,
                result.getTotal(),
                totalPages,
                page == 1,
                page >= totalPages
        );
    }

    @Override
    public PageResult<UnboundTemplateInfo> findUnboundTemplates(Long topologyId, String keyword, int page, int size) {
        Page<ReportTemplatePO> pageParam = new Page<>(page, size);
        IPage<ReportTemplatePO> result = topologyReportTemplateMapper.selectUnboundTemplates(pageParam, topologyId, keyword);

        List<UnboundTemplateInfo> content = result.getRecords().stream()
                .map(po -> new UnboundTemplateInfo(
                        po.getId(),
                        po.getName(),
                        po.getDescription(),
                        po.getCategory()
                ))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) result.getTotal() / size);
        return new PageResult<>(
                content,
                page,
                size,
                result.getTotal(),
                totalPages,
                page == 1,
                page >= totalPages
        );
    }
}
