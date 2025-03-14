<template>
  <div class="admin-layout">
    <!-- 侧边导航栏 -->
    <div class="admin-sidebar">
      <div class="admin-logo">
        <h2>管理中心</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        router
        :collapse="isCollapse"
      >
        <el-menu-item index="/admin/announcements">
          <el-icon><Promotion /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/lost-items">
          <el-icon><Search /></el-icon>
          <template #title>寻物管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/found-items">
          <el-icon><Collection /></el-icon>
          <template #title>招领管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/claims">
          <el-icon><Connection /></el-icon>
          <template #title>认领管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <template #title>举报管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="admin-content">
      <!-- 头部导航栏 -->
      <div class="admin-header">
        <div class="header-left">
          <el-button @click="toggleSidebar" text>
            <el-icon :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/admin' }">管理中心</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentSection">{{ currentSection }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ adminName }}</span>
          <el-dropdown trigger="click" @command="handleCommand">
            <el-avatar :size="32" :icon="User" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容主体区 -->
      <div class="admin-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Promotion,
  Search,
  Collection,
  Connection,
  Warning,
  User,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)
const adminName = ref('管理员')

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 计算当前管理区域的标题
const currentSection = computed(() => {
  const path = route.path
  if (path.includes('/admin/announcements')) return '公告管理'
  if (path.includes('/admin/lost-items')) return '寻物管理'
  if (path.includes('/admin/found-items')) return '招领管理'
  if (path.includes('/admin/claims')) return '认领管理'
  if (path.includes('/admin/reports')) return '举报管理'
  if (path.includes('/admin/users')) return '用户管理'
  return ''
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理头像下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      confirmLogout()
      break
  }
}

// 确认登出
const confirmLogout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await userStore.logout()
      router.push('/login')
    })
    .catch(() => {
      // 用户取消操作，不做任何处理
    })
}

onMounted(async () => {
  // 获取管理员信息
  const user = userStore.currentUser
  if (user) {
    adminName.value = user.username || '管理员'
  }

  // 检查权限
  if (!userStore.hasAdminAccess) {
    router.push('/')
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.admin-sidebar {
  display: flex;
  flex-direction: column;
  background-color: #304156;
  color: white;
  transition: width 0.3s;
  width: var(--sidebar-width, 220px);
  height: 100%;
  z-index: 10;
}

.admin-logo {
  padding: 16px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.admin-menu {
  flex: 1;
  border-right: none;
  background-color: #304156;
}

:deep(.el-menu) {
  border-right: none !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #bfcbd9 !important;
}

:deep(.el-menu-item.is-active) {
  color: #409EFF !important;
  background-color: #263445 !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
}

.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left .el-button {
  margin-right: 15px;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-name {
  margin-right: 12px;
  font-size: 14px;
  color: #606266;
}

.admin-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    --sidebar-width: 64px;
  }
}
</style>
