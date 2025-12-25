package com.catface996.aiops.repository.mysql.po.subgraph;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 子图成员关联持久化对象
 *
 * <p>数据库表 subgraph_member 的映射对象</p>
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Data
@TableName("subgraph_member")
public class SubgraphMemberPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父子图资源ID（必须是 SUBGRAPH 类型的 resource）
     */
    @TableField("subgraph_id")
    private Long subgraphId;

    /**
     * 成员资源ID（可以是任意类型包括 SUBGRAPH，实现嵌套）
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 添加时间
     */
    @TableField("added_at")
    private LocalDateTime addedAt;

    /**
     * 添加者用户ID
     */
    @TableField("added_by")
    private Long addedBy;

    // ==================== 派生字段（不映射到数据库，用于 JOIN 查询结果）====================

    /**
     * 成员资源名称（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String memberName;

    /**
     * 成员资源类型代码（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String memberTypeCode;

    /**
     * 成员资源状态（JOIN 查询填充）
     */
    @TableField(exist = false)
    private String memberStatus;

    /**
     * 成员是否为子图类型（JOIN 查询填充）
     */
    @TableField(exist = false)
    private Boolean isSubgraph;

    /**
     * 如果成员是子图，其包含的成员数量（JOIN 查询填充）
     */
    @TableField(exist = false)
    private Integer nestedMemberCount;
}
