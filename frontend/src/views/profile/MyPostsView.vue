<template>
  <div class="my-posts-container">
    <h2 class="page-title">我的论坛帖子</h2>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-state">
      <el-alert type="error" :closable="false" show-icon :title="error" />
    </div>

    <div v-else-if="userPosts.length === 0" class="empty-state">
      <el-empty description="您还没有发布过帖子">
        <template #extra>
          <el-button type="primary" @click="$router.push('/forum/create')">
            发布新帖子
          </el-button>
        </template>
      </el-empty>
    </div>

    <template v-else>
      <el-table
        :data="userPosts"
        style="width: 100%"
        row-key="id"
        border
        @row-click="handleRowClick"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click.stop="viewPost(scope.row.id)">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="评论数" width="100" align="center">
          <template #default="scope">
            {{ scope.row.commentCount }}
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button
                type="primary"
                size="small"
                plain
                @click.stop="editPost(scope.row.id)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                plain
                @click.stop="confirmDelete(scope.row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="totalPages > 1">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalElements"
          :page-size="pageSize"
          :current-page="currentPage + 1"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePostsStore } from '@/stores/posts'
import { formatDate } from '@/utils/dateHelpers'

const router = useRouter()
const postsStore = usePostsStore()

// 状态
const loading = computed(() => postsStore.loading)
const error = computed(() => postsStore.error)
const userPosts = computed(() => postsStore.userPosts)
const totalElements = computed(() => postsStore.totalElements)
const totalPages = computed(() => postsStore.totalPages)
const currentPage = computed(() => postsStore.currentPage)
const pageSize = computed(() => postsStore.pageSize)

// 生命周期钩子
onMounted(async () => {
  try {
    await postsStore.fetchMyPosts()
  } catch (error) {
    console.error('获取我的帖子失败:', error)
  }
})

// 处理分页变化
async function handlePageChange(page: number) {
  try {
    await postsStore.fetchMyPosts(page - 1)
    window.scrollTo(0, 0)
  } catch (error) {
    console.error('获取帖子失败:', error)
  }
}

// 行点击事件
function handleRowClick(row: any) {
  viewPost(row.id)
}

// 查看帖子
function viewPost(id: number) {
  router.push(`/forum/${id}`)
}

// 编辑帖子
function editPost(id: number) {
  router.push(`/forum/edit/${id}`)
}

// 确认删除帖子
function confirmDelete(post: any) {
  ElMessageBox.confirm(
    `确定要删除帖子 "${post.title}" 吗？删除后将无法恢复。`,
    '删除帖子',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await postsStore.deletePost(post.id)
      ElMessage.success('帖子已删除')
      // 重新加载数据
      await postsStore.fetchMyPosts(currentPage.value)
    } catch (error) {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除帖子失败，请稍后再试')
    }
  }).catch(() => {
    // 用户取消删除
  })
}
</script>

<style scoped>
.my-posts-container {
  padding: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
}

.loading-container, .error-state, .empty-state {
  margin: 20px 0;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
