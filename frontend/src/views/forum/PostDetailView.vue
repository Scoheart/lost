<template>
  <div class="post-detail-view">
    <MainLayout>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="15" animated />
      </div>

      <div v-else-if="error" class="error-state">
        <el-result
          icon="error"
          title="加载失败"
          :sub-title="error"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/forum')">返回论坛</el-button>
          </template>
        </el-result>
      </div>

      <template v-else-if="post">
        <div class="post-header">
          <div class="post-title-section">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-meta">
              <div class="author-info">
                <el-avatar :size="32" :src="post.userAvatar">{{ post.username.substring(0, 2) }}</el-avatar>
                <span class="author-name">{{ post.username }}</span>
              </div>
              <div class="post-date">
                <el-tooltip :content="formatDate(post.createdAt, true)" placement="top">
                  <span>发布于: {{ getRelativeTime(post.createdAt) }}</span>
                </el-tooltip>
                <template v-if="post.updatedAt !== post.createdAt">
                  <el-divider direction="vertical" />
                  <el-tooltip :content="formatDate(post.updatedAt, true)" placement="top">
                    <span>更新于: {{ getRelativeTime(post.updatedAt) }}</span>
                  </el-tooltip>
                </template>
              </div>
            </div>
          </div>

          <div class="post-actions">
            <el-button v-if="isPostOwner" type="primary" plain @click="editPost">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button v-if="isPostOwner" type="danger" plain @click="confirmDelete">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button
              v-if="isLoggedIn && !isPostOwner"
              type="warning"
              plain
              @click="reportPost"
            >
              <el-icon><Warning /></el-icon> 举报
            </el-button>
          </div>
        </div>

        <el-card class="post-content-card">
          <div class="post-content">{{ post.content }}</div>
        </el-card>

        <!-- 评论区 -->
        <div class="comments-section">
          <h2 class="comments-title">评论 ({{ comments.length }})</h2>

          <!-- 评论表单 -->
          <el-card v-if="isLoggedIn" class="comment-form-card">
            <el-form @submit.prevent="submitComment">
              <el-form-item>
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="3"
                  placeholder="写下您的评论..."
                  :maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item class="comment-form-actions">
                <el-button type="primary" @click="submitComment" :loading="commentSubmitting">
                  发表评论
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <div v-else class="login-prompt">
            <el-alert
              title="请登录后发表评论"
              type="info"
              center
              :closable="false"
              show-icon
            >
              <template #default>
                <el-button
                  type="primary"
                  size="small"
                  @click="$router.push('/login?redirect=' + $route.fullPath)"
                >
                  去登录
                </el-button>
              </template>
            </el-alert>
          </div>

          <!-- 评论列表 -->
          <div v-if="comments.length === 0" class="no-comments">
            <el-empty description="暂无评论" />
          </div>

          <div v-else class="comments-list">
            <el-card
              v-for="comment in comments"
              :key="comment.id"
              class="comment-card"
              shadow="hover"
            >
              <div class="comment-header">
                <div class="comment-user-info">
                  <el-avatar :size="32" :src="comment.userAvatar">
                    {{ comment.username.substring(0, 2) }}
                  </el-avatar>
                  <div class="comment-user-meta">
                    <span class="comment-username">{{ comment.username }}</span>
                    <el-tooltip :content="formatDate(comment.createdAt, true)" placement="top">
                      <span class="comment-date">{{ getRelativeTime(comment.createdAt) }}</span>
                    </el-tooltip>
                  </div>
                </div>
                <div class="comment-actions">
                  <el-button
                    v-if="isCommentOwner(comment)"
                    type="danger"
                    size="small"
                    text
                    @click="deleteComment(comment)"
                  >
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                  <el-button
                    v-if="isLoggedIn && !isCommentOwner(comment)"
                    type="warning"
                    size="small"
                    text
                    @click="reportComment(comment)"
                  >
                    <el-icon><Warning /></el-icon> 举报
                  </el-button>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </el-card>
          </div>

          <!-- 评论分页 -->
          <div v-if="totalCommentPages > 1" class="comments-pagination">
            <el-pagination
              layout="prev, pager, next"
              :total="totalComments"
              :page-size="commentsPageSize"
              :current-page="commentsCurrentPage"
              @current-change="handleCommentPageChange"
              background
            />
          </div>
        </div>
      </template>
    </MainLayout>

    <!-- 举报对话框 -->
    <ReportDialog
      v-model:visible="reportDialogVisible"
      :item-id="reportItemId"
      :item-type="reportItemType"
      :item-title="reportItemTitle"
      @submitted="handleReportSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Warning } from '@element-plus/icons-vue'
import { usePostsStore } from '@/stores/posts'
import { useUserStore } from '@/stores/user'
import { useCommentsStore } from '@/stores/comments'
import MainLayout from '@/components/layout/MainLayout.vue'
import ReportDialog from '@/components/ReportDialog.vue'
import { formatDate, getRelativeTime } from '@/utils/dateHelpers'

