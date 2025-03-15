<template>
  <main-layout>
    <div class="home-page">
      <!-- 轮播图 Banner -->
      <el-carousel height="400px" indicator-position="outside" class="banner">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-content" :style="{ backgroundImage: `url(${banner.image})` }">
            <div class="banner-text">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.description }}</p>
              <el-button type="primary" @click="goToPage(banner.link)">{{ banner.buttonText }}</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>

      <!-- 服务功能卡片 -->
      <div class="service-cards">
        <h2 class="section-title text-center">我们的服务</h2>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" v-for="service in services" :key="service.id">
            <el-card shadow="hover" class="service-card card-hover" @click="goToPage(service.link)">
              <div class="service-icon">
                <el-icon :size="40"><component :is="service.icon" /></el-icon>
              </div>
              <h3>{{ service.title }}</h3>
              <p>{{ service.description }}</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 最新寻物启事 -->
      <div class="latest-section">
        <div class="section-header">
          <h2 class="section-title">最新寻物启事</h2>
          <el-button type="primary" text @click="$router.push('/lost-items')">查看更多</el-button>
        </div>
        <div v-if="loadingLostItems" class="loading-container">
          <el-skeleton :rows="1" animated />
        </div>
        <el-empty v-else-if="latestLostItems.length === 0" description="暂无寻物启事" />
        <el-row v-else :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in latestLostItems" :key="item.id">
            <el-card shadow="hover" class="item-card card-hover" @click="viewLostItemDetail(item.id)">
              <div class="item-image">
                <el-image v-if="item.images && item.images.length > 0" :src="item.images[0]" fit="cover">
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
                <el-tag class="item-status-tag" :type="getLostStatusType(item.status)">
                  {{ getLostStatusLabel(item.status) }}
                </el-tag>
              </div>
              <div class="item-content">
                <h3 class="item-title">{{ item.title }}</h3>
                <p class="item-location">
                  <el-icon><Location /></el-icon>
                  <span>{{ item.lostLocation }}</span>
                </p>
                <p class="item-time">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(item.lostDate) }}</span>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 最新失物招领 -->
      <div class="latest-section">
        <div class="section-header">
          <h2 class="section-title">最新失物招领</h2>
          <el-button type="primary" text @click="$router.push('/found-items')">查看更多</el-button>
        </div>
        <div v-if="loadingFoundItems" class="loading-container">
          <el-skeleton :rows="1" animated />
        </div>
        <el-empty v-else-if="latestFoundItems.length === 0" description="暂无失物招领" />
        <el-row v-else :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in latestFoundItems" :key="item.id">
            <el-card shadow="hover" class="item-card card-hover" @click="viewFoundItemDetail(item.id)">
              <div class="item-image">
                <el-image v-if="item.images && item.images.length > 0" :src="item.images[0]" fit="cover">
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
                <el-tag class="item-status-tag" :type="getFoundStatusType(item.status)">
                  {{ getFoundStatusLabel(item.status) }}
                </el-tag>
              </div>
              <div class="item-content">
                <h3 class="item-title">{{ item.title }}</h3>
                <p class="item-location">
                  <el-icon><Location /></el-icon>
                  <span>{{ item.foundLocation }}</span>
                </p>
                <p class="item-time">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(item.foundDate) }}</span>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 公告栏 -->
      <div class="latest-section">
        <div class="section-header">
          <h2 class="section-title">社区公告</h2>
          <el-button type="primary" text @click="$router.push('/announcements')">查看更多</el-button>
        </div>
        <div v-if="loadingAnnouncements" class="loading-container">
          <el-skeleton :rows="1" animated />
        </div>
        <el-empty v-else-if="announcements.length === 0" description="暂无公告" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="announcement in announcements"
            :key="announcement.id"
            :timestamp="formatDate(announcement.publishDate)"
            placement="top"
            :type="announcement.isSticky ? 'primary' : ''"
          >
            <el-card shadow="hover" @click="viewAnnouncementDetail(announcement.id)">
              <div class="announcement-card">
                <div class="announcement-header">
                  <h3>{{ announcement.title }}</h3>
                  <el-tag v-if="announcement.isSticky" size="small" type="warning">置顶</el-tag>
                </div>
                <p class="announcement-summary">{{ truncateContent(announcement.content) }}</p>
                <div class="announcement-footer">
                  <span>发布人: {{ announcement.adminName }}</span>
                  <el-button type="primary" text size="small">查看详情</el-button>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 统计数据 -->
      <div class="stats-section">
        <h2 class="section-title text-center">系统总览</h2>
        <div v-if="loadingStats" class="loading-container">
          <el-skeleton :rows="1" animated />
        </div>
        <el-row v-else :gutter="30">
          <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="stat in stats" :key="stat.id">
            <el-card shadow="hover" class="stat-card">
              <el-icon :size="32" :class="stat.color"><component :is="stat.icon" /></el-icon>
              <div class="stat-info">
                <h3 class="stat-title">{{ stat.title }}</h3>
                <p class="stat-value">{{ stat.value }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  FindReplace,
  Bell,
  ChatDotRound,
  Location,
  Calendar,
  Picture,
  Check as Checked,
  Timer,
} from '@element-plus/icons-vue'
import { format } from 'date-fns'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useLostItemsStore } from '@/stores/lostItems'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useAnnouncementsStore } from '@/stores/announcements'
import type { LostItem } from '@/stores/lostItems'
import type { FoundItem } from '@/stores/foundItems'
import type { Announcement } from '@/stores/announcements'

