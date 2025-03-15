<template>
  <div class="login-container">
    <div class="login-panel">
      <div class="logo-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>住宅小区互助寻物系统</h1>
      </div>

      <h2 class="login-title">居民用户登录</h2>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="compact-form"
      >
        <el-form-item prop="username" label="用户名/邮箱">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
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
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
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
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, message: '用户名不能少于3个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6个字符', trigger: 'blur' },
  ],
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''

      try {
        console.log('Submitting login for:', loginForm.username)
        const result = await userStore.login(
          loginForm.username,
          loginForm.password
        )

        if (result.success) {
          console.log('Login successful')

          // 检查登录状态
          console.log('Auth state after login:',
            'token:', !!userStore.token,
            'user:', !!userStore.user,
            'isAuthenticated:', userStore.isAuthenticated
          )

          ElMessage.success('登录成功！')

          // 如果有重定向参数，则重定向到指定页面
          const redirectPath = route.query.redirect as string
          router.push(redirectPath || '/')
        } else {
          if (result.message && result.message.includes('管理员账号')) {
            // 显示更友好的错误提示，引导管理员使用正确的登录入口
            error.value = '您正在使用管理员账号登录居民入口，请前往管理员登录页面'
            ElMessage.warning({
              message: '请使用管理员登录入口',
              duration: 5000
            })
          } else {
            error.value = result.message || '登录失败，请检查用户名和密码'
          }
        }
      } catch (err) {
        console.error('Login error:', err)
        error.value = '登录失败，请稍后再试'
      } finally {
        loading.value = false
      }
    }
  })
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<style scoped>
.login-container {
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

.login-panel {
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
  font-size: 20px;
  color: #303133;
  text-align: center;
  margin-bottom: 20px;
  font-weight: 500;
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

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

.error-message {
  margin-top: 20px;
}

@media (max-width: 576px) {
  .login-panel {
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
