FROM node:20-alpine AS builder
WORKDIR /app

# 分步复制依赖文件，优先安装依赖，利用Docker缓存
COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm@9 && pnpm config set registry ${NPM_REGISTRY} && pnpm install --frozen-lockfile

# 复制源代码并构建
COPY . .
RUN pnpm build:prod

FROM nginx:alpine
# 从builder阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 3333

# 以默认模式启动nginx
CMD ["nginx", "-g", "daemon off;"]
