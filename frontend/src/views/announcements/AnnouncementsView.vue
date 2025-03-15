<template>
  <main-layout>
    <div class="announcements-container">
      <div class="page-header">
        <h1>社区公告</h1>
        <p>查看社区重要通知和信息</p>
      </div>

      <!-- 公告列表 -->
      <div class="announcements-section">
        <div class="list-header">
          <h2 class="section-title">社区公告</h2>

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
        <el-empty v-else-if="announcements.length === 0" description="暂无公告" />

        <!-- 公告列表 -->
        <div v-else class="announcements-list">
          <el-timeline>
            <el-timeline-item
              v-for="announcement in announcements"
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
          <div v-if="totalCount > 0" class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[20, 50, 100]"
              :total="totalCount"
              layout="sizes, prev, pager, next, jumper, total"
              background
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
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
import type { Announcement } from '@/stores/announcements'
import { useAnnouncementsStore } from '@/stores/announcements'
import { format } from 'date-fns'
import MainLayout from '@/components/layout/MainLayout.vue'
import { Search, Calendar, User } from '@element-plus/icons-vue'

const router = useRouter()
const announcementsStore = useAnnouncementsStore()

// 状态
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(5)

// 计算属性
const announcements = computed(() => {
  return announcementsStore.announcements
})

const totalCount = computed(() => {
  return announcementsStore.pagination.total
})

// 方法
const viewAnnouncementDetail = (id: number) => {
  router.push(`/announcements/${id}`)
}

const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

const truncateContent = (content: string, maxLength = 100) => {
  if (!content) return ''
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

const handleSearch = () => {
  currentPage.value = 1
  fetchAnnouncements()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchAnnouncements()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchAnnouncements()
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    await announcementsStore.fetchAnnouncements({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value || undefined
    })

    console.log('Announcements fetch complete. Data received:', {
      all: announcementsStore.announcements,
      total: announcementsStore.pagination.total
    })
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  position: relative;
  padding-left: 12px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 16px;
  width: 4px;
  background: #409EFF;
  border-radius: 2px;
}

.section-title .el-icon {
  margin-right: 5px;
}

.announcements-section {
  margin-bottom: 50px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.list-actions {
  display: flex;
  align-items: center;
}

.search-input {
  width: 250px;
}

.loading-container {
  padding: 20px 0;
}

.announcements-list {
  margin-top: 20px;
}

.announcement-card {
  margin-bottom: 20px;
  position: relative;
  height: 100%;
  transition: transform 0.3s;
}

.card-hover:hover {
  transform: translateY(-5px);
}

.announcement-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  color: #909399;
}

.announcement-meta div {
  display: flex;
  align-items: center;
}

.announcement-meta .el-icon {
  margin-right: 4px;
}

.announcement-excerpt {
  color: #606266;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.announcement-author {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.announcement-author .el-icon {
  margin-right: 4px;
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
    margin-top: 10px;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}
</style>