const router = useRouter()
const route = useRoute()
const postsStore = usePostsStore()
const userStore = useUserStore()
const commentsStore = useCommentsStore()

// 状态
const loading = computed(() => postsStore.loading)
const error = computed(() => postsStore.error)
const post = computed(() => postsStore.currentPost)
const comments = computed(() => commentsStore.comments)
const commentsCurrentPage = ref(1)
const commentsPageSize = ref(10)
const totalComments = ref(0)
const totalCommentPages = ref(0)
const commentSubmitting = ref(false)

// 评论表单
const commentForm = ref({
  content: ''
})

// 举报相关
const reportDialogVisible = ref(false)
const reportItemId = ref<number | null>(null)
const reportItemType = ref<string>('')
const reportItemTitle = ref<string>('')

// 计算属性
const isLoggedIn = computed(() => userStore.isAuthenticated)
const isPostOwner = computed(() => postsStore.isPostOwner)
const currentUserID = computed(() => userStore.user?.id)

// 获取帖子ID（如果存在）
const postId = computed(() => {
  const id = route.params.id
  return id ? parseInt(id as string) : null
})

// 检查用户是否是评论的作者
const isCommentOwner = (comment: any) => {
  return currentUserID.value === comment.userId
}

// 生命周期钩子
onMounted(async () => {
  if (!postId.value) {
    router.push('/forum')
    return
  }

  try {
    await postsStore.fetchPostById(postId.value)
    await loadComments()
  } catch (error) {
    console.error('加载帖子详情失败:', error)
  }
})

// 加载评论
async function loadComments() {
  if (!postId.value) return

  try {
    const result = await commentsStore.fetchComments(postId.value, 'post', commentsCurrentPage.value, commentsPageSize.value)
    totalComments.value = result.totalItems
    totalCommentPages.value = result.totalPages
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败，请稍后再试')
  }
}

// 处理评论分页变化
async function handleCommentPageChange(page: number) {
  commentsCurrentPage.value = page
  await loadComments()
}

// 提交评论
async function submitComment() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!commentForm.value.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  if (!postId.value) {
    ElMessage.error('帖子ID无效')
    return
  }

  commentSubmitting.value = true

  try {
    await commentsStore.addComment({
      content: commentForm.value.content,
      itemId: postId.value,
      itemType: 'post'
    })

    commentForm.value.content = ''
    ElMessage.success('评论发布成功')

    // 重新加载第一页评论
    commentsCurrentPage.value = 1
    await loadComments()
  } catch (error) {
    console.error('发布评论失败:', error)
    ElMessage.error('发布评论失败，请稍后再试')
  } finally {
    commentSubmitting.value = false
  }
}

// 删除评论
function deleteComment(comment: any) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  ElMessageBox.confirm(
    '确定要删除这条评论吗？删除后将无法恢复。',
    '删除评论',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await commentsStore.deleteComment(comment.id, 'post')
      ElMessage.success('评论已删除')
      await loadComments()
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败，请稍后再试')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 编辑帖子
function editPost() {
  if (!postId.value) return
  router.push(`/forum/edit/${postId.value}`)
}

// 确认删除帖子
function confirmDelete() {
  ElMessageBox.confirm(
    '确定要删除这篇帖子吗？删除后将无法恢复。',
    '删除帖子',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    if (!postId.value) return

    try {
      await postsStore.deletePost(postId.value)
      ElMessage.success('帖子已删除')
      router.push('/forum')
    } catch (error) {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除帖子失败，请稍后再试')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 举报帖子
function reportPost() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (!post.value || !post.value.id) {
    ElMessage.error('帖子信息不完整')
    return
  }

  reportItemType.value = 'POST'
  reportItemId.value = post.value.id
  reportItemTitle.value = post.value.title
  reportDialogVisible.value = true
}

// 举报评论
function reportComment(comment: any) {
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
function handleReportSubmitted(report: any) {
  console.log('举报已提交:', report)
}
</script>

<style scoped>
.post-detail-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.loading-container, .error-state {
  padding: 20px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.post-title-section {
  flex: 1;
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 24px;
  color: #303133;
  word-break: break-word;
}

.post-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 8px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-weight: 500;
  color: #303133;
}

.post-date {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.post-content-card {
  margin-bottom: 24px;
}

.post-content {
  white-space: pre-line;
  line-height: 1.7;
  color: #303133;
  word-break: break-word;
}

.comments-section {
  margin-top: 40px;
}

.comments-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #303133;
  font-weight: 600;
}

.comment-form-card {
  margin-bottom: 24px;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 0;
}

.login-prompt {
  margin-bottom: 24px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  transition: all 0.3s;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.comment-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-user-meta {
  display: flex;
  flex-direction: column;
}

.comment-username {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.comment-date {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
  word-break: break-word;
  margin-left: 40px;
}

.comments-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.no-comments {
  padding: 30px 0;
}

@media (max-width: 768px) {
  .post-header {
    flex-direction: column;
  }

  .post-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
