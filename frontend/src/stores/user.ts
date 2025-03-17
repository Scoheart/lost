// stores/user.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'

export interface User {
  id: number
  username: string
  email: string
  role: 'resident' | 'admin' | 'sysadmin'
  avatar?: string
  phone?: string
  realName?: string
  address?: string
  createdAt: string
  updatedAt: string
}

// API操作结果通用接口
export interface ApiResult {
  success: boolean
  message?: string
  pending?: boolean
  data?: any
}

// 处理中状态的结果
export interface ApiPendingResult extends ApiResult {
  success: false
  pending: true
}

export interface UserState {
  user: User | null
  token: string | null
  loading: boolean
  error: string | undefined
  lastFetch: number | null
  initialized: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    token: localStorage.getItem('token'),
    loading: false,
    error: undefined,
    lastFetch: null, // 记录上次获取用户数据的时间戳
    initialized: false // 标记用户状态是否已初始化
  }),

  getters: {
    isAuthenticated: (state) => {
      return !!state.token && !!state.user;
    },
    isAdmin: (state) => {
      return state.user?.role === 'admin' || state.user?.role === 'sysadmin'
    },
    isSysAdmin: (state) => {
      return state.user?.role === 'sysadmin'
    },
    // 添加新的getter: 判断用户是否是小区管理员（不包括系统管理员）
    isCommunityAdmin: (state) => {
      return state.user?.role === 'admin'
    },
    userProfile: (state) => state.user,
    // 判断是否需要重新获取用户数据（5分钟过期）
    shouldRefreshUserData: (state) => {
      if (!state.lastFetch) return true
      const fiveMinutes = 5 * 60 * 1000
      return Date.now() - state.lastFetch > fiveMinutes
    }
  },

  actions: {
    /**
     * 管理员登录专用方法
     */
    async adminLogin(username: string, password: string): Promise<ApiResult> {
      this.loading = true
      this.error = undefined
      console.log('[UserStore] Admin login attempt for user:', username)

      try {
        // 使用相同的登录接口，但添加admin标志
        const response = await apiClient.post('/auth/login', {
          username,
          password,
          adminLogin: true // 添加标志表明这是管理员登录
        })

        console.log('[UserStore] Admin login response received')

        if (response.data.success) {
          const responseData = response.data.data
          const token = responseData.token
          const role = responseData.role

          // 验证角色是否为管理员或系统管理员
          if (role !== 'admin' && role !== 'sysadmin') {
            this.error = '您的账号没有管理员权限'
            console.log('[UserStore] Login failed: Not an admin account')
            return { success: false, message: this.error }
          }

          // 构建用户对象
          const user: User = {
            id: responseData.id,
            username: responseData.username,
            email: responseData.email,
            role: responseData.role,
            avatar: responseData.avatar,
            phone: responseData.phone,
            realName: responseData.realName,
            address: responseData.address,
            createdAt: responseData.createdAt || new Date().toISOString(),
            updatedAt: responseData.updatedAt || new Date().toISOString()
          }

          // 设置token和用户信息
          this.setToken(token)
          this.setUser(user)
          console.log('[UserStore] Admin login successful, user data and token set. Role:', user.role)

          return { success: true }
        } else {
          this.error = response.data.message || '登录失败'
          console.log('[UserStore] Admin login failed:', this.error)
          return { success: false, message: this.error }
        }
      } catch (error: any) {
        this.handleApiError(error, '登录失败，请检查账号和密码')
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户登录
     */
    async login(username: string, password: string): Promise<ApiResult> {
      this.loading = true
      this.error = undefined
      console.log('[UserStore] Login attempt for user:', username)

      try {
        const response = await apiClient.post('/auth/login', { username, password })
        console.log('[UserStore] Login response received')

        if (response.data.success) {
          const responseData = response.data.data
          const token = responseData.token
          const role = responseData.role

          // 验证角色是否为居民用户
          if (role !== 'resident') {
            this.error = '管理员账号请使用管理员登录入口'
            console.log('[UserStore] Login failed: Not a resident account')
            return { success: false, message: this.error }
          }

          // 构建用户对象
          const user: User = {
            id: responseData.id,
            username: responseData.username,
            email: responseData.email,
            role: responseData.role,
            avatar: responseData.avatar,
            phone: responseData.phone,
            realName: responseData.realName,
            address: responseData.address,
            createdAt: responseData.createdAt || new Date().toISOString(),
            updatedAt: responseData.updatedAt || new Date().toISOString()
          }

          // 设置token和用户信息
          this.setToken(token)
          this.setUser(user)
          console.log('[UserStore] Login successful, user data and token set')

          return { success: true }
        } else {
          this.error = response.data.message || '登录失败，请检查用户名和密码'
          console.log('[UserStore] Login failed:', this.error)
          return { success: false, message: this.error }
        }
      } catch (error: any) {
        this.handleApiError(error, '登录失败，请稍后再试')
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户注册
     */
    async register(userData: {
      username: string,
      password: string,
      realName: string,
      address: string,
      phone?: string
    }): Promise<ApiResult> {
      this.loading = true
      this.error = undefined
      console.log('[UserStore] Register attempt')

      try {
        const response = await apiClient.post('/auth/register', userData)
        console.log('[UserStore] Registration successful')
        return { success: true, message: response.data.message || '注册成功，请登录' }
      } catch (error: any) {
        this.handleApiError(error, '注册失败，请稍后再试')
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 初始化用户状态
     * 在应用启动时调用，验证token并加载用户数据
     */
    async initialize(): Promise<boolean> {
      // 如果已经初始化，跳过
      if (this.initialized) {
        return this.isAuthenticated;
      }

      console.log('[UserStore] Initializing user state');

      // 如果没有token，标记为已初始化并返回
      if (!this.token) {
        console.log('[UserStore] No token found during initialization');
        this.initialized = true;
        return false;
      }

      try {
        // 尝试获取用户信息
        const result = await this.fetchCurrentUser();
        this.initialized = true;
        return result.success;
      } catch (error) {
        console.error('[UserStore] Initialization error:', error);
        this.initialized = true;
        return false;
      }
    },

    /**
     * 获取当前用户信息
     */
    async fetchCurrentUser(): Promise<ApiResult> {
      // 没有令牌，跳过请求
      if (!this.token) {
        console.log('[UserStore] fetchCurrentUser: No token available')
        return { success: false, message: '未登录' }
      }

      // 已有用户信息且未过期，跳过请求
      if (this.user && !this.shouldRefreshUserData) {
        console.log('[UserStore] fetchCurrentUser: Using cached user data')
        return { success: true }
      }

      // 已在加载中，防止重复请求
      if (this.loading) {
        console.log('[UserStore] fetchCurrentUser: Request already in progress')
        return { success: false, pending: true } as ApiPendingResult
      }

      console.log('[UserStore] fetchCurrentUser: Fetching user data')
      this.loading = true

      try {
        const response = await apiClient.get('/users/me')

        if (response.data.success) {
          const userData = response.data.data

          const user: User = {
            id: userData.id,
            username: userData.username,
            email: userData.email,
            role: userData.role,
            avatar: userData.avatar,
            phone: userData.phone,
            realName: userData.realName,
            address: userData.address,
            createdAt: userData.createdAt || new Date().toISOString(),
            updatedAt: userData.updatedAt || new Date().toISOString()
          }

          this.setUser(user)
          console.log('[UserStore] fetchCurrentUser: User data updated')
          return { success: true }
        } else {
          console.error('[UserStore] fetchCurrentUser: API returned error:', response.data.message)
          // 处理令牌失效的情况
          this.clearUserData()
          throw new Error(response.data.message || '获取用户信息失败')
        }
      } catch (error: any) {
        this.handleApiError(error, '获取用户信息失败')

        // 令牌无效或过期，清除本地数据
        if (error.response?.status === 401 || error.response?.status === 403) {
          console.log('[UserStore] fetchCurrentUser: Invalid token, logging out')
          this.clearUserData()
        }

        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 清除用户数据（保持登出逻辑不变，但提取公共逻辑到一个单独的方法）
     */
    clearUserData(): void {
      this.user = null
      this.token = null
      this.lastFetch = null
      localStorage.removeItem('token')
    },

    /**
     * 退出登录
     */
    logout(): void {
      console.log('[UserStore] Logging out user')
      this.clearUserData()
    },

    /**
     * 设置令牌
     */
    setToken(token: string): void {
      this.token = token
      localStorage.setItem('token', token)
    },

    /**
     * 设置用户信息
     */
    setUser(user: User): void {
      this.user = user
      this.lastFetch = Date.now()
    },

    /**
     * 更新用户资料
     */
    async updateProfile(profileData: Partial<User>): Promise<ApiResult> {
      if (!this.token || !this.user) {
        console.log('[UserStore] updateProfile: No token or user data')
        return { success: false, message: '未登录' }
      }

      this.loading = true
      console.log('[UserStore] updateProfile: Updating user profile', profileData)

      try {
        const response = await apiClient.put('/users/profile', profileData)

        // If the response contains updated user data, update the store
        if (response.data?.data) {
          this.setUser({
            ...this.user,
            ...response.data.data
          })
        } else if (response.data) {
          this.setUser({
            ...this.user,
            ...profileData
          })
        }

        console.log('[UserStore] updateProfile: Profile updated successfully')
        return {
          success: true,
          message: response.data.message || '个人信息更新成功',
          data: response.data.data || this.user
        }
      } catch (error: any) {
        this.handleApiError(error, '更新个人信息失败')
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 修改密码
     */
    async changePassword(passwords: { oldPassword: string, newPassword: string }): Promise<ApiResult> {
      if (!this.token) {
        console.log('[UserStore] changePassword: No token available')
        return { success: false, message: '未登录' }
      }

      this.loading = true
      console.log('[UserStore] changePassword: Changing password')

      try {
        const response = await apiClient.put('/users/change-password', passwords)
        console.log('[UserStore] changePassword: Password changed successfully')
        return { success: true, message: response.data.message || '密码修改成功' }
      } catch (error: any) {
        this.handleApiError(error, '密码修改失败')
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    /**
     * 统一处理API错误
     */
    handleApiError(error: any, defaultMessage: string): void {
      console.error('[UserStore] API Error:', error)

      if (error.response?.data?.message) {
        this.error = error.response.data.message
      } else if (error.message) {
        this.error = error.message
      } else {
        this.error = defaultMessage
      }
    }
  }
})
