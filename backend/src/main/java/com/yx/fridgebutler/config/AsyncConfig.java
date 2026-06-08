package com.yx.fridgebutler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类。
 * <p>启用 Spring 异步支持，并配置容量统计专用的线程池。</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 容量统计异步执行器。
     * <p>用于执行AI调用等耗时操作，避免阻塞主请求线程。</p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "capacityStatsExecutor")
    public Executor capacityStatsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("capacity-stats-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 邮件发送异步执行器。
     * <p>
     * 用于执行邮件发送等网络 IO 操作，避免阻塞主请求线程。
     * 为后续的邮件提醒功能预留容量。
     * </p>
     *
     * @return 线程池执行器
     */
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mail-sender-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
