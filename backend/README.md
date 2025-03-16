# 住宅小区互助寻物系统 - 后端

这是住宅小区互助寻物系统的后端部分，使用Spring Boot、Spring Security、MyBatis和MySQL构建。

## 技术栈

- **框架**: Spring Boot 3.2.3
- **安全**: Spring Security 6.x, JWT 认证
- **数据存储**: MySQL 8.0
- **缓存**: Redis 6.0+
- **ORM 框架**: MyBatis
- **API 文档**: Swagger/OpenAPI
- **构建工具**: Maven 3.8+
- **环境支持**: 支持开发和生产环境配置

## 项目结构

```
src/main/java/com/community/lostandfound/
├── config                  # 全局配置
├── controller              # REST API 控制器
├── dto                     # 数据传输对象
├── entity                  # 领域模型
├── exception               # 异常处理
├── repository              # 数据访问层
├── security                # 安全配置
├── service                 # 业务逻辑层
│   └── impl                # 服务实现
└── LostAndFoundApplication.java  # 主类

src/main/resources/
├── application.yml         # 公共配置
├── application-dev.yml     # 开发环境配置
├── application-prod.yml    # 生产环境配置
├── mapper/                 # MyBatis XML 映射文件
└── schema.sql              # 数据库初始化脚本
```

## 多环境配置

系统支持多环境配置，使用Spring Profiles实现:

- **开发环境** (`application-dev.yml`): 用于本地开发，包含详细日志，不需要设置文件base-url
- **生产环境** (`application-prod.yml`): 用于生产部署，包含优化的数据库连接池、文件访问URL配置等

### 切换环境配置

设置 `spring.profiles.active` 参数:

```bash
# 方法1: 命令行参数
java -jar -Dspring.profiles.active=prod app.jar

# 方法2: 环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

## 开发环境设置

### 先决条件

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+ (可选，用于缓存)

### 数据库设置

1. 创建MySQL数据库:

```sql
CREATE DATABASE lost_and_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行`src/main/resources/schema.sql`创建表结构和初始数据。

### 修改配置

在`src/main/resources/application-dev.yml`中修改以下配置:

- 数据库连接设置 (URL, 用户名, 密码)
- Redis连接设置 (如果使用)
- 文件上传目录 `file.upload.dir`

### 构建和运行 (开发环境)

```bash
# 克隆项目
git clone [repository_url]
cd backend

# 编译打包
mvn clean package -DskipTests

# 运行应用 (开发环境)
java -jar target/lostandfound-0.0.1-SNAPSHOT.jar

# 或者使用Maven Spring Boot插件
mvn spring-boot:run
```

应用将在 http://localhost:8080/api 上运行。

## 部署指南 (生产环境)

### 使用部署脚本

1. 将编译好的JAR文件和`deploy.sh`上传到服务器:

```bash
scp target/lostandfound-0.0.1-SNAPSHOT.jar user@server:/path/to/deploy/app.jar
scp deploy.sh user@server:/path/to/deploy/
```

2. 运行部署脚本:

```bash
cd /path/to/deploy
chmod +x deploy.sh
./deploy.sh
```

### 环境变量设置 (生产环境)

生产环境推荐通过环境变量设置敏感信息:

```bash
export SPRING_PROFILES_ACTIVE=prod
export FILE_BASE_URL=https://your-domain.com/api
export DB_PASSWORD=your_secure_password
export REDIS_PASSWORD=your_redis_password
```

### 文件上传配置

系统支持图片上传，生产环境需要设置基础URL确保文件可访问:

```yaml
file:
  upload:
    dir: /path/to/uploads  # 绝对路径
    base-url: https://your-domain.com/api
```

## API 端点

### 认证

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户管理

- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/profile` - 更新用户资料
- `PUT /api/users/change-password` - 修改密码
- `POST /api/users/avatar` - 上传用户头像

### 寻物启事

- `GET /api/lost-items` - 获取寻物启事列表 (支持分页、筛选和排序)
- `GET /api/lost-items/{id}` - 获取寻物启事详情
- `POST /api/lost-items` - 发布寻物启事 (支持多图片上传)
- `PUT /api/lost-items/{id}` - 更新寻物启事
- `DELETE /api/lost-items/{id}` - 删除寻物启事
- `GET /api/lost-items/my-posts` - 获取我发布的寻物启事
- `PUT /api/lost-items/{id}/status` - 更新寻物启事状态

### 失物招领

- `GET /api/found-items` - 获取失物招领列表 (支持分页、筛选和排序)
- `GET /api/found-items/{id}` - 获取失物招领详情
- `POST /api/found-items` - 发布失物招领 (支持多图片上传)
- `PUT /api/found-items/{id}` - 更新失物招领
- `DELETE /api/found-items/{id}` - 删除失物招领
- `GET /api/found-items/my-posts` - 获取我发布的失物招领
- `PUT /api/found-items/{id}/status` - 更新失物招领状态

### 认领申请

- `POST /api/claims` - 提交认领申请
- `GET /api/claims/my-claims` - 获取我提交的认领申请
- `GET /api/claims/my-item-claims` - 获取我物品的认领申请
- `PUT /api/claims/{id}/status` - 处理认领申请

### 评论

- `POST /api/comments` - 添加评论
- `GET /api/comments` - 获取评论列表
- `DELETE /api/comments/{id}` - 删除评论

### 公告管理

- `GET /api/announcements` - 获取公告列表
- `GET /api/announcements/{id}` - 获取公告详情
- `POST /api/announcements` - 发布公告 (需管理员权限)
- `PUT /api/announcements/{id}` - 更新公告 (需管理员权限)
- `DELETE /api/announcements/{id}` - 删除公告 (需管理员权限)

### 居民信息管理

- `GET /api/residents` - 获取居民列表 (需管理员权限)
- `POST /api/residents` - 添加居民信息
- `PUT /api/residents/{id}` - 更新居民信息
- `DELETE /api/residents/{id}` - 删除居民信息

### 管理功能

- `GET /api/admin/users` - 获取用户列表 (需系统管理员权限)
- `PUT /api/admin/users/{id}/role` - 修改用户角色 (需系统管理员权限)
- `PUT /api/admin/users/{id}/enable` - 启用用户 (需系统管理员权限)
- `PUT /api/admin/users/{id}/disable` - 禁用用户 (需系统管理员权限)
- `GET /api/admin/dashboard` - 获取管理仪表盘数据 (需管理员权限)
- `GET /api/admin/settings` - 获取系统设置 (需管理员权限)
- `PUT /api/admin/settings` - 更新系统设置 (需管理员权限)

### 文件上传

- `POST /api/files/upload` - 通用文件上传
- `POST /api/files/upload/item-images` - 上传物品图片
- `POST /api/files/upload/avatar` - 上传用户头像

## 安全架构

- JWT令牌认证，默认有效期24小时
- 基于角色的访问控制 (RBAC)，支持用户、居民管理员、系统管理员三级权限
- 密码加密存储 (BCrypt)
- 请求参数验证 (@Valid)
- 防止SQL注入 (预编译语句)
- 防止XSS (输入验证和输出编码)

## 日志系统

- 使用SLF4J + Logback日志框架
- 开发环境：详细日志，级别为DEBUG
- 生产环境：精简日志，级别为INFO/WARN
- 错误日志记录请求信息，便于追踪问题

## 性能优化

- 生产环境数据库连接池配置优化 (HikariCP)
- 实体类使用Lombok减少模板代码
- 缓存机制支持 (可使用Redis)
- 分页查询优化，防止大数据量查询
- 多环境配置，便于开发和生产环境切换 