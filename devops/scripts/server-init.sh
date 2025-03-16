#!/bin/bash
#
# 阿里云ECS服务器初始化脚本 (精简版)
# 适用于阿里云Linux系统
#

# 配置项
MYSQL_ROOT_PASSWORD=${1:-"your_password_here"}
DEPLOY_PATH=${2:-"/opt/lostandfound/backend"}
FRONTEND_PATH=${3:-"/var/www/html/lostandfound"}
LOG_FILE="/tmp/server-init.log"

# 启用颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志输出
echo -e "${BLUE}🚀 开始服务器初始化...${NC}" | tee -a $LOG_FILE
echo -e "📅 $(date)" | tee -a $LOG_FILE
echo -e "💾 部署路径: ${DEPLOY_PATH}" | tee -a $LOG_FILE
echo -e "🌐 前端路径: ${FRONTEND_PATH}" | tee -a $LOG_FILE

# 检查root权限
if [ "$(id -u)" -ne 0 ]; then
    echo -e "${RED}❌ 错误: 此脚本需要root权限运行${NC}" | tee -a $LOG_FILE
    exit 1
fi

# 创建标记文件的函数
mark_installed() {
    mkdir -p /opt/lostandfound/.install_flags
    touch "/opt/lostandfound/.install_flags/$1"
    echo -e "${GREEN}✅ 已标记 $1 为已安装${NC}" | tee -a $LOG_FILE
}

# 检查是否已安装
is_installed() {
    if [ -f "/opt/lostandfound/.install_flags/$1" ]; then
        return 0 # 已安装
    else
        return 1 # 未安装
    fi
}

# 创建必要目录
echo -e "${BLUE}📂 创建必要目录...${NC}" | tee -a $LOG_FILE
mkdir -p "$DEPLOY_PATH"
mkdir -p "$FRONTEND_PATH"
mkdir -p "$DEPLOY_PATH/logs"
mkdir -p "$DEPLOY_PATH/uploads"
echo -e "${GREEN}✅ 目录创建完成${NC}" | tee -a $LOG_FILE

# 更新软件包列表
if ! is_installed "repo_updated"; then
    echo -e "${BLUE}🔄 更新软件包列表...${NC}" | tee -a $LOG_FILE
    yum makecache -y &>> $LOG_FILE
    mark_installed "repo_updated"
else
    echo -e "${GREEN}✓ 软件包列表已更新，跳过${NC}" | tee -a $LOG_FILE
fi

# 安装必要软件包
echo -e "${BLUE}📦 安装必要软件包...${NC}" | tee -a $LOG_FILE

# 安装Java 17 (OpenJDK)
if ! is_installed "java17" && ! command -v java &>/dev/null; then
    echo -e "${YELLOW}⏳ 安装Java 17...${NC}" | tee -a $LOG_FILE
    yum install -y java-17-openjdk-devel &>> $LOG_FILE
    if [ $? -eq 0 ]; then
        mark_installed "java17"
        echo -e "${GREEN}✅ Java 17安装成功${NC}" | tee -a $LOG_FILE
        java -version | tee -a $LOG_FILE
    else
        echo -e "${RED}❌ Java 17安装失败${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${GREEN}✓ Java已安装，跳过${NC}" | tee -a $LOG_FILE
    java -version | tee -a $LOG_FILE
fi

# 安装MySQL/MariaDB
if ! is_installed "mysql" && ! command -v mysql &>/dev/null; then
    echo -e "${YELLOW}⏳ 安装MySQL/MariaDB...${NC}" | tee -a $LOG_FILE
    yum install -y mysql-server &>> $LOG_FILE || yum install -y mariadb-server &>> $LOG_FILE
    if [ $? -eq 0 ]; then
        mark_installed "mysql"
        echo -e "${GREEN}✅ MySQL/MariaDB安装成功${NC}" | tee -a $LOG_FILE
    else
        echo -e "${RED}❌ MySQL/MariaDB安装失败${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${GREEN}✓ MySQL/MariaDB已安装，跳过${NC}" | tee -a $LOG_FILE
fi

# 安装Nginx
if ! is_installed "nginx" && ! command -v nginx &>/dev/null; then
    echo -e "${YELLOW}⏳ 安装Nginx...${NC}" | tee -a $LOG_FILE
    yum install -y nginx &>> $LOG_FILE
    if [ $? -eq 0 ]; then
        mark_installed "nginx"
        echo -e "${GREEN}✅ Nginx安装成功${NC}" | tee -a $LOG_FILE
    else
        echo -e "${RED}❌ Nginx安装失败${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${GREEN}✓ Nginx已安装，跳过${NC}" | tee -a $LOG_FILE
fi

# 安装Redis
if ! is_installed "redis" && ! command -v redis-server &>/dev/null; then
    echo -e "${YELLOW}⏳ 安装Redis...${NC}" | tee -a $LOG_FILE
    yum install -y redis &>> $LOG_FILE
    if [ $? -eq 0 ]; then
        mark_installed "redis"
        echo -e "${GREEN}✅ Redis安装成功${NC}" | tee -a $LOG_FILE
    else
        echo -e "${RED}❌ Redis安装失败${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${GREEN}✓ Redis已安装，跳过${NC}" | tee -a $LOG_FILE
