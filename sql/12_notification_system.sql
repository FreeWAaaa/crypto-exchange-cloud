-- ===========================================
-- 通知系统表
-- ===========================================

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` text NOT NULL COMMENT '通知内容',
    `type` tinyint(1) NOT NULL COMMENT '通知类型（1系统通知 2交易通知 3活动通知 4安全通知）',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '通知状态（0未读 1已读）',
    `is_push` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否推送（0不推送 1推送）',
    `push_time` varchar(20) DEFAULT NULL COMMENT '推送时间',
    `read_time` varchar(20) DEFAULT NULL COMMENT '阅读时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
