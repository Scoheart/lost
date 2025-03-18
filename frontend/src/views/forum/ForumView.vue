<template>
  <div class="forum-container">
    <MainLayout>
      <div class="page-header">
        <div class="title-section">
          <h1>邻居论坛</h1>
          <p>在这里您可以与小区内的居民分享日常生活、交流经验、讨论话题。</p>
        </div>
        <div class="actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子标题或内容"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
          <el-button type="primary" @click="$router.push('/forum/create')" v-if="isLoggedIn">
            <el-icon><Plus /></el-icon> 发布新帖子
          </el-button>
        </div>
      </div>

      <div class="content">
        <el-card v-if="isSearching" shadow="never" class="search-info">
          <div class="search-result-header">
            <span>搜索结果: "{{ searchKeyword }}"</span>
            <el-button type="text" @click="clearSearch">清除搜索</el-button>
          </div>
        </el-card>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="posts.length === 0" class="empty-state">
          <el-empty description="暂无帖子" v-if="!isSearching">
            <el-button type="primary" @click="$router.push('/forum/create')" v-if="isLoggedIn">
              发布新帖子
            </el-button>
          </el-empty>
          <el-empty description="没有找到匹配的帖子" v-else>
            <el-button type="primary" @click="clearSearch">返回全部帖子</el-button>
          </el-empty>
        </div>

        <div v-else class="posts-list">
          <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover">
            <div class="post-header">
              <div class="user-info">
                <el-avatar :size="40" :src="post.userAvatar">{{ post.username.substring(0, 2) }}</el-avatar>
                <div class="user-details">
                  <span class="username">{{ post.username }}</span>
                  <span class="date">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </div>
            <div class="post-content" @click="goToPostDetail(post.id)">
              <h2 class="post-title">{{ post.title }}</h2>
              <p class="post-text">{{ truncateText(post.content, 200) }}</p>
            </div>
            <div class="post-footer">
              <div class="post-stats">
                <span class="comments">
                  <el-icon><ChatDotRound /></el-icon> {{ post.commentCount }} 条评论
                </span>
              </div>
              <el-button text @click="goToPostDetail(post.id)">阅读全文</el-button>
            </div>
          </el-card>
        </div>

        <div class="pagination-container" v-if="totalPages > 1">
          <el-pagination
            layout="prev, pager, next"
            :total="totalElements"
            :page-size="pageSize"
            :current-page="currentPage + 1"
            @current-change="handlePageChange"
            :pager-count="5"
            background
          />
        </div>
      </div>
    </MainLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, ChatDotRound } from '@element-plus/icons-vue'
import { usePostsStore } from '@/stores/posts'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/components/layout/MainLayout.vue'
import { formatDate } from '@/utils/dateHelpers'

const router = useRouter()
const postsStore = usePostsStore()
const userStore = useUserStore()

// 状态
const searchKeyword = ref('')
const isSearching = ref(false)

// 计算属性
const loading = computed(() => postsStore.loading)
const posts = computed(() => postsStore.posts)
const totalPages = computed(() => postsStore.totalPages)
const currentPage = computed(() => postsStore.currentPage)
const pageSize = computed(() => postsStore.pageSize)
const totalElements = computed(() => postsStore.totalElements)
const isLoggedIn = computed(() => userStore.isAuthenticated)

// 生命周期钩子
onMounted(async () => {
  try {
    await postsStore.fetchPosts()
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败，请稍后再试')
  }
})

// 方法
function goToPostDetail(postId: number) {
  router.push(`/forum/${postId}`)
}

function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    clearSearch()
    return
  }

  isSearching.value = true
  try {
    await postsStore.searchPosts(searchKeyword.value)
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后再试')
  }
}

async function clearSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  try {
    await postsStore.fetchPosts(0)
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败，请稍后再试')
  }
}

async function handlePageChange(page: number) {
  try {
    if (isSearching.value) {
      await postsStore.searchPosts(searchKeyword.value, page - 1)
    } else {
      await postsStore.fetchPosts(page - 1)
    }
    window.scrollTo(0, 0)
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败，请稍后再试')
  }
}
</script>

<style scoped>
.forum-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
}

.title-section h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.title-section p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 250px;
}

.content {
  border-radius: 4px;
  background-color: transparent;
}

.search-info {
  margin-bottom: 20px;
}

.search-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  transition: all 0.3s;
  cursor: default;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.date {
  font-size: 12px;
  color: #909399;
}

.post-content {
  cursor: pointer;
}

.post-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.post-text {
  margin: 0;
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.comments {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
