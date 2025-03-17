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
          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" disabled placeholder="用户名不可修改" />
          </el-form-item>

          <el-form-item label="姓名" prop="realName">
            <el-input v-model="profileForm.realName" disabled placeholder="姓名不可修改" />
          </el-form-item>

          <el-form-item label="住址" prop="address">
            <el-input v-model="profileForm.address" disabled placeholder="住址不可修改" />
          </el-form-item>

          <el-form-item label="电子邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入您的电子邮箱" />
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入您的手机号码" />
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
  realName: '',
  address: '',
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userStore = useUserStore()

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
  profileForm.address = user.address || ''

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
          phone: profileForm.phone
        }

        const result = await userStore.updateProfile(updateData)

        if (result.success) {
          ElMessage.success('个人信息已更新')
          // Update form with the returned data if available
          if (result.data) {
            profileForm.email = result.data.email || profileForm.email
            profileForm.phone = result.data.phone || profileForm.phone
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

// 生命周期钩子
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.settings-container {
  padding: 10px;
  min-height: auto;
}

.loading-container {
  min-height: 400px;
  padding: 20px;
}

.page-title {
  margin: 10px 0 20px 0;
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
  height: auto;
}

@media (max-width: 768px) {
  .settings-card {
    padding: 15px;
  }
}
</style>
