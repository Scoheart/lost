#!/bin/bash
#
# 数据库初始化脚本 (精简版)
# 适用于阿里云Linux系统
#

# 配置项
DB_PASSWORD=${1:-"your_password_here"}
SQL_FILE=${2:-"init-database.sql"}
DB_NAME="lost_and_found"
DB_LOG="/tmp/init-database.log"

# 启用颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    echo -e "${RED}❌ 错误: 此脚本需要root权限运行${NC}" | tee -a $DB_LOG
    exit 1
fi

echo -e "${BLUE}🚀 开始数据库初始化...${NC}" | tee -a $DB_LOG
echo -e "📅 $(date)" | tee -a $DB_LOG
echo -e "🗄️ 数据库名称: ${DB_NAME}" | tee -a $DB_LOG
echo -e "📄 使用SQL文件: ${SQL_FILE}" | tee -a $DB_LOG

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}❌ 错误: MySQL/MariaDB未安装${NC}" | tee -a $DB_LOG
    echo -e "${YELLOW}⚠️ 请先安装数据库后再运行此脚本${NC}" | tee -a $DB_LOG
    exit 1
fi

# 检查MySQL服务状态
if ! systemctl is-active --quiet mysql && ! systemctl is-active --quiet mariadb && ! systemctl is-active --quiet mysqld; then
    echo -e "${YELLOW}⚠️ MySQL/MariaDB服务未运行，尝试启动...${NC}" | tee -a $DB_LOG
    
    if systemctl start mysql 2>/dev/null || systemctl start mariadb 2>/dev/null || systemctl start mysqld 2>/dev/null; then
        echo -e "${GREEN}✅ 数据库服务已启动${NC}" | tee -a $DB_LOG
    else
        echo -e "${RED}❌ 无法启动数据库服务${NC}" | tee -a $DB_LOG
        exit 1
    fi
else
    echo -e "${GREEN}✓ 数据库服务已在运行${NC}" | tee -a $DB_LOG
fi

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo -e "${RED}❌ 错误: SQL文件不存在: $SQL_FILE${NC}" | tee -a $DB_LOG
    exit 1
fi

# 检查数据库是否已存在
DB_EXISTS=$(mysql --user=root --password="$DB_PASSWORD" -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep -c "$DB_NAME")
if [ "$DB_EXISTS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 数据库 '$DB_NAME' 已存在${NC}" | tee -a $DB_LOG
    read -p "$(echo -e ${YELLOW}❓ 是否重新初始化数据库? [y/N]:${NC} )" ANSWER
    
    if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}ℹ️ 跳过数据库初始化${NC}" | tee -a $DB_LOG
        exit 0
    else
        echo -e "${YELLOW}⚠️ 将重新初始化数据库 '$DB_NAME'${NC}" | tee -a $DB_LOG
    fi
fi

# 重置MySQL root密码
echo -e "${BLUE}🔑 配置MySQL root用户密码...${NC}" | tee -a $DB_LOG

# 检查MySQL是否已设置密码
if mysql -u root -e "SELECT 1" &>/dev/null; then
    # 无密码可以连接，设置密码
    mysqladmin -u root password "$DB_PASSWORD" &>/dev/null && echo -e "${GREEN}✅ MySQL root密码已设置${NC}" | tee -a $DB_LOG
elif mysql -u root -p"$DB_PASSWORD" -e "SELECT 1" &>/dev/null; then
    # 尝试使用提供的密码连接，如果成功，密码已正确设置
    echo -e "${GREEN}✓ MySQL root密码已正确设置${NC}" | tee -a $DB_LOG
