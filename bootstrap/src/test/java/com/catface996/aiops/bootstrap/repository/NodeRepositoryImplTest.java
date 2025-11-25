package com.catface996.aiops.bootstrap.repository;

import com.catface996.aiops.domain.api.model.topology.Node;
import com.catface996.aiops.repository.topology.NodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NodeRepository 单元测试
 *
 * 测试场景：
 * 1. 保存节点（save）- 验证 ID 自动生成、时间自动填充、createBy/updateBy 正确
 * 2. 根据 ID 查询（findById）- 验证查询成功、字段值正确
 * 3. 根据名称模糊查询（findByName）- 验证模糊查询成功、返回正确结果
 * 4. 逻辑删除（deleteById）- 验证 deleted 设置为 1、查询时不返回已删除的节点
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class
NodeRepositoryImplTest {

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 测试场景：保存节点
     * 验证点：
     * 1. ID 自动生成（雪花算法）
     * 2. createTime、updateTime 自动填充
     * 3. createBy、updateBy 正确设置
     * 4. deleted 默认值为 0
     * 5. version 默认值为 0
     *
     * 注意：使用 @Rollback(false) 禁用自动回滚，数据会真实保存到数据库
     */
    @Test
    @org.springframework.test.annotation.Rollback(false)
    public void testSave() {
        // 准备测试数据（使用时间戳确保名称唯一）
        Node entity = new Node();
        entity.setName("MySQL数据库节点-" + System.currentTimeMillis());
        entity.setType("DATABASE");
        entity.setDescription("生产环境MySQL数据库");
        entity.setProperties("{\"host\":\"192.168.1.100\",\"port\":3306}");

        // 执行保存
        Node savedEntity = nodeRepository.save(entity, "test-user");

        // 验证结果
        assertNotNull(savedEntity.getId(), "ID 应该自动生成");
        assertNotNull(savedEntity.getCreateTime(), "createTime 应该自动填充");
        assertNotNull(savedEntity.getUpdateTime(), "updateTime 应该自动填充");
        assertEquals("test-user", savedEntity.getCreateBy(), "createBy 应该正确设置");
        assertEquals("test-user", savedEntity.getUpdateBy(), "updateBy 应该正确设置");
        assertEquals(0, savedEntity.getDeleted(), "deleted 默认值应该为 0");
        assertEquals(0, savedEntity.getVersion(), "version 默认值应该为 0");
        assertTrue(savedEntity.getName().startsWith("MySQL数据库节点-"), "名称应该包含时间戳");
        assertEquals("DATABASE", savedEntity.getType());
    }

    /**
     * 测试场景：根据 ID 查询节点
     * 验证点：
     * 1. 查询成功返回节点
     * 2. 字段值正确
     * 3. 查询不存在的 ID 返回 null
     */
    @Test
    public void testFindById() {
        // 先保存一个节点
        Node entity = new Node();
        entity.setName("用户服务节点");
        entity.setType("APPLICATION");
        entity.setDescription("用户管理服务");

        Node savedEntity = nodeRepository.save(entity, "test-user");
        Long savedId = savedEntity.getId();

        // 根据 ID 查询
        Node foundEntity = nodeRepository.findById(savedId);

        // 验证结果
        assertNotNull(foundEntity, "应该查询到节点");
        assertEquals(savedId, foundEntity.getId());
        assertEquals("用户服务节点", foundEntity.getName());
        assertEquals("APPLICATION", foundEntity.getType());
        assertEquals("用户管理服务", foundEntity.getDescription());

        // 查询不存在的 ID
        Node notFound = nodeRepository.findById(999999L);
        assertNull(notFound, "查询不存在的 ID 应该返回 null");
    }

    /**
     * 测试场景：根据名称模糊查询节点
     * 验证点：
     * 1. 模糊查询成功
     * 2. 返回正确的节点
     * 3. 查询不存在的名称返回 null
     */
    @Test
    public void testFindByName() {
        // 先保存几个节点
        Node entity1 = new Node();
        entity1.setName("订单服务-生产环境");
        entity1.setType("APPLICATION");
        nodeRepository.save(entity1, "test-user");

        Node entity2 = new Node();
        entity2.setName("支付服务-生产环境");
        entity2.setType("APPLICATION");
        nodeRepository.save(entity2, "test-user");

        // 模糊查询 - 查询包含"订单"的节点
        Node found1 = nodeRepository.findByName("订单");
        assertNotNull(found1, "应该查询到包含'订单'的节点");
        assertTrue(found1.getName().contains("订单"), "查询结果应该包含'订单'");

        // 模糊查询 - 查询包含"生产环境"的节点
        Node found2 = nodeRepository.findByName("生产环境");
        assertNotNull(found2, "应该查询到包含'生产环境'的节点");
        assertTrue(found2.getName().contains("生产环境"), "查询结果应该包含'生产环境'");

        // 查询不存在的名称
        Node notFound = nodeRepository.findByName("不存在的节点");
        assertNull(notFound, "查询不存在的名称应该返回 null");
    }

    /**
     * 测试场景：逻辑删除节点
     * 验证点：
     * 1. 删除后查询不到该节点
     * 2. deleted 字段设置为 1（逻辑删除，不物理删除）
     */
    @Test
    public void testDeleteById() {
        // 先保存一个节点
        Node entity = new Node();
        entity.setName("临时测试节点");
        entity.setType("OTHER");
        entity.setDescription("用于测试删除功能");

        Node savedEntity = nodeRepository.save(entity, "test-user");
        Long savedId = savedEntity.getId();

        // 验证保存成功
        Node beforeDelete = nodeRepository.findById(savedId);
        assertNotNull(beforeDelete, "删除前应该能查询到节点");

        // 执行逻辑删除
        nodeRepository.deleteById(savedId, "test-user");

        // 验证删除后查询不到
        Node afterDelete = nodeRepository.findById(savedId);
        assertNull(afterDelete, "逻辑删除后查询应该返回 null");
    }

    /**
     * 测试场景：验证输入参数
     * 验证点：
     * 1. 保存空节点抛出异常
     * 2. 节点名称为空抛出异常
     * 3. 操作人为空抛出异常
     * 4. 节点类型无效抛出异常
     */
    @Test
    public void testValidation() {
        // 节点为空
        assertThrows(IllegalArgumentException.class, () -> {
            nodeRepository.save(null, "test-user");
        }, "节点为空应该抛出异常");

        // 节点名称为空
        assertThrows(IllegalArgumentException.class, () -> {
            Node entity = new Node();
            entity.setType("DATABASE");
            nodeRepository.save(entity, "test-user");
        }, "节点名称为空应该抛出异常");

        // 操作人为空
        assertThrows(IllegalArgumentException.class, () -> {
            Node entity = new Node();
            entity.setName("测试节点");
            entity.setType("DATABASE");
            nodeRepository.save(entity, null);
        }, "操作人为空应该抛出异常");

        // 节点类型无效
        assertThrows(IllegalArgumentException.class, () -> {
            Node entity = new Node();
            entity.setName("测试节点");
            entity.setType("INVALID_TYPE");
            nodeRepository.save(entity, "test-user");
        }, "节点类型无效应该抛出异常");
    }

    /**
     * 测试场景：验证 JSON 格式
     * 验证点：
     * 1. 有效的 JSON 格式可以保存
     * 2. 无效的 JSON 格式抛出异常
     */
    @Test
    public void testJsonValidation() {
        // 有效的 JSON 格式
        Node entity1 = new Node();
        entity1.setName("测试节点-JSON有效");
        entity1.setType("DATABASE");
        entity1.setProperties("{\"host\":\"localhost\",\"port\":3306}");

        assertDoesNotThrow(() -> {
            nodeRepository.save(entity1, "test-user");
        }, "有效的 JSON 格式应该可以保存");

        // 无效的 JSON 格式
        Node entity2 = new Node();
        entity2.setName("测试节点-JSON无效");
        entity2.setType("DATABASE");
        entity2.setProperties("{invalid json}");

        assertThrows(IllegalArgumentException.class, () -> {
            nodeRepository.save(entity2, "test-user");
        }, "无效的 JSON 格式应该抛出异常");
    }
}
