<template>
  <div class="admin-forgot-password-container">
    <div class="admin-forgot-password-panel">
      <div class="logo-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>住宅小区互助寻物系统</h1>
      </div>

      <h2 class="page-title">管理员密码找回</h2>
      <div class="admin-notice">
        <el-alert
          title="管理员账号密码找回"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            请输入您的管理员账号关联的邮箱，我们将向您发送密码重置邮件。
          </template>
        </el-alert>
      </div>

      <el-form
        ref="formRef"
        :model="forgotForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="compact-form"
      >
        <el-form-item prop="email" label="电子邮箱">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入关联的电子邮箱"
            :prefix-icon="Message"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" class="submit-btn" :loading="loading">
            发送重置邮件
          </el-button>
        </el-form-item>

        <div class="back-link">
          <router-link to="/admin/login">返回登录</router-link>
        </div>
      </el-form>

      <div v-if="submitResult" class="result-message">
        <el-alert
          :title="submitResult.message"
          :type="submitResult.type"
          show-icon
          :closable="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import apiClient from '@/utils/api'

interface SubmitResult {
  message: string
  type: 'success' | 'error' | 'info'
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitResult = ref<SubmitResult | null>(null)

const forgotForm = reactive({
  email: ''
})

const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的电子邮箱格式', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      submitResult.value = null

      try {
        // 向后端发送请求，添加adminPasswordReset标记
        const response = await apiClient.post('/auth/forgot-password', {
          email: forgotForm.email,
          isAdmin: true // 标记是管理员密码重置
        })

        submitResult.value = {
          message: '重置邮件已发送，请检查您的邮箱',
          type: 'success'
        }
      } catch (error: any) {
        const errorMessage = error.response?.data?.message || '发送重置邮件失败，请稍后再试'
        submitResult.value = {
          message: errorMessage,
          type: 'error'
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.admin-forgot-password-container {
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

.admin-forgot-password-panel {
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

.page-title {
  font-size: 20px;
  color: #303133;
  text-align: center;
  margin-bottom: 10px;
  font-weight: 500;
}

.admin-notice {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
}

.back-link {
  text-align: center;
  margin-top: 15px;
  color: #606266;
}

.back-link a {
  color: #409eff;
  text-decoration: none;
}

.back-link a:hover {
  text-decoration: underline;
}

.result-message {
  margin-top: 20px;
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

@media (max-width: 576px) {
  .admin-forgot-password-panel {
    box-shadow: none;
    padding: 20px;
  }

  .page-title {
    font-size: 18px;
  }
}
</style>
