#!/bin/bash
#
# 阿里云ECS服务器初始化脚本 (简化版)
# 用于在阿里云服务器上配置Java、MySQL、Nginx环境
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
DEPLOY_PATH=${2:-"/opt/lostandfound/backend"}
FRONTEND_PATH=${3:-"/var/www/html/lostandfound"}
APP_DB_NAME="lost_and_found"

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
}

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    log_error "此脚本需要root权限运行"
    exit 1
fi

# 创建目录
log_info "创建必要目录..."
mkdir -p "$DEPLOY_PATH"
mkdir -p "$FRONTEND_PATH"
mkdir -p "$DEPLOY_PATH/logs"
mkdir -p "$DEPLOY_PATH/uploads"

# 直接使用阿里云镜像安装软件包
log_info "更新软件包列表..."
if command -v apt &> /dev/null; then
    # Debian/Ubuntu系统
    apt update -y
elif command -v yum &> /dev/null; then
    # CentOS/RHEL/Alibaba Cloud Linux系统
    yum makecache -y
fi

log_info "安装必要软件包..."
if command -v apt &> /dev/null; then
    # Debian/Ubuntu系统
    apt install -y openjdk-17-jdk nginx mysql-server redis-server
elif command -v yum &> /dev/null; then
    # CentOS/RHEL/Alibaba Cloud Linux系统
    
    # 安装Java 17 (使用Amazon Corretto)
    log_info "安装Java 17..."
    if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "version \"17"; then
        yum install -y java-17-amazon-corretto-devel
    else
        log_info "Java 17已安装，跳过"
    fi
    
    # 安装MySQL/MariaDB
    log_info "安装MySQL/MariaDB..."
    if ! command -v mysql &> /dev/null; then
        yum install -y mysql-server || yum install -y mariadb-server
    else
        log_info "MySQL/MariaDB已安装，跳过"
    fi
    
    # 安装Nginx
    log_info "安装Nginx..."
    if ! command -v nginx &> /dev/null; then
        yum install -y nginx
    else
        log_info "Nginx已安装，跳过"
    fi
    
    # 安装Redis
    log_info "安装Redis..."
    if ! command -v redis-server &> /dev/null && ! command -v redis-cli &> /dev/null; then
        yum install -y redis
    else
        log_info "Redis已安装，跳过"
    fi
fi

# 启动数据库服务
log_info "启动MySQL/MariaDB服务..."
if command -v systemctl &> /dev/null; then
    # 根据系统类型启动对应的服务
    if systemctl list-unit-files | grep -q mariadb; then
        systemctl enable --now mariadb
    elif systemctl list-unit-files | grep -q mysqld; then
        systemctl enable --now mysqld
    else
        systemctl enable --now mysql
    fi
else
    log_warning "无法使用systemctl启动MySQL服务，请手动启动"
fi

# 启动Redis服务
log_info "启动Redis服务..."
if command -v systemctl &> /dev/null; then
    if systemctl list-unit-files | grep -q redis; then
        systemctl enable --now redis
    else
        systemctl enable --now redis-server
    fi
else
    log_warning "无法使用systemctl启动Redis服务，请手动启动"
fi

# 启动Nginx服务
log_info "启动Nginx服务..."
if command -v systemctl &> /dev/null; then
    systemctl enable --now nginx
else
    log_warning "无法使用systemctl启动Nginx服务，请手动启动"
fi

# 初始化数据库
log_info "检查数据库初始化脚本..."
INIT_DB_SQL_FILE="/tmp/init-database.sql"
INIT_DB_SCRIPT="/tmp/init-database.sh"

if [ -f "$INIT_DB_SQL_FILE" ] && [ -f "$INIT_DB_SCRIPT" ]; then
    log_info "执行数据库初始化脚本..."
    chmod +x "$INIT_DB_SCRIPT"
    "$INIT_DB_SCRIPT" "$MYSQL_ROOT_PASSWORD" "$INIT_DB_SQL_FILE"
else
    log_warning "数据库初始化脚本不存在，跳过初始化"
fi

# 设置防火墙（如果启用）
log_info "配置防火墙..."
if command -v firewall-cmd &> /dev/null; then
    # CentOS/RHEL使用firewalld
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    log_success "防火墙配置完成"
elif command -v ufw &> /dev/null; then
    # Ubuntu使用ufw
    ufw allow http
    ufw allow https
    ufw allow 8080/tcp
    log_success "防火墙配置完成"
else
    log_warning "未检测到防火墙服务"
fi

log_success "服务器初始化完成!"
log_info "部署路径: $DEPLOY_PATH"
log_info "前端路径: $FRONTEND_PATH" 