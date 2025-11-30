package com.catface996.aiops.repository.mysql.po.resource;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源持久化对象
 *
 * <p>数据库表 resource 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@TableName("resource")
public class ResourcePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资源名称
     */
    @TableField("name")
    private String name;

    /**
     * 资源类型ID
     */
    @TableField("resource_type_id")
    private Long resourceTypeId;

    /**
     * 资源描述
     */
    @TableField("description")
    private String description;

    /**
     * 资源状态（RUNNING, STOPPED, MAINTENANCE, OFFLINE）
     */
    @TableField("status")
    private String status;

    /**
     * 敏感配置信息（AES-256加密存储）
     */
    @TableField("config_data")
    private String configData;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
