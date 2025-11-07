-- ===========================================
-- 用户认证相关表
-- ===========================================

-- 用户实名认证表
CREATE TABLE IF NOT EXISTS `user_verification` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
    `id_card` varchar(18) NOT NULL COMMENT '身份证号',
    `id_card_front` varchar(255) DEFAULT NULL COMMENT '身份证正面照片',
    `id_card_back` varchar(255) DEFAULT NULL COMMENT '身份证反面照片',
    `id_card_hand` varchar(255) DEFAULT NULL COMMENT '手持身份证照片',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '认证状态（0待审核 1审核通过 2审核拒绝）',
    `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_time` varchar(20) DEFAULT NULL COMMENT '审核时间',
    `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
    `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实名认证表';

-- 用户邀请记录表
CREATE TABLE IF NOT EXISTS `user_invite` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inviter_id` bigint(20) NOT NULL COMMENT '邀请人ID',
    `invitee_id` bigint(20) NOT NULL COMMENT '被邀请人ID',
    `invite_code` varchar(20) NOT NULL COMMENT '邀请码',
    `reward_amount` decimal(20,8) DEFAULT '0.00000000' COMMENT '邀请奖励',
    `reward_coin` varchar(20) DEFAULT NULL COMMENT '奖励币种',
    `reward_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '奖励状态（0未发放 1已发放）',
    `reward_time` varchar(20) DEFAULT NULL COMMENT '奖励发放时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    PRIMARY KEY (`id`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_invitee_id` (`invitee_id`),
    KEY `idx_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请记录表';
