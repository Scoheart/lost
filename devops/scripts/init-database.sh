#!/bin/bash
#
# 数据库初始化脚本 (简化版)
# 用于在阿里云服务器上初始化MySQL/MariaDB数据库
#

set -e

# 显示彩色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置项
MYSQL_ROOT_PASSWORD=${1:-"your_password_here"}
SQL_SCRIPT_PATH=${2:-"$(dirname "$0")/init-database.sql"}

# 日志输出函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    log_error "此脚本需要root权限运行"
    exit 1
fi

# 检查MySQL是否已安装
if ! command -v mysql &> /dev/null; then
    log_error "MySQL未安装，请先运行服务器初始化脚本"
fi

# 检查SQL脚本文件是否存在
if [ ! -f "$SQL_SCRIPT_PATH" ]; then
    log_error "SQL脚本文件不存在: $SQL_SCRIPT_PATH"
fi

log_info "开始初始化数据库..."
log_info "使用SQL脚本: $SQL_SCRIPT_PATH"

# 确保MySQL/MariaDB服务正在运行
if command -v systemctl &> /dev/null; then
    if systemctl list-unit-files | grep -q mariadb; then
        systemctl is-active --quiet mariadb || systemctl start mariadb
    elif systemctl list-unit-files | grep -q mysqld; then
        systemctl is-active --quiet mysqld || systemctl start mysqld
    else
        systemctl is-active --quiet mysql || systemctl start mysql
    fi
fi

# 执行SQL脚本
log_info "正在执行SQL脚本..."

# 尝试使用密码方式执行SQL脚本
if mysql -u root -p"$MYSQL_ROOT_PASSWORD" < "$SQL_SCRIPT_PATH" 2>/dev/null; then
    log_success "数据库初始化成功！"
else
    # 如果密码方式失败，尝试无密码方式（适用于初始安装）
    log_warning "使用密码方式执行失败，尝试无密码方式..."
    if mysql -u root < "$SQL_SCRIPT_PATH" 2>/dev/null; then
        log_success "数据库初始化成功！（使用无密码方式）"
        
        # 设置root密码（适用于初始安装后的密码设置）
        log_info "正在设置MySQL root密码..."
        if mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD';" 2>/dev/null || \
           mysql -u root -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$MYSQL_ROOT_PASSWORD');" 2>/dev/null; then
            log_success "MySQL root密码设置成功"
        else
            log_warning "MySQL root密码设置失败，请手动设置密码"
        fi
    else
        log_error "数据库初始化失败，请检查MySQL连接和SQL脚本"
    fi
fi

# 验证数据库是否创建成功
DB_VALIDATION_CMD="USE lost_and_found; SHOW TABLES;"
if mysql -u root -p"$MYSQL_ROOT_PASSWORD" -e "$DB_VALIDATION_CMD" 2>/dev/null | grep -q "users"; then
    log_success "验证成功：数据库表已创建！"
    
    # 显示统计信息
    TABLE_COUNT=$(mysql -u root -p"$MYSQL_ROOT_PASSWORD" -N -e "USE lost_and_found; SHOW TABLES;" 2>/dev/null | wc -l)
    log_info "已创建 $TABLE_COUNT 个数据库表"
else
    log_warning "验证失败：数据库表可能未正确创建，请手动检查"
fi

log_success "数据库初始化过程完成" 