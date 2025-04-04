<template>
  <div class="users-manage-container">
    <div class="page-header">
      <h2 class="page-title">系统用户管理</h2>
      <div class="action-buttons">
        <el-dropdown @command="openAddUserDialog">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            添加用户
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin">添加小区管理员</el-dropdown-item>
              <el-dropdown-item command="resident">添加居民用户</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 搜索和过滤 -->
    <div class="filters-container">
      <el-input
        v-model="searchQuery"
        placeholder="搜索用户（用户名、邮箱、手机号）"
        class="search-input"
        :prefix-icon="Search"
        clearable
        @clear="loadUsers"
      />
      <el-select
        v-model="roleFilter"
        placeholder="用户角色"
        clearable
        @change="loadUsers"
        class="role-filter"
      >
        <el-option label="全部用户" value="" />
        <el-option label="系统管理员" value="sysadmin" />
        <el-option label="小区管理员" value="admin" />
        <el-option label="居民用户" value="resident" />
      </el-select>
      <el-select
        v-model="statusFilter"
        placeholder="账号状态"
        clearable
        @change="loadUsers"
        class="status-filter"
      >
        <el-option label="全部状态" value="" />
        <el-option label="正常" value="active" />
        <el-option label="锁定" value="locked" />
      </el-select>
      <el-button type="primary" @click="loadUsers">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 用户列表 -->
    <el-card shadow="never" class="users-table-card">
      <el-table
        v-loading="loading"
        :data="users"
        border
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="280" />
        <el-table-column prop="realName" label="真实姓名" min-width="120">
          <template #default="scope">
            {{ scope.row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="密码" min-width="120">
          <template #default="scope">
            <div class="password-field">
              <span>••••••••</span>
              <el-button
                type="primary"
                size="small"
                @click="resetPassword(scope.row)"
                link
              >
                重置
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="scope">
            <el-tag
              :type="getRoleTagType(scope.row.role)"
              effect="plain"
            >
              {{ getRoleDisplayName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120">
          <template #default="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="住址" min-width="150">
          <template #default="scope">
            {{ scope.row.address || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="getStatusTagType(scope.row)"
              effect="plain"
            >
              {{ getStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button
                type="primary"
                size="small"
                @click="openEditDialog(scope.row)"
                text
              >
                编辑
              </el-button>
              <el-button
                :type="scope.row.isLocked ? 'warning' : 'success'"
                size="small"
                @click="toggleUserStatus(scope.row)"
                text
              >
                {{ scope.row.isLocked ? '解锁' : '锁定' }}
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="resetPassword(scope.row)"
                text
              >
                重置密码
              </el-button>
              <el-button
                v-if="scope.row.role !== 'sysadmin' || currentUser.id !== scope.row.id"
                type="danger"
                size="small"
                @click="deleteUser(scope.row)"
                text
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle()"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="formRules"
        label-width="80px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="userForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="小区管理员" value="admin" />
            <el-option label="居民用户" value="resident" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <template v-if="!editingUser">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="userForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="userForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser" :loading="submitting">
            {{ editingUser ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Edit, Delete, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { format } from 'date-fns'
import apiClient from '@/utils/api'
import { useUserStore } from '@/stores/user'

// 接口定义
interface User {
  id: number
  username: string
  email: string
  phone: string
  realName: string
  address: string
  role: 'resident' | 'admin' | 'sysadmin'
  avatar: string
  isLocked: boolean
  createdAt: string
  updatedAt: string
}

// 状态管理
const userStore = useUserStore()
const currentUser = computed(() => userStore.user!)
const loading = ref(false)
const submitting = ref(false)
const users = ref<User[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const editingUser = ref<User | null>(null)
const userFormRef = ref<FormInstance>()
const addingUserRole = ref<'resident' | 'admin' | 'sysadmin' | null>(null)

// 表单数据
const userForm = reactive({
  id: 0,
  username: '',
  email: '' as string | null,
  role: 'resident' as 'resident' | 'admin' | 'sysadmin',
  phone: '',
  realName: '',
  address: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  phone: [
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' },
    { max: 200, message: '地址长度不能超过200个字符', trigger: 'blur' }
  ],
  password: [
    { required: !editingUser.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !editingUser.value, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
})

// 生命周期钩子
onMounted(() => {
  loadUsers()
})

// 方法定义
const loadUsers = async () => {
  loading.value = true;
  try {
    // 将前端的status转换为后端API所需参数
    let isLocked = undefined;

    if (statusFilter.value === 'active') {
      isLocked = false;
    } else if (statusFilter.value === 'locked') {
      isLocked = true;
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined,
      role: roleFilter.value || undefined,
      status: isLocked
    };

    console.log('请求参数:', params);
    const response = await apiClient.get('/admin/users', { params });
    console.log('API响应:', response.data);

    users.value = response.data.data.items;
    total.value = response.data.data.totalItems;
  } catch (error) {
    console.error('Failed to load users:', error);
    ElMessage.error('加载用户数据失败');
  } finally {
    loading.value = false;
  }
}

const formatDateTime = (dateStr: string) => {
  try {
    return format(new Date(dateStr), 'yyyy-MM-dd HH:mm:ss')
  } catch (e) {
    return dateStr || '-'
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadUsers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadUsers()
}

const getRoleDisplayName = (role: string) => {
  switch (role) {
    case 'sysadmin':
      return '系统管理员'
    case 'admin':
      return '小区管理员'
    case 'resident':
      return '居民用户'
    default:
      return '未知角色'
  }
}

const getRoleTagType = (role: string) => {
  switch (role) {
    case 'sysadmin':
      return 'danger'
    case 'admin':
      return 'warning'
    case 'resident':
      return 'info'
    default:
      return ''
  }
}

const getDialogTitle = () => {
  if (editingUser.value) {
    return `编辑用户: ${editingUser.value.username}`
  }

  if (addingUserRole.value === 'admin') {
    return '添加小区管理员'
  }

  if (addingUserRole.value === 'resident') {
    return '添加居民用户'
  }

  return '添加用户'
}

const openAddUserDialog = (role: 'resident' | 'admin' | 'sysadmin') => {
  editingUser.value = null
  addingUserRole.value = role
  resetForm()
  userForm.role = role
  dialogVisible.value = true
}

// 获取单个用户的详细信息
const fetchUserDetails = async (userId: number): Promise<User | null> => {
  try {
    const response = await apiClient.get(`/admin/users/${userId}`)
    return response.data.data
  } catch (error) {
    console.error('Failed to fetch user details:', error)
    ElMessage.error('获取用户详情失败')
    return null
  }
}

// 更新openEditDialog函数，先获取完整的用户信息
const openEditDialog = async (user: User) => {
  loading.value = true
  try {
    // 获取用户详细信息
    const userDetails = await fetchUserDetails(user.id)
    if (!userDetails) {
      throw new Error('无法获取用户详情')
    }

    // 设置编辑状态和表单数据
    editingUser.value = userDetails
    addingUserRole.value = null
    resetForm()
    userForm.id = userDetails.id
    userForm.username = userDetails.username
    userForm.email = userDetails.email
    userForm.role = userDetails.role
    userForm.phone = userDetails.phone || ''
    userForm.realName = userDetails.realName || ''
    userForm.address = userDetails.address || ''
    dialogVisible.value = true
  } catch (error) {
    console.error('Error opening edit dialog:', error)
    ElMessage.error('打开编辑对话框失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  userForm.id = 0
  userForm.username = ''
  userForm.email = ''
  userForm.role = addingUserRole.value || 'resident'
  userForm.phone = ''
  userForm.realName = ''
  userForm.address = ''
  userForm.password = ''
  userForm.confirmPassword = ''
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

const saveUser = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      // 添加用户时验证密码是否一致
      if (!editingUser.value && userForm.password !== userForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }

      submitting.value = true
      try {
        if (editingUser.value) {
          // 编辑现有用户
          const { password, confirmPassword, ...updateData } = userForm

          // 检查是否是更改自己的角色
          if (currentUser.value.id === editingUser.value.id &&
              updateData.role !== currentUser.value.role) {
            throw new Error('不能修改自己的角色类型')
          }

          // 处理空邮箱 - 将空字符串转换为null
          if (!updateData.email || updateData.email.trim() === '') {
            updateData.email = null;
          }

          // 更新用户信息
          await apiClient.put(`/admin/users/${userForm.id}`, updateData)
          ElMessage.success('用户信息更新成功')
        } else {
          // 添加新用户 - 直接使用管理员API
          const { confirmPassword, ...userData } = userForm

          // 处理空邮箱 - 将空字符串转换为null
          if (!userData.email || userData.email.trim() === '') {
            userData.email = null;
          }

          const newUserData = {
            username: userData.username,
            email: userData.email,
            password: userData.password,
            phone: userData.phone || null,
            realName: userData.realName,
            address: userData.address,
            role: userData.role,
            isLocked: false  // 默认不锁定账号
          };

          // 创建用户
          await apiClient.post('/admin/users', newUserData)
          ElMessage.success('用户添加成功')
        }
        dialogVisible.value = false
        loadUsers()
      } catch (error: any) {
        const errorMsg = error.response?.data?.message || error.message || '操作失败，请稍后再试'
        ElMessage.error(errorMsg)
      } finally {
        submitting.value = false
      }
    }
  })
}

const toggleUserStatus = async (user: User) => {
  // 不能锁定自己
  if (user.id === currentUser.value.id) {
    ElMessage.warning('不能修改当前登录账号的状态')
    return
  }

  const action = user.isLocked ? '解锁' : '锁定'
  const newLocked = !user.isLocked

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？${action === '锁定' ? '锁定后该用户将无法登录系统。' : ''}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        title: `${action}确认`
      }
    )

    // 更新状态
    await apiClient.put(`/admin/users/${user.id}/status`, {
      isLocked: newLocked
    })

    ElMessage.success(`用户${action}成功`)
    // 更新本地数据
    user.isLocked = newLocked
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || `${action}失败，请稍后再试`
      ElMessage.error(errorMsg)
    }
  }
}

const deleteUser = async (user: User) => {
  // 不能删除自己
  if (user.id === currentUser.value.id) {
    ElMessage.warning('不能删除当前登录的账号')
    return
  }

  // 系统管理员只能由其他系统管理员删除
  if (user.role === 'sysadmin' && currentUser.value.role !== 'sysadmin') {
    ElMessage.warning('只有系统管理员可以删除其他系统管理员账号')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复。`,
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        title: '删除确认'
      }
    )

    await apiClient.delete(`/admin/users/${user.id}`)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '删除失败，请稍后再试'
      ElMessage.error(errorMsg)
    }
  }
}

// 把服务器数据的isLocked转换为前端界面使用的status
const getStatusFromServerData = (user: User): 'active' | 'locked' => {
  return user.isLocked ? 'locked' : 'active'
}

// 添加重置密码的函数
const resetPassword = async (user: User) => {
  // 不能通过这种方式重置自己的密码
  if (user.id === currentUser.value.id) {
    ElMessage.warning('不能通过此方式重置自己的密码，请到个人设置中修改密码')
    return
  }

  // 弹出密码输入框
  try {
    const { value: newPassword } = await ElMessageBox.prompt(
      '请输入新密码（至少6个字符）',
      '重置用户密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
        inputValidator: (value) => {
          if (!value) {
            return '密码不能为空'
          }
          if (value.length < 6) {
            return '密码长度不能少于6个字符'
          }
          return true
        },
        inputErrorMessage: '请输入有效的密码'
      }
    )

    if (newPassword) {
      await apiClient.put(`/admin/users/${user.id}/reset-password?newPassword=${encodeURIComponent(newPassword)}`)
      ElMessage.success(`用户 ${user.username} 的密码已重置`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '密码重置失败，请稍后再试'
      ElMessage.error(errorMsg)
    }
  }
}

// 新增辅助方法来处理状态显示
const getStatusTagType = (user: User): string => {
  return user.isLocked ? 'danger' : 'success';
}

const getStatusLabel = (user: User): string => {
  return user.isLocked ? '锁定' : '正常';
}
</script>

<style scoped>
.users-manage-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.filters-container {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  flex-wrap: wrap;
}

.password-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-input {
  min-width: 280px;
  max-width: 400px;
}

.role-filter,
.status-filter {
  width: 140px;
}

.users-table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.operation-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px;
  white-space: nowrap;
  min-width: fit-content;
  justify-content: center;
}

.operation-buttons .el-button {
  padding-left: 6px;
  padding-right: 6px;
  margin-left: 0;
  margin-right: 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .action-buttons {
    width: 100%;
  }

  .filters-container {
    flex-direction: column;
    width: 100%;
  }

  .search-input,
  .role-filter,
  .status-filter {
    width: 100%;
    max-width: 100%;
  }
}
</style>
