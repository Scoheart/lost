import { format, parseISO, isValid } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * Format a date string into a user-friendly format
 * @param dateString Date string from the backend
 * @param includeTime Whether to include time in the formatted string
 * @returns Formatted date string
 */
export const formatDate = (dateString: string | null | undefined, includeTime = false): string => {
  if (!dateString) return ''

  try {
    const date = typeof dateString === 'string'
      ? parseISO(dateString)
      : new Date(dateString)

    if (!isValid(date) || date.getFullYear() === 1970) {
      return ''
    }

    return includeTime
      ? format(date, 'yyyy年MM月dd日 HH:mm:ss', { locale: zhCN })
      : format(date, 'yyyy年MM月dd日', { locale: zhCN })
  } catch (error) {
    console.error('Error formatting date:', dateString, error)
    return ''
  }
}

/**
 * Format a date for API submission (ISO format)
 * @param date Date object or date string
 * @returns ISO formatted date string
 */
export const formatDateForApi = (date: Date | string | null | undefined): string => {
  if (!date) return ''

  try {
    const dateObj = typeof date === 'string' ? new Date(date) : date
    if (!isValid(dateObj)) return ''
    return dateObj.toISOString().split('T')[0] // YYYY-MM-DD format
  } catch (error) {
    console.error('Error formatting date for API:', date, error)
    return ''
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
