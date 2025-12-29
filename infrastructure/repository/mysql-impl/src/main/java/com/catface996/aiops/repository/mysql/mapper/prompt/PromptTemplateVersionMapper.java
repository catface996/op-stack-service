package com.catface996.aiops.repository.mysql.mapper.prompt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplateVersionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板版本 Mapper 接口
 *
 * <p>SQL 定义在 mapper/prompt/PromptTemplateVersionMapper.xml</p>
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
    PromptTemplateVersionPO selectByTemplateIdAndVersion(@Param("templateId") Long templateId,
                                                          @Param("versionNumber") Integer versionNumber);

    /**
     * 查询模板的所有版本（按版本号降序）
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    List<PromptTemplateVersionPO> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的最新版本
     *
     * @param templateId 模板ID
     * @return 最新版本
     */
    PromptTemplateVersionPO selectLatestByTemplateId(@Param("templateId") Long templateId);

    /**
     * 查询模板的版本数量
     *
     * @param templateId 模板ID
     * @return 版本数量
     */
    long countByTemplateId(@Param("templateId") Long templateId);

    /**
     * 删除模板的所有版本
     *
     * @param templateId 模板ID
     * @return 删除的行数
     */
    int deleteByTemplateId(@Param("templateId") Long templateId);
}
