-- Seata 分布式事务数据库初始化脚本
-- 用于存储 Seata 的事务日志和锁信息

CREATE DATABASE IF NOT EXISTS cex_seata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cex_seata;

-- 全局事务表（存储全局事务信息）
CREATE TABLE IF NOT EXISTS global_table (
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    transaction_id BIGINT COMMENT '事务ID',
    status TINYINT NOT NULL COMMENT '事务状态（0:Active, 1:Committed, 2:Rollbacked, 3:Timeout Rollbacking, 4:Timeout Rollbacked, 5:Timeout Committing）',
    application_id VARCHAR(32) COMMENT '应用ID',
    transaction_service_group VARCHAR(32) COMMENT '事务服务组',
    transaction_name VARCHAR(128) COMMENT '事务名称',
    timeout INT COMMENT '超时时间',
    begin_time BIGINT COMMENT '开始时间',
    application_data VARCHAR(2000) COMMENT '应用数据',
    gmt_create DATETIME COMMENT '创建时间',
    gmt_modified DATETIME COMMENT '修改时间',
    PRIMARY KEY (xid),
    KEY idx_status_gmt_modified (status, gmt_modified),
    KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局事务表';

-- 分支事务表（存储分支事务信息）
CREATE TABLE IF NOT EXISTS branch_table (
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    transaction_id BIGINT COMMENT '事务ID',
    resource_group_id VARCHAR(32) COMMENT '资源组ID',
    resource_id VARCHAR(256) COMMENT '资源ID',
    branch_type VARCHAR(8) COMMENT '分支类型（AT、TCC、SAGA、XA）',
    status TINYINT COMMENT '分支事务状态（0:Registered, 1:Phase One Committed, 2:Phase One Rollbacked, 3:Phase Two Committed, 4:Phase Two Rollbacked）',
    client_id VARCHAR(64) COMMENT '客户端ID',
    application_data VARCHAR(2000) COMMENT '应用数据',
    gmt_create DATETIME(6) COMMENT '创建时间',
    gmt_modified DATETIME(6) COMMENT '修改时间',
    PRIMARY KEY (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分支事务表';

-- 锁表（存储锁信息）
CREATE TABLE IF NOT EXISTS lock_table (
    row_key VARCHAR(128) NOT NULL COMMENT '行键',
    xid VARCHAR(128) COMMENT '全局事务ID',
    transaction_id BIGINT COMMENT '事务ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    resource_id VARCHAR(256) COMMENT '资源ID',
    table_name VARCHAR(32) COMMENT '表名',
    pk VARCHAR(36) COMMENT '主键',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0:Unlocked, 1:Locked）',
    gmt_create DATETIME COMMENT '创建时间',
    gmt_modified DATETIME COMMENT '修改时间',
    PRIMARY KEY (row_key),
    KEY idx_branch_id (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='锁表';

-- 分布式锁表（用于 Seata 内部锁）
CREATE TABLE IF NOT EXISTS distributed_lock (
    lock_key VARCHAR(20) NOT NULL COMMENT '锁键',
    lock_value VARCHAR(20) NOT NULL COMMENT '锁值',
    expire BIGINT COMMENT '过期时间',
    PRIMARY KEY (lock_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁表';

