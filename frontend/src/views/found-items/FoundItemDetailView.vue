<template>
  <main-layout>
    <div class="found-item-detail-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div style="padding: 20px">
              <el-skeleton-item
                variant="h1"
                style="width: 70%; height: 40px; margin-bottom: 20px"
              />
              <div style="display: flex; align-items: center; margin-bottom: 20px">
                <el-skeleton-item
                  variant="image"
                  style="width: 32px; height: 32px; margin-right: 16px"
                />
                <el-skeleton-item variant="text" style="width: 100px" />
              </div>
              <div style="display: flex; gap: 20px; margin-bottom: 20px">
                <el-skeleton-item variant="image" style="width: 300px; height: 200px" />
                <el-skeleton-item variant="text" style="width: 100%; height: 200px" />
              </div>
              <div style="margin-top: 30px">
                <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 10px" />
                <div v-for="i in 3" :key="i" style="margin-bottom: 20px">
                  <el-skeleton-item
                    variant="image"
                    style="width: 32px; height: 32px; margin-right: 16px; float: left"
                  />
                  <div>
                    <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 8px" />
                    <el-skeleton-item variant="text" style="width: 100%" />
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 错误状态 -->
      <el-result
        v-else-if="error"
        icon="error"
        title="获取失物招领信息失败"
        sub-title="请稍后再试或返回列表"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/found-items')">
            返回失物招领列表
          </el-button>
        </template>
      </el-result>

      <!-- 无数据状态 -->
      <el-empty v-else-if="!foundItem" description="未找到该失物招领信息">
        <el-button type="primary" @click="$router.push('/found-items')">
          返回失物招领列表
        </el-button>
      </el-empty>

      <!-- 物品详情 -->
      <template v-else>
        <div class="navigation">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/found-items' }">失物招领</el-breadcrumb-item>
            <el-breadcrumb-item>详情页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 物品卡片 -->
        <el-card class="item-card">
          <div class="item-header">
            <h1 class="item-title">
              {{ foundItem.title }}
              <el-tag type="primary" v-if="foundItem.status === 'pending'">待认领</el-tag>
              <el-tag type="warning" v-else-if="foundItem.status === 'processing'">认领中</el-tag>
              <el-tag type="success" v-else-if="foundItem.status === 'claimed'">已认领</el-tag>
            </h1>

            <div class="item-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ foundItem.username }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                发布于{{ formatDate(foundItem.createdAt) }}
              </span>
              <span class="meta-item">
                <el-button plain size="small" @click="reportItem" v-if="isLoggedIn && !isItemOwner">
                  举报
                </el-button>
              </span>
            </div>
          </div>

          <div class="item-main">
            <div class="item-gallery" v-if="foundItem.images && foundItem.images.length > 0">
              <el-carousel :interval="4000" arrow="always" height="300px">
                <el-carousel-item v-for="(image, index) in foundItem.images" :key="index">
                  <img :src="image" class="item-image" @click="previewImage(image)" />
                </el-carousel-item>
              </el-carousel>
            </div>

            <div class="item-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="拾获物品">
                  {{ foundItem.title }}
                </el-descriptions-item>
                <el-descriptions-item label="物品类别">
                  {{ getCategoryName(foundItem.category) }}
                </el-descriptions-item>
                <el-descriptions-item label="拾获时间">
                  {{ formatDate(foundItem.foundDate, true) }}
                </el-descriptions-item>
                <el-descriptions-item label="拾获地点">
                  {{ foundItem.foundLocation }}
                </el-descriptions-item>
                <el-descriptions-item v-if="foundItem.storageLocation" label="存放位置">
                  {{ foundItem.storageLocation }}
                </el-descriptions-item>
                <el-descriptions-item label="联系方式">
                  {{ foundItem.contactInfo }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <div class="item-description">
            <h3>详细描述</h3>
            <p>{{ foundItem.description }}</p>
          </div>

          <div class="item-note" v-if="foundItem.claimRequirements">
            <h3>认领要求</h3>
            <el-alert type="info" :closable="false" show-icon>
              <p>{{ foundItem.claimRequirements }}</p>
            </el-alert>
          </div>

          <div class="item-actions" v-if="foundItem.status === 'pending'">
            <el-button type="primary" @click="initiateClaimProcess" v-if="canClaim">
              这是我的物品
            </el-button>

            <div class="owner-actions" v-if="isItemOwner">
              <el-button type="primary" @click="redirectToEdit"> 编辑招领 </el-button>
              <el-button type="info" @click="closeItem"> 删除招领 </el-button>
              <el-button type="danger" @click="deleteItem"> 删除招领 </el-button>
            </div>
          </div>

          <!-- 认领中状态 -->
          <div class="item-actions" v-if="foundItem.status === 'processing'">
            <el-alert type="info" :closable="false" show-icon>
              <template v-if="isItemOwner">
                <p><strong>该物品正在被申请认领中</strong></p>
                <p>
                  请前往
                  <router-link to="/claim-communication?tab=processing">认领交流</router-link>
                  页面查看认领申请
                </p>
              </template>
              <template v-else>
                <p>该物品正在被认领中，请等待确认或查看其他物品。</p>
              </template>
            </el-alert>
          </div>

          <!-- 已认领状态 -->
          <div class="item-actions" v-if="foundItem.status === 'claimed'">
            <el-alert type="success" :closable="false" show-icon>
              <p><strong>该物品已被认领</strong></p>
            </el-alert>
          </div>
        </el-card>

        <!-- 底部操作按钮 -->
        <div class="action-buttons" v-if="isLoggedIn">
          <!-- Note: Removed duplicate report button here to keep only the one in the header -->
        </div>

        <!-- 举报对话框 -->
        <report-dialog
          v-model:visible="reportDialogVisible"
          :item-type="reportItemType"
          :item-id="reportItemId"
          :item-title="reportItemTitle"
          @report-submitted="handleReportSubmitted"
        />
      </template>

      <!-- 认领对话框 -->
      <el-dialog v-model="claimDialogVisible" title="认领流程" width="500px">
        <div class="claim-dialog-content">
          <p>如果您是该物品的失主，请填写认领申请信息：</p>

          <el-form :model="claimForm" ref="claimFormRef" label-position="top">
            <el-form-item
              label="认领说明"
              prop="description"
              :rules="[
                { required: true, message: '请填写认领说明', trigger: 'blur' },
                { min: 10, message: '说明至少包含10个字符', trigger: 'blur' },
              ]"
            >
              <el-input
                v-model="claimForm.description"
                type="textarea"
                :rows="4"
                placeholder="请详细描述物品特征或提供能证明您是物品所有者的信息"
              />
            </el-form-item>
          </el-form>

          <el-alert type="warning" :closable="false" show-icon>
            <template #title>
              认领要求：{{ foundItem?.claimRequirements || '请提供有效的物品归属证明' }}
            </template>
          </el-alert>

          <p class="claim-note">
            提交认领申请后，物品状态将变为"认领中"，其他人将无法申请认领此物品。
          </p>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="claimDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitClaimApplication" :loading="claimSubmitting"
              >提交认领申请</el-button
            >
          </div>
        </template>
      </el-dialog>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Calendar, Delete, Picture } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { ElMessage, ElMessageBox, ElImage } from 'element-plus'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { useClaimsStore } from '@/stores/claims'
