# 项目结构说明

```
crypto-exchange/
├── pom.xml                          # 父项目POM文件
├── README.md                        # 项目说明文档
├── docker-compose.yml               # Docker编排文件
├── start.sh                         # 启动脚本
├── stop.sh                          # 停止脚本
├── sql/                             # 数据库初始化脚本
│   ├── 01_user.sql                  # 用户数据库
│   ├── 02_trade.sql                 # 交易数据库
│   ├── 03_wallet.sql               # 钱包数据库
│   ├── 04_matching.sql             # 撮合引擎数据库
│   ├── 05_admin.sql                # 管理数据库
│   ├── 06_notification.sql         # 通知数据库
│   └── 07_activity.sql             # 活动数据库
├── cex-common/                      # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/cex/common/
│       ├── core/
│       │   ├── domain/              # 基础实体类
│       │   │   ├── BaseEntity.java
│       │   │   └── Result.java
│       │   ├── exception/           # 异常类
│       │   │   └── BusinessException.java
│       │   └── util/               # 工具类
│       │       └── JwtUtils.java
│       └── ...
├── cex-gateway/                     # API网关服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/gateway/
│       ├── GatewayApplication.java
│       └── ...
├── cex-user/                        # 用户服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/user/
│       ├── UserApplication.java
│       ├── domain/
│       │   ├── entity/              # 实体类
│       │   │   └── User.java
│       │   └── dto/                # 数据传输对象
│       │       ├── UserRegisterDTO.java
│       │       └── UserLoginDTO.java
│       ├── service/                # 服务层
│       │   ├── UserService.java
│       │   └── impl/
│       │       └── UserServiceImpl.java
│       ├── mapper/                 # 数据访问层
│       │   └── UserMapper.java
│       ├── controller/            # 控制器层
│       │   └── UserController.java
│       └── src/main/resources/
│           └── application.yml
├── cex-trade/                      # 交易服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/trade/
│       ├── TradeApplication.java
│       ├── domain/
│       │   ├── entity/
│       │   │   ├── TradeSymbol.java
│       │   │   └── TradeOrder.java
│       │   └── dto/
│       │       └── PlaceOrderDTO.java
│       ├── service/
│       │   ├── TradeService.java
│       │   └── impl/
│       │       └── TradeServiceImpl.java
│       ├── mapper/
│       │   └── TradeOrderMapper.java
│       ├── controller/
│       │   └── TradeController.java
│       └── src/main/resources/
│           └── application.yml
├── cex-wallet/                     # 钱包服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/wallet/
│       ├── WalletApplication.java
│       ├── domain/
│       │   └── entity/
│       │       ├── WalletBalance.java
│       │       ├── WalletDeposit.java
│       │       └── WalletWithdraw.java
│       ├── service/
│       │   ├── WalletService.java
│       │   └── impl/
│       │       └── WalletServiceImpl.java
│       ├── mapper/
│       │   └── WalletBalanceMapper.java
│       ├── controller/
│       │   └── WalletController.java
│       └── src/main/resources/
│           └── application.yml
├── cex-matching/                   # 撮合引擎服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/matching/
│       ├── MatchingApplication.java
│       ├── domain/
│       │   ├── entity/
│       │   │   ├── OrderBook.java
│       │   │   └── TradeRecord.java
│       │   └── dto/
│       │       └── OrderDTO.java
│       ├── service/
│       │   ├── MatchingService.java
│       │   └── impl/
│       │       └── MatchingServiceImpl.java
│       ├── controller/
│       │   └── MatchingController.java
│       └── src/main/resources/
│           └── application.yml
├── cex-admin/                      # 管理服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/admin/
│       ├── AdminApplication.java
│       └── src/main/resources/
│           └── application.yml
├── cex-notification/               # 通知服务
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/cex/notification/
│       ├── NotificationApplication.java
│       └── src/main/resources/
│           └── application.yml
└── cex-activity/                   # 活动服务
    ├── pom.xml
    ├── Dockerfile
    └── src/main/java/com/cex/activity/
        ├── ActivityApplication.java
        └── src/main/resources/
            └── application.yml
```

## 服务端口分配

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| cex-gateway | 8080 | API网关 |
| cex-user | 8081 | 用户服务 |
| cex-trade | 8082 | 交易服务 |
| cex-wallet | 8083 | 钱包服务 |
| cex-matching | 8084 | 撮合引擎 |
| cex-admin | 8085 | 管理服务 |
| cex-notification | 8086 | 通知服务 |
| cex-activity | 8087 | 活动服务 |

## 数据库分配

| 服务名称 | 数据库名称 | 说明 |
|---------|-----------|------|
| cex-user | cex_user | 用户相关数据 |
| cex-trade | cex_trade | 交易相关数据 |
| cex-wallet | cex_wallet | 钱包相关数据 |
| cex-matching | cex_matching | 撮合引擎数据 |
| cex-admin | cex_admin | 管理后台数据 |
| cex-notification | cex_notification | 通知相关数据 |
| cex-activity | cex_activity | 活动相关数据 |

## Redis数据库分配

| 服务名称 | Redis DB | 说明 |
|---------|----------|------|
| cex-user | 0 | 用户缓存 |
| cex-trade | 1 | 交易缓存 |
| cex-wallet | 2 | 钱包缓存 |
| cex-matching | 3 | 撮合引擎缓存 |
