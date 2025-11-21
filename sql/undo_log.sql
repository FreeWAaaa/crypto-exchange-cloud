-- Seata AT 模式 undo_log 表
-- 需要在每个业务数据库中创建此表
-- 用于存储 Seata 的回滚日志

-- 使用说明：
-- 1. 在 cex_trade 数据库中执行：USE cex_trade; 然后执行下面的 CREATE TABLE
-- 2. 在 cex_wallet 数据库中执行：USE cex_wallet; 然后执行下面的 CREATE TABLE

CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

