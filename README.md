# 🚀 CEX 数字货币交易所系统

> 基于 Spring Cloud Alibaba 的现代化数字货币交易所微服务系统

## 📖 项目简介

CEX 是一个功能完整、架构现代化的数字货币交易所系统，采用微服务架构，技术栈全面升级，业务逻辑保持稳定可靠。

### ✨ 核心特性

- 🎯 **微服务架构**：7个独立微服务，易于扩展和维护
- 🔥 **技术先进**：Spring Cloud Alibaba、JDK 21、MyBatis Plus
- ⚡ **高性能**：内存撮合引擎、Redis缓存、RocketMQ异步
- 🔐 **安全可靠**：JWT认证、乐观锁、分布式事务
- 📊 **功能完整**：用户、钱包、交易、撮合、管理、活动、通知

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Cloud Gateway                  │
│                      (API网关 8080)                       │
└──────────────────────┬──────────────────────────────────┘
                       │
       ┌───────────────┴───────────────┐
       │         Nacos (8848)          │
       │   服务注册中心 + 配置中心       │
       └───────────────┬───────────────┘
                       │
    ┌──────────────────┼──────────────────┐
    │                  │                  │
┌───▼───┐      ┌──────▼──────┐    ┌─────▼─────┐
│ User  │      │   Wallet    │    │   Trade   │
│ 8081  │◄────►│    8083     │◄──►│   8082    │
└───┬───┘      └──────┬──────┘    └─────┬─────┘
    │                 │                  │
    │                 │           ┌──────▼────────┐
    │                 │           │   Matching    │
    │                 │           │     8084      │
    │                 │           └───────────────┘
    │          ┌──────▼──────┐
    │          │    Admin    │
    │          │    8085     │
    │          └─────────────┘
    │
┌───▼────────┐        ┌─────────────┐
│Notification│        │  Activity   │
│   8086     │        │    8087     │
└────────────┘        └─────────────┘
```

---

## 📦 模块说明

| 模块 | 端口 | 说明 | 核心功能 |
|------|------|------|---------|
| **cex-gateway** | 8080 | API网关 | 路由、限流、鉴权 |
| **cex-user** | 8081 | 用户服务 | 注册、登录、认证、邀请 |
| **cex-trade** | 8082 | 交易服务 | 下单、撤单、订单管理 |
| **cex-wallet** | 8083 | 钱包服务 | 充值、提现、余额管理 |
| **cex-matching** | 8084 | 撮合引擎 | 订单撮合、盘口维护 |
| **cex-admin** | 8085 | 管理后台 | 用户管理、订单管理、审核 |
| **cex-notification** | 8086 | 通知服务 | 短信、邮件、站内消息 |
| **cex-activity** | 8087 | 活动服务 | 签到、红包、活动管理 |

---

## 🛠️ 技术栈

### 后端框架
- **Spring Boot**: 2.7.18
- **Spring Cloud**: 2021.0.8
- **Spring Cloud Alibaba**: 2021.0.5.0

### 微服务组件
- **Nacos**: 服务注册与配置中心
- **Gateway**: API网关
- **Sentinel**: 限流熔断
- **RocketMQ**: 消息队列
- **Seata**: 分布式事务（预留）
- **OpenFeign**: 服务调用

### 数据存储
- **MySQL**: 8.0+ (主数据库)
- **Redis**: 6.0+ (缓存)
- **MyBatis Plus**: 3.5.3.1 (ORM)
- **Druid**: 数据库连接池

### 开发工具
- **JDK**: 21
- **Maven**: 3.8+
- **Lombok**: 代码简化
- **Hutool**: 工具类库

---

## ⚙️ 环境要求

```
JDK:        21+
MySQL:      8.0+
Redis:      6.0+
Nacos:      2.2.0+
RocketMQ:   4.9.0+
Maven:      3.8+
```

---

## 🚀 快速启动

详见：[QUICK_START.md](QUICK_START.md)

### 1. 启动基础服务

```bash
# MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0

# Redis
docker run -d --name redis -p 6379:6379 redis:6-alpine

# Nacos (单机模式)
docker run -d --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.2.0

# RocketMQ (可选，如果需要撮合引擎)
# 参考 docker-compose.yml
```

### 2. 初始化数据库

```bash
cd sql
mysql -u root -p < 01_user.sql
mysql -u root -p < 02_trade.sql
mysql -u root -p < 03_wallet.sql
mysql -u root -p < 05_admin.sql
mysql -u root -p < 06_notification.sql
mysql -u root -p < 07_activity.sql
```

### 3. 编译项目

```bash
cd /Users/subby/projects/cex
mvn clean package -DskipTests
```

### 4. 启动微服务

```bash
# 方式1：使用 JAR 包启动
java -jar cex-gateway/target/cex-gateway-1.0.0.jar
java -jar cex-user/target/cex-user-1.0.0.jar
java -jar cex-wallet/target/cex-wallet-1.0.0.jar
java -jar cex-trade/target/cex-trade-1.0.0.jar
java -jar cex-matching/target/cex-matching-1.0.0.jar
java -jar cex-admin/target/cex-admin-1.0.0.jar
java -jar cex-notification/target/cex-notification-1.0.0.jar
java -jar cex-activity/target/cex-activity-1.0.0.jar

