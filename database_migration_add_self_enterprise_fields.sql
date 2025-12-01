-- 为自主实习申请添加统一社会信用代码、所属行业、法人代表字段
-- 执行时间：2024年
-- 状态：已完成

USE internship_management_db;

-- 添加 self_unified_social_credit_code 字段（统一社会信用代码）
-- 注意：如果字段已存在，执行会报错，需要先检查
ALTER TABLE `internship_apply` 
ADD COLUMN `self_unified_social_credit_code` VARCHAR(50) NULL 
AFTER `self_enterprise_address`;

-- 添加 self_industry 字段（所属行业）
ALTER TABLE `internship_apply` 
ADD COLUMN `self_industry` VARCHAR(100) NULL 
AFTER `self_unified_social_credit_code`;

-- 添加 self_legal_person 字段（法人代表）
ALTER TABLE `internship_apply` 
ADD COLUMN `self_legal_person` VARCHAR(50) NULL 
AFTER `self_industry`;

-- 检查字段是否存在的SQL（执行前可以先运行此查询）
-- SELECT COLUMN_NAME FROM information_schema.COLUMNS 
-- WHERE TABLE_SCHEMA = 'internship_management_db' 
-- AND TABLE_NAME = 'internship_apply' 
-- AND COLUMN_NAME IN ('self_unified_social_credit_code', 'self_industry', 'self_legal_person');

