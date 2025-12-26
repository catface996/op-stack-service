package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.node.NodeTypePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节点类型 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface NodeTypeMapper extends BaseMapper<NodeTypePO> {

    /**
     * 查询所有节点类型
     *
     * @return 节点类型列表
     */
    @Select("SELECT * FROM node_type ORDER BY id")
    List<NodeTypePO> selectAll();

    /**
     * 根据编码查询节点类型
     *
     * @param code 类型编码
     * @return 节点类型信息
     */
    @Select("SELECT * FROM node_type WHERE code = #{code}")
    NodeTypePO selectByCode(@Param("code") String code);
}
