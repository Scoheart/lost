<template>
  <main-layout>
    <div class="forum-container">
      <div class="header-section">
        <h1 class="forum-title">社区论坛</h1>
        <div class="action-buttons">
          <el-button type="primary" @click="$router.push('/forum/create')">
            <el-icon><Plus /></el-icon>发布新帖子
          </el-button>
        </div>
      </div>

      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索帖子..."
          class="search-input"
          clearable
          @clear="handleSearch"
          @input="handleSearchInput"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="selectedCategory"
          placeholder="全部分类"
          style="margin-left: 10px; width: 150px"
          @change="handleCategoryChange"
        >
          <el-option label="全部分类" value="" />
          <el-option
            v-for="category in categories"
            :key="category.value"
            :label="category.label"
            :value="category.value"
          />
        </el-select>
        <el-select
          v-model="sortBy"
          placeholder="排序"
          style="margin-left: 10px; width: 120px"
          @change="handleSort"
        >
          <el-option label="最新发布" value="latest" />
          <el-option label="最多评论" value="mostComments" />
          <el-option label="最多点赞" value="mostLikes" />
        </el-select>
      </div>

      <el-card class="categories-card" v-if="!searchQuery">
        <template #header>
          <div class="categories-header">
            <span>热门分类</span>
            <el-button text @click="showAllCategories = !showAllCategories">
              {{ showAllCategories ? '收起' : '查看全部' }}
            </el-button>
          </div>
        </template>
        <div class="categories-list">
          <el-tag
            v-for="category in (showAllCategories ? categories : categories.slice(0, 8))"
            :key="category.value"
            :type="selectedCategory === category.value ? '' : 'info'"
            effect="plain"
            class="category-tag"
            @click="selectedCategory = category.value; handleCategoryChange()"
          >
            {{ category.label }}
          </el-tag>
        </div>
      </el-card>

      <div class="main-content">
        <div v-if="loading" class="loading-section">
          <el-skeleton :rows="10" animated />
        </div>

        <el-empty
          v-else-if="posts.length === 0"
          description="暂无相关帖子"
          :image-size="200"
        >
          <el-button type="primary" @click="$router.push('/forum/create')">
            发布第一篇帖子
          </el-button>
        </el-empty>

        <div v-else class="posts-list">
          <el-card
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            @click="viewPostDetail(post.id)"
          >
            <div class="post-header">
              <div class="post-user-info">
                <el-avatar :size="40" :src="post.user.avatar">
                  {{ post.user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-name-time">
                  <span class="username">{{ post.user.username }}</span>
                  <span class="post-time">{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
              <el-tag v-if="post.category" size="small" type="info" effect="plain">
                {{ getCategoryLabel(post.category) }}
              </el-tag>
            </div>

            <h3 class="post-title">{{ post.title }}</h3>

            <p class="post-summary" v-if="post.content">{{ truncateContent(post.content) }}</p>

            <div v-if="post.images && post.images.length > 0" class="post-images">
              <el-image
                v-for="(image, index) in post.images.slice(0, 3)"
                :key="index"
                :src="image"
                fit="cover"
                class="post-image"
                :preview-src-list="post.images"
                preview-teleported
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-if="post.images.length > 3" class="more-images">
                <span>+{{ post.images.length - 3 }}</span>
              </div>
            </div>

            <div class="post-footer">
              <div class="post-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ post.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatLineRound /></el-icon>
                  {{ post.commentCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ post.likeCount || 0 }}
                </span>
              </div>
            </div>
          </el-card>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="total, prev, pager, next, jumper"
              background
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <div class="sidebar">
          <el-card class="hot-topics-card">
            <template #header>
              <div class="card-header">
                <span>热门话题</span>
              </div>
            </template>
            <div v-if="loadingHotTopics" class="loading-sidebar">
              <el-skeleton :rows="5" animated />
            </div>
            <ul v-else class="hot-topics-list">
              <li
                v-for="topic in hotTopics"
                :key="topic.id"
                class="hot-topic-item"
                @click="viewPostDetail(topic.id)"
              >
                <span class="hot-topic-title">{{ topic.title }}</span>
                <div class="hot-topic-meta">
                  <span class="hot-comments">
                    <el-icon><ChatLineRound /></el-icon> {{ topic.commentCount }}
                  </span>
                  <span class="hot-time">{{ formatDate(topic.createdAt, 'MM-dd') }}</span>
                </div>
              </li>
            </ul>
          </el-card>

          <el-card class="stats-card">
            <template #header>
              <div class="card-header">
                <span>论坛数据</span>
              </div>
            </template>
            <ul class="stats-list">
              <li class="stats-item">
                <div class="stats-label">帖子总数</div>
                <div class="stats-value">{{ stats.totalPosts || 0 }}</div>
              </li>
              <li class="stats-item">
                <div class="stats-label">用户总数</div>
                <div class="stats-value">{{ stats.totalUsers || 0 }}</div>
              </li>
              <li class="stats-item">
                <div class="stats-label">今日发帖</div>
                <div class="stats-value">{{ stats.todayPosts || 0 }}</div>
              </li>
              <li class="stats-item">
                <div class="stats-label">今日活跃</div>
                <div class="stats-value">{{ stats.activeUsers || 0 }}</div>
              </li>
            </ul>
          </el-card>
        </div>
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { format } from 'date-fns'
import {
  Plus,
  Search,
  View,
  ChatLineRound,
  Star,
  Picture
} from '@element-plus/icons-vue'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import debounce from 'lodash/debounce'
import MainLayout from '@/components/layout/MainLayout.vue'

const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()

// 搜索和筛选
const searchQuery = ref('')
const selectedCategory = ref('')
const sortBy = ref('latest')
const showAllCategories = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据
const posts = ref([])
const hotTopics = ref([])
const loading = ref(false)
const loadingHotTopics = ref(false)
const stats = ref({
  totalPosts: 0,
  totalUsers: 0,
  todayPosts: 0,
  activeUsers: 0
})

// 分类
const categories = [
  { label: '社区通知', value: 'community-notice' },
  { label: '活动组织', value: 'activity' },
  { label: '失物招领', value: 'lost-found' },
  { label: '闲置交易', value: 'trading' },
  { label: '业主投诉', value: 'complaint' },
  { label: '业主提问', value: 'question' },
  { label: '民生话题', value: 'livelihood' },
  { label: '装修经验', value: 'decoration' },
  { label: '邻里互助', value: 'help' },
  { label: '安全防范', value: 'security' },
  { label: '小区建设', value: 'construction' },
  { label: '其他话题', value: 'other' }
]

// 方法
const fetchPosts = async () => {
  loading.value = true
  try {
    const response = await forumStore.fetchPosts({
      page: currentPage.value,
      pageSize: pageSize.value,
      category: selectedCategory.value,
      keyword: searchQuery.value,
      sort: sortBy.value
    })

    posts.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch forum posts:', error)
    ElMessage.error('获取论坛帖子失败')
  } finally {
    loading.value = false
  }
}

const fetchHotTopics = async () => {
  loadingHotTopics.value = true
  try {
    const response = await forumStore.fetchHotTopics()
    hotTopics.value = response.items || []
  } catch (error) {
    console.error('Failed to fetch hot topics:', error)
  } finally {
    loadingHotTopics.value = false
  }
}

const fetchForumStats = async () => {
  try {
    const response = await forumStore.fetchForumStats()
    stats.value = response || {
      totalPosts: 0,
      totalUsers: 0,
      todayPosts: 0,
      activeUsers: 0
    }
  } catch (error) {
    console.error('Failed to fetch forum stats:', error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchPosts()
}

const handleCategoryChange = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleSort = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
}

const handleSearchInput = debounce(() => {
  currentPage.value = 1
  fetchPosts()
}, 500)

const viewPostDetail = (id) => {
  router.push(`/forum/${id}`)
}

const formatDate = (dateString, formatStr = 'yyyy-MM-dd') => {
  try {
    return format(new Date(dateString), formatStr)
  } catch (error) {
    return dateString
  }
}

const truncateContent = (content, maxLength = 150) => {
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

const getCategoryLabel = (categoryValue) => {
  const category = categories.find(c => c.value === categoryValue)
  return category ? category.label : categoryValue
}

// 生命周期
onMounted(() => {
  fetchPosts()
  fetchHotTopics()
  fetchForumStats()
})

// 监听搜索和分类变化
watch([selectedCategory, sortBy], () => {
  currentPage.value = 1
  fetchPosts()
})
</script>

<style scoped>
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forum-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.search-bar {
  display: flex;
  margin-bottom: 20px;
}

.search-input {
  max-width: 400px;
}

.categories-card {
  margin-bottom: 20px;
}

.categories-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.categories-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
  margin-right: 8px;
  margin-bottom: 8px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.loading-section,
.loading-sidebar {
  padding: 20px 0;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.post-user-info {
  display: flex;
  align-items: center;
}

.user-name-time {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}

.username {
  font-weight: 500;
  font-size: 14px;
}

.post-time {
  font-size: 12px;
  color: #909399;
}

.post-title {
  margin: 10px 0;
  font-size: 18px;
  font-weight: 600;
}

.post-summary {
  color: #606266;
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.5;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.post-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.more-images {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #909399;
}

.hot-topics-card,
.stats-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
}

.hot-topics-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-topic-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.hot-topic-item:last-child {
  border-bottom: none;
}

.hot-topic-item:hover .hot-topic-title {
  color: #409eff;
}

.hot-topic-title {
  display: block;
  font-size: 14px;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-topic-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.stats-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stats-item {
  text-align: center;
  padding: 10px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stats-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .post-images {
    overflow-x: auto;
  }
}
</style>
