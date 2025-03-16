#!/bin/bash
#
# 数据库初始化脚本
# 用于初始化MySQL数据库，创建表结构和默认数据
# 兼容多种Linux发行版（Ubuntu、Debian、CentOS、RHEL、Alibaba Cloud Linux）
#

set -e  # 遇到错误立即退出

# 显示彩色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置项
MYSQL_ROOT_PASSWORD=${1:-"your_password_here"}
SQL_SCRIPT_PATH=${2:-"$(dirname "$0")/init-database.sql"}

# 打印带颜色的状态信息
info() {
  echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
  echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
  echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
  echo -e "${RED}[ERROR]${NC} $1"
  exit 1
}

# 检查是否为root用户
if [ "$(id -u)" -ne 0 ]; then
  error "此脚本需要root权限运行。请使用sudo运行或切换到root用户。"
fi

# 检测Linux发行版类型
if [ -f /etc/os-release ]; then
  . /etc/os-release
  OS_TYPE=$ID
  OS_VERSION=$VERSION_ID
  OS_NAME=$NAME
  info "检测到操作系统: $OS_NAME $OS_VERSION"
else
  OS_TYPE=$(uname -s)
  OS_VERSION=$(uname -r)
  OS_NAME="$OS_TYPE $OS_VERSION"
  warning "无法精确检测发行版，将使用通用方法执行SQL脚本"
fi

# 检查MySQL是否已安装
if ! command -v mysql &> /dev/null; then
  error "MySQL未安装。请先安装MySQL服务器。"
fi

# 检查MySQL服务状态
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  MYSQL_SERVICE="mysql"
elif [[ "$OS_VERSION" == "7" && ("$OS_TYPE" == "centos" || "$OS_TYPE" == "rhel") ]]; then
  MYSQL_SERVICE="mariadb"
else
  MYSQL_SERVICE="mysqld"
fi

# 检查MySQL/MariaDB服务是否运行
if ! systemctl is-active --quiet $MYSQL_SERVICE; then
  warning "MySQL服务未运行，尝试启动..."
  systemctl start $MYSQL_SERVICE || error "无法启动MySQL服务"
fi

# 检查SQL脚本文件是否存在
if [ ! -f "$SQL_SCRIPT_PATH" ]; then
  error "SQL脚本文件不存在: $SQL_SCRIPT_PATH"
fi

info "开始初始化数据库..."
info "使用SQL脚本: $SQL_SCRIPT_PATH"

# 执行SQL脚本，根据不同发行版调整MySQL命令
info "正在执行SQL脚本..."

# 使用不同的认证方式尝试连接MySQL并执行脚本
if [[ "$OS_TYPE" == "centos" || "$OS_TYPE" == "rhel" || "$OS_TYPE" == "aliyun" || "$OS_TYPE" == "alinux" ]] && [[ "$OS_VERSION" == "7" ]]; then
  # CentOS/RHEL 7 + MariaDB
  if mysql -u root -p"$MYSQL_ROOT_PASSWORD" < "$SQL_SCRIPT_PATH"; then
    success "数据库初始化成功！"
  else
    warning "使用密码认证失败，尝试无密码方式..."
    if mysql -u root < "$SQL_SCRIPT_PATH"; then
      success "数据库初始化成功！（使用无密码方式）"
    else
      error "数据库初始化失败。请检查SQL脚本和MySQL连接。"
    fi
  fi
else
  # 其他发行版
  if mysql -u root -p"$MYSQL_ROOT_PASSWORD" < "$SQL_SCRIPT_PATH"; then
    success "数据库初始化成功！"
  else
    error "数据库初始化失败。请检查SQL脚本和MySQL连接。"
  fi
fi

# 验证数据库是否创建成功（兼容不同认证方式）
DB_VALIDATION_CMD="USE lost_and_found; SHOW TABLES;"
if mysql -u root -p"$MYSQL_ROOT_PASSWORD" -e "$DB_VALIDATION_CMD" 2>/dev/null | grep -q "users"; then
  success "验证成功：数据库表已创建！"
  
  # 显示统计信息
  TABLE_COUNT=$(mysql -u root -p"$MYSQL_ROOT_PASSWORD" -N -e "USE lost_and_found; SHOW TABLES;" 2>/dev/null | wc -l)
  info "已创建 $TABLE_COUNT 个数据库表。"
elif [[ "$OS_TYPE" == "centos" || "$OS_TYPE" == "rhel" || "$OS_TYPE" == "aliyun" || "$OS_TYPE" == "alinux" ]] && [[ "$OS_VERSION" == "7" ]]; then
  # 尝试无密码方式验证（适用于一些MariaDB版本）
  if mysql -u root -e "$DB_VALIDATION_CMD" 2>/dev/null | grep -q "users"; then
    success "验证成功：数据库表已创建！（使用无密码方式验证）"
    
    # 显示统计信息
    TABLE_COUNT=$(mysql -u root -N -e "USE lost_and_found; SHOW TABLES;" 2>/dev/null | wc -l)
    info "已创建 $TABLE_COUNT 个数据库表。"
  else
    warning "验证失败：数据库表可能未正确创建。请手动检查。"
  fi
else
  warning "验证失败：数据库表可能未正确创建。请手动检查。"
fi 