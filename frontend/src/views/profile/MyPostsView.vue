<template>
  <div class="my-posts-container">
    <h2 class="page-title">我的发布</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 寻物启事 -->
      <el-tab-pane label="寻物启事" name="lost-items">
        <div v-if="loadingLostItems" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <el-empty
          v-else-if="lostItems.length === 0"
          description="您还没有发布过寻物启事"
        >
          <el-button type="primary" @click="$router.push('/lost-items/create')">
            发布寻物启事
          </el-button>
        </el-empty>

        <div v-else class="items-list">
          <el-table
            :data="lostItems"
            style="width: 100%"
            @row-click="(row) => viewLostItemDetail(row.id)"
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

            <el-table-column prop="lostLocation" label="丢失地点" min-width="120" show-overflow-tooltip />

            <el-table-column label="丢失时间" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.lostDate) }}
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getLostStatusType(scope.row.status)">
                  {{ getLostStatusLabel(scope.row.status) }}
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
                  @click.stop="viewLostItemDetail(scope.row.id)"
                >
                  查看
                </el-button>
                <el-button
                  link
                  type="warning"
                  @click.stop="editLostItem(scope.row.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click.stop="deleteLostItem(scope.row.id, scope.row.title)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="lostItemsPage"
              :page-size="pageSize"
              :total="totalLostItems"
              layout="total, prev, pager, next, jumper"
              @current-change="handleLostItemsPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 失物招领 -->
      <el-tab-pane label="失物招领" name="found-items">
        <div v-if="loadingFoundItems" class="loading-container">
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
              v-model:current-page="foundItemsPage"
              :page-size="pageSize"
              :total="totalFoundItems"
              layout="total, prev, pager, next, jumper"
              @current-change="handleFoundItemsPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 论坛帖子 -->
      <el-tab-pane label="论坛帖子" name="forum-posts">
        <div v-if="loadingForumPosts" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <el-empty
          v-else-if="forumPosts.length === 0"
          description="您还没有发布过论坛帖子"
        >
          <el-button type="primary" @click="$router.push('/forum/create')">
            发布帖子
          </el-button>
        </el-empty>

        <div v-else class="items-list">
          <el-table
            :data="forumPosts"
            style="width: 100%"
            @row-click="(row) => viewForumPost(row.id)"
          >
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />

            <el-table-column label="发布时间" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>

            <el-table-column prop="viewCount" label="查看" width="80" align="center" />

            <el-table-column prop="commentCount" label="回复" width="80" align="center" />

            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getPostStatusType(scope.row.status)">
                  {{ getPostStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  @click.stop="viewForumPost(scope.row.id)"
                >
                  查看
                </el-button>
                <el-button
                  link
                  type="warning"
                  @click.stop="editForumPost(scope.row.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click.stop="deleteForumPost(scope.row.id, scope.row.title)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="forumPostsPage"
              :page-size="pageSize"
              :total="totalForumPosts"
              layout="total, prev, pager, next, jumper"
              @current-change="handleForumPostsPageChange"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { useLostItemsStore } from '@/stores/lostItems'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useForumStore } from '@/stores/forum'

const router = useRouter()
const lostItemsStore = useLostItemsStore()
const foundItemsStore = useFoundItemsStore()
const forumStore = useForumStore()

// 活动标签页
const activeTab = ref('lost-items')

// 分页和加载状态
const pageSize = ref(10)
const lostItemsPage = ref(1)
const foundItemsPage = ref(1)
const forumPostsPage = ref(1)
const loadingLostItems = ref(false)
const loadingFoundItems = ref(false)
const loadingForumPosts = ref(false)

// 数据
const lostItems = ref([])
const foundItems = ref([])
const forumPosts = ref([])
const totalLostItems = ref(0)
const totalFoundItems = ref(0)
const totalForumPosts = ref(0)

// 方法
const handleTabChange = (tab) => {
  if (tab === 'lost-items' && lostItems.value.length === 0) {
    fetchLostItems()
  } else if (tab === 'found-items' && foundItems.value.length === 0) {
    fetchFoundItems()
  } else if (tab === 'forum-posts' && forumPosts.value.length === 0) {
    fetchForumPosts()
  }
}

const formatDate = (dateString) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString
  }
}

