// API 响应处理帮助工具
import type { Comment, CommentPagination } from '@/stores/lostItems'

/**
 * 从API响应中提取数据，适应不同的响应格式
 * @param response API响应对象
 * @param dataField 可选的数据字段名称
 * @returns 从响应中提取的数据
 */
export function extractDataFromResponse(response: any, dataField?: string) {
  if (dataField && response.data[dataField]) {
    return response.data[dataField]
  }

  // 尝试不同的常见响应格式
  return response.data.data || response.data.item || response.data
}

/**
 * 处理API错误
 * @param error 捕获的错误对象
 * @param defaultMessage 默认错误消息
 * @returns 格式化的错误对象
 */
export function handleApiError(error: any, defaultMessage: string) {
  console.error(`API Error: ${defaultMessage}`, error)
  const errorMessage = error.response?.data?.message || defaultMessage
  return { success: false, message: errorMessage }
}

/**
 * 处理评论响应数据
 * @param response API响应
 * @returns 处理后的评论数据和分页信息
 */
export function processCommentsResponse(response: any) {
  const responseData = response.data

  // 获取评论数据，确保始终为数组
  let comments: Comment[] = []
  if (responseData.comments) {
    comments = Array.isArray(responseData.comments) ? responseData.comments : []
  } else if (responseData.data && Array.isArray(responseData.data)) {
    comments = responseData.data
  } else if (Array.isArray(responseData)) {
    comments = responseData
  }

  // 获取分页信息
  const pagination: CommentPagination = {
    currentPage: responseData.currentPage || 1,
    pageSize: responseData.pageSize || 10,
    totalItems: responseData.totalItems || comments.length,
    totalPages: responseData.totalPages || 1
  }

  return { comments, pagination }
}

/**
 * 确定API端点是否应该使用状态更新专用URL
 * @param itemData 要更新的数据对象
 * @returns 是否应使用状态更新专用URL
 */
export function shouldUseStatusEndpoint(itemData: any): boolean {
  return itemData &&
         Object.keys(itemData).length === 1 &&
         'status' in itemData
}

/**
 * 生成适当的API端点URL
 * @param baseEndpoint 基础端点路径（如 '/lost-items' 或 '/found-items'）
 * @param id 项目ID
 * @param itemData 要更新的数据
 * @returns 应使用的API端点URL
 */
export function getUpdateEndpoint(baseEndpoint: string, id: number, itemData?: any): string {
  if (itemData && shouldUseStatusEndpoint(itemData)) {
    return `${baseEndpoint}/${id}/status`
  }

  return `${baseEndpoint}/${id}`
}
