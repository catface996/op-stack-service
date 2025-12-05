package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.application.api.dto.llm.CreateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.LlmServiceDTO;
import com.catface996.aiops.application.api.dto.llm.UpdateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.UpdateStatusCommand;
import com.catface996.aiops.application.api.service.llm.LlmServiceApplicationService;
import com.catface996.aiops.interface_.http.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * LLM 服务配置控制器
 *
 * <p>提供 LLM 服务管理相关的 HTTP 接口</p>
 *
 * <p>需求追溯：FR-001~011</p>
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/llm-services")
@RequiredArgsConstructor
@Tag(name = "LLM 服务管理", description = "LLM 服务配置管理接口")
public class LlmServiceController {

    private final LlmServiceApplicationService llmServiceApplicationService;

    // ==================== 获取服务列表 ====================

    /**
     * 获取 LLM 服务列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取 LLM 服务列表", description = "获取所有 LLM 服务配置，支持按启用状态过滤")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> listLlmServices(
            @Parameter(description = "是否只返回启用的服务")
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<LlmServiceDTO> items = llmServiceApplicationService.list(enabledOnly);
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", items.size());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 创建服务 ====================

    /**
     * 创建 LLM 服务
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建 LLM 服务", description = "创建新的 LLM 服务配置")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = LlmServiceDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "服务名称已存在")
    })
    public ResponseEntity<ApiResponse<LlmServiceDTO>> createLlmService(
            @Valid @RequestBody CreateLlmServiceCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result));
    }

    // ==================== 获取服务详情 ====================

    /**
     * 获取 LLM 服务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取 LLM 服务详情", description = "根据 ID 获取 LLM 服务配置详情")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<ApiResponse<LlmServiceDTO>> getLlmService(
            @Parameter(description = "服务 ID") @PathVariable Long id) {
        LlmServiceDTO result = llmServiceApplicationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 更新服务 ====================

    /**
     * 更新 LLM 服务
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新 LLM 服务", description = "更新指定 LLM 服务的配置")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数无效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "服务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "服务名称已存在")
    })
    public ResponseEntity<ApiResponse<LlmServiceDTO>> updateLlmService(
            @Parameter(description = "服务 ID") @PathVariable Long id,
            @Valid @RequestBody UpdateLlmServiceCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.update(id, command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 删除服务 ====================

    /**
     * 删除 LLM 服务
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除 LLM 服务", description = "删除指定的 LLM 服务配置")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "删除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "服务不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "服务被引用，需强制删除")
    })
    public ResponseEntity<Void> deleteLlmService(
            @Parameter(description = "服务 ID") @PathVariable Long id,
            @Parameter(description = "是否强制删除")
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        llmServiceApplicationService.delete(id, force);
        return ResponseEntity.noContent().build();
    }

    // ==================== 更新服务状态 ====================

    /**
     * 更新服务状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新服务状态", description = "启用或禁用 LLM 服务")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "无法禁用唯一默认服务"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<ApiResponse<LlmServiceDTO>> updateLlmServiceStatus(
            @Parameter(description = "服务 ID") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusCommand command) {
        LlmServiceDTO result = llmServiceApplicationService.updateStatus(id, command.getEnabled());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== 设置默认服务 ====================

    /**
     * 设置为默认服务
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "设置默认服务", description = "将指定服务设为默认 LLM 服务")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "设置成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "只能将启用的服务设为默认"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "服务不存在")
    })
    public ResponseEntity<ApiResponse<LlmServiceDTO>> setDefaultLlmService(
            @Parameter(description = "服务 ID") @PathVariable Long id) {
        LlmServiceDTO result = llmServiceApplicationService.setDefault(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
