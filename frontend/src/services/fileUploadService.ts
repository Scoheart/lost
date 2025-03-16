import axios from 'axios'
import { useUserStore } from '@/stores/user'

// 环境变量
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const IS_DEV = import.meta.env.MODE === 'development' || import.meta.env.VITE_DEBUG === 'true'
const MAX_UPLOAD_SIZE = Number(import.meta.env.VITE_MAX_UPLOAD_SIZE) || 5 // MB

/**
 * 文件上传服务
 * 处理所有的文件上传和头像上传
 */
export default {
  /**
   * 处理和规范化文件URL
   * 解决本地开发和生产环境中的路径问题，确保URL在不同环境中都能正确访问
   * @param url 原始文件URL
   * @returns 处理后的URL
   */
  normalizeFileUrl(url: string): string {
    if (!url) return ''

    // 记录开发环境日志
    if (IS_DEV) {
      console.log('正在处理文件URL:', url)
    }

    // 如果已经是完整的HTTP/HTTPS URL，进一步处理特殊情况
    if (url.startsWith('http://') || url.startsWith('https://')) {
      // 特殊情况：云服务器上的图片URL可能包含本地文件系统路径
      // 例如 http://121.40.52.9/api/home/laf/be/uploads/avatars/xxx.png
      if (url.includes('/home/laf/be/uploads/')) {
        const match = url.match(/\/home\/laf\/be\/uploads\/(.+)/)
        if (match && match[1]) {
          // 提取服务器地址部分
          const serverUrlMatch = url.match(/(https?:\/\/[^\/]+)/)
          const serverUrl = serverUrlMatch ? serverUrlMatch[1] : ''
          // 构造修正后的URL
          const normalizedUrl = `${serverUrl}/api/uploads/${match[1]}`
          if (IS_DEV) console.log('规范化后的URL:', normalizedUrl)
          return normalizedUrl
        }
      }
      return url
    }

    // 处理相对路径
    // 例如：uploads/avatars/xxx.jpg 或 /uploads/avatars/xxx.jpg
    if (url.includes('uploads/') || url.includes('/uploads/')) {
      const baseUrl = API_BASE_URL
      // 提取uploads后面的部分
      const match = url.match(/uploads\/(.+)/)
      if (match && match[1]) {
        const normalizedUrl = `${baseUrl}/uploads/${match[1]}`
        if (IS_DEV) console.log('规范化后的URL:', normalizedUrl)
        return normalizedUrl
      }
    }

    return url
  },

  /**
   * 通用文件上传方法
   * @param file 要上传的文件
   * @param type 文件类型（例如：'avatar', 'item-image'）
   * @returns 上传结果
   */
  async uploadFile(file: File, type: string = 'general') {
    const userStore = useUserStore()

    try {
      // 验证文件
      if (!this.validateFile(file)) {
        throw new Error('文件验证失败')
      }

      const formData = new FormData()
      formData.append('file', file)
      formData.append('type', type)

      const headers: Record<string, string> = {
        'Content-Type': 'multipart/form-data'
      }

      // 如果用户已登录，添加认证令牌
      if (userStore.token) {
        headers['Authorization'] = `Bearer ${userStore.token}`
      }

      if (IS_DEV) {
        console.log(`开始上传文件: ${file.name}, 类型: ${type}, 大小: ${(file.size / 1024).toFixed(2)}KB`)
      }

      const response = await axios.post(`${API_BASE_URL}/upload`, formData, { headers })

      // 处理响应中的文件URL
      if (response.data && response.data.data && response.data.data.url) {
        response.data.data.url = this.normalizeFileUrl(response.data.data.url)
      }

      if (IS_DEV) {
        console.log('文件上传成功:', response.data)
      }

      return response.data
    } catch (error) {
      console.error('文件上传失败:', error)
      throw error
    }
  },

  /**
   * 上传用户头像
   * @param file 头像文件
   * @returns 上传结果
   */
  async uploadAvatar(file: File) {
    const userStore = useUserStore()

    if (!userStore.isAuthenticated) {
      throw new Error('用户未登录，无法上传头像')
    }

    try {
      // 验证文件
      if (!this.validateFile(file, {
        maxSize: 2, // 头像限制2MB
        allowedTypes: ['image/jpeg', 'image/png', 'image/gif']
      })) {
        throw new Error('头像文件验证失败')
      }

      const formData = new FormData()
      formData.append('file', file)

      const headers: Record<string, string> = {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${userStore.token}`
      }

      if (IS_DEV) {
        console.log(`开始上传头像: ${file.name}, 大小: ${(file.size / 1024).toFixed(2)}KB`)
      }

      const response = await axios.post(`${API_BASE_URL}/upload/avatar`, formData, { headers })

      // 处理响应中的文件URL
      if (response.data && response.data.data && response.data.data.url) {
        response.data.data.url = this.normalizeFileUrl(response.data.data.url)

        // 更新用户头像URL
        if (userStore.user) {
          userStore.user.avatar = response.data.data.url
        }
      }

      if (IS_DEV) {
        console.log('头像上传成功:', response.data)
      }

      return response.data
    } catch (error) {
      console.error('头像上传失败:', error)
      throw error
    }
  },

  /**
   * 验证文件大小和类型
   * @param file 要验证的文件
   * @param options 验证选项
   * @returns 是否通过验证
   */
  validateFile(file: File, options: {
    maxSize?: number, // 单位MB
    allowedTypes?: string[],
    showMessage?: boolean
  } = {}): boolean {
    const {
      maxSize = MAX_UPLOAD_SIZE,
      allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
      showMessage = true
    } = options

    // 检查文件类型
    const isValidType = allowedTypes.includes(file.type)
    if (!isValidType && showMessage) {
      console.error(`不支持的文件类型: ${file.type}, 支持的类型: ${allowedTypes.join(', ')}`)
    }

    // 检查文件大小
    const isValidSize = file.size <= maxSize * 1024 * 1024
    if (!isValidSize && showMessage) {
      console.error(`文件过大: ${(file.size / (1024 * 1024)).toFixed(2)}MB, 最大限制: ${maxSize}MB`)
    }

    return isValidType && isValidSize
  }
}
