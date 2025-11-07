# 线程池使用示例

## 🎯 实际项目中的使用场景

### 场景1：不需要线程池（最常见）

```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // ✅ 不需要线程池 - 同步执行
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return Result.success(user);
    }
}

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    // ✅ 不需要线程池 - 在同一个Tomcat线程中执行
    public User getUser(Long id) {
        return userMapper.selectById(id);
    }
}

// 执行流程：
// HTTP请求 → Tomcat线程 → Controller → Service → Mapper
// 整个过程在同一个线程中完成，不需要额外的线程池
```

---

### 场景2：需要线程池 - 异步发送通知

```java
@Service
public class OrderService {
    
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor asyncExecutor;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    // ✅ 需要线程池 - 异步执行
    public void placeOrder(Order order) {
        // 1. 同步处理订单（必须等待完成）
        processOrder(order);
        
        // 2. 异步发送通知（不阻塞用户）
        asyncExecutor.execute(() -> {
            try {
                emailService.sendOrderConfirmation(order);
                smsService.sendOrderSms(order);
            } catch (Exception e) {
                log.error("发送通知失败", e);
            }
        });
        
        // 立即返回给用户，通知在后台发送
    }
}
```

---

### 场景3：需要线程池 - 批量并发处理

```java
@Service
public class DataImportService {
    
    @Autowired
    @Qualifier("businessExecutor")
    private Executor businessExecutor;
    
    // ✅ 需要线程池 - 并发处理大数据量
    public void batchImportUsers(List<User> users) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (User user : users) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                importUser(user);
            }, businessExecutor);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        log.info("批量导入完成，共导入{}个用户", users.size());
    }
}
```

---

### 场景4：需要线程池 - 并发查询提升性能

```java
@Service
public class UserService {
    
    @Autowired
    @Qualifier("businessExecutor")
    private Executor businessExecutor;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private WalletMapper walletMapper;
    
    // ✅ 需要线程池 - 并发查询数据库
    public UserDetail getUserDetail(Long userId) {
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            return userMapper.selectById(userId);
        }, businessExecutor);
        
        CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> {
            return orderMapper.selectByUserId(userId);
        }, businessExecutor);
        
        CompletableFuture<Wallet> walletFuture = CompletableFuture.supplyAsync(() -> {
            return walletMapper.selectByUserId(userId);
        }, businessExecutor);
        
        // 等待所有查询完成
        CompletableFuture.allOf(userFuture, ordersFuture, walletFuture).join();
        
        UserDetail detail = new UserDetail();
        detail.setUser(userFuture.join());
        detail.setOrders(ordersFuture.join());
        detail.setWallet(walletFuture.join());
        
        return detail;
        
        // 性能对比：
        // 串行：200ms + 150ms + 100ms = 450ms
        // 并发：max(200ms, 150ms, 100ms) = 200ms ✅
    }
}
```

---

### 场景5：需要线程池 - 异步任务注解

```java
@Service
@Slf4j
public class NotificationService {
    
    // ✅ 需要线程池 - 使用@Async注解
    @Async("asyncExecutor")
    public void sendEmail(String email, String content) {
        // 在 asyncExecutor 线程池中执行
        emailService.send(email, content);
        log.info("邮件发送完成");
    }
    
    @Async("asyncExecutor")
    public void sendSms(String phone, String content) {
        // 在 asyncExecutor 线程池中执行
        smsService.send(phone, content);
        log.info("短信发送完成");
    }
}
```

---

### 场景6：需要线程池 - 抢红包并发控制

```java
@Service
public class ActivityService {
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Autowired
    private RedPacketMapper redPacketMapper;
    
    // ✅ 需要线程池 + 分布式锁
    public Result<String> grabRedPacket(Long userId, String packetId) {
        String lockKey = "redpacket:grab:" + packetId;
        
        // 使用分布式锁保证并发安全
        return redisLockUtil.executeWithLock(lockKey, () -> {
            // 在锁内执行业务逻辑
            return doGrabRedPacket(userId, packetId);
        }, 10, TimeUnit.SECONDS);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Result<String> doGrabRedPacket(Long userId, String packetId) {
        // 业务逻辑
        RedPacket redPacket = redPacketMapper.selectOne(
            new LambdaQueryWrapper<RedPacket>()
                .eq(RedPacket::getPacketId, packetId)
                .last("FOR UPDATE")  // 数据库行锁
        );
        
        // 检查状态...
        // 分配红包...
        
        return Result.success("抢红包成功");
    }
}
```

