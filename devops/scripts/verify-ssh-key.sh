#!/bin/bash
#
# SSH密钥验证工具
# 用于检查SSH私钥是否格式正确，可用于GitHub Actions
#

set -e

# 显示彩色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

if [ "$#" -ne 1 ]; then
    echo -e "${RED}错误: 请提供SSH私钥文件路径${NC}"
    echo "用法: $0 <ssh私钥文件路径>"
    echo "例如: $0 ~/.ssh/id_ed25519"
    exit 1
fi

SSH_KEY_FILE="$1"

# 检查文件是否存在
if [ ! -f "$SSH_KEY_FILE" ]; then
    echo -e "${RED}错误: 文件 '$SSH_KEY_FILE' 不存在${NC}"
    exit 1
fi

echo -e "${BLUE}开始验证SSH密钥...${NC}"

# 检查文件权限
FILE_PERMS=$(stat -c "%a" "$SSH_KEY_FILE" 2>/dev/null || stat -f "%Lp" "$SSH_KEY_FILE")
if [[ "$FILE_PERMS" != "600" && "$FILE_PERMS" != "400" ]]; then
    echo -e "${YELLOW}警告: 文件权限不是600或400（当前: $FILE_PERMS）${NC}"
    echo -e "${YELLOW}建议执行: chmod 600 $SSH_KEY_FILE${NC}"
fi

# 检查BEGIN/END标记
if ! grep -q "BEGIN" "$SSH_KEY_FILE"; then
    echo -e "${RED}错误: 文件缺少BEGIN标记，不是有效的私钥格式${NC}"
    echo -e "${YELLOW}正确的私钥应包含类似以下的行:${NC}"
    echo "-----BEGIN OPENSSH PRIVATE KEY-----"
    echo "..."
    echo "-----END OPENSSH PRIVATE KEY-----"
    exit 1
fi

if ! grep -q "END" "$SSH_KEY_FILE"; then
    echo -e "${RED}错误: 文件缺少END标记，不是有效的私钥格式${NC}"
    exit 1
fi

# 使用ssh-keygen验证密钥
echo -e "${BLUE}验证密钥格式...${NC}"
if ssh-keygen -l -f "$SSH_KEY_FILE" &>/dev/null; then
    KEY_INFO=$(ssh-keygen -l -f "$SSH_KEY_FILE")
    echo -e "${GREEN}✓ 密钥格式正确!${NC}"
    echo -e "${BLUE}密钥信息: $KEY_INFO${NC}"
else
    echo -e "${RED}✗ 密钥格式无效!${NC}"
    echo -e "${YELLOW}可能的原因:${NC}"
    echo "1. 文件不是私钥（可能是公钥）"
    echo "2. 私钥格式不正确"
    echo "3. 私钥已损坏"
    exit 1
fi

# 检查密钥是否加密
if grep -q "ENCRYPTED" "$SSH_KEY_FILE"; then
    echo -e "${YELLOW}警告: 此密钥有密码保护${NC}"
    echo -e "${YELLOW}GitHub Actions不能使用有密码保护的密钥${NC}"
    echo "建议生成一个新的无密码密钥用于自动化部署:"
    echo "ssh-keygen -t ed25519 -f ~/.ssh/github_deploy_key -N \"\""
    exit 1
fi

# 检查文件MIME类型（检测无意中存为文本/HTML等格式）
if command -v file &>/dev/null; then
    MIME_TYPE=$(file --mime-type -b "$SSH_KEY_FILE")
    if [[ "$MIME_TYPE" != "text/plain" && "$MIME_TYPE" != "application/octet-stream" ]]; then
        echo -e "${YELLOW}警告: 文件MIME类型异常: $MIME_TYPE${NC}"
        echo "期望的类型是 text/plain 或 application/octet-stream"
    fi
fi

# 检查换行符，Windows CRLF可能导致问题
if command -v file &>/dev/null; then
    if file "$SSH_KEY_FILE" | grep -q "CRLF"; then
        echo -e "${YELLOW}警告: 文件使用Windows换行符 (CRLF)${NC}"
        echo "这可能在某些Linux系统上导致问题"
        echo "建议执行: tr -d '\r' < $SSH_KEY_FILE > ${SSH_KEY_FILE}.unix && mv ${SSH_KEY_FILE}.unix $SSH_KEY_FILE"
    fi
fi

# 测试成功
echo -e "${GREEN}✓ 验证完成: 此SSH密钥可用于GitHub Actions!${NC}"
echo ""
echo -e "${BLUE}如何使用此密钥:${NC}"
echo "1. 复制整个文件内容（包括BEGIN/END行）"
echo "   cat $SSH_KEY_FILE"
echo "2. 将完整内容添加到GitHub项目的Secrets (ALIYUN_SSH_PRIVATE_KEY)"
echo "3. 确保公钥已添加到服务器的authorized_keys文件"
echo ""

# 打印公钥（如果存在）
PUBLIC_KEY="${SSH_KEY_FILE}.pub"
if [ -f "$PUBLIC_KEY" ]; then
    echo -e "${BLUE}对应的公钥 ($PUBLIC_KEY):${NC}"
    cat "$PUBLIC_KEY"
    echo ""
    echo -e "${BLUE}将此公钥添加到服务器的~/.ssh/authorized_keys文件${NC}"
else
    echo -e "${YELLOW}未找到对应的公钥文件: $PUBLIC_KEY${NC}"
    echo "可以使用以下命令从私钥生成公钥:"
    echo "ssh-keygen -y -f $SSH_KEY_FILE > ${SSH_KEY_FILE}.pub"
fi

exit 0 