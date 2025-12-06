package com.catface996.aiops.application.impl.service.subgraph;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDetailDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphTopologyDTO;
import com.catface996.aiops.application.api.dto.subgraph.request.*;
import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.domain.model.subgraph.SubgraphTopology;
import com.catface996.aiops.domain.service.subgraph.SubgraphDomainService;
import com.catface996.aiops.repository.resource.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 子图应用服务实现类单元测试
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务14-19: 应用服务测试</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@DisplayName("子图应用服务测试")
class SubgraphApplicationServiceImplTest {

    private SubgraphApplicationServiceImpl subgraphApplicationService;
    private SubgraphDomainService subgraphDomainService;
    private ResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        subgraphDomainService = mock(SubgraphDomainService.class);
        resourceRepository = mock(ResourceRepository.class);
        subgraphApplicationService = new SubgraphApplicationServiceImpl(subgraphDomainService, resourceRepository);
    }

    // ==================== 任务14: 创建子图测试 ====================

    @Nested
    @DisplayName("创建子图测试")
    class CreateSubgraphTest {

        @Test
        @DisplayName("应该成功创建子图并返回DTO")
        void shouldCreateSubgraphAndReturnDTO() {
            // Given
            CreateSubgraphRequest request = CreateSubgraphRequest.builder()
                    .name("test-subgraph")
                    .description("Test description")
                    .tags(Arrays.asList("tag1", "tag2"))
                    .metadata(Map.of("key", "value"))
                    .build();

            Subgraph createdSubgraph = createTestSubgraph(1L, "test-subgraph");

            when(subgraphDomainService.createSubgraph(
                    eq("test-subgraph"), eq("Test description"),
                    any(), any(), eq(100L), eq("admin")
            )).thenReturn(createdSubgraph);

            // When
            SubgraphDTO result = subgraphApplicationService.createSubgraph(request, 100L, "admin");

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("test-subgraph", result.getName());
            assertEquals(0, result.getResourceCount());
            verify(subgraphDomainService).createSubgraph(any(), any(), any(), any(), any(), any());
        }
    }

    // ==================== 任务15: 查询子图测试 ====================

    @Nested
    @DisplayName("查询子图测试")
    class QuerySubgraphTest {

        @Test
        @DisplayName("应该成功获取子图列表")
        void shouldListSubgraphs() {
            // Given
            ListSubgraphsRequest request = ListSubgraphsRequest.builder()
                    .page(1)
                    .size(10)
                    .build();

            List<Subgraph> subgraphs = Arrays.asList(
                    createTestSubgraph(1L, "subgraph-1"),
                    createTestSubgraph(2L, "subgraph-2")
            );

            when(subgraphDomainService.listSubgraphs(100L, 1, 10)).thenReturn(subgraphs);
            when(subgraphDomainService.countSubgraphs(100L)).thenReturn(2L);
            when(subgraphDomainService.countResources(anyLong())).thenReturn(5);

            // When
            PageResult<SubgraphDTO> result = subgraphApplicationService.listSubgraphs(request, 100L);

            // Then
            assertEquals(2, result.getContent().size());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("应该支持关键词搜索")
        void shouldSearchByKeyword() {
            // Given
            ListSubgraphsRequest request = ListSubgraphsRequest.builder()
                    .keyword("production")
                    .page(1)
                    .size(10)
                    .build();

            List<Subgraph> subgraphs = Collections.singletonList(
                    createTestSubgraph(1L, "production-network")
            );

            when(subgraphDomainService.searchSubgraphs("production", 100L, 1, 10)).thenReturn(subgraphs);
            when(subgraphDomainService.countSearchSubgraphs("production", 100L)).thenReturn(1L);
            when(subgraphDomainService.countResources(1L)).thenReturn(10);

            // When
            PageResult<SubgraphDTO> result = subgraphApplicationService.listSubgraphs(request, 100L);

            // Then
            assertEquals(1, result.getContent().size());
            assertTrue(result.getContent().get(0).getName().contains("production"));
        }

        @Test
        @DisplayName("应该成功获取子图详情")
        void shouldGetSubgraphDetail() {
            // Given
            Subgraph subgraph = createTestSubgraph(1L, "test-subgraph");
            List<SubgraphPermission> permissions = Arrays.asList(
                    createTestPermission(1L, 1L, 100L, PermissionRole.OWNER)
            );
            List<Long> resourceIds = Arrays.asList(10L, 20L, 30L);

            when(subgraphDomainService.getSubgraphDetail(1L, 100L)).thenReturn(Optional.of(subgraph));
            when(subgraphDomainService.getPermissions(1L)).thenReturn(permissions);
            when(subgraphDomainService.getResourceIds(1L, 100L)).thenReturn(resourceIds);

            // When
            SubgraphDetailDTO result = subgraphApplicationService.getSubgraphDetail(1L, 100L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1, result.getPermissions().size());
            assertEquals(3, result.getResourceIds().size());
            assertEquals(3, result.getResourceCount());
        }
    }

    // ==================== 任务16: 更新子图测试 ====================

    @Nested
    @DisplayName("更新子图测试")
    class UpdateSubgraphTest {

        @Test
        @DisplayName("应该成功更新子图")
        void shouldUpdateSubgraph() {
            // Given
            UpdateSubgraphRequest request = UpdateSubgraphRequest.builder()
                    .name("new-name")
                    .description("new description")
                    .version(1)
                    .build();

            Subgraph updatedSubgraph = createTestSubgraph(1L, "new-name");

            when(subgraphDomainService.updateSubgraph(
                    eq(1L), eq("new-name"), eq("new description"),
                    any(), any(), eq(1), eq(100L), eq("admin")
            )).thenReturn(updatedSubgraph);
            when(subgraphDomainService.countResources(1L)).thenReturn(5);

            // When
            SubgraphDTO result = subgraphApplicationService.updateSubgraph(1L, request, 100L, "admin");

            // Then
            assertNotNull(result);
            assertEquals("new-name", result.getName());
            assertEquals(5, result.getResourceCount());
        }
    }

    // ==================== 任务17: 删除子图测试 ====================

    @Nested
    @DisplayName("删除子图测试")
    class DeleteSubgraphTest {

        @Test
        @DisplayName("应该成功删除子图")
        void shouldDeleteSubgraph() {
            // Given
            doNothing().when(subgraphDomainService).deleteSubgraph(1L, 100L, "admin");

            // When
            subgraphApplicationService.deleteSubgraph(1L, 100L, "admin");

            // Then
            verify(subgraphDomainService).deleteSubgraph(1L, 100L, "admin");
        }
    }

    // ==================== 任务18: 资源管理测试 ====================

    @Nested
    @DisplayName("资源管理测试")
    class ResourceManagementTest {

        @Test
        @DisplayName("应该成功添加资源到子图")
        void shouldAddResources() {
            // Given
            AddResourcesRequest request = AddResourcesRequest.builder()
                    .resourceIds(Arrays.asList(10L, 20L, 30L))
                    .build();

            doNothing().when(subgraphDomainService).addResources(
                    eq(1L), eq(Arrays.asList(10L, 20L, 30L)), eq(100L), eq("admin")
            );

            // When
            subgraphApplicationService.addResources(1L, request, 100L, "admin");

            // Then
            verify(subgraphDomainService).addResources(1L, Arrays.asList(10L, 20L, 30L), 100L, "admin");
        }

        @Test
        @DisplayName("应该成功从子图移除资源")
        void shouldRemoveResources() {
            // Given
            RemoveResourcesRequest request = RemoveResourcesRequest.builder()
                    .resourceIds(Arrays.asList(10L, 20L))
                    .build();

            doNothing().when(subgraphDomainService).removeResources(
                    eq(1L), eq(Arrays.asList(10L, 20L)), eq(100L), eq("admin")
            );

            // When
            subgraphApplicationService.removeResources(1L, request, 100L, "admin");

            // Then
            verify(subgraphDomainService).removeResources(1L, Arrays.asList(10L, 20L), 100L, "admin");
        }
    }

    // ==================== 权限管理测试 ====================

    @Nested
    @DisplayName("权限管理测试")
    class PermissionManagementTest {

        @Test
        @DisplayName("应该成功添加权限")
        void shouldAddPermission() {
            // Given
            UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                    .userId(200L)
                    .role("VIEWER")
                    .build();

            doNothing().when(subgraphDomainService).addPermission(
                    eq(1L), eq(200L), eq(PermissionRole.VIEWER), eq(100L), eq("admin")
            );

            // When
            subgraphApplicationService.addPermission(1L, request, 100L, "admin");

            // Then
            verify(subgraphDomainService).addPermission(1L, 200L, PermissionRole.VIEWER, 100L, "admin");
        }

        @Test
        @DisplayName("应该成功移除权限")
        void shouldRemovePermission() {
            // Given
            doNothing().when(subgraphDomainService).removePermission(1L, 200L, 100L, "admin");

            // When
            subgraphApplicationService.removePermission(1L, 200L, 100L, "admin");

            // Then
            verify(subgraphDomainService).removePermission(1L, 200L, 100L, "admin");
        }
    }

    // ==================== 任务19: 拓扑查询测试 ====================

    @Nested
    @DisplayName("拓扑查询测试")
    class TopologyQueryTest {

        @Test
        @DisplayName("应该成功获取子图拓扑")
        void shouldGetSubgraphTopology() {
            // Given
            SubgraphTopology topology = new SubgraphTopology(
                    1L, "test-subgraph",
                    Arrays.asList(10L, 20L, 30L),
                    Collections.emptyList()
            );

            when(subgraphDomainService.getSubgraphTopology(1L, 100L)).thenReturn(topology);

            // When
            SubgraphTopologyDTO result = subgraphApplicationService.getSubgraphTopology(1L, 100L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getSubgraphId());
            assertEquals("test-subgraph", result.getSubgraphName());
            assertEquals(3, result.getNodeCount());
            assertEquals(0, result.getEdgeCount());
        }
    }

    // ==================== 辅助方法 ====================

    private Subgraph createTestSubgraph(Long id, String name) {
        Subgraph subgraph = new Subgraph();
        subgraph.setId(id);
        subgraph.setName(name);
        subgraph.setDescription("Test description for " + name);
        subgraph.setTags(new ArrayList<>());
        subgraph.setMetadata(new HashMap<>());
        subgraph.setCreatedBy(100L);
        subgraph.setCreatedAt(LocalDateTime.now());
        subgraph.setUpdatedAt(LocalDateTime.now());
        subgraph.setVersion(0);
        return subgraph;
    }

    private SubgraphPermission createTestPermission(Long id, Long subgraphId, Long userId, PermissionRole role) {
        SubgraphPermission permission = new SubgraphPermission();
        permission.setId(id);
        permission.setSubgraphId(subgraphId);
        permission.setUserId(userId);
        permission.setRole(role);
        permission.setGrantedAt(LocalDateTime.now());
        permission.setGrantedBy(userId);
        return permission;
    }
}
