<template>
  <div class="claims-manage-container">
    <h2 class="page-title">认领管理</h2>

    <el-card shadow="never">
      <div class="filter-row">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="认领状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px">
              <el-option label="待审核" value="pending" />
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>

          <el-form-item label="申请日期">
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

          <el-form-item label="物品名称">
            <el-input
              v-model="filterForm.itemTitle"
              placeholder="输入物品名称关键词"
              clearable
              style="width: 160px"
            />
          </el-form-item>

          <el-form-item label="申请人">
            <el-input
              v-model="filterForm.applicantName"
              placeholder="输入申请人姓名"
              clearable
              style="width: 140px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 认领申请列表 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <el-table
        v-else
        :data="claimsList"
        border
        style="width: 100%"
        v-loading="tableLoading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="foundItemTitle" label="物品名称" min-width="180">
          <template #default="scope">
            <el-link @click="viewItemDetail(scope.row)">{{ scope.row.foundItemTitle }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="认领说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="认领申请人" width="120" />
        <el-table-column label="申请时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="招领发布者" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button type="info" size="small" @click="viewDetail(scope.row)" text>
                详情
              </el-button>
              <el-popconfirm
                title="确定要删除这条认领记录吗？"
                @confirm="deleteClaim(scope.row.id)"
              >
                <template #reference>
                  <el-button type="danger" size="small" text>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="认领详情"
      width="600px"
    >
      <div v-if="selectedClaim" class="claim-detail">
        <div class="detail-section">
          <h3>物品信息</h3>
          <p><strong>物品名称：</strong>{{ selectedClaim.foundItemTitle }}</p>
          <p><strong>招领发布者：</strong>{{ selectedClaim.ownerName }}</p>
        </div>

        <el-divider />

        <div class="detail-section">
          <h3>认领信息</h3>
          <p><strong>认领申请人：</strong>{{ selectedClaim.applicantName }}</p>
          <p><strong>认领说明：</strong>{{ selectedClaim.description }}</p>
          <p><strong>联系方式：</strong>{{ selectedClaim.applicantContact || '未提供' }}</p>
          <p><strong>申请时间：</strong>{{ formatDate(selectedClaim.createdAt) }}</p>
          <p><strong>状态：</strong>
            <el-tag :type="getStatusTagType(selectedClaim.status)">
              {{ getStatusLabel(selectedClaim.status) }}
            </el-tag>
          </p>
        </div>

        <div v-if="selectedClaim.status !== 'pending'" class="detail-section">
          <h3>处理结果</h3>
          <p><strong>处理时间：</strong>{{ formatDate(selectedClaim.processedAt) || '未处理' }}</p>
          <p v-if="selectedClaim.status === 'approved'"><strong>结果：</strong>申请已被批准</p>
          <p v-if="selectedClaim.status === 'rejected'"><strong>结果：</strong>申请已被拒绝</p>
          <p v-if="selectedClaim.status === 'completed'"><strong>结果：</strong>物品已完成交接</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import apiClient from '@/utils/api'
import { handleApiError } from '@/utils/apiHelpers'
import { Search } from '@element-plus/icons-vue'

// 认领申请接口
interface ClaimApplication {
  id: number
  foundItemId: number
  foundItemTitle: string
  foundItemImage: string | null
  applicantId: number
  applicantName: string
  applicantContact: string | null
  ownerId: number
  ownerName: string
  description: string
  status: 'pending' | 'approved' | 'rejected' | 'completed'
  createdAt: string
  updatedAt: string
  processedAt: string | null
}

// 分页数据接口
interface ClaimsPagination {
  applications: ClaimApplication[]
  currentPage: number
  pageSize: number
  totalPages: number
  totalItems: number
}

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 状态
const loading = ref(true)
const tableLoading = ref(false)
const submitting = ref(false)
const claimsList = ref<ClaimApplication[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 过滤表单
const filterForm = reactive({
  status: '',
  dateRange: [],
  itemTitle: '',
  applicantName: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const selectedClaim = ref<ClaimApplication | null>(null)

// 获取认领申请列表
const fetchClaims = async () => {
  loading.value = true
  tableLoading.value = true

  try {
    // 构建URL和查询参数
    const params = new URLSearchParams()
    params.append('page', currentPage.value.toString())
    params.append('size', pageSize.value.toString())
    if (filterForm.status) {
      params.append('status', filterForm.status)
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.append('startDate', filterForm.dateRange[0])
      params.append('endDate', filterForm.dateRange[1])
    }
    if (filterForm.itemTitle) {
      params.append('itemTitle', filterForm.itemTitle)
    }
    if (filterForm.applicantName) {
      params.append('applicantName', filterForm.applicantName)
    }

    const response = await apiClient.get(`/claims/admin/all?${params.toString()}`)
    const responseData = response.data

    if (responseData.success) {
      const data = responseData.data as ClaimsPagination
      claimsList.value = data.applications
      total.value = data.totalItems
    } else {
      throw new Error(responseData.message || '获取认领申请列表失败')
    }
  } catch (error) {
    const errorResult = handleApiError(error, '获取认领申请列表失败')
    ElMessage.error(errorResult.message)
  } finally {
    loading.value = false
    tableLoading.value = false
  }
}

// 删除认领记录
const deleteClaim = async (claimId: number) => {
  tableLoading.value = true

  try {
    const response = await apiClient.delete(`/claims/admin/${claimId}`)
    const responseData = response.data

    if (responseData.success) {
      ElMessage.success('认领记录已删除')
      // 刷新列表
      fetchClaims()
    } else {
      throw new Error(responseData.message || '删除认领记录失败')
    }
  } catch (error) {
    const errorResult = handleApiError(error, '删除认领记录失败')
    ElMessage.error(errorResult.message)
  } finally {
    tableLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string | null) => {
  if (!dateString) return '未知'
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm')
  } catch (error) {
    return dateString
  }
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return statusMap[status] || status
}

// 查看物品详情
const viewItemDetail = (claim: ClaimApplication) => {
  window.open(`/found-items/${claim.foundItemId}`, '_blank')
}

// 查看详情
const viewDetail = (claim: ClaimApplication) => {
  selectedClaim.value = claim
  detailDialogVisible.value = true
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchClaims()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.status = ''
  filterForm.dateRange = []
  filterForm.itemTitle = ''
  filterForm.applicantName = ''
  handleSearch()
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchClaims()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchClaims()
}

// 生命周期钩子
onMounted(() => {
  fetchClaims()
})
</script>

<style scoped>
.claims-manage-container {
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

.claim-detail {
  font-size: 14px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #409EFF;
}

.detail-section p {
  margin: 8px 0;
  line-height: 1.6;
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
</style>
