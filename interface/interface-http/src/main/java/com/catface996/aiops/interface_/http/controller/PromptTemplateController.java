package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDetailDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateVersionDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreatePromptTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.DeleteTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.GetTemplateDetailRequest;
import com.catface996.aiops.application.api.dto.prompt.request.GetVersionDetailRequest;
import com.catface996.aiops.application.api.dto.prompt.request.ListPromptTemplatesRequest;
import com.catface996.aiops.application.api.dto.prompt.request.RollbackTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.UpdatePromptTemplateRequest;
import com.catface996.aiops.application.api.service.prompt.PromptTemplateApplicationService;
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
 * 提示词模板管理控制器（POST-Only API）
 *
 * <p>提供提示词模板管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>模板管理接口：</p>
 * <ul>
 *   <li>POST /api/service/v1/prompt-templates/create - 创建模板</li>
 *   <li>POST /api/service/v1/prompt-templates/list - 查询模板列表</li>
 *   <li>POST /api/service/v1/prompt-templates/detail - 查询模板详情</li>
 *   <li>POST /api/service/v1/prompt-templates/update - 更新模板内容</li>
 *   <li>POST /api/service/v1/prompt-templates/rollback - 回滚到历史版本</li>
 *   <li>POST /api/service/v1/prompt-templates/delete - 删除模板</li>
 *   <li>POST /api/service/v1/prompt-templates/version/detail - 查询指定版本详情</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/service/v1/prompt-templates")
@RequiredArgsConstructor
@Tag(name = "提示词模板管理", description = "提示词模板管理接口：创建、查询、更新、回滚、删除（POST-Only API）")
public class PromptTemplateController {

    private final PromptTemplateApplicationService promptTemplateApplicationService;

    /**
     * 创建提示词模板
     *
     * <p>创建新的提示词模板，系统返回模板 ID 和版本号 1。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建提示词模板", description = "创建新的提示词模板，包含名称、内容、用途，系统返回模板 ID 和版本号 1")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = PromptTemplateDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效（内容为空或超过64KB限制）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "用途不存在"),
            @ApiResponse(responseCode = "409", description = "模板名称已存在")
    })
    public ResponseEntity<Result<PromptTemplateDTO>> createTemplate(
            @Valid @RequestBody CreatePromptTemplateRequest request) {

        log.info("创建提示词模板，name: {}, usageId: {}, operatorId: {}",
                request.getName(), request.getUsageId(), request.getOperatorId());

        PromptTemplateDTO template = promptTemplateApplicationService.createPromptTemplate(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("模板创建成功", template));
    }

    /**
     * 查询提示词模板列表
     *
     * <p>分页查询提示词模板列表，支持按用途筛选和名称搜索。</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查询提示词模板列表", description = "分页查询提示词模板列表，支持按用途筛选和名称搜索")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<PageResult<PromptTemplateDTO>>> listTemplates(
            @Valid @RequestBody ListPromptTemplatesRequest request) {

        log.info("查询提示词模板列表，usageId: {}, keyword: {}, page: {}, size: {}",
                request.getUsageId(), request.getKeyword(), request.getPage(), request.getSize());

        PageResult<PromptTemplateDTO> result = promptTemplateApplicationService.listPromptTemplates(request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取模板详情（包含版本历史）
     *
     * <p>查看模板详情和所有历史版本列表。</p>
     */
    @PostMapping("/detail")
    @Operation(summary = "获取模板详情", description = "查看模板详情和所有历史版本列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<Result<PromptTemplateDetailDTO>> getTemplateDetail(
            @Valid @RequestBody GetTemplateDetailRequest request) {

        log.info("获取模板详情，id: {}", request.getId());

        PromptTemplateDetailDTO detail = promptTemplateApplicationService.getTemplateDetail(request.getId());

        return ResponseEntity.ok(Result.success(detail));
    }

    /**
     * 获取指定版本详情
     *
     * <p>查看指定版本的完整内容。</p>
     */
    @PostMapping("/version/detail")
    @Operation(summary = "获取指定版本详情", description = "查看指定版本的完整内容")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板或版本不存在")
    })
    public ResponseEntity<Result<PromptTemplateVersionDTO>> getVersionDetail(
            @Valid @RequestBody GetVersionDetailRequest request) {

        log.info("获取版本详情，templateId: {}, versionNumber: {}",
                request.getTemplateId(), request.getVersionNumber());

        PromptTemplateVersionDTO version = promptTemplateApplicationService.getVersionDetail(
                request.getTemplateId(), request.getVersionNumber());

        return ResponseEntity.ok(Result.success(version));
    }

    /**
     * 更新模板内容（生成新版本）
     *
     * <p>更新模板内容，系统自动生成新版本，版本号递增。</p>
     */
    @PostMapping("/update")
    @Operation(summary = "更新模板内容", description = "更新模板内容，系统自动生成新版本，版本号递增")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效（内容为空或超过64KB限制）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突或内容无变化")
    })
    public ResponseEntity<Result<PromptTemplateDTO>> updateTemplate(
            @Valid @RequestBody UpdatePromptTemplateRequest request) {

        log.info("更新提示词模板，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        PromptTemplateDTO template = promptTemplateApplicationService.updatePromptTemplate(request);

        return ResponseEntity.ok(Result.success("模板更新成功", template));
    }

    /**
     * 回滚到历史版本
     *
     * <p>将模板回滚到历史版本（通过创建新版本实现）。</p>
     */
    @PostMapping("/rollback")
    @Operation(summary = "回滚到历史版本", description = "将模板回滚到历史版本（通过创建新版本实现）")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "回滚成功"),
            @ApiResponse(responseCode = "400", description = "参数无效（已是最早版本）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板或目标版本不存在"),
            @ApiResponse(responseCode = "409", description = "版本冲突")
    })
    public ResponseEntity<Result<PromptTemplateDTO>> rollbackTemplate(
            @Valid @RequestBody RollbackTemplateRequest request) {

        log.info("回滚模板，id: {}, targetVersion: {}, operatorId: {}",
                request.getId(), request.getTargetVersion(), request.getOperatorId());

        PromptTemplateDTO template = promptTemplateApplicationService.rollbackPromptTemplate(request);

        return ResponseEntity.ok(Result.success("模板回滚成功", template));
    }

    /**
     * 删除模板（软删除）
     *
     * <p>软删除模板，删除后模板不可查询。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除模板", description = "软删除模板，删除后模板不可查询")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<Result<Void>> deleteTemplate(
            @Valid @RequestBody DeleteTemplateRequest request) {

        log.info("删除模板，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        promptTemplateApplicationService.deletePromptTemplate(request);

        return ResponseEntity.ok(Result.success("模板删除成功", null));
    }
}
