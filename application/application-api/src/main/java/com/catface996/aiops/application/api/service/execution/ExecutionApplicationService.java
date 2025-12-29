package com.catface996.aiops.application.api.service.execution;

import com.catface996.aiops.application.api.dto.execution.ExecutionEventDTO;
import com.catface996.aiops.application.api.dto.execution.request.CancelExecutionRequest;
import com.catface996.aiops.application.api.dto.execution.request.TriggerExecutionRequest;
import reactor.core.publisher.Flux;

/**
 * 执行应用服务接口
 *
 * <p>提供多智能体执行的应用层接口。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>验证拓扑图配置</li>
 *   <li>查询层级团队结构</li>
 *   <li>调用 Executor 服务</li>
 *   <li>流式传输执行事件</li>
 *   <li>取消正在执行的运行</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须提供 POST 端点触发多智能体执行</li>
 *   <li>FR-009: 系统必须通过 SSE 流式传输执行事件</li>
 *   <li>FR-010: 系统必须支持取消正在执行的运行</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface ExecutionApplicationService {

    /**
     * 触发多智能体执行
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>验证拓扑图存在且配置正确</li>
     *   <li>查询层级团队结构</li>
     *   <li>创建 Executor 层级结构</li>
     *   <li>启动执行运行</li>
     *   <li>返回事件流（第一个事件包含 runId）</li>
     * </ol>
     *
     * @param request 触发请求（包含 topologyId 和 userMessage）
     * @return 执行事件流
     * @throws IllegalArgumentException 如果拓扑图不存在
     * @throws IllegalStateException 如果拓扑图缺少 Global Supervisor 或团队
     * @throws RuntimeException 如果 Executor 服务调用失败
     */
    Flux<ExecutionEventDTO> triggerExecution(TriggerExecutionRequest request);

    /**
     * 取消正在执行的运行
     *
     * @param request 取消请求（包含 runId）
     * @return 取消结果事件
     */
    ExecutionEventDTO cancelExecution(CancelExecutionRequest request);
}
