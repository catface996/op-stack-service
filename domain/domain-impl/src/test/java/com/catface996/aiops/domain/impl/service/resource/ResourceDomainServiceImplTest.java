package com.catface996.aiops.domain.impl.service.resource;

import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.model.resource.ResourceType;
import com.catface996.aiops.domain.service.resource.AuditLogService;
import com.catface996.aiops.infrastructure.cache.api.service.ResourceCacheService;
import com.catface996.aiops.infrastructure.security.api.service.EncryptionService;
import com.catface996.aiops.repository.resource.ResourceRepository;
import com.catface996.aiops.repository.resource.ResourceTagRepository;
import com.catface996.aiops.repository.resource.ResourceTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 资源领域服务实现类单元测试
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@DisplayName("资源领域服务测试")
class ResourceDomainServiceImplTest {

    private ResourceDomainServiceImpl resourceDomainService;
    private ResourceRepository resourceRepository;
    private ResourceTypeRepository resourceTypeRepository;
    private ResourceTagRepository resourceTagRepository;
    private EncryptionService encryptionService;
    private ResourceCacheService cacheService;
    private AuditLogService auditLogService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        resourceRepository = mock(ResourceRepository.class);
        resourceTypeRepository = mock(ResourceTypeRepository.class);
        resourceTagRepository = mock(ResourceTagRepository.class);
        encryptionService = mock(EncryptionService.class);
        cacheService = mock(ResourceCacheService.class);
        auditLogService = mock(AuditLogService.class);
        objectMapper = new ObjectMapper();

