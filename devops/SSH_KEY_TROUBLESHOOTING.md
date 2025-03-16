# SSH密钥故障排除指南

在使用GitHub Actions部署到阿里云ECS时，如果遇到SSH密钥相关错误（如 `Error loading key "(stdin)": error in libcrypto`），请参考本指南解决问题。

## 常见错误

### 错误：`Error loading key "(stdin)": error in libcrypto`

这通常表示SSH密钥格式不正确或已损坏。可能的原因包括：

1. 密钥格式不是标准的OpenSSH格式
2. 复制密钥到GitHub Secrets时丢失了换行符
3. 密钥被以错误的格式保存（如Windows文本格式）
4. 提供了公钥而非私钥

## 解决方案

### 1. 生成新的SSH密钥

```bash
# 生成ED25519密钥（推荐）
ssh-keygen -t ed25519 -C "your_email@example.com"

# 或生成RSA密钥（如需更广泛兼容性）
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

按照提示操作，密钥将保存在 `~/.ssh/id_ed25519`（或 `~/.ssh/id_rsa`）。

### 2. 正确复制私钥

```bash
# 显示私钥内容
cat ~/.ssh/id_ed25519  # 或 cat ~/.ssh/id_rsa

# 确保复制整个输出，包括BEGIN和END行及所有换行符
```

私钥格式应类似于：

```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEA...（中间内容很长）...
...
-----END OPENSSH PRIVATE KEY-----
```

### 3. 正确添加到GitHub Secrets

1. 在GitHub项目页面，进入 `Settings` > `Secrets and variables` > `Actions`
2. 点击 `New repository secret`
3. 名称填写 `ALIYUN_SSH_PRIVATE_KEY`
4. 值中粘贴**完整的**私钥内容（包括BEGIN和END行及所有换行符）
5. 点击 `Add secret`

### 4. 检查服务器的授权密钥

确保SSH公钥已添加到服务器的授权密钥中：

```bash
# 在本地生成公钥的内容
cat ~/.ssh/id_ed25519.pub  # 或 cat ~/.ssh/id_rsa.pub

# 将输出复制到服务器的 ~/.ssh/authorized_keys 文件
```

在服务器上执行：

```bash
# 确保权限正确
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

### 5. 测试连接

在修改GitHub Secrets后，可以手动触发工作流进行测试：

1. 进入GitHub项目的 `Actions` 标签
2. 选择 `部署到阿里云ECS` 工作流
3. 点击 `Run workflow` 按钮
4. 从下拉菜单选择分支（通常是 `main` 或 `master`）
5. 点击 `Run workflow` 开始测试

## 其他常见问题

### 密钥有密码保护

如果您的SSH密钥设置了密码保护，GitHub Actions无法自动使用它。请生成一个不带密码保护的新密钥专门用于CI/CD。

### SELinux或权限问题

如果SSH连接成功但部署失败，可能是服务器上的SELinux或权限问题。请参考 `server-init.sh` 和相关部署脚本中的SELinux处理部分。

---

如有任何问题，请联系项目维护者或参考GitHub Actions和SSH官方文档。 