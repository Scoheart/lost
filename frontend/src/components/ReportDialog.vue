<template>
  <el-dialog
    v-model="dialogVisible"
    title="举报内容"
    width="500px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="reportForm"
      :rules="rules"
      label-position="top"
      class="report-form"
    >
      <el-form-item label="举报类型" prop="type">
        <el-input v-model="typeText" disabled />
      </el-form-item>

      <el-form-item label="举报对象" prop="title">
        <el-input v-model="reportedItemTitle" disabled />
      </el-form-item>

      <el-form-item label="举报原因" prop="reason">
        <el-select
          v-model="selectedReasonTemplate"
          placeholder="选择常见举报原因"
          @change="handleReasonTemplateChange"
          style="width: 100%; margin-bottom: 10px"
        >
          <el-option label="请选择举报原因" value="" />
          <el-option label="违规/不适当内容" value="违规/不适当内容" />
          <el-option label="骚扰/欺诈行为" value="骚扰/欺诈行为" />
          <el-option label="虚假信息" value="虚假信息" />
          <el-option label="广告/垃圾信息" value="广告/垃圾信息" />
          <el-option label="侵犯隐私" value="侵犯隐私" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>

        <el-input
          v-model="reportForm.reason"
          type="textarea"
          :rows="4"
          placeholder="请详细描述举报原因（最少5个字，最多500个字）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-alert
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #title>
          提交举报须知
        </template>
        <template #default>
          <p>请确保您提供的举报理由真实、客观，恶意举报可能导致账号受限。</p>
          <p>举报将由社区管理员审核处理，审核结果不会通知举报者。</p>
        </template>
      </el-alert>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="submitting">
          提交举报
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const userStore = useUserStore()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  itemType: {
    type: String,
    required: true,
    validator: (value) => ['LOST_ITEM', 'FOUND_ITEM', 'COMMENT'].includes(value)
  },
  itemId: {
    type: [String, Number],
    required: true
  },
  itemTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'submitted'])

// 表单ref
const formRef = ref(null)

// 对话框可见性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 举报表单
const reportForm = ref({
  reportType: props.itemType,
  reportedItemId: props.itemId,
  reason: ''
})

// 选中的原因模板
const selectedReasonTemplate = ref('')

// 提交中状态
const submitting = ref(false)

// 举报类型文本
const typeText = computed(() => {
  switch (props.itemType) {
    case 'LOST_ITEM':
      return '寻物启事'
    case 'FOUND_ITEM':
      return '失物招领'
    case 'COMMENT':
      return '留言评论'
    default:
      return '未知类型'
  }
})

// 被举报内容标题
const reportedItemTitle = computed(() => {
  if (props.itemType === 'COMMENT') {
    return `评论ID: ${props.itemId}`
  }
  return props.itemTitle || `ID: ${props.itemId}`
})

// 表单验证规则
const rules = {
  reason: [
    { required: true, message: '请填写举报原因', trigger: 'blur' },
    { min: 5, max: 500, message: '举报原因长度必须在5到500个字符之间', trigger: 'blur' }
  ]
}

// 监听属性变化，更新表单
watch(
  () => [props.itemType, props.itemId],
  ([newType, newId]) => {
    reportForm.value.reportType = newType
    reportForm.value.reportedItemId = newId
  }
)

// 处理原因模板变更
function handleReasonTemplateChange(template) {
  if (template) {
    if (reportForm.value.reason) {
      reportForm.value.reason = `${template}：${reportForm.value.reason}`
    } else {
      reportForm.value.reason = template
    }
  }
}

// 提交举报
async function submitReport() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!userStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        dialogVisible.value = false
        return
      }

      submitting.value = true

      try {
        const response = await api.post('/reports', reportForm.value)

        if (response.data.success) {
          ElMessage.success('举报提交成功，感谢您的反馈')
          emit('submitted', response.data.data)
          dialogVisible.value = false
        } else {
          ElMessage.error(response.data.message || '举报提交失败')
        }
      } catch (error) {
        console.error('提交举报失败:', error)
        if (error.response?.data?.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('举报提交失败，请稍后再试')
        }
      } finally {
        submitting.value = false
      }
    }
  })
}

// 对话框关闭时清空表单
function handleClosed() {
  reportForm.value.reason = ''
  selectedReasonTemplate.value = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.report-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  line-height: 1.2;
}

.report-form :deep(.el-form-item) {
  margin-bottom: 15px;
}
</style>
