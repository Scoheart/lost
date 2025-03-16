#!/bin/bash
#
# Nginx配置脚本 (精简版)
# 适用于阿里云Linux系统
#

# 获取参数
FRONTEND_PATH=${1:-"/var/www/html/lostandfound"}
SERVER_NAME=${2:-$(hostname -f)}
NGINX_LOG="/tmp/setup-nginx.log"

# 启用颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    echo -e "${RED}❌ 错误: 此脚本需要root权限运行${NC}" | tee -a $NGINX_LOG
    exit 1
fi

echo -e "${BLUE}🚀 开始配置Nginx...${NC}" | tee -a $NGINX_LOG
echo -e "📅 $(date)" | tee -a $NGINX_LOG
echo -e "📂 前端路径: ${FRONTEND_PATH}" | tee -a $NGINX_LOG
echo -e "🌐 服务器名称: ${SERVER_NAME}" | tee -a $NGINX_LOG

# 检查Nginx是否安装
if ! command -v nginx &>/dev/null; then
    echo -e "${RED}❌ 错误: Nginx未安装${NC}" | tee -a $NGINX_LOG
    
    # 尝试自动安装Nginx
    echo -e "${YELLOW}⚠️ 尝试安装Nginx...${NC}" | tee -a $NGINX_LOG
    if command -v yum &>/dev/null; then
        yum install -y nginx &>> $NGINX_LOG
    elif command -v apt-get &>/dev/null; then
        apt-get update &>> $NGINX_LOG
        apt-get install -y nginx &>> $NGINX_LOG
    else
        echo -e "${RED}❌ 无法自动安装Nginx，请手动安装后重试${NC}" | tee -a $NGINX_LOG
        exit 1
    fi
    
    if ! command -v nginx &>/dev/null; then
        echo -e "${RED}❌ Nginx安装失败${NC}" | tee -a $NGINX_LOG
        exit 1
    else
        echo -e "${GREEN}✅ Nginx安装成功${NC}" | tee -a $NGINX_LOG
    fi
else
    echo -e "${GREEN}✓ Nginx已安装${NC}" | tee -a $NGINX_LOG
    nginx -v 2>&1 | tee -a $NGINX_LOG
fi

# 确保前端目录存在
if [ ! -d "$FRONTEND_PATH" ]; then
    echo -e "${YELLOW}⚠️ 前端目录不存在，创建: $FRONTEND_PATH${NC}" | tee -a $NGINX_LOG
    mkdir -p "$FRONTEND_PATH"
    echo -e "${GREEN}✅ 前端目录已创建${NC}" | tee -a $NGINX_LOG
else
    echo -e "${GREEN}✓ 前端目录已存在${NC}" | tee -a $NGINX_LOG
fi

# 检查是否已有配置文件
NGINX_CONFIG="/etc/nginx/conf.d/lostandfound.conf"
CONFIG_EXISTS=false
if [ -f "$NGINX_CONFIG" ]; then
    echo -e "${YELLOW}🔍 检测到已存在的Nginx配置...${NC}" | tee -a $NGINX_LOG
    grep -q "$FRONTEND_PATH" "$NGINX_CONFIG" && grep -q "$SERVER_NAME" "$NGINX_CONFIG"
    if [ $? -eq 0 ]; then
        echo -e "${BLUE}ℹ️ 已存在相同的Nginx配置${NC}" | tee -a $NGINX_LOG
        read -p "$(echo -e ${YELLOW}❓ 是否覆盖现有配置? [y/N]:${NC} )" ANSWER
        if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}ℹ️ 保留现有配置并退出${NC}" | tee -a $NGINX_LOG
            echo -e "${GREEN}✅ Nginx已配置，无需更改${NC}" | tee -a $NGINX_LOG
            exit 0
        else
            echo -e "${YELLOW}⚠️ 将覆盖现有配置${NC}" | tee -a $NGINX_LOG
            CONFIG_EXISTS=true
        fi
    else
        echo -e "${YELLOW}⚠️ 现有配置与目标参数不匹配，将更新${NC}" | tee -a $NGINX_LOG
        CONFIG_EXISTS=true
    fi
fi

# 创建备份（如果现有配置存在）
if [ "$CONFIG_EXISTS" = true ]; then
    BACKUP_FILE="${NGINX_CONFIG}.$(date +%Y%m%d%H%M%S).bak"
    echo -e "${BLUE}💾 备份现有配置到: $BACKUP_FILE${NC}" | tee -a $NGINX_LOG
    cp "$NGINX_CONFIG" "$BACKUP_FILE"
fi

