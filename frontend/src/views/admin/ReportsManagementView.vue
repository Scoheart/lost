<template>
  <div class="reports-management">
    <h1 class="page-title">举报管理</h1>

    <!-- 筛选区域 -->
    <div class="filter-box">
      <el-card shadow="never">
        <div class="filter-container">
          <el-select
            v-model="statusFilter"
            placeholder="举报状态"
            clearable
            class="filter-item"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>

          <el-select
            v-model="typeFilter"
            placeholder="举报类型"
            clearable
            class="filter-item"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="寻物启事" value="LOST_ITEM" />
            <el-option label="失物招领" value="FOUND_ITEM" />
            <el-option label="留言评论" value="COMMENT" />
          </el-select>

          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="filter-item date-range"
            @change="handleFilterChange"
          />

          <el-button type="primary" @click="fetchReports">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片行 -->
    <div class="stat-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content pending">
              <div class="stat-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ pendingCount }}</div>
                <div class="stat-label">待处理举报</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content resolved">
              <div class="stat-icon">
                <el-icon><Select /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ resolvedCount }}</div>
                <div class="stat-label">已处理举报</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content rejected">
              <div class="stat-icon">
                <el-icon><Close /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ rejectedCount }}</div>
                <div class="stat-label">已驳回举报</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 举报列表 -->
    <div class="reports-list-container">
      <el-card shadow="never" class="reports-list">
        <div class="table-operations">
          <el-button type="primary" @click="handleBatchProcess" :disabled="selectedReports.length === 0">
            批量处理选中的举报
          </el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="reports"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column type="index" width="50" label="#" />
          <el-table-column prop="reportType" label="举报类型" width="120">
            <template #default="scope">
              <el-tag
                :type="getReportTagType(scope.row)"
                effect="plain"
              >
                {{ getReportDisplayType(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reportedItemContent" label="被举报内容" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.reportedItemContent || scope.row.reportedItemTitle }}
            </template>
          </el-table-column>
          <el-table-column prop="reporterUsername" label="举报人" width="120" />
          <el-table-column prop="reportedUsername" label="被举报人" width="120" />
          <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="getStatusTag(scope.row.status)"
                effect="light"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="举报时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="200">
            <template #default="scope">
              <el-button-group v-if="scope.row.status === 'PENDING'">
                <el-button type="primary" size="small" @click="handleResolve(scope.row)">
                  处理
                </el-button>
                <el-button type="danger" size="small" @click="handleReject(scope.row)">
                  驳回
                </el-button>
              </el-button-group>
              <el-button v-else type="primary" size="small" plain @click="viewDetail(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页控制 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalItems"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 举报处理对话框 -->
    <el-dialog
      v-model="resolveDialogVisible"
      title="处理举报"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="resolveFormRef"
        :model="resolveForm"
        :rules="resolveFormRules"
        label-width="100px"
        class="resolution-form"
      >
        <div class="report-info">
          <div class="report-info-item">
            <strong>举报类型:</strong> {{ getReportDisplayType(currentReport) }}
          </div>
          <div class="report-info-item">
            <strong>举报内容:</strong> {{ currentReport.reportedItemContent || currentReport.reportedItemTitle }}
            <el-button
              v-if="currentReport.reportType !== 'COMMENT' || isClaimReport(currentReport)"
              type="text"
              size="small"
              @click="viewReportedItem(currentReport)"
            >
              查看详情
            </el-button>
          </div>
          <div class="report-info-item">
            <strong>举报人:</strong> {{ currentReport.reporterUsername }}
          </div>
          <div class="report-info-item">
            <strong>被举报人:</strong> {{ currentReport.reportedUsername }}
          </div>
          <div class="report-info-item">
            <strong>举报原因:</strong> {{ currentReport.reason }}
          </div>
          <div class="report-info-item">
            <strong>举报时间:</strong> {{ formatDateTime(currentReport.createdAt) }}
          </div>
        </div>

        <el-divider content-position="center">处理决定</el-divider>

        <el-form-item label="处理结果" prop="status">
          <el-radio-group v-model="resolveForm.status">
            <el-radio :label="'RESOLVED'">确认违规</el-radio>
            <el-radio :label="'REJECTED'">驳回举报</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="处理说明" prop="resolutionNotes">
          <el-input
            v-model="resolveForm.resolutionNotes"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明，将作为内部记录"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="处理方式" prop="actionType" v-if="resolveForm.status === 'RESOLVED'">
          <el-select v-model="resolveForm.actionType" placeholder="请选择处理方式" style="width: 100%">
            <el-option label="不采取行动" value="NONE" />
            <el-option label="锁定用户" value="USER_LOCK" />
          </el-select>
          <div class="form-help-text">
            <el-text size="small" type="info">
              <template v-if="resolveForm.actionType === 'USER_LOCK'">
                锁定用户：用户被锁定期间无法登录系统。
              </template>
            </el-text>
          </div>
        </el-form-item>

        <el-form-item
          label="处理天数"
          prop="actionDays"
          v-if="resolveForm.status === 'RESOLVED' && resolveForm.actionType === 'USER_LOCK'"
        >
          <el-input-number
            v-model="resolveForm.actionDays"
            :min="1"
            :max="365"
            controls-position="right"
          />
          <span class="form-help-text">天</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resolveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResolve" :loading="submitting">
            提交处理
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 举报详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="举报详情"
      width="600px"
    >
      <div class="report-detail" v-if="currentReport">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="举报类型">
            {{ getReportDisplayType(currentReport) }}
          </el-descriptions-item>
          <el-descriptions-item label="被举报内容">
            {{ currentReport.reportedItemContent || currentReport.reportedItemTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="举报人">
            {{ currentReport.reporterUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="被举报人">
            {{ currentReport.reportedUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="举报原因">
            {{ currentReport.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="举报时间">
            {{ formatDateTime(currentReport.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="举报状态">
            <el-tag :type="getStatusTag(currentReport.status)">
              {{ getStatusText(currentReport.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理结果" v-if="currentReport.status !== 'PENDING'">
            {{ currentReport.resolutionNotes || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人" v-if="currentReport.resolvedByAdminUsername">
            {{ currentReport.resolvedByAdminUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="currentReport.resolvedAt">
            {{ formatDateTime(currentReport.resolvedAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="isClaimReport(currentReport)" label="举报类型备注">
            <el-tag type="warning">认领申请举报需重点关注</el-tag>
            <p class="report-note">认领申请可能涉及物品归属争议，请仔细核查双方信息</p>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Search,
  Refresh,
  Timer,
  Select,
  Close,
  ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import api from '@/utils/api'

// 状态变量
const loading = ref(false)
const reports = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const pendingCount = ref(0)
const resolvedCount = ref(0)
const rejectedCount = ref(0)

// 选中的记录
const selectedReports = ref([])

// 筛选条件
const statusFilter = ref('')
const typeFilter = ref('')
const dateRange = ref([])

// 对话框相关
const resolveDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentReport = ref({})
const submitting = ref(false)

// 表单相关
const resolveFormRef = ref(null)
const resolveForm = reactive({
  status: 'RESOLVED',
  resolutionNotes: '',
  actionType: 'NONE',
  actionDays: 7
})

// 表单验证规则
const resolveFormRules = {
  status: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  resolutionNotes: [
    { required: true, message: '请输入处理说明', trigger: 'blur' },
    { min: 5, max: 500, message: '处理说明长度在5到500个字符之间', trigger: 'blur' }
  ],
  actionType: [
    {
      required: true,
      message: '请选择处理方式',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (resolveForm.status === 'RESOLVED' && !value) {
          callback(new Error('请选择处理方式'))
        } else {
          callback()
        }
      }
    }
  ],
  actionDays: [
    {
      required: true,
      message: '请填写处理天数',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (
          resolveForm.status === 'RESOLVED' &&
          ['USER_BAN', 'USER_LOCK'].includes(resolveForm.actionType) &&
          (!value || value < 1)
        ) {
          callback(new Error('请填写处理天数，至少1天'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 初始化
onMounted(() => {
  fetchReports()
  fetchStatistics()
})

// 获取举报列表
async function fetchReports() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (statusFilter.value) {
      params.status = statusFilter.value
    }

    if (typeFilter.value) {
      params.type = typeFilter.value
    }

    // 添加日期范围筛选 (在实际API中需要支持)
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await api.get('/admin/reports', { params })

    if (response.data.success) {
      reports.value = response.data.data.reports
      totalItems.value = response.data.data.totalItems
      pendingCount.value = response.data.data.pendingReportsCount

      // 计算已处理和已驳回的数量
      calculateCounts()
    } else {
      ElMessage.error(response.data.message || '获取举报列表失败')
    }
  } catch (error) {
    console.error('Failed to fetch reports:', error)
    ElMessage.error('获取举报列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
async function fetchStatistics() {
  try {
    const response = await api.get('/admin/reports/count')
    if (response.data.success) {
      pendingCount.value = response.data.data.pendingCount

      // 这里可以根据API的实际返回值设置其他计数
      // resolvedCount.value = response.data.data.resolvedCount
      // rejectedCount.value = response.data.data.rejectedCount
    }
  } catch (error) {
    console.error('Failed to fetch statistics:', error)
  }
}

// 计算不同状态的举报数量
function calculateCounts() {
  // 如果API未返回具体计数，可以临时计算
  const resolved = reports.value.filter(item => item.status === 'RESOLVED').length
  const rejected = reports.value.filter(item => item.status === 'REJECTED').length

  resolvedCount.value = resolved
  rejectedCount.value = rejected
}

// 处理选择变化
function handleSelectionChange(selection) {
  selectedReports.value = selection
}

// 分页处理
function handleSizeChange(size) {
  pageSize.value = size
  fetchReports()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchReports()
}

// 筛选处理
function handleFilterChange() {
  currentPage.value = 1 // 重置到第一页
  fetchReports()
}

function resetFilters() {
  statusFilter.value = ''
  typeFilter.value = ''
  dateRange.value = []
  currentPage.value = 1
  fetchReports()
}

// 处理举报
function handleResolve(report) {
  currentReport.value = report
  resolveForm.status = 'RESOLVED'
  resolveForm.resolutionNotes = ''
  resolveForm.actionType = 'NONE'
  resolveForm.actionDays = 7
  resolveDialogVisible.value = true
}

function handleReject(report) {
  currentReport.value = report
  resolveForm.status = 'REJECTED'
  resolveForm.resolutionNotes = '举报内容不符合违规标准'
  resolveForm.actionType = 'NONE'
  resolveForm.actionDays = 0
  resolveDialogVisible.value = true
}

// 批量处理
function handleBatchProcess() {
  if (selectedReports.value.length === 0) {
    ElMessage.warning('请选择要处理的举报')
    return
  }

  // 只能批量处理待处理的举报
  const pendingReports = selectedReports.value.filter(report => report.status === 'PENDING')

  if (pendingReports.length === 0) {
    ElMessage.warning('选中的举报中没有待处理的记录')
    return
  }

  ElMessageBox.confirm(
    `确定要批量处理 ${pendingReports.length} 条举报吗？默认将全部标记为已驳回。`,
    '批量处理举报',
    {
      confirmButtonText: '确认处理',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    loading.value = true

    try {
      // 这里需要API支持批量处理
      // const response = await api.post('/admin/reports/batch-process', {
      //   reportIds: pendingReports.map(report => report.id),
      //   status: 'REJECTED',
      //   resolutionNotes: '批量处理：举报内容不符合违规标准'
      // })

      // if (response.data.success) {
      //   ElMessage.success('批量处理成功')
      //   fetchReports()
      // } else {
      //   ElMessage.error(response.data.message || '批量处理失败')
      // }

      // 如果API不支持批量处理，可以使用顺序处理代替
      const results = await Promise.all(
        pendingReports.map(report =>
          api.put(`/admin/reports/${report.id}/resolve`, {
            status: 'REJECTED',
            resolutionNotes: '批量处理：举报内容不符合违规标准',
            actionType: 'NONE'
          })
        )
      )

      const success = results.filter(r => r.data && r.data.success).length

      ElMessage.success(`成功处理 ${success} 条举报`)
      fetchReports()
      fetchStatistics()

    } catch (error) {
      console.error('Failed to batch process reports:', error)
      ElMessage.error('批量处理失败，请稍后再试')
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // 用户取消，不做任何操作
  })
}

// 查看详情
function viewDetail(report) {
  currentReport.value = report
  detailDialogVisible.value = true
}

// 提交处理
async function submitResolve() {
  if (!resolveFormRef.value) return

  await resolveFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true

      try {
        const payload = {
          status: resolveForm.status,
          resolutionNotes: resolveForm.resolutionNotes
        }

        // 只有在确认违规时才添加处理方式
        if (resolveForm.status === 'RESOLVED') {
          payload.actionType = resolveForm.actionType

          // 只有在封禁或锁定用户时才需要天数
          if (['USER_BAN', 'USER_LOCK'].includes(resolveForm.actionType)) {
            if (!resolveForm.actionDays || resolveForm.actionDays < 1) {
              ElMessage.warning('请设置处理天数（至少1天）')
              submitting.value = false
              return
            }
            payload.actionDays = resolveForm.actionDays

            // 提示用户确认处理操作
            const actionDesc = resolveForm.actionType === 'USER_BAN' ? '封禁' : '锁定'
            const confirmMessage = `您将${actionDesc}用户 "${currentReport.value.reportedUsername}" ${resolveForm.actionDays}天，${
              resolveForm.actionType === 'USER_LOCK' ? '锁定期间该用户将无法登录系统' : ''
            }，确认继续吗？`

            try {
              await ElMessageBox.confirm(confirmMessage, '确认操作', {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning'
              })
            } catch (e) {
              // 用户取消操作
              submitting.value = false
              return
            }
          }

          // 提示用户确认删除内容
          if (resolveForm.actionType === 'CONTENT_DELETE') {
            try {
              await ElMessageBox.confirm('您确定要删除被举报的内容吗？此操作不可恢复。', '确认删除', {
                confirmButtonText: '确认删除',
                cancelButtonText: '取消',
                type: 'warning'
              })
            } catch (e) {
              // 用户取消操作
              submitting.value = false
              return
            }
          }
        }

        const response = await api.put(`/admin/reports/${currentReport.value.id}/resolve`, payload)

        if (response.data.success) {
          ElMessage.success('举报处理成功')

          // 提示用户处理结果
          if (resolveForm.status === 'RESOLVED') {
            if (resolveForm.actionType === 'USER_BAN') {
              ElMessage.info(`用户 "${currentReport.value.reportedUsername}" 已被封禁 ${resolveForm.actionDays} 天`)
            } else if (resolveForm.actionType === 'USER_LOCK') {
              ElMessage.info(`用户 "${currentReport.value.reportedUsername}" 已被锁定 ${resolveForm.actionDays} 天`)
            } else if (resolveForm.actionType === 'CONTENT_DELETE') {
              ElMessage.info('被举报的内容已被删除')
            }
          }

          resolveDialogVisible.value = false
          fetchReports()
          fetchStatistics()
        } else {
          ElMessage.error(response.data.message || '举报处理失败')
        }
      } catch (error) {
        console.error('Failed to resolve report:', error)
        ElMessage.error('举报处理失败，请稍后再试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 格式转换函数
function formatDateTime(dateString) {
  if (!dateString) return '-'

  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return dateString
  }
}

function getReportTypeText(type) {
  const types = {
    'LOST_ITEM': '寻物启事',
    'FOUND_ITEM': '失物招领',
    'COMMENT': '留言评论'
  }
  return types[type] || type
}

// 获取更专业的举报类型显示文本
function getReportDisplayType(report) {
  if (!report) return '';
  if (isClaimReport(report)) {
    return '认领申请';
  }
  return getReportTypeText(report.reportType);
}

// 根据标题判断是否是认领申请举报
function isClaimReport(report) {
  if (!report) return false;
  return report.reportType === 'COMMENT' &&
         (report.reportedItemTitle && report.reportedItemTitle.includes('认领申请'));
}

function getReportTypeTag(type) {
  const tags = {
    'LOST_ITEM': 'danger',
    'FOUND_ITEM': 'primary',
    'COMMENT': 'info'
  }
  return tags[type] || ''
}

function getStatusText(status) {
  const statuses = {
    'PENDING': '待处理',
    'RESOLVED': '已处理',
    'REJECTED': '已驳回'
  }
  return statuses[status] || status
}

function getStatusTag(status) {
  const tags = {
    'PENDING': 'warning',
    'RESOLVED': 'success',
    'REJECTED': 'info'
  }
  return tags[status] || ''
}

// 查看被举报内容详情
function viewReportedItem(report) {
  if (report.reportType === 'LOST_ITEM') {
    window.open(`/lost-items/${report.reportedItemId}`, '_blank');
  } else if (report.reportType === 'FOUND_ITEM') {
    window.open(`/found-items/${report.reportedItemId}`, '_blank');
  } else if (isClaimReport(report)) {
    // 对于认领申请举报，可以跳转到认领交流页面
    ElMessage.info('认领申请举报，建议在认领交流页面中查看');
    window.open(`/claim-communications`, '_blank');
  }
}

// 查看用户资料
function viewUserProfile(userId) {
  // 这里可以跳转到用户详情页或者打开一个查看用户信息的对话框
  // 根据实际情况来实现
  ElMessage.info('用户ID: ' + userId);
  // 例如：window.open(`/admin/users/${userId}`, '_blank');
}

// 获取报告标签类型
function getReportTagType(report) {
  if (isClaimReport(report)) {
    return 'warning';  // 认领申请使用warning类型
  }
  return getReportTypeTag(report.reportType);
}
</script>

<style scoped>
.reports-management {
  padding: 0 20px 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: 500;
  color: #303133;
}

.filter-box {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.filter-item {
  width: 180px;
}

.date-range {
  width: 400px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
  cursor: default;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.pending .stat-icon {
  background-color: #E6A23C;
}

.resolved .stat-icon {
  background-color: #67C23A;
}

.rejected .stat-icon {
  background-color: #909399;
}

.reports-list-container {
  margin-top: 20px;
}

.table-operations {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.report-info {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
}

.report-info-item {
  margin-bottom: 8px;
  line-height: 1.5;
}

.report-info-item:last-child {
  margin-bottom: 0;
}

.resolution-form {
  margin-top: 20px;
}

.form-help-text {
  margin-left: 10px;
  color: #909399;
}

.report-detail {
  margin-top: 10px;
}

.report-note {
  margin-top: 5px;
  margin-left: 20px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    width: 100%;
  }

  .date-range {
    width: 100%;
  }
}
</style>