const router = useRouter()
const lostItemsStore = useLostItemsStore()
const foundItemsStore = useFoundItemsStore()
const announcementsStore = useAnnouncementsStore()

// 加载状态
const loadingLostItems = ref(false)
const loadingFoundItems = ref(false)
const loadingAnnouncements = ref(false)
const loadingStats = ref(false)

// 数据
const latestLostItems = ref<LostItem[]>([])
const latestFoundItems = ref<FoundItem[]>([])
const announcements = ref<Announcement[]>([])

// Banner数据
const banners = [
  {
    id: 1,
    title: '丢失物品？立即发布寻物启事',
    description: '通过我们的平台发布寻物启事，提高物品找回几率',
    buttonText: '发布寻物启事',
    link: '/lost-items/create',
    image: 'https://images.unsplash.com/photo-1606293926794-de930d0b3113'
  },
  {
    id: 2,
    title: '拾获物品？发布失物招领',
    description: '拾获物品后在平台上发布，帮助失主尽快认领',
    buttonText: '发布失物招领',
    link: '/found-items/create',
    image: 'https://images.unsplash.com/photo-1577979749830-f1d742b96791'
  },
  {
    id: 3,
    title: '社区互助，让每一次丢失都有回家的可能',
    description: '共建互助社区，提高物品归还率',
    buttonText: '了解更多',
    link: '/announcements',
    image: 'https://images.unsplash.com/photo-1528605248644-14dd04022da1'
  }
]

// 服务卡片数据
const services = [
  {
    id: 1,
    title: '寻物启事',
    description: '发布丢失物品信息，提高找回几率',
    icon: 'Search',
    link: '/lost-items'
  },
  {
    id: 2,
    title: '失物招领',
    description: '发布拾获物品信息，帮助失主找回',
    icon: 'FindReplace',
    link: '/found-items'
  },
  {
    id: 3,
    title: '社区公告',
    description: '查看社区最新公告和通知',
    icon: 'Bell',
    link: '/announcements'
  }
]

// 统计数据
const stats = ref([
  {
    id: 1,
    title: '寻物启事',
    value: '0',
    icon: 'Search',
    color: 'text-warning'
  },
  {
    id: 2,
    title: '失物招领',
    value: '0',
    icon: 'FindReplace',
    color: 'text-primary'
  },
  {
    id: 3,
    title: '成功找回',
    value: '0',
    icon: 'Checked',
    color: 'text-success'
  },
  {
    id: 4,
    title: '平均找回时间',
    value: '计算中...',
    icon: 'Timer',
    color: 'text-info'
  }
])

