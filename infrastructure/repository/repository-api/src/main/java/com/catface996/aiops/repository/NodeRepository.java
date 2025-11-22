package com.catface996.aiops.repository;

/**
 * 节点仓储接口
 *
 * 定义节点实体的数据访问契约，包括基本 CRUD 操作
 *
 * User Story 1 (US1) - 持久化和检索系统节点信息:
 * - save: 保存节点
 * - findById: 根据 ID 查询节点
 * - findByName: 根据名称查询节点
 */
public interface NodeRepository {

    /**
     * 保存节点
     *
     * @param entity   节点实体
     * @param operator 操作人（用于填充 createBy 和 updateBy）
     * @return 保存后的节点实体（包含生成的 ID 和时间戳）
     */
    NodeEntity save(NodeEntity entity, String operator);

    /**
     * 根据 ID 查询节点
     *
     * @param id 节点 ID
     * @return 节点实体，如果不存在或已删除返回 null
     */
    NodeEntity findById(Long id);

    /**
     * 根据名称模糊查询节点
     *
     * @param name 节点名称（支持模糊查询，自动添加 %name%）
     * @return 节点实体，如果不存在或已删除返回 null（多条时返回第一条）
     */
    NodeEntity findByName(String name);

    /**
     * 逻辑删除节点
     *
     * @param id       节点 ID
     * @param operator 操作人（用于填充 updateBy）
     */
    void deleteById(Long id, String operator);
}
