#!/bin/bash
#
# 后端部署脚本 - 在阿里云ECS上部署和重启Spring Boot应用
# 此脚本用于GitHub Actions工作流，在服务器上自动化部署后端应用
# 兼容多种Linux发行版（Ubuntu、Debian、CentOS、RHEL、Alibaba Cloud Linux）
#

# 应用名称和日志文件
APP_NAME="lostandfound-backend"
LOG_FILE="app.log"
JVM_OPTS="-Xms512m -Xmx1024m"
JAVA_OPTS="-Dspring.profiles.active=prod"

# 端口和PID文件
SERVER_PORT=8080
PID_FILE="app.pid"

# 获取当前目录
DEPLOY_DIR=$(pwd)
JAR_FILE="$DEPLOY_DIR/app.jar"

# 显示彩色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检测Linux发行版类型
if [ -f /etc/os-release ]; then
  . /etc/os-release
  OS_TYPE=$ID
  OS_VERSION=$VERSION_ID
  OS_NAME=$NAME
else
  OS_TYPE=$(uname -s)
  OS_VERSION=$(uname -r)
  OS_NAME="$OS_TYPE $OS_VERSION"
fi

# 打印带颜色的状态信息
log() {
  echo -e "${BLUE}[$APP_NAME]${NC} $1"
}

success() {
  echo -e "${GREEN}[$APP_NAME]${NC} $1"
}

warning() {
  echo -e "${YELLOW}[$APP_NAME]${NC} $1"
}

error() {
  echo -e "${RED}[$APP_NAME]${NC} $1"
  exit 1
}

# 检查Java是否安装
if ! command -v java &> /dev/null; then
  error "Java未安装。请先安装Java JDK。"
fi

# 打印系统信息
log "操作系统: $OS_NAME"
log "Java版本: $(java -version 2>&1 | head -n 1)"

# 确保目录存在
mkdir -p "$DEPLOY_DIR/logs"
mkdir -p "$DEPLOY_DIR/uploads"  # 确保上传目录存在
log "检查并创建必要目录"

# 检查SELinux（针对RHEL/CentOS系统）
if [[ "$OS_TYPE" == "centos" || "$OS_TYPE" == "rhel" || "$OS_TYPE" == "aliyun" || "$OS_TYPE" == "alinux" || "$OS_TYPE" == "fedora" ]]; then
  if command -v sestatus &> /dev/null; then
    if sestatus | grep -q "SELinux status: enabled"; then
      log "SELinux已启用，设置必要的上下文..."
      
      # 设置Java应用目录上下文
      if command -v chcon &> /dev/null; then
        chcon -R -t bin_t "$JAR_FILE" || warning "设置JAR文件SELinux上下文失败"
        chcon -R -t var_log_t "$DEPLOY_DIR/logs" || warning "设置日志目录SELinux上下文失败"
      fi
      
      # 允许Java进程读写必要目录
      if command -v setsebool &> /dev/null; then
        setsebool -P httpd_can_network_connect 1 || warning "设置SELinux网络连接策略失败"
      fi
    fi
  fi
fi

# 检查应用是否正在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        log "正在停止应用 (PID: $PID)..."
        kill -15 $PID
        sleep 5
        
        # 如果应用未能优雅关闭，强制终止
        if ps -p $PID > /dev/null; then
            warning "应用未能优雅关闭，强制终止..."
            kill -9 $PID
        fi
    else
        warning "PID文件存在但进程不存在，可能是上次非正常退出"
    fi
fi

log "开始部署应用..."

# 根据不同系统，采用不同的启动方式
if [[ "$OS_TYPE" == "centos" || "$OS_TYPE" == "rhel" || "$OS_TYPE" == "aliyun" || "$OS_TYPE" == "alinux" || "$OS_TYPE" == "fedora" ]]; then
  # 对于CentOS/RHEL，确保支持Systemd服务
  SYSTEMD_AVAILABLE=$(systemctl --version 2>/dev/null || echo "")
  
  if [[ -n "$SYSTEMD_AVAILABLE" ]]; then
    log "使用systemd启动应用..."
    
    # 创建systemd服务文件
    cat > "/tmp/${APP_NAME}.service" <<EOF
[Unit]
Description=Lost and Found Spring Boot Application
After=syslog.target network.target

