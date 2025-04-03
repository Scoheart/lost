FROM node:20-alpine AS builder
WORKDIR /app

# 分步复制依赖文件，优先安装依赖，利用Docker缓存
COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm@9 && pnpm install --frozen-lockfile

# 复制源代码并构建
COPY . .
RUN pnpm build:prod

FROM nginx:alpine
# 使用非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /var/cache/nginx /var/log/nginx && \
    chown -R appuser:appgroup /var/cache/nginx /var/log/nginx && \
    # 添加curl用于健康检查
    apk add --no-cache curl

# 从builder阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 设置正确的文件权限
RUN chown -R appuser:appgroup /usr/share/nginx/html

EXPOSE 3333

# 使用非root用户运行
USER appuser

# 使用CMD数组语法启动nginx
CMD ["nginx", "-g", "daemon off;"]
