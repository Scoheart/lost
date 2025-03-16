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
        <!-- 添加面包屑导航 -->
        <div class="navigation">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/announcements' }">社区公告</el-breadcrumb-item>
            <el-breadcrumb-item>详情页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="announcement-content">
          <h1 class="announcement-title">{{ announcement.title }}</h1>

          <div class="announcement-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ announcement.adminName }}</span>
            </div>
            <div class="meta-divider">|</div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>发布于: {{ formatDateTime(announcement.createdAt) }}</span>
            </div>
            <div class="meta-divider">|</div>
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>更新于: {{ formatDateTime(announcement.updatedAt) }}</span>
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
      </template>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Announcement } from '@/stores/announcements'
import { useAnnouncementsStore } from '@/stores/announcements'
import { format } from 'date-fns'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
// Import icons
import { User, Calendar, Timer } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const announcementsStore = useAnnouncementsStore()

// 状态变量
const loading = ref(true)
const error = ref(false)
const announcement = ref<Partial<Announcement>>({
  id: 0,
  title: '',
  content: '',
  adminName: '',
  publishDate: ''
})

// 计算属性
const contentParagraphs = computed(() => {
  const content = announcement.value.content || ''
  return content.split('\n').filter(paragraph => paragraph.trim() !== '')
})

// 方法
const fetchAnnouncementDetail = async (id: number) => {
  loading.value = true
  error.value = false

  try {
    const response = await announcementsStore.fetchAnnouncementById(id)

    if (response.success && response.data) {
      announcement.value = response.data
      document.title = `${announcement.value.title || ''} - 社区公告`
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

const formatDateTime = (dateString: string | undefined) => {
  if (!dateString) return '未知日期'
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return dateString
  }
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

/* 添加面包屑导航样式 */
.navigation {
  margin-bottom: 20px;
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
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 0;
  color: #909399;
  font-size: 14px;
}

.meta-divider {
  margin: 0 10px;
  color: #DCDFE6;
  font-size: 14px;
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

.meta-item .el-icon {
  margin-right: 6px;
}

@media (max-width: 768px) {
  .announcement-title {
    font-size: 22px;
  }

  .announcement-content {
    padding: 20px;
  }

  .announcement-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .meta-item {
    margin-bottom: 8px;
  }

  .meta-divider {
    display: none;
  }
}
</style>
