package com.cex.common.core.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 使用Redisson实现分布式锁
 * 
 * @author cex
 */
@Slf4j
@Component
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
public class RedisLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 加锁（默认超时时间）
     * 
     * @param key 锁的key
     * @return 是否加锁成功
     */
    public boolean lock(String key) {
        return lock(key, 30, TimeUnit.SECONDS);
    }

    /**
     * 加锁
     * 
     * @param key 锁的key
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否加锁成功
     */
    public boolean lock(String key, long timeout, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败，key：{}", key, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试加锁（立即返回）
     * 
     * @param key 锁的key
     * @return 是否加锁成功
     */
    public boolean tryLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock();
    }

    /**
     * 解锁
     * 
     * @param key 锁的key
     */
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("释放分布式锁成功，key：{}", key);
        }
    }

    /**
     * 带租期的锁（防止死锁）
     * 
     * @param key 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 租期时间
     * @param unit 时间单位
     * @return 是否加锁成功
     */
    public boolean lockWithLease(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败，key：{}", key, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 使用分布式锁执行任务（带事务同步）
     * 如果在事务中，锁会在事务提交后自动释放
     * 
     * @param key 锁的key
     * @param task 要执行的任务
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 执行结果
     */
    public <T> T executeWithLock(String key, java.util.function.Supplier<T> task, long timeout, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(timeout, unit);
            if (!acquired) {
                log.warn("获取分布式锁失败，key：{}", key);
                throw new RuntimeException("获取分布式锁失败");
            }
            
            log.debug("获取分布式锁成功，key：{}", key);
            T result = task.get();
            
            // 如果在事务中，等待事务提交后再释放锁
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            unlock(key);
                            log.debug("事务提交后释放分布式锁，key：{}", key);
                        }
                    }
                );
            } else {
                // 不在事务中，立即释放锁
                unlock(key);
            }
            
            return result;
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败，key：{}", key, e);
            Thread.currentThread().interrupt();
            if (acquired) {
                unlock(key);
            }
            throw new RuntimeException("获取分布式锁失败", e);
        } catch (Exception e) {
            if (acquired) {
                unlock(key);
            }
            throw e;
        }
    }

    /**
     * 使用分布式锁执行任务（默认超时时间）
     */
    public <T> T executeWithLock(String key, java.util.function.Supplier<T> task) {
        return executeWithLock(key, task, 30, TimeUnit.SECONDS);
    }

    /**
     * 使用分布式锁执行无返回值任务
     */
    public void executeWithLock(String key, Runnable task, long timeout, TimeUnit unit) {
        if (lock(key, timeout, unit)) {
            try {
                log.debug("获取分布式锁成功，key：{}", key);
                task.run();
            } finally {
                unlock(key);
            }
        } else {
            log.warn("获取分布式锁失败，key：{}", key);
            throw new RuntimeException("获取分布式锁失败");
        }
    }

    /**
     * 使用分布式锁执行无返回值任务（默认超时时间）
     */
    public void executeWithLock(String key, Runnable task) {
        executeWithLock(key, task, 30, TimeUnit.SECONDS);
    }
}
