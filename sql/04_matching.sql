-- 撮合引擎数据库初始化脚本
CREATE DATABASE IF NOT EXISTS cex_matching DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cex_matching;

-- K线数据表
CREATE TABLE IF NOT EXISTS kline_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    period VARCHAR(10) NOT NULL COMMENT '周期（1m, 5m, 15m, 30m, 1h, 4h, 1d）',
    open_price DECIMAL(20,8) NOT NULL COMMENT '开盘价',
    high_price DECIMAL(20,8) NOT NULL COMMENT '最高价',
    low_price DECIMAL(20,8) NOT NULL COMMENT '最低价',
    close_price DECIMAL(20,8) NOT NULL COMMENT '收盘价',
    volume DECIMAL(20,8) DEFAULT 0 COMMENT '成交量',
    amount DECIMAL(20,8) DEFAULT 0 COMMENT '成交额',
    trade_count INT DEFAULT 0 COMMENT '成交笔数',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    UNIQUE KEY uk_symbol_period_time (symbol, period, start_time),
    INDEX idx_symbol (symbol),
    INDEX idx_period (period),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='K线数据表';

-- 深度数据表
CREATE TABLE IF NOT EXISTS depth_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    side TINYINT NOT NULL COMMENT '买卖方向（1买入 2卖出）',
    price DECIMAL(20,8) NOT NULL COMMENT '价格',
    amount DECIMAL(20,8) NOT NULL COMMENT '数量',
    order_count INT DEFAULT 0 COMMENT '订单数量',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_symbol_side_price (symbol, side, price),
    INDEX idx_symbol (symbol),
    INDEX idx_side (side)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='深度数据表';

-- 行情数据表
CREATE TABLE IF NOT EXISTS market_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    symbol VARCHAR(20) NOT NULL UNIQUE COMMENT '交易对',
    price DECIMAL(20,8) NOT NULL COMMENT '最新价格',
    change_24h DECIMAL(20,8) DEFAULT 0 COMMENT '24小时涨跌',
    change_rate_24h DECIMAL(10,6) DEFAULT 0 COMMENT '24小时涨跌幅',
    high_24h DECIMAL(20,8) DEFAULT 0 COMMENT '24小时最高价',
    low_24h DECIMAL(20,8) DEFAULT 0 COMMENT '24小时最低价',
    volume_24h DECIMAL(20,8) DEFAULT 0 COMMENT '24小时成交量',
    amount_24h DECIMAL(20,8) DEFAULT 0 COMMENT '24小时成交额',
    market_cap DECIMAL(20,8) DEFAULT 0 COMMENT '市值',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_symbol (symbol),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行情数据表';
