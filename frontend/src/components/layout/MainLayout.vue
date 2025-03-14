<!-- components/layout/MainLayout.vue -->
<template>
  <div class="main-layout">
    <el-container>
      <el-header height="60px">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            <span>住宅小区互助寻物系统</span>
          </div>

          <div class="header-links">
            <el-menu mode="horizontal" :router="true" :default-active="activeRoute" class="menu">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/announcements">社区公告</el-menu-item>
              <el-menu-item index="/lost-items">寻物启事</el-menu-item>
              <el-menu-item index="/found-items">失物招领</el-menu-item>
              <el-menu-item index="/forum">邻里论坛</el-menu-item>
            </el-menu>
          </div>

          <div class="user-actions">
            <template v-if="isLoggedIn">
              <el-dropdown trigger="click">
                <div class="user-avatar">
                  <el-avatar :size="32" :src="userAvatar">{{ userInitials }}</el-avatar>
                  <span>{{ user?.username }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="$router.push('/profile')">
                      <el-icon><User /></el-icon> 个人中心
                    </el-dropdown-item>
                    <el-dropdown-item @click="$router.push('/profile/my-posts')">
                      <el-icon><Document /></el-icon> 我的发布
                    </el-dropdown-item>
                    <el-dropdown-item v-if="isAdmin" @click="$router.push('/admin')">
                      <el-icon><Setting /></el-icon> 管理中心
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleLogout" divided>
                      <el-icon><SwitchButton /></el-icon> 退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="$router.push('/login')">登录</el-button>
              <el-button @click="$router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </el-header>

      <el-main>
        <div class="main-content">
          <slot></slot>
        </div>
      </el-main>

      <el-footer height="80px">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>住宅小区互助寻物系统，让邻里互助更便捷</p>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <p>邮箱: contact@example.com</p>
            <p>电话: 010-12345678</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <p><a href="/lost-items">寻物启事</a> | <a href="/found-items">失物招领</a></p>
            <p><a href="/forum">邻里论坛</a> | <a href="/announcements">社区公告</a></p>
          </div>
        </div>
        <div class="copyright">© {{ currentYear }} 住宅小区互助寻物系统 - 版权所有</div>
      </el-footer>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, User, Document, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 计算属性
const isLoggedIn = computed(() => {
  const authenticated = userStore.isAuthenticated
  console.log('Authentication status:', authenticated, 'Token:', !!userStore.token, 'User:', !!userStore.user)
  return authenticated
})
const isAdmin = computed(() => userStore.isAdmin)
const user = computed(() => userStore.userProfile)
const userAvatar = computed(() => user.value?.avatar || '')
const userInitials = computed(() => {
  return user.value?.username ? user.value.username.substring(0, 2).toUpperCase() : '用户'
})
const currentYear = computed(() => new Date().getFullYear())
const activeRoute = computed(() => router.currentRoute.value.path)

// 监听登录状态变化
watch(() => userStore.token, (newToken) => {
  console.log('Token changed:', !!newToken)
  if (newToken && !userStore.user) {
    userStore.fetchCurrentUser()
  }
}, { immediate: true })

// 生命周期钩子
onMounted(async () => {
  console.log('MainLayout mounted, token:', !!userStore.token, 'user:', !!userStore.user)
  // 如果有token但没有用户信息，尝试获取用户信息
  if (userStore.token && !userStore.user) {
    await userStore.fetchCurrentUser()
  }
})

// 方法
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      userStore.logout()
      ElMessage.success('已安全退出登录')
      router.push('/')
    })
    .catch(() => {
      // 取消退出登录
    })
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-container {
  min-height: 100vh;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.logo .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.header-links {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu {
  border-bottom: none;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-avatar span {
  margin-left: 8px;
}

.main-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.el-footer {
  background-color: #f7f7f7;
  padding: 20px;
  color: #606266;
}

.footer-content {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  padding: 0 20px;
}

.footer-section h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #303133;
}

.footer-section p {
  margin: 4px 0;
  font-size: 14px;
}

.footer-section a {
  color: #409eff;
  text-decoration: none;
}

.footer-section a:hover {
  text-decoration: underline;
}

.copyright {
  text-align: center;
  font-size: 12px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    padding: 10px;
    height: auto;
  }

  .el-header {
    height: auto !important;
    padding: 0;
  }

  .logo {
    margin-bottom: 10px;
  }

  .header-links {
    width: 100%;
    overflow-x: auto;
  }

  .user-actions {
    margin-top: 10px;
    width: 100%;
    justify-content: center;
  }

  .footer-content {
    flex-direction: column;
    align-items: center;
  }

  .footer-section {
    margin-bottom: 20px;
    text-align: center;
  }
}
</style>
