#!/bin/bash
#
# SSH密钥验证脚本 (精简版)
# 用于验证SSH密钥是否正确配置
#

SSH_DIR="$HOME/.ssh"
AUTH_KEYS_FILE="$SSH_DIR/authorized_keys"

echo "[INFO] 验证SSH密钥配置..."

# 检查SSH目录是否存在
if [ ! -d "$SSH_DIR" ]; then
    echo "[INFO] 创建SSH目录..."
    mkdir -p "$SSH_DIR"
    chmod 700 "$SSH_DIR"
fi

# 检查authorized_keys文件是否存在
if [ ! -f "$AUTH_KEYS_FILE" ]; then
    echo "[INFO] 创建authorized_keys文件..."
    touch "$AUTH_KEYS_FILE"
    chmod 600 "$AUTH_KEYS_FILE"
fi

# 验证公钥是否已添加
if [ $# -eq 1 ]; then
    PUBLIC_KEY="$1"
    
    # 检查公钥是否已存在
    if grep -q "$PUBLIC_KEY" "$AUTH_KEYS_FILE"; then
        echo "[SUCCESS] SSH公钥已配置"
    else
        echo "[INFO] 添加新的SSH公钥..."
        echo "$PUBLIC_KEY" >> "$AUTH_KEYS_FILE"
        echo "[SUCCESS] SSH公钥已添加"
    fi
else
    echo "[INFO] 未提供公钥，仅检查配置"
    
    # 检查是否有任何公钥
    if [ -s "$AUTH_KEYS_FILE" ]; then
        echo "[SUCCESS] authorized_keys文件存在且非空"
    else
        echo "[WARNING] authorized_keys文件为空，请添加SSH公钥"
    fi
fi

echo "[INFO] SSH配置检查完成" 