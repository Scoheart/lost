#!/bin/bash
#
# 阿里云ECS服务器初始化脚本
# 用于全新的服务器首次配置，安装所有运行项目所需的软件和环境
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
DEPLOY_PATH=${2:-"/opt/lostandfound/backend"}
FRONTEND_PATH=${3:-"/var/www/html/lostandfound"}
APP_DB_NAME="lost_and_found"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"

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
  warning "无法精确检测发行版，将使用通用方法安装软件"
fi

# 根据不同的发行版选择包管理器
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  PKG_MANAGER="apt-get"
  PKG_UPDATE="$PKG_MANAGER update"
  PKG_UPGRADE="$PKG_MANAGER upgrade -y"
  PKG_INSTALL="$PKG_MANAGER install -y"
  JAVA_PKG="openjdk-17-jdk"
  MYSQL_PKG="mysql-server"
  REDIS_PKG="redis-server"
  NGINX_PKG="nginx"
  FIREWALL_PKG="ufw"
  EXTRA_PKGS="curl unzip wget git"
elif [[ "$OS_TYPE" == "centos" ]] || [[ "$OS_TYPE" == "rhel" ]] || [[ "$OS_TYPE" == "aliyun" ]] || [[ "$OS_TYPE" == "alinux" ]] || [[ "$OS_TYPE" == "fedora" ]]; then
  PKG_MANAGER="yum"
  # 对于CentOS 8+/RHEL 8+/AliyunLinux 3+，使用dnf
  if [[ "$OS_VERSION" == 8* ]] || [[ "$OS_VERSION" == 9* ]] || [[ "$OS_VERSION" -ge 3 ]]; then
    PKG_MANAGER="dnf"
  fi
  PKG_UPDATE="$PKG_MANAGER check-update || true"  # check-update返回100表示有更新，会导致脚本退出
  PKG_UPGRADE="$PKG_MANAGER upgrade -y"
  PKG_INSTALL="$PKG_MANAGER install -y"
  JAVA_PKG="java-17-openjdk-devel"
  MYSQL_PKG="mysql-server"
  # RHEL/CentOS 7可能需要使用mariadb-server
  if [[ "$OS_VERSION" == "7" ]]; then
    MYSQL_PKG="mariadb-server"
  fi
  REDIS_PKG="redis"
  NGINX_PKG="nginx"
  FIREWALL_PKG="firewalld"
  EXTRA_PKGS="curl unzip wget git"
  
  # 如果是较新的RHEL/CentOS系列，可能需要启用EPEL源
  if command -v dnf &> /dev/null; then
    info "检查是否已安装EPEL源..."
    if ! dnf list installed epel-release &> /dev/null; then
      info "安装EPEL源..."
      dnf install -y epel-release
    fi
  elif command -v yum &> /dev/null; then
    info "检查是否已安装EPEL源..."
    if ! yum list installed epel-release &> /dev/null; then
      info "安装EPEL源..."
      yum install -y epel-release
    fi
  fi
else
  error "不支持的操作系统: $OS_NAME"
fi

info "开始服务器初始化..."

# 1. 更新系统包
info "正在更新系统包..."
$PKG_UPDATE || warning "系统包更新失败，但将继续执行"
$PKG_UPGRADE || warning "系统包升级失败，但将继续执行"

# 2. 安装Java 17
info "正在安装Java 17..."
$PKG_INSTALL $JAVA_PKG || error "Java 17安装失败"
java -version
success "Java 17安装成功"

# 3. 安装MySQL
info "正在安装MySQL服务器..."
$PKG_INSTALL $MYSQL_PKG || error "MySQL安装失败"

# 根据发行版不同，MySQL服务名可能不同
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  MYSQL_SERVICE="mysql"
elif [[ "$OS_VERSION" == "7" ]]; then
  MYSQL_SERVICE="mariadb"
else
  MYSQL_SERVICE="mysqld"
fi

systemctl enable $MYSQL_SERVICE || warning "无法将MySQL设置为开机启动"
systemctl start $MYSQL_SERVICE || error "MySQL启动失败"

# 配置MySQL
info "正在配置MySQL..."
if [[ "$OS_TYPE" == "centos" ]] || [[ "$OS_TYPE" == "rhel" ]] || [[ "$OS_TYPE" == "aliyun" ]] || [[ "$OS_TYPE" == "alinux" ]] || [[ "$OS_TYPE" == "fedora" ]]; then
  if [[ "$OS_VERSION" == "7" ]]; then
    # 对于CentOS/RHEL 7，使用mariadb的初始化方式
    info "使用MariaDB的初始化方式..."
    mysql -e "UPDATE mysql.user SET Password=PASSWORD('$MYSQL_ROOT_PASSWORD') WHERE User='root';" || warning "密码设置失败"
    mysql -e "FLUSH PRIVILEGES;" || warning "权限刷新失败"
  else
    info "使用MySQL的初始化方式..."
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD';" || warning "密码设置失败"
    mysql -e "FLUSH PRIVILEGES;" || warning "权限刷新失败"
  fi
else
  # Ubuntu/Debian的方式
  mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$MYSQL_ROOT_PASSWORD';" || warning "密码设置失败"
  mysql -e "FLUSH PRIVILEGES;" || warning "权限刷新失败"
fi
success "MySQL配置完成"

# 4. 安装Redis
info "正在安装Redis服务器..."
$PKG_INSTALL $REDIS_PKG || error "Redis安装失败"
systemctl enable redis || warning "无法将Redis设置为开机启动"

# Redis服务名在不同发行版中可能不同
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  REDIS_SERVICE="redis-server"
else
  REDIS_SERVICE="redis"
fi

systemctl start $REDIS_SERVICE || error "Redis启动失败"
redis-cli ping || warning "Redis ping测试失败"
success "Redis安装成功"

