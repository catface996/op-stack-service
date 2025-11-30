package com.catface996.aiops.domain.model.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源领域模型测试
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@DisplayName("资源实体测试")
class ResourceEntityTest {

    @Nested
    @DisplayName("创建资源测试")
    class CreateResourceTest {

        @Test
        @DisplayName("应该成功创建资源并设置默认状态")
        void shouldCreateResourceWithDefaultStatus() {
            // Given
            String name = "test-server";
            String description = "测试服务器";
            Long resourceTypeId = 1L;
            String attributes = "{\"ip\":\"192.168.1.1\"}";
            Long createdBy = 100L;

            // When
            Resource resource = Resource.create(name, description, resourceTypeId, attributes, createdBy);

            // Then
            assertNotNull(resource);
            assertEquals(name, resource.getName());
            assertEquals(description, resource.getDescription());
            assertEquals(resourceTypeId, resource.getResourceTypeId());
            assertEquals(attributes, resource.getAttributes());
            assertEquals(createdBy, resource.getCreatedBy());
            assertEquals(ResourceStatus.RUNNING, resource.getStatus(), "默认状态应该是 RUNNING");
            assertEquals(0, resource.getVersion(), "初始版本号应该是 0");
            assertNotNull(resource.getCreatedAt());
            assertNotNull(resource.getUpdatedAt());
        }

        @Test
        @DisplayName("创建资源时描述可以为空")
        void shouldCreateResourceWithNullDescription() {
            // When
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);

            // Then
            assertNotNull(resource);
            assertNull(resource.getDescription());
        }
    }

    @Nested
    @DisplayName("更新资源测试")
    class UpdateResourceTest {

        @Test
        @DisplayName("应该成功更新资源信息")
        void shouldUpdateResourceInfo() {
            // Given
            Resource resource = Resource.create("old-name", "旧描述", 1L, "{}", 100L);
            String newName = "new-name";
            String newDescription = "新描述";
            String newAttributes = "{\"ip\":\"192.168.1.2\"}";

            // When
            resource.update(newName, newDescription, newAttributes);

            // Then
            assertEquals(newName, resource.getName());
            assertEquals(newDescription, resource.getDescription());
            assertEquals(newAttributes, resource.getAttributes());
        }

        @Test
        @DisplayName("更新时传入null应该保持原值")
        void shouldKeepOriginalValueWhenUpdateWithNull() {
            // Given
            Resource resource = Resource.create("original-name", "原描述", 1L, "{\"key\":\"value\"}", 100L);
            String originalName = resource.getName();
            String originalDescription = resource.getDescription();
            String originalAttributes = resource.getAttributes();

            // When
            resource.update(null, null, null);

            // Then
            assertEquals(originalName, resource.getName());
            assertEquals(originalDescription, resource.getDescription());
            assertEquals(originalAttributes, resource.getAttributes());
        }
    }

    @Nested
    @DisplayName("资源状态测试")
    class ResourceStatusTest {

        @Test
        @DisplayName("RUNNING状态 isRunning() 应该返回 true")
        void runningStatusShouldReturnTrue() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);
            resource.setStatus(ResourceStatus.RUNNING);

            // Then
            assertTrue(resource.isRunning());
            assertFalse(resource.isStopped());
            assertFalse(resource.isInMaintenance());
            assertFalse(resource.isOffline());
        }

        @Test
        @DisplayName("STOPPED状态 isStopped() 应该返回 true")
        void stoppedStatusShouldReturnTrue() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);
            resource.setStatus(ResourceStatus.STOPPED);

            // Then
            assertFalse(resource.isRunning());
            assertTrue(resource.isStopped());
            assertFalse(resource.isInMaintenance());
            assertFalse(resource.isOffline());
        }

        @Test
        @DisplayName("MAINTENANCE状态 isInMaintenance() 应该返回 true")
        void maintenanceStatusShouldReturnTrue() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);
            resource.setStatus(ResourceStatus.MAINTENANCE);

            // Then
            assertFalse(resource.isRunning());
            assertFalse(resource.isStopped());
            assertTrue(resource.isInMaintenance());
            assertFalse(resource.isOffline());
        }

        @Test
        @DisplayName("OFFLINE状态 isOffline() 应该返回 true")
        void offlineStatusShouldReturnTrue() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);
            resource.setStatus(ResourceStatus.OFFLINE);

            // Then
            assertFalse(resource.isRunning());
            assertFalse(resource.isStopped());
            assertFalse(resource.isInMaintenance());
            assertTrue(resource.isOffline());
        }
    }

    @Nested
    @DisplayName("资源所有权测试")
    class OwnershipTest {

        @Test
        @DisplayName("创建者应该是资源的Owner")
        void creatorShouldBeOwner() {
            // Given
            Long creatorId = 100L;
            Resource resource = Resource.create("server", null, 1L, "{}", creatorId);

            // Then
            assertTrue(resource.isOwner(creatorId));
        }

        @Test
        @DisplayName("非创建者不应该是资源的Owner")
        void nonCreatorShouldNotBeOwner() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);

            // Then
            assertFalse(resource.isOwner(200L));
        }

        @Test
        @DisplayName("null用户ID不应该是Owner")
        void nullUserIdShouldNotBeOwner() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);

            // Then
            assertFalse(resource.isOwner(null));
        }
    }

    @Nested
    @DisplayName("版本控制测试")
    class VersionControlTest {

        @Test
        @DisplayName("新资源版本号应该是0")
        void newResourceShouldHaveVersionZero() {
            // When
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);

            // Then
            assertEquals(0, resource.getVersion());
        }

        @Test
        @DisplayName("incrementVersion应该增加版本号")
        void incrementVersionShouldIncreaseVersion() {
            // Given
            Resource resource = Resource.create("server", null, 1L, "{}", 100L);
            assertEquals(0, resource.getVersion());

            // When
            resource.incrementVersion();

            // Then
            assertEquals(1, resource.getVersion());
        }
    }
}
