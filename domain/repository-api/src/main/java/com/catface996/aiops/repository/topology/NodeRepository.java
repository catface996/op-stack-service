package com.catface996.aiops.repository.topology;

import com.catface996.aiops.domain.model.topology.Node;

/**
 * 节点仓储接口
 *
 * <p>定义节点实体的数据访问契约,包括基本 CRUD 操作。</p>
 *
 * <p>User Story 1 (US1) - 持久化和检索系统节点信息:</p>
 * <ul>
 *   <li>save: 保存节点</li>
 *   <li>findById: 根据 ID 查询节点</li>
 *   <li>findByName: 根据名称查询节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
public interface NodeRepository {

    /**
     * 保存节点
     *
     * @param node     节点实体
     * @param operator 操作人（用于填充 createBy 和 updateBy）
     * @return 保存后的节点实体（包含生成的 ID 和时间戳）
     */
    Node save(Node node, String operator);

    /**
     * 根据 ID 查询节点
     *
     * @param id 节点 ID
     * @return 节点实体，如果不存在或已删除返回 null
     */
    Node findById(Long id);

    /**
     * 根据名称模糊查询节点
     *
     * @param name 节点名称（支持模糊查询，自动添加 %name%）
     * @return 节点实体，如果不存在或已删除返回 null（多条时返回第一条）
     */
    Node findByName(String name);

    /**
     * 逻辑删除节点
     *
     * @param id       节点 ID
     * @param operator 操作人（用于填充 updateBy）
     */
    void deleteById(Long id, String operator);
}
