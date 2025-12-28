package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import com.catface996.aiops.repository.mysql.po.topology.TopologyReportTemplatePO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 拓扑图-报告模板关联 Mapper 接口
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
    @Select("SELECT * FROM topology_2_report_template " +
            "WHERE topology_id = #{topologyId} AND report_template_id = #{reportTemplateId}")
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
    @Delete("<script>" +
            "DELETE FROM topology_2_report_template " +
            "WHERE topology_id = #{topologyId} " +
            "AND report_template_id IN " +
            "<foreach collection='reportTemplateIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
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
    @Select("<script>" +
            "SELECT trt.id, trt.topology_id, trt.report_template_id, trt.created_by, trt.created_at, " +
            "rt.name AS template_name, rt.description AS template_description, rt.category AS template_category " +
            "FROM topology_2_report_template trt " +
            "INNER JOIN report_template rt ON trt.report_template_id = rt.id AND rt.deleted = 0 " +
            "WHERE trt.topology_id = #{topologyId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (rt.name LIKE CONCAT('%', #{keyword}, '%') OR rt.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY trt.created_at DESC" +
            "</script>")
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
    @Select("<script>" +
            "SELECT rt.* FROM report_template rt " +
            "WHERE rt.deleted = 0 " +
            "AND rt.id NOT IN (" +
            "    SELECT report_template_id FROM topology_2_report_template " +
            "    WHERE topology_id = #{topologyId}" +
            ") " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (rt.name LIKE CONCAT('%', #{keyword}, '%') OR rt.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY rt.created_at DESC" +
            "</script>")
    IPage<ReportTemplatePO> selectUnboundTemplates(Page<?> page,
                                                    @Param("topologyId") Long topologyId,
                                                    @Param("keyword") String keyword);

    /**
     * 查询拓扑图已绑定的模板ID列表
     *
     * @param topologyId 拓扑图ID
     * @return 模板ID列表
     */
    @Select("SELECT report_template_id FROM topology_2_report_template " +
            "WHERE topology_id = #{topologyId}")
    List<Long> selectBoundTemplateIds(@Param("topologyId") Long topologyId);

    /**
     * 统计拓扑图绑定的模板数量
     *
     * @param topologyId 拓扑图ID
     * @return 绑定数量
     */
    @Select("SELECT COUNT(*) FROM topology_2_report_template " +
            "WHERE topology_id = #{topologyId}")
    int countByTopologyId(@Param("topologyId") Long topologyId);
}
