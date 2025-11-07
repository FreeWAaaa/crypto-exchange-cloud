# 🚦 Sentinel限流规则模板

> **说明**：本文档提供Sentinel限流规则的Nacos配置模板  
> **适用场景**：网关和服务层的限流保护

---

## 📋 目录

1. [规则类型说明](#规则类型说明)
2. [网关限流规则](#网关限流规则)
3. [服务限流规则](#服务限流规则)
4. [Nacos配置示例](#nacos配置示例)
5. [规则管理](#规则管理)

---

## 🎯 规则类型说明

### 1. 流控规则（Flow Rule）

**作用**：限制QPS或并发线程数

**参数**：
- `resource`：资源名称（接口路径）
- `grade`：限流类型（0-QPS，1-并发线程数）
- `count`：限流阈值
- `strategy`：流控策略（0-直接，1-关联，2-链路）
- `controlBehavior`：流控效果（0-快速失败，1-Warm Up，2-排队等待）

### 2. 熔断规则（Degrade Rule）

**作用**：服务降级，防止雪崩

**参数**：
- `resource`：资源名称
- `grade`：熔断类型（0-慢调用比例，1-异常比例，2-异常数）
- `count`：阈值
- `timeWindow`：熔断时长（秒）

### 3. 网关限流规则（Gateway Flow Rule）

**作用**：网关层面的限流

**参数**：
- `resource`：资源名称（路由ID或API路径）
- `resourceMode`：资源模式（0-路由，1-API）
- `count`：限流阈值
- `intervalSec`：统计窗口（秒）

---

## 🌐 网关限流规则

### 1. 网关全局限流

**配置位置**：Nacos `cex-gateway-flow-rules`

```json
[
  {
    "resource": "__custom",
    "resourceMode": 0,
    "count": 1000,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0,
    "burst": 0,
    "maxQueueingTimeMs": 500
  }
]
```

**说明**：
- 全局QPS限制：1000/秒
- 超出限制快速失败

### 2. 用户服务限流

```json
[
  {
    "resource": "cex-user",
    "resourceMode": 0,
    "count": 500,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/user/login",
    "resourceMode": 1,
    "count": 10,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/user/register",
    "resourceMode": 1,
    "count": 20,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  }
]
```

**说明**：
- 用户服务整体：500 QPS
- 登录接口：10次/分钟（防暴力破解）
- 注册接口：20次/分钟（防刷注册）

### 3. 交易服务限流

```json
[
  {
    "resource": "cex-trade",
    "resourceMode": 0,
    "count": 1000,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/trade/order/place",
    "resourceMode": 1,
    "count": 100,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/trade/market/**",
    "resourceMode": 1,
    "count": 2000,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  }
]
```

**说明**：
- 交易服务整体：1000 QPS
- 下单接口：100 QPS（防止刷单）
- 行情接口：2000 QPS（高频查询，允许更高）

### 4. 钱包服务限流

```json
[
  {
    "resource": "cex-wallet",
    "resourceMode": 0,
    "count": 500,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/wallet/withdraw/apply",
    "resourceMode": 1,
    "count": 10,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  }
]
```

**说明**：
- 钱包服务整体：500 QPS
- 提现接口：10次/分钟（防刷提现）

### 5. 管理服务限流

```json
[
  {
    "resource": "cex-admin",
    "resourceMode": 0,
    "count": 200,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/admin/login",
    "resourceMode": 1,
    "count": 5,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  }
]
```

**说明**：
- 管理服务整体：200 QPS
- 管理员登录：5次/分钟（防暴力破解）

---

## 🔧 服务限流规则

### 1. 用户服务限流规则

**配置位置**：Nacos `cex-user-flow-rules`

```json
[
  {
    "resource": "/api/user/info",
    "grade": 0,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  },
  {
    "resource": "/api/user/verification/submit",
    "grade": 0,
    "count": 5,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

### 2. 交易服务限流规则

**配置位置**：Nacos `cex-trade-flow-rules`

```json
[
  {
    "resource": "/api/trade/order/place",
    "grade": 0,
    "count": 50,
    "strategy": 0,
    "controlBehavior": 2,
    "maxQueueingTimeMs": 1000,
    "clusterMode": false
  },
  {
    "resource": "/api/trade/order/cancel",
    "grade": 0,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

**说明**：
- 下单接口：50 QPS，超出后排队等待（最多1秒）
- 撤单接口：100 QPS，超出后快速失败

### 3. 钱包服务限流规则

**配置位置**：Nacos `cex-wallet-flow-rules`

```json
[
  {
    "resource": "/api/wallet/withdraw/apply",
    "grade": 0,
    "count": 10,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  },
  {
    "resource": "/api/wallet/balance/**",
    "grade": 0,
    "count": 200,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

---

## ⚡ 熔断规则

### 1. 网关熔断规则

**配置位置**：Nacos `cex-gateway-degrade-rules`

```json
[
  {
    "resource": "cex-user",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000
  },
  {
    "resource": "cex-wallet",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000
  }
]
```

**说明**：
- 异常比例超过50%时，熔断10秒
- 最少5个请求才触发统计

### 2. 服务熔断规则

**配置位置**：Nacos `cex-user-degrade-rules`、`cex-trade-degrade-rules` 等

```json
[
  {
    "resource": "/api/user/info",
    "grade": 1,
    "count": 0.6,
    "timeWindow": 30,
    "minRequestAmount": 10,
    "statIntervalMs": 1000
  }
]
```

---

## 📝 Nacos配置示例

### 1. 在Nacos中创建配置

#### 配置1：网关流控规则

- **Data ID**：`cex-gateway-flow-rules`
- **Group**：`SENTINEL_GROUP`
- **配置格式**：JSON
- **内容**：

```json
[
  {
    "resource": "cex-user",
    "resourceMode": 0,
    "count": 500,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "cex-trade",
    "resourceMode": 0,
    "count": 1000,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "cex-wallet",
    "resourceMode": 0,
    "count": 500,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/user/login",
    "resourceMode": 1,
    "count": 10,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/user/register",
    "resourceMode": 1,
    "count": 20,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/trade/order/place",
    "resourceMode": 1,
    "count": 100,
    "intervalSec": 1,
    "grade": 1,
    "controlBehavior": 0
  },
  {
    "resource": "/api/wallet/withdraw/apply",
    "resourceMode": 1,
    "count": 10,
    "intervalSec": 60,
    "grade": 1,
    "controlBehavior": 0
  }
]
```

#### 配置2：网关熔断规则

- **Data ID**：`cex-gateway-degrade-rules`
- **Group**：`SENTINEL_GROUP`
- **配置格式**：JSON
- **内容**：

```json
[
  {
    "resource": "cex-user",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000
  },
  {
    "resource": "cex-trade",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000
  },
  {
    "resource": "cex-wallet",
    "grade": 1,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000
  }
]
```

#### 配置3：用户服务流控规则

- **Data ID**：`cex-user-flow-rules`
- **Group**：`SENTINEL_GROUP`
- **配置格式**：JSON
- **内容**：

```json
[
  {
    "resource": "/api/user/info",
    "grade": 0,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

#### 配置4：交易服务流控规则

- **Data ID**：`cex-trade-flow-rules`
- **Group**：`SENTINEL_GROUP`
- **配置格式**：JSON
- **内容**：

```json
[
  {
    "resource": "/api/trade/order/place",
    "grade": 0,
    "count": 50,
    "strategy": 0,
    "controlBehavior": 2,
    "maxQueueingTimeMs": 1000,
    "clusterMode": false
  }
]
```

#### 配置5：钱包服务流控规则

- **Data ID**：`cex-wallet-flow-rules`
- **Group**：`SENTINEL_GROUP`
- **配置格式**：JSON
- **内容**：

```json
[
  {
    "resource": "/api/wallet/withdraw/apply",
    "grade": 0,
    "count": 10,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

---

## 🎯 规则管理

### 1. 动态更新规则

规则存储在Nacos中，修改后会自动推送到各服务，无需重启。

### 2. 规则优先级

1. **网关规则**：优先级最高，在网关层拦截
2. **服务规则**：服务层二次限流

### 3. 规则调整建议

#### 开发环境
- 限流阈值可以设置较大
- 主要用于测试功能

#### 测试环境
- 设置合理的限流阈值
- 验证限流功能

#### 生产环境
- 根据实际流量调整
- 监控限流触发情况
- 及时调整规则

### 4. 监控指标

可以通过Sentinel控制台查看：
- QPS统计
- 限流触发次数
- 熔断触发次数
- 响应时间

---

## 📊 限流策略选择

### 1. 快速失败（Default）

**适用场景**：大部分接口

```json
{
  "controlBehavior": 0
}
```

### 2. Warm Up（预热）

**适用场景**：系统启动、冷启动

```json
{
  "controlBehavior": 1,
  "warmUpPeriodSec": 10
}
```

### 3. 排队等待（匀速排队）

**适用场景**：下单、支付等需要保证顺序的操作

```json
{
  "controlBehavior": 2,
  "maxQueueingTimeMs": 1000
}
```

---

## ✅ 总结

### 限流层级

1. **网关层**：全局限流、服务级限流、接口级限流
2. **服务层**：服务内部限流、方法级限流

### 限流策略

1. **QPS限流**：限制每秒请求数
2. **并发线程数限流**：限制同时处理的请求数
3. **熔断降级**：服务异常时自动熔断

### 最佳实践

1. 根据业务重要性设置不同的限流阈值
2. 登录、注册等接口设置更严格的限流
3. 关键业务接口使用排队等待策略
4. 定期监控和调整限流规则

---

**文档版本**：v1.0  
**最后更新**：2025-01-31

