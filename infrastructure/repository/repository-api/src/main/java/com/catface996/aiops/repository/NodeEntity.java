package com.catface996.aiops.repository;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点实体（领域实体）
 *
 * 用途: 表示系统中的节点，可以是数据库服务器、业务应用、API 接口、报表系统等
 *
 * 特性: 纯 POJO，无框架注解，实现 Serializable 接口
 */
@Data
public class NodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（雪花算法自动生成）
     */
    private Long id;

    /**
     * 节点名称（唯一，最大 100 字符）
     */
    private String name;

    /**
     * 节点类型（DATABASE/APPLICATION/API/REPORT/OTHER）
     */
    private String type;

    /**
     * 节点描述（可选，最大 500 字符）
     */
    private String description;

    /**
     * 节点属性（JSON 格式字符串）
     */
    private String properties;

    /**
     * 创建时间（系统自动填充）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（系统自动填充）
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 逻辑删除标记（0=活动，1=已删除）
     */
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;
}
