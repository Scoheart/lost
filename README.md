# 小区失物招领平台

## 系统环境配置指南

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
   java -jar -Dspring.profiles.active=dev backend.jar
   ```

2. **通过环境变量**：
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   java -jar backend.jar
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
npm run dev

# 生产环境构建
npm run build:prod
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

### 注意事项

1. 特定环境的配置文件不能包含`spring.profiles.active`属性
2. 环境特定的配置会覆盖主配置文件中的同名配置
3. 敏感信息（如密码、密钥）应使用环境变量传入
4. 前端环境变量必须以`VITE_`开头才能在客户端代码中访问 