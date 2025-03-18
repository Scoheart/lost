<template>
  <main-layout>
    <div class="lost-items-view">
      <div class="page-header">
        <h1>寻物启事</h1>
        <p>查看社区内的寻物信息，或发布您丢失的物品</p>
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
            {{ isAuthenticated ? '发布寻物启事' : '登录后发布' }}
          </el-button>
        </div>
      </div>

      <ItemsList
        :items="filteredLostItems"
        item-type="lost"
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
import { useLostItemsStore } from '@/stores/lostItems'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/components/layout/MainLayout.vue'
import ItemsList from '@/components/common/ItemsList.vue'
import { ITEM_CATEGORIES } from '@/constants/categories'
import { formatDate } from '@/utils/dateHelpers'

const router = useRouter()
const lostItemsStore = useLostItemsStore()
const userStore = useUserStore()

// State
const loading = ref(false)
const error = ref<string | null>(null)

// Computed
const isAuthenticated = computed(() => userStore.isAuthenticated)
const filteredLostItems = computed(() => {
  // Ensure dates are properly formatted for display
  return lostItemsStore.filteredItems.map(item => ({
    ...item,
    // If lostDate is null, use createdAt as fallback
    lostDate: item.lostDate || item.createdAt
  }))
})
const pagination = computed(() => lostItemsStore.pagination)

// Filters
const filters = reactive({
  keyword: '',
  category: null as string | null,
})

// Methods
const handleSearch = async () => {
  loading.value = true

  try {
    await lostItemsStore.fetchLostItems({
      page: 1,
      pageSize: pagination.value.pageSize,
      query: filters.keyword,
      category: filters.category || undefined
    })
  } catch (error) {
    console.error('Failed to search lost items:', error)
    ElMessage.error('搜索失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (page: number) => {
  loading.value = true

  try {
    await lostItemsStore.fetchLostItems({
      page,
      pageSize: pagination.value.pageSize,
      query: filters.keyword,
      category: filters.category || undefined
    })
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
    ElMessage.error('获取寻物启事列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const viewItemDetails = (id: number) => {
  router.push(`/lost-items/${id}`)
}

const showAddItemDialog = () => {
  router.push('/lost-items/create')
}

// Initialize
onMounted(async () => {
  loading.value = true

  try {
    await lostItemsStore.fetchLostItems({
      page: 1,
      pageSize: pagination.value.pageSize
    })
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
    ElMessage.error('获取寻物启事列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.lost-items-view {
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
