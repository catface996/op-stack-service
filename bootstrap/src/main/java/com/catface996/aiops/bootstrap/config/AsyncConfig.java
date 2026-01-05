package com.catface996.aiops.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置类
 *
 * <p>配置异步任务执行器，遵循项目异步实现标准。</p>
 *
 * <p>线程池命名规范：</p>
 * <ul>
 *   <li>diagnosisExecutor - 诊断任务持久化</li>
 *   <li>taskExecutor - 通用异步任务</li>
 *   <li>eventExecutor - 事件处理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 诊断任务持久化执行器
     *
     * <p>用于诊断任务完成、错误、取消时的异步持久化处理。</p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "diagnosisExecutor")
    public Executor diagnosisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("diagnosis-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 通用异步任务执行器
     *
     * <p>用于一般性的异步任务处理。</p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
