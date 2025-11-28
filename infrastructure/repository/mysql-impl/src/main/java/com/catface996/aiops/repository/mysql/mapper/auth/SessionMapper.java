package com.catface996.aiops.repository.mysql.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.auth.SessionPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话 Mapper 接口
 *
 * <p>提供会话数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.4, 1.5: 会话存储</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface SessionMapper extends BaseMapper<SessionPO> {

    /**
     * 根据用户ID查询单个会话（最近的一个）
     *
     * @param userId 用户ID
     * @return 会话PO对象，如果不存在返回null
     */
    SessionPO selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SessionPO> selectAllByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除会话
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量删除会话
     *
     * @param sessionIds 会话ID列表
     * @return 删除的记录数
     */
    int batchDeleteByIds(@Param("sessionIds") List<String> sessionIds);

    /**
     * 删除所有过期会话
     *
     * @return 删除的记录数
     */
    int deleteExpiredSessions();

    /**
     * 统计用户的会话数量
     *
     * @param userId 用户ID
     * @return 会话数量
     */
    int countByUserId(@Param("userId") Long userId);
}
