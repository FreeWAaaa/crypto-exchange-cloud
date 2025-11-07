# 🔒 各服务细粒度鉴权Demo

> **说明**：本文档提供各服务细粒度鉴权实现的Demo代码  
> **适用场景**：服务内部需要验证用户权限、角色、资源权限等

---

## 📋 目录

1. [设计原则](#设计原则)
2. [用户服务（cex-user）](#用户服务cex-user)
3. [交易服务（cex-trade）](#交易服务cex-trade)
4. [钱包服务（cex-wallet）](#钱包服务cex-wallet)
5. [管理服务（cex-admin）](#管理服务cex-admin)
6. [活动服务（cex-activity）](#活动服务cex-activity)

---

## 🎯 设计原则

### 1. 双重验证

- **网关层**：验证Token有效性、用户身份
- **服务层**：验证业务权限、资源权限

### 2. 用户信息获取

服务可以从请求头获取用户信息：

```java
@RequestHeader("X-User-Id") Long userId
@RequestHeader("X-User-Name") String username
@RequestHeader("X-User-Level") Integer level
@RequestHeader("X-User-Verified") Integer verified
```

### 3. 权限注解

可以使用自定义注解标记需要权限的方法：

```java
@RequireLogin           // 需要登录
@RequireVerified        // 需要实名认证
@RequireLevel(level = 3) // 需要等级3以上
@RequireAdmin           // 需要管理员权限
```

---

## 👤 用户服务（cex-user）

### 场景1：查看个人信息（需要登录）

```java
@RestController
@RequestMapping("/api/user")
public class UserInfoController {
    
    /**
     * 获取当前用户信息
     * 需要：登录
     */
    @GetMapping("/info")
    public Result<UserInfo> getCurrentUserInfo(
            @RequestHeader("X-User-Id") Long userId) {
        // 从请求头获取用户ID（网关已验证Token）
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 转换为DTO（隐藏敏感信息）
        UserInfo userInfo = UserInfo.fromUser(user);
        return Result.success(userInfo);
    }
    
    /**
     * 修改个人信息
     * 需要：登录 + 验证是本人
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateUserInfoDTO dto) {
        // 验证用户ID匹配（防止越权）
        if (!userId.equals(dto.getUserId())) {
            return Result.error("无权修改他人信息");
        }
        
        userService.updateUserInfo(dto);
        return Result.success();
    }
}
```

### 场景2：提交实名认证（需要登录 + 未认证）

```java
@RestController
@RequestMapping("/api/user/verification")
public class VerificationController {
    
    /**
     * 提交实名认证
     * 需要：登录 + 未认证过
     */
    @PostMapping("/submit")
    public Result<Void> submitVerification(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Verified") Integer verified,
            @RequestBody VerificationDTO dto) {
        
        // 验证是否已认证
        if (verified == 1) {
            return Result.error("已认证，无需重复提交");
        }
        
        // 验证是否已提交（审核中）
        UserVerification existing = userVerificationService.getByUserId(userId);
        if (existing != null && existing.getStatus() == 0) {
            return Result.error("认证申请已提交，审核中");
        }
        
        // 提交认证
        userVerificationService.submitVerification(userId, dto);
        return Result.success();
    }
}
```

---

## 💰 交易服务（cex-trade）

### 场景1：下单（需要登录 + 实名认证）

```java
@RestController
@RequestMapping("/api/trade/order")
public class TradeOrderController {
    
    /**
     * 下单
     * 需要：登录 + 实名认证 + 交易密码
     */
    @PostMapping("/place")
    public Result<String> placeOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Verified") Integer verified,
            @RequestBody PlaceOrderDTO dto) {
        
        // 1. 验证实名认证
        if (verified != 1) {
            return Result.error("请先完成实名认证");
        }
        
        // 2. 验证交易密码（在DTO中）
        User user = userService.getById(userId);
        if (!userService.verifyTradePassword(userId, dto.getTradePassword())) {
            return Result.error("交易密码错误");
        }
        
        // 3. 验证余额（调用钱包服务）
        WalletBalance balance = walletFeignClient.getBalance(
            userId, dto.getBaseCoin());
        if (balance.getAvailable().compareTo(dto.getAmount()) < 0) {
            return Result.error("余额不足");
        }
        
        // 4. 下单
        String orderNo = tradeService.placeOrder(userId, dto);
        return Result.success(orderNo);
    }
    
    /**
     * 查询我的订单
     * 需要：登录 + 验证是本人
     */
    @GetMapping("/my/list")
    public Result<List<TradeOrder>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long queryUserId) {
        
        // 验证只能查询自己的订单
        Long targetUserId = queryUserId != null ? queryUserId : userId;
        if (!userId.equals(targetUserId)) {
            return Result.error("无权查询他人订单");
        }
        
        List<TradeOrder> orders = tradeService.getOrdersByUserId(targetUserId);
        return Result.success(orders);
    }
}
```

### 场景2：撤单（需要登录 + 验证订单归属）

```java
@PostMapping("/cancel/{orderNo}")
public Result<Void> cancelOrder(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable String orderNo) {
    
    // 1. 查询订单
    TradeOrder order = tradeService.getByOrderNo(orderNo);
    if (order == null) {
        return Result.error("订单不存在");
    }
    
    // 2. 验证订单归属
    if (!order.getUserId().equals(userId)) {
        return Result.error("无权撤销他人订单");
    }
    
    // 3. 验证订单状态
    if (order.getStatus() != 0 && order.getStatus() != 1) {
        return Result.error("订单状态不允许撤销");
    }
    
    // 4. 撤单
    tradeService.cancelOrder(orderNo);
    return Result.success();
}
```

---

## 💳 钱包服务（cex-wallet）

### 场景1：提现（需要登录 + 实名认证 + 交易密码）

```java
@RestController
@RequestMapping("/api/wallet/withdraw")
public class WalletWithdrawController {
    
    /**
     * 申请提现
     * 需要：登录 + 实名认证 + 交易密码
     */
    @PostMapping("/apply")
    public Result<String> applyWithdraw(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Verified") Integer verified,
            @RequestBody WithdrawApplyDTO dto) {
        
        // 1. 验证实名认证
        if (verified != 1) {
            return Result.error("请先完成实名认证");
        }
        
        // 2. 验证交易密码
        if (!userService.verifyTradePassword(userId, dto.getTradePassword())) {
            return Result.error("交易密码错误");
        }
        
        // 3. 验证余额
        WalletBalance balance = walletBalanceService.getBalance(
            userId, dto.getCoin());
        if (balance.getAvailable().compareTo(dto.getAmount()) < 0) {
            return Result.error("余额不足");
        }
        
        // 4. 验证提现金额限制
        if (dto.getAmount().compareTo(new BigDecimal("100")) < 0) {
            return Result.error("提现金额不能少于100");
        }
        
        // 5. 申请提现
        String withdrawNo = walletWithdrawService.applyWithdraw(userId, dto);
        return Result.success(withdrawNo);
    }
    
    /**
     * 查询我的提现记录
     * 需要：登录 + 验证是本人
     */
    @GetMapping("/my/list")
    public Result<List<WalletWithdraw>> getMyWithdraws(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long queryUserId) {
        
        Long targetUserId = queryUserId != null ? queryUserId : userId;
        if (!userId.equals(targetUserId)) {
            return Result.error("无权查询他人记录");
        }
        
        List<WalletWithdraw> withdraws = walletWithdrawService.getByUserId(targetUserId);
        return Result.success(withdraws);
    }
}
```

### 场景2：查看余额（需要登录）

```java
@GetMapping("/balance/{coin}")
public Result<WalletBalance> getBalance(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable String coin,
        @RequestParam(required = false) Long queryUserId) {
    
    // 验证只能查询自己的余额
    Long targetUserId = queryUserId != null ? queryUserId : userId;
    if (!userId.equals(targetUserId)) {
        return Result.error("无权查询他人余额");
    }
    
    WalletBalance balance = walletBalanceService.getBalance(targetUserId, coin);
    return Result.success(balance);
}
```

---

## 👨‍💼 管理服务（cex-admin）

### 场景1：管理员登录（需要管理员账号）

```java
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {
    
    /**
     * 管理员登录
     * 需要：管理员账号密码
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> adminLogin(@RequestBody AdminLoginDTO dto) {
        // 验证管理员账号密码
        AdminUser admin = adminService.getByUsername(dto.getUsername());
        if (admin == null || !adminService.verifyPassword(dto.getPassword(), admin.getPassword())) {
            return Result.error("账号或密码错误");
        }
        
        // 验证管理员状态
        if (admin.getStatus() != 0) {
            return Result.error("账号已被停用");
        }
        
        // 生成管理员Token（包含admin标识）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getId());
        claims.put("username", admin.getUsername());
        claims.put("role", "admin");  // 标识为管理员
        
        String token = JwtUtils.generateToken(claims);
        
        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);
        response.setAdminInfo(admin);
        
        return Result.success(response);
    }
}
```

### 场景2：用户管理（需要管理员权限）

```java
@RestController
@RequestMapping("/api/admin/user")
public class UserManageController {
    
    /**
     * 查询用户列表
     * 需要：管理员权限
     */
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,  // 需要网关传递role
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // 验证管理员权限
        if (!"admin".equals(role)) {
            return Result.error("需要管理员权限");
        }
        
        // 验证管理员是否存在
        AdminUser admin = adminService.getById(adminId);
        if (admin == null || admin.getStatus() != 0) {
            return Result.error("管理员账号无效");
        }
        
        Page<User> users = userService.getUserList(page, size);
        return Result.success(users);
    }
    
    /**
     * 停用用户
     * 需要：管理员权限
     */
    @PutMapping("/disable/{userId}")
    public Result<Void> disableUser(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        
        // 验证管理员权限
        if (!"admin".equals(role)) {
            return Result.error("需要管理员权限");
        }
        
        userService.disableUser(userId);
        return Result.success();
    }
}
```

### 场景3：提现审核（需要管理员权限）

```java
@RestController
@RequestMapping("/api/admin/wallet")
public class WalletManageController {
    
    /**
     * 审核提现
     * 需要：管理员权限
     */
    @PostMapping("/withdraw/audit")
    public Result<Void> auditWithdraw(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody WithdrawAuditDTO dto) {
        
        // 验证管理员权限
        if (!"admin".equals(role)) {
            return Result.error("需要管理员权限");
        }
        
        // 审核提现
        walletWithdrawService.auditWithdraw(dto.getWithdrawNo(), dto.getStatus(), adminId);
        return Result.success();
    }
}
```

---

## 🎁 活动服务（cex-activity）

### 场景1：签到（需要登录）

```java
@RestController
@RequestMapping("/api/activity/sign")
public class SignController {
    
    /**
     * 签到
     * 需要：登录
     */
    @PostMapping
    public Result<SignRecord> sign(
            @RequestHeader("X-User-Id") Long userId) {
        
        // 检查是否已签到
        SignRecord todayRecord = signService.getTodayRecord(userId);
        if (todayRecord != null) {
            return Result.error("今日已签到");
        }
        
        // 签到
        SignRecord record = signService.sign(userId);
        return Result.success(record);
    }
}
```

### 场景2：抢红包（需要登录 + 验证领取次数）

```java
@RestController
@RequestMapping("/api/activity/redpacket")
public class RedEnvelopeController {
    
    /**
     * 抢红包
     * 需要：登录 + 验证是否已领取
     */
    @PostMapping("/grab/{packetId}")
    public Result<String> grabRedPacket(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String packetId) {
        
        // 1. 查询红包
        RedPacket packet = redEnvelopeService.getByPacketId(packetId);
        if (packet == null) {
            return Result.error("红包不存在");
        }
        
        // 2. 验证红包状态
        if (packet.getStatus() != 0) {
            return Result.error("红包已领完或已过期");
        }
        
        // 3. 验证是否已领取（防重复领取）
        RedEnvelopeDetail detail = redEnvelopeService.getDetailByUserAndPacket(
            userId, packetId);
        if (detail != null) {
            return Result.error("已领取过该红包");
        }
        
        // 4. 抢红包（使用分布式锁）
        String amount = redEnvelopeService.grabRedPacket(userId, packetId);
        return Result.success(amount);
    }
}
```

---

## 🛠️ 通用工具类

### 1. 权限验证工具类

```java
package com.cex.common.core.util;

import com.cex.common.core.exception.BusinessException;

/**
 * 权限验证工具类
 */
public class AuthUtils {
    
    /**
     * 验证用户ID匹配（防止越权）
     */
    public static void validateUserId(Long currentUserId, Long targetUserId) {
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException("无权操作他人数据");
        }
    }
    
    /**
     * 验证实名认证
     */
    public static void validateVerified(Integer verified) {
        if (verified == null || verified != 1) {
            throw new BusinessException("请先完成实名认证");
        }
    }
    
    /**
     * 验证管理员权限
     */
    public static void validateAdmin(String role) {
        if (!"admin".equals(role)) {
            throw new BusinessException("需要管理员权限");
        }
    }
    
    /**
     * 验证用户等级
     */
    public static void validateLevel(Integer userLevel, Integer requiredLevel) {
        if (userLevel == null || userLevel < requiredLevel) {
            throw new BusinessException("用户等级不足，需要等级" + requiredLevel);
        }
    }
}
```

### 2. 使用示例

```java
@GetMapping("/my/orders")
public Result<List<Order>> getMyOrders(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam(required = false) Long queryUserId) {
    
    // 使用工具类验证
    Long targetUserId = queryUserId != null ? queryUserId : userId;
    AuthUtils.validateUserId(userId, targetUserId);
    
    List<Order> orders = orderService.getByUserId(targetUserId);
    return Result.success(orders);
}
```

---

## 📝 总结

### 验证层级

1. **网关层**：Token有效性、用户身份
2. **服务层**：业务权限、资源权限、数据权限

### 验证要点

1. ✅ **用户身份验证**：从请求头获取用户ID
2. ✅ **资源归属验证**：验证操作的是自己的数据
3. ✅ **业务权限验证**：实名认证、用户等级等
4. ✅ **管理员权限验证**：管理员操作需要验证role

### 最佳实践

1. 所有需要登录的接口都从请求头获取用户ID
2. 涉及资源操作时，验证资源归属
3. 敏感操作（提现、大额交易）需要额外验证（交易密码、谷歌验证等）
4. 使用工具类统一权限验证逻辑

---

**文档版本**：v1.0  
**最后更新**：2025-01-31

