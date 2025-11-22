package com.catface996.aiops.repository.mysql.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点持久化对象（数据库表映射对象）
 *
 * 用途: 数据库表 t_node 的映射对象，包含 MyBatis-Plus 注解
 *
 * 特性: 包含 MyBatis-Plus 注解（@TableName、@TableId、@TableField、@TableLogic、@Version）
 */
@Data
@TableName("t_node")
public class NodePO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("description")
    private String description;

    @TableField("properties")
    private String properties;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableLogic(value = "0", delval = "1")
    @TableField(value = "deleted", fill = FieldFill.INSERT, insertStrategy = FieldStrategy.ALWAYS)
    private Integer deleted;

    @Version
    @TableField(value = "version", fill = FieldFill.INSERT, insertStrategy = FieldStrategy.ALWAYS)
    private Integer version;
}
