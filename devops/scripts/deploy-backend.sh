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
DEPLOY_LOG="/tmp/deploy-backend.log"

# 启用颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取部署目录和JAR文件路径
DEPLOY_DIR=${1:-$(pwd)}
JAR_FILE="$DEPLOY_DIR/app.jar"
DB_PASSWORD=${2:-"your_password_here"}

# 确保目录存在
mkdir -p "$DEPLOY_DIR/logs"
mkdir -p "$DEPLOY_DIR/uploads"

echo -e "${BLUE}🚀 开始部署后端应用...${NC}" | tee -a $DEPLOY_LOG
echo -e "📅 $(date)" | tee -a $DEPLOY_LOG
echo -e "📂 部署目录: ${DEPLOY_DIR}" | tee -a $DEPLOY_LOG
echo -e "📄 JAR文件: ${JAR_FILE}" | tee -a $DEPLOY_LOG

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}❌ 错误: JAR文件不存在: $JAR_FILE${NC}" | tee -a $DEPLOY_LOG
    echo -e "${YELLOW}⚠️ 请确保已上传应用JAR文件${NC}" | tee -a $DEPLOY_LOG
    exit 1
fi

# 检查应用是否正在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        echo -e "${YELLOW}⏳ 正在停止应用 (PID: $PID)...${NC}" | tee -a $DEPLOY_LOG
        kill -15 $PID
        
        # 使用进度条显示等待过程
        echo -ne "${YELLOW}⏳ 等待应用停止 [" | tee -a $DEPLOY_LOG
        for i in {1..5}; do
            echo -n "▓" | tee -a $DEPLOY_LOG
            sleep 1
        done
        echo -e "] 完成${NC}" | tee -a $DEPLOY_LOG
        
        # 如果应用未能优雅关闭，强制终止
        if ps -p $PID > /dev/null; then
            echo -e "${RED}⚠️ 应用未能优雅关闭，强制终止...${NC}" | tee -a $DEPLOY_LOG
            kill -9 $PID
        else
            echo -e "${GREEN}✅ 应用已成功停止${NC}" | tee -a $DEPLOY_LOG
        fi
    else
        echo -e "${YELLOW}⚠️ PID文件存在但进程不存在，可能是上次非正常退出${NC}" | tee -a $DEPLOY_LOG
    fi
fi

echo -e "${BLUE}🔄 准备部署应用...${NC}" | tee -a $DEPLOY_LOG

# 测试数据库连接
echo -e "${BLUE}🔍 测试数据库连接...${NC}" | tee -a $DEPLOY_LOG
DB_NAME="lost_and_found"

