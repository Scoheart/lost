#!/bin/bash
#
# 数据库初始化脚本 (精简版)
# 适用于阿里云Linux系统
#

# 配置项
DB_PASSWORD=${1:-"your_password_here"}
SQL_FILE=${2:-"init-database.sql"}
DB_NAME="lost_and_found"

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    echo "[ERROR] 此脚本需要root权限运行"
    exit 1
fi

echo "[INFO] 开始数据库初始化..."
echo "[INFO] 使用SQL文件: $SQL_FILE"

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    echo "[ERROR] MySQL/MariaDB未安装，请先安装数据库"
    exit 1
fi

# 检查MySQL服务状态
if ! systemctl is-active --quiet mysql && ! systemctl is-active --quiet mariadb && ! systemctl is-active --quiet mysqld; then
    echo "[INFO] MySQL/MariaDB服务未运行，启动服务..."
    systemctl start mysql || systemctl start mariadb || systemctl start mysqld
fi

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "[ERROR] SQL文件不存在: $SQL_FILE"
    exit 1
fi

# 创建数据库并导入数据
echo "[INFO] 创建数据库并导入数据..."

# 创建数据库
mysql -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入SQL文件
mysql $DB_NAME < "$SQL_FILE"

# 验证数据库是否成功创建
if mysql -e "USE $DB_NAME; SHOW TABLES;" | grep -q "."; then
    echo "[SUCCESS] 数据库初始化成功"
else
    echo "[ERROR] 数据库初始化失败"
    exit 1
fi 