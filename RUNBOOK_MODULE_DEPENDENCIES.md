# CEX 微服务依赖与启动指南

## 基础设施一览（必需/可选）
- 必需：
  - MySQL 8.x（业务库：cex_user/cex_trade/cex_wallet/cex_admin/cex_notification/cex_activity 等；Nacos 使用库：nacos）
  - Redis 6.x（缓存、分布式锁）
  - Nacos 2.2.x（注册中心 + 配置中心，端口 8848/9848）
- 可选（当前项目已集成）：
  - RocketMQ 4.9.x（NameServer 9876，Broker 10909/10911）
  - RocketMQ Console（8089 → 8080）
  - Sentinel 控制台（未启用控制台依赖，可后续接入）

> 参考 docker-compose.yml 中的服务名与端口映射，已为你适配 Apple Silicon（必要时设定 platform: linux/amd64）。

---

## 模块与依赖矩阵

| 模块 | 端口 | 直接依赖的中间件 | 依赖的其它服务 | 说明 |
|---|---|---|---|---|
| cex-gateway | 8080 | Nacos, Redis | 下游各业务服务 | API 入口，路由到各微服务；仅需中间件可先独立起动。
| cex-user | 8081 | MySQL, Redis, Nacos | 无强依赖（被网关调用） | 用户注册/登录/实名等。
| cex-trade | 8082 | MySQL, Redis, Nacos, RocketMQ | cex-matching（通过 MQ/HTTP 协作） | 下单、订单管理、交易记录等。
| cex-wallet | 8083 | MySQL, Redis, Nacos, RocketMQ | 无（被 trade/活动调用） | 充值/提现/余额流水。
| cex-matching | 8084 | Redis, Nacos, RocketMQ | 与 trade 通过 MQ 交互 | 撮合引擎（内存订单簿、撮合广播）。
| cex-admin | 8085 | MySQL, Nacos | 无 | 系统配置/统计等后台接口。
| cex-notification | 8086 | MySQL, Nacos | 无 | 系统/交易/活动通知。
| cex-activity | 8087 | MySQL, Nacos | cex-wallet（发放奖励时） | 活动、红包等。

---

## 最小启动集（按场景）

- 测试 User（注册/登录/鉴权）
  - 基础：MySQL, Redis, Nacos
  - 服务：cex-gateway, cex-user
  - 访问：`http://localhost:8080`（通过网关路由到 user）

- 测试 Admin（系统配置）
  - 基础：MySQL, Nacos
  - 服务：cex-gateway, cex-admin

- 测试 Trade（下单/订单查询，不含撮合）
  - 基础：MySQL, Redis, Nacos, RocketMQ
  - 服务：cex-gateway, cex-trade（可选：cex-wallet 若涉及余额冻结/扣减）
  - 说明：下单通常会发 MQ 消息给撮合；如不启用 matching，则无法形成真实成交，只能验证下单与订单数据侧。

- 测试 Matching（撮合链路）
  - 基础：Redis, Nacos, RocketMQ（NameServer + Broker + Console 可视化）
  - 服务：cex-matching（建议同时起 cex-trade 端到端联调）
  - 说明：trade -> MQ -> matching -> 反馈成交 -> trade/wallet。

- 测试 Wallet（充值提现/流水）
  - 基础：MySQL, Nacos
  - 服务：cex-gateway, cex-wallet
  - 说明：如从交易侧写入流水，需同时起 trade；从活动侧发奖，需起 activity。

- 测试 Notification（通知）
  - 基础：MySQL, Nacos
  - 服务：cex-gateway, cex-notification

- 测试 Activity（红包/活动参与）
  - 基础：MySQL, Nacos
  - 服务：cex-gateway, cex-activity（发奖联动 wallet 时需 cex-wallet）

---

## 推荐启动顺序（一次性启动全链）
1. 基础设施：MySQL → Redis → Nacos → RocketMQ（nameserver → broker → console）
2. 核心入口：cex-gateway（8080）
3. 业务微服务：
   - 常驻：cex-user → cex-wallet → cex-trade → cex-matching → cex-notification → cex-activity → cex-admin
   - 可按需启动：只测哪个模块就按“最小启动集”起对应服务

> 提示：网关与各服务均通过 Nacos 进行服务发现，必须保证 Nacos 可用，否则服务无法注册/发现。

---

## 端口/健康检查速查
- Nacos: 8848（Web `/nacos` 登录）
- RocketMQ NameServer: 9876（无 Web）
- RocketMQ Broker: 10911（Remoting），10909（HA）
- RocketMQ Console: 8089 → 8080（Web）
- Gateway: 8080（统一入口）
- 各服务：见矩阵（8081..8087）

---

## 常见问题与排查
- 服务启动报 Nacos 连接失败
  - 检查 `bootstrap.yml` 中 Nacos 地址（默认 `localhost:8848`）与 docker-compose 实际地址是否一致；本地容器互访使用服务名（如 `nacos:8848`）。
- MQ 相关功能不可用
  - 确认 NameServer/Broker 已启动；业务服务环境变量中存在 `SPRING_CLOUD_STREAM_ROCKETMQ_BINDER_NAME_SERVER` 指向 `rocketmq-nameserver:9876`。
- 数据库连接失败
  - 确认对应业务库已创建，连接串与账号权限正确。
- Apple Silicon 提示架构不匹配
  - 已在 compose 中显式 `platform: linux/amd64`，属正常仿真提示，不影响功能；如遇性能瓶颈再换 arm64 多架构镜像。

---

## 快速命令
- 仅启动某模块（示例：user）：
  - `docker compose up -d nacos mysql redis`（若未启）
  - `mvn -pl cex-user -am spring-boot:run`（或 IDE 直接运行）
  - `mvn -pl cex-gateway -am spring-boot:run`（或 IDE 直接运行）
- 启动撮合链：
  - `docker compose up -d rocketmq-nameserver rocketmq-broker rocketmq-console`
  - 运行 `cex-trade`、`cex-matching`、（可选）`cex-wallet`

---

## 备注
- Swagger 已在 `cex-admin`、`cex-wallet` 引入（springfox-boot-starter 3.0.0）。如需给其它模块补充 API 文档，同样引入该依赖即可。
- 若使用 Docker 运行业务服务镜像，确保容器内环境变量与 docker-compose 中保持一致（Nacos/Redis/MySQL/MQ 地址）。
