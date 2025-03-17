<template>
  <main-layout>
    <div class="found-item-detail-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div style="padding: 20px;">
              <el-skeleton-item variant="h1" style="width: 70%; height: 40px; margin-bottom: 20px;" />
              <div style="display: flex; align-items: center; margin-bottom: 20px;">
                <el-skeleton-item variant="image" style="width: 32px; height: 32px; margin-right: 16px;" />
                <el-skeleton-item variant="text" style="width: 100px;" />
              </div>
              <div style="display: flex; gap: 20px; margin-bottom: 20px">
                <el-skeleton-item variant="image" style="width: 300px; height: 200px" />
                <el-skeleton-item variant="text" style="width: 100%; height: 200px" />
              </div>
              <div style="margin-top: 30px;">
                <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 10px;" />
                <div v-for="i in 3" :key="i" style="margin-bottom: 20px;">
                  <el-skeleton-item variant="image" style="width: 32px; height: 32px; margin-right: 16px; float: left;" />
                  <div>
                    <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 8px;" />
                    <el-skeleton-item variant="text" style="width: 100%;" />
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
      <el-empty
        v-else-if="!foundItem"
        description="未找到该失物招领信息"
      >
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
                <el-button
                  type="text"
                  size="small"
                  @click="reportItem"
                  v-if="isLoggedIn && !isOwner"
                >
                  <el-icon><Warning /></el-icon> 举报
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
            <el-alert
              type="info"
              :closable="false"
              show-icon
            >
              <p>{{ foundItem.claimRequirements }}</p>
            </el-alert>
          </div>

          <div class="item-actions" v-if="foundItem.status === 'pending'">
            <el-button
              type="primary"
              @click="initiateClaimProcess"
              v-if="!isOwner"
            >
              这是我的物品
            </el-button>

            <div class="owner-actions" v-if="isOwner">
              <el-button
                type="primary"
                @click="redirectToEdit"
              >
                编辑招领
              </el-button>
              <el-button
                type="info"
                @click="closeItem"
              >
                删除招领
              </el-button>
              <el-button
                type="danger"
                @click="deleteItem"
              >
                删除招领
              </el-button>
            </div>
          </div>

          <!-- 认领中状态 -->
          <div class="item-actions" v-if="foundItem.status === 'processing'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
            >
              <template v-if="isOwner">
                <p><strong>该物品正在被申请认领中</strong></p>
                <p>请前往 <router-link to="/claim-communication?tab=processing">认领交流</router-link> 页面查看认领申请</p>
              </template>
              <template v-else>
                <p>该物品正在被认领中，请等待确认或查看其他物品。</p>
              </template>
            </el-alert>
          </div>

          <!-- 已认领状态 -->
          <div class="item-actions" v-if="foundItem.status === 'claimed'">
            <el-alert
              type="success"
              :closable="false"
              show-icon
            >
              <p><strong>该物品已被认领</strong></p>
            </el-alert>
          </div>
        </el-card>

        <!-- 评论区 -->
        <div class="comments-section">
          <h2 class="section-title">留言板 ({{ totalComments }}条)</h2>

          <!-- 评论列表 -->
          <div class="comments-list" v-if="comments.length > 0">
            <el-timeline>
              <el-timeline-item
                v-for="comment in comments"
                :key="comment.id"
                :timestamp="formatDate(comment.createdAt, true)"
                :type="getTimelineItemType(comment.id)"
              >
                <el-card class="comment-card">
                  <div class="comment-author">
                    <el-avatar :size="32" :src="comment.userAvatar || ''">{{ getInitials(comment.username) }}</el-avatar>
                    <span class="author-name">{{ comment.username }}</span>
                    <span class="comment-actions" v-if="isLoggedIn && userStore.user.id !== comment.userId">
                      <el-button
                        type="text"
                        size="small"
                        @click="reportComment(comment)"
                        title="举报此评论"
                      >
                        <el-icon><Warning /></el-icon>
                      </el-button>
                    </span>
                  </div>
                  <div class="comment-content">
                    {{ comment.content }}
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>

            <!-- 分页 -->
            <div class="pagination-container" v-if="totalComments > pageSize">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[5, 10, 20, 50]"
                :total="totalComments"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </div>

          <!-- 无评论提示 -->
          <el-empty
            v-else
            description="暂无留言"
            :image-size="100"
          >
          </el-empty>

          <!-- 评论输入框 -->
          <el-card class="comment-form-card" v-if="isLoggedIn">
            <div class="comment-form-header">
              <h3>发表留言</h3>
              <p>如果您认为这是您的物品，请描述细节以证明物品归属</p>
            </div>
            <el-form :model="commentForm" @submit.prevent="submitComment">
              <el-form-item>
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="3"
                  :maxlength="500"
                  show-word-limit
                  placeholder="写下您的留言，如需认领请提供能够证明物品归属的信息..."
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitComment"
                  :loading="commentSubmitting"
                  :disabled="!commentForm.content.trim()"
                >
                  发表留言
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 登录提示 -->
          <el-alert
            v-else
            title="登录后才能发表留言"
            type="info"
            :closable="false"
            center
            show-icon
            style="margin-top: 20px;"
          >
            <template #default>
              <el-button
                type="primary"
                @click="goToLogin"
                plain
                size="small"
                style="margin-left: 10px;"
              >
                去登录
              </el-button>
            </template>
          </el-alert>
        </div>

        <!-- 相似物品 -->
        <div class="related-items" v-if="relatedItems.length > 0">
          <h2 class="section-title">相似拾获物品</h2>
          <el-row :gutter="20">
            <el-col
              v-for="item in relatedItems"
              :key="item.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <el-card
                shadow="hover"
                class="related-item-card"
                @click="goToItem(item.id)"
              >
                <div class="related-item-image">
                  <img :src="item.images && item.images.length > 0 ? item.images[0] : '/placeholder.jpg'" />
                </div>
                <h3 class="related-item-title">{{ item.title }}</h3>
                <div class="related-item-footer">
                  <span class="related-item-location">{{ item.foundLocation }}</span>
                  <el-tag size="small" type="primary" v-if="item.status === 'pending'">招领中</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 认领对话框 -->
      <el-dialog
        v-model="claimDialogVisible"
        title="认领流程"
        width="500px"
      >
        <div class="claim-dialog-content">
          <p>如果您是该物品的失主，请填写认领申请信息：</p>

          <el-form :model="claimForm" ref="claimFormRef" label-position="top">
            <el-form-item
              label="认领说明"
              prop="description"
              :rules="[
                { required: true, message: '请填写认领说明', trigger: 'blur' },
                { min: 10, message: '说明至少包含10个字符', trigger: 'blur' }
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

          <el-alert
            type="warning"
            :closable="false"
            show-icon
          >
            <template #title>
              认领要求：{{ foundItem?.claimRequirements || '请提供有效的物品归属证明' }}
            </template>
          </el-alert>

          <p class="claim-note">提交认领申请后，物品状态将变为"认领中"，其他人将无法申请认领此物品。</p>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="claimDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitClaimApplication" :loading="claimSubmitting">提交认领申请</el-button>
          </div>
        </template>
      </el-dialog>

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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Calendar, Picture, Warning } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { ElMessage, ElMessageBox, ElImage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { useClaimsStore } from '@/stores/claims'
import type { FoundItem } from '@/stores/foundItems'
import type { Comment } from '@/stores/lostItems'
import ReportDialog from '@/components/ReportDialog.vue'

const router = useRouter()
const route = useRoute()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()
const claimsStore = useClaimsStore()

// 状态变量
const loading = ref(false)
const error = ref<string | null>(null)
const commentForm = ref({
  content: ''
})
const commentSubmitting = ref(false)
const claimDialogVisible = ref(false)
const claimSubmitting = ref(false)
const claimForm = ref({
  description: ''
})
const claimFormRef = ref<any>(null)
const actionLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const reportDialogVisible = ref(false)
const reportItemType = ref('')
const reportItemId = ref(null)
const reportItemTitle = ref('')

// 计算属性
const itemId = computed(() => {
  const id = Number(route.params.id)
  return isNaN(id) ? null : id
})

const foundItem = computed(() => foundItemsStore.currentItem as FoundItem | null)
const comments = computed(() => foundItemsStore.comments as Comment[])
const isLoggedIn = computed(() => userStore.isAuthenticated)
const user = computed(() => userStore.userProfile)

const isOwner = computed(() => {
  if (!isLoggedIn.value || !foundItem.value || !user.value) return false
  return foundItem.value.userId === user.value.id
})

// 认领信息
const claimInfo = computed(() => foundItem.value?.claimInfo || null)

// 获取相关物品（同类别的其他拾获物品）
const relatedItems = computed(() => {
  if (!foundItem.value) return []
  return foundItemsStore.items
    .filter(item =>
      item.id !== foundItem.value!.id &&
      item.category === foundItem.value!.category &&
      item.status === 'pending'
    )
    .slice(0, 4) // 最多显示4个相关物品
})

const totalComments = computed(() => foundItemsStore.totalComments)

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
    'electronics': '电子设备',
    'documents': '证件/文件',
    'clothing': '衣物/包包',
    'keys': '钥匙/门禁卡',
    'jewelry': '首饰/配饰',
    'books': '书籍/教材',
    'pets': '宠物',
    'other': '其他物品'
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
    urlList: foundItem.value?.images || [url]
  })
}

