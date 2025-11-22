package com.catface996.aiops.repository.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.NodePO;
import org.apache.ibatis.annotations.Param;

/**
 * 节点 Mapper 接口
 */
public interface NodeMapper extends BaseMapper<NodePO> {

    /**
     * 根据名称查询节点
     */
    NodePO selectByName(@Param("name") String name);
}
