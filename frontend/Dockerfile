FROM node:20-alpine AS builder

WORKDIR /app

# 一次性复制全部文件
COPY . .

# 安装 pnpm 并安装依赖
RUN npm install -g pnpm@9 && pnpm install

# 构建生产版本
RUN pnpm build:prod
