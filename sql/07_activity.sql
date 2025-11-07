-- ====================================
-- CEX 活动系统数据库
-- ====================================

CREATE DATABASE IF NOT EXISTS cex_activity DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cex_activity;

-- ====================================
-- 1. 签到活动表
-- ====================================
DROP TABLE IF EXISTS `sign_activity`;
CREATE TABLE `sign_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coin` VARCHAR(20) NOT NULL COMMENT '赠送币种',
  `amount` DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '赠送数量',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `status` INT DEFAULT 0 COMMENT '活动状态（0进行中 1已结束）',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '活动标题',
  `description` TEXT DEFAULT NULL COMMENT '活动描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到活动表';

-- 初始化签到活动
INSERT INTO `sign_activity` (`coin`, `amount`, `end_date`, `status`, `title`, `description`) 
VALUES ('USDT', 1.00000000, '2026-12-31', 0, '每日签到送USDT', '每日签到可获得1USDT奖励');

-- ====================================
-- 2. 签到记录表
-- ====================================
DROP TABLE IF EXISTS `sign_record`;
CREATE TABLE `sign_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `coin` VARCHAR(20) NOT NULL COMMENT '赠送币种',
  `amount` DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '赠送数量',
  `sign_date` DATE NOT NULL COMMENT '签到日期',
  `consecutive_days` INT DEFAULT 1 COMMENT '连续签到天数',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`, `deleted`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ====================================
-- 3. 红包表
-- ====================================
DROP TABLE IF EXISTS `red_envelope`;
CREATE TABLE `red_envelope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `envelope_no` VARCHAR(50) NOT NULL COMMENT '红包编号',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '发起人ID（0表示平台红包）',
  `envelope_type` INT DEFAULT 0 COMMENT '红包类型（0随机 1定额）',
  `total_amount` DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '红包总额',
  `received_amount` DECIMAL(18,8) DEFAULT 0 COMMENT '已领取金额',
  `count` INT NOT NULL COMMENT '红包数量',
  `received_count` INT DEFAULT 0 COMMENT '已领取数量',
  `coin` VARCHAR(20) NOT NULL COMMENT '红包币种',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '红包标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '红包描述',
  `status` INT DEFAULT 0 COMMENT '状态（0领取中 1已领完 2已过期）',
  `expired_hours` INT DEFAULT 24 COMMENT '过期小时数',
  `is_invite` INT DEFAULT 0 COMMENT '是否邀请红包（0否 1是）',
  `is_platform` INT DEFAULT 0 COMMENT '是否平台红包（0否 1是）',
  `max_random` DECIMAL(18,8) DEFAULT NULL COMMENT '最大随机金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_envelope_no` (`envelope_no`, `deleted`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包表';

-- ====================================
-- 4. 红包领取详情表
-- ====================================
DROP TABLE IF EXISTS `red_envelope_detail`;
CREATE TABLE `red_envelope_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `envelope_id` BIGINT NOT NULL COMMENT '红包ID',
  `envelope_no` VARCHAR(50) NOT NULL COMMENT '红包编号',
  `user_id` BIGINT NOT NULL COMMENT '领取人ID',
  `amount` DECIMAL(18,8) NOT NULL DEFAULT 0 COMMENT '领取金额',
  `coin` VARCHAR(20) NOT NULL COMMENT '领取币种',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_envelope_user` (`envelope_id`, `user_id`, `deleted`),
  KEY `idx_envelope_no` (`envelope_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包领取详情表';

-- ====================================
-- 索引优化说明
-- ====================================
-- 1. sign_activity 表：
--    - idx_status: 状态索引，筛选进行中的活动
--    - idx_end_date: 结束日期索引

-- 2. sign_record 表：
--    - uk_user_date: 用户+日期唯一索引，防止重复签到
--    - idx_activity_id: 活动ID索引
--    - idx_sign_date: 签到日期索引

-- 3. red_envelope 表：
--    - uk_envelope_no: 红包编号唯一索引
--    - idx_user_id: 用户ID索引，查询用户发送的红包
--    - idx_status: 状态索引
--    - idx_create_time: 时间索引

-- 4. red_envelope_detail 表：
--    - uk_envelope_user: 红包+用户唯一索引，防止重复领取
--    - idx_envelope_no: 红包编号索引
--    - idx_user_id: 用户ID索引，查询用户领取的红包
--    - idx_create_time: 时间索引