// 方法
const goToPage = (link: string) => {
  router.push(link)
}

const viewLostItemDetail = (id: number) => {
  router.push(`/lost-items/${id}`)
}

const viewFoundItemDetail = (id: number) => {
  router.push(`/found-items/${id}`)
}

const viewAnnouncementDetail = (id: number) => {
  router.push(`/announcements/${id}`)
}

const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString)
    return format(date, 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

const truncateContent = (content: string, maxLength = 100) => {
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

const getLostStatusLabel = (status: string) => {
  switch (status) {
    case 'pending':
      return '寻找中'
    case 'found':
      return '已找到'
    case 'closed':
      return '已关闭'
    default:
      return '未知'
  }
}

const getLostStatusType = (status: string) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'found':
      return 'success'
    case 'closed':
      return 'info'
    default:
      return 'info'
  }
}

const getFoundStatusLabel = (status: string) => {
  switch (status) {
    case 'unclaimed':
      return '待认领'
    case 'claimed':
      return '已认领'
    case 'processing':
      return '认领中'
    case 'closed':
      return '已关闭'
    default:
      return '未知'
  }
}

const getFoundStatusType = (status: string) => {
  switch (status) {
    case 'unclaimed':
      return 'primary'
    case 'processing':
      return 'warning'
    case 'claimed':
      return 'success'
    case 'closed':
      return 'info'
    default:
      return 'info'
  }
}

// 生命周期钩子
onMounted(async () => {
  // 获取最新寻物启事
  loadingLostItems.value = true
  try {
    await lostItemsStore.fetchLostItems()
    latestLostItems.value = lostItemsStore.items.slice(0, 4)
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
  } finally {
    loadingLostItems.value = false
  }

  // 获取最新失物招领
  loadingFoundItems.value = true
  try {
    await foundItemsStore.fetchFoundItems()
    latestFoundItems.value = foundItemsStore.items.slice(0, 4)
  } catch (error) {
    console.error('Failed to fetch found items:', error)
  } finally {
    loadingFoundItems.value = false
  }

  // 获取最新公告
  loadingAnnouncements.value = true
  try {
    await announcementsStore.fetchAnnouncements()
    announcements.value = announcementsStore.announcements.slice(0, 3)
  } catch (error) {
    console.error('Failed to fetch announcements:', error)
  } finally {
    loadingAnnouncements.value = false
  }

  // 加载统计数据
  await loadStatistics()
})

