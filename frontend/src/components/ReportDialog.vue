<template>
  <el-dialog
    v-model="dialogVisible"
    title="举报内容"
    width="500px"
    @closed="handleClose"
  >
    <div class="report-dialog-content">
      <p class="report-item-info">您正在举报: {{ itemTitle }}</p>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="submitReport" label-position="top">
        <el-form-item label="举报原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择举报原因" style="width: 100%">
            <el-option label="垃圾广告" value="SPAM" />
            <el-option label="侮辱谩骂" value="ABUSE" />
            <el-option label="色情内容" value="PORNOGRAPHY" />
            <el-option label="政治敏感" value="POLITICAL" />
            <el-option label="违法信息" value="ILLEGAL" />
            <el-option label="其他原因" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述具体情况..."
            :maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="submitting">提交举报</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import apiClient from '@/utils/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  itemId: {
    type: Number,
    default: null
  },
  itemType: {
    type: String,
    default: ''
  },
  itemTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'submitted'])

// 本地状态
const dialogVisible = ref(props.visible)
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = ref({
  reason: '',
  description: ''
})

// 表单校验规则
const rules: FormRules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写详细说明', trigger: 'blur' },
    { min: 10, max: 500, message: '详细说明长度应在10到500个字符之间', trigger: 'blur' }
  ]
}

// 监听外部visible属性变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

// 监听本地dialogVisible变化，同步到外部
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
  if (!newVal) {
    resetForm()
  }
})

// 关闭对话框时重置表单
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.value = {
    reason: '',
    description: ''
  }
  formRef.value?.resetFields()
}

// 提交举报
const submitReport = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true

    try {
      const response = await apiClient.post('/reports', {
        itemId: props.itemId,
        itemType: props.itemType,
        reason: form.value.reason,
        description: form.value.description
      })

      if (response.data.success) {
        ElMessage.success('举报提交成功，我们会尽快处理')
        dialogVisible.value = false
        emit('submitted', response.data.data)
      } else {
        ElMessage.error(response.data.message || '举报提交失败')
      }
    } catch (error: any) {
      console.error('举报提交失败:', error)
      ElMessage.error(error.response?.data?.message || '举报提交失败，请稍后再试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.report-dialog-content {
  padding: 0 10px;
}

.report-item-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}
</style>
