<template>
  <main-layout>
    <div class="found-items-container">
      <div class="page-header">
        <h1>失物招领</h1>
        <p>查看社区内的拾取物品信息，或发布您捡到的物品</p>
      </div>

      <div class="actions-container">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="16">
            <div class="search-container">
              <el-input
                v-model="filters.keyword"
                placeholder="搜索物品名称、描述或地点"
                clearable
                :prefix-icon="Search"
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              />
              <el-select
                v-model="filters.category"
                placeholder="物品类别"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="category in categories"
                  :key="category.value"
                  :label="category.label"
                  :value="category.value"
                />
              </el-select>
              <el-select
                v-model="filters.status"
                placeholder="状态"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="status in statusOptions"
                  :key="status.value"
                  :label="status.label"
                  :value="status.value"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="action-buttons">
              <el-button
                type="primary"
                :icon="Plus"
                @click="$router.push('/found-items/create')"
              >
                发布失物招领
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 错误状态 -->
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="error-alert"
      />

      <!-- 内容展示 -->
      <template v-if="!loading && !error">
        <!-- 无数据状态 -->
        <el-empty
          v-if="filteredItems.length === 0"
          description="暂无失物招领信息"
        >
          <el-button type="primary" @click="$router.push('/found-items/create')">
            发布失物招领
          </el-button>
        </el-empty>

        <!-- 数据列表 -->
        <div v-else class="items-grid">
          <el-row :gutter="20">
            <el-col
              v-for="item in filteredItems"
              :key="item.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <el-card
                class="item-card card-hover"
                shadow="hover"
                @click="viewItemDetail(item.id)"
              >
                <div class="item-image">
                  <el-image
                    v-if="item.images && item.images.length > 0"
                    :src="item.images[0]"
                    fit="cover"
                    class="card-image"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                  <el-tag
                    class="item-status-tag"
                    :type="getStatusType(item.status)"
                  >
                    {{ getStatusLabel(item.status) }}
                  </el-tag>
                </div>
                <div class="item-content">
                  <h3 class="item-title">{{ item.title }}</h3>
                  <p class="item-category">
                    <el-tag size="small" effect="plain">{{ getCategoryLabel(item.category) }}</el-tag>
                  </p>
                  <p class="item-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ item.foundLocation || '未知地点' }}</span>
                  </p>
                  <p class="item-time">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(item.foundDate) }}</span>
                  </p>
                  <p class="item-poster">
                    <el-icon><User /></el-icon>
                    <span>{{ item.username || item.user?.username }}</span>
                  </p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 分页器 -->
        <el-pagination
          v-if="filteredItems.length > 0"
          class="pagination"
          layout="prev, pager, next, jumper"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.page"
          @current-change="handlePageChange"
        />
      </template>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { format } from 'date-fns'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import {
  Plus,
  Search,
  Picture,
  Location,
  Calendar,
  User
} from '@element-plus/icons-vue'

const router = useRouter()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()
const loading = ref(false)
const error = ref<string | null>(null)

// 筛选条件
const filters = reactive({
  keyword: '',
  category: null as string | null,
  status: null as string | null
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

// 计算属性
const isAuthenticated = computed(() => userStore.isAuthenticated)
const filteredItems = computed(() => {
  return foundItemsStore.filteredItems || []
})
const totalItems = computed(() => filteredItems.value.length)

// 物品类别
const categories = [
  { label: '电子产品', value: 'electronics' },
  { label: '证件卡片', value: 'documents' },
  { label: '日常用品', value: 'daily' },
  { label: '钱包钥匙', value: 'money' },
  { label: '首饰配件', value: 'jewelry' },
  { label: '书籍文件', value: 'books' },
  { label: '箱包服装', value: 'clothing' },
  { label: '其他物品', value: 'other' }
]

// 状态选项
const statusOptions = [
  { value: 'unclaimed', label: '待认领' },
  { value: 'processing', label: '认领中' },
  { value: 'claimed', label: '已认领' },
  { value: 'closed', label: '已关闭' }
]

// 生命周期钩子
onMounted(async () => {
  await fetchFoundItems()
})

// 方法
const fetchFoundItems = async () => {
  loading.value = true
  error.value = null

  try {
    // 设置过滤条件
    foundItemsStore.setFilters({
      keyword: filters.keyword || null,
      category: filters.category || null,
      status: filters.status || null
    })

    await foundItemsStore.fetchFoundItems()

    // 更新分页信息
    pagination.total = foundItemsStore.pagination.total
    pagination.page = foundItemsStore.pagination.page
    pagination.pageSize = foundItemsStore.pagination.pageSize
  } catch (err) {
    console.error('Failed to fetch found items:', err)
    error.value = '获取失物招领列表失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.page = 1
  await fetchFoundItems()
}

const handlePageChange = async (page: number) => {
  pagination.page = page
  foundItemsStore.pagination.page = page
  await fetchFoundItems()
}

const viewItemDetail = (id: number) => {
  router.push(`/found-items/${id}`)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '未知日期'

  try {
    const date = new Date(dateString)
    return format(date, 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'unclaimed':
      return '待认领'
    case 'processing':
      return '认领中'
    case 'claimed':
      return '已认领'
    case 'closed':
      return '已关闭'
    default:
      return '未知'
  }
}

const getStatusType = (status: string) => {
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

const getCategoryLabel = (categoryValue: string) => {
  const category = categories.find(c => c.value === categoryValue)
  return category ? category.label : categoryValue || '其他'
}
</script>

<style scoped>
.found-items-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.actions-container {
  margin-bottom: 30px;
}

.search-container {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-container .el-input {
  flex: 2;
  min-width: 200px;
}

.search-container .el-select {
  flex: 1;
  min-width: 120px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.loading-container {
  margin: 40px 0;
}

.error-alert {
  margin: 20px 0;
}

.items-grid {
  margin-bottom: 30px;
}

.item-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.item-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
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

.item-category {
  margin: 5px 0;
}

.item-location,
.item-time,
.item-poster {
  display: flex;
  align-items: center;
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

.item-location .el-icon,
.item-time .el-icon,
.item-poster .el-icon {
  margin-right: 5px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .action-buttons {
    justify-content: center;
    margin-top: 20px;
  }

  .search-container {
    flex-direction: column;
  }

  .search-container .el-input,
  .search-container .el-select {
    width: 100%;
  }

  .item-image {
    height: 180px;
  }
}
</style>
