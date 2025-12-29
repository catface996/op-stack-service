package com.catface996.aiops.repository.mysql.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报告模板 Mapper 接口
 *
 * <p>SQL 定义在 mapper/report/ReportTemplateMapper.xml</p>
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
    ReportTemplatePO selectByName(@Param("name") String name);

    /**
     * 分页查询模板列表
     *
     * @param page     分页参数
     * @param category 模板分类筛选（可选）
     * @param keyword  关键词搜索（可选，搜索 name, description）
     * @return 分页结果
     */
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
    long countByCondition(@Param("category") String category,
                          @Param("keyword") String keyword);

    /**
     * 批量查询存在的模板ID
     *
     * @param ids 模板ID列表
     * @return 存在的模板ID列表
     */
    List<Long> selectExistingIds(@Param("ids") List<Long> ids);

    /**
     * 软删除报告模板
     *
     * @param id        模板ID
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id, @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