else
    # 尝试重置密码，但这可能需要根据MySQL版本有所调整
    echo -e "${YELLOW}⚠️ 尝试重置MySQL root密码...${NC}" | tee -a $DB_LOG
    
    # 方法1: 使用mysqladmin (可能需要旧密码)
    mysqladmin -u root -p"$DB_PASSWORD" password "$DB_PASSWORD" &>/dev/null || true
    
    # 方法2: 使用直接的SQL命令
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASSWORD';" &>/dev/null || true
    mysql -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$DB_PASSWORD');" &>/dev/null || true
    
    # 验证是否成功
    if mysql -u root -p"$DB_PASSWORD" -e "SELECT 1" &>/dev/null; then
        echo -e "${GREEN}✅ MySQL root密码已重置${NC}" | tee -a $DB_LOG
    else
        echo -e "${RED}❌ 无法自动重置MySQL密码${NC}" | tee -a $DB_LOG
        echo -e "${YELLOW}⚠️ 您可能需要手动重置MySQL root密码${NC}" | tee -a $DB_LOG
        echo -e "${YELLOW}💡 提示: 尝试以下命令手动重置MySQL密码:${NC}" | tee -a $DB_LOG
        echo -e "   sudo mysqld_safe --skip-grant-tables &" | tee -a $DB_LOG
        echo -e "   mysql -u root -e \"FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY 'aliyun888';\"" | tee -a $DB_LOG
        exit 1
    fi
fi

# 创建临时配置文件以避免密码提示
echo -e "${BLUE}📝 创建临时MySQL配置文件...${NC}" | tee -a $DB_LOG
MY_CNF=$(mktemp)
cat > "$MY_CNF" << EOL
[client]
user=root
password="$DB_PASSWORD"
EOL

# 创建数据库并导入数据
echo -e "${BLUE}🗃️ 创建数据库并导入数据...${NC}" | tee -a $DB_LOG

# 创建数据库
echo -e "${YELLOW}⏳ 创建数据库 '$DB_NAME'...${NC}" | tee -a $DB_LOG
mysql --defaults-file="$MY_CNF" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" && echo -e "${GREEN}✅ 数据库创建成功${NC}" | tee -a $DB_LOG

# 导入SQL文件
echo -e "${YELLOW}⏳ 导入SQL文件...${NC}" | tee -a $DB_LOG
mysql --defaults-file="$MY_CNF" $DB_NAME < "$SQL_FILE" && echo -e "${GREEN}✅ SQL文件导入成功${NC}" | tee -a $DB_LOG

# 验证数据库是否成功创建
echo -e "${BLUE}🔍 验证数据库初始化...${NC}" | tee -a $DB_LOG
TABLES_COUNT=$(mysql --defaults-file="$MY_CNF" -e "USE $DB_NAME; SHOW TABLES;" | wc -l)
if [ "$TABLES_COUNT" -gt 0 ]; then
    TABLES_LIST=$(mysql --defaults-file="$MY_CNF" -e "USE $DB_NAME; SHOW TABLES;" | tail -n +2 | tr '\n' ', ' | sed 's/,$/\n/')
    echo -e "${GREEN}✅ 数据库初始化成功${NC}" | tee -a $DB_LOG
    echo -e "${GREEN}📊 共创建了 $((TABLES_COUNT-1)) 个表: ${TABLES_LIST}${NC}" | tee -a $DB_LOG
else
    echo -e "${RED}❌ 数据库初始化失败${NC}" | tee -a $DB_LOG
    exit 1
fi

# 删除临时配置文件
rm -f "$MY_CNF"
echo -e "${GREEN}✓ 临时配置文件已清理${NC}" | tee -a $DB_LOG

# 确保MySQL允许本地连接
echo -e "${BLUE}🔌 配置MySQL允许本地连接...${NC}" | tee -a $DB_LOG
mysql --user=root --password="$DB_PASSWORD" -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY '$DB_PASSWORD'; FLUSH PRIVILEGES;" && echo -e "${GREEN}✅ MySQL权限配置成功${NC}" | tee -a $DB_LOG

echo -e "${GREEN}🎉 MySQL配置完成，本地连接已启用${NC}" | tee -a $DB_LOG
echo -e "${BLUE}💡 数据库信息:${NC}" | tee -a $DB_LOG
echo -e "  🗄️ 数据库名称: ${DB_NAME}" | tee -a $DB_LOG
echo -e "  👤 用户名: root" | tee -a $DB_LOG
echo -e "  🔐 密码: ******" | tee -a $DB_LOG
echo -e "  🔗 连接URL: jdbc:mysql://localhost:3306/${DB_NAME}" | tee -a $DB_LOG
echo -e "  📝 日志文件: ${DB_LOG}" | tee -a $DB_LOG 