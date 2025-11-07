-- ===========================================
-- 活动系统表
-- ===========================================

-- 活动表
CREATE TABLE IF NOT EXISTS `activity` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '活动名称',
    `type` tinyint(1) NOT NULL COMMENT '活动类型（1抢购 2分配 3挖矿 4红包）',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '活动状态（0未开始 1进行中 2已结束）',
    `start_time` varchar(20) NOT NULL COMMENT '开始时间',
    `end_time` varchar(20) NOT NULL COMMENT '结束时间',
    `coin` varchar(20) NOT NULL COMMENT '活动币种',
    `total_amount` decimal(20,8) NOT NULL COMMENT '活动总数量',
    `released_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '已发放数量',
    `price` decimal(20,8) DEFAULT NULL COMMENT '活动价格',
    `participant_limit` int(11) DEFAULT NULL COMMENT '参与人数限制',
    `participant_count` int(11) DEFAULT '0' COMMENT '已参与人数',
    `description` text COMMENT '活动描述',
    `rules` text COMMENT '活动规则',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0未启用 1已启用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 红包表
CREATE TABLE IF NOT EXISTS `red_packet` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `packet_id` varchar(50) NOT NULL COMMENT '红包ID',
    `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
    `type` tinyint(1) NOT NULL COMMENT '红包类型（1普通红包 2拼手气红包）',
    `coin` varchar(20) NOT NULL COMMENT '红包币种',
    `total_amount` decimal(20,8) NOT NULL COMMENT '红包总金额',
    `total_count` int(11) NOT NULL COMMENT '红包总个数',
    `received_count` int(11) DEFAULT '0' COMMENT '已领取个数',
    `received_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '已领取金额',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '红包状态（0未发送 1已发送 2已领完 3已过期）',
    `expire_time` varchar(20) DEFAULT NULL COMMENT '过期时间',
    `blessing` varchar(200) DEFAULT NULL COMMENT '红包祝福语',
    `is_public` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公开（0不公开 1公开）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_packet_id` (`packet_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包表';
