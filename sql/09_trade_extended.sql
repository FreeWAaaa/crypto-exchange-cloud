-- ===========================================
-- 交易系统相关表
-- ===========================================

-- 交易记录表
CREATE TABLE IF NOT EXISTS `trade_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trade_id` varchar(50) NOT NULL COMMENT '交易ID',
    `symbol` varchar(20) NOT NULL COMMENT '交易对',
    `buy_order_id` bigint(20) NOT NULL COMMENT '买方订单ID',
    `sell_order_id` bigint(20) NOT NULL COMMENT '卖方订单ID',
    `buy_user_id` bigint(20) NOT NULL COMMENT '买方用户ID',
    `sell_user_id` bigint(20) NOT NULL COMMENT '卖方用户ID',
    `price` decimal(20,8) NOT NULL COMMENT '成交价格',
    `amount` decimal(20,8) NOT NULL COMMENT '成交数量',
    `money` decimal(20,8) NOT NULL COMMENT '成交金额',
    `buy_fee` decimal(20,8) DEFAULT '0.00000000' COMMENT '买方手续费',
    `sell_fee` decimal(20,8) DEFAULT '0.00000000' COMMENT '卖方手续费',
    `fee_coin` varchar(20) DEFAULT NULL COMMENT '手续费币种',
    `trade_time` varchar(20) NOT NULL COMMENT '交易时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_id` (`trade_id`),
    KEY `idx_symbol` (`symbol`),
    KEY `idx_buy_user_id` (`buy_user_id`),
    KEY `idx_sell_user_id` (`sell_user_id`),
    KEY `idx_trade_time` (`trade_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- K线数据表
CREATE TABLE IF NOT EXISTS `kline_data` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `symbol` varchar(20) NOT NULL COMMENT '交易对',
    `period` varchar(10) NOT NULL COMMENT '周期（1m, 5m, 15m, 30m, 1h, 4h, 1d）',
    `open_price` decimal(20,8) NOT NULL COMMENT '开盘价',
    `high_price` decimal(20,8) NOT NULL COMMENT '最高价',
    `low_price` decimal(20,8) NOT NULL COMMENT '最低价',
    `close_price` decimal(20,8) NOT NULL COMMENT '收盘价',
    `volume` decimal(20,8) NOT NULL COMMENT '成交量',
    `amount` decimal(20,8) NOT NULL COMMENT '成交额',
    `start_time` varchar(20) NOT NULL COMMENT '开始时间',
    `end_time` varchar(20) NOT NULL COMMENT '结束时间',
    `count` int(11) DEFAULT '0' COMMENT '成交笔数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_symbol_period_time` (`symbol`, `period`, `start_time`),
    KEY `idx_symbol_period` (`symbol`, `period`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='K线数据表';
