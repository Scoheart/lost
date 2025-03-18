<template>
  <div class="items-list-container">
    <!-- Loading skeleton -->
    <el-row :gutter="20" class="items-list" v-if="loading">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="i in 8" :key="i">
        <el-skeleton animated :loading="loading">
          <template #template>
            <div class="item-skeleton">
              <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
              <div style="padding: 14px">
                <el-skeleton-item variant="h3" style="width: 50%" />
                <div style="display: flex; align-items: center; margin-top: 8px">
                  <el-skeleton-item variant="text" style="margin-right: 16px" />
                  <el-skeleton-item variant="text" style="width: 30%" />
                </div>
                <el-skeleton-item variant="text" style="width: 80%; margin-top: 8px" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-col>
    </el-row>

    <!-- Empty state -->
    <el-row v-else-if="items.length === 0">
      <el-col :span="24">
        <el-empty :description="emptyText" :image-size="200">
          <template #description>
            <p>{{ emptyText }}</p>
          </template>
          <el-button type="primary" @click="$emit('add')" v-if="isAuthenticated">{{
            addButtonText
          }}</el-button>
        </el-empty>
      </el-col>
    </el-row>

    <!-- Items list -->
    <el-row :gutter="20" class="items-list" v-else>
      <el-col v-for="item in items" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="item-card" @click="viewItemDetails(item.id)">
          <div class="item-image">
            <el-image
              :src="item.images?.[0] || '/placeholder-image.png'"
              fit="cover"
              :preview-src-list="item.images || []"
              :initial-index="0"
              :alt="itemType === 'lost' ? '丢失物品图片' : '拾获物品图片'"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>
            <ItemStatusTag :status="item.status" :item-type="itemType" effect="light" />
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-description">{{ item.description }}</p>
            <div class="item-meta">
              <div class="meta-item">
                <el-icon><Discount /></el-icon>
                <span>{{ item[locationField] }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>
                  {{ item[dateField] ? formatDate(item[dateField]) : formatDate(item.createdAt) }}
                </span>
              </div>
              <div class="meta-item">
                <el-icon><UserFilled /></el-icon>
                <span>{{ getContactName(item) }}</span>
              </div>
            </div>
            <div class="item-footer">
              <el-tag size="small">{{ getCategoryLabel(item.category) }}</el-tag>
              <span class="reward" v-if="itemType === 'lost' && item.reward">
                悬赏: {{ item.reward }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Pagination -->
    <div class="pagination-container" v-if="items.length > 0 && showPagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="totalCount"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, Calendar, UserFilled, Discount } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateHelpers'
import ItemStatusTag from './ItemStatusTag.vue'
import type { LostItem } from '@/stores/lostItems'
import type { FoundItem } from '@/stores/foundItems'

// Define prop types
interface Props {
  items: LostItem[] | FoundItem[] | any[] // Accept both item types or any array
  itemType: 'lost' | 'found'
  loading: boolean
  isAuthenticated: boolean
  totalCount: number
  currentPage: number
  pageSize: number
  categories: { value: string; label: string }[]
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  itemType: 'lost',
  loading: false,
  isAuthenticated: false,
  totalCount: 0,
  currentPage: 1,
  pageSize: 10,
  categories: () => [],
})

const emit = defineEmits(['page-change', 'view-details', 'add'])

const router = useRouter()

// Compute field names based on item type
const dateField = computed(() => (props.itemType === 'lost' ? 'lostDate' : 'foundDate'))
const locationField = computed(() => (props.itemType === 'lost' ? 'lostLocation' : 'foundLocation'))

// Get contact name from item
const getContactName = (item: any) => {
  if (!item) return ''

  // If there's a specific contactName field, use that
  if (item.contactName) return item.contactName

  // Otherwise fall back to username
  return item.username || ''
}

// Get category label from value
const getCategoryLabel = (categoryValue: string) => {
  const category = props.categories.find((cat: any) => cat.value === categoryValue)
  return category?.label || '其他物品'
}

// Handle pagination change
const handlePageChange = (page: number) => {
  emit('page-change', page)
}

// Handle view item details
const viewItemDetails = (id: number) => {
  emit('view-details', id)
}

// Handle add item
const handleAddItem = () => {
  emit('add')
}

// Computed values
const emptyText = computed(() => {
  return props.itemType === 'lost' ? '暂无符合条件的寻物信息' : '暂无符合条件的招领信息'
})

const addButtonText = computed(() => {
  return props.itemType === 'lost' ? '发布寻物启事' : '发布招领启事'
})

const showPagination = computed(() => props.totalCount > props.pageSize)
</script>

<style scoped>
.items-list-container {
  width: 100%;
}

.items-list {
  margin-bottom: 24px;
}

.item-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
  cursor: pointer;
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

.item-image .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.item-content {
  padding: 16px;
}

.item-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-description {
  margin: 0 0 12px;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-meta {
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}

.meta-item .el-icon {
  margin-right: 6px;
  font-size: 14px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reward {
  font-size: 14px;
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.item-skeleton {
  border-radius: 4px;
  overflow: hidden;
  height: 100%;
}
</style>
