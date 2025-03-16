#!/bin/bash

# 部署脚本 - 在阿里云ECS上重启Spring Boot应用
# 此脚本应该被上传到服务器的部署目录

# 应用名称和日志文件
APP_NAME="lostandfound-backend"
LOG_FILE="app.log"
JVM_OPTS="-Xms512m -Xmx1024m"

# 环境变量配置
SPRING_PROFILES_ACTIVE="prod"
FILE_BASE_URL="http://121.40.52.9/api"
FILE_UPLOAD_DIR="$(pwd)/uploads"

# 端口和PID文件
SERVER_PORT=8080
PID_FILE="app.pid"

# 获取当前目录
DEPLOY_DIR=$(pwd)
JAR_FILE="$DEPLOY_DIR/app.jar"

# 确保目录存在
mkdir -p "$DEPLOY_DIR/logs"
mkdir -p "$DEPLOY_DIR/uploads"  # 确保上传目录存在
mkdir -p "$DEPLOY_DIR/uploads/avatars"
mkdir -p "$DEPLOY_DIR/uploads/item-images"

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

# 设置环境变量
export SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE"
export FILE_BASE_URL="$FILE_BASE_URL"
export FILE_UPLOAD_DIR="$FILE_UPLOAD_DIR"

# 启动应用
nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dfile.base.url=$FILE_BASE_URL \
    -Dfile.upload.dir=$FILE_UPLOAD_DIR \
    -jar "$JAR_FILE" \
    --server.port=$SERVER_PORT \
    > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &

# 保存PID到文件
echo $! > "$PID_FILE"
echo "[$APP_NAME] 应用已启动 (PID: $(cat $PID_FILE))，日志: $DEPLOY_DIR/logs/$LOG_FILE"

# 检查应用是否成功启动
echo "[$APP_NAME] 等待应用启动 (30秒)..."
sleep 15

# 检查是否成功启动
if ps -p $(cat "$PID_FILE") > /dev/null; then
    # 检查日志中是否有启动成功的标志
    if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
        echo "[$APP_NAME] 应用启动成功!"
    else
        # 应用进程存在但可能尚未完全启动，再等待
        echo "[$APP_NAME] 应用正在启动中，继续等待..."
        sleep 15
        if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
            echo "[$APP_NAME] 应用启动成功!"
        else
            echo "[$APP_NAME] 应用可能未完全启动，最近的错误日志:"
            tail -n 50 "$DEPLOY_DIR/logs/$LOG_FILE" | grep -i "error\|exception"
            echo "[$APP_NAME] 请检查完整日志: $DEPLOY_DIR/logs/$LOG_FILE"
            exit 1
        fi
    fi
else
    echo "[$APP_NAME] 应用启动失败，最近的错误日志:"
    tail -n 50 "$DEPLOY_DIR/logs/$LOG_FILE" | grep -i "error\|exception"
    echo "[$APP_NAME] 请检查完整日志: $DEPLOY_DIR/logs/$LOG_FILE"
    exit 1
fi 