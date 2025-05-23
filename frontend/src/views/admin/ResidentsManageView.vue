<template>
  <div class="residents-manage-container">
    <div class="page-header">
      <h2 class="page-title">居民管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加居民
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="搜索用户">
          <el-input
            v-model="filterForm.search"
            placeholder="用户名、邮箱、手机号"
            clearable
            @clear="loadResidents"
            class="search-input"
            :prefix-icon="Search"
          />
        </el-form-item>

        <el-form-item label="账号状态">
          <el-select
            v-model="filterForm.isLocked"
            placeholder="选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="false" />
            <el-option label="锁定" :value="true" />
          </el-select>
        </el-form-item>

        <el-form-item label="注册日期">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadResidents">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120">
          <template #default="scope">
            {{ scope.row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="120">
          <template #default="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="住址" min-width="180">
          <template #default="scope">
            {{ scope.row.address || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row)">
              {{ getStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
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
                @click="toggleResidentStatus(scope.row)"
                text
              >
                {{ scope.row.isLocked ? '解锁' : '锁定' }}
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
        <template v-if="!editingResident">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="residentForm.username" placeholder="请输入用户名" />
          </el-form-item>
        </template>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="residentForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <!-- 只在添加模式显示邮箱和密码 -->
        <template v-if="!editingResident">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="residentForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="residentForm.email" placeholder="请输入邮箱（选填）" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="residentForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="residentForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
        </template>

        <el-form-item label="住址" prop="address">
          <el-input v-model="residentForm.address" placeholder="请输入住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveResident" :loading="submitting">
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
  role: string
  avatar?: string | null
  realName?: string | null
  address?: string | null
  isLocked: boolean
  createdAt: string
  updatedAt: string
}

// 状态管理
const loading = ref(false)
const submitting = ref(false)
const residents = ref<Resident[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editingResident = ref<Resident | null>(null)
const residentFormRef = ref<FormInstance>()

// 筛选表单
const filterForm = reactive({
  search: '',
  isLocked: null as boolean | null,
  dateRange: [] as string[]
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 表单数据
const residentForm = reactive({
  id: 0,
  username: '',
  email: '' as string | null,
  phone: '',
  password: '',
  confirmPassword: '',
  realName: '',
  address: ''
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
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  password: [
    { required: !editingResident.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !editingResident.value, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== residentForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  address: [
    { required: true, message: '请输入住址', trigger: 'blur' },
    { max: 200, message: '住址长度不能超过200个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
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
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      search: filterForm.search || undefined,
      status: filterForm.isLocked !== null ? !filterForm.isLocked : undefined
    }

    // 添加日期范围过滤
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }

    console.log('请求参数:', params)
    const response = await apiClient.get('/residents', { params })
    console.log('API响应:', response.data)

    if (response.data.success) {
      residents.value = response.data.data.items || []
      total.value = response.data.data.totalItems || 0
    } else {
      ElMessage.error(response.data.message || '获取居民数据失败')
    }
  } catch (error) {
    console.error('Failed to load residents:', error)
    ElMessage.error('加载居民数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilters = () => {
  filterForm.search = ''
  filterForm.isLocked = null
  filterForm.dateRange = []
  currentPage.value = 1
  loadResidents()
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
  residentForm.realName = resident.realName || ''
  residentForm.address = resident.address || ''
  dialogVisible.value = true
}

const resetForm = () => {
  residentForm.id = 0
  residentForm.username = ''
  residentForm.email = ''
  residentForm.phone = ''
  residentForm.password = ''
  residentForm.confirmPassword = ''
  residentForm.realName = ''
  residentForm.address = ''
  if (residentFormRef.value) {
    residentFormRef.value.resetFields()
  }
}

const handleSaveResident = async () => {
  if (!residentFormRef.value) return

  await residentFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证密码是否一致(在添加模式下)
      if (!editingResident.value && residentForm.password !== residentForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致');
        return;
      }

      submitting.value = true
      try {
        if (editingResident.value) {
          // 编辑现有居民 - 只更新真实姓名和住址
          const updateData = {
            realName: residentForm.realName,
            address: residentForm.address
          }
          const result = await apiClient.put(`/residents/${residentForm.id}`, updateData)
          if (result.data.success) {
            ElMessage.success('居民信息已更新')
            dialogVisible.value = false
            loadResidents()
          } else {
            ElMessage.error(result.data.message || '更新居民失败')
          }
        } else {
          // 添加新居民 - 从表单数据中删除确认密码字段
          const { confirmPassword, ...submitData } = residentForm;

          // 处理空邮箱 - 将空字符串转换为null
          if (!submitData.email || submitData.email.trim() === '') {
            submitData.email = null;
          }

          // 添加默认锁定状态为未锁定(false)
          const residentData = {
            ...submitData,
            isLocked: false
          };

          const result = await apiClient.post('/residents', residentData)
          if (result.data.success) {
            ElMessage.success('居民已添加')
            dialogVisible.value = false
            loadResidents()
          } else {
            ElMessage.error(result.data.message || '添加居民失败')
          }
        }
      } catch (error: any) {
        const message = error.response?.data?.message || (editingResident.value ? '更新居民失败' : '添加居民失败')
        ElMessage.error(message)
        console.error('Failed to save resident:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const toggleResidentStatus = async (resident: Resident) => {
  const action = resident.isLocked ? '解锁' : '锁定'
  const newStatus = !resident.isLocked

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

    const result = await apiClient.put(`/residents/${resident.id}/status`, {
      isLocked: newStatus
    })

    if (result.data.success) {
      resident.isLocked = newStatus
      ElMessage.success(`用户${action}成功`)
    } else {
      ElMessage.error(result.data.message || `${action}失败，请稍后再试`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || `${action}失败，请稍后再试`
      ElMessage.error(errorMsg)
    }
  }
}

// 辅助函数用于获取状态标签类型和文本
const getStatusTagType = (resident: Resident): string => {
  return resident.isLocked ? 'danger' : 'success'
}

const getStatusLabel = (resident: Resident): string => {
  return resident.isLocked ? '锁定' : '正常'
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

.filter-card {
  margin-bottom: 20px;
}

.search-input {
  width: 220px;
}

.residents-table-card {
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

  .filter-card :deep(.el-form-item) {
    margin-bottom: 10px;
  }
}
</style>
