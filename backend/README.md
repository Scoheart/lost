# 住宅小区互助寻物系统 - 后端

这是住宅小区互助寻物系统的后端部分，使用Spring Boot、Spring Security、MyBatis和MySQL构建。

## 技术栈

- **框架**: Spring Boot 3.2.3
- **安全**: Spring Security, JWT 认证
- **数据存储**: MySQL
- **缓存**: Redis
- **ORM 框架**: MyBatis
- **API**: RESTful API

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
```

## 开发环境设置

### 先决条件

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 数据库设置

1. 创建MySQL数据库:

```sql
CREATE DATABASE lost_and_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行`src/main/resources/schema.sql`创建表结构和初始数据。

### 修改配置

在`src/main/resources/application.yml`中修改以下配置：

- 数据库连接设置
- Redis连接设置
- JWT密钥

### 构建和运行

```bash
mvn clean package
java -jar target/lostandfound-0.0.1-SNAPSHOT.jar
```

或者使用Maven Spring Boot插件:

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080/api 上运行。

## API 端点

### 认证

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户管理

- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/profile` - 更新用户资料
- `PUT /api/users/change-password` - 修改密码

### 寻物启事

- `GET /api/lost-items` - 获取寻物启事列表
- `GET /api/lost-items/{id}` - 获取寻物启事详情
- `POST /api/lost-items` - 发布寻物启事
- `PUT /api/lost-items/{id}` - 更新寻物启事
- `DELETE /api/lost-items/{id}` - 删除寻物启事
- `GET /api/lost-items/{id}/comments` - 获取寻物启事评论
- `POST /api/lost-items/{id}/comments` - 添加评论

### 失物招领

- `GET /api/found-items` - 获取失物招领列表
- `GET /api/found-items/{id}` - 获取失物招领详情
- `POST /api/found-items` - 发布失物招领
- `PUT /api/found-items/{id}` - 更新失物招领
- `DELETE /api/found-items/{id}` - 删除失物招领
- `GET /api/found-items/{id}/comments` - 获取失物招领评论
- `POST /api/found-items/{id}/comments` - 添加评论

### 公告管理

- `GET /api/announcements` - 获取公告列表
- `GET /api/announcements/{id}` - 获取公告详情
- `POST /api/announcements` - 发布公告 (需管理员权限)
- `PUT /api/announcements/{id}` - 更新公告 (需管理员权限)
- `DELETE /api/announcements/{id}` - 删除公告 (需管理员权限)

### 邻里论坛

- `GET /api/forum` - 获取帖子列表
- `GET /api/forum/{id}` - 获取帖子详情
- `POST /api/forum` - 发布帖子
- `PUT /api/forum/{id}` - 更新帖子
- `DELETE /api/forum/{id}` - 删除帖子
- `GET /api/forum/{id}/comments` - 获取帖子评论
- `POST /api/forum/{id}/comments` - 添加评论

### 管理功能

- `GET /api/admin/reports` - 获取举报列表 (需管理员权限)
- `PUT /api/admin/reports/{id}` - 处理举报 (需管理员权限)
- `GET /api/admin/claims` - 获取认领列表 (需管理员权限)
- `PUT /api/admin/claims/{id}` - 处理认领申请 (需管理员权限)
- `GET /api/admin/users` - 获取用户列表 (需系统管理员权限)
- `PUT /api/admin/users/{id}/role` - 修改用户角色 (需系统管理员权限)
- `PUT /api/admin/users/{id}/enable` - 启用用户 (需系统管理员权限)
- `PUT /api/admin/users/{id}/disable` - 禁用用户 (需系统管理员权限)
- `PUT /api/admin/users/{id}/lock` - 锁定用户 (需系统管理员权限)
- `PUT /api/admin/users/{id}/unlock` - 解锁用户 (需系统管理员权限)

## 安全架构

- JWT令牌认证
- 基于角色的访问控制
- 密码加密存储
- 请求验证 