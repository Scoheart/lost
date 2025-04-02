<template>
  <main-layout>
    <div class="claim-communications-container">
      <div class="page-header">
        <h1>认领交流</h1>
        <p>在这里您可以查看和管理自己提交的认领申请，以及处理他人对您发布物品的认领申请</p>
      </div>

      <div class="actions-container">
        <el-card class="claim-card">
          <el-tabs v-model="activeTab" class="tab-container">
            <el-tab-pane label="我的申请" name="myApplications">
              <!-- 我的申请标签页 -->
              <div class="tab-content">
                <div class="filter-container">
                  <el-select
                    v-model="myApplicationsFilter"
                    placeholder="申请状态"
                    clearable
                    class="filter-select"
                    @change="loadMyApplications"
                  >
                    <el-option label="全部申请" value="" />
                    <el-option label="待处理" value="pending" />
                    <el-option label="已批准" value="approved" />
                    <el-option label="已拒绝" value="rejected" />
                  </el-select>
                </div>

                <div class="application-list" v-loading="myApplicationsLoading">
                  <div v-if="myApplications.length === 0" class="empty-list">
                    <el-empty description="暂无认领申请，您可以在失物招领页面申请认领物品" />
                  </div>

                  <div v-else>
                    <el-row :gutter="20">
                      <el-col
                        v-for="item in myApplications"
                        :key="item.id"
                        :xs="24"
                        :sm="12"
                        :md="8"
                        :lg="8"
                        :xl="6"
                      >
                        <div class="application-item">
                          <div class="item-header">
                            <div class="item-image">
                              <el-image
                                v-if="item.foundItemImage"
                                :src="item.foundItemImage"
                                fit="cover"
                              />
                              <div v-else class="no-image">
                                <el-icon><Picture /></el-icon>
                              </div>
                            </div>
                            <div class="item-info">
                              <h3 class="item-title">{{ item.foundItemTitle }}</h3>
                              <p class="item-meta">
                                <span>申请时间: {{ formatDate(item.createdAt) }}</span>
                                <span>发布者: {{ item.ownerName }}</span>
                              </p>
                              <p class="item-description">{{ item.description }}</p>

                              <!-- 添加举报按钮 -->
                              <div class="report-action" v-if="item.ownerId !== userStore.user?.id">
                                <el-button
                                  type="text"
                                  size="small"
                                  @click="reportApplication(item)"
                                  title="举报申请"
                                >
                                  <!-- <el-icon><Warning /></el-icon> 举报 -->
                                </el-button>
                              </div>
                            </div>
                            <div class="item-status">
                              <el-tag :type="getStatusType(item.status)" effect="light">
                                {{ getStatusText(item.status) }}
                              </el-tag>
                            </div>
                          </div>

                          <div class="item-footer" v-if="item.status === 'approved'">
                            <el-alert type="success" :closable="false" show-icon>
                              <template #title>
                                您的认领申请已被批准，请联系物品所有者完成认领
                              </template>
                              <p v-if="item.ownerName">联系人：{{ item.ownerName }}</p>
                              <p v-if="item.ownerContact">联系方式：{{ item.ownerContact }}</p>
                            </el-alert>
                          </div>
                        </div>
                      </el-col>
                    </el-row>

                    <div class="pagination-container">
                      <el-pagination
                        background
                        layout="prev, pager, next"
                        :total="myApplicationsTotalItems"
                        :page-size="pageSize"
                        :current-page="myApplicationsCurrentPage"
                        @current-change="handleMyApplicationsPageChange"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="待处理申请" name="processingApplications">
              <!-- 待处理申请标签页 -->
              <div class="tab-content">
                <div class="filter-container">
                  <el-select
                    v-model="processingApplicationsFilter"
                    placeholder="申请状态"
                    clearable
                    class="filter-select"
                    @change="loadProcessingApplications"
                  >
                    <el-option label="全部申请" value="" />
                    <el-option label="待处理" value="pending" />
                    <el-option label="已批准" value="approved" />
                    <el-option label="已拒绝" value="rejected" />
                  </el-select>
                </div>

                <div class="application-list" v-loading="processingApplicationsLoading">
                  <div v-if="processingApplications.length === 0" class="empty-list">
                    <el-empty
                      description="暂无待处理的认领申请，当其他用户申请认领您发布的失物招领时，将会在此显示"
                    />
                  </div>

                  <div v-else>
                    <el-row :gutter="20">
                      <el-col
                        v-for="item in processingApplications"
                        :key="item.id"
                        :xs="24"
                        :sm="12"
                        :md="8"
                        :lg="8"
                        :xl="6"
                      >
                        <div class="application-item">
                          <div class="item-header">
                            <div class="item-image">
                              <el-image
                                v-if="item.foundItemImage"
                                :src="item.foundItemImage"
                                fit="cover"
                              />
                              <div v-else class="no-image">
                                <el-icon><Picture /></el-icon>
                              </div>
                            </div>
                            <div class="item-info">
                              <h3 class="item-title">{{ item.foundItemTitle }}</h3>
                              <p class="item-meta">
                                <span>申请人: {{ item.applicantName }}</span>
                                <span>申请时间: {{ formatDate(item.createdAt) }}</span>
                              </p>
                              <p class="item-description">{{ item.description }}</p>

                              <!-- 添加举报按钮 -->
                              <div
                                class="report-action"
                                v-if="item.applicantId !== userStore.user?.id"
                              >
                                <el-button
                                  type="text"
                                  size="small"
                                  @click="reportApplication(item)"
                                  title="举报申请"
                                >
                                  <!-- <el-icon><Warning /></el-icon> 举报 -->
                                </el-button>
                              </div>
                            </div>
                            <div class="item-status">
                              <div v-if="item.status === 'pending'" class="action-buttons">
                                <el-button
                                  type="primary"
                                  size="small"
                                  @click="approveApplication(item.id)"
                                  :loading="loadingStates.get(`approve-${item.id}`)"
                                >
                                  同意认领
                                </el-button>
                                <el-button
                                  type="danger"
                                  size="small"
                                  @click="rejectApplication(item.id)"
                                  :loading="loadingStates.get(`reject-${item.id}`)"
                                >
                                  拒绝
                                </el-button>
                              </div>

                              <el-tag v-else :type="getStatusType(item.status)" effect="light">
                                {{ getStatusText(item.status) }}
                              </el-tag>
                            </div>
                          </div>

                          <div class="item-footer" v-if="item.status === 'approved'">
                            <el-alert type="success" :closable="false" show-icon>
                              <p v-if="item.applicantName">认领人：{{ item.applicantName }}</p>
                              <p v-if="item.applicantContact">
                                联系方式：{{ item.applicantContact }}
                              </p>
                            </el-alert>
                          </div>
                        </div>
                      </el-col>
                    </el-row>

                    <div class="pagination-container">
                      <el-pagination
                        background
                        layout="prev, pager, next"
                        :total="processingApplicationsTotalItems"
                        :page-size="pageSize"
                        :current-page="processingApplicationsCurrentPage"
                        @current-change="handleProcessingApplicationsPageChange"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>

      <!-- 举报对话框 -->
      <report-dialog
        v-model="reportDialogVisible"
        :item-type="reportItemType"
        :item-id="reportItemId"
        :item-title="reportItemTitle"
        @submitted="handleReportSubmitted"
      />
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, reactive } from 'vue'
import { Picture, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useClaimsStore } from '@/stores/claims'
import { useUserStore } from '@/stores/user'
import { format } from 'date-fns'
import MainLayout from '@/components/layout/MainLayout.vue'
import ReportDialog from '@/components/ReportDialog.vue'

