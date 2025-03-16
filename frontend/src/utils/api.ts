import axios from 'axios'
import { useUserStore } from '@/stores/user'

// 获取环境变量中的配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const API_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT) || 10000
const IS_DEV = import.meta.env.MODE === 'development' || import.meta.env.VITE_DEBUG === 'true'

// 创建axios实例，基于环境配置
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 添加请求拦截器处理认证令牌
apiClient.interceptors.request.use(
  (config) => {
    // 开发环境下记录请求日志
    if (IS_DEV) {
      console.log(`API Request (${import.meta.env.MODE}): ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config)
    }

    // 获取并添加认证令牌
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器处理常见错误
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error

    // 开发环境下记录错误
    if (IS_DEV && error.response) {
      console.error(`API Error (${import.meta.env.MODE}): ${error.response.status}`, error.response.data)
    }

    // 处理认证错误 (401)
    if (response && response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      // 如果需要，可以在这里添加重定向到登录页面的逻辑
    }

    return Promise.reject(error)
  }
)

export default apiClient
