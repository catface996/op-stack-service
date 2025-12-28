package com.catface996.aiops.repository.report;

import com.catface996.aiops.domain.model.report.Report;
import com.catface996.aiops.domain.model.report.ReportStatus;
import com.catface996.aiops.domain.model.report.ReportType;

import java.util.List;
import java.util.Optional;

/**
 * 报告仓储接口
 *
 * <p>提供报告实体的数据访问操作。报告创建后不可修改，因此不提供 update 方法。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface ReportRepository {

    /**
     * 根据ID查询报告
     *
     * @param id 报告ID
     * @return 报告实体（如果存在且未删除）
     */
    Optional<Report> findById(Long id);

    /**
     * 分页查询报告列表
     *
     * @param type      报告类型筛选（可选）
     * @param status    报告状态筛选（可选）
     * @param keyword   关键词搜索（可选，搜索 title, summary, tags）
     * @param sortBy    排序字段（可选，默认 created_at）
     * @param sortOrder 排序方向（可选，默认 desc）
     * @param page      页码（从1开始）
     * @param size      每页大小
     * @return 报告列表
     */
    List<Report> findByCondition(ReportType type, ReportStatus status, String keyword,
                                  String sortBy, String sortOrder, int page, int size);

    /**
     * 按条件统计报告数量
     *
     * @param type    报告类型筛选（可选）
     * @param status  报告状态筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return 报告数量
     */
    long countByCondition(ReportType type, ReportStatus status, String keyword);

    /**
     * 保存报告
     *
     * @param report 报告实体
     * @return 保存后的报告实体（包含生成的ID）
     */
    Report save(Report report);

    /**
     * 删除报告（软删除）
     *
     * @param id 报告ID
     * @return 删除是否成功
     */
    boolean deleteById(Long id);

    /**
     * 检查报告是否存在
     *
     * @param id 报告ID
     * @return true if report exists and not deleted
     */
    boolean existsById(Long id);
}