const router = useRouter()
const route = useRoute()
const claimsStore = useClaimsStore()
const userStore = useUserStore()

// 标签页控制
const activeTab = ref('myApplications')

// 分页
const pageSize = 10

// 我的申请
const myApplicationsFilter = ref('')
const myApplicationsCurrentPage = computed(() => claimsStore.myApplications.currentPage)
const myApplicationsTotalItems = computed(() => claimsStore.myApplications.totalItems)
const myApplications = computed(() => claimsStore.myApplications.list)
const myApplicationsLoading = computed(() => claimsStore.myApplications.loading)

// 待处理申请
const processingApplicationsFilter = ref('')
const processingApplicationsCurrentPage = computed(
  () => claimsStore.processingApplications.currentPage,
)
const processingApplicationsTotalItems = computed(
  () => claimsStore.processingApplications.totalItems,
)
const processingApplications = computed(() => claimsStore.processingApplications.list)
const processingApplicationsLoading = computed(() => claimsStore.processingApplications.loading)

// 加载状态跟踪 - 使用 reactive Map
const loadingStates = reactive(new Map<string, boolean>())

// 添加举报相关变量
const reportDialogVisible = ref(false)
const reportItemType = ref('')
const reportItemId = ref(null)
const reportItemTitle = ref('')

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'myApplications') {
    loadMyApplications()
    // 更新URL查询参数
    router.replace({ path: '/claim-communications', query: { tab: 'myApplications' } })
  } else if (newTab === 'processingApplications') {
    loadProcessingApplications()
    // 更新URL查询参数
    router.replace({ path: '/claim-communications', query: { tab: 'processing' } })
  }
})

