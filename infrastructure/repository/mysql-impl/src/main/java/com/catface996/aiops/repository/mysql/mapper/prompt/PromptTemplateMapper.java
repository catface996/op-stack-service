package com.catface996.aiops.repository.mysql.mapper.prompt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 提示词模板 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplatePO> {

    /**
     * 根据名称查询模板
     *
     * @param name 模板名称
     * @return 模板信息
     */
    @Select("SELECT * FROM prompt_template WHERE name = #{name} AND deleted = 0")
    PromptTemplatePO selectByName(@Param("name") String name);

    /**
     * 根据ID查询模板（带用途信息和当前版本内容）
     *
     * @param id 模板ID
     * @return 模板信息
     */
    @Select("SELECT pt.*, tu.name AS usage_name, ptv.content " +
            "FROM prompt_template pt " +
            "LEFT JOIN template_usage tu ON pt.usage_id = tu.id AND tu.deleted = 0 " +
            "LEFT JOIN prompt_template_version ptv ON pt.id = ptv.template_id AND pt.current_version = ptv.version_number " +
            "WHERE pt.id = #{id} AND pt.deleted = 0")
    PromptTemplatePO selectByIdWithDetail(@Param("id") Long id);

    /**
     * 分页查询模板（带用途信息）
     *
     * @param page    分页参数
     * @param keyword 关键词模糊查询（可选，搜索名称和描述）
     * @param usageId 用途ID筛选（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT pt.*, tu.name AS usage_name " +
            "FROM prompt_template pt " +
            "LEFT JOIN template_usage tu ON pt.usage_id = tu.id AND tu.deleted = 0 " +
            "<where>" +
            "pt.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (pt.name LIKE CONCAT('%', #{keyword}, '%') OR pt.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='usageId != null'>" +
            "AND pt.usage_id = #{usageId} " +
            "</if>" +
            "</where>" +
            "ORDER BY pt.created_at DESC" +
            "</script>")
    IPage<PromptTemplatePO> selectPageWithUsage(Page<PromptTemplatePO> page,
                                                 @Param("keyword") String keyword,
                                                 @Param("usageId") Long usageId);

    /**
     * 按条件统计模板数量
     *
     * @param keyword 关键词模糊查询（可选）
     * @param usageId 用途ID筛选（可选）
     * @return 模板数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM prompt_template pt " +
            "<where>" +
            "pt.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (pt.name LIKE CONCAT('%', #{keyword}, '%') OR pt.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='usageId != null'>" +
            "AND pt.usage_id = #{usageId} " +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("keyword") String keyword,
                          @Param("usageId") Long usageId);
}
