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

    async fetchFoundItems() {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get('/found-items', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize,
            ...this.filters
          }
        })

        this.items = response.data.items
        this.pagination.total = response.data.total
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取失物招领列表失败'
      } finally {
        this.loading = false
      }
    },

    async fetchFoundItemById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await apiClient.get(`/found-items/${id}`)
        this.currentItem = response.data

        // 同时获取评论
        await this.fetchComments(id)
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取失物招领详情失败'
      } finally {
        this.loading = false
      }
    },

    async fetchComments(itemId: number) {
      try {
        const response = await apiClient.get(`/found-items/${itemId}/comments`)
        this.comments = response.data
      } catch (error: any) {
        console.error('获取评论失败', error)
        // 评论获取失败不影响主流程，只记录错误
      }
    },

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
        // 更新列表（可选，取决于后端返回格式）
        if (response.data.item) {
          this.items.unshift(response.data.item)
        }
        return { success: true, id: response.data.id }
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

    async updateFoundItem(id: number, itemData: Partial<FoundItem>) {
      if (!id) return { success: false, message: '无效的ID' }

      this.loading = true
      this.error = null

      try {
        const response = await apiClient.put(`/found-items/${id}`, itemData)

        // 更新当前查看的物品（如果是同一个）
        if (this.currentItem?.id === id) {
          this.currentItem = response.data.item || { ...this.currentItem, ...itemData }
        }

        // 更新列表中的物品
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = response.data.item || { ...this.items[itemIndex], ...itemData }
        }

        return { success: true }
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新失物招领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    },

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

        this.comments.unshift(response.data)
        return { success: true }
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

        // 更新状态
        if (this.currentItem?.id === id) {
          this.currentItem = response.data.item || {
            ...this.currentItem,
            status: 'claimed',
            updatedAt: new Date().toISOString()
          }
        }

        // 更新列表中的物品状态
        const itemIndex = this.items.findIndex(item => item.id === id)
        if (itemIndex !== -1) {
          this.items[itemIndex] = response.data.item || {
            ...this.items[itemIndex],
            status: 'claimed',
            updatedAt: new Date().toISOString()
          }
        }

        return { success: true, message: response.data.message || '认领申请已提交，等待物品主人确认' }
      } catch (error: any) {
        this.error = error.response?.data?.message || '认领失败'
        return { success: false, message: this.error }
      } finally {
        this.loading = false
      }
    }
  },
})
