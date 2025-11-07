-- 用户数据库初始化脚本
CREATE DATABASE IF NOT EXISTS cex_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cex_user;

-- 用户表（扩展字段以兼容旧项目业务）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    salt VARCHAR(64) NOT NULL COMMENT '密码盐',
    mobile VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT 'https://bizzan.oss-cn-hangzhou.aliyuncs.com/defaultavatar.png' COMMENT '头像',
    status TINYINT DEFAULT 0 COMMENT '用户状态（0正常 1停用 2冻结）',
    verified TINYINT DEFAULT 0 COMMENT '是否实名认证（0未认证 1已认证 2审核中 3审核失败）',
    real_name VARCHAR(50) COMMENT '实名认证姓名',
    id_card VARCHAR(20) COMMENT '身份证号',
    id_card_front VARCHAR(255) COMMENT '身份证正面照片',
    id_card_back VARCHAR(255) COMMENT '身份证反面照片',
    id_card_hand VARCHAR(255) COMMENT '手持身份证照片',
    verified_time DATETIME COMMENT '实名认证时间',
    invite_code VARCHAR(20) UNIQUE COMMENT '邀请码',
    inviter_id BIGINT COMMENT '邀请人ID',
    first_level_count INT DEFAULT 0 COMMENT '一级邀请人数',
    second_level_count INT DEFAULT 0 COMMENT '二级邀请人数',
    third_level_count INT DEFAULT 0 COMMENT '三级邀请人数',
    level TINYINT DEFAULT 0 COMMENT '用户等级（0普通 1VIP1 2VIP2...）',
    trade_password VARCHAR(255) COMMENT '交易密码',
    trade_password_set TINYINT DEFAULT 0 COMMENT '是否设置交易密码（0未设置 1已设置）',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    google_auth_enabled TINYINT DEFAULT 0 COMMENT '是否启用谷歌验证器（0未启用 1已启用）',
    google_auth_secret VARCHAR(255) COMMENT '谷歌验证器密钥',
    sms_auth_enabled TINYINT DEFAULT 1 COMMENT '是否启用短信验证（0未启用 1已启用）',
    email_auth_enabled TINYINT DEFAULT 0 COMMENT '是否启用邮箱验证（0未启用 1已启用）',
    user_type TINYINT DEFAULT 0 COMMENT '用户类型（0普通用户 1VIP用户 2商家）',
    super_partner VARCHAR(10) DEFAULT '0' COMMENT '超级合伙人标识（0普通用户 1超级合伙人 2专业超级合伙人）',
    transactions INT DEFAULT 0 COMMENT '交易次数',
    appeal_times INT DEFAULT 0 COMMENT '申诉次数',
    appeal_success_times INT DEFAULT 0 COMMENT '申诉胜诉次数',
    business_status TINYINT DEFAULT 0 COMMENT '商家认证状态（0未认证 1已认证 2审核中 3审核失败）',
    business_apply_time DATETIME COMMENT '商家认证申请时间',
    business_verified_time DATETIME COMMENT '商家认证通过时间',
    can_publish_ad TINYINT DEFAULT 1 COMMENT '是否可以发布广告（0否 1是）',
    can_trade TINYINT DEFAULT 1 COMMENT '是否可以交易（0否 1是）',
    can_sign_in TINYINT DEFAULT 1 COMMENT '是否可以签到（0否 1是）',
    token VARCHAR(500) COMMENT 'JWT Token',
    token_expire_time DATETIME COMMENT 'Token过期时间',
    country VARCHAR(50) COMMENT '国家',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '地区',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_username (username),
    INDEX idx_mobile (mobile),
    INDEX idx_email (email),
    INDEX idx_invite_code (invite_code),
    INDEX idx_inviter_id (inviter_id),
    INDEX idx_status (status),
    INDEX idx_verified (verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户邀请关系表
CREATE TABLE IF NOT EXISTS user_invite_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    invite_level TINYINT NOT NULL DEFAULT 1 COMMENT '邀请层级（1一级 2二级 3三级）',
    reward_status TINYINT DEFAULT 0 COMMENT '奖励状态（0未发放 1已发放）',
    reward_amount DECIMAL(20,8) DEFAULT 0 COMMENT '奖励金额',
    reward_coin VARCHAR(20) COMMENT '奖励币种',
    reward_time DATETIME COMMENT '奖励发放时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_user_id (user_id),
    INDEX idx_inviter_id (inviter_id),
    INDEX idx_invite_level (invite_level),
    INDEX idx_reward_status (reward_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户邀请关系表';

-- 用户支付信息表
CREATE TABLE IF NOT EXISTS user_payment_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    payment_type TINYINT NOT NULL COMMENT '支付类型（1银行卡 2支付宝 3微信）',
    bank_name VARCHAR(100) COMMENT '银行名称',
    bank_card VARCHAR(50) COMMENT '银行卡号',
    bank_branch VARCHAR(200) COMMENT '开户行',
    alipay_account VARCHAR(100) COMMENT '支付宝账号',
    wechat_account VARCHAR(100) COMMENT '微信账号',
    real_name VARCHAR(50) NOT NULL COMMENT '收款人姓名',
    qr_code VARCHAR(255) COMMENT '收款二维码',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    INDEX idx_user_id (user_id),
    INDEX idx_payment_type (payment_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户支付信息表';

-- 插入测试用户（注意：密码为123456，salt需要根据实际生成）
-- 测试密码: 123456
-- Salt示例: a1b2c3d4e5f6
-- 加密后密码: MD5(123456 + salt)
INSERT INTO sys_user (username, password, salt, mobile, nickname, invite_code, level, can_trade, can_publish_ad, can_sign_in) VALUES 
('admin', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'a1b2c3d4e5f6g7h8i9j0', '13800138000', '管理员', 'U2AAAAAA', 5, 1, 1, 1),
('testuser', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'b2c3d4e5f6g7h8i9j0k1', '13800138001', '测试用户', 'U2AAAAAB', 0, 1, 1, 1);
