package com.catface996.aiops.repository.mysql.mapper.prompt;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.prompt.TemplateUsagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 模板用途 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface TemplateUsageMapper extends BaseMapper<TemplateUsagePO> {

    /**
     * 根据编码查询用途
     *
     * @param code 用途编码
     * @return 用途信息
     */
    @Select("SELECT * FROM template_usage WHERE code = #{code} AND deleted = 0")
    TemplateUsagePO selectByCode(@Param("code") String code);

    /**
     * 根据名称查询用途
     *
     * @param name 用途名称
     * @return 用途信息
     */
    @Select("SELECT * FROM template_usage WHERE name = #{name} AND deleted = 0")
    TemplateUsagePO selectByName(@Param("name") String name);

    /**
     * 统计使用该用途的模板数量
     *
     * @param usageId 用途ID
     * @return 使用该用途的模板数量
     */
    @Select("SELECT COUNT(*) FROM prompt_template WHERE usage_id = #{usageId} AND deleted = 0")
    long countTemplatesByUsageId(@Param("usageId") Long usageId);
}
