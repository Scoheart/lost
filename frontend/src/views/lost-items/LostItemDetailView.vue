<template>
  <main-layout>
    <div class="lost-item-detail-container">
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
        title="获取寻物启事失败"
        sub-title="请稍后再试或返回列表"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/lost-items')">
            返回寻物启事列表
          </el-button>
        </template>
      </el-result>

      <!-- 无数据状态 -->
      <el-empty v-else-if="!lostItem" description="未找到该寻物启事">
        <el-button type="primary" @click="$router.push('/lost-items')">
          返回寻物启事列表
        </el-button>
      </el-empty>

      <!-- 物品详情 -->
      <template v-else>
        <div class="navigation">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/lost-items' }">寻物启事</el-breadcrumb-item>
            <el-breadcrumb-item>详情页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 物品卡片 -->
        <el-card class="item-card">
          <div class="item-header">
            <h1 class="item-title">
              {{ lostItem.title }}
            </h1>
            <div class="item-meta">
              <span class="item-category">{{ getCategoryName(lostItem.category) }}</span>
              <span class="item-date">发布于: {{ formatDate(lostItem.createdAt) }}</span>
              <span class="meta-item">
                <el-button
                  type="text"
                  size="small"
                  @click="reportItem"
                  v-if="isLoggedIn && !isItemOwner"
                >
                  <el-icon><CircleClose /></el-icon> 举报
                </el-button>
              </span>
            </div>
          </div>

          <div class="item-main">
            <div class="item-gallery" v-if="lostItem.images && lostItem.images.length > 0">
              <el-carousel :interval="4000" arrow="always" height="300px">
                <el-carousel-item v-for="(image, index) in lostItem.images" :key="index">
                  <img :src="image" class="item-image" @click="previewImage(image)" />
                </el-carousel-item>
              </el-carousel>
            </div>

            <div class="item-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="丢失物品">
                  {{ lostItem.title }}
                </el-descriptions-item>
                <el-descriptions-item label="物品类别">
                  {{ getCategoryName(lostItem.category) }}
                </el-descriptions-item>
                <el-descriptions-item label="丢失时间">
                  {{
                    lostItem.lostDate
                      ? formatDate(lostItem.lostDate, true)
                      : formatDate(lostItem.createdAt, true)
                  }}
                </el-descriptions-item>
                <el-descriptions-item label="丢失地点">
                  {{ lostItem.lostLocation }}
                </el-descriptions-item>
                <el-descriptions-item label="悬赏金额" v-if="lostItem.reward">
                  <span class="reward">¥ {{ lostItem.reward }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="联系方式">
                  {{ lostItem.contactInfo }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <div class="item-description">
            <h3>详细描述</h3>
            <p>{{ lostItem.description }}</p>
          </div>

          <div class="item-actions">
            <el-button type="primary" @click="contactOwner" v-if="!isOwner">
              我有这个物品
            </el-button>

            <div class="owner-actions" v-if="isOwner">
              <el-button type="primary" @click="redirectToEdit"> 编辑寻物启事 </el-button>
              <el-button type="danger" @click="deleteItem"> 删除寻物启事 </el-button>
            </div>
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
                    <el-avatar :size="32" :src="comment.userAvatar || ''">{{
                      getInitials(comment.username)
                    }}</el-avatar>
                    <span class="author-name">{{ comment.username }}</span>
                    <span class="comment-actions">
                      <!-- 评论举报按钮 - 仅对他人评论显示 -->
                      <el-button
                        type="text"
                        size="small"
                        @click="reportComment(comment)"
                        v-if="isAuthenticated && userStore.user?.id !== comment.userId"
                        title="举报此评论"
                      >
                        举报
                      </el-button>

                      <!-- 评论删除按钮 - 仅对自己的评论显示 -->
                      <el-button
                        type="text"
                        size="small"
                        @click="deleteComment(comment)"
                        v-if="isAuthenticated && userStore.user?.id === comment.userId"
                        title="删除此评论"
                      >
                        <el-icon><Delete /></el-icon>
                        删除
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
          <el-empty v-else description="暂无留言" :image-size="100"> </el-empty>

          <!-- 评论输入框 -->
          <el-card class="comment-form-card" v-if="isLoggedIn">
            <div class="comment-form-header">
              <h3>发表留言</h3>
              <p>留下您的联系方式，帮助失主找到失物</p>
            </div>
            <el-form :model="commentForm" @submit.prevent="submitComment">
              <el-form-item>
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="3"
                  :maxlength="500"
                  show-word-limit
                  placeholder="写下您的留言，如有线索请留下联系方式..."
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
            style="margin-top: 20px"
          >
            <template #default>
              <el-button
                type="primary"
                @click="goToLogin"
                plain
                size="small"
                style="margin-left: 10px"
              >
                去登录
              </el-button>
            </template>
          </el-alert>
        </div>

        <!-- 相似物品 -->
        <div class="related-items" v-if="relatedItems.length > 0">
          <h2 class="section-title">相似丢失物品</h2>
          <el-row :gutter="20">
            <el-col v-for="item in relatedItems" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card shadow="hover" class="related-item-card" @click="goToItem(item.id)">
                <div class="related-item-image">
                  <img
                    :src="
                      item.images && item.images.length > 0 ? item.images[0] : '/placeholder.jpg'
                    "
                  />
                </div>
                <h3 class="related-item-title">{{ item.title }}</h3>
                <div class="related-item-footer">
                  <span class="related-item-location">{{ item.lostLocation }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 联系对话框 -->
      <el-dialog v-model="contactDialogVisible" title="联系失主" width="500px">
        <div class="contact-dialog-content">
          <p>如果您认为自己拥有或发现了这个物品，请使用以下联系方式联系失主：</p>

          <el-alert type="success" :closable="false" show-icon>
            <template #title> 联系方式: {{ lostItem?.contactInfo }} </template>
          </el-alert>

          <p class="contact-note">您也可以在下方留言，我们会通知失主查看。</p>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="contactDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="goToCommentForm">留言给失主</el-button>
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
import { User, Calendar, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElImage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useLostItemsStore } from '@/stores/lostItems'
import { useUserStore } from '@/stores/user'
import type { LostItem, Comment } from '@/stores/lostItems'
import ReportDialog from '@/components/ReportDialog.vue'
import { formatDate } from '@/utils/dateHelpers'

const router = useRouter()
const route = useRoute()
const lostItemsStore = useLostItemsStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(false)
const error = ref(false)
const commentForm = ref({
  content: '',
})
const commentSubmitting = ref(false)
const contactDialogVisible = ref(false)
const actionLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const reportDialogVisible = ref(false)
const reportItemType = ref('')
const reportItemId = ref<number | null>(null)
const reportItemTitle = ref('')

// 计算属性
const itemId = computed(() => {
  const id = Number(route.params.id)
  return isNaN(id) ? null : id
})

const lostItem = computed(() => lostItemsStore.currentItem as LostItem | null)
const comments = computed(() => lostItemsStore.comments as Comment[])
const isLoggedIn = computed(() => userStore.isAuthenticated)
const user = computed(() => userStore.userProfile)
const totalComments = computed(() => lostItemsStore.totalComments)
const isItemOwner = computed(() => {
  return isLoggedIn.value && lostItem.value && userStore.user?.id === lostItem.value.userId
})

const isOwner = computed(() => {
  if (!isLoggedIn.value || !lostItem.value || !user.value) return false
  return lostItem.value.userId === user.value.id
})

const isAuthenticated = computed(() => userStore.isAuthenticated)

// 获取相关物品（同类别的其他丢失物品）
const relatedItems = computed(() => {
  if (!lostItem.value) return []
  return lostItemsStore.items
    .filter((item) => item.id !== lostItem.value!.id && item.category === lostItem.value!.category)
    .slice(0, 4) // 最多显示4个相关物品
})

// 方法
// 获取用户名首字母，用于头像
const getInitials = (username: string) => {
  return username ? username.substring(0, 2).toUpperCase() : 'U'
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
  router.push(`/lost-items/${id}`)
}

// 预览图片
const previewImage = (url: string) => {
  ElImage.PreviewService({
    urlList: lostItem.value?.images || [url],
  })
}

// 联系失主
const contactOwner = () => {
  contactDialogVisible.value = true
}

// 跳转到评论表单
const goToCommentForm = () => {
  contactDialogVisible.value = false

  // 滚动到评论表单
  nextTick(() => {
    const commentForm = document.querySelector('.comment-form-card')
    if (commentForm) {
      commentForm.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

// 提交评论
const submitComment = async () => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录后再发表评论')
    router.push('/login')
    return
  }

  if (!itemId.value) {
    ElMessage.error('物品ID无效，无法提交评论')
    return
  }

  if (!commentForm.value.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  commentSubmitting.value = true

  try {
    const result = await lostItemsStore.addComment(itemId.value, commentForm.value.content)

    if (result.success) {
      ElMessage.success('评论发表成功')
      commentForm.value.content = ''
      await loadComments()

      // Scroll to the new comment
      nextTick(() => {
        const commentsContainer = document.querySelector('.comments-section')
        if (commentsContainer) {
          commentsContainer.scrollTop = commentsContainer.scrollHeight
        }
      })
    } else {
      ElMessage.error(result.message || '评论发表失败')
    }
  } catch (error) {
    console.error('Failed to submit comment:', error)
    ElMessage.error('评论发表失败，请稍后再试')
  } finally {
    commentSubmitting.value = false
  }
}

const loadComments = async () => {
  if (!itemId.value) {
    console.error('无效的物品ID，无法加载评论')
    return
  }

  try {
    await lostItemsStore.fetchComments(itemId.value, currentPage.value, pageSize.value)
  } catch (error) {
    console.error('Failed to load comments:', error)
    ElMessage.error('加载评论失败，请稍后再试')
  }
}

// 将寻物启事标记为"已找到"
const markAsFound = async () => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录后再执行此操作')
    return
  }

  if (!itemId.value) {
    ElMessage.error('物品ID无效，无法执行操作')
    return
  }

  actionLoading.value = true

  try {
    const result = await lostItemsStore.markAsFound(itemId.value)

    if (result.success) {
      ElMessage.success('物品已标记为已找到')
      await loadItemDetail()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Failed to mark item as found:', error)
    ElMessage.error('操作失败，请稍后再试')
  } finally {
    actionLoading.value = false
    contactDialogVisible.value = false
  }
}

// 编辑物品信息
const redirectToEdit = () => {
  if (!itemId.value) return
  router.push(`/lost-items/edit/${itemId.value}`)
}

// 确认删除物品
const deleteItem = () => {
  if (!itemId.value) return

  ElMessageBox.confirm('确定要删除这个寻物启事吗？删除后将无法恢复。', '删除寻物启事', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
  })
    .then(async () => {
      try {
        const result = await lostItemsStore.deleteLostItem(itemId.value!)

        if (result.success) {
          ElMessage.success('寻物启事已删除')
          router.push('/lost-items')
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete item:', error)
        ElMessage.error('删除失败，请稍后再试')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 加载物品详情
const loadItemDetail = async () => {
  loading.value = true
  error.value = false

  try {
    if (!itemId.value) {
      error.value = true
      ElMessage.error('无效的物品ID')
      return
    }

    // Fetch the lost item
    await lostItemsStore.fetchLostItemById(itemId.value)

    // Check if we have a valid item
    if (lostItemsStore.currentItem) {
      // Set report info
      reportItemId.value = lostItemsStore.currentItem.id
      reportItemTitle.value = lostItemsStore.currentItem.title

      // Load comments for this item
      await loadComments()
    } else {
      error.value = true
      ElMessage.error('未找到该物品')
    }
  } catch (err) {
    console.error('Failed to load lost item details:', err)
    error.value = true
    ElMessage.error('加载物品详情失败')
  } finally {
    loading.value = false
  }
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

// 处理分页
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  loadItemDetail()
}

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage
  loadItemDetail()
}

// 举报物品
function reportItem() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!lostItem.value) {
    ElMessage.warning('找不到物品信息')
    return
  }

  reportItemType.value = 'LOST_ITEM'
  reportItemId.value = lostItem.value.id
  reportItemTitle.value = lostItem.value.title
  reportDialogVisible.value = true
}

// 举报评论
const reportComment = (comment: Comment) => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录后再进行举报')
    return
  }

  reportItemType.value = 'COMMENT'
  reportItemId.value = comment.id
  reportItemTitle.value = `评论: ${comment.content.substring(0, 20)}...`
  reportDialogVisible.value = true
}

// 处理举报提交后的操作
function handleReportSubmitted(report: any) {
  console.log('举报已提交:', report)
}

// 删除评论
const deleteComment = async (comment: Comment) => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录后再进行操作')
    return
  }

  if (!userStore.user || userStore.user.id !== comment.userId) {
    ElMessage.error('您没有权限删除此评论')
    return
  }

  try {
    const result = await lostItemsStore.deleteComment(comment.id)
    if (result.success) {
      ElMessage.success('评论删除成功')
      loadComments()
    } else {
      ElMessage.error(result.message || '评论删除失败')
    }
  } catch (error) {
    console.error('Failed to delete comment:', error)
    ElMessage.error('评论删除失败，请稍后再试')
  }
}
</script>

<style scoped>
.lost-item-detail-container {
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
  gap: 8px;
  font-size: 14px;
  align-items: center;
  flex-wrap: wrap;
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
  margin-bottom: 30px;
}

.item-description h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
}

.item-description p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.reward {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
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

.contact-dialog-content p {
  margin-bottom: 15px;
}

.contact-note {
  margin-top: 20px;
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
