<template>
  <div class="lost-items-container">
    <div class="page-header">
      <h1>寻物启事</h1>
      <p>查看社区内的寻物信息，或发布您丢失的物品</p>
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
              @click="handleAddLostItem"
              v-if="isLoggedIn"
            >
              发布寻物启事
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

    <el-row :gutter="20" class="lost-items-list">
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

      <template v-else-if="filteredLostItems.length === 0">
        <el-col :span="24">
          <el-empty
            description="暂无寻物信息"
            :image-size="200"
          >
            <template #description>
              <p>暂无符合条件的寻物信息</p>
            </template>
            <el-button type="primary" @click="handleAddLostItem" v-if="isLoggedIn">发布寻物启事</el-button>
          </el-empty>
        </el-col>
      </template>

      <el-col
        v-for="item in filteredLostItems"
        :key="item.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card shadow="hover" class="lost-item-card" @click="viewItemDetails(item.id)">
          <div class="item-image">
            <el-image
              :src="item.images?.[0] || '/placeholder-image.png'"
              fit="cover"
              :preview-src-list="item.images || []"
              :initial-index="0"
              alt="丢失物品图片"
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
                <span>{{ item.lostLocation }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(item.lostTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><UserFilled /></el-icon>
                <span>{{ item.contact.name }}</span>
              </div>
            </div>
            <div class="item-footer">
              <el-tag size="small">{{ getCategoryLabel(item.category) }}</el-tag>
              <span class="reward" v-if="item.reward">
                悬赏: {{ item.reward }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-container" v-if="filteredLostItems.length > 0">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="totalCount"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增寻物对话框 -->
    <el-dialog v-model="dialogVisible" title="发布寻物启事" width="60%" @closed="resetForm">
      <el-form
        ref="formRef"
        :model="lostItemForm"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="lostItemForm.title" placeholder="请输入物品名称" />
        </el-form-item>

        <el-form-item label="物品类别" prop="category">
          <el-select v-model="lostItemForm.category" placeholder="请选择物品类别">
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="丢失时间" prop="lostTime">
          <el-date-picker
            v-model="lostItemForm.lostTime"
            type="datetime"
            placeholder="选择日期和时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="丢失地点" prop="lostLocation">
          <el-input v-model="lostItemForm.lostLocation" placeholder="请输入丢失的具体地点" />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="lostItemForm.description"
            type="textarea"
            rows="4"
            placeholder="请详细描述物品的特征，丢失情况等"
          />
        </el-form-item>

        <el-form-item label="悬赏金额" prop="reward">
          <el-input-number v-model="lostItemForm.reward" :min="0" :step="10" />
          <span class="form-tip">可选，设置悬赏金额可提高找回几率</span>
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="lostItemForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="lostItemForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="物品图片">
          <el-upload
            action="/upload"
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
          <el-button type="primary" @click="submitLostItem" :loading="submitting">
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
import { ElMessage, FormInstance, FormRules, ElLoading } from 'element-plus'
import { useLostItemsStore } from '@/stores/lostItems'
import { useUserStore } from '@/stores/user'
import type { UploadUserFile } from 'element-plus'
import fileUploadService from '@/services/fileUploadService'

const router = useRouter()
const lostItemsStore = useLostItemsStore()
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
const totalCount = computed(() => lostItemsStore.totalLostItems)
const filteredLostItems = computed(() => {
  return lostItemsStore.filteredLostItems
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
const lostItemForm = reactive({
  title: '',
  category: '',
  lostTime: '',
  lostLocation: '',
  description: '',
  reward: 0,
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
  lostTime: [
    { required: true, message: '请选择丢失时间', trigger: 'change' }
  ],
  lostLocation: [
    { required: true, message: '请输入丢失地点', trigger: 'blur' }
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
    await lostItemsStore.fetchLostItems({
      page: currentPage.value,
      limit: pageSize.value
    })
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
    ElMessage.error('获取寻物启事列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
})

// 状态处理
const getStatusClass = (status: string) => {
  switch (status) {
    case 'open': return 'status-open'
    case 'found': return 'status-found'
    case 'closed': return 'status-closed'
    default: return ''
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'open': return '寻找中'
    case 'found': return '已找到'
    case 'closed': return '已关闭'
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
    await lostItemsStore.fetchLostItems({
      page: 1,
      limit: pageSize.value,
      query: searchQuery.value,
      category: categoryFilter.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    currentPage.value = 1
  } catch (error) {
    console.error('Failed to search lost items:', error)
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
    await lostItemsStore.fetchLostItems({
      page,
      limit: pageSize.value,
      query: searchQuery.value,
      category: categoryFilter.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
  } catch (error) {
    console.error('Failed to fetch lost items:', error)
    ElMessage.error('获取寻物启事列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewItemDetails = (id: string | number) => {
  router.push(`/lost-items/${id}`)
}

// 添加寻物启事
const handleAddLostItem = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发布寻物启事')
    router.push('/login')
    return
  }

  dialogVisible.value = true
}

// 文件上传处理
const handleFileChange = (uploadFile: UploadUserFile) => {
  if (uploadFile.raw) {
    const isValid = fileUploadService.validateFile(uploadFile.raw, {
      allowedTypes: ['image/jpeg', 'image/png', 'image/gif'],
      maxSize: 5,
      showMessage: true
    })

    if (!isValid) {
      const index = fileList.value.findIndex(item => item.uid === uploadFile.uid)
      if (index !== -1) {
        fileList.value.splice(index, 1)
      }
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
  lostItemForm.reward = 0
}

// 提交表单
const submitLostItem = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true

      // 显示加载指示器
      const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在上传图片和提交数据...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      try {
        // 上传图片
        const uploadPromises = fileList.value.map(async (file) => {
          if (file.raw) {
            try {
              const result = await fileUploadService.uploadFile(file.raw, 'item-image');
              if (result.success && result.data && result.data.url) {
                return result.data.url;
              } else {
                console.error('图片上传失败:', result);
                throw new Error(result.message || '图片上传失败');
              }
            } catch (error) {
              console.error('图片上传错误:', error);
              throw error;
            }
          }
          return null;
        });

        // 等待所有图片上传完成
        const uploadedImages = (await Promise.all(uploadPromises)).filter(url => url !== null) as string[];

        // 准备要提交的数据
        const lostItemData = {
          ...lostItemForm,
          images: uploadedImages,
          status: 'pending',
          contact: {
            name: lostItemForm.contactName,
            phone: lostItemForm.contactPhone
          }
        }

        // 提交数据
        const result = await lostItemsStore.addLostItem(lostItemData)

        if (result.success) {
          ElMessage.success('寻物启事发布成功')
          dialogVisible.value = false
          resetForm();
          // 重新加载数据
          handleSearch()
        } else {
          ElMessage.error(result.message || '发布失败，请稍后再试')
        }
      } catch (error) {
        console.error('Failed to submit lost item:', error)
        ElMessage.error('发布失败，请稍后再试')
      } finally {
        // 关闭加载指示器
        loadingInstance.close()
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.lost-items-container {
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

.lost-items-list {
  margin-bottom: 24px;
}

.lost-item-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
  cursor: pointer;
}

.lost-item-card:hover {
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

.status-open {
  background-color: #409EFF;
}

.status-found {
  background-color: #67C23A;
}

.status-closed {
  background-color: #909399;
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
  color: #F56C6C;
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