fi

# 启动服务
echo -e "${BLUE}🔌 启动必要服务...${NC}" | tee -a $LOG_FILE

# 启动MySQL/MariaDB
if systemctl list-unit-files | grep -q mariadb; then
    if ! systemctl is-active --quiet mariadb; then
        echo -e "${YELLOW}⏳ 启动MariaDB...${NC}" | tee -a $LOG_FILE
        systemctl enable --now mariadb &>> $LOG_FILE
        echo -e "${GREEN}✅ MariaDB已启动${NC}" | tee -a $LOG_FILE
    else
        echo -e "${GREEN}✓ MariaDB已在运行，跳过${NC}" | tee -a $LOG_FILE
    fi
else
    if ! systemctl is-active --quiet mysqld; then
        echo -e "${YELLOW}⏳ 启动MySQL...${NC}" | tee -a $LOG_FILE
        systemctl enable --now mysqld &>> $LOG_FILE
        echo -e "${GREEN}✅ MySQL已启动${NC}" | tee -a $LOG_FILE
    else
        echo -e "${GREEN}✓ MySQL已在运行，跳过${NC}" | tee -a $LOG_FILE
    fi
fi

# 启动Redis
if ! systemctl is-active --quiet redis; then
    echo -e "${YELLOW}⏳ 启动Redis...${NC}" | tee -a $LOG_FILE
    systemctl enable --now redis &>> $LOG_FILE
    echo -e "${GREEN}✅ Redis已启动${NC}" | tee -a $LOG_FILE
else
    echo -e "${GREEN}✓ Redis已在运行，跳过${NC}" | tee -a $LOG_FILE
fi

# 启动Nginx
if ! systemctl is-active --quiet nginx; then
    echo -e "${YELLOW}⏳ 启动Nginx...${NC}" | tee -a $LOG_FILE
    systemctl enable --now nginx &>> $LOG_FILE
    echo -e "${GREEN}✅ Nginx已启动${NC}" | tee -a $LOG_FILE
else
    echo -e "${GREEN}✓ Nginx已在运行，跳过${NC}" | tee -a $LOG_FILE
fi

# 初始化数据库
echo -e "${BLUE}🗃️ 检查数据库初始化脚本...${NC}" | tee -a $LOG_FILE
INIT_DB_SQL_FILE="/tmp/init-database.sql"
INIT_DB_SCRIPT="/tmp/init-database.sh"

if [ -f "$INIT_DB_SQL_FILE" ] && [ -f "$INIT_DB_SCRIPT" ]; then
    if ! is_installed "db_initialized"; then
        echo -e "${YELLOW}⏳ 执行数据库初始化脚本...${NC}" | tee -a $LOG_FILE
        chmod +x "$INIT_DB_SCRIPT"
        "$INIT_DB_SCRIPT" "$MYSQL_ROOT_PASSWORD" "$INIT_DB_SQL_FILE" | tee -a $LOG_FILE
        if [ $? -eq 0 ]; then
            mark_installed "db_initialized"
            echo -e "${GREEN}✅ 数据库初始化成功${NC}" | tee -a $LOG_FILE
        else
            echo -e "${RED}❌ 数据库初始化失败${NC}" | tee -a $LOG_FILE
        fi
    else
        echo -e "${GREEN}✓ 数据库已初始化，跳过${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${YELLOW}⚠️ 数据库初始化脚本不存在，跳过初始化${NC}" | tee -a $LOG_FILE
fi

# 配置防火墙
if ! is_installed "firewall_configured"; then
    echo -e "${BLUE}🔥 配置防火墙...${NC}" | tee -a $LOG_FILE
    if command -v firewall-cmd &>/dev/null; then
        firewall-cmd --permanent --add-service=http &>> $LOG_FILE
        firewall-cmd --permanent --add-service=https &>> $LOG_FILE
        firewall-cmd --permanent --add-port=8080/tcp &>> $LOG_FILE
        firewall-cmd --reload &>> $LOG_FILE
        mark_installed "firewall_configured"
        echo -e "${GREEN}✅ 防火墙配置完成${NC}" | tee -a $LOG_FILE
    else
        echo -e "${YELLOW}⚠️ 未检测到firewalld，跳过防火墙配置${NC}" | tee -a $LOG_FILE
    fi
else
    echo -e "${GREEN}✓ 防火墙已配置，跳过${NC}" | tee -a $LOG_FILE
fi

echo -e "${GREEN}🎉 服务器初始化完成!${NC}" | tee -a $LOG_FILE
echo -e "📁 部署路径: ${DEPLOY_PATH}" | tee -a $LOG_FILE
echo -e "🌐 前端路径: ${FRONTEND_PATH}" | tee -a $LOG_FILE
echo -e "📝 日志文件: ${LOG_FILE}" | tee -a $LOG_FILE