// 加载统计数据
const loadStatistics = async () => {
  loadingStats.value = true
  try {
    // 获取统计数据
    const lostItemCount = lostItemsStore.items.length
    const foundItemCount = foundItemsStore.items.length

    // 成功找回的物品数量（状态为已找到的寻物+已认领的失物）
    const foundItemsCount = lostItemsStore.items.filter(item => item.status === 'found').length
    const claimedItemsCount = foundItemsStore.items.filter(item => item.status === 'claimed').length
    const successCount = foundItemsCount + claimedItemsCount

    // 计算平均找回时间
    let totalDays = 0
    let itemsWithValidDates = 0

    // 计算已找到的寻物的平均时间
    lostItemsStore.items.filter(item => item.status === 'found').forEach(item => {
      try {
        const lostDate = new Date(item.lostDate)
        const foundDate = new Date(item.updatedAt) // 假设updatedAt是物品状态更新为已找到的时间
        if (!isNaN(lostDate.getTime()) && !isNaN(foundDate.getTime())) {
          const diffTime = Math.abs(foundDate.getTime() - lostDate.getTime())
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
          totalDays += diffDays
          itemsWithValidDates++
        }
      } catch (e) {
        console.error('Error calculating date difference:', e)
      }
    })

    // 计算已认领的失物的平均时间
    foundItemsStore.items.filter(item => item.status === 'claimed').forEach(item => {
      try {
        const foundDate = new Date(item.foundDate)
        const claimedDate = new Date(item.updatedAt) // 假设updatedAt是物品状态更新为已认领的时间
        if (!isNaN(foundDate.getTime()) && !isNaN(claimedDate.getTime())) {
          const diffTime = Math.abs(claimedDate.getTime() - foundDate.getTime())
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
          totalDays += diffDays
          itemsWithValidDates++
        }
      } catch (e) {
        console.error('Error calculating date difference:', e)
      }
    })

    // 计算平均天数
    let avgDays = itemsWithValidDates > 0 ? (totalDays / itemsWithValidDates).toFixed(1) : '0'

    // 更新统计数据
    stats.value = [
      {
        id: 1,
        title: '寻物启事',
        value: lostItemCount.toString(),
        icon: 'Search',
        color: 'text-warning'
      },
      {
        id: 2,
        title: '失物招领',
        value: foundItemCount.toString(),
        icon: 'FindReplace',
        color: 'text-primary'
      },
      {
        id: 3,
        title: '成功找回',
        value: successCount.toString(),
        icon: 'Checked',
        color: 'text-success'
      },
      {
        id: 4,
        title: '平均找回时间',
        value: `${avgDays}天`,
        icon: 'Timer',
        color: 'text-info'
      }
    ]
  } catch (error) {
    console.error('Failed to load statistics:', error)
  } finally {
    loadingStats.value = false
  }
}
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

/* Banner */
.banner {
  margin-bottom: 40px;
}

.banner-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  position: relative;
}

.banner-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
}

.banner-text {
  position: relative;
  color: white;
  padding: 0 60px;
  max-width: 600px;
  z-index: 1;
}

.banner-text h2 {
  font-size: 32px;
  margin-bottom: 16px;
}

.banner-text p {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.9;
}

/* 服务卡片 */
.service-cards {
  margin-bottom: 40px;
}

.service-card {
  height: 220px;
  padding: 24px;
  margin-bottom: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-icon {
  margin-bottom: 16px;
  color: #409EFF;
}

.service-card h3 {
  font-size: 18px;
  margin-bottom: 12px;
}

.service-card p {
  color: #606266;
  font-size: 14px;
}

/* 最新物品和公告 */
.latest-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  padding-bottom: 15px;
  position: relative;
  margin-bottom: 25px;
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  border-radius: 3px;
}

.section-title.text-center::after {
  left: 50%;
  transform: translateX(-50%);
}

.text-center {
  text-align: center;
}

.item-card {
  margin-bottom: 20px;
  cursor: pointer;
}

.item-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-placeholder .el-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.item-status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.item-content {
  padding: 15px 0 5px;
}

.item-title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-location,
.item-time {
  display: flex;
  align-items: center;
  margin: 6px 0;
  font-size: 13px;
  color: #606266;
}

.item-location .el-icon,
.item-time .el-icon {
  margin-right: 5px;
}

/* 公告样式 */
.announcement-card {
  cursor: pointer;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.announcement-summary {
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

/* 统计卡片 */
.stats-section {
  margin-top: 60px;
  margin-bottom: 40px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  margin-bottom: 20px;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-info {
  margin-left: 15px;
  overflow: hidden;
}

.stat-title {
  font-size: 16px;
  margin: 0 0 5px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.text-warning {
  color: #E6A23C;
}

.text-primary {
  color: #409EFF;
}

.text-success {
  color: #67C23A;
}

.text-info {
  color: #909399;
}

.loading-container {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .banner-text {
    padding: 0 20px;
  }

  .banner-text h2 {
    font-size: 24px;
  }

  .banner-text p {
    font-size: 16px;
  }

  .section-title {
    font-size: 20px;
  }

  .stat-card {
    margin-bottom: 15px;
    padding: 15px;
  }

  .stat-info {
    margin-left: 10px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>
