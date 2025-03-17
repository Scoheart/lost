// 统一的物品分类常量
export const ITEM_CATEGORIES = [
  { value: 'electronics', label: '电子设备' },
  { value: 'documents', label: '证件/文件' },
  { value: 'clothing', label: '衣物/包包' },
  { value: 'keys', label: '钥匙/门禁卡' },
  { value: 'jewelry', label: '首饰/配饰' },
  { value: 'books', label: '书籍/教材' },
  { value: 'pets', label: '宠物' },
  { value: 'other', label: '其他物品' },
]

// 寻物启事状态常量
export const LOST_ITEM_STATUS = [
  { value: 'pending', label: '寻找中' },
]

// 失物招领状态常量
export const FOUND_ITEM_STATUS = [
  { value: 'pending', label: '待认领' },
  { value: 'processing', label: '认领中' },
  { value: 'claimed', label: '已认领' },
]

// 通用方法，根据值获取标签
export const getCategoryLabel = (category: string | null) => {
  // 尝试在类别映射中查找
  const categoryObj = ITEM_CATEGORIES.find((c) => c.value === category)
  if (categoryObj) {
    return categoryObj.label
  }

  // 如果找不到，返回原始值或默认值
  return category || '其他物品'
}

// 获取寻物启事状态标签
export const getLostItemStatusLabel = (status: string | null) => {
  const statusObj = LOST_ITEM_STATUS.find((s) => s.value === status)
  if (statusObj) {
    return statusObj.label
  }
  return status || '未知状态'
}

// 获取失物招领状态标签
export const getFoundItemStatusLabel = (status: string | null) => {
  const statusObj = FOUND_ITEM_STATUS.find((s) => s.value === status)
  if (statusObj) {
    return statusObj.label
  }
  return status || '未知状态'
}
