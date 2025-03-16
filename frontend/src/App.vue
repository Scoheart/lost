<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

const isDev = import.meta.env.DEV
const envName = import.meta.env.MODE
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

onMounted(async () => {
  // 如果有token，尝试获取用户信息
  if (userStore.token) {
    await userStore.fetchCurrentUser()
  }
})
</script>

<template>
  <!-- Debug information - only visible in development -->
  <div v-if="isDev" class="env-debug">
    <p>Environment: {{ envName }}</p>
    <p>API URL: {{ apiBaseUrl }}</p>
  </div>

  <!-- Rest of your app -->
  <RouterView />
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  height: 100%;
  width: 100%;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.text-center {
  text-align: center;
}

.mt-20 {
  margin-top: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.card-hover:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.env-debug {
  position: fixed;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.75);
  color: lime;
  font-family: monospace;
  padding: 8px;
  font-size: 12px;
  z-index: 9999;
  border-top-left-radius: 4px;
}
</style>
