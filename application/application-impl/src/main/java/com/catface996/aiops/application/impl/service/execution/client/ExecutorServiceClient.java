package com.catface996.aiops.application.impl.service.execution.client;

import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyResponse;
import com.catface996.aiops.application.impl.service.execution.client.dto.ExecutorEvent;
import com.catface996.aiops.application.impl.service.execution.client.dto.StartRunRequest;
import com.catface996.aiops.application.impl.service.execution.client.dto.StartRunResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Executor 服务客户端
 *
 * <p>用于与外部 Executor 服务通信，实现多智能体执行功能。</p>
 *
 * <p>API 端点：</p>
 * <ul>
 *   <li>POST /api/executor/v1/hierarchies/create - 创建层级结构</li>
 *   <li>POST /api/executor/v1/runs/start - 启动执行运行</li>
 *   <li>POST /api/executor/v1/runs/stream - 流式获取执行事件 (body: {"id": "run_id"})</li>
 *   <li>POST /api/executor/v1/runs/cancel - 取消执行运行 (body: {"id": "run_id"})</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@Component
public class ExecutorServiceClient {

    private final WebClient webClient;
    private final Duration readTimeout;

    public ExecutorServiceClient(
            @Value("${executor.service.base-url}") String baseUrl,
            @Value("${executor.service.timeout.connect:5000}") int connectTimeout,
            @Value("${executor.service.timeout.read:60000}") int readTimeoutMs) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.readTimeout = Duration.ofMillis(readTimeoutMs);
        log.info("ExecutorServiceClient initialized with baseUrl: {}", baseUrl);
    }

    /**
     * 创建层级结构
     *
     * @param request 创建请求
     * @return 创建响应
     */
    public Mono<CreateHierarchyResponse> createHierarchy(CreateHierarchyRequest request) {
        log.debug("Creating hierarchy: {}", request.getName());
        return webClient.post()
                .uri("/api/executor/v1/hierarchies/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreateHierarchyResponse.class)
                .doOnSuccess(response -> log.info("Hierarchy created: {}", response.getHierarchyId()))
                .doOnError(e -> log.error("Failed to create hierarchy: {}", e.getMessage()));
    }

    /**
     * 启动执行运行
     *
     * @param request 启动请求
     * @return 启动响应
     */
    public Mono<StartRunResponse> startRun(StartRunRequest request) {
        log.debug("Starting run for hierarchy: {}", request.getHierarchyId());
        return webClient.post()
                .uri("/api/executor/v1/runs/start")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(StartRunResponse.class)
                .doOnSuccess(response -> log.info("Run started: {}", response.getRunId()))
                .doOnError(e -> log.error("Failed to start run: {}", e.getMessage()));
    }

    /**
     * 流式获取执行事件
     *
     * @param runId 运行 ID
     * @return 事件流
     */
    public Flux<ExecutorEvent> streamEvents(String runId) {
        log.debug("Streaming events for run: {}", runId);
        // Executor API 使用 POST 请求，body 为 {"id": "run_id"}
        return webClient.post()
                .uri("/api/executor/v1/runs/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(java.util.Map.of("id", runId))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<ExecutorEvent>>() {})
                .filter(sse -> sse != null && sse.data() != null)
                .map(ServerSentEvent::data)
                .timeout(readTimeout)
                .doOnNext(event -> log.debug("Received event: type={}, agent={}", event.getType(), event.getAgent()))
                .doOnComplete(() -> log.info("Event stream completed for run: {}", runId))
                .doOnError(e -> log.error("Event stream error for run {}: {}", runId, e.getMessage()));
    }

    /**
     * 取消执行运行
     *
     * @param runId 运行 ID
     * @return 取消是否成功
     */
    public Mono<Boolean> cancelRun(String runId) {
        log.info("Cancelling run: {}", runId);
        return webClient.post()
                .uri("/api/executor/v1/runs/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of("id", runId))
                .retrieve()
                .bodyToMono(CancelRunResponse.class)
                .map(response -> Boolean.TRUE.equals(response.getSuccess()))
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Run cancelled successfully: {}", runId);
                    } else {
                        log.warn("Failed to cancel run: {}", runId);
                    }
                })
                .doOnError(e -> log.error("Error cancelling run {}: {}", runId, e.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * 检查服务是否可用
     *
     * @return 服务是否可用
     */
    public Mono<Boolean> isServiceAvailable() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false)
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * 取消运行响应（内部使用）
     */
    @lombok.Data
    private static class CancelRunResponse {
        private Boolean success;
        private String message;
    }
}
