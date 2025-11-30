package com.catface996.aiops.repository.mysql.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceTypePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源类型 Mapper 接口
 *
 * <p>提供资源类型数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceTypeMapper extends BaseMapper<ResourceTypePO> {

    /**
     * 根据类型编码查询资源类型
     *
     * @param code 类型编码
     * @return 资源类型PO对象，如果不存在返回null
     */
    ResourceTypePO selectByCode(@Param("code") String code);

    /**
     * 查询所有系统预置的资源类型
     *
     * @return 系统预置资源类型列表
     */
    List<ResourceTypePO> selectSystemTypes();
}
