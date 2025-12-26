package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.prompt.TemplateUsageDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreateTemplateUsageRequest;
import com.catface996.aiops.application.api.dto.prompt.request.DeleteUsageRequest;
import com.catface996.aiops.application.api.service.prompt.TemplateUsageApplicationService;
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

import java.util.List;

/**
 * 模板用途管理控制器（POST-Only API）
 *
 * <p>提供模板用途管理相关的HTTP接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>用途管理接口：</p>
 * <ul>
 *   <li>POST /api/v1/template-usages/create - 创建用途</li>
 *   <li>POST /api/v1/template-usages/list - 查询所有用途</li>
 *   <li>POST /api/v1/template-usages/delete - 删除用途</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/template-usages")
@RequiredArgsConstructor
@Tag(name = "模板用途管理", description = "模板用途管理接口：创建、查询、删除（POST-Only API）")
public class TemplateUsageController {

    private final TemplateUsageApplicationService templateUsageApplicationService;

    /**
     * 创建模板用途
     *
     * <p>创建新的模板用途，用于对提示词模板进行分类。</p>
     */
    @PostMapping("/create")
    @Operation(summary = "创建模板用途", description = "创建新的模板用途，包含编码、名称、描述")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = TemplateUsageDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效（编码格式错误）"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "409", description = "用途编码已存在")
    })
    public ResponseEntity<Result<TemplateUsageDTO>> createUsage(
            @Valid @RequestBody CreateTemplateUsageRequest request) {

        log.info("创建模板用途，code: {}, name: {}", request.getCode(), request.getName());

        TemplateUsageDTO usage = templateUsageApplicationService.createUsage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success("用途创建成功", usage));
    }

    /**
     * 查询所有模板用途
     *
     * <p>查询所有可用的模板用途列表。</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查询所有模板用途", description = "查询所有可用的模板用途列表")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<Result<List<TemplateUsageDTO>>> listUsages() {

        log.info("查询所有模板用途");

        List<TemplateUsageDTO> usages = templateUsageApplicationService.listUsages();

        return ResponseEntity.ok(Result.success(usages));
    }

    /**
     * 删除模板用途
     *
     * <p>删除指定的模板用途，如果有模板正在使用该用途则无法删除。</p>
     */
    @PostMapping("/delete")
    @Operation(summary = "删除模板用途", description = "删除指定的模板用途，如果有模板正在使用该用途则无法删除")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "用途不存在"),
            @ApiResponse(responseCode = "409", description = "用途正在被使用，无法删除")
    })
    public ResponseEntity<Result<Void>> deleteUsage(
            @Valid @RequestBody DeleteUsageRequest request) {

        log.info("删除模板用途，usageId: {}", request.getId());

        templateUsageApplicationService.deleteUsage(request.getId());

        return ResponseEntity.ok(Result.success("用途删除成功", null));
    }
}
