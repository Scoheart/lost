#!/bin/bash
#
# 后端部署脚本 (精简版)
# 仅适用于阿里云Linux系统
#

# 应用名称和日志文件
APP_NAME="lostandfound-backend"
LOG_FILE="app.log"
PID_FILE="app.pid"
JVM_OPTS="-Xms512m -Xmx1024m"

# 获取部署目录和JAR文件路径
DEPLOY_DIR=${1:-$(pwd)}
JAR_FILE="$DEPLOY_DIR/app.jar"
DB_PASSWORD=${2:-"your_password_here"}

# 确保目录存在
mkdir -p "$DEPLOY_DIR/logs"
mkdir -p "$DEPLOY_DIR/uploads"

echo "[INFO] 开始部署应用..."
echo "[INFO] 部署目录: $DEPLOY_DIR"
echo "[INFO] JAR文件: $JAR_FILE"

# 检查应用是否正在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        echo "[INFO] 正在停止应用 (PID: $PID)..."
        kill -15 $PID
        sleep 5
        
        # 如果应用未能优雅关闭，强制终止
        if ps -p $PID > /dev/null; then
            echo "[WARNING] 应用未能优雅关闭，强制终止..."
            kill -9 $PID
        fi
    else
        echo "[WARNING] PID文件存在但进程不存在，可能是上次非正常退出"
    fi
fi

echo "[INFO] 开始部署应用..."

# 测试数据库连接
echo "[INFO] 测试数据库连接..."
if mysql -u root -p"$DB_PASSWORD" -e "SELECT 1;" &>/dev/null; then
    echo "[SUCCESS] 数据库连接测试成功"
else
    echo "[WARNING] 数据库连接测试失败，请检查凭据"
fi

# 替换配置文件中的数据库密码
echo "[INFO] 更新配置文件中的数据库密码..."
CONFIG_FILE="$DEPLOY_DIR/application-prod.yml"
if [ -f "$CONFIG_FILE" ]; then
    # 创建备份
    cp "$CONFIG_FILE" "$CONFIG_FILE.bak"
    
    # 使用perl替代sed，更安全地处理特殊字符
    perl -i -pe "s/\\\$\{DB_PASSWORD:your_password_here\}/$DB_PASSWORD/g" "$CONFIG_FILE"
    echo "[SUCCESS] 配置文件已更新"
else
    echo "[WARNING] 未找到配置文件: $CONFIG_FILE"
fi

# 启动应用
echo "[INFO] 启动应用..."
nohup java $JVM_OPTS -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    --spring.datasource.password="$DB_PASSWORD" \
    > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &

# 保存PID到文件
echo $! > "$PID_FILE"
echo "[INFO] 应用已启动 (PID: $(cat $PID_FILE))"

# 检查应用是否成功启动
echo "[INFO] 等待应用启动 (30秒)..."
sleep 20

# 验证应用是否成功启动
if ps -p $(cat "$PID_FILE") > /dev/null; then
    if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
        echo "[SUCCESS] 应用启动成功!"
    else
        echo "[WARNING] 应用正在启动中，日志未显示完全启动..."
    fi
else
    echo "[ERROR] 应用启动失败，请检查日志: $DEPLOY_DIR/logs/$LOG_FILE"
    exit 1
fi 