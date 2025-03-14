<template>
  <div class="announcements-manage">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 发布新公告
      </el-button>
    </div>

    <!-- 搜索过滤 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索标题或内容"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已下架" value="unpublished" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-select v-model="filterForm.isSticky" placeholder="是否置顶" clearable>
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <el-empty
        v-else-if="announcements.length === 0"
        description="暂无公告数据"
      >
        <el-button type="primary" @click="openCreateDialog">发布新公告</el-button>
      </el-empty>

      <el-table
        v-else
        :data="announcements"
        style="width: 100%"
        border
        stripe
        highlight-current-row
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="title" label="公告标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="adminName" label="发布人" width="120" />
        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isSticky" type="warning">置顶</el-tag>
            <el-tag v-else type="info">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
            >
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              link
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 'published' && !scope.row.isSticky"
              size="small"
              type="warning"
              @click="handleSetSticky(scope.row)"
              link
            >
              置顶
            </el-button>
            <el-button
              v-if="scope.row.status === 'published' && scope.row.isSticky"
              size="small"
              type="info"
              @click="handleCancelSticky(scope.row)"
              link
            >
              取消置顶
            </el-button>
            <el-button
              v-if="scope.row.status === 'published'"
              size="small"
              type="danger"
              @click="handleUnpublish(scope.row)"
              link
            >
              下架
            </el-button>
            <el-button
              v-if="scope.row.status === 'unpublished'"
              size="small"
              type="success"
              @click="handlePublish(scope.row)"
              link
            >
              发布
            </el-button>
            <el-popconfirm
              title="确定要删除这条公告吗？"
              @confirm="handleDelete(scope.row)"
            >
              <template #reference>
                <el-button
                  size="small"
                  type="danger"
                  link
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布新公告'"
      width="60%"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="announcementForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="10"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="是否置顶">
          <el-switch v-model="announcementForm.isSticky" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button
            type="primary"
            @click="handleSaveAnnouncement"
            :loading="submitting"
          >
            {{ isEdit ? '保存' : '发布' }}
          </el-button>
          <el-button
            v-if="!isEdit"
            @click="handleSaveDraft"
            :loading="submitting"
          >
            保存为草稿
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAnnouncementsStore } from '@/stores/announcements'
import { useUserStore } from '@/stores/user'
import { format } from 'date-fns'

const announcementsStore = useAnnouncementsStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref<FormInstance>()

// 过滤表单
const filterForm = reactive({
  keyword: '',
  status: '',
  isSticky: null as boolean | null
})

// 公告表单
const announcementForm = reactive({
  id: null as number | null,
  title: '',
  content: '',
  isSticky: false,
  status: 'published'
})

// 表单验证规则
const formRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度应在10-5000个字符之间', trigger: 'blur' }
  ]
})

// 计算属性
const announcements = computed(() => announcementsStore.announcements)

// 格式化日期
const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString)
    return format(date, 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return dateString
  }
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'published':
      return '已发布'
    case 'draft':
      return '草稿'
    case 'unpublished':
      return '已下架'
    default:
      return '未知'
  }
}

// 获取状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'published':
      return 'success'
    case 'draft':
      return 'info'
    case 'unpublished':
      return 'danger'
    default:
      return 'info'
  }
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.status = ''
  filterForm.isSticky = null
  handleSearch()
}

// 搜索公告
const handleSearch = async () => {
  currentPage.value = 1
  fetchAnnouncements()
}

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    await announcementsStore.fetchAnnouncements({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: filterForm.keyword || undefined,
      status: filterForm.status || undefined,
      isSticky: filterForm.isSticky
    })
    total.value = announcementsStore.total
  } catch (error) {
    console.error('Failed to fetch announcements:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchAnnouncements()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchAnnouncements()
}

// 打开创建对话框
const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row: any) => {
  isEdit.value = true
  announcementForm.id = row.id
  announcementForm.title = row.title
  announcementForm.content = row.content
  announcementForm.isSticky = row.isSticky
  announcementForm.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.isSticky = false
  announcementForm.status = 'published'
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 关闭对话框
const handleCloseDialog = () => {
  if (submitting.value) return

  if (announcementForm.title || announcementForm.content) {
    ElMessageBox.confirm(
      '确定要关闭吗？未保存的内容将会丢失',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(() => {
        dialogVisible.value = false
        resetForm()
      })
      .catch(() => {})
  } else {
    dialogVisible.value = false
  }
}

// 保存公告
const handleSaveAnnouncement = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitting.value = true
        try {
          const result = isEdit.value
            ? await announcementsStore.updateAnnouncement({
                id: announcementForm.id as number,
                title: announcementForm.title,
                content: announcementForm.content,
                isSticky: announcementForm.isSticky,
                status: 'published'
              })
            : await announcementsStore.createAnnouncement({
                title: announcementForm.title,
                content: announcementForm.content,
                isSticky: announcementForm.isSticky,
                status: 'published'
              })

          if (result.success) {
            ElMessage.success(isEdit.value ? '公告更新成功' : '公告发布成功')
            dialogVisible.value = false
            fetchAnnouncements()
          } else {
            ElMessage.error(result.message || '操作失败')
          }
        } catch (error) {
          console.error('Save announcement error:', error)
          ElMessage.error('操作失败，请稍后再试')
        } finally {
          submitting.value = false
        }
      }
    })
  }
}

// 保存为草稿
const handleSaveDraft = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitting.value = true
        try {
          const result = await announcementsStore.createAnnouncement({
            title: announcementForm.title,
            content: announcementForm.content,
            isSticky: false,
            status: 'draft'
          })

          if (result.success) {
            ElMessage.success('草稿保存成功')
            dialogVisible.value = false
            fetchAnnouncements()
          } else {
            ElMessage.error(result.message || '保存失败')
          }
        } catch (error) {
          console.error('Save draft error:', error)
          ElMessage.error('保存失败，请稍后再试')
        } finally {
          submitting.value = false
        }
      }
    })
  }
}

// 处理设置置顶
const handleSetSticky = async (row: any) => {
  try {
    const result = await announcementsStore.updateAnnouncement({
      id: row.id,
      isSticky: true
    })

    if (result.success) {
      ElMessage.success('公告已置顶')
      fetchAnnouncements()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Set sticky error:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 处理取消置顶
const handleCancelSticky = async (row: any) => {
  try {
    const result = await announcementsStore.updateAnnouncement({
      id: row.id,
      isSticky: false
    })

    if (result.success) {
      ElMessage.success('已取消置顶')
      fetchAnnouncements()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Cancel sticky error:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 处理下架公告
const handleUnpublish = async (row: any) => {
  try {
    const result = await announcementsStore.updateAnnouncement({
      id: row.id,
      status: 'unpublished',
      isSticky: false
    })

    if (result.success) {
      ElMessage.success('公告已下架')
      fetchAnnouncements()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Unpublish error:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 处理发布公告
const handlePublish = async (row: any) => {
  try {
    const result = await announcementsStore.updateAnnouncement({
      id: row.id,
      status: 'published'
    })

    if (result.success) {
      ElMessage.success('公告已发布')
      fetchAnnouncements()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Publish error:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 处理删除公告
const handleDelete = async (row: any) => {
  try {
    const result = await announcementsStore.deleteAnnouncement(row.id)

    if (result.success) {
      ElMessage.success('公告已删除')
      fetchAnnouncements()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('Delete announcement error:', error)
    ElMessage.error('删除失败，请稍后再试')
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcements-manage {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
  }

  .filter-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }
}
</style>
