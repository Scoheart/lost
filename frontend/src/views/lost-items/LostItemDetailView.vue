<template>
  <main-layout>
    <div class="lost-item-detail-container">
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
      <el-empty
        v-else-if="!lostItem"
        description="未找到该寻物启事"
      >
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
              <el-tag type="danger" v-if="lostItem.status === 'pending'">寻找中</el-tag>
              <el-tag type="success" v-else-if="lostItem.status === 'found'">已找到</el-tag>
              <el-tag type="info" v-else-if="lostItem.status === 'closed'">已关闭</el-tag>
            </h1>

            <div class="item-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ lostItem.username }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                发布于{{ formatDate(lostItem.createdAt) }}
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
                  {{ formatDate(lostItem.lostDate, true) }}
                </el-descriptions-item>
                <el-descriptions-item label="丢失地点">
                  {{ lostItem.lostLocation }}
                </el-descriptions-item>
                <el-descriptions-item
                  label="悬赏金额"
                  v-if="lostItem.reward"
                >
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

          <div class="item-actions" v-if="lostItem.status === 'pending'">
            <el-button
              type="primary"
              @click="contactOwner"
              v-if="!isOwner"
            >
              我有这个物品
            </el-button>

            <div class="owner-actions" v-if="isOwner">
              <el-button
                type="primary"
                @click="markAsFound"
                :loading="actionLoading"
              >
                已找到物品
              </el-button>
              <el-button
                type="danger"
                @click="closeItem"
                :loading="actionLoading"
              >
                关闭寻物
              </el-button>
              <el-button
                type="primary"
                plain
                @click="editItem"
              >
                编辑信息
              </el-button>
              <el-button
                type="danger"
                plain
                @click="confirmDeleteItem"
              >
                删除
              </el-button>
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
                    <el-avatar :size="32" :src="comment.userAvatar || ''">{{ getInitials(comment.username) }}</el-avatar>
                    <span class="author-name">{{ comment.username }}</span>
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
          <h2 class="section-title">相似丢失物品</h2>
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
                  <span class="related-item-location">{{ item.lostLocation }}</span>
                  <el-tag size="small" type="danger" v-if="item.status === 'pending'">寻找中</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 联系对话框 -->
      <el-dialog
        v-model="contactDialogVisible"
        title="联系失主"
        width="500px"
      >
        <div class="contact-dialog-content">
          <p>如果您认为自己拥有或发现了这个物品，请使用以下联系方式联系失主：</p>

          <el-alert
            type="success"
            :closable="false"
            show-icon
          >
            <template #title>
              联系方式: {{ lostItem?.contactInfo }}
            </template>
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
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Calendar, Picture } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { ElMessage, ElMessageBox, ElImage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useLostItemsStore } from '@/stores/lostItems'
import { useUserStore } from '@/stores/user'
import type { LostItem, Comment } from '@/stores/lostItems'

const router = useRouter()
const route = useRoute()
const lostItemsStore = useLostItemsStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(false)
const error = ref(false)
const commentForm = ref({
  content: ''
})
const commentSubmitting = ref(false)
const contactDialogVisible = ref(false)
const actionLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

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

const isOwner = computed(() => {
  if (!isLoggedIn.value || !lostItem.value || !user.value) return false
  return lostItem.value.userId === user.value.id
})

// 获取相关物品（同类别的其他丢失物品）
const relatedItems = computed(() => {
  if (!lostItem.value) return []
  return lostItemsStore.items
    .filter(item =>
      item.id !== lostItem.value!.id &&
      item.category === lostItem.value!.category &&
      item.status === 'pending'
    )
    .slice(0, 4) // 最多显示4个相关物品
})

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
  router.push(`/lost-items/${id}`)
}

