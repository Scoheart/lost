<template>
  <div class="users-manage-container">
    <h2 class="page-title">用户管理</h2>

    <el-card shadow="never">
      <div class="filter-row">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="用户角色">
            <el-select v-model="filterForm.role" placeholder="选择角色" clearable>
              <el-option label="普通用户" value="resident" />
              <el-option label="管理员" value="admin" />
              <el-option label="系统管理员" value="sysadmin" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
              <el-option label="正常" value="active" />
              <el-option label="禁用" value="disabled" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="用户名/邮箱"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <el-table
        v-else
        :data="usersList"
        border
        style="width: 100%"
        v-loading="tableLoading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户头像" width="90">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.username.charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="editUser(scope.row)"
              :disabled="scope.row.role === 'sysadmin' && userStore.user?.id !== scope.row.id"
            >
              编辑
            </el-button>
            <el-button
              :type="scope.row.status === 'active' ? 'danger' : 'success'"
              size="small"
              @click="toggleUserStatus(scope.row)"
              :disabled="scope.row.role === 'sysadmin' && userStore.user?.id !== scope.row.id"
            >
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="500px"
    >
      <el-form
        v-if="selectedUser"
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="选择角色" style="width: 100%">
            <el-option label="普通用户" value="resident" />
            <el-option
              label="管理员"
              value="admin"
              :disabled="selectedUser.role === 'sysadmin' && userStore.user?.id !== selectedUser.id"
            />
            <el-option
              label="系统管理员"
              value="sysadmin"
              :disabled="userStore.user?.role !== 'sysadmin'"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="重置密码">
          <el-switch v-model="userForm.resetPassword" />
        </el-form-item>
        <el-form-item v-if="userForm.resetPassword" label="新密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser" :loading="submitting">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

// 模拟数据 - 实际开发时会从API获取数据
interface User {
  id: number
  username: string
  email: string
  role: 'resident' | 'admin' | 'sysadmin'
  status: 'active' | 'disabled'
  avatar?: string
  createdAt: string
  updatedAt: string
}

// 状态
const loading = ref(true)
const tableLoading = ref(false)
const submitting = ref(false)
const usersList = ref<User[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userFormRef = ref<FormInstance>()

// 过滤表单
const filterForm = reactive({
  role: '',
  status: '',
  keyword: ''
})

// 编辑用户对话框
const editDialogVisible = ref(false)
const selectedUser = ref<User | null>(null)
const userForm = reactive({
  username: '',
  email: '',
  role: 'resident' as 'resident' | 'admin' | 'sysadmin',
  resetPassword: false,
  password: ''
})

// 表单验证规则
const userRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, callback) => {
      if (userForm.resetPassword && (!value || value.length < 6)) {
        callback(new Error('密码长度至少为6个字符'))
      } else {
        callback()
      }
    }}
  ]
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  tableLoading.value = true

  try {
    // 这里实际开发时会调用API
    // const response = await api.get('/admin/users', {
    //   params: {
    //     page: currentPage.value,
    //     pageSize: pageSize.value,
    //     role: filterForm.role || undefined,
    //     status: filterForm.status || undefined,
    //     keyword: filterForm.keyword || undefined
    //   }
    // })

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 800))

    const mockData = {
      items: [
        {
          id: 1,
          username: 'admin',
          email: 'admin@example.com',
          role: 'sysadmin',
          status: 'active',
          avatar: '',
          createdAt: '2023-10-01T08:30:00Z',
          updatedAt: '2023-10-01T08:30:00Z'
        },
        {
          id: 2,
          username: 'manager',
          email: 'manager@example.com',
          role: 'admin',
          status: 'active',
          avatar: '',
          createdAt: '2023-10-02T10:15:00Z',
          updatedAt: '2023-10-02T10:15:00Z'
        },
        {
          id: 3,
          username: 'user1',
          email: 'user1@example.com',
          role: 'resident',
          status: 'active',
          avatar: '',
          createdAt: '2023-10-03T14:20:00Z',
          updatedAt: '2023-10-03T14:20:00Z'
        },
        {
          id: 4,
          username: 'user2',
          email: 'user2@example.com',
          role: 'resident',
          status: 'disabled',
          avatar: '',
          createdAt: '2023-10-04T16:45:00Z',
          updatedAt: '2023-10-05T09:30:00Z'
        }
      ],
      total: 4
    }

    usersList.value = mockData.items as User[]
    total.value = mockData.total
  } catch (error) {
    console.error('Failed to fetch users:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
    tableLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm')
  } catch (error) {
    return dateString
  }
}

// 获取角色标签样式
const getRoleTagType = (role: string) => {
  const roleMap: Record<string, string> = {
    resident: '',
    admin: 'warning',
    sysadmin: 'danger'
  }
  return roleMap[role] || ''
}

// 获取角色标签文本
const getRoleLabel = (role: string) => {
  const roleMap: Record<string, string> = {
    resident: '普通用户',
    admin: '管理员',
    sysadmin: '系统管理员'
  }
  return roleMap[role] || role
}

// 编辑用户
const editUser = (user: User) => {
  selectedUser.value = user
  userForm.username = user.username
  userForm.email = user.email
  userForm.role = user.role
  userForm.resetPassword = false
  userForm.password = ''
  editDialogVisible.value = true
}

// 保存用户
const saveUser = async () => {
  if (!selectedUser.value) return
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 这里实际开发时会调用API
        // const data = {
        //   username: userForm.username,
        //   email: userForm.email,
        //   role: userForm.role,
        //   ...(userForm.resetPassword && { password: userForm.password })
        // }
        // const response = await api.put(`/admin/users/${selectedUser.value.id}`, data)

        // 模拟处理
        await new Promise(resolve => setTimeout(resolve, 800))

        // 更新本地数据
        const index = usersList.value.findIndex(u => u.id === selectedUser.value?.id)
        if (index !== -1) {
          usersList.value[index] = {
            ...usersList.value[index],
            username: userForm.username,
            email: userForm.email,
            role: userForm.role,
            updatedAt: new Date().toISOString()
          }
        }

        ElMessage.success('用户信息更新成功')
        editDialogVisible.value = false
      } catch (error) {
        console.error('Failed to update user:', error)
        ElMessage.error('更新用户信息失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 切换用户状态
const toggleUserStatus = (user: User) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  const warningText = user.status === 'active' ?
    '禁用后该用户将无法登录系统，是否继续？' :
    '启用后该用户将恢复正常使用，是否继续？'

  ElMessageBox.confirm(warningText, `确认${action}用户`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    tableLoading.value = true
    try {
      // 这里实际开发时会调用API
      // const response = await api.put(`/admin/users/${user.id}/status`, {
      //   status: user.status === 'active' ? 'disabled' : 'active'
      // })

      // 模拟处理
      await new Promise(resolve => setTimeout(resolve, 800))

      // 更新本地数据
      const index = usersList.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        usersList.value[index] = {
          ...usersList.value[index],
          status: usersList.value[index].status === 'active' ? 'disabled' : 'active',
          updatedAt: new Date().toISOString()
        }
      }

      ElMessage.success(`用户${action}成功`)
    } catch (error) {
      console.error(`Failed to ${action} user:`, error)
      ElMessage.error(`${action}用户失败`)
    } finally {
      tableLoading.value = false
    }
  }).catch(() => {})
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.role = ''
  filterForm.status = ''
  filterForm.keyword = ''
  handleSearch()
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUsers()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUsers()
}

// 生命周期钩子
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-manage-container {
  padding: 0 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  font-weight: 600;
}

.filter-row {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
