<template>
  <div class="register-container">
    <div class="register-panel">
      <div class="logo-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>住宅小区互助寻物系统</h1>
      </div>

      <h2 class="register-title">用户注册</h2>

      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="compact-form"
      >
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="email" label="邮箱 (选填)">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="realName" label="真实姓名">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            :prefix-icon="User"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="address" label="住址">
          <el-input
            v-model="registerForm.address"
            placeholder="请输入住址信息"
            :prefix-icon="Location"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" class="submit-btn" :loading="loading">
            注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
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
import { useRouter } from 'vue-router'
import { User, Lock, Message, Location } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const error = ref('')

const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  password: '',
  confirmPassword: '',
  address: '',
})

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      if (formRef.value) {
        formRef.value.validateField('confirmPassword')
      }
    }
    callback()
  }
}

const validatePassConfirm = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 仅当邮箱字段有值时才验证格式
const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    // 邮箱为空，不做验证，直接通过
    callback()
  } else {
    // 邮箱不为空，验证格式
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
    if (!emailPattern.test(value)) {
      callback(new Error('请输入有效的邮箱地址'))
    } else {
      callback()
    }
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名不能少于3个字符', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  address: [{ required: true, message: '请输入住址信息', trigger: 'blur' }],
  email: [{ required: false, validator: validateEmail, trigger: 'blur' }],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码不能少于6个字符', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validatePassConfirm, trigger: 'blur' }],
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''

      try {
        const result = await userStore.register({
          username: registerForm.username,
          realName: registerForm.realName,
          password: registerForm.password,
          address: registerForm.address,
          email: registerForm.email,
        })

        if (result.success) {
          ElMessage.success('注册成功！即将跳转到登录页面')
          setTimeout(() => {
            router.push('/login')
          }, 1500)
        } else {
          error.value = result.message || '注册失败，请检查您的输入'
        }
      } catch (err: any) {
        console.error('Registration error:', err)
        if (err.response && err.response.data && err.response.data.message) {
          // 处理后端返回的错误信息
          error.value = err.response.data.message
        } else {
          error.value = '注册失败，请稍后再试'
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  padding: 0 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.register-panel {
  width: 100%;
  max-width: 460px;
  max-height: calc(100vh - 40px);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
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

.register-title {
  font-size: 20px;
  color: #303133;
  text-align: center;
  margin-bottom: 20px;
  font-weight: 500;
}

.submit-btn {
  width: 100%;
  margin-top: 5px;
  padding: 10px 0;
}

.login-link {
  text-align: center;
  margin-top: 15px;
  color: #606266;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

@media (max-width: 576px) {
  .register-panel {
    box-shadow: none;
    padding: 15px;
  }

  .logo-header h1 {
    font-size: 18px;
  }

  .register-title {
    font-size: 16px;
  }
}

/* Add compact form styles */
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
  -webkit-box-shadow: 0 0 0 30px transparent inset !important;
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
