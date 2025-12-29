package com.catface996.aiops.repository.mysql.mapper.prompt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 提示词模板 Mapper 接口
 *
 * <p>SQL 定义在 mapper/prompt/PromptTemplateMapper.xml</p>
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
    PromptTemplatePO selectByName(@Param("name") String name);

    /**
     * 根据ID查询模板（带用途信息和当前版本内容）
     *
     * @param id 模板ID
     * @return 模板信息
     */
    PromptTemplatePO selectByIdWithDetail(@Param("id") Long id);

    /**
     * 分页查询模板（带用途信息）
     *
     * @param page    分页参数
     * @param keyword 关键词模糊查询（可选，搜索名称和描述）
     * @param usageId 用途ID筛选（可选）
     * @return 分页结果
     */
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
    long countByCondition(@Param("keyword") String keyword,
                          @Param("usageId") Long usageId);

    /**
     * 软删除模板
     *
     * @param id        模板ID
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id, @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
