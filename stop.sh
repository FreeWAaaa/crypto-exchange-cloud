#!/bin/bash

# 加密货币交易所停止脚本

echo "=========================================="
echo "    Crypto Exchange Platform Shutdown    "
echo "=========================================="

# 停止函数
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "停止 $service_name 服务 (PID: $pid)..."
            kill $pid
            sleep 2
            
            # 强制杀死进程
            if ps -p $pid > /dev/null 2>&1; then
                echo "强制停止 $service_name 服务..."
                kill -9 $pid
            fi
            
            echo "✓ $service_name 服务已停止"
        else
            echo "✗ $service_name 服务未运行"
        fi
        rm -f "$pid_file"
    else
        echo "✗ 未找到 $service_name 服务的PID文件"
    fi
}

# 按顺序停止服务
stop_service "Activity"
stop_service "Notification"
stop_service "Admin"
stop_service "Matching"
stop_service "Wallet"
stop_service "Trade"
stop_service "User"
stop_service "Gateway"

echo ""
echo "=========================================="
echo "    所有服务已停止！"
echo "=========================================="
