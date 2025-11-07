package com.cex.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 
 * @author cex
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 核心业务线程池
     * 用于处理核心业务逻辑（订单、交易等）
     * 
     * 隔离策略：
     * 1. 使用AbortPolicy拒绝策略 - 任务满了直接拒绝，不会阻塞
     * 2. 限制最大线程数 - 防止单个业务占用所有CPU
     * 3. 合理的队列大小 - 既能缓冲又不会无限堆积
     */
    @Bean("businessExecutor")
    public Executor businessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 = CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数 = 核心数 × 2（防止单个业务占用过多资源）
        executor.setMaxPoolSize(corePoolSize * 2);
        // 队列容量（缓冲层）
        executor.setQueueCapacity(500);
        // 线程名前缀
        executor.setThreadNamePrefix("business-");
        // 拒绝策略：AbortPolicy - 满了就拒绝，不会阻塞（关键！）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        
        log.info("核心业务线程池初始化完成，核心线程：{}，最大线程：{}", corePoolSize, corePoolSize * 2);
        return executor;
    }

    /**
     * 异步任务线程池
     * 用于处理异步任务（非紧急业务）
     * 
     * 隔离策略：
     * 1. 小规模的线程池 - 不超过核心线程数的一半
     * 2. 使用AbortPolicy - 非关键业务可以直接拒绝
     * 3. 较小的队列 - 防止非关键任务堆积
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(Math.max(corePoolSize / 2, 2));
        executor.setMaxPoolSize(corePoolSize);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("异步任务线程池初始化完成");
        return executor;
    }

    /**
     * 定时任务线程池
     * 用于处理定时任务（低优先级）
     * 
     * 隔离策略：
     * 1. 固定小规模 - 定时任务不需要太多线程
     * 2. 使用AbortPolicy - 定时任务失败可以下次再执行
     * 3. 小队列 - 定时任务堆积说明系统异常
     */
    @Bean("scheduledExecutor")
    public Executor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("定时任务线程池初始化完成");
        return executor;
    }

    /**
     * CompletableFuture专用线程池
     * 用于CompletableFuture的异步任务执行
     * 使用自定义ThreadPoolTaskExecutor，避免与其他SDK共享ForkJoinPool
     * 
     * 为什么不使用ForkJoinPool：
     * 1. ForkJoinPool是全局公共线程池，可能被其他SDK使用
     * 2. 多个组件共享可能导致互相竞争资源
     * 3. 自定义线程池更可控、更安全
     */
    @Bean("completableFutureExecutor")
    public Executor completableFutureExecutor() {
        int parallelism = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(parallelism);
        executor.setMaxPoolSize(parallelism * 2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("completable-future-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("CompletableFuture线程池初始化完成，并行度：{}", parallelism);
        return executor;
    }

    /**
     * IO密集型任务线程池
     * 用于网络IO、文件IO等阻塞任务
     * 
     * 隔离策略：
     * 1. IO密集型可以配置更多线程（因为线程会阻塞等待IO）
     * 2. 使用AbortPolicy - IO任务满了直接拒绝
     * 3. 较大队列 - 网络IO本身有延迟，需要更大的缓冲
     */
    @Bean("ioTaskExecutor")
    public Executor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // IO密集型任务可以配置更多线程（公式：核心数 × 2）
        executor.setCorePoolSize(corePoolSize * 2);
        executor.setMaxPoolSize(corePoolSize * 4);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("io-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("IO任务线程池初始化完成");
        return executor;
    }
}
