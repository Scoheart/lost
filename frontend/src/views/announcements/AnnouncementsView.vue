<template>
  <main-layout>
    <div class="announcements-container">
      <div class="page-header">
        <h1>社区公告</h1>
        <p>查看社区重要通知和信息</p>
      </div>

      <!-- 置顶公告 -->
      <div v-if="stickyAnnouncements.length > 0" class="sticky-announcements">
        <h2 class="section-title">
          <el-icon><Top /></el-icon> 置顶公告
        </h2>
        <el-row :gutter="20">
          <el-col
            v-for="announcement in stickyAnnouncements"
            :key="announcement.id"
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-card shadow="hover" class="announcement-card card-hover" @click="viewAnnouncementDetail(announcement.id)">
              <div class="sticky-badge">
                <el-icon><Top /></el-icon>
              </div>
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <div class="announcement-meta">
                <div>
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(announcement.publishDate) }}</span>
                </div>
                <div>
                  <el-icon><User /></el-icon>
                  <span>{{ announcement.adminName }}</span>
                </div>
              </div>
              <p class="announcement-excerpt">{{ truncateContent(announcement.content) }}</p>
              <el-button type="primary" text>查看详情</el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 公告列表 -->
      <div class="regular-announcements">
        <div class="list-header">
          <h2 class="section-title">全部公告</h2>

          <div class="list-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索公告"
              clearable
              class="search-input"
              :prefix-icon="Search"
              @input="handleSearch"
            />
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 无数据状态 -->
        <el-empty v-else-if="regularAnnouncements.length === 0" description="暂无公告" />

        <!-- 公告列表 -->
        <div v-else class="announcements-list">
          <el-timeline>
            <el-timeline-item
              v-for="announcement in regularAnnouncements"
              :key="announcement.id"
              :timestamp="formatDate(announcement.publishDate)"
              placement="top"
            >
              <el-card shadow="hover" @click="viewAnnouncementDetail(announcement.id)">
                <h3 class="announcement-title">{{ announcement.title }}</h3>
                <p class="announcement-excerpt">{{ truncateContent(announcement.content) }}</p>
                <div class="announcement-footer">
                  <span class="announcement-author">
                    <el-icon><User /></el-icon> {{ announcement.adminName }}
                  </span>
                  <el-button type="primary" text size="small">阅读全文</el-button>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <!-- 分页器 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="totalCount"
              layout="prev, pager, next, jumper"
              background
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Top, Search, Calendar, User } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useAnnouncementsStore } from '@/stores/announcements'

const router = useRouter()
const announcementsStore = useAnnouncementsStore()

// 状态
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性
const stickyAnnouncements = computed(() => {
  return announcementsStore.stickyAnnouncements
})

const regularAnnouncements = computed(() => {
  return announcementsStore.regularAnnouncements
})

const totalCount = computed(() => {
  return announcementsStore.pagination.total
})

// 方法
const viewAnnouncementDetail = (id) => {
  router.push(`/announcements/${id}`)
}

const formatDate = (dateString) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

const truncateContent = (content, maxLength = 100) => {
  if (!content) return ''
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

const handleSearch = () => {
  currentPage.value = 1
  fetchAnnouncements()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchAnnouncements()
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    await announcementsStore.fetchAnnouncements()
  } catch (error) {
    console.error('Failed to fetch announcements:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcements-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  margin-bottom: 20px;
}

.section-title .el-icon {
  margin-right: 8px;
  color: #E6A23C;
}

.sticky-announcements {
  margin-bottom: 40px;
}

.announcement-card {
  height: 100%;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sticky-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #E6A23C;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.announcement-title {
  margin-top: 5px;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

.announcement-meta {
  display: flex;
  margin-bottom: 15px;
  font-size: 13px;
  color: #909399;
}

.announcement-meta div {
  display: flex;
  align-items: center;
  margin-right: 15px;
}

.announcement-meta .el-icon {
  margin-right: 4px;
}

.announcement-excerpt {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-actions {
  display: flex;
}

.search-input {
  width: 240px;
}

.announcements-list {
  margin-top: 20px;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}

.announcement-author {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
}

.announcement-author .el-icon {
  margin-right: 4px;
}

.loading-container {
  padding: 40px 0;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-actions {
    margin-top: 15px;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}
</style>
