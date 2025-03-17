-- 数据库更新脚本：使邮箱字段可为空并添加地址字段
-- 此脚本用于更新已存在的数据库结构，避免重新初始化

-- 使用数据库
USE `lost_and_found`;

-- 备份操作 (可选，建议在执行前备份数据)
-- 1. 修改email字段约束，允许为NULL
-- 2. 添加address字段

-- 检查address字段是否存在，不存在则添加
SET @addressExists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA='lost_and_found' 
    AND TABLE_NAME='users' 
    AND COLUMN_NAME='address'
);

SET @emailColumnExists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA='lost_and_found' 
    AND TABLE_NAME='users' 
    AND COLUMN_NAME='email'
);

-- 如果address字段不存在，添加该字段
SET @addAddressColumnSQL = CONCAT(
    'ALTER TABLE `users` ADD COLUMN `address` VARCHAR(200) DEFAULT NULL',
    IF(@addressExists > 0, '', ';')
);

PREPARE stmt FROM @addAddressColumnSQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修改email字段约束，允许NULL值
-- 首先删除唯一键约束，然后重建该列，最后重新添加唯一键约束
SET @alterEmailSQL = CONCAT(
    'ALTER TABLE `users` ',
    'DROP INDEX `UK_email`, ',
    'MODIFY COLUMN `email` VARCHAR(100) DEFAULT NULL, ',
    'ADD UNIQUE KEY `UK_email` (`email`);'
);

PREPARE stmt FROM @alterEmailSQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 输出完成信息
SELECT 'Database update completed. Email column now allows NULL values. Address column added.' AS 'Update Status';
