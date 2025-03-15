# Spring Boot 应用自动部署到阿里云 CI/CD 设置指南

本文档介绍如何设置 CI/CD 流程，使得每次将代码推送到 Git 仓库时，自动构建并部署 Spring Boot 应用到阿里云 ECS 实例。

## 方案选择

我们提供了以下三种 CI/CD 方案供选择：

1. **GitHub Actions** (推荐): 如果您使用 GitHub 托管代码
2. **阿里云云效（Cloud Toolkit）**: 阿里云原生 CI/CD 服务
3. **Jenkins**: 如果您已有 Jenkins 服务器

根据您的具体情况选择最适合的方案。

## 方案一：GitHub Actions 部署到阿里云 ECS

### 前提条件

1. GitHub 仓库已设置
2. 阿里云 ECS 实例已创建并可通过 SSH 访问
3. 已在 ECS 上安装 JDK 17 或以上版本

### 设置步骤

1. **配置 GitHub Secrets**

   在 GitHub 仓库中添加以下 secrets:
   - `ALIYUN_SSH_PRIVATE_KEY`: SSH 私钥
   - `ALIYUN_HOST`: 阿里云 ECS 实例的公网 IP 或域名
   - `ALIYUN_USER`: 用于 SSH 连接的用户名（通常是 `root` 或 `admin`）
   - `DEPLOY_PATH`: 应用部署路径（例如 `/home/admin/app`）

2. **上传部署脚本到服务器**

   首次设置时，您需要手动将 `deploy.sh` 脚本上传到服务器的部署目录：
   
   ```bash
   scp backend/deploy.sh username@your-aliyun-ecs-ip:/path/to/deploy/directory/
   ```

3. **推送代码触发部署**

   只要向 `main` 或 `master` 分支推送代码，且变更包含 `backend` 目录中的文件，就会自动触发部署流程。

## 方案二：阿里云云效（Cloud Toolkit）

### 设置步骤

1. **开通阿里云云效服务**
   
   访问 [阿里云云效](https://www.aliyun.com/product/yunxiao) 并开通服务。

2. **创建流水线**

   在云效控制台中创建新的流水线，上传或粘贴 `aliyun-cloudtoolkit.yml` 配置文件内容。

3. **配置环境变量**

   在流水线设置中，配置以下环境变量：
   - `TARGET_SERVER_ID`: 云效平台中配置的目标服务器ID

4. **添加代码源**

   将您的 Git 仓库添加为代码源，并配置自动触发规则。

## 方案三：Jenkins

### 前提条件

1. Jenkins 服务器已设置
2. 已安装必要的 Jenkins 插件: Maven Integration, SSH Agent, Pipeline

### 设置步骤

1. **添加凭据**

   在 Jenkins 凭据管理中添加:
   - `aliyun-server-credentials`: 阿里云服务器的用户名和密码（或IP地址）
   - `aliyun-ssh-key`: 用于SSH连接的私钥

2. **创建 Pipeline 任务**

   创建新的 Pipeline 任务，选择 "Pipeline script from SCM" 并设置您的 Git 仓库。
   指定 Jenkinsfile 路径为 `backend/Jenkinsfile`。

3. **配置构建触发器**

   设置基于 Git webhook 的触发器，以便在代码推送时自动构建。

## 部署脚本说明

`deploy.sh` 脚本执行以下操作：

1. 检查应用是否已在运行，如果是则优雅停止
2. 启动新的应用实例，使用 nohup 确保在 SSH 会话结束后继续运行
3. 将进程 ID 保存到 PID 文件
4. 检查应用是否成功启动

您可以根据需要修改此脚本，例如调整 JVM 参数、添加环境变量等。

## 常见问题排查

- **部署失败**：检查服务器上的日志文件 (`$DEPLOY_PATH/logs/app.log`)
- **无法连接服务器**：验证 SSH 凭据和服务器防火墙设置
- **应用无法启动**：检查 JDK 版本、应用配置和端口冲突

## 更多资源

- [阿里云云效文档](https://help.aliyun.com/document_detail/153448.html)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Jenkins Pipeline 文档](https://www.jenkins.io/doc/book/pipeline/) 