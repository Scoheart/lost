# 小区失物招领平台

## 项目概述

本项目是一个全栈应用，为社区居民提供失物招领管理服务。系统支持物品丢失登记、招领发布、认领申请处理、评论互动等功能，并提供管理员后台进行内容审核和用户管理。

## 系统架构

本系统采用前后端分离架构，主要由以下部分组成：

- **前端**：基于Vue 3 + TypeScript + Element Plus的SPA应用
- **后端**：Spring Boot微服务架构，提供RESTful API
- **数据库**：MySQL数据库存储结构化数据
- **缓存**：Redis用于会话管理和热点数据缓存
- **Web服务器**：Nginx处理静态资源和反向代理
- **CI/CD**：GitHub Actions自动化构建和部署

## 技术栈

### 前端技术栈

- **框架**: Vue 3
- **语言**: TypeScript
- **状态管理**: Pinia
- **UI组件**: Element Plus
- **构建工具**: Vite 6.x
- **HTTP客户端**: Axios
- **路由**: Vue Router 4

### 后端技术栈

- **框架**: Spring Boot 3.x
- **安全**: Spring Security + JWT
- **ORM**: MyBatis
- **缓存**: Redis
- **数据库**: MySQL 8.x
- **文件存储**: 本地文件系统

### DevOps工具链

- **CI/CD**: GitHub Actions
- **部署脚本**: Shell
- **监控**: 日志管理
- **版本控制**: Git

## 目录结构

```
lost-and-found/
├── .github/workflows/       # GitHub Actions工作流定义
│   └── deploy-to-aliyun.yml # 阿里云部署工作流
├── backend/                 # 后端Spring Boot应用
│   ├── src/                 # Java源代码
│   ├── pom.xml              # Maven依赖配置
│   ├── deploy.sh            # 后端部署脚本
│   └── target/              # 构建输出目录
├── frontend/                # 前端Vue应用
│   ├── src/                 # TypeScript源代码
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── services/        # API服务封装
│   │   ├── stores/          # Pinia状态管理
│   │   ├── utils/           # 工具类
│   │   └── views/           # 页面组件
│   ├── dist/                # 构建输出目录
│   └── vite.config.ts       # Vite构建配置
└── devops/                  # DevOps工具和脚本
    ├── scripts/             # 部署和初始化脚本
    │   ├── init-database.sh # 数据库初始化脚本
    │   ├── init-database.sql# 数据库结构和初始数据
    │   └── server-init.sh   # 服务器初始化脚本
    └── config/              # 环境配置文件
        ├── application-prod.yml # 生产环境配置模板
        └── nginx.conf.template # Nginx配置模板
```

## 环境配置

本项目使用Spring Boot的多环境配置功能，支持在不同的环境中使用不同的配置。

### 配置文件结构

系统使用以下配置文件结构：

- **`application.yml`**：主配置文件，包含所有环境共享的配置和默认环境选择
- **`application-dev.yml`**：开发环境特定配置，适用于本地开发和测试
- **`application-prod.yml`**：生产环境特定配置，适用于服务器部署

### 后端环境配置

#### 激活特定环境

可以通过以下几种方式激活特定环境：

1. **通过命令行参数**：
   ```bash
   java -jar -Dspring.profiles.active=dev backend/target/app.jar
   ```

2. **通过环境变量**：
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   java -jar backend/target/app.jar
   ```

3. **在IDE中设置**：
   在运行配置中添加环境变量`SPRING_PROFILES_ACTIVE=dev`

#### 文件上传URL配置

系统根据不同环境自动选择正确的文件URL生成方式：

1. **开发环境（dev）**：
   - 不设置`file.upload.base-url`，系统会根据当前请求自动生成URL
   - 生成格式：`http://localhost:8080/api/uploads/...`

2. **生产环境（prod）**：
   - 通过`FILE_BASE_URL`环境变量或配置文件指定基础URL
   - 生成格式：`http://your-domain.com/api/uploads/...`

### 前端环境配置

前端使用Vite的环境配置功能，支持不同的构建模式：

#### 配置文件

- **`.env`**：所有环境共享的环境变量
- **`.env.development`**：开发环境特定变量
- **`.env.production`**：生产环境特定变量

#### 构建命令

