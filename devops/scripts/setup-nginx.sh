#!/bin/bash
#
# Nginx配置脚本
# 为站点配置Nginx，支持HTTP/HTTPS访问
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
FRONTEND_PATH=${1:-"/var/www/html/lostandfound"}
SERVER_NAME=${2:-$(hostname -I | awk '{print $1}')}
NGINX_CONF_PATH=""
NGINX_AVAILABLE_PATH=""
NGINX_ENABLED_PATH=""

# 脚本目录
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
  warning "无法精确检测发行版，将使用通用方法配置Nginx"
fi

# 根据发行版设置Nginx配置路径
if [[ "$OS_TYPE" == "ubuntu" ]] || [[ "$OS_TYPE" == "debian" ]]; then
  NGINX_CONF_PATH="/etc/nginx/sites-available/lostandfound"
  NGINX_AVAILABLE_PATH="/etc/nginx/sites-available"
  NGINX_ENABLED_PATH="/etc/nginx/sites-enabled"
elif [[ "$OS_TYPE" == "centos" ]] || [[ "$OS_TYPE" == "rhel" ]] || [[ "$OS_TYPE" == "aliyun" ]] || [[ "$OS_TYPE" == "alinux" ]] || [[ "$OS_TYPE" == "fedora" ]]; then
  NGINX_CONF_PATH="/etc/nginx/conf.d/lostandfound.conf"
  NGINX_AVAILABLE_PATH="/etc/nginx/conf.d"
  # CentOS不使用sites-enabled模式
  NGINX_ENABLED_PATH=""
else
  # 默认使用Ubuntu的路径
  NGINX_CONF_PATH="/etc/nginx/sites-available/lostandfound"
  NGINX_AVAILABLE_PATH="/etc/nginx/sites-available"
  NGINX_ENABLED_PATH="/etc/nginx/sites-enabled"
  warning "未知系统类型，使用默认Nginx配置路径"
fi

info "开始配置Nginx..."
info "前端目录: $FRONTEND_PATH"
info "服务器名称: $SERVER_NAME"
info "Nginx配置路径: $NGINX_CONF_PATH"

# 检查Nginx是否已安装
if ! command -v nginx &> /dev/null; then
  error "Nginx未安装。请先安装Nginx。"
fi

# 检查前端目录是否存在
if [ ! -d "$FRONTEND_PATH" ]; then
  warning "前端目录不存在: $FRONTEND_PATH"
  info "创建前端目录..."
  mkdir -p "$FRONTEND_PATH"
fi

# 创建Nginx配置文件模板
cat > /tmp/nginx.conf.template <<EOF
server {
    listen 80;
    server_name SERVER_NAME;
    
    # 前端 - 静态文件
    location / {
        root FRONTEND_PATH;
        index index.html;
        try_files \$uri \$uri/ /index.html;
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
        proxy_send_timeout 60s;
    }
    
    # 安全设置
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options SAMEORIGIN;
    add_header X-XSS-Protection "1; mode=block";
}
EOF

# 替换模板中的变量
sed -e "s|SERVER_NAME|$SERVER_NAME|g" -e "s|FRONTEND_PATH|$FRONTEND_PATH|g" /tmp/nginx.conf.template > /tmp/lostandfound.conf

# 创建Nginx配置
info "创建Nginx配置文件: $NGINX_CONF_PATH"
mkdir -p "$(dirname "$NGINX_CONF_PATH")"
cp /tmp/lostandfound.conf "$NGINX_CONF_PATH"

# 在Ubuntu/Debian上创建符号链接
if [[ -n "$NGINX_ENABLED_PATH" ]]; then
  if [[ ! -d "$NGINX_ENABLED_PATH" ]]; then
    info "创建sites-enabled目录: $NGINX_ENABLED_PATH"
    mkdir -p "$NGINX_ENABLED_PATH"
  fi
  
  info "创建符号链接到sites-enabled目录"
  ln -sf "$NGINX_CONF_PATH" "$NGINX_ENABLED_PATH/lostandfound"
  
  # 移除默认配置（如果存在）
  if [ -f "$NGINX_ENABLED_PATH/default" ]; then
    info "移除默认Nginx配置"
    rm -f "$NGINX_ENABLED_PATH/default"
  fi
fi

# 设置SELinux上下文（对于CentOS/RHEL系统）
if [[ "$OS_TYPE" == "centos" ]] || [[ "$OS_TYPE" == "rhel" ]] || [[ "$OS_TYPE" == "aliyun" ]] || [[ "$OS_TYPE" == "alinux" ]] || [[ "$OS_TYPE" == "fedora" ]]; then
  if command -v sestatus &> /dev/null; then
    if sestatus | grep -q "SELinux status: enabled"; then
      info "检测到SELinux已启用，设置必要的上下文..."
      
      # 为前端目录设置适当的SELinux上下文
      if command -v chcon &> /dev/null; then
        info "为Web内容设置SELinux上下文..."
        chcon -R -t httpd_sys_content_t "$FRONTEND_PATH" || warning "设置前端目录SELinux上下文失败"
      fi
      
      # 允许Nginx连接到后端
      if command -v setsebool &> /dev/null; then
        info "允许Nginx连接后端..."
        setsebool -P httpd_can_network_connect 1 || warning "设置SELinux网络连接策略失败"
      fi
    fi
  fi
fi

# 测试Nginx配置
info "测试Nginx配置..."
if nginx -t; then
  success "Nginx配置测试通过"
else
  error "Nginx配置测试失败。请检查配置文件。"
fi

# 重新加载Nginx
info "重新加载Nginx配置..."
if systemctl reload nginx; then
  success "Nginx配置已重新加载"
else
  warning "Nginx配置重载失败，尝试重启服务..."
  
  # 尝试重启服务
  if systemctl restart nginx; then
    success "Nginx服务已重启"
  else
    error "Nginx服务重启失败。请手动检查日志和配置。"
  fi
fi

info "配置完成。现在可以通过以下方式访问应用:"
success "http://$SERVER_NAME/" 