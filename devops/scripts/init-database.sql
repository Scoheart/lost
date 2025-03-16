-- 数据库初始化脚本
-- 基于实体类自动生成的SQL脚本 (无外键约束)

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `lost_and_found` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `lost_and_found`;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `role` varchar(20) NOT NULL DEFAULT 'resident' COMMENT '角色: resident(居民), admin(管理员), sysadmin(系统管理员)',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话号码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用: 1(启用), 0(禁用)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 寻物启事表
-- ----------------------------
DROP TABLE IF EXISTS `lost_items`;
CREATE TABLE `lost_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '寻物启事ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `description` text COMMENT '描述',
  `lost_date` date NOT NULL COMMENT '丢失日期',
  `lost_location` varchar(255) NOT NULL COMMENT '丢失地点',
  `category` varchar(50) NOT NULL COMMENT '物品类别',
  `images` text COMMENT '图片URL（JSON字符串）',
  `reward` decimal(10,2) DEFAULT '0.00' COMMENT '悬赏金额',
  `contact_info` varchar(255) NOT NULL COMMENT '联系方式',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), found(已找到), closed(已关闭)',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_lost_date` (`lost_date`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='寻物启事表';

-- ----------------------------
-- 失物招领表
-- ----------------------------
DROP TABLE IF EXISTS `found_items`;
CREATE TABLE `found_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '失物招领ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `description` text COMMENT '描述',
  `found_date` date NOT NULL COMMENT '拾取日期',
  `found_location` varchar(255) NOT NULL COMMENT '拾取地点',
  `storage_location` varchar(255) DEFAULT NULL COMMENT '存放地点',
  `category` varchar(50) NOT NULL COMMENT '物品类别',
  `images` text COMMENT '图片URL（JSON字符串）',
  `contact_info` varchar(255) NOT NULL COMMENT '联系方式',
  `claim_requirements` text COMMENT '认领要求',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待认领), claimed(已认领), closed(已关闭)',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_found_date` (`found_date`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表';

-- ----------------------------
-- 认领申请表
-- ----------------------------
DROP TABLE IF EXISTS `claim_applications`;
CREATE TABLE `claim_applications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '认领申请ID',
  `found_item_id` bigint(20) NOT NULL COMMENT '失物招领ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '申请说明',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '申请状态: pending(待处理), approved(已同意), rejected(已拒绝)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `processed_at` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_found_item_id` (`found_item_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认领申请表';

-- ----------------------------
-- 留言表
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `content` text NOT NULL COMMENT '留言内容',
  `item_id` bigint(20) NOT NULL COMMENT '关联的物品ID',
  `item_type` varchar(10) NOT NULL COMMENT '物品类型: lost(寻物启事), found(失物招领)',
  `user_id` bigint(20) NOT NULL COMMENT '发布留言的用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id_type` (`item_id`,`item_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言表';

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `admin_id` bigint(20) NOT NULL COMMENT '管理员ID',
  `status` varchar(20) NOT NULL DEFAULT 'published' COMMENT '公告状态: published(已发布), draft(草稿)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ----------------------------
-- 论坛帖子表
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE `forum_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `title` varchar(100) NOT NULL COMMENT '帖子标题',
  `content` text NOT NULL COMMENT '帖子内容',
  `images` text COMMENT '图片URL（JSON字符串）',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表';

-- ----------------------------
-- 论坛评论表
-- ----------------------------
DROP TABLE IF EXISTS `forum_comments`;
CREATE TABLE `forum_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `content` text NOT NULL COMMENT '评论内容',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论者用户ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID，用于回复',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论表';

-- ----------------------------
-- 举报表
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `report_type` varchar(20) NOT NULL COMMENT '举报类型: user, lost_item, found_item, forum_post, comment',
  `reported_item_id` bigint(20) NOT NULL COMMENT '被举报项目ID',
  `reported_user_id` bigint(20) NOT NULL COMMENT '被举报用户ID',
  `reporter_user_id` bigint(20) NOT NULL COMMENT '举报者用户ID',
  `reason` text NOT NULL COMMENT '举报原因',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), approved(已批准), rejected(已拒绝)',
  `admin_note` text COMMENT '管理员备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `processed_at` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_reported_item_id` (`reported_item_id`),
  KEY `idx_reporter_user_id` (`reporter_user_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1; 