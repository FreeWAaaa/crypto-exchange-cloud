-- ===========================================
-- 钱包系统扩展表
-- ===========================================

-- 钱包地址表
CREATE TABLE IF NOT EXISTS `wallet_address` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `coin` varchar(20) NOT NULL COMMENT '币种',
    `address` varchar(100) NOT NULL COMMENT '钱包地址',
    `private_key` varchar(255) DEFAULT NULL COMMENT '私钥（加密存储）',
    `address_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '地址类型（1充值地址 2提现地址）',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0未启用 1已启用）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_coin_address` (`user_id`, `coin`, `address`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coin` (`coin`),
    KEY `idx_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包地址表';

-- 钱包流水记录表
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `coin` varchar(20) NOT NULL COMMENT '币种',
    `type` tinyint(1) NOT NULL COMMENT '交易类型（1充值 2提现 3买入 4卖出 5手续费 6奖励 7退款）',
    `amount` decimal(20,8) NOT NULL COMMENT '交易金额',
    `fee` decimal(20,8) DEFAULT '0.00000000' COMMENT '手续费',
    `balance` decimal(20,8) NOT NULL COMMENT '余额（交易后余额）',
    `order_id` varchar(50) DEFAULT NULL COMMENT '关联订单ID',
    `tx_hash` varchar(100) DEFAULT NULL COMMENT '交易哈希',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '交易状态（0待确认 1已确认 2失败）',
    `confirmations` int(11) DEFAULT '0' COMMENT '确认数',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coin` (`coin`),
    KEY `idx_type` (`type`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_tx_hash` (`tx_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水记录表';
