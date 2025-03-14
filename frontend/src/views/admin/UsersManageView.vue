<template>
  <div class="users-manage-container">
    <div class="page-header">
      <h2 class="page-title">系统用户管理</h2>
      <div class="action-buttons">
        <el-button type="success" @click="openAddUserDialog('admin')">
          <el-icon><Plus /></el-icon>
          添加小区管理员
        </el-button>
        <el-button type="primary" @click="openAddUserDialog('resident')">
          <el-icon><Plus /></el-icon>
          添加居民用户
        </el-button>
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
        <el-table-column prop="email" label="邮箱" min-width="180" />
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
        <el-table-column prop="createdAt" label="创建时间" min-width="160" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '锁定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button
                type="primary"
                size="small"
                @click="openEditDialog(scope.row)"
                :icon="Edit"
                text
              >
                编辑
              </el-button>
              <el-button
                :type="scope.row.status === 'active' ? 'danger' : 'success'"
                size="small"
                @click="toggleUserStatus(scope.row)"
                text
              >
                {{ scope.row.status === 'active' ? '锁定' : '解锁' }}
              </el-button>
              <el-button
                v-if="scope.row.role !== 'sysadmin' || currentUser.id !== scope.row.id"
                type="danger"
                size="small"
                @click="deleteUser(scope.row)"
                :icon="Delete"
                text
              >
                删除
              </el-button>
            </el-button-group>
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
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-if="currentUser.role === 'sysadmin'"
              label="系统管理员"
              value="sysadmin"
            />
            <el-option label="小区管理员" value="admin" />
            <el-option label="居民用户" value="resident" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="active" />
            <el-option label="锁定" value="locked" />
          </el-select>
        </el-form-item>
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
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
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
  role: 'resident' | 'admin' | 'sysadmin'
  phone?: string
  createdAt: string
  updatedAt: string
  status: 'active' | 'locked'
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
  email: '',
  role: 'resident' as 'resident' | 'admin' | 'sysadmin',
  phone: '',
  password: '',
  status: 'active' as 'active' | 'locked'
})

// 表单验证规则
const formRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
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
  password: [
    { required: !editingUser.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择用户状态', trigger: 'change' }
  ]
})

// 生命周期钩子
onMounted(() => {
  loadUsers()
})

// 方法定义
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined,
      role: roleFilter.value || undefined,
      status: statusFilter.value || undefined
    }

    const response = await apiClient.get('/admin/users', { params })
    users.value = response.data.data.content
    total.value = response.data.data.totalElements
  } catch (error) {
    console.error('Failed to load users:', error)
    ElMessage.error('加载用户数据失败')
  } finally {
    loading.value = false
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

const openEditDialog = (user: User) => {
  editingUser.value = user
  addingUserRole.value = null
  resetForm()
  userForm.id = user.id
  userForm.username = user.username
  userForm.email = user.email
  userForm.role = user.role
  userForm.phone = user.phone || ''
  userForm.status = user.status
  dialogVisible.value = true
}

const resetForm = () => {
  userForm.id = 0
  userForm.username = ''
  userForm.email = ''
  userForm.role = addingUserRole.value || 'resident'
  userForm.phone = ''
  userForm.password = ''
  userForm.status = 'active'
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

const saveUser = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (editingUser.value) {
          // 编辑现有用户
          const { password, ...updateData } = userForm

          // 检查是否是更改自己的角色
          if (currentUser.value.id === editingUser.value.id &&
              updateData.role !== currentUser.value.role) {
            throw new Error('不能修改自己的角色类型')
          }

          await apiClient.put(`/admin/users/${userForm.id}`, updateData)
          ElMessage.success('用户信息更新成功')
        } else {
          // 添加新用户
          await apiClient.post('/admin/users', userForm)
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

  const action = user.status === 'active' ? '锁定' : '解锁'
  const newStatus = user.status === 'active' ? 'locked' : 'active'

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

    await apiClient.put(`/admin/users/${user.id}/status`, {
      status: newStatus
    })

    ElMessage.success(`用户${action}成功`)
    // 更新本地数据
    user.status = newStatus
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
