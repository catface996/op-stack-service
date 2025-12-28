package com.catface996.aiops.repository.mysql.mapper.relationship;

import com.catface996.aiops.repository.mysql.po.relationship.RelationshipPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源关系 Mapper 接口
 *
 * <p>提供资源关系数据的数据库访问操作</p>
 * <p>不继承 MyBatis-Plus BaseMapper，避免自动生成SQL的表名问题</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public interface RelationshipMapper {

    /**
     * 插入关系记录
     *
     * @param relationship 关系对象
     * @return 插入的行数
     */
    int insert(RelationshipPO relationship);

    /**
     * 根据ID查询关系
     *
     * @param id 关系ID
     * @return 关系对象
     */
    RelationshipPO selectById(@Param("id") Long id);

    /**
     * 根据ID更新关系
     *
     * @param relationship 关系对象
     * @return 更新的行数
     */
    int updateById(RelationshipPO relationship);

    /**
     * 根据ID删除关系
     *
     * @param id 关系ID
     * @return 删除的行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据源资源ID查询关系（下游依赖查询）
     *
     * @param sourceResourceId 源资源ID
     * @return 关系列表
     */
    List<RelationshipPO> selectBySourceResourceId(@Param("sourceResourceId") Long sourceResourceId);

    /**
     * 根据目标资源ID查询关系（上游依赖查询）
     *
     * @param targetResourceId 目标资源ID
     * @return 关系列表
     */
    List<RelationshipPO> selectByTargetResourceId(@Param("targetResourceId") Long targetResourceId);

    /**
     * 分页查询关系列表（支持多条件筛选）
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param relationshipType 关系类型（可选）
     * @param status 关系状态（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 关系列表
     */
    List<RelationshipPO> selectByConditions(@Param("sourceResourceId") Long sourceResourceId,
                                             @Param("targetResourceId") Long targetResourceId,
                                             @Param("relationshipType") String relationshipType,
                                             @Param("status") String status,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    /**
     * 按条件统计关系数量
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param relationshipType 关系类型（可选）
     * @param status 关系状态（可选）
     * @return 关系数量
     */
    long countByConditions(@Param("sourceResourceId") Long sourceResourceId,
                           @Param("targetResourceId") Long targetResourceId,
                           @Param("relationshipType") String relationshipType,
                           @Param("status") String status);

    /**
     * 检查关系是否已存在
     *
     * @param sourceResourceId 源资源ID
     * @param targetResourceId 目标资源ID
     * @param relationshipType 关系类型
     * @return 存在返回1，不存在返回0
     */
    int existsBySourceAndTargetAndType(@Param("sourceResourceId") Long sourceResourceId,
                                        @Param("targetResourceId") Long targetResourceId,
                                        @Param("relationshipType") String relationshipType);

    /**
     * 根据源资源ID、目标资源ID和类型删除关系
     *
     * @param sourceResourceId 源资源ID
     * @param targetResourceId 目标资源ID
     * @param relationshipType 关系类型
     * @return 删除的行数
     */
    int deleteBySourceAndTargetAndType(@Param("sourceResourceId") Long sourceResourceId,
                                        @Param("targetResourceId") Long targetResourceId,
                                        @Param("relationshipType") String relationshipType);

    /**
     * 删除与指定资源关联的所有关系（级联删除）
     *
     * @param resourceId 资源ID
     * @return 删除的行数
     */
    int deleteByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 批量查询多个资源的下游关系（用于图遍历优化）
     *
     * @param sourceResourceIds 源资源ID列表
     * @return 关系列表
     */
    List<RelationshipPO> selectBySourceResourceIds(@Param("sourceResourceIds") List<Long> sourceResourceIds);
}
