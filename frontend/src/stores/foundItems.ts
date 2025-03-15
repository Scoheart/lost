// stores/foundItems.ts
import { defineStore } from 'pinia'
import apiClient from '@/utils/api'
import type { Comment } from '@/stores/lostItems'

export interface FoundItem {
  id: number
  title: string
  description: string
  foundDate: string
  foundLocation: string
  category: string
  images: string[]
  contactInfo: string
  status: 'pending' | 'claimed' | 'closed'
  userId: number
  username: string
  createdAt: string
  updatedAt: string
}

export interface FoundItemsState {
  items: FoundItem[]
  currentItem: FoundItem | null
  comments: Comment[]
  loading: boolean
  error: string | null
  filters: {
    category: string | null
    status: string | null
    keyword: string | null
  }
  pagination: {
    page: number
    pageSize: number
    total: number
  }
}

export const useFoundItemsStore = defineStore('foundItems', {
  state: (): FoundItemsState => ({
    items: [],
    currentItem: null,
    comments: [],
    loading: false,
    error: null,
    filters: {
      category: null,
      status: null,
      keyword: null,
    },
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0,
    },
  }),

  getters: {
    filteredItems: (state) => {
      let result = [...state.items]

      if (state.filters.category) {
        result = result.filter((item) => item.category === state.filters.category)
      }

      if (state.filters.status) {
        result = result.filter((item) => item.status === state.filters.status)
      }

      if (state.filters.keyword) {
        const keyword = state.filters.keyword.toLowerCase()
        result = result.filter(
          (item) =>
            item.title.toLowerCase().includes(keyword) ||
            item.description.toLowerCase().includes(keyword) ||
            item.foundLocation.toLowerCase().includes(keyword),
        )
      }

      return result
    },

    paginatedItems: (state) => {
      const start = (state.pagination.page - 1) * state.pagination.pageSize
      const end = start + state.pagination.pageSize
      return state.items.slice(start, end)
    },
  },

  actions: {
    setFilters(filters: Partial<FoundItemsState['filters']>) {
      this.filters = { ...this.filters, ...filters }
    },

    // GET /found-items - 查询失物招领列表
    async fetchFoundItems({ page = 1, pageSize = 10, status = '', category = '', query = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)
        if (category) queryParams.append('category', category)
        if (query) queryParams.append('query', query)

        const queryString = queryParams.toString()
        const url = queryString ? `/found-items?${queryString}` : '/found-items'

        const response = await apiClient.get(url)

        // 根据API响应格式处理数据
        if (response.data.data?.items) {
          // 新API格式：response.data.data.items包含物品数组
          this.items = response.data.data.items
          this.pagination.total = response.data.data.totalItems || 0
          this.pagination.page = response.data.data.currentPage || page
          this.pagination.pageSize = response.data.data.pageSize || pageSize
        } else if (response.data.items) {
          // 旧格式：response.data.items包含物品数组
          this.items = response.data.items
        } else if (Array.isArray(response.data.data)) {
          // 直接数组格式
          this.items = response.data.data
        } else if (Array.isArray(response.data)) {
          // 最直接的数组格式
          this.items = response.data
        } else {
          // 无法识别的格式，确保items是数组
          console.warn('API响应格式不符合预期，无法获取物品列表', response.data)
          this.items = []
        }

        return {
          success: true,
          message: '获取失物招领列表成功',
          data: this.items
        }
      } catch (error) {
        console.error('获取失物招领列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取失物招领列表失败',
          error
        }
      }
    },

    // GET /found-items/{id} - 查询单个失物招领
    async fetchFoundItemById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/found-items/${id}`)
        this.currentItem = response.data.data || response.data

        // 同时获取评论
        await this.fetchComments(id)

        return { success: true, data: this.currentItem }
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取失物招领详情失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // GET /found-items/mine - 查询当前用户的失物招领
    async fetchMyFoundItems({ page = 1, pageSize = 10, status = '' } = {}) {
      try {
        const queryParams = new URLSearchParams()
        if (page) queryParams.append('page', page.toString())
        if (pageSize) queryParams.append('pageSize', pageSize.toString())
        if (status) queryParams.append('status', status)

        const queryString = queryParams.toString()
        const url = queryString ? `/found-items/mine?${queryString}` : '/found-items/mine'

        const response = await apiClient.get(url)

        // 根据API响应格式处理数据
        if (response.data.data?.items) {
          // 新API格式：response.data.data.items包含物品数组
          this.items = response.data.data.items
          this.pagination.total = response.data.data.totalItems || 0
          this.pagination.page = response.data.data.currentPage || page
          this.pagination.pageSize = response.data.data.pageSize || pageSize
        } else if (response.data.items) {
          // 旧格式：response.data.items包含物品数组
          this.items = response.data.items
        } else if (Array.isArray(response.data.data)) {
          // 直接数组格式
          this.items = response.data.data
        } else if (Array.isArray(response.data)) {
          // 最直接的数组格式
          this.items = response.data
        } else {
          // 无法识别的格式，确保items是数组
          console.warn('API响应格式不符合预期，无法获取物品列表', response.data)
          this.items = []
        }

        return {
          success: true,
          message: '获取我的失物招领列表成功',
          data: this.items
        }
      } catch (error) {
        console.error('获取我的失物招领列表失败:', error)
        this.items = []
        return {
          success: false,
          message: '获取我的失物招领列表失败',
          error
        }
      }
    },

    async fetchComments(itemId: number) {
      try {
        const response = await apiClient.get(`/found-items/${itemId}/comments`)
        this.comments = response.data.data || response.data || []
        return { success: true, data: this.comments }
      } catch (error: any) {
        console.error('获取评论失败', error)
        return { success: false, message: error.response?.data?.message || '获取评论失败' }
      }
    },

    // POST /found-items - 发布失物招领
    async createFoundItem(
      itemData: Omit<
        FoundItem,
        'id' | 'userId' | 'username' | 'status' | 'createdAt' | 'updatedAt'
      >,
    ) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post('/found-items', itemData)
        const newItem = response.data.item || response.data.data || response.data

        // 更新列表（可选，取决于后端返回格式）
        if (newItem) {
          this.items.unshift(newItem)
        }

        return {
          success: true,
          id: newItem.id || response.data.id,
          data: newItem
        }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // PUT /found-items/{id} - 更新失物招领
    async updateFoundItem(id: number, itemData: Partial<FoundItem>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        const response = await apiClient.put(`/found-items/${id}`, itemData)
        const updatedItem = response.data.item || response.data.data || response.data

        // 更新当前查看的物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = updatedItem || { ...this.currentItem, ...itemData }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = updatedItem || { ...this.items[itemIndex], ...itemData }
        }

        return { success: true, data: updatedItem }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    // DELETE /found-items/{id} - 删除失物招领
    async deleteFoundItem(id: number) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        await apiClient.delete(`/found-items/${id}`)

        // 从列表中移除
        this.items = this.items.filter(item => item.id !== id)

        // 清除当前物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = null
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async addComment(comment: { content: string; itemId: number }) {
      try {
        const response = await apiClient.post(`/found-items/${comment.itemId}/comments`, {
          content: comment.content
        })

        const newComment = response.data.data || response.data
        this.comments.unshift(newComment)

        return { success: true, data: newComment }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '发表评论失败'
        }
      }
    },

    async claimItem(id: number, claimData: { description: string }) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.post(`/found-items/${id}/claim`, claimData)
        const updatedItem = response.data.item || response.data.data || response.data

        // 更新状态
        if (this.currentItem?.id === id) {
          this.currentItem = updatedItem || {
            ...this.currentItem,
            status: 'claimed'
          }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = updatedItem || { ...this.items[itemIndex], status: 'claimed' }
        }

        return { success: true, data: updatedItem }
      } catch (error: any) {
        this.error = error.response?.data?.message || '认领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    }
  },
})
