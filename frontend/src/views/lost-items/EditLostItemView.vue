<template>
  <main-layout>
    <div class="lost-item-form-container">
      <!-- Loading state when editing -->
      <div v-if="isEditing && loading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <div style="padding: 20px; max-width: 800px; margin: 0 auto;">
              <el-skeleton-item variant="h1" style="width: 50%; height: 40px; margin-bottom: 20px;" />
              <div style="display: flex; flex-direction: column; gap: 20px;">
                <el-skeleton-item variant="text" style="height: 40px;" />
                <el-skeleton-item variant="text" style="height: 40px;" />
                <el-skeleton-item variant="text" style="height: 40px;" />
                <el-skeleton-item variant="text" style="height: 100px;" />
                <el-skeleton-item variant="text" style="height: 40px;" />
                <el-skeleton-item variant="text" style="height: 40px;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- Error state when editing -->
      <el-result
        v-else-if="isEditing && error"
        icon="error"
        title="获取寻物启事信息失败"
        sub-title="请稍后再试或返回列表"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/lost-items')">
            返回寻物启事列表
          </el-button>
        </template>
      </el-result>

      <!-- Form content -->
      <div v-else class="form-content">
        <div class="page-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/lost-items' }">寻物启事</el-breadcrumb-item>
            <el-breadcrumb-item>{{ isEditing ? '编辑' : '发布' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title">{{ isEditing ? '编辑寻物启事' : '发布寻物启事' }}</h1>
          <p class="page-description">
            {{ isEditing ? '更新您的寻物启事信息，提高找回物品的几率' : '填写以下信息，发布寻物启事，让社区帮您找回丢失的物品' }}
          </p>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          class="lost-item-form"
          @submit.prevent="submitForm"
        >
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="16">
              <!-- 基本信息 -->
              <el-card class="form-card">
                <template #header>
                  <div class="card-header">
                    <h2>基本信息</h2>
                    <p>物品的基本描述信息</p>
                  </div>
                </template>

                <el-form-item label="物品名称" prop="title">
                  <el-input
                    v-model="formData.title"
                    placeholder="请输入物品名称，例如：黑色钱包、蓝色水杯等"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="物品类别" prop="category">
                  <el-select
                    v-model="formData.category"
                    placeholder="请选择物品类别"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="(label, value) in categoryOptions"
                      :key="value"
                      :label="label"
                      :value="value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="丢失时间" prop="lostDate">
                  <el-date-picker
                    v-model="formData.lostDate"
                    type="datetime"
                    placeholder="选择日期和时间"
                    style="width: 100%"
                    format="YYYY-MM-DD HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disableFutureDates"
                    @focus="handleDateFocus"
                  />
                </el-form-item>

                <el-form-item label="丢失地点" prop="lostLocation">
                  <el-input
                    v-model="formData.lostLocation"
                    placeholder="请输入丢失地点，例如：3号楼附近、小区北门等"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="悬赏金额" prop="reward">
                  <el-input-number
                    v-model="formData.reward"
                    :min="0"
                    :max="10000"
                    :step="10"
                    :precision="0"
                    style="width: 100%"
                    placeholder="可选，如需设置悬赏金额请输入"
                  >
                    <template #prepend>¥</template>
                  </el-input-number>
                  <div class="form-tip">可选项，设置悬赏金额可以提高找回几率</div>
                </el-form-item>

                <el-form-item label="详细描述" prop="description">
                  <el-input
                    v-model="formData.description"
                    type="textarea"
                    placeholder="请详细描述物品的特征、丢失情况等，以便他人辨认"
                    :rows="6"
                    maxlength="1000"
                    show-word-limit
                  />
                </el-form-item>
              </el-card>

              <!-- 联系方式 -->
              <el-card class="form-card">
                <template #header>
                  <div class="card-header">
                    <h2>联系方式</h2>
                    <p>如何联系到您（至少填写一项）</p>
                  </div>
                </template>

                <el-form-item label="联系信息" prop="contactInfo">
                  <el-input
                    v-model="formData.contactInfo"
                    placeholder="请输入联系方式，例如：电话、微信、邮箱等"
                    maxlength="100"
                    show-word-limit
                  />
                  <div class="form-tip">
                    请填写至少一种有效的联系方式，方便他人联系您。建议填写电话号码或微信号
                  </div>
                </el-form-item>
              </el-card>
            </el-col>

            <!-- 图片上传和提交 -->
            <el-col :xs="24" :sm="24" :md="8" class="right-column">
              <div class="sticky-container">
                <!-- 图片上传 -->
                <el-card class="form-card">
                  <template #header>
                    <div class="card-header">
                      <h2>物品图片</h2>
                      <p>可上传物品的照片（可选）</p>
                    </div>
                  </template>

                  <el-form-item label="上传图片" prop="images">
                    <el-upload
                      ref="uploadRef"
                      action="/api/upload"
                      list-type="picture-card"
                      :limit="5"
                      :multiple="true"
                      :file-list="fileList"
                      :on-exceed="handleExceed"
                      :before-upload="beforeUpload"
                      :on-success="handleUploadSuccess"
                      :on-error="handleUploadError"
                      :on-remove="handleRemove"
                      :headers="{ Authorization: `Bearer ${userStore.token}` }"
                    >
                      <el-icon><Plus /></el-icon>
                      <template #tip>
                        <div class="upload-tip">
                          支持JPG、PNG格式，单张不超过5MB，最多5张<br>
                          点击下方图片可以预览，鼠标悬停可以删除
                        </div>
                      </template>
                    </el-upload>

                    <div v-if="formData.images.length > 0" class="uploaded-images-info">
                      已上传 {{ formData.images.length }} 张图片
                    </div>
                  </el-form-item>
                </el-card>

                <!-- 提交按钮 -->
                <el-card class="form-card">
                  <template #header>
                    <div class="card-header">
                      <h2>发布信息</h2>
                    </div>
                  </template>

                  <div class="form-actions">
                    <el-button
                      type="primary"
                      size="large"
                      :loading="submitting"
                      @click="submitForm"
                    >
                      {{ isEditing ? '保存修改' : '发布寻物启事' }}
                    </el-button>
                    <el-button
                      plain
                      size="large"
                      @click="goBack"
                    >
                      取消
                    </el-button>
                  </div>
                </el-card>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import type { FormInstance, UploadFile, UploadUserFile } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useLostItemsStore } from '@/stores/lostItems'
import { useUserStore } from '@/stores/user'
import { ITEM_CATEGORIES } from '@/constants/categories'
import fileUploadService from '@/services/fileUploadService'

const router = useRouter()
const route = useRoute()
const lostItemsStore = useLostItemsStore()
const userStore = useUserStore()

// Form ref
const formRef = ref<FormInstance>()
const uploadRef = ref<FormInstance>()

// State variables
const loading = ref(false)
const error = ref(false)
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])

