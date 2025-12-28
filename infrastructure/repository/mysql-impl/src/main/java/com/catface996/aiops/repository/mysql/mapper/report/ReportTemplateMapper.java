package com.catface996.aiops.repository.mysql.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报告模板 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplatePO> {

    /**
     * 根据名称查询模板
     *
     * @param name 模板名称
     * @return 模板信息
     */
    @Select("SELECT * FROM report_templates WHERE name = #{name} AND deleted = 0")
    ReportTemplatePO selectByName(@Param("name") String name);

    /**
     * 分页查询模板列表
     *
     * @param page     分页参数
     * @param category 模板分类筛选（可选）
     * @param keyword  关键词搜索（可选，搜索 name, description）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM report_templates " +
            "<where>" +
            "deleted = 0 " +
            "<if test='category != null and category != \"\"'>" +
            "AND category = #{category} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<ReportTemplatePO> selectPageByCondition(Page<ReportTemplatePO> page,
                                                    @Param("category") String category,
                                                    @Param("keyword") String keyword);

    /**
     * 按条件统计模板数量
     *
     * @param category 模板分类筛选（可选）
     * @param keyword  关键词搜索（可选）
     * @return 模板数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM report_templates " +
            "<where>" +
            "deleted = 0 " +
            "<if test='category != null and category != \"\"'>" +
            "AND category = #{category} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("category") String category,
                          @Param("keyword") String keyword);
}