---

### 场景7：需要线程池 - 定时任务

```java
@Component
public class DataSyncTask {
    
    @Autowired
    @Qualifier("scheduledExecutor")
    private Executor scheduledExecutor;
    
    // ✅ 需要线程池 - 定时任务
    @Scheduled(fixedDelay = 60000)  // 每60秒执行一次
    public void syncData() {
        // 在 scheduledExecutor 线程池中执行
        log.info("开始同步数据");
        syncFromRemoteServer();
        log.info("同步完成");
    }
}
```

---

### 场景8：需要线程池 - IO操作

```java
@Service
public class FileService {
    
    @Autowired
    @Qualifier("ioTaskExecutor")
    private Executor ioTaskExecutor;
    
    // ✅ 需要线程池 - IO密集型任务
    public void uploadFile(String filePath, byte[] data) {
        ioTaskExecutor.execute(() -> {
            try {
                // 读取文件（IO操作）
                FileInputStream fis = new FileInputStream(filePath);
                
                // 上传到云存储（网络IO）
                cloudStorage.upload(data);
                
                // 更新数据库
                fileMapper.insert(new FileInfo(filePath));
                
                log.info("文件上传完成");
            } catch (Exception e) {
                log.error("文件上传失败", e);
            }
        });
    }
}
```

---

## 📊 使用场景总结

### ✅ 需要线程池的场景

| 场景 | 原因 | 示例 |
|------|------|------|
| **异步执行** | 不想阻塞用户 | 发送邮件、短信 |
| **批量处理** | 需要并发处理 | 批量导入数据 |
| **并发查询** | 提升性能 | 并行查询多个数据库 |
| **后台任务** | 后台执行 | 生成报表、数据分析 |
| **定时任务** | 定时执行 | 数据同步、清理 |
| **IO操作** | 线程会阻塞 | 文件上传、网络请求 |
| **并发控制** | 控制并发数 | 抢红包、秒杀 |

### ❌ 不需要线程池的场景

| 场景 | 原因 | 示例 |
|------|------|------|
| **普通查询** | 同步执行 | `getUser(Long id)` |
| **CRUD操作** | 一次请求一次响应 | `save()`、`update()` |
| **普通Controller** | Spring MVC已处理 | `@GetMapping` |
| **普通Service** | 在同一线程执行 | `findAll()` |

---

## 🎯 判断是否需要线程池的核心问题

### 问题1：是否需要异步执行？

```java
// 同步执行 → 不需要线程池
User user = userService.getUser(1L);
return user;

// 异步执行 → 需要线程池
asyncExecutor.execute(() -> {
    userService.getUser(1L);
});
```

### 问题2：是否需要并发执行？

```java
// 串行执行 → 不需要线程池
for (Order order : orders) {
    processOrder(order);
}

// 并发执行 → 需要线程池
for (Order order : orders) {
    businessExecutor.execute(() -> processOrder(order));
}
```

### 问题3：是否需要控制并发数？

```java
// 需要限流 → 需要线程池
executor.execute(() -> {
    // 最多同时执行16个任务
});
```

---

## 🎉 最终理解

### 你的理解完全正确

```
线程池 = 并发限流器
       = 最大并发数量控制
       = 防止系统被拖垮
       = 业务隔离 + 拒绝策略
```

### 为什么很多方法不需要线程池？

```
因为Spring MVC本身就是多线程的！

每个HTTP请求 → Tomcat线程 → Controller → Service → Mapper
整个过程在同一个线程中完成

除非需要异步/并发，否则不需要额外线程池 ✅
```

### 什么时候需要线程池？

```
需要异步/并发/限流的地方：
├─ 异步任务（@Async）
├─ 批量处理（批量导入）
├─ 并发查询（并行数据库查询）
├─ 后台处理（发送通知）
├─ 定时任务（@Scheduled）
├─ IO操作（文件上传）
└─ 并发控制（抢红包、秒杀）
```

---

## 📚 相关文档

- `THREAD_POOL_FUNDAMENTALS.md` - 线程池基础理解
- `THREAD_POOL_ISOLATION.md` - 线程池隔离策略
- `THREAD_POOL_BEST_PRACTICE.md` - 线程池最佳实践
