-- Create Database
CREATE DATABASE IF NOT EXISTS lost_and_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lost_and_found;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('resident', 'admin', 'sysadmin') NOT NULL DEFAULT 'resident',
    avatar VARCHAR(255),
    phone VARCHAR(20),
    real_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Lost Items table
CREATE TABLE IF NOT EXISTS lost_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    lost_date DATE NOT NULL,
    lost_location VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    images TEXT, -- JSON array of image URLs
    reward DECIMAL(10, 2),
    contact_info VARCHAR(255) NOT NULL,
    status ENUM('pending', 'found', 'closed') NOT NULL DEFAULT 'pending',
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_user_id (user_id),
    INDEX idx_lost_date (lost_date),
    FULLTEXT INDEX ft_idx_title_desc_location (title, description, lost_location)
);

-- Found Items table
CREATE TABLE IF NOT EXISTS found_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    found_date DATE NOT NULL,
    found_location VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    images TEXT, -- JSON array of image URLs
    contact_info VARCHAR(255) NOT NULL,
    storage_location VARCHAR(255),
    status ENUM('pending', 'claimed', 'closed') NOT NULL DEFAULT 'pending',
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_user_id (user_id),
    INDEX idx_found_date (found_date),
    FULLTEXT INDEX ft_idx_title_desc_location (title, description, found_location)
);

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_type ENUM('lost', 'found', 'forum') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_item_id_type (item_id, item_type)
);

-- Claims table
CREATE TABLE IF NOT EXISTS claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id BIGINT,
    found_item_id BIGINT,
    claimant_id BIGINT NOT NULL,
    item_owner_user_id BIGINT,
    description TEXT,
    contact_info VARCHAR(255),
    status ENUM('pending', 'approved', 'rejected', 'completed') NOT NULL DEFAULT 'pending',
    admin_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    INDEX idx_status (status),
    INDEX idx_lost_item_id (lost_item_id),
    INDEX idx_found_item_id (found_item_id),
    INDEX idx_claimant_id (claimant_id),
    INDEX idx_item_owner_user_id (item_owner_user_id)
);

-- Announcements table
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    is_important BOOLEAN NOT NULL DEFAULT FALSE,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'published',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_important (is_important),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status)
);

-- Forum Posts table
CREATE TABLE IF NOT EXISTS forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    images TEXT, -- JSON array of image URLs
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FULLTEXT INDEX ft_idx_title_content (title, content)
);

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type ENUM('user', 'lost_item', 'found_item', 'forum_post', 'comment') NOT NULL,
    reported_item_id BIGINT NOT NULL,
    reported_user_id BIGINT,
    reporter_user_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    admin_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    INDEX idx_report_type (report_type),
    INDEX idx_status (status),
    INDEX idx_reported_item_id (reported_item_id),
    INDEX idx_reporter_user_id (reporter_user_id),
    INDEX idx_reported_user_id (reported_user_id)
);

-- Categories for Lost Items
CREATE TABLE IF NOT EXISTS lost_items_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories for Found Items
CREATE TABLE IF NOT EXISTS found_items_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Initial Admin User (password is 'admin123')
INSERT INTO users (username, email, password, role, created_at, updated_at, is_enabled, is_locked)
VALUES ('admin', 'admin@example.com', '$2a$10$qA3GhJQVsMdUwAD0S5NsS.1wUDIWxvd1m7TfFL/D9WOzRvUrg5iS.', 'sysadmin', NOW(), NOW(), true, false);

-- Initial Categories
INSERT INTO lost_items_categories (name) VALUES
('电子设备'), ('钱包/钱'), ('钥匙'), ('衣物'), ('首饰'), ('证件'), ('书籍/文件'), ('其他');

INSERT INTO found_items_categories (name) VALUES
('电子设备'), ('钱包/钱'), ('钥匙'), ('衣物'), ('首饰'), ('证件'), ('书籍/文件'), ('其他'); 