package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportTemplateDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.GetReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportTemplatesRequest;
import com.catface996.aiops.application.api.dto.report.request.UpdateReportTemplateRequest;
import com.catface996.aiops.application.api.service.report.ReportTemplateApplicationService;
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
 * 报告模板管理控制器（POST-Only API）
 *
 * <p>提供报告模板管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>模板管理接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/report-templates/list - 查询模板列表</li>
 *   <li>POST /api/service/v1/report-templates/get - 查询模板详情</li>
 *   <li>POST /api/service/v1/report-templates/create - 创建模板</li>
 *   <li>POST /api/service/v1/report-templates/update - 更新模板</li>
 *   <li>POST /api/service/v1/report-templates/delete - 删除模板</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/report-templates")
@RequiredArgsConstructor
@Tag(name = "报告模板管理", description = "报告模板管理接口：查询、创建、更新、删除模板（POST-Only API）")
public class ReportTemplateController {

    private final ReportTemplateApplicationService reportTemplateApplicationService;

    /**
     * 查询模板列表
     *
     * <p>分页查询报告模板列表，支持按分类筛选和关键词搜索。</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查询模板列表", description = "分页查询报告模板列表，支持按分类筛选和关键词搜索")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<ReportTemplateDTO>>> listTemplates(
            @Valid @RequestBody ListReportTemplatesRequest request) {

        log.info("查询模板列表，category: {}, keyword: {}, page: {}, size: {}",
                request.getCategory(), request.getKeyword(), request.getPage(), request.getSize());

        PageResult<ReportTemplateDTO> result = reportTemplateApplicationService.listTemplates(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取模板详情
     *
     * <p>查看模板详情，包含完整的模板内容（含占位符）。</p>
     */
    @PostMapping("/get")
    @Operation(summary = "获取模板详情", description = "查看模板详情，包含完整的模板内容（含占位符）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReportTemplateDTO.class))),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<Result<ReportTemplateDTO>> getTemplate(
            @Valid @RequestBody GetReportTemplateRequest request) {

        log.info("获取模板详情，id: {}", request.getId());

        ReportTemplateDTO template = reportTemplateApplicationService.getTemplateById(request.getId());

        return ResponseEntity.ok(Result.success(template));
    }

    /**
     * 创建模板
     *
     * <p>创建新的报告模板。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建模板", description = "创建新的报告模板")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = ReportTemplateDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效（名称为空、分类无效、内容为空等）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "模板名称已存在")
    })
    public ResponseEntity<Result<ReportTemplateDTO>> createTemplate(
            @Valid @RequestBody CreateReportTemplateRequest request) {

        log.info("创建模板，name: {}, category: {}", request.getName(), request.getCategory());

        ReportTemplateDTO template = reportTemplateApplicationService.createTemplate(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("模板创建成功", template));
    }

    /**
     * 更新模板
     *
     * <p>更新报告模板信息，使用乐观锁进行并发控制。</p>
     */
    @PostMapping("/update")
    @Operation(summary = "更新模板", description = "更新报告模板信息，使用乐观锁进行并发控制")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ReportTemplateDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突或名称已存在")
    })
    public ResponseEntity<Result<ReportTemplateDTO>> updateTemplate(
            @Valid @RequestBody UpdateReportTemplateRequest request) {

        log.info("更新模板，id: {}, expectedVersion: {}", request.getId(), request.getExpectedVersion());

        ReportTemplateDTO template = reportTemplateApplicationService.updateTemplate(request);

        return ResponseEntity.ok(Result.success("模板更新成功", template));
    }

    /**
     * 删除模板
     *
     * <p>软删除报告模板，删除后模板不可查询。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除模板", description = "软删除报告模板，删除后模板不可查询")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<Result<Void>> deleteTemplate(
            @Valid @RequestBody DeleteReportTemplateRequest request) {

        log.info("删除模板，id: {}", request.getId());

        reportTemplateApplicationService.deleteTemplate(request);

        return ResponseEntity.ok(Result.success("模板删除成功", null));
    }
}
