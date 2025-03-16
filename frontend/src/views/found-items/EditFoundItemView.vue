<template>
  <main-layout>
    <div class="found-item-form-container">
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
        title="获取失物招领信息失败"
        sub-title="请稍后再试或返回列表"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/found-items')">
            返回失物招领列表
          </el-button>
        </template>
      </el-result>

      <!-- Form content -->
      <div v-else class="form-content">
        <div class="page-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/found-items' }">失物招领</el-breadcrumb-item>
            <el-breadcrumb-item>{{ isEditing ? '编辑' : '发布' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title">{{ isEditing ? '编辑失物招领' : '发布失物招领' }}</h1>
          <p class="page-description">
            {{ isEditing ? '更新您的失物招领信息，帮助失主找回物品' : '填写以下信息，发布失物招领，帮助失主找回物品' }}
          </p>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          class="found-item-form"
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

                <el-form-item label="拾获时间" prop="foundDate">
                  <el-date-picker
                    v-model="formData.foundDate"
                    type="datetime"
                    placeholder="选择日期和时间"
                    style="width: 100%"
                    format="YYYY-MM-DD HH:mm"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled-date="disableFutureDates"
                  />
                </el-form-item>

                <el-form-item label="拾获地点" prop="foundLocation">
                  <el-input
                    v-model="formData.foundLocation"
                    placeholder="请输入拾获地点，例如：3号楼附近、小区北门等"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="存放位置" prop="storageLocation">
                  <el-input
                    v-model="formData.storageLocation"
                    placeholder="请输入物品当前存放位置，例如：物业前台、3号楼门卫室等"
                    maxlength="100"
                    show-word-limit
                  />
                  <div class="form-tip">填写物品当前存放位置，方便失主认领</div>
                </el-form-item>

                <el-form-item label="详细描述" prop="description">
                  <el-input
                    v-model="formData.description"
                    type="textarea"
                    placeholder="请详细描述物品的特征、拾获情况等，以便失主辨认"
                    :rows="6"
                    maxlength="1000"
                    show-word-limit
                  />
                </el-form-item>
              </el-card>

              <!-- 认领要求和联系方式 -->
              <el-card class="form-card">
                <template #header>
                  <div class="card-header">
                    <h2>认领要求与联系方式</h2>
                    <p>物品认领条件和如何联系到您</p>
                  </div>
                </template>

                <el-form-item label="认领要求" prop="claimRequirements">
                  <el-input
                    v-model="formData.claimRequirements"
                    type="textarea"
                    placeholder="请填写认领要求，例如：需提供物品特征描述、购买凭证等"
                    :rows="3"
                    maxlength="500"
                    show-word-limit
                  />
                  <div class="form-tip">
                    为确保物品归还给真正的失主，请说明认领者需要满足哪些条件
                  </div>
                </el-form-item>

                <el-form-item label="联系信息" prop="contactInfo">
                  <el-input
                    v-model="formData.contactInfo"
                    placeholder="请输入联系方式，例如：电话、微信、邮箱等"
                    maxlength="100"
                    show-word-limit
                  />
                  <div class="form-tip">
                    请填写至少一种有效的联系方式，方便失主联系您。建议填写电话号码或微信号
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
                      {{ isEditing ? '保存修改' : '发布失物招领' }}
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
import { useFoundItemsStore } from '@/stores/foundItems'
import { useUserStore } from '@/stores/user'
import { ITEM_CATEGORIES } from '@/constants/categories'
import fileUploadService from '@/services/fileUploadService'

const router = useRouter()
const route = useRoute()
const foundItemsStore = useFoundItemsStore()
const userStore = useUserStore()

// Form ref
const formRef = ref<FormInstance>()

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
  foundDate: '',
  foundLocation: '',
  storageLocation: '',
  description: '',
  claimRequirements: '',
  contactInfo: '',
  images: [] as string[]
})

// Form validation rules
const formRules = {
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2-50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物品类别', trigger: 'change' }
  ],
  foundDate: [
    { required: true, message: '请选择拾获时间', trigger: 'change' }
  ],
  foundLocation: [
    { required: true, message: '请输入拾获地点', trigger: 'blur' },
    { min: 2, max: 100, message: '长度应为2-100个字符', trigger: 'blur' }
  ],
  storageLocation: [
    { required: true, message: '请输入存放位置', trigger: 'blur' },
    { min: 2, max: 100, message: '长度应为2-100个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '长度应为10-1000个字符', trigger: 'blur' }
  ],
  claimRequirements: [
    { required: true, message: '请输入认领要求', trigger: 'blur' },
    { min: 5, max: 500, message: '长度应为5-500个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { min: 5, max: 100, message: '长度应为5-100个字符', trigger: 'blur' }
  ]
}

// Methods
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
    router.push(`/found-items/${itemId.value}`)
  } else {
    router.push('/found-items')
  }
}

// Check if form has changes
const hasFormChanges = () => {
  if (!isEditing.value) {
    // New form
    return formData.title ||
           formData.category ||
           formData.foundDate ||
           formData.foundLocation ||
           formData.storageLocation ||
           formData.description ||
           formData.claimRequirements ||
           formData.contactInfo ||
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
          result = await foundItemsStore.updateFoundItem(itemId.value, formData)
        } else {
          // Create new item
          result = await foundItemsStore.createFoundItem(formData)
        }

        if (result.success) {
          ElMessage.success({
            message: isEditing.value ? '失物招领信息已更新' : '失物招领信息已发布',
            duration: 2000
          })

          if (isEditing.value && itemId.value) {
            router.push(`/found-items/${itemId.value}`)
          } else if (result.data && result.data.id) {
            router.push(`/found-items/${result.data.id}`)
          } else {
            router.push('/found-items')
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
    await foundItemsStore.fetchFoundItemById(itemId.value)
    const item = foundItemsStore.currentItem

    if (item) {
      // Populate form data
      formData.title = item.title || ''
      formData.category = item.category || ''
      formData.foundDate = item.foundDate || ''
      formData.foundLocation = item.foundLocation || ''
      formData.storageLocation = item.storageLocation || ''
      formData.description = item.description || ''
      formData.claimRequirements = item.claimRequirements || ''
      formData.contactInfo = item.contactInfo || ''
      formData.images = item.images || []

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
    ElMessage.warning('请先登录后再发布失物招领')
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
.found-item-form-container {
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

.found-item-form {
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
