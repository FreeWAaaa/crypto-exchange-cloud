package com.cex.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步配置
 * 
 * @author cex
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 默认的异步任务执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("async-default-");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 异步任务异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("异步任务执行失败，方法：{}, 参数：{}", method.getName(), params, throwable);
        };
    }
}
