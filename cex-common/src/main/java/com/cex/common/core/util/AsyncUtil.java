package com.cex.common.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 异步任务工具类
 * 简化CompletableFuture的使用
 * 
 * @author cex
 */
@Slf4j
@Component
public class AsyncUtil {

    @Autowired
    @Qualifier("businessExecutor")
    private Executor businessExecutor;

    @Autowired
    @Qualifier("asyncExecutor")
    private Executor asyncExecutor;

    @Autowired
    @Qualifier("completableFutureExecutor")
    private Executor completableFutureExecutor;

    @Autowired
    @Qualifier("ioTaskExecutor")
    private Executor ioTaskExecutor;

    /**
     * 使用CompletableFuture执行异步任务（无返回值）
     * 
     * @param task 要执行的任务
     * @return CompletableFuture
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("异步任务执行失败", e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    /**
     * 使用CompletableFuture执行异步任务（有返回值）
     */
    public <T> CompletableFuture<T> executeAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("异步任务执行失败", e);
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    /**
     * 使用业务线程池执行异步任务
     */
    public <T> CompletableFuture<T> executeBusinessAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("业务异步任务执行失败", e);
                throw new RuntimeException(e);
            }
        }, businessExecutor);
    }

    /**
     * 使用IO线程池执行异步任务（适合IO密集型）
     */
    public <T> CompletableFuture<T> executeIoAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("IO异步任务执行失败", e);
                throw new RuntimeException(e);
            }
        }, ioTaskExecutor);
    }

    /**
     * 使用CompletableFuture专用线程池执行CPU密集型任务
     */
    public <T> CompletableFuture<T> executeCompletableFutureAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("CompletableFuture异步任务执行失败", e);
                throw new RuntimeException(e);
            }
        }, completableFutureExecutor);
    }

    /**
     * 并行执行多个任务
     */
    @SafeVarargs
    public final <T> CompletableFuture<T[]> executeParallel(java.util.function.Supplier<T>... suppliers) {
        @SuppressWarnings("unchecked")
        CompletableFuture<T>[] futures = new CompletableFuture[suppliers.length];
        for (int i = 0; i < suppliers.length; i++) {
            futures[i] = executeAsync(suppliers[i]);
        }
        
        return CompletableFuture.allOf(futures)
            .thenApply(v -> {
                @SuppressWarnings("unchecked")
                T[] results = (T[]) new Object[suppliers.length];
                for (int i = 0; i < suppliers.length; i++) {
                    results[i] = futures[i].join();
                }
                return results;
            });
    }

    /**
     * 等待所有任务完成
     */
    public void waitAll(CompletableFuture<?>... futures) {
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            log.error("等待异步任务完成失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取线程池信息
     */
    public String getPoolInfo() {
        if (asyncExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncExecutor;
            ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) asyncExecutor;
            return String.format(
                "线程池信息 - 活跃线程：%d，最大线程：%d，队列大小：%d",
                pool.getThreadPoolExecutor().getActiveCount(),
                pool.getThreadPoolExecutor().getMaximumPoolSize(),
                pool.getThreadPoolExecutor().getQueue().size()
            );
        }
        return "线程池信息获取失败";
    }
}
