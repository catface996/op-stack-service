package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportRequest;
import com.catface996.aiops.application.api.dto.report.request.GetReportRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportsRequest;
import com.catface996.aiops.application.api.service.report.ReportApplicationService;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报告管理控制器（POST-Only API）
 *
 * <p>提供报告管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>报告管理接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/reports/list - 查询报告列表</li>
 *   <li>POST /api/service/v1/reports/get - 查询报告详情</li>
 *   <li>POST /api/service/v1/reports/create - 创建报告</li>
 *   <li>POST /api/service/v1/reports/delete - 删除报告</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/reports")
@RequiredArgsConstructor
@Tag(name = "报告管理", description = "报告管理接口：查询、创建、删除报告（POST-Only API）")
public class ReportController {

    private final ReportApplicationService reportApplicationService;

    /**
     * 查询报告列表
     *
     * <p>分页查询报告列表，支持按类型、状态筛选和关键词搜索。</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查询报告列表", description = "分页查询报告列表，支持按类型、状态筛选和关键词搜索")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<ReportDTO>>> listReports(
            @Valid @RequestBody ListReportsRequest request) {

        log.info("查询报告列表，type: {}, status: {}, keyword: {}, page: {}, size: {}",
                request.getType(), request.getStatus(), request.getKeyword(),
                request.getPage(), request.getSize());

        PageResult<ReportDTO> result = reportApplicationService.listReports(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取报告详情
     *
     * <p>查看报告详情，包含完整的 Markdown 内容。</p>
     */
    @PostMapping("/get")
    @Operation(summary = "获取报告详情", description = "查看报告详情，包含完整的 Markdown 内容")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReportDTO.class))),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "报告不存在")
    })
    public ResponseEntity<Result<ReportDTO>> getReport(
            @Valid @RequestBody GetReportRequest request) {

        log.info("获取报告详情，id: {}", request.getId());

        ReportDTO report = reportApplicationService.getReportById(request.getId());

        return ResponseEntity.ok(Result.success(report));
    }

    /**
     * 创建报告
     *
     * <p>创建新的报告。报告创建后不可修改。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建报告", description = "创建新的报告。报告创建后不可修改（immutable）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ReportDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效（标题为空、类型无效等）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "关联的拓扑不存在")
    })
    public ResponseEntity<Result<ReportDTO>> createReport(
            @Valid @RequestBody CreateReportRequest request) {

        log.info("创建报告，title: {}, type: {}, author: {}",
                request.getTitle(), request.getType(), request.getAuthor());

        ReportDTO report = reportApplicationService.createReport(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("报告创建成功", report));
    }

    /**
     * 删除报告
     *
     * <p>软删除报告，删除后报告不可查询。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除报告", description = "软删除报告，删除后报告不可查询")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "报告不存在")
    })
    public ResponseEntity<Result<Void>> deleteReport(
            @Valid @RequestBody DeleteReportRequest request) {

        log.info("删除报告，id: {}", request.getId());

        reportApplicationService.deleteReport(request);

        return ResponseEntity.ok(Result.success("报告删除成功", null));
    }
}
