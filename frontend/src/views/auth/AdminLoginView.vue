<template>
  <div class="admin-login-container">
    <div class="admin-login-panel">
      <div class="logo-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>住宅小区互助寻物系统</h1>
      </div>

      <h2 class="login-title">管理员登录</h2>
      <div class="admin-login-notice">
        <el-alert
          title="管理员专用登录入口"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            此登录入口仅供<strong>小区管理员</strong>和<strong>系统管理员</strong>使用。居民用户请使用<router-link to="/login">普通登录</router-link>。
          </template>
        </el-alert>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="compact-form"
      >
        <el-form-item prop="username" label="管理员账号">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            :prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            :disabled="loading"
          />
        </el-form-item>

        <div class="remember-forgot">
          <el-checkbox v-model="loginForm.rememberMe" :disabled="loading">记住我</el-checkbox>
          <el-button type="text" @click="goToForgotPassword" :disabled="loading"
            >忘记密码？</el-button
          >
        </div>

        <el-form-item>
          <el-button type="primary" native-type="submit" class="submit-btn" :loading="loading">
            管理员登录
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const error = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, message: '账号不能少于3个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' },
  ],
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''

      try {
        console.log('Submitting admin login for:', loginForm.username)
        // 使用adminLogin方法，专门用于管理员登录
        const result = await userStore.adminLogin(
          loginForm.username,
          loginForm.password
        )

        if (result.success) {
          console.log('Admin login successful')

          // 检查登录状态和角色
          console.log('Auth state after login:',
            'token:', !!userStore.token,
            'user:', !!userStore.user,
            'role:', userStore.user?.role,
            'isAuthenticated:', userStore.isAuthenticated
          )

          ElMessage.success('管理员登录成功！')

          // 根据管理员类型跳转到不同页面
          if (userStore.isSysAdmin) {
            router.push('/admin/users') // 系统管理员直接进入用户管理
          } else if (userStore.isAdmin) {
            router.push('/admin/announcements') // 小区管理员进入公告管理
          } else {
            // 如果不是管理员角色，显示错误
            error.value = '您的账号没有管理员权限'
            userStore.logout() // 登出非管理员用户
            return
          }
        } else {
          if (result.message && result.message.includes('没有管理员权限')) {
            // 显示更友好的错误提示，引导居民用户使用正确的登录入口
            error.value = '您正在使用居民账号登录管理员入口，请前往居民登录页面'
            ElMessage.warning({
              message: '请使用居民登录入口',
              duration: 5000
            })
          } else {
            error.value = result.message || '登录失败，请检查账号和密码'
          }
        }
      } catch (err) {
        console.error('Admin login error:', err)
        error.value = '登录失败，请稍后再试'
      } finally {
        loading.value = false
      }
    }
  })
}

const goToForgotPassword = () => {
  router.push('/admin/forgot-password')
}
</script>

<style scoped>
.admin-login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  padding: 0 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.admin-login-panel {
  width: 100%;
  max-width: 400px;
  max-height: calc(100vh - 40px);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 0;
  overflow-y: auto;
}

.logo-header {
  text-align: center;
  margin-bottom: 20px;
}

.logo {
  width: 60px;
  height: 60px;
  margin-bottom: 10px;
}

.logo-header h1 {
  font-size: 20px;
  color: #409eff;
  font-weight: 500;
}

.login-title {
  font-size: 22px;
  color: #303133;
  text-align: center;
  margin-bottom: 10px;
  font-weight: 500;
}

.admin-login-notice {
  margin-bottom: 20px;
}

.admin-login-notice :deep(.el-alert) {
  margin-bottom: 0;
}

.admin-login-notice a {
  color: #409eff;
  text-decoration: none;
}

.admin-login-notice a:hover {
  text-decoration: underline;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
}

.error-message {
  margin-top: 20px;
}

@media (max-width: 576px) {
  .admin-login-panel {
    box-shadow: none;
    padding: 20px;
  }

  .logo-header h1 {
    font-size: 20px;
  }

  .login-title {
    font-size: 18px;
  }
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.compact-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  line-height: 1.2;
}

.compact-form :deep(.el-input__wrapper) {
  padding: 0 11px;
}

.compact-form :deep(.el-input__inner) {
  height: 36px;
}

/* Override browser autofill styling */
:deep(input:-webkit-autofill),
:deep(input:-webkit-autofill:hover),
:deep(input:-webkit-autofill:focus),
:deep(input:-webkit-autofill:active) {
  /* Use a very light blue instead of pure white */
  -webkit-box-shadow: 0 0 0 30px rgba(232, 240, 254, 0.4) inset !important;
  -webkit-text-fill-color: #606266 !important;
  transition: background-color 5000s ease-in-out 0s;
}

/* Make input backgrounds transparent to show autofilled content */
:deep(.el-input__wrapper) {
  background-color: transparent !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-input__wrapper:hover) {
  background-color: transparent !important;
}
</style>
