<template>
  <div class="settings-container">
    <h2 class="page-title">个人设置</h2>

    <el-tabs v-model="activeTab">
      <!-- 个人信息设置 -->
      <el-tab-pane label="个人信息" name="profile">
        <el-card shadow="never">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            status-icon
          >
            <el-form-item label="用户头像">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar v-if="avatarUrl" :size="90" :src="avatarUrl" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-hint">点击上传头像，图片格式为 JPG/PNG，不超过 2MB</div>
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
              <div>{{ formatDate(userInfo.createdAt) }}</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="profileLoading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-card shadow="never">
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

          <el-divider />

          <h3 class="section-title">账号安全</h3>
          <div class="security-items">
            <div class="security-item">
              <div class="security-info">
                <el-icon class="security-icon"><Message /></el-icon>
                <div>
                  <div class="security-title">邮箱验证</div>
                  <div class="security-desc">用于接收安全验证信息和重要通知</div>
                </div>
              </div>
              <div>
                <el-tag v-if="emailVerified" type="success">已验证</el-tag>
                <el-button v-else type="primary" size="small" @click="verifyEmail">
                  验证邮箱
                </el-button>
              </div>
            </div>

            <div class="security-item">
              <div class="security-info">
                <el-icon class="security-icon"><Phone /></el-icon>
                <div>
                  <div class="security-title">手机验证</div>
                  <div class="security-desc">用于接收安全验证信息和重要通知</div>
                </div>
              </div>
              <div>
                <el-tag v-if="phoneVerified" type="success">已验证</el-tag>
                <el-button v-else type="primary" size="small" @click="verifyPhone">
                  验证手机
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 通知设置 -->
      <el-tab-pane label="通知设置" name="notifications">
        <el-card shadow="never">
          <h3 class="section-title">通知设置</h3>
          <div class="notification-items">
            <div class="notification-item">
              <div class="notification-info">
                <div class="notification-title">寻物消息通知</div>
                <div class="notification-desc">当您的寻物启事收到回复或匹配时收到通知</div>
              </div>
              <el-switch v-model="notificationSettings.lostItems" @change="saveNotificationSettings" />
            </div>

            <div class="notification-item">
              <div class="notification-info">
                <div class="notification-title">招领消息通知</div>
                <div class="notification-desc">当您的拾获物品有人认领时收到通知</div>
              </div>
              <el-switch v-model="notificationSettings.foundItems" @change="saveNotificationSettings" />
            </div>

            <div class="notification-item">
              <div class="notification-info">
                <div class="notification-title">系统公告通知</div>
                <div class="notification-desc">接收系统公告和更新信息</div>
              </div>
              <el-switch v-model="notificationSettings.announcements" @change="saveNotificationSettings" />
            </div>

            <div class="notification-item">
              <div class="notification-info">
                <div class="notification-title">邮件通知</div>
                <div class="notification-desc">通过邮件接收通知</div>
              </div>
              <el-switch v-model="notificationSettings.email" @change="saveNotificationSettings" />
            </div>

            <div class="notification-item">
              <div class="notification-info">
                <div class="notification-title">短信通知</div>
                <div class="notification-desc">通过短信接收重要通知</div>
              </div>
              <el-switch v-model="notificationSettings.sms" @change="saveNotificationSettings" />
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Message, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { format } from 'date-fns'

const userStore = useUserStore()
const activeTab = ref('profile')

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 加载状态
const profileLoading = ref(false)
const passwordLoading = ref(false)

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

// 通知设置
const notificationSettings = reactive({
  lostItems: true,
  foundItems: true,
  announcements: true,
  email: true,
  sms: false
})

// 验证状态
const emailVerified = ref(true)
const phoneVerified = ref(false)

// 上传相关
const uploadUrl = 'https://api.example.com/upload/avatar'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))
const avatarUrl = ref('')

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

// 计算属性
const userInfo = computed(() => userStore.currentUser || {})

// 方法
const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd')
  } catch (error) {
    return dateString || '-'
  }
}

const loadUserData = () => {
  const user = userStore.currentUser
  if (user) {
    profileForm.username = user.username || ''
    profileForm.email = user.email || ''
    profileForm.phone = user.phone || ''
    profileForm.realName = user.realName || ''
    avatarUrl.value = user.avatar || ''
  }
}

const updateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      profileLoading.value = true
      try {
        const result = await userStore.updateProfile({
          email: profileForm.email,
          phone: profileForm.phone,
          realName: profileForm.realName
        })

        if (result.success) {
          ElMessage.success('个人信息已更新')
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
          currentPassword: passwordForm.currentPassword,
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

const verifyEmail = () => {
  ElMessage.success('验证邮件已发送，请查收')
}

const verifyPhone = () => {
  ElMessage.success('验证短信已发送，请查收')
}

const saveNotificationSettings = () => {
  // 在实际应用中，这里会调用API保存通知设置
  ElMessage.success('通知设置已保存')
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

.avatar-uploader {
  text-align: center;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 90px;
  height: 90px;
  line-height: 90px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.security-items,
.notification-items {
  margin-bottom: 20px;
}

.security-item,
.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.security-item:last-child,
.notification-item:last-child {
  border-bottom: none;
}

.security-info,
.notification-info {
  display: flex;
  align-items: center;
}

.security-icon {
  font-size: 24px;
  margin-right: 16px;
  color: #409EFF;
}

.security-title,
.notification-title {
  font-size: 16px;
  margin-bottom: 4px;
  color: #303133;
}

.security-desc,
.notification-desc {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .security-item,
  .notification-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .security-item > div:last-child,
  .notification-item > div:last-child {
    margin-top: 10px;
    align-self: flex-end;
  }
}
</style>
