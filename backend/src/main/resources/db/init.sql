-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS lost_and_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE lost_and_found;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT '用户角色: resident(居民), admin(社区管理员), sysadmin(系统管理员)',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话号码',
    real_name VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用',
    is_locked BOOLEAN NOT NULL DEFAULT FALSE COMMENT '账户是否锁定',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    admin_id BIGINT NOT NULL COMMENT '发布公告的管理员ID',
    is_sticky BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否置顶',
    status VARCHAR(20) NOT NULL DEFAULT 'published' COMMENT '状态: published(已发布), draft(草稿)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_sticky_created (is_sticky, created_at)
);

-- 失物表
CREATE TABLE IF NOT EXISTS lost_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    lost_date DATE COMMENT '丢失日期',
    lost_location VARCHAR(255) COMMENT '丢失地点',
    category VARCHAR(50) COMMENT '分类',
    images TEXT COMMENT '图片URL，多个用逗号分隔',
    reward DECIMAL(10,2) COMMENT '悬赏金额',
    contact_info VARCHAR(100) COMMENT '联系方式',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), found(已找到), closed(已关闭)',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_lost_date (lost_date)
);

-- 招领表
CREATE TABLE IF NOT EXISTS found_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    found_date DATE COMMENT '拾取日期',
    found_location VARCHAR(255) COMMENT '拾取地点',
    category VARCHAR(50) COMMENT '分类',
    images TEXT COMMENT '图片URL，多个用逗号分隔',
    contact_info VARCHAR(100) COMMENT '联系方式',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), claimed(已认领), closed(已关闭)',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_found_date (found_date)
);

-- 认领申请表
CREATE TABLE IF NOT EXISTS claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_type VARCHAR(10) NOT NULL COMMENT '物品类型: lost(寻物), found(招领)',
    claimant_user_id BIGINT NOT NULL COMMENT '认领者ID',
    item_owner_user_id BIGINT NOT NULL COMMENT '物品发布者ID',
    description TEXT COMMENT '认领描述/证明',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), approved(已批准), rejected(已拒绝), completed(已完成)',
    admin_note TEXT COMMENT '管理员备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL COMMENT '处理时间',
    INDEX idx_item_id_type (item_id, item_type),
    INDEX idx_claimant_user_id (claimant_user_id),
    INDEX idx_item_owner_user_id (item_owner_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '评论内容',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_type VARCHAR(10) NOT NULL COMMENT '物品类型: lost(寻物), found(招领)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_item_id_type (item_id, item_type),
    INDEX idx_created_at (created_at)
);

-- 论坛帖子表
CREATE TABLE IF NOT EXISTS forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images TEXT COMMENT '图片URL，JSON格式',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- 举报表
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(20) NOT NULL COMMENT '举报类型: user, lost_item, found_item, forum_post, comment',
    reported_item_id BIGINT DEFAULT NULL COMMENT '被举报物品ID',
    reported_user_id BIGINT DEFAULT NULL COMMENT '被举报用户ID',
    reporter_user_id BIGINT NOT NULL COMMENT '举报者ID',
    reason VARCHAR(255) NOT NULL COMMENT '举报原因',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), approved(已批准), rejected(已拒绝)',
    admin_note TEXT COMMENT '管理员备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL COMMENT '处理时间',
    INDEX idx_report_type (report_type),
    INDEX idx_reported_item_id (reported_item_id),
    INDEX idx_reported_user_id (reported_user_id),
    INDEX idx_reporter_user_id (reporter_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 失物分类表（可选，因为实体类中直接使用了category字段）
CREATE TABLE IF NOT EXISTS lost_items_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 招领分类表（可选，因为实体类中直接使用了category字段）
CREATE TABLE IF NOT EXISTS found_items_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初始化分类
INSERT INTO lost_items_categories (name) VALUES 
('电子设备'), ('钱包/钱'), ('钥匙'), ('衣物'), ('首饰'), ('证件'), ('书籍/文件'), ('其他');

INSERT INTO found_items_categories (name) VALUES 
('电子设备'), ('钱包/钱'), ('钥匙'), ('衣物'), ('首饰'), ('证件'), ('书籍/文件'), ('其他');