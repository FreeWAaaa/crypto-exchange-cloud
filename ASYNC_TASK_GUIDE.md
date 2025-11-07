# 异步任务使用指南

## 🎯 概述

项目已配置多个线程池，支持不同的使用场景：

- `businessExecutor`: 核心业务线程池（10核心，50最大）
- `asyncExecutor`: 异步任务线程池（5核心，20最大）
- `scheduledExecutor`: 定时任务线程池（3核心，10最大）
- `completableFutureExecutor`: CompletableFuture专用线程池（ForkJoinPool）
- `ioTaskExecutor`: IO密集型任务线程池（20核心，100最大）

## 📚 使用方式

### 1. 使用 @Async 注解（推荐）

```java
@Service
public class OrderService {
    
    @Async("asyncExecutor")  // 指定线程池
    public CompletableFuture<String> processOrderAsync(Long orderId) {
        // 异步处理订单
        return CompletableFuture.completedFuture("订单处理完成");
    }
    
    @Async  // 使用默认线程池
    public void sendEmail(String email) {
        // 异步发送邮件
    }
}
```

### 2. 使用 CompletableFuture

```java
@Service
public class TradeService {
    
    @Autowired
    @Qualifier("businessExecutor")
    private Executor businessExecutor;
    
    public void processTrade(Long orderId) {
        // 使用指定线程池
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            // 业务逻辑
            return "处理完成";
        }, businessExecutor);
        
        // 获取结果
        String result = future.get();
    }
}
```

### 3. 使用 AsyncUtil 工具类（推荐）

```java
@Service
public class ActivityService {
    
    @Autowired
    private AsyncUtil asyncUtil;
    
    public void processActivity(Long activityId) {
        // 异步执行任务
        CompletableFuture<String> future = asyncUtil.executeAsync(() -> {
            // 业务逻辑
            return "处理完成";
        });
        
        // 处理结果
        future.thenAccept(result -> {
            log.info("处理结果：{}", result);
        });
    }
    
    // 并行执行多个任务
    public void processMultiple() {
        CompletableFuture<String> future1 = asyncUtil.executeAsync(() -> {
            return "任务1完成";
        });
        CompletableFuture<String> future2 = asyncUtil.executeAsync(() -> {
            return "任务2完成";
        });
        
        // 等待所有任务完成
        CompletableFuture.allOf(future1, future2).join();
    }
    
    // IO密集型任务
    public void downloadFile(String url) {
        asyncUtil.executeIoAsync(() -> {
            // 下载文件
            return "下载完成";
        });
    }
}
```

### 4. 复杂场景示例

```java
@Service
public class ComplexService {
    
    @Autowired
    private AsyncUtil asyncUtil;
    
    /**
     * 示例：订单处理流程
     * 1. 异步校验库存
     * 2. 异步计算价格
     * 3. 异步生成订单
     */
    public Result<Order> createOrder(Long userId, List<OrderItem> items) {
        // 1. 并行执行多个校验任务
        CompletableFuture<Boolean> stockCheck = asyncUtil.executeBusinessAsync(() -> {
            return checkStock(items);
        });
        
        CompletableFuture<BigDecimal> priceCalc = asyncUtil.executeBusinessAsync(() -> {
            return calculatePrice(items);
        });
        
        // 2. 等待所有任务完成
        asyncUtil.waitAll(stockCheck, priceCalc);
        
        // 3. 创建订单
        Order order = createOrderInfo(userId, items, priceCalc.get());
        
        return Result.success(order);
    }
    
    /**
     * 示例：多阶段异步处理
     */
    public void processWithStages(Long dataId) {
        // 第一阶段：数据预处理
        CompletableFuture<String> stage1 = asyncUtil.executeAsync(() -> {
            return preprocessData(dataId);
        });
        
        // 第二阶段：数据转换（依赖第一阶段结果）
        CompletableFuture<String> stage2 = stage1.thenApplyAsync(result -> {
            return transformData(result);
        }, businessExecutor);
        
        // 第三阶段：数据保存（依赖第二阶段结果）
        stage2.thenAcceptAsync(result -> {
            saveData(result);
        }, businessExecutor);
    }
}
```

## 🎨 使用场景对照表

| 场景 | 线程池选择 | 示例 |
|-----|----------|------|
| **普通的异步任务** | `asyncExecutor` | 发送短信、邮件 |
| **核心业务逻辑** | `businessExecutor` | 订单处理、支付 |
| **IO操作** | `ioTaskExecutor` | 文件上传、下载 |
| **CPU计算** | `completableFutureExecutor` | 复杂计算、数据分析 |
| **定时任务** | `scheduledExecutor` | 定时报表、数据统计 |

## ⚠️ 注意事项

1. **避免在@Async方法中调用同类其他@Async方法**：会失效
2. **CompletableFuture.get()会阻塞**：在线程池中使用
3. **注意异常处理**：使用handle()或exceptionally()处理异常
4. **资源清理**：及时关闭CompletableFuture

## 🔧 最佳实践

```java
@Service
public class BestPracticeService {
    
    @Autowired
    private AsyncUtil asyncUtil;
    
    /**
     * 推荐做法：使用CompletableFuture处理异常
     */
    public void processWithExceptionHandling(Long dataId) {
        asyncUtil.executeAsync(() -> {
            // 业务逻辑
            processData(dataId);
        }).exceptionally(throwable -> {
            log.error("异步任务执行失败", throwable);
            // 异常处理
            return null;
        });
    }
    
    /**
     * 推荐做法：使用chain编排任务
     */
    public CompletableFuture<String> processData(Long dataId) {
        return asyncUtil.executeAsync(() -> fetchData(dataId))
            .thenApplyAsync(data -> transformData(data), businessExecutor)
            .thenApplyAsync(result -> saveData(result), businessExecutor)
            .exceptionally(throwable -> {
                log.error("处理失败", throwable);
                return "处理失败";
            });
    }
}
```

现在你可以轻松使用异步任务了！🚀
