package com.catface996.aiops.domain.model.diagnosis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent诊断过程领域实体
 *
 * <p>记录单个Agent在一次诊断任务中的诊断过程，内容为整合后的完整文本。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>一个诊断任务可关联多个Agent诊断过程</li>
 *   <li>每个Agent诊断过程通过 agent_bound_id 关联到具体的Agent绑定</li>
 *   <li>content 为从Redis整合后的完整文本</li>
 *   <li>agent_name 冗余存储，避免JOIN查询</li>
 *   <li>即使Agent无输出，也创建记录，content 标记为"无输出"</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentDiagnosisProcess {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联诊断任务ID
     */
    private Long taskId;

    /**
     * 关联AgentBound ID
     */
    private Long agentBoundId;

    /**
     * Agent名称（冗余存储）
     */
    private String agentName;

    /**
     * 诊断内容（整合后的完整文本）
     */
    private String content;

    /**
     * Agent开始诊断时间
     */
    private LocalDateTime startedAt;

    /**
     * Agent结束诊断时间
     */
    private LocalDateTime endedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    // ==================== 常量 ====================

    /**
     * 无输出时的默认内容
     */
    public static final String NO_OUTPUT_CONTENT = "无输出";

    // ==================== 工厂方法 ====================

    /**
     * 创建新的Agent诊断过程记录
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @param agentName    Agent名称
     * @return 新的Agent诊断过程记录
     */
    public static AgentDiagnosisProcess create(Long taskId, Long agentBoundId, String agentName) {
        return AgentDiagnosisProcess.builder()
                .taskId(taskId)
                .agentBoundId(agentBoundId)
                .agentName(agentName)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    /**
     * 创建无输出的Agent诊断过程记录
     *
     * @param taskId       诊断任务ID
     * @param agentBoundId Agent绑定ID
     * @param agentName    Agent名称
     * @return 无输出的Agent诊断过程记录
     */
    public static AgentDiagnosisProcess createNoOutput(Long taskId, Long agentBoundId, String agentName) {
        return AgentDiagnosisProcess.builder()
                .taskId(taskId)
                .agentBoundId(agentBoundId)
                .agentName(agentName)
                .content(NO_OUTPUT_CONTENT)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 设置诊断内容（整合后的完整文本）
     *
     * @param content 诊断内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 设置开始时间
     *
     * @param startedAt 开始时间
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * 设置结束时间
     *
     * @param endedAt 结束时间
     */
    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    /**
     * 检查是否有输出
     *
     * @return true 如果有有效输出
     */
    public boolean hasOutput() {
        return content != null && !content.isEmpty() && !NO_OUTPUT_CONTENT.equals(content);
    }

    /**
     * 获取诊断时长（秒）
     *
     * @return 诊断时长秒数，如果未完成返回0
     */
    public long getDurationSeconds() {
        if (startedAt == null || endedAt == null) {
            return 0;
        }
        return java.time.Duration.between(startedAt, endedAt).getSeconds();
    }

    /**
     * 获取内容长度
     *
     * @return 内容长度（字符数），如果内容为空返回0
     */
    public int getContentLength() {
        return content != null ? content.length() : 0;
    }
}