# 方式2：使用 Docker Compose（推荐）
docker-compose up -d
```

---

## 📡 API 接口

### 网关地址
- **Gateway**: http://localhost:8080

### 用户相关
```
POST /api/user/register/phone     # 手机号注册
POST /api/user/register/email     # 邮箱注册
POST /api/user/login               # 用户登录
GET  /api/user/info                # 获取用户信息
POST /api/user/verification/submit # 提交实名认证
```

### 钱包相关
```
GET  /api/wallet/balance            # 查询余额
POST /api/wallet/deposit/address    # 获取充值地址
POST /api/wallet/withdraw/apply     # 申请提现
GET  /api/wallet/transaction/list   # 资产流水
```

### 交易相关
```
POST /api/trade/order/place         # 下单
POST /api/trade/order/cancel        # 撤单
GET  /api/trade/order/current       # 当前委托
GET  /api/trade/order/history       # 历史委托
GET  /api/trade/symbol/list         # 交易对列表
```

### 管理后台
```
POST /admin/auth/login              # 管理员登录
GET  /admin/user/list               # 用户列表
POST /admin/verification/audit      # 实名审核
POST /admin/wallet/withdraw/audit   # 提现审核
GET  /admin/statistics/dashboard    # 首页统计
```

### 活动相关
```
POST /api/activity/sign/in          # 签到
POST /api/activity/redenvelope/send # 发红包
POST /api/activity/redenvelope/receive # 领红包
```

---

## 📊 数据库设计

### 数据库列表
```
cex_user          # 用户数据（3张表）
cex_wallet        # 钱包数据（5张表）
cex_trade         # 交易数据（2张表）
cex_admin         # 管理数据（3张表）
cex_notification  # 通知数据（3张表）
cex_activity      # 活动数据（4张表）
```

详见 `sql/` 目录下的SQL脚本

---

## 🎯 核心功能

### 1. 用户系统
- ✅ 手机号/邮箱注册
- ✅ 多种登录方式
- ✅ 实名认证（三要素）
- ✅ 三级邀请推广
- ✅ 交易密码
- ✅ Google Authenticator
- ✅ 用户等级体系

### 2. 钱包系统
- ✅ 多币种钱包
- ✅ 充值地址生成
- ✅ 充值自动到账
- ✅ 提现申请与审核
- ✅ 余额冻结/解冻
- ✅ 完整资产流水
- ✅ 并发安全（乐观锁）

### 3. 交易系统
- ✅ 限价单交易
- ✅ 市价单交易
- ✅ 订单撮合
- ✅ 实时盘口
- ✅ 成交记录
- ✅ K线数据（预留）

### 4. 撮合引擎
- ✅ 价格优先算法
- ✅ 时间优先算法
- ✅ 内存撮合
- ✅ 高性能处理
- ✅ 分摊模式支持

### 5. 管理后台
- ✅ 用户管理
- ✅ 订单管理
- ✅ 钱包管理
- ✅ 实名审核
- ✅ 提现审核
- ✅ 数据统计
- ✅ 系统配置

### 6. 活动系统
- ✅ 签到活动
- ✅ 红包系统
- ✅ 活动配置

### 7. 通知系统
- ✅ 短信通知
- ✅ 邮件通知
- ✅ 站内消息

---

## 📚 文档目录

- **README.md** - 项目说明（本文件）
- **QUICK_START.md** - 快速启动指南
- **PROJECT_STRUCTURE.md** - 项目结构说明
- **业务功能对比-完整版.md** - 与老项目业务对比
- **模块迁移-最终完成总结.md** - 迁移总结
- **技术文档/**
  - CONCURRENCY_OPTIMIZATION.md - 并发优化
  - TRANSACTION_LOCK_BEST_PRACTICE.md - 事务锁最佳实践
  - THREAD_POOL_*.md - 线程池相关文档

---

## 🧪 测试账号

### 管理员账号
```
用户名：admin
密码：admin123
```

### 测试流程
1. Admin后台登录
2. 注册测试用户
3. 模拟充值到账
4. 下单交易测试
5. 提现申请测试
6. 审核功能测试

---

## 📈 性能指标

| 指标 | 目标值 |
|------|--------|
| 订单处理延迟 | < 10ms |
| 撮合成功率 | > 99.9% |
| 并发处理能力 | 10000 TPS |
| 接口响应时间 | < 100ms |

---

## 🔧 配置说明

### 数据库配置
修改各模块的 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cex_xxx
    username: root
    password: root
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
```

### Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
```

---

## 🐛 问题排查

### 常见问题

**Q1: 服务无法注册到Nacos？**
- 检查Nacos是否启动
- 检查 `server-addr` 配置是否正确
- 检查网络连通性

**Q2: RocketMQ连接失败？**
- 检查NameServer是否启动
- 检查端口9876是否开放
- 撮合功能依赖RocketMQ

**Q3: 数据库连接失败？**
- 检查MySQL是否启动
- 检查数据库是否已创建
- 检查用户名密码是否正确

---

## 📞 技术支持

如有问题，请查看：
1. `QUICK_START.md` - 快速启动指南
2. `业务功能对比-完整版.md` - 业务说明
3. `模块迁移-最终完成总结.md` - 技术细节

---

## 📄 开源协议

MIT License

---

## 🎉 项目亮点

### 技术亮点
- ✅ Spring Cloud Alibaba 全家桶
- ✅ MyBatis Plus 优雅ORM
- ✅ RocketMQ 高性能消息
- ✅ JWT 无状态认证
- ✅ 乐观锁并发控制

### 业务亮点
- ✅ 完整的交易流程
- ✅ 成熟的撮合算法
- ✅ 完善的风控体系
- ✅ 丰富的活动系统
- ✅ 专业的管理后台

---

**项目已就绪，可直接部署使用！** 🚀🚀🚀
