#!/bin/bash
#
# Nginx配置脚本 (精简版)
# 适用于阿里云Linux系统
#

# 获取参数
FRONTEND_PATH=${1:-"/var/www/html/lostandfound"}
SERVER_NAME=${2:-$(hostname -f)}

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    echo "[ERROR] 此脚本需要root权限运行"
    exit 1
fi

echo "[INFO] 开始配置Nginx..."
echo "[INFO] 前端路径: $FRONTEND_PATH"
echo "[INFO] 服务器名称: $SERVER_NAME"

# 确保前端目录存在
if [ ! -d "$FRONTEND_PATH" ]; then
    echo "[INFO] 创建前端目录: $FRONTEND_PATH"
    mkdir -p "$FRONTEND_PATH"
fi

# 创建Nginx配置
NGINX_CONFIG="/etc/nginx/conf.d/lostandfound.conf"

echo "[INFO] 创建Nginx配置文件: $NGINX_CONFIG"
cat > "$NGINX_CONFIG" << EOL
server {
    listen 80;
    server_name $SERVER_NAME;
    root $FRONTEND_PATH;
    index index.html;

    # 前端文件缓存设置
    location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }

    # 前端路由处理 - 所有请求首先尝试文件系统，然后回退到index.html
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # API代理 - 转发到后端服务
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
EOL

# 测试Nginx配置
echo "[INFO] 测试Nginx配置..."
nginx -t

# 如果配置测试成功，则重新加载Nginx
if [ $? -eq 0 ]; then
    echo "[INFO] 重新加载Nginx配置..."
    systemctl reload nginx
    echo "[SUCCESS] Nginx配置完成"
    echo "[INFO] 应用可通过以下地址访问: http://$SERVER_NAME/"
else
    echo "[ERROR] Nginx配置测试失败，请检查配置文件"
    exit 1
fi 