// Check if editing or creating
const isEditing = computed(() => Boolean(route.params.id))
const itemId = computed(() => {
  const id = Number(route.params.id)
  return isNaN(id) ? null : id
})

// Category options - 将对象转换为期望的格式
const categoryOptions = Object.fromEntries(
  ITEM_CATEGORIES.map(cat => [cat.value, cat.label])
)

// Form data
const formData = reactive({
  title: '',
  category: '',
  lostDate: '',
  lostLocation: '',
  description: '',
  contactInfo: '',
  reward: 0,
  images: [] as string[]
})

// Store the status separately from the form data
const itemStatus = ref<string | undefined>(undefined)

// Form validation rules
const formRules = {
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2-50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物品类别', trigger: 'change' }
  ],
  lostDate: [
    { required: true, message: '请选择丢失时间', trigger: 'change' }
  ],
  lostLocation: [
    { required: true, message: '请输入丢失地点', trigger: 'blur' },
    { min: 2, max: 100, message: '长度应为2-100个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '长度应为10-1000个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { min: 5, max: 100, message: '长度应为5-100个字符', trigger: 'blur' }
  ]
}

// Methods
// Handle focus on date picker to set default value
const handleDateFocus = () => {
  if (!formData.lostDate) {
    // If date is not set, default to current time
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    formData.lostDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }
}

// Disable future dates in date picker
const disableFutureDates = (time: Date) => {
  return time.getTime() > Date.now()
}

// Handle file upload exceed
const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

// Before upload hook
const beforeUpload = (file: File) => {
  return fileUploadService.validateFile(file, {
    allowedTypes: ['image/jpeg', 'image/png'],
    maxSize: 5,
    showMessage: true
  })
}

// Handle upload success
const handleUploadSuccess = (response: any, uploadFile: UploadFile) => {
  if (response.success && response.data && response.data.url) {
    formData.images.push(response.data.url)
    console.log('Image uploaded successfully:', response.data.url)
  } else {
    console.error('Image upload response missing data:', response)
    ElMessage.error('图片上传成功，但返回数据异常')
  }
}

// Handle upload error
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

// Handle remove
const handleRemove = (uploadFile: UploadFile) => {
  // Remove from formData.images
  const index = fileList.value.findIndex(file => file.uid === uploadFile.uid)
  if (index !== -1 && index < formData.images.length) {
    formData.images.splice(index, 1)
  }
}

