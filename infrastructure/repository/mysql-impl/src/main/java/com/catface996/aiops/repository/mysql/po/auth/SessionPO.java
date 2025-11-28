package com.catface996.aiops.repository.mysql.po.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话持久化对象
 *
 * <p>数据库表 t_session 的映射对象</p>
 * <p>MySQL作为主存储，Redis作为缓存层（Cache-Aside模式）</p>
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
@Data
@TableName("t_session")
public class SessionPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（UUID）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * JWT Token
     */
    @TableField("token")
    private String token;

    /**
     * 过期时间（绝对超时）
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 设备信息（JSON格式）
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 最后活动时间
     */
    @TableField("last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * 绝对超时时长（秒）
     */
    @TableField("absolute_timeout")
    private Integer absoluteTimeout;

    /**
     * 空闲超时时长（秒）
     */
    @TableField("idle_timeout")
    private Integer idleTimeout;

    /**
     * 是否启用记住我功能
     */
    @TableField("remember_me")
    private Boolean rememberMe;
}