import MainLayout from '@/components/layout/MainLayout.vue'
import ItemStatusTag from '@/components/common/ItemStatusTag.vue'
import ReportDialog from '@/components/ReportDialog.vue'
import type { FormInstance } from 'element-plus'

// 基本数据
const router = useRouter()
const route = useRoute()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()
const claimsStore = useClaimsStore()

// 物品详情数据
const foundItem = ref<any>(null)
const loading = ref(true)
const error = ref<boolean>(false)
const itemId = ref<number | null>(null)

// 图片查看器
const imageViewerVisible = ref(false)
const imageViewerIndex = ref(0)

// 认领相关数据
const claimDialogVisible = ref(false)
const claimForm = ref({
  description: '',
})
const claimFormRef = ref<FormInstance | null>(null)
const claimSubmitting = ref(false)

// 举报相关数据
const reportDialogVisible = ref(false)
const reportItemType = ref('FOUND_ITEM')
const reportItemId = ref<number | null>(null)
const reportItemTitle = ref('')

// 辅助状态计算
const isLoggedIn = computed(() => userStore.isAuthenticated)
const isItemOwner = computed(() => {
  return isLoggedIn.value && foundItem.value && userStore.user?.id === foundItem.value.userId
})
const canClaim = computed(() => {
  return (
    isLoggedIn.value &&
    foundItem.value &&
    foundItem.value.status === 'pending' &&
    userStore.user?.id !== foundItem.value.userId
  )
})
const isOwner = computed(() => {
  return isLoggedIn.value && foundItem.value?.userId === userStore.user?.id
})
const currentUserID = computed(() => userStore.user?.id)

