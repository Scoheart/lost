-- 数据库初始化脚本
-- 基于实体类自动生成的SQL脚本 (无外键约束)

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `lost_and_found` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `lost_and_found`;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `role` varchar(20) DEFAULT 'USER',
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_username` (`username`),
  UNIQUE KEY `UK_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 寻物启事表
-- ----------------------------
DROP TABLE IF EXISTS `lost_items`;
CREATE TABLE IF NOT EXISTS `lost_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text,
  `category` varchar(50) DEFAULT NULL,
  `lost_time` datetime DEFAULT NULL,
  `lost_location` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `contact_info` varchar(100) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `status` varchar(20) DEFAULT 'OPEN',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 失物招领表
-- ----------------------------
DROP TABLE IF EXISTS `found_items`;
CREATE TABLE IF NOT EXISTS `found_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text,
  `category` varchar(50) DEFAULT NULL,
  `found_time` datetime DEFAULT NULL,
  `found_location` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `contact_info` varchar(100) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `status` varchar(20) DEFAULT 'OPEN',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 认领申请表
-- ----------------------------
DROP TABLE IF EXISTS `claim_applications`;
CREATE TABLE IF NOT EXISTS `claim_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL,
  `item_type` varchar(20) NOT NULL,
  `applicant_id` bigint NOT NULL,
  `description` text,
  `contact_info` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 留言表
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE IF NOT EXISTS `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `user_id` bigint NOT NULL,
  `post_id` bigint DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE IF NOT EXISTS `announcements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `publisher_id` bigint NOT NULL,
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 论坛帖子表
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE IF NOT EXISTS `forum_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `author_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `views` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 举报表
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE IF NOT EXISTS `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(20) NOT NULL,
  `reason` text NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1; 