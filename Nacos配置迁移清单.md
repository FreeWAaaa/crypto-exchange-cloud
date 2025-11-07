# 📋 Nacos配置迁移清单（方案A）

> **迁移方案**：全部配置迁移到Nacos，本地只保留Nacos连接信息  
> **文档用途**：可直接复制粘贴到Nacos配置中心  
> **创建时间**：2025-01-31

---

## 📋 目录

1. [迁移说明](#迁移说明)
2. [Nacos配置清单](#nacos配置清单)
3. [各服务配置详情](#各服务配置详情)
4. [本地保留配置](#本地保留配置)

---

## 📝 迁移说明

### 迁移原则

- ✅ **全部业务配置** → 迁移到Nacos
- ✅ **白名单配置** → 迁移到Nacos（从代码中提取）
- ✅ **本地只保留** → Nacos连接信息（bootstrap.yml）

### 配置命名规则

```
Data ID: ${spring.application.name}.yaml
Group: DEFAULT_GROUP
Namespace: public
```

例如：
- `cex-gateway.yaml`
- `cex-user.yaml`
- `cex-trade.yaml`

---

## 📦 Nacos配置清单

### 需要在Nacos中创建的配置

| 序号 | Data ID | Group | Namespace | 说明 |
|------|---------|-------|-----------|------|
| 1 | `cex-gateway.yaml` | DEFAULT_GROUP | public | 网关服务配置 |
| 2 | `cex-user.yaml` | DEFAULT_GROUP | public | 用户服务配置 |
| 3 | `cex-trade.yaml` | DEFAULT_GROUP | public | 交易服务配置 |
| 4 | `cex-wallet.yaml` | DEFAULT_GROUP | public | 钱包服务配置 |
| 5 | `cex-matching.yaml` | DEFAULT_GROUP | public | 撮合引擎配置 |
| 6 | `cex-admin.yaml` | DEFAULT_GROUP | public | 管理服务配置 |
| 7 | `cex-notification.yaml` | DEFAULT_GROUP | public | 通知服务配置 |
| 8 | `cex-activity.yaml` | DEFAULT_GROUP | public | 活动服务配置 |
| 9 | `common-config.yaml` | DEFAULT_GROUP | public | 公共配置（已存在） |

---

## 🔧 各服务配置详情

### 1. 网关服务配置（cex-gateway.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-gateway.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - API网关服务配置
# ===========================================

# Spring Cloud Gateway配置
spring:
  main:
    web-application-type: reactive
  
  cloud:
    gateway:
      # 服务发现配置
      discovery:
        locator:
          # 启用服务发现路由
          enabled: true
          # 服务ID转换为小写
          lower-case-service-id: true
          # 路由ID生成器
          route-id-generator: SimpleRouteIdGenerator
      
      # 路由配置
      routes:
        # 用户服务路由
        - id: cex-user
          uri: lb://cex-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway

        # 交易服务路由
        - id: cex-trade
          uri: lb://cex-trade
          predicates:
            - Path=/api/trade/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
        
        # 钱包服务路由
        - id: cex-wallet
          uri: lb://cex-wallet
          predicates:
            - Path=/api/wallet/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
        
        # 撮合引擎路由
        - id: cex-matching
          uri: lb://cex-matching
          predicates:
            - Path=/api/matching/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
        
        # 管理服务路由
        - id: cex-admin
          uri: lb://cex-admin
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
        
        # 通知服务路由
        - id: cex-notification
          uri: lb://cex-notification
          predicates:
            - Path=/api/notification/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
        
        # 活动服务路由
        - id: cex-activity
          uri: lb://cex-activity
          predicates:
            - Path=/api/activity/**
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Source, gateway
      
      # 全局跨域配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
      
      # 默认过滤器（对所有路由生效）
      default-filters:
        - AddRequestHeader=X-Gateway-Source, gateway
      
      # HTTP客户端配置
      httpclient:
        connect-timeout: 10000
        response-timeout: 30000
        pool:
          max-connections: 500
          max-connections-per-route: 50
          max-idle-time: 30000
          max-life-time: 60000

    # Sentinel熔断限流配置
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
        port: 8719
        heartbeat-interval-ms: 1000
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cex-gateway-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
            namespace: public
        ds2:
          nacos:
            server-addr: localhost:8848
            dataId: cex-gateway-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
            namespace: public
      scg:
        enabled: true
        fallback:
          mode: response
          response-status: 429
          response-body: '{"code":429,"message":"请求过于频繁，请稍后再试"}'

  # Redis配置（用于JWT黑名单）
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 200
        max-idle: 10
        min-idle: 5

# ===========================================
# 网关鉴权白名单配置
# ===========================================
gateway:
  auth:
    # 白名单路径列表（无需Token即可访问）
    white-list:
      # 用户服务 - 注册登录相关
      - /api/user/login
      - /api/user/register
      - /api/user/register/mobile
      - /api/user/register/email
      - /api/user/captcha
      - /api/user/send-sms
      - /api/user/send-email
      - /api/user/check-username
      - /api/user/check-mobile
      - /api/user/check-email
      - /api/user/forget-password
      - /api/user/reset-password
      - /api/user/info/public
      - /api/user/invite/check
      
      # 交易服务 - 行情数据（公开）
      - /api/trade/symbol/list
      - /api/trade/symbol/detail
      - /api/trade/market/ticker
      - /api/trade/market/depth
      - /api/trade/market/kline
      - /api/trade/market/trades
      
      # 活动服务 - 活动列表
      - /api/activity/list
      - /api/activity/detail/**
      
      # 通知服务 - 发送验证码
      - /api/notification/sms/send
      - /api/notification/email/send
      
      # 管理服务 - 管理员登录
      - /api/admin/login
      
      # 系统接口
      - /actuator/**
      - /swagger-ui/**
      - /v3/api-docs/**

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud.gateway: debug
    com.alibaba.nacos: info
    com.alibaba.csp.sentinel: info
    reactor.netty: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /Users/subby/logs/cex/gateway/gateway.log
    max-size: 100MB
    max-history: 30

# ===========================================
# 管理端点配置
# ===========================================
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  health:
    show-details: always
    show-components: always
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 60s
```

---

### 2. 用户服务配置（cex-user.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-user.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 用户服务配置
# ===========================================

# 数据源配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/cex_user?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      web-stat-filter:
        enabled: true
        url-pattern: "/*"
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"
      stat-view-servlet:
        enabled: true
        url-pattern: "/druid/*"
        allow: 127.0.0.1,192.168.163.1
        deny: 192.168.1.73
        reset-enable: false
        login-username: admin
        login-password: 123456
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 0
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
      shutdown-timeout: 100ms

  # Sentinel熔断限流配置
  cloud:
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
        port: 8719
        heartbeat-interval-ms: 1000
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cex-user-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
            namespace: public
        ds2:
          nacos:
            server-addr: localhost:8848
            dataId: cex-user-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
            namespace: public

# ===========================================
# MyBatis Plus配置
# ===========================================
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.cex.user.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      insert-strategy: NOT_NULL
      update-strategy: NOT_NULL
      select-strategy: NOT_EMPTY

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud: debug
    com.alibaba.nacos: info
    com.alibaba.csp.sentinel: info
    com.baomidou.mybatisplus: debug
    com.alibaba.druid: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /Users/subby/logs/cex/user/user.log
    max-size: 100MB
    max-history: 30

# ===========================================
# 应用自定义配置
# ===========================================
app:
  promote:
    prefix: http://localhost:8080/#/register?code=

# ===========================================
# 管理端点配置
# ===========================================
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  health:
    show-details: always
    show-components: always
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 60s
```

---

### 3. 交易服务配置（cex-trade.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-trade.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 交易服务配置
# ===========================================

# 数据源配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/cex_trade?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 1
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
      shutdown-timeout: 100ms
  
  # Spring Cloud Stream配置（RocketMQ）
  cloud:
    stream:
      function:
        definition: tradeResultInput;orderCompletedInput
      rocketmq:
        binder:
          name-server: localhost:9876
      bindings:
        order-input:
          destination: exchange-order-topic
          content-type: application/json
          producer:
            group: trade-order-group
            send-timeout: 3000
        order-cancel-input:
          destination: exchange-order-cancel-topic
          content-type: application/json
          producer:
            group: trade-cancel-group
            send-timeout: 3000
        tradeResultInput-in-0:
          destination: exchange-trade-result-topic
          content-type: application/json
          group: trade-result-group
          consumer:
            max-attempts: 3
            concurrency: 2
        orderCompletedInput-in-0:
          destination: exchange-order-completed-topic
          content-type: application/json
          group: trade-completed-group
          consumer:
            max-attempts: 3
            concurrency: 2

    # Sentinel熔断限流配置
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
        port: 8719
        heartbeat-interval-ms: 1000
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cex-trade-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
            namespace: public
        ds2:
          nacos:
            server-addr: localhost:8848
            dataId: cex-trade-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
            namespace: public

# ===========================================
# MyBatis Plus配置
# ===========================================
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.cex.trade.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      insert-strategy: NOT_NULL
      update-strategy: NOT_NULL
      select-strategy: NOT_EMPTY

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud: debug
    com.alibaba.nacos: info
    com.alibaba.csp.sentinel: info
    com.baomidou.mybatisplus: debug
    com.alibaba.druid: info
    org.apache.rocketmq: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /Users/subby/logs/cex/trade/trade.log
    max-size: 100MB
    max-history: 30

# ===========================================
# 管理端点配置
# ===========================================
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  health:
    show-details: always
    show-components: always
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 60s
```

---

### 4. 钱包服务配置（cex-wallet.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-wallet.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 钱包服务配置
# ===========================================

# 数据源配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/cex_wallet?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 2
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
      shutdown-timeout: 100ms
  
  # Spring Cloud Stream配置（RocketMQ）
  cloud:
    stream:
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          deposit-input:
            consumer:
              group: wallet-deposit-group
              max-attempts: 3
              concurrency: 1
          withdraw-input:
            consumer:
              group: wallet-withdraw-group
              max-attempts: 3
              concurrency: 1

    # Sentinel熔断限流配置
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
        port: 8719
        heartbeat-interval-ms: 1000
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cex-wallet-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
            namespace: public
        ds2:
          nacos:
            server-addr: localhost:8848
            dataId: cex-wallet-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
            namespace: public

# ===========================================
# MyBatis Plus配置
# ===========================================
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.cex.wallet.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      insert-strategy: NOT_NULL
      update-strategy: NOT_NULL
      select-strategy: NOT_EMPTY

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud: debug
    com.alibaba.nacos: info
    com.alibaba.csp.sentinel: info
    com.baomidou.mybatisplus: debug
    com.alibaba.druid: info
    org.apache.rocketmq: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /Users/subby/logs/cex/wallet/wallet.log
    max-size: 100MB
    max-history: 30

# ===========================================
# 管理端点配置
# ===========================================
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  health:
    show-details: always
    show-components: always
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 60s
```

---

### 5. 撮合引擎配置（cex-matching.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-matching.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 撮合引擎服务配置
# ===========================================

# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 3
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
      shutdown-timeout: 100ms
  
  # Spring Cloud Stream配置（RocketMQ）
  cloud:
    stream:
      function:
        definition: orderInput;orderCancelInput
      rocketmq:
        binder:
          name-server: localhost:9876
      bindings:
        orderInput-in-0:
          destination: exchange-order-topic
          content-type: application/json
          group: matching-order-group
          consumer:
            max-attempts: 3
            concurrency: 1
        orderCancelInput-in-0:
          destination: exchange-order-cancel-topic
          content-type: application/json
          group: matching-cancel-group
          consumer:
            max-attempts: 3
            concurrency: 1
        trade-result-out:
          destination: exchange-trade-result-topic
          content-type: application/json
          producer:
            group: matching-trade-group
            send-timeout: 3000
        order-completed-out:
          destination: exchange-order-completed-topic
          content-type: application/json
          producer:
            group: matching-order-completed-group
            send-timeout: 3000
        trade-plate-out:
          destination: exchange-trade-plate-topic
          content-type: application/json
          producer:
            group: matching-plate-group
            send-timeout: 3000

    # Sentinel熔断限流配置
    sentinel:
      enabled: true
      transport:
        dashboard: localhost:8080
        port: 8719
        heartbeat-interval-ms: 1000
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cex-matching-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: flow
            namespace: public
        ds2:
          nacos:
            server-addr: localhost:8848
            dataId: cex-matching-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
            namespace: public

# ===========================================
# 撮合引擎专用配置
# ===========================================
matching:
  engine:
    enabled: true
    thread-pool-size: 10
    timeout: 5000
    batch-size: 100
    matching-interval: 100
  orderbook:
    max-depth: 1000
    cleanup-interval: 60000
    expire-time: 300000
  kline:
    periods:
      - 1m
      - 5m
      - 15m
      - 30m
      - 1h
      - 4h
      - 1d
    retention-days: 30
    update-interval: 1000

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud: debug
    com.alibaba.nacos: info
    com.alibaba.csp.sentinel: info
    org.apache.rocketmq: info
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /Users/subby/logs/cex/matching/matching.log
    max-size: 100MB
    max-history: 30

# ===========================================
# 管理端点配置
# ===========================================
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  health:
    show-details: always
    show-components: always
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 60s
```

---

### 6. 管理服务配置（cex-admin.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-admin.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 管理服务配置
# ===========================================

# 数据源配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/cex_admin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,slf4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 4
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
      shutdown-timeout: 100ms

# ===========================================
# MyBatis Plus配置
# ===========================================
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.cex.admin.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      insert-strategy: NOT_NULL
      update-strategy: NOT_NULL
      select-strategy: NOT_EMPTY

# ===========================================
# Feign配置
# ===========================================
feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 10000
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50

# ===========================================
# 日志配置
# ===========================================
logging:
  level:
    com.cex: debug
    org.springframework.cloud: debug
    com.alibaba.nacos: info
  file:
    name: /Users/subby/logs/cex/admin/admin.log
```

---

### 7. 通知服务配置（cex-notification.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-notification.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 通知服务配置
# ===========================================

# 日志配置
logging:
  level:
    com.cex: debug
  file:
    name: /Users/subby/logs/cex/notification/notification.log
```

**说明**：通知服务配置较简单，主要是日志配置。其他配置（如邮件、短信）可以根据实际需求添加。

---

### 8. 活动服务配置（cex-activity.yaml）

**在Nacos中创建配置：**
- **Data ID**: `cex-activity.yaml`
- **Group**: `DEFAULT_GROUP`
- **Namespace**: `public`
- **配置格式**: `YAML`

**配置内容：**

```yaml
# ===========================================
# 加密货币交易所 - 活动服务配置
# ===========================================

# 日志配置
logging:
  level:
    com.cex: debug
  file:
    name: /Users/subby/logs/cex/activity/activity.log
```

**说明**：活动服务配置较简单，主要是日志配置。其他配置（如活动规则、红包配置）可以根据实际需求添加。

---

## 📝 本地保留配置

### 各服务的 bootstrap.yml（必须保留）

所有服务的 `bootstrap.yml` 保持不变，只包含 Nacos 连接信息：

```yaml
# Spring Cloud Bootstrap配置文件
# 用于在应用启动时加载外部配置，优先级高于application.yml
# 主要用于配置注册中心、配置中心等基础设施

spring:
  # 应用名称
  application:
    name: cex-xxx  # 根据服务名称修改
  
  # Spring Cloud配置
  cloud:
    # Nacos配置中心
    nacos:
      config:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
        file-extension: yaml
        enabled: true
        refresh-enabled: true
        shared-configs:
          - data-id: common-config.yaml
            group: DEFAULT_GROUP
            refresh: true
      
      # Nacos服务发现
      discovery:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
        enabled: true
        weight: 1
        metadata:
          version: 1.0.0
          zone: default
```

### 各服务的 application.yml（最小化）

所有服务的 `application.yml` 可以只保留端口配置（可选）：

```yaml
# 服务器配置（可选，也可以放到Nacos）
server:
  port: 8081  # 根据服务端口修改
```

**或者完全清空**，所有配置都从 Nacos 获取。

---

## ✅ 迁移步骤

### 1. 在Nacos中创建配置

1. 登录 Nacos 控制台（http://localhost:8848/nacos）
2. 进入 **配置管理** → **配置列表**
3. 点击 **+** 按钮创建配置
4. 按照上面的配置清单，逐个创建配置

### 2. 配置创建示例

以创建 `cex-gateway.yaml` 为例：

1. **Data ID**: `cex-gateway.yaml`
2. **Group**: `DEFAULT_GROUP`
3. **命名空间**: `public`（默认）
4. **配置格式**: `YAML`
5. **配置内容**: 复制上面的 `cex-gateway.yaml` 内容
6. **描述**: `网关服务配置`

### 3. 验证配置

1. 启动服务
2. 查看日志，确认从 Nacos 加载配置成功
3. 测试服务功能，确认配置生效

---

## 📋 配置清单总结

### 需要创建的配置（8个）

| 序号 | Data ID | 说明 | 优先级 |
|------|---------|------|--------|
| 1 | `cex-gateway.yaml` | 网关服务配置（含白名单） | ⭐⭐⭐ 最高 |
| 2 | `cex-user.yaml` | 用户服务配置 | ⭐⭐⭐ |
| 3 | `cex-trade.yaml` | 交易服务配置 | ⭐⭐⭐ |
| 4 | `cex-wallet.yaml` | 钱包服务配置 | ⭐⭐⭐ |
| 5 | `cex-matching.yaml` | 撮合引擎配置 | ⭐⭐ |
| 6 | `cex-admin.yaml` | 管理服务配置 | ⭐⭐ |
| 7 | `cex-notification.yaml` | 通知服务配置 | ⭐ |
| 8 | `cex-activity.yaml` | 活动服务配置 | ⭐ |

### 已存在的配置

- `common-config.yaml` - 公共配置（已存在，无需创建）

---

## 🎯 注意事项

### 1. 配置优先级

```
application.yml（本地） > Nacos配置 > bootstrap.yml（本地）
```

如果某个配置在多个地方都有，`application.yml` 会覆盖 Nacos 的配置。

### 2. 配置刷新

- `refresh-enabled: true` 已开启
- 修改 Nacos 配置后，服务会自动刷新
- 无需重启服务

### 3. 环境隔离

如果需要多环境（dev/test/prod），可以使用：
- **命名空间（Namespace）**：`dev`、`test`、`prod`
- **配置分组（Group）**：`DEV_GROUP`、`TEST_GROUP`、`PROD_GROUP`
- **Profile**：`cex-user-dev.yaml`、`cex-user-prod.yaml`

### 4. 白名单配置

网关白名单已从代码中提取到 `cex-gateway.yaml` 的 `gateway.auth.white-list` 配置项中。

**后续需要修改代码**，让 `AuthGlobalFilter` 从配置中读取白名单，而不是硬编码。

---

## 📝 后续代码修改建议

### 1. 修改 AuthGlobalFilter

将硬编码的白名单改为从配置读取：

```java
@ConfigurationProperties(prefix = "gateway.auth")
public class GatewayAuthProperties {
    private List<String> whiteList = new ArrayList<>();
    // getter/setter
}
```

### 2. 修改 application.yml

清空或最小化，只保留端口配置（可选）。

---

## ✅ 总结

### 已完成

1. ✅ 整理所有服务的配置到 Nacos 格式
2. ✅ 提取网关白名单配置
3. ✅ 创建完整的迁移清单文档

### 下一步

1. 在 Nacos 中创建上述 8 个配置
2. 清空或最小化各服务的 `application.yml`
3. 修改 `AuthGlobalFilter` 从配置读取白名单（可选，当前硬编码也能工作）
4. 测试验证配置加载

---

**文档版本**：v1.0  
**最后更新**：2025-01-31

