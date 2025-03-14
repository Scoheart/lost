<template>
  <main-layout>
    <div class="announcement-detail-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div class="skeleton-container">
              <el-skeleton-item variant="p" style="width: 60%; height: 40px" />
              <div class="skeleton-meta">
                <el-skeleton-item variant="text" style="width: 30%" />
                <el-skeleton-item variant="text" style="width: 30%" />
              </div>
              <el-skeleton-item variant="p" style="width: 100%; height: 400px" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 错误状态 -->
      <el-result
        v-else-if="error"
        icon="error"
        title="获取公告详情失败"
        sub-title="请检查网络连接或稍后再试"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/announcements')">
            返回公告列表
          </el-button>
        </template>
      </el-result>

      <!-- 公告内容 -->
      <template v-else>
        <div class="announcement-header">
          <el-page-header
            @back="$router.push('/announcements')"
            :title="announcement.title"
          />
          <el-tag v-if="announcement.isSticky" type="warning">置顶</el-tag>
        </div>

        <div class="announcement-content">
          <h1 class="announcement-title">{{ announcement.title }}</h1>

          <div class="announcement-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ announcement.adminName }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(announcement.publishDate) }}</span>
            </div>
          </div>

          <div class="content-divider"></div>

          <div class="announcement-body">
            <p v-for="(paragraph, index) in contentParagraphs" :key="index">
              {{ paragraph }}
            </p>
          </div>
        </div>

        <!-- 相关动作 -->
        <div class="announcement-actions">
          <el-button type="primary" plain @click="$router.push('/announcements')">
            返回公告列表
          </el-button>
        </div>

        <!-- 相关公告 -->
        <div class="related-announcements" v-if="relatedAnnouncements.length > 0">
          <h3 class="section-title">相关公告</h3>
          <el-row :gutter="20">
            <el-col
              v-for="relatedAnnouncement in relatedAnnouncements"
              :key="relatedAnnouncement.id"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <el-card
                shadow="hover"
                class="related-card"
                @click="viewAnnouncementDetail(relatedAnnouncement.id)"
              >
                <h4>{{ relatedAnnouncement.title }}</h4>
                <p class="related-date">{{ formatDate(relatedAnnouncement.publishDate) }}</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Calendar } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useAnnouncementsStore } from '@/stores/announcements'

const route = useRoute()
const router = useRouter()
const announcementsStore = useAnnouncementsStore()

// 状态变量
const loading = ref(true)
const error = ref(false)
const announcement = ref({
  id: 0,
  title: '',
  content: '',
  adminName: '',
  publishDate: '',
  isSticky: false
})
const relatedAnnouncements = ref([])

// 计算属性
const contentParagraphs = computed(() => {
  if (!announcement.value.content) return []

  return announcement.value.content
    .split('\n')
    .filter(paragraph => paragraph.trim() !== '')
})

// 方法
const fetchAnnouncementDetail = async (id: number) => {
  loading.value = true
  error.value = false

  try {
    const response = await announcementsStore.fetchAnnouncementById(id)

    if (response.success) {
      announcement.value = response.data
      document.title = `${announcement.value.title} - 社区公告`
      fetchRelatedAnnouncements()
    } else {
      error.value = true
      ElMessage.error(response.message || '获取公告详情失败')
    }
  } catch (err) {
    console.error('Failed to fetch announcement:', err)
    error.value = true
    ElMessage.error('获取公告详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const fetchRelatedAnnouncements = async () => {
  try {
    // 在实际应用中，这里会根据当前公告的标签或者分类来获取相关公告
    // 这里简化为获取最新的3条公告，排除当前公告
    await announcementsStore.fetchAnnouncements()

    relatedAnnouncements.value = announcementsStore.announcements
      .filter(item => item.id !== announcement.value.id)
      .slice(0, 3)
  } catch (error) {
    console.error('Failed to fetch related announcements:', error)
  }
}

const formatDateTime = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return dateString
  }
}

const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

const viewAnnouncementDetail = (id: number) => {
  router.push(`/announcements/${id}`)
}

// 生命周期钩子
onMounted(async () => {
  const id = Number(route.params.id)

  if (isNaN(id)) {
    error.value = true
    ElMessage.error('无效的公告ID')
    return
  }

  await fetchAnnouncementDetail(id)
})
</script>

<style scoped>
.announcement-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 40px 0;
}

.skeleton-container {
  padding: 20px;
}

.skeleton-meta {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.announcement-content {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.announcement-title {
  font-size: 28px;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 20px;
  line-height: 1.4;
}

.announcement-meta {
  display: flex;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  color: #909399;
  font-size: 14px;
}

.meta-item .el-icon {
  margin-right: 6px;
}

.content-divider {
  height: 1px;
  background: #e0e0e0;
  margin: 20px 0;
}

.announcement-body {
  color: #303133;
  line-height: 1.8;
  font-size: 16px;
}

.announcement-body p {
  margin-bottom: 16px;
}

.announcement-actions {
  margin: 30px 0;
  display: flex;
  justify-content: center;
}

.related-announcements {
  margin-top: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  height: 16px;
  width: 4px;
  background: #409EFF;
  border-radius: 2px;
}

.related-card {
  height: 100%;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-card h4 {
  margin: 0 0 10px;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-date {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

@media (max-width: 768px) {
  .announcement-title {
    font-size: 22px;
  }

  .announcement-content {
    padding: 20px;
  }
}
</style>