// 开始认领流程
const initiateClaimProcess = () => {
  claimDialogVisible.value = true
}

// 加载物品详情
const loadItemDetail = async () => {
  try {
    loading.value = true;
    error.value = null;

    // 获取物品ID
    const itemId = Number(route.params.id);
    if (isNaN(itemId)) {
      error.value = '无效的物品ID';
      return;
    }

    // 分别获取物品详情和评论
    const itemResult = await foundItemsStore.fetchFoundItemById(itemId);

    if (!itemResult.success) {
      error.value = itemResult.message || '获取失物招领失败';
      return;
    }

    // 物品详情获取成功后，获取评论
    const commentsResult = await foundItemsStore.fetchComments(
      itemId,
      currentPage.value,
      pageSize.value
    );

    // 如果评论获取失败，只记录警告但继续显示物品详情
    if (!commentsResult.success) {
      console.warn('Failed to fetch comments:', commentsResult.message);
      // 不阻止页面显示，只显示物品信息
    }

    // 获取相关物品
    if (foundItem.value && foundItem.value.category) {
      await foundItemsStore.fetchFoundItems({
        category: foundItem.value.category,
        page: 1,
        pageSize: 4
      });
    }

    // 滚动到顶部
    window.scrollTo(0, 0);
  } catch (err) {
    console.error('Error loading found item details:', err);
    error.value = err instanceof Error ? err.message : '获取失物招领详情时发生错误';
  } finally {
    loading.value = false;
  }
};

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    return;
  }

  if (!itemId.value) {
    ElMessage.error('物品ID无效');
    return;
  }

  commentSubmitting.value = true;
  try {
    const result = await foundItemsStore.addComment(itemId.value, {
      content: commentForm.value.content
    });

    if (result.success) {
      commentForm.value.content = ''; // 清空评论内容
      currentPage.value = 1; // 重置到第一页以便查看新评论

      // 重新加载评论
      if (itemId.value) {
        await foundItemsStore.fetchComments(itemId.value, currentPage.value, pageSize.value);
      }

      ElMessage.success('评论成功');
    } else {
      ElMessage.error(result.message || '评论失败')
    }
  } catch (error) {
    console.error('Failed to submit comment:', error)
    ElMessage.error('评论失败，请稍后再试')
  } finally {
    commentSubmitting.value = false
  }
}

