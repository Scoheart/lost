<template>
  <main-layout>
    <div class="create-post-container">
      <div class="page-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/forum' }">邻里论坛</el-breadcrumb-item>
          <el-breadcrumb-item>发布帖子</el-breadcrumb-item>
        </el-breadcrumb>

        <h1>发布帖子</h1>
        <p class="header-desc">在邻里论坛分享您的想法、问题或建议</p>
      </div>

      <el-card class="post-form-card">
        <el-form
          ref="postFormRef"
          :model="postForm"
          :rules="postRules"
          label-position="top"
          @submit.prevent="submitPost"
        >
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="postForm.title"
              placeholder="请输入帖子标题（5-100字）"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-select
              v-model="postForm.category"
              placeholder="请选择帖子分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.value"
                :label="category.label"
                :value="category.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <div class="editor-toolbar">
              <el-button-group>
                <el-button @click="insertMarkdown('**', '**')" title="加粗">
                  <el-icon><Bold /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('*', '*')" title="斜体">
                  <el-icon><Italic /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('\n> ', '')" title="引用">
                  <el-icon><QuoteLeft /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('\n```\n', '\n```')" title="代码块">
                  <el-icon><Postcard /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('\n- ', '')" title="无序列表">
                  <el-icon><List /></el-icon>
                </el-button>
                <el-button @click="insertOrderedList" title="有序列表">
                  <el-icon><Finished /></el-icon>
                </el-button>
                <el-button @click="insertLink" title="插入链接">
                  <el-icon><Link /></el-icon>
                </el-button>
              </el-button-group>
            </div>

            <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="12"
              placeholder="请输入帖子内容（支持Markdown格式）"
              maxlength="10000"
              show-word-limit
              resize="vertical"
              ref="contentInputRef"
            />

            <div class="editor-preview">
              <el-divider>预览</el-divider>
              <div class="preview-content" v-html="previewContent"></div>
            </div>
          </el-form-item>

          <el-form-item label="图片上传">
            <el-upload
              v-model:file-list="fileList"
              list-type="picture-card"
              :limit="9"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :on-change="handleFileChange"
              :auto-upload="false"
              :multiple="true"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="upload-tip">
                  支持jpg、png格式，单张不超过5MB，最多上传9张
                </div>
              </template>
            </el-upload>

            <el-dialog v-model="dialogVisible">
              <img w-full :src="dialogImageUrl" alt="Preview Image" />
            </el-dialog>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button
                @click="cancelCreate"
                :disabled="isSubmitting"
              >取消</el-button>
              <el-button
                type="primary"
                native-type="submit"
                :loading="isSubmitting"
              >发布帖子</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="posting-tips">
        <el-card shadow="never">
          <template #header>
            <div class="tips-header">
              <el-icon><InfoFilled /></el-icon>
              <span>发帖指南</span>
            </div>
          </template>
          <ul class="tips-list">
            <li>帖子标题应简明扼要，清晰表达主题</li>
            <li>内容应详细具体，如有必要可分段落</li>
            <li>请遵守社区规则，不发布违规内容</li>
            <li>尊重他人，保持友善的交流态度</li>
            <li>如有问题，建议提供详细的背景信息</li>
          </ul>
        </el-card>
      </div>
    </div>
  </main-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { InfoFilled, Bold, Italic, QuoteLeft, Postcard, List, Finished, Link, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useForumStore } from '@/stores/forum'
import { useUserStore } from '@/stores/user'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

const router = useRouter()
const route = useRoute()
const forumStore = useForumStore()
const userStore = useUserStore()
const postFormRef = ref<FormInstance>()
const contentInputRef = ref()
const isSubmitting = ref(false)

// 表单数据
const postForm = ref({
  title: '',
  category: '',
  content: '',
  images: [] as string[],
  status: 'published'
})

// 表单验证规则
const postRules = ref<FormRules>({
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在5-100个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择帖子分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, max: 10000, message: '内容长度应在10-10000个字符之间', trigger: 'blur' }
  ]
})

// 文件上传
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 编辑模式判断
const isEditing = computed(() => route.path.includes('/edit/'))
const postId = computed(() => route.params.id)

// 状态
const submitting = ref(false)
const savingDraft = ref(false)

