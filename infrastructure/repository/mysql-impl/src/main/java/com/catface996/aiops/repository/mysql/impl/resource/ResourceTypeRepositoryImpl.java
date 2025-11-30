package com.catface996.aiops.repository.mysql.impl.resource;

import com.catface996.aiops.domain.model.resource.ResourceType;
import com.catface996.aiops.repository.resource.ResourceTypeRepository;
import com.catface996.aiops.repository.mysql.mapper.resource.ResourceTypeMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceTypePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源类型仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现资源类型数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Repository
public class ResourceTypeRepositoryImpl implements ResourceTypeRepository {

    private final ResourceTypeMapper resourceTypeMapper;

    public ResourceTypeRepositoryImpl(ResourceTypeMapper resourceTypeMapper) {
        this.resourceTypeMapper = resourceTypeMapper;
    }

    @Override
    public Optional<ResourceType> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资源类型ID不能为null");
        }
        ResourceTypePO po = resourceTypeMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public Optional<ResourceType> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("资源类型编码不能为空");
        }
        ResourceTypePO po = resourceTypeMapper.selectByCode(code);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<ResourceType> findAll() {
        List<ResourceTypePO> poList = resourceTypeMapper.selectList(null);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceType> findSystemTypes() {
        List<ResourceTypePO> poList = resourceTypeMapper.selectSystemTypes();
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ResourceType save(ResourceType resourceType) {
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型实体不能为null");
        }
        ResourceTypePO po = toPO(resourceType);
        resourceTypeMapper.insert(po);
        ResourceTypePO savedPO = resourceTypeMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public ResourceType update(ResourceType resourceType) {
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型实体不能为null");
        }
        ResourceTypePO po = toPO(resourceType);
        resourceTypeMapper.updateById(po);
        ResourceTypePO updatedPO = resourceTypeMapper.selectById(po.getId());
        return toEntity(updatedPO);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资源类型ID不能为null");
        }
        resourceTypeMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return resourceTypeMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return resourceTypeMapper.selectByCode(code) != null;
    }

    @Override
    public long count() {
        return resourceTypeMapper.selectCount(null);
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private ResourceTypePO toPO(ResourceType entity) {
        if (entity == null) {
            return null;
        }
        ResourceTypePO po = new ResourceTypePO();
        po.setId(entity.getId());
        po.setCode(entity.getCode());
        po.setName(entity.getName());
        po.setDescription(entity.getDescription());
        po.setIcon(entity.getIcon());
        po.setIsSystem(entity.getIsSystem());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private ResourceType toEntity(ResourceTypePO po) {
        if (po == null) {
            return null;
        }
        ResourceType entity = new ResourceType();
        entity.setId(po.getId());
        entity.setCode(po.getCode());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setIcon(po.getIcon());
        entity.setIsSystem(po.getIsSystem());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
