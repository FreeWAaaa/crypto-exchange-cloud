-- ===========================================
-- 系统管理默认配置（仅插入，不建表）
-- 说明：表结构请以 05_admin.sql 为准，此文件仅做可重复执行的默认数据灌入
-- ===========================================

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`)
VALUES
('system.name', 'CEX交易所', '系统名称'),
('system.version', '1.0.0', '系统版本'),
('trade.fee.rate', '0.001', '交易手续费率'),
('trade.min.amount', '0.00000001', '最小交易数量'),
('security.login.max.attempts', '5', '最大登录尝试次数'),
('security.password.min.length', '8', '密码最小长度')
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `config_name` = VALUES(`config_name`);
