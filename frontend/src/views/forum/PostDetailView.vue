<template>
  <main-layout>
    <div class="post-detail-container">
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
              <el-skeleton-item variant="text" style="width: 100%; height: 200px" />
              <div style="margin-top: 20px;">
                <el-skeleton-item variant="text" style="width: 50%;" />
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
        title="获取帖子失败"
        sub-title="请稍后再试或返回论坛"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/forum')">
            返回论坛
          </el-button>
        </template>
      </el-result>

      <!-- 无数据状态 -->
      <el-empty
        v-else-if="!post"
        description="未找到该帖子"
      >
        <el-button type="primary" @click="$router.push('/forum')">
          返回论坛
        </el-button>
      </el-empty>

      <!-- 帖子详情 -->
      <template v-else>
        <div class="post-navigation">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/forum' }">邻里论坛</el-breadcrumb-item>
            <el-breadcrumb-item>帖子详情</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 帖子内容 -->
        <el-card class="post-card">
          <div class="post-header">
            <h1 class="post-title">
              <span v-if="post.isTop" class="top-label">置顶</span>
              <span v-if="post.status === 'locked'" class="locked-label">锁定</span>
              {{ post.title }}
            </h1>
          </div>

          <div class="post-meta">
            <div class="post-author">
              <el-avatar :size="40" :src="post.user?.avatar">
                {{ post.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ post.user?.username }}</span>
                <span class="post-time">发布于 {{ formatDate(post.createdAt) }}</span>
              </div>
            </div>

            <div class="post-actions" v-if="canEditPost">
              <el-dropdown trigger="click" @command="handleCommand">
                <el-button type="info" plain>
                  操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑帖子</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除帖子</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="post-body">
            <div v-if="post.images && post.images.length > 0" class="post-images">
              <el-image
                v-for="(image, index) in post.images"
                :key="index"
                :src="image"
                fit="contain"
                class="post-image"
                :preview-src-list="post.images"
                preview-teleported
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon> 图片加载失败
                  </div>
                </template>
              </el-image>
            </div>

            <div class="post-content" v-html="formattedContent"></div>

            <div class="post-footer">
              <div class="post-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon> {{ post.viewCount || 0 }} 次浏览
                </span>
                <span class="stat-item">
                  <el-icon><ChatLineRound /></el-icon> {{ post.commentCount || 0 }} 条评论
                </span>
              </div>

              <div class="post-actions">
                <el-button
                  :type="post.isLiked ? 'primary' : 'default'"
                  :icon="Star"
                  @click="handleLike"
                  :loading="likeLoading"
                >
                  {{ post.likeCount || 0 }} 赞
                </el-button>
                <el-button
                  type="primary"
                  @click="scrollToComment"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 评论区 -->
        <div class="comments-section">
          <div class="comments-header">
            <h2>
              <el-icon><ChatDotRound /></el-icon>
              评论 ({{ comments.length }})
            </h2>
            <el-button
              type="primary"
              plain
              :icon="Plus"
              @click="focusCommentInput"
              v-if="isLoggedIn && post.status !== 'locked'"
            >
              写评论
            </el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list" v-if="comments.length > 0">
            <el-card
              v-for="comment in comments"
              :key="comment.id"
              class="comment-card"
              shadow="never"
            >
              <div class="comment-author">
                <el-avatar :size="32" :src="comment.user?.avatar">
                  {{ comment.user?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="author-info">
                  <span class="author-name">{{ comment.user?.username }}</span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                </div>
              </div>
              <div class="comment-content">
                {{ comment.content }}
              </div>
              <div class="comment-actions">
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="Star"
                  @click="handleLikeComment(comment.id)"
                >
                  {{ comment.likeCount }} 赞
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  :icon="ChatLineRound"
                  @click="replyToComment(comment.id, comment.user?.username)"
                  v-if="isLoggedIn && post.status !== 'locked'"
                >
                  回复
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- 无评论提示 -->
          <el-empty
            v-else
            description="暂无评论"
            :image-size="100"
          >
            <el-button
              type="primary"
              @click="focusCommentInput"
              v-if="isLoggedIn && post.status !== 'locked'"
            >
              发表第一条评论
            </el-button>
          </el-empty>

          <!-- 评论输入框 -->
          <el-card class="comment-input-card" v-if="isLoggedIn && post.status !== 'locked'">
            <div class="comment-input-header">
              <el-avatar :size="40" :src="''">{{ userInitials }}</el-avatar>
              <span>{{ user?.username }}</span>
            </div>
            <el-form :model="commentForm" @submit.prevent="submitComment">
              <el-form-item>
                <el-input
                  ref="commentInputRef"
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="3"
                  :maxlength="500"
                  show-word-limit
                  placeholder="写下你的评论..."
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitComment"
                  :loading="commentSubmitting"
                  :disabled="!commentForm.content.trim()"
                >
                  发表评论
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 帖子已锁定提示 -->
          <el-alert
            v-else-if="post.status === 'locked'"
            title="该帖子已被锁定，无法发表新评论"
            type="info"
            :closable="false"
            center
            show-icon
            style="margin-top: 20px;"
          />

          <!-- 登录提示 -->
          <el-alert
            v-else
            title="登录后才能发表评论"
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

        <!-- 推荐帖子 -->
        <div class="related-posts" v-if="relatedPosts.length > 0">
          <h2 class="section-title">推荐帖子</h2>
          <el-row :gutter="20">
            <el-col
              v-for="relatedPost in relatedPosts"
              :key="relatedPost.id"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <el-card
                shadow="hover"
                class="related-post-card"
                @click="goToRelatedPost(relatedPost.id)"
              >
                <h3>{{ relatedPost.title }}</h3>
                <p>{{ truncateContent(relatedPost.content, 50) }}</p>
                <div class="card-footer">
                  <span class="post-author">{{ relatedPost.username }}</span>
                  <span class="post-stats">
                    <el-icon><ChatDotRound /></el-icon> {{ relatedPost.commentCount }}
                  </span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 举报对话框 -->
      <el-dialog
        v-model="reportDialogVisible"
        title="举报帖子"
        width="450px"
      >
        <el-form :model="reportForm" label-position="top">
          <el-form-item label="举报原因" required>
            <el-select
              v-model="reportForm.reason"
              placeholder="请选择举报原因"
              style="width: 100%"
            >
              <el-option label="垃圾信息" value="spam" />
              <el-option label="违法违规" value="illegal" />
              <el-option label="色情低俗" value="pornography" />
              <el-option label="人身攻击" value="bullying" />
              <el-option label="虚假信息" value="fake" />
              <el-option label="其他原因" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明" v-if="reportForm.reason === 'other'">
            <el-input
              v-model="reportForm.details"
              type="textarea"
              :rows="3"
              placeholder="请说明详细原因..."
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="reportDialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              @click="submitReport"
              :loading="reportSubmitting"
              :disabled="!reportForm.reason"
            >
              提交举报
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Star,
  Plus,
  ChatDotRound,
  ChatLineRound,
  Edit,
  Delete,
  WarningFilled,
  MoreFilled,
  ArrowDown,
  Picture,
  View
} from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'
import type { ForumPost, ForumComment } from '@/stores/forum'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

const router = useRouter()
const route = useRoute()
const forumStore = useForumStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(false)
const error = ref(false)
const commentInputRef = ref(null)
const commentForm = ref({
  content: ''
})
const commentSubmitting = ref(false)
const isLikeLoading = ref(false)
const reportDialogVisible = ref(false)
const reportForm = ref({
  reason: '',
  details: ''
})
const reportSubmitting = ref(false)

// 计算属性
const postId = computed(() => {
  const id = Number(route.params.id)
  return isNaN(id) ? null : id
})

const post = computed(() => forumStore.currentPost as ForumPost | null)
const comments = computed(() => forumStore.comments as ForumComment[])
const isLoggedIn = computed(() => userStore.isAuthenticated)
const user = computed(() => userStore.userProfile)
const userInitials = computed(() => {
  return user.value?.username ? user.value.username.substring(0, 2).toUpperCase() : 'U'
})

const isAuthor = computed(() => {
  if (!isLoggedIn.value || !post.value || !user.value) return false
  return post.value.userId === user.value.id
})

const showMoreActions = computed(() => {
  return isLoggedIn.value && (isAuthor.value || !isAuthor.value)
})

const contentParagraphs = computed(() => {
  if (!post.value?.content) return []
  return post.value.content.split('\n').filter(p => p.trim() !== '')
})

// 获取推荐帖子（除当前帖子外的最新帖子）
const relatedPosts = computed(() => {
  if (!post.value) return []
  return forumStore.posts
    .filter(p => p.id !== post.value!.id)
    .slice(0, 3)
})

const formattedContent = computed(() => {
  if (!post.value.content) return ''

  // 使用marked将markdown转换为HTML，并使用DOMPurify进行安全处理
  const rawHtml = marked(post.value.content)
  return DOMPurify.sanitize(rawHtml)
})

// 方法
// 获取用户名首字母，用于头像
const getInitials = (username: string) => {
  return username ? username.substring(0, 2).toUpperCase() : 'U'
}

// 格式化日期
const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString)
    return format(date, 'yyyy年MM月dd日 HH:mm', { locale: zhCN })
  } catch (error) {
    return dateString
  }
}

