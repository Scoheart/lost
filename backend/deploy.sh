#!/bin/bash

# 部署脚本 - 在阿里云ECS上重启Spring Boot应用
# 此脚本应该被上传到服务器的部署目录

# 应用名称和日志文件
APP_NAME="lostandfound-backend"
LOG_FILE="app.log"
JVM_OPTS="-Xms512m -Xmx1024m"
JAVA_OPTS=""

# 端口和PID文件
SERVER_PORT=8080
PID_FILE="app.pid"

# 获取当前目录
DEPLOY_DIR=$(pwd)
JAR_FILE="$DEPLOY_DIR/app.jar"

# 确保目录存在
mkdir -p "$DEPLOY_DIR/logs"

# 检查应用是否正在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        echo "[$APP_NAME] 正在停止应用 (PID: $PID)..."
        kill -15 $PID
        sleep 5
        
        # 如果应用未能优雅关闭，强制终止
        if ps -p $PID > /dev/null; then
            echo "[$APP_NAME] 应用未能优雅关闭，强制终止..."
            kill -9 $PID
        fi
    fi
fi

echo "[$APP_NAME] 开始部署应用..."

# 启动应用
nohup java $JVM_OPTS $JAVA_OPTS -jar "$JAR_FILE" \
    --server.port=$SERVER_PORT \
    --spring.profiles.active=prod \
    > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &

# 保存PID到文件
echo $! > "$PID_FILE"
echo "[$APP_NAME] 应用已启动 (PID: $(cat $PID_FILE))，日志: $DEPLOY_DIR/logs/$LOG_FILE"

# 检查应用是否成功启动
sleep 10
if ps -p $(cat "$PID_FILE") > /dev/null; then
    echo "[$APP_NAME] 应用启动成功!"
else
    echo "[$APP_NAME] 应用启动失败，请检查日志!"
    exit 1
fi 