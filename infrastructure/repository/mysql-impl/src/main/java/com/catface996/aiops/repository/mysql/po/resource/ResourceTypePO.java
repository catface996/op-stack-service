package com.catface996.aiops.repository.mysql.po.resource;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源类型持久化对象
 *
 * <p>数据库表 resource_type 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@TableName("resource_type")
public class ResourceTypePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型编码（唯一，如：SERVER, APPLICATION等）
     */
    @TableField("code")
    private String code;

    /**
     * 类型名称（如：服务器、应用程序等）
     */
    @TableField("name")
    private String name;

    /**
     * 类型描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标（可选，用于UI展示）
     */
    @TableField("icon")
    private String icon;

    /**
     * 是否为系统预置类型
     */
    @TableField("is_system")
    private Boolean isSystem;

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
}
