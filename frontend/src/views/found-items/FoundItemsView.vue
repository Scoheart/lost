<template>
  <main-layout>
    <div class="found-items-view">
      <div class="page-header">
        <h1>失物招领</h1>
        <p>查看已被拾到的物品信息，或发布您拾获的物品</p>
      </div>

      <div class="filters-container">
        <div class="search-filters">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索物品名称或描述"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />

          <el-select
            v-model="filters.category"
            placeholder="物品类别"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="category in ITEM_CATEGORIES"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>

          <el-button
            type="primary"
            @click="isAuthenticated ? showAddItemDialog() : router.push('/login')"
          >
            <el-icon><Plus /></el-icon>
            {{ isAuthenticated ? '发布招领启事' : '登录后发布' }}
          </el-button>
        </div>
      </div>

      <ItemsList
        :items="filteredFoundItems"
        item-type="found"
        :loading="loading"
        :is-authenticated="isAuthenticated"
        :total-count="pagination.total"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :categories="ITEM_CATEGORIES"
        @page-change="handlePageChange"
        @view-details="viewItemDetails"
        @add="showAddItemDialog"
      />
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/components/layout/MainLayout.vue'
import ItemsList from '@/components/common/ItemsList.vue'
import { ITEM_CATEGORIES } from '@/constants/categories'
import { formatDate } from '@/utils/dateHelpers'

const router = useRouter()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()

// State
const loading = ref(false)
const error = ref<string | null>(null)

// Computed
const isAuthenticated = computed(() => userStore.isAuthenticated)
const filteredFoundItems = computed(() => {
  // Ensure dates are properly formatted for display and filter out claimed items
  return foundItemsStore.filteredItems
    .filter(item => item.status !== 'claimed')
    .map(item => ({
      ...item,
      // If foundDate is null, use createdAt as fallback
      foundDate: item.foundDate || item.createdAt
    }))
})
const pagination = computed(() => foundItemsStore.pagination)

// Filters
const filters = reactive({
  keyword: '',
  category: null as string | null,
})

// Methods
const handleSearch = async () => {
  loading.value = true

  console.log('Searching found items with:', {
    keyword: filters.keyword,
    category: filters.category
  })

  try {
    await foundItemsStore.fetchFoundItems({
      page: 1,
      pageSize: pagination.value.pageSize,
      query: filters.keyword,
      category: filters.category || undefined
    })
  } catch (error) {
    console.error('Failed to search found items:', error)
    ElMessage.error('搜索失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (page: number) => {
  loading.value = true

  try {
    await foundItemsStore.fetchFoundItems({
      page,
      pageSize: pagination.value.pageSize,
      query: filters.keyword,
      category: filters.category || undefined
    })
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取失物招领列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const viewItemDetails = (id: number) => {
  router.push(`/found-items/${id}`)
}

const showAddItemDialog = () => {
  router.push('/found-items/create')
}

// Initialize
onMounted(async () => {
  loading.value = true

  try {
    await foundItemsStore.fetchFoundItems({
      page: 1,
      pageSize: pagination.value.pageSize
    })
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取失物招领列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.found-items-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 15px;
  color: #606266;
}

.filters-container {
  margin-bottom: 30px;
}

.search-filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.search-filters .el-input {
  max-width: 250px;
}

.search-filters .el-select {
  width: 150px;
}

@media (max-width: 768px) {
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .search-filters .el-input,
  .search-filters .el-select {
    max-width: none;
    margin-bottom: 10px;
  }
}
</style>


