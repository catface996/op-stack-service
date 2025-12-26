package com.catface996.aiops.domain.service.prompt;

import com.catface996.aiops.domain.model.prompt.TemplateUsage;

import java.util.List;
import java.util.Optional;

/**
 * 模板用途领域服务接口
 *
 * <p>提供模板用途管理的核心业务逻辑。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface TemplateUsageDomainService {

    /**
     * 创建用途
     *
     * @param code        用途编码
     * @param name        用途名称
     * @param description 用途描述
     * @return 创建的用途
     */
    TemplateUsage createUsage(String code, String name, String description);

    /**
     * 获取所有用途
     *
     * @return 用途列表
     */
    List<TemplateUsage> listUsages();

    /**
     * 根据ID获取用途
     *
     * @param usageId 用途ID
     * @return 用途实体
     */
    Optional<TemplateUsage> getUsageById(Long usageId);

    /**
     * 删除用途（软删除）
     *
     * @param usageId 用途ID
     */
    void deleteUsage(Long usageId);

    /**
     * 检查用途是否存在
     *
     * @param usageId 用途ID
     * @return true if usage exists
     */
    boolean existsById(Long usageId);
}
