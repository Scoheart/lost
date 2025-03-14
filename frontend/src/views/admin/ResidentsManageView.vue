<template>
  <div class="residents-manage-container">
    <div class="page-header">
      <h2 class="page-title">居民管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加居民
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索居民（用户名、邮箱、手机号）"
        class="search-input"
        :prefix-icon="Search"
        clearable
        @clear="loadResidents"
      />
      <el-button type="primary" @click="loadResidents">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 居民列表 -->
    <el-card shadow="never" class="residents-table-card">
      <el-table
        v-loading="loading"
        :data="residents"
        border
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无数据'"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="120">
          <template #default="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160">
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
        <el-table-column label="操作" width="200" fixed="right">
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
                @click="toggleResidentStatus(scope.row)"
                :icon="scope.row.status === 'active' ? 'Lock' : 'Unlock'"
                text
              >
                {{ scope.row.status === 'active' ? '锁定' : '解锁' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="deleteResident(scope.row)"
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

    <!-- 添加/编辑居民对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingResident ? '编辑居民' : '添加居民'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="residentFormRef"
        :model="residentForm"
        :rules="formRules"
        label-width="80px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="residentForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="residentForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="residentForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingResident">
          <el-input
            v-model="residentForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="residentForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="active" />
            <el-option label="锁定" value="locked" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveResident" :loading="submitting">
            {{ editingResident ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { format } from 'date-fns'
import apiClient from '@/utils/api'

// 接口定义
interface Resident {
  id: number
  username: string
  email: string
  phone?: string
  createdAt: string
  updatedAt: string
  status: 'active' | 'locked'
}

// 状态管理
const loading = ref(false)
const submitting = ref(false)
const residents = ref<Resident[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const dialogVisible = ref(false)
const editingResident = ref<Resident | null>(null)
const residentFormRef = ref<FormInstance>()

// 表单数据
const residentForm = reactive({
  id: 0,
  username: '',
  email: '',
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
  phone: [
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', required: !editingResident.value },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ]
})

// 生命周期钩子
onMounted(() => {
  loadResidents()
})

// 方法定义
const loadResidents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value || undefined
    }

    const response = await apiClient.get('/admin/residents', { params })
    residents.value = response.data.data.content
    total.value = response.data.data.totalElements
  } catch (error) {
    console.error('Failed to load residents:', error)
    ElMessage.error('加载居民数据失败')
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
  loadResidents()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadResidents()
}

const openAddDialog = () => {
  editingResident.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (resident: Resident) => {
  editingResident.value = resident
  resetForm()
  residentForm.id = resident.id
  residentForm.username = resident.username
  residentForm.email = resident.email
  residentForm.phone = resident.phone || ''
  residentForm.status = resident.status
  dialogVisible.value = true
}

const resetForm = () => {
  residentForm.id = 0
  residentForm.username = ''
  residentForm.email = ''
  residentForm.phone = ''
  residentForm.password = ''
  residentForm.status = 'active'
  if (residentFormRef.value) {
    residentFormRef.value.resetFields()
  }
}

const saveResident = async () => {
  if (!residentFormRef.value) return

  await residentFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (editingResident.value) {
          // 编辑现有居民
          const { password, ...updateData } = residentForm
          await apiClient.put(`/admin/residents/${residentForm.id}`, updateData)
          ElMessage.success('居民信息更新成功')
        } else {
          // 添加新居民
          await apiClient.post('/admin/residents', residentForm)
          ElMessage.success('居民添加成功')
        }
        dialogVisible.value = false
        loadResidents()
      } catch (error: any) {
        const errorMsg = error.response?.data?.message || '操作失败，请稍后再试'
        ElMessage.error(errorMsg)
      } finally {
        submitting.value = false
      }
    }
  })
}

const toggleResidentStatus = async (resident: Resident) => {
  const action = resident.status === 'active' ? '锁定' : '解锁'
  const newStatus = resident.status === 'active' ? 'locked' : 'active'

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${resident.username}" 吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await apiClient.put(`/admin/residents/${resident.id}/status`, {
      status: newStatus
    })

    ElMessage.success(`用户${action}成功`)
    // 更新本地数据
    resident.status = newStatus
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || `${action}失败，请稍后再试`
      ElMessage.error(errorMsg)
    }
  }
}

const deleteResident = async (resident: Resident) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${resident.username}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )

    await apiClient.delete(`/admin/residents/${resident.id}`)
    ElMessage.success('用户删除成功')
    loadResidents()
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '删除失败，请稍后再试'
      ElMessage.error(errorMsg)
    }
  }
}
</script>

<style scoped>
.residents-manage-container {
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

.search-bar {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  max-width: 400px;
}

.residents-table-card {
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

  .search-bar {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    max-width: 100%;
  }
}
</style>