// 关闭失物招领
function closeItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  ElMessageBox.confirm(
    '确认删除此失物招领吗？',
    '确认操作',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      // 使用专用的关闭函数
      const result = await foundItemsStore.closeItem(itemId.value)

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
  }).catch(() => {
    // 用户取消操作
  })
}

// 编辑物品信息
function redirectToEdit() {
  if (!itemId.value) return
  router.push(`/found-items/edit/${itemId.value}`)
}

// 删除物品
function deleteItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  ElMessageBox.confirm(
    '确认删除此失物招领吗？此操作不可撤销。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const result = await foundItemsStore.deleteFoundItem(itemId.value)

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
  }).catch(() => {
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
  }
)

onMounted(() => {
  loadItemDetail()
})

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  loadItemDetail()
}

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage
  loadItemDetail()
}

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
    await claimsStore.applyForClaim(itemId.value, {
      description: claimForm.value.description
    })

    claimDialogVisible.value = false

    // 重新加载物品详情，显示最新状态
    await loadItemDetail()
  } catch (error) {
    console.error('Failed to submit claim application:', error)
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

  reportItemType.value = 'FOUND_ITEM'
  reportItemId.value = foundItem.value.id
  reportItemTitle.value = foundItem.value.title
  reportDialogVisible.value = true
}

// 举报评论
function reportComment(comment) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  reportItemType.value = 'COMMENT'
  reportItemId.value = comment.id
  reportItemTitle.value = `评论: ${comment.content.substring(0, 20)}${comment.content.length > 20 ? '...' : ''}`
  reportDialogVisible.value = true
}

// 处理举报提交后的操作
function handleReportSubmitted(report) {
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

.item-description h3, .item-note h3 {
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

.comments-section {
  margin-bottom: 40px;
}

.comments-list {
  margin-bottom: 20px;
}

.comment-card {
  margin-bottom: 10px;
}

.comment-author {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.author-name {
  margin-left: 8px;
  font-weight: 500;
  flex-grow: 1;
}

.comment-content {
  color: #303133;
  line-height: 1.6;
}

.comment-form-card {
  margin-top: 20px;
}

.comment-form-header {
  margin-bottom: 15px;
}

.comment-form-header h3 {
  margin: 0 0 5px;
  font-size: 16px;
}

.comment-form-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
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

.comment-actions {
  margin-left: auto;
}

.comment-actions .el-button {
  padding: 2px 5px;
  color: #909399;
}

.comment-actions .el-button:hover {
  color: #f56c6c;
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
