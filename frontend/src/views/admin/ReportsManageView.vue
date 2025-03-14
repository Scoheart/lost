<template>
  <div class="reports-manage-container">
    <h2 class="page-title">举报管理</h2>

    <el-card shadow="never">
      <div class="filter-row">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="举报类型">
            <el-select v-model="filterForm.type" placeholder="选择类型" clearable>
              <el-option label="论坛帖子" value="forum_post" />
              <el-option label="寻物启事" value="lost_item" />
              <el-option label="失物招领" value="found_item" />
              <el-option label="用户举报" value="user" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
              <el-option label="待处理" value="pending" />
              <el-option label="已处理" value="processed" />
              <el-option label="已驳回" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 举报列表 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <el-table
        v-else
        :data="reportsList"
        border
        style="width: 100%"
        v-loading="tableLoading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="举报类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="被举报内容" min-width="200">
          <template #default="scope">
            <el-link @click="viewReportedContent(scope.row)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="200" />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column label="举报时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'pending'" type="success" size="small" @click="processReport(scope.row)">
              处理
            </el-button>
            <el-button v-if="scope.row.status === 'pending'" type="danger" size="small" @click="rejectReport(scope.row)">
              驳回
            </el-button>
            <el-button v-if="scope.row.status !== 'pending'" type="info" size="small" @click="viewDetail(scope.row)">
              详情
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

    <!-- 处理举报对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理举报"
      width="500px"
    >
      <el-form ref="processFormRef" :model="processForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.action">
            <el-radio label="delete">删除内容</el-radio>
            <el-radio label="warning">警告用户</el-radio>
            <el-radio label="block">禁言用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmProcessReport" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 驳回举报对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回举报"
      width="500px"
    >
      <el-form ref="rejectFormRef" :model="rejectForm" label-width="100px">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRejectReport" :loading="submitting">
            确认
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

// 模拟数据 - 实际开发时会从API获取数据
interface Report {
  id: number
  type: 'forum_post' | 'lost_item' | 'found_item' | 'user'
  targetId: number
  title: string
  reason: string
  reporterId: number
  reporterName: string
  status: 'pending' | 'processed' | 'rejected'
  createdAt: string
  updatedAt: string
  processComment?: string
  processAction?: 'delete' | 'warning' | 'block'
}

// 状态
const loading = ref(true)
const tableLoading = ref(false)
const submitting = ref(false)
const reportsList = ref<Report[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 过滤表单
const filterForm = reactive({
  type: '',
  status: ''
})

// 处理举报对话框
const processDialogVisible = ref(false)
const currentReport = ref<Report | null>(null)
const processForm = reactive({
  action: 'delete',
  comment: ''
})

// 驳回举报对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: ''
})

// 获取举报列表
const fetchReports = async () => {
  loading.value = true
  tableLoading.value = true

  try {
    // 这里实际开发时会调用API
    // const response = await api.get('/admin/reports', {
    //   params: {
    //     page: currentPage.value,
    //     pageSize: pageSize.value,
    //     type: filterForm.type || undefined,
    //     status: filterForm.status || undefined
    //   }
    // })

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 800))

    const mockData = {
      items: [
        {
          id: 1,
          type: 'forum_post',
          targetId: 101,
          title: '这是一个被举报的论坛帖子',
          reason: '内容包含不适当的言论',
          reporterId: 201,
          reporterName: '用户A',
          status: 'pending',
          createdAt: '2023-11-10T08:30:00Z',
          updatedAt: '2023-11-10T08:30:00Z'
        },
        {
          id: 2,
          type: 'lost_item',
          targetId: 102,
          title: '寻找我的手机',
          reason: '虚假信息',
          reporterId: 202,
          reporterName: '用户B',
          status: 'processed',
          createdAt: '2023-11-09T14:20:00Z',
          updatedAt: '2023-11-09T16:45:00Z',
          processComment: '内容已删除',
          processAction: 'delete'
        },
        {
          id: 3,
          type: 'user',
          targetId: 103,
          title: '用户C',
          reason: '发布垃圾信息',
          reporterId: 203,
          reporterName: '用户D',
          status: 'rejected',
          createdAt: '2023-11-08T09:15:00Z',
          updatedAt: '2023-11-08T10:30:00Z'
        }
      ],
      total: 3
    }

    reportsList.value = mockData.items as Report[]
    total.value = mockData.total
  } catch (error) {
    console.error('Failed to fetch reports:', error)
    ElMessage.error('获取举报列表失败')
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

// 获取举报类型标签样式
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    forum_post: 'primary',
    lost_item: 'success',
    found_item: 'warning',
    user: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取举报类型标签文本
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    forum_post: '论坛帖子',
    lost_item: '寻物启事',
    found_item: '失物招领',
    user: '用户举报'
  }
  return typeMap[type] || type
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'warning',
    processed: 'success',
    rejected: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    processed: '已处理',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

