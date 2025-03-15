<template>
  <main-layout>
    <div class="profile-container">
      <el-row :gutter="20">
        <!-- 侧边栏导航 -->
        <el-col :xs="24" :sm="6" :md="5" :lg="4">
          <el-card class="profile-sidebar" shadow="never">
            <div class="user-info">
              <el-upload
                class="avatar-uploader"
                action="/api/users/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :size="100" :src="userAvatar" class="avatar-image" :shape="'square'">
                  <el-icon v-if="!userAvatar"><UserIcon /></el-icon>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Upload /></el-icon>
                </div>
              </el-upload>
              <div class="upload-hint">点击更换头像</div>
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
              <el-menu-item index="/profile/my-lost-items">
                <el-icon><Search /></el-icon>
                <span>我的寻物</span>
              </el-menu-item>
              <el-menu-item index="/profile/my-found-items">
                <el-icon><Collection /></el-icon>
                <span>我的招领</span>
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
  Search,
  Collection,
  Setting,
  Upload,
  Phone
} from '@element-plus/icons-vue'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useUserStore, type User } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const isLoaded = ref(false)
const loading = ref(false)

// 计算属性
const currentUser = computed(() => userStore.user || {} as User)
const userAvatar = computed(() => (currentUser.value?.avatar as string) || '')
const activeMenu = computed(() => route.path)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 头像上传处理
const handleAvatarSuccess = (response: any) => {
  if (response.success) {
    ElMessage.success('头像上传成功')
    userStore.setUser({
      ...currentUser.value,
      avatar: response.data.url
    })
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 上传前验证
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 加载用户资料
const loadUserData = async () => {
  if (loading.value) return

  loading.value = true
  try {
    // If we don't have user data or it's been too long since the last fetch
    if (!userStore.user ||
        (userStore.lastFetch && Date.now() - userStore.lastFetch > 5 * 60 * 1000)) {
      // Fetch fresh user data
      const result = await userStore.fetchCurrentUser()
      if (!result.success) {
        ElMessage.warning('获取用户信息失败，请刷新页面重试')
      }
    }
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
  margin: 0 0 20px 0;
}

.user-info {
  text-align: center;
  padding: 20px 0;
  background-color: #f9f9f9;
  border-radius: 10px;
  margin: 0 10px 20px 10px;
}

.avatar-uploader {
  display: inline-block;
  position: relative;
  cursor: pointer;
  margin-bottom: 10px;
}

.avatar-image {
  transition: all 0.3s;
  border-radius: 8px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  width: 100px !important;
  height: 100px !important;
}

.avatar-uploader:hover .avatar-image {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15) !important;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.upload-hint {
  font-size: 13px;
  color: #909399;
  margin-top: 10px;
  text-align: center;
}

.profile-menu {
  border-right: none;
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

