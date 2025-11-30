package com.catface996.aiops.repository.mysql.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourcePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源 Mapper 接口
 *
 * <p>提供资源数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceMapper extends BaseMapper<ResourcePO> {

    /**
     * 根据名称和类型ID查询资源
     *
     * @param name 资源名称
     * @param resourceTypeId 资源类型ID
     * @return 资源PO对象，如果不存在返回null
     */
    ResourcePO selectByNameAndTypeId(@Param("name") String name,
                                      @Param("resourceTypeId") Long resourceTypeId);

    /**
     * 分页查询资源列表（带条件过滤）
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 资源列表
     */
    List<ResourcePO> selectByCondition(@Param("resourceTypeId") Long resourceTypeId,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * 按条件统计资源数量
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 资源数量
     */
    long countByCondition(@Param("resourceTypeId") Long resourceTypeId,
                          @Param("status") String status,
                          @Param("keyword") String keyword);

    /**
     * 根据创建者ID查询资源
     *
     * @param createdBy 创建者ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 资源列表
     */
    List<ResourcePO> selectByCreatedBy(@Param("createdBy") Long createdBy,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * 更新资源状态（带乐观锁）
     *
     * @param id 资源ID
     * @param status 新状态
     * @param version 当前版本号
     * @return 更新的行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("version") Integer version);
}
