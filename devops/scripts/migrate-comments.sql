-- 评论数据迁移脚本
-- 此脚本用于将原有的评论数据结构更新为符合新的实体结构

-- 使用数据库
USE `lost_and_found`;

-- 开始事务
START TRANSACTION;

-- 备份原有数据（如果需要）
CREATE TABLE IF NOT EXISTS `comments_backup` LIKE `comments`;
INSERT INTO `comments_backup` SELECT * FROM `comments`;

-- 添加数据库表注释和字段注释（如果不存在）
ALTER TABLE `comments` 
COMMENT='评论表 - 支持物品评论和帖子评论';

ALTER TABLE `comments` 
MODIFY COLUMN `item_id` bigint NOT NULL COMMENT '关联的ID，可能是物品ID或帖子ID',
MODIFY COLUMN `item_type` varchar(20) NOT NULL COMMENT '类型: lost, found, post';

-- 检查是否存在无效类型的评论数据
SELECT COUNT(*) AS invalid_comments FROM `comments` 
WHERE item_type NOT IN ('lost', 'found', 'post');

-- 如果有无效数据，可以选择修正或移除
UPDATE `comments` SET item_type = 'post' 
WHERE item_type NOT IN ('lost', 'found', 'post');

-- 确保post类型的评论指向有效的帖子
-- 检查无效的帖子评论（指向不存在的帖子）
SELECT c.id, c.item_id, c.item_type 
FROM `comments` c 
LEFT JOIN `posts` p ON c.item_id = p.id 
WHERE c.item_type = 'post' AND p.id IS NULL;

-- 确保lost类型的评论指向有效的寻物启事
-- 检查无效的寻物启事评论
SELECT c.id, c.item_id, c.item_type 
FROM `comments` c 
LEFT JOIN `lost_items` l ON c.item_id = l.id 
WHERE c.item_type = 'lost' AND l.id IS NULL;

-- 确保found类型的评论指向有效的失物招领
-- 检查无效的失物招领评论
SELECT c.id, c.item_id, c.item_type 
FROM `comments` c 
LEFT JOIN `found_items` f ON c.item_id = f.id 
WHERE c.item_type = 'found' AND f.id IS NULL;

-- 确保所有评论都关联到有效的用户
-- 检查无效的用户评论
SELECT c.id, c.user_id 
FROM `comments` c 
LEFT JOIN `users` u ON c.user_id = u.id 
WHERE u.id IS NULL;

-- 提交事务
COMMIT;

-- 数据统计信息
SELECT 'Item comments' AS type, COUNT(*) AS count FROM `comments` WHERE item_type IN ('lost', 'found');
SELECT 'Post comments' AS type, COUNT(*) AS count FROM `comments` WHERE item_type = 'post';
SELECT 'Total comments' AS type, COUNT(*) AS count FROM `comments`; 