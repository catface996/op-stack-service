package com.catface996.aiops.repository.mysql.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceTagPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源标签 Mapper 接口
 *
 * <p>提供资源标签数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceTagMapper extends BaseMapper<ResourceTagPO> {

    /**
     * 根据资源ID查询所有标签
     *
     * @param resourceId 资源ID
     * @return 标签列表
     */
    List<ResourceTagPO> selectByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 根据标签名称查询资源ID列表
     *
     * @param tagName 标签名称
     * @return 资源ID列表
     */
    List<Long> selectResourceIdsByTagName(@Param("tagName") String tagName);

    /**
     * 删除资源的所有标签
     *
     * @param resourceId 资源ID
     * @return 删除的行数
     */
    int deleteByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 删除资源的指定标签
     *
     * @param resourceId 资源ID
     * @param tagName 标签名称
     * @return 删除的行数
     */
    int deleteByResourceIdAndTagName(@Param("resourceId") Long resourceId,
                                      @Param("tagName") String tagName);

    /**
     * 检查资源是否已有指定标签
     *
     * @param resourceId 资源ID
     * @param tagName 标签名称
     * @return 存在返回1，否则返回0
     */
    int existsByResourceIdAndTagName(@Param("resourceId") Long resourceId,
                                      @Param("tagName") String tagName);

    /**
     * 获取热门标签（按使用次数排序）
     *
     * @param limit 返回数量限制
     * @return 标签名称列表
     */
    List<String> selectPopularTags(@Param("limit") int limit);
}
