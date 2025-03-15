<template>
  <div class="settings-container">
    <h2 class="page-title">个人设置</h2>

    <div v-if="isLoading" class="loading-container">
      <el-skeleton style="width: 100%" animated>
        <template #template>
          <div style="padding: 20px">
            <el-skeleton-item variant="p" style="width: 60%; height: 40px" />
            <div style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0">
              <el-skeleton-item variant="text" style="width: 30%" />
              <el-skeleton-item variant="text" style="width: 30%" />
            </div>
            <el-skeleton-item variant="p" style="width: 100%; height: 300px" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else>
      <!-- 个人信息设置 -->
      <el-card shadow="never" class="settings-card">
        <h3 class="section-title">基本信息</h3>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="100px"
          status-icon
        >
          <el-form-item label="用户头像">
            <div class="avatar-container">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <div class="avatar-wrapper">
                  <el-avatar v-if="avatarUrl" :size="100" :src="avatarUrl" />
                  <div v-else class="avatar-placeholder">
                    <el-icon class="avatar-icon"><Plus /></el-icon>
                  </div>
                  <div class="avatar-overlay">
                    <el-icon><Edit /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
              </el-upload>
              <div class="upload-hint">点击上传头像，图片格式为 JPG/PNG，不超过 2MB</div>
            </div>
          </el-form-item>

          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" disabled placeholder="用户名不可修改" />
          </el-form-item>

          <el-form-item label="电子邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入您的电子邮箱" />
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入您的手机号码" />
          </el-form-item>

          <el-form-item label="姓名" prop="realName">
            <el-input v-model="profileForm.realName" placeholder="请输入您的真实姓名" />
          </el-form-item>

          <el-form-item label="注册时间">
            <div>{{ userCreatedAt }}</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="updateProfile" :loading="profileLoading">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 修改密码 -->
      <el-card shadow="never" class="settings-card">
        <h3 class="section-title">修改密码</h3>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          status-icon
        >
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="updatePassword" :loading="passwordLoading">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore, type User } from '@/stores/user'
import { format } from 'date-fns'

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 加载状态
const profileLoading = ref(false)
const passwordLoading = ref(false)
const isLoading = ref(true)

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  realName: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userStore = useUserStore()

// 上传相关
const uploadUrl = computed(() => '/api/users/avatar') // Use actual API endpoint
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))
const avatarUrl = ref('')

// 计算属性
const userCreatedAt = computed(() => {
  const user = userStore.user
  if (!user || !user.createdAt) return '-'

  try {
    return format(new Date(user.createdAt), 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return user.createdAt || '-'
  }
})

// 验证规则
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }

  const phoneRegex = /^1[3456789]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入有效的手机号码'))
  } else {
    callback()
  }
}

const profileRules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入电子邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的电子邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  realName: [
    { max: 20, message: '姓名长度不能超过20个字符', trigger: 'blur' }
  ]
})

const passwordRules = reactive<FormRules>({
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 方法
const loadUserData = () => {
  isLoading.value = true

  const user = userStore.user
  if (!user) {
    isLoading.value = false
    return
  }

  profileForm.username = user.username || ''
  profileForm.email = user.email || ''
  profileForm.phone = user.phone || ''
  profileForm.realName = user.realName || ''
  avatarUrl.value = user.avatar || ''

  isLoading.value = false
}

const updateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      profileLoading.value = true
      try {
        // Create update data with typed fields for the API
        const updateData: Partial<User> = {
          email: profileForm.email,
          phone: profileForm.phone,
          realName: profileForm.realName
        }

        const result = await userStore.updateProfile(updateData)

        if (result.success) {
          ElMessage.success('个人信息已更新')
          // Update form with the returned data if available
          if (result.data) {
            profileForm.email = result.data.email || profileForm.email
            profileForm.phone = result.data.phone || profileForm.phone
            profileForm.realName = result.data.realName || profileForm.realName
          }
        } else {
          ElMessage.error(result.message || '更新失败')
        }
      } catch (error) {
        console.error('Failed to update profile:', error)
        ElMessage.error('更新失败，请稍后再试')
      } finally {
        profileLoading.value = false
      }
    }
  })
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        const result = await userStore.changePassword({
          oldPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })

        if (result.success) {
          ElMessage.success('密码已更新')
          passwordForm.currentPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          ElMessage.error(result.message || '更新失败')
        }
      } catch (error) {
        console.error('Failed to update password:', error)
        ElMessage.error('更新失败，请稍后再试')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }

  return true
}

const handleAvatarSuccess = (response: any) => {
  if (response.success) {
    avatarUrl.value = response.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 生命周期钩子
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.settings-container {
  padding: 0 10px;
}

.loading-container {
  min-height: 400px;
  padding: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.settings-card {
  margin-bottom: 20px;
  padding: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 10px 0;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed #c0c4cc;
  border-radius: 50%;
}

.avatar-icon {
  font-size: 40px;
  color: #909399;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
}

.avatar-overlay span {
  margin-top: 6px;
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-uploader {
  display: inline-block;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
  text-align: center;
  max-width: 250px;
}

.avatar-uploader-icon {
  display: none;
}

@media (max-width: 768px) {
  .settings-card {
    padding: 15px;
  }

  .avatar-container {
    margin: 5px 0;
  }

  .avatar-wrapper {
    width: 80px;
    height: 80px;
  }

  .avatar-placeholder {
    width: 80px;
    height: 80px;
  }

  .avatar-icon {
    font-size: 32px;
  }

  .avatar-overlay span {
    font-size: 10px;
  }
}
</style>
