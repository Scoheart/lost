# 阿里云ECS一键部署指南

本目录包含了将项目自动化部署到阿里云ECS服务器所需的所有脚本和配置文件。

## 目录结构

```
devops/
  ├── scripts/           # 部署脚本
  │   ├── server-init.sh       # 服务器初始化脚本
  │   ├── init-database.sql    # 数据库初始化SQL脚本
  │   ├── init-database.sh     # 数据库初始化执行脚本
  │   ├── deploy-backend.sh    # 后端部署脚本
  │   └── setup-nginx.sh       # Nginx配置脚本
  ├── config/            # 配置文件
  │   ├── application-prod.yml # 后端生产环境配置
  │   ├── nginx.conf.template  # Nginx配置模板
  │   └── .env.production      # 前端生产环境变量
  └── workflows/         # GitHub Actions工作流
      └── deploy-to-aliyun.yml # 自动部署工作流
```

## 快速开始

### 1. 配置GitHub Secrets

在GitHub仓库中设置以下Secrets:

| Secret名称 | 描述 | 示例值 |
|------------|------|--------|
| `ALIYUN_SSH_PRIVATE_KEY` | SSH私钥，用于连接服务器 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `ALIYUN_USER` | 服务器用户名 | `root` |
| `ALIYUN_HOST` | 服务器IP地址或域名 | `123.123.123.123` |
| `DEPLOY_PATH` | 后端部署路径 | `/opt/lostandfound/backend` |
| `FRONTEND_DEPLOY_PATH` | 前端部署路径 | `/var/www/html/lostandfound` |
| `DB_PASSWORD` | MySQL数据库密码 | `your_secure_password` |
| `DOMAIN_NAME` | 可选: 域名 | `example.com` |

### 2. 激活GitHub Actions工作流

1. 将`devops/workflows/deploy-to-aliyun.yml`文件复制到项目根目录下的`.github/workflows/`目录中:

```bash
mkdir -p .github/workflows
cp devops/workflows/deploy-to-aliyun.yml .github/workflows/
```

2. 提交并推送更改:

```bash
git add .github/workflows/deploy-to-aliyun.yml
git commit -m "添加自动部署工作流"
git push origin main
```

### 3. 部署流程

工作流将自动执行以下步骤:

1. **检查环境**: 验证所有必要的Secrets是否已设置
2. **服务器检查**: 检查服务器是否已安装所需软件
3. **自动初始化**: 如果服务器是全新的，自动安装必要的软件
4. **数据库初始化**: 创建数据库并初始化表结构
5. **后端构建**: 使用Maven构建Spring Boot应用
6. **前端构建**: 使用pnpm构建Vue前端应用
7. **部署**: 将应用部署到服务器并配置Nginx
8. **验证**: 验证部署是否成功

## 数据库初始化

本部署方案包含完整的数据库自动初始化功能，初始化过程会：

1. 创建`lost_and_found`数据库
2. 创建所有必要的表结构，无外键约束：
   - `users`: 用户表
   - `lost_items`: 寻物启事表
   - `found_items`: 失物招领表
   - `claim_applications`: 认领申请表
   - `comments`: 留言表
   - `announcements`: 公告表
   - `forum_posts`: 论坛帖子表
   - `forum_comments`: 论坛评论表
   - `reports`: 举报表

> **设计说明**: 所有数据表之间的关联关系和约束在应用程序业务层实现，数据库层不设置外键约束，以提高性能和灵活性。

### 手动执行数据库初始化

如果需要单独执行数据库初始化，可以使用以下命令：

```bash
# 上传初始化脚本
scp devops/scripts/init-database.sql user@your-server-ip:/tmp/
scp devops/scripts/init-database.sh user@your-server-ip:/tmp/

# 连接到服务器并执行
ssh user@your-server-ip
cd /tmp
chmod +x init-database.sh
sudo ./init-database.sh your_db_password init-database.sql
```

## 手动服务器初始化（如有需要）

如果您需要手动初始化服务器，可以执行以下步骤:

```bash
# 1. 上传初始化脚本到服务器
scp devops/scripts/server-init.sh user@your-server-ip:/tmp/
scp devops/scripts/init-database.sql user@your-server-ip:/tmp/
scp devops/scripts/init-database.sh user@your-server-ip:/tmp/

# 2. 连接到服务器
ssh user@your-server-ip

# 3. 执行初始化脚本
cd /tmp
chmod +x server-init.sh
chmod +x init-database.sh
sudo ./server-init.sh your_db_password /opt/lostandfound/backend /var/www/html/lostandfound
```

