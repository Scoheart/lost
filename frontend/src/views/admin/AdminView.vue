<template>
  <div class="admin-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px" class="admin-sidebar">
        <div class="sidebar-header">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
          <h2>系统管理</h2>
        </div>

        <el-menu
          router
          :default-active="activeMenu"
          class="admin-menu"
        >
          <!-- 所有管理员可见的菜单项 -->
          <el-menu-item index="/admin/announcements">
            <el-icon><Notification /></el-icon>
            <span>公告管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/claims">
            <el-icon><Connection /></el-icon>
            <span>认领记录</span>
          </el-menu-item>

          <el-menu-item index="/admin/reports">
            <el-icon><Warning /></el-icon>
            <span>举报管理</span>
          </el-menu-item>

          <!-- 小区管理员和系统管理员可见 -->
          <el-menu-item index="/admin/residents">
            <el-icon><User /></el-icon>
            <span>居民管理</span>
          </el-menu-item>

          <!-- 只有系统管理员可见 -->
          <el-menu-item index="/admin/users" v-if="userStore.isSysAdmin">
            <el-icon><Setting /></el-icon>
            <span>系统用户管理</span>
          </el-menu-item>

          <!-- 返回前台 -->
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
        </el-menu>

        <!-- 管理员信息 -->
        <div class="admin-info">
          <el-avatar :size="40" :src="userAvatar">
            {{ userInitials }}
          </el-avatar>
          <div class="admin-info-text">
            <div class="admin-name">{{ userStore.user?.username }}</div>
            <div class="admin-role">{{ adminRoleText }}</div>
          </div>
          <el-dropdown trigger="click">
            <el-icon class="admin-actions-icon"><Setting /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <!-- 开发环境警告 -->
        <div v-if="isDev && allowAdminAccess" class="dev-mode-banner">
          <el-alert
            title="开发环境: 管理员权限已绕过"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              当前处于开发环境，管理员权限检查已绕过。这仅适用于开发测试。
            </template>
          </el-alert>
        </div>

        <!-- 管理员角色提示 -->
        <div v-if="isRoleSpecificPage" class="role-warning">
          <el-alert
            :title="roleWarningTitle"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              {{ roleWarningMessage }}
            </template>
          </el-alert>
        </div>

        <!-- 路由视图 -->
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Notification,
  Connection,
  Warning,
  User,
  Setting,
  SwitchButton,
  HomeFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 开发环境变量
const isDev = import.meta.env.DEV
const allowAdminAccess = import.meta.env.VITE_ALLOW_ADMIN_ACCESS === 'true'

// 计算属性
const activeMenu = computed(() => route.path)
const userInitials = computed(() => {
  const username = userStore.user?.username || ''
  return username.substring(0, 2).toUpperCase()
})
const userAvatar = computed(() => userStore.user?.avatar || '')

const adminRoleText = computed(() => {
  if (userStore.isSysAdmin) {
    return '系统管理员'
  } else if (userStore.isCommunityAdmin) {
    return '小区管理员'
  }
  return '管理员'
})

// 判断当前页面是否为角色特定页面
const isRoleSpecificPage = computed(() => {
  // 检查是否在系统管理员专有页面但不是系统管理员
  return (
    route.path === '/admin/users' &&
    !userStore.isSysAdmin &&
    !(isDev && allowAdminAccess)
  )
})

// 角色警告信息
const roleWarningTitle = computed(() => {
  if (route.path === '/admin/users') {
    return '系统用户管理仅限系统管理员访问'
  }
  return '权限提示'
})

const roleWarningMessage = computed(() => {
  if (route.path === '/admin/users') {
    return '您当前的角色是小区管理员，无法访问系统用户管理功能。如需访问，请联系系统管理员提升您的权限。'
  }
  return '您没有足够的权限访问此页面'
})

// 方法
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出管理员登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    ElMessage.success('已安全退出登录')
    router.push('/admin/login')
  } catch (e) {
    // 用户取消，不做任何操作
  }
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.el-container {
  height: 100%;
  width: 100%;
}

.admin-sidebar {
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  transition: width 0.3s;
  overflow-x: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.sidebar-header h2 {
  margin: 0;
  color: #fff;
  font-size: 18px;
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
  background-color: transparent;
  flex: 1;
}

.admin-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #263445;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: #263445;
}

.admin-main {
  padding: 20px;
  background-color: #f5f7f9;
  overflow-y: auto;
}

.admin-info {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-info-text {
  margin-left: 10px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-name {
  font-size: 14px;
  color: #fff;
}

.admin-role {
  font-size: 12px;
  color: #8f9bb3;
}

.admin-actions-icon {
  color: #bfcbd9;
  font-size: 18px;
  cursor: pointer;
}

.dev-mode-banner {
  margin-bottom: 20px;
}

.role-warning {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .admin-sidebar {
    position: absolute;
    z-index: 10;
    height: 100%;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .admin-sidebar.expanded {
    transform: translateX(0);
  }
}
</style>
