package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.repository.subgraph.SubgraphMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 循环检测算法单元测试
 *
 * <p>测试子图嵌套时的循环引用检测逻辑</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 添加成员（循环检测）</li>
 *   <li>任务 T027: 循环检测单元测试</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@DisplayName("循环检测算法测试")
class CycleDetectionTest {

    private SubgraphMemberDomainServiceImpl memberDomainService;
    private SubgraphMemberRepository memberRepository;
    private RelationshipDomainService relationshipDomainService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(SubgraphMemberRepository.class);
        relationshipDomainService = mock(RelationshipDomainService.class);
        memberDomainService = new SubgraphMemberDomainServiceImpl(memberRepository, relationshipDomainService);
    }

    // ==================== wouldCreateCycle 测试 ====================

    @Nested
    @DisplayName("wouldCreateCycle 方法测试")
    class WouldCreateCycleTest {

        @Test
        @DisplayName("非子图类型成员不会产生循环")
        void shouldReturnFalseWhenCandidateIsNotSubgraph() {
            // Given
            Long subgraphId = 1L;
            Long candidateMemberId = 100L; // 普通资源，不是子图

            when(memberRepository.isSubgraphType(candidateMemberId)).thenReturn(false);

            // When
            boolean result = memberDomainService.wouldCreateCycle(subgraphId, candidateMemberId);

            // Then
            assertFalse(result);
            verify(memberRepository).isSubgraphType(candidateMemberId);
        }

        @Test
        @DisplayName("添加自己到自己会产生循环")
        void shouldReturnTrueWhenAddingSelfToSelf() {
            // Given
            Long subgraphId = 1L;
            Long candidateMemberId = 1L; // 自己

            when(memberRepository.isSubgraphType(candidateMemberId)).thenReturn(true);

            // When
            boolean result = memberDomainService.wouldCreateCycle(subgraphId, candidateMemberId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("添加祖先子图会产生循环 - 直接父级")
        void shouldReturnTrueWhenAddingDirectParent() {
            // Given
            // 结构: Parent(2) contains Child(1)
            // 尝试将 Parent(2) 添加到 Child(1) 中会形成循环
            Long childSubgraphId = 1L;
            Long parentSubgraphId = 2L;

            when(memberRepository.isSubgraphType(parentSubgraphId)).thenReturn(true);
            // Child(1) 的父级是 Parent(2)
            when(memberRepository.findSubgraphIdsByMemberId(childSubgraphId)).thenReturn(Arrays.asList(parentSubgraphId));
            when(memberRepository.findSubgraphIdsByMemberId(parentSubgraphId)).thenReturn(Collections.emptyList());

            // When
            boolean result = memberDomainService.wouldCreateCycle(childSubgraphId, parentSubgraphId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("添加祖先子图会产生循环 - 祖父级")
        void shouldReturnTrueWhenAddingGrandparent() {
            // Given
            // 结构: GrandParent(3) contains Parent(2) contains Child(1)
            // 尝试将 GrandParent(3) 添加到 Child(1) 中会形成循环
            Long childSubgraphId = 1L;
            Long parentSubgraphId = 2L;
            Long grandparentSubgraphId = 3L;

            when(memberRepository.isSubgraphType(grandparentSubgraphId)).thenReturn(true);
            // Child(1) 的父级是 Parent(2)
            when(memberRepository.findSubgraphIdsByMemberId(childSubgraphId)).thenReturn(Arrays.asList(parentSubgraphId));
            // Parent(2) 的父级是 GrandParent(3)
            when(memberRepository.findSubgraphIdsByMemberId(parentSubgraphId)).thenReturn(Arrays.asList(grandparentSubgraphId));
            // GrandParent(3) 没有父级
            when(memberRepository.findSubgraphIdsByMemberId(grandparentSubgraphId)).thenReturn(Collections.emptyList());

            // When
            boolean result = memberDomainService.wouldCreateCycle(childSubgraphId, grandparentSubgraphId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("添加非祖先子图不会产生循环")
        void shouldReturnFalseWhenAddingNonAncestor() {
            // Given
            // 结构: Parent(2) contains Child(1), Sibling(3) 是独立的
            // 尝试将 Sibling(3) 添加到 Child(1) 中不会形成循环
            Long childSubgraphId = 1L;
            Long siblingSubgraphId = 3L;

            when(memberRepository.isSubgraphType(siblingSubgraphId)).thenReturn(true);
            // Child(1) 的父级是 Parent(2)
            when(memberRepository.findSubgraphIdsByMemberId(childSubgraphId)).thenReturn(Arrays.asList(2L));
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Collections.emptyList());

            // When
            boolean result = memberDomainService.wouldCreateCycle(childSubgraphId, siblingSubgraphId);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("多重继承场景 - 添加共同祖先会产生循环")
        void shouldReturnTrueWhenAddingCommonAncestor() {
            // Given
            // 结构: Root(4) contains [Parent1(2), Parent2(3)],
            //       Parent1(2) contains Child(1)
            //       Parent2(3) contains Child(1)
            // 尝试将 Root(4) 添加到 Child(1) 会形成循环
            Long childSubgraphId = 1L;
            Long rootSubgraphId = 4L;

            when(memberRepository.isSubgraphType(rootSubgraphId)).thenReturn(true);
            // Child(1) 的父级是 [Parent1(2), Parent2(3)]
            when(memberRepository.findSubgraphIdsByMemberId(childSubgraphId)).thenReturn(Arrays.asList(2L, 3L));
            // Parent1(2) 和 Parent2(3) 的父级都是 Root(4)
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Arrays.asList(rootSubgraphId));
            when(memberRepository.findSubgraphIdsByMemberId(3L)).thenReturn(Arrays.asList(rootSubgraphId));
            when(memberRepository.findSubgraphIdsByMemberId(rootSubgraphId)).thenReturn(Collections.emptyList());

            // When
            boolean result = memberDomainService.wouldCreateCycle(childSubgraphId, rootSubgraphId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("深层嵌套场景 - 多级祖先检测")
        void shouldDetectCycleInDeepNesting() {
            // Given
            // 结构: Level5(5) -> Level4(4) -> Level3(3) -> Level2(2) -> Level1(1)
            // 尝试将 Level5(5) 添加到 Level1(1) 会形成循环
            Long level1 = 1L;
            Long level5 = 5L;

            when(memberRepository.isSubgraphType(level5)).thenReturn(true);
            when(memberRepository.findSubgraphIdsByMemberId(1L)).thenReturn(Arrays.asList(2L));
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Arrays.asList(3L));
            when(memberRepository.findSubgraphIdsByMemberId(3L)).thenReturn(Arrays.asList(4L));
            when(memberRepository.findSubgraphIdsByMemberId(4L)).thenReturn(Arrays.asList(5L));
            when(memberRepository.findSubgraphIdsByMemberId(5L)).thenReturn(Collections.emptyList());

            // When
            boolean result = memberDomainService.wouldCreateCycle(level1, level5);

            // Then
            assertTrue(result);
        }
    }

    // ==================== getAncestorSubgraphIds 测试 ====================

    @Nested
    @DisplayName("getAncestorSubgraphIds 方法测试")
    class GetAncestorSubgraphIdsTest {

        @Test
        @DisplayName("没有父级的子图应返回空集合")
        void shouldReturnEmptySetWhenNoParents() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.findSubgraphIdsByMemberId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            Set<Long> ancestors = memberDomainService.getAncestorSubgraphIds(subgraphId);

            // Then
            assertTrue(ancestors.isEmpty());
        }

        @Test
        @DisplayName("应返回所有祖先 - 单链结构")
        void shouldReturnAllAncestorsInSingleChain() {
            // Given
            // 结构: GrandParent(3) -> Parent(2) -> Child(1)
            Long childSubgraphId = 1L;

            when(memberRepository.findSubgraphIdsByMemberId(1L)).thenReturn(Arrays.asList(2L));
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Arrays.asList(3L));
            when(memberRepository.findSubgraphIdsByMemberId(3L)).thenReturn(Collections.emptyList());

            // When
            Set<Long> ancestors = memberDomainService.getAncestorSubgraphIds(childSubgraphId);

            // Then
            assertEquals(2, ancestors.size());
            assertTrue(ancestors.contains(2L));
            assertTrue(ancestors.contains(3L));
        }

        @Test
        @DisplayName("应返回所有祖先 - 多父级结构")
        void shouldReturnAllAncestorsWithMultipleParents() {
            // Given
            // 结构: Root(4) -> [Parent1(2), Parent2(3)] -> Child(1)
            Long childSubgraphId = 1L;

            when(memberRepository.findSubgraphIdsByMemberId(1L)).thenReturn(Arrays.asList(2L, 3L));
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Arrays.asList(4L));
            when(memberRepository.findSubgraphIdsByMemberId(3L)).thenReturn(Arrays.asList(4L));
            when(memberRepository.findSubgraphIdsByMemberId(4L)).thenReturn(Collections.emptyList());

            // When
            Set<Long> ancestors = memberDomainService.getAncestorSubgraphIds(childSubgraphId);

            // Then
            assertEquals(3, ancestors.size());
            assertTrue(ancestors.contains(2L));
            assertTrue(ancestors.contains(3L));
            assertTrue(ancestors.contains(4L));
        }

        @Test
        @DisplayName("应避免重复计算共同祖先")
        void shouldNotCountCommonAncestorsTwice() {
            // Given
            // 结构: CommonAncestor(5) -> [Path1: 3->2, Path2: 4->2] -> Child(1)
            Long childSubgraphId = 1L;

            // Child 有两个父级
            when(memberRepository.findSubgraphIdsByMemberId(1L)).thenReturn(Arrays.asList(2L));
            // Parent 有两个父级，这两个父级有共同祖先
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Arrays.asList(3L, 4L));
            when(memberRepository.findSubgraphIdsByMemberId(3L)).thenReturn(Arrays.asList(5L));
            when(memberRepository.findSubgraphIdsByMemberId(4L)).thenReturn(Arrays.asList(5L));
            when(memberRepository.findSubgraphIdsByMemberId(5L)).thenReturn(Collections.emptyList());

            // When
            Set<Long> ancestors = memberDomainService.getAncestorSubgraphIds(childSubgraphId);

            // Then
            assertEquals(4, ancestors.size()); // 2, 3, 4, 5 各只出现一次
            assertTrue(ancestors.contains(2L));
            assertTrue(ancestors.contains(3L));
            assertTrue(ancestors.contains(4L));
            assertTrue(ancestors.contains(5L));
        }
    }
}
