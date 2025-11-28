-- 系统配置初始化脚本
-- 注意：执行前请先删除欢迎通知、提醒通知模板等不需要的配置

-- 删除不需要的配置（如果有的话）
DELETE FROM system_config WHERE config_key LIKE '%welcome%' OR config_key LIKE '%notification%' OR config_key LIKE '%template%';

-- 插入分享码相关配置
INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('share_code_length', '8', 'share_code', '班级分享码长度（位数）', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('share_code_expire_days', '30', 'share_code', '班级分享码有效期（天数）', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

-- 插入企业相关配置（示例，可根据实际需要调整）
INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('enterprise_default_status', '1', 'enterprise', '企业默认状态：1-启用，0-禁用', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

-- 插入学校相关配置（示例，可根据实际需要调整）
INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('school_default_status', '1', 'school', '学校默认状态：1-启用，0-禁用', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

-- 插入学生相关配置（示例，可根据实际需要调整）
INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('student_default_status', '1', 'student', '学生默认状态：1-启用，0-禁用', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

-- 插入评价权重配置
INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('enterprise_evaluation_weight', '0.4', 'evaluation', '企业评价权重（综合成绩计算用，小数形式，如0.4表示40%），三个权重之和必须等于1.0', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('school_evaluation_weight', '0.4', 'evaluation', '学校评价权重（综合成绩计算用，小数形式，如0.4表示40%），三个权重之和必须等于1.0', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

INSERT INTO system_config (config_key, config_value, config_type, description, delete_flag, create_time, update_time) 
VALUES 
('student_self_evaluation_weight', '0.2', 'evaluation', '学生自评权重（综合成绩计算用，小数形式，如0.2表示20%），三个权重之和必须等于1.0', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), update_time = NOW();