// 方法
// 获取用户名首字母，用于头像
const getInitials = (username: string) => {
  return username ? username.substring(0, 2).toUpperCase() : 'U'
}

// 格式化日期
const formatDate = (dateString: string, full = false) => {
  try {
    const date = new Date(dateString)
    return full
      ? format(date, 'yyyy年MM月dd日 HH:mm:ss', { locale: zhCN })
      : format(date, 'yyyy年MM月dd日', { locale: zhCN })
  } catch (error) {
    return dateString
  }
}

// 获取物品类别名称
const getCategoryName = (category: string) => {
  const categories = {
    electronics: '电子设备',
    documents: '证件/文件',
    clothing: '衣物/包包',
    keys: '钥匙/门禁卡',
    jewelry: '首饰/配饰',
    books: '书籍/教材',
    pets: '宠物',
    other: '其他物品',
  }
  return categories[category as keyof typeof categories] || category
}

// 时间线项目类型（交替颜色）
const getTimelineItemType = (id: number) => {
  const types = ['primary', 'success', 'warning', 'info']
  return types[id % types.length]
}

// 跳转到登录页
const goToLogin = () => {
  router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

// 跳转到相关物品
const goToItem = (id: number) => {
  if (id === itemId.value) return
  router.push(`/found-items/${id}`)
}

// 预览图片
const previewImage = (url: string) => {
  ElImage.PreviewService({
    urlList: foundItem.value?.images || [url],
  })
}

// 开始认领流程
const initiateClaimProcess = () => {
  claimDialogVisible.value = true
}

// 加载物品详情
async function loadItemDetail() {
  if (!route.params.id) {
    error.value = true
    ElMessage.error('无效的物品ID')
    return
  }

  loading.value = true
  error.value = false

  try {
    console.log('正在加载失物招领详情，ID:', route.params.id)
    const parsedItemId = parseInt(route.params.id as string, 10)
    if (isNaN(parsedItemId)) {
      error.value = true
      ElMessage.error('无效的物品ID格式')
      return
    }

    itemId.value = parsedItemId

    // 获取物品详情
    const itemResult = await foundItemsStore.fetchFoundItemById(parsedItemId)
    console.log('获取物品详情API响应:', itemResult)

    if (!itemResult.success) {
      error.value = true
      ElMessage.error(itemResult.message || '获取失物招领失败')
      return
    }

    // 检查store中是否有currentItem，并更新本地的foundItem
    if (foundItemsStore.currentItem) {
      foundItem.value = { ...foundItemsStore.currentItem }
      console.log('成功加载失物招领数据:', foundItem.value)
    } else {
      console.error('物品详情加载失败: store.currentItem为空')
      error.value = true
      ElMessage.error('获取失物招领数据失败')
      return
    }

    // 滚动到顶部
    window.scrollTo(0, 0)
  } catch (err) {
    console.error('加载失物招领详情时发生错误:', err)
    error.value = true
    ElMessage.error(err instanceof Error ? err.message : '获取失物招领详情时发生错误')
  } finally {
    loading.value = false
  }
}

// 关闭失物招领
function closeItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!itemId.value) {
    ElMessage.warning('无效的物品ID')
    return
  }

  ElMessageBox.confirm('确认删除此失物招领吗？', '确认操作', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info',
  })
    .then(async () => {
      try {
        // 使用专用的关闭函数
        const id = itemId.value
        if (!id) {
          ElMessage.warning('物品ID无效')
          return
        }

        const result = await foundItemsStore.closeItem(id)

        if (result.success) {
          ElMessage.success('失物招领已删除')
          // 跳转到失物招领列表页
          router.push('/found-items')
        } else {
          ElMessage.error(result.message || '操作失败')
        }
      } catch (error) {
        console.error('删除失物招领失败:', error)
        ElMessage.error('操作失败，请稍后再试')
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 编辑物品信息
function redirectToEdit() {
  if (!itemId.value) {
    ElMessage.warning('无效的物品ID')
    return
  }
  router.push(`/found-items/edit/${itemId.value}`)
}

// 删除物品
function deleteItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!itemId.value) {
    ElMessage.warning('无效的物品ID')
    return
  }

  ElMessageBox.confirm('确认删除此失物招领吗？此操作不可撤销。', '警告', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        const id = itemId.value
        if (!id) {
          ElMessage.warning('物品ID无效')
          return
        }

        const result = await foundItemsStore.deleteFoundItem(id)

        if (result.success) {
          ElMessage.success('失物招领已删除')
          router.push('/found-items')
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失物招领失败:', error)
        ElMessage.error('删除失败，请稍后再试')
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 路由参数变化时重新加载数据
watch(
  () => route.params.id,
  () => {
    if (itemId.value) {
      loadItemDetail()
    }
  },
)

onMounted(() => {
  loadItemDetail()
})

// 提交认领申请
const submitClaimApplication = async () => {
  if (!claimFormRef.value) return

  try {
    // 手动验证表单
    const valid = await claimFormRef.value.validate().catch(() => false)
    if (!valid || !foundItem.value || !itemId.value) {
      return
    }

    claimSubmitting.value = true
    const result = await claimsStore.applyForClaim(itemId.value, {
      description: claimForm.value.description,
    })

    if (result.success) {
      ElMessage.success('认领申请已提交成功')
      claimDialogVisible.value = false

      // 重新加载物品详情，显示最新状态
      await loadItemDetail()
    } else {
      ElMessage.error(result.message || '提交认领申请失败')
    }
  } catch (error) {
    console.error('Failed to submit claim application:', error)
    ElMessage.error('提交认领申请时发生错误，请稍后再试')
  } finally {
    claimSubmitting.value = false
  }
}

// 举报物品
function reportItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!foundItem.value) {
    ElMessage.warning('找不到物品信息')
    return
  }

  reportItemType.value = 'FOUND_ITEM'
  reportItemId.value = foundItem.value.id
  reportItemTitle.value = foundItem.value.title
  reportDialogVisible.value = true
}

// 处理举报提交后的操作
function handleReportSubmitted(report: any) {
  console.log('举报已提交:', report)
}
</script>

<style scoped>
.found-item-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.navigation {
  margin-bottom: 20px;
}

.item-card {
  margin-bottom: 30px;
}

.item-header {
  margin-bottom: 20px;
}

.item-title {
  font-size: 24px;
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-meta {
  display: flex;
  color: #606266;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.meta-item .el-icon {
  margin-right: 5px;
}

.item-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.item-gallery {
  border-radius: 4px;
  overflow: hidden;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.item-description {
  margin-bottom: 20px;
}

.item-description h3,
.item-note h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
}

.item-description p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.item-note {
  margin-bottom: 30px;
}

.item-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.owner-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.related-items {
  margin-top: 40px;
}

.related-item-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.related-item-card:hover {
  transform: translateY(-5px);
}

.related-item-image {
  height: 150px;
  overflow: hidden;
  margin-bottom: 10px;
}

.related-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-item-title {
  margin: 0 0 10px;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.claim-dialog-content p {
  margin-bottom: 15px;
}

.claim-steps {
  padding-left: 20px;
  margin-bottom: 20px;
}

.claim-steps li {
  margin-bottom: 15px;
}

.claim-steps p {
  margin: 5px 0 0 0;
  color: #606266;
}

.claim-note {
  margin-top: 20px;
  color: #909399;
}

.color-note {
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .item-main {
    grid-template-columns: 1fr;
  }

  .owner-actions {
    flex-direction: column;
    width: 100%;
  }

  .owner-actions .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
