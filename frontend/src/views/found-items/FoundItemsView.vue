<template>
  <main-layout>
    <div class="found-items-container">
      <div class="page-header">
        <h1>失物招领</h1>
        <p>查看社区内的拾取物品信息，或发布您捡到的物品</p>
      </div>

      <div class="actions-container">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="24" :lg="24">
            <div class="filter-bar">
              <div class="filter-container">
                <div class="filter-inputs">
                  <el-input
                    v-model="filters.keyword"
                    placeholder="搜索物品名称、描述或地点"
                    clearable
                    :prefix-icon="Search"
                    @clear="handleSearch"
                    @keyup.enter="handleSearch"
                    class="search-input"
                  />
                  <el-select
                    v-model="filters.category"
                    placeholder="物品类别"
                    clearable
                    @change="handleSearch"
                    class="filter-select"
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
                    class="filter-select"
                  >
                    <el-option
                      v-for="status in statusOptions"
                      :key="status.value"
                      :label="status.label"
                      :value="status.value"
                    />
                  </el-select>
                </div>

                <!-- 按钮区域单独放置 -->
                <div class="button-container">
                  <el-button
                    v-if="isFilterActive"
                    type="primary"
                    :icon="Delete"
                    plain
                    @click="resetFilters"
                    class="filter-btn"
                  >
                    清除筛选
                  </el-button>

                  <el-button
                    type="primary"
                    :icon="Plus"
                    @click="$router.push('/found-items/create')"
                    class="create-btn"
                  >
                    发布失物招领
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 显示激活的筛选条件 -->
      <div v-if="isFilterActive" class="active-filters">
        <span class="filter-label">筛选条件:</span>
        <div class="filter-tags">
          <el-tag v-if="filters.keyword" closable @close="clearKeyword" class="filter-tag">
            关键词: {{ filters.keyword }}
          </el-tag>
          <el-tag v-if="filters.category" closable @close="clearCategory" class="filter-tag">
            类别: {{ getCategoryLabel(filters.category) }}
          </el-tag>
          <el-tag v-if="filters.status" closable @close="clearStatus" class="filter-tag">
            状态: {{ getStatusText(filters.status) }}
          </el-tag>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 错误状态 -->
      <el-alert v-if="error" :title="error" type="error" show-icon class="error-alert" />

      <!-- 内容展示 -->
      <template v-if="!loading && !error">
        <!-- 无数据状态 -->
        <el-empty
          v-if="filteredItems.length === 0"
          :description="
            isFilterActive ? '未找到符合条件的物品，请尝试其他筛选条件' : '暂无失物招领信息'
          "
        >
          <template v-if="isFilterActive">
            <el-button @click="resetFilters">清除筛选条件</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/found-items/create')">
              发布失物招领
            </el-button>
          </template>
        </el-empty>

        <!-- 数据列表 -->
        <div v-else class="items-grid">
          <el-row :gutter="20">
            <el-col v-for="item in filteredItems" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="item-card card-hover" shadow="hover" @click="viewItemDetail(item.id)">
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
                  <el-tag class="item-status-tag" :type="getStatusType(item.status)">
                    {{ getStatusText(item.status) }}
                  </el-tag>
                </div>
                <div class="item-content">
                  <h3 class="item-title">{{ item.title }}</h3>
                  <div class="item-info">
                    <p class="item-category">
                      <el-tag size="small" effect="plain">{{
                        getCategoryLabel(item.category)
                      }}</el-tag>
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
                      <span>{{ item.username }}</span>
                    </p>
                  </div>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { format } from 'date-fns'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import {
  ITEM_CATEGORIES,
  FOUND_ITEM_STATUS,
  getCategoryLabel,
  getFoundItemStatusLabel,
} from '@/constants/categories'
import { Plus, Search, Picture, Location, Calendar, User, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()
const loading = ref(false)
const error = ref<string | null>(null)

// 筛选条件
const filters = reactive({
  keyword: '',
  category: null as string | null,
  status: null as string | null,
})

// 计算筛选条件是否激活
const isFilterActive = computed(() => {
  return !!filters.keyword || !!filters.category || !!filters.status
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
})

// 计算属性
const isAuthenticated = computed(() => userStore.isAuthenticated)
const filteredItems = computed(() => {
  return foundItemsStore.filteredItems || []
})
const totalItems = computed(() => filteredItems.value.length)

// 物品类别 - 使用统一的类别系统
const categories = ITEM_CATEGORIES

// 状态选项
const statusOptions = FOUND_ITEM_STATUS

// URL参数同步
onMounted(() => {
  // 从URL读取筛选参数
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('keyword')) filters.keyword = urlParams.get('keyword') || ''
  if (urlParams.has('category')) filters.category = urlParams.get('category')
  if (urlParams.has('status')) filters.status = urlParams.get('status')
  if (urlParams.has('page')) pagination.page = parseInt(urlParams.get('page') || '1')

  fetchFoundItems()
})

