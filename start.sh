#!/bin/bash

# 加密货币交易所启动脚本

echo "=========================================="
echo "    Crypto Exchange Platform Startup     "
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请安装Maven 3.6+"
    exit 1
fi

# 检查端口是否被占用
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null; then
        echo "警告: 端口 $1 已被占用"
        return 1
    fi
    return 0
}

# 检查所有服务端口
ports=(8080 8081 8082 8083 8084 8085 8086 8087)
for port in "${ports[@]}"; do
    check_port $port
done

echo "开始编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

echo "编译完成，开始启动服务..."

# 启动函数
start_service() {
    local service_name=$1
    local port=$2
    local dir=$3
    
    echo "启动 $service_name 服务 (端口: $port)..."
    cd $dir
    nohup mvn spring-boot:run > ../logs/${service_name}.log 2>&1 &
    echo $! > ../logs/${service_name}.pid
    cd ..
    
    # 等待服务启动
    sleep 5
    if check_port $port; then
        echo "✓ $service_name 服务启动成功"
    else
        echo "✗ $service_name 服务启动失败"
    fi
}

# 创建日志目录
mkdir -p logs

# 按顺序启动服务
start_service "Gateway" 8080 "cex-gateway"
start_service "User" 8081 "cex-user"
start_service "Trade" 8082 "cex-trade"
start_service "Wallet" 8083 "cex-wallet"
start_service "Matching" 8084 "cex-matching"
start_service "Admin" 8085 "cex-admin"
start_service "Notification" 8086 "cex-notification"
start_service "Activity" 8087 "cex-activity"

echo ""
echo "=========================================="
echo "    所有服务启动完成！"
echo "=========================================="
echo "API网关: http://localhost:8080"
echo "用户服务: http://localhost:8081"
echo "交易服务: http://localhost:8082"
echo "钱包服务: http://localhost:8083"
echo "撮合引擎: http://localhost:8084"
echo "管理服务: http://localhost:8085"
echo "通知服务: http://localhost:8086"
echo "活动服务: http://localhost:8087"
echo ""
echo "查看日志: tail -f logs/*.log"
echo "停止服务: ./stop.sh"
echo "=========================================="
