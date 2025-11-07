# 项目依赖分析报告

## 📋 你的问题

> 本机电脑是用docker跑了 redis mysql，还需要依赖什么？
> 当前项目是不是除了这两个之外还需要依赖 Nacos RocketMQ？

---

## ✅ 结论

**是的，你的项目还需要启动：**

1. ✅ **Nacos** - 必需
2. ✅ **RocketMQ** - 必需（部分服务）
3. ✅ **Sentinel** - 可选（目前仅依赖管理，暂未启用控制台）

---

## 📊 详细分析

### 1. 必需的基础设施

#### ✅ MySQL (你已有)
```yaml
用途：数据存储
端口：3306
状态：✅ 你已经有了
```

#### ✅ Redis (你已有)
```yaml
用途：缓存、分布式锁（Redisson）、会话存储
端口：6379
状态：✅ 你已经有了
```

#### ✅ Nacos (必需 - 你需要启动)
```yaml
用途：
  1. 服务注册中心 - 所有微服务需要注册到这里
  2. 配置中心 - 从Nacos加载配置（bootstrap.yml）
  3. 服务发现 - 服务之间互相调用需要Nacos

使用到的服务：
  - cex-gateway (必须)
  - cex-user (必须)
  - cex-trade (必须)
  - cex-wallet (必须)
  - cex-matching (必须)
  - cex-admin (必须)
  - cex-notification (必须)
  - cex-activity (必须)

端口：8848（HTTP）、9848（gRPC）
状态：❌ 需要启动
```

#### ✅ RocketMQ (必需 - 部分服务需要)
```yaml
用途：消息队列，用于异步通信
  - 交易订单推送
  - 钱包余额变化通知
  - 撮合结果推送

使用到的服务：
  - cex-trade (依赖RocketMQ)
  - cex-wallet (依赖RocketMQ)
  - cex-matching (依赖RocketMQ)

不需要的服务：
  - cex-user (不依赖)
  - cex-admin (不依赖)
  - cex-notification (不依赖)
  - cex-activity (不依赖)
  - cex-gateway (不依赖)

端口：9876（NameServer）、10909/10911（Broker）
状态：❌ 需要启动
```

---

## 🔍 详细配置分析

### 1. Nacos配置（所有服务都需要）

每个服务的 `bootstrap.yml` 都有：
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848  # 配置中心地址
      discovery:
        server-addr: localhost:8848  # 注册中心地址
```

**影响**：
- 如果Nacos不启动，所有服务都无法启动
- 每个服务启动时都会连接Nacos

---

### 2. RocketMQ配置（部分服务需要）

#### 使用RocketMQ的服务：
```yaml
# cex-trade/pom.xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>

# cex-wallet/pom.xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>

# cex-matching/pom.xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

#### 不使用RocketMQ的服务：
- cex-user ❌
- cex-admin ❌
- cex-notification ❌
- cex-activity ❌
- cex-gateway ❌

**影响**：
- 如果RocketMQ不启动，以下服务可能启动失败：
  - cex-trade
  - cex-wallet
  - cex-matching

---

### 3. Sentinel配置（依赖管理中有，但当前未启用控制台）

```yaml
# 大部分服务的 pom.xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

**当前状态**：
- ✅ 依赖已添加
- ❌ Sentinel控制台未配置
- ✅ 不影响项目运行（默认规则）

**建议**：
- 暂时不需要启动Sentinel控制台
- 后续如果需要熔断限流，可以再配置

---

## 🚀 你需要启动的服务清单

### 1. Docker Compose已配置的服务

`docker-compose.yml` 已经为你准备好了所有服务：

```bash
# 启动所有服务（包括MySQL、Redis、Nacos、RocketMQ）
docker-compose up -d

# 只启动基础设施（不包括业务服务）
docker-compose up -d mysql redis nacos rocketmq-nameserver rocketmq-broker
```

### 2. 必需的服务

```bash
# MySQL (端口3306) - ✅ 你已有
# Redis (端口6379) - ✅ 你已有

# Nacos (端口8848, 9848) - ❌ 需要启动
nacos:
  image: nacos/nacos-server:v2.2.0
  ports:
    - "8848:8848"  # HTTP API
    - "9848:9848"  # gRPC

# RocketMQ NameServer (端口9876) - ❌ 需要启动
rocketmq-nameserver:
  image: apache/rocketmq:4.9.4
  ports:
    - "9876:9876"

# RocketMQ Broker (端口10909, 10911) - ❌ 需要启动
rocketmq-broker:
  image: apache/rocketmq:4.9.4
  ports:
    - "10909:10909"
    - "10911:10911"
```

### 3. 可选的服务

```bash
# Sentinel控制台 - ⚠️ 可选（目前未配置）
# 暂时不需要启动
```

---

## 📝 启动顺序

### 正确的启动顺序：

```bash
# 1. 先启动基础设施
docker-compose up -d mysql
docker-compose up -d redis
docker-compose up -d nacos
docker-compose up -d rocketmq-nameserver
docker-compose up -d rocketmq-broker

# 2. 验证服务
# 访问 http://localhost:8848/nacos
# 默认账号密码: nacos/nacos

# 3. 然后启动业务服务
mvn spring-boot:run -pl cex-gateway
mvn spring-boot:run -pl cex-user
# ...
```

---

## ⚠️ 注意事项

### 1. Nacos必须启动

```
如果不启动Nacos：
  ❌ 所有微服务无法启动
  ❌ bootstrap.yml 无法加载配置
  ❌ 服务注册中心无法使用
  ❌ 微服务之间无法调用
```

### 2. RocketMQ必须启动

```
如果不启动RocketMQ：
  ❌ cex-trade 启动失败
  ❌ cex-wallet 启动失败
  ❌ cex-matching 启动失败
  
  ✅ cex-user 可以启动
  ✅ cex-admin 可以启动
  ✅ cex-notification 可以启动
  ✅ cex-activity 可以启动
```

### 3. 服务启动顺序

```
1. MySQL & Redis (你已有) ✅
2. Nacos (必需) ❌ 需要启动
3. RocketMQ (必需，部分服务用) ❌ 需要启动
4. 业务服务
```

---

## 🎯 总结

### 你现在有的：
- ✅ MySQL
- ✅ Redis

### 你需要的：
- ❌ Nacos（必需）
- ❌ RocketMQ（必需，部分服务）

### 不需要的：
- ⚠️ Sentinel控制台（可选，暂不需要）

---

## 🚀 建议

**最简单的方式**：直接运行 `docker-compose up`，它会自动启动所有需要的服务！

```bash
# 只启动基础设施
docker-compose up -d mysql redis nacos rocketmq-nameserver rocketmq-broker

# 验证
curl http://localhost:8848/nacos
curl http://localhost:9876

# 然后启动你的业务服务
mvn spring-boot:run
```
