package com.catface996.aiops.repository.mysql.impl.resource;

import com.catface996.aiops.domain.model.resource.ResourceTag;
import com.catface996.aiops.repository.resource.ResourceTagRepository;
import com.catface996.aiops.repository.mysql.mapper.resource.ResourceTagMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceTagPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源标签仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现资源标签数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Repository
public class ResourceTagRepositoryImpl implements ResourceTagRepository {

    private final ResourceTagMapper resourceTagMapper;

    public ResourceTagRepositoryImpl(ResourceTagMapper resourceTagMapper) {
        this.resourceTagMapper = resourceTagMapper;
    }

    @Override
    public Optional<ResourceTag> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("标签ID不能为null");
        }
        ResourceTagPO po = resourceTagMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<ResourceTag> findByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        List<ResourceTagPO> poList = resourceTagMapper.selectByResourceId(resourceId);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findResourceIdsByTagName(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        return resourceTagMapper.selectResourceIdsByTagName(tagName);
    }

    @Override
    public ResourceTag save(ResourceTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("标签实体不能为null");
        }
        ResourceTagPO po = toPO(tag);
        resourceTagMapper.insert(po);
        ResourceTagPO savedPO = resourceTagMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public List<ResourceTag> saveAll(List<ResourceTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("标签ID不能为null");
        }
        resourceTagMapper.deleteById(id);
    }

    @Override
    public void deleteByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        resourceTagMapper.deleteByResourceId(resourceId);
    }

    @Override
    public void deleteByResourceIdAndTagName(Long resourceId, String tagName) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        resourceTagMapper.deleteByResourceIdAndTagName(resourceId, tagName);
    }

    @Override
    public boolean existsByResourceIdAndTagName(Long resourceId, String tagName) {
        if (resourceId == null || tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        return resourceTagMapper.existsByResourceIdAndTagName(resourceId, tagName) > 0;
    }

    @Override
    public List<String> findPopularTags(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        return resourceTagMapper.selectPopularTags(limit);
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private ResourceTagPO toPO(ResourceTag entity) {
        if (entity == null) {
            return null;
        }
        ResourceTagPO po = new ResourceTagPO();
        po.setId(entity.getId());
        po.setResourceId(entity.getResourceId());
        po.setTagName(entity.getTagName());
        // tagValue 在领域模型中不需要，PO中保留用于数据库兼容
        po.setCreatedAt(entity.getCreatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private ResourceTag toEntity(ResourceTagPO po) {
        if (po == null) {
            return null;
        }
        ResourceTag entity = new ResourceTag();
        entity.setId(po.getId());
        entity.setResourceId(po.getResourceId());
        entity.setTagName(po.getTagName());
        // tagValue 在领域模型中不需要
        entity.setCreatedAt(po.getCreatedAt());
        return entity;
    }
}
