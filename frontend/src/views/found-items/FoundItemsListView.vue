<template>
  <div class="found-items-container">
    <div class="page-header">
      <h1>拾物公告</h1>
      <p>查看社区内拾得物品的信息，或发布您捡到的物品</p>
    </div>

    <div class="actions-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="16">
          <div class="search-container">
            <el-input
              v-model="searchQuery"
              placeholder="搜索物品名称、描述或地点"
              clearable
              :prefix-icon="Search"
              @input="handleSearch"
            />
            <el-select
              v-model="categoryFilter"
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
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
            />
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="add-btn-container">
            <el-button
              type="primary"
              icon="Plus"
              @click="handleAddFoundItem"
              v-if="isLoggedIn"
            >
              发布拾物公告
            </el-button>
            <el-button
              type="primary"
              plain
              @click="$router.push('/login')"
              v-else
            >
              登录后发布
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="found-items-list">
      <template v-if="loading">
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
      </template>

      <template v-else-if="filteredFoundItems.length === 0">
        <el-col :span="24">
          <el-empty
            description="暂无拾物信息"
            :image-size="200"
          >
            <template #description>
              <p>暂无符合条件的拾物信息</p>
            </template>
            <el-button type="primary" @click="handleAddFoundItem" v-if="isLoggedIn">发布拾物公告</el-button>
          </el-empty>
        </el-col>
      </template>

      <el-col
        v-for="item in filteredFoundItems"
        :key="item.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card shadow="hover" class="found-item-card" @click="viewItemDetails(item.id)">
          <div class="item-image">
            <el-image
              :src="item.images?.[0] || '/placeholder-image.png'"
              fit="cover"
              :preview-src-list="item.images || []"
              :initial-index="0"
              alt="拾得物品图片"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>
            <div class="item-status" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </div>
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-description">{{ item.description }}</p>
            <div class="item-meta">
              <div class="meta-item">
                <el-icon><Map /></el-icon>
                <span>{{ item.foundLocation }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.foundTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><UserFilled /></el-icon>
                <span>{{ item.contact.name }}</span>
              </div>
            </div>
            <div class="item-footer">
              <el-tag size="small">{{ getCategoryLabel(item.category) }}</el-tag>
              <el-tag size="small" type="success" v-if="item.storedLocation">
                存放位置: {{ item.storedLocation }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-container" v-if="filteredFoundItems.length > 0">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="totalCount"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增拾物对话框 -->
    <el-dialog v-model="dialogVisible" title="发布拾物公告" width="60%" @closed="resetForm">
      <el-form
        ref="formRef"
        :model="foundItemForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="foundItemForm.title" placeholder="请输入物品名称" />
        </el-form-item>

        <el-form-item label="物品类别" prop="category">
          <el-select v-model="foundItemForm.category" placeholder="请选择物品类别">
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="拾得时间" prop="foundTime">
          <el-date-picker
            v-model="foundItemForm.foundTime"
            type="datetime"
            placeholder="选择日期和时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="拾得地点" prop="foundLocation">
          <el-input v-model="foundItemForm.foundLocation" placeholder="请输入拾得的具体地点" />
        </el-form-item>

        <el-form-item label="存放位置" prop="storedLocation">
          <el-input v-model="foundItemForm.storedLocation" placeholder="请输入物品当前存放的位置" />
          <span class="form-tip">例如：小区物业办公室、门卫室等</span>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="foundItemForm.description"
            type="textarea"
            rows="4"
            placeholder="请详细描述物品的特征，拾得情况等"
          />
        </el-form-item>

        <el-form-item label="物品领取要求" prop="claimRequirements">
          <el-input
            v-model="foundItemForm.claimRequirements"
            type="textarea"
            rows="2"
            placeholder="失主需要满足什么条件才能领取物品？"
          />
          <span class="form-tip">例如：能描述物品特征、提供购买凭证等</span>
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="foundItemForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="foundItemForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="物品图片">
          <el-upload
            action="/api/upload"
            list-type="picture-card"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            multiple
          >
            <template #default>
              <el-icon><Plus /></el-icon>
            </template>
            <template #file="{ file }">
              <img :src="file.url" class="el-upload-list__item-thumbnail" />
            </template>
          </el-upload>
          <div class="form-tip">上传物品的照片，最多5张</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFoundItem" :loading="submitting">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture, Map, Calendar, UserFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import type { UploadUserFile } from 'element-plus'

const router = useRouter()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(true)
const searchQuery = ref('')
const categoryFilter = ref('')
const dateRange = ref<[string, string] | null>(null)
const currentPage = ref(1)
const pageSize = ref(16)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const totalCount = computed(() => foundItemsStore.totalFoundItems)
const filteredFoundItems = computed(() => {
  return foundItemsStore.filteredFoundItems
})

// 物品类别
const categories = [
  { value: 'electronics', label: '电子产品' },
  { value: 'documents', label: '证件证书' },
  { value: 'keys', label: '钥匙钥匙扣' },
  { value: 'clothing', label: '服装衣物' },
  { value: 'jewelry', label: '首饰饰品' },
  { value: 'bags', label: '背包/箱包' },
  { value: 'cards', label: '银行卡/会员卡' },
  { value: 'other', label: '其他物品' }
]

// 表单数据
const foundItemForm = reactive({
  title: '',
  category: '',
  foundTime: '',
  foundLocation: '',
  storedLocation: '',
  description: '',
  claimRequirements: '',
  contactName: '',
  contactPhone: '',
  images: [] as string[]
})

// 表单验证规则
const formRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物品类别', trigger: 'change' }
  ],
  foundTime: [
    { required: true, message: '请选择拾得时间', trigger: 'change' }
  ],
  foundLocation: [
    { required: true, message: '请输入拾得地点', trigger: 'blur' }
  ],
  storedLocation: [
    { required: true, message: '请输入存放位置', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入物品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在10到500个字符之间', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

// 生命周期钩子
onMounted(async () => {
  try {
    await foundItemsStore.fetchFoundItems({
      page: currentPage.value,
      limit: pageSize.value
    })
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取拾物公告列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
})

// 状态处理
const getStatusClass = (status: string) => {
  switch (status) {
    case 'unclaimed': return 'status-unclaimed'
    case 'claimed': return 'status-claimed'
    case 'pending': return 'status-pending'
    default: return ''
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'unclaimed': return '待认领'
    case 'claimed': return '已认领'
    case 'pending': return '认领中'
    default: return '未知'
  }
}

// 获取分类名称
const getCategoryLabel = (categoryValue: string) => {
  const category = categories.find(cat => cat.value === categoryValue)
  return category?.label || '其他物品'
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 搜索
const handleSearch = async () => {
  loading.value = true
  try {
    await foundItemsStore.fetchFoundItems({
      page: 1,
      limit: pageSize.value,
      query: searchQuery.value,
      category: categoryFilter.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    currentPage.value = 1
  } catch (error) {
    console.error('Failed to search found items:', error)
    ElMessage.error('搜索失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 分页
const handlePageChange = async (page: number) => {
  currentPage.value = page
  loading.value = true
  try {
    await foundItemsStore.fetchFoundItems({
      page,
      limit: pageSize.value,
      query: searchQuery.value,
      category: categoryFilter.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
  } catch (error) {
    console.error('Failed to fetch found items:', error)
    ElMessage.error('获取拾物公告列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewItemDetails = (id: string | number) => {
  router.push(`/found-items/${id}`)
}

// 添加拾物公告
const handleAddFoundItem = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发布拾物公告')
    router.push('/login')
    return
  }

  dialogVisible.value = true
}

// 文件上传处理
const handleFileChange = (uploadFile: any) => {
  if (fileList.value.length > 5) {
    ElMessage.warning('最多只能上传5张图片')
    const index = fileList.value.indexOf(uploadFile)
    if (index !== -1) {
      fileList.value.splice(index, 1)
    }
  }
}

const handleFileRemove = (uploadFile: any) => {
  const index = fileList.value.indexOf(uploadFile)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  fileList.value = []
}

// 提交表单
const submitFoundItem = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true

      try {
        // 模拟图片上传
        const uploadedImages = fileList.value.map(file => URL.createObjectURL(file.raw as Blob))

        // 准备要提交的数据
        const foundItemData = {
          ...foundItemForm,
          images: uploadedImages,
          status: 'unclaimed',
          contact: {
            name: foundItemForm.contactName,
            phone: foundItemForm.contactPhone
          }
        }

        // 提交数据
        const result = await foundItemsStore.addFoundItem(foundItemData)

        if (result.success) {
          ElMessage.success('拾物公告发布成功')
          dialogVisible.value = false
          // 重新加载数据
          handleSearch()
        } else {
          ElMessage.error(result.message || '发布失败，请稍后再试')
        }
      } catch (error) {
        console.error('Failed to submit found item:', error)
        ElMessage.error('发布失败，请稍后再试')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.found-items-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
}

.actions-container {
  margin-bottom: 24px;
}

.search-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-container .el-input {
  flex: 1;
  min-width: 200px;
}

.search-container .el-select {
  width: 160px;
}

.add-btn-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .add-btn-container {
    justify-content: flex-start;
  }

  .search-container {
    margin-bottom: 12px;
  }
}

.found-items-list {
  margin-bottom: 24px;
}

.found-item-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
  cursor: pointer;
}

.found-item-card:hover {
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

.item-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
  z-index: 1;
}

.status-unclaimed {
  background-color: #409EFF;
}

.status-claimed {
  background-color: #67C23A;
}

.status-pending {
  background-color: #E6A23C;
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
  flex-wrap: wrap;
  gap: 8px;
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
