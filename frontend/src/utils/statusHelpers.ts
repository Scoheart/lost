// statusHelpers.ts - 处理物品状态的帮助工具

/**
 * 获取寻物启事状态的展示名称
 * @param status 寻物启事状态
 */
export function getLostStatusLabel(status: string): string {
  switch (status) {
    case 'pending':
      return '寻找中'
    case 'found':
      return '已找到'
    case 'closed':
      return '已关闭'
    default:
      return '未知'
  }
}

/**
 * 获取失物招领状态的展示名称
 * @param status 失物招领状态
 */
export function getFoundStatusLabel(status: string): string {
  switch (status) {
    case 'pending':
      return '招领中'
    case 'claimed':
      return '已认领'
    case 'closed':
      return '已关闭'
    default:
      return '未知'
  }
}

/**
 * 获取寻物启事状态对应的Element UI标签类型
 * @param status 寻物启事状态
 */
export function getLostStatusType(status: string): string {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'found':
      return 'success'
    case 'closed':
      return 'info'
    default:
      return 'info'
  }
}

/**
 * 获取失物招领状态对应的Element UI标签类型
 * @param status 失物招领状态
 */
export function getFoundStatusType(status: string): string {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'claimed':
      return 'success'
    case 'closed':
      return 'info'
    default:
      return 'info'
  }
}

/**
 * 获取寻物启事状态变更后的消息
 * @param newStatus 新状态
 */
export function getLostStatusChangeMessage(newStatus: string): string {
  switch (newStatus) {
    case 'found':
      return '物品已标记为已找到'
    case 'closed':
      return '寻物启事已关闭'
    default:
      return '状态已更新'
  }
}

/**
 * 获取失物招领状态变更后的消息
 * @param newStatus 新状态
 */
export function getFoundStatusChangeMessage(newStatus: string): string {
  switch (newStatus) {
    case 'claimed':
      return '物品已标记为已认领'
    case 'closed':
      return '失物招领已关闭'
    default:
      return '状态已更新'
  }
}
