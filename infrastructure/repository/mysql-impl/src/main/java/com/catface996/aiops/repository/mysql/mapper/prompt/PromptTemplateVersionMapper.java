package com.catface996.aiops.repository.mysql.mapper.prompt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplateVersionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模板版本 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface PromptTemplateVersionMapper extends BaseMapper<PromptTemplateVersionPO> {

    /**
     * 根据模板ID和版本号查询版本
     *
     * @param templateId    模板ID
     * @param versionNumber 版本号
     * @return 版本信息
     */
    @Select("SELECT * FROM prompt_template_version WHERE template_id = #{templateId} AND version_number = #{versionNumber}")
    PromptTemplateVersionPO selectByTemplateIdAndVersion(@Param("templateId") Long templateId,
                                                          @Param("versionNumber") Integer versionNumber);

    /**
     * 查询模板的所有版本（按版本号降序）
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    @Select("SELECT * FROM prompt_template_version WHERE template_id = #{templateId} ORDER BY version_number DESC")
    List<PromptTemplateVersionPO> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的最新版本
     *
     * @param templateId 模板ID
     * @return 最新版本
     */
    @Select("SELECT * FROM prompt_template_version WHERE template_id = #{templateId} ORDER BY version_number DESC LIMIT 1")
    PromptTemplateVersionPO selectLatestByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的版本数量
     *
     * @param templateId 模板ID
     * @return 版本数量
     */
    @Select("SELECT COUNT(*) FROM prompt_template_version WHERE template_id = #{templateId}")
    long countByTemplateId(@Param("templateId") Long templateId);

    /**
     * 删除模板的所有版本
     *
     * @param templateId 模板ID
     * @return 删除的行数
     */
    @Select("DELETE FROM prompt_template_version WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);
}
