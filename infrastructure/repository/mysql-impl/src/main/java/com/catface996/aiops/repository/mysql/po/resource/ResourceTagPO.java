package com.catface996.aiops.repository.mysql.po.resource;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源标签持久化对象
 *
 * <p>数据库表 resource_tag 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@TableName("resource_tag")
public class ResourceTagPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 标签值（可选）
     */
    @TableField("tag_value")
    private String tagValue;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
