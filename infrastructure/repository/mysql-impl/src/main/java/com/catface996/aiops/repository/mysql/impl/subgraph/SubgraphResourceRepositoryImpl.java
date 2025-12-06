package com.catface996.aiops.repository.mysql.impl.subgraph;

import com.catface996.aiops.domain.model.subgraph.SubgraphResource;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphResourceMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphResourcePO;
import com.catface996.aiops.repository.subgraph.SubgraphResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 子图资源关联仓储实现类
 *
 * <p>使用 MyBatis 实现子图资源关联数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 *   <li>需求7.3: 拓扑图只显示子图内节点之间的关系</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Repository
public class SubgraphResourceRepositoryImpl implements SubgraphResourceRepository {

    private final SubgraphResourceMapper resourceMapper;

    public SubgraphResourceRepositoryImpl(SubgraphResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    @Override
    public SubgraphResource addResource(SubgraphResource subgraphResource) {
        if (subgraphResource == null) {
            throw new IllegalArgumentException("子图资源关联实体不能为null");
        }
        SubgraphResourcePO po = toPO(subgraphResource);
        resourceMapper.insert(po);
        return toEntity(resourceMapper.selectById(po.getId()));
    }

    @Override
    public void addResources(List<SubgraphResource> subgraphResources) {
        if (subgraphResources == null || subgraphResources.isEmpty()) {
            return;
        }
        List<SubgraphResourcePO> poList = subgraphResources.stream()
                .map(this::toPO)
                .collect(Collectors.toList());
        resourceMapper.batchInsert(poList);
    }

    @Override
    public void removeResource(Long subgraphId, Long resourceId) {
        if (subgraphId == null || resourceId == null) {
            throw new IllegalArgumentException("子图ID和资源ID不能为null");
        }
        resourceMapper.deleteBySubgraphIdAndResourceId(subgraphId, resourceId);
    }

    @Override
    public void removeResources(Long subgraphId, List<Long> resourceIds) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        resourceMapper.batchDeleteBySubgraphIdAndResourceIds(subgraphId, resourceIds);
    }

    @Override
    public List<Long> findResourceIdsBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        return resourceMapper.selectResourceIdsBySubgraphId(subgraphId);
    }

    @Override
    public List<SubgraphResource> findBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        List<SubgraphResourcePO> poList = resourceMapper.selectBySubgraphId(subgraphId);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int countBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return resourceMapper.countBySubgraphId(subgraphId);
    }

    @Override
    public List<Long> findSubgraphIdsByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        return resourceMapper.selectSubgraphIdsByResourceId(resourceId);
    }

    @Override
    public boolean existsInSubgraph(Long subgraphId, Long resourceId) {
        if (subgraphId == null || resourceId == null) {
            return false;
        }
        return resourceMapper.existsInSubgraph(subgraphId, resourceId) > 0;
    }

    @Override
    public void deleteAllBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        resourceMapper.deleteAllBySubgraphId(subgraphId);
    }

    @Override
    public void deleteAllByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        resourceMapper.deleteAllByResourceId(resourceId);
    }

    @Override
    public boolean isSubgraphEmpty(Long subgraphId) {
        if (subgraphId == null) {
            return true;
        }
        return resourceMapper.countBySubgraphId(subgraphId) == 0;
    }

    @Override
    public List<SubgraphResource> findBySubgraphIdPaged(Long subgraphId, int page, int size) {
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图ID不能为null");
        }
        int offset = (page - 1) * size;
        List<SubgraphResourcePO> poList = resourceMapper.selectBySubgraphIdPaged(subgraphId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // ==================== 对象转换方法 ====================

    /**
     * 将领域实体转换为持久化对象
     */
    private SubgraphResourcePO toPO(SubgraphResource entity) {
        if (entity == null) {
            return null;
        }
        SubgraphResourcePO po = new SubgraphResourcePO();
        po.setId(entity.getId());
        po.setSubgraphId(entity.getSubgraphId());
        po.setResourceId(entity.getResourceId());
        po.setAddedAt(entity.getAddedAt());
        po.setAddedBy(entity.getAddedBy());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private SubgraphResource toEntity(SubgraphResourcePO po) {
        if (po == null) {
            return null;
        }
        SubgraphResource entity = new SubgraphResource();
        entity.setId(po.getId());
        entity.setSubgraphId(po.getSubgraphId());
        entity.setResourceId(po.getResourceId());
        entity.setAddedAt(po.getAddedAt());
        entity.setAddedBy(po.getAddedBy());
        return entity;
    }
}