        resourceDomainService = new ResourceDomainServiceImpl(
                resourceRepository,
                resourceTypeRepository,
                resourceTagRepository,
                encryptionService,
                cacheService,
                auditLogService,
                objectMapper
        );
    }

    @Nested
    @DisplayName("创建资源测试")
    class CreateResourceTest {

        @Test
        @DisplayName("应该成功创建资源")
        void shouldCreateResourceSuccessfully() {
            // Given
            String name = "test-server";
            String description = "测试服务器";
            Long resourceTypeId = 1L;
            String attributes = "{\"ip\":\"192.168.1.1\",\"password\":\"secret123\"}";
            Long operatorId = 100L;
            String operatorName = "admin";

            when(resourceTypeRepository.existsById(resourceTypeId)).thenReturn(true);
            when(resourceRepository.existsByNameAndTypeId(name, resourceTypeId)).thenReturn(false);
            when(encryptionService.isEncrypted(anyString())).thenReturn(false);
            when(encryptionService.encrypt("secret123")).thenReturn("ENC:encrypted_password");
            when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
                Resource r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            // When
            Resource result = resourceDomainService.createResource(name, description, resourceTypeId,
                    attributes, operatorId, operatorName);

            // Then
            assertNotNull(result);
            assertEquals(name, result.getName());
            assertEquals(description, result.getDescription());
            assertEquals(resourceTypeId, result.getResourceTypeId());
            assertEquals(ResourceStatus.RUNNING, result.getStatus());

            // 验证审计日志被记录
            verify(auditLogService).logCreate(eq(1L), anyString(), eq(operatorId), eq(operatorName));
            // 验证缓存被清除
            verify(cacheService).evictAllResourceLists();
        }

        @Test
        @DisplayName("资源名称为空时应该抛出异常")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () ->
                    resourceDomainService.createResource("", "desc", 1L, "{}", 100L, "admin"));
            assertThrows(IllegalArgumentException.class, () ->
                    resourceDomainService.createResource(null, "desc", 1L, "{}", 100L, "admin"));
        }

        @Test
        @DisplayName("资源类型不存在时应该抛出业务异常")
        void shouldThrowExceptionWhenResourceTypeNotFound() {
            // Given
            when(resourceTypeRepository.existsById(99L)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    resourceDomainService.createResource("server", "desc", 99L, "{}", 100L, "admin"));
            assertEquals(ResourceErrorCode.RESOURCE_TYPE_NOT_FOUND.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("资源名称重复时应该抛出业务异常")
        void shouldThrowExceptionWhenNameConflict() {
            // Given
            when(resourceTypeRepository.existsById(1L)).thenReturn(true);
            when(resourceRepository.existsByNameAndTypeId("existing-server", 1L)).thenReturn(true);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    resourceDomainService.createResource("existing-server", "desc", 1L, "{}", 100L, "admin"));
            assertEquals(ResourceErrorCode.RESOURCE_NAME_CONFLICT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("查询资源测试")
    class GetResourceTest {

        @Test
        @DisplayName("应该成功从数据库查询资源")
        void shouldGetResourceFromDatabase() {
            // Given
            Long resourceId = 1L;
            Resource mockResource = new Resource();
            mockResource.setId(resourceId);
            mockResource.setName("test-server");
            mockResource.setAttributes("{\"ip\":\"192.168.1.1\"}");

            when(cacheService.getResource(resourceId)).thenReturn(Optional.empty());
            when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(mockResource));

            // When
            Optional<Resource> result = resourceDomainService.getResourceById(resourceId);

            // Then
            assertTrue(result.isPresent());
            assertEquals("test-server", result.get().getName());
            // 验证结果被缓存
            verify(cacheService).cacheResource(eq(resourceId), anyString());
        }

        @Test
        @DisplayName("资源ID为null时应该返回空Optional")
        void shouldReturnEmptyWhenIdIsNull() {
            // When
            Optional<Resource> result = resourceDomainService.getResourceById(null);

            // Then
            assertTrue(result.isEmpty());
            verify(resourceRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("更新资源测试")
    class UpdateResourceTest {

        @Test
        @DisplayName("应该成功更新资源")
        void shouldUpdateResourceSuccessfully() {
            // Given
            Long resourceId = 1L;
            Resource existingResource = new Resource();
            existingResource.setId(resourceId);
            existingResource.setName("old-name");
            existingResource.setDescription("旧描述");
            existingResource.setResourceTypeId(1L);
            existingResource.setAttributes("{\"ip\":\"192.168.1.1\"}");
            existingResource.setVersion(0);

            when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
            when(resourceRepository.existsByNameAndTypeId("new-name", 1L)).thenReturn(false);
            when(resourceRepository.update(any(Resource.class))).thenReturn(true);

            // When
            Resource result = resourceDomainService.updateResource(resourceId, "new-name", "新描述",
                    "{\"ip\":\"192.168.1.2\"}", 0, 100L, "admin");

            // Then
            assertEquals("new-name", result.getName());
            assertEquals("新描述", result.getDescription());

            // 验证审计日志被记录
            verify(auditLogService).logUpdate(eq(resourceId), anyString(), anyString(), eq(100L), eq("admin"));
            // 验证缓存被清除
            verify(cacheService).evictResource(resourceId);
        }

        @Test
        @DisplayName("资源不存在时应该抛出业务异常")
        void shouldThrowExceptionWhenResourceNotFound() {
            // Given
            when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    resourceDomainService.updateResource(99L, "name", "desc", "{}", 0, 100L, "admin"));
            assertEquals(ResourceErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("版本号冲突时应该抛出业务异常")
        void shouldThrowExceptionWhenVersionConflict() {
            // Given
            Resource existingResource = new Resource();
            existingResource.setId(1L);
            existingResource.setVersion(5);

            when(resourceRepository.findById(1L)).thenReturn(Optional.of(existingResource));

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    resourceDomainService.updateResource(1L, "name", "desc", "{}", 3, 100L, "admin"));
            assertEquals(ResourceErrorCode.VERSION_CONFLICT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("删除资源测试")
    class DeleteResourceTest {

        @Test
        @DisplayName("应该成功删除资源")
        void shouldDeleteResourceSuccessfully() {
            // Given
            Long resourceId = 1L;
            Resource existingResource = new Resource();
            existingResource.setId(resourceId);
            existingResource.setName("test-server");

            when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));

            // When
            resourceDomainService.deleteResource(resourceId, "test-server", 100L, "admin");

            // Then
            verify(resourceTagRepository).deleteByResourceId(resourceId);
            verify(resourceRepository).deleteById(resourceId);
            verify(cacheService).evictResource(resourceId);
            verify(auditLogService).logDelete(eq(resourceId), anyString(), eq(100L), eq("admin"));
        }

        @Test
        @DisplayName("确认名称不匹配时应该抛出业务异常")
        void shouldThrowExceptionWhenNameMismatch() {
            // Given
            Resource existingResource = new Resource();
            existingResource.setId(1L);
            existingResource.setName("actual-name");

            when(resourceRepository.findById(1L)).thenReturn(Optional.of(existingResource));

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    resourceDomainService.deleteResource(1L, "wrong-name", 100L, "admin"));
            assertEquals(ResourceErrorCode.RESOURCE_NAME_MISMATCH.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("资源状态更新测试")
    class UpdateStatusTest {

        @Test
        @DisplayName("应该成功更新资源状态")
        void shouldUpdateStatusSuccessfully() {
            // Given
            Long resourceId = 1L;
            Resource existingResource = new Resource();
            existingResource.setId(resourceId);
            existingResource.setStatus(ResourceStatus.RUNNING);
            existingResource.setVersion(0);

            when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
            when(resourceRepository.updateStatus(resourceId, ResourceStatus.MAINTENANCE, 0)).thenReturn(true);

            // When
            resourceDomainService.updateResourceStatus(resourceId, ResourceStatus.MAINTENANCE, 0, 100L, "admin");

            // Then
            verify(cacheService).evictResource(resourceId);
            verify(auditLogService).logStatusChange(eq(resourceId), eq("RUNNING"), eq("MAINTENANCE"), eq(100L), eq("admin"));
        }
    }

    @Nested
    @DisplayName("权限检查测试")
    class PermissionCheckTest {

        @Test
        @DisplayName("管理员应该有权限操作所有资源")
        void adminShouldHavePermission() {
            // When & Then
            assertTrue(resourceDomainService.checkOwnerPermission(1L, 100L, true));
        }

        @Test
        @DisplayName("资源Owner应该有权限操作资源")
        void ownerShouldHavePermission() {
            // Given
            Resource resource = new Resource();
            resource.setId(1L);
            resource.setCreatedBy(100L);

            when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

            // When & Then
            assertTrue(resourceDomainService.checkOwnerPermission(1L, 100L, false));
        }

        @Test
        @DisplayName("非Owner非管理员不应该有权限操作资源")
        void nonOwnerNonAdminShouldNotHavePermission() {
            // Given
            Resource resource = new Resource();
            resource.setId(1L);
            resource.setCreatedBy(100L);

            when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

            // When & Then
            assertFalse(resourceDomainService.checkOwnerPermission(1L, 200L, false));
        }
    }

    @Nested
    @DisplayName("资源类型查询测试")
    class ResourceTypeQueryTest {

        @Test
        @DisplayName("应该返回资源类型详情")
        void shouldReturnResourceTypeById() {
            // Given
            ResourceType mockType = new ResourceType();
            mockType.setId(1L);
            mockType.setCode("SERVER");
            mockType.setName("服务器");

            when(resourceTypeRepository.findById(1L)).thenReturn(Optional.of(mockType));

            // When
            Optional<ResourceType> result = resourceDomainService.getResourceTypeById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals("SERVER", result.get().getCode());
        }
    }
}
