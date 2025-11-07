# 快速启动指南

## 环境准备

### 1. 安装基础环境
```bash
# 安装JDK 17+
brew install openjdk@17

# 安装Maven
brew install maven

# 安装MySQL
brew install mysql

# 安装Redis
brew install redis

# 安装Docker (可选)
brew install docker
```

### 2. 启动基础服务
```bash
# 启动MySQL
brew services start mysql

# 启动Redis
brew services start redis

# 创建数据库
mysql -u root -p < sql/01_user.sql
mysql -u root -p < sql/02_trade.sql
mysql -u root -p < sql/03_wallet.sql
mysql -u root -p < sql/04_matching.sql
mysql -u root -p < sql/05_admin.sql
mysql -u root -p < sql/06_notification.sql
mysql -u root -p < sql/07_activity.sql
```

### 3. 启动Nacos
```bash
# 下载Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.0/nacos-server-2.2.0.tar.gz
tar -xzf nacos-server-2.2.0.tar.gz
cd nacos/bin

# 启动Nacos (单机模式)
sh startup.sh -m standalone
```

## 启动项目

### 方式一：使用启动脚本
```bash
# 给脚本执行权限
chmod +x start.sh stop.sh

# 启动所有服务
./start.sh

# 停止所有服务
./stop.sh
```

### 方式二：使用Docker Compose
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 停止所有服务
docker-compose down
```

### 方式三：手动启动
```bash
# 编译项目
mvn clean compile

# 启动网关服务
cd cex-gateway && mvn spring-boot:run &

# 启动用户服务
cd cex-user && mvn spring-boot:run &

# 启动交易服务
cd cex-trade && mvn spring-boot:run &

# 启动钱包服务
cd cex-wallet && mvn spring-boot:run &

# 启动撮合引擎
cd cex-matching && mvn spring-boot:run &

# 启动管理服务
cd cex-admin && mvn spring-boot:run &

# 启动通知服务
cd cex-notification && mvn spring-boot:run &

# 启动活动服务
cd cex-activity && mvn spring-boot:run &
```

## 验证服务

### 1. 检查服务状态
```bash
# 检查端口占用
lsof -i :8080  # 网关
lsof -i :8081  # 用户服务
lsof -i :8082  # 交易服务
lsof -i :8083  # 钱包服务
lsof -i :8084  # 撮合引擎
lsof -i :8085  # 管理服务
lsof -i :8086  # 通知服务
lsof -i :8087  # 活动服务
```

### 2. 测试API接口
```bash
# 测试用户注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "mobile": "13800138000",
    "smsCode": "123456",
    "agreeTerms": true
  }'

# 测试用户登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'

# 测试获取交易对列表
curl -X GET http://localhost:8080/api/trade/symbol/list

# 测试获取用户资产
curl -X GET "http://localhost:8080/api/wallet/balance/list?userId=1"
```

## 常见问题

### 1. 端口被占用
```bash
# 查看端口占用
lsof -i :8080

# 杀死进程
kill -9 <PID>
```

### 2. 数据库连接失败
```bash
# 检查MySQL状态
brew services list | grep mysql

# 重启MySQL
brew services restart mysql

# 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES;"
```

### 3. Redis连接失败
```bash
# 检查Redis状态
brew services list | grep redis

# 重启Redis
brew services restart redis

# 测试Redis连接
redis-cli ping
```

### 4. Nacos连接失败
```bash
# 检查Nacos状态
curl http://localhost:8848/nacos/v1/console/health

# 重启Nacos
cd nacos/bin && sh shutdown.sh && sh startup.sh -m standalone
```

## 开发调试

### 1. 查看日志
```bash
# 查看所有服务日志
tail -f logs/*.log

# 查看特定服务日志
tail -f logs/gateway.log
tail -f logs/user.log
```

### 2. 热重载开发
```bash
# 使用Spring Boot DevTools
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

### 3. 数据库调试
```bash
# 连接数据库
mysql -u root -p cex_user

# 查看表结构
DESCRIBE sys_user;

# 查看数据
SELECT * FROM sys_user LIMIT 10;
```

## 性能优化

### 1. JVM参数调优
```bash
# 在启动脚本中添加JVM参数
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### 2. 数据库连接池调优
```yaml
# 在application.yml中调整连接池参数
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
```

### 3. Redis连接池调优
```yaml
# 在application.yml中调整Redis连接池参数
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-wait: -1ms
        max-idle: 10
        min-idle: 5
```
