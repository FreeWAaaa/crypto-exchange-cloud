-- 交易数据库初始化脚本
CREATE DATABASE IF NOT EXISTS cex_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cex_trade;

-- 交易对表（扩展字段）
CREATE TABLE IF NOT EXISTS trade_symbol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    symbol VARCHAR(20) NOT NULL UNIQUE COMMENT '交易对名称',
    base_coin VARCHAR(10) NOT NULL COMMENT '基础币种',
    quote_coin VARCHAR(10) NOT NULL COMMENT '计价币种',
    status TINYINT DEFAULT 0 COMMENT '交易对状态（0正常 1停用）',
    min_trade_amount DECIMAL(20,8) NOT NULL COMMENT '最小交易数量',
    max_trade_amount DECIMAL(20,8) NOT NULL COMMENT '最大交易数量',
    price_precision INT DEFAULT 2 COMMENT '价格精度',
    amount_precision INT DEFAULT 8 COMMENT '数量精度',
    fee_rate DECIMAL(10,6) DEFAULT 0.001 COMMENT '手续费率',
    sort INT DEFAULT 0 COMMENT '排序',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门（0否 1是）',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐（0否 1是）',
    symbol_type TINYINT DEFAULT 0 COMMENT '交易对类型（0现货 1期货）',
    enable_market_buy TINYINT DEFAULT 1 COMMENT '是否启用市价买（0否 1是）',
    enable_market_sell TINYINT DEFAULT 1 COMMENT '是否启用市价卖（0否 1是）',
    min_turnover DECIMAL(20,8) DEFAULT 0 COMMENT '最小成交额',
    min_sell_price DECIMAL(20,8) DEFAULT 0 COMMENT '最小卖出价格',
    max_buy_price DECIMAL(20,8) DEFAULT 0 COMMENT '最高买入价格',
    max_trading_order INT DEFAULT 0 COMMENT '最大同时交易订单数（0不限制）',
    max_trading_time INT DEFAULT 0 COMMENT '委托超时时间（秒，0不过期）',
    visible TINYINT DEFAULT 1 COMMENT '是否可见（0不可见 1可见）',
    tradeable TINYINT DEFAULT 1 COMMENT '是否可交易（0不可交易 1可交易）',
    zone INT DEFAULT 0 COMMENT '交易区域（0默认 1创新区 2主板区）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_symbol (symbol),
    INDEX idx_status (status),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易对表';

-- 订单表（扩展字段）
CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    base_coin VARCHAR(10) NOT NULL COMMENT '基础币种',
    quote_coin VARCHAR(10) NOT NULL COMMENT '计价币种',
    order_type TINYINT NOT NULL COMMENT '订单类型（1限价单 2市价单）',
    side TINYINT NOT NULL COMMENT '买卖方向（1买入 2卖出）',
    price DECIMAL(20,8) COMMENT '订单价格',
    amount DECIMAL(20,8) NOT NULL COMMENT '订单数量',
    filled_amount DECIMAL(20,8) DEFAULT 0 COMMENT '已成交数量',
    filled_money DECIMAL(20,8) DEFAULT 0 COMMENT '已成交金额',
    avg_price DECIMAL(20,8) DEFAULT 0 COMMENT '平均成交价',
    status TINYINT DEFAULT 0 COMMENT '订单状态（0待成交 1部分成交 2完全成交 3已撤销 4超时）',
    fee DECIMAL(20,8) DEFAULT 0 COMMENT '手续费',
    fee_coin VARCHAR(10) COMMENT '手续费币种',
    source TINYINT DEFAULT 1 COMMENT '订单来源（1Web 2APP 3API）',
    client_order_id VARCHAR(50) COMMENT '客户端订单ID',
    use_discount TINYINT DEFAULT 0 COMMENT '是否使用折扣（0不使用 1使用）',
    cancel_time DATETIME COMMENT '撤销时间',
    cancel_reason VARCHAR(255) COMMENT '撤销原因',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_symbol (symbol),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_user_symbol_status (user_id, symbol, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 成交记录表
CREATE TABLE IF NOT EXISTS trade_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    trade_id VARCHAR(50) NOT NULL UNIQUE COMMENT '成交ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    buy_order_no VARCHAR(50) NOT NULL COMMENT '买方订单号',
    sell_order_no VARCHAR(50) NOT NULL COMMENT '卖方订单号',
    buy_user_id BIGINT NOT NULL COMMENT '买方用户ID',
    sell_user_id BIGINT NOT NULL COMMENT '卖方用户ID',
    price DECIMAL(20,8) NOT NULL COMMENT '成交价格',
    amount DECIMAL(20,8) NOT NULL COMMENT '成交数量',
    money DECIMAL(20,8) NOT NULL COMMENT '成交金额',
    buy_fee DECIMAL(20,8) DEFAULT 0 COMMENT '买方手续费',
    sell_fee DECIMAL(20,8) DEFAULT 0 COMMENT '卖方手续费',
    trade_time DATETIME NOT NULL COMMENT '成交时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_trade_id (trade_id),
    INDEX idx_symbol (symbol),
    INDEX idx_buy_order_no (buy_order_no),
    INDEX idx_sell_order_no (sell_order_no),
    INDEX idx_trade_time (trade_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成交记录表';

-- 插入测试交易对
INSERT INTO trade_symbol (symbol, base_coin, quote_coin, min_trade_amount, max_trade_amount, price_precision, amount_precision, fee_rate, sort, is_hot, is_recommend, enable_market_buy, enable_market_sell, visible, tradeable) VALUES 
('BTC/USDT', 'BTC', 'USDT', 0.00001, 1000, 2, 8, 0.001, 1, 1, 1, 1, 1, 1, 1),
('ETH/USDT', 'ETH', 'USDT', 0.001, 10000, 2, 6, 0.001, 2, 1, 1, 1, 1, 1, 1),
('LTC/USDT', 'LTC', 'USDT', 0.01, 100000, 2, 4, 0.001, 3, 0, 0, 1, 1, 1, 1),
('BCH/USDT', 'BCH', 'USDT', 0.001, 10000, 2, 6, 0.001, 4, 0, 0, 1, 1, 1, 1);
