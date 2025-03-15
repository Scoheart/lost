#!/bin/bash

# API 端点
URL="http://localhost:3333/api/announcements/admin"

# 请求头
HEADERS=(
    "-H" "Accept: application/json, text/plain, */*"
    "-H" "Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7"
    "-H" "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJpZCI6MywiZW1haWwiOiJhZG1pbjFAYWRtaW4xLmNvbSIsInVzZXJuYW1lIjoiYWRtaW4xIiwic3ViIjoiYWRtaW4xIiwiaWF0IjoxNzQyMDEyMzc1LCJleHAiOjE3NDIwOTg3NzV9.oytnrSF_2HiMrGdN1e8iN0DCptK0JXsG6v8wNyAuyww"
    "-H" "Connection: keep-alive"
    "-H" "Content-Type: application/json"
    "-H" "Origin: http://localhost:3333"
    "-H" "Referer: http://localhost:3333/admin/announcements"
    "-H" "Sec-Fetch-Dest: empty"
    "-H" "Sec-Fetch-Mode: cors"
    "-H" "Sec-Fetch-Site: same-origin"
    "-H" "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
    "-H" "sec-ch-ua: \"Not(A:Brand\";v=\"99\", \"Google Chrome\";v=\"133\", \"Chromium\";v=\"133\""
    "-H" "sec-ch-ua-mobile: ?0"
    "-H" "sec-ch-ua-platform: \"macOS\""
)

# 生成随机字符串的函数（修复区域设置问题）
generate_random_string() {
    LC_ALL=C tr -dc 'a-zA-Z0-9' </dev/urandom | fold -w 20 | head -n 1
}

# 生成随机布尔值的函数
generate_random_boolean() {
    if [ $((RANDOM % 2)) -eq 0 ]; then
        echo "true"
    else
        echo "false"
    fi
}

# 发送 100 条请求
for i in {1..100}; do
    # 生成随机数据
    TITLE=$(generate_random_string)
    CONTENT=$(generate_random_string)
    IS_STICKY=$(generate_random_boolean)
    STATUS="published"

    # 构造 JSON 数据
    DATA="{\"title\":\"$TITLE\",\"content\":\"$CONTENT\",\"isSticky\":$IS_STICKY,\"status\":\"$STATUS\"}"

    # 发送 curl 请求
    echo "Sending request $i: $DATA"
    curl -X POST "$URL" "${HEADERS[@]}" --data-raw "$DATA"

    # 可选：添加延迟以避免服务器过载
    sleep 1
done

echo "All requests sent!"