// 分类
const categories = [
  { label: '社区通知', value: 'community-notice' },
  { label: '活动组织', value: 'activity' },
  { label: '失物招领', value: 'lost-found' },
  { label: '闲置交易', value: 'trading' },
  { label: '业主投诉', value: 'complaint' },
  { label: '业主提问', value: 'question' },
  { label: '民生话题', value: 'livelihood' },
  { label: '装修经验', value: 'decoration' },
  { label: '邻里互助', value: 'help' },
  { label: '安全防范', value: 'security' },
  { label: '小区建设', value: 'construction' },
  { label: '其他话题', value: 'other' }
]

// 计算属性
const previewContent = computed(() => {
  if (!postForm.value.content) {
    return '<div class="empty-preview">预览区域</div>'
  }

  // 使用marked将Markdown转换为HTML，并使用DOMPurify进行安全处理
  const rawHtml = marked(postForm.value.content)
  return DOMPurify.sanitize(rawHtml)
})

// 方法
// 获取帖子详情（编辑模式）
const fetchPostDetail = async () => {
  if (!isEditing.value) return

  try {
    const post = await forumStore.fetchPostDetail(postId.value)

    if (!post) {
      ElMessage.error('帖子不存在或已被删除')
      router.push('/forum')
      return
    }

    // 检查是否有权限编辑
    if (post.userId !== userStore.currentUser?.id && userStore.currentUser?.role !== 'admin') {
      ElMessage.error('您没有权限编辑此帖子')
      router.push(`/forum/${postId.value}`)
      return
    }

    // 填充表单数据
    postForm.value.title = post.title || ''
    postForm.value.category = post.category || ''
    postForm.value.content = post.content || ''
    postForm.value.status = post.status || 'published'

    // 处理图片
    if (post.images && post.images.length > 0) {
      postForm.value.images = [...post.images]

      // 转换为fileList格式
      fileList.value = post.images.map((url, index) => ({
        name: `image-${index}.jpg`,
        url
      }))
    }
  } catch (error) {
    console.error('Failed to fetch post details:', error)
    ElMessage.error('获取帖子详情失败')
    router.push('/forum')
  }
}

// 图片上传相关方法
const handleRemove = (file, fileList) => {
  // 从images数组中删除对应的url
  if (file.url) {
    const index = postForm.value.images.indexOf(file.url)
    if (index !== -1) {
      postForm.value.images.splice(index, 1)
    }
  }
}

const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url || URL.createObjectURL(file.raw)
  dialogVisible.value = true
}

const handleFileChange = (file, fileList) => {
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning(`图片 ${file.name} 大小超过5MB限制`)
    // 从fileList中移除
    const index = fileList.indexOf(file)
    if (index !== -1) {
      fileList.splice(index, 1)
    }
  }
}

// 处理文件上传
const uploadFiles = async () => {
  const filesToUpload = fileList.value.filter(file => !file.url && file.raw)

  if (filesToUpload.length === 0) {
    return postForm.value.images
  }

  try {
    // 这里假设有一个上传文件的API，实际使用时需替换
    const uploadPromises = filesToUpload.map(file => {
      const formData = new FormData()
      formData.append('file', file.raw)

      return forumStore.uploadImage(formData)
    })

    const results = await Promise.all(uploadPromises)

    // 合并已有图片和新上传的图片URL
    const uploadedUrls = results.map(res => res.url).filter(Boolean)
    const existingUrls = postForm.value.images.filter(url => url)

    return [...existingUrls, ...uploadedUrls]
  } catch (error) {
    console.error('Failed to upload images:', error)
    ElMessage.error('图片上传失败，请重试')
    throw error
  }
}

// 保存草稿
const saveAsDraft = async () => {
  try {
    savingDraft.value = true

    // 进行表单验证，但不对内容做最小长度要求
    const isValid = await postFormRef.value.validate((valid, fields) => {
      // 只验证标题和分类
      if (fields && fields.content && fields.content[0].message.includes('10到')) {
        return true
      }
      return valid
    })

    if (!isValid) return

    // 上传图片
    const imageUrls = await uploadFiles()

    const postData = {
      title: postForm.value.title,
      category: postForm.value.category,
      content: postForm.value.content,
      images: imageUrls,
      status: 'draft'
    }

    let result
    if (isEditing.value) {
      result = await forumStore.updatePost(postId.value, postData)
    } else {
      result = await forumStore.createPost(postData)
    }

    if (result.success) {
      ElMessage.success('草稿保存成功')
      if (!isEditing.value) {
        router.push(`/profile/posts`)
      }
    } else {
      ElMessage.error(result.message || '草稿保存失败')
    }
  } catch (error) {
    console.error('Failed to save draft:', error)
    ElMessage.error('草稿保存失败，请重试')
  } finally {
    savingDraft.value = false
  }
}

