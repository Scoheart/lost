import axios from 'axios'
import { useUserStore } from '@/stores/user'

// Create axios instance with base URL
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add request interceptor to include auth token
apiClient.interceptors.request.use(
  (config) => {
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

// Add response interceptor to handle common errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error

    if (response && response.status === 401) {
      // Handle unauthorized (e.g., token expired)
      const userStore = useUserStore()
      userStore.logout()
      // Redirect to login could be handled here or in the component
    }

    return Promise.reject(error)
  }
)

export default apiClient
