package com.catface996.aiops.repository.mysql.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceAuditLogPO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源审计日志 Mapper 接口
 *
 * <p>提供资源审计日志数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceAuditLogMapper extends BaseMapper<ResourceAuditLogPO> {

    /**
     * 根据资源ID分页查询审计日志
     *
     * @param resourceId 资源ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表（按时间倒序排列）
     */
    List<ResourceAuditLogPO> selectByResourceId(@Param("resourceId") Long resourceId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    /**
     * 统计资源的审计日志数量
     *
     * @param resourceId 资源ID
     * @return 审计日志数量
     */
    long countByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 根据操作类型查询审计日志
     *
     * @param resourceId 资源ID
     * @param operation 操作类型
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<ResourceAuditLogPO> selectByResourceIdAndOperation(@Param("resourceId") Long resourceId,
                                                             @Param("operation") String operation,
                                                             @Param("offset") int offset,
                                                             @Param("limit") int limit);

    /**
     * 根据时间范围查询审计日志
     *
     * @param resourceId 资源ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<ResourceAuditLogPO> selectByResourceIdAndTimeRange(@Param("resourceId") Long resourceId,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime,
                                                             @Param("offset") int offset,
                                                             @Param("limit") int limit);

    /**
     * 根据操作人ID查询审计日志
     *
     * @param operatorId 操作人ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<ResourceAuditLogPO> selectByOperatorId(@Param("operatorId") Long operatorId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    /**
     * 删除资源的所有审计日志
     *
     * @param resourceId 资源ID
     * @return 删除的行数
     */
    int deleteByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 清理指定时间之前的审计日志
     *
     * @param beforeTime 时间点
     * @return 删除的行数
     */
    int deleteByCreatedAtBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