# 创建Nginx配置
echo -e "${BLUE}📝 创建Nginx配置文件: $NGINX_CONFIG${NC}" | tee -a $NGINX_LOG
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
    
    # 访问日志和错误日志配置
    access_log /var/log/nginx/${SERVER_NAME}-access.log;
    error_log /var/log/nginx/${SERVER_NAME}-error.log;
}
EOL

echo -e "${GREEN}✅ Nginx配置文件已创建${NC}" | tee -a $NGINX_LOG

# 检查前端文件
echo -e "${BLUE}🔍 检查前端文件...${NC}" | tee -a $NGINX_LOG
if [ ! -f "$FRONTEND_PATH/index.html" ]; then
    echo -e "${YELLOW}⚠️ 警告: 前端目录中缺少index.html文件${NC}" | tee -a $NGINX_LOG
    # 创建一个临时的index.html，以便测试
    echo -e "${BLUE}ℹ️ 创建临时index.html文件...${NC}" | tee -a $NGINX_LOG
    cat > "$FRONTEND_PATH/index.html" << EOL
<!DOCTYPE html>
<html>
<head>
    <title>小区失物招领平台 - 初始化中</title>
    <style>
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            background-color: #f5f7fa;
            color: #333;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            margin: 0;
            padding: 20px;
            text-align: center;
        }
        .container {
            max-width: 600px;
            background-color: white;
            border-radius: 8px;
            padding: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        h1 {
            color: #409EFF;
            margin-bottom: 20px;
        }
        .spinner {
            display: inline-block;
            width: 50px;
            height: 50px;
            border: 3px solid rgba(64,158,255,0.3);
            border-radius: 50%;
            border-top-color: #409EFF;
            animation: spin 1s ease-in-out infinite;
            margin-bottom: 20px;
        }
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="spinner"></div>
        <h1>小区失物招领平台</h1>
        <p>应用正在初始化中，请稍候...</p>
        <p><small>前端文件尚未部署，此为临时页面</small></p>
    </div>
</body>
</html>
EOL
    echo -e "${GREEN}✓ 临时index.html文件已创建${NC}" | tee -a $NGINX_LOG
else
    echo -e "${GREEN}✓ 前端文件已存在${NC}" | tee -a $NGINX_LOG
fi

# 测试Nginx配置
echo -e "${BLUE}🧪 测试Nginx配置...${NC}" | tee -a $NGINX_LOG
nginx -t 2>&1 | tee -a $NGINX_LOG

# 如果配置测试成功，则重新加载Nginx
if [ $? -eq 0 ]; then
    echo -e "${BLUE}🔄 重新加载Nginx配置...${NC}" | tee -a $NGINX_LOG
    
    # 检查Nginx服务是否运行
    if ! systemctl is-active --quiet nginx; then
        echo -e "${YELLOW}⚠️ Nginx服务未运行，正在启动...${NC}" | tee -a $NGINX_LOG
        systemctl start nginx
    else
        systemctl reload nginx
    fi
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}🎉 Nginx配置完成!${NC}" | tee -a $NGINX_LOG
        echo -e "${GREEN}🔗 应用可通过以下地址访问: http://$SERVER_NAME/${NC}" | tee -a $NGINX_LOG
        
        # 检查防火墙状态
        if command -v firewall-cmd &>/dev/null && firewall-cmd --state &>/dev/null; then
            echo -e "${BLUE}🔍 检查防火墙配置...${NC}" | tee -a $NGINX_LOG
            if ! firewall-cmd --list-services | grep -q "http"; then
                echo -e "${YELLOW}⚠️ 防火墙未开放HTTP服务，尝试开放...${NC}" | tee -a $NGINX_LOG
                firewall-cmd --permanent --add-service=http &>> $NGINX_LOG
                firewall-cmd --reload &>> $NGINX_LOG
                echo -e "${GREEN}✅ 防火墙已配置${NC}" | tee -a $NGINX_LOG
            else
                echo -e "${GREEN}✓ 防火墙已配置允许HTTP流量${NC}" | tee -a $NGINX_LOG
            fi
        fi
    else
        echo -e "${RED}❌ Nginx重启失败${NC}" | tee -a $NGINX_LOG
        exit 1
    fi
else
    echo -e "${RED}❌ Nginx配置测试失败，请检查配置文件${NC}" | tee -a $NGINX_LOG
    exit 1
fi

echo -e "${GREEN}📝 Nginx配置日志: $NGINX_LOG${NC}"
echo -e "${BLUE}💡 提示: 可以通过 'curl localhost' 命令测试Nginx是否正常工作${NC}" 