# 首先测试MySQL服务连接
if mysql -u root -p"$DB_PASSWORD" -e "SELECT 1;" &>/dev/null; then
    echo -e "${GREEN}✅ MySQL服务连接成功${NC}" | tee -a $DEPLOY_LOG
    
    # 然后检查特定数据库是否存在
    if mysql -u root -p"$DB_PASSWORD" -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep -q "$DB_NAME"; then
        echo -e "${GREEN}✅ 数据库 '$DB_NAME' 存在${NC}" | tee -a $DEPLOY_LOG
        
        # 检查数据库表
        TABLES_COUNT=$(mysql -u root -p"$DB_PASSWORD" -e "USE $DB_NAME; SHOW TABLES;" 2>/dev/null | wc -l)
        if [ "$TABLES_COUNT" -gt 1 ]; then
            echo -e "${GREEN}✅ 数据库包含 $((TABLES_COUNT-1)) 个表${NC}" | tee -a $DEPLOY_LOG
            
            # 检查必需的核心表
            REQUIRED_TABLES=("users" "lost_items" "found_items")
            MISSING_TABLES=()
            
            for TABLE in "${REQUIRED_TABLES[@]}"; do
                if ! mysql -u root -p"$DB_PASSWORD" -e "USE $DB_NAME; SHOW TABLES LIKE '$TABLE';" 2>/dev/null | grep -q "$TABLE"; then
                    MISSING_TABLES+=("$TABLE")
                fi
            done
            
            if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
                echo -e "${RED}⚠️ 警告: 缺少必需的表: ${MISSING_TABLES[*]}${NC}" | tee -a $DEPLOY_LOG
                echo -e "${YELLOW}⚠️ 请先正确初始化数据库，或者应用可能无法正常工作${NC}" | tee -a $DEPLOY_LOG
                
                # 询问用户是否继续
                read -p "$(echo -e ${YELLOW}❓ 是否继续部署? [y/N]:${NC} )" ANSWER
                if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
                    echo -e "${BLUE}ℹ️ 部署已取消${NC}" | tee -a $DEPLOY_LOG
                    exit 1
                fi
            else
                echo -e "${GREEN}✅ 所有必需的表都存在${NC}" | tee -a $DEPLOY_LOG
                
                # 查询用户表中的记录数，确认是否有初始用户
                USER_COUNT=$(mysql -u root -p"$DB_PASSWORD" -e "SELECT COUNT(*) FROM $DB_NAME.users;" 2>/dev/null | tail -1)
                echo -e "${GREEN}✓ 用户表中有 $USER_COUNT 条记录${NC}" | tee -a $DEPLOY_LOG
                
                if [ "$USER_COUNT" -eq 0 ]; then
                    echo -e "${YELLOW}⚠️ 用户表中没有记录，可能需要创建初始管理员用户${NC}" | tee -a $DEPLOY_LOG
                fi
            fi
        else
            echo -e "${RED}⚠️ 警告: 数据库存在但没有表或表结构不完整${NC}" | tee -a $DEPLOY_LOG
            echo -e "${YELLOW}⚠️ 请先初始化数据库，或者应用可能无法正常工作${NC}" | tee -a $DEPLOY_LOG
            
            # 询问用户是否继续
            read -p "$(echo -e ${YELLOW}❓ 是否继续部署? [y/N]:${NC} )" ANSWER
            if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
                echo -e "${BLUE}ℹ️ 部署已取消${NC}" | tee -a $DEPLOY_LOG
                exit 1
            fi
        fi
    else
        echo -e "${RED}⚠️ 警告: 数据库 '$DB_NAME' 不存在${NC}" | tee -a $DEPLOY_LOG
        echo -e "${YELLOW}⚠️ 应用启动后将尝试创建数据库，但可能会失败${NC}" | tee -a $DEPLOY_LOG
        echo -e "${YELLOW}💡 建议先运行数据库初始化脚本: /tmp/init-database.sh${NC}" | tee -a $DEPLOY_LOG
        
        # 询问用户是否继续
        read -p "$(echo -e ${YELLOW}❓ 是否继续部署? [y/N]:${NC} )" ANSWER
        if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}ℹ️ 部署已取消${NC}" | tee -a $DEPLOY_LOG
            exit 1
        fi
    fi
