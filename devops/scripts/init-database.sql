-- 数据库初始化脚本
-- 基于实体类设计的SQL脚本 (无外键约束)

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
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) DEFAULT 'resident',
  `avatar` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_enabled` boolean DEFAULT TRUE,
  `is_banned` boolean DEFAULT FALSE,
  `ban_end_time` timestamp NULL DEFAULT NULL,
  `ban_reason` varchar(255) DEFAULT NULL,
  `is_locked` boolean DEFAULT FALSE,
  `lock_end_time` timestamp NULL DEFAULT NULL,
  `lock_reason` varchar(255) DEFAULT NULL,
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
  `lost_date` date DEFAULT NULL,
  `lost_location` varchar(255) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `images` text DEFAULT NULL,
  `reward` decimal(10, 2) DEFAULT NULL,
  `contact_info` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `user_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 失物招领表
-- ----------------------------
DROP TABLE IF EXISTS `found_items`;
CREATE TABLE IF NOT EXISTS `found_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text,
  `found_date` date DEFAULT NULL,
  `found_location` varchar(255) DEFAULT NULL,
  `storage_location` varchar(255) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `images` text DEFAULT NULL,
  `contact_info` varchar(100) DEFAULT NULL,
  `claim_requirements` text DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `user_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 认领申请表
-- ----------------------------
DROP TABLE IF EXISTS `claim_applications`;
CREATE TABLE IF NOT EXISTS `claim_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `found_item_id` bigint NOT NULL,
  `applicant_id` bigint NOT NULL,
  `description` text,
  `status` varchar(20) DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `processed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_found_item_id` (`found_item_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 留言表
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE IF NOT EXISTS `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `item_id` bigint NOT NULL,
  `item_type` varchar(20) NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_item_id_type` (`item_id`, `item_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE IF NOT EXISTS `announcements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `admin_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT 'published',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 论坛帖子表
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE IF NOT EXISTS `forum_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `images` text DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 举报表
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE IF NOT EXISTS `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_type` varchar(20) NOT NULL COMMENT '举报类型: LOST_ITEM, FOUND_ITEM, COMMENT',
  `reported_item_id` bigint NOT NULL COMMENT '被举报的内容ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `reported_user_id` bigint NOT NULL COMMENT '被举报人ID',
  `reason` text NOT NULL COMMENT '举报原因',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, RESOLVED, REJECTED',
  `resolution_notes` text DEFAULT NULL COMMENT '处理说明',
  `resolved_by_admin_id` bigint DEFAULT NULL COMMENT '处理管理员ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `resolved_at` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_type_item` (`report_type`, `reported_item_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1; 