// 查看被举报的内容
const viewReportedContent = (report: Report) => {
  // 根据举报类型和targetId跳转到对应内容页面
  ElMessage.info('查看被举报内容: ' + report.title)
}

// 查看详情
const viewDetail = (report: Report) => {
  ElMessageBox.alert(
    `处理结果: ${report.processAction === 'delete' ? '删除内容' :
       report.processAction === 'warning' ? '警告用户' :
       report.processAction === 'block' ? '禁言用户' : '未知'}\n\n` +
    `处理说明: ${report.processComment || '无'}`,
    '举报处理详情',
    { confirmButtonText: '确定' }
  )
}

// 处理举报
const processReport = (report: Report) => {
  currentReport.value = report
  processForm.action = 'delete'
  processForm.comment = ''
  processDialogVisible.value = true
}

// 确认处理举报
const confirmProcessReport = async () => {
  if (!currentReport.value) return

  submitting.value = true
  try {
    // 这里实际开发时会调用API
    // const response = await api.put(`/admin/reports/${currentReport.value.id}/process`, {
    //   action: processForm.action,
    //   comment: processForm.comment
    // })

    // 模拟处理
    await new Promise(resolve => setTimeout(resolve, 800))

    // 更新本地数据
    const index = reportsList.value.findIndex(r => r.id === currentReport.value?.id)
    if (index !== -1) {
      reportsList.value[index] = {
        ...reportsList.value[index],
        status: 'processed',
        processAction: processForm.action,
        processComment: processForm.comment,
        updatedAt: new Date().toISOString()
      }
    }

    ElMessage.success('举报处理成功')
    processDialogVisible.value = false
  } catch (error) {
    console.error('Failed to process report:', error)
    ElMessage.error('处理举报失败')
  } finally {
    submitting.value = false
  }
}

// 驳回举报
const rejectReport = (report: Report) => {
  currentReport.value = report
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认驳回举报
const confirmRejectReport = async () => {
  if (!currentReport.value) return

  submitting.value = true
  try {
    // 这里实际开发时会调用API
    // const response = await api.put(`/admin/reports/${currentReport.value.id}/reject`, {
    //   reason: rejectForm.reason
    // })

    // 模拟处理
    await new Promise(resolve => setTimeout(resolve, 800))

    // 更新本地数据
    const index = reportsList.value.findIndex(r => r.id === currentReport.value?.id)
    if (index !== -1) {
      reportsList.value[index] = {
        ...reportsList.value[index],
        status: 'rejected',
        processComment: rejectForm.reason,
        updatedAt: new Date().toISOString()
      }
    }

    ElMessage.success('举报已驳回')
    rejectDialogVisible.value = false
  } catch (error) {
    console.error('Failed to reject report:', error)
    ElMessage.error('驳回举报失败')
  } finally {
    submitting.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReports()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.type = ''
  filterForm.status = ''
  handleSearch()
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchReports()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchReports()
}

// 生命周期钩子
onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.reports-manage-container {
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