// 监听筛选条件变化，更新URL
watch(
  [() => filters.keyword, () => filters.category, () => filters.status, () => pagination.page],
  () => {
    updateUrlParams()
  },
  { deep: true },
)

// 更新URL参数
const updateUrlParams = () => {
  const params = new URLSearchParams()

  if (filters.keyword) params.set('keyword', filters.keyword)
  if (filters.category) params.set('category', filters.category)
  if (filters.status) params.set('status', filters.status)
  if (pagination.page > 1) params.set('page', pagination.page.toString())

  const newUrl = window.location.pathname + (params.toString() ? `?${params.toString()}` : '')
  history.replaceState(null, '', newUrl)
}

// 重置所有筛选条件
const resetFilters = () => {
  filters.keyword = ''
  filters.category = null
  filters.status = null
  pagination.page = 1
  fetchFoundItems()
  ElMessage.success('已清除所有筛选条件')
}

// 清除特定筛选条件
const clearKeyword = () => {
  filters.keyword = ''
  handleSearch()
}

const clearCategory = () => {
  filters.category = null
  handleSearch()
}

const clearStatus = () => {
  filters.status = null
  handleSearch()
}

// 方法
const fetchFoundItems = async () => {
  loading.value = true
  error.value = null

  try {
    // 设置过滤条件
    foundItemsStore.setFilters({
      keyword: filters.keyword || null,
      category: filters.category || null,
      status: filters.status || null,
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

const getStatusText = (status: string) => {
  switch (status) {
    case 'pending':
      return '待认领'
    case 'processing':
      return '认领中'
    case 'claimed':
      return '已认领'
    default:
      return '未知'
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'pending':
      return 'primary'
    case 'processing':
      return 'warning'
    case 'claimed':
      return 'success'
    default:
      return 'info'
  }
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

/* 筛选区域样式 */
.actions-container {
  margin-bottom: 20px;
}

.filter-bar {
  width: 100%;
}

.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  min-height: 40px;
}

.filter-inputs {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 700px;
}

.search-input {
  flex: 2;
  min-width: 200px;
  max-width: 300px;
}

.filter-select {
  flex: 1;
  min-width: 120px;
  max-width: 140px;
}

/* 按钮区域样式 */
.button-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  margin-left: auto;
}

/* 使按钮与表单控件高度一致，并确保内容居中 */
.filter-btn,
.create-btn {
  padding: 0 15px !important;
  margin: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 40px !important;
}

/* 创建按钮的宽度限制 */
.create-btn {
  max-width: 140px !important;
}

/* 筛选标签区域 */
.active-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 15px;
  margin-bottom: 20px;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-label {
  margin-right: 10px;
  color: #606266;
  font-weight: bold;
  white-space: nowrap;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  margin: 0;
  font-size: 12px;
  height: 24px;
  line-height: 22px;
  padding: 0 8px;
}

.loading-container {
  margin: 40px 0;
}

.error-alert {
  margin: 20px 0;
}

/* 卡片样式 */
.items-grid {
  margin-bottom: 30px;
}

.el-row {
  margin: 0 -10px;
}

.el-col {
  padding: 0 10px;
  margin-bottom: 20px;
}

.item-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border-radius: 4px;
  overflow: hidden;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.item-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  font-size: 12px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
}

.item-content {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  margin: 0 0 12px;
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

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-category {
  margin: 0 0 10px;
}

.item-category .el-tag {
  display: inline-block;
  margin: 0;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
  font-size: 12px;
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-regular);
}

.item-location,
.item-time,
.item-poster {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

.item-location .el-icon,
.item-time .el-icon,
.item-poster .el-icon {
  margin-right: 5px;
  flex-shrink: 0;
  font-size: 16px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .filter-inputs {
    flex-direction: column;
    max-width: none;
  }

  .button-container {
    flex-direction: row;
    justify-content: center;
    margin-left: 0;
    width: 100%;
  }

  .filter-btn,
  .create-btn {
    flex: 1;
    max-width: none !important;
  }

  .item-image {
    height: 180px;
  }
}
</style>