else
    echo -e "${RED}⚠️ 数据库连接测试失败，请检查MySQL服务和凭据${NC}" | tee -a $DEPLOY_LOG
    echo -e "${YELLOW}⚠️ 应用部署可能因数据库连接问题而失败${NC}" | tee -a $DEPLOY_LOG
    
    # 询问用户是否继续
    read -p "$(echo -e ${YELLOW}❓ 是否继续部署? [y/N]:${NC} )" ANSWER
    if [[ ! "$ANSWER" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}ℹ️ 部署已取消${NC}" | tee -a $DEPLOY_LOG
        exit 1
    fi
fi

# 替换配置文件中的数据库密码
echo -e "${BLUE}🔧 更新配置文件中的数据库密码...${NC}" | tee -a $DEPLOY_LOG
CONFIG_FILE="$DEPLOY_DIR/application-prod.yml"
if [ -f "$CONFIG_FILE" ]; then
    # 创建备份
    cp "$CONFIG_FILE" "$CONFIG_FILE.bak"
    echo -e "${BLUE}💾 已创建配置文件备份: ${CONFIG_FILE}.bak${NC}" | tee -a $DEPLOY_LOG
    
    # 使用perl替代sed，更安全地处理特殊字符
    perl -i -pe "s/\\\$\{DB_PASSWORD:your_password_here\}/$DB_PASSWORD/g" "$CONFIG_FILE"
    echo -e "${GREEN}✅ 配置文件已更新${NC}" | tee -a $DEPLOY_LOG
else
    echo -e "${RED}⚠️ 未找到配置文件: $CONFIG_FILE${NC}" | tee -a $DEPLOY_LOG
    echo -e "${YELLOW}🔔 将使用命令行参数设置数据库密码${NC}" | tee -a $DEPLOY_LOG
fi

# 启动应用
echo -e "${BLUE}🚀 启动应用...${NC}" | tee -a $DEPLOY_LOG
nohup java $JVM_OPTS -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    --spring.datasource.password="$DB_PASSWORD" \
    > "$DEPLOY_DIR/logs/$LOG_FILE" 2>&1 &

# 保存PID到文件
echo $! > "$PID_FILE"
echo -e "${GREEN}✅ 应用已启动 (PID: $(cat $PID_FILE))${NC}" | tee -a $DEPLOY_LOG

# 检查应用是否成功启动
echo -e "${BLUE}⏳ 等待应用启动 (30秒)...${NC}" | tee -a $DEPLOY_LOG

# 使用进度条显示等待过程
echo -ne "${YELLOW}⏳ 正在启动 [" | tee -a $DEPLOY_LOG
for i in {1..20}; do
    echo -n "▓" | tee -a $DEPLOY_LOG
    sleep 1
    
    # 每5秒检查一次日志，看是否有启动成功的信息
    if [ $((i % 5)) -eq 0 ]; then
        if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
            break
        fi
    fi
done
echo -e "] 完成${NC}" | tee -a $DEPLOY_LOG

# 验证应用是否成功启动
if ps -p $(cat "$PID_FILE") > /dev/null; then
    if grep -q "Started" "$DEPLOY_DIR/logs/$LOG_FILE"; then
        echo -e "${GREEN}🎉 应用启动成功!${NC}" | tee -a $DEPLOY_LOG
        
        # 显示应用信息
        APP_PORT=$(grep -o "server.port=.*" "$DEPLOY_DIR/logs/$LOG_FILE" | head -1 | cut -d= -f2 || echo "8080")
        echo -e "${GREEN}🔗 应用访问地址: http://$(hostname -I | awk '{print $1}'):${APP_PORT}/api${NC}" | tee -a $DEPLOY_LOG
        echo -e "${GREEN}📝 应用日志文件: $DEPLOY_DIR/logs/$LOG_FILE${NC}" | tee -a $DEPLOY_LOG
    else
        echo -e "${YELLOW}⚠️ 应用正在启动中，日志未显示完全启动...${NC}" | tee -a $DEPLOY_LOG
        echo -e "${YELLOW}📊 请稍后查看日志: $DEPLOY_DIR/logs/$LOG_FILE${NC}" | tee -a $DEPLOY_LOG
    fi
else
    echo -e "${RED}❌ 应用启动失败，请检查日志: $DEPLOY_DIR/logs/$LOG_FILE${NC}" | tee -a $DEPLOY_LOG
    
    # 显示最近的错误日志
    echo -e "${RED}🔍 最近的错误日志:${NC}" | tee -a $DEPLOY_LOG
    grep -i "error\|exception" "$DEPLOY_DIR/logs/$LOG_FILE" | tail -10 | tee -a $DEPLOY_LOG
    exit 1
fi

echo -e "${GREEN}📝 部署日志: $DEPLOY_LOG${NC}" 