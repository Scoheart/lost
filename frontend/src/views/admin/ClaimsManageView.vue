<template>
  <div class="claims-manage-container">
    <h2 class="page-title">认领管理</h2>

    <el-card shadow="never">
      <div class="filter-row">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="认领状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
              <el-option label="待审核" value="pending" />
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
          <el-form-item label="物品类型">
            <el-select v-model="filterForm.itemType" placeholder="选择类型" clearable>
              <el-option label="失物招领" value="found" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
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
        <el-table-column prop="itemTitle" label="物品名称" min-width="180">
          <template #default="scope">
            <el-link @click="viewItemDetail(scope.row)">{{ scope.row.itemTitle }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="claimDescription" label="认领说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="userName" label="申请人" width="120" />
        <el-table-column label="申请时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="itemOwner" label="物品发布者" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'pending'" type="success" size="small" @click="approveClaim(scope.row)">
              批准
            </el-button>
            <el-button v-if="scope.row.status === 'pending'" type="danger" size="small" @click="rejectClaim(scope.row)">
              拒绝
            </el-button>
            <el-button v-if="scope.row.status === 'approved'" type="primary" size="small" @click="completeClaim(scope.row)">
              完成交接
            </el-button>
            <el-button type="info" size="small" @click="viewDetail(scope.row)">
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

    <!-- 处理认领对话框 -->
    <el-dialog
      v-model="processingDialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form ref="processFormRef" :model="processForm" label-width="100px">
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="actionPlaceholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmProcess" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="认领详情"
      width="600px"
    >
      <div v-if="selectedClaim" class="claim-detail">
        <div class="detail-section">
          <h3>物品信息</h3>
          <p><strong>物品名称：</strong>{{ selectedClaim.itemTitle }}</p>
          <p><strong>物品类型：</strong>{{ selectedClaim.itemType === 'found' ? '失物招领' : '其他' }}</p>
          <p><strong>发布者：</strong>{{ selectedClaim.itemOwner }}</p>
          <p><strong>物品描述：</strong>{{ selectedClaim.itemDescription }}</p>
        </div>

        <el-divider />

        <div class="detail-section">
          <h3>认领信息</h3>
          <p><strong>认领人：</strong>{{ selectedClaim.userName }}</p>
          <p><strong>认领说明：</strong>{{ selectedClaim.claimDescription }}</p>
          <p><strong>联系方式：</strong>{{ selectedClaim.contactInfo }}</p>
          <p><strong>申请时间：</strong>{{ formatDate(selectedClaim.createdAt) }}</p>
          <p><strong>状态：</strong>
            <el-tag :type="getStatusTagType(selectedClaim.status)">
              {{ getStatusLabel(selectedClaim.status) }}
            </el-tag>
          </p>
        </div>

        <div v-if="selectedClaim.status !== 'pending'" class="detail-section">
          <h3>处理结果</h3>
          <p><strong>处理人：</strong>{{ selectedClaim.processedBy || '系统' }}</p>
          <p><strong>处理时间：</strong>{{ formatDate(selectedClaim.processedAt) }}</p>
          <p><strong>处理说明：</strong>{{ selectedClaim.processComment || '无' }}</p>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'

// 模拟数据 - 实际开发时会从API获取数据
interface Claim {
  id: number
  itemId: number
  itemTitle: string
  itemType: 'found'
  itemDescription: string
  itemOwner: string
  userId: number
  userName: string
  claimDescription: string
  contactInfo: string
  status: 'pending' | 'approved' | 'rejected' | 'completed'
  createdAt: string
  updatedAt: string
  processedBy?: string
  processedAt?: string
  processComment?: string
}

// 状态
const loading = ref(true)
const tableLoading = ref(false)
const submitting = ref(false)
const claimsList = ref<Claim[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 过滤表单
const filterForm = reactive({
  status: '',
  itemType: ''
})

// 处理对话框
const processingDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedClaim = ref<Claim | null>(null)
const processForm = reactive({
  action: '',
  comment: ''
})

// 计算属性
const dialogTitle = computed(() => {
  switch (processForm.action) {
    case 'approve':
      return '批准认领'
    case 'reject':
      return '拒绝认领'
    case 'complete':
      return '完成交接'
    default:
      return '处理认领'
  }
})

const actionPlaceholder = computed(() => {
  switch (processForm.action) {
    case 'approve':
      return '请输入批准认领的说明，如物品交接地点、时间等'
    case 'reject':
      return '请输入拒绝认领的原因'
    case 'complete':
      return '请输入完成交接的备注信息'
    default:
      return '请输入处理说明'
  }
})

// 获取认领申请列表
const fetchClaims = async () => {
  loading.value = true
  tableLoading.value = true

  try {
    // 这里实际开发时会调用API
    // const response = await api.get('/admin/claims', {
    //   params: {
    //     page: currentPage.value,
    //     pageSize: pageSize.value,
    //     status: filterForm.status || undefined,
    //     itemType: filterForm.itemType || undefined
    //   }
    // })

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 800))

    const mockData = {
      items: [
        {
          id: 1,
          itemId: 201,
          itemTitle: '校园内捡到一部银色手机',
          itemType: 'found',
          itemDescription: '在教学楼门口找到的，是一部银色的iPhone 13',
          itemOwner: '张三',
          userId: 301,
          userName: '李四',
          claimDescription: '这是我的手机，丢失在教学楼附近，可以提供开机密码',
          contactInfo: '13800138000',
          status: 'pending',
          createdAt: '2023-11-10T08:30:00Z',
          updatedAt: '2023-11-10T08:30:00Z'
        },
        {
          id: 2,
          itemId: 202,
          itemTitle: '食堂捡到一串钥匙',
          itemType: 'found',
          itemDescription: '中午在食堂二楼捡到的钥匙，有校园卡和宿舍钥匙',
          itemOwner: '王五',
          userId: 302,
          userName: '赵六',
          claimDescription: '这是我的钥匙，丢失在食堂，钥匙上有一个小熊挂饰',
          contactInfo: '13900139000',
          status: 'approved',
          createdAt: '2023-11-09T14:20:00Z',
          updatedAt: '2023-11-09T16:45:00Z',
          processedBy: '管理员A',
          processedAt: '2023-11-09T16:45:00Z',
          processComment: '已通知双方在物业办公室交接'
        },
        {
          id: 3,
          itemId: 203,
          itemTitle: '篮球场旁捡到学生证',
          itemType: 'found',
          itemDescription: '下午在篮球场边的长椅上发现的学生证',
          itemOwner: '小明',
          userId: 303,
          userName: '小红',
          claimDescription: '这是我的学生证，上面的照片和信息都是我的',
          contactInfo: '13600136000',
          status: 'completed',
          createdAt: '2023-11-08T09:15:00Z',
          updatedAt: '2023-11-09T10:30:00Z',
          processedBy: '管理员B',
          processedAt: '2023-11-09T10:30:00Z',
          processComment: '双方已完成交接，物品已归还'
        }
      ],
      total: 3
    }

    claimsList.value = mockData.items as Claim[]
    total.value = mockData.total
  } catch (error) {
    console.error('Failed to fetch claims:', error)
    ElMessage.error('获取认领申请列表失败')
  } finally {
    loading.value = false
    tableLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
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
const viewItemDetail = (claim: Claim) => {
  // 根据物品类型和ID跳转到对应详情页面
  if (claim.itemType === 'found') {
    window.open(`/found-items/${claim.itemId}`, '_blank')
  }
}

// 查看详情
const viewDetail = (claim: Claim) => {
  selectedClaim.value = claim
  detailDialogVisible.value = true
}

// 批准认领
const approveClaim = (claim: Claim) => {
  selectedClaim.value = claim
  processForm.action = 'approve'
  processForm.comment = ''
  processingDialogVisible.value = true
}

// 拒绝认领
const rejectClaim = (claim: Claim) => {
  selectedClaim.value = claim
  processForm.action = 'reject'
  processForm.comment = ''
  processingDialogVisible.value = true
}

// 完成交接
const completeClaim = (claim: Claim) => {
  selectedClaim.value = claim
  processForm.action = 'complete'
  processForm.comment = ''
  processingDialogVisible.value = true
}

// 确认处理
const confirmProcess = async () => {
  if (!selectedClaim.value) return

  submitting.value = true
  try {
    // 这里实际开发时会调用API
    // const endpoint =
    //   processForm.action === 'approve' ? '/admin/claims/${selectedClaim.value.id}/approve' :
    //   processForm.action === 'reject' ? '/admin/claims/${selectedClaim.value.id}/reject' :
    //   '/admin/claims/${selectedClaim.value.id}/complete'
    //
    // const response = await api.put(endpoint, {
    //   comment: processForm.comment
    // })

    // 模拟处理
    await new Promise(resolve => setTimeout(resolve, 800))

    // 更新本地数据
    const index = claimsList.value.findIndex(c => c.id === selectedClaim.value?.id)
    if (index !== -1) {
      let newStatus = 'pending'
      switch (processForm.action) {
        case 'approve':
          newStatus = 'approved'
          break
        case 'reject':
          newStatus = 'rejected'
          break
        case 'complete':
          newStatus = 'completed'
          break
      }

      claimsList.value[index] = {
        ...claimsList.value[index],
        status: newStatus as 'pending' | 'approved' | 'rejected' | 'completed',
        processedBy: '当前管理员',
        processedAt: new Date().toISOString(),
        processComment: processForm.comment,
        updatedAt: new Date().toISOString()
      }
    }

    const actionText =
      processForm.action === 'approve' ? '批准' :
      processForm.action === 'reject' ? '拒绝' :
      '完成'

    ElMessage.success(`认领申请${actionText}成功`)
    processingDialogVisible.value = false
  } catch (error) {
    console.error('Failed to process claim:', error)
    ElMessage.error('处理认领申请失败')
  } finally {
    submitting.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchClaims()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.status = ''
  filterForm.itemType = ''
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
</style>