[Service]
User=$(whoami)
WorkingDirectory=$DEPLOY_DIR
ExecStart=/usr/bin/java $JVM_OPTS $JAVA_OPTS -jar $JAR_FILE --server.port=$SERVER_PORT
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=append:$DEPLOY_DIR/logs/$LOG_FILE
StandardError=append:$DEPLOY_DIR/logs/$LOG_FILE

[Install]
WantedBy=multi-user.target
EOF
    
    # 使用systemd启动服务
    sudo mv "/tmp/${APP_NAME}.service" "/etc/systemd/system/${APP_NAME}.service"
    sudo systemctl daemon-reload
    sudo systemctl restart "$APP_NAME"
    sudo systemctl enable "$APP_NAME"
    
    # 保存PID到文件（兼容性考虑）
    systemctl show -p MainPID "$APP_NAME" | cut -d= -f2 > "$PID_FILE"
    log "应用已通过systemd启动，服务名: $APP_NAME"
  else
    # 回退到传统的nohup方式
    log "使用nohup启动应用..."
    nohup java $JVM_OPTS $JAVA_OPTS -jar "$JAR_FILE" \
        --server.port=$SERVER_PORT \
        > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &
    
    # 保存PID到文件
    echo $! > "$PID_FILE"
    log "应用已启动 (PID: $(cat $PID_FILE))，日志: $DEPLOY_DIR/logs/$LOG_FILE"
  fi
else
  # 对于Ubuntu/Debian等系统，使用传统nohup方式
  log "使用nohup启动应用..."
  nohup java $JVM_OPTS $JAVA_OPTS -jar "$JAR_FILE" \
      --server.port=$SERVER_PORT \
      > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &
  
  # 保存PID到文件
  echo $! > "$PID_FILE"
  log "应用已启动 (PID: $(cat $PID_FILE))，日志: $DEPLOY_DIR/logs/$LOG_FILE"
fi

# 等待应用启动
log "等待应用启动 (30秒)..."
sleep 15

# 获取当前PID
if [ -f "$PID_FILE" ]; then
  CURRENT_PID=$(cat "$PID_FILE")
  
  # 检查进程是否存在
  if ps -p $CURRENT_PID > /dev/null; then
      # 检查日志中是否有启动成功的标志
      if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
          success "应用启动成功！"
      else
          # 应用进程存在但可能尚未完全启动，再等待
          log "应用正在启动中，继续等待..."
          sleep 15
          if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
              success "应用启动成功！"
          else
              warning "应用可能未完全启动，最近的错误日志:"
              tail -n 50 "$DEPLOY_DIR/logs/$LOG_FILE" | grep -i "error\|exception"
              
              # 再次检查进程是否存在
              if ps -p $CURRENT_PID > /dev/null; then
                  log "进程仍在运行，可能需要更长时间启动或检查配置问题"
                  exit 0  # 不要将此视为错误，让部署继续
              else 
                  error "应用启动失败，进程已退出"
              fi
          fi
      fi
  else
      error "应用启动失败，最近的错误日志:"
      tail -n 50 "$DEPLOY_DIR/logs/$LOG_FILE" | grep -i "error\|exception"
      log "请检查完整日志: $DEPLOY_DIR/logs/$LOG_FILE"
      exit 1
  fi
else
  error "PID文件不存在，应用启动失败"
fi

# 检查应用是否正常响应
log "检查应用HTTP响应..."
sleep 5

# 根据不同系统使用不同的方式检查
if command -v curl &> /dev/null; then
  # 使用curl检查
  if curl -s -o /dev/null -w "%{http_code}" http://localhost:$SERVER_PORT/api/actuator/health 2>/dev/null | grep -q "200"; then
    success "应用HTTP健康检查成功！"
  else
    warning "应用HTTP健康检查未通过，但进程仍在运行"
    warning "请检查应用日志获取更多信息: $DEPLOY_DIR/logs/$LOG_FILE"
  fi
elif command -v wget &> /dev/null; then
  # 使用wget检查
  if wget -q --spider http://localhost:$SERVER_PORT/api/actuator/health; then
    success "应用HTTP健康检查成功！"
  else
    warning "应用HTTP健康检查未通过，但进程仍在运行"
    warning "请检查应用日志获取更多信息: $DEPLOY_DIR/logs/$LOG_FILE"
  fi
else
  # 简单检查进程状态
  if ps -p $CURRENT_PID > /dev/null; then
    success "应用进程仍在运行，但无法执行HTTP健康检查（未安装curl或wget）"
  else
    error "应用进程已停止运行"
  fi
fi

success "部署完成 🚀" 