// 提交发布
const submitPost = async () => {
  try {
    submitting.value = true

    // 表单验证
    await postFormRef.value.validate()

    // 上传图片
    const imageUrls = await uploadFiles()

    const postData = {
      title: postForm.value.title,
      category: postForm.value.category,
      content: postForm.value.content,
      images: imageUrls,
      status: 'published'
    }

    let result
    if (isEditing.value) {
      result = await forumStore.updatePost(postId.value, postData)
    } else {
      result = await forumStore.createPost(postData)
    }

    if (result.success) {
      ElMessage.success(isEditing.value ? '帖子更新成功' : '帖子发布成功')
      if (isEditing.value) {
        router.push(`/forum/${postId.value}`)
      } else {
        router.push(`/forum/${result.data.id}`)
      }
    } else {
      ElMessage.error(result.message || (isEditing.value ? '更新失败' : '发布失败'))
    }
  } catch (error) {
    console.error('Failed to submit post:', error)
    ElMessage.error(isEditing.value ? '更新失败，请重试' : '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

// Markdown编辑器辅助方法
const insertMarkdown = (prefix, suffix) => {
  const textarea = contentInputRef.value.input.ref
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = postForm.value.content

  const selectedText = text.substring(start, end)
  const replacement = prefix + selectedText + suffix

  postForm.value.content = text.substring(0, start) + replacement + text.substring(end)

  // 重新设置光标位置
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(
      start + prefix.length,
      start + prefix.length + selectedText.length
    )
  })
}

const insertOrderedList = () => {
  const textarea = contentInputRef.value.input.ref
  const start = textarea.selectionStart
  const text = postForm.value.content

  // 添加有序列表
  postForm.value.content = text.substring(0, start) + '\n1. ' + text.substring(start)

  // 重新设置光标位置
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + 4, start + 4)
  })
}

const insertLink = () => {
  const textarea = contentInputRef.value.input.ref
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = postForm.value.content

  const selectedText = text.substring(start, end)
  const linkText = selectedText || '链接文本'
  const replacement = `[${linkText}](https://)`

  postForm.value.content = text.substring(0, start) + replacement + text.substring(end)

  // 重新设置光标位置，将光标放在URL部分
  const newCursorPos = start + linkText.length + 3

  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(newCursorPos, newCursorPos + 8)
  })
}

// 生命周期
onMounted(async () => {
  // 检查登录状态
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再发布帖子')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }

  if (isEditing.value) {
    await fetchPostDetail()
  }
})

// 取消创建
const cancelCreate = () => {
  if (postForm.value.title || postForm.value.content) {
    ElMessageBox.confirm(
      '确定要放弃编辑吗？未保存的内容将会丢失。',
      '放弃编辑',
      {
        confirmButtonText: '确定放弃',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    ).then(() => {
      router.push('/forum')
    }).catch(() => {
      // 用户选择继续编辑，不做任何操作
    })
  } else {
    router.push('/forum')
  }
}
</script>

<style scoped>
.create-post-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

.page-header {
  grid-column: 1 / -1;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 15px 0 10px;
  font-size: 24px;
}

.header-desc {
  color: #606266;
  margin-bottom: 20px;
}

.post-form-card {
  grid-column: 1;
}

.posting-tips {
  grid-column: 2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.tips-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.tips-header .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.tips-list {
  padding-left: 20px;
  margin: 10px 0;
}

.tips-list li {
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.5;
}

.editor-toolbar {
  margin-bottom: 10px;
}

.editor-preview {
  margin-top: 20px;
}

.preview-content {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f8f9fa;
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.preview-content h1,
.preview-content h2,
.preview-content h3 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.preview-content h1 {
  font-size: 2em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

.preview-content h2 {
  font-size: 1.5em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

.preview-content h3 {
  font-size: 1.25em;
}

.preview-content p,
.preview-content ul,
.preview-content ol {
  margin-top: 0;
  margin-bottom: 16px;
}

.preview-content ul,
.preview-content ol {
  padding-left: 2em;
}

.preview-content blockquote {
  margin: 0;
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
}

.preview-content pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 3px;
}

.preview-content code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
}

.preview-content pre code {
  padding: 0;
  margin: 0;
  font-size: 100%;
  background-color: transparent;
  border: 0;
}

.empty-preview {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
}

@media (max-width: 768px) {
  .create-post-container {
    grid-template-columns: 1fr;
  }

  .post-form-card,
  .posting-tips {
    grid-column: 1;
  }
}
</style>
