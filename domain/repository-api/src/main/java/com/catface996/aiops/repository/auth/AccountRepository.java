package com.catface996.aiops.repository.auth;

import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountStatus;

import java.util.Optional;

/**
 * 账号仓储接口
 *
 * <p>提供账号实体的数据访问操作，遵循DDD仓储模式。</p>
 * <p>仓储负责领域对象的持久化和重建，隔离领域层与基础设施层。</p>
 *
 * <p>实现说明：</p>
 * <ul>
 *   <li>使用MyBatis-Plus实现数据访问</li>
 *   <li>数据存储在MySQL数据库</li>
 *   <li>支持事务管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface AccountRepository {

    /**
     * 根据ID查询账号
     *
     * <p>根据账号ID查询账号实体。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-001: 用户名密码登录</li>
     *   <li>REQ-FR-002: 邮箱密码登录</li>
     *   <li>REQ-FR-006: 管理员手动解锁</li>
     * </ul>
     *
     * @param id 账号ID
     * @return 账号实体（如果存在）
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<Account> findById(Long id);

    /**
     * 根据用户名查询账号
     *
     * <p>根据用户名查询账号实体。</p>
     * <p>用户名唯一，最多返回一个结果。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-001: 用户名密码登录</li>
     *   <li>REQ-FR-003: 账号注册（唯一性验证）</li>
     * </ul>
     *
     * @param username 用户名
     * @return 账号实体（如果存在）
     * @throws IllegalArgumentException 如果username为空或null
     */
    Optional<Account> findByUsername(String username);

    /**
     * 根据邮箱查询账号
     *
     * <p>根据邮箱查询账号实体。</p>
     * <p>邮箱唯一，最多返回一个结果。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-002: 邮箱密码登录</li>
     *   <li>REQ-FR-003: 账号注册（唯一性验证）</li>
     * </ul>
     *
     * @param email 邮箱
     * @return 账号实体（如果存在）
     * @throws IllegalArgumentException 如果email为空或null
     */
    Optional<Account> findByEmail(String email);

    /**
     * 保存账号
     *
     * <p>保存账号实体到数据库。</p>
     * <p>如果账号ID为null，则执行插入操作；否则执行更新操作。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-003: 账号注册</li>
     *   <li>REQ-FR-005: 防暴力破解（更新账号状态）</li>
     *   <li>REQ-FR-006: 管理员手动解锁（更新账号状态）</li>
     * </ul>
     *
     * @param account 账号实体
     * @return 保存后的账号实体（包含生成的ID）
     * @throws IllegalArgumentException 如果account为null
     * @throws com.catface996.aiops.domain.api.exception.auth.DuplicateUsernameException 如果用户名已存在
     * @throws com.catface996.aiops.domain.api.exception.auth.DuplicateEmailException 如果邮箱已存在
     */
    Account save(Account account);

    /**
     * 更新账号状态
     *
     * <p>更新指定账号的状态。</p>
     * <p>此方法用于账号锁定、解锁、禁用等操作。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-005: 防暴力破解（锁定账号）</li>
     *   <li>REQ-FR-006: 管理员手动解锁（解锁账号）</li>
     * </ul>
     *
     * @param id 账号ID
     * @param status 新的账号状态
     * @throws IllegalArgumentException 如果id或status为null
     * @throws com.catface996.aiops.domain.api.exception.auth.AccountNotFoundException 如果账号不存在
     */
    void updateStatus(Long id, AccountStatus status);

    /**
     * 删除账号
     *
     * <p>根据ID删除账号。</p>
     * <p>注意：此方法为物理删除，谨慎使用。</p>
     * <p>建议使用软删除（更新状态为DISABLED）代替物理删除。</p>
     *
     * @param id 账号ID
     * @throws IllegalArgumentException 如果id为null
     */
    void deleteById(Long id);

    /**
     * 检查用户名是否存在
     *
     * <p>检查指定的用户名是否已被使用。</p>
     * <p>用于注册时的唯一性验证。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-003: 账号注册（唯一性验证）</li>
     * </ul>
     *
     * @param username 用户名
     * @return true if username exists, false otherwise
     * @throws IllegalArgumentException 如果username为空或null
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * <p>检查指定的邮箱是否已被使用。</p>
     * <p>用于注册时的唯一性验证。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-003: 账号注册（唯一性验证）</li>
     * </ul>
     *
     * @param email 邮箱
     * @return true if email exists, false otherwise
     * @throws IllegalArgumentException 如果email为空或null
     */
    boolean existsByEmail(String email);
}