// Go back
const goBack = () => {
  if (hasFormChanges()) {
    ElMessageBox.confirm(
      '有未保存的修改，确定要离开吗？',
      '未保存的修改',
      {
        confirmButtonText: '离开',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    ).then(() => {
      navigateBack()
    }).catch(() => {
      // Continue editing
    })
  } else {
    navigateBack()
  }
}

// Navigate back
const navigateBack = () => {
  if (isEditing.value && itemId.value) {
    router.push(`/lost-items/${itemId.value}`)
  } else {
    router.push('/lost-items')
  }
}

// Check if form has changes
const hasFormChanges = () => {
  if (!isEditing.value) {
    // New form
    return formData.title ||
           formData.category ||
           formData.lostDate ||
           formData.lostLocation ||
           formData.description ||
           formData.contactInfo ||
           formData.reward > 0 ||
           formData.images.length > 0
  } else {
    // TODO: Compare with original data when editing
    return true
  }
}

// Submit form
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitting.value = true

      // 显示加载指示器
      const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在处理中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      try {
        let result

        if (isEditing.value && itemId.value) {
          // Update existing item
          // Include the status if it was loaded from the existing item
          const updateData: any = { ...formData }
          if (itemStatus.value) {
            // Add the status to the update data
            updateData.status = itemStatus.value
          }
          result = await lostItemsStore.updateLostItem(itemId.value, updateData)
        } else {
          // Create new item
          result = await lostItemsStore.createLostItem(formData)
        }

        if (result.success) {
          ElMessage.success({
            message: isEditing.value ? '寻物启事已更新' : '寻物启事已发布',
            duration: 2000
          })

          if (isEditing.value && itemId.value) {
            router.push(`/lost-items/${itemId.value}`)
          } else if (result.data && result.data.id) {
            router.push(`/lost-items/${result.data.id}`)
          } else {
            router.push('/lost-items')
          }
        } else {
          ElMessage.error(result.message || '操作失败，请重试')
        }
      } catch (error) {
        console.error('Failed to submit:', error)
        ElMessage.error('提交失败，请稍后再试')
      } finally {
        // 关闭加载指示器
        loadingInstance.close()
        submitting.value = false
      }
    } else {
      console.log('Validation failed:', fields)
      ElMessage.error('请检查表单填写是否正确')
    }
  })
}

// Load item data when editing
const loadItemData = async () => {
  if (!isEditing.value || !itemId.value) return

  loading.value = true
  error.value = false

  try {
    await lostItemsStore.fetchLostItemById(itemId.value)
    const item = lostItemsStore.currentItem

    if (item) {
      // Populate form data
      formData.title = item.title || ''
      formData.category = item.category || ''
      if (!item.lostDate) {
        formData.lostDate = item.createdAt || new Date().toISOString().slice(0, 19).replace('T', ' ');
      } else {
        formData.lostDate = item.lostDate
      }
      formData.lostLocation = item.lostLocation || ''
      formData.description = item.description || ''
      formData.contactInfo = item.contactInfo || ''
      formData.reward = item.reward || 0
      formData.images = item.images || []

      // Store the current status separately
      itemStatus.value = item.status

      // Prepare file list for upload component
      fileList.value = formData.images.map((url, index) => ({
        name: `image-${index + 1}.jpg`,
        url,
        uid: index
      }))
    } else {
      error.value = true
    }
  } catch (err) {
    console.error('Failed to load item data:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// Check authentication
const checkAuth = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再发布寻物启事')
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
  }
}

// Watch for route changes
watch(() => route.params.id, () => {
  if (isEditing.value && itemId.value) {
    loadItemData()
  }
})

// Lifecycle hooks
onMounted(() => {
  checkAuth()

  if (isEditing.value && itemId.value) {
    loadItemData()
  }
})
</script>

<style scoped>
.lost-item-form-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  margin: 16px 0 8px;
}

.page-description {
  color: #606266;
  font-size: 16px;
  margin: 0 0 20px;
}

.lost-item-form {
  margin-top: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  margin-bottom: 5px;
}

.card-header h2 {
  font-size: 18px;
  margin: 0 0 5px;
}

.card-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.form-tip, .upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

.uploaded-images-info {
  font-size: 13px;
  color: #409EFF;
  margin-top: 10px;
  text-align: center;
}

.form-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.form-actions .el-button {
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

.form-actions .el-button + .el-button {
  margin-top: 12px;
  margin-left: 0;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }
}

.right-column {
  position: relative;
}

.sticky-container {
  position: sticky;
  top: 20px;
  z-index: 10;
}

.form-card {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
