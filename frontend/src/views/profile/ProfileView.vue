<template>
  <main-layout>
    <div class="profile-container">
      <el-row :gutter="20">
        <!-- 侧边栏导航 -->
        <el-col :xs="24" :sm="6" :md="5" :lg="4">
          <el-card class="profile-sidebar" shadow="never">
            <div class="user-info">
              <el-avatar :size="80" :src="userAvatar">
                <el-icon v-if="!userAvatar"><UserIcon /></el-icon>
              </el-avatar>
              <h3>{{ currentUser.username }}</h3>
              <p>{{ currentUser.email }}</p>
            </div>

            <el-divider />

            <el-menu
              :default-active="activeMenu"
              class="profile-menu"
              router
            >
              <el-menu-item index="/profile/settings">
                <el-icon><Setting /></el-icon>
                <span>个人设置</span>
              </el-menu-item>
              <el-menu-item index="/profile/my-posts">
                <el-icon><Document /></el-icon>
                <span>我的发布</span>
              </el-menu-item>
              <el-menu-item index="/profile/my-lost-items">
                <el-icon><Search /></el-icon>
                <span>我的寻物</span>
              </el-menu-item>
              <el-menu-item index="/profile/my-found-items">
                <el-icon><Collection /></el-icon>
                <span>我的招领</span>
              </el-menu-item>
              <el-menu-item index="/profile/messages">
                <el-icon><ChatDotRound /></el-icon>
                <span>我的消息</span>
                <el-badge v-if="unreadMessageCount > 0" :value="unreadMessageCount" class="menu-badge" />
              </el-menu-item>
            </el-menu>
          </el-card>
        </el-col>

        <!-- 内容区域 -->
        <el-col :xs="24" :sm="18" :md="19" :lg="20">
          <router-view v-if="isLoaded" />
          <div v-else class="loading-container">
            <el-skeleton style="width: 100%" animated>
              <template #template>
                <div style="padding: 20px">
                  <el-skeleton-item variant="p" style="width: 60%; height: 40px" />
                  <div style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0">
                    <el-skeleton-item variant="text" style="width: 30%" />
                    <el-skeleton-item variant="text" style="width: 30%" />
                  </div>
                  <el-skeleton-item variant="p" style="width: 100%; height: 300px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </el-col>
      </el-row>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  User as UserIcon,
  Document,
  Search,
  Collection,
  ChatDotRound,
  Setting
} from '@element-plus/icons-vue'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useUserStore, type User } from '@/stores/user'
import { useMessageStore } from '@/stores/messages'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()
const isLoaded = ref(false)
const loading = ref(false)

// 计算属性
const currentUser = computed(() => userStore.user || {} as User)
const userAvatar = computed(() => (currentUser.value?.avatar as string) || '')
const unreadMessageCount = computed(() => messageStore.unreadCount || 0)
const activeMenu = computed(() => route.path)

// 加载用户资料并获取未读消息
const loadUserData = async () => {
  if (loading.value) return

  loading.value = true
  try {
    // 加载未读消息数量
    await messageStore.fetchUnreadCount()
    isLoaded.value = true
  } catch (error) {
    console.error('Failed to load user data:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 生命周期钩子
onMounted(async () => {
  console.log('[ProfileView] Component mounted')
  // 路由守卫已确保只有认证用户才能访问此页面
  await loadUserData()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-sidebar {
  margin-bottom: 20px;
}

.user-info {
  text-align: center;
  padding: 20px 0;
}

.user-info h3 {
  margin: 15px 0 5px;
  font-size: 18px;
  font-weight: 600;
}

.user-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-menu {
  border-right: none;
}

.menu-badge {
  margin-left: 10px;
}

.loading-container {
  min-height: 400px;
}

@media (max-width: 768px) {
  .profile-sidebar {
    margin-bottom: 20px;
  }

  .user-info {
    padding: 15px 0;
  }
}
</style>

