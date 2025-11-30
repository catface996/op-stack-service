package com.catface996.aiops.repository.mysql.impl.resource;

import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.repository.resource.ResourceRepository;
import com.catface996.aiops.repository.mysql.mapper.resource.ResourceMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourcePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现资源数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Repository
public class ResourceRepositoryImpl implements ResourceRepository {

    private final ResourceMapper resourceMapper;

    public ResourceRepositoryImpl(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    @Override
    public Optional<Resource> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        ResourcePO po = resourceMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public Optional<Resource> findByIdWithType(Long id) {
        // 基础实现，与findById相同，资源类型关联在Service层处理
        return findById(id);
    }

    @Override
    public Optional<Resource> findByNameAndTypeId(String name, Long resourceTypeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("资源名称不能为空");
        }
        if (resourceTypeId == null) {
            throw new IllegalArgumentException("资源类型ID不能为null");
        }
        ResourcePO po = resourceMapper.selectByNameAndTypeId(name, resourceTypeId);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<Resource> findByCondition(Long resourceTypeId, ResourceStatus status,
                                          String keyword, int page, int size) {
        int offset = (page - 1) * size;
        String statusStr = status != null ? status.name() : null;
        List<ResourcePO> poList = resourceMapper.selectByCondition(
                resourceTypeId, statusStr, keyword, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(Long resourceTypeId, ResourceStatus status, String keyword) {
        String statusStr = status != null ? status.name() : null;
        return resourceMapper.countByCondition(resourceTypeId, statusStr, keyword);
    }

    @Override
    public Resource save(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("资源实体不能为null");
        }
        ResourcePO po = toPO(resource);
        resourceMapper.insert(po);
        ResourcePO savedPO = resourceMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public boolean update(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("资源实体不能为null");
        }
        if (resource.getId() == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        ResourcePO po = toPO(resource);
        int rows = resourceMapper.updateById(po);
        return rows > 0;
    }

    @Override
    public boolean updateStatus(Long id, ResourceStatus status, Integer version) {
        if (id == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        if (status == null) {
            throw new IllegalArgumentException("资源状态不能为null");
        }
        if (version == null) {
            throw new IllegalArgumentException("版本号不能为null");
        }
        int rows = resourceMapper.updateStatus(id, status.name(), version);
        return rows > 0;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        resourceMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return resourceMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByNameAndTypeId(String name, Long resourceTypeId) {
        if (name == null || name.trim().isEmpty() || resourceTypeId == null) {
            return false;
        }
        return resourceMapper.selectByNameAndTypeId(name, resourceTypeId) != null;
    }

    @Override
    public List<Resource> findByCreatedBy(Long createdBy, int page, int size) {
        if (createdBy == null) {
            throw new IllegalArgumentException("创建者ID不能为null");
        }
        int offset = (page - 1) * size;
        List<ResourcePO> poList = resourceMapper.selectByCreatedBy(createdBy, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return resourceMapper.selectCount(null);
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private ResourcePO toPO(Resource entity) {
        if (entity == null) {
            return null;
        }
        ResourcePO po = new ResourcePO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setResourceTypeId(entity.getResourceTypeId());
        po.setDescription(entity.getDescription());
        po.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        po.setConfigData(entity.getAttributes()); // 映射 attributes -> config_data
        po.setCreatedBy(entity.getCreatedBy());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        po.setVersion(entity.getVersion());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private Resource toEntity(ResourcePO po) {
        if (po == null) {
            return null;
        }
        Resource entity = new Resource();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setResourceTypeId(po.getResourceTypeId());
        entity.setDescription(po.getDescription());
        entity.setStatus(po.getStatus() != null ? ResourceStatus.valueOf(po.getStatus()) : null);
        entity.setAttributes(po.getConfigData()); // 映射 config_data -> attributes
        entity.setCreatedBy(po.getCreatedBy());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        entity.setVersion(po.getVersion());
        return entity;
    }
}
