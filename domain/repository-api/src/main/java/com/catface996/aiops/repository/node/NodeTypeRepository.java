package com.catface996.aiops.repository.node;

import com.catface996.aiops.domain.model.node.NodeType;

import java.util.List;
import java.util.Optional;

/**
 * 节点类型仓储接口
 *
 * <p>提供节点类型实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface NodeTypeRepository {

    /**
     * 根据ID查询节点类型
     *
     * @param id 节点类型ID
     * @return 节点类型实体（如果存在）
     */
    Optional<NodeType> findById(Long id);

    /**
     * 根据编码查询节点类型
     *
     * @param code 类型编码
     * @return 节点类型实体（如果存在）
     */
    Optional<NodeType> findByCode(String code);

    /**
     * 查询所有节点类型
     *
     * @return 节点类型列表
     */
    List<NodeType> findAll();

    /**
     * 检查节点类型是否存在
     *
     * @param id 节点类型ID
     * @return true if type exists
     */
    boolean existsById(Long id);

    /**
     * 检查节点类型编码是否已存在
     *
     * @param code 类型编码
     * @return true if code exists
     */
    boolean existsByCode(String code);
}
