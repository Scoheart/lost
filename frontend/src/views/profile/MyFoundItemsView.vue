<template>
  <div class="my-found-items-container">
    <h2 class="page-title">我的招领</h2>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <el-empty
      v-else-if="foundItems.length === 0"
      description="您还没有发布过失物招领"
    >
      <el-button type="primary" @click="$router.push('/found-items/create')">
        发布失物招领
      </el-button>
    </el-empty>

    <div v-else class="items-list">
      <el-table
        :data="foundItems"
        style="width: 100%"
        @row-click="(row) => viewFoundItemDetail(row.id)"
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

        <el-table-column prop="foundLocation" label="拾获地点" min-width="120" show-overflow-tooltip />

        <el-table-column label="拾获时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.foundDate) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getFoundStatusType(scope.row.status)">
              {{ getFoundStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click.stop="viewFoundItemDetail(scope.row.id)"
            >
              查看
            </el-button>
            <el-button
              link
              type="warning"
              @click.stop="editFoundItem(scope.row.id)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click.stop="deleteFoundItem(scope.row.id, scope.row.title)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { useFoundItemsStore } from '@/stores/foundItems'
import { getFoundStatusLabel, getFoundStatusType } from '@/utils/statusHelpers'
import type { FoundItem } from '@/stores/foundItems'

const router = useRouter()
const foundItemsStore = useFoundItemsStore()

// 分页和加载状态
const pageSize = ref(10)
const page = ref(1)
const loading = ref(false)
const total = ref(0)

// 数据
const foundItems = ref<FoundItem[]>([])

// 方法
const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

// 加载失物招领数据
const fetchFoundItems = async () => {
  loading.value = true
  try {
    // 获取用户的失物招领列表
    const response = await foundItemsStore.fetchMyFoundItems({
      page: page.value,
      pageSize: pageSize.value
    })

    foundItems.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取失物招领列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchFoundItems()
}

const viewFoundItemDetail = (id: number) => {
  router.push(`/found-items/${id}`)
}

const editFoundItem = (id: number) => {
  router.push(`/found-items/edit/${id}`)
}

const deleteFoundItem = (id: number, title: string) => {
  ElMessageBox.confirm(
    `确定要删除失物招领 "${title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const result = await foundItemsStore.deleteFoundItem(id)
        if (result.success) {
          ElMessage.success('删除成功')
          fetchFoundItems()
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete found item:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消操作，不执行任何操作
    })
}

// 初始化
onMounted(() => {
  fetchFoundItems()
})
</script>

<style scoped>
.my-found-items-container {
  padding: 0 10px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
}

.loading-container {
  padding: 20px 0;
}

.items-list {
  margin-top: 20px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
