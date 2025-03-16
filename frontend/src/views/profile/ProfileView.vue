<template>
  <main-layout>
    <div class="profile-container">
      <el-row :gutter="20">
        <!-- 侧边栏导航 -->
        <el-col :xs="24" :sm="6" :md="5" :lg="4">
          <el-card class="profile-sidebar" shadow="never">
            <div class="user-info">
              <!-- 隐藏的文件输入框 -->
              <input
                type="file"
                ref="fileInputRef"
                accept="image/jpeg,image/png,image/gif"
                @change="handleFileSelect"
                style="display: none"
              />

              <!-- 点击此区域触发文件选择 -->
              <div class="avatar-uploader" @click="triggerFileInput">
                <el-avatar :size="100" :src="userAvatar" class="avatar-image" :shape="'square'">
                  <el-icon v-if="!userAvatar"><UserIcon /></el-icon>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Upload /></el-icon>
                </div>
              </div>
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
import { ElMessage, ElLoading } from 'element-plus'
import fileUploadService from '@/services/fileUploadService'

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

// 手动处理头像上传
const fileInputRef = ref<HTMLInputElement | null>(null)

// 触发文件选择对话框
const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

// 处理文件选择
const handleFileSelect = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0]

    // 验证文件
    if (!fileUploadService.validateFile(file, {
      allowedTypes: ['image/jpeg', 'image/png', 'image/gif'],
      maxSize: 2,
      showMessage: true
    })) {
      return
    }

    let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

    try {
      // 显示上传中提示
      loadingInstance = ElLoading.service({
        lock: true,
        text: '头像上传中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      // 上传头像
      const result = await fileUploadService.uploadAvatar(file)

      if (result.success && result.data && result.data.url) {
        ElMessage.success('头像上传成功')
        userStore.setUser({
          ...currentUser.value,
          avatar: result.data.url
        })
      } else {
        ElMessage.error(result.message || '头像上传失败')
      }
    } catch (error) {
      console.error('头像上传失败:', error)
      ElMessage.error(`头像上传失败: ${error}`)
    } finally {
      // 关闭上传提示
      if (loadingInstance) {
        loadingInstance.close()
      }

      // 清空文件输入框，以便可以重新选择同一文件
      input.value = ''
    }
  }
}

// 头像上传处理
const handleAvatarSuccess = (response: any) => {
  if (response.success && response.data && response.data.url) {
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
  return fileUploadService.validateFile(file, {
    allowedTypes: ['image/jpeg', 'image/png', 'image/gif'],
    maxSize: 2,
    showMessage: true
  })
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

