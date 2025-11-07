# CEX 加密货币交易所系统

## 🎯 项目概述

基于Spring Cloud Alibaba微服务架构的加密货币交易所系统，参考了[Dylan-CS/crypto-exchange](https://github.com/Dylan-CS/crypto-exchange)项目的业务逻辑，使用更现代化的技术栈重构。

## 🏗️ 技术架构

### 微服务架构
- **注册中心**: Nacos Discovery
- **配置中心**: Nacos Config  
- **API网关**: Spring Cloud Gateway
- **熔断器**: Sentinel
- **消息队列**: RocketMQ
- **数据库**: MySQL + Redis
- **ORM框架**: MyBatis Plus
- **构建工具**: Maven

### 服务模块
- `cex-common`: 公共模块
- `cex-gateway`: API网关
- `cex-user`: 用户服务
- `cex-trade`: 交易服务
- `cex-wallet`: 钱包服务
- `cex-matching`: 撮合引擎
- `cex-admin`: 管理后台
- `cex-notification`: 通知服务
- `cex-activity`: 活动服务

## 📊 核心业务功能

### 1. 用户管理系统 (cex-user)
- ✅ 用户注册/登录
- ✅ 实名认证
- ✅ 邀请推广
- ✅ 用户等级管理
- ✅ 安全设置（谷歌验证器、短信验证）
- ✅ 用户验证记录管理
- ✅ 邀请奖励系统

### 2. 交易系统 (cex-trade)
- ✅ 币币交易
- ✅ 限价单/市价单
- ✅ 订单管理
- ✅ 交易记录
- ✅ K线数据
- ✅ 交易对管理
- ✅ 交易统计

### 3. 钱包系统 (cex-wallet)
- ✅ 资产管理
- ✅ 充值/提现
- ✅ 钱包地址管理
- ✅ 交易流水
- ✅ 多币种支持
- ✅ 余额冻结/解冻
- ✅ 钱包版本控制

### 4. 撮合引擎 (cex-matching)
- ✅ 订单撮合
- ✅ 价格匹配
- ✅ 深度数据
- ✅ 实时行情
- ✅ 撮合引擎控制
- ✅ 订单簿管理

### 5. 活动系统 (cex-activity)
- ✅ 创新实验室
- ✅ 红包功能
- ✅ 抢购活动
- ✅ 挖矿活动
- ✅ 推广奖励
- ✅ 活动管理
- ✅ 红包创建/抢取

### 6. 通知系统 (cex-notification)
- ✅ 系统通知
- ✅ 交易通知
- ✅ 活动通知
- ✅ 安全通知
- ✅ 通知推送
- ✅ 未读消息统计
- ✅ 通知管理

### 7. 管理后台 (cex-admin)
- ✅ 系统配置管理
- ✅ 数据统计
- ✅ 用户统计
- ✅ 交易统计
- ✅ 钱包统计
- ✅ 系统监控
- ✅ 配置参数管理

## 🗄️ 数据库设计

### 核心表结构
- `sys_user`: 用户基础信息
- `user_verification`: 用户实名认证
- `user_invite`: 用户邀请记录
- `trade_order`: 交易订单
- `trade_record`: 交易记录
- `trade_symbol`: 交易对
- `kline_data`: K线数据
- `wallet_balance`: 钱包余额
- `wallet_deposit`: 充值记录
- `wallet_withdraw`: 提现记录
- `wallet_address`: 钱包地址
- `wallet_transaction`: 钱包流水
- `activity`: 活动信息
- `red_packet`: 红包信息
- `notification`: 通知信息
- `sys_config`: 系统配置

## 🚀 快速开始

### 环境要求
- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+

### 启动步骤
1. **启动基础设施**
   ```bash
   # 启动Nacos
   sh nacos/bin/startup.sh -m standalone
   
   # 启动Redis
   redis-server
   
   # 启动MySQL
   mysql.server start
   ```

2. **初始化数据库**
   ```bash
   # 执行SQL脚本
   mysql -u root -p < sql/01_database.sql
   mysql -u root -p < sql/02_user.sql
   mysql -u root -p < sql/03_trade.sql
   mysql -u root -p < sql/04_wallet.sql
   mysql -u root -p < sql/05_matching.sql
   mysql -u root -p < sql/06_notification.sql
   mysql -u root -p < sql/07_activity.sql
   mysql -u root -p < sql/08_user_verification.sql
   mysql -u root -p < sql/09_trade_extended.sql
   mysql -u root -p < sql/10_wallet_extended.sql
   mysql -u root -p < sql/11_activity_system.sql
   mysql -u root -p < sql/12_notification_system.sql
   mysql -u root -p < sql/13_admin_system.sql
   ```

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **启动服务**
   ```bash
   # 启动网关
   mvn spring-boot:run -pl cex-gateway
   
   # 启动用户服务
   mvn spring-boot:run -pl cex-user
   
   # 启动交易服务
   mvn spring-boot:run -pl cex-trade
   
   # 启动钱包服务
   mvn spring-boot:run -pl cex-wallet
   
   # 启动撮合引擎
   mvn spring-boot:run -pl cex-matching
   ```

## 📈 系统特性

### 技术优势
- ✅ **现代化架构**: Spring Cloud Alibaba生态
- ✅ **高性能**: 微服务架构，支持水平扩展
- ✅ **高可用**: 服务熔断、限流、降级
- ✅ **易维护**: 模块化设计，代码结构清晰
- ✅ **易扩展**: 支持新功能快速开发

### 业务优势
- ✅ **完整功能**: 涵盖交易所核心业务
- ✅ **安全可靠**: 多重安全验证机制
- ✅ **用户体验**: 支持Web、APP多端
- ✅ **运营支持**: 丰富的活动营销功能

## 🔧 开发指南

### 代码规范
- 使用Lombok减少样板代码
- 统一异常处理
- 统一返回结果格式
- 完善的日志记录

### 数据库规范
- 统一使用BaseEntity基类
- 软删除机制
- 乐观锁版本控制
- 完善的索引设计

## 📝 更新日志

### v1.0.0 (2025-10-24)
- ✅ 完成基础架构搭建
- ✅ 迁移核心业务逻辑
- ✅ 完善数据库设计
- ✅ 修复编译问题
- ✅ 验证系统可用性

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进项目！

## 📄 许可证

本项目基于Apache 2.0许可证开源。

## ⚠️ 免责声明

本项目仅供学习研究使用，请勿用于商业用途。使用本项目进行商业活动造成的任何损失，作者不承担责任。
