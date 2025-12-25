package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.llm.CreateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.LlmServiceDTO;
import com.catface996.aiops.application.api.dto.llm.UpdateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.UpdateStatusCommand;
import com.catface996.aiops.application.api.service.llm.LlmServiceApplicationService;
import com.catface996.aiops.interface_.http.request.llm.DeleteLlmServiceRequest;
import com.catface996.aiops.interface_.http.request.llm.GetLlmServiceRequest;
import com.catface996.aiops.interface_.http.request.llm.QueryLlmServicesRequest;
import com.catface996.aiops.interface_.http.request.llm.SetDefaultLlmServiceRequest;
import com.catface996.aiops.interface_.http.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 服务配置控制器（POST-Only API）
 *
 * <p>提供 LLM 服务管理相关的 HTTP 接口，所有业务接口统一使用 POST 方法。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~011</li>
 *   <li>Feature 024: POST-Only API 重构</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/llm-services")
@RequiredArgsConstructor
@Tag(name = "LLM 服务管理", description = "LLM 服务配置管理接口：服务配置 CRUD、启用/禁用、设置默认服务、优先级管理")
public class LlmServiceController {

    private final LlmServiceApplicationService llmServiceApplicationService;

    // ==================== 获取服务列表 ====================

    /**
     * 获取 LLM 服务列表
     */
    @PostMapping("/query")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取 LLM 服务列表", description = "获取所有 LLM 服务配置，支持按启用状态过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public ResponseEntity<Result<Map<String, Object>>> listLlmServices(
            @Valid @RequestBody QueryLlmServicesRequest request) {
        List<LlmServiceDTO> items = llmServiceApplicationService.list(request.getEnabledOnly());
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", items.size());
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 创建服务 ====================

    /**
     * 创建 LLM 服务
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建 LLM 服务", description = "创建新的 LLM 服务配置")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = LlmServiceDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "409", description = "服务名称已存在")
    })
    public ResponseEntity<Result<LlmServiceDTO>> createLlmService(
            @Valid @RequestBody CreateLlmServiceCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(result));
    }

    // ==================== 获取服务详情 ====================

    /**
     * 获取 LLM 服务详情
     */
    @PostMapping("/get")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取 LLM 服务详情", description = "根据 ID 获取 LLM 服务配置详情。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<Result<LlmServiceDTO>> getLlmService(
            @Valid @RequestBody GetLlmServiceRequest request) {
        LlmServiceDTO result = llmServiceApplicationService.getById(request.getId());
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 更新服务 ====================

    /**
     * 更新 LLM 服务
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新 LLM 服务", description = "更新指定 LLM 服务的配置。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数无效"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "服务不存在"),
            @ApiResponse(responseCode = "409", description = "服务名称已存在")
    })
    public ResponseEntity<Result<LlmServiceDTO>> updateLlmService(
            @Valid @RequestBody UpdateLlmServiceCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.update(command.getId(), command);
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 删除服务 ====================

    /**
     * 删除 LLM 服务
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除 LLM 服务", description = "删除指定的 LLM 服务配置。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "服务不存在"),
            @ApiResponse(responseCode = "409", description = "服务被引用，需强制删除")
    })
    public ResponseEntity<Result<Void>> deleteLlmService(
            @Valid @RequestBody DeleteLlmServiceRequest request) {
        llmServiceApplicationService.delete(request.getId(), request.getForce());
        return ResponseEntity.ok(Result.success("LLM服务删除成功", null));
    }

    // ==================== 更新服务状态 ====================

    /**
     * 更新服务状态（启用/禁用）
     */
    @PostMapping("/update-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新服务状态", description = "启用或禁用 LLM 服务。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "无法禁用唯一默认服务"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<Result<LlmServiceDTO>> updateLlmServiceStatus(
            @Valid @RequestBody UpdateStatusCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.updateStatus(command.getId(), command.getEnabled());
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 设置默认服务 ====================

    /**
     * 设置为默认服务
     */
    @PostMapping("/set-default")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "设置默认服务", description = "将指定服务设为默认 LLM 服务。ID通过请求体传递")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "设置成功"),
            @ApiResponse(responseCode = "400", description = "只能将启用的服务设为默认"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<Result<LlmServiceDTO>> setDefaultLlmService(
            @Valid @RequestBody SetDefaultLlmServiceRequest request) {
        LlmServiceDTO result = llmServiceApplicationService.setDefault(request.getId());
        return ResponseEntity.ok(Result.success(result));
    }
}
