import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 格式化日期
 * @param dateString 日期字符串
 * @param full 是否显示完整时间（带时分秒）
 * @returns 格式化后的日期字符串
 */
export function formatDate(dateString: string | undefined | null, full = false): string {
  if (!dateString) return '-'

  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '-'

    if (full) {
      return format(date, 'yyyy-MM-dd HH:mm:ss', { locale: zhCN })
    }
    return format(date, 'yyyy-MM-dd', { locale: zhCN })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}

/**
 * 格式化日期时间
 * @param dateString 日期字符串
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(dateString: string | undefined | null): string {
  return formatDate(dateString, true)
}

/**
 * 获取相对时间（如：1小时前，2天前）
 * @param dateString 日期字符串
 * @returns 相对时间字符串
 */
export function getRelativeTime(dateString: string | undefined | null): string {
  if (!dateString) return '-'

  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '-'

    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffSeconds = Math.floor(diffMs / 1000)
    const diffMinutes = Math.floor(diffSeconds / 60)
    const diffHours = Math.floor(diffMinutes / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffSeconds < 60) {
      return '刚刚'
    } else if (diffMinutes < 60) {
      return `${diffMinutes}分钟前`
    } else if (diffHours < 24) {
      return `${diffHours}小时前`
    } else if (diffDays < 30) {
      return `${diffDays}天前`
    } else {
      return formatDate(dateString)
    }
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}
