package com.catface996.aiops.repository.mysql.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.report.ReportPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报告 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Mapper
public interface ReportMapper extends BaseMapper<ReportPO> {

    /**
     * 分页查询报告列表
     *
     * @param page      分页参数
     * @param type      报告类型筛选（可选）
     * @param status    报告状态筛选（可选）
     * @param keyword   关键词搜索（可选，搜索 title, summary）
     * @param sortBy    排序字段（可选）
     * @param sortOrder 排序方向（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM reports " +
            "<where>" +
            "deleted = 0 " +
            "<if test='type != null and type != \"\"'>" +
            "AND type = #{type} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "<choose>" +
            "<when test='sortBy == \"title\"'>" +
            "ORDER BY title " +
            "</when>" +
            "<when test='sortBy == \"type\"'>" +
            "ORDER BY type " +
            "</when>" +
            "<when test='sortBy == \"status\"'>" +
            "ORDER BY status " +
            "</when>" +
            "<otherwise>" +
            "ORDER BY created_at " +
            "</otherwise>" +
            "</choose>" +
            "<if test='sortOrder == \"asc\"'>" +
            "ASC" +
            "</if>" +
            "<if test='sortOrder != \"asc\"'>" +
            "DESC" +
            "</if>" +
            "</script>")
    IPage<ReportPO> selectPageByCondition(Page<ReportPO> page,
                                           @Param("type") String type,
                                           @Param("status") String status,
                                           @Param("keyword") String keyword,
                                           @Param("sortBy") String sortBy,
                                           @Param("sortOrder") String sortOrder);

    /**
     * 按条件统计报告数量
     *
     * @param type    报告类型筛选（可选）
     * @param status  报告状态筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return 报告数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM reports " +
            "<where>" +
            "deleted = 0 " +
            "<if test='type != null and type != \"\"'>" +
            "AND type = #{type} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("type") String type,
                          @Param("status") String status,
                          @Param("keyword") String keyword);
}
