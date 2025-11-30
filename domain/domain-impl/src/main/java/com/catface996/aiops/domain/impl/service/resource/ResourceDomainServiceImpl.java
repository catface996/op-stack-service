package com.catface996.aiops.domain.impl.service.resource;

import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.model.resource.ResourceType;
import com.catface996.aiops.domain.service.resource.AuditLogService;
import com.catface996.aiops.domain.service.resource.ResourceDomainService;
import com.catface996.aiops.infrastructure.cache.api.service.ResourceCacheService;
import com.catface996.aiops.infrastructure.security.api.service.EncryptionService;
import com.catface996.aiops.repository.resource.ResourceRepository;
import com.catface996.aiops.repository.resource.ResourceTagRepository;
import com.catface996.aiops.repository.resource.ResourceTypeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 资源领域服务实现
 *
 * <p>实现资源管理的核心业务逻辑，协调多个Repository和基础设施服务。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>资源CRUD操作</li>
 *   <li>敏感数据加密解密</li>
 *   <li>缓存管理</li>
 *   <li>审计日志记录</li>
 *   <li>权限验证辅助</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Service
public class ResourceDomainServiceImpl implements ResourceDomainService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDomainServiceImpl.class);

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final EncryptionService encryptionService;
    private final ResourceCacheService cacheService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ResourceDomainServiceImpl(
            ResourceRepository resourceRepository,
            ResourceTypeRepository resourceTypeRepository,
            ResourceTagRepository resourceTagRepository,
            EncryptionService encryptionService,
            ResourceCacheService cacheService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.encryptionService = encryptionService;
        this.cacheService = cacheService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Resource createResource(String name, String description, Long resourceTypeId,
                                   String attributes, Long operatorId, String operatorName) {
        logger.info("创建资源，name: {}, typeId: {}, operatorId: {}", name, resourceTypeId, operatorId);

        // 1. 参数验证
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("资源名称不能为空");
        }
        if (resourceTypeId == null) {
            throw new IllegalArgumentException("资源类型ID不能为空");
        }

        // 2. 验证资源类型存在
        if (!resourceTypeRepository.existsById(resourceTypeId)) {
            throw new BusinessException(ResourceErrorCode.RESOURCE_TYPE_NOT_FOUND);
        }

        // 3. 验证资源名称在同类型下唯一
        if (resourceRepository.existsByNameAndTypeId(name.trim(), resourceTypeId)) {
            throw new BusinessException(ResourceErrorCode.RESOURCE_NAME_CONFLICT);
        }

        // 4. 加密敏感配置
        String encryptedAttributes = encryptAttributes(attributes);

        // 5. 创建资源实体
        Resource resource = Resource.create(name.trim(), description, resourceTypeId,
                encryptedAttributes, operatorId);

        // 6. 保存资源
        Resource savedResource = resourceRepository.save(resource);
        logger.info("资源创建成功，resourceId: {}", savedResource.getId());

        // 7. 清除列表缓存
        cacheService.evictAllResourceLists();

        // 8. 记录创建审计日志
        String auditValue = toJson(savedResource);
        auditLogService.logCreate(savedResource.getId(), auditValue, operatorId, operatorName);

        return savedResource;
    }

    @Override
    public List<Resource> listResources(Long resourceTypeId, ResourceStatus status,
                                        String keyword, int page, int size) {
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        // 检查是否应该使用缓存
        if (cacheService.shouldCachePage(page)) {
            String cacheKey = cacheService.generateListCacheKey(resourceTypeId,
                    status != null ? status.name() : null, keyword, page, size);
            Optional<String> cached = cacheService.getResourceList(cacheKey);
            if (cached.isPresent()) {
                logger.debug("从缓存获取资源列表，cacheKey: {}", cacheKey);
                return fromJsonList(cached.get());
            }
        }

        // 从数据库查询
        List<Resource> resources = resourceRepository.findByCondition(
                resourceTypeId, status, keyword, page, size);

        // 解密属性
        resources.forEach(this::decryptResourceAttributes);

        // 缓存结果
        if (cacheService.shouldCachePage(page)) {
            String cacheKey = cacheService.generateListCacheKey(resourceTypeId,
                    status != null ? status.name() : null, keyword, page, size);
            cacheService.cacheResourceList(cacheKey, toJson(resources));
        }

        return resources;
    }

    @Override
    public long countResources(Long resourceTypeId, ResourceStatus status, String keyword) {
        return resourceRepository.countByCondition(resourceTypeId, status, keyword);
    }

    @Override
    public Optional<Resource> getResourceById(Long resourceId) {
        if (resourceId == null) {
            return Optional.empty();
        }

        // 尝试从缓存获取
        Optional<String> cached = cacheService.getResource(resourceId);
        if (cached.isPresent()) {
            logger.debug("从缓存获取资源详情，resourceId: {}", resourceId);
            Resource resource = fromJson(cached.get(), Resource.class);
            if (resource != null) {
                return Optional.of(resource);
            }
        }

        // 从数据库查询
        Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);

        if (resourceOpt.isPresent()) {
            Resource resource = resourceOpt.get();
            // 解密属性
            decryptResourceAttributes(resource);
            // 缓存结果
            cacheService.cacheResource(resourceId, toJson(resource));
        }

        return resourceOpt;
    }

    @Override
    public Optional<Resource> getResourceByIdWithType(Long resourceId) {
        if (resourceId == null) {
            return Optional.empty();
        }

        Optional<Resource> resourceOpt = resourceRepository.findByIdWithType(resourceId);

        resourceOpt.ifPresent(this::decryptResourceAttributes);

        return resourceOpt;
    }

    @Override
    @Transactional
    public Resource updateResource(Long resourceId, String name, String description,
                                   String attributes, Integer version,
                                   Long operatorId, String operatorName) {
        logger.info("更新资源，resourceId: {}, operatorId: {}", resourceId, operatorId);

        // 1. 验证资源存在
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // 2. 保存旧值用于审计
        String oldValue = toJson(resource);

        // 3. 验证版本号（乐观锁）
        if (version != null && !version.equals(resource.getVersion())) {
            throw new BusinessException(ResourceErrorCode.VERSION_CONFLICT);
        }

        // 4. 如果更改了名称，验证新名称在同类型下唯一
        if (name != null && !name.equals(resource.getName())) {
            if (resourceRepository.existsByNameAndTypeId(name, resource.getResourceTypeId())) {
                throw new BusinessException(ResourceErrorCode.RESOURCE_NAME_CONFLICT);
            }
        }

        // 5. 加密敏感配置
        String encryptedAttributes = encryptAttributes(attributes);

        // 6. 更新资源（MyBatis-Plus @Version 会自动处理版本号递增）
        resource.update(name, description, encryptedAttributes);

        boolean updated = resourceRepository.update(resource);
        if (!updated) {
            throw new BusinessException(ResourceErrorCode.VERSION_CONFLICT);
        }

        logger.info("资源更新成功，resourceId: {}", resourceId);

        // 7. 清除缓存
        cacheService.evictResource(resourceId);

        // 8. 记录更新审计日志
        String newValue = toJson(resource);
        auditLogService.logUpdate(resourceId, oldValue, newValue, operatorId, operatorName);

        // 9. 解密后返回
        decryptResourceAttributes(resource);
        return resource;
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId, String confirmName, Long operatorId, String operatorName) {
        logger.info("删除资源，resourceId: {}, operatorId: {}", resourceId, operatorId);

        // 1. 验证资源存在
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // 2. 验证资源名称确认
        if (confirmName == null || !confirmName.equals(resource.getName())) {
            throw new BusinessException(ResourceErrorCode.RESOURCE_NAME_MISMATCH);
        }

        // 3. 保存旧值用于审计
        String oldValue = toJson(resource);

        // 4. 删除资源标签
        resourceTagRepository.deleteByResourceId(resourceId);

        // 5. 删除资源
        resourceRepository.deleteById(resourceId);
        logger.info("资源删除成功，resourceId: {}", resourceId);

        // 6. 清除缓存
        cacheService.evictResource(resourceId);

        // 7. 记录删除审计日志
        auditLogService.logDelete(resourceId, oldValue, operatorId, operatorName);
    }

    @Override
    @Transactional
    public Resource updateResourceStatus(Long resourceId, ResourceStatus newStatus, Integer version,
                                        Long operatorId, String operatorName) {
        logger.info("更新资源状态，resourceId: {}, newStatus: {}, operatorId: {}",
                resourceId, newStatus, operatorId);

        // 1. 验证资源存在
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // 2. 保存旧状态
        ResourceStatus oldStatus = resource.getStatus();

        // 3. 更新状态
        boolean updated = resourceRepository.updateStatus(resourceId, newStatus, version);
        if (!updated) {
            throw new BusinessException(ResourceErrorCode.VERSION_CONFLICT);
        }

        logger.info("资源状态更新成功，resourceId: {}, {} -> {}", resourceId, oldStatus, newStatus);

        // 4. 清除缓存
        cacheService.evictResource(resourceId);

        // 5. 记录状态变更审计日志
        auditLogService.logStatusChange(resourceId,
                oldStatus != null ? oldStatus.name() : null,
                newStatus.name(),
                operatorId, operatorName);

        // 6. 重新获取更新后的资源
        return resourceRepository.findById(resourceId).orElse(resource);
    }

    @Override
    public boolean checkOwnerPermission(Long resourceId, Long userId, boolean isAdmin) {
        // 管理员有所有权限
        if (isAdmin) {
            return true;
        }

        if (resourceId == null || userId == null) {
            return false;
        }

        // 检查是否为资源Owner
        return resourceRepository.findById(resourceId)
                .map(resource -> resource.isOwner(userId))
                .orElse(false);
    }

    @Override
    public List<ResourceAuditLog> getAuditLogs(Long resourceId, int page, int size) {
        return auditLogService.getAuditLogs(resourceId, page, size);
    }

    @Override
    public List<ResourceType> getAllResourceTypes() {
        // 尝试从缓存获取
        Optional<String> cached = cacheService.getResourceTypes();
        if (cached.isPresent()) {
            logger.debug("从缓存获取资源类型列表");
            return fromJsonList(cached.get(), ResourceType.class);
        }

        // 从数据库查询
        List<ResourceType> types = resourceTypeRepository.findAll();

        // 缓存结果
        cacheService.cacheResourceTypes(toJson(types));

        return types;
    }

    @Override
    public Optional<ResourceType> getResourceTypeById(Long resourceTypeId) {
        if (resourceTypeId == null) {
            return Optional.empty();
        }
        return resourceTypeRepository.findById(resourceTypeId);
    }

    // ===== 私有辅助方法 =====

    /**
     * 需要加密的敏感字段名称（不区分大小写）
     */
    private static final java.util.Set<String> SENSITIVE_FIELDS = java.util.Set.of(
            "password", "secret", "key", "token", "credential", "apikey", "api_key",
            "private_key", "privatekey", "access_token", "accesstoken", "secret_key"
    );

    /**
     * 加密资源属性中的敏感字段
     * <p>只对 JSON 中的敏感字段进行加密，保持整体 JSON 结构不变</p>
     */
    @SuppressWarnings("unchecked")
    private String encryptAttributes(String attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return attributes;
        }

        try {
            // 解析 JSON
            java.util.Map<String, Object> map = objectMapper.readValue(attributes, java.util.Map.class);

            // 遍历并加密敏感字段
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 只加密字符串类型的敏感字段
                if (value instanceof String && isSensitiveField(key)) {
                    String strValue = (String) value;
                    // 如果未加密，则加密
                    if (!encryptionService.isEncrypted(strValue)) {
                        entry.setValue(encryptionService.encrypt(strValue));
                    }
                }
            }

            // 转回 JSON 字符串
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            logger.warn("加密属性失败，保留原值", e);
            return attributes;
        }
    }

    /**
     * 解密资源属性中的敏感字段
     */
    @SuppressWarnings("unchecked")
    private void decryptResourceAttributes(Resource resource) {
        if (resource == null || resource.getAttributes() == null) {
            return;
        }

        String attributes = resource.getAttributes();
        try {
            // 解析 JSON
            java.util.Map<String, Object> map = objectMapper.readValue(attributes, java.util.Map.class);

            // 遍历并解密敏感字段
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 只解密字符串类型的敏感字段
                if (value instanceof String && isSensitiveField(key)) {
                    String strValue = (String) value;
                    // 如果已加密，则解密
                    if (encryptionService.isEncrypted(strValue)) {
                        try {
                            entry.setValue(encryptionService.decrypt(strValue));
                        } catch (Exception e) {
                            logger.warn("解密字段 {} 失败，resourceId: {}", key, resource.getId(), e);
                        }
                    }
                }
            }

            // 转回 JSON 字符串
            resource.setAttributes(objectMapper.writeValueAsString(map));
        } catch (Exception e) {
            logger.warn("解密资源属性失败，resourceId: {}", resource.getId(), e);
            // 解密失败保留原值
        }
    }

    /**
     * 判断字段是否为敏感字段
     */
    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lowerName = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.stream().anyMatch(lowerName::contains);
    }

    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("JSON序列化失败", e);
            return "{}";
        }
    }

    /**
     * JSON字符串转对象
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("JSON反序列化失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转Resource列表
     */
    private List<Resource> fromJsonList(String json) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Resource.class));
        } catch (JsonProcessingException e) {
            logger.error("JSON反序列化失败", e);
            return List.of();
        }
    }

    /**
     * JSON字符串转指定类型列表
     */
    private <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            logger.error("JSON反序列化失败", e);
            return List.of();
        }
    }
}
