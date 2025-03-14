<template>
  <main-layout>
    <div class="found-items-container">
      <div class="header-section">
        <h1 class="page-title">失物招领</h1>
        <el-button type="primary" @click="handleCreateItem">
          <el-icon><Plus /></el-icon>
          发布失物招领
        </el-button>
      </div>

      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="物品类型">
            <el-select v-model="filterForm.category" placeholder="全部类型" clearable>
              <el-option label="全部类型" value="" />
              <el-option
                v-for="category in categories"
                :key="category.value"
                :label="category.label"
                :value="category.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="拾获时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
            />
          </el-form-item>

          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
              <el-option label="全部状态" value="" />
              <el-option label="待认领" value="unclaimed" />
              <el-option label="认领中" value="processing" />
              <el-option label="已认领" value="claimed" />
              <el-option label="已关闭" value="closed" />
            </el-select>
          </el-form-item>

          <el-form-item label="关键词">
            <el-input
              v-model="filterForm.keyword"
              placeholder="搜索物品名称、描述等"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="view-options">
        <span class="view-label">展示方式：</span>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>

        <div class="sort-options">
          <span class="sort-label">排序方式：</span>
          <el-select v-model="sortBy" size="small" @change="handleSearch">
            <el-option label="最新发布" value="createdAt" />
            <el-option label="拾获时间" value="foundDate" />
          </el-select>
        </div>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="6" animated />
      </div>

      <el-empty
        v-else-if="foundItems.length === 0"
        description="暂无符合条件的失物招领"
        :image-size="200"
      >
        <el-button type="primary" @click="handleCreateItem">发布失物招领</el-button>
      </el-empty>

      <!-- 网格视图 -->
      <div v-else-if="viewMode === 'grid'" class="items-grid">
        <el-card
          v-for="item in foundItems"
          :key="item.id"
          class="item-card"
          @click="viewItemDetail(item.id)"
          shadow="hover"
        >
          <div class="item-image">
            <el-image
              v-if="item.images && item.images.length > 0"
              :src="item.images[0]"
              fit="cover"
              :preview-src-list="item.images"
              preview-teleported
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
              v-if="item.status"
              :type="getStatusType(item.status)"
              class="item-status-tag"
            >
              {{ getStatusLabel(item.status) }}
            </el-tag>
          </div>

          <div class="item-info">
            <h3 class="item-title">{{ item.title }}</h3>

            <div class="item-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ item.foundLocation || '未知地点' }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.foundDate) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Box /></el-icon>
                <span>{{ getCategoryLabel(item.category) }}</span>
              </div>
            </div>

            <div class="item-footer">
              <div class="user-info">
                <el-avatar :size="24" :src="item.user?.avatar">
                  {{ item.user?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ item.user?.username }}</span>
              </div>
              <span class="post-time">{{ formatDate(item.createdAt, 'MM-dd') }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 列表视图 -->
      <div v-else class="items-list">
        <el-table
          :data="foundItems"
          style="width: 100%"
          @row-click="(row) => viewItemDetail(row.id)"
        >
          <el-table-column label="物品图片" width="120">
            <template #default="scope">
              <el-image
                v-if="scope.row.images && scope.row.images.length > 0"
                :src="scope.row.images[0]"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px"
                :preview-src-list="scope.row.images"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-placeholder" style="width: 80px; height: 80px">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="title" label="物品名称" min-width="180" show-overflow-tooltip />

          <el-table-column prop="category" label="物品类型" width="120">
            <template #default="scope">
              {{ getCategoryLabel(scope.row.category) }}
            </template>
          </el-table-column>

          <el-table-column prop="foundLocation" label="拾获地点" min-width="120" show-overflow-tooltip />

          <el-table-column label="拾获时间" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.foundDate) }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="保管地点" width="150" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.storageLocation || '未提供' }}
            </template>
          </el-table-column>

          <el-table-column label="发布者" width="120">
            <template #default="scope">
              <div class="user-info">
                <el-avatar :size="24" :src="scope.row.user?.avatar">
                  {{ scope.row.user?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ scope.row.user?.username }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container" v-if="totalItems > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalItems"
          layout="total, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { format } from 'date-fns'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Search,
  Grid,
  List,
  Picture,
  Location,
  Calendar,
  Box
} from '@element-plus/icons-vue'

const router = useRouter()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const foundItems = ref([])
const viewMode = ref('grid')
const sortBy = ref('createdAt')
const currentPage = ref(1)
const pageSize = ref(12)
const totalItems = ref(0)

// 过滤条件
const filterForm = reactive({
  keyword: '',
  category: '',
  status: '',
  dateRange: [],
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 物品类型
const categories = [
  { label: '证件', value: 'documents' },
  { label: '电子设备', value: 'electronics' },
  { label: '现金/钱包', value: 'money' },
  { label: '生活用品', value: 'daily' },
  { label: '交通工具', value: 'vehicle' },
  { label: '首饰/配饰', value: 'jewelry' },
  { label: '书籍/文具', value: 'books' },
  { label: '钥匙', value: 'keys' },
  { label: '其他', value: 'other' }
]

// 方法
const fetchFoundItems = async () => {
  loading.value = true
  try {
    const filters = {
      page: currentPage.value,
      pageSize: pageSize.value,
      sort: sortBy.value,
      keyword: filterForm.keyword,
      category: filterForm.category,
      status: filterForm.status,
      startDate: filterForm.dateRange?.[0] || null,
      endDate: filterForm.dateRange?.[1] || null
    }

    const response = await foundItemsStore.fetchFoundItems(filters)

    foundItems.value = response.items || []
    totalItems.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取失物招领列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchFoundItems()
}

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.category = ''
  filterForm.status = ''
  filterForm.dateRange = []
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchFoundItems()
}

const viewItemDetail = (id) => {
  router.push(`/found-items/${id}`)
}

const handleCreateItem = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再发布失物招领')
    router.push(`/login?redirect=/found-items/create`)
    return
  }

  router.push('/found-items/create')
}

const formatDate = (dateString, formatStr = 'yyyy-MM-dd') => {
  if (!dateString) return '未知日期'

  try {
    return format(new Date(dateString), formatStr)
  } catch (error) {
    return dateString
  }
}

const getStatusLabel = (status) => {
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

const getStatusType = (status) => {
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

const getCategoryLabel = (categoryValue) => {
  const category = categories.find(c => c.value === categoryValue)
  return category ? category.label : categoryValue || '其他'
}

// 生命周期
onMounted(() => {
  fetchFoundItems()
})
</script>

<style scoped>
.found-items-container {
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

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.filter-section {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.view-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-label,
.sort-label {
  margin-right: 10px;
  color: #606266;
}

.sort-options {
  display: flex;
  align-items: center;
}

.loading-section {
  padding: 20px 0;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-image {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.item-image .el-image {
  height: 100%;
  width: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.item-status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.item-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  margin-bottom: 15px;
  flex: 1;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.meta-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  margin-left: 8px;
  color: #606266;
}

.post-time {
  color: #909399;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .found-items-container {
    padding: 10px;
  }

  .filter-form {
    flex-direction: column;
  }

  .items-grid {
    grid-template-columns: 1fr;
  }

  .item-image {
    height: 160px;
  }
}
</style>