## 手动部署（仅在GitHub Actions不可用时使用）

### 后端部署:

```bash
# 构建后端
cd backend
mvn clean package -DskipTests

# 上传文件到服务器
scp target/*.jar user@your-server-ip:/opt/lostandfound/backend/app.jar
scp devops/config/application-prod.yml user@your-server-ip:/opt/lostandfound/backend/
scp devops/scripts/deploy-backend.sh user@your-server-ip:/opt/lostandfound/backend/deploy.sh

# 执行部署脚本
ssh user@your-server-ip 'cd /opt/lostandfound/backend && chmod +x deploy.sh && ./deploy.sh'
```

### 前端部署:

```bash
# 构建前端
cd frontend
pnpm install
pnpm run build:prod

# 上传到服务器
cd dist
tar -czvf ../dist.tar.gz .
scp ../dist.tar.gz user@your-server-ip:/var/www/html/lostandfound/

# 在服务器上解压
ssh user@your-server-ip 'cd /var/www/html/lostandfound && rm -rf * && tar -xzvf dist.tar.gz && rm dist.tar.gz'

# 配置Nginx
scp devops/scripts/setup-nginx.sh user@your-server-ip:/tmp/
ssh user@your-server-ip 'chmod +x /tmp/setup-nginx.sh && sudo /tmp/setup-nginx.sh /var/www/html/lostandfound your-server-ip'
```

## 故障排除指南

### SSH密钥问题

部署过程中最常见的问题是SSH密钥格式错误。如果遇到以下错误：

```
Error loading key "(stdin)": error in libcrypto
```

请参考 `SSH_KEY_TROUBLESHOOTING.md` 文档获取详细的解决方案。

也可以使用我们提供的验证工具来检查您的SSH密钥是否格式正确：

```bash
# 用法
./scripts/verify-ssh-key.sh <私钥文件路径>

# 例如
./scripts/verify-ssh-key.sh ~/.ssh/id_ed25519
```

该工具将执行全面检查并提供具体建议。

### SELinux问题

在CentOS/RHEL/Alibaba Cloud Linux系统上，SELinux可能会导致部署问题。我们的脚本会尝试自动处理SELinux上下文和策略，但如果遇到权限问题，可能需要手动调整SELinux设置：

```bash
# 检查SELinux状态
sestatus

# 允许Nginx连接到后端服务
setsebool -P httpd_can_network_connect 1

# 为Web内容设置正确上下文
chcon -R -t httpd_sys_content_t /var/www/html/lostandfound
```

### 数据库问题

- **数据库初始化失败**: 查看MySQL错误日志 `sudo cat /var/log/mysql/error.log`
- **无法连接数据库**: 检查MySQL服务状态 `sudo systemctl status mysql`
- **表结构不完整**: 手动执行SQL脚本 `mysql -u root -p < init-database.sql`

### 服务器初始化问题

- **Java安装失败**: 尝试手动安装 `sudo apt-get install -y openjdk-17-jdk`
- **MySQL配置问题**: 检查MySQL服务状态 `sudo systemctl status mysql`
- **Redis连接失败**: 检查Redis服务状态 `sudo systemctl status redis-server`

### 部署问题

- **SSH连接失败**: 确保SSH私钥格式正确并具有正确的权限
- **无法上传文件**: 检查目标目录的权限和磁盘空间
- **应用启动失败**: 查看日志文件 `cat /opt/lostandfound/backend/logs/app.log`
- **Nginx配置错误**: 检查Nginx配置 `sudo nginx -t`

### 应用运行问题

- **前端无法访问**: 检查Nginx状态 `sudo systemctl status nginx`
- **API不可用**: 检查后端服务状态 `ps aux | grep java`
- **数据库错误**: 检查数据库连接 `mysql -u root -p -e "SHOW DATABASES;"`

## 安全建议

1. **使用专用数据库用户**: 避免使用root用户连接数据库
2. **启用防火墙**: 只允许必要的端口访问
3. **设置SSL**: 配置HTTPS以加密传输数据
4. **定期备份**: 设置自动备份数据库和应用数据
5. **修改默认密码**: 初始化后立即修改默认管理员密码

## 维护提示

- **查看应用日志**: `tail -f /opt/lostandfound/backend/logs/app.log`
- **重启应用**: `cd /opt/lostandfound/backend && ./deploy.sh`
- **重启Nginx**: `sudo systemctl restart nginx`
- **更新前端**: 只需推送到GitHub，CI/CD流程将自动更新 