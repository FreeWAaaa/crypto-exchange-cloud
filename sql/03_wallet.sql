-- 钱包数据库初始化脚本
CREATE DATABASE IF NOT EXISTS cex_wallet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cex_wallet;

-- 用户资产表（扩展字段）
CREATE TABLE IF NOT EXISTS wallet_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coin VARCHAR(10) NOT NULL COMMENT '币种',
    available_balance DECIMAL(26,16) DEFAULT 0 COMMENT '可用余额',
    frozen_balance DECIMAL(26,16) DEFAULT 0 COMMENT '冻结余额',
    total_balance DECIMAL(26,16) DEFAULT 0 COMMENT '总余额',
    address VARCHAR(255) COMMENT '充值地址',
    memo VARCHAR(50) COMMENT 'Memo标签（EOS/XRP等需要）',
    to_released DECIMAL(18,8) DEFAULT 0 COMMENT '待释放余额',
    is_lock TINYINT DEFAULT 0 COMMENT '钱包锁定状态（0正常 1锁定）',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    UNIQUE KEY uk_user_coin (user_id, coin),
    INDEX idx_user_id (user_id),
    INDEX idx_coin (coin),
    INDEX idx_address (address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资产表';

-- 充值记录表
CREATE TABLE IF NOT EXISTS wallet_deposit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    deposit_no VARCHAR(50) NOT NULL UNIQUE COMMENT '充值单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coin VARCHAR(10) NOT NULL COMMENT '币种',
    address VARCHAR(255) NOT NULL COMMENT '充值地址',
    amount DECIMAL(20,8) NOT NULL COMMENT '充值数量',
    confirm_amount DECIMAL(20,8) DEFAULT 0 COMMENT '确认数量',
    confirmations INT DEFAULT 0 COMMENT '当前确认次数',
    need_confirmations INT DEFAULT 3 COMMENT '需要确认次数',
    tx_hash VARCHAR(255) UNIQUE COMMENT '交易哈希',
    block_height BIGINT COMMENT '区块高度',
    confirm_time DATETIME COMMENT '确认时间',
    status TINYINT DEFAULT 0 COMMENT '充值状态（0待确认 1已确认 2失败）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_deposit_no (deposit_no),
    INDEX idx_user_id (user_id),
    INDEX idx_coin (coin),
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_address (address),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- 提现记录表
CREATE TABLE IF NOT EXISTS wallet_withdraw (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    withdraw_no VARCHAR(50) NOT NULL UNIQUE COMMENT '提现单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coin VARCHAR(10) NOT NULL COMMENT '币种',
    address VARCHAR(255) NOT NULL COMMENT '提现地址',
    amount DECIMAL(20,8) NOT NULL COMMENT '提现数量',
    fee DECIMAL(20,8) DEFAULT 0 COMMENT '手续费',
    actual_amount DECIMAL(20,8) DEFAULT 0 COMMENT '实际到账数量',
    tx_hash VARCHAR(255) COMMENT '交易哈希',
    block_height BIGINT COMMENT '区块高度',
    confirmations INT DEFAULT 0 COMMENT '确认次数',
    status TINYINT DEFAULT 0 COMMENT '提现状态（0待审核 1审核通过 2已发送 3已完成 4已拒绝 5已撤销）',
    auditor_id BIGINT COMMENT '审核人ID',
    auditor VARCHAR(50) COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    send_time DATETIME COMMENT '发送时间',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_withdraw_no (withdraw_no),
    INDEX idx_user_id (user_id),
    INDEX idx_coin (coin),
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录表';

-- 资产流水表
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coin VARCHAR(10) NOT NULL COMMENT '币种',
    flow_type TINYINT NOT NULL COMMENT '流水类型（1充值 2提现 3交易买入 4交易卖出 5手续费 6转账 7奖励 8系统调整）',
    amount DECIMAL(20,8) NOT NULL COMMENT '变动数量',
    balance_before DECIMAL(26,16) NOT NULL COMMENT '变动前余额',
    balance_after DECIMAL(26,16) NOT NULL COMMENT '变动后余额',
    fee DECIMAL(20,8) DEFAULT 0 COMMENT '手续费',
    related_id VARCHAR(50) COMMENT '关联ID（订单号、充值单号等）',
    tx_hash VARCHAR(255) COMMENT '交易哈希',
    status TINYINT DEFAULT 1 COMMENT '状态（0待确认 1已确认 2失败）',
    confirmations INT DEFAULT 0 COMMENT '确认数',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_user_id (user_id),
    INDEX idx_coin (coin),
    INDEX idx_flow_type (flow_type),
    INDEX idx_related_id (related_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产流水表';

-- 钱包地址表
CREATE TABLE IF NOT EXISTS wallet_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coin VARCHAR(10) NOT NULL COMMENT '币种',
    address VARCHAR(255) NOT NULL COMMENT '钱包地址',
    memo VARCHAR(50) COMMENT 'Memo标签',
    private_key VARCHAR(500) COMMENT '私钥（加密存储）',
    address_type TINYINT DEFAULT 1 COMMENT '地址类型（1充值地址 2提现地址 3冷钱包地址）',
    address_source TINYINT DEFAULT 1 COMMENT '地址来源（1RPC生成 2用户导入 3系统分配）',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用（0未启用 1已启用）',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    last_use_time DATETIME COMMENT '最后使用时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    UNIQUE KEY uk_address (address),
    INDEX idx_user_id (user_id),
    INDEX idx_coin (coin),
    INDEX idx_address_type (address_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包地址表';

-- 插入测试数据
INSERT INTO wallet_balance (user_id, coin, available_balance, frozen_balance, total_balance, version) VALUES
(1, 'BTC', 1.00000000, 0.00000000, 1.00000000, 0),
(1, 'ETH', 10.00000000, 0.00000000, 10.00000000, 0),
(1, 'USDT', 10000.00000000, 0.00000000, 10000.00000000, 0),
(2, 'BTC', 0.50000000, 0.00000000, 0.50000000, 0),
(2, 'ETH', 5.00000000, 0.00000000, 5.00000000, 0),
(2, 'USDT', 5000.00000000, 0.00000000, 5000.00000000, 0);
