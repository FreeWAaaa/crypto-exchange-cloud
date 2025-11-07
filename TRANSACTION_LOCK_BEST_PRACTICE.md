# 事务与锁的最佳实践总结

## 📚 核心问题

### 问题1：本地调用事务失效
```java
public class Service {
    public void methodA() {
        this.methodB();  // ❌ 本地调用，@Transactional不会生效
    }
    
    @Transactional
    public void methodB() {
        // 业务逻辑
    }
}
```

### 问题2：锁和事务的时序问题
```java
@Transactional
public void method() {
    redisLockUtil.executeWithLock("lock", () -> {
        // 业务逻辑
        dbMapper.update(...);  // 还未提交
    });
    // ❌ 锁在这里释放了，但事务还没提交
    // 其他线程可能看到未提交的数据
}
```

## ✅ 解决方案：自注入 + 事务同步

### 1. 使用自注入解决本地调用问题
```java
@Service
public class ActivityServiceImpl implements ActivityService {
    
    @Autowired
    private ActivityServiceImpl self;  // 自注入
    
    @Override
    public Result<String> grabRedPacket(Long userId, String packetId) {
        return redisLockUtil.executeWithLock(lockKey, () -> {
            return self.doGrabRedPacket(userId, packetId);  // ✅ 通过代理调用
        }, 10, TimeUnit.SECONDS);
    }
    
    @Transactional  // ✅ 现在会生效
    public Result<String> doGrabRedPacket(Long userId, String packetId) {
        // 业务逻辑
    }
}
```

### 2. 事务同步机制
```java
// 工具类自动处理事务同步
if (TransactionSynchronizationManager.isActualTransactionActive()) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                unlock(key);  // ✅ 事务提交后才释放锁
            }
        }
    );
}
```

## 🎯 最佳实践总结

### ✅ 推荐的写法

```java
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl {
    
    @Autowired
    private ActivityServiceImpl self;  // 自注入
    
    public Result<String> grabRedPacket(Long userId, String packetId) {
        String lockKey = "redpacket:grab:" + packetId;
        
        return redisLockUtil.executeWithLock(lockKey, () -> {
            return self.doGrabRedPacket(userId, packetId);  // 通过代理调用
        }, 10, TimeUnit.SECONDS);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Result<String> doGrabRedPacket(Long userId, String packetId) {
        // 1. 使用数据库行锁
        RedPacket redPacket = redPacketMapper.selectOne(
            new LambdaQueryWrapper<RedPacket>()
                .eq(RedPacket::getPacketId, packetId)
                .last("FOR UPDATE")
        );
        
        // 2. 业务逻辑
        // ...
        
        return Result.success();
    }
}
```

### ❌ 不推荐的写法

```java
// ❌ 本地调用，事务不生效
public void grabRedPacket() {
    return this.doGrabRedPacket(...);  // @Transactional不会生效
}

// ❌ Controller加锁，违反分层原则
@RestController
public class ActivityController {
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    public Result<String> grabRedPacket() {
        return redisLockUtil.executeWithLock("lock", () -> {
            return activityService.grabRedPacket(...);  // 违反分层
        });
    }
}
```

## 📊 执行流程

```
1. 获取分布式锁
2. 通过代理调用方法（自注入）
3. AOP开启事务
4. 执行业务逻辑
5. 业务逻辑返回
6. 注册事务同步回调
7. AOP提交事务
8. 事务提交后释放分布式锁 ✅
```

## 🎉 关键要点

1. **必须保留@Transactional注解** - 声明式事务需要
2. **必须使用自注入** - 确保通过代理调用，AOP才能生效
3. **工具类自动处理事务同步** - 事务提交后才释放锁
4. **使用数据库行锁** - 双重保险
5. **分层清晰** - 不要在Controller加锁

现在你的代码已经是最佳实践了！🚀