// 初始化加载
onMounted(() => {
  // 在URL有查询参数tab时，切换到对应标签页
  const query = route.query
  if (query.tab === 'processing') {
    activeTab.value = 'processingApplications'
    loadProcessingApplications()
  } else {
    // 无tab参数或tab=myApplications时显示我的申请页
    activeTab.value = 'myApplications'
    loadMyApplications()

    // 如果没有查询参数，添加默认查询参数
    if (!query.tab) {
      router.replace({ path: '/claim-communications', query: { tab: 'myApplications' } })
    }
  }
})

/**
 * 格式化日期
 */
const formatDate = (dateString: string): string => {
  if (!dateString) return ''

  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm')
  } catch (error) {
    console.error('Date formatting error:', error)
    return dateString
  }
}

/**
 * 获取状态显示文本
 */
const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    approved: '已批准',
    rejected: '已拒绝',
  }
  return statusMap[status] || status
}

/**
 * 获取状态标签类型
 */
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    pending: 'info',
    approved: 'success',
    rejected: 'danger',
  }
  return typeMap[status] || 'info'
}

/**
 * 加载我的认领申请
 */
const loadMyApplications = async (): Promise<void> => {
  try {
    await claimsStore.fetchMyApplications({
      status: myApplicationsFilter.value,
      page: myApplicationsCurrentPage.value,
    })
  } catch (error) {
    console.error('Failed to load my applications:', error)
  }
}

/**
 * 加载待处理认领申请
 */
const loadProcessingApplications = async (): Promise<void> => {
  try {
    await claimsStore.fetchProcessingApplications({
      status: processingApplicationsFilter.value,
      page: processingApplicationsCurrentPage.value,
    })
  } catch (error) {
    console.error('Failed to load processing applications:', error)
  }
}

/**
 * 处理我的申请分页变化
 */
const handleMyApplicationsPageChange = (page: number): void => {
  claimsStore.fetchMyApplications({
    status: myApplicationsFilter.value,
    page: page,
  })
}

/**
 * 处理待处理申请分页变化
 */
const handleProcessingApplicationsPageChange = (page: number): void => {
  claimsStore.fetchProcessingApplications({
    status: processingApplicationsFilter.value,
    page: page,
  })
}

/**
 * 批准认领申请
 */