# 5. 安装Nginx
info "正在安装Nginx..."
$PKG_INSTALL $NGINX_PKG || error "Nginx安装失败"
systemctl enable nginx || warning "无法将Nginx设置为开机启动"
systemctl start nginx || error "Nginx启动失败"
success "Nginx安装成功"

# 6. 创建部署目录
info "正在创建部署目录..."
mkdir -p $DEPLOY_PATH/logs
mkdir -p $DEPLOY_PATH/uploads
mkdir -p $FRONTEND_PATH

# 7. 设置目录权限
info "正在设置目录权限..."
chmod -R 755 $DEPLOY_PATH
chmod -R 755 $FRONTEND_PATH
success "目录创建和权限设置完成"

# 8. 配置防火墙
info "正在配置防火墙..."
$PKG_INSTALL $FIREWALL_PKG || warning "防火墙安装失败"

# 根据发行版不同配置防火墙
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  # Ubuntu/Debian使用ufw
  ufw allow ssh || warning "SSH端口开放失败"
  ufw allow http || warning "HTTP端口开放失败"
  ufw allow https || warning "HTTPS端口开放失败"
  ufw allow 8080/tcp || warning "8080端口开放失败"
  # 不自动启用防火墙，以免SSH连接被切断
  info "防火墙规则已添加，但未自动启用。请使用 'sudo ufw enable' 手动启用"
else
  # CentOS/RHEL使用firewalld
  systemctl enable firewalld || warning "无法设置firewalld开机启动"
  systemctl start firewalld || warning "无法启动firewalld"
  firewall-cmd --permanent --add-service=ssh || warning "SSH端口开放失败"
  firewall-cmd --permanent --add-service=http || warning "HTTP端口开放失败"
  firewall-cmd --permanent --add-service=https || warning "HTTPS端口开放失败"
  firewall-cmd --permanent --add-port=8080/tcp || warning "8080端口开放失败"
  firewall-cmd --reload || warning "防火墙规则重载失败"
  info "防火墙已配置并启用"
fi

# 9. 安装其他必要工具
info "正在安装其他必要工具..."
$PKG_INSTALL $EXTRA_PKGS || warning "工具安装部分失败"

# 10. 初始化数据库
info "正在初始化数据库..."

# 检查SQL初始化脚本是否与此脚本在同一目录
if [ -f "${SCRIPT_DIR}/init-database.sql" ]; then
  info "找到数据库初始化SQL脚本: ${SCRIPT_DIR}/init-database.sql"
  
  # 检查初始化脚本是否存在，如果不存在则创建临时脚本
  if [ -f "${SCRIPT_DIR}/init-database.sh" ]; then
    info "找到数据库初始化执行脚本: ${SCRIPT_DIR}/init-database.sh"
    chmod +x "${SCRIPT_DIR}/init-database.sh"
    "${SCRIPT_DIR}/init-database.sh" "$MYSQL_ROOT_PASSWORD" "${SCRIPT_DIR}/init-database.sql" || warning "数据库初始化可能未完全成功，但将继续执行"
  else
    warning "未找到数据库初始化执行脚本，使用内联命令执行SQL"
    
    # 直接执行SQL脚本
    if mysql -u root -p"$MYSQL_ROOT_PASSWORD" < "${SCRIPT_DIR}/init-database.sql"; then
      success "数据库初始化成功！"
    else
      warning "数据库初始化失败，但将继续部署"
    fi
  fi
else
  warning "未找到数据库初始化SQL脚本，跳过数据库表初始化"
  
  # 仅创建数据库而不初始化表结构
  mysql -e "CREATE DATABASE IF NOT EXISTS $APP_DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || error "数据库创建失败"
  mysql -e "FLUSH PRIVILEGES;" || warning "MySQL权限刷新失败"
  info "已创建数据库 $APP_DB_NAME （无表结构）"
fi

# SELinux处理（针对CentOS/RHEL）
if [[ "$OS_TYPE" == "centos" ]] || [[ "$OS_TYPE" == "rhel" ]] || [[ "$OS_TYPE" == "aliyun" ]] || [[ "$OS_TYPE" == "alinux" ]] || [[ "$OS_TYPE" == "fedora" ]]; then
  if command -v sestatus &> /dev/null; then
    if sestatus | grep -q "SELinux status: enabled"; then
      info "检测到SELinux已启用，设置必要的上下文..."
      
      # 为Web内容设置SELinux上下文
      if command -v chcon &> /dev/null; then
        chcon -R -t httpd_sys_content_t $FRONTEND_PATH || warning "设置前端目录SELinux上下文失败"
      fi
      
      # 允许Nginx连接到后端
      if command -v setsebool &> /dev/null; then
        setsebool -P httpd_can_network_connect 1 || warning "设置SELinux网络连接策略失败"
      fi
      
      success "SELinux配置完成"
    fi
  fi
fi

# 11. 提示用户
cat <<EOF > /root/deployment-info.txt
==================================================
服务器初始化完成！

系统信息:
- 操作系统: $OS_NAME
- 包管理器: $PKG_MANAGER

部署信息:
- MySQL 根密码: $MYSQL_ROOT_PASSWORD
- 数据库名称: $APP_DB_NAME
- 后端部署路径: $DEPLOY_PATH
- 前端部署路径: $FRONTEND_PATH

请记住上述信息，并在GitHub Actions中设置相应Secrets:
- DB_PASSWORD: MySQL根密码
- DEPLOY_PATH: 后端部署路径
- FRONTEND_DEPLOY_PATH: 前端部署路径
==================================================
EOF

cat /root/deployment-info.txt
success "服务器初始化完成！🎉"
info "您可以在 /root/deployment-info.txt 查看部署信息" 