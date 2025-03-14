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

      try {
        const response = await apiClient.post('/auth/login', { username, password })
        const { token, user } = response.data
        this.setToken(token)
        this.setUser(user as User)

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '登录失败，请检查用户名和密码'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async register(userData: { username: string, email: string, password: string }) {
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
      if (!this.token) return

      this.loading = true

      try {
        const response = await apiClient.get('/users/me')
        this.setUser(response.data as User)
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取用户信息失败'
        this.logout()
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
