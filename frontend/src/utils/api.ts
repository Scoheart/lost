import axios from 'axios'
import { useUserStore } from '@/stores/user'

// Get environment variables
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const API_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT) || 10000

// Create axios instance with environment-based configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add request interceptor to include auth token
apiClient.interceptors.request.use(
  (config) => {
    // For debugging in development environment
    if (import.meta.env.DEV) {
      console.log(`API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config)
    }

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

    // Additional error logging in development
    if (import.meta.env.DEV && error.response) {
      console.error(`API Error: ${error.response.status}`, error.response.data)
    }

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
