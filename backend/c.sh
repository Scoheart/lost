# 获取用户列表
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/admin/users?page=1&size=10

# 获取单个用户
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/admin/users/1

# 创建新用户
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"username":"newuser","email":"newuser@example.com","password":"password123","realName":"新用户","phone":"13800138000","role":"resident"}' \
  http://localhost:8080/api/admin/users

# 更新用户信息
curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"email":"updated@example.com","phone":"13900139000","realName":"更新的名字"}' \
  http://localhost:8080/api/admin/users/1

# 更新用户状态
curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"isEnabled":true,"isLocked":false}' \
  http://localhost:8080/api/admin/users/1/status

# 重置用户密码
curl -X PUT -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/admin/users/1/reset-password?newPassword=newpass123

# 删除用户
curl -X DELETE -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/admin/users/1