const approveApplication = async (applicationId: number): Promise<void> => {
  try {
    ElMessageBox.confirm(
      '确定批准该认领申请吗？批准后物品状态将变为"已认领"，您将无法批准其他申请。',
      '批准认领申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
      .then(async () => {
        try {
          // 设置本地加载状态
          loadingStates.set(`approve-${applicationId}`, true)

          await claimsStore.approveApplication(applicationId)
          await loadProcessingApplications()
        } catch (error) {
          console.error('Failed to approve application:', error)
        } finally {
          // 清除本地加载状态
          loadingStates.delete(`approve-${applicationId}`)
        }
      })
      .catch(() => {
        // 用户取消
      })
  } catch (error) {
    console.error('Error showing confirmation dialog:', error)
  }
}

/**
 * 拒绝认领申请
 */
const rejectApplication = async (applicationId: number): Promise<void> => {
  try {
    ElMessageBox.confirm(
      '确定拒绝该认领申请吗？拒绝后该申请将被关闭，物品将回到"待认领"状态。',
      '拒绝认领申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
      .then(async () => {
        try {
          // 设置本地加载状态
          loadingStates.set(`reject-${applicationId}`, true)

          await claimsStore.rejectApplication(applicationId)
          await loadProcessingApplications()
        } catch (error) {
          console.error('Failed to reject application:', error)
        } finally {
          // 清除本地加载状态
          loadingStates.delete(`reject-${applicationId}`)
        }
      })
      .catch(() => {
        // 用户取消
      })
  } catch (error) {
    console.error('Error showing confirmation dialog:', error)
  }
}

/**
 * 举报认领申请
 */
const reportApplication = (application) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  reportItemType.value = 'COMMENT' // 使用COMMENT类型，因为后端API支持的类型有限
  reportItemId.value = application.id

  // 提供更详细的举报描述
  let itemType = '未知物品'
  let itemTitle = ''

  if (application.foundItemTitle) {
    itemType = '失物招领'
    itemTitle = application.foundItemTitle
  } else if (application.lostItemTitle) {
    itemType = '寻物启事'
    itemTitle = application.lostItemTitle
  }

  const username = application.applicantName || application.ownerName || '用户'
  reportItemTitle.value = `认领申请 - ${itemType}「${itemTitle}」- ${username}`
  reportDialogVisible.value = true
}

/**
 * 处理举报提交
 */
const handleReportSubmitted = (report) => {
  ElMessage.success('举报已提交，感谢您的反馈')
  console.log('举报已提交:', report)
}
</script>

<style scoped>
.claim-communications-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

/* 筛选区域样式 */
.actions-container {
  margin-bottom: 20px;
}

.claim-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.tab-container {
  margin-top: 10px;
}

.tab-content {
  margin-top: 15px;
}

.filter-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  justify-content: center;
}

.filter-select {
  width: 200px;
}

.application-list {
  min-height: 300px;
}

.empty-list {
  padding: 40px 0;
  text-align: center;
}

.application-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  height: 100%;
  transition:
    box-shadow 0.3s,
    transform 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.application-item:hover {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-header {
  flex: 1;
}

.item-image {
  width: 100%;
  height: 180px;
  margin-bottom: 12px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #909399;
  font-size: 36px;
}

.item-info {
  margin-bottom: 12px;
}

.item-title {
  font-size: 16px;
  margin: 0 0 10px 0;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.item-meta span {
  margin-bottom: 4px;
}

.item-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-status {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.item-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .claim-communications-container {
    padding: 10px;
  }

  .action-buttons {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }

  .filter-container {
    flex-direction: column;
    align-items: center;
  }

  .filter-select {
    width: 100%;
    margin-bottom: 10px;
  }
}

.message-author {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.author-name {
  font-weight: 500;
  flex-grow: 1;
}

/* 添加留言操作按钮样式 */
.message-actions {
  margin-left: auto;
}

.message-actions .el-button {
  padding: 2px 5px;
  color: #909399;
}

.message-actions .el-button:hover {
  color: #f56c6c;
}

.report-action {
  margin-top: 8px;
  text-align: right;
}

.report-action .el-button {
  color: #909399;
  padding: 2px 5px;
}

.report-action .el-button:hover {
  color: #f56c6c;
}
</style>
