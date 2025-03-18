<template>
  <el-tag
    :type="tagType"
    :effect="effect"
    :size="size"
    class="status-tag"
    :class="statusClass"
  >
    {{ statusText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// Define props with TypeScript types
interface Props {
  status: string
  itemType: 'lost' | 'found'
  effect?: 'plain' | 'light' | 'dark'
  size?: 'large' | 'default' | 'small'
}

const props = withDefaults(defineProps<Props>(), {
  effect: 'light',
  size: 'small'
})

// Display text for different statuses
const statusText = computed(() => {
  if (props.itemType === 'lost') {
    switch (props.status) {
      case 'pending': return '寻找中'
      case 'found': return '已找到'
      case 'closed': return '已关闭'
      default: return '未知'
    }
  } else {
    switch (props.status) {
      case 'pending': return '待认领'
      case 'claimed': return '已认领'
      case 'closed': return '已归档'
      default: return '未知'
    }
  }
})

// Element Plus tag type based on status
const tagType = computed(() => {
  switch (props.status) {
    case 'pending': return 'primary'
    case 'found':
    case 'claimed': return 'success'
    case 'closed': return 'info'
    default: return 'info'
  }
})

// Additional class based on status
const statusClass = computed(() => {
  return `status-${props.status}`
})
</script>

<style scoped>
.status-tag {
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-radius: 4px;
}

.status-pending {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}

.status-found,
.status-claimed {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
  border-color: var(--el-color-success-light-5);
}

.status-closed {
  background-color: var(--el-color-info-light-9);
  color: var(--el-color-info);
  border-color: var(--el-color-info-light-5);
}
</style>
