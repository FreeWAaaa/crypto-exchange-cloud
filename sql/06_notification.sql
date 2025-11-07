-- ====================================
-- CEX 通知系统数据库
-- ====================================

CREATE DATABASE IF NOT EXISTS cex_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cex_notification;

-- ====================================
-- 1. 站内消息表
-- ====================================
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '用户ID（0表示系统消息）',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` INT DEFAULT 1 COMMENT '消息类型（1系统通知 2交易通知 3活动通知）',
  `is_read` INT DEFAULT 0 COMMENT '是否已读（0未读 1已读）',
  `business_id` VARCHAR(100) DEFAULT NULL COMMENT '关联业务ID',
  `business_type` VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',
  `link` VARCHAR(500) DEFAULT NULL COMMENT '链接地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ====================================
-- 2. 短信发送记录表
-- ====================================
DROP TABLE IF EXISTS `sms_record`;
CREATE TABLE `sms_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
  `content` VARCHAR(500) NOT NULL COMMENT '短信内容',
  `sms_type` INT DEFAULT 1 COMMENT '短信类型（1验证码 2通知 3营销）',
  `status` INT DEFAULT 0 COMMENT '发送状态（0待发送 1已发送 2发送失败）',
  `response_code` VARCHAR(50) DEFAULT NULL COMMENT '第三方响应码',
  `response_message` VARCHAR(500) DEFAULT NULL COMMENT '第三方响应消息',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_status` (`status`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_sms_type` (`sms_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送记录表';

-- ====================================
-- 3. 邮件发送记录表
-- ====================================
DROP TABLE IF EXISTS `email_record`;
CREATE TABLE `email_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `to_email` VARCHAR(100) NOT NULL COMMENT '收件人邮箱',
  `subject` VARCHAR(200) NOT NULL COMMENT '邮件主题',
  `content` TEXT NOT NULL COMMENT '邮件内容',
  `email_type` INT DEFAULT 1 COMMENT '邮件类型（1验证码 2通知 3营销）',
  `status` INT DEFAULT 0 COMMENT '发送状态（0待发送 1已发送 2发送失败）',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_to_email` (`to_email`),
  KEY `idx_status` (`status`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_email_type` (`email_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送记录表';

-- ====================================
-- 索引优化说明
-- ====================================
-- 1. sys_message 表：
--    - idx_user_id: 用户ID索引，快速查询用户消息
--    - idx_is_read: 已读状态索引，筛选未读消息
--    - idx_create_time: 时间索引，按时间排序
--    - idx_message_type: 消息类型索引

-- 2. sms_record 表：
--    - idx_mobile: 手机号索引
--    - idx_status: 状态索引，筛选发送状态
--    - idx_send_time: 发送时间索引

-- 3. email_record 表：
--    - idx_to_email: 邮箱索引
--    - idx_status: 状态索引
--    - idx_send_time: 发送时间索引
