package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.domain.model.subgraph.SubgraphMember;
import com.catface996.aiops.domain.model.subgraph.SubgraphTopologyResult;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.domain.service.subgraph.SubgraphMemberDomainService.SubgraphMembersWithRelations;
import com.catface996.aiops.repository.subgraph.SubgraphMemberRepository;
import com.catface996.aiops.repository.subgraph.entity.SubgraphMemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 嵌套子图展开单元测试
 *
 * <p>测试子图嵌套展开的递归逻辑、深度限制和拓扑数据构建</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求9: 拓扑数据查询（嵌套展开）</li>
 *   <li>任务 T054: 嵌套展开单元测试</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@DisplayName("嵌套子图展开测试")
class NestedExpansionTest {

    private SubgraphMemberDomainServiceImpl memberDomainService;
    private SubgraphMemberRepository memberRepository;
    private RelationshipDomainService relationshipDomainService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(SubgraphMemberRepository.class);
        relationshipDomainService = mock(RelationshipDomainService.class);
        memberDomainService = new SubgraphMemberDomainServiceImpl(memberRepository, relationshipDomainService);
    }

    // ==================== expandNestedSubgraphs 测试 ====================

    @Nested
    @DisplayName("expandNestedSubgraphs 方法测试")
    class ExpandNestedSubgraphsTest {

        @Test
        @DisplayName("空子图应返回空列表")
        void shouldReturnEmptyListWhenSubgraphIsEmpty() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(subgraphId, 3, null, 0);

            // Then
            assertTrue(result.isEmpty());
            verify(memberRepository).findBySubgraphId(subgraphId);
        }

        @Test
        @DisplayName("达到最大深度时应停止展开")
        void shouldStopExpandingWhenMaxDepthReached() {
            // Given
            Long subgraphId = 1L;
            int maxDepth = 2;

            // When - 从深度2开始，已达到最大深度
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(subgraphId, maxDepth, null, 2);

            // Then
            assertTrue(result.isEmpty());
            verify(memberRepository, never()).findBySubgraphId(any());
        }

        @Test
        @DisplayName("单层展开 - 普通成员")
        void shouldExpandSingleLevelWithRegularMembers() {
            // Given
            Long subgraphId = 1L;
            SubgraphMemberEntity member1 = createMemberEntity(1L, subgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity member2 = createMemberEntity(2L, subgraphId, 102L, "Database-1", "DATABASE", false);

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Arrays.asList(member1, member2));

            // When
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(subgraphId, 3, null, 0);

            // Then
            assertEquals(2, result.size());
            assertFalse(result.get(0).isSubgraph());
            assertFalse(result.get(1).isSubgraph());
        }

        @Test
        @DisplayName("两层嵌套展开")
        void shouldExpandTwoLevelsOfNesting() {
            // Given
            // 结构: RootSubgraph(1) contains [Server(101), NestedSubgraph(2)]
            //       NestedSubgraph(2) contains [Database(201), Cache(202)]
            Long rootSubgraphId = 1L;
            Long nestedSubgraphId = 2L;

            SubgraphMemberEntity server = createMemberEntity(1L, rootSubgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity nested = createMemberEntity(2L, rootSubgraphId, nestedSubgraphId, "Backend-Services", "SUBGRAPH", true);

            SubgraphMemberEntity database = createMemberEntity(3L, nestedSubgraphId, 201L, "MySQL", "DATABASE", false);
            SubgraphMemberEntity cache = createMemberEntity(4L, nestedSubgraphId, 202L, "Redis", "CACHE", false);

            when(memberRepository.findBySubgraphId(rootSubgraphId)).thenReturn(Arrays.asList(server, nested));
            when(memberRepository.findBySubgraphId(nestedSubgraphId)).thenReturn(Arrays.asList(database, cache));

            // When
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(rootSubgraphId, 3, null, 0);

            // Then
            assertEquals(4, result.size());
            // 验证包含所有成员
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Server-1")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Backend-Services")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("MySQL")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Redis")));
        }

        @Test
        @DisplayName("三层嵌套展开")
        void shouldExpandThreeLevelsOfNesting() {
            // Given
            // 结构: Level1(1) -> Level2(2) -> Level3(3) -> Resource(301)
            Long level1Id = 1L;
            Long level2Id = 2L;
            Long level3Id = 3L;

            SubgraphMemberEntity level2Member = createMemberEntity(1L, level1Id, level2Id, "Level2", "SUBGRAPH", true);
            SubgraphMemberEntity level3Member = createMemberEntity(2L, level2Id, level3Id, "Level3", "SUBGRAPH", true);
            SubgraphMemberEntity resource = createMemberEntity(3L, level3Id, 301L, "DeepResource", "SERVER", false);

            when(memberRepository.findBySubgraphId(level1Id)).thenReturn(Arrays.asList(level2Member));
            when(memberRepository.findBySubgraphId(level2Id)).thenReturn(Arrays.asList(level3Member));
            when(memberRepository.findBySubgraphId(level3Id)).thenReturn(Arrays.asList(resource));

            // When
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(level1Id, 5, null, 0);

            // Then
            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Level2")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Level3")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("DeepResource")));
        }

        @Test
        @DisplayName("深度限制应截断嵌套展开")
        void shouldTruncateExpansionAtDepthLimit() {
            // Given
            // 结构: Level1(1) -> Level2(2) -> Level3(3)
            // maxDepth=2，Level3 不应被展开
            Long level1Id = 1L;
            Long level2Id = 2L;
            Long level3Id = 3L;

            SubgraphMemberEntity level2Member = createMemberEntity(1L, level1Id, level2Id, "Level2", "SUBGRAPH", true);
            SubgraphMemberEntity level3Member = createMemberEntity(2L, level2Id, level3Id, "Level3", "SUBGRAPH", true);
            SubgraphMemberEntity deepResource = createMemberEntity(3L, level3Id, 301L, "DeepResource", "SERVER", false);

            when(memberRepository.findBySubgraphId(level1Id)).thenReturn(Arrays.asList(level2Member));
            when(memberRepository.findBySubgraphId(level2Id)).thenReturn(Arrays.asList(level3Member));
            when(memberRepository.findBySubgraphId(level3Id)).thenReturn(Arrays.asList(deepResource));

            // When - maxDepth=2，从深度0开始
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(level1Id, 2, null, 0);

            // Then
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Level2")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Level3")));
            // DeepResource 不应被展开
            assertFalse(result.stream().anyMatch(m -> m.getMemberName().equals("DeepResource")));
            // Level3 的内容不应被查询
            verify(memberRepository, never()).findBySubgraphId(level3Id);
        }

        @Test
        @DisplayName("多分支嵌套展开")
        void shouldExpandMultipleBranches() {
            // Given
            // 结构: Root(1) contains [Branch1(2), Branch2(3)]
            //       Branch1(2) contains [Resource1(201)]
            //       Branch2(3) contains [Resource2(301)]
            Long rootId = 1L;
            Long branch1Id = 2L;
            Long branch2Id = 3L;

            SubgraphMemberEntity branch1 = createMemberEntity(1L, rootId, branch1Id, "Branch1", "SUBGRAPH", true);
            SubgraphMemberEntity branch2 = createMemberEntity(2L, rootId, branch2Id, "Branch2", "SUBGRAPH", true);
            SubgraphMemberEntity resource1 = createMemberEntity(3L, branch1Id, 201L, "Resource1", "SERVER", false);
            SubgraphMemberEntity resource2 = createMemberEntity(4L, branch2Id, 301L, "Resource2", "DATABASE", false);

            when(memberRepository.findBySubgraphId(rootId)).thenReturn(Arrays.asList(branch1, branch2));
            when(memberRepository.findBySubgraphId(branch1Id)).thenReturn(Arrays.asList(resource1));
            when(memberRepository.findBySubgraphId(branch2Id)).thenReturn(Arrays.asList(resource2));

            // When
            List<SubgraphMember> result = memberDomainService.expandNestedSubgraphs(rootId, 5, null, 0);

            // Then
            assertEquals(4, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Branch1")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Branch2")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Resource1")));
            assertTrue(result.stream().anyMatch(m -> m.getMemberName().equals("Resource2")));
        }
    }

    // ==================== getMembersWithRelations 测试 ====================

    @Nested
    @DisplayName("getMembersWithRelations 方法测试")
    class GetMembersWithRelationsTest {

        @Test
        @DisplayName("不展开嵌套时返回直接成员")
        void shouldReturnDirectMembersWhenNotExpanding() {
            // Given
            Long subgraphId = 1L;
            SubgraphMemberEntity member1 = createMemberEntity(1L, subgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity nested = createMemberEntity(2L, subgraphId, 2L, "NestedSubgraph", "SUBGRAPH", true);

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Arrays.asList(member1, nested));

            // When
            SubgraphMembersWithRelations result = memberDomainService.getMembersWithRelations(subgraphId, false, 3);

            // Then
            assertEquals(subgraphId, result.getSubgraphId());
            assertEquals(2, result.getMembers().size());
            assertEquals(2, result.getNodeCount());
            // 不展开时，嵌套子图不应查询其内部成员
            verify(memberRepository, times(1)).findBySubgraphId(subgraphId);
        }

        @Test
        @DisplayName("展开嵌套时返回所有成员")
        void shouldReturnAllMembersWhenExpanding() {
            // Given
            Long rootSubgraphId = 1L;
            Long nestedSubgraphId = 2L;

            SubgraphMemberEntity server = createMemberEntity(1L, rootSubgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity nested = createMemberEntity(2L, rootSubgraphId, nestedSubgraphId, "Backend", "SUBGRAPH", true);
            SubgraphMemberEntity database = createMemberEntity(3L, nestedSubgraphId, 201L, "MySQL", "DATABASE", false);

            when(memberRepository.findBySubgraphId(rootSubgraphId)).thenReturn(Arrays.asList(server, nested));
            when(memberRepository.findBySubgraphId(nestedSubgraphId)).thenReturn(Arrays.asList(database));

            // When
            SubgraphMembersWithRelations result = memberDomainService.getMembersWithRelations(rootSubgraphId, true, 3);

            // Then
            assertEquals(rootSubgraphId, result.getSubgraphId());
            assertEquals(3, result.getMembers().size());
            assertEquals(3, result.getNodeCount());
            // 验证嵌套子图信息
            assertFalse(result.getNestedSubgraphs().isEmpty());
        }

        @Test
        @DisplayName("应正确设置 maxDepth")
        void shouldSetMaxDepthCorrectly() {
            // Given
            Long subgraphId = 1L;
            int maxDepth = 5;

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            SubgraphMembersWithRelations result = memberDomainService.getMembersWithRelations(subgraphId, false, maxDepth);

            // Then
            assertEquals(maxDepth, result.getMaxDepth());
        }

        @Test
        @DisplayName("无成员时边数应为0")
        void shouldHaveZeroEdgesWhenNoMembers() {
            // Given
            Long subgraphId = 1L;

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            SubgraphMembersWithRelations result = memberDomainService.getMembersWithRelations(subgraphId, false, 3);

            // Then
            assertEquals(0, result.getEdgeCount());
            assertTrue(result.getRelationships().isEmpty());
        }
    }

    // ==================== getSubgraphTopology 测试 ====================

    @Nested
    @DisplayName("getSubgraphTopology 方法测试")
    class GetSubgraphTopologyTest {

        @Test
        @DisplayName("不展开时返回直接成员的拓扑")
        void shouldReturnDirectMemberTopologyWhenNotExpanding() {
            // Given
            Long subgraphId = 1L;
            SubgraphMemberEntity member1 = createMemberEntity(1L, subgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity member2 = createMemberEntity(2L, subgraphId, 102L, "Server-2", "SERVER", false);

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Arrays.asList(member1, member2));

            // When
            SubgraphTopologyResult result = memberDomainService.getSubgraphTopology(subgraphId, false);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getNodes().size());
        }

        @Test
        @DisplayName("展开时返回所有成员的拓扑")
        void shouldReturnAllMemberTopologyWhenExpanding() {
            // Given
            Long rootSubgraphId = 1L;
            Long nestedSubgraphId = 2L;

            SubgraphMemberEntity server = createMemberEntity(1L, rootSubgraphId, 101L, "Server", "SERVER", false);
            SubgraphMemberEntity nested = createMemberEntity(2L, rootSubgraphId, nestedSubgraphId, "Nested", "SUBGRAPH", true);
            SubgraphMemberEntity database = createMemberEntity(3L, nestedSubgraphId, 201L, "DB", "DATABASE", false);

            when(memberRepository.findBySubgraphId(rootSubgraphId)).thenReturn(Arrays.asList(server, nested));
            when(memberRepository.findBySubgraphId(nestedSubgraphId)).thenReturn(Arrays.asList(database));

            // When
            SubgraphTopologyResult result = memberDomainService.getSubgraphTopology(rootSubgraphId, true);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getNodes().size());
        }

        @Test
        @DisplayName("应正确构建子图边界")
        void shouldBuildSubgraphBoundariesCorrectly() {
            // Given
            Long rootSubgraphId = 1L;
            SubgraphMemberEntity member1 = createMemberEntity(1L, rootSubgraphId, 101L, "Server-1", "SERVER", false);
            SubgraphMemberEntity member2 = createMemberEntity(2L, rootSubgraphId, 102L, "Server-2", "SERVER", false);

            when(memberRepository.findBySubgraphId(rootSubgraphId)).thenReturn(Arrays.asList(member1, member2));

            // When
            SubgraphTopologyResult result = memberDomainService.getSubgraphTopology(rootSubgraphId, false);

            // Then
            assertNotNull(result);
            assertFalse(result.getSubgraphBoundaries().isEmpty());
            // 验证边界包含所有成员
            assertTrue(result.getSubgraphBoundaries().stream()
                    .anyMatch(b -> b.getSubgraphId().equals(rootSubgraphId)));
        }

        @Test
        @DisplayName("空子图应返回空拓扑")
        void shouldReturnEmptyTopologyForEmptySubgraph() {
            // Given
            Long subgraphId = 1L;

            when(memberRepository.findBySubgraphId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            SubgraphTopologyResult result = memberDomainService.getSubgraphTopology(subgraphId, false);

            // Then
            assertNotNull(result);
            assertTrue(result.getNodes().isEmpty());
            assertTrue(result.getEdges().isEmpty());
        }
    }

    // ==================== 辅助方法 ====================

    private SubgraphMemberEntity createMemberEntity(Long id, Long subgraphId, Long memberId,
                                                     String memberName, String memberTypeCode, boolean isSubgraph) {
        SubgraphMemberEntity entity = new SubgraphMemberEntity(subgraphId, memberId, 1L);
        entity.setId(id);
        entity.setMemberName(memberName);
        entity.setMemberTypeCode(memberTypeCode);
        entity.setMemberStatus("ACTIVE");
        entity.setIsSubgraph(isSubgraph);
        entity.setNestedMemberCount(isSubgraph ? 0 : null);
        entity.setAddedAt(LocalDateTime.now());
        return entity;
    }
}
