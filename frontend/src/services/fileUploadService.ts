import axios from 'axios'
import { useUserStore } from '@/stores/user'

/**
 * 文件上传服务
 * 处理所有的文件上传和头像上传
 */
export default {
  /**
   * 通用文件上传方法
   * @param file 要上传的文件
   * @param type 文件类型（例如：'avatar', 'item-image'）
   * @returns 上传结果
   */
  async uploadFile(file: File, type: string = 'general') {
    const userStore = useUserStore()

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('type', type)

      const headers = {
        'Content-Type': 'multipart/form-data'
      }

      // 如果用户已登录，添加认证令牌
      if (userStore.token) {
        headers['Authorization'] = `Bearer ${userStore.token}`
      }

      const response = await axios.post('/api/upload', formData, { headers })
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
      const formData = new FormData()
      formData.append('file', file)

      const headers = {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${userStore.token}`
      }

      const response = await axios.post('/api/users/avatar', formData, { headers })
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
  } = {}) {
    const {
      maxSize = 5,
      allowedTypes = ['image/jpeg', 'image/png', 'image/gif'],
      showMessage = true
    } = options

    // 检查文件类型
    const isValidType = allowedTypes.includes(file.type)
    if (!isValidType && showMessage) {
      console.error('不支持的文件类型:', file.type)
    }

    // 检查文件大小
    const isValidSize = file.size <= maxSize * 1024 * 1024
    if (!isValidSize && showMessage) {
      console.error('文件过大:', file.size, '最大限制:', maxSize * 1024 * 1024)
    }

    return isValidType && isValidSize
  }
}