// 预览图片
const previewImage = (url: string) => {
  ElImage.PreviewService({
    urlList: lostItem.value?.images || [url]
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
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('评论不能为空');
    return;
  }

  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    return;
  }

  commentSubmitting.value = true;
  try {
    const result = await lostItemsStore.addComment(itemId.value, commentForm.value.content);

    if (result.success) {
      commentForm.value.content = ''; // 清空评论内容
      currentPage.value = 1; // 重置到第一页以便查看新评论

      // 重新加载评论
      await lostItemsStore.fetchComments(itemId.value, currentPage.value, pageSize.value);

      ElMessage.success('评论成功');
    } else {
      ElMessage.error(result.message || '评论失败');
    }
  } catch (error) {
    console.error('Error submitting comment:', error);
    ElMessage.error('评论提交时发生错误');
  } finally {
    commentSubmitting.value = false;
  }
}

// 将寻物启事标记为"已找到"
const markAsFound = async () => {
  if (!lostItem.value || !itemId.value) return

  ElMessageBox.confirm(
    '确认已找到此物品吗？更新后状态将变为"已找到"。',
    '更新状态',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    actionLoading.value = true
    try {
      // 使用专用的状态更新函数
      const result = await lostItemsStore.markAsFound(itemId.value)

      if (result.success) {
        ElMessage.success('状态已更新为"已找到"')
        // 不需要重新获取物品详情，因为markAsFound已经更新了本地状态
      } else {
        ElMessage.error(result.message || '更新失败')
      }
    } catch (error) {
      console.error('Failed to update item status:', error)
      ElMessage.error('更新失败，请稍后再试')
    } finally {
      actionLoading.value = false
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 关闭寻物启事
const closeItem = async () => {
  if (!lostItem.value || !itemId.value) return

  ElMessageBox.confirm(
    '确认关闭此寻物启事吗？关闭后将不再公开展示。',
    '关闭寻物启事',
    {
      confirmButtonText: '确认关闭',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    actionLoading.value = true
    try {
      // 使用专用的关闭函数
      const result = await lostItemsStore.closeItem(itemId.value)

      if (result.success) {
        ElMessage.success('寻物启事已关闭')
        router.push('/lost-items')
      } else {
        ElMessage.error(result.message || '关闭失败')
      }
    } catch (error) {
      console.error('Failed to close lost item:', error)
      ElMessage.error('关闭失败，请稍后再试')
    } finally {
      actionLoading.value = false
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 编辑物品信息
const editItem = () => {
  if (!itemId.value) return
  router.push(`/lost-items/edit/${itemId.value}`)
}

// 确认删除物品
const confirmDeleteItem = () => {
  if (!itemId.value) return

  ElMessageBox.confirm(
    '确定要删除这个寻物启事吗？删除后将无法恢复。',
    '删除寻物启事',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(async () => {
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
  }).catch(() => {
    // 用户取消删除
  })
}

// 加载物品详情
const loadItemDetail = async () => {
  try {
    loading.value = true
    error.value = null

    // 获取物品ID
    const itemId = Number(route.params.id)
    if (isNaN(itemId)) {
      error.value = '无效的物品ID'
      return
    }

    // 分别获取物品详情和评论
    const itemResult = await lostItemsStore.fetchLostItemById(itemId)

    if (!itemResult.success) {
      error.value = itemResult.message || '获取寻物启事失败'
      return
    }

    // 物品详情获取成功后，获取评论
    const commentsResult = await lostItemsStore.fetchComments(
      itemId,
      currentPage.value,
      pageSize.value
    )

    // 如果评论获取失败，只记录警告但继续显示物品详情
    if (!commentsResult.success) {
      console.warn('Failed to fetch comments:', commentsResult.message)
      // 不阻止页面显示，只显示物品信息
    }

    // 获取相关物品
    if (lostItem.value && lostItem.value.category) {
      await lostItemsStore.fetchLostItems({
        category: lostItem.value.category,
        page: 1,
        pageSize: 4
      })
    }

    // 滚动到顶部
    window.scrollTo(0, 0)
  } catch (error) {
    console.error('Error loading lost item details:', error)
    error.value = error.message || '获取寻物启事详情时发生错误'
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
  }
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
  margin-bottom: 10px;
}

.author-name {
  margin-left: 10px;
  font-weight: 500;
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