// 寻物启事相关方法
const fetchLostItems = async () => {
  loadingLostItems.value = true
  try {
    const response = await lostItemsStore.fetchUserLostItems({
      page: lostItemsPage.value,
      pageSize: pageSize.value
    })

    lostItems.value = response.items || []
    totalLostItems.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
    ElMessage.error('获取寻物启事列表失败')
  } finally {
    loadingLostItems.value = false
  }
}

const handleLostItemsPageChange = (page) => {
  lostItemsPage.value = page
  fetchLostItems()
}

const viewLostItemDetail = (id) => {
  router.push(`/lost-items/${id}`)
}

const editLostItem = (id) => {
  router.push(`/lost-items/edit/${id}`)
}

const deleteLostItem = (id, title) => {
  ElMessageBox.confirm(
    `确定要删除寻物启事 "${title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const result = await lostItemsStore.deleteLostItem(id)
        if (result.success) {
          ElMessage.success('删除成功')
          fetchLostItems()
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete lost item:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消操作，不执行任何操作
    })
}

const getLostStatusLabel = (status) => {
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

const getLostStatusType = (status) => {
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

// 失物招领相关方法
const fetchFoundItems = async () => {
  loadingFoundItems.value = true
  try {
    const response = await foundItemsStore.fetchUserFoundItems({
      page: foundItemsPage.value,
      pageSize: pageSize.value
    })

    foundItems.value = response.items || []
    totalFoundItems.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取失物招领列表失败')
  } finally {
    loadingFoundItems.value = false
  }
}

const handleFoundItemsPageChange = (page) => {
  foundItemsPage.value = page
  fetchFoundItems()
}

const viewFoundItemDetail = (id) => {
  router.push(`/found-items/${id}`)
}

const editFoundItem = (id) => {
  router.push(`/found-items/edit/${id}`)
}

const deleteFoundItem = (id, title) => {
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

const getFoundStatusLabel = (status) => {
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

const getFoundStatusType = (status) => {
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

// 论坛帖子相关方法
const fetchForumPosts = async () => {
  loadingForumPosts.value = true
  try {
    const response = await forumStore.fetchUserPosts({
      page: forumPostsPage.value,
      pageSize: pageSize.value
    })

    forumPosts.value = response.items || []
    totalForumPosts.value = response.total || 0
  } catch (error) {
    console.error('Failed to fetch forum posts:', error)
    ElMessage.error('获取论坛帖子列表失败')
  } finally {
    loadingForumPosts.value = false
  }
}

const handleForumPostsPageChange = (page) => {
  forumPostsPage.value = page
  fetchForumPosts()
}

const viewForumPost = (id) => {
  router.push(`/forum/${id}`)
}

const editForumPost = (id) => {
  router.push(`/forum/edit/${id}`)
}

const deleteForumPost = (id, title) => {
  ElMessageBox.confirm(
    `确定要删除帖子 "${title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const result = await forumStore.deletePost(id)
        if (result.success) {
          ElMessage.success('删除成功')
          fetchForumPosts()
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete forum post:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消操作，不执行任何操作
    })
}

const getPostStatusLabel = (status) => {
  switch (status) {
    case 'published':
      return '已发布'
    case 'draft':
      return '草稿'
    case 'hidden':
      return '已隐藏'
    default:
      return '未知'
  }
}

const getPostStatusType = (status) => {
  switch (status) {
    case 'published':
      return 'success'
    case 'draft':
      return 'info'
    case 'hidden':
      return 'danger'
    default:
      return 'info'
  }
}

// 初始化
onMounted(() => {
  fetchLostItems()
})
</script>

<style scoped>
.my-posts-container {
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
