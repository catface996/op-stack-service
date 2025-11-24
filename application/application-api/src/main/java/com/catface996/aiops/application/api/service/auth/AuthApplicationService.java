package com.catface996.aiops.application.api.service.auth;

import com.catface996.aiops.application.api.dto.auth.LoginResult;
import com.catface996.aiops.application.api.dto.auth.RegisterResult;
import com.catface996.aiops.application.api.dto.auth.SessionValidationResult;
import com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest;
import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;

/**
 * 认证应用服务接口
 *
 * <p>提供用户注册、登录、登出、会话管理和账号管理等核心认证功能。
 * 本接口作为应用层的统一入口，协调领域服务和基础设施层完成业务流程。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>编排用户注册和登录业务流程</li>
 *   <li>协调会话管理和会话互斥逻辑</li>
 *   <li>处理账号锁定和解锁流程</li>
 *   <li>记录审计日志（通过日志框架）</li>
 *   <li>转换 DTO 和领域模型</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
public interface AuthApplicationService {

    /**
     * 用户注册
     *
     * <p>创建新的用户账号，包括以下流程：</p>
     * <ol>
     *   <li>验证用户名和邮箱唯一性</li>
     *   <li>验证密码强度要求</li>
     *   <li>使用 BCrypt 加密密码</li>
     *   <li>创建账号实体并持久化到数据库</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param request 注册请求，包含用户名、邮箱和密码
     * @return 注册结果，包含账号ID和基本信息
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.DuplicateUsernameException 当用户名已存在时抛出
     * @throws com.catface996.aiops.common.exception.DuplicateEmailException 当邮箱已存在时抛出
     * @throws com.catface996.aiops.common.exception.InvalidPasswordException 当密码不符合强度要求时抛出
     */
    RegisterResult register(RegisterRequest request);

    /**
     * 用户登录
     *
     * <p>验证用户凭据并创建会话，包括以下流程：</p>
     * <ol>
     *   <li>检查账号是否被锁定</li>
     *   <li>根据标识符（用户名或邮箱）查找账号</li>
     *   <li>验证密码是否正确</li>
     *   <li>处理会话互斥（使旧会话失效）</li>
     *   <li>创建新会话并生成 JWT Token</li>
     *   <li>存储会话到 Redis</li>
     *   <li>重置登录失败计数</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * <p>登录失败处理：</p>
     * <ul>
     *   <li>记录登录失败次数到 Redis</li>
     *   <li>连续 5 次失败后锁定账号 30 分钟</li>
     *   <li>返回通用错误消息"用户名或密码错误"</li>
     * </ul>
     *
     * @param request 登录请求，包含标识符（用户名或邮箱）、密码和是否记住我
     * @return 登录结果，包含 JWT Token、用户信息和会话信息
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.AuthenticationException 当认证失败时抛出
     * @throws com.catface996.aiops.common.exception.AccountLockedException 当账号被锁定时抛出
     * @throws com.catface996.aiops.common.exception.AccountNotFoundException 当账号不存在时抛出
     */
    LoginResult login(LoginRequest request);

    /**
     * 用户登出
     *
     * <p>使用户会话失效，包括以下流程：</p>
     * <ol>
     *   <li>解析 JWT Token 获取会话ID</li>
     *   <li>从 Redis 删除会话信息</li>
     *   <li>从 MySQL 删除会话记录（降级方案）</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param token JWT Token（包含 Bearer 前缀）
     * @throws IllegalArgumentException 当 Token 无效时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     * @throws com.catface996.aiops.common.exception.SessionNotFoundException 当会话不存在时抛出
     */
    void logout(String token);

    /**
     * 验证会话
     *
     * <p>验证用户会话是否有效，包括以下流程：</p>
     * <ol>
     *   <li>解析 JWT Token 获取会话ID</li>
     *   <li>从 Redis 查询会话信息（优先）</li>
     *   <li>如果 Redis 未命中，从 MySQL 查询（降级）</li>
     *   <li>检查会话是否过期</li>
     *   <li>返回会话验证结果</li>
     * </ol>
     *
     * @param token JWT Token（包含 Bearer 前缀）
     * @return 会话验证结果，包含会话是否有效、用户信息和过期时间
     * @throws IllegalArgumentException 当 Token 无效时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     * @throws com.catface996.aiops.common.exception.SessionExpiredException 当会话已过期时抛出
     */
    SessionValidationResult validateSession(String token);

    /**
     * 强制登出其他设备
     *
     * <p>使当前用户在其他设备的会话失效，然后在当前设备重新登录，包括以下流程：</p>
     * <ol>
     *   <li>解析 JWT Token 获取用户ID</li>
     *   <li>验证密码是否正确（安全验证）</li>
     *   <li>查询该用户的所有活跃会话</li>
     *   <li>删除所有旧会话</li>
     *   <li>创建新会话并生成 JWT Token</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param token 当前 JWT Token（包含 Bearer 前缀）
     * @param password 用户密码（用于安全验证）
     * @return 登录结果，包含新的 JWT Token、用户信息和会话信息
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.AuthenticationException 当密码验证失败时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     * @deprecated 该方法签名可能会在后续版本中调整，建议使用 {@code forceLogoutOthers(ForceLogoutRequest)} 代替
     */
    @Deprecated
    LoginResult forceLogoutOthers(String token, String password);

    /**
     * 强制登出其他设备（推荐使用）
     *
     * <p>使当前用户在其他设备的会话失效，然后在当前设备重新登录，包括以下流程：</p>
     * <ol>
     *   <li>解析请求中的 JWT Token 获取用户ID</li>
     *   <li>验证密码是否正确（安全验证）</li>
     *   <li>查询该用户的所有活跃会话</li>
     *   <li>删除所有旧会话</li>
     *   <li>创建新会话并生成 JWT Token</li>
     *   <li>记录审计日志</li>
     * </ol>
     *
     * @param request 强制登出请求，包含 Token 和密码
     * @return 登录结果，包含新的 JWT Token、用户信息和会话信息
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.AuthenticationException 当密码验证失败时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     */
    LoginResult forceLogoutOthers(ForceLogoutRequest request);

    /**
     * 管理员手动解锁账号
     *
     * <p>管理员手动解除账号锁定状态，包括以下流程：</p>
     * <ol>
     *   <li>验证管理员身份和权限</li>
     *   <li>查询账号信息</li>
     *   <li>清除登录失败计数（Redis）</li>
     *   <li>如果账号状态为 LOCKED，更新为 ACTIVE</li>
     *   <li>记录审计日志（包含管理员ID和操作时间）</li>
     * </ol>
     *
     * @param adminToken 管理员 JWT Token（包含 Bearer 前缀）
     * @param accountId 待解锁的账号ID
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws com.catface996.aiops.common.exception.ForbiddenException 当非管理员尝试解锁时抛出
     * @throws com.catface996.aiops.common.exception.AccountNotFoundException 当账号不存在时抛出
     * @throws com.catface996.aiops.common.exception.InvalidTokenException 当 Token 格式错误时抛出
     */
    void unlockAccount(String adminToken, Long accountId);
}
