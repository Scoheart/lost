#!/bin/bash
#
# 数据库初始化脚本 (精简版)
# 适用于阿里云Linux系统
#

# 配置项
DB_PASSWORD=${1:-"your_password_here"}
SQL_FILE=${2:-"init-database.sql"}
DB_NAME="lost_and_found"

# 检查是否有root权限
if [ "$(id -u)" -ne 0 ]; then
    echo "[ERROR] 此脚本需要root权限运行"
    exit 1
fi

echo "[INFO] 开始数据库初始化..."
echo "[INFO] 使用SQL文件: $SQL_FILE"

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    echo "[ERROR] MySQL/MariaDB未安装，请先安装数据库"
    exit 1
fi

# 检查MySQL服务状态
if ! systemctl is-active --quiet mysql && ! systemctl is-active --quiet mariadb && ! systemctl is-active --quiet mysqld; then
    echo "[INFO] MySQL/MariaDB服务未运行，启动服务..."
    systemctl start mysql || systemctl start mariadb || systemctl start mysqld
fi

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "[ERROR] SQL文件不存在: $SQL_FILE"
    exit 1
fi

# 重置MySQL root密码
echo "[INFO] 配置MySQL root用户密码..."

# 检查MySQL是否已设置密码
if mysql -u root -e "SELECT 1" &>/dev/null; then
    # 无密码可以连接，设置密码
    mysqladmin -u root password "$DB_PASSWORD" &>/dev/null && echo "[INFO] MySQL root密码已设置"
elif mysql -u root -p"$DB_PASSWORD" -e "SELECT 1" &>/dev/null; then
    # 尝试使用提供的密码连接，如果成功，密码已正确设置
    echo "[INFO] MySQL root密码已正确设置"
else
    # 尝试重置密码，但这可能需要根据MySQL版本有所调整
    echo "[WARNING] 尝试重置MySQL root密码..."
    
    # 方法1: 使用mysqladmin (可能需要旧密码)
    mysqladmin -u root -p"$DB_PASSWORD" password "$DB_PASSWORD" &>/dev/null || true
    
    # 方法2: 使用直接的SQL命令
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASSWORD';" &>/dev/null || true
    mysql -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$DB_PASSWORD');" &>/dev/null || true
    
    # 验证是否成功
    if mysql -u root -p"$DB_PASSWORD" -e "SELECT 1" &>/dev/null; then
        echo "[SUCCESS] MySQL root密码已重置"
    else
        echo "[WARNING] 无法自动重置MySQL密码，可能需要手动设置"
    fi
fi

# 创建临时配置文件以避免密码提示
echo "[INFO] 创建临时MySQL配置文件..."
MY_CNF=$(mktemp)
cat > "$MY_CNF" << EOL
[client]
user=root
password="$DB_PASSWORD"
EOL

# 创建数据库并导入数据
echo "[INFO] 创建数据库并导入数据..."

# 创建数据库
mysql --defaults-file="$MY_CNF" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入SQL文件
mysql --defaults-file="$MY_CNF" $DB_NAME < "$SQL_FILE"

# 验证数据库是否成功创建
if mysql --defaults-file="$MY_CNF" -e "USE $DB_NAME; SHOW TABLES;" | grep -q "."; then
    echo "[SUCCESS] 数据库初始化成功"
else
    echo "[ERROR] 数据库初始化失败"
    exit 1
fi

# 删除临时配置文件
rm -f "$MY_CNF"

# 确保MySQL允许本地连接
echo "[INFO] 配置MySQL允许本地连接..."
mysql --user=root --password="$DB_PASSWORD" -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY '$DB_PASSWORD'; FLUSH PRIVILEGES;"

echo "[SUCCESS] MySQL配置完成，本地连接已启用" 