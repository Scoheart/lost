// stores/user.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface User {
  id: number
  username: string
  email: string
  role: 'resident' | 'admin' | 'sysadmin'
  avatar?: string
  createdAt: string
  updatedAt: string
}

export interface UserState {
  user: User | null
  token: string | null
  loading: boolean
  error: string | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    token: localStorage.getItem('token'),
    loading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    isAdmin: (state) => state.user?.role === 'admin' || state.user?.role === 'sysadmin',
    isSysAdmin: (state) => state.user?.role === 'sysadmin',
    userProfile: (state) => state.user
  },

  actions: {
    async login(username: string, password: string) {
      this.loading = true
      this.error = null
      console.log('Login attempt for user:', username)

      try {
        const response = await apiClient.post('/auth/login', { username, password })
        console.log('Login response:', response.data)

        // 正确获取响应中的数据
        if (response.data.success) {
          const responseData = response.data.data
          const token = responseData.token

          // 构建用户对象
          const user: User = {
            id: responseData.id,
            username: responseData.username,
            email: responseData.email,
            role: responseData.role,
            avatar: responseData.avatar,
            createdAt: responseData.createdAt || new Date().toISOString(),
            updatedAt: responseData.updatedAt || new Date().toISOString()
          }

          console.log('Extracted token and user data:', { token: !!token, user })

          // 设置token和用户信息
          this.token = token
          localStorage.setItem('token', token)
          this.user = user

          console.log('Authentication state after login:',
            'token set:', !!this.token,
            'user set:', !!this.user,
            'isAuthenticated:', !!this.token && !!this.user
          )

          return { success: true }
        } else {
          this.error = response.data.message || '登录失败'
          return { success: false, message: this.error }
        }
      } catch (error: any) {
        this.error = error.response?.data?.message || '登录失败，请检查用户名和密码'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async register(userData: { username: string, email: string, password: string, phone?: string }) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/auth/register', userData)
        return { success: true, message: response.data.message || '注册成功，请登录' }
      } catch (error: any) {
        this.error = error.response?.data?.message || '注册失败，请稍后再试'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
    },

    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUser(user: User) {
      this.user = user
    },

    async fetchCurrentUser() {
      if (!this.token) {
        console.log('fetchCurrentUser: No token available, skipping')
        return
      }

      console.log('fetchCurrentUser: Fetching user data with token')
      this.loading = true

      try {
        const response = await apiClient.get('/users/me')
        console.log('fetchCurrentUser response:', response.data)

        if (response.data.success) {
          const userData = response.data.data

          const user: User = {
            id: userData.id,
            username: userData.username,
            email: userData.email,
            role: userData.role,
            avatar: userData.avatar,
            createdAt: userData.createdAt || new Date().toISOString(),
            updatedAt: userData.updatedAt || new Date().toISOString()
          }

          this.user = user
          console.log('fetchCurrentUser: Successfully set user data')
          return { success: true }
        } else {
          throw new Error(response.data.message || '获取用户信息失败')
        }
      } catch (error: any) {
        console.error('fetchCurrentUser error:', error)
        this.error = error.response?.data?.message || error.message || '获取用户信息失败'
        this.logout()
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async updateProfile(profileData: Partial<User>) {
      if (!this.token || !this.user) return { success: false, message: '未登录' }

      this.loading = true

      try {
        const response = await apiClient.put('/users/profile', profileData)
        this.setUser(response.data as User)
        return { success: true, message: response.data.message || '个人信息更新成功' }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新个人信息失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async changePassword(passwords: { oldPassword: string, newPassword: string }) {
      if (!this.token) return { success: false, message: '未登录' }

      this.loading = true

      try {
        const response = await apiClient.put('/users/change-password', passwords)
        return { success: true, message: response.data.message || '密码修改成功' }
      } catch (error: any) {
        this.error = error.response?.data?.message || '密码修改失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    }
  }
})
