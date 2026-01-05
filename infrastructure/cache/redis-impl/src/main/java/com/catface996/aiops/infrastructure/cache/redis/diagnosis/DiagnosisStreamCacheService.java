package com.catface996.aiops.infrastructure.cache.redis.diagnosis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 诊断流式数据缓存服务
 *
 * <p>使用 Redis List 存储 executor 流式响应数据，按 Agent 维度分类存储。</p>
 *
 * <p>Key 命名规范：</p>
 * <ul>
 *   <li>诊断任务 Agent 列表索引：diagnosis:task:{taskId}:agents</li>
 *   <li>Agent 流式数据：diagnosis:task:{taskId}:agent:{agentBoundId}</li>
 *   <li>Agent 元数据：diagnosis:task:{taskId}:agent:{agentBoundId}:meta</li>
 * </ul>
 *
 * <p>TTL: 24小时（86400秒）</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Service
public class DiagnosisStreamCacheService {

    private static final String KEY_PREFIX = "diagnosis:task:";
    private static final String AGENTS_SUFFIX = ":agents";
    private static final String AGENT_INFIX = ":agent:";
    private static final String META_SUFFIX = ":meta";

    /**
     * 默认 TTL: 24小时
     */
    private static final long DEFAULT_TTL_SECONDS = 86400L;

    /**
     * 元数据字段
     */
    private static final String META_AGENT_NAME = "agentName";
    private static final String META_STARTED_AT = "startedAt";
    private static final String META_ENDED_AT = "endedAt";

    private final RedisTemplate<String, Object> redisTemplate;

    public DiagnosisStreamCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 写入操作 ====================

    /**
     * 追加流式数据片段到指定 Agent
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @param agentName    Agent名称
     * @param content      流式内容片段
     */
    public void appendStreamContent(Long taskId, Long agentBoundId, String agentName, String content) {
        String dataKey = buildAgentDataKey(taskId, agentBoundId);
        String agentsKey = buildAgentsIndexKey(taskId);

        // 追加数据到 List
        redisTemplate.opsForList().rightPush(dataKey, content);
        redisTemplate.expire(dataKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);

        // 添加 agentBoundId 到索引 Set
        redisTemplate.opsForSet().add(agentsKey, agentBoundId.toString());
        redisTemplate.expire(agentsKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);

        // 保存/更新 Agent 元数据（首次时设置 agentName 和 startedAt）
        String metaKey = buildAgentMetaKey(taskId, agentBoundId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(metaKey))) {
            Map<String, String> meta = new HashMap<>();
            meta.put(META_AGENT_NAME, agentName);
            meta.put(META_STARTED_AT, LocalDateTime.now().toString());
            redisTemplate.opsForHash().putAll(metaKey, meta);
            redisTemplate.expire(metaKey, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * 标记 Agent 诊断结束
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     */
    public void markAgentEnded(Long taskId, Long agentBoundId) {
        String metaKey = buildAgentMetaKey(taskId, agentBoundId);
        redisTemplate.opsForHash().put(metaKey, META_ENDED_AT, LocalDateTime.now().toString());
    }

    // ==================== 读取操作 ====================

    /**
     * 获取诊断任务的所有 Agent ID 列表
     *
     * @param taskId 诊断任务ID
     * @return Agent绑定ID列表
     */
    public Set<Long> getAgentBoundIds(Long taskId) {
        String agentsKey = buildAgentsIndexKey(taskId);
        Set<Object> members = redisTemplate.opsForSet().members(agentsKey);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(obj -> Long.parseLong(obj.toString()))
                .collect(Collectors.toSet());
    }

    /**
     * 获取指定 Agent 的完整流式内容（合并所有片段）
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @return 完整内容字符串
     */
    public String getAgentContent(Long taskId, Long agentBoundId) {
        String dataKey = buildAgentDataKey(taskId, agentBoundId);
        List<Object> contents = redisTemplate.opsForList().range(dataKey, 0, -1);
        if (contents == null || contents.isEmpty()) {
            return "";
        }
        return contents.stream()
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * 获取指定 Agent 的元数据
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @return 元数据 Map（包含 agentName, startedAt, endedAt）
     */
    public Map<String, String> getAgentMeta(Long taskId, Long agentBoundId) {
        String metaKey = buildAgentMetaKey(taskId, agentBoundId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(metaKey);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue() != null ? e.getValue().toString() : ""
                ));
    }

    /**
     * 获取 Agent 名称
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @return Agent名称，如果不存在返回空字符串
     */
    public String getAgentName(Long taskId, Long agentBoundId) {
        Map<String, String> meta = getAgentMeta(taskId, agentBoundId);
        return meta.getOrDefault(META_AGENT_NAME, "");
    }

    /**
     * 获取 Agent 开始时间
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @return 开始时间，如果不存在返回 null
     */
    public LocalDateTime getAgentStartedAt(Long taskId, Long agentBoundId) {
        Map<String, String> meta = getAgentMeta(taskId, agentBoundId);
        String startedAt = meta.get(META_STARTED_AT);
        return startedAt != null ? LocalDateTime.parse(startedAt) : null;
    }

    /**
     * 获取 Agent 结束时间
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @return 结束时间，如果不存在返回 null
     */
    public LocalDateTime getAgentEndedAt(Long taskId, Long agentBoundId) {
        Map<String, String> meta = getAgentMeta(taskId, agentBoundId);
        String endedAt = meta.get(META_ENDED_AT);
        return endedAt != null ? LocalDateTime.parse(endedAt) : null;
    }

    // ==================== 清理操作 ====================

    /**
     * 清理诊断任务的所有 Redis 数据
     *
     * <p>在数据成功持久化到数据库后调用</p>
     *
     * @param taskId 诊断任务ID
     */
    public void cleanupTaskData(Long taskId) {
        Set<Long> agentBoundIds = getAgentBoundIds(taskId);

        // 删除所有 Agent 数据和元数据
        for (Long agentBoundId : agentBoundIds) {
            String dataKey = buildAgentDataKey(taskId, agentBoundId);
            String metaKey = buildAgentMetaKey(taskId, agentBoundId);
            redisTemplate.delete(dataKey);
            redisTemplate.delete(metaKey);
        }

        // 删除 Agent 索引
        String agentsKey = buildAgentsIndexKey(taskId);
        redisTemplate.delete(agentsKey);
    }

    /**
     * 检查诊断任务是否有缓存数据
     *
     * @param taskId 诊断任务ID
     * @return true 如果有缓存数据
     */
    public boolean hasTaskData(Long taskId) {
        String agentsKey = buildAgentsIndexKey(taskId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(agentsKey));
    }

    // ==================== Key 构建方法 ====================

    private String buildAgentsIndexKey(Long taskId) {
        return KEY_PREFIX + taskId + AGENTS_SUFFIX;
    }

    private String buildAgentDataKey(Long taskId, Long agentBoundId) {
        return KEY_PREFIX + taskId + AGENT_INFIX + agentBoundId;
    }

    private String buildAgentMetaKey(Long taskId, Long agentBoundId) {
        return KEY_PREFIX + taskId + AGENT_INFIX + agentBoundId + META_SUFFIX;
    }
}
