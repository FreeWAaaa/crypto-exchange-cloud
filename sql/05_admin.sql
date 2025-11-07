-- ====================================
-- CEX 管理后台数据库
-- ====================================

CREATE DATABASE IF NOT EXISTS cex_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cex_admin;

-- ====================================
-- 1. 管理员用户表
-- ====================================
DROP TABLE IF EXISTS `sys_admin_user`;
CREATE TABLE `sys_admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `salt` VARCHAR(50) NOT NULL COMMENT '盐值',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
  `role_name` VARCHAR(50) DEFAULT NULL COMMENT '角色名称',
  `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
  `dept_name` VARCHAR(50) DEFAULT NULL COMMENT '部门名称',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` INT DEFAULT 0 COMMENT '状态（0正常 1禁用）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- 初始化管理员账号（密码：admin123）
INSERT INTO `sys_admin_user` (`username`, `password`, `salt`, `real_name`, `role_name`, `status`) 
VALUES ('admin', '2c9f0b42e1e3f6e8e7a6d4b3c5a8f9d1', 'abc123', '超级管理员', '超级管理员', 0);

-- ====================================
-- 2. 系统配置表
-- ====================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_type` INT DEFAULT 1 COMMENT '配置类型（1系统配置 2业务配置）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `status` INT DEFAULT 0 COMMENT '状态（0启用 1禁用）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
  `deleted` INT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`, `deleted`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`) VALUES
('system.name', 'CEX数字货币交易所', '系统名称', 1, '系统显示名称'),
('system.version', '1.0.0', '系统版本', 1, '当前系统版本号'),
('withdraw.audit.mode', 'manual', '提现审核模式', 2, 'manual-人工审核 auto-自动审核'),
('withdraw.min.amount', '0.01', '最小提现金额', 2, '单次最小提现金额'),
('withdraw.fee.rate', '0.001', '提现手续费率', 2, '默认提现手续费率'),
('trade.fee.rate', '0.002', '交易手续费率', 2, '默认交易手续费率'),
('register.sms.verify', 'true', '注册短信验证', 2, '是否开启注册短信验证'),
('register.invite.required', 'false', '注册邀请码必填', 2, '注册时是否必须填写邀请码');

-- ====================================
-- 3. 操作日志表
-- ====================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `admin_name` VARCHAR(50) DEFAULT NULL COMMENT '管理员姓名',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作内容',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
  `location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
  `status` INT DEFAULT 0 COMMENT '状态（0成功 1失败）',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `cost_time` BIGINT DEFAULT NULL COMMENT '耗时（毫秒）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ====================================
-- 索引优化说明
-- ====================================
-- 1. sys_admin_user 表：
--    - uk_username: 用户名唯一索引，支持软删除
--    - idx_status: 状态索引，快速筛选启用/禁用用户
--    - idx_create_time: 创建时间索引，支持按时间排序

-- 2. sys_config 表：
--    - uk_config_key: 配置键唯一索引
--    - idx_config_type: 配置类型索引，按类型分组查询
--    - idx_status: 状态索引

-- 3. sys_operation_log 表：
--    - idx_admin_id: 管理员ID索引，查询操作记录
--    - idx_module: 模块索引，按模块查询
--    - idx_create_time: 时间索引，按时间范围查询
