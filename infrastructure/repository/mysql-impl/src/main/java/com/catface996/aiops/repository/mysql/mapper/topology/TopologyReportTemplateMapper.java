package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import com.catface996.aiops.repository.mysql.po.topology.TopologyReportTemplatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拓扑图-报告模板关联 Mapper 接口
 *
 * <p>SQL 定义在 mapper/topology/TopologyReportTemplateMapper.xml</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Mapper
public interface TopologyReportTemplateMapper extends BaseMapper<TopologyReportTemplatePO> {

    /**
     * 根据拓扑图ID和报告模板ID查询关联记录
     *
     * @param topologyId       拓扑图ID
     * @param reportTemplateId 报告模板ID
     * @return 关联记录
     */
    TopologyReportTemplatePO selectByTopologyIdAndTemplateId(
            @Param("topologyId") Long topologyId,
            @Param("reportTemplateId") Long reportTemplateId);

    /**
     * 批量物理删除关联记录
     *
     * @param topologyId        拓扑图ID
     * @param reportTemplateIds 报告模板ID列表
     * @return 删除的记录数
     */
    int batchDelete(@Param("topologyId") Long topologyId,
                    @Param("reportTemplateIds") List<Long> reportTemplateIds);

    /**
     * 分页查询已绑定的报告模板（带模板详情）
     *
     * @param page       分页参数
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选，模糊匹配模板名称和描述）
     * @return 已绑定的报告模板分页结果
     */
    IPage<TopologyReportTemplatePO> selectBoundTemplates(Page<?> page,
                                                          @Param("topologyId") Long topologyId,
                                                          @Param("keyword") String keyword);

    /**
     * 分页查询未绑定的报告模板
     *
     * @param page       分页参数
     * @param topologyId 拓扑图ID
     * @param keyword    搜索关键词（可选，模糊匹配模板名称和描述）
     * @return 未绑定的报告模板分页结果
     */
    IPage<ReportTemplatePO> selectUnboundTemplates(Page<?> page,
                                                    @Param("topologyId") Long topologyId,
                                                    @Param("keyword") String keyword);

    /**
     * 查询拓扑图已绑定的模板ID列表
     *
     * @param topologyId 拓扑图ID
     * @return 模板ID列表
     */
    List<Long> selectBoundTemplateIds(@Param("topologyId") Long topologyId);

    /**
     * 统计拓扑图绑定的模板数量
     *
     * @param topologyId 拓扑图ID
     * @return 绑定数量
     */
    int countByTopologyId(@Param("topologyId") Long topologyId);
}
