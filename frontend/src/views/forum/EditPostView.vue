<template>
  <div class="edit-post-view">
    <MainLayout>
      <div class="page-header">
        <h1>{{ isEditing ? '编辑帖子' : '发布新帖子' }}</h1>
        <p>{{ isEditing ? '编辑您的帖子内容' : '在邻里论坛分享您的想法、问题或经验' }}</p>
      </div>

      <el-form
        ref="formRef"
        :model="postForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
        class="post-form"
      >
        <el-form-item prop="title" label="标题">
          <el-input
            v-model="postForm.title"
            placeholder="请输入帖子标题（2-100个字符）"
            :maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item prop="content" label="内容">
          <el-input
            v-model="postForm.content"
            type="textarea"
            placeholder="请输入帖子内容（5-5000个字符）"
            :rows="10"
            :maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" native-type="submit" :loading="submitting">
              {{ isEditing ? '保存修改' : '发布帖子' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </MainLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { usePostsStore } from '@/stores/posts'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/components/layout/MainLayout.vue'

const router = useRouter()
const route = useRoute()
const postsStore = usePostsStore()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 获取帖子ID（如果存在）
const postId = computed(() => {
  const id = route.params.id
  return id ? parseInt(id as string) : null
})

// 是否是编辑模式
const isEditing = computed(() => !!postId.value)

// 表单数据
const postForm = reactive({
  title: '',
  content: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度必须在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 5, max: 5000, message: '内容长度必须在5-5000个字符之间', trigger: 'blur' }
  ]
})

// 如果是编辑模式，加载帖子数据
onMounted(async () => {
  if (isEditing.value && postId.value) {
    try {
      const post = await postsStore.fetchPostById(postId.value)

      // 确保只有帖子作者可以编辑
      if (!postsStore.isPostOwner) {
        ElMessage.error('您无权编辑该帖子')
        router.push('/forum')
        return
      }

      // 填充表单数据
      postForm.title = post.title
      postForm.content = post.content
    } catch (error) {
      console.error('获取帖子失败:', error)
      ElMessage.error('获取帖子失败，请稍后再试')
      router.push('/forum')
    }
  }
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 检查用户是否已登录
    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    submitting.value = true

    try {
      if (isEditing.value && postId.value) {
        // 更新帖子
        await postsStore.updatePost(postId.value, {
          title: postForm.title,
          content: postForm.content
        })
        ElMessage.success('帖子更新成功')
        router.push(`/forum/${postId.value}`)
      } else {
        // 创建新帖子
        const post = await postsStore.createPost({
          title: postForm.title,
          content: postForm.content
        })
        ElMessage.success('帖子发布成功')
        router.push(`/forum/${post.id}`)
      }
    } catch (error) {
      console.error('保存帖子失败:', error)
      ElMessage.error('保存帖子失败，请稍后再试')
    } finally {
      submitting.value = false
    }
  })
}

// 返回上一页或论坛首页
const goBack = () => {
  if (isEditing.value && postId.value) {
    if (postForm.title !== postsStore.currentPost?.title ||
        postForm.content !== postsStore.currentPost?.content) {
      ElMessageBox.confirm('您有未保存的更改，确定要离开吗？', '确认离开', {
        confirmButtonText: '离开',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        router.push(`/forum/${postId.value}`)
      }).catch(() => {
        // 用户取消离开，继续编辑
      })
    } else {
      router.push(`/forum/${postId.value}`)
    }
  } else {
    if (postForm.title || postForm.content) {
      ElMessageBox.confirm('您有未发布的内容，确定要离开吗？', '确认离开', {
        confirmButtonText: '离开',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        router.push('/forum')
      }).catch(() => {
        // 用户取消离开，继续编辑
      })
    } else {
      router.push('/forum')
    }
  }
}
</script>

<style scoped>
.edit-post-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.post-form {
  background-color: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

:deep(.el-textarea__inner) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  line-height: 1.6;
  resize: vertical;
}
</style>
