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
            <el-menu mode="horizontal" :router="true" :default-active="activeRoute" class="menu" :ellipsis="false">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/announcements">社区公告</el-menu-item>
              <el-menu-item index="/lost-items">寻物启事</el-menu-item>
              <el-menu-item index="/found-items">失物招领</el-menu-item>
            </el-menu>
          </div>

          <div class="user-actions">
            <template v-if="authReady">
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
                <el-link
                  type="info"
                  :underline="false"
                  class="admin-link"
                  @click="$router.push('/admin/login')"
                >
                  管理员入口
                </el-link>
              </template>
            </template>
            <template v-else>
              <el-skeleton style="width: 150px; height: 32px" animated />
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
            <p><a href="/announcements">社区公告</a></p>
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
const authReady = computed(() => userStore.initialized)

// 计算属性
const isLoggedIn = computed(() => userStore.isAuthenticated)
const isAdmin = computed(() => userStore.isAdmin)
const user = computed(() => userStore.userProfile)
const userAvatar = computed(() => user.value?.avatar || '')
const userInitials = computed(() => {
  return user.value?.username ? user.value.username.substring(0, 2).toUpperCase() : '用户'
})
const currentYear = computed(() => new Date().getFullYear())
const activeRoute = computed(() => router.currentRoute.value.path)

// 监听登录状态变化
watch(() => userStore.token, (newToken, oldToken) => {
  console.log('Token changed:', !!newToken)
  // 只在token发生实际变化且没有用户信息时获取用户数据
  if (newToken && !userStore.user && !userStore.loading && newToken !== oldToken) {
    userStore.fetchCurrentUser()
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
  overflow-x: auto;
}

.menu {
  border-bottom: none;
  white-space: nowrap;
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

.admin-link {
  margin-left: 10px;
  font-size: 14px;
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
    justify-content: flex-start;
  }

  .el-menu.el-menu--horizontal {
    border-bottom: none;
  }

  .menu :deep(.el-menu-item) {
    padding: 0 15px;
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

  .admin-link {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>
