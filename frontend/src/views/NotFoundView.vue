<template>
  <div class="not-found-container">
    <div class="content-wrapper">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-message">抱歉，您访问的页面不存在或已被移除</p>
      <div class="action-buttons">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">回到首页</el-button>
      </div>
      <div class="illustration">
        <img src="../assets/images/404.svg" alt="页面未找到" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.content-wrapper {
  max-width: 800px;
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #409EFF;
  line-height: 1.2;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 32px;
  color: #303133;
  margin-bottom: 16px;
}

.error-message {
  font-size: 18px;
  color: #606266;
  margin-bottom: 30px;
}

.action-buttons {
  margin-bottom: 40px;
}

.action-buttons .el-button {
  margin: 0 10px;
  padding: 12px 24px;
}

.illustration {
  max-width: 400px;
  margin: 0 auto;
}

.illustration img {
  max-width: 100%;
  height: auto;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 100px;
  }

  .error-title {
    font-size: 28px;
  }

  .error-message {
    font-size: 16px;
  }
}
</style>
