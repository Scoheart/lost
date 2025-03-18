#!/bin/bash

# 数据库迁移脚本

# 数据库配置
DB_USER="root"
DB_PASS="password"
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="lost_and_found"

# 显示脚本执行步骤
set -x

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo "=== 开始数据库迁移 ==="

# 1. 执行数据库初始化脚本
echo "正在执行数据库初始化脚本..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS < "$SCRIPT_DIR/init-database.sql"

# 2. 执行评论数据结构迁移脚本
echo "正在执行评论数据迁移脚本..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS < "$SCRIPT_DIR/migrate-comments.sql"

# 3. 检查迁移结果
echo "正在检查迁移结果..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "
USE $DB_NAME;
SELECT 'users' AS table_name, COUNT(*) AS record_count FROM users
UNION ALL
SELECT 'lost_items' AS table_name, COUNT(*) AS record_count FROM lost_items
UNION ALL
SELECT 'found_items' AS table_name, COUNT(*) AS record_count FROM found_items
UNION ALL
SELECT 'posts' AS table_name, COUNT(*) AS record_count FROM posts
UNION ALL
SELECT 'comments' AS table_name, COUNT(*) AS record_count FROM comments
UNION ALL
SELECT 'item comments' AS table_name, COUNT(*) AS record_count FROM comments WHERE item_type IN ('lost', 'found')
UNION ALL
SELECT 'post comments' AS table_name, COUNT(*) AS record_count FROM comments WHERE item_type = 'post';
"

echo "=== 数据库迁移完成 ==="

# 脚本使用说明：
# 1. 根据您的环境配置数据库连接参数
# 2. 确保mysql命令可用
# 3. 赋予执行权限：chmod +x db-migration.sh
# 4. 执行脚本：./db-migration.sh 