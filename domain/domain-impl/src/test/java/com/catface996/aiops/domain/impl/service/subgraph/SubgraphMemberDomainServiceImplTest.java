package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.domain.model.subgraph.SubgraphMember;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.repository.subgraph.SubgraphMemberRepository;
import com.catface996.aiops.repository.subgraph.entity.SubgraphMemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 子图成员领域服务实现类单元测试
 *
 * <p>v2.0 设计：测试子图成员管理的核心业务逻辑</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>任务 T028: addMembers 单元测试</li>
 *   <li>任务 T035: removeMembers 单元测试</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@DisplayName("子图成员领域服务测试 v2.0")
class SubgraphMemberDomainServiceImplTest {

    private SubgraphMemberDomainServiceImpl memberDomainService;
    private SubgraphMemberRepository memberRepository;
    private RelationshipDomainService relationshipDomainService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(SubgraphMemberRepository.class);
        relationshipDomainService = mock(RelationshipDomainService.class);
        memberDomainService = new SubgraphMemberDomainServiceImpl(memberRepository, relationshipDomainService);
    }

    // ==================== T028: addMembers 测试 ====================

    @Nested
    @DisplayName("添加成员测试 (T028)")
    class AddMembersTest {

        @Test
        @DisplayName("应该成功添加成员到子图")
        void shouldAddMembersSuccessfully() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L, 20L, 30L);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(0);
            when(memberRepository.existsBySubgraphIdAndMemberId(eq(subgraphId), anyLong())).thenReturn(false);
            when(memberRepository.filterSubgraphTypeIds(anyList())).thenReturn(Collections.emptyList());
            when(memberRepository.batchSave(anyList())).thenReturn(3);

            // When
            int result = memberDomainService.addMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(3, result);
            verify(memberRepository).batchSave(argThat(entities -> entities.size() == 3));
        }

        @Test
        @DisplayName("子图ID为空时应抛出异常")
        void shouldThrowExceptionWhenSubgraphIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () ->
                    memberDomainService.addMembers(null, Arrays.asList(1L, 2L), 100L));
        }

        @Test
        @DisplayName("操作者ID为空时应抛出异常")
        void shouldThrowExceptionWhenOperatorIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () ->
                    memberDomainService.addMembers(1L, Arrays.asList(1L, 2L), null));
        }

        @Test
        @DisplayName("成员列表为空时应返回0")
        void shouldReturnZeroWhenMemberIdsEmpty() {
            // When
            int result = memberDomainService.addMembers(1L, Collections.emptyList(), 100L);

            // Then
            assertEquals(0, result);
            verify(memberRepository, never()).batchSave(anyList());
        }

        @Test
        @DisplayName("成员列表为null时应返回0")
        void shouldReturnZeroWhenMemberIdsNull() {
            // When
            int result = memberDomainService.addMembers(1L, null, 100L);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("资源不是子图类型时应抛出异常")
        void shouldThrowExceptionWhenNotSubgraphType() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(false);

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    memberDomainService.addMembers(subgraphId, Arrays.asList(10L), 100L));
            assertTrue(ex.getMessage().contains("不是子图类型"));
        }

        @Test
        @DisplayName("超过成员数量限制时应抛出异常")
        void shouldThrowExceptionWhenExceedingMemberLimit() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(495); // 已有495个

            // When & Then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    memberDomainService.addMembers(subgraphId, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), 100L)); // 再添加6个会超过500
            assertTrue(ex.getMessage().contains("超过成员数量限制"));
        }

        @Test
        @DisplayName("应该跳过已存在的成员")
        void shouldSkipExistingMembers() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L, 20L, 30L);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(0);
            // 10L 已存在
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 10L)).thenReturn(true);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 20L)).thenReturn(false);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 30L)).thenReturn(false);
            when(memberRepository.filterSubgraphTypeIds(anyList())).thenReturn(Collections.emptyList());
            when(memberRepository.batchSave(anyList())).thenReturn(2);

            // When
            int result = memberDomainService.addMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(2, result);
            verify(memberRepository).batchSave(argThat(entities -> entities.size() == 2));
        }

        @Test
        @DisplayName("所有成员都已存在时应返回0")
        void shouldReturnZeroWhenAllMembersExist() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L, 20L);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(2);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 10L)).thenReturn(true);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 20L)).thenReturn(true);

            // When
            int result = memberDomainService.addMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(0, result);
            verify(memberRepository, never()).batchSave(anyList());
        }

        @Test
        @DisplayName("添加子图成员时检测到循环应抛出异常")
        void shouldThrowExceptionWhenCycleDetected() {
            // Given
            Long subgraphId = 1L;
            Long ancestorSubgraphId = 2L; // 这是 subgraphId 的祖先
            List<Long> memberIds = Arrays.asList(ancestorSubgraphId);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(0);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, ancestorSubgraphId)).thenReturn(false);
            // 候选成员是子图类型
            when(memberRepository.filterSubgraphTypeIds(Arrays.asList(ancestorSubgraphId))).thenReturn(Arrays.asList(ancestorSubgraphId));
            // 模拟祖先关系：subgraphId(1) 的父级是 ancestorSubgraphId(2)
            when(memberRepository.isSubgraphType(ancestorSubgraphId)).thenReturn(true);
            when(memberRepository.findSubgraphIdsByMemberId(subgraphId)).thenReturn(Arrays.asList(ancestorSubgraphId));
            when(memberRepository.findSubgraphIdsByMemberId(ancestorSubgraphId)).thenReturn(Collections.emptyList());

            // When & Then
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    memberDomainService.addMembers(subgraphId, memberIds, operatorId));
            assertTrue(ex.getMessage().contains("循环引用"));
        }

        @Test
        @DisplayName("添加非祖先子图成员应成功")
        void shouldSucceedWhenAddingNonAncestorSubgraph() {
            // Given
            Long subgraphId = 1L;
            Long siblingSubgraphId = 3L; // 这不是 subgraphId 的祖先
            List<Long> memberIds = Arrays.asList(siblingSubgraphId);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(0);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, siblingSubgraphId)).thenReturn(false);
            when(memberRepository.filterSubgraphTypeIds(Arrays.asList(siblingSubgraphId))).thenReturn(Arrays.asList(siblingSubgraphId));
            // siblingSubgraphId 是子图类型
            when(memberRepository.isSubgraphType(siblingSubgraphId)).thenReturn(true);
            // subgraphId 的父级是 2L，不包含 siblingSubgraphId
            when(memberRepository.findSubgraphIdsByMemberId(subgraphId)).thenReturn(Arrays.asList(2L));
            when(memberRepository.findSubgraphIdsByMemberId(2L)).thenReturn(Collections.emptyList());
            when(memberRepository.batchSave(anyList())).thenReturn(1);

            // When
            int result = memberDomainService.addMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("应该正确设置添加时间和添加者")
        void shouldSetAddedAtAndAddedBy() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L);
            Long operatorId = 100L;

            when(memberRepository.isSubgraphType(subgraphId)).thenReturn(true);
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(0);
            when(memberRepository.existsBySubgraphIdAndMemberId(subgraphId, 10L)).thenReturn(false);
            when(memberRepository.filterSubgraphTypeIds(anyList())).thenReturn(Collections.emptyList());
            when(memberRepository.batchSave(anyList())).thenReturn(1);

            // When
            memberDomainService.addMembers(subgraphId, memberIds, operatorId);

            // Then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SubgraphMemberEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(memberRepository).batchSave(captor.capture());
            List<SubgraphMemberEntity> entities = captor.getValue();

            assertEquals(1, entities.size());
            SubgraphMemberEntity entity = entities.get(0);
            assertEquals(subgraphId, entity.getSubgraphId());
            assertEquals(10L, entity.getMemberId());
            assertEquals(operatorId, entity.getAddedBy());
            assertNotNull(entity.getAddedAt());
        }
    }

    // ==================== T035: removeMembers 测试 ====================

    @Nested
    @DisplayName("移除成员测试 (T035)")
    class RemoveMembersTest {

        @Test
        @DisplayName("应该成功移除成员")
        void shouldRemoveMembersSuccessfully() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L, 20L, 30L);
            Long operatorId = 100L;

            when(memberRepository.batchDelete(subgraphId, memberIds)).thenReturn(3);

            // When
            int result = memberDomainService.removeMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(3, result);
            verify(memberRepository).batchDelete(subgraphId, memberIds);
        }

        @Test
        @DisplayName("子图ID为空时应返回0")
        void shouldReturnZeroWhenSubgraphIdIsNull() {
            // When
            int result = memberDomainService.removeMembers(null, Arrays.asList(1L), 100L);

            // Then
            assertEquals(0, result);
            verify(memberRepository, never()).batchDelete(anyLong(), anyList());
        }

        @Test
        @DisplayName("成员列表为空时应返回0")
        void shouldReturnZeroWhenMemberIdsEmpty() {
            // When
            int result = memberDomainService.removeMembers(1L, Collections.emptyList(), 100L);

            // Then
            assertEquals(0, result);
            verify(memberRepository, never()).batchDelete(anyLong(), anyList());
        }

        @Test
        @DisplayName("成员列表为null时应返回0")
        void shouldReturnZeroWhenMemberIdsNull() {
            // When
            int result = memberDomainService.removeMembers(1L, null, 100L);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("部分成员不存在时应返回实际移除数量")
        void shouldReturnActualRemovedCount() {
            // Given
            Long subgraphId = 1L;
            List<Long> memberIds = Arrays.asList(10L, 20L, 30L);
            Long operatorId = 100L;

            // 只有2个成员实际被移除
            when(memberRepository.batchDelete(subgraphId, memberIds)).thenReturn(2);

            // When
            int result = memberDomainService.removeMembers(subgraphId, memberIds, operatorId);

            // Then
            assertEquals(2, result);
        }
    }

    // ==================== 成员查询测试 ====================

    @Nested
    @DisplayName("成员查询测试")
    class MemberQueryTest {

        @Test
        @DisplayName("应该成功分页查询成员列表")
        void shouldListMembersPaged() {
            // Given
            Long subgraphId = 1L;
            int page = 1;
            int size = 10;

            SubgraphMemberEntity entity1 = createTestEntity(1L, subgraphId, 10L);
            SubgraphMemberEntity entity2 = createTestEntity(2L, subgraphId, 20L);

            when(memberRepository.findBySubgraphIdPaged(subgraphId, 0, size))
                    .thenReturn(Arrays.asList(entity1, entity2));

            // When
            List<SubgraphMember> result = memberDomainService.getMembersBySubgraphIdPaged(subgraphId, page, size);

            // Then
            assertEquals(2, result.size());
            assertEquals(10L, result.get(0).getMemberId());
            assertEquals(20L, result.get(1).getMemberId());
        }

        @Test
        @DisplayName("子图ID为空时应返回空列表")
        void shouldReturnEmptyListWhenSubgraphIdIsNull() {
            // When
            List<SubgraphMember> result = memberDomainService.getMembersBySubgraphIdPaged(null, 1, 10);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("应该正确计算成员数量")
        void shouldCountMembersCorrectly() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.countBySubgraphId(subgraphId)).thenReturn(5);

            // When
            int result = memberDomainService.countMembers(subgraphId);

            // Then
            assertEquals(5, result);
        }

        @Test
        @DisplayName("子图ID为空时成员数量应返回0")
        void shouldReturnZeroCountWhenSubgraphIdIsNull() {
            // When
            int result = memberDomainService.countMembers(null);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("应该正确获取成员ID列表")
        void shouldGetMemberIds() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.findMemberIdsBySubgraphId(subgraphId)).thenReturn(Arrays.asList(10L, 20L, 30L));

            // When
            List<Long> result = memberDomainService.getMemberIds(subgraphId);

            // Then
            assertEquals(3, result.size());
            assertTrue(result.containsAll(Arrays.asList(10L, 20L, 30L)));
        }
    }

    // ==================== isSubgraphEmpty 测试 ====================

    @Nested
    @DisplayName("子图空检查测试")
    class IsSubgraphEmptyTest {

        @Test
        @DisplayName("空子图应返回true")
        void shouldReturnTrueWhenEmpty() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.hasMembers(subgraphId)).thenReturn(false);

            // When
            boolean result = memberDomainService.isSubgraphEmpty(subgraphId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("非空子图应返回false")
        void shouldReturnFalseWhenNotEmpty() {
            // Given
            Long subgraphId = 1L;
            when(memberRepository.hasMembers(subgraphId)).thenReturn(true);

            // When
            boolean result = memberDomainService.isSubgraphEmpty(subgraphId);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("子图ID为空时应返回true")
        void shouldReturnTrueWhenSubgraphIdIsNull() {
            // When
            boolean result = memberDomainService.isSubgraphEmpty(null);

            // Then
            assertTrue(result);
        }
    }

    // ==================== isSubgraphType 测试 ====================

    @Nested
    @DisplayName("子图类型检查测试")
    class IsSubgraphTypeTest {

        @Test
        @DisplayName("子图类型应返回true")
        void shouldReturnTrueForSubgraphType() {
            // Given
            Long resourceId = 1L;
            when(memberRepository.isSubgraphType(resourceId)).thenReturn(true);

            // When
            boolean result = memberDomainService.isSubgraphType(resourceId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("非子图类型应返回false")
        void shouldReturnFalseForNonSubgraphType() {
            // Given
            Long resourceId = 1L;
            when(memberRepository.isSubgraphType(resourceId)).thenReturn(false);

            // When
            boolean result = memberDomainService.isSubgraphType(resourceId);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("资源ID为空时应返回false")
        void shouldReturnFalseWhenResourceIdIsNull() {
            // When
            boolean result = memberDomainService.isSubgraphType(null);

            // Then
            assertFalse(result);
        }
    }

    // ==================== 辅助方法 ====================

    private SubgraphMemberEntity createTestEntity(Long id, Long subgraphId, Long memberId) {
        SubgraphMemberEntity entity = new SubgraphMemberEntity(subgraphId, memberId, 100L);
        entity.setId(id);
        entity.setAddedAt(LocalDateTime.now());
        entity.setMemberName("Resource-" + memberId);
        entity.setMemberTypeCode("SERVER");
        entity.setMemberStatus("RUNNING");
        entity.setIsSubgraph(false);
        entity.setNestedMemberCount(0);
        return entity;
    }
}
