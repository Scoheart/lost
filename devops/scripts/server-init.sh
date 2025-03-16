#!/bin/bash
#
# 阿里云ECS服务器初始化脚本 (精简版)
# 适用于阿里云Linux系统
#

# 配置项
MYSQL_ROOT_PASSWORD=${1:-"your_password_here"}
DEPLOY_PATH=${2:-"/opt/lostandfound/backend"}
FRONTEND_PATH=${3:-"/var/www/html/lostandfound"}

# 日志输出
echo "[INFO] 开始服务器初始化..."

# 检查root权限
if [ "$(id -u)" -ne 0 ]; then
    echo "[ERROR] 此脚本需要root权限运行"
    exit 1
fi

# 创建必要目录
echo "[INFO] 创建必要目录..."
mkdir -p "$DEPLOY_PATH"
mkdir -p "$FRONTEND_PATH"
mkdir -p "$DEPLOY_PATH/logs"
mkdir -p "$DEPLOY_PATH/uploads"

# 更新软件包列表
echo "[INFO] 更新软件包列表..."
yum makecache -y

# 安装必要软件包
echo "[INFO] 安装必要软件包..."

# 安装Java 17 (OpenJDK)
echo "[INFO] 安装Java 17..."
yum install -y java-17-openjdk-devel

# 安装MySQL/MariaDB
echo "[INFO] 安装MySQL/MariaDB..."
yum install -y mysql-server || yum install -y mariadb-server

# 安装Nginx
echo "[INFO] 安装Nginx..."
yum install -y nginx

# 安装Redis
echo "[INFO] 安装Redis..."
yum install -y redis

# 启动服务
echo "[INFO] 启动必要服务..."

# 启动MySQL/MariaDB
if systemctl list-unit-files | grep -q mariadb; then
    systemctl enable --now mariadb
else
    systemctl enable --now mysqld
fi

# 启动Redis
systemctl enable --now redis

# 启动Nginx
systemctl enable --now nginx

# 初始化数据库
echo "[INFO] 检查数据库初始化脚本..."
INIT_DB_SQL_FILE="/tmp/init-database.sql"
INIT_DB_SCRIPT="/tmp/init-database.sh"

if [ -f "$INIT_DB_SQL_FILE" ] && [ -f "$INIT_DB_SCRIPT" ]; then
    echo "[INFO] 执行数据库初始化脚本..."
    chmod +x "$INIT_DB_SCRIPT"
    "$INIT_DB_SCRIPT" "$MYSQL_ROOT_PASSWORD" "$INIT_DB_SQL_FILE"
else
    echo "[WARNING] 数据库初始化脚本不存在，跳过初始化"
fi

# 配置防火墙
echo "[INFO] 配置防火墙..."
if command -v firewall-cmd &>/dev/null; then
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    echo "[INFO] 防火墙配置完成"
fi

echo "[SUCCESS] 服务器初始化完成!"
echo "[INFO] 部署路径: $DEPLOY_PATH"
echo "[INFO] 前端路径: $FRONTEND_PATH"
