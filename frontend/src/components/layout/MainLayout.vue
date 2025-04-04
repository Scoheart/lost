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
            <el-menu
              mode="horizontal"
              :router="true"
              :default-active="activeRoute"
              class="menu"
              :ellipsis="false"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/announcements">社区公告</el-menu-item>
              <el-menu-item index="/lost-items">寻物启事</el-menu-item>
              <el-menu-item index="/found-items">失物招领</el-menu-item>
              <el-menu-item index="/forum">邻居论坛</el-menu-item>
              <el-menu-item index="/claim-communications" v-if="isLoggedIn">认领交流</el-menu-item>
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
                <el-button
                  v-if="!isAuthenticated"
                  type="primary"
                  @click="$router.push('/login')"
                  class="login-btn"
                >
                  登录
                </el-button>
                <el-button @click="$router.push('/register')">注册</el-button>
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

      <el-footer>
        <div class="footer-content">
          <div class="footer-section about">
            <h3>关于我们</h3>
            <p>
              小区互助寻物系统旨在帮助小区内丢失物品的居民快速找回物品，也为拾到物品的居民提供一个归还途径。
            </p>
          </div>
          <div class="footer-section links">
            <h3>快速链接</h3>
            <p><router-link to="/lost-items">寻物启事</router-link></p>
            <p><router-link to="/found-items">失物招领</router-link></p>
            <p><router-link to="/forum">邻居论坛</router-link></p>
            <p><router-link to="/announcements">社区公告</router-link></p>
          </div>
        </div>
        <div class="copyright">
          <p>© {{ new Date().getFullYear() }} 小区互助寻物系统 - 保留所有权利</p>
        </div>
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
watch(
  () => userStore.token,
  (newToken, oldToken) => {
    console.log('Token changed:', !!newToken)
    // 只在token发生实际变化且没有用户信息时获取用户数据
    if (newToken && !userStore.user && !userStore.loading && newToken !== oldToken) {
      userStore.fetchCurrentUser()
    }
  },
)

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
  border-top: 1px solid #e9ecef;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-section {
  flex: 1;
  margin-right: 30px;
  max-width: 350px;
}

.footer-section:last-child {
  margin-right: 0;
}

.footer-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
  position: relative;
  padding-bottom: 10px;
}

.footer-section h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 2px;
  background-color: #409eff;
}

.footer-section p {
  margin: 10px 0;
  color: #606266;
  line-height: 1.6;
}

.footer-section a {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-section a:hover {
  color: #66b1ff;
  text-decoration: none;
}

.copyright {
  text-align: center;
  font-size: 14px;
  padding: 20px 0 0;
  margin-top: 30px;
  border-top: 1px solid #e0e0e0;
  color: #909399;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.copyright p {
  margin-bottom: 10px;
}

.admin-link {
  color: #909399;
  font-size: 14px;
  transition: color 0.3s ease;
}

.admin-link:hover {
  color: #409eff;
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
    padding: 0 20px;
  }

  .footer-section {
    margin-right: 0;
    margin-bottom: 30px;
    max-width: 100%;
    text-align: left;
  }

  .footer-section h3 {
    text-align: left;
  }

  .footer-section h3::after {
    left: 0;
    transform: none;
  }

  .copyright {
    padding: 20px;
    text-align: center;
    margin-top: 0;
  }

  .admin-link {
    display: block;
    margin: 10px auto 0;
  }
}
</style>