// 截断内容
const truncateContent = (content: string, maxLength = 100) => {
  if (!content || content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

// 跳转到登录页
const goToLogin = () => {
  router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

// 跳转到相关帖子
const goToRelatedPost = (id: number) => {
  if (id === postId.value) return
  router.push(`/forum/${id}`)
}

// 聚焦评论输入框
const focusCommentInput = () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  nextTick(() => {
    if (commentInputRef.value) {
      // @ts-ignore
      commentInputRef.value.focus()
    }
  })
}

// 回复评论
const replyToComment = (commentId: number, username: string) => {
  commentForm.value.content = `@${username} `
  focusCommentInput()
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  if (!postId.value) return

  commentSubmitting.value = true
  try {
    const result = await forumStore.addComment({
      content: commentForm.value.content,
      postId: postId.value
    })

    if (result.success) {
      ElMessage.success('评论发表成功')
      commentForm.value.content = ''
      // 重新获取评论
      await forumStore.fetchComments(postId.value)
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

// 点赞帖子
const handleLike = async () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  if (!postId.value) return

  isLikeLoading.value = true
  try {
    const result = await forumStore.likePost(postId.value)

    if (result.success) {
      ElMessage.success(result.message || '点赞成功')
      // 更新帖子数据
      await forumStore.fetchPostById(postId.value)
    } else {
      ElMessage.error(result.message || '点赞失败')
    }
  } catch (error) {
    console.error('Failed to like post:', error)
    ElMessage.error('点赞失败，请稍后再试')
  } finally {
    isLikeLoading.value = false
  }
}

// 点赞评论
const handleLikeComment = (commentId: number) => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  ElMessage.success('评论点赞成功')
  // 实际应用中，这里需要调用API点赞评论
  // 同时更新评论数据
}

// 编辑帖子
const handleEditPost = () => {
  if (!postId.value) return
  router.push(`/forum/edit/${postId.value}`)
}

// 确认删除帖子
const confirmDeletePost = () => {
  if (!postId.value) return

  ElMessageBox.confirm(
    '确定要删除这篇帖子吗？删除后将无法恢复。',
    '删除帖子',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const result = await forumStore.deletePost(postId.value!)

      if (result.success) {
        ElMessage.success('帖子已删除')
        router.push('/forum')
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('Failed to delete post:', error)
      ElMessage.error('删除失败，请稍后再试')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 举报帖子
const handleReport = () => {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }

  reportForm.value = {
    reason: '',
    details: ''
  }
  reportDialogVisible.value = true
}

// 提交举报
const submitReport = async () => {
  if (!reportForm.value.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }

  if (reportForm.value.reason === 'other' && !reportForm.value.details.trim()) {
    ElMessage.warning('请填写详细原因')
    return
  }

  if (!postId.value) return

  reportSubmitting.value = true
  try {
    const reason = reportForm.value.reason === 'other'
      ? reportForm.value.details
      : reportForm.value.reason

    const result = await forumStore.reportPost(postId.value, reason)

    if (result.success) {
      ElMessage.success('举报已提交，感谢您的反馈')
      reportDialogVisible.value = false
    } else {
      ElMessage.error(result.message || '举报提交失败')
    }
  } catch (error) {
    console.error('Failed to submit report:', error)
    ElMessage.error('举报提交失败，请稍后再试')
  } finally {
    reportSubmitting.value = false
  }
}

// 加载帖子详情和评论
const loadPostDetail = async () => {
  if (!postId.value) return

  loading.value = true
  error.value = false

  try {
    await forumStore.fetchPostById(postId.value)
    await forumStore.fetchComments(postId.value)
  } catch (err) {
    console.error('Failed to load post detail:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// 路由参数变化时重新加载数据
watch(
  () => route.params.id,
  () => {
    if (postId.value) {
      loadPostDetail()
    }
  }
)

onMounted(() => {
  loadPostDetail()
})

const canEditPost = computed(() => {
  if (!post.value || !userStore.currentUser) return false

  // 作者可以编辑自己的帖子，管理员可以编辑任何帖子
  return post.value.userId === userStore.currentUser.id || userStore.currentUser.role === 'admin'
})

const handleCommand = (command) => {
  if (command === 'edit') {
    router.push(`/forum/edit/${postId.value}`)
  } else if (command === 'delete') {
    ElMessageBox.confirm(
      '确定要删除这篇帖子吗？删除后无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(async () => {
        try {
          const result = await forumStore.deletePost(postId.value)
          if (result.success) {
            ElMessage.success('帖子已成功删除')
            router.push('/forum')
          } else {
            ElMessage.error(result.message || '删除失败')
          }
        } catch (error) {
          console.error('Failed to delete post:', error)
          ElMessage.error('删除失败，请稍后重试')
        }
      })
      .catch(() => {
        // 用户取消操作
      })
  }
}

const scrollToComment = () => {
  const commentEditor = document.getElementById('comment-editor')
  if (commentEditor) {
    commentEditor.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.post-navigation {
  margin-bottom: 20px;
}

.post-card {
  margin-bottom: 30px;
}

.post-header {
  margin-bottom: 20px;
}

.post-title {
  font-size: 24px;
  margin: 0 0 10px;
  line-height: 1.4;
  display: flex;
  align-items: center;
}

.top-label, .locked-label {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
  height: fit-content;
}

.top-label {
  background-color: #e6a23c;
  color: white;
}

.locked-label {
  background-color: #909399;
  color: white;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.post-author {
  display: flex;
  align-items: center;
}

.author-info {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  font-size: 14px;
}

.post-time, .comment-time {
  font-size: 12px;
  color: #909399;
}

.post-body {
  margin-bottom: 30px;
}

.post-images {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.post-image {
  max-width: 100%;
  border-radius: 4px;
}

.image-error {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.comments-section {
  margin-top: 30px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comments-header h2 {
  display: flex;
  align-items: center;
  margin: 0;
  font-size: 18px;
}

.comments-header h2 .el-icon {
  margin-right: 8px;
}

.comments-list {
  margin-bottom: 30px;
}

.comment-card {
  margin-bottom: 15px;
}

.comment-card:not(:last-child) {
  border-bottom: 1px solid #ebeef5;
}

.comment-author {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-content {
  padding: 10px 0;
  line-height: 1.6;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 5px 0;
}

.comment-input-card {
  margin-top: 20px;
}

.comment-input-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.comment-input-header span {
  margin-left: 10px;
  font-weight: 500;
}

.related-posts {
  margin-top: 50px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.related-post-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.related-post-card:hover {
  transform: translateY(-5px);
}

.related-post-card h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-post-card p {
  color: #606266;
  margin-bottom: 15px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 12px;
}

.card-footer .post-stats {
  display: flex;
  align-items: center;
}

.card-footer .post-stats .el-icon {
  margin-right: 5px;
}

@media (max-width: 768px) {
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-actions {
    margin-top: 10px;
    align-self: flex-end;
  }

  .comments-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .comments-header h2 {
    margin-bottom: 10px;
  }
}
</style>