```bash
# 开发环境启动
pnpm dev

# 生产环境构建
pnpm build:prod
```

### 环境变量列表

#### 后端关键环境变量

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的环境配置 | dev |
| `FILE_BASE_URL` | 文件访问基础URL | http://121.40.52.9/api |
| `FILE_UPLOAD_DIR` | 文件上传目录 | uploads |
| `DB_PASSWORD` | 数据库密码 | shuhao201028 |

#### 前端关键环境变量

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| `VITE_API_BASE_URL` | API基础路径 | /api |
| `VITE_API_TIMEOUT` | 请求超时时间(ms) | 10000 (开发) / 30000 (生产) |
| `BACKEND_URL` | 开发时后端地址 | http://localhost:8080 |

## 开发指南

### 后端开发

#### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

#### 本地开发设置

```bash
# 克隆仓库
git clone https://github.com/your-org/lost-and-found.git
cd lost-and-found

# 编译后端项目
cd backend
mvn clean package -DskipTests

# 初始化数据库（如需要）
cd ../devops/scripts
./init-database.sh

# 启动后端（开发环境）
cd ../../backend
java -jar -Dspring.profiles.active=dev target/app.jar
```

### 前端开发

#### 环境要求

- Node.js 18+
- pnpm 9+

#### 本地开发设置

```bash
# 进入前端目录
cd frontend

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 生产构建
pnpm build:prod
```

## 部署指南

### 手动部署

#### 后端部署

使用项目提供的部署脚本：

```bash
# 进入后端目录
cd backend

# 执行部署脚本
./deploy.sh
```

该脚本会自动：
1. 停止当前运行的应用程序（如有）
2. 设置必要的环境变量
3. 以生产环境模式启动应用程序
4. 保存进程ID以便将来管理
5. 验证应用程序是否成功启动

#### 前端部署

```bash
# 进入前端目录
cd frontend

# 生产构建
pnpm build:prod

# 将构建结果部署到Web服务器
cp -r dist/* /var/www/html/
```

### 使用GitHub Actions自动部署

项目已配置GitHub Actions工作流，可以实现自动部署：

1. 代码推送到`main`分支会触发CI/CD流程
2. 自动构建前后端代码
3. 通过SSH将构建结果部署到阿里云服务器
4. 自动重启应用程序

配置文件位于`.github/workflows/deploy-to-aliyun.yml`。

#### 配置自动部署

要启用自动部署，需要在GitHub仓库设置以下秘密：

1. `SERVER_HOST`: 服务器IP地址
2. `SERVER_USERNAME`: SSH用户名
3. `SERVER_SSH_KEY`: SSH私钥
4. `SERVER_TARGET_DIR`: 部署目标目录

## 服务器配置

### Nginx配置

在生产环境中，建议使用Nginx作为前端静态资源服务器和API代理。配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端 - 静态文件
    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
        expires 7d;
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 上传文件访问
    location /api/uploads {
        alias /path/to/uploads;
        expires 30d;
    }
}
```

## 主要功能模块

- **用户管理**: 注册、登录、个人信息管理
- **寻物启事**: 发布、管理、搜索寻物信息
- **失物招领**: 发布、管理招领信息
- **认领申请**: 提交、审核认领申请
- **评论互动**: 寻物和招领信息的评论功能
- **管理后台**: 内容审核、用户管理、系统设置

## 故障排除

### 常见问题

#### 文件上传问题

如果文件上传后URL不正确，检查：
1. 是否正确配置了`file.upload.base-url`
2. Nginx是否配置了正确的上传目录访问规则
3. 上传目录是否有正确的读写权限

#### 环境配置问题

如果环境配置加载错误：
1. 确保特定环境的配置文件不包含`spring.profiles.active`属性
2. 检查环境变量或命令行参数是否正确设置
3. 验证配置文件格式是否正确

#### 数据库连接问题

数据库连接失败时：
1. 检查数据库连接字符串和凭据
2. 确认数据库服务是否运行
3. 检查网络连接和防火墙设置

## 注意事项

1. 特定环境的配置文件不能包含`spring.profiles.active`属性
2. 环境特定的配置会覆盖主配置文件中的同名配置
3. 敏感信息（如密码、密钥）应使用环境变量传入
4. 前端环境变量必须以`VITE_`开头才能在客户端代